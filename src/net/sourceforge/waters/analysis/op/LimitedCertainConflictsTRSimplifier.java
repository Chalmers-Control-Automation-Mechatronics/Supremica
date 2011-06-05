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

import gnu.trove.TIntArrayList;
import gnu.trove.TIntStack;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonTools;


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
    } else if (mCertainConflictsInfo != null) {
      mCertainConflictsInfo.setBlockingStates(mCoreachableStates);
    }
    final int tauID = EventEncoding.TAU;
    final int numStates = rel.getNumberOfStates();
    final int shift = AutomatonTools.log2(numStates);
    final int mask = (1 << shift) - 1;
    final int numEvents = rel.getNumberOfProperEvents();
    final int eshift = AutomatonTools.log2(numEvents);
    final int root = 1 << (shift + eshift);
    final TransitionIterator closureIter =
      rel.createPredecessorsTauClosureIterator();
    final TransitionIterator succIter = rel.createSuccessorsReadOnlyIterator();
    boolean result = false;
    boolean modified;
    final TIntArrayList victims = new TIntArrayList();
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
                mCoreachableStates.clear(pred);
                mUnvisitedStates.push(pred);
                victims.add(pred);
              }
            }
          }
          if (!victims.isEmpty()) {
            mHasRemovedTransitions = modified = true;
            for (int index = 0; index < victims.size(); index++) {
              final int victim = victims.get(index);
              rel.removeOutgoingTransitions(victim);
              rel.setMarked(victim, defaultID, false);
              if (mCertainConflictsInfo != null) {
                mCertainConflictsInfo.
                  addCertainConflictTransition(victim, tauID, state);
              }
            }
            victims.clear();
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
                succIter.reset(ppred, event);
                while (succIter.advance()) {
                  if (ppred != pred) {
                    final int code = (event << shift) | ppred;
                    victims.add(code);
                    break;
                  } else if (succIter.getCurrentTargetState() != state) {
                    final int code = root | (event << shift) | ppred;
                    victims.add(code);
                    break;
                  }
                }
              }
              if (mCertainConflictsInfo != null) {
                mCertainConflictsInfo.
                  addCertainConflictTransition(pred, event, state);
              }
            }
          }
          if (!victims.isEmpty()) {
            mHasRemovedTransitions = modified = true;
            for (int index = 0; index < victims.size(); index++) {
              final int victim = victims.get(index);
              final int event = (victim & ~root) >>> shift;
              final int pred = victim & mask;
              rel.removeOutgoingTransitions(pred, event);
              if ((victim & root) != 0) {
                rel.addTransition(pred, event, state);
              }
            }
            victims.clear();
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
    if (mCertainConflictsInfo != null) {
      mCertainConflictsInfo.setCertainConflictStates(mCoreachableStates);
    }

    final int numCoreachable = mCoreachableStates.cardinality();
    if (numCoreachable == numReachable - 1) {
      // Only one state of certain conflicts. No result partition,
      // but let us try to add selfloops and remove events.
      final int bstate = mCoreachableStates.nextClearBit(0);
      succIter.reset(bstate, -1);
      result |= succIter.advance();
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
    mCertainConflictsInfo = null;
    mPredecessorsIterator = null;
    mCoreachableStates = null;
    mUnvisitedStates = null;
    super.reset();
  }


  //#########################################################################
  //# Specific Access
  public boolean hasRemovedTransitions()
  {
    return mHasRemovedTransitions;
  }

  public LimitedCertainConflictsInfo getCertainConflictsInfo()
  {
    return mCertainConflictsInfo;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.op.AbstractTRSimplifier
  @Override
  protected void setUp()
  throws AnalysisException
  {
    super.setUp();
    if (!getAppliesPartitionAutomatically()) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      mCertainConflictsInfo = new LimitedCertainConflictsInfo(rel);
    }
    mHasRemovedTransitions = false;
  }

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
  private LimitedCertainConflictsInfo mCertainConflictsInfo;
  private boolean mHasRemovedTransitions;

  private BitSet mCoreachableStates;
  private TIntStack mUnvisitedStates;
  private TransitionIterator mPredecessorsIterator;

}
