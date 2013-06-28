//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   ActiveEventsTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.set.hash.TIntHashSet;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.HashFunctions;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersIntHashingStrategy;
import net.sourceforge.waters.analysis.tr.WatersIntIntHashMap;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonTools;


/**
 * <P>An implementation of the <I>Active Events Rule</I>.</P>
 *
 * <P>This rule merges all states that are incoming equivalent and have
 * equal sets of eligible events.</P>
 *
 * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional Verification
 * in Supervisory Control. SIAM Journal of Control and Optimization,
 * 48(3), 1914-1938, 2009.</P>
 *
 * @author Robi Malik
 */

public class ActiveEventsTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#######################################################################
  //# Constructors
  public ActiveEventsTRSimplifier()
  {
  }

  public ActiveEventsTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions (including stored silent transitions of the
   * transitive closure) that will be stored.
   * @param limit
   *          The new transition limit, or {@link Integer#MAX_VALUE} to allow an
   *          unlimited number of transitions.
   */
  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
  }

  /**
   * Gets the transition limit.
   * @see #setTransitionLimit(int) setTransitionLimit()
   */
  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_ALL;
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractMarkingTRSimplifier
  @Override
  public boolean isDeadlockAware()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final WatersIntHashingStrategy strategy = new ActiveEventsStateHash();
    final WatersIntIntHashMap map =
      new WatersIntIntHashMap(numStates, IntListBuffer.NULL, strategy);
    final IntListBuffer prepartition = new IntListBuffer();
    final int[] lists = new int[numStates];
    for (int state = 0; state < numStates; state++) {
      if (rel.isReachable(state)) {
        checkAbort();
        int list = map.get(state);
        if (list == IntListBuffer.NULL) {
          list = prepartition.createList();
          map.put(state, list);
          lists[state] = list;
        }
        prepartition.append(list, state);
      }
    }
    final int numClasses = map.size();
    if (numClasses == rel.getNumberOfReachableStates()) {
      return false;
    } else {
      final int[][] partition = new int[numClasses][];
      int index = 0;
      for (int state = 0; state < numStates; state++) {
        final int list = lists[state];
        if (list != IntListBuffer.NULL) {
          checkAbort();
          partition[index++] = prepartition.toArray(list);
        }
      }
      setResultPartitionArray(partition);
      applyResultPartitionAutomatically();
      return true;
    }
  }

  @Override
  protected void applyResultPartition()
    throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.removeTauSelfLoops();
    removeProperSelfLoopEvents();
  }


  //#########################################################################
  //# Inner Class ActiveEventsStateHash
  private class ActiveEventsStateHash implements WatersIntHashingStrategy
  {

    //#######################################################################
    //# Constructor
    private ActiveEventsStateHash()
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final TauClosure forwardTauClosure =
        rel.createSuccessorsTauClosure(mTransitionLimit);
      mForwardsTauClosureIterator = forwardTauClosure.createIterator();
      mForwardsEventIterator = rel.createSuccessorsReadOnlyIterator();
      final TauClosure backwardTauClosure =
        rel.createPredecessorsTauClosure(mTransitionLimit);
      mBackwardsTauClosureIterator1 = backwardTauClosure.createIterator();
      mBackwardsEventIterator = rel.createPredecessorsReadOnlyIterator();
      mBackwardsTauClosureIterator2 = backwardTauClosure.createIterator();
      mCurrentSet1 = new TIntHashSet();
      mCurrentSet2 = new TIntHashSet();
      mPreviousRoot = -1;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.abstraction.WatersIntHashingStrategy
    @Override
    public int computeHashCode(final int root)
    {
      if (root == mPreviousRoot) {
        return mCachedResult;
      } else {
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int numProperEvents = rel.getNumberOfProperEvents();
        final int[] props = getPropositions();
        int result = 0;
        mCurrentSet1.clear();
        mForwardsTauClosureIterator.resetState(root);
        while (mForwardsTauClosureIterator.advance()) {
          final int state = mForwardsTauClosureIterator.getCurrentTargetState();
          for (final int prop : props) {
            if (rel.isMarked(state, prop)) {
              final int event = numProperEvents + prop;
              if (mCurrentSet1.add(event)) {
                result += HashFunctions.hash(event);
              }
            }
          }
          mForwardsEventIterator.resetState(state);
          while (mForwardsEventIterator.advance()) {
            final int event = mForwardsEventIterator.getCurrentEvent();
            if (event != EventEncoding.TAU && mCurrentSet1.add(event)) {
              result += HashFunctions.hash(event);
            }
          }
        }
        result *= 31;
        final int numStates = rel.getNumberOfStates();
        final int shift = AutomatonTools.log2(numStates);
        mCurrentSet1.clear();
        mBackwardsTauClosureIterator1.resetState(root);
        while (mBackwardsTauClosureIterator1.advance()) {
          final int state1 =
            mBackwardsTauClosureIterator1.getCurrentSourceState();
          if (rel.isInitial(state1)) {
            final int code = numProperEvents << shift;
            if (mCurrentSet1.add(code)) {
              result += HashFunctions.hash(code);
            }
          }
          mBackwardsEventIterator.resetState(state1);
          while (mBackwardsEventIterator.advance()) {
            final int event = mBackwardsEventIterator.getCurrentEvent();
            if (event != EventEncoding.TAU) {
              final int eshift = event << shift;
              final int state2 =
                mBackwardsEventIterator.getCurrentSourceState();
              mBackwardsTauClosureIterator2.resetState(state2);
              while (mBackwardsTauClosureIterator2.advance()) {
                final int state =
                  mBackwardsTauClosureIterator2.getCurrentSourceState();
                final int code = state | eshift;
                if (mCurrentSet1.add(code)) {
                  result += HashFunctions.hash(code);
                }
              }
            }
          }
        }
        mCurrentSet1.clear();
        mPreviousRoot = root;
        mCachedResult = result;
        return result;
      }
    }

    @Override
    public boolean equals(final int root1, final int root2)
    {
      try {
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int numProperEvents = rel.getNumberOfProperEvents();
        final int[] props = getPropositions();
        mCurrentSet1.clear();
        mCurrentSet2.clear();
        mForwardsTauClosureIterator.resetState(root1);
        while (mForwardsTauClosureIterator.advance()) {
          final int state =
            mForwardsTauClosureIterator.getCurrentTargetState();
          for (final int prop : props) {
            if (rel.isMarked(state, prop)) {
              final int event = numProperEvents + prop;
              mCurrentSet1.add(event);
            }
          }
          mForwardsEventIterator.resetState(state);
          while (mForwardsEventIterator.advance()) {
            final int event = mForwardsEventIterator.getCurrentEvent();
            if (event != EventEncoding.TAU) {
              mCurrentSet1.add(event);
            }
          }
        }
        mForwardsTauClosureIterator.resetState(root2);
        while (mForwardsTauClosureIterator.advance()) {
          final int state =
            mForwardsTauClosureIterator.getCurrentTargetState();
          for (final int prop : props) {
            if (rel.isMarked(state, prop)) {
              final int event = numProperEvents + prop;
              if (!mCurrentSet1.contains(event)) {
                return false;
              }
              mCurrentSet2.add(event);
            }
          }
          mForwardsEventIterator.resetState(state);
          while (mForwardsEventIterator.advance()) {
            final int event = mForwardsEventIterator.getCurrentEvent();
            if (event != EventEncoding.TAU) {
              if (!mCurrentSet1.contains(event)) {
                return false;
              }
              mCurrentSet2.add(event);
            }
          }
        }
        if (mCurrentSet1.size() != mCurrentSet2.size()) {
          return false;
        }
        final int numStates = rel.getNumberOfStates();
        final int shift = AutomatonTools.log2(numStates);
        mCurrentSet1.clear();
        mCurrentSet2.clear();
        mBackwardsTauClosureIterator1.resetState(root1);
        while (mBackwardsTauClosureIterator1.advance()) {
          final int state1 =
            mBackwardsTauClosureIterator1.getCurrentSourceState();
          if (rel.isInitial(state1)) {
            final int code = numProperEvents << shift;
            mCurrentSet1.add(code);
          }
          mBackwardsEventIterator.resetState(state1);
          while (mBackwardsEventIterator.advance()) {
            final int event = mBackwardsEventIterator.getCurrentEvent();
            if (event != EventEncoding.TAU) {
              final int eshift = event << shift;
              final int state2 =
                mBackwardsEventIterator.getCurrentSourceState();
              mBackwardsTauClosureIterator2.resetState(state2);
              while (mBackwardsTauClosureIterator2.advance()) {
                final int state =
                  mBackwardsTauClosureIterator2.getCurrentSourceState();
                final int code = state | eshift;
                mCurrentSet1.add(code);
              }
            }
          }
        }
        mBackwardsTauClosureIterator1.resetState(root2);
        while (mBackwardsTauClosureIterator1.advance()) {
          final int state1 =
            mBackwardsTauClosureIterator1.getCurrentSourceState();
          if (rel.isInitial(state1)) {
            final int code = numProperEvents << shift;
            if (!mCurrentSet1.contains(code)) {
              return false;
            }
            mCurrentSet2.add(code);
          }
          mBackwardsEventIterator.resetState(state1);
          while (mBackwardsEventIterator.advance()) {
            final int event = mBackwardsEventIterator.getCurrentEvent();
            if (event != EventEncoding.TAU) {
              final int eshift = event << shift;
              final int state2 =
                mBackwardsEventIterator.getCurrentSourceState();
              mBackwardsTauClosureIterator2.resetState(state2);
              while (mBackwardsTauClosureIterator2.advance()) {
                final int state =
                  mBackwardsTauClosureIterator2.getCurrentSourceState();
                final int code = state | eshift;
                if (!mCurrentSet1.contains(code)) {
                  return false;
                }
                mCurrentSet2.add(code);
              }
            }
          }
        }
        return mCurrentSet1.size() == mCurrentSet2.size();
      } finally {
        mCurrentSet1.clear();
        mCurrentSet2.clear();
      }
    }


    //#######################################################################
    //# Data Members
    private final TransitionIterator mForwardsTauClosureIterator;
    private final TransitionIterator mForwardsEventIterator;
    private final TransitionIterator mBackwardsTauClosureIterator1;
    private final TransitionIterator mBackwardsEventIterator;
    private final TransitionIterator mBackwardsTauClosureIterator2;
    private final TIntHashSet mCurrentSet1;
    private final TIntHashSet mCurrentSet2;

    private int mPreviousRoot;
    private int mCachedResult;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#######################################################################
  //# Data Members
  private int mTransitionLimit = Integer.MAX_VALUE;

}

