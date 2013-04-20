//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   LimitedCertainConflictsTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.MemStateProxy;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


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
 * <LI>Every state with an outgoing silent transition to a state of certain
 *     conflicts also is a state of certain conflicts.</LI>
 * <LI>If a state&nbsp;<I>s</I> has an outgoing transition labelled by
 *     event&nbsp;<I>e</I> to a state of certain conflicts, or if such a
 *     transition  is reachable from <I>s</I> via  a sequence of silent
 *     transitions, then all other transitions from&nbsp;<I>s</I>
 *     labelled&nbsp;<I>e</I> can be removed.</LI>
 * </UL>
 *
 * <P>As transitions are removed, new blocking states may emerge, so the
 * above properties are re-evaluated repeatedly until saturation.</P>
 *
 * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional Verification
 * in Supervisory Control. SIAM Journal of Control and Optimization,
 * 48(3), 1914-1938, 2009.</P>
 *
 * @author Robi Malik
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
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_ALL;
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
    mStateInfo = null;
    super.reset();
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
    return mStateInfo[state];
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp()
  throws AnalysisException
  {
    super.setUp();
    mStateInfo = null;
    mHasRemovedTransitions = false;
    mHasCertainConflictTransitions = false;
  }

  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    final int defaultID = getDefaultMarkingID();
    if (defaultID < 0) {
      return false;
    }
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mPredecessorsIterator = rel.createPredecessorsReadOnlyIterator();
    mMaxLevel = COREACHABLE;
    int level = BLOCKING;
    int numCoreachable = findCoreachableStates(level);
    int numReachable = rel.getNumberOfReachableStates();
    if (numCoreachable == numReachable) {
      return false;
    }
    mMaxLevel = BLOCKING;
    final int tauID = EventEncoding.TAU;
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
      // check for tau-transitions to certain conflicts
      for (int state = 0; state < numStates; state++) {
        if (mStateInfo[state] == level && rel.isReachable(state)) {
          checkAbort();
          mUnvisitedStates.push(state);
          while (mUnvisitedStates.size() > 0) {
            final int popped = mUnvisitedStates.pop();
            mPredecessorsIterator.reset(popped, tauID);
            while (mPredecessorsIterator.advance()) {
              final int pred = mPredecessorsIterator.getCurrentSourceState();
              if (mStateInfo[pred] == COREACHABLE) {
                mMaxLevel = nextlevel;
                mStateInfo[pred] = nextlevel;
                mUnvisitedStates.push(pred);
                victims.add(pred);
              }
            }
          }
          if (!victims.isEmpty()) {
            mHasRemovedTransitions = mHasCertainConflictTransitions =
              modified = true;
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
      mPredecessorsIterator.resetEvents(EventEncoding.NONTAU, numEvents);
      for (int state = 0; state < numStates; state++) {
        if (mStateInfo[state] >= level && rel.isReachable(state)) {
          checkAbort();
          mPredecessorsIterator.resetState(state);
          while (mPredecessorsIterator.advance()) {
            final int pred = mPredecessorsIterator.getCurrentSourceState();
            if (mStateInfo[pred] == COREACHABLE) {
              closureIter.resetState(pred);
              while (closureIter.advance()) {
                final int ppred = closureIter.getCurrentSourceState();
                final int event = mPredecessorsIterator.getCurrentEvent();
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
            mHasRemovedTransitions = modified = true;
            for (int index = 0; index < victims.size(); index++) {
              final int victim = victims.get(index);
              final int event = (victim & ~root) >>> shift;
              final int pred = victim & mask;
              if (!mHasCertainConflictTransitions && mStateInfo[pred] < 0) {
                // We have certain conflict transitions if we are deleting a
                // transition from a coreachable state to another coreachable
                // state.
                assert level == 0;
                succIter.reset(pred, event);
                while (succIter.advance()) {
                  final int target = succIter.getCurrentTargetState();
                  if (mStateInfo[target] < 0) {
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

    rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    numReachable = rel.getNumberOfReachableStates();
    if (numCoreachable == 0) {
      // No coreachable states. Merge all reachable states into a single
      // blocking state, and be sure to remove all transitions.
      if (numReachable > 1) {
        result = true;
        final int[] clazz = new int[numReachable];
        int index = 0;
        for (int state = 0; state < numStates; state++) {
          if (rel.isReachable(state)) {
            clazz[index++] = state;
            rel.removeOutgoingTransitions(state);
          }
        }
        final int[][] partition = new int[1][];
        partition[0] = clazz;
        setResultPartitionArray(partition);
        applyResultPartitionAutomatically();
      } else {
        result = mHasRemovedTransitions;
        for (int state = 0; state < numStates; state++) {
          if (rel.isReachable(state)) {
            result |= rel.removeOutgoingTransitions(state);
            break;
          }
        }
      }
    } else if (numCoreachable == numReachable - 1) {
      // Only one state of certain conflicts. No result partition,
      // but let us try to add selfloops and remove events.
      int bstate;
      for (bstate = 0; bstate < numStates; bstate++) {
        if (rel.isReachable(bstate) && mStateInfo[bstate] != COREACHABLE) {
          break;
        }
      }
      succIter.reset(bstate, -1);
      result |= succIter.advance();
      for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
        if ((rel.getProperEventStatus(event) & EventEncoding.STATUS_UNUSED) == 0) {
          rel.addTransition(bstate, event, bstate);
        }
      }
      result |= rel.removeProperSelfLoopEvents();
      rel.removeOutgoingTransitions(bstate);
    } else {
      // More than one state of certain conflicts.
      // Create a partition that can be applied separately.
      result = true;
      final int numClasses = numCoreachable + 1;
      final int[][] partition = new int[numClasses][];
      final int numBlocking = numReachable - numCoreachable;
      final int[] bclazz = new int[numBlocking];
      int bindex = 0;
      int cindex = 0;
      for (int state = 0; state < numStates; state++) {
        if (mStateInfo[state] == COREACHABLE) {
          final int[] clazz = new int[1];
          clazz[0] = state;
          partition[cindex++] = clazz;
        } else if (rel.isReachable(state)) {
          bclazz[bindex++] = state;
        }
      }
      partition[cindex] = bclazz;
      setResultPartitionArray(partition);
      applyResultPartitionAutomatically();
    }
    return result;
  }

  @Override
  protected void tearDown()
  {
    mPredecessorsIterator = null;
    mUnvisitedStates = null;
    super.tearDown();
  }

  @Override
  protected void applyResultPartition()
  throws AnalysisException
  {
    // 1. Remove all transitions originating from certain conflicts states.
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final List<int[]> partition = getResultPartition();
    final int end = partition.size();
    final int[] bclass = partition.listIterator(end).previous();
    for (final int state : bclass) {
      rel.removeOutgoingTransitions(state);
    }
    // 2. Apply the partition
    super.applyResultPartition();
    // 3. Add selfloops to certain conflicts and try to remove events
    rel.removeTauSelfLoops();
    final int bstate = end - 1;
    if (bstate > 0) {
      final int numEvents = rel.getNumberOfProperEvents();
      for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
        if ((rel.getProperEventStatus(event) & EventEncoding.STATUS_UNUSED) == 0) {
          rel.addTransition(bstate, event, bstate);
        }
      }
      rel.removeProperSelfLoopEvents();
    }
    rel.removeOutgoingTransitions(bstate);
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
      if ((rel.getProperEventStatus(e) & EventEncoding.STATUS_UNUSED) == 0) {
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
        if (mStateInfo[state] <= level) {
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
        final int info = mStateInfo[state];
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
    int info = mStateInfo[state];
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
          info = mStateInfo[succ];
          if (info < level) {
            return succ;
          } else if (info == level && visited.add(succ)) {
            queue.add(succ);
          }
        }
      }
      assert false : "Could not found state with expected level!";
      return -1;
    } else {
      return -1;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private int findCoreachableStates(final int level)
  throws AbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int defaultID = getDefaultMarkingID();
    if (mStateInfo == null) {
      mStateInfo = new int[numStates];
      mUnvisitedStates = new TIntStack();
      if (level != 0) {
        Arrays.fill(mStateInfo, level);
      }
    } else {
      for (int state = 0; state < numStates; state++) {
        if (mStateInfo[state] == COREACHABLE) {
          mStateInfo[state] = level;
        }
      }
    }
    mPredecessorsIterator.resetEvent(-1);
    int coreachable = 0;
    for (int state = 0; state < numStates; state++) {
      if (rel.isMarked(state, defaultID) &&
          rel.isReachable(state) &&
          mStateInfo[state] == level) {
        checkAbort();
        mStateInfo[state] = COREACHABLE;
        mUnvisitedStates.push(state);
        coreachable++;
        while (mUnvisitedStates.size() > 0) {
          final int popped = mUnvisitedStates.pop();
          mPredecessorsIterator.resetState(popped);
          while (mPredecessorsIterator.advance()) {
            final int pred = mPredecessorsIterator.getCurrentSourceState();
            if (rel.isReachable(pred) && mStateInfo[pred] == level) {
              mStateInfo[pred] = COREACHABLE;
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
  private boolean mHasRemovedTransitions;
  private boolean mHasCertainConflictTransitions;

  private int mMaxLevel;
  private int[] mStateInfo;
  private TIntStack mUnvisitedStates;
  private TransitionIterator mPredecessorsIterator;


  //#########################################################################
  //# Class Constants
  private static final int BLOCKING = 0;
  private static final int COREACHABLE = -1;

}
