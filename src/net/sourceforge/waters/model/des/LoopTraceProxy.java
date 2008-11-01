//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   LoopTraceProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.des;


/**
 * <P>A loop counterexample trace for some automata of a product DES.</P>
 *
 * @see ProductDESProxy
 *
 * @author Robi Malik
 */

public interface LoopTraceProxy
  extends TraceProxy
{

  //#########################################################################
  //# Getters
  /**
   * Gets the loop index of this trace.
   * The loop index identifies the number of the step (starting at&nbsp;0)
   * in the trace where the loop starts. If the trace has steps 0,...,<I>n</I>,
   * and the loop index is at position&nbsp<I>i</I>, then it represents the
   * loop 0,...,<I>n</I>,<I>i</I>,...,<I>n</I>,...
   */
  public int getLoopIndex();

}
