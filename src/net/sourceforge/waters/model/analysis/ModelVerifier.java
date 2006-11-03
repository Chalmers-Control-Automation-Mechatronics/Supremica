//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ModelVerifier
//###########################################################################
//# $Id: ModelVerifier.java,v 1.3 2006-11-03 01:00:07 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.TraceProxy;


/**
 * <P>A model verifier takes a finite-state machine model as input and
 * performs a particular kind of verification on it. In contrast to a
 * general {@link ModelAnalyser}, a verifier always produces a Boolean
 * result that states whether a particular property checked is satisfied or
 * not. Furthermore, if the property is found not to be satisfied, the
 * model verifier provides a counterexample in the form of a sequence of
 * events, explaining why the property that was checked is not
 * satisfied.</P>
 *
 * <P>To use a model verifier, the user first creates an instance of a
 * subclass of this class, and sets up the model to be checked as well as
 * any other parameters that may be needed. Then verification is started
 * using the {@link #run() run()} method. Afterwards results can be queried
 * using the {@link #isSatisfied()} and {@link #getCounterExample()}
 * methods. This all can be done with the following code.</P>
 *
 * <P>
 * <CODE>{@link net.sourceforge.waters.model.des.ProductDESProxyFactory}
 *   factory =
 *   {@link net.sourceforge.waters.plain.des.ProductDESElementFactory}.{@link
 *   net.sourceforge.waters.plain.des.ProductDESElementFactory#getInstance()
 *   getInstance}();</CODE><BR>
 * <CODE>ModelVerifier verifier = new ControllabilityChecker(des, factory);
 * //</CODE> <I>e.g.</I><BR>
 * <CODE>verifier.{@link #run() run()};</CODE><BR>
 * <CODE>boolean result = verifier.{@link #isSatisfied()};</CODE><BR>
 * <CODE>if (result) {</CODE><BR>
 * <CODE>&nbsp;&nbsp;//</CODE> <I>property satisfied ...</I><BR>
 * <CODE>} else {</CODE><BR>
 * <CODE>&nbsp;&nbsp;//</CODE> <I>property not satisfied ...</I><BR>
 * <CODE>&nbsp;&nbsp;{@link TraceProxy}
 *   counterexample = verifier.{@link #getCounterExample()};</CODE><BR>
 * <CODE>}</CODE></P>
 *
 * <P>This class is subclassed to implement model checking algorithms for
 * various properties.</P>
 *
 * @author Robi Malik
 */

public interface ModelVerifier extends ModelAnalyser
{

  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets the result of model checking.
   * @return <CODE>true</CODE> if the property checked is satisfied,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before
   *         {@link ModelAnalyser#run() run()} has been called.
   */
  public boolean isSatisfied();

  /**
   * Gets a counterexample if model checking has found that the
   * property checked is not satisfied.
   * @return A trace object constructed for the model that was checked.
   *         It shares events and automata with the input model.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link
   *         ModelAnalyser#run() run()} has been called, or model checking has
   *         found that the property is satisfied and there is no
   *         counterexample.
   */
  public TraceProxy getCounterExample();

  public VerificationResult getAnalysisResult();

}
