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
import net.sourceforge.waters.model.analysis.OverflowException;


/**
 * Transition relation simplifier that implements halfway synthesis.
 *
 * @author Robi Malik, Sahar Mohajerani, fq11
 */

public class SupervisorReductionTRSimplifier extends
  AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public SupervisorReductionTRSimplifier() throws AnalysisException
  {
    mBadStateIndex = -1;
  }

  public SupervisorReductionTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
    mBadStateIndex = -1;
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
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mNumProperEvents = rel.getNumberOfProperEvents() - 1;
    for (int e = 1; e <= mNumProperEvents; e++) {
      if ((rel.getProperEventStatus(e) & EventEncoding.STATUS_CONTROLLABLE) == EventEncoding.STATUS_CONTROLLABLE) {
        mFirstCEvent = e;
        break;
      }
    }
    rel.setName(mOuptutName);
  }

  @Override
  public boolean runSimplifier() throws AnalysisException
  {
    if (mBadStateIndex == -1) {
      setBadStateIndex();
    }
    if (mBadStateIndex == -1) {
      return createOneStateTR(new TIntArrayList(0));
    }
    setUpClasses();
    if (mCurrEvent == -1) {
      final TIntArrayList enabDisabEvents = new TIntArrayList();
      final TIntArrayList disabEvents = new TIntArrayList();
      setUpEventList(enabDisabEvents, disabEvents);
      if (enabDisabEvents.size() == 0) {
        return createOneStateTR(disabEvents);
      } else {
        mainProcedure(enabDisabEvents);
        return mergeTR(false, true);
      }
    } else {
      final TIntArrayList singletonList = new TIntArrayList();
      singletonList.add(mCurrEvent);
      mainProcedure(singletonList);
      return mergeTR(true, false);
    }
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
  // Configuration
  @Override
  public void setTransitionRelation(final ListBufferTransitionRelation rel)
  {
    super.setTransitionRelation(rel);
    mBadStateIndex = -1;
  }

  public void setOutputName(final String name)
  {
    mOuptutName = name;
  }

  public String getOutputName()
  {
    return mOuptutName;
  }

  public void setEvent(final int e)
  {
    mCurrEvent = e;
  }

  public void setBadStateIndex()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    for (int s = 0; s < rel.getNumberOfStates(); s++) {
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      iter.resetState(s);
      if (!iter.advance() & rel.isReachable(s) & rel.getAllMarkings(s) == 0) {
        mBadStateIndex = s;
        return;
      }
    }
    mBadStateIndex = -1;
  }

  public void setBadStateIndex(final int s)
  {
    mBadStateIndex = s;
  }

  public int getBadStateIndex()
  {
    return mBadStateIndex;
  }

  //##########################################################################
  // Methods for Supervisor Reduction

  private boolean mainProcedure(final TIntArrayList ctrlEvents)
  {
    boolean merged = false;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    for (int i = 0; i < numStates - 1; i++) {
      if (!rel.isReachable(i) || i == mBadStateIndex || i > getMinimum(i)) {
        continue;
      }
      for (int j = i + 1; j < numStates; j++) {
        if (!rel.isReachable(j) || j == mBadStateIndex || j > getMinimum(j)) {
          continue;
        }
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
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    mStateToClass = new int[numStates];
    mClasses = new IntListBuffer();
    for (int s = 0; s < numStates; s++) {
      if (s != mBadStateIndex) {
        final int list = mClasses.createList();
        mClasses.add(list, s);
        mStateToClass[s] = list;
      } else {
        mStateToClass[s] = IntListBuffer.NULL;
      }
    }
  }

  public void setUpEventList(final TIntArrayList enabDisabEvents,
                             final TIntArrayList disabEvents)
  {
    enabDisabEvents.clear();
    disabEvents.clear();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TIntArrayList[] disabledStates =
      new TIntArrayList[mNumProperEvents + 1];
    final TIntArrayList[] enabledStates =
      new TIntArrayList[mNumProperEvents + 1];
    final TransitionIterator iterator =
      rel.createAllTransitionsReadOnlyIterator();
    iterator.resetEvents(mFirstCEvent, mNumProperEvents);
    while (iterator.advance()) {
      final int currentEvent = iterator.getCurrentEvent();
      final int succ = iterator.getCurrentTargetState();
      final int pre = iterator.getCurrentSourceState();
      if (succ == mBadStateIndex) {
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
        if (disabledStates[i] != null && enabledStates[i] != null
            && disabledStates[i].equals(disabledStates[j])
            && enabledStates[i].equals(enabledStates[j])) {
          disabledStates[j] = null;
          enabledStates[j] = null;
        }

      }
    }
    for (int e = mFirstCEvent; e <= mNumProperEvents; e++) {
      if (disabledStates[e] != null) {
        if (enabledStates[e] != null) {
          enabDisabEvents.add(e);
        } else {
          disabEvents.add(e);
        }
      }
    }
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

  /**
   * Merges mTransitionRelation
   *
   * @param removeBadTrans
   *          Remove transitions to the bad state
   * @param addBadState
   *          Add a bad state
   * @return true if transition relation is changed
   */
  private boolean mergeTR(final boolean removeBadTrans,
                          final boolean addBadState)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    boolean trChanged = false;
    if (removeBadTrans) {
      removeBadStateTransitions();
    }
    final List<int[]> mergingStates = new ArrayList<int[]>();
    for (int i = 0; i < mBadStateIndex; i++) {
      final int listID = mStateToClass[i];
      if (mClasses.getFirst(listID) == i) {
        final int[] states = mClasses.toArray(listID);
        mergingStates.add(states);
      } else {
        trChanged = true;
      }
    }
    if (addBadState) {
      final int[] badState = new int[1];
      badState[0] = mBadStateIndex;
      mBadStateIndex = mergingStates.size();
      mergingStates.add(badState);
    }
    rel.merge(mergingStates);
    final boolean selfLoofRemoved = rel.removeProperSelfLoopEvents();
    return trChanged | selfLoofRemoved;
  }

  /**
   * Removes transitions to the bad state
   *
   * @return true if at least one transition is removed
   */
  private boolean removeBadStateTransitions()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    boolean trChanged = false;
    final TransitionIterator iter =
      rel.createAllTransitionsModifyingIterator();
    while (iter.advance()) {
      final int to = iter.getCurrentTargetState();
      if (to == mBadStateIndex) {
        iter.remove();
        trChanged = true;
      }
    }
    rel.setReachable(mBadStateIndex, false);
    return trChanged;
  }

  /**
   * Makes transition relation have only one state
   *
   * @return true if transition relation is changed
   * @throws OverflowException
   */
  private boolean createOneStateTR(final TIntArrayList disabEvents)
    throws OverflowException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    for (int e = 0; e < rel.getNumberOfProperEvents(); e++) {
      if (!disabEvents.contains(e)) {
        final byte status = rel.getProperEventStatus(e);
        rel.setProperEventStatus(e, status | EventEncoding.STATUS_UNUSED);
      }
    }
    final long markings = rel.createMarkings();
    rel.setUsedPropositions(markings);
    for (int s = 1; s < rel.getNumberOfStates(); s++) {
      rel.setReachable(s, false);
    }
    rel.removeOutgoingTransitions(0);
    rel.setReachable(0, true);
    rel.setInitial(0, true);
    return true;
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
  private int mCurrEvent;
  private String mOuptutName;

  private int[] mStateToClass;
  private IntListBuffer mClasses;
  private int[] mShadowStateToClass;
  private IntListBuffer mShadowClasses;
}
