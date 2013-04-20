//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SilentIncomingTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntStack;

import java.util.BitSet;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A list buffer transition relation implementation of the
 * <I>Silent Incoming Rule</I> or the <I>Only Silent Incoming Rule</I>.</P>
 *
 * <P>The <I>Silent Incoming Rule</I> removes a transition
 * when a tau event links two states <I>x</I> and&nbsp;<I>y</I> where at most
 * the source state&nbsp;<I>x</I> contains the precondition
 * marking&nbsp;<I>alpha</I>. If the target state&nbsp;<I>y</I> becomes
 * unreachable, it is removed, too. All transitions originating from the target
 * state&nbsp;<I>y</I> are copied to the source state&nbsp;<I>x</I>.</P>
 *
 * <P>The implementation can be configured to remove only transitions leading
 * to states that become unreachable, giving the <I>Only Silent Incoming
 * Rule</I>.</P>
 *
 * <P>The implementation supports both standard and generalised nonblocking
 * variants of the abstraction. If a precondition marking is configured,
 * only transitions leading to states not marked by the precondition marking
 * can be abstracted (as described above). Without a precondition marking, only
 * transitions leading to states with an outgoing silent transition can be
 * abstracted.</P>
 *
 * <P><I>References:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification
 * in Supervisory Control. SIAM Journal of Control and Optimization,
 * 48(3), 1914-1938, 2009.<BR>
 * Robi Malik, Ryan Leduc. A Compositional Approach for Verifying
 * Generalised Nonblocking, Proc. 7th International Conference on Control and
 * Automation, ICCA'09, 448-453, Christchurch, New Zealand, 2009.</P>
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
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  public boolean runSimplifier()
  throws AnalysisException
  {
    final int tauID = EventEncoding.TAU;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if ((rel.getProperEventStatus(tauID) & EventEncoding.STATUS_UNUSED) != 0) {
      return false;
    } else if (getPreconditionMarkingID() < 0) {
      mTauTestIterator = rel.createSuccessorsReadOnlyIterator();
      mTauTestIterator.resetEvent(tauID);
    }
    final int numStates = rel.getNumberOfStates();
    final BitSet keep = new BitSet(numStates);
    if (mRestrictsToUnreachableStates) {
      for (int state = 0; state < numStates; state++) {
        if (rel.isInitial(state) ||
            !isReducible(state) ||
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
        if (!isReducible(state) || !rel.isReachable(state)) {
          keep.set(state);
        }
      }
    }
    checkAbort();
    if (keep.cardinality() == numStates) {
      return false;
    }
    final TransitionIterator reader = rel.createSuccessorsReadOnlyIterator();
    final TransitionIterator writer = rel.createSuccessorsModifyingIterator();
    final TIntArrayList targets = new TIntArrayList();
    final TIntStack stack = new TIntStack();
    boolean modified = false;
    for (int source = 0; source < numStates; source++) {
      if (rel.isReachable(source)) {
        checkAbort();
        final TIntHashSet visited = new TIntHashSet();
        stack.push(source);
        visited.add(source);
        while (stack.size() > 0) {
          final int current = stack.pop();
          reader.reset(current, tauID);
          while (reader.advance()) {
            final int target = reader.getCurrentTargetState();
            if (!keep.get(target) && visited.add(target)) {
              stack.push(target);
              targets.add(target);
            }
          }
        }
        if (!targets.isEmpty()) {
          rel.copyOutgoingTransitions(targets, source);
          writer.reset(source, tauID);
          while (writer.advance()) {
            final int target = writer.getCurrentTargetState();
            if (visited.contains(target)) {
              writer.remove();
            }
          }
          targets.clear();
          modified = true;
        }
      }
    }
    if (modified) {
      applyResultPartitionAutomatically();
    }
    return modified;
  }

  @Override
  protected void tearDown()
  {
    mTauTestIterator = null;
    super.tearDown();
  }

  @Override
  protected void applyResultPartition()
  throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.checkReachability();
    rel.removeTauSelfLoops();
    rel.removeProperSelfLoopEvents();
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean isReducible(final int state)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int alphaID = getPreconditionMarkingID();
    if (alphaID < 0) {
      mTauTestIterator.resetState(state);
      return mTauTestIterator.advance();
    } else {
      return !rel.isMarked(state, alphaID);
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mRestrictsToUnreachableStates = true;

  private TransitionIterator mTauTestIterator;

}
