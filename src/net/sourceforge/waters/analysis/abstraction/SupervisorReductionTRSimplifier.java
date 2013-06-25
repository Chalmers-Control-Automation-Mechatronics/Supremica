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
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * Transition relation simplifier that implements halfway synthesis.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class SupervisorReductionTRSimplifier extends
  AbstractSynthesisTRSimplifier
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
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  public void reset()
  {
    super.reset();
  }

  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    // Get TR
    mTransitionRelation = getTransitionRelation();
    // Get number of states
    mBadStateIndex = mTransitionRelation.getNumberOfStates() - 1;
    // Read event status
    mNumProperEvents = mTransitionRelation.getNumberOfProperEvents() - 1;
    for (int e = 1; e <= mNumProperEvents; e++) {
      if ((mTransitionRelation.getProperEventStatus(e) & EventEncoding.STATUS_CONTROLLABLE) == EventEncoding.STATUS_CONTROLLABLE) {
        mFirstCEvent = e;
        break;
      }
    }
  }

  @Override
  protected boolean runSimplifier() throws AnalysisException
  {
    return false;
  }

  @Override
  public boolean run() throws AnalysisException
  {
    setUp();
    setUpClasses();
    if (event == -1) {
      final TIntArrayList eventList = setUpEventList();
      if (eventList.size() != 0) {
        mainProcedure(eventList);
        mergeTR(false, true);
      } else {
        return false;
      }
    } else {
      final TIntArrayList singletonList = new TIntArrayList();
      singletonList.add(event);
      mainProcedure(singletonList);
      mergeTR(true, false);
    }
    return true;
  }

  //#########################################################################
  // Configuration
  public void setName(final String name)
  {
    mTransitionRelation.setName(name);
  }

  public void setEvent(final int e)
  {
    event = e;
  }

  public TIntArrayList getDisabledEvents()
  {
    return mDisabledEvents;
  }

  //##########################################################################
  // Methods for Supervisor Reduction

  private boolean mainProcedure(final TIntArrayList ctrlEvents)
  {
    boolean merged = false;
    for (int i = 0; i < mBadStateIndex - 1; i++) {
      if (i > getMinimum(i)) {
        continue;
      }
      for (int j = i + 1; j < mBadStateIndex; j++) {
        if (j > getMinimum(j)) {
          continue;
        }
        TLongHashSet mergedPairs = new TLongHashSet();
        mShadowClasses = new IntListBuffer();
        mShadowStateToClass = new int[mBadStateIndex];
        for (int s = 0; s < mBadStateIndex; s++) {
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
    return merged;
  }

  private boolean checkMergibility(final int x, final int y, final int x0,
                                  final int y0,
                                  final TLongHashSet mergedPairs,
                                  final TIntArrayList ctrlEvents)
  {
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
        if (succ != -1) {
          if (xSet.add(xx)) {
            xList.add(xx);
          }
          if (succ != mBadStateIndex) {
            enabled = true;
          } else {
            disabled = true;
          }
        }
      }
      if (disabled) {
        for (final int yy : listY) {
          final int succ = getSuccessorState(yy, e);
          if (succ != -1) {
            if (succ != mBadStateIndex) {
              return false;
            }
          }
        }
      }
      if (enabled) {
        for (final int yy : listY) {
          final int succ = getSuccessorState(yy, e);
          if (succ != -1) {
            if (succ == mBadStateIndex) {
              return false;
            }
          }
        }
      }
    }

    final int l = mergeLists(lx, ly, mShadowClasses);
    updateStateToClass(l, mShadowStateToClass, mShadowClasses);

    final long pair = constructPair(x, y);
    mergedPairs.add(pair);

    for (int e = 1; e <= mNumProperEvents; e++) {
      xSet.clear();
      ySet.clear();
      xList.clear();
      yList.clear();
      for (final int xx : listX) {
        final int xSucc = getSuccessorState(xx, e);
        if (xSucc != -1 && xSucc != mBadStateIndex) {
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
        if (ySucc != -1 && ySucc != mBadStateIndex) {
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

  private void setUpClasses()
  {
    mStateToClass = new int[mBadStateIndex];
    mClasses = new IntListBuffer();
    for (int s = 0; s < mBadStateIndex; s++) {
      final int list = mClasses.createList();
      mClasses.add(list, s);
      mStateToClass[s] = list;
    }
  }

  public TIntArrayList setUpEventList()
  {
    mDisabledEvents = new TIntArrayList();
    final TIntArrayList eventList = new TIntArrayList();
    final TIntArrayList[] eventToDisabledStates =
      new TIntArrayList[mNumProperEvents + 1];
    final TIntArrayList[] eventToEnabledStates =
      new TIntArrayList[mNumProperEvents + 1];
    final TransitionIterator iterator =
      mTransitionRelation.createAllTransitionsReadOnlyIterator();
    iterator.resetEvents(mFirstCEvent, mNumProperEvents);
    while (iterator.advance()) {
      final int currentEvent = iterator.getCurrentEvent();
      final int succ = iterator.getCurrentTargetState();
      final int pre = iterator.getCurrentSourceState();
      if (succ == mBadStateIndex) {
        if (eventToDisabledStates[currentEvent] == null) {
          eventToDisabledStates[currentEvent] = new TIntArrayList();
        }
        eventToDisabledStates[currentEvent].add(pre);
      } else {
        if (eventToEnabledStates[currentEvent] == null) {
          eventToEnabledStates[currentEvent] = new TIntArrayList();
        }
        eventToEnabledStates[currentEvent].add(pre);
      }

    }
    for (int i = 0; i < mNumProperEvents; i++) {
      for (int j = i + 1; j <= mNumProperEvents; j++) {
        if (eventToDisabledStates[i] != null
            && eventToEnabledStates[i] != null
            && eventToDisabledStates[i].equals(eventToDisabledStates[j])
            && eventToEnabledStates[i].equals(eventToEnabledStates[j])) {
          eventToDisabledStates[j] = null;
          eventToEnabledStates[j] = null;
        }

      }
    }
    for (int e = mFirstCEvent; e <= mNumProperEvents; e++) {
      if (eventToDisabledStates[e] != null) {
        if (eventToEnabledStates[e] != null) {
          eventList.add(e);
        } else {
          mDisabledEvents.add(e);
        }
      }
    }
    return eventList;
  }

  private int getSuccessorState(final int source, final int event)
  {
    final TransitionIterator iter =
      mTransitionRelation.createSuccessorsReadOnlyIterator();
    iter.reset(source, event);
    if (iter.advance()) {
      return iter.getCurrentTargetState();
    } else {
      return -1;
    }
  }

  private void merge(final TLongHashSet mergedPairs)
  {
    final TLongIterator itr = mergedPairs.iterator();
    while (itr.hasNext()) {
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

  private void mergeTR(final boolean removeBadTrans, final boolean addBadState)
  {
    if (removeBadTrans) {
      removeBadStateTransitions();
    }
    final List<int[]> mergingStates = new ArrayList<int[]>();
    for (int i = 0; i < mBadStateIndex; i++) {
      final int listID = mStateToClass[i];
      if (mClasses.getFirst(listID) == i) {
        final int[] states = mClasses.toArray(listID);
        mergingStates.add(states);
      }
    }
    if (addBadState) {
      final int[] states = new int[1];
      states[0] = mBadStateIndex;
      mBadStateIndex = mergingStates.size();
      mergingStates.add(states);
    }
    mTransitionRelation.merge(mergingStates);
    mTransitionRelation.removeProperSelfLoopEvents();
  }

  public void removeBadStateTransitions()
  {
    final TransitionIterator iter =
      mTransitionRelation.createAllTransitionsModifyingIterator();
    while (iter.advance()) {
      final int to = iter.getCurrentTargetState();
      if (to == mBadStateIndex) {
        iter.remove();
      }
    }
    mTransitionRelation.setReachable(mBadStateIndex, false);
  }

  //#########################################################################
  // For Debugging
  public int[] showClassList(final IntListBuffer classes, final int list)
  {
    final int[] array = classes.toArray(list);
    return array;
  }

  //#########################################################################
  //# Data Members
  private int mFirstCEvent;
  private int mNumProperEvents;
  private int mBadStateIndex;
  private ListBufferTransitionRelation mTransitionRelation;

  private int event;

  private TIntArrayList mDisabledEvents;

  private int[] mStateToClass;
  private IntListBuffer mClasses;
  private int[] mShadowStateToClass;
  private IntListBuffer mShadowClasses;
}
