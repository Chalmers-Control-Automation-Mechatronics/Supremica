//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.compiler
//# CLASS:   BackgroundCompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.compiler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.Timer;

import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ProxySubject;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.properties.Config;
import org.supremica.properties.SupremicaPropertyChangeEvent;
import org.supremica.properties.SupremicaPropertyChangeListener;

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
    mTimer = new Timer(DELAY_AFTER_EDIT, this);
    mTimer.setRepeats(false);
    container.getModule().addModelObserver(this);

    mCompilerPropertyChangeListener = new SupremicaPropertyChangeListener() {
      @Override
      public void propertyChanged(final SupremicaPropertyChangeEvent event)
      {
        setModuleChanged();
      }
    };
    Config.OPTIMIZING_COMPILER.addPropertyChangeListener
      (mCompilerPropertyChangeListener);
    Config.NORMALIZING_COMPILER.addPropertyChangeListener
      (mCompilerPropertyChangeListener);
    Config.USE_EVENT_ALPHABET.addPropertyChangeListener
      (mCompilerPropertyChangeListener);

    mRemoveObserverAction = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        removeObserver();
      }
    };

    forceCompile(null);
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
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    if (mModuleChanged && !mRunning) {
      compile(null);
    }
  }


  //#########################################################################
  //# Invocation
  public void compile(final CompilationObserver observer)
  {
    setObserver(observer);
    if (mModuleChanged && !mRunning) {
      mModuleChanged = false;
      mRunning = true;
      mCompiler.setOptimizationEnabled(Config.OPTIMIZING_COMPILER.isTrue());
      mCompiler.setNormalizationEnabled(Config.NORMALIZING_COMPILER.isTrue());
      mCompiler.setUsingEventAlphabet(Config.USE_EVENT_ALPHABET.isTrue());
      mCompiler.setInputModule(mModuleContainer.getModule(), true);
      mWorker.compile();
    } else if (mModuleChanged && mRunning) {
      mRestart = true;
      mWorker.abort();
    } else if (!mModuleChanged && !mRunning) {
      notifyObserver();
    }
  }

  public void forceCompile(final CompilationObserver observer)
  {
    setModuleChanged();
    compile(observer);
  }

  public void terminate()
  {
    Config.OPTIMIZING_COMPILER.removePropertyChangeListener
      (mCompilerPropertyChangeListener);
    mTimer.stop();
    mWorker.terminate();
  }

  public void compilationFinished(final ProductDESProxy compiledDES,
                                  final EvalException evalException)
  {
    assert compiledDES != null || evalException != null;
    mRunning = false;
    if (mRestart) {
      mRestart = false;
      mCompiler.resetAbort();
      compile(mObserver);
    } else {
      mCompiledDES = compiledDES;
      mEvalException = evalException;
      mSourceInfoMap = mCompiler.getSourceInfoMap();
      mModuleContainer.setCompilationException(evalException);
      notifyObserver();
      if (mModuleChanged) {
        mTimer.restart();
      }
    }
  }


  //#########################################################################
  //# Simple Access
  public ProductDESProxy getCompiledDES()
  {
    return mCompiledDES;
  }

  public Map<Object, SourceInfo> getSourceInfoMap()
  {
    return mSourceInfoMap;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setModuleChanged()
  {
    mModuleChanged = true;
    if (mObserver != null) {
      removeObserver();
    }
    mTimer.restart();
  }

  private void removeObserver()
  {
    mObserver = null;
    mDialog.dispose();
    mDialog = null;
  }

  private void setObserver(final CompilationObserver observer)
  {
    if (mObserver != null) {
      removeObserver();
    }
    mObserver = observer;
    if (mObserver != null) {
      final IDE ide = mModuleContainer.getIDE();
      mDialog = new CompilationDialog(ide, mRemoveObserverAction);
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
      final IDE ide = mModuleContainer.getIDE();
      for (final EvalException ex : mEvalException.getAll()) {
        if (!(ex.getLocation() instanceof ProxySubject)) {
          ide.error(ex.getMessage());
        }
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleContainer mModuleContainer;
  private final ModuleCompiler mCompiler;
  private final CompilationWorker mWorker;
  private final Timer mTimer;
  private final SupremicaPropertyChangeListener
    mCompilerPropertyChangeListener;
  private final ActionListener mRemoveObserverAction;

  private boolean mModuleChanged;
  private boolean mRunning;
  private boolean mRestart;
  private CompilationObserver mObserver;
  private ProductDESProxy mCompiledDES;
  private EvalException mEvalException;
  private Map<Object, SourceInfo> mSourceInfoMap;
  private CompilationDialog mDialog;


  //#########################################################################
  //# Class Constants
  private static final int DELAY_AFTER_EDIT = 1000;

}
