//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.HashFunctions;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersIntHashingStrategy;
import net.sourceforge.waters.analysis.tr.WatersIntIntHashMap;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonTools;


/**
 * <P>An implementation of the <I>Silent Continuation Rule</I>.</P>
 *
 * <P>This rule merges all states that are incoming equivalent and have
 * at least one outgoing silent transition.</P>
 *
 * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional Verification
 * in Supervisory Control. SIAM Journal of Control and Optimization,
 * 48(3), 1914-1938, 2009.</P>
 *
 * @author Robi Malik
 */

public class SilentContinuationTRSimplifier
  extends AbstractTRSimplifier
{

  //#######################################################################
  //# Constructors
  public SilentContinuationTRSimplifier()
  {
  }

  public SilentContinuationTRSimplifier(final ListBufferTransitionRelation rel)
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
  //# Interface net.sourceforge.waters.analysis.abstraction.
  //# TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_PREDECESSORS;
  }

  @Override
  public boolean isPartitioning()
  {
    return true;
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, true, false);
    return setStatistics(stats);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if ((rel.getProperEventStatus(EventEncoding.TAU) &
         EventStatus.STATUS_UNUSED) != 0) {
      return false;
    }
    final int numStates = rel.getNumberOfStates();
    final BitSet candidates = new BitSet(numStates);
    final TransitionIterator iter =
      rel.createAllTransitionsReadOnlyIterator(EventEncoding.TAU);
    while (iter.advance()) {
      final int source = iter.getCurrentSourceState();
      candidates.set(source);
    }
    final int numCandidates = candidates.cardinality();
    if (numCandidates == 0) {
      return false;
    }
    final WatersIntHashingStrategy strategy =
      new IncomingEquivalenceStateHash();
    final WatersIntIntHashMap map =
      new WatersIntIntHashMap(numCandidates, IntListBuffer.NULL, strategy);
    final IntListBuffer prepartition = new IntListBuffer();
    final int[] lists = new int [numStates];
    for (int state = candidates.nextSetBit(0); state >= 0;
         state = candidates.nextSetBit(state + 1)) {
      checkAbort();
      int list = map.get(state);
      if (list == IntListBuffer.NULL) {
        list = prepartition.createList();
        map.put(state, list);
      }
      prepartition.append(list, state);
      lists[state] = list;
    }
    final int numClasses = map.size();
    if (numClasses == numCandidates) {
      return false;
    } else {
      final int numSingles = numStates - numCandidates;
      final List<int[]> classes =
        new ArrayList<int[]>(numClasses + numSingles);
      for (int state = 0; state < numStates; state++) {
        checkAbort();
        final int list = lists[state];
        if (list != IntListBuffer.NULL) {
          final int first = prepartition.getFirst(list);
          if (state == first) {
            final int[] clazz = prepartition.toArray(list);
            classes.add(clazz);
          }
        } else if (rel.isReachable(state)) {
          final int[] clazz = new int[1];
          clazz[0] = state;
          classes.add(clazz);
        }
      }
      final TRPartition partition = new TRPartition(classes, numStates);
      setResultPartition(partition);
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
    rel.removeProperSelfLoopEvents();
  }


  //#########################################################################
  //# Inner Class IncomingEquavalenceStateHash
  private class IncomingEquivalenceStateHash
    implements WatersIntHashingStrategy
  {

    //#######################################################################
    //# Constructor
    private IncomingEquivalenceStateHash()
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final TauClosure closure = rel.createPredecessorsTauClosure(mTransitionLimit);
      mBackwardsTauClosureIterator1 = closure.createIterator();
      mBackwardsEventIterator = rel.createPredecessorsReadOnlyIterator();
      mBackwardsTauClosureIterator2 = closure.createIterator();
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
        final int numStates = rel.getNumberOfStates();
        final int shift = AutomatonTools.log2(numStates);
        int result = 0;
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
