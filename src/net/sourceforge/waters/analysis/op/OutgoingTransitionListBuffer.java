//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   OutgoingTransitionListBuffer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

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
   * @throws OverflowException if the encoding for states and events does
   *         not fit in the 32 bits available.
   */
  public OutgoingTransitionListBuffer(final EventEncoding eventEnc,
                                      final StateEncoding stateEnc)
      throws OverflowException
  {
    super(eventEnc, stateEnc);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.water.analysis.op.TransitionListBuffer
  public StateProxy getFromState(final TransitionProxy trans)
  {
    return trans.getSource();
  }

  public StateProxy getToState(final TransitionProxy trans)
  {
    return trans.getTarget();
  }

  public int getIteratorSourceState(final TransitionIterator iter)
  {
    return iter.getCurrentFromState();
  }

  public int getIteratorTargetState(final TransitionIterator iter)
  {
    return iter.getCurrentToState();
  }

}
