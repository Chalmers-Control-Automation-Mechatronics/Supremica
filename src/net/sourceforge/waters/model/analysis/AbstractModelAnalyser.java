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
import java.util.Collections;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


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
    this(null, factory);
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
    final ProductDESProxy des = createProductDESProxy(aut, mFactory);
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


  //#########################################################################
  //# Auxiliary Static Methods
  public static ProductDESProxy createProductDESProxy
    (final AutomatonProxy aut, final ProductDESProxyFactory factory)
  {
    final String name = aut.getName();
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<AutomatonProxy> automata = Collections.singletonList(aut);
    return factory.createProductDESProxy(name, events, automata);
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private ProductDESProxy mModel;
  private int mNodeLimit;
  private int mTransitionLimit;
  private boolean mIsAborting;

}
