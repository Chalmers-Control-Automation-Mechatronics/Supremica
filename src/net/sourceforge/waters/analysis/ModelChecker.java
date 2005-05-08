//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ModelChecker
//###########################################################################
//# $Id: ModelChecker.java,v 1.1 2005-05-08 00:24:31 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis;

import java.util.List;

import net.sourceforge.waters.model.des.ProductDESProxy;


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
 * <P>To use a model checker, the use first creates an instance of a
 * subclass of this class, and sets up the model to be checked as well as
 * any other parameters that may be needed. Then model checking is started
 * using the {@link #run()} method. Afterwards results can be queried using
 * the {@link #getResult()} and {@link #getCounterExample()} methods. This
 * all can be done with the following code.</P>
 *
 * <P>
 * <CODE>ModelChecker checker = new ControllabilityChecker(des);
 * //</CODE> <I>e.g.</I><BR>
 * <CODE>checker.{@link #run()};</CODE><BR>
 * <CODE>boolean result = checker.{@link #getResult()};</CODE><BR>
 * <CODE>if (result) {</CODE><BR>
 * <CODE>&nbsp;&nbsp;//</CODE> <I>property satisfied ...</I><BR>
 * <CODE>} else {</CODE><BR>
 * <CODE>&nbsp;&nbsp;//</CODE> <I>property not satisfied ...</I><BR>
 * <CODE>&nbsp;&nbsp;{@link List} counterexample =
 *   checker.{@link #getCounterExample()};</CODE><BR>
 * <CODE>}</CODE></P>
 *
 * <P>This class is to be subclassed to implement model checking
 * algorithms for various properties. Each subclass must implement
 * at least the methods {@link #run()}, {@link #getResult()},
 * and {@link #getCounterExample()}.</P>
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
   */
  public ModelChecker(final ProductDESProxy model)
  {
    mModel = model;
  }


  //#########################################################################
  //# Invocation
  /**
   * Runs this model checker.
   * This method starts the model checking process on the model given
   * as parameter to the constructor of this object. On termination,
   * the result of checking the property is known and can be queried
   * using the {@link #getResult()} and {@link #getCounterExample()}
   * methods.
   * @return <CODE>true</CODE> or <CODE>false</CODE> to indicate
   *         whether the property checked is satisfied or not.
   *         The same value can be queried using the {@link #getResult()}
   *         method.
   */
  public abstract boolean run();


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the model under investigation by this model checker.
   * @return The model checked by this model checker,
   *         as passed into its constructor
   *         {@link #ModelChecker(ProductDESProxy) ModelChecker()}.
   */
  public ProductDESProxy getModel()
  {
    return mModel;
  }

  /**
   * Gets the result of model checking.
   * @return <CODE>true</CODE> if the property checked is satisfied,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link #run()}
   *         has been called.
   */
  public abstract boolean getResult();

  /**
   * Gets a counterexample if model checking has found that the
   * property checked is not satisfied.
   * @return A list of events of type
   *         {@link net.sourceforge.waters.model.des.EventProxy EventProxy}
   *         representing an error trace. The event objects returned
   *         must be part of the model that was checked.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link #run()}
   *         has been called, or model checking has found that the
   *         property is satisfied and there is no counterexample.
   */
  public abstract List getCounterExample();
  

  //#########################################################################
  //# Data Members
  /**
   * The model under investigation by this model checker.
   */
  private final ProductDESProxy mModel;

}
