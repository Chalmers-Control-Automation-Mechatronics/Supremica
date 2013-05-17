//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis.module
//# CLASS:   AbstractModuleAnalyser
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.module;

import java.util.List;

import net.sourceforge.waters.model.analysis.AbortException;
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

  public List<ParameterBindingProxy> getBinding()
  {
    return mBinding;
  }

  public void setBindings(final List<ParameterBindingProxy> binding)
  {
    mBinding = binding;
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


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Initialises the model analyser for a new run.
   * This method should be called by all subclasses at the beginning of
   * each {@link ModelAnalyzer#run() run()}. If overridden, the overriding
   * method should call the superclass methods first.
   * @throws AnalysisException
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
   * and if so, performs the abort by throwing an {@link AbortException}.
   * This method should be called periodically by any model analyser that
   * supports being aborted by user request.
   */
  protected void checkAbort()
    throws AbortException
  {
    if (mIsAborting) {
      final AbortException exception = new AbortException();
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
    return new DefaultAnalysisResult();
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
  List<ParameterBindingProxy> mBinding;

}
