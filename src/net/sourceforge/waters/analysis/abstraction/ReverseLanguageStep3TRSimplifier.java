//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;

import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.hash.TLongHashSet;


/**
 * <P>A transition relation simplifier that implements an experimental
 * supervisor reduction algorithm based on reverse languages This
 * simplifier performs the third step of the algorithm, which deletes
 * transitions that are not present in the unreduced supervisor.</P>
 *
 * @author Robi Malik
 */

public class ReverseLanguageStep3TRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public ReverseLanguageStep3TRSimplifier()
  {
  }

  public ReverseLanguageStep3TRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }


  //#########################################################################
  //# Configuration
  /**
   * Stores the original unreduced supervisor on this transition relation
   * simplifier.
   * @param  rel     Transition relation of the original supervisor.
   *                 This transition relation is copied, and the copy
   *                 stored in the transition relation simplifier.
   */
  public void setOriginalSupervisor(final ListBufferTransitionRelation rel)
  {
    mOriginalSupervisor = new ListBufferTransitionRelation
      (rel, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
  }

  public ListBufferTransitionRelation getOriginalSupervisor()
  {
    return mOriginalSupervisor;
  }

  /**
   * Sets the state limit. The states limit specifies the maximum
   * number of states that will be explored.
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


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    final int numStates = mOriginalSupervisor.getNumberOfStates();
    mPairSet = new TLongHashSet(numStates, 0.5f, -1);
    mTransitionSet = new TLongHashSet(numStates, 0.5f, -1);
    mPairList = new TLongArrayList(numStates);
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mTransitionIterator1 = rel.createSuccessorsReadOnlyIterator();
    mTransitionIterator2 = mOriginalSupervisor.createSuccessorsReadOnlyIterator();
  }

  @Override
  public boolean runSimplifier() throws AnalysisException
  {
    createInitialPair();
    for (int p = 0; p < mPairList.size(); p++) {
      expandPair(p);
    }
    final boolean removed = removeTransitions();
    if (removed) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      rel.checkReachability();
      final int defaultID = getDefaultMarkingID();
      rel.removeProperSelfLoopEvents(defaultID);
    }
    return removed;
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mPairSet = mTransitionSet = null;
    mPairList = null;
    mTransitionIterator1 = mTransitionIterator2 = null;
  }

  @Override
  public void reset()
  {
    mOriginalSupervisor = null;
  }


  //#########################################################################
  //# Algorithm
  private void createInitialPair()
    throws OverflowException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int s1 = rel.getFirstInitialState();
    final int s2 = mOriginalSupervisor.getFirstInitialState();
    createPair(s1, s2);
  }

  private void expandPair(final int p)
    throws OverflowException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numEvents = rel.getNumberOfProperEvents();
    final long pair = mPairList.get(p);
    final int s1 = (int) (pair & 0xffffffff);
    mTransitionIterator1.resetState(s1);
    final int s2 = (int) (pair >>> 32);
    mTransitionIterator2.resetState(s2);
    boolean advance1 = mTransitionIterator1.advance();
    boolean advance2 = mTransitionIterator2.advance();
    while (advance1 || advance2) {
      final int e1 =
        advance1 ? mTransitionIterator1.getCurrentEvent() : numEvents;
      final int e2 =
        advance2 ? mTransitionIterator2.getCurrentEvent() : numEvents;
      if (e1 == e2) {
        final int t1 = mTransitionIterator1.getCurrentTargetState();
        final int t2 = mTransitionIterator2.getCurrentTargetState();
        recordTransition(s1, e1);
        createPair(t1, t2);
        advance1 = mTransitionIterator1.advance();
        advance2 = mTransitionIterator2.advance();
      } else if (e1 < e2) {
        final byte status2 = mOriginalSupervisor.getProperEventStatus(e1);
        if (!EventStatus.isUsedEvent(status2)) {
          final int t1 = mTransitionIterator1.getCurrentTargetState();
          createPair(t1, s2);
        }
        advance1 = mTransitionIterator1.advance();
      } else {
        final byte status1 = rel.getProperEventStatus(e2);
        if (!EventStatus.isUsedEvent(status1)) {
          final int t2 = mTransitionIterator2.getCurrentTargetState();
          recordTransition(s1, e2);
          createPair(s1, t2);
        }
        advance2 = mTransitionIterator2.advance();
      }
    }
  }

  private boolean createPair(final int s1, final int s2)
    throws OverflowException
  {
    final long pair = s1 | ((long) s2 << 32);
    if (!mPairSet.add(pair)) {
      return false;
    } else if (mPairList.size() < mStateLimit) {
      mPairList.add(pair);
      return true;
    } else {
      throw new OverflowException(OverflowKind.STATE, mStateLimit);
    }
  }

  private boolean recordTransition(final int s, final int e)
  {
    final long pair = s | ((long) e << 32);
    return mTransitionSet.add(pair);
  }

  private boolean removeTransitions()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createAllTransitionsModifyingIterator();
    boolean removed = false;
    while (iter.advance()) {
      final int s = iter.getCurrentSourceState();
      final int e = iter.getCurrentEvent();
      final long pair = s | ((long) e << 32);
      if (!mTransitionSet.contains(pair)) {
        iter.remove();
        removed = true;
      }
    }
    return removed;
  }


  //#########################################################################
  //# Data Members
  private ListBufferTransitionRelation mOriginalSupervisor;
  private int mStateLimit = Integer.MAX_VALUE;

  private TLongHashSet mPairSet;
  private TLongHashSet mTransitionSet;
  private TLongArrayList mPairList;
  private TransitionIterator mTransitionIterator1;
  private TransitionIterator mTransitionIterator2;
}
