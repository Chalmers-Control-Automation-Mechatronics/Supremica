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

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.MemStateProxy;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.TransitionListBuffer;
import net.sourceforge.waters.model.analysis.UserAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * <P>An implementation of the <I>Certain Conflicts Rule</I>.</P>
 *
 * <P>This rule identifies blocking states and some other states representing
 * certain conflicts in a given automaton, and replaces these states by
 * a single blocking states. The following properties are used to approximate
 * the <I>set of certain conflicts</I>.</P>
 *
 * <UL>
 * <LI>Every blocking state is a state of certain conflicts.</LI>
 * <LI>Every state with an outgoing always enabled ({@link
 *     EventStatus#STATUS_ALWAYS_ENABLED}) transition to a state of certain
 *     conflicts also is a state of certain conflicts.</LI>
 * <LI>If a state&nbsp;<I>x</I> has an outgoing transition labelled by
 *     event&nbsp;<I>e</I> to a state of certain conflicts, or if such a
 *     transition is reachable from <I>x</I> via  a sequence of silent
 *     transitions, then all other transitions from&nbsp;<I>x</I>
 *     labelled&nbsp;<I>e</I> can be removed.</LI>
 * </UL>
 *
 * <P>As transitions are removed, new blocking states may emerge, so the
 * above properties are re-evaluated repeatedly until saturation.</P>
 *
 * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional Verification
 * in Supervisory Control. SIAM Journal of Control and Optimization,
 * <STRONG>48</STRONG>(3), 1914-1938, 2009.<BR>
 * Colin Pilbrow, Robi Malik. Compositional Nonblocking Verification with
 * Always Enabled Events and Selfloop-only Events. Proc. 2nd International
 * Workshop on Formal Techniques for Safety-Critical Systems, FTSCS 2013,
 * 147-162, Queenstown, New Zealand, 2013.</P>
 *
 * @author Robi Malik, Colin Pilbrow
 */

public class LimitedCertainConflictsTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#######################################################################
  //# Constructors
  public LimitedCertainConflictsTRSimplifier()
  {
  }

  public LimitedCertainConflictsTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_ALL;
  }

  @Override
  public boolean isAlwaysEnabledEventsSupported()
  {
    return true;
  }

  @Override
  public CertainConflictsStatistics getStatistics()
  {
    return (CertainConflictsStatistics) super.getStatistics();
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats = new CertainConflictsStatistics(this);
    return setStatistics(stats);
  }

  @Override
  public void reset()
  {
    mLevelInfo = null;
    super.reset();
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractMarkingTRSimplifier
  @Override
  public boolean isDumpStateAware()
  {
    return true;
  }


  //#########################################################################
  //# Specific Access
  /**
   * Returns whether the last run has removed transitions from coreachable
   * states. If this returns <CODE>false</CODE>, the result may be treated
   * as the merging of blocking states, which allows for simple trace
   * expansion. Otherwise proper certain conflicts trace expansion using
   * {@link net.sourceforge.waters.analysis.compositional.LimitedCertainConflictsTraceExpander
   * LimitedCertainConflictsTraceExpander} is necessary.
   */
  public boolean hasCertainConflictTransitions()
  {
    return mHasCertainConflictTransitions;
  }

  /**
   * Returns the maximum level assigned to any state during the last run.
   * @see #getLevel(int) getLevel()
   */
  public int getMaxLevel()
  {
    return mMaxLevel;
  }

  /**
   * Returns the level of certain conflicts for the given state.
   * After completion of the algorithm, each state is assigned a level
   * as follows.
   * <UL>
   * <LI>States with level COREACHABLE = -1 are not states of certain
   *     conflicts.</LI>
   * <LI>States with level BLOCKING = 0 are blocking states.</LI>
   * <LI>States with a positive odd level can reach a state of the
   *     lower level using local transitions.</LI>
   * <LI>States with a positive even level can reach a state of a
   *     lower level using shared transitions, but not using only local
   *     transitions.</LI>
   * </UL>
   * @param  state    State code of the state to be checked.
   * @return The level of certain conflicts for this state.
   */
  public int getLevel(final int state)
  {
    return mLevelInfo[state];
  }

  /**
   * Returns the array containing the levels of all states.
   * @see #getLevel(int) getLevel()
   */
  public int[] getLevels()
  {
    return mLevelInfo;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp()
  throws AnalysisException
  {
    super.setUp();
    mLevelInfo = null;
    mHasCertainConflictTransitions = false;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mAllEventsBackwardsIterator = rel.createPredecessorsReadOnlyIterator();
    mAlwaysEnabledBackwardsIterator =
      rel.createPredecessorsReadOnlyIteratorByStatus
        (EventStatus.STATUS_ALWAYS_ENABLED);
    mProperEventsBackwardsIterator =
      rel.createPredecessorsReadOnlyIterator(~EventStatus.STATUS_LOCAL);
  }

  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    final int defaultID = getDefaultMarkingID();
    if (defaultID < 0) {
      return false;
    }
    mMaxLevel = COREACHABLE;
    int level = BLOCKING;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    int numReachable = rel.getNumberOfReachableStates();
    if (numReachable <= 1) {
      return false;
    }
    int numCoreachable = findCoreachableStates(level);
    if (numCoreachable == numReachable) {
      return false;
    }
    mMaxLevel = BLOCKING;
    final int numStates = rel.getNumberOfStates();
    final int shift = AutomatonTools.log2(numStates);
    final int mask = (1 << shift) - 1;
    final int numEvents = rel.getNumberOfProperEvents();
    final int eshift = AutomatonTools.log2(numEvents);
    final int root = 1 << (shift + eshift);
    final TauClosure closure = rel.createPredecessorsTauClosure(0);
    final TransitionIterator closureIter = closure.createIterator();
    final TransitionIterator succIter = rel.createSuccessorsReadOnlyIterator();
    boolean result = false;
    boolean modified;
    final TIntArrayList victims = new TIntArrayList();
    do {
      int nextlevel = level + 1;
      modified = false;
      // check for always enabled transitions to certain conflicts
      for (int state = 0; state < numStates; state++) {
        if (mLevelInfo[state] == level && rel.isReachable(state)) {
          checkAbort();
          mUnvisitedStates.push(state);
          while (mUnvisitedStates.size() > 0) {
            final int popped = mUnvisitedStates.pop();
            mAlwaysEnabledBackwardsIterator.resetState(popped);
            while (mAlwaysEnabledBackwardsIterator.advance()) {
              final int pred =
                mAlwaysEnabledBackwardsIterator.getCurrentSourceState();
              if (mLevelInfo[pred] == COREACHABLE) {
                mMaxLevel = nextlevel;
                mLevelInfo[pred] = nextlevel;
                mUnvisitedStates.push(pred);
                victims.add(pred);
              }
            }
          }
          if (!victims.isEmpty()) {
            mHasCertainConflictTransitions = modified = true;
            for (int index = 0; index < victims.size(); index++) {
              final int victim = victims.get(index);
              rel.removeOutgoingTransitions(victim);
              rel.setMarked(victim, defaultID, false);
            }
            victims.clear();
          }
        }
      }
      // check for proper event transitions to certain conflicts
      nextlevel++;
      for (int state = 0; state < numStates; state++) {
        if (mLevelInfo[state] >= level && rel.isReachable(state)) {
          checkAbort();
          mProperEventsBackwardsIterator.resetState(state);
          while (mProperEventsBackwardsIterator.advance()) {
            final int pred =
              mProperEventsBackwardsIterator.getCurrentSourceState();
            if (mLevelInfo[pred] == COREACHABLE) {
              closureIter.resetState(pred);
              while (closureIter.advance()) {
                final int ppred = closureIter.getCurrentSourceState();
                final int event =
                  mProperEventsBackwardsIterator.getCurrentEvent();
                succIter.reset(ppred, event);
                while (succIter.advance()) {
                  if (ppred != pred) {
                    final int code = (event << shift) | ppred;
                    victims.add(code);
                    break;
                  } else if (succIter.getCurrentTargetState() != state) {
                    final int code = root | (event << shift) | ppred;
                    victims.add(code);
                    break;
                  }
                }
              }
            }
          }
          if (!victims.isEmpty()) {
            modified = true;
            for (int index = 0; index < victims.size(); index++) {
              final int victim = victims.get(index);
              final int event = (victim & ~root) >>> shift;
              final int pred = victim & mask;
              if (!mHasCertainConflictTransitions && mLevelInfo[pred] < 0) {
                // We have certain conflict transitions if we are deleting a
                // transition from a coreachable state to another coreachable
                // state.
                assert level == 0;
                succIter.reset(pred, event);
                while (succIter.advance()) {
                  final int target = succIter.getCurrentTargetState();
                  if (mLevelInfo[target] < 0) {
                    mHasCertainConflictTransitions = true;
                    break;
                  }
                }
              }
              rel.removeOutgoingTransitions(pred, event);
              if ((victim & root) != 0) {
                rel.addTransition(pred, event, state);
              }
            }
            victims.clear();
          }
        }
      }
      if (modified) {
        result = true;
        level = nextlevel;
        rel.checkReachability();
        final int newNumCoreachable = findCoreachableStates(level);
        if (newNumCoreachable == numCoreachable) {
          break;
        }
        numCoreachable = newNumCoreachable;
        mMaxLevel = nextlevel;
      }
    } while (modified);

    numReachable = rel.getNumberOfReachableStates();
    final int dumpIndex = rel.getDumpStateIndex();
    assert mLevelInfo[dumpIndex] == BLOCKING : "Dump state not found blocking?";
    int blockingInit = -1;
    if (numCoreachable < numReachable) {
      // Some reachable states are blocking. Is one of them initial?
      if (rel.isInitial(dumpIndex)) {
        blockingInit = dumpIndex;
      } else {
        for (int s = 0; s < numStates; s++) {
          if (mLevelInfo[s] != COREACHABLE && rel.isInitial(s)) {
            blockingInit = s;
            break;
          }
        }
      }
    }

    int outputConfig = getPreferredOutputConfiguration();
    if (outputConfig == 0) {
      outputConfig = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    }
    if (blockingInit >= 0) {
      // There is a blocking initial state.
      // Mark all other states as unreachable, and delete all transitions.
      rel.reset(0, outputConfig);
      for (int s = 0; s < numStates; s++) {
        if (s != blockingInit) {
          rel.setReachable(s, false);
        }
      }
      rel.setMarked(blockingInit, defaultID, false);
      final byte status = rel.getProperEventStatus(EventEncoding.TAU);
      rel.setProperEventStatus(EventEncoding.TAU,
                               status | EventStatus.STATUS_UNUSED);
      mHasCertainConflictTransitions = result = true;
    } else if (numCoreachable == numReachable - 1) {
      // Only one reachable state of certain conflicts---find it ...
      int ccState = -1;
      if (rel.isReachable(dumpIndex)) {
        ccState = dumpIndex;
      } else {
        for (int s = 0; s < numStates; s++) {
          if (mLevelInfo[s] >= 0) {
            ccState = s;
            break;
          }
        }
        assert ccState >= 0 : "Certain conflicts state not found?";
      }
      // Try to remove selfloops ...
      rel.reconfigure(outputConfig);
      final TransitionListBuffer succ = rel.getSuccessorBuffer();
      if (succ != null) {
        result |= succ.removeStateTransitions(ccState);
      }
      final TransitionListBuffer pred = rel.getPredecessorBuffer();
      if (pred != null) {
        final TransitionIterator iter = pred.createModifyingIterator(ccState);
        while (iter.advance()) {
          if (iter.getCurrentSourceState() == ccState) {
            iter.remove();
            result = true;
          }
        }
      }
      result |= removeProperSelfLoopEvents();
      result |= rel.removeTauSelfLoops();
    } else {
      // More than one reachable state of certain conflicts.
      // TODO CONFIG_PREDECESSORS version?
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      // Create partition to facilitate simple trace expansion ...
      if (!mHasCertainConflictTransitions) {
        final int[] stateToClass = new int[numStates];
        for (int s = 0; s < numStates; s++) {
          if (!rel.isReachable(s)) {
            stateToClass[s] = -1;
          } else if (mLevelInfo[s] >= 0) {
            stateToClass[s] = dumpIndex;
          } else {
            stateToClass[s] = s;
          }
        }
        final TRPartition partition = new TRPartition(stateToClass, numStates);
        setResultPartition(partition);
      }
      // Replace all certain conflicts states by the dump state ...
      final TransitionIterator iter = rel.createAllTransitionsModifyingIterator();
      while (iter.advance()) {
        final int t = iter.getCurrentTargetState();
        if (mLevelInfo[t] >= 0 && t != dumpIndex) {
          iter.setCurrentToState(dumpIndex);
        }
      }
      for (int s = 0; s < numStates; s++) {
        if (mLevelInfo[s] >= 0) {
          rel.setReachable(s, s == dumpIndex);
        }
      }
      removeProperSelfLoopEvents();
      rel.removeTauSelfLoops();
      result = true;
    }
    return result;
  }

  @Override
  protected void tearDown()
  {
    mAllEventsBackwardsIterator = null;
    mAlwaysEnabledBackwardsIterator = null;
    mProperEventsBackwardsIterator = null;
    mUnvisitedStates = null;
    super.tearDown();
  }

  @Override
  protected void recordFinish(final boolean success)
  {
    super.recordFinish(success);
    final CertainConflictsStatistics stats = getStatistics();
    if (stats != null) {
      stats.recordMaxCertainConflictsLevel(mMaxLevel);
    }
  }


  //#########################################################################
  //# Trace Computation Support
  /**
   * Creates a test automaton to check whether certain conflict states of
   * the given or a lower level can be reached. States of certain conflicts
   * of levels to be checked are flagged using selfloops of the given event
   * <CODE>prop</CODE>, so their reachability can be tested using a language
   * inclusion check.
   */
  public AutomatonProxy createTestAutomaton
    (final ProductDESProxyFactory factory,
     final EventEncoding eventEnc,
     final StateEncoding stateEnc,
     final int initCode,
     final EventProxy prop,
     final int level)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numEvents = eventEnc.getNumberOfEvents();
    final Collection<EventProxy> events = new ArrayList<EventProxy>(numEvents);
    for (int e = 0; e < eventEnc.getNumberOfProperEvents(); e++) {
      if ((rel.getProperEventStatus(e) & EventStatus.STATUS_UNUSED) == 0) {
        final EventProxy event = eventEnc.getProperEvent(e);
        if (event != null) {
          events.add(event);
        }
      }
    }
    events.add(prop);
    final int numStates = rel.getNumberOfStates();
    int numReachable = 0;
    int numCritical = 0;
    for (int state = 0; state < numStates; state++) {
      if (rel.isReachable(state)) {
        numReachable++;
        if (mLevelInfo[state] <= level) {
          numCritical++;
        }
      }
    }
    final StateProxy[] states = new StateProxy[numStates];
    final List<StateProxy> reachable = new ArrayList<StateProxy>(numReachable);
    final int numTrans = rel.getNumberOfTransitions();
    final Collection<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(numTrans + numCritical);
    int code = 0;
    for (int state = 0; state < numStates; state++) {
      if (rel.isReachable(state)) {
        final boolean init =
          initCode >= 0 ? state == initCode : rel.isInitial(state);
        final StateProxy memstate = new MemStateProxy(code++, init);
        states[state] = memstate;
        reachable.add(memstate);
        final int info = mLevelInfo[state];
        if (info != COREACHABLE && info <= level) {
          final TransitionProxy trans =
            factory.createTransitionProxy(memstate, prop, memstate);
          transitions.add(trans);
        }
      }
    }
    stateEnc.init(states);
    final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
    while (iter.advance()) {
      final int s = iter.getCurrentSourceState();
      if (rel.isReachable(s)) {
        final int t = iter.getCurrentTargetState();
        final StateProxy source = states[s];
        final int e = iter.getCurrentEvent();
        final EventProxy event = eventEnc.getProperEvent(e);
        final StateProxy target = states[t];
        final TransitionProxy trans =
          factory.createTransitionProxy(source, event, target);
        transitions.add(trans);
      }
    }
    final String name = rel.getName() + ":certainconf:" + level;
    final ComponentKind kind = ComponentKind.PLANT;
    return factory.createAutomatonProxy(name, kind,
                                        events, reachable, transitions);
  }

  public int findTauReachableState(final int state, int level)
  {
    level++;
    int info = mLevelInfo[state];
    if (info < level) {
      return state;
    } else if (info == level) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      final int tau = EventEncoding.TAU;
      final TIntArrayList queue = new TIntArrayList();
      final TIntHashSet visited = new TIntHashSet();
      queue.add(state);
      visited.add(state);
      for (int rpos = 0; rpos < queue.size(); rpos++) {
        final int current = queue.get(rpos);
        iter.reset(current, tau);
        while (iter.advance()) {
          final int succ = iter.getCurrentTargetState();
          info = mLevelInfo[succ];
          if (info < level) {
            return succ;
          } else if (info == level && visited.add(succ)) {
            queue.add(succ);
          }
        }
      }
      assert false : "Could not find state with expected level!";
      return -1;
    } else {
      return -1;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private int findCoreachableStates(final int level)
  throws UserAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int defaultID = getDefaultMarkingID();
    if (mLevelInfo == null) {
      mLevelInfo = new int[numStates];
      mUnvisitedStates = new TIntArrayStack();
      if (level != 0) {
        Arrays.fill(mLevelInfo, level);
      }
    } else {
      for (int state = 0; state < numStates; state++) {
        if (mLevelInfo[state] == COREACHABLE) {
          mLevelInfo[state] = level;
        }
      }
    }
    int coreachable = 0;
    for (int state = 0; state < numStates; state++) {
      if (rel.isMarked(state, defaultID) &&
          rel.isReachable(state) &&
          mLevelInfo[state] == level) {
        checkAbort();
        mLevelInfo[state] = COREACHABLE;
        mUnvisitedStates.push(state);
        coreachable++;
        while (mUnvisitedStates.size() > 0) {
          final int popped = mUnvisitedStates.pop();
          mAllEventsBackwardsIterator.resetState(popped);
          while (mAllEventsBackwardsIterator.advance()) {
            final int pred = mAllEventsBackwardsIterator.getCurrentSourceState();
            if (rel.isReachable(pred) && mLevelInfo[pred] == level) {
              mLevelInfo[pred] = COREACHABLE;
              mUnvisitedStates.push(pred);
              coreachable++;
            }
          }
        }
      }
    }
    return coreachable;
  }


  //#########################################################################
  //# Data Members
  private boolean mHasCertainConflictTransitions;

  private int mMaxLevel;
  private int[] mLevelInfo;
  private TIntStack mUnvisitedStates;
  private TransitionIterator mAllEventsBackwardsIterator;
  private TransitionIterator mAlwaysEnabledBackwardsIterator;
  private TransitionIterator mProperEventsBackwardsIterator;


  //#########################################################################
  //# Class Constants
  private static final int BLOCKING = 0;
  private static final int COREACHABLE = -1;

}
