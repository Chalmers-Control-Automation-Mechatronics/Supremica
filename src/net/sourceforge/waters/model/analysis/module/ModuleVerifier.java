//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis.module
//# CLASS:   ModuleVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.module;

import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * <P>A module verifier takes a module as input and
 * performs a particular kind of verification on it. In contrast to a
 * general {@link ModuleAnalyzer}, a verifier always produces a Boolean
 * result that states whether a particular property checked is satisfied or
 * not. Furthermore, if the property is found not to be satisfied, the
 * module verifier provides a counterexample in the form of a sequence of
 * events, explaining why the property that was checked is not
 * satisfied.</P>
 *
 * <P>This class is subclassed to implement model checking algorithms for
 * various properties.</P>
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public interface ModuleVerifier extends ModuleAnalyzer
{

  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets the result of model checking.
   * @return <CODE>true</CODE> if the property checked is satisfied,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before
   *         {@link ModelAnalyzer#run() run()} has been called.
   */
  public boolean isSatisfied();

  /**
   * Gets a counterexample if model checking has found that the
   * property checked is not satisfied.
   * @return A trace object constructed for the model that was checked.
   *         It shares events and automata with the input model.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link
   *         ModelAnalyzer#run() run()} has been called, or model checking has
   *         found that the property is satisfied and there is no
   *         counterexample.
   */
  public TraceProxy getCounterExample();

  @Override
  public VerificationResult getAnalysisResult();

}
