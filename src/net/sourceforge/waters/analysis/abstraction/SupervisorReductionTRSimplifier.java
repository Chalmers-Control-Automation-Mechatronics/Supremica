//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.IntListBuffer.ReadOnlyIterator;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * Transition relation simplifier that implements halfway synthesis.
 *
 * @author Fangqian Qiu, Robi Malik
 */

public class SupervisorReductionTRSimplifier extends
  AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public SupervisorReductionTRSimplifier() throws AnalysisException
  {
  }

  public SupervisorReductionTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_ALL;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return false;
  }

  //#########################################################################
  //# Configuration
  public void setRestrictedEvent(final int event)
  {
    mRestrictedEvent = event;
  }

  public int getRestrictedEvent()
  {
    return mRestrictedEvent;
  }

  public void setExperimentalMode(final boolean experimentalMode)
  {
    mExperimentalMode = experimentalMode;
  }

  public boolean getExperimentalMode()
  {
    return mExperimentalMode;
  }

  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mNumProperEvents = rel.getNumberOfProperEvents();
  }

  @Override
  public boolean runSimplifier() throws AnalysisException
  {
    try {
      setUp();
      final TRPartition partition;
      if (findBadStates()) {
        final TIntArrayList enabDisabEvents = new TIntArrayList();
        final TIntArrayList disabEvents = new TIntArrayList();
        if (mRestrictedEvent < 0) {
          // monolithic supervisor reduction
          setUpClasses();
          setUpEventList(enabDisabEvents, disabEvents);
          if (enabDisabEvents.size() == 0) {
            partition = createOneStateTR(disabEvents);
          } else {
            partition = reduceSupervisor(enabDisabEvents);
          }
        } else {
          // supervisor localization
          final TIntArrayList singletonList = new TIntArrayList(1);
          singletonList.add(mRestrictedEvent);
          if (mExperimentalMode) {
            final ListBufferTransitionRelation rel = getTransitionRelation();
            final int numStates = rel.getNumberOfStates();
            mStateToClass = new int[numStates];
            mClasses = new IntListBuffer();
            partition = reduceSupervisorExperimental(singletonList);
          } else {
            setUpClasses();
            partition = reduceSupervisor(singletonList);
          }
        }
      } else {
        partition = createOneStateTR(new TIntArrayList(0));
      }
      setResultPartition(partition);
      if (partition != null) {
        applyResultPartitionAutomatically();
      }
      return partition != null;
    } finally {
      tearDown();
    }
  }

  @Override
  protected void applyResultPartition() throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    // Remove uncontrollable events that are selfloop-only
    final TransitionIterator iter =
      rel.createAllTransitionsReadOnlyIterator();
    for (int e = EventEncoding.NONTAU; e < mNumProperEvents; e++) {
      checkAbort();
      final byte status = rel.getProperEventStatus(e);
      if (!EventStatus.isControllableEvent(status)) {
        iter.resetEvent(e);
        boolean selfloopOnly = true;
        while (iter.advance()) {
          if (iter.getCurrentSourceState() != iter.getCurrentTargetState()) {
            selfloopOnly = false;
            break;
          }
        }
        if (selfloopOnly) {
          rel.removeEvent(e);
        }
      }
    }
    // Remove ordinary full-selfloop events
    final int marking = getDefaultMarkingID();
    rel.removeProperSelfLoopEvents(marking);
    rel.removeRedundantPropositions();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mStateToClass = null;
    mClasses = null;
    mShadowStateToClass = null;
    mShadowClasses = null;
    mStateMap = null;
    mInverseMap = null;
    mSearchingBitSet = null;
  }


  //#########################################################################
  //# Methods for Supervisor Reduction
  private TRPartition reduceSupervisor(final TIntArrayList restrictedEventList)
    throws AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int marking = getDefaultMarkingID();
    mMerged = false;

    for (int i = 0; i < numStates - 1; i++) {
      if (!rel.isReachable(i) || rel.isDeadlockState(i, marking)
          || i > getMinimum(i)) {
        continue;
      }
      for (int j = i + 1; j < numStates; j++) {
        if (!rel.isReachable(j) || rel.isDeadlockState(j, marking)
            || j > getMinimum(j)) {
          continue;
        }
        checkAbort();
        TLongHashSet statePairs = new TLongHashSet();
        mShadowClasses = new IntListBuffer();
        mShadowStateToClass = new int[numStates];
        for (int s = 0; s < numStates; s++) {
          mShadowStateToClass[s] = IntListBuffer.NULL;
        }
        if (checkMergibility(i, j, i, j, statePairs, restrictedEventList)) {
          merge(statePairs);
          mMerged = true;
        }
        statePairs = null;
        mShadowClasses = null;
        mShadowStateToClass = null;
      }
    }
    if (mMerged) {
      return createResultPartition();
    } else {
      return null;
    }
  }

  private boolean checkMergibility(final int x, final int y, final int x0,
                                   final int y0,
                                   final TLongHashSet statePairs,
                                   final TIntArrayList restrictedEventList)
    throws AnalysisAbortException
  {
    checkAbort();
    if (mStateToClass[x] == mStateToClass[y]) {
      return true;
    }

    final int minX = getMinimum(x);
    final int minY = getMinimum(y);
    if (minX == minY) {
      return true;
    }
    final long p1 = constructLong(minX, minY);
    final long p2 = constructLong(x0, y0);
    if (compare(p1, p2) < 0) {
      return false;
    }

    copyIfShadowNull(x);
    copyIfShadowNull(y);

    final int lx = mShadowStateToClass[x];
    final int ly = mShadowStateToClass[y];
    final int[] listX = mShadowClasses.toArray(lx);
    final int[] listY = mShadowClasses.toArray(ly);

    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int marking = getDefaultMarkingID();
    final TIntHashSet xSet = new TIntHashSet();
    final TIntHashSet ySet = new TIntHashSet();
    final TIntArrayList xList = new TIntArrayList();
    final TIntArrayList yList = new TIntArrayList();

    for (int i = 0; i < restrictedEventList.size(); i++) {
      final int e = restrictedEventList.get(i);
      xSet.clear();
      xList.clear();
      boolean enabled = false;
      boolean disabled = false;
      for (final int xx : listX) {
        final int succ = getSuccessorState(xx, e);
        if (succ >= 0) {
          if (xSet.add(xx)) {
            xList.add(xx);
          }
          if (rel.isDeadlockState(succ, marking)) {
            disabled = true;
          } else {
            enabled = true;
          }
        }
      }
      if (disabled) {
        for (final int yy : listY) {
          final int succ = getSuccessorState(yy, e);
          if (succ >= 0 && !rel.isDeadlockState(succ, marking)) {
            return false;
          }
        }
      }
      if (enabled) {
        for (final int yy : listY) {
          final int succ = getSuccessorState(yy, e);
          if (succ >= 0 && rel.isDeadlockState(succ, marking)) {
            return false;
          }
        }
      }
    }

    final int l = mergeLists(lx, ly, mShadowClasses);
    updateStateToClass(l, mShadowStateToClass, mShadowClasses);

    final long pair = constructLong(x, y);
    statePairs.add(pair);

    for (int e = EventEncoding.NONTAU; e < mNumProperEvents; e++) {
      xSet.clear();
      ySet.clear();
      xList.clear();
      yList.clear();
      for (final int xx : listX) {
        final int xSucc = getSuccessorState(xx, e);
        if (xSucc >= 0 && !rel.isDeadlockState(xSucc, marking)) {
          final int xmin = getMinimum(xSucc);
          if (xSet.add(xmin)) {
            xList.add(xmin);
          }
        }
      }
      if (xList.isEmpty()) {
        continue;
      }
      for (final int yy : listY) {
        final int ySucc = getSuccessorState(yy, e);
        if (ySucc >= 0 && !rel.isDeadlockState(ySucc, marking)) {
          final int ymin = getMinimum(ySucc);
          if (ySet.add(ymin)) {
            yList.add(ymin);
          }
        }
      }
      for (int i = 0; i < xList.size(); i++) {
        for (int j = 0; j < yList.size(); j++) {
          if (!checkMergibility(xList.get(i), yList.get(j), x0, y0,
                                statePairs, restrictedEventList)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private TRPartition reduceSupervisorExperimental(final TIntArrayList restrictedEventList)
    throws AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int marking = getDefaultMarkingID();
    mMerged = false;

    // find disabled states
    mSearchingBitSet = new BitSet();
    mQueue = new ArrayDeque<Integer>();
    final TransitionIterator iter =
      rel.createAllTransitionsReadOnlyIterator();
    iter.resetEvent(restrictedEventList.get(0));
    while (iter.advance()) {
      final int pre = iter.getCurrentSourceState();
      final int succ = iter.getCurrentTargetState();
      if (rel.isDeadlockState(succ, marking)) {
        mQueue.add(pre);
        mSearchingBitSet.set(pre);
      }
    }

    // reorder states
    mStateMap = new int[numStates];
    mInverseMap = new int[numStates];
    int count = 0;
    final TransitionIterator preIter =
      rel.createPredecessorsReadOnlyIterator();
    while (!mQueue.isEmpty()) {
      final int state = mQueue.remove().intValue();
      if (rel.isDeadlockState(state, marking)) {
        continue;
      }
      final int list = mClasses.createList();
      mClasses.add(list, state);
      mStateToClass[count] = list;
      mInverseMap[state] = count;
      mStateMap[count++] = state;
      preIter.resetState(state);
      while (preIter.advance()) {
        final int source = preIter.getCurrentSourceState();
        if (mSearchingBitSet.get(source)) {
          continue;
        } else {
          mSearchingBitSet.set(source);
          mQueue.add(new Integer(source));
        }
      }
    }

    for (int s = 0; s < numStates; s++) {
      if (!mSearchingBitSet.get(s)) {
        final int list = mClasses.createList();
        mClasses.add(list, s);
        mStateToClass[count] = list;
        mInverseMap[s] = count;
        mStateMap[count++] = s;
      }
    }

    for (int sum = 1; sum < 2 * (numStates - 1); sum++) {
      final int hi = (sum - 1) / 2;
      final int lo = Math.max(0, sum - numStates + 1);
      for (int b = hi; b >= lo; b--) {
        final int a = sum - b;
        if (rel.isReachable(a) && !rel.isDeadlockState(a, marking)
            && rel.isReachable(b) && !rel.isDeadlockState(b, marking)) {
          if (mStateMap[a] > getMinimumExperimental(mStateMap[a])
              || mStateMap[b] > getMinimumExperimental(mStateMap[b])) {
            continue;
          }
          checkAbort();
          TLongHashSet statePairs = new TLongHashSet();
          mShadowClasses = new IntListBuffer();
          mShadowStateToClass = new int[numStates];
          for (int s = 0; s < numStates; s++) {
            mShadowStateToClass[s] = IntListBuffer.NULL;
          }
          if (checkMergibilityExperimental(mStateMap[a], mStateMap[b],
                                           statePairs, restrictedEventList)) {
            mergeExperimental(statePairs);
            mMerged = true;
          }
          statePairs = null;
          mShadowClasses = null;
          mShadowStateToClass = null;
        }
      }
    }

    if (mMerged) {
      return createResultPartition();
    } else {
      return null;
    }
  }

  private boolean checkMergibilityExperimental(final int x0,
                                               final int y0,
                                               final TLongHashSet statePairs,
                                               final TIntArrayList restrictedEventList)
    throws AnalysisAbortException
  {
    final Deque<Long> pairStack = new ArrayDeque<Long>();
    pairStack.push(constructLong(x0, y0));
    final long initialPair = constructLong(mInverseMap[x0], mInverseMap[y0]);

    while (!pairStack.isEmpty()) {
      checkAbort();
      final long pair = pairStack.pop().longValue();
      final int x = getState(0, pair);
      final int y = getState(1, pair);

      if (mStateToClass[mInverseMap[x]] == mStateToClass[mInverseMap[y]]) {
        continue;
      }

      final int minX = getMinimumExperimental(x);
      final int minY = getMinimumExperimental(y);
      if (minX == minY) {
        continue;
      }

      final long minPair =
        constructLong(mInverseMap[minX], mInverseMap[minY]);
      if (compareOrder(minPair, initialPair) < 0) {
        return false;
      }

      if (mShadowStateToClass[mInverseMap[x]] == IntListBuffer.NULL) {
        final int newlist =
          mShadowClasses.copy(mStateToClass[mInverseMap[x]], mClasses);
        final ReadOnlyIterator iter =
          mShadowClasses.createReadOnlyIterator(newlist);
        iter.reset(newlist);
        while (iter.advance()) {
          final int current = iter.getCurrentData();
          mShadowStateToClass[mInverseMap[current]] = newlist;
        }
      }
      if (mShadowStateToClass[mInverseMap[y]] == IntListBuffer.NULL) {
        final int newlist =
          mShadowClasses.copy(mStateToClass[mInverseMap[y]], mClasses);
        final ReadOnlyIterator iter =
          mShadowClasses.createReadOnlyIterator(newlist);
        iter.reset(newlist);
        while (iter.advance()) {
          final int current = iter.getCurrentData();
          mShadowStateToClass[mInverseMap[current]] = newlist;
        }
      }

      final int lx = mShadowStateToClass[mInverseMap[x]];
      final int ly = mShadowStateToClass[mInverseMap[y]];
      final int[] listX = mShadowClasses.toArray(lx);
      final int[] listY = mShadowClasses.toArray(ly);

      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int marking = getDefaultMarkingID();
      final TIntHashSet xSet = new TIntHashSet();
      final TIntHashSet ySet = new TIntHashSet();
      final TIntArrayList xList = new TIntArrayList();
      final TIntArrayList yList = new TIntArrayList();

      for (int i = 0; i < restrictedEventList.size(); i++) {
        final int e = restrictedEventList.get(i);
        xSet.clear();
        xList.clear();
        boolean enabled = false;
        boolean disabled = false;
        for (final int xx : listX) {
          final int succ = getSuccessorState(xx, e);
          if (succ >= 0) {
            if (xSet.add(xx)) {
              xList.add(xx);
            }
            if (rel.isDeadlockState(succ, marking)) {
              disabled = true;
            } else {
              enabled = true;
            }
          }
        }
        if (disabled) {
          for (final int yy : listY) {
            final int succ = getSuccessorState(yy, e);
            if (succ >= 0 && !rel.isDeadlockState(succ, marking)) {
              return false;
            }
          }
        }
        if (enabled) {
          for (final int yy : listY) {
            final int succ = getSuccessorState(yy, e);
            if (succ >= 0 && rel.isDeadlockState(succ, marking)) {
              return false;
            }
          }
        }
      }

      final int l = mergeLists(lx, ly, mShadowClasses);
      updateStateToClassExperimental(l, mShadowStateToClass, mShadowClasses);

      statePairs.add(pair);

      for (int e = EventEncoding.NONTAU; e < mNumProperEvents; e++) {
        xSet.clear();
        ySet.clear();
        xList.clear();
        yList.clear();
        for (final int xx : listX) {
          final int xSucc = getSuccessorState(xx, e);
          if (xSucc >= 0 && !rel.isDeadlockState(xSucc, marking)) {
            final int xmin = getMinimumExperimental(xSucc);
            if (xSet.add(xmin)) {
              xList.add(xmin);
            }
          }
        }
        if (xList.isEmpty()) {
          continue;
        }
        for (final int yy : listY) {
          final int ySucc = getSuccessorState(yy, e);
          if (ySucc >= 0 && !rel.isDeadlockState(ySucc, marking)) {
            final int ymin = getMinimumExperimental(ySucc);
            if (ySet.add(ymin)) {
              yList.add(ymin);
            }
          }
        }
        for (int i = 0; i < xList.size(); i++) {
          for (int j = 0; j < yList.size(); j++) {
            pairStack.push(constructLong(xList.get(i), yList.get(j)));
          }
        }
      }
    }
    return true;
  }

  private void setUpClasses() throws AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int marking = getDefaultMarkingID();
    mStateToClass = new int[numStates];
    mClasses = new IntListBuffer();
    for (int s = 0; s < numStates; s++) {
      checkAbort();
      if (rel.isDeadlockState(s, marking)) {
        mStateToClass[s] = IntListBuffer.NULL;
      } else {
        final int list = mClasses.createList();
        mClasses.add(list, s);
        mStateToClass[s] = list;
      }
    }
  }

  public void setUpEventList(final TIntArrayList enabDisabEvents,
                             final TIntArrayList disabEvents)
    throws AnalysisAbortException
  {
    enabDisabEvents.clear();
    disabEvents.clear();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mNumProperEvents = rel.getNumberOfProperEvents();
    final int marking = getDefaultMarkingID();
    final TIntArrayList[] disabledEventsToStates =
      new TIntArrayList[mNumProperEvents];
    final TIntArrayList[] enabledEventsToStates =
      new TIntArrayList[mNumProperEvents];
    final TransitionIterator iter =
      rel.createAllTransitionsReadOnlyIteratorByStatus
        (EventStatus.STATUS_CONTROLLABLE);
    while (iter.advance()) {
      // for each controllable event ...
      checkAbort();
      final int currentEvent = iter.getCurrentEvent();
      final int succ = iter.getCurrentTargetState();
      final int pre = iter.getCurrentSourceState();
      if (rel.isDeadlockState(succ, marking)) {
        if (disabledEventsToStates[currentEvent] == null) {
          disabledEventsToStates[currentEvent] = new TIntArrayList();
        }
        disabledEventsToStates[currentEvent].add(pre);
      } else {
        if (enabledEventsToStates[currentEvent] == null) {
          enabledEventsToStates[currentEvent] = new TIntArrayList();
        }
        enabledEventsToStates[currentEvent].add(pre);
      }
    }
    for (int i = 0; i < mNumProperEvents - 1; i++) {
      for (int j = i + 1; j < mNumProperEvents; j++) {
        checkAbort();
        // if event e1 is enabled and disabled in the same set of states as event e2, then ignore one of them
        if (disabledEventsToStates[i] != null
            && enabledEventsToStates[i] != null
            && disabledEventsToStates[i].equals(disabledEventsToStates[j])
            && enabledEventsToStates[i].equals(enabledEventsToStates[j])) {
          disabledEventsToStates[j] = null;
          enabledEventsToStates[j] = null;
        }

      }
    }
    for (int e = EventEncoding.NONTAU; e < mNumProperEvents; e++) {
      final byte status = rel.getProperEventStatus(e);
      if (EventStatus.isControllableEvent(status)) {
        if (disabledEventsToStates[e] != null) {
          if (enabledEventsToStates[e] != null) {
            enabDisabEvents.add(e);
          } else {
            disabEvents.add(e);
          }
        }
      }
    }
  }

  //#########################################################################
  //# Auxiliary Methods
  private boolean findBadStates()
  {
    mBadStateIndex = -1;
    mNumBadStates = 0;
    final int marking = getDefaultMarkingID();
    if (marking >= 0) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      for (int s = 0; s < numStates; s++) {
        if (rel.isReachable(s) && rel.isDeadlockState(s, marking)) {
          if (mBadStateIndex < 0) {
            mBadStateIndex = s;
          }
          mNumBadStates++;
        }
      }
    }
    return mNumBadStates > 0;
  }

  private TRPartition createResultPartition() throws AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int marking = getDefaultMarkingID();
    final int[] badClass = mNumBadStates > 0 ? new int[mNumBadStates] : null;
    int nextBad = 0;
    final List<int[]> classes = new ArrayList<>();
    boolean trChanged = mNumBadStates > 1;
    for (int s = 0; s < numStates; s++) {
      checkAbort();
      if (rel.isReachable(s)) {
        if (rel.isDeadlockState(s, marking)) {
          badClass[nextBad++] = s;
        } else {
          int listID = mStateToClass[s];
          if (mExperimentalMode) {
            listID = mStateToClass[mInverseMap[s]];
          }
          if (mClasses.getFirst(listID) == s) {
            final int[] states = mClasses.toArray(listID);
            classes.add(states);
          } else {
            trChanged = true;
          }
        }
      }
    }
    if (trChanged) {
      if (badClass != null) {
        classes.add(badClass);
      }
      return new TRPartition(classes, numStates);
    } else {
      return null;
    }
  }

  private void merge(final TLongHashSet statePairs)
    throws AnalysisAbortException
  {
    final TLongIterator itr = statePairs.iterator();
    while (itr.hasNext()) {
      checkAbort();
      final long pair = itr.next();
      final int hi = getState(0, pair);
      final int lo = getState(1, pair);
      if (mStateToClass[hi] != mStateToClass[lo]) {
        final int list1 = mStateToClass[hi];
        final int list2 = mStateToClass[lo];
        final int list3 = mergeLists(list1, list2, mClasses);
        updateStateToClass(list3, mStateToClass, mClasses);
      }
    }
  }

  private void mergeExperimental(final TLongHashSet statePairs)
    throws AnalysisAbortException
  {
    final TLongIterator itr = statePairs.iterator();
    while (itr.hasNext()) {
      checkAbort();
      final long pair = itr.next();
      final int hi = getState(0, pair);
      final int lo = getState(1, pair);
      if (mStateToClass[mInverseMap[hi]] != mStateToClass[mInverseMap[lo]]) {
        final int list1 = mStateToClass[mInverseMap[hi]];
        final int list2 = mStateToClass[mInverseMap[lo]];
        final int list3 = mergeLists(list1, list2, mClasses);
        updateStateToClassExperimental(list3, mStateToClass, mClasses);
      }
    }
  }

  private int mergeLists(final int list1, final int list2,
                         final IntListBuffer classes)
  {
    final int x = classes.getFirst(list1);
    final int y = classes.getFirst(list2);
    if (x < y) {
      return classes.catenateDestructively(list1, list2);
    } else if (x > y) {
      return classes.catenateDestructively(list2, list1);
    }
    return list1;
  }

  private void copyIfShadowNull(final int state)
    throws AnalysisAbortException
  {
    if (mShadowStateToClass[state] == IntListBuffer.NULL) {
      final int newlist = mShadowClasses.copy(mStateToClass[state], mClasses);
      final ReadOnlyIterator iter =
        mShadowClasses.createReadOnlyIterator(newlist);
      iter.reset(newlist);
      while (iter.advance()) {
        final int current = iter.getCurrentData();
        mShadowStateToClass[current] = newlist;
      }
    }
  }

  private void updateStateToClass(final int list, final int[] stateToClass,
                                  final IntListBuffer classes)
    throws AnalysisAbortException
  {
    final ReadOnlyIterator iter = classes.createReadOnlyIterator(list);
    iter.reset(list);
    while (iter.advance()) {
      final int current = iter.getCurrentData();
      stateToClass[current] = list;
    }
  }

  private void updateStateToClassExperimental(final int list,
                                              final int[] stateToClass,
                                              final IntListBuffer classes)
    throws AnalysisAbortException
  {
    final ReadOnlyIterator iter = classes.createReadOnlyIterator(list);
    iter.reset(list);
    while (iter.advance()) {
      final int current = iter.getCurrentData();
      stateToClass[mInverseMap[current]] = list;
    }
  }

  private int compare(final long pair1, final long pair2)
  {
    if (pair1 < pair2) {
      return -1;
    } else if (pair1 > pair2) {
      return 1;
    } else {
      return 0;
    }
  }

  private int getMinimum(final int state)
  {
    if (mShadowStateToClass == null
        || mShadowStateToClass[state] == IntListBuffer.NULL) {
      return mClasses.getFirst(mStateToClass[state]);
    } else {
      return mShadowClasses.getFirst(mShadowStateToClass[state]);
    }
  }

  private int getMinimumExperimental(final int state)
  {
    if (mShadowStateToClass == null
        || mShadowStateToClass[mInverseMap[state]] == IntListBuffer.NULL) {
      return mClasses.getFirst(mStateToClass[mInverseMap[state]]);
    } else {
      return mShadowClasses.getFirst(mShadowStateToClass[mInverseMap[state]]);
    }
  }

  private int getState(final int position, final long pair)
  {
    if (position == 0) {
      return (int) (pair >>> 32);
    } else if (position == 1) {
      return (int) (pair & 0xffffffff);
    }
    return -1;
  }

  private long constructLong(int state1, int state2)
  {
    if (state1 > state2) {
      state1 = state1 + state2;
      state2 = state1 - state2;
      state1 = state1 - state2;
    }
    final long pair = state2 | ((long) state1 << 32);
    return pair;
  }

  private TRPartition createOneStateTR(final TIntArrayList disabEvents)
    throws OverflowException, AnalysisAbortException
  {
    final ListBufferTransitionRelation oldRel = getTransitionRelation();
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation("OneStateSup",
                                       ComponentKind.SUPERVISOR,
                                       mNumProperEvents,
                                       oldRel.getNumberOfPropositions(),
                                       2, 1,
                                       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    rel.setInitial(0, true);
    // set all events in disabEvents USED
    for (int e = EventEncoding.TAU; e < rel.getNumberOfProperEvents(); e++) {
      if (!disabEvents.contains(e)) {
        final byte status = rel.getProperEventStatus(e);
        rel.setProperEventStatus(e, status | EventStatus.STATUS_UNUSED);
      }
    }
    // Create a one-reachable-state automaton if the disabled event set is
    // empty, otherwise create a two-reachable-state automaton.
    if (disabEvents.isEmpty()) {
      // set initial state markings
      final int numProps = rel.getNumberOfPropositions();
      for (int p = 0; p < numProps; p++) {
        rel.setPropositionUsed(p, false);
      }
      // dump state not reachable
      rel.setReachable(1, false);
    } else {
      // add transitions from state 0 to dump state
      for (int i = 0; i < disabEvents.size(); i++) {
        rel.addTransition(0, disabEvents.get(i), 1);
      }
      // set initial state markings
      final int numProps = rel.getNumberOfPropositions();
      for (int p = 0; p < numProps; p++) {
        rel.setMarked(0, p, true);
      }
    }
    final int marking = getDefaultMarkingID();
    final int oldNumStates = oldRel.getNumberOfStates();
    final int[] stateToClass = new int[oldNumStates];
    for (int s = 0; s < oldNumStates; s++) {
      if (!oldRel.isReachable(s)) {
        stateToClass[s] = -1;
      } else if (!oldRel.isDeadlockState(s, marking)) {
        stateToClass[s] = 0;
      } else {
        stateToClass[s] = 1;
      }
    }
    return new TRPartition(stateToClass, 2);
  }

  private int getSuccessorState(final int source, final int event)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    iter.reset(source, event);
    if (iter.advance()) {
      return iter.getCurrentTargetState();
    } else {
      return -1;
    }
  }

  private int compareOrder(final long pair1, final long pair2)
  {
    final int pair1a = getState(0, pair1);
    final int pair1b = getState(1, pair1);
    final int pair2a = getState(0, pair2);
    final int pair2b = getState(1, pair2);
    if (pair1a + pair1b < pair2a + pair2b) {
      return -1;
    } else if (pair1a + pair1b > pair2a + pair2b) {
      return 1;
    } else {
      return pair2a - pair1a;
    }
  }

  //#########################################################################
  //# For Debugging
  public int[] showClassList(final IntListBuffer classes, final int list)
  {
    final int[] array = classes.toArray(list);
    return array;
  }

  public void PrintStateToClassToConsole(final int event)
  {
    System.out.println("[------" + event + "------]");
    for (int i = 0; i < mStateToClass.length; i++) {
      System.out.println(mStateToClass[i]);
    }
  }

  //#########################################################################
  //# Data Members
  private int mNumProperEvents;
  private int mBadStateIndex;
  private int mNumBadStates;
  private int mRestrictedEvent;
  private int[] mStateToClass;
  private IntListBuffer mClasses;
  private int[] mShadowStateToClass;
  private IntListBuffer mShadowClasses;
  private boolean mExperimentalMode = false;
  private boolean mMerged;
  private int[] mStateMap;
  private int[] mInverseMap;

  private Queue<Integer> mQueue;
  private BitSet mSearchingBitSet;
}
