//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.compiler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Timer;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOptions;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.OptionChangeEvent;
import net.sourceforge.waters.model.options.OptionChangeListener;
import net.sourceforge.waters.model.options.WatersOptionPages;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ProxySubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

/**
 * <P>The unit that controls background compilation in the IDE.</P>
 *
 * <P>Each module is associated with a background compiler, which is
 * stored in the {@link ModuleContainer} after the module is loaded.
 * The background compiler listens for changes to the module, and when
 * a change is detected, recompilation is initiated after expiry of a
 * timeout. After the timeout, the {@link ModuleCompiler} is started in
 * a separate thread of type {@link CompilationWorker}. When compilation
 * finishes, the {@link CompilationWorker} notifies the background compiler,
 * which records the compiled DES or the errors so that they are available
 * to the {@link ModuleContainer}.</P>
 *
 * <P>The background compiler includes support for large modules that take
 * a long time to compile. If the user switches tabs or requests verification
 * while the background compiler is running, a dialog ({@link
 * CompilationDialog}) is displayed to inform the user that the operation is
 * suspended until compilation completes. The user can dismiss the dialog
 * by clicking an Abort button, in which case they can use the editor again
 * while compilation continues.</P>
 *
 * <P>The automatic compilation can be disabled through the configuration
 * option. In that case, module changes
 * no longer a trigger a timer and the module does not get compiled
 * automatically. The compiler is still started when the user switches tabs or
 * requests verification, in which case the {@link CompilationDialog} is
 * displayed, and aborting it does not only dismiss the dialog but also
 * abort the compilation.</P>
 *
 * @author Tom Levy, Robi Malik
 */

public class BackgroundCompiler
  implements ModelObserver, ActionListener
{
  //#########################################################################
  //# Constructors
  public BackgroundCompiler(final ModuleContainer container)
  {
    mModuleContainer = container;
    final IDE ide = container.getIDE();
    final DocumentManager manager = ide.getDocumentManager();
    final ProductDESProxyFactory desfactory =
      ProductDESElementFactory.getInstance();
    mCompiler = new ModuleCompiler(manager, desfactory, null);
    mCompiler.setSourceInfoEnabled(true);
    mCompiler.setMultiExceptionsEnabled(true);
    mWorker = new CompilationWorker(this, mCompiler, container.getName());
    container.getModule().addModelObserver(this);

    mEnablementPropertyChangeListener = new OptionChangeListener() {
      @Override
      public void optionChanged(final OptionChangeEvent event)
      {
        setTimerEnabled(CompilerOptions.BACKGROUND_COMPILER.getValue());
      }
    };
    mEnablementPropertyChangeListener.optionChanged(null);
    CompilerOptions.BACKGROUND_COMPILER.
    addOptionChangeListener(mEnablementPropertyChangeListener);
    mCompilerPropertyChangeListener = new OptionChangeListener() {
      @Override
      public void optionChanged(final OptionChangeEvent event)
      {
        setModuleChanged();
      }
    };
    final List<Option<?>> options =
      mCompiler.getOptions(WatersOptionPages.COMPILER);
    mOptions = new ArrayList<>(options.size());
    for (final Option<?> option : options) {
      if (option.isEditable()) {
        mOptions.add(option);
        option.addOptionChangeListener(mCompilerPropertyChangeListener);
      }
    }
    mAbortButtonAction = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        removeObserver();
        abortNonBackgroundCompiler();
      }
    };
    mModuleChanged = true;
    if (CompilerOptions.BACKGROUND_COMPILER.getValue()) {
      compile(null);
    }
  }


  //#########################################################################
  //# Simple Access
  /**
   * Retrieves the {@link ProductDESProxy} from the compiler.
   * This method returns the latest available result of compilation.
   * @return The product DES from the last successful run of the
   *         {@link ModuleCompiler}, or <CODE>null</CODE>.
   */
  public ProductDESProxy getCompiledDES()
  {
    return mCompiledDES;
  }

  /**
   * Retrieves the source information map that associated objects in the
   * produced {@link ProductDESProxy} with locations in the compiled
   * {@link ModuleProxy}.
   * @return The source information map from the last successful run of the
   *         {@link ModuleCompiler}, or <CODE>null</CODE>.
   */
  public Map<Object, SourceInfo> getSourceInfoMap()
  {
    return mSourceInfoMap;
  }


  //#########################################################################
  //# Invocation
  /**
   * Ensures that the module is recompiled as soon as possible if it has
   * changed.
   * If the module has changed and the compiler is not already running,
   * compilation is started.
   * If the module has changed and the compiler is already running, it is
   * requested to abort and a flag is set to restart the compiler as soon as
   * it is ready.
   * @param  observer  An observer object to be notified as soon as the
   *                   compilation has finished and the results are available,
   *                   or <CODE>null</CODE>.
   */
  public void compile(final CompilationObserver observer)
  {
    setObserver(observer);
    if (mModuleChanged && !mRunning) {
      mModuleChanged = false;
      mRunning = true;
      for (final Option<?> option : mOptions) {
        mCompiler.setOption(option);
      }
      mCompiler.setInputModule(mModuleContainer.getModule(), true);
      mWorker.compile();
    } else if (mModuleChanged && mRunning) {
      mRestart = true;
      mWorker.abort();
    } else if (!mModuleChanged && !mRunning) {
      notifyObserver();
    }
  }

  /**
   * Ensures that the module is recompiled as soon as possible even if it has
   * not changed. This method marks the module as changed and then calls
   * {@link #compile(CompilationObserver) compile()}.
   * @param  observer  An observer object to be notified as soon as the
   *                   compilation has finished and the results are available,
   *                   or <CODE>null</CODE>.
   */
  public void forceCompile(final CompilationObserver observer)
  {
    mModuleChanged = true;
    compile(observer);
  }

  /**
   * Terminates the background compiler. This method is called for cleanup
   * when the {@link ModuleContainer} is closed in the IDE. It cancels any
   * running compilation, stops all threads, and unregisters all option
   * listeners.
   */
  public void terminate()
  {
    for (final Option<?> option : mOptions) {
      option.removeOptionChangeListener(mCompilerPropertyChangeListener);
    }
    CompilerOptions.BACKGROUND_COMPILER.removeOptionChangeListener
      (mEnablementPropertyChangeListener);
    setTimerEnabled(false);
    mWorker.terminate();
  }

  /**
   * Callback to notify the background compiler when the {@link ModuleCompiler}
   * has finished. This method records the results of compilation and notifies
   * the observer, or restarts the compilation if the recompilation was
   * initiated after the compiler has started.
   * @param  compiledDES    The compiled DES returned by the
   *                        {@link ModuleCompiler}, or <CODE>null</CODE>
   *                        in case of error.
   * @param  exception  An exception produced by the {@link ModuleCompiler},
   *                        or <CODE>null</CODE> if compilation was
   *                        successful.
   */
  public void compilationFinished(final ProductDESProxy compiledDES,
                                  final AnalysisException exception)
  {
    assert compiledDES != null || exception != null;
    mRunning = false;
    if (mRestart) {
      mRestart = false;
      compile(mObserver);
    } else {
      mCompiledDES = compiledDES;
      mEvalException = exception;
      mSourceInfoMap = mCompiler.getSourceInfoMap();
      mModuleContainer.setCompilationException(exception);
      notifyObserver();
      if (mModuleChanged) {
        startTimer();
      }
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  @Override
  public void modelChanged(final ModelChangeEvent event)
  {
    final int kind = event.getKind();
    switch (kind) {
    case ModelChangeEvent.NAME_CHANGED:
    case ModelChangeEvent.STATE_CHANGED:
    case ModelChangeEvent.ITEM_ADDED:
    case ModelChangeEvent.ITEM_REMOVED:
      setModuleChanged();
      break;
    default:
      break;
    }
  }

  @Override
  public int getModelObserverPriority()
  {
    return ModelObserver.RENDERING_PRIORITY;
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  /**
   * Callback for when the timer expires.
   * Triggers compilation if not already compiling and the module has changed.
   */
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    if (mTimer != null && mModuleChanged && !mRunning) {
      compile(null);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setModuleChanged()
  {
    mModuleChanged = true;
    removeObserver();
    startTimer();
  }

  private void removeObserver()
  {
    if (mObserver != null) {
      mObserver = null;
      mDialog.dispose();
      mDialog = null;
    }
  }

  private void setObserver(final CompilationObserver observer)
  {
    removeObserver();
    mObserver = observer;
    if (mObserver != null) {
      final IDE ide = mModuleContainer.getIDE();
      mDialog = new CompilationDialog(ide, mAbortButtonAction);
    }
  }

  private void notifyObserver()
  {
    final boolean hasObserver = (mObserver != null);
    final boolean succeeded = (mCompiledDES != null);
    if (hasObserver && succeeded) {
      final CompilationObserver observer = mObserver;
      removeObserver();
      observer.compilationSucceeded(mCompiledDES);
    } else if (hasObserver && !succeeded) {
      mDialog.setEvalException(mEvalException, mObserver.getVerb());
    } else if (!hasObserver && !succeeded) {
      for (final AnalysisException exception : mEvalException.getLeafExceptions()) {
        if (!hasLocation(exception)) {
          final Logger logger = LogManager.getLogger();
          logger.error(exception.getMessage());
        }
      }
    }
  }

  private boolean hasLocation(final AnalysisException exception)
  {
    if (exception instanceof EvalException) {
      final EvalException evalException = (EvalException) exception;
      return evalException.getLocation() instanceof ProxySubject;
    } else {
      return false;
    }
  }

  private void setTimerEnabled(final boolean enable)
  {
    if (enable) {
      mTimer = new Timer(DELAY_AFTER_EDIT, this);
      mTimer.setRepeats(false);
      if (mModuleChanged) {
        startTimer();
      }
    } else if (mTimer != null) {
      mTimer.stop();
      mTimer = null;
      if (mObserver == null) {
        abortNonBackgroundCompiler();
      }
    }
  }

  private void startTimer()
  {
    if (mTimer != null) {
      mTimer.restart();
    }
  }

  private void abortNonBackgroundCompiler()
  {
    if (mRunning && mTimer == null) {
      mWorker.abort();
      mModuleChanged = true;
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleContainer mModuleContainer;
  private final ModuleCompiler mCompiler;
  private final List<Option<?>> mOptions;
  private final CompilationWorker mWorker;
  private final OptionChangeListener mEnablementPropertyChangeListener;
  private final OptionChangeListener mCompilerPropertyChangeListener;
  private final ActionListener mAbortButtonAction;

  private Timer mTimer;
  private boolean mModuleChanged;
  private boolean mRunning;
  private boolean mRestart;
  private CompilationObserver mObserver;
  private ProductDESProxy mCompiledDES;
  private AnalysisException mEvalException;
  private Map<Object, SourceInfo> mSourceInfoMap;
  private CompilationDialog mDialog;


  //#########################################################################
  //# Class Constants
  private static final int DELAY_AFTER_EDIT = 1000;

}
