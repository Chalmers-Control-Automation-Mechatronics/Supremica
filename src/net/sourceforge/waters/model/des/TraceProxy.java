//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.util.List;

import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A execution sequence for some automata of a product DES.</P>
 *
 * <P>A trace consists of an alternating sequence of states and events
 * that can be executed by several synchronised automata. The state
 * information is optional for deterministic automata as it can be
 * computed from the automata in this case.</P>
 *
 * <P>For nondeterministic automata, state information is required, and it
 * is stored in a sequence of trace steps ({@link TraceStepProxy}) as follows.
 * The first step represents the initial states of the automata, and each
 * following step contains an event and the state reached by the automata
 * after executing this event.</P>
 *
 * <P>Traces can be computed as part of counterexamples
 * ({@link CounterExampleProxy}), or be obtained by simulation.</P>
 *
 * @author Robi Malik
 */

public interface TraceProxy
  extends Proxy
{

  //#########################################################################
  //# Getters
  /**
   * Gets the name of this trace.
   * The name can identify or describe a trace more precisely in
   * counterexamples that have more than one trace. It may be an empty
   * string in all other cases.
   */
  public String getName();

  /**
   * Gets the sequence of events constituting this trace.
   * @return  An unmodifiable list of objects of type {@link EventProxy}.
   */
  public List<EventProxy> getEvents();

  /**
   * Gets the sequence of states and events constituting this trace.
   * This method returns a list of {@link TraceStepProxy} objects,
   * each consisting of a pair of incoming event and target state.
   * The first entry has a <CODE>null</CODE> event and the initial state as
   * its target state, while every other entry has a non-null event
   * and the state reached after that event.
   * @return  An unmodifiable list of objects of type {@link TraceStepProxy}.
   */
  public List<TraceStepProxy> getTraceSteps();

  /**
   * Gets the start position of the cycle in a cyclic trace.
   * The loop index identifies the number of the step (starting at&nbsp;0)
   * in the trace where the loop starts. If the trace has steps 0,...,<I>n</I>,
   * and the loop index is at position&nbsp;<I>i</I>, then it represents the
   * loop 0,...,<I>n</I>,<I>i</I>,...,<I>n</I>,...
   * @return The index of the trace step starting a cycle,
   *         or <CODE>-1</CODE> if it is not a cyclic trace.
   */
  public int getLoopIndex();

}
