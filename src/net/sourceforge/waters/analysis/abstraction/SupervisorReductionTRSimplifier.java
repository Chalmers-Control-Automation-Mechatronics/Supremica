//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   HalfWaySynthesisTRSimplifier
//###########################################################################
//# $Id: 44365f9ce27545868ec37b61ed041ded9c304a22 $
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.IntListBuffer.ReadOnlyIterator;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * Transition relation simplifier that implements halfway synthesis.
 *
 * @author Fangqian Qiu, Robi Malik
 */

public class SupervisorReductionTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public SupervisorReductionTRSimplifier() throws AnalysisException
  {
  }

  public SupervisorReductionTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return false;
  }


  //#########################################################################
  //# Configuration
  public void setControlledEvent(final int event)
  {
    mControlledEvent = event;
  }

  public int getControlledEvent()
  {
    return mControlledEvent;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mNumProperEvents = rel.getNumberOfProperEvents() - 1;
  }

  @Override
  public boolean runSimplifier() throws AnalysisException
  {
    final TRPartition partition;
    if (findBadStates()) {
      setUpClasses();
      if (mControlledEvent < 0) {
        final TIntArrayList enabDisabEvents = new TIntArrayList();
        final TIntArrayList disabEvents = new TIntArrayList();
        setUpEventList(enabDisabEvents, disabEvents);
        if (enabDisabEvents.size() == 0) {
          partition = createOneStateTR(disabEvents);
        } else {
          partition = reduceSupervisor(enabDisabEvents);
        }
      } else {
        final TIntArrayList singletonList = new TIntArrayList(1);
        singletonList.add(mControlledEvent);
        partition = reduceSupervisor(singletonList);
      }
    } else {
      partition = createOneStateTR(new TIntArrayList(0));
    }
    setResultPartition(partition);
    if (partition != null) {
      applyResultPartitionAutomatically();
    }
    return partition != null;
  }

  @Override
  protected void applyResultPartition()
    throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    // Remove uncontrollable events that are selfloop-only
    final TransitionIterator iter =
      rel.createAllTransitionsReadOnlyIterator();
    for (int e = EventEncoding.NONTAU; e <= mNumProperEvents; e++) {
      checkAbort();
      final byte status = rel.getProperEventStatus(e);
      if (!EventEncoding.isControllableEvent(status)) {
        iter.resetEvent(e);
        boolean selfloopOnly = true;
        while (iter.advance()) {
          if (iter.getCurrentSourceState() != iter.getCurrentTargetState()) {
            selfloopOnly = false;
            break;
          }
        }
        if (selfloopOnly) {
          rel.removeEvent(e);
        }
      }
    }
    // Remove ordinary full-selfloop events
    final int marking = getDefaultMarkingID();
    rel.removeProperSelfLoopEvents(marking);
    rel.removeRedundantPropositions();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mStateToClass = null;
    mClasses = null;
    mShadowStateToClass = null;
    mShadowClasses = null;
  }


  //#########################################################################
  //# Methods for Supervisor Reduction
  private TRPartition reduceSupervisor(final TIntArrayList ctrlEvents)
    throws AnalysisAbortException
  {
    boolean merged = false;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int marking = getDefaultMarkingID();
    for (int i = 0; i < numStates - 1; i++) {
      if (!rel.isReachable(i) ||
          rel.isDeadlockState(i, marking) ||
          i > getMinimum(i)) {
        continue;
      }
      for (int j = i + 1; j < numStates; j++) {
        if (!rel.isReachable(j) ||
            rel.isDeadlockState(j, marking) ||
            j > getMinimum(j)) {
          continue;
        }
        checkAbort();
        TLongHashSet mergedPairs = new TLongHashSet();
        mShadowClasses = new IntListBuffer();
        mShadowStateToClass = new int[numStates];
        for (int s = 0; s < numStates; s++) {
          mShadowStateToClass[s] = IntListBuffer.NULL;
        }
        if (checkMergibility(i, j, i, j, mergedPairs, ctrlEvents)) {
          merge(mergedPairs);
          merged = true;
        }
        mergedPairs = null;
        mShadowClasses = null;
        mShadowStateToClass = null;
      }
    }
    if (merged) {
      return createResultPartition();
    } else {
      return null;
    }
  }

  private boolean checkMergibility(final int x, final int y,
                                   final int x0, final int y0,
                                   final TLongHashSet mergedPairs,
                                   final TIntArrayList ctrlEvents)
    throws AnalysisAbortException
  {
    checkAbort();
    if (mStateToClass[x] == mStateToClass[y]) {
      return true;
    }

    final int minX = getMinimum(x);
    final int minY = getMinimum(y);
    if (minX == minY) {
      return true;
    }
    final long p1 = constructPair(minX, minY);
    final long p2 = constructPair(x0, y0);
    if (compare(p1, p2) < 0) {
      return false;
    }

    copyIfShadowNull(x);
    copyIfShadowNull(y);

    final int lx = mShadowStateToClass[x];
    final int ly = mShadowStateToClass[y];
    final int[] listX = mShadowClasses.toArray(lx);
    final int[] listY = mShadowClasses.toArray(ly);

    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int marking = getDefaultMarkingID();
    final TIntHashSet xSet = new TIntHashSet();
    final TIntHashSet ySet = new TIntHashSet();
    final TIntArrayList xList = new TIntArrayList();
    final TIntArrayList yList = new TIntArrayList();

    for (int i = 0; i < ctrlEvents.size(); i++) {
      final int e = ctrlEvents.get(i);
      xSet.clear();
      xList.clear();
      boolean enabled = false;
      boolean disabled = false;
      for (final int xx : listX) {
        final int succ = getSuccessorState(xx, e);
        if (succ >= 0) {
          if (xSet.add(xx)) {
            xList.add(xx);
          }
          if (rel.isDeadlockState(succ, marking)) {
            disabled = true;
          } else {
            enabled = true;
          }
        }
      }
      if (disabled) {
        for (final int yy : listY) {
          final int succ = getSuccessorState(yy, e);
          if (succ >= 0 && !rel.isDeadlockState(succ, marking)) {
            return false;
          }
        }
      }
      if (enabled) {
        for (final int yy : listY) {
          final int succ = getSuccessorState(yy, e);
          if (succ >= 0 && rel.isDeadlockState(succ, marking)) {
            return false;
          }
        }
      }
    }

    final int l = mergeLists(lx, ly, mShadowClasses);
    updateStateToClass(l, mShadowStateToClass, mShadowClasses);

    final long pair = constructPair(x, y);
    mergedPairs.add(pair);

    for (int e = EventEncoding.NONTAU; e <= mNumProperEvents; e++) {
      xSet.clear();
      ySet.clear();
      xList.clear();
      yList.clear();
      for (final int xx : listX) {
        final int xSucc = getSuccessorState(xx, e);
        if (xSucc >= 0 && !rel.isDeadlockState(xSucc, marking)) {
          final int xmin = getMinimum(xSucc);
          if (xSet.add(xmin)) {
            xList.add(xmin);
          }
        }
      }
      if (xList.isEmpty()) {
        continue;
      }
      for (final int yy : listY) {
        final int ySucc = getSuccessorState(yy, e);
        if (ySucc >= 0 && !rel.isDeadlockState(ySucc, marking)) {
          final int ymin = getMinimum(ySucc);
          if (ySet.add(ymin)) {
            yList.add(ymin);
          }
        }
      }
      for (int i = 0; i < xList.size(); i++) {
        for (int j = 0; j < yList.size(); j++) {
          if (!checkMergibility(xList.get(i), yList.get(j), x0, y0,
                                mergedPairs, ctrlEvents)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private void setUpClasses() throws AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int marking = getDefaultMarkingID();
    mStateToClass = new int[numStates];
    mClasses = new IntListBuffer();
    for (int s = 0; s < numStates; s++) {
      checkAbort();
      if (rel.isDeadlockState(s, marking)) {
        mStateToClass[s] = IntListBuffer.NULL;
      } else {
        final int list = mClasses.createList();
        mClasses.add(list, s);
        mStateToClass[s] = list;
      }
    }
  }

  public void setUpEventList(final TIntArrayList enabDisabEvents,
                             final TIntArrayList disabEvents)
    throws AnalysisAbortException
  {
    enabDisabEvents.clear();
    disabEvents.clear();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int marking = getDefaultMarkingID();
    final TIntArrayList[] disabledStates =
      new TIntArrayList[mNumProperEvents + 1];
    final TIntArrayList[] enabledStates =
      new TIntArrayList[mNumProperEvents + 1];
    final TransitionIterator iter =
      rel.createAllTransitionsReadOnlyIteratorByStatus
        (EventEncoding.STATUS_CONTROLLABLE);
    while (iter.advance()) {
      checkAbort();
      final int currentEvent = iter.getCurrentEvent();
      final int succ = iter.getCurrentTargetState();
      final int pre = iter.getCurrentSourceState();
      if (rel.isDeadlockState(succ, marking)) {
        if (disabledStates[currentEvent] == null) {
          disabledStates[currentEvent] = new TIntArrayList();
        }
        disabledStates[currentEvent].add(pre);
      } else {
        if (enabledStates[currentEvent] == null) {
          enabledStates[currentEvent] = new TIntArrayList();
        }
        enabledStates[currentEvent].add(pre);
      }
    }
    for (int i = 0; i < mNumProperEvents; i++) {
      for (int j = i + 1; j <= mNumProperEvents; j++) {
        checkAbort();
        if (disabledStates[i] != null &&
            enabledStates[i] != null &&
            disabledStates[i].equals(disabledStates[j]) &&
            enabledStates[i].equals(enabledStates[j])) {
          disabledStates[j] = null;
          enabledStates[j] = null;
        }

      }
    }
    for (int e = EventEncoding.NONTAU; e <= mNumProperEvents; e++) {
      final byte status = rel.getProperEventStatus(e);
      if (EventEncoding.isControllableEvent(status)) {
        if (disabledStates[e] != null) {
          if (enabledStates[e] != null) {
            enabDisabEvents.add(e);
          } else {
            disabEvents.add(e);
          }
        }
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean findBadStates()
  {
    mBadStateIndex = -1;
    mNumBadStates = 0;
    final int marking = getDefaultMarkingID();
    if (marking >= 0) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      for (int s = 0; s < numStates; s++) {
        if (rel.isReachable(s) && rel.isDeadlockState(s, marking)) {
          if (mBadStateIndex < 0) {
            mBadStateIndex = s;
          }
          mNumBadStates++;
        }
      }
    }
    return mNumBadStates > 0;
  }

  private TRPartition createResultPartition() throws AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int marking = getDefaultMarkingID();
    final int[] badClass = mNumBadStates > 0 ? new int[mNumBadStates] : null;
    int nextBad = 0;
    final List<int[]> classes = new ArrayList<>();
    boolean trChanged = mNumBadStates > 1;
    for (int s = 0; s < numStates; s++) {
      checkAbort();
      if (rel.isReachable(s)) {
        if (rel.isDeadlockState(s, marking)) {
          badClass[nextBad++] = s;
        } else {
          final int listID = mStateToClass[s];
          if (mClasses.getFirst(listID) == s) {
            final int[] states = mClasses.toArray(listID);
            classes.add(states);
          } else {
            trChanged = true;
          }
        }
      }
    }
    if (trChanged) {
      if (badClass != null) {
        classes.add(badClass);
      }
      return new TRPartition(classes, numStates);
    } else {
      return null;
    }
  }

  private void merge(final TLongHashSet mergedPairs)
    throws AnalysisAbortException
  {
    final TLongIterator itr = mergedPairs.iterator();
    while (itr.hasNext()) {
      checkAbort();
      final long pair = itr.next();
      final int hi = getState(0, pair);
      final int lo = getState(1, pair);
      if (mStateToClass[hi] != mStateToClass[lo]) {
        final int list1 = mStateToClass[hi];
        final int list2 = mStateToClass[lo];
        final int list3 = mergeLists(list1, list2, mClasses);
        updateStateToClass(list3, mStateToClass, mClasses);
      }
    }
  }

  private int mergeLists(final int list1, final int list2,
                         final IntListBuffer classes)
  {
    final int x = classes.getFirst(list1);
    final int y = classes.getFirst(list2);
    if (x < y) {
      return classes.catenateDestructively(list1, list2);
    } else if (x > y) {
      return classes.catenateDestructively(list2, list1);
    }
    return list1;
  }

  private void copyIfShadowNull(final int state)
    throws AnalysisAbortException
  {
    if (mShadowStateToClass[state] == IntListBuffer.NULL) {
      final int newlist = mShadowClasses.copy(mStateToClass[state], mClasses);
      final ReadOnlyIterator iter =
        mShadowClasses.createReadOnlyIterator(newlist);
      iter.reset(newlist);
      while (iter.advance()) {
        final int current = iter.getCurrentData();
        mShadowStateToClass[current] = newlist;
      }
    }
  }

  private void updateStateToClass(final int list, final int[] stateToClass,
                                  final IntListBuffer classes)
    throws AnalysisAbortException
  {
    final ReadOnlyIterator iter = classes.createReadOnlyIterator(list);
    iter.reset(list);
    while (iter.advance()) {
      final int current = iter.getCurrentData();
      stateToClass[current] = list;
    }
  }

  private int compare(final long pair1, final long pair2)
  {
    if (pair1 < pair2) {
      return -1;
    } else if (pair1 > pair2) {
      return 1;
    } else {
      return 0;
    }
  }

  private int getMinimum(final int state)
  {
    if (mShadowStateToClass == null
        || mShadowStateToClass[state] == IntListBuffer.NULL) {
      return mClasses.getFirst(mStateToClass[state]);
    } else {
      return mShadowClasses.getFirst(mShadowStateToClass[state]);
    }
  }

  private int getState(final int position, final long pair)
  {
    if (position == 0) {
      return (int) (pair >>> 32);
    } else if (position == 1) {
      return (int) (pair & 0xffffffff);
    }
    return -1;
  }

  private long constructPair(int state1, int state2)
  {
    if (state1 > state2) {
      state1 = state1 + state2;
      state2 = state1 - state2;
      state1 = state1 - state2;
    }
    final long pair = state2 | ((long) state1 << 32);
    return pair;
  }

  private TRPartition createOneStateTR(final TIntArrayList disabEvents)
    throws OverflowException, AnalysisAbortException
  {
    final ListBufferTransitionRelation oldRel = getTransitionRelation();
    // create a one-state automaton if the disabled event set is empty
    // otherwise create a two-state automaton
    final int numStates = disabEvents.isEmpty() ? 1 : 2;
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation("OneStateSup",
                                       ComponentKind.SUPERVISOR,
                                       mNumProperEvents + 1,
                                       oldRel.getNumberOfPropositions(),
                                       numStates,
                                       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    rel.setInitial(0, true);
    // set all events in disabEvents USED
    for (int e = EventEncoding.TAU; e < rel.getNumberOfProperEvents(); e++) {
      if (!disabEvents.contains(e)) {
        final byte status = rel.getProperEventStatus(e);
        rel.setProperEventStatus(e, status | EventEncoding.STATUS_UNUSED);
      }
    }
    if (numStates == 2) {
      // add transitions from state 0 to dump state
      for (int i = 0; i < disabEvents.size(); i++) {
        rel.addTransition(0, disabEvents.get(i), 1);
      }
      // set initial state markings
      final int numProps = rel.getNumberOfPropositions();
      for (int p = 0; p < numProps; p++) {
        rel.setMarked(0, p, true);
      }
    } else if (numStates == 1) {
      // set initial state markings
      final long markings = rel.createMarkings();
      rel.setUsedPropositions(markings);
    }
    final int marking = getDefaultMarkingID();
    final int oldNumStates = oldRel.getNumberOfStates();
    final int[] stateToClass = new int[oldNumStates];
    for (int s = 0; s < oldNumStates; s++) {
      if (!oldRel.isReachable(s)) {
        stateToClass[s] = -1;
      } else if (!oldRel.isDeadlockState(s, marking)) {
        stateToClass[s] = 0;
      } else {
        stateToClass[s] = 1;
      }
    }
    return new TRPartition(stateToClass, numStates);
  }

  private int getSuccessorState(final int source, final int event)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    iter.reset(source, event);
    if (iter.advance()) {
      return iter.getCurrentTargetState();
    } else {
      return -1;
    }
  }


  //#########################################################################
  //# For Debugging
  public int[] showClassList(final IntListBuffer classes, final int list)
  {
    final int[] array = classes.toArray(list);
    return array;
  }


  //#########################################################################
  //# Data Members
  private int mNumProperEvents;
  private int mBadStateIndex;
  private int mNumBadStates;
  private int mControlledEvent;
  private int[] mStateToClass;
  private IntListBuffer mClasses;
  private int[] mShadowStateToClass;
  private IntListBuffer mShadowClasses;
}
