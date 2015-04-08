//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   CertainUnsupervisabilityTRSimplifier
//###########################################################################
//# $Id: 44365f9ce27545868ec37b61ed041ded9c304a22 $
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.procedure.TLongProcedure;
import gnu.trove.set.hash.TLongHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * Transition relation simplifier that implements certain unsupervisability.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class CertainUnsupervisabilityTRSimplifier
  extends AbstractMarkingTRSimplifier
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
  public void setOutputMode(final HalfWaySynthesisTRSimplifier.OutputMode mode)
  {
    mOutputMode = mode;
  }

  /**
   * Gets the operation mode for halfway synthesis.
   */
  public HalfWaySynthesisTRSimplifier.OutputMode getOutputMode()
  {
    return mOutputMode;
  }

  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions (including stored silent transitions of the
   * transitive closure) that will be stored.
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
    mUnsupervisablePairs = new TLongHashSet(numStates);
  }

  @Override
  public void reset()
  {
    super.reset();
    mBadStates = null;
    mUnsupervisablePairs = null;
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
        final TRPartition partition =
          TRPartition.createEmptyPartition(numStates);
        setResultPartition(partition);
        applyResultPartitionAutomatically();
        return true;
      }
      numBadStates++;
    }

    // 3. Check if there is any change.'
    // The transition relation changes if:
    // a) there is more than one dump state;
    // b) there are non-retained transitions to bad states;
    // c) there are outgoing transitions from bad states;
    // d) there are unsupervisable transitions to non-bad states.
    boolean needPartition = numBadStates > 1; // a)
    if (!needPartition) {
      final TransitionIterator iter =
        rel.createPredecessorsReadOnlyIterator(dumpState);
      while (iter.advance()) {
        checkAbort();
        final int event = iter.getCurrentEvent();
        final byte status = rel.getProperEventStatus(event);
        if (!mOutputMode.isRetainedEvent(status)) { // b)
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
        if (mBadStates.get(source)) { // c)
          needPartition = true;
          break;
        }
        final int event = iter.getCurrentEvent();
        final long key = (((long)source)<<32)|event;
        if (mUnsupervisablePairs.contains(key)) {
          final int target = iter.getCurrentTargetState();
          if (!mBadStates.get(target)) { // d)
            needPartition = true;
            break;
          }
        }
      }
    }

    // 4. Create result partition.
    //  Singleton classes for all safe states, no entries for bad states.
    if (needPartition || mOutputMode == HalfWaySynthesisTRSimplifier.OutputMode.PSEUDO_SUPERVISOR) {
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
      final TransitionIterator iterAllTrans =
        rel.createAllTransitionsModifyingIterator();
      while (iterAllTrans.advance()) {
        checkAbort();
        final int source = iterAllTrans.getCurrentSourceState();
        if (mBadStates.get(source)) {
          iterAllTrans.remove();
        } else {
          final int target = iterAllTrans.getCurrentTargetState();
          if (mBadStates.get(target)) {
            final int event = iterAllTrans.getCurrentEvent();
            final byte status = rel.getProperEventStatus(event);
            if (mOutputMode.isRetainedEvent(status)) {
              if (target != dumpState) {
                rel.addTransition(source, event, dumpState);
                iterAllTrans.remove();
              }
            } else {
              iterAllTrans.remove();
            }
          }
        }
      }
      final int config = getPreferredOutputConfiguration() |
        ListBufferTransitionRelation.CONFIG_SUCCESSORS;
      rel.reconfigure(config);
      /*
       * A more sophisticated way is forEach method below.
       * TLongIterator iterUnsupPairs = mUnsupervisablePairs.iterator();
       * while (iterUnsupPairs.hasNext()) {
       *   long key = iterUnsupPairs.next();
       * }
       */
      final TransitionIterator iterSucc = rel.createSuccessorsModifyingIterator();
      mUnsupervisablePairs.forEach(new TLongProcedure() {
        @Override
        public boolean execute(final long key)
        {
          final int source = (int) (key >> 32);
          final int event = (int) (key & 0xffffffffL);
          iterSucc.reset(source, event);
          while (iterSucc.advance()) {
            final int target = iterSucc.getCurrentTargetState();
            if (!mBadStates.get(target)) {
              iterSucc.remove();
            }
          }
          return true;
        }
      });
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
      final int defaultID = getDefaultMarkingID();
      rel.removeProperSelfLoopEvents(defaultID);
      rel.removeRedundantPropositions();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void calculateUnsupervisableStates()
    throws AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TauClosure closure =
      rel.createPredecessorsClosure(mTransitionLimit,
                                    EventStatus.STATUS_LOCAL,
                                    ~EventStatus.STATUS_CONTROLLABLE);
    final TransitionIterator tauIter = closure.createIterator();
    final TransitionIterator tauEventIter =
      closure.createPreEventClosureIteratorByStatus
        (~EventStatus.STATUS_LOCAL, ~EventStatus.STATUS_CONTROLLABLE);
    final TransitionIterator eventIter =
      rel.createPredecessorsReadOnlyIterator();
    final TIntStack stack = new TIntArrayStack();
    final int marking = getDefaultMarkingID();
    final int numStates = rel.getNumberOfStates();
    boolean foundNewBad;
    do {
      final BitSet coreachableStates = new BitSet(numStates);
      for (int x = 0; x < numStates; x++) {
        if (rel.isMarked(x, marking) && !mBadStates.get(x)) {
          coreachableStates.set(x);
          stack.push(x);
        }
      }
      while (stack.size() > 0) {
        checkAbort();
        final int x = stack.pop();
        eventIter.resetState(x);
        while (eventIter.advance()) {
          final int w = eventIter.getCurrentSourceState();
          if (!coreachableStates.get(w) && !mBadStates.get(w)) {
            final int sigma = eventIter.getCurrentEvent();
            final long key = (((long) w) << 32) | sigma;
            if (!mUnsupervisablePairs.contains(key)) {
              coreachableStates.set(w);
              stack.push(w);
            }
          }
        }
      }
      foundNewBad = false;
      for (int s = coreachableStates.previousClearBit(numStates-1); s >= 0;
           s = coreachableStates.previousClearBit(s-1)) {
        if (!mBadStates.get(s) && rel.isReachable(s)) {
          foundNewBad = true;
          tauIter.resume(s);
          while (tauIter.advance()) {
            checkAbort();
            final int x = tauIter.getCurrentSourceState();
            mBadStates.set(x);
            tauEventIter.resetState(x);
            while (tauEventIter.advance()) {
              final int w = tauEventIter.getCurrentSourceState();
              final int upsilon = tauEventIter.getCurrentEvent();
              final long key = (((long) w) << 32) | upsilon;
              mUnsupervisablePairs.add(key);
            }
          }
        }
      }
    } while (foundNewBad);
  }


  //#########################################################################
  //# Data Members
  private HalfWaySynthesisTRSimplifier.OutputMode mOutputMode =
    HalfWaySynthesisTRSimplifier.OutputMode.ABSTRACTION;
  private int mTransitionLimit = Integer.MAX_VALUE;

  private BitSet mBadStates;
  private TLongHashSet mUnsupervisablePairs;
}
