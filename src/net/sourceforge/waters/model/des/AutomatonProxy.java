//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.des;

import java.util.Collection;
import java.util.Map;
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

  /**
   * Gets the attribute map for this automaton.
   * The attribute map can be used by tools supporting external model
   * formats to store information that does not appear in standard DES
   * models.
   * @return An immutable map mapping attribute names to values.
   */
  public Map<String,String> getAttributes();

}
