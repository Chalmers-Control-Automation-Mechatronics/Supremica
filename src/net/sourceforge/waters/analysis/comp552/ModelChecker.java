//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: COMP452/552-10B Assignment 3
//# PACKAGE: net.sourceforge.waters.analysis.comp552
//# CLASS:   ModelChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.comp552;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * <P>An abstract class defining the interface of a model checker.</P>
 *
 * <P>A model checker takes a finite-state machine model as input
 * and performs a particular kind of analysis on it. When done,
 * it returns the result, i.e., whether the particular property checked
 * is true or false. Furthermore, if the property is found not to
 * be satisfied, the model checker provides a counterexample in the
 * form of a sequence of events, explaining why the property that was
 * checked is not satisfied.</P>
 *
 * <P>To use a model checker, the user first creates an instance of a
 * subclass of this class, and sets up the model to be checked as well as
 * any other parameters that may be needed. Then model checking is started
 * using the {@link #run()} method. Then, if the property was found not to be
 * satisfied a counterexample can be retrieved using the {@link
 * #getCounterExample()} method. This all can be done with the following
 * code.</P>
 *
 * <P>
 * <CODE>{@link ProductDESProxyFactory} factory =
 *   {@link net.sourceforge.waters.plain.des.ProductDESElementFactory}.{@link
 *   net.sourceforge.waters.plain.des.ProductDESElementFactory#getInstance()
 *   getInstance}();</CODE><BR>
 * <CODE>ModelChecker checker = new {@link ConflictChecker}(des, factory);
 * //</CODE> <I>e.g.</I><BR>
 * <CODE>boolean result = checker.{@link #run()};</CODE><BR>
 * <CODE>if (result) {</CODE><BR>
 * <CODE>&nbsp;&nbsp;//</CODE> <I>property satisfied ...</I><BR>
 * <CODE>} else {</CODE><BR>
 * <CODE>&nbsp;&nbsp;//</CODE> <I>property not satisfied ...</I><BR>
 * <CODE>&nbsp;&nbsp;{@link net.sourceforge.waters.model.des.TraceProxy}
 *   counterexample = checker.{@link #getCounterExample()};</CODE><BR>
 * <CODE>}</CODE></P>
 *
 * <P>This class is to be subclassed to implement model checking
 * algorithms for various properties. Each subclass must implement
 * at least the methods {@link #run()} and {@link #getCounterExample()}.</P>
 *
 * @author Robi Malik
 */

public abstract class ModelChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new model checker to check a particular model.
   * @param  model   The model to be checked by this model checker.
   * @param  factory Factory used for trace construction.
   */
  public ModelChecker(final ProductDESProxy model,
                      final ProductDESProxyFactory factory)
  {
    mModel = model;
    mFactory = factory;
  }


  //#########################################################################
  //# Invocation
  /**
   * Runs this model checker.
   * This method starts the model checking process on the model given
   * as parameter to the constructor of this object. On termination,
   * if the result is false, a counterexample can be queried
   * using {@link #getCounterExample()} method.
   * @return <CODE>true</CODE> or <CODE>false</CODE> to indicate
   *         whether the property checked is satisfied or not.
   */
  public abstract boolean run();


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the model under investigation by this model checker.
   * @return The model checked by this model checker,
   *         as passed into its constructor
   *         {@link #ModelChecker(ProductDESProxy,ProductDESProxyFactory)
   *         ModelChecker()}.
   */
  public ProductDESProxy getModel()
  {
    return mModel;
  }

  /**
   * Gets the factory used for trace construction.
   */
  public ProductDESProxyFactory getFactory()
  {
    return mFactory;
  }

  /**
   * Gets a counterexample if model checking has found that the
   * property checked is not satisfied.
   * @return A trace object constructed for the model that was checked.
   *         It shares events and automata with the input model.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link #run()}
   *         has been called, or model checking has found that the
   *         property is satisfied and there is no counterexample.
   */
  public abstract TraceProxy getCounterExample();


  //#########################################################################
  //# Data Members
  /**
   * The model under investigation by this model checker.
   */
  private final ProductDESProxy mModel;

  /**
   * The factory used for trace construction.
   */
  private final ProductDESProxyFactory mFactory;

}
