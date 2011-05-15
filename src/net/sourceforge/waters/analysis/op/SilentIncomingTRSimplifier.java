//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   SilentIncomingTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.util.BitSet;

import net.sourceforge.waters.analysis.op.EventEncoding;
import net.sourceforge.waters.analysis.op.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.op.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A list buffer transition relation implementation of the
 * <I>Silent Incoming Rule</I>.</P>
 *
 * <P>The <I>Silent Incoming Rule</I> removes a transition
 * when a tau event links two states <I>x</I> and&nbsp;<I>y</I> where at most
 * the source state&nbsp;<I>x</I> contains the precondition
 * marking&nbsp;<I>alpha</I>. If the target state&nbsp;<I>y</I> becomes
 * unreachable, it is removed, too. All transitions originating from the target
 * state&nbsp;<I>y</I> are copied to the source state&nbsp;<I>x</I>.</P>
 *
 * @author Rachel Francis, Robi Malik
 */

public class SilentIncomingTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public SilentIncomingTRSimplifier()
  {
  }

  public SilentIncomingTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets whether abstraction is applied to all states or only to states
   * that become unreachable. When this option is set to <CODE>true</CODE>
   * (the default), then the <I>Silent Incoming Rule</I> is only applied
   * to tau-transitions that lead to a state that becomes unreachable
   * by application of the rule. When set to <CODE>false</CODE>, the rule
   * is applied to all tau transitions leading to a state not marked by
   * the precondition, regardless of whether these states become unreachable
   * or not.
   */
  public void setRestrictsToUnreachableStates(final boolean restrict)
  {
    mRestrictsToUnreachableStates = restrict;
  }

  /**
   * Gets whether abstraction is applied to all states or only to states
   * that become unreachable.
   * @see #setRestrictsToUnreachableStates(boolean) setRestrictsToUnreachableStates()
   */
  public boolean getRestrictsToUnreachableStates()
  {
    return mRestrictsToUnreachableStates;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  @Override
  public int getPreferredConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }

  public boolean run()
    throws AnalysisException
  {
    setUp();
    final int tauID = EventEncoding.TAU;
    final int alphaID = getPreconditionMarkingID();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final BitSet keep = new BitSet(numStates);
    if (mRestrictsToUnreachableStates) {
      for (int state = 0; state < numStates; state++) {
        if (rel.isInitial(state) ||
            rel.isMarked(state, alphaID) ||
            !rel.isReachable(state)) {
          keep.set(state);
        }
      }
      final TransitionIterator iter =
        rel.createAllTransitionsReadOnlyIterator();
      while (iter.advance()) {
        if (iter.getCurrentEvent() != tauID) {
          final int target = iter.getCurrentTargetState();
          keep.set(target);
        }
      }
    } else {
      for (int state = 0; state < numStates; state++) {
        if (rel.isMarked(state, alphaID) || !rel.isReachable(state)) {
          keep.set(state);
        }
      }
    }
    if (keep.cardinality() == numStates) {
      return false;
    }
    final TransitionIterator iter = rel.createSuccessorsModifyingIterator();
    int source = 0;
    boolean modified = false;
    main:
    while (source < numStates) {
      if (rel.isReachable(source)) {
        iter.reset(source, tauID);
        while (iter.advance()) {
          final int target = iter.getCurrentTargetState();
          if (!keep.get(target)) {
            iter.remove();
            rel.copyOutgoingTransitions(target, source);
            modified = true;
            // After copying outgoing transitions from target to source,
            // the source state may receive new tau-transitions. To make sure
            // these are processed, we start checking the source state again.
            continue main;
          }
        }
      }
      source++;
    }
    if (modified) {
      applyResultPartitionAutomatically();
    }
    return modified;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.op.AbstractTRSimplifier
  @Override
  protected void applyResultPartition()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.checkReachability();
    rel.removeTauSelfLoops();
    rel.removeProperSelfLoopEvents();
  }


  //#########################################################################
  //# Data Members
  private boolean mRestrictsToUnreachableStates = true;

}
