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

import gnu.trove.TIntCollection;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;
import gnu.trove.stack.array.TLongArrayStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.IntSetBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersIntIterator;
import net.sourceforge.waters.analysis.tr.WatersIntPairStack;
import net.sourceforge.waters.model.analysis.UserAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;


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
  //# Configuration
  public void setMode(final Mode mode)
  {
    mMode = mode;
  }

  public Mode getMode()
  {
    return mMode;
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
    mCliqueIterator = mCliqueDB.iterator();
    mListBuffer = new IntListBuffer();
    mTransitionIterator = rel.createSuccessorsReadOnlyIterator();
  }

  @Override
  public boolean runSimplifier() throws AnalysisException
  {
    setUpCompatibilityRelation();

    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final TIntArrayList initList = new TIntArrayList(1);
    for (int s = 0; s < numStates; s++) {
      if (rel.isInitial(s) && mCompatibilityRelation.isRelevant(s)) {
        initList.add(s);
      }
    }
    final TIntArrayList initCopy = new TIntArrayList(initList);
    final CliqueQueue queue = new CliqueQueue();
    queue.addSmallCover(initCopy);
    if (mAddingEventCovers) {
      addEventCovers(queue);
    }

    final int numRelevant =
      mCompatibilityRelation.getNumberOfRelevantStates();
    queue.propagate();

    final TIntArrayList list = new TIntArrayList(numRelevant);
    final int dumpIndex = queue.createDumpState();
    boolean dumpReachable = false;
    queue.preselectSingletonCovers();

    resetEventInfo();
    final long[] markings = new long[queue.size() + 1];
    final int numEvents = rel.getNumberOfProperEvents();
    final int limit = getTransitionLimit();
    final PreTransitionBuffer buffer =
      new PreTransitionBuffer(numEvents, limit);
    final int initState = queue.selectCover(initList);
    assert initState >= 0;
    // Start from 1, skip dump state 0
    for (int s = 1; s < queue.getNumberOfSelectedCovers(); s++) {
      final int clique = queue.getSelectedCover(s);
      for (final EventInfo info : mUsedEvents) {
        checkAbort();
        final int e = info.getEvent();
        if (info.collectAllSuccessors(clique, list)) {
          if (queue.isCover(clique, list)) {
            buffer.addTransition(s, e, s);
          } else {
            queue.preselectCovers(list);
            final int t = queue.selectCover(list);
            assert t >= 0;
            buffer.addTransition(s, e, t);
          }
        } else if (info.isDisabledControllable(clique)) {
          buffer.addTransition(s, e, dumpIndex);
          dumpReachable = true;
        }
      }
      markings[s] = collectMarkings(clique);
    }
    final int numCovers = queue.getNumberOfSelectedCovers();

    if (numCovers - 1 < mCompatibilityRelation.size()) {
      final int numTrans = buffer.size();
      rel.reset(numCovers, dumpIndex, numTrans,
                ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      buffer.addOutgoingTransitions(rel);
      rel.setInitial(initState, true);
      // Start from 1, skip dump state 0
      for (int s = 1; s < numCovers; s++) {
        rel.setAllMarkings(s, markings[s]);
      }
      if (!dumpReachable) {
        rel.setReachable(dumpIndex, false);
        rel.removeRedundantPropositions();
      }
      rel.removeProperSelfLoopEvents();
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mEventInfo = null;
    mUsedEvents = null;
    mRelevantEvents = null;
    mCompatibilityRelation = null;
    mConnectivityMap = null;
    mCliqueDB = null;
    mCliqueIterator = null;
    mListBuffer = null;
    mTransitionIterator = null;
  }


  //#########################################################################
  //# Compatibility Relation
  private void setUpCompatibilityRelation()
    throws UserAbortException
  {
    createEventInfo();
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
        if (enabled.isEmpty()) {
          mEventInfo[e].setBlocked();
        } else {
          for (int j = 0; j < enabled.size(); j++) {
            final int s1 = enabled.get(j);
            for (int k = 0; k < disabled.size(); k++) {
              final int s2 = disabled.get(k);
              propagateIncompatible(stack, s1, s2);
            }
          }
        }
      }
    }
    mCompatibilityRelation.filterStatesCompatibleWithAll();
    setUpTransitionInfo();
    setUpAllCompatibleMarkings();
    if (mMode == Mode.MAX_CLIQUES) {
      mConnectivityMap = new ConnectivityMap();
    }
  }

  private void propagateIncompatible(final TLongArrayStack stack,
                                     final int state1,
                                     final int state2)
    throws UserAbortException
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
          checkAbort();
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

  private void createEventInfo()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numEvents = rel.getNumberOfProperEvents();
    mEventInfo = new EventInfo[numEvents];
    mUsedEvents = new ArrayList<>(numEvents);
    for (int e = 0; e < numEvents; e++) {
      final byte status = rel.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status)) {
        final EventInfo info = new EventInfo(e);
        mEventInfo[e] = info;
        mUsedEvents.add(info);
      }
    }
  }

  private void setUpTransitionInfo()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numEvents = rel.getNumberOfProperEvents();
    final int numReachable = rel.getNumberOfReachableStates();
    final int numAllCompatible =
      mCompatibilityRelation.getNumberOfAllCompatibleStates();
    final TIntArrayList[] relevantStates = new TIntArrayList[numEvents];
    final TIntHashSet[] allCompatibleSucc =
      numAllCompatible > 0 ? new TIntHashSet[numEvents] : null;
      final TIntHashSet[] otherSucc = new TIntHashSet[numEvents];
   for (final EventInfo info : mUsedEvents) {
      final int e = info.getEvent();
      if (!info.isBlocked()) {
        relevantStates[e] = new TIntArrayList(numReachable - numAllCompatible);
        if (allCompatibleSucc != null) {
          allCompatibleSucc[e] = new TIntHashSet();
        }
        otherSucc[e] = new TIntHashSet();
      }
    }
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    for (final int s : mCompatibilityRelation.getAllCompatibleStates()) {
      iter.resetState(s);
      while (iter.advance()) {
        final int e = iter.getCurrentEvent();
        final int t = iter.getCurrentTargetState();
        if (mCompatibilityRelation.isRelevant(t)) {
          allCompatibleSucc[e].add(t);
        }
      }
    }
    for (final int s : mCompatibilityRelation.getRelevantStates()) {
      iter.resetState(s);
      while (iter.advance()) {
        final int e = iter.getCurrentEvent();
        final int t = iter.getCurrentTargetState();
        if (mCompatibilityRelation.isRelevant(t) &&
            (allCompatibleSucc == null || !allCompatibleSucc[e].contains(t))) {
          relevantStates[e].add(s);
          otherSucc[e].add(t);
        }
      }
    }
    mRelevantEvents = new LinkedList<>();
    for (final EventInfo info : mUsedEvents) {
      if (!info.isBlocked()) {
        final int e = info.getEvent();
        final TIntCollection succ =
          allCompatibleSucc == null ? null : allCompatibleSucc[e];
        info.setTransitionInfo(relevantStates[e], succ, otherSucc[e].size());
        if (!info.isCovered()) {
          mRelevantEvents.add(info);
        }
      }
    }
  }

  private void setUpAllCompatibleMarkings()
    throws UserAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mAllCompatibleMarkings = rel.createMarkings();
    for (final int s : mCompatibilityRelation.getAllCompatibleStates()) {
      mAllCompatibleMarkings |= rel.getAllMarkings(s);
    }
  }

  private void resetEventInfo()
  {
    mRelevantEvents.clear();
    for (final EventInfo info : mUsedEvents) {
      if (!info.isBlocked()) {
        info.reset();
        if (!info.isCovered()) {
          mRelevantEvents.add(info);
        }
      }
    }
  }


  //#########################################################################
  //# Extra Covers
  private void addEventCovers(final CliqueQueue queue)
    throws AnalysisException
  {
    final int numRelevant = mCompatibilityRelation.getNumberOfRelevantStates();
    final TIntArrayList buffer = new TIntArrayList(numRelevant);
    final Iterator<EventInfo> iter = mRelevantEvents.iterator();
    while (iter.hasNext()) {
      final EventInfo info = iter.next();
      if (info.collectEnablingClique(buffer)) {
        queue.addSmallCover(buffer);
        iter.remove();
      }
    }
  }


  //#########################################################################
  //# State Marking
  private long collectMarkings(final int clique)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    long result = mAllCompatibleMarkings;
    mCliqueIterator.reset(clique);
    while (mCliqueIterator.advance()) {
      final int s = mCliqueIterator.getCurrentData();
      result |= rel.getAllMarkings(s);
    }
    return result;
  }


  //#########################################################################
  //# Inner Enumeration Mode
  public enum Mode {
    SMALL_CLIQUES,
    GREEDY_UNION,
    MAX_CLIQUES
  }


  //#########################################################################
  //# Inner Class EventInfo
  private class EventInfo
  {
    //#######################################################################
    //# Constructor
    private EventInfo(final int event)
    {
      mEvent = event;
      mBlocked = mAllCompatibleCovered = mAllCovered = false;
      mRelevantStates = mAllCompatibleSuccessors = null;
    }

    //#######################################################################
    //# Simple Access
    private int getEvent()
    {
      return mEvent;
    }

    private boolean isBlocked()
    {
      return mBlocked;
    }

    private void setBlocked()
    {
      mBlocked = true;
    }

    private boolean isCovered()
    {
      return mAllCovered;
    }

    private void setTransitionInfo(final TIntCollection relevantStates,
                                   final TIntCollection allCompatibleSucc,
                                   final int numRelevantTargets)
    {
      mRelevantStates = relevantStates.toArray();
      mAllCompatibleSuccessors =
        allCompatibleSucc == null || allCompatibleSucc.isEmpty() ?
        null : allCompatibleSucc.toArray();
      if (mAllCompatibleSuccessors != null) {
        Arrays.sort(mAllCompatibleSuccessors);
      }
      mNumRelevantTargets = numRelevantTargets;
      reset();
    }

    private void reset()
    {
      mAllCompatibleCovered = mAllCompatibleSuccessors == null;
      mAllCovered = mAllCompatibleCovered && mRelevantStates.length == 0;
    }

    //#######################################################################
    //# Successors
    private boolean collectAllSuccessors(final int clique,
                                         final TIntArrayList output)
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int dumpIndex = rel.getDumpStateIndex();
      boolean nonEmpty = false;
      final TIntHashSet set = new TIntHashSet();
      mCliqueIterator.reset(clique);
      while (mCliqueIterator.advance()) {
        final int s = mCliqueIterator.getCurrentData();
        mTransitionIterator.reset(s, mEvent);
        while (mTransitionIterator.advance()) {
          final int t = mTransitionIterator.getCurrentTargetState();
          if (t != dumpIndex || !isSupervisedEvent(mEvent)) {
            nonEmpty = true;
            if (mCompatibilityRelation.isRelevant(t)) {
              set.add(t);
            }
          }
        }
      }
      final int[] allCompatibles =
        mCompatibilityRelation.getAllCompatibleStates();
      for (final int s : allCompatibles) {
        mTransitionIterator.reset(s, mEvent);
        while (mTransitionIterator.advance()) {
          final int t = mTransitionIterator.getCurrentTargetState();
          if (t != dumpIndex || !isSupervisedEvent(mEvent)) {
            nonEmpty = true;
            if (mCompatibilityRelation.isRelevant(t)) {
              set.add(t);
            }
          }
        }
      }
      output.clear();
      output.addAll(set);
      output.sort();
      return nonEmpty;
    }

    private void collectRelevantSuccessors(final int clique,
                                           final TIntHashSet cliqueSet,
                                           final TIntArrayList output)
    {
      final TIntHashSet set = new TIntHashSet();
      if (mCliqueDB.size(clique) < mRelevantStates.length) {
        mCliqueIterator.reset(clique);
        while (mCliqueIterator.advance()) {
          final int s = mCliqueIterator.getCurrentData();
          mTransitionIterator.reset(s, mEvent);
          while (mTransitionIterator.advance()) {
            final int t = mTransitionIterator.getCurrentTargetState();
            if (mCompatibilityRelation.isRelevant(t)) {
              set.add(t);
            }
          }
        }
      } else {
        if (cliqueSet.isEmpty()) {
          mCliqueDB.collect(clique, cliqueSet);
        }
        for (final int s : mRelevantStates) {
          if (cliqueSet.contains(s)) {
            mTransitionIterator.reset(s, mEvent);
            while (mTransitionIterator.advance()) {
              final int t = mTransitionIterator.getCurrentTargetState();
              set.add(t);
            }
          }
        }
      }
      output.clear();
      if (!set.isEmpty()) {
        output.addAll(set);
        if (mAllCompatibleSuccessors != null) {
          output.addAll(mAllCompatibleSuccessors);
        }
        output.sort();
      } else if (!mAllCompatibleCovered) {
        output.addAll(mAllCompatibleSuccessors);
      }
      mAllCompatibleCovered = true;
      mAllCovered |= (set.size() == mNumRelevantTargets);
    }

    private boolean isDisabledControllable(final int clique)
    {
      if (!isSupervisedEvent(mEvent)) {
        return false;
      } else if (mBlocked) {
        return true;
      } else {
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int dumpIndex = rel.getDumpStateIndex();
        mCliqueIterator.reset(clique);
        while (mCliqueIterator.advance()) {
          final int s = mCliqueIterator.getCurrentData();
          mTransitionIterator.reset(s, mEvent);
          if (mTransitionIterator.advance()) {
            return mTransitionIterator.getCurrentTargetState() == dumpIndex;
          }
        }
        return false;
      }
    }

    private boolean collectEnablingClique(final TIntArrayList buffer)
      throws UserAbortException
    {
      buffer.clear();
      for (final int s : mCompatibilityRelation.getRelevantStates()) {
        mTransitionIterator.reset(s, mEvent);
        if (mTransitionIterator.advance()) {
          checkAbort();
          for (int i = 0; i < buffer.size(); i++) {
            final int s0 = buffer.get(i);
            if (!mCompatibilityRelation.isCompatible(s0, s)) {
              return false;
            }
          }
          buffer.add(s);
        }
      }
      return buffer.size() > 1;
    }

    //#######################################################################
    //# Data Members
    private final int mEvent;
    private boolean mBlocked;
    private boolean mAllCompatibleCovered;
    private boolean mAllCovered;
    private int[] mRelevantStates;
    private int[] mAllCompatibleSuccessors;
    private int mNumRelevantTargets;
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

    private int getNumberOfRelevantStates()
    {
      return mRelevantStates.length;
    }

    private int getNumberOfAllCompatibleStates()
    {
      return mAllCompatibleStates.length;
    }

    private int[] getAllCompatibleStates()
    {
      return mAllCompatibleStates;
    }

    private int[] getRelevantStates()
    {
      return mRelevantStates;
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
      mBuffer = new TIntArrayList(remaining.size());
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

    private int extendToSmallCover(final TIntArrayList clique)
      throws UserAbortException
    {
      mBuffer.clear();
      if (!clique.isEmpty()) {
        final int cliqueSize = clique.size();
        int last = clique.get(cliqueSize - 1);
        collectCommonNeighbours(clique, mBuffer);
        outer:
        for (int i = 0; i < mBuffer.size(); i++) {
          checkAbort();
          final int s1 = mBuffer.get(i);
          for (int j = 0; j < mBuffer.size(); j++) {
            final int s2 = mBuffer.get(j);
            if (!isCompatible(s1, s2)) {
              continue outer;
            }
          }
          clique.add(s1);
          if (s1 < last) {
            last = -1;
          }
        }
        if (last < 0) {
          clique.sort();
        }
      }
      return mCliqueDB.add(clique);
    }

    private void collectCommonNeighbours(final TIntArrayList cliqueList,
                                         final TIntArrayList neighbours)
      throws UserAbortException
    {
      final TIntHashSet cliqueSet = new TIntHashSet(cliqueList);
      outer:
      for (final int s1 : mRelevantStates) {
        if (!cliqueSet.contains(s1)) {
          checkAbort();
          for (int i2 = 0; i2 < cliqueList.size(); i2++) {
            final int s2 = cliqueList.get(i2);
            if (!isCompatible(s1, s2)) {
              continue outer;
            }
          }
          neighbours.add(s1);
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final TLongHashSet mIncompatiblePairs;
    private int[] mRelevantStates;
    private int[] mAllCompatibleStates;
    private boolean[] mRelevant;
    private TIntArrayList mBuffer;
  }


  //#########################################################################
  //# Inner Class ConnectivityMap
  private class ConnectivityMap
  {
    //#######################################################################
    //# Constructor
    private ConnectivityMap()
    {
      final int numRelevant = mCompatibilityRelation.getNumberOfRelevantStates();
      mInterStateConnectivity = new TLongIntHashMap(numRelevant, 0.5f, -1, 0);
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      final int dumpIndex = rel.getDumpStateIndex();
      mAllCompatibleStatesConnectivity = new int[numStates];
      final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
      while (iter.advance()) {
        final int s = iter.getCurrentFromState();
        final int t = iter.getCurrentToState();
        if (mCompatibilityRelation.isRelevant(s)) {
          if (mCompatibilityRelation.isRelevant(t)) {
            if (s != t) {
              final long pair = WatersIntPairStack.createPair(s, t);
              mInterStateConnectivity.adjustOrPutValue(pair, 1, 1);
            }
          } else if (t != dumpIndex) {
            mAllCompatibleStatesConnectivity[s]++;
          }
        } else if (s != dumpIndex) {
          if (mCompatibilityRelation.isRelevant(t)) {
            mAllCompatibleStatesConnectivity[t]++;
          }
        }
      }
      mCliqueBuffer = new TIntArrayList(numRelevant);
      mNeighboursBuffer = new TIntArrayList(numRelevant);
      mRetainedBuffer = new TIntArrayList(numRelevant);
    }

    //#######################################################################
    //# Computing Connectivity
    private int getAllCompatibleStatesConnectivity(final int state)
    {
      return mAllCompatibleStatesConnectivity[state];
    }

    private int computeConnectivity(final int state,
                                    final TIntArrayList states)
    {
      int result = 0;
      for (int i = 0; i < states.size(); i++) {
        final int s = states.get(i);
        final long pair = WatersIntPairStack.createPair(state, s);
        result += mInterStateConnectivity.get(pair);
      }
      return result;
    }

    //#######################################################################
    //# Clique Extension
    private int extendToMaxClique(final int clique)
      throws UserAbortException
    {
      mCliqueBuffer.clear();
      mCliqueDB.collect(clique, mCliqueBuffer);
      mNeighboursBuffer.clear();
      mCompatibilityRelation.collectCommonNeighbours(mCliqueBuffer,
                                                         mNeighboursBuffer);
      if (mNeighboursBuffer.isEmpty()) {
        return clique;
      }
      NeighbourInfo best = new NeighbourInfo();
      NeighbourInfo other = new NeighbourInfo();
      do {
        checkAbort();
        best.reset(mNeighboursBuffer.get(0));
        for (int i = 1; i < mNeighboursBuffer.size(); i++) {
          other.reset(mNeighboursBuffer.get(i));
          if (other.isBetterThan(best, mCliqueBuffer, mNeighboursBuffer)) {
            final NeighbourInfo tmp = best;
            best = other;
            other = tmp;
          }
        }
        final int bestState = best.getState();
        mCliqueBuffer.add(bestState);
        mRetainedBuffer.clear();
        for (int i = 0; i < mNeighboursBuffer.size(); i++) {
          final int s = mNeighboursBuffer.get(i);
          if (s != bestState &&
              mCompatibilityRelation.isCompatible(s, bestState)) {
            mRetainedBuffer.add(s);
          }
        }
        final TIntArrayList tmp = mNeighboursBuffer;
        mNeighboursBuffer = mRetainedBuffer;
        mRetainedBuffer = tmp;
      } while (!mNeighboursBuffer.isEmpty());
      mCliqueBuffer.sort();
      return mCliqueDB.add(mCliqueBuffer);
    }

    //#######################################################################
    //# Data Members
    private final TLongIntHashMap mInterStateConnectivity;
    private final int[] mAllCompatibleStatesConnectivity;
    private final TIntArrayList mCliqueBuffer;
    private TIntArrayList mNeighboursBuffer;
    private TIntArrayList mRetainedBuffer;
  }


  //#########################################################################
  //# Inner Class NeighbourInfo
  private class NeighbourInfo
  {
    //#######################################################################
    //# Constructor
    private NeighbourInfo()
    {
      this(-1);
    }

    private NeighbourInfo(final int state)
    {
      mState = state;
    }

    //#######################################################################
    //# Simple Access
    private int getState()
    {
      return mState;
    }

    private void reset(final int state)
    {
      mState = state;
      mSelectedStatesConnectivity = -1;
      mOtherStatesConnectivity = -1;
    }

    //#######################################################################
    //# Comparison
    private boolean isBetterThan(final NeighbourInfo info,
                                 final TIntArrayList clique,
                                 final TIntArrayList neighbours)
    {
      int diff = getSelectedStatesConnectivity(clique) -
        info.getSelectedStatesConnectivity(clique);
      if (diff != 0) {
        return diff > 0;
      }
      diff = getOtherStatesConnectivity(neighbours) -
        info.getOtherStatesConnectivity(neighbours);
      if (diff != 0) {
        return diff > 0;
      }
      diff = mConnectivityMap.getAllCompatibleStatesConnectivity(mState) -
        mConnectivityMap.getAllCompatibleStatesConnectivity(info.mState);
      if (diff != 0) {
        return diff > 0;
      }
      return mState < info.mState;
    }

    private int getSelectedStatesConnectivity(final TIntArrayList states)
    {
      if (mSelectedStatesConnectivity < 0) {
        mSelectedStatesConnectivity =
          mConnectivityMap.computeConnectivity(mState, states);
      }
      return mSelectedStatesConnectivity;
    }

    private int getOtherStatesConnectivity(final TIntArrayList states)
    {
      if (mOtherStatesConnectivity < 0) {
        mOtherStatesConnectivity =
          mConnectivityMap.computeConnectivity(mState, states);
      }
      return mOtherStatesConnectivity;
    }

    //#######################################################################
    //# Data Members
    private int mState;
    private int mSelectedStatesConnectivity = -1;
    private int mOtherStatesConnectivity = -1;
  }


  //#########################################################################
  //# Inner Class CliqueQueue
  private class CliqueQueue
  {
    //#######################################################################
    //# Constructor
    private CliqueQueue()
    {
      this(mListBuffer.createList(), getStateLimit());
    }

    private CliqueQueue(final int closed, final int limit)
    {
      final int numRelevant =
        mCompatibilityRelation.getNumberOfRelevantStates();
      mOpenCliques = mListBuffer.createList();
      mClosedCliques = closed;
      mVisited = new TIntHashSet(numRelevant);
      mLimit = limit;
      mListReadIterator = mListBuffer.createReadOnlyIterator();
      mListEditIterator = mListBuffer.createModifyingIterator();
      mCliqueIterator = mCliqueDB.iterator();
      mPropagationBuffer = new TIntArrayList(numRelevant);
      mCliqueBuffer = new TIntArrayList(numRelevant);
      mAdditionsBuffer = new TIntArrayList(numRelevant);
      mSize = mListBuffer.getLength(closed);
    }


    //#######################################################################
    //# Simple Access
    private boolean isFinished()
    {
      return mListBuffer.isEmpty(mOpenCliques);
    }

    private int size()
    {
      return mSize;
    }

    private int getNumberOfSelectedCovers()
    {
      if (mSelectedCovers == null) {
        return 0;
      } else {
        return mSelectedCovers.size();
      }
    }

    private int getSelectedCover(final int s)
    {
      return mSelectedCovers.get(s);
    }

    //#######################################################################
    //# Algorithm
    private void propagate()
      throws AnalysisException
    {
      main:
      while (!isFinished()) {
        final int clique = closeFirst();
        final TIntHashSet cliqueSet = new TIntHashSet();
        final Iterator<EventInfo> iter = mRelevantEvents.iterator();
        while (iter.hasNext()) {
          final EventInfo info = iter.next();
          info.collectRelevantSuccessors(clique, cliqueSet, mPropagationBuffer);
          if (info.isCovered()) {
            iter.remove();
          }
          if (addSmallCover(mPropagationBuffer)) {
            if (!isFirstClosedClique(clique)) {
              continue main;
            }
          }
        }
      }
    }

    //#######################################################################
    //# Queue Management
    private boolean addSmallCover(final TIntArrayList cliqueList)
      throws AnalysisException
    {
      final int clique = mCliqueDB.add(cliqueList);
      if (!mVisited.add(clique)) {
        return false;
      }
      int cover = mCompatibilityRelation.extendToSmallCover(cliqueList);
      if (cover != clique && !mVisited.add(cover)) {
        return false;
      } else if (isDominatedBySome(cover, mOpenCliques) ||
                 isDominatedBySome(cover, mClosedCliques)) {
        return false;
      } else if (mMode == Mode.GREEDY_UNION) {
        cover = removeAllDominatedWithUnion(cover);
      } else {
        removeAllDominated(cover, mOpenCliques);
        removeAllDominated(cover, mClosedCliques);
      }
      if (mSize < mLimit) {
        mListBuffer.prepend(mOpenCliques, cover);
        mSize++;
        return true;
      } else {
        throw new OverflowException(mLimit);
      }
    }

    private int closeFirst()
      throws AnalysisException
    {
      mListEditIterator.reset(mOpenCliques);
      mListEditIterator.advance();
      final int clique = mListEditIterator.getCurrentData();
      if (mMode == Mode.MAX_CLIQUES) {
        final int extension = mConnectivityMap.extendToMaxClique(clique);
        if (extension != clique) {
          mVisited.add(extension);
          mListEditIterator.remove();
          removeAllDominated(extension, mOpenCliques);
          removeAllDominated(extension, mClosedCliques);
          mListBuffer.prepend(mClosedCliques, extension);
          return extension;
        }
      }
      mListEditIterator.moveToStart(mClosedCliques);
      return clique;
    }

    private boolean isFirstClosedClique(final int clique)
    {
      if (mListBuffer.isEmpty(mClosedCliques)) {
        return false;
      } else {
        return mListBuffer.getFirst(mClosedCliques) == clique;
      }
    }

    private boolean isCover(final int existing, final TIntArrayList clique)
    {
      int numAvailable = mCliqueDB.size(existing);
      if (numAvailable < clique.size()) {
        return false;
      } else {
        final WatersIntIterator iter = mCliqueDB.iterator(existing);
        for (int i = 0; i < clique.size(); i++) {
          final int numMissing = clique.size() - i;
          final int cliqueState = clique.get(i);
          boolean found = false;
          while (iter.advance()) {
            final int existingState = iter.getCurrentData();
            if (existingState >= cliqueState) {
              found = (existingState == cliqueState);
              break;
            } else if (--numAvailable < numMissing) {
              return false;
            }
          }
          if (!found) {
            return false;
          }
        }
        return true;
      }
    }

    //#######################################################################
    //# Domination Tests
    private boolean isDominatedBySome(final int clique, final int list)
      throws UserAbortException
    {
      mListReadIterator.reset(list);
      while (mListReadIterator.advance()) {
        checkAbort();
        final int existing = mListReadIterator.getCurrentData();
        if (mCliqueDB.containsAll(existing, clique)) {
          return true;
        }
      }
      return false;
    }

    private void removeAllDominated(final int clique, final int list)
      throws UserAbortException
    {
      mListEditIterator.reset(list);
      while (mListEditIterator.advance()) {
        checkAbort();
        final int existing = mListEditIterator.getCurrentData();
        if (mCliqueDB.containsAll(clique, existing)) {
          mListEditIterator.remove();
          mSize--;
        }
      }
    }

    private int removeAllDominatedWithUnion(final int clique)
      throws UserAbortException
    {
      mCliqueBuffer.clear();
      mCliqueDB.collect(clique, mCliqueBuffer);
      final TIntHashSet cliqueSet = new TIntHashSet(mCliqueBuffer);
      mCliqueDB.collect(clique, cliqueSet);
      if (removeAllDominatedWithUnion(mCliqueBuffer, cliqueSet, mOpenCliques) ||
          removeAllDominatedWithUnion(mCliqueBuffer, cliqueSet, mClosedCliques)) {
        final int union = mCliqueDB.add(cliqueSet);
        mVisited.add(union);
        return union;
      } else {
        return clique;
      }
    }

    private boolean removeAllDominatedWithUnion(final TIntArrayList cliqueList,
                                                final TIntHashSet cliqueSet,
                                                final int list)
      throws UserAbortException
    {
      boolean addedSome = false;
      mListEditIterator.reset(list);
      queueLoop:
      while (mListEditIterator.advance()) {
        checkAbort();
        mAdditionsBuffer.clear();
        final int existing = mListEditIterator.getCurrentData();
        mCliqueIterator.reset(existing);
        while (mCliqueIterator.advance()) {
          final int existingState = mCliqueIterator.getCurrentData();
          if (!cliqueSet.contains(existingState)) {
            for (int i = 0; i < cliqueList.size(); i++) {
              final int cliqueState = cliqueList.get(i);
              if (!mCompatibilityRelation.isCompatible(existingState,
                                                       cliqueState)) {
                continue queueLoop;
              }
            }
            mAdditionsBuffer.add(existingState);
          }
        }
        mListEditIterator.remove();
        mSize--;
        if (!mAdditionsBuffer.isEmpty()) {
          cliqueList.addAll(mAdditionsBuffer);
          cliqueSet.addAll(mAdditionsBuffer);
          addedSome = true;
        }
      }
      return addedSome;
    }

    //#######################################################################
    //# Final Cover Selection
    private int createDumpState()
    {
      final int size = mListBuffer.getLength(mClosedCliques + 1);
      mSelectedCovers = new TIntArrayList(size);
      mSelectedCovers.add(-1);
      return 0;
    }

    private int selectCover(final TIntArrayList clique)
    {
      clique.sort();
      // Start from 1, skip dump state 0
      for (int i = 1; i < mSelectedCovers.size(); i++) {
        final int existing = mSelectedCovers.get(i);
        if (isCover(existing, clique)) {
          return i;
        }
      }
      mListEditIterator.reset(mClosedCliques);
      while (mListEditIterator.advance()) {
        final int closed = mListEditIterator.getCurrentData();
        if (isCover(closed, clique)) {
          final int code = mSelectedCovers.size();
          mSelectedCovers.add(closed);
          mListEditIterator.remove();
          return code;
        }
      }
      return -1;
    }

    private int preselectCover(final TIntArrayList clique)
      throws AnalysisException
    {
      for (int i = 1; i < mSelectedCovers.size(); i++) {
        final int existing = mSelectedCovers.get(i);
        if (isCover(existing, clique)) {
          return -1;
        }
      }
      int found = -1;
      mListReadIterator.reset(mClosedCliques);
      while (mListReadIterator.advance()) {
        final int closed = mListReadIterator.getCurrentData();
        if (isCover(closed, clique)) {
          if (found < 0) {
            found = closed;
          } else {
            return -1;
          }
        }
      }
      return found;
    }

    private void preselectCovers(final TIntArrayList clique)
      throws AnalysisException
    {
      int cover = preselectCover(clique);
      if (cover >= 0) {
        mListBuffer.remove(mClosedCliques, cover);
        mSelectedCovers.add(cover);
        final TIntStack stack = new TIntArrayStack();
        stack.push(cover);
        while (stack.size() > 0) {
          final int c = stack.pop();
          final TIntHashSet cliqueSet = new TIntHashSet();
          final Iterator<EventInfo> iter = mRelevantEvents.iterator();
          while (iter.hasNext()) {
            final EventInfo info = iter.next();
            info.collectRelevantSuccessors(c, cliqueSet, mCliqueBuffer);
            if (info.isCovered()) {
              iter.remove();
            }
            cover = preselectCover(mCliqueBuffer);
            if (cover >= 0) {
              mListBuffer.remove(mClosedCliques, cover);
              mSelectedCovers.add(cover);
              stack.push(cover);
            }
          }
        }
      }
    }

    private void preselectSingletonCovers()
      throws AnalysisException
    {
      final TIntArrayList list = new TIntArrayList(1);
      list.add(-1);
      for (final int s : mCompatibilityRelation.getRelevantStates()) {
        list.set(0, s);
        preselectCovers(list);
      }
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("OPEN: ");
      builder.append(mListBuffer.toString(mOpenCliques));
      builder.append("\nCLOSED: ");
      builder.append(mListBuffer.toString(mClosedCliques));
      if (mSelectedCovers != null) {
        builder.append("\nSELECTED: [");
        for (int i = 1; i < mSelectedCovers.size(); i++) {
          if (i > 1) {
            builder.append(", ");
          }
          builder.append(mSelectedCovers.get(i));
        }
        builder.append(']');
      }
      return builder.toString();
    }

    //#######################################################################
    //# Data Members
    private final int mOpenCliques;
    private final int mClosedCliques;
    private final TIntHashSet mVisited;
    private final int mLimit;
    private final IntListBuffer.ReadOnlyIterator mListReadIterator;
    private final IntListBuffer.ModifyingIterator mListEditIterator;
    private final IntSetBuffer.IntSetIterator mCliqueIterator;
    private final TIntArrayList mPropagationBuffer;
    private final TIntArrayList mCliqueBuffer;
    private final TIntArrayList mAdditionsBuffer;
    private TIntArrayList mSelectedCovers = null;
    private int mSize;
  }


  //#########################################################################
  //# Data Members
  private Mode mMode = Mode.SMALL_CLIQUES;
  private final boolean mAddingEventCovers = false;

  private EventInfo[] mEventInfo;
  private List<EventInfo> mUsedEvents;
  private List<EventInfo> mRelevantEvents;
  private CompatibilityRelation mCompatibilityRelation;
  private long mAllCompatibleMarkings;
  private ConnectivityMap mConnectivityMap;
  private IntSetBuffer mCliqueDB;
  private IntSetBuffer.IntSetIterator mCliqueIterator;
  private IntListBuffer mListBuffer;
  private TransitionIterator mTransitionIterator;

}
