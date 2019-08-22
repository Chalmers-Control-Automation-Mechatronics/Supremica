//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;
import gnu.trove.stack.array.TLongArrayStack;

import java.util.Arrays;

import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntSetBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersIntIterator;
import net.sourceforge.waters.analysis.tr.WatersIntPairStack;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;


/**
 * @author Robi Malik
 */

public class SmallCliqueSupervisorReductionTRSimplifier
  extends AbstractSupervisorReductionTRSimplifier
{

  //#########################################################################
  //# Constructors
  public SmallCliqueSupervisorReductionTRSimplifier()
  {
    setAppliesPartitionAutomatically(true);
  }

  public SmallCliqueSupervisorReductionTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    super(rel);
    setAppliesPartitionAutomatically(true);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public boolean isPartitioning()
  {
    return false;
  }

  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_ALL;
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    mCliqueDB = new IntSetBuffer(numStates, numStates);
  }

  @Override
  public boolean runSimplifier() throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int size = rel.getNumberOfReachableStates();
    if (size < 2000) {
      setUpCompatibilityRelation();
      System.out.print(mCompatibilityRelation.size());
      System.out.print(',');
      System.out.print(mCompatibilityRelation.getNumberOfAllCompatibleStates());
      System.out.print(',');
      final int numStates = rel.getNumberOfStates();
      final TIntArrayList list = new TIntArrayList(1);
      for (int s = 0; s < numStates; s++) {
        if (rel.isInitial(s) && mCompatibilityRelation.isRelevant(s)) {
          list.add(s);
        }
      }
      final int init = mCompatibilityRelation.getUniqueCover(list);
      System.out.print(mCliqueDB.size(init));
      System.out.print(',');
      System.out.flush();

      final int numEvents = rel.getNumberOfProperEvents();
      final CliqueQueue queue = new CliqueQueue();
      queue.addNewClique(init);
      main:
      while (!queue.isEmpty()) {
        final int clique = queue.closeFirst();
        for (int e = 0; e < numEvents; e++) {
          final byte status = rel.getProperEventStatus(e);
          if (EventStatus.isUsedEvent(status)) {
            list.clear();
            collectSuccessors(clique, e, list);
            if (!list.isEmpty()) {
              final int succ = mCompatibilityRelation.getUniqueCover(list);
              queue.addNewClique(succ);
              if (!queue.isFirstClosedClique(clique)) {
                continue main;
              }
            }
          }
        }
      }
      System.out.println(queue.mClosedCliques.size());
    }
    return false;
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mCompatibilityRelation = null;
    mAllCompatibleSuccessors = null;
    mCliqueDB = null;
  }


  //#########################################################################
  //# Compatibility Relation
  private void setUpCompatibilityRelation()
  {
    final TIntArrayList supervisedEvents = getSupervisedEvents();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final TIntArrayList enabled = new TIntArrayList(numStates);
    final TIntArrayList disabled = new TIntArrayList(numStates);
    final int dumpIndex = rel.getDumpStateIndex();
    final TransitionIterator iter =
      rel.createAllTransitionsReadOnlyIterator();
    mCompatibilityRelation = new CompatibilityRelation();
    final TLongArrayStack stack = new TLongArrayStack(numStates);
    for (int i = 0; i < supervisedEvents.size(); i++) {
      enabled.clear();
      disabled.clear();
      final int e = supervisedEvents.get(i);
      iter.resetEvent(e);
      while (iter.advance()) {
        final int s = iter.getCurrentSourceState();
        if (iter.getCurrentTargetState() == dumpIndex) {
          disabled.add(s);
        } else {
          enabled.add(s);
        }
      }
      if (!disabled.isEmpty()) {
        for (int j = 0; j < enabled.size(); j++) {
          final int s1 = enabled.get(j);
          for (int k = 0; k < disabled.size(); k++) {
            final int s2 = disabled.get(k);
            propagateIncompatible(stack, s1, s2);
          }
        }
      }
    }
    mCompatibilityRelation.filterStatesCompatibleWithAll();
    setUpAllCompatibleSuccessors();
  }

  private void propagateIncompatible(final TLongArrayStack stack,
                                     final int state1,
                                     final int state2)
  {
    if (mCompatibilityRelation.pushIncompatibleIfNew(stack, state1, state2)) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final TransitionIterator iter1 = rel.createPredecessorsReadOnlyIterator();
      final TransitionIterator iter2 = rel.createPredecessorsReadOnlyIterator();
      while (stack.size() > 0) {
        final long pair = stack.pop();
        final int t1 = WatersIntPairStack.getLo(pair);
        iter1.resetState(t1);
        final int t2 = WatersIntPairStack.getHi(pair);
        while (iter1.advance()) {
          final int s1 = iter1.getCurrentSourceState();
          final int event = iter1.getCurrentEvent();
          iter2.reset(t2, event);
          while (iter2.advance()) {
            final int s2 = iter2.getCurrentSourceState();
            mCompatibilityRelation.pushIncompatibleIfNew(stack, s1, s2);
          }
        }
      }
    }
  }

  private void setUpAllCompatibleSuccessors()
  {
    final int[] allCompatibles =
      mCompatibilityRelation.getAllCompatibleStates();
    if (allCompatibles.length > 0) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numEvents = rel.getNumberOfProperEvents();
      mAllCompatibleSuccessors = new int[numEvents][];
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      for (int e = 0; e < numEvents; e++) {
        final byte status = rel.getProperEventStatus(e);
        if (EventStatus.isUsedEvent(status)) {
          final TIntHashSet successors = new TIntHashSet();
          for (final int s : allCompatibles) {
            iter.reset(s, e);
            while (iter.advance()) {
              final int t = iter.getCurrentTargetState();
              if (mCompatibilityRelation.isRelevant(t)) {
                successors.add(t);
              }
            }
          }
          if (!successors.isEmpty()) {
            mAllCompatibleSuccessors[e] = successors.toArray();
            Arrays.sort(mAllCompatibleSuccessors[e]);
          }
        }
      }
    }
  }


  //#########################################################################
  //# State Expansion
  private void collectSuccessors(final int clique,
                                 final int event,
                                 final TIntArrayList output)
  {
    final TIntHashSet set;
    if (mAllCompatibleSuccessors != null &&
        mAllCompatibleSuccessors[event] != null) {
      set = new TIntHashSet(mAllCompatibleSuccessors[event]);
    } else {
      set = new TIntHashSet();
    }
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator transIter = rel.createSuccessorsReadOnlyIterator();
    final WatersIntIterator cliqueIter = mCliqueDB.iterator(clique);
    while (cliqueIter.advance()) {
      final int s = cliqueIter.getCurrentData();
      transIter.reset(s, event);
      while (transIter.advance()) {
        final int t = transIter.getCurrentTargetState();
        if (mCompatibilityRelation.isRelevant(t)) {
          set.add(t);
        }
      }
    }
    output.addAll(set);
    output.sort();
  }


  //#########################################################################
  //# Inner Class CompatibilityRelation
  private class CompatibilityRelation
  {
    //#######################################################################
    //# Constructor
    private CompatibilityRelation()
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      final int dumpIndex = rel.getDumpStateIndex();
      final TIntArrayList list = new TIntArrayList(numStates - 1);
      for (int s = 0; s < numStates; s++) {
        if (rel.isReachable(s) && s != dumpIndex) {
          list.add(s);
        }
      }
      mRelevantStates = list.toArray();
      mIncompatiblePairs = new TLongHashSet(numStates);
    }

    //#######################################################################
    //# Simple Access
    private int size()
    {
      if (mAllCompatibleStates == null) {
        return mRelevantStates.length;
      } else {
        return mRelevantStates.length + mAllCompatibleStates.length;
      }
    }

    private boolean isRelevant(final int state)
    {
      return mRelevant[state];
    }

    private boolean isCompatible(final int state1, final int state2)
    {
      if (state1 == state2) {
        return true;
      } else {
        final long pair = WatersIntPairStack.createPair(state1, state2);
        return isCompatible(pair);
      }
    }

    private boolean isCompatible(final long pair)
    {
      return !mIncompatiblePairs.contains(pair);
    }

    private boolean addKnownDistinctIncompatible(final long pair)
    {
      return mIncompatiblePairs.add(pair);
    }

    private WatersIntIterator getNeighboursIterator(final int state)
    {
      return new NeighboursIterator(state);
    }

    private int getNumberOfAllCompatibleStates()
    {
      return mAllCompatibleStates.length;
    }

    private int[] getAllCompatibleStates()
    {
      return mAllCompatibleStates;
    }

    //#######################################################################
    //# Advanced Access
    private boolean pushIncompatibleIfNew(final TLongArrayStack stack,
                                          final int state1,
                                          final int state2)
    {
      if (state1 != state2) {
        final long pair = WatersIntPairStack.createPair(state1, state2);
        if (addKnownDistinctIncompatible(pair)) {
          stack.push(pair);
          return true;
        }
      }
      return false;
    }

    private void filterStatesCompatibleWithAll()
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      mRelevant = new boolean[numStates];
      final int size = mRelevantStates.length;
      final TIntArrayList allCompatible = new TIntArrayList(size);
      final TIntArrayList remaining = new TIntArrayList(size);
      for (final int s : mRelevantStates) {
        if (isCompatibleWithAll(s)) {
          allCompatible.add(s);
        } else {
          remaining.add(s);
          mRelevant[s] = true;
        }
      }
      mAllCompatibleStates = allCompatible.toArray();
      mRelevantStates = remaining.toArray();
      mBuffer1 = new TIntArrayList(remaining.size());
      mBuffer2 = new TIntArrayList(remaining.size());
    }

    private boolean isCompatibleWithAll(final int state)
    {
      for (final int s : mRelevantStates) {
        if (!isCompatible(state, s)) {
          return false;
        }
      }
      return true;
    }

    private void collectCommonNeighbours(final TIntArrayList clique,
                                         final TIntArrayList neighbours)
    {
      final int state1 = clique.get(0);
      final WatersIntIterator iter = getNeighboursIterator(state1);
      outer:
      while (iter.advance()) {
        final int s1 = iter.getCurrentData();
        for (int i = 1; i < clique.size(); i++) {
          final int s2 = clique.get(i);
          if (!isCompatible(s1, s2)) {
            continue outer;
          }
        }
        neighbours.add(s1);
      }
    }

    private int getUniqueCover(final TIntArrayList clique)
    {
      mBuffer1.clear();
      mBuffer2.clear();
      if (!clique.isEmpty()) {
        collectCommonNeighbours(clique, mBuffer1);
        outer:
        for (int i = 0; i < mBuffer1.size(); i++) {
          final int s1 = mBuffer1.get(i);
          for (int j = 0; j < mBuffer1.size(); j++) {
            final int s2 = mBuffer1.get(j);
            if (!isCompatible(s1, s2)) {
              continue outer;
            }
          }
          mBuffer2.add(s1);
        }
      }
      return mCliqueDB.add(mBuffer2);
    }

    //#######################################################################
    //# Data Members
    private final TLongHashSet mIncompatiblePairs;
    private int[] mRelevantStates;
    private int[] mAllCompatibleStates;
    private boolean[] mRelevant;
    private TIntArrayList mBuffer1;
    private TIntArrayList mBuffer2;
  }


  //#########################################################################
  //# Inner Class NeighboursIterator
  private class NeighboursIterator implements WatersIntIterator
  {
    //#######################################################################
    //# Constructor
    private NeighboursIterator(final int state)
    {
      mState = state;
      mCurrentIndex = -1;
    }

    private NeighboursIterator(final NeighboursIterator iter)
    {
      mState = iter.mState;
      mCurrentIndex = iter.mCurrentIndex;
    }


    //#######################################################################
    //# Constructor
    @Override
    public NeighboursIterator clone()
    {
      return new NeighboursIterator(this);
    }

    @Override
    public void reset()
    {
      mCurrentIndex = -1;
    }

    @Override
    public boolean advance()
    {
      int next = mCurrentIndex + 1;
      final int end = mCompatibilityRelation.mRelevantStates.length;
      while (next < end) {
        final int state = mCompatibilityRelation.mRelevantStates[next];
        final long pair = WatersIntPairStack.createPair(mState, state);
        if (mCompatibilityRelation.isCompatible(pair)) {
          mCurrentIndex = next;
          return true;
        }
        next++;
      }
      mCurrentIndex = end;
      return false;
    }

    @Override
    public int getCurrentData()
    {
      return mCompatibilityRelation.mRelevantStates[mCurrentIndex];
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) + " does not support removal!");
    }

    //#######################################################################
    //# Data Members
    private final int mState;
    private int mCurrentIndex;
  }


  //#########################################################################
  //# Inner Class CliqueQueue
  private class CliqueQueue
  {
    //#######################################################################
    //# Simple Access
    private boolean addNewClique(final int newClique)
    {
      boolean removedSome = false;
      for (int i = 0; i < 2; i++) {
        final TIntIterator iter =
          i == 0 ? mClosedCliques.iterator() : mOpenCliques.iterator();
        while (iter.hasNext()) {
          final int existingClique = iter.next();
          if (!removedSome &&
              mCliqueDB.containsAll(existingClique, newClique)) {
            return false;
          } else if (mCliqueDB.containsAll(newClique, existingClique)) {
            iter.remove();
            removedSome = true;
          }
        }
      }
      mOpenCliques.add(newClique);
      return true;
    }

    private boolean isEmpty()
    {
      return mOpenCliques.isEmpty();
    }

    private boolean isFirstClosedClique(final int clique)
    {
      if (mClosedCliques.isEmpty()) {
        return false;
      } else {
        return mClosedCliques.get(0) == clique;
      }
    }

    private int closeFirst()
    {
      final TIntIterator iter = mOpenCliques.iterator();
      int best = iter.next();
      int bestSize = mCliqueDB.size(best);
      while (iter.hasNext()) {
        final int clique = iter.next();
        final int size = mCliqueDB.size(clique);
        if (size > bestSize) {
          best = clique;
          bestSize = size;
        } else if (size == bestSize && clique < best) {
          best = clique;
        }
      }
      mOpenCliques.remove(best);
      if (mClosedCliques.isEmpty()) {
        mClosedCliques.add(best);
      } else {
        mClosedCliques.insert(0, best);
      }
      return best;
    }

    //#######################################################################
    //# Data Members
    private final TIntLinkedList mOpenCliques = new TIntLinkedList();
    private final TIntLinkedList mClosedCliques = new TIntLinkedList();
  }


  //#########################################################################
  //# Data Members
  private CompatibilityRelation mCompatibilityRelation;
  private int[][] mAllCompatibleSuccessors;
  private IntSetBuffer mCliqueDB;


  //#########################################################################
  //# Class Constants

}
