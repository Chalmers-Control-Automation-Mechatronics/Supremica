//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   AutomatonProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.des;

import java.util.Collection;
import java.util.Set;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

/**
 * <P>A finite-state machine.</P>
 *
 * <P>This class is a straightforward representation of a finite-state
 * machine or Kripke structure.  It consists of an event alphabet, a list
 * of states, and a list transitions.</P>
 *
 * <P>The alphabet contains all the events that are used for
 * synchronisation between this and other automata, and all the
 * propositions used to label states of this Kripke structure. Events not
 * contained in the alphabet are assumed to be selflooped in all states of
 * this automaton. Propositions not contained in the alphabet are assumed
 * to be true in all states.</P>
 *
 * <P>In addition, finite-state machines in a discrete-event systems
 * context can be classified as <I>plant</I> or <I>specification</I>.
 * This is information is also associated with each automaton.</P>
 *
 * @author Robi Malik
 */

public interface AutomatonProxy
  extends NamedProxy
{

  //#########################################################################
  //# Cloning
  /**
   * Creates and returns a copy of this automaton. This method supports the
   * general contract of the {@link java.lang.Object#clone() clone()}
   * method. Its precise semantics differs for different implementations of
   * the <CODE>Proxy</CODE> interface.
   */
  public AutomatonProxy clone();


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the kind (<I>plant</I>, <I>specification</I>, etc.) of this
   * automaton.
   */
  public ComponentKind getKind();

  /**
   * Gets the event alphabet for this automaton.
   * This method returns the set of events on which this automaton
   * synchronises, or the set of all events that can occur on its
   * transitions.
   * @return  The set of events.
   */
  public Set<EventProxy> getEvents();

  /**
   * Gets the set of states for this automaton.
   * @return  The set of states.
   */
  public Set<StateProxy> getStates();

  /**
   * Gets the list of transitions for this automaton.
   * @return  A collection of transitions. Implementations may or may not
   *          guarantee the absence of duplicates.
   */
  public Collection<TransitionProxy> getTransitions();

}
