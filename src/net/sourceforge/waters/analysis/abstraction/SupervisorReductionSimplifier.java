//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.abstraction;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;


/**
 * <P>An interface for transition relation simplifiers that implement
 * supervisor reduction.</P>
 *
 * <P>A supervisor reduction simplifier takes as input a transition
 * relation representing a supervisor, and transforms it into a smaller
 * supervisor that makes the same control decisions. Supervisor reduction
 * is based on the observation that for each controllable event, there are
 * states where it must be disabled and states where it must be enabled,
 * while for other states it does not matter whether or not the supervisor
 * enables the event because it is not possible in the plant.</P>
 *
 * <P>To distinguish these three possibilities, the supervisor reduction
 * simplifier checks for each controllable event the states that have an
 * outgoing transition with that event. If the transition's target state is
 * the dump state, then the supervisor must disable the event in the
 * transition's source state; if the transition's target state is not the dump
 * state, then the supervisor must enable the event in the transition's source
 * state. States without any transitions for a controllable event are assumed
 * to be &quot;don't care&quot; and may be merged into enabling or disabling
 * states during supervisor reduction.</P>
 *
 * <P>Supervisor reduction may be performed for all controllable events
 * simultaneously, or for one controllable event only. The latter case
 * is known as <I>supervisor localisation</I> and requires that that the
 * supervisor reduction simplifier is invoked several times to create separate
 * supervisors for each controllable event that needs to be disabled in at
 * least one state.</P>
 *
 * <P>The result of this transition relation simplifier follows the same
 * conventions as the input, i.e., disablement of a controllable event is
 * indicated by the presence of a transition to the dump state. A pure
 * supervisor automaton is obtained by deleting the dump state and
 * associated transitions.</P>
 *
 * @see SupervisorReductionFactory
 * @see ListBufferTransitionRelation#getDumpStateIndex()
 *
 * @author Robi Malik
 */

public interface SupervisorReductionSimplifier
  extends TransitionRelationSimplifier
{

  //#########################################################################
  //# Configuration
  /**
   * Sets the controllable event to be enabled or disabled by the supervisor.
   * If set, supervisor reduction will produce a localised supervisor
   * controlling only this event. Otherwise all controllable events are
   * subject to control.
   * @param  event  The number of the controllable event to be enabled or
   *                disabled by the supervisor; or -1 to control all events.
   */
  public void setSupervisedEvent(final int event);

  /**
   * Gets the controllable event to be enabled or disabled by the supervisor.
   * @see #setSupervisedEvent(int) setSupervisedEvent()
   */
  public int getSupervisedEvent();

}
