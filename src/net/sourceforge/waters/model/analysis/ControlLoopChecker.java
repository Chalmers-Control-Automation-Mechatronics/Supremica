//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ControlLoopChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Collection;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopTraceProxy;


/**
 * <P>A model verifier that checks for control loops. This model verifier
 * checks whether the synchronous composition of all automata in the input
 * model contains any non-empty loop consisting of controllable events
 * only. The set of loops events can be parameterised using a kind
 * translator.</P>
 *
 * @see KindTranslator
 * @author Robi Malik
 */

public interface ControlLoopChecker extends ModelVerifier
{

  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets a counterexample if the model was found to be not control-loop free.
   * representing a control-loop error trace. A control-loop error
   * trace is a nonempty sequence of events that ends in a loop consisting of
   * controllable events only.
   * @return A trace object representing the counterexample.
   *         The returned trace is constructed for the input product DES
   *         of this control-loop checker and shares its automata and
   *         event objects.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link #run()}
   *         has been called, or model checking has found that the
   *         property is satisfied and there is no counterexample.
   */
  public LoopTraceProxy getCounterExample();

  /**
   * Gets a collection of events that are guaranteed not to be
   * contained in any control-loop after a failed call to
   * {@link #run()}. This is an optional method.
   * @throws IllegalStateException if this method is called before
   *         {@link #run()}, or if the last call to to {@link #run()}
   *         returned true.
   */
  public Collection<EventProxy> getNonLoopEvents();
}
