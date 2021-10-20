//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A transition relation simplifier to remove all silent (&tau;)
 * transitions while preserving language equivalence.</P>
 *
 * <P>This simplifier collapses local event transitions aggressively.
 * It eliminates all &tau; transitions while possibly introducing
 * nondeterministic branching. The number of states cannot increase,
 * but the number of transitions can.</P>
 *
 * @author Robi Malik
 */

public class TauEliminationTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructor
  public TauEliminationTRSimplifier()
  {
  }

  public TauEliminationTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets what events are considered as silent. The default of this setting
   * is <CODE>true</CODE>, which means that the simplifier checks for
   * {@link EventEncoding#TAU TAU} events only. If this is set to
   * <CODE>false</CODE>, then all local events, i.e., all events with status
   * {@link EventStatus#STATUS_LOCAL STATUS_LOCAL} will be removed.
   */
  public void setTauOnly(final boolean tauOnly)
  {
    mTauOnly = tauOnly;
  }

  /**
   * Returns what events are considered as silent.
   * @see #setTauOnly(boolean)
   */
  public boolean isTauOnly()
  {
    return mTauOnly;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }

  @Override
  public boolean isPartitioning()
  {
    return false;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return false;
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, true, false);
    return setStatistics(stats);
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
    mIterator = rel.createSuccessorsReadOnlyIterator();
    mCurrentGroupStack = new TIntArrayStack(numStates >> 6);
    mPendingRootsStack = new TIntArrayStack(numStates >> 1);
    mRoot = new boolean[numStates];
    mMerging = false;
  }

  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    for (int s = 0; s < numStates; s++) {
      if (rel.isInitial(s)) {
        explore(s);
      }
    }
    if (mMerging) {
      cleanUp();
      applyResultPartition();
    }
    return mMerging;
  }

  @Override
  protected void tearDown()
  {
    mIterator = null;
    mRoot = null;
    super.tearDown();
  }

  @Override
  public void applyResultPartition()
    throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final byte status = rel.getProperEventStatus(EventEncoding.TAU);
    rel.setProperEventStatus(EventEncoding.TAU,
                             status | EventStatus.STATUS_UNUSED);
    rel.removeProperSelfLoopEvents();
    rel.removeRedundantPropositions();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void explore(final int start)
    throws AnalysisAbortException
  {
    if (!mRoot[start]) {
      mRoot[start] = true;
      mPendingRootsStack.push(start);
      while (mPendingRootsStack.size() > 0) {
        checkAbort();
        final int root = mPendingRootsStack.pop();
        exploreGroup(root);
      }
    }
  }

  private void exploreGroup(final int root)
    throws AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TIntHashSet visited = new TIntHashSet();
    visited.add(root);
    mCurrentGroupStack.push(root);
    long allMarkings = rel.createMarkings();
    while (mCurrentGroupStack.size() > 0) {
      checkAbort();
      final int s = mCurrentGroupStack.pop();
      final long markings = rel.getAllMarkings(s);
      allMarkings = rel.mergeMarkings(allMarkings, markings);
      mIterator.resetState(s);
      while (mIterator.advance()) {
        final int e = mIterator.getCurrentEvent();
        final int t = mIterator.getCurrentTargetState();
        if (isTau(e)) {
          if (visited.add(t)) {
            mCurrentGroupStack.push(t);
          }
          mMerging = true;
        } else {
          if (s != root) {
            rel.addTransition(root, e, t);
          }
          if (!mRoot[t]) {
            mRoot[t] = true;
            mPendingRootsStack.push(t);
          }
        }
      }
    }
    rel.setAllMarkings(root, allMarkings);
  }

  private boolean isTau(final int e)
  {
    if (mTauOnly) {
      return e == EventEncoding.TAU;
    } else {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final byte status = rel.getProperEventStatus(e);
      return (status & EventStatus.STATUS_LOCAL) != 0;
    }
  }

  private void cleanUp()
    throws AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter;
    if (mTauOnly) {
      iter = rel.createSuccessorsModifyingIterator();
      iter.resetEvent(EventEncoding.TAU);
    } else {
      iter = rel.createSuccessorsModifyingIteratorByStatus
        (EventStatus.STATUS_LOCAL);
    }
    final int numStates = rel.getNumberOfStates();
    for (int s = 0; s < numStates; s++) {
      checkAbort();
      if (mRoot[s]) {
        iter.resetState(s);
        while (iter.advance()) {
          iter.remove();
        }
      } else {
        rel.setReachable(s, false);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mTauOnly = true;
  private TransitionIterator mIterator;
  private TIntStack mCurrentGroupStack;
  private TIntStack mPendingRootsStack;
  private boolean[] mRoot;
  private boolean mMerging;

}
