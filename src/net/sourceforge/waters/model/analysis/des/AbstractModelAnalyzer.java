//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractModelAnalyser
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.DefaultAnalysisResult;
import net.sourceforge.waters.model.analysis.InvalidModelException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.log4j.Logger;


/**
 * An abstract base class that can be used for all model analyser
 * implementations. This class simply provides access to model and
 * factory members needed by virtually any model analyser implementation.
 *
 * @author Robi Malik
 */

public abstract class AbstractModelAnalyzer implements ModelAnalyzer
{

  //#########################################################################
  //# Constructors
  public AbstractModelAnalyzer(final ProductDESProxyFactory factory,
                               final KindTranslator translator)
  {
    this((ProductDESProxy) null, factory, translator);
  }

  public AbstractModelAnalyzer(final ProductDESProxy model,
                               final ProductDESProxyFactory factory,
                               final KindTranslator translator)
  {
    mFactory = factory;
    mKindTranslator = translator;
    mModel = model;
    mNodeLimit = Integer.MAX_VALUE;
    mTransitionLimit = Integer.MAX_VALUE;
    mIsAborting = false;
  }

  public AbstractModelAnalyzer(final AutomatonProxy aut,
                               final ProductDESProxyFactory factory,
                               final KindTranslator translator)
  {
    this(AutomatonTools.createProductDESProxy(aut, factory),
         factory, translator);
  }


  //#########################################################################
  //# Simple Access Methods
  public ProductDESProxyFactory getFactory()
  {
    return mFactory;
  }

  public ProductDESProxy getModel()
  {
    return mModel;
  }

  public void setModel(final ProductDESProxy model)
  {
    mModel = model;
    clearAnalysisResult();
  }

  public void setModel(final AutomatonProxy aut)
  {
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy(aut, mFactory);
    setModel(des);
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

  public void setNodeLimit(final int limit)
  {
    mNodeLimit = limit;
  }

  public int getNodeLimit()
  {
    return mNodeLimit;
  }

  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
  }

  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }

  public AnalysisResult getAnalysisResult()
  {
    return mAnalysisResult;
  }

  public void clearAnalysisResult()
  {
    mAnalysisResult = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  public void requestAbort()
  {
    mIsAborting = true;
  }

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
    throws AnalysisException
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
   * Attempts to retrieve the single input automaton from the input model.
   * This method checks whether the input model contains exactly one
   * automaton, and if this is the case, returns it.
   * @throws InvalidModelException if the input model does not contain
   *         exactly one automaton.
   */
  protected AutomatonProxy getInputAutomaton()
    throws InvalidModelException
  {
    final ProductDESProxy des = getModel();
    final Collection<AutomatonProxy> automata = des.getAutomata();
    if (automata.size() == 1) {
      return automata.iterator().next();
    } else {
      throw new InvalidModelException
        ("The input product DES '" + des.getName() +
         "' does not contain exactly one automaton, which is required for " +
         ProxyTools.getShortClassName(this) + "!");
    }
  }

  /**
   * Returns whether the given event is a proper event under the current
   * {@link KindTranslator}. A proper event is controllable or uncontrollable,
   * not a proposition.
   * @see #getKindTranslator()
   * @see EventKind
   */
  protected boolean isProperEvent(final EventProxy event)
  {
    final KindTranslator translator = getKindTranslator();
    final EventKind kind = translator.getEventKind(event);
    return kind == EventKind.CONTROLLABLE || kind == EventKind.UNCONTROLLABLE;
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
  private final ProductDESProxyFactory mFactory;
  private ProductDESProxy mModel;
  private AnalysisResult mAnalysisResult;
  private KindTranslator mKindTranslator;

  private int mNodeLimit;
  private int mTransitionLimit;
  private long mStartTime;
  private boolean mIsAborting;

}
