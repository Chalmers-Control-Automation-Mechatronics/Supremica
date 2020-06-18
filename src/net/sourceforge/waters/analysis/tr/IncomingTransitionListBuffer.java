//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.tr;

import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

/**
 * A transition list buffer for incoming transitions.
 *
 * This class is used by the {@link ListBufferTransitionRelation} to
 * store its incoming transitions. Transitions are indexed by their target
 * state, which are identified as 'from' states. Source states are used
 * as 'to' states.
 *
 * @see ListBufferTransitionRelation
 * @see TransitionListBuffer
 *
 * @author Robi Malik
 */

public class IncomingTransitionListBuffer extends TransitionListBuffer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new incoming transition list buffer.
   * The transition buffer is set up for a fixed number of states and events,
   * which defines an encoding and can no more be changed.
   * @param  numStates    The number of states that can be encoded in
   *                      transitions.
   * @param  eventStatus  Status flags of events, based on constants defined
   *                      in {@link EventStatus}.
   * @throws OverflowException if the encoding for states and events does
   *         not fit in the 32 bits available.
   */
  public IncomingTransitionListBuffer(final int numStates,
                                      final EventStatusProvider eventStatus)
    throws OverflowException
  {
    super(numStates, eventStatus);
  }

  /**
   * Creates a new incoming transition list buffer.
   * The transition buffer is set up for a fixed number of states and events,
   * which defines an encoding and can no more be changed.
   * @param  numStates    The number of states that can be encoded in
   *                      transitions.
   * @param  eventStatus  Status flags of events, based on constants defined
   *                      in {@link EventStatus}.
   * @param  numTrans     Estimated number of transitions, used to determine
   *                      buffer size.
   * @throws OverflowException if the encoding for states and events does
   *         not fit in the 32 bits available.
   */
  public IncomingTransitionListBuffer(final int numStates,
                                      final EventStatusProvider eventStatus,
                                      final int numTrans)
    throws OverflowException
  {
    super(numStates, eventStatus, numTrans);
  }

  /**
   * Creates a copy of the given transition list buffer.
   * This method performs a shallow copy of the given source transition list
   * buffer, no matter whether the source is an incoming or outgoing buffer.
   * Data structures are shared, so the source should no longer be used after
   * the copy.
   */
  IncomingTransitionListBuffer(final TransitionListBuffer buffer)
  {
    super(buffer);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.water.analysis.op.TransitionListBuffer
  @Override
  public StateProxy getFromState(final TransitionProxy trans)
  {
    return trans.getTarget();
  }

  @Override
  public StateProxy getToState(final TransitionProxy trans)
  {
    return trans.getSource();
  }

  @Override
  public int getIteratorSourceState(final TransitionIterator iter)
  {
    return iter.getCurrentToState();
  }

  @Override
  public int getIteratorTargetState(final TransitionIterator iter)
  {
    return iter.getCurrentFromState();
  }

  @Override
  public int getOtherIteratorFromState(final TransitionIterator iter)
  {
    return iter.getCurrentTargetState();
  }

  @Override
  public int getOtherIteratorToState(final TransitionIterator iter)
  {
    return iter.getCurrentSourceState();
  }

}
