//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   LimitedCertainConflictsTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntStack;
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
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_ALL;
  }

  @Override
  public void reset()
  {
    mStateInfo = null;
    super.reset();
  }


  //#########################################################################
  //# Specific Access
  public boolean hasRemovedTransitions()
  {
    return mHasRemovedTransitions;
  }

  public int getMaxLevel()
  {
    return mMaxLevel;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.op.AbstractTRSimplifier
  @Override
  protected void setUp()
  throws AnalysisException
  {
    super.setUp();
    mStateInfo = null;
    mHasRemovedTransitions = false;
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
    int level = mMaxLevel = BLOCKING;
    int numCoreachable = findCoreachableStates(level);
    int numReachable = rel.getNumberOfReachableStates();
    if (numCoreachable == numReachable) {
      return false;
    }
    final int tauID = EventEncoding.TAU;
    final int numStates = rel.getNumberOfStates();
    final int shift = AutomatonTools.log2(numStates);
    final int mask = (1 << shift) - 1;
    final int numEvents = rel.getNumberOfProperEvents();
    final int eshift = AutomatonTools.log2(numEvents);
    final int root = 1 << (shift + eshift);
    final TransitionIterator closureIter =
      rel.createPredecessorsTauClosureIterator();
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
            mHasRemovedTransitions = modified = true;
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
        if (mStateInfo[state] >= level && rel.isReachable(state)) {
          checkAbort();
          mPredecessorsIterator.reset(state, -1);
          while (mPredecessorsIterator.advance()) {
            final int event = mPredecessorsIterator.getCurrentEvent();
            final int pred = mPredecessorsIterator.getCurrentSourceState();
            if (event != tauID && mStateInfo[pred] == COREACHABLE) {
              closureIter.resetState(pred);
              while (closureIter.advance()) {
                final int ppred = closureIter.getCurrentSourceState();
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
    if (numCoreachable == numReachable - 1) {
      // Only one state of certain conflicts. No result partition,
      // but let us try to add selfloops and remove events.
      int bstate;
      for (bstate = 0; bstate < numStates; bstate++) {
        if (mStateInfo[bstate] != COREACHABLE) {
          break;
        }
      }
      succIter.reset(bstate, -1);
      result |= succIter.advance();
      for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
        if (rel.isUsedEvent(event)) {
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
    final int numEvents = rel.getNumberOfProperEvents();
    for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
      if (rel.isUsedEvent(event)) {
        rel.addTransition(bstate, event, bstate);
      }
    }
    rel.removeProperSelfLoopEvents();
    rel.removeOutgoingTransitions(bstate);
  }

  /**
   * Creates a test automaton to check whether certain conflicts states of
   * the given level can be reached. States of certain conflicts are flagged
   * using selfloops of the given event <CODE>prop</CODE>, which can be tested
   * for using language inclusion check.
   */
  public AutomatonProxy createTestAutomaton
    (final ProductDESProxyFactory factory,
     final EventEncoding eventEnc,
     final StateEncoding stateEnc,
     final int init,
     final EventProxy prop,
     final int level)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numEvents = eventEnc.getNumberOfEvents();
    final Collection<EventProxy> events = new ArrayList<EventProxy>(numEvents);
    for (int e = 0; e < eventEnc.getNumberOfProperEvents(); e++) {
      if (rel.isUsedEvent(e)) {
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
      if (isTestState(state, level)) {
        numReachable++;
        if (mStateInfo[state] == level) {
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
      if (isTestState(state, level)) {
        final StateProxy memstate = new MemStateProxy(code++, state == init);
        states[state] = memstate;
        reachable.add(memstate);
        if (mStateInfo[state] == level) {
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
      final int t = iter.getCurrentTargetState();
      if (isTestState(s, level) && isTestState(t, level)) {
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

  private boolean isTestState(final int state, final int level)
  {
    final int status = mStateInfo[state];
    return status == COREACHABLE || status >= level;
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

  private int mMaxLevel;
  private int[] mStateInfo;
  private TIntStack mUnvisitedStates;
  private TransitionIterator mPredecessorsIterator;


  //#########################################################################
  //# Class Constants
  private static final int BLOCKING = 0;
  private static final int COREACHABLE = -1;

}
