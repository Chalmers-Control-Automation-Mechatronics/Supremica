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

import gnu.trove.iterator.TIntByteIterator;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntByteHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersIntPairStack;
import net.sourceforge.waters.model.analysis.UserAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.options.EnumOption;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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

public class SuWonhamSupervisorReductionTRSimplifier
  extends AbstractSupervisorReductionTRSimplifier
{

  //#########################################################################
  //# Constructors
  public SuWonhamSupervisorReductionTRSimplifier()
  {
    setAppliesPartitionAutomatically(true);
  }

  public SuWonhamSupervisorReductionTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
    setAppliesPartitionAutomatically(true);
  }


  //#########################################################################
  //# Configuration
  public void setPairOrdering(final PairOrdering ordering)
  {
    mPairOrdering = ordering;
  }

  public PairOrdering getPairOrdering()
  {
    return mPairOrdering;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.options.Configurable
  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, StepSimplifierFactory.
              OPTION_SuWonhamSupervisorReduction_PairOrdering);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(StepSimplifierFactory.
                     OPTION_SuWonhamSupervisorReduction_PairOrdering)) {
      final EnumOption<?> enumOption = (EnumOption<?>) option;
      final PairOrdering ordering = (PairOrdering) enumOption.getValue();
      setPairOrdering(ordering);
    } else {
      super.setOption(option);
    }
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
    mPairOrderingHandler = mPairOrdering.createHandler(this);
    mStack = new WatersIntPairStack();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final int numStates = rel.getNumberOfReachableStates();
    mStates1 = new TIntArrayList(numStates);
    mStates2 = new TIntArrayList(numStates);
    mSuccessors2 = new TIntArrayList(numStates);
  }

  @Override
  public boolean runSimplifier() throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TRPartition partition;
    setUpCompatibilityRelation();
    if (mCompatibilityRelation.isEmpty()) {
      partition = createOneStatePartition();
      rel.merge(partition);
    } else {
      partition = mPairOrderingHandler.mergeAllPairs();
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
  public void applyResultPartition()
  {
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mPairOrderingHandler = null;
    mCompatibilityRelation = null;
    mStack = null;
    mStates1 = mStates2 = mSuccessors2 = null;
    mSparePartition = null;
  }


  //#########################################################################
  //# Methods for Supervisor Reduction
  private ListBufferPartition checkMergibility(final int state1,
                                               final int state2)
    throws UserAbortException
  {
    final ListBufferPartition partition;
    if (mSparePartition == null) {
      partition = new ListBufferPartition();
    } else {
      partition = mSparePartition;
      partition.reset();
      mSparePartition = null;
    }
    mStack.clear();
    mStack.push(state1, state2);
    while (!mStack.isEmpty()) {
      checkAbort();
      final long pair = mStack.pop();
      if (!mergeAndExpand(pair, partition)) {
        return null;
      }
    }
    return partition;
  }

  private boolean mergeAndExpand(final long pair,
                                 final ListBufferPartition partition)
  {
    final int state1 = WatersIntPairStack.getLo(pair);
    final int state2 = WatersIntPairStack.getHi(pair);
    if (partition.isEquivalent(state1, state2)) {
      return true;
    } else if (mPairOrderingHandler.hasFailedMergibilityCheck(state1, state2,
                                                              partition)) {
      return false;
    } else if (!partition.isClassCompatible(state1, state2)) {
      return false;
    }

    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int clazz1 = partition.getClassCode(state1);
    partition.getStates(clazz1, mStates1);
    final int clazz2 = partition.getClassCode(state2);
    partition.getStates(clazz2, mStates2);
    if (clazz1 < clazz2) {
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
      mSuccessors2.clear();
      final TIntHashSet set2 = new TIntHashSet();
      for (int i2 = 0; i2 < mStates2.size(); i2++) {
        final int s2 = mStates2.get(i2);
        iter.reset(s2, e);
        if (iter.advance()) {
          final int t2 = iter.getCurrentTargetState();
          if (t2 == dumpIndex) {
            if (isSupervisedEvent(e)) {
              continue eventLoop;
            } else {
              continue;
            }
          }
          final int min2 = partition.getClassCode(t2);
          if (set2.add(min2)) {
            mSuccessors2.add(min2);
          }
        }
      }
      if (mSuccessors2.size() == 0) {
        continue;
      }
      final TIntHashSet set1 = new TIntHashSet();
      for (int i1 = mStates1.size() - 1; i1 >= 0; i1--) {
        final int s1 = mStates1.get(i1);
        iter.reset(s1, e);
        if (iter.advance()) {
          final int t1 = iter.getCurrentTargetState();
          if (t1 == dumpIndex) {
            continue;
          }
          final int min1 = partition.getClassCode(t1);
          if (set1.add(min1)) {
            for (int i2 = mSuccessors2.size() - 1; i2 >= 0; i2--) {
              final int min2 = mSuccessors2.get(i2);
              if (min1 == min2) {
                continue;
              } else if (mPairOrderingHandler.hasFailedMergibilityCheck
                          (min1, min2, partition)) {
                return false;
              } else if (!partition.isClassCompatible(min1, min2)) {
                return false;
              }
              mStack.push(min1, min2);
            }
          }
        }
      }
    }
    return true;
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
    mCompatibilityRelation = new LinkedList<>();
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
      if (!enabled.isEmpty() && !disabled.isEmpty()) {
        final CompatibilityPair pair = new CompatibilityPair(enabled, disabled);
        addCompatibilityPair(mCompatibilityRelation, pair);
      }
    }
    Collections.sort(mCompatibilityRelation);
  }

  private boolean updateCompatibilityRelation(final ListBufferPartition partition)
  {
    List<CompatibilityPair> changedPairs = null;
    Iterator<CompatibilityPair> iter = mCompatibilityRelation.iterator();
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
      iter = mCompatibilityRelation.iterator();
      while (iter.hasNext()) {
        final CompatibilityPair unchanged = iter.next();
        for (final CompatibilityPair changed : changedPairs) {
          if (changed.subsumes(unchanged)) {
            iter.remove();
            break;
          }
        }
      }
      mCompatibilityRelation.addAll(changedPairs);
      Collections.sort(mCompatibilityRelation);
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
    for (final CompatibilityPair pair : mCompatibilityRelation) {
      if (!pair.isCompatible(state1, state2)) {
        return false;
      }
    }
    return true;
  }


  //#########################################################################
  //# Partition
  private TRPartition createOneStatePartition()
    throws OverflowException, UserAbortException
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

  private ListBufferPartition incorporatePartition
    (ListBufferPartition result, final ListBufferPartition partition)
  {
    if (partition != null) {
      applyPartition(partition);
      result = result.merge(partition);
      if (result != partition && mSparePartition == null) {
        mSparePartition = partition;
      }
    }
    return result;
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
    final int dumpIndex = rel.getDumpStateIndex();
    for (int state = 0; state < numStates; state++) {
      if (rel.isReachable(state) && state == partition.getClassCode(state)) {
        events.clear();
        writer.resetState(state);
        while (writer.advance()) {
          final int e = writer.getCurrentEvent();
          final int t = writer.getCurrentTargetState();
          if (t == dumpIndex && !isSupervisedEvent(e)) {
            writer.remove();
          } else {
            events.add(e);
            final int min = partition.getClassCode(t);
            if (t != min) {
              writer.setCurrentToState(min);
            }
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
                final int t = reader.getCurrentTargetState();
                if (t == dumpIndex && !isSupervisedEvent(e)) {
                  // skip
                } else if (events.add(e)) {
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
  //# Inner Enumeration PairOrdering
  public enum PairOrdering
  {
    //#######################################################################
    //# Enumeration
    LEXICOGRAPHIC("Lexicographic") {
      @Override
      PairOrderingHandler
      createHandler(final SuWonhamSupervisorReductionTRSimplifier owner)
      {
        return owner.new LexicographicPairOrderingHandler();
      }
    },
    DIAGONAL1("Diagonal 1") {
      @Override
      PairOrderingHandler
      createHandler(final SuWonhamSupervisorReductionTRSimplifier owner)
      {
        return owner.new Diagonal1PairOrderingHandler();
      }
    },
    DIAGONAL2("Diagonal 2") {
      @Override
      PairOrderingHandler
      createHandler(final SuWonhamSupervisorReductionTRSimplifier owner)
      {
        return owner.new Diagonal2PairOrderingHandler();
      }
    };

    //#######################################################################
    //# Constructor
    private PairOrdering(final String name)
    {
      mName = name;
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public String toString()
    {
      return mName;
    }

    //#######################################################################
    //# Handler Creation
    abstract PairOrderingHandler createHandler
      (SuWonhamSupervisorReductionTRSimplifier owner);

    //#######################################################################
    //# Data Members
    private final String mName;
  }


  //#########################################################################
  //# Inner Interface PairOrderingHandler
  private interface PairOrderingHandler
  {
    public TRPartition mergeAllPairs()
      throws UserAbortException;
    public boolean hasFailedMergibilityCheck
      (int state1, int state2, final ListBufferPartition partition);
  }


  //#########################################################################
  //# Inner Class LexicographicPairOrdering
  private class LexicographicPairOrderingHandler
    implements PairOrderingHandler
  {
    @Override
    public TRPartition mergeAllPairs()
      throws UserAbortException
    {
      ListBufferPartition result = new ListBufferPartition();
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      final int dumpIndex = rel.getDumpStateIndex();
      for (mCurrentIndex1 = 0; mCurrentIndex1 < numStates; mCurrentIndex1++) {
        if (!rel.isReachable(mCurrentIndex1) || mCurrentIndex1 == dumpIndex) {
          continue;
        }
        for (mCurrentIndex2 = mCurrentIndex1 + 1;
             mCurrentIndex2 < numStates;
             mCurrentIndex2++) {
          if (!rel.isReachable(mCurrentIndex2) || mCurrentIndex2 == dumpIndex) {
            continue;
          }
          checkAbort();
          final ListBufferPartition partition =
            checkMergibility(mCurrentIndex1, mCurrentIndex2);
          if (VERBOSE_DEBUG) {
            final Logger logger = LogManager.getFormatterLogger();
            logger.debug("%d/%d - %s", mCurrentIndex1, mCurrentIndex2,
                         partition == null ? "failed" : "merged");
          }
          result = incorporatePartition(result, partition);
        }
      }
      return result.createTRPartition();
    }

    @Override
    public boolean hasFailedMergibilityCheck(final int state1,
                                             final int state2,
                                             final ListBufferPartition partition)
    {
      int min1 = partition.getClassCode(state1);
      int min2 = partition.getClassCode(state2);
      if (min1 > min2) {
        final int tmp = min1;
        min1 = min2;
        min2 = tmp;
      }
      if (min1 < mCurrentIndex1) {
        return true;
      } else if (min1 == mCurrentIndex1) {
        return min2 < mCurrentIndex2;
      } else {
        return false;
      }
    }
  }


  //#########################################################################
  //# Inner Class Diagonal1PairOrderingHandler
  private class Diagonal1PairOrderingHandler implements PairOrderingHandler
  {
    @Override
    public TRPartition mergeAllPairs()
      throws UserAbortException
    {
      ListBufferPartition result = new ListBufferPartition();
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      final int dumpIndex = rel.getDumpStateIndex();
      outerLoop:
      for (mCurrentIndex1 = 1; mCurrentIndex1 < numStates; mCurrentIndex1++) {
        if (!rel.isReachable(mCurrentIndex1) || mCurrentIndex1 == dumpIndex) {
          continue;
        }
        for (mCurrentIndex2 = 0;
             mCurrentIndex2 < mCurrentIndex1;
             mCurrentIndex2++) {
          if (!rel.isReachable(mCurrentIndex2) || mCurrentIndex2 == dumpIndex) {
            continue;
          }
          checkAbort();
          final ListBufferPartition partition =
            checkMergibility(mCurrentIndex1, mCurrentIndex2);
          if (VERBOSE_DEBUG) {
            final Logger logger = LogManager.getFormatterLogger();
            logger.debug("%d/%d - %s", mCurrentIndex1, mCurrentIndex2,
                         partition == null ? "failed" : "merged");
          }
          result = incorporatePartition(result, partition);
          if (!rel.isReachable(mCurrentIndex1)) {
            continue outerLoop;
          }
        }
      }
      return result.createTRPartition();
    }

    @Override
    public boolean hasFailedMergibilityCheck(final int state1,
                                             final int state2,
                                             final ListBufferPartition partition)
    {
      int min1 = partition.getClassCode(state1);
      int min2 = partition.getClassCode(state2);
      if (min1 < min2) {
        final int tmp = min1;
        min1 = min2;
        min2 = tmp;
      }
      if (min1 < mCurrentIndex1) {
        return true;
      } else if (min1 == mCurrentIndex1) {
        return min2 < mCurrentIndex2;
      } else {
        return false;
      }
    }
  }


  //#########################################################################
  //# Inner Class Diagonal2PairOrderingHandler
  private class Diagonal2PairOrderingHandler implements PairOrderingHandler
  {
    @Override
    public TRPartition mergeAllPairs()
      throws UserAbortException
    {
      ListBufferPartition result = new ListBufferPartition();
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      final int dumpIndex = rel.getDumpStateIndex();
      outerLoop:
        for (mCurrentIndex1 = 1; mCurrentIndex1 < numStates; mCurrentIndex1++) {
        if (!rel.isReachable(mCurrentIndex1) || mCurrentIndex1 == dumpIndex) {
          continue;
        }
        for (mCurrentIndex2 = mCurrentIndex1 - 1;
             mCurrentIndex2 >= 0;
             mCurrentIndex2--) {
          if (!rel.isReachable(mCurrentIndex2) || mCurrentIndex2 == dumpIndex) {
            continue;
          }
          checkAbort();
          final ListBufferPartition partition =
            checkMergibility(mCurrentIndex1, mCurrentIndex2);
          if (VERBOSE_DEBUG) {
            final Logger logger = LogManager.getFormatterLogger();
            logger.debug("%d/%d - %s", mCurrentIndex1, mCurrentIndex2,
                         partition == null ? "failed" : "merged");
          }
          result = incorporatePartition(result, partition);
          if (!rel.isReachable(mCurrentIndex1)) {
            continue outerLoop;
          }
        }
      }
      return result.createTRPartition();
    }

    @Override
    public boolean hasFailedMergibilityCheck(final int state1,
                                             final int state2,
                                             final ListBufferPartition partition)
    {
      int min1 = partition.getClassCode(state1);
      int min2 = partition.getClassCode(state2);
      if (min1 < min2) {
        final int tmp = min1;
        min1 = min2;
        min2 = tmp;
      }
      if (min1 < mCurrentIndex1) {
        return true;
      } else if (min1 == mCurrentIndex1) {
        return min2 > mCurrentIndex2;
      } else {
        return false;
      }
    }
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
      byte mode = DONT_CARE;
      final TIntByteIterator iter = other.mStatusMap.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final int state = iter.key();
        final byte status = mStatusMap.get(state);
        if (status == DONT_CARE) {
          return false;
        }
        final boolean equal = status == iter.value();
        switch (mode) {
        case EQUAL:
          if (!equal) {
            return false;
          }
          break;
        case INVERSE:
          if (equal) {
            return false;
          }
          break;
        default:
          mode = equal ? EQUAL : INVERSE;
          break;
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
      if (mMap == null) {
        return null;
      }
      final int list = mMap.get(clazz);
      if (list == IntListBuffer.NULL) {
        return null;
      }
      return mListBuffer.createReadOnlyIterator(list);
    }

    private TRPartition createTRPartition()
    {
      if (mMap == null) {
        return null;
      } else {
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int numStates = rel.getNumberOfStates();
        final List<int[]> classes = new ArrayList<>(numStates);
        for (int s = 0; s < numStates; s++) {
          final int list = mMap.get(s);
          if (list == IntListBuffer.NULL ||
              mListBuffer.getFirst(list) != s) {
            classes.add(null);
          } else {
            final int[] array = mListBuffer.toArray(list);
            classes.add(array);
          }
        }
        return new TRPartition(classes, numStates);
      }
    }

    private int getClassCode(final int state)
    {
      if (mMap == null) {
        return state;
      }
      final int list = mMap.get(state);
      if (list == IntListBuffer.NULL) {
        return state;
      }
      return mListBuffer.getFirst(list);
    }

    private void getStates(final int clazz, final TIntArrayList buffer)
    {
      buffer.clear();
      if (mMap == null) {
        buffer.add(clazz);
        return;
      }
      final int list = mMap.get(clazz);
      if (list == IntListBuffer.NULL) {
        buffer.add(clazz);
        return;
      }
      mListBuffer.toTIntCollection(list, buffer);
    }

    private void initialize()
    {
      if (mMap == null) {
        mMap = new TIntIntHashMap(10, 0.5f, -1, IntListBuffer.NULL);
        if (mListBuffer == null) {
          mListBuffer = new IntListBuffer();
          mIterator = mListBuffer.createReadOnlyIterator();
        } else {
          mListBuffer.clear();
        }
      }
    }

    private boolean isClassCompatible(final int state1,
                                      final int state2)
    {
      final int list1, list2;
      if (mMap == null) {
        list1 = list2 = IntListBuffer.NULL;
      } else {
        list1 = mMap.get(state1);
        list2 = mMap.get(state2);
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
        for (final CompatibilityPair pair : mCompatibilityRelation) {
          if (!pair.isCompatible(iter1, iter2)) {
            return false;
          }
        }
        return true;
      }
    }

    private boolean isEquivalent(final int state1, final int state2)
    {
      if (mMap == null) {
        return state1 == state2;
      }
      final int list1 = mMap.get(state1);
      if (list1 == IntListBuffer.NULL) {
        return state1 == state2;
      }
      return list1 == mMap.get(state2);
    }

    private ListBufferPartition merge(final ListBufferPartition partition)
    {
      if (mMap == null) {
        return partition;
      } else if (partition.mMap == null) {
        return this;
      } else {
        final IntListBuffer.Iterator listIter = partition.mIterator;
        final TIntIntIterator mapIter = partition.mMap.iterator();
        while (mapIter.hasNext()) {
          mapIter.advance();
          final int clazz = mapIter.key();
          final int list = mapIter.value();
          listIter.reset(list);
          listIter.advance();
          if (listIter.getCurrentData() == clazz) {
            while (listIter.advance()) {
              final int clazz2 = listIter.getCurrentData();
              mergeClasses(clazz, clazz2);
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
        final int list1 = mMap.get(clazz1);
        final int list2 = mMap.get(clazz2);
        if (list1 == IntListBuffer.NULL) {
          if (list2 == IntListBuffer.NULL) {
            final int list = mListBuffer.createList();
            mListBuffer.append(list, clazz1);
            mListBuffer.append(list, clazz2);
            mMap.put(clazz1, list);
            mMap.put(clazz2, list);
          } else {
            mMap.put(clazz1, list2);
            mListBuffer.prepend(list2, clazz1);
          }
        } else {
          if (list2 == IntListBuffer.NULL) {
            mMap.put(clazz2, list1);
            mListBuffer.append(list1, clazz2);
          } else {
            mIterator.reset(list2);
            while (mIterator.advance()) {
              final int state = mIterator.getCurrentData();
              mMap.put(state, list1);
            }
            mListBuffer.catenateDestructively(list1, list2);
          }
        }
      }
    }

    private void reset()
    {
      mMap = null;
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      if (mMap == null) {
        return "(identity partition)";
      } else {
        final StringBuilder builder = new StringBuilder();
        final int[] keys = mMap.keys();
        for (final int clazz : keys) {
          if (getClassCode(clazz) == clazz) {
            builder.append(clazz);
            builder.append(": [");
            final int list = mMap.get(clazz);
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
    private TIntIntHashMap mMap;
    private IntListBuffer mListBuffer;
    private IntListBuffer.Iterator mIterator;
  }


  //#########################################################################
  //# Data Members
  private PairOrdering mPairOrdering = PairOrdering.LEXICOGRAPHIC;

  private PairOrderingHandler mPairOrderingHandler;
  private List<CompatibilityPair> mCompatibilityRelation;
  private int mCurrentIndex1;
  private int mCurrentIndex2;
  private WatersIntPairStack mStack;
  private TIntArrayList mStates1;
  private TIntArrayList mStates2;
  private TIntArrayList mSuccessors2;
  private ListBufferPartition mSparePartition;


  //#########################################################################
  //# Class Constants
  private static final byte DONT_CARE = 0;
  private static final byte ENABLING = 1;
  private static final byte DISABLING = 2;
  private static final byte EQUAL = 3;
  private static final byte INVERSE = 4;

  private static final boolean VERBOSE_DEBUG = false;

}
