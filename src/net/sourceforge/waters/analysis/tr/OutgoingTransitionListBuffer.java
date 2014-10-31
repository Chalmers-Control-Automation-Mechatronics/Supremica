//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   OutgoingTransitionListBuffer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

/**
 * A transition list buffer for outgoing transitions.
 *
 * This class is used by the {@link ListBufferTransitionRelation} to
 * store its outgoing transitions. Transitions are indexed by their source
 * state, which are identified as 'from' states. Target states are used
 * as 'to' states.
 *
 * @see ListBufferTransitionRelation
 * @see TransitionListBuffer
 *
 * @author Robi Malik
 */

public class OutgoingTransitionListBuffer extends TransitionListBuffer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new outgoing transition list buffer.
   * The transition buffer is set up for a fixed number of states and events,
   * which defines an encoding and can no more be changed.
   * @param  numStates    The number of states that can be encoded in
   *                      transitions.
   * @param  eventStatus  Status flags of events, based on constants defined
   *                      in {@link EventStatus}.
   * @throws OverflowException if the encoding for states and events does
   *         not fit in the 32 bits available.
   */
  public OutgoingTransitionListBuffer(final int numStates,
                                      final EventStatusProvider eventStatus)
    throws OverflowException
  {
    super(numStates, eventStatus);
  }

  /**
   * Creates a new outgoing transition list buffer.
   * The transition buffer is set up for a fixed number of states and events,
   * which defines an encoding and can no more be changed.
   * @param  numEvents    The number of events that can be encoded in
   *                      transitions.
   * @param  numStates    The number of states that can be encoded in
   *                      transitions.
   * @param  eventStatus  Status flags of events, based on constants defined
   *                      in {@link EventStatus}.
   * @param  numTrans     Estimated number of transitions, used to determine
   *                      buffer size.
   * @throws OverflowException to indicate that the encoding for states and
   *                      events does not fit in the 32 bits available.
   */
  public OutgoingTransitionListBuffer(final int numStates,
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
  OutgoingTransitionListBuffer(final TransitionListBuffer buffer)
  {
    super(buffer);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.water.analysis.op.TransitionListBuffer
  @Override
  public StateProxy getFromState(final TransitionProxy trans)
  {
    return trans.getSource();
  }

  @Override
  public StateProxy getToState(final TransitionProxy trans)
  {
    return trans.getTarget();
  }

  @Override
  public int getIteratorSourceState(final TransitionIterator iter)
  {
    return iter.getCurrentFromState();
  }

  @Override
  public int getIteratorTargetState(final TransitionIterator iter)
  {
    return iter.getCurrentToState();
  }


  @Override
  public int getOtherIteratorFromState(final TransitionIterator iter)
  {
    return iter.getCurrentSourceState();
  }


  @Override
  public int getOtherIteratorToState(final TransitionIterator iter)
  {
    return iter.getCurrentTargetState();
  }

}
