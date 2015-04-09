//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   HalfWaySynthesisTRSimplifier
//###########################################################################
//# $Id: 44365f9ce27545868ec37b61ed041ded9c304a22 $
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * Transition relation simplifier that implements halfway synthesis.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class HalfWaySynthesisTRSimplifier extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public HalfWaySynthesisTRSimplifier()
  {
  }

  public HalfWaySynthesisTRSimplifier(final ListBufferTransitionRelation rel)
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
    final BitSet coreachableStates = new BitSet(numStates);
    do {
      coreachableStates.clear();
      findCoreachableStates(coreachableStates, mBadStates);
      for (int s = coreachableStates.nextClearBit(0); s < numStates;
           s = coreachableStates.nextClearBit(s + 1)) {
        if (rel.isReachable(s)) {
          mBadStates.set(s);
        }
      }
    } while (findMoreBadStates());
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
        final TRPartition partition =
          TRPartition.createEmptyPartition(numStates);
        setResultPartition(partition);
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
    if (needPartition || mOutputMode == OutputMode.PSEUDO_SUPERVISOR) {
      final List<int[]> classes = new ArrayList<>(numStates);
      for (int s = 0; s < numStates; s++) {
        if (mBadStates.get(s) || !rel.isReachable(s)) {
          classes.add(null);
        } else {
          final int[] clazz = new int[1];
          clazz[0] = s;
          classes.add(clazz);
        }
      }
      final TRPartition partition = new TRPartition(classes, numStates);
      setResultPartition(partition);
      applyResultPartitionAutomatically();
    }
    return needPartition;
  }

  @Override
  public void applyResultPartition()
    throws AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TRPartition partition = getResultPartition();
    if (partition.isEmpty()) {
      // 5a. Set all states unreachable, set all events unused.
      final int numStates = rel.getNumberOfStates();
      for (int state = 0; state < numStates; state++) {
        rel.setReachable(state, false);
      }
      final int numEvents = rel.getNumberOfProperEvents();
      for (int e = 0; e < numEvents; e++) {
        final byte status = rel.getProperEventStatus(e);
        rel.setProperEventStatus(e, status | EventStatus.STATUS_UNUSED);
      }
      final int numProps = rel.getNumberOfPropositions();
      for (int p = 0; p < numProps; p++) {
        rel.setPropositionUsed(p, false);
      }
    } else {
      // 5b. Check transitions from safe states to bad states.
      //  Delete controllable transitions to bad state.
      //  Redirect other bad state transitions to dump state.
      final int dumpState = mBadStates.nextSetBit(0);
      final int defaultID = getDefaultMarkingID();
      rel.setMarked(dumpState, defaultID, false);
//      boolean dumpReachable = rel.isInitial(dumpState);
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
//              dumpReachable = true;
            } else {
              iter.remove();
            }
          }
        }
      }
//      int s = dumpReachable ? mBadStates.nextSetBit(dumpState + 1) : dumpState;
//      for (; s >= 0; s = mBadStates.nextSetBit(s + 1)) {
//        rel.setReachable(s, false);
//      }
      final int config = getPreferredOutputConfiguration() |
        ListBufferTransitionRelation.CONFIG_SUCCESSORS;
      rel.reconfigure(config);
      if (rel.checkReachability()) {
        // Fix result partition --- is this safe???
        final List<int[]> classes = partition.getClasses();
        final ListIterator<int[]> liter = classes.listIterator();
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
      rel.removeProperSelfLoopEvents(defaultID);
      rel.removeRedundantPropositions();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void findCoreachableStates(final BitSet coreachable,
                                     final BitSet badStates)
    throws AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createPredecessorsReadOnlyIterator();
    final int defaultID = getDefaultMarkingID();
    final int numStates = rel.getNumberOfStates();
    final TIntStack unvisited = new TIntArrayStack();
    // Creates a hash set of all states which can reach a marked state.
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (rel.isMarked(sourceID, defaultID) && rel.isReachable(sourceID)
          && !badStates.get(sourceID) && !coreachable.get(sourceID)) {
        checkAbort();
        coreachable.set(sourceID);
        unvisited.push(sourceID);
        while (unvisited.size() > 0) {
          final int newSource = unvisited.pop();
          iter.resetState(newSource);
          while (iter.advance()) {
            final int predID = iter.getCurrentSourceState();
            if (rel.isReachable(predID) && !badStates.get(predID) &&
                !coreachable.get(predID)) {
              coreachable.set(predID);
              unvisited.push(predID);
            }
          }
        }
      }
    }
  }

  private boolean findMoreBadStates()
    throws AnalysisAbortException
  {
    boolean hasAdded = false;
    final BitSet oldBadStates = (BitSet) mBadStates.clone();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter =
      rel.createPredecessorsReadOnlyIteratorByStatus
        (EventStatus.STATUS_LOCAL, ~EventStatus.STATUS_CONTROLLABLE);
    final TIntStack unvisited = new TIntArrayStack();
    for (int state = oldBadStates.nextSetBit(0); state >= 0;
         state = oldBadStates.nextSetBit(state + 1)) {
      unvisited.push(state);
      while (unvisited.size() > 0) {
        final int current = unvisited.pop();
        iter.resetState(current);
        while (iter.advance()) {
          final int source = iter.getCurrentSourceState();
          if (rel.isReachable(source) && !mBadStates.get(source)) {
            hasAdded = true;
            mBadStates.set(source);
            unvisited.push(source);
          }
        }
      }
    }
    return hasAdded;
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
        return !EventStatus.isControllableEvent(status);
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
        return EventStatus.isControllableEvent(status);
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

}
