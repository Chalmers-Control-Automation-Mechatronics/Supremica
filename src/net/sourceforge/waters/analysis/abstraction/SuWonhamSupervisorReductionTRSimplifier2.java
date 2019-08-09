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

import gnu.trove.iterator.TIntByteIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntByteHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;
import gnu.trove.stack.TLongStack;
import gnu.trove.stack.array.TLongArrayStack;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;


/**
 * <P>A transition relation simplifier that implements the Su/Wonham
 * supervisor reduction algorithm.</P>
 *
 * <P>This supervisor reduction simplifier supports the classical
 * supervisor reduction algorithm by Su and Wonham (2004), as well as
 * its localised version by Cai and Wonham (2010). The latter is obtained
 * by configuring the supervised event and invoking supervisor reduction
 * once for each controllable event.</P>
 *
 * <P>
 * <I>References.</I><BR>
 * R. Su and W. Murray Wonham. Supervisor Reduction for Discrete-Event
 * Systems. Discrete Event Dynamic Systems: Theory and Applications,
 * <STRONG>14</STRONG>&nbsp;(1), 31-53, 2004.<BR>
 * Kai Cai and W. M. Wonham. Supervisor Localization: A Top-Down Approach to
 * Distributed Control of Discrete-Event Systems. IEEE Transactions on
 * Automatic Control, <STRONG>55</STRONG>&nbsp;(3), 605-618, 2010.
 * </P>
 *
 * @author Fangqian Qiu, Robi Malik
 */

public class SuWonhamSupervisorReductionTRSimplifier2
  extends AbstractSupervisorReductionTRSimplifier
{

  //#########################################################################
  //# Constructors
  public SuWonhamSupervisorReductionTRSimplifier2()
  {
  }

  public SuWonhamSupervisorReductionTRSimplifier2(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public boolean isPartitioning()
  {
    return true;
  }

  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    mStack = new StackOfPairs();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final int numStates = rel.getNumberOfReachableStates();
    mStates1 = new TIntArrayList(numStates);
    mStates2 = new TIntArrayList(numStates);
    mSuccessors1 = new TIntArrayList(numStates);
    mSuccessors2 = new TIntArrayList(numStates);
  }

  @Override
  public boolean runSimplifier() throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TRPartition partition;
    setUpCompatibilityRelation();
    if (mCompatibiliyRelation.isEmpty()) {
      partition = createOneStatePartition();
      rel.merge(partition);
    } else {
      setUpStateOrder();
      partition = mergeStatesInLexicographicalOrder();
    }
    setResultPartition(partition);
    if (partition != null) {
      rel.removeProperSelfLoopEvents();
      rel.removeRedundantPropositions();
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mCompatibiliyRelation = null;
    mOrderedStates = null;
    mStateOrderIndex = null;
    mStack = null;
    mStates1 = mStates2 = mSuccessors1 = mSuccessors2 = null;
  }


  //#########################################################################
  //# Methods for Supervisor Reduction
  private TRPartition mergeStatesInLexicographicalOrder()
    throws AnalysisAbortException
  {
    ListBufferPartition result = new ListBufferPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    for (mCurrentIndex1 = 0;
         mCurrentIndex1 < mOrderedStates.length;
         mCurrentIndex1++) {
      final int state1 = mOrderedStates[mCurrentIndex1];
      if (!rel.isReachable(state1)) {
        continue;
      }
      for (mCurrentIndex2 = mCurrentIndex1 + 1;
           mCurrentIndex2 < mOrderedStates.length;
           mCurrentIndex2++) {
        final int state2 = mOrderedStates[mCurrentIndex2];
        if (!rel.isReachable(state2)) {
          continue;
        }
        checkAbort();
        final ListBufferPartition partition = checkMergibility(state1, state2);
        // System.out.format("%d/%d - %s\n", state1, state2,
        //                   partition == null ? "failed" : "merged");
        if (partition != null) {
          applyPartition(partition);
          result = result.merge(partition);
        }
      }
    }
    return result.createTRPartition();
  }

  private boolean hasFailedMergibilityCheck(final int state1,
                                            final int state2,
                                            final ListBufferPartition partition)
  {
    int min1 = partition.getClassCode(state1);
    int min2 = partition.getClassCode(state2);
    if (mStateOrderIndex[min1] > mStateOrderIndex[min2]) {
      final int tmp = min1;
      min1 = min2;
      min2 = tmp;
    }
    if (mStateOrderIndex[min1] < mCurrentIndex1) {
      return true;
    } else if (mStateOrderIndex[min1] == mCurrentIndex1) {
      return mStateOrderIndex[min2] < mCurrentIndex2;
    } else {
      return false;
    }
  }

  @SuppressWarnings("unused")
  private TRPartition mergeStatesInLexicographicalOrder2()
    throws AnalysisAbortException
  {
    ListBufferPartition result = null;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    for (mCurrentIndex1 = 1;
         mCurrentIndex1 < mOrderedStates.length;
         mCurrentIndex1++) {
      final int state1 = mOrderedStates[mCurrentIndex1];
      if (!rel.isReachable(state1)) {
        continue;
      }
      for (mCurrentIndex2 = 0;
           mCurrentIndex2 < mCurrentIndex1;
           mCurrentIndex2++) {
        final int state2 = mOrderedStates[mCurrentIndex2];
        if (!rel.isReachable(state2)) {
          continue;
        }
        checkAbort();
        final ListBufferPartition partition = checkMergibility(state1, state2);
        if (partition != null) {
          applyPartition(partition);
          result = result.merge(partition);
        }
      }
    }
    return result.createTRPartition();
  }

  @SuppressWarnings("unused")
  private TRPartition mergeStatesInDiagonalOrder()
    throws AnalysisAbortException
  {
    ListBufferPartition result = null;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int end1 = 2 * mOrderedStates.length;
    for (int sum = 1; sum < end1; sum++) {
      final int start2 = Math.max(sum - mOrderedStates.length, 0);
      final int end2 = (sum + 1) >> 1;
      for (mCurrentIndex1 = start2; mCurrentIndex1 < end2; mCurrentIndex1++) {
        final int state1 = mOrderedStates[mCurrentIndex1];
        if (!rel.isReachable(state1)) {
          continue;
        }
        mCurrentIndex2 = sum - mCurrentIndex1;
        final int state2 = mOrderedStates[mCurrentIndex2];
        if (!rel.isReachable(state2)) {
          continue;
        }
        checkAbort();
        final ListBufferPartition partition = checkMergibility(state1, state2);
        if (partition != null) {
          applyPartition(partition);
          result = result.merge(partition);
        }
      }
    }
    return result.createTRPartition();
  }

  private ListBufferPartition checkMergibility(final int state1, final int state2)
    throws AnalysisAbortException
  {
    final ListBufferPartition partition = new ListBufferPartition();
    mStack.clear();
    mStack.push(state1, state2);
    while (!mStack.isEmpty()) {
      checkAbort();
      final long pair = mStack.pop();
      final int s1 = StackOfPairs.getLo(pair);
      final int s2 = StackOfPairs.getHi(pair);
      if (!mergeAndExpand(s1, s2, partition)) {
        return null;
      }
    }
    return partition;
  }

  private boolean mergeAndExpand(final int state1,
                                 final int state2,
                                 final ListBufferPartition partition)
  {
    if (partition.isEquivalent(state1, state2)) {
      return true;
    } else if (hasFailedMergibilityCheck(state1, state2, partition)) {
      return false;
    } else if (!partition.isClassCompatible(state1, state2)) {
      return false;
    }

    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int clazz1 = partition.getClassCode(state1);
    partition.getStates(clazz1, mStates1);
    final int clazz2 = partition.getClassCode(state2);
    partition.getStates(clazz2, mStates2);
    if (mStateOrderIndex[clazz1] < mStateOrderIndex[clazz2]) {
      partition.mergeClasses(clazz1, clazz2);
    } else {
      partition.mergeClasses(clazz2, clazz1);
    }

    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    final int dumpIndex = rel.getDumpStateIndex();
    final int numEvents = rel.getNumberOfProperEvents();
    eventLoop:
    for (int e = 0; e < numEvents; e++) {
      final byte status = rel.getProperEventStatus(e);
      if (!EventStatus.isUsedEvent(status)) {
        continue;
      }
      mSuccessors1.clear();
      final TIntHashSet set1 = new TIntHashSet();
      for (int i = 0; i < mStates1.size(); i++) {
        final int s1 = mStates1.get(i);
        iter.reset(s1, e);
        if (iter.advance()) {
          final int t1 = iter.getCurrentTargetState();
          if (t1 == dumpIndex) {
            continue eventLoop;
          }
          final int min1 = partition.getClassCode(t1);
          if (set1.add(min1)) {
            mSuccessors1.add(min1);
          }
        }
      }
      if (mSuccessors1.size() == 0) {
        continue;
      }
      mSuccessors2.clear();
      final TIntHashSet set2 = new TIntHashSet();
      for (int i = 0; i < mStates2.size(); i++) {
        final int s2 = mStates2.get(i);
        iter.reset(s2, e);
        if (iter.advance()) {
          final int t2 = iter.getCurrentTargetState();
          final int min2 = partition.getClassCode(t2);
          if (set2.add(min2)) {
            mSuccessors2.add(min2);
          }
        }
      }
      for (int i1 = mSuccessors1.size() - 1; i1 >= 0; i1--) {
        final int t1 = mSuccessors1.get(i1);
        for (int i2 = mSuccessors2.size() - 1; i2 >= 0; i2--) {
          final int t2 = mSuccessors2.get(i2);
          if (t1 == t2) {
            continue;
          } else if (hasFailedMergibilityCheck(t1, t2, partition)) {
            return false;
          } else if (!partition.isClassCompatible(t1, t2)) {
            return false;
          }
          mStack.push(t1, t2);
        }
      }
    }
    return true;
  }


  //#########################################################################
  //# Compatibility Relation
  private void setUpCompatibilityRelation()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TIntArrayList controllableEvents;
    final int supEvent = getSupervisedEvent();
    if (supEvent >= 0) {
      controllableEvents = new TIntArrayList(1);
      controllableEvents.add(supEvent);
    } else {
      final int numEvents = rel.getNumberOfProperEvents();
      controllableEvents = new TIntArrayList(numEvents);
      for (int e = 0; e < numEvents; e++) {
        final byte status = rel.getProperEventStatus(e);
        if (EventStatus.isUsedEvent(status) &&
            EventStatus.isControllableEvent(status)) {
          controllableEvents.add(e);
        }
      }
    }
    mCompatibiliyRelation = new LinkedList<>();
    final int numStates = rel.getNumberOfStates();
    final TIntArrayList enabled = new TIntArrayList(numStates);
    final TIntArrayList disabled = new TIntArrayList(numStates);
    final int dumpIndex = rel.getDumpStateIndex();
    final TransitionIterator iter =
      rel.createAllTransitionsModifyingIterator();
    for (int i = 0; i < controllableEvents.size(); i++) {
      enabled.clear();
      disabled.clear();
      final int e = controllableEvents.get(i);
      iter.resetEvent(e);
      while (iter.advance()) {
        final int s = iter.getCurrentSourceState();
        if (iter.getCurrentTargetState() == dumpIndex) {
          disabled.add(s);
        } else {
          enabled.add(s);
        }
      }
      if (!enabled.isEmpty() && !disabled.isEmpty()) {
        final CompatibilityPair pair = new CompatibilityPair(enabled, disabled);
        addCompatibilityPair(mCompatibiliyRelation, pair);
      }
    }
    Collections.sort(mCompatibiliyRelation);
  }

  private boolean updateCompatibilityRelation(final ListBufferPartition partition)
  {
    List<CompatibilityPair> changedPairs = null;
    Iterator<CompatibilityPair> iter = mCompatibiliyRelation.iterator();
    while (iter.hasNext()) {
      final CompatibilityPair pair = iter.next();
      if (pair.applyPartition(partition)) {
        iter.remove();
        if (changedPairs == null) {
          changedPairs = new LinkedList<>();
          changedPairs.add(pair);
        } else {
          addCompatibilityPair(changedPairs, pair);
        }
      }
    }
    if (changedPairs != null) {
      iter = mCompatibiliyRelation.iterator();
      while (iter.hasNext()) {
        final CompatibilityPair unchanged = iter.next();
        for (final CompatibilityPair changed : changedPairs) {
          if (changed.subsumes(unchanged)) {
            iter.remove();
            break;
          }
        }
      }
      mCompatibiliyRelation.addAll(changedPairs);
      Collections.sort(mCompatibiliyRelation);
      return true;
    } else {
      return false;
    }
  }

  private static boolean addCompatibilityPair
    (final List<CompatibilityPair> relation,
     final CompatibilityPair pair)
  {
    final Iterator<CompatibilityPair> iter = relation.iterator();
    while (iter.hasNext()) {
      final CompatibilityPair existing = iter.next();
      if (existing.subsumes(pair)) {
        return false;
      } else if (pair.subsumes(existing)) {
        iter.remove();
      }
    }
    relation.add(pair);
    return true;
  }

  private boolean isStateCompatible(final int state1,
                                    final int state2)
  {
    for (final CompatibilityPair pair : mCompatibiliyRelation) {
      if (!pair.isCompatible(state1, state2)) {
        return false;
      }
    }
    return true;
  }


  //#########################################################################
  //# State Order
  private void setUpStateOrder()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    mStateOrderIndex = new int[numStates];
    final int numReachable = rel.getNumberOfReachableStates() - 1;
    mOrderedStates = new int[numReachable];
    final int dumpIndex = rel.getDumpStateIndex();
    int orderIndex = 0;
    for (int s = 0; s < numStates; s++) {
      if (rel.isReachable(s) && s != dumpIndex) {
        mStateOrderIndex[s] = orderIndex;
        mOrderedStates[orderIndex] = s;
        orderIndex++;
      } else {
        mStateOrderIndex[s] = -1;
      }
    }
  }


  //#########################################################################
  //# Partition
  private TRPartition createOneStatePartition()
    throws OverflowException, AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if (rel.getNumberOfReachableStates() <= 1 &&
        rel.getNumberOfTransitions() == 0) {
      return null;
    } else {
      final int dumpIndex = rel.getDumpStateIndex();
      final int numStates = rel.getNumberOfStates();
      final int[] stateToClass = new int[numStates];
      for (int s = 0; s < numStates; s++) {
        if (!rel.isReachable(s)) {
          stateToClass[s] = -1;
        } else if (s != dumpIndex) {
          stateToClass[s] = 0;
        } else {
          stateToClass[s] = 1;
        }
      }
      return new TRPartition(stateToClass, 2);
    }
  }

  private void applyPartition(final ListBufferPartition partition)
  {
    if (partition == null) {
      return;
    }
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator writer = rel.createSuccessorsModifyingIterator();
    final TransitionIterator reader = rel.createSuccessorsReadOnlyIterator();
    final int numEvents = rel.getNumberOfProperEvents();
    final TIntHashSet events = new TIntHashSet(numEvents);
    final int numProps = rel.getNumberOfPropositions();
    final int numStates = rel.getNumberOfStates();
    for (int state = 0; state < numStates; state++) {
      if (rel.isReachable(state) && state == partition.getClassCode(state)) {
        events.clear();
        writer.resetState(state);
        while (writer.advance()) {
          final int e = writer.getCurrentEvent();
          events.add(e);
          final int t = writer.getCurrentTargetState();
          final int min = partition.getClassCode(t);
          if (t != min) {
            writer.setCurrentToState(min);
          }
        }
        final int clazz = partition.getClassCode(state);
        final IntListBuffer.Iterator iter = partition.createIterator(clazz);
        if (iter != null) {
          while (iter.advance()) {
            final int s = iter.getCurrentData();
            if (s != state) {
              reader.resetState(s);
              while (reader.advance()) {
                final int e = reader.getCurrentEvent();
                if (events.add(e)) {
                  final int t = reader.getCurrentTargetState();
                  final int min = partition.getClassCode(t);
                  rel.addTransition(state, e, min);
                }
              }
              if (rel.isInitial(s)) {
                rel.setInitial(state, true);
              }
              for (int p = 0; p < numProps; p++) {
                if (rel.isMarked(s, p)) {
                  rel.setMarked(state, p, true);
                }
              }
              rel.setReachable(s, false);
            }
          }
        }
      }
    }
    updateCompatibilityRelation(partition);
  }


  //#########################################################################
  //# Inner Class CompatibilityPair
  private static class CompatibilityPair
    implements Comparable<CompatibilityPair>
  {
    //#######################################################################
    //# Constructor
    private CompatibilityPair(final TIntArrayList enabling,
                              final TIntArrayList disabling)
    {
      mStatusMap = new TIntByteHashMap
        (enabling.size() + disabling.size(), 0.5f, -1, DONT_CARE);
      for (int i = 0; i < enabling.size(); i++) {
        final int state = enabling.get(i);
        mStatusMap.put(state, ENABLING);
      }
      for (int i = 0; i < disabling.size(); i++) {
        final int state = disabling.get(i);
        mStatusMap.put(state, DISABLING);
      }
    }

    //#######################################################################
    //# Interface java.lang.Comparable<CompatibilityPair>
    @Override
    public int compareTo(final CompatibilityPair other)
    {
      return other.size() - size();
    }

    //#######################################################################
    //# Compatibility Checking
    private int size()
    {
      return mStatusMap.size();
    }

    private boolean isCompatible(final int state1, final int state2)
    {
      final byte status1 = mStatusMap.get(state1);
      if (status1 == DONT_CARE) {
        return true;
      } else {
        final byte status2 = mStatusMap.get(state2);
        return status2 == DONT_CARE || status1 == status2;
      }
    }

    private boolean isCompatible(final IntListBuffer.Iterator iter1,
                                 final IntListBuffer.Iterator iter2)
    {
      byte status1 = DONT_CARE;
      iter1.reset();
      while (iter1.advance()) {
        final int s = iter1.getCurrentData();
        status1 = mStatusMap.get(s);
        if (status1 != DONT_CARE) {
          break;
        }
      }
      if (status1 != DONT_CARE) {
        iter2.reset();
        while (iter2.advance()) {
          final int s = iter2.getCurrentData();
          final byte status2 = mStatusMap.get(s);
          if (status2 != DONT_CARE) {
            return status1 == status2;
          }
        }
      }
      return true;
    }

    private boolean subsumes(final CompatibilityPair other)
    {
      final TIntByteIterator iter = other.mStatusMap.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final int state = iter.key();
        if (iter.value() != mStatusMap.get(state)) {
          return false;
        }
      }
      return true;
    }

    private boolean applyPartition(final ListBufferPartition partition)
    {
      final TIntArrayList changed = new TIntArrayList(size());
      final TIntByteIterator iter = mStatusMap.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final int state = iter.key();
        if (state != partition.getClassCode(state)) {
          changed.add(state);
        }
      }
      if (changed.size() > 0) {
        for (int i = 0; i < changed.size(); i++) {
          final int state = changed.get(i);
          final byte status = mStatusMap.remove(state);
          final int min = partition.getClassCode(state);
          mStatusMap.put(min, status);
        }
        return true;
      } else {
        return false;
      }
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final TIntArrayList enabling = new TIntArrayList(size());
      final TIntArrayList disabling = new TIntArrayList(size());
      final TIntByteIterator iter = mStatusMap.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final int state = iter.key();
        switch (iter.value()) {
        case ENABLING:
          enabling.add(state);
          break;
        case DISABLING:
          disabling.add(state);
          break;
        default:
          assert false : "Unexpected status in compatibility map!";
        }
      }
      enabling.sort();
      disabling.sort();
      final StringBuilder builder = new StringBuilder();
      builder.append("Enabling: ");
      builder.append(enabling.toString());
      builder.append("\nDisabling: ");
      builder.append(disabling.toString());
      return builder.toString();
    }

    //#######################################################################
    //# Data Members
    private final TIntByteHashMap mStatusMap;
  }


  //#########################################################################
  //# Inner Class ListBufferPartition
  private class ListBufferPartition
  {
    //#######################################################################
    //# Data Members
    private IntListBuffer.Iterator createIterator(final int clazz)
    {
      if (mClassToList == null) {
        return null;
      }
      final int list = mClassToList[clazz];
      if (list == IntListBuffer.NULL) {
        return null;
      }
      return mListBuffer.createReadOnlyIterator(list);
    }

    private TRPartition createTRPartition()
    {
      if (mStateToClass == null) {
        return null;
      } else {
        return new TRPartition(mStateToClass, mStateToClass.length);
      }
    }

    private int getClassCode(final int state)
    {
      if (mStateToClass == null) {
        return state;
      } else {
        return mStateToClass[state];
      }
    }

    private void getStates(final int clazz, final TIntArrayList buffer)
    {
      buffer.clear();
      if (mStateToClass == null) {
        buffer.add(clazz);
        return;
      }
      final int list = mClassToList[clazz];
      if (list == IntListBuffer.NULL) {
        buffer.add(clazz);
        return;
      }
      mListBuffer.toArrayList(list, buffer);
    }

    private void initialize()
    {
      if (mStateToClass == null) {
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int numStates = rel.getNumberOfStates();
        mStateToClass = new int[numStates];
        mClassToList = new int[numStates];
        mListBuffer = new IntListBuffer();
        for (int s = 0; s < numStates; s++) {
          if (rel.isReachable(s)) {
            mStateToClass[s] = s;
          } else {
            mStateToClass[s] = -1;
          }
        }
        mIterator = mListBuffer.createReadOnlyIterator();
      }
    }

    private boolean isClassCompatible(final int state1,
                                      final int state2)
    {
      final int list1, list2;
      if (mStateToClass == null) {
        list1 = list2 = IntListBuffer.NULL;
      } else {
        final int clazz1 = mStateToClass[state1];
        list1 = mClassToList[clazz1];
        final int clazz2 = mStateToClass[state2];
        list2 = mClassToList[clazz2];
      }
      if (list1 == IntListBuffer.NULL && list2 == IntListBuffer.NULL) {
        return isStateCompatible(state1, state2);
      } else if (list1 == IntListBuffer.NULL) {
        mIterator.reset(list2);
        while (mIterator.advance()) {
          final int s2 = mIterator.getCurrentData();
          if (!isStateCompatible(state1, s2)) {
            return false;
          }
        }
        return true;
      } else if (list2 == IntListBuffer.NULL) {
        mIterator.reset(list1);
        while (mIterator.advance()) {
          final int s1 = mIterator.getCurrentData();
          if (!isStateCompatible(s1, state2)) {
            return false;
          }
        }
        return true;
      } else {
        final IntListBuffer.Iterator iter1 =
          mListBuffer.createReadOnlyIterator(list1);
        final IntListBuffer.Iterator iter2 =
          mListBuffer.createReadOnlyIterator(list2);
        for (final CompatibilityPair pair : mCompatibiliyRelation) {
          if (!pair.isCompatible(iter1, iter2)) {
            return false;
          }
        }
        return true;
      }
    }

    private boolean isEquivalent(final int state1, final int state2)
    {
      if (mStateToClass == null) {
        return state1 == state2;
      } else {
        return mStateToClass[state1] == mStateToClass[state2];
      }
    }

    private ListBufferPartition merge(final ListBufferPartition partition)
    {
      if (mStateToClass == null) {
        return partition;
      } else if (partition.mStateToClass == null) {
        return this;
      } else {
        final IntListBuffer.Iterator iter = partition.mIterator;
        for (final int list : partition.mClassToList) {
          if (list != IntListBuffer.NULL) {
            iter.reset(list);
            iter.advance();
            final int state1 = iter.getCurrentData();
            final int clazz1 = mStateToClass[state1];
            while (iter.advance()) {
              final int state2 = iter.getCurrentData();
              final int clazz2 = mStateToClass[state2];
              mergeClasses(clazz1, clazz2);
            }
          }
        }
        return this;
      }
    }

    private void mergeClasses(final int clazz1, final int clazz2)
    {
      if (clazz1 != clazz2) {
        initialize();
        final int list1 = mClassToList[clazz1];
        final int list2 = mClassToList[clazz2];
        if (list1 == IntListBuffer.NULL && list2 == IntListBuffer.NULL) {
          mStateToClass[clazz2] = clazz1;
          final int list = mListBuffer.createList();
          mListBuffer.append(list, clazz1);
          mListBuffer.append(list, clazz2);
          mClassToList[clazz1] = list;
        } else if (list2 == IntListBuffer.NULL) {
          mStateToClass[clazz2] = clazz1;
          mListBuffer.append(list1, clazz2);
        } else if (list1 == IntListBuffer.NULL) {
          mIterator.reset(list2);
          while (mIterator.advance()) {
            final int state = mIterator.getCurrentData();
            mStateToClass[state] = clazz1;
          }
          mListBuffer.prepend(list2, clazz1);
          mClassToList[clazz1] = list2;
          mClassToList[clazz2] = IntListBuffer.NULL;
        } else {
          mIterator.reset(list2);
          while (mIterator.advance()) {
            final int state = mIterator.getCurrentData();
            mStateToClass[state] = clazz1;
          }
          mClassToList[clazz1] = mListBuffer.catenateDestructively(list1, list2);
          mClassToList[clazz2] = IntListBuffer.NULL;
        }
      }
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      if (mStateToClass == null) {
        return "(identity partition)";
      } else {
        final StringBuilder builder = new StringBuilder();
        for (int clazz = 0; clazz < mClassToList.length; clazz++) {
          if (mStateToClass[clazz] == clazz) {
            builder.append(clazz);
            builder.append(": [");
            final int list = mClassToList[clazz];
            if (list == IntListBuffer.NULL) {
              builder.append(clazz);
            } else {
              boolean first = true;
              final IntListBuffer.Iterator iter =
                mListBuffer.createReadOnlyIterator(list);
              iter.reset(list);
              while (iter.advance()) {
                if (first) {
                  first = false;
                } else {
                  builder.append(", ");
                }
                final int s = iter.getCurrentData();
                builder.append(s);
              }
            }
            builder.append("]\n");
          }
        }
        return builder.toString();
      }
    }

    //#######################################################################
    //# Data Members
    private int[] mStateToClass;
    private int[] mClassToList;
    private IntListBuffer mListBuffer;
    private IntListBuffer.Iterator mIterator;
  }


  //#########################################################################
  //# Inner Class StackOfPairs
  private static class StackOfPairs
  {
    //#######################################################################
    //# Access
    private void clear()
    {
      mStack.clear();
      mVisited = new TLongHashSet();
    }

    private boolean isEmpty()
    {
      return mStack.size() == 0;
    }

    private long pop()
    {
      return mStack.pop();
    }

    private void push(final int s1, final int s2)
    {
      final long lo, hi;
      if (s1 < s2) {
        lo = s1;
        hi = s2;
      } else {
        lo = s2;
        hi = s1;
      }
      final long pair = lo | (hi << 32);
      if (mVisited.add(pair)) {
        mStack.push(pair);
      }
    }

    //#######################################################################
    //# Pair Decomposition
    private static int getLo(final long pair)
    {
      return (int) (pair & 0xffffffffL);
    }

    private static int getHi(final long pair)
    {
      return (int) (pair >>> 32);
    }

    //#######################################################################
    //# Data Members
    private final TLongStack mStack = new TLongArrayStack();
    private TLongHashSet mVisited = null;
  }


  //#########################################################################
  //# Data Members
  private List<CompatibilityPair> mCompatibiliyRelation;
  private int[] mOrderedStates;
  private int[] mStateOrderIndex;
  private int mCurrentIndex1;
  private int mCurrentIndex2;
  private StackOfPairs mStack;
  private TIntArrayList mStates1;
  private TIntArrayList mStates2;
  private TIntArrayList mSuccessors1;
  private TIntArrayList mSuccessors2;


  //#########################################################################
  //# Class Constants
  private static final byte DONT_CARE = 0;
  private static final byte ENABLING = 1;
  private static final byte DISABLING = 2;

}
