//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>A model verifier that checks whether a system of composed automata
 * is <I>nonblocking</I>.
 * A conflict checker analyses the input model and returns with success if
 * the synchronous product of the automata in the model is nonblocking;
 * otherwise it returns with failure and produces a <I>conflict error
 * trace</I> ({@link ConflictTraceProxy}).</P>
 *
 * @author Robi Malik
 */

public interface ConflictChecker extends ModelVerifier
{

  //#########################################################################
  //# Configuration
  /**
   * <P>Sets the <I>marking proposition</I> to be used for conflict
   * checking.</P>
   * <P>The marking proposition defines which states are marked. Every state
   * has a list of propositions attached to it; the conflict checker
   * considers only those states as marked that are labelled by
   * <CODE>marking</CODE>, i.e., their list of propositions must contain
   * this event (exactly the same object).</P>
   * <P>A marking proposition of&nbsp;<CODE>null</CODE> may be specified to
   * use the <I>default marking</I>. In this case, the model must contain a
   * proposition event named {@link
   * net.sourceforge.waters.model.module#EventDeclProxy.DEFAULT_MARKING_NAME
   * EventDeclProxy.DEFAULT_MARKING_NAME}, which is used as marking
   * proposition. It is an error to request default marking, if no suitable
   * event is present.</P>
   * @param  marking  The marking proposition to be used,
   *                  or <CODE>null</CODE> to use the default marking
   *                  proposition of the model.
   */
  public void setMarkingProposition(EventProxy marking);

  /**
   * Gets the <I>marking proposition</I> used for conflict checking.
   * @return The current marking proposition or <CODE>null</CODE> to
   *         indicate default marking.
   * @see #setMarkingProposition(EventProxy)
   */
  public EventProxy getMarkingProposition();


  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets a counterexample if the model was found to be conflicting,
   * representing a conflict error trace. A conflict error trace is a
   * sequence of events that takes the model to a state that is not
   * coreachable. That is, after executing the counterexample, the automata
   * are in a state from where it is no longer possible to reach a state
   * where all automata are marked at the same time.
   * @return A trace object representing the counterexample.
   *         The returned trace is constructed for the input product DES
   *         of this conflict checker and shares its automata and
   *         event objects.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link #run()}
   *         has been called, or model checking has found that the
   *         property is satisfied and there is no counterexample.
   */
  public ConflictTraceProxy getCounterExample();

}
