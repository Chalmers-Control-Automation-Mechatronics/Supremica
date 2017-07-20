//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.model.analysis.module;

import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultAnalysisResult;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;

import org.apache.log4j.Logger;


/**
 * An abstract base class that can be used for all module analyser
 * implementations. This class simply provides access to model and
 * factory members needed by virtually any model analyser implementation.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public abstract class AbstractModuleAnalyzer implements ModuleAnalyzer
{

  //#########################################################################
  //# Constructors
  public AbstractModuleAnalyzer(final ModuleProxyFactory factory,
                                final KindTranslator translator)
  {
    this((ModuleProxy) null, factory, translator);
  }

  public AbstractModuleAnalyzer(final ModuleProxy model,
                                final ModuleProxyFactory factory,
                                final KindTranslator translator)
  {
    mFactory = factory;
    mKindTranslator = translator;
    mModel = model;
    mNodeLimit = Integer.MAX_VALUE;
    mTransitionLimit = Integer.MAX_VALUE;
    mIsAborting = false;
  }


  //#########################################################################
  //# Simple Access Methods
  @Override
  public ModuleProxyFactory getFactory()
  {
    return mFactory;
  }

  @Override
  public ModuleProxy getModel()
  {
    return mModel;
  }

  @Override
  public void setModel(final ModuleProxy model)
  {
    mModel = model;
    clearAnalysisResult();
  }

  public List<ParameterBindingProxy> getBindings()
  {
    return mBindings;
  }

  public void setBindings(final List<ParameterBindingProxy> binding)
  {
    mBindings = binding;
  }

  public void setKindTranslator(final KindTranslator translator)
  {
    if (mKindTranslator != translator) {
      mKindTranslator = translator;
      clearAnalysisResult();
    }
  }

  public KindTranslator getKindTranslator()
  {
    return mKindTranslator;
  }

  @Override
  public void setNodeLimit(final int limit)
  {
    mNodeLimit = limit;
  }

  @Override
  public int getNodeLimit()
  {
    return mNodeLimit;
  }

  @Override
  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
  }

  @Override
  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }

  @Override
  public AnalysisResult getAnalysisResult()
  {
    return mAnalysisResult;
  }

  @Override
  public void clearAnalysisResult()
  {
    mAnalysisResult = null;
  }



  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mIsAborting = true;
  }

  @Override
  public boolean isAborting()
  {
    return mIsAborting;
  }

  @Override
  public void resetAbort()
  {
    mIsAborting = false;
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Initialises the model analyser for a new run.
   * This method should be called by all subclasses at the beginning of
   * each {@link ModelAnalyzer#run() run()}. If overridden, the overriding
   * method should call the superclass methods first.
   */
  protected void setUp()
    throws EvalException, AnalysisException
  {
    mStartTime = System.currentTimeMillis();
    mIsAborting = false;
    mAnalysisResult = createAnalysisResult();
  }

  /**
   * Resets the model analyser at the end of a run.
   * This method should be called by all subclasses upon completion of
   * each {@link ModelAnalyzer#run() run()}, even if an exception is
   * thrown. If overridden, the overriding method should call the superclass
   * methods last.
   */
  protected void tearDown()
  {
    mIsAborting = false;
    addStatistics();
  }

  /**
   * Checks whether the model analyser has been requested to abort,
   * and if so, performs the abort by throwing an {@link AnalysisAbortException}.
   * This method should be called periodically by any model analyser that
   * supports being aborted by user request.
   */
  protected void checkAbort()
    throws AnalysisAbortException
  {
    if (mIsAborting) {
      final AnalysisAbortException exception = new AnalysisAbortException();
      setExceptionResult(exception);
      throw exception;
    }
  }

  /**
   * Creates an analysis result object to store the model analyser's results.
   * This method is called is in {@link #setUp()} to ensure that the
   * analysis result is available throughout the analysis run. It is
   * overridden by subclasses the require a more specific analysis result
   * type.
   */
  protected AnalysisResult createAnalysisResult()
  {
    return new DefaultAnalysisResult(getClass());
  }

  /**
   * Replaces the present result record by the given new result record.
   */
  protected void setAnalysisResult(final AnalysisResult result)
  {
    mAnalysisResult = result;
  }

  /**
   * Stores the given Boolean value on the analysis result and marks the run
   * as completed.
   * @return The given Boolean value.
   */
  protected boolean setBooleanResult(final boolean value)
  {
    mAnalysisResult.setSatisfied(value);
    addStatistics();
    return value;
  }

  /**
   * Stores the given exception on the analysis result and marks the run
   * as completed.
   * @return The given exception.
   */
  protected AnalysisException setExceptionResult
    (final AnalysisException exception)
  {
    if (mAnalysisResult != null) {
      mAnalysisResult.setException(exception);
      addStatistics();
    }
    return exception;
  }


  /**
   * Stores any available statistics on this analyser's last run in the
   * analysis result. This default implementation presently does nothing.
   * It needs to be overridden by subclasses, who should always call the
   * superclass method first.
   */
  protected void addStatistics()
  {
    if (mAnalysisResult != null) {
      final long current = System.currentTimeMillis();
      mAnalysisResult.setRuntime(current - mStartTime);
      final long usage = DefaultAnalysisResult.getCurrentMemoryUsage();
      mAnalysisResult.updatePeakMemoryUsage(usage);
    }
  }


  //#########################################################################
  //# Logging
  public Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private ModuleProxy mModel;
  private AnalysisResult mAnalysisResult;
  private KindTranslator mKindTranslator;

  private int mNodeLimit;
  private int mTransitionLimit;
  private long mStartTime;
  private boolean mIsAborting;
  List<ParameterBindingProxy> mBindings;

}
