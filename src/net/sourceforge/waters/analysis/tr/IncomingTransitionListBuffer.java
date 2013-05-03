//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   IncomingTransitionListBuffer
//###########################################################################
//# $Id$
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
   * @param  numEvents   The number of events that can be encoded in
   *                     transitions.
   * @param  numStates   The number of states that can be encoded in
   *                     transitions.
   * @throws OverflowException if the encoding for states and events does
   *         not fit in the 32 bits available.
   */
  public IncomingTransitionListBuffer(final int numEvents, final int numStates, final byte[] eventStatus)
    throws OverflowException
  {
    super(numEvents, numStates,  eventStatus);
  }

  /**
   * Creates a new incoming transition list buffer.
   * The transition buffer is set up for a fixed number of states and events,
   * which defines an encoding and can no more be changed.
   * @param  numEvents   The number of events that can be encoded in
   *                     transitions.
   * @param  numStates   The number of states that can be encoded in
   *                     transitions.
   * @param  numTrans    Estimated number of transitions, used to determine
   *                     buffer size.
   * @throws OverflowException if the encoding for states and events does
   *         not fit in the 32 bits available.
   */
  public IncomingTransitionListBuffer(final int numEvents,
                                      final int numStates,
                                      final byte[] eventStatus,
                                      final int numTrans)
    throws OverflowException
  {
    super(numEvents, numStates, eventStatus, numTrans);
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
