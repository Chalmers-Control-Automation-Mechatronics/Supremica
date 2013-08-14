//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   HalfWaySynthesisTRSimplifier
//###########################################################################
//# $Id: 44365f9ce27545868ec37b61ed041ded9c304a22 $
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.set.hash.TLongHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventEncoding.OrderingInfo;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * Transition relation simplifier that implements halfway synthesis.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class CertainUnsupervisabilityTRSimplifier extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public CertainUnsupervisabilityTRSimplifier()
  {
  }

  public CertainUnsupervisabilityTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the operation mode of halfway synthesis.
   * The operation mode determines which transitions to dump state are
   * retained and which are deleted.
   */
  public void setOutputMode(final OutputMode mode)
  {
    mOutputMode = mode;
  }

  /**
   * Gets the operation mode for halfway synthesis.
   */
  public OutputMode getOutputMode()
  {
    return mOutputMode;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_PREDECESSORS;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return false;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    mBadStates = new BitSet(numStates);
  }

  // TODO tearDown()

  @Override
  public void reset()
  {
    super.reset();
    mBadStates = null;
  }

  @Override
  protected boolean runSimplifier() throws AnalysisException
  {
    final int defaultID = getDefaultMarkingID();
    if (defaultID < 0) {
      return false;
    }
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();

    // 1. Do synthesis --- find bad states
    calculateUnsupervisableStates();
    // If there are no bad states, no need to synthesise
    final int dumpState = mBadStates.nextSetBit(0);
    if (dumpState < 0) {
      return false;
    }

    // 2. Is some initial state bad?
    //  If yes, abstract to empty automaton. Supervisor cannot exist.
    int numBadStates = 0;
    for (int s = dumpState; s >= 0; s = mBadStates.nextSetBit(s + 1)) {
      checkAbort();
      if (rel.isInitial(s)) {
        final List<int[]> partition = Collections.emptyList();
        setResultPartitionList(partition);
        applyResultPartitionAutomatically();
        return true;
      }
      numBadStates++;
    }

    // 3. The transitions relation changes if:
    //  - there is more than one dump state;
    //  - there are outgoing transitions from bad states;
    //  - there are non-retained transitions to bad states;
    boolean needPartition = numBadStates > 1;
    if (!needPartition) {
      final TransitionIterator iter =
        rel.createPredecessorsReadOnlyIterator(dumpState);
      while (iter.advance()) {
        checkAbort();
        final int event = iter.getCurrentEvent();
        final byte status = rel.getProperEventStatus(event);
        if (!mOutputMode.isRetainedEvent(status)) {
          needPartition = true;
          break;
        }
      }
    }
    if (!needPartition) {
      final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
      while (iter.advance()) {
        checkAbort();
        final int source = iter.getCurrentSourceState();
        if (mBadStates.get(source)) {
          needPartition = true;
          break;
        }
      }
    }

    // 4. Create result partition.
    //  Singleton classes for all safe states, no entries for bad states.
    final List<int[]> partition = new ArrayList<int[]>(numStates);
    for (int s = 0; s < numStates; s++) {
      if (mBadStates.get(s) || !rel.isReachable(s)) {
        partition.add(null);
      } else {
        final int[] clazz = new int[1];
        clazz[0] = s;
        partition.add(clazz);
      }
    }
    setResultPartitionList(partition);
    applyResultPartitionAutomatically();

    return needPartition;
  }


  @Override
  public void applyResultPartition()
    throws AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final List<int[]> partition = getResultPartition();
    if (partition.isEmpty()) {
      // 5a. Set all states unreachable, set all events unused.
      final int numStates = rel.getNumberOfStates();
      for (int state = 0; state < numStates; state++) {
        rel.setReachable(state, false);
      }
      final int numEvents = rel.getNumberOfProperEvents();
      for (int event = 0; event < numEvents; event++) {
        final byte status = rel.getProperEventStatus(event);
        rel.setProperEventStatus(event, status | EventEncoding.STATUS_UNUSED);
      }
      final long none = rel.createMarkings();
      rel.setUsedPropositions(none);
    } else {
      // 5b. Check transitions from safe states to bad states.
      //  Delete controllable transitions to bad state.
      //  Redirect other bad state transitions to dump state.
      final int dumpState = mBadStates.nextSetBit(0);
      boolean dumpReachable = rel.isInitial(dumpState);
      final TransitionIterator iter =
        rel.createAllTransitionsModifyingIterator();
      while (iter.advance()) {
        checkAbort();
        final int source = iter.getCurrentSourceState();
        if (mBadStates.get(source)) {
          iter.remove();
        } else {
          final int target = iter.getCurrentTargetState();
          if (mBadStates.get(target)) {
            final int event = iter.getCurrentEvent();
            final byte status = rel.getProperEventStatus(event);
            if (mOutputMode.isRetainedEvent(status)) {
              if (target != dumpState) {
                rel.addTransition(source, event, dumpState);
                iter.remove();
              }
              dumpReachable = true;
            } else {
              iter.remove();
            }
          }
        }
      }
      int s = dumpReachable ? mBadStates.nextSetBit(dumpState + 1) : dumpState;
      for (; s >= 0; s = mBadStates.nextSetBit(s + 1)) {
        rel.setReachable(s, false);
      }
      final int config = getPreferredOutputConfiguration() |
        ListBufferTransitionRelation.CONFIG_SUCCESSORS;
      rel.reconfigure(config);
      if (rel.checkReachability()) {
        // Fix result partition --- is this safe???
        final ListIterator<int[]> liter = partition.listIterator();
        while (liter.hasNext()) {
          final int[] clazz = liter.next();
          if (clazz != null) {
            final int state = clazz[0];
            if (!rel.isReachable(state)) {
              liter.set(null);
            }
          }
        }
      }
      final int defaultID = getDefaultMarkingID();
      rel.removeProperSelfLoopEvents(defaultID);
      rel.removeRedundantPropositions();
    }
  }


  //#########################################################################
  //# Auxiliary Methods

  private void calculateUnsupervisableStates()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final OrderingInfo info = rel.getOrderingInfo();
    final int firstLocalUnont =  info.getFirstEventIndex
      (EventEncoding.STATUS_LOCAL, ~EventEncoding.STATUS_CONTROLLABLE);
    final int lastLocalUncont = info.getLastEventIndex
      (EventEncoding.STATUS_LOCAL, ~EventEncoding.STATUS_CONTROLLABLE);
    //TODO
    final TauClosure closure = rel.createPredecessorsTauClosure
      (firstLocalUnont, lastLocalUncont, Integer.MAX_VALUE);
    final TransitionIterator tauIter = closure.createIterator();
    final TransitionIterator tauEventIter = closure.createPreEventClosureIterator();
    final TransitionIterator eventIter = rel.createPredecessorsReadOnlyIterator();
    final int prop = getDefaultMarkingID();
    final int numStates = rel.getNumberOfStates();
    mBadStates = new BitSet(numStates);
    mUnsupervisablePairs = new TLongHashSet(numStates);
    boolean foundNewBad = true;
    while (foundNewBad) {
      mCoreachableStates = new BitSet(numStates);
      final TIntStack stack = new TIntArrayStack();
      tauIter.reset();
      for (int x = 0; x < numStates; x++) {
        if (rel.isMarked(x, prop)) {
          tauIter.resume(x);
          while (tauIter.advance()) {
            final int w = tauIter.getCurrentSourceState();
            if(!mBadStates.get(w)) {
              mCoreachableStates.set(w);
              stack.push(w);
            }
          }
        }
      }
      while (stack.size() > 0) {
        final int x = stack.pop();
        eventIter.resetState(x);
        while (eventIter.advance()) {
          final int w = eventIter.getCurrentSourceState();
          if (!mCoreachableStates.get(w) && !mBadStates.get(w)) {
            final int sigma = eventIter.getCurrentEvent();
            final long key = (((long)w)<<32)|sigma;
            if (!mUnsupervisablePairs.contains(key)) {
              mCoreachableStates.set(w);
              stack.push(w);
            }
          }
        }
      }
      foundNewBad = false;
      for (int s = mCoreachableStates.previousClearBit(numStates-1); s >= 0;
           s = mCoreachableStates.previousClearBit(s-1)) {
        if (!mBadStates.get(s) && rel.isReachable(s)) {
          foundNewBad = true;
          tauIter.resetState(s);
          while (tauIter.advance()) {
            final int x = tauIter.getCurrentSourceState();
            mBadStates.set(x);
            tauEventIter.resetState(x);
            tauEventIter.resetEventsByStatus(~EventEncoding.STATUS_LOCAL,
                                             ~EventEncoding.STATUS_CONTROLLABLE);
            while (tauEventIter.advance()) {
              final int w = tauEventIter.getCurrentSourceState();
              final int upsilon = tauEventIter.getCurrentEvent();
              final long key = (((long)w)<<32)|upsilon;
              mUnsupervisablePairs.add(key);
            }
          }
        }
      }
    }
  }

  //#########################################################################
  //# Inner Enumeration OutputMode
  /**
   * The operation mode of halfway synthesis.
   * Different settings determine which transitions to dump state are
   * retained and which are deleted.
   */
  public enum OutputMode
  {
    /**
     * Halfway synthesis mode to compute an abstraction.
     * Controllable transitions to dump states are deleted,
     * uncontrollable transitions to dump states are retained.
     * This setting is the default.
     */
    ABSTRACTION {
      @Override
      public boolean isRetainedEvent(final byte status)
      {
        return !EventEncoding.isControllableEvent(status);
      }

      @Override
      public boolean isRetainedEvent(final EventKind kind)
      {
        return kind == EventKind.UNCONTROLLABLE;
      }
    },
    /**
     * Halfway synthesis mode to compute a pseudo-supervisor.
     * Uncontrollable transitions to dump states are deleted,
     * controllable transitions to dump states are retained.
     */
    PSEUDO_SUPERVISOR {
      @Override
      public boolean isRetainedEvent(final byte status)
      {
        return EventEncoding.isControllableEvent(status);
      }

      @Override
      public boolean isRetainedEvent(final EventKind kind)
      {
        return kind == EventKind.CONTROLLABLE;
      }
    },
    /**
     * Halfway synthesis mode to compute a proper supervisor.
     * All transitions to dump states are deleted,
     */
    PROPER_SUPERVISOR {
      @Override
      public boolean isRetainedEvent(final byte status)
      {
        return false;
      }

      @Override
      public boolean isRetainedEvent(final EventKind kind)
      {
        return false;
      }
    };

    //#########################################################################
    //# Data Members
    public abstract boolean isRetainedEvent(byte status);

    public abstract boolean isRetainedEvent(EventKind kind);
  }


  //#########################################################################
  //# Data Members
  private OutputMode mOutputMode = OutputMode.ABSTRACTION;

  private BitSet mBadStates;
  private BitSet mCoreachableStates;
  private TLongHashSet mUnsupervisablePairs;

}
