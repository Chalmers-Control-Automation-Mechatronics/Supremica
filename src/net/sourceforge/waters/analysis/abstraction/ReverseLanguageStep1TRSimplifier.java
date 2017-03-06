//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntSetBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.InvalidModelException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.NondeterministicDESException;
import net.sourceforge.waters.model.base.ProxyTools;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongIntHashMap;


/**
 * <P>A transition relation simplifier that implements an experimental
 * supervisor reduction algorithm based on reverse languages This
 * simplifier performs the first step of the algorithm, which determines
 * the reverse language of separation.</P>
 *
 * @author Robi Malik
 */

public class ReverseLanguageStep1TRSimplifier
  extends AbstractSupervisorReductionTRSimplifier
{

  //#########################################################################
  //# Constructors
  public ReverseLanguageStep1TRSimplifier()
    throws AnalysisException
  {
  }

  public ReverseLanguageStep1TRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_PREDECESSORS;
  }

  @Override
  public boolean isPartitioning()
  {
    return false;
  }


  //#########################################################################
  //# Configuration
  public void setEnablingSupervisor(final boolean enabling)
  {
    mEnablingSupervisor = enabling;
  }

  public boolean getEnablingSupervisor()
  {
    return mEnablingSupervisor;
  }

  /**
   * Sets the state limit. The states limit specifies the maximum
   * number of states that will be created.
   * @param limit
   *          The new state limit, or {@link Integer#MAX_VALUE} to allow
   *          an unlimited number of states.
   */
  public void setStateLimit(final int limit)
  {
    mStateLimit = limit;
  }

  /**
   * Gets the state limit.
   * @see #setStateLimit(int) setStateLimit()
   */
  public int getStateLimit()
  {
    return mStateLimit;
  }

  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions that will be created.
   * @param limit
   *          The new transition limit, or {@link Integer#MAX_VALUE} to allow
   *          an unlimited number of transitions.
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
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if (!rel.isDeterministic()) {
      throw new NondeterministicDESException
        (ProxyTools.getShortClassName(this) +
         " does not support nondeterministic input transition relation '" +
         rel.getName() + "'!");
    }
    final int numStates = rel.getNumberOfStates();
    final int numEvents = rel.getNumberOfProperEvents();
    int supervisedEvent = getSupervisedEvent();
    if (supervisedEvent < 0 || supervisedEvent >= numEvents) {
      supervisedEvent = -1;
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        if ((rel.getProperEventStatus(e) & EventStatus.STATUS_CONTROLLABLE) != 0) {
          if (supervisedEvent < 0) {
            supervisedEvent = e;
          } else {
            supervisedEvent = -1;
            break;
          }
        }
      }
      if (supervisedEvent < 0) {
        supervisedEvent = getSupervisedEvent();
        throw new InvalidModelException("Invalid supervised event number " +
                                        supervisedEvent + " for " +
                                        ProxyTools.getShortClassName(this) + "!");
      }
      setSupervisedEvent(supervisedEvent);
    }
    mSetBuffer = new IntSetBuffer(numStates);
    mPairList = new TLongArrayList(numStates);
    mPairMap = new TLongIntHashMap(numStates, 0.5f, -1, -1);
    mPreTransitionBuffer = new PreTransitionBuffer(numEvents, mTransitionLimit);
    mEnablingList = new TIntArrayList(numStates);
    mDisablingList = new TIntArrayList(numStates);
    mSetIterator = mSetBuffer.iterator();
    mPredecessorsIterator = rel.createPredecessorsReadOnlyIterator();
    mEmptySetIndex = mSetBuffer.add(mEnablingList);
    final int dumpIndex = rel.getDumpStateIndex();
    for (int s = 0; s < numStates; s++) {
      if (rel.isReachable(s) && s != dumpIndex) {
        mEnablingList.add(s);
        if (rel.isInitial(s)) {
          mInitialState = s;
        }
      }
    }
    mFullSetIndex = mSetBuffer.add(mEnablingList);
    mEnablingList.clear();
  }

  @Override
  public boolean runSimplifier() throws AnalysisException
  {
    createInitialPair();
    for (int p = 0; p < mPairList.size(); p++) {
      expandPair(p);
    }
    createPairsTR();
    return true;
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mSetBuffer = null;
    mPairList = null;
    mPairMap = null;
    mPreTransitionBuffer = null;
    mEnablingList = mDisablingList = null;
    mSetIterator = null;
    mPredecessorsIterator = null;
  }


  //#########################################################################
  //# Algorithm
  private int createInitialPair()
  {
    mEnablingList.clear();
    mDisablingList.clear();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int dumpIndex = rel.getDumpStateIndex();
    final int supervisedEvent = getSupervisedEvent();
    final TransitionIterator iter =
      rel.createAllTransitionsReadOnlyIterator(supervisedEvent);
    while (iter.advance()) {
      final int source = iter.getCurrentSourceState();
      if (iter.getCurrentTargetState() == dumpIndex) {
        mDisablingList.add(source);
      } else {
        mEnablingList.add(source);
      }
    }
    mDecisivePair = createPair(mEnablingList, mDisablingList);
    return mDecisivePair;
  }

  private void expandPair(final int targetPairIndex)
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numEvents = rel.getNumberOfProperEvents();
    final long pair = mPairList.get(targetPairIndex);
    final int enablingIndex = (int) (pair & 0xffffffffL);
    final int disablingIndex = (int) (pair >> 32);
    if (mSetBuffer.size(enablingIndex) == 0 ||
        mSetBuffer.size(disablingIndex) == 0) {
      checkAbort();
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        mPreTransitionBuffer.addTransition(targetPairIndex, e, targetPairIndex);
      }
    } else {
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        checkAbort();
        collectPredecessors(enablingIndex, e, mEnablingList);
        collectPredecessors(disablingIndex, e, mDisablingList);
        final int sourcePairIndex = createPair(mEnablingList, mDisablingList);
        if (sourcePairIndex >= 0) {
          mPreTransitionBuffer.addTransition(targetPairIndex, e, sourcePairIndex);
          mEnablingList.clear();
          mDisablingList.clear();
        }
      }
    }
  }

  private void collectPredecessors(final int setIndex,
                                   final int event,
                                   final TIntArrayList outputList)
  {
    outputList.clear();
    mSetIterator.reset(setIndex);
    while (mSetIterator.advance()) {
      final int target = mSetIterator.getCurrentData();
      mPredecessorsIterator.reset(target, event);
      while (mPredecessorsIterator.advance()) {
        final int source = mPredecessorsIterator.getCurrentSourceState();
        outputList.add(source);
      }
    }
  }

  private int createPair(final TIntArrayList enablingList,
                         final TIntArrayList disablingList)
  {
    final long enablingIndex, disablingIndex;
    if (enablingList.isEmpty()) {
      if (disablingList.isEmpty()) {
        return -1;
      } else if (!mEnablingSupervisor) {
        enablingIndex = mEmptySetIndex;
        disablingIndex = mFullSetIndex;
      } else {
        return -1;
      }
    } else {
      if (!disablingList.isEmpty()) {
        enablingList.sort();
        enablingIndex = mSetBuffer.add(enablingList);
        disablingList.sort();
        disablingIndex = mSetBuffer.add(disablingList);
      } else if (mEnablingSupervisor) {
        enablingIndex = mFullSetIndex;
        disablingIndex = mEmptySetIndex;
      } else {
        return -1;
      }
    }
    final long pair = enablingIndex | (disablingIndex << 32);
    final int next = mPairList.size();
    int index = mPairMap.putIfAbsent(pair, next);
    if (index < 0) {
      index = next;
      mPairList.add(pair);
    }
    return index;
  }

  private void createPairsTR()
    throws OverflowException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = mPairList.size();
    final int numTrans = mPreTransitionBuffer.size();
    rel.reset(numStates, numTrans,
              ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final int numProps = rel.getNumberOfPropositions();
    final int defaultMarking = getDefaultMarkingID();
    for (int p = 0; p < numProps; p++) {
      rel.setPropositionUsed(p, p == defaultMarking);
    }
    final int dump = rel.getDumpStateIndex();
    for (int s = 0; s < numStates; s++) {
      final long pair = mPairList.get(s);
      final int set;
      if (mEnablingSupervisor) {
        set = (int) (pair & 0xffffffffL);
      } else {
        set = (int) (pair >> 32);
      }
      if (set == mFullSetIndex || mSetBuffer.contains(set, mInitialState)) {
        rel.setInitial(s, true);
      }
      if (s != dump) {
        rel.setMarked(s, defaultMarking, true);
      }
    }
    mPreTransitionBuffer.addIncomingTransitions(rel);
    if (!mEnablingSupervisor) {
      final int event = getSupervisedEvent();
      rel.addTransition(mDecisivePair, event, dump);
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mEnablingSupervisor = false;
  private int mStateLimit = Integer.MAX_VALUE;
  private int mTransitionLimit = Integer.MAX_VALUE;

  private IntSetBuffer mSetBuffer;
  private TLongArrayList mPairList;
  private TLongIntHashMap mPairMap;
  private PreTransitionBuffer mPreTransitionBuffer;

  private TIntArrayList mEnablingList;
  private TIntArrayList mDisablingList;
  private IntSetBuffer.IntSetIterator mSetIterator;
  private TransitionIterator mPredecessorsIterator;
  private int mDecisivePair;
  private int mInitialState;
  private long mEmptySetIndex;
  private long mFullSetIndex;
}
