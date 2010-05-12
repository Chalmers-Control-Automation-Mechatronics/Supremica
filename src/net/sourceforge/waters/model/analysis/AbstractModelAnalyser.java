//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractModelAnalyser
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Collection;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.log4j.Logger;


/**
 * An abstract base class that can be used for all model analyser
 * implementations. This class simply provides access to model and
 * factory members needed by virtually any model analyser implementation.
 *
 * @author Robi Malik
 */

public abstract class AbstractModelAnalyser implements ModelAnalyser
{

  //#########################################################################
  //# Constructors
  public AbstractModelAnalyser(final ProductDESProxyFactory factory)
  {
    this((ProductDESProxy) null, factory);
  }

  public AbstractModelAnalyser(final ProductDESProxy model,
                               final ProductDESProxyFactory factory)
  {
    mFactory = factory;
    mModel = model;
    mNodeLimit = Integer.MAX_VALUE;
    mTransitionLimit = Integer.MAX_VALUE;
    mIsAborting = false;
  }

  public AbstractModelAnalyser(final AutomatonProxy aut,
                               final ProductDESProxyFactory factory)
  {
    this(AutomatonTools.createProductDESProxy(aut, factory), factory);
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
   * each {@link ModelAnalyser#run() run()}. If overridden, the overriding
   * method should call the superclass methods first.
   * @throws AnalysisException
   */
  protected void setUp()
    throws AnalysisException
  {
    mStartTime = System.currentTimeMillis();
    mIsAborting = false;
  }

  /**
   * Resets the model analyser at the end of a run.
   * This method should be called by all subclasses upon completion of
   * each {@link ModelAnalyser#run() run()}, even if an exception is
   * thrown. If overridden, the overriding method should call the superclass
   * methods last.
   */
  protected void tearDown()
  {
    mIsAborting = false;
    final long current = System.currentTimeMillis();
    final AnalysisResult result = getAnalysisResult();
    if (result != null) {
      result.setRuntime(current - mStartTime);
    }
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
      throw new AbortException();
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
  private int mNodeLimit;
  private int mTransitionLimit;
  private long mStartTime;
  private boolean mIsAborting;

}
