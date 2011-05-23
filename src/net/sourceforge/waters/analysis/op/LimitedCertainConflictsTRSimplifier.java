//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   LimitedCertainConflictsTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.util.BitSet;
import java.util.List;

import gnu.trove.TIntStack;

import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>An implementation of the <I>Certain Conflicts Rule</I>.</P>
 *
 * <P>This rule identifies blocking states and some other states representing
 * certain conflicts in a given automaton, and replaces these states by
 * a single blocking states. The following properties are used to approximate
 * the <I>set of certain conflicts</I>.</P>
 *
 * <UL>
 * <LI>Every blocking state is a state of certain conflicts.</LI>
 * <LI>Every state with an outgoing silent transition to a state of certain
 *     conflicts also is a state of certain conflicts.</LI>
 * <LI>If a state&nbsp;<I>s</I> has an outgoing transition labelled by
 *     event&nbsp;<I>e</I> to a state of certain conflicts, or if such a
 *     transition  is reachable from <I>s</I> via  a sequence of silent
 *     transitions, then all other transitions from&nbsp;<I>s</I>
 *     labelled&nbsp;<I>e</I> can be removed.</LI>
 * </UL>
 *
 * <P>As transitions are removed, new blocking states may emerge, so the
 * above properties are re-evaluated repeatedly until saturation.</P>
 *
 * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional Verification
 * in Supervisory Control. SIAM Journal of Control and Optimization,
 * 48(3), 1914-1938, 2009.</P>
 *
 * @author Robi Malik
 */

public class LimitedCertainConflictsTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#######################################################################
  //# Constructors
  public LimitedCertainConflictsTRSimplifier()
  {
  }

  public LimitedCertainConflictsTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_ALL;
  }

  public boolean run()
    throws AnalysisException
  {
    setUp();
    final int defaultID = getDefaultMarkingID();
    if (defaultID < 0) {
      return false;
    }
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mPredecessorsIterator = rel.createPredecessorsReadOnlyIterator();
    findCoreachableStates();
    int numReachable = rel.getNumberOfReachableStates();
    if (mCoreachableStates.cardinality() == rel.getNumberOfReachableStates()) {
      return false;
    }
    final int tauID = EventEncoding.TAU;
    final int numStates = rel.getNumberOfStates();
    final TransitionIterator closureIter =
      rel.createPredecessorsTauClosureIterator();
    final TransitionIterator succIter = rel.createSuccessorsReadOnlyIterator();
    boolean result = false;
    boolean modified;
    do {
      modified = false;
      for (int state = 0; state < numStates; state++) {
        if (!mCoreachableStates.get(state) && rel.isReachable(state)) {
          // check for tau-transitions to certain conflicts
          mUnvisitedStates.push(state);
          while (mUnvisitedStates.size() > 0) {
            final int popped = mUnvisitedStates.pop();
            mPredecessorsIterator.reset(popped, tauID);
            while (mPredecessorsIterator.advance()) {
              final int pred = mPredecessorsIterator.getCurrentSourceState();
              if (mCoreachableStates.get(pred)) {
                modified = true;
                mCoreachableStates.clear(pred);
                mUnvisitedStates.push(pred);
                rel.removeOutgoingTransitions(pred);
                rel.setMarked(pred, defaultID, false);
              }
            }
          }
          // check for proper event transitions to certain conflicts
          mPredecessorsIterator.reset(state, -1);
          while (mPredecessorsIterator.advance()) {
            final int event = mPredecessorsIterator.getCurrentEvent();
            final int pred = mPredecessorsIterator.getCurrentSourceState();
            if (event != tauID && mCoreachableStates.get(pred)) {
              closureIter.resetState(pred);
              while (closureIter.advance()) {
                final int ppred = closureIter.getCurrentSourceState();
                if (ppred == pred) {
                  succIter.reset(pred, event);
                  while (succIter.advance()) {
                    if (succIter.getCurrentTargetState() != state) {
                      rel.removeOutgoingTransitions(pred, event);
                      rel.addTransition(pred, event, state);
                      modified = true;
                      break;
                    }
                  }
                } else {
                  modified |= rel.removeOutgoingTransitions(ppred, event);
                }
              }
            }
          }
        }
      }
      if (modified) {
        result = true;
        findCoreachableStates();
      }
    } while (modified);

    rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    if (result) {
      if (rel.checkReachability()) {
        numReachable = 0;
        for (int state = 0; state < numStates; state++) {
          if (rel.isReachable(state)) {
            numReachable++;
          } else {
            mCoreachableStates.clear(state);
          }
        }
      }
    }

    final int numCoreachable = mCoreachableStates.cardinality();
    if (numCoreachable == numReachable - 1) {
      // Only one state of certain conflicts. No result partition,
      // but let us try to add selfloops and remove events.
      final int bstate = mCoreachableStates.nextClearBit(0);
      succIter.reset(bstate, -1);
      result |= succIter.advance();
      final int numEvents = rel.getNumberOfProperEvents();
      for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
        if (rel.isUsedEvent(event)) {
          rel.addTransition(bstate, event, bstate);
        }
      }
      result |= rel.removeProperSelfLoopEvents();
      rel.removeOutgoingTransitions(bstate);
    } else {
      // More than one state of certain conflicts.
      // Create a partition that can be applied separately.
      result = true;
      final int numClasses = numCoreachable + 1;
      final int[][] partition = new int[numClasses][];
      final int numBlocking = numReachable - numCoreachable;
      final int[] bclazz = new int[numBlocking];
      int bindex = 0;
      int cindex = 0;
      for (int state = 0; state < numStates; state++) {
        if (mCoreachableStates.get(state)) {
          final int[] clazz = new int[1];
          clazz[0] = state;
          partition[cindex++] = clazz;
        } else if (rel.isReachable(state)) {
          bclazz[bindex++] = state;
        }
      }
      partition[cindex] = bclazz;
      setResultPartitionArray(partition);
      applyResultPartitionAutomatically();
    }
    return result;
  }

  @Override
  public void reset()
  {
    mPredecessorsIterator = null;
    mCoreachableStates = null;
    mUnvisitedStates = null;
    super.reset();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.op.AbstractTRSimplifier
  @Override
  protected void applyResultPartition()
  throws AnalysisException
  {
    // 1. Remove all transitions originating from certain conflicts states.
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final List<int[]> partition = getResultPartition();
    final int end = partition.size();
    final int[] bclass = partition.listIterator(end).previous();
    for (final int state : bclass) {
      rel.removeOutgoingTransitions(state);
    }
    // 2. Apply the partition
    super.applyResultPartition();
    // 3. Add selfloops to certain conflicts and try to remove events
    rel.removeTauSelfLoops();
    final int bstate = end - 1;
    final int numEvents = rel.getNumberOfProperEvents();
    for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
      if (rel.isUsedEvent(event)) {
        rel.addTransition(bstate, event, bstate);
      }
    }
    rel.removeProperSelfLoopEvents();
    rel.removeOutgoingTransitions(bstate);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void findCoreachableStates()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int defaultID = getDefaultMarkingID();
    if (mCoreachableStates == null) {
      mCoreachableStates = new BitSet(numStates);
      mUnvisitedStates = new TIntStack();
    } else {
      mCoreachableStates.clear();
    }
    for (int state = 0; state < numStates; state++) {
      if (rel.isMarked(state, defaultID) &&
          rel.isReachable(state) &&
          !mCoreachableStates.get(state)) {
        mCoreachableStates.set(state);
        mUnvisitedStates.push(state);
        while (mUnvisitedStates.size() > 0) {
          final int popped = mUnvisitedStates.pop();
          mPredecessorsIterator.resetState(popped);
          while (mPredecessorsIterator.advance()) {
            final int pred = mPredecessorsIterator.getCurrentSourceState();
            if (rel.isReachable(pred) && !mCoreachableStates.get(pred)) {
              mCoreachableStates.set(pred);
              mUnvisitedStates.push(pred);
            }
          }
        }
      }
    }
  }


  //#########################################################################
  //# Data Members
  private BitSet mCoreachableStates;
  private TIntStack mUnvisitedStates;
  private TransitionIterator mPredecessorsIterator;

}
