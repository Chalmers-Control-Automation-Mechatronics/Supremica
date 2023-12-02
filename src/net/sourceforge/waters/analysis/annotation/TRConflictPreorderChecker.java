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

package net.sourceforge.waters.analysis.annotation;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.gnonblocking.FindBlockingStates;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AbstractAbortable;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.DefaultAnalysisResult;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;


/**
 * <P>An implementation of the conflict preorder algorithm.</P>
 *
 * <P><I>Reference:</I><BR>
 * Simon Ware, Robi Malik. An Algorithm to Test the Conflict Preorder.
 * Science of Computer Programming, <STRONG>89</STRONG>&nbsp;(A), 23&ndash;40,
 * 2014.</P>
 *
 * @author Simon Ware, Robi Malik
 */

public class TRConflictPreorderChecker
  extends AbstractAbortable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a conflict preorder checker to compare two transition
   * relations. The conflict preorder checker compares two transition
   * relations defined using the same event encoding.
   * @param first   A transition relation that is to be checked whether it
   *                is less conflicting than the second argument.
   * @param second  A transition relation that is to be checked whether it
   *                is more conflicting than the first argument.
   * @param marking The event code of the marking proposition in both
   *                transition relations.
   */
  public TRConflictPreorderChecker(final ListBufferTransitionRelation first,
                                   final ListBufferTransitionRelation second,
                                   final int marking)
  {
    mFirstRelation = first;
    mSecondRelation = second;
    mSetCache = new HashMap<TIntHashSet, TIntHashSet>();
    mTupleCache = new TObjectIntHashMap<LCPair>();
    mStates = new ArrayList<LCPair>();
    mFirstLC = new TIntHashSet();
    mExpanded = 0;
    mMarking = marking;
    final FindBlockingStates fbs = new FindBlockingStates(second, mMarking);
    mSecondBlocking = fbs.getBlockingStates();
    mSuccessors = new ArrayList<TIntArrayList>();
    mPredeccessors = new ArrayList<TIntHashSet[]>();
  }


  //#########################################################################
  //# Configuration
  public static int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }

  public ConflictPreorderResult getAnalysisResult()
  {
    return mAnalysisResult;
  }


  //#########################################################################
  //# Invocation
  /**
   * Runs this checker to determine whether the given transition relations
   * are related through the conflict preorder.
   * @return <CODE>true</CODE> if the given first transition relation is
   *         less conflicting than the given second transition relation.
   * @see #TRConflictPreorderChecker(ListBufferTransitionRelation, ListBufferTransitionRelation, int)
   *      TRConflictPreorderChecker()
   */
  public boolean isLessConflicting()
    throws AnalysisException
  {
    try {
      setUp();
      final TIntHashSet first = new TIntHashSet();
      final TIntHashSet second = new TIntHashSet();
      for (int s = 0; s < mFirstRelation.getNumberOfStates(); s++) {
        if (mFirstRelation.isInitial(s)) {
          first.add(s);
        }
      }
      for (int s = 0; s < mSecondRelation.getNumberOfStates(); s++) {
        if (mSecondRelation.isInitial(s)) {
          second.add(s);
        }
      }
      checkAbort();
      final TIntHashSet init1 = calculateTauReachable(first, mFirstRelation);
      final TIntHashSet init2 = calculateTauReachable(second, mSecondRelation);
      final LCPair init = createPair(init1, init2);
      final boolean result = isLessConflicting(init);
      return setBooleanResult(result);
    } catch (final OutOfMemoryError error) {
      tearDown();
      System.gc();
      final OverflowException exception = new OverflowException(error);
      setExceptionResult(exception);
      throw exception;
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Coordination
  /**
   * Initialises the model analyser for a new run.
   * This method should be called by all subclasses at the beginning of
   * each {@link ModelAnalyzer#run() run()}. If overridden, the overriding
   * method should call the superclass methods first.
   */
  private void setUp()
  {
    mAnalysisResult = new ConflictPreorderResult();
    mAnalysisResult.addPair(mFirstRelation, mSecondRelation);
    mStartTime = System.currentTimeMillis();
  }

  /**
   * Resets the model analyser at the end of a run.
   * This method should be called by all subclasses upon completion of
   * each {@link ModelAnalyzer#run() run()}, even if an exception is
   * thrown. If overridden, the overriding method should call the superclass
   * methods last.
   */
  private void tearDown()
  {
    mSetCache = null;
    mTupleCache = null;
    mStates = null;
    mFirstLC = null;
    mSecondBlocking = null;
    mSuccessors = null;
    mPredeccessors = null;
    addStatistics();
  }

  /**
   * Stores the given Boolean value on the analysis result and marks the run
   * as completed.
   * @return The given Boolean value.
   */
  private boolean setBooleanResult(final boolean value)
  {
    mAnalysisResult.setSatisfied(value);
    addStatistics();
    return value;
  }

  /**
   * Stores the given exception on the analysis result and marks the run
   * as completed.
   * @return The given exception.
   */
  private AnalysisException setExceptionResult
    (final AnalysisException exception)
  {
    if (mAnalysisResult != null) {
      mAnalysisResult.setException(exception);
      addStatistics();
    }
    return exception;
  }


  /**
   * Stores any available statistics on this analyser's last run in the
   * analysis result.
   */
  private void addStatistics()
  {
    if (mAnalysisResult != null) {
      final long current = System.currentTimeMillis();
      mAnalysisResult.setRuntime(current - mStartTime);
      final long usage = DefaultAnalysisResult.getCurrentMemoryUsage();
      mAnalysisResult.updatePeakMemoryUsage(usage);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private TIntHashSet calculateTauReachable(final int state,
                                            final ListBufferTransitionRelation trans)
  {
    final TIntHashSet set = new TIntHashSet();
    set.add(state);
    return calculateTauReachable(set, trans);
  }

  private TIntHashSet calculateTauReachable(final TIntHashSet set,
                                            final ListBufferTransitionRelation trans)
  {
    final TIntHashSet taureach = new TIntHashSet(set.toArray());
    final TIntArrayList togo = new TIntArrayList(set.toArray());
    while (!togo.isEmpty()) {
      final int state = togo.removeAt(togo.size() - 1);
      final TransitionIterator ti =
        trans.createSuccessorsReadOnlyIterator(state, EventEncoding.TAU);
      while (ti.advance()) {
        if (taureach.add(ti.getCurrentTargetState())) {
          togo.add(ti.getCurrentTargetState());
        }
      }
    }
    return taureach;
  }

  private TIntHashSet calculateSuccessor(TIntHashSet set, final int event,
                                         final ListBufferTransitionRelation trans)
  {
    set = calculateTauReachable(set, trans);// this shouldn't be needed
    final TIntHashSet succ = new TIntHashSet();
    final TIntIterator it = set.iterator();
    while (it.hasNext()) {
      final int s = it.next();
      if (s == -1) {return null;}
      if (event != trans.getNumberOfProperEvents()) {
        final TransitionIterator ti = trans.createSuccessorsReadOnlyIterator(s, event);
        while (ti.advance()) {
          succ.add(ti.getCurrentTargetState());
        }
      } else {
        if (trans.isMarked(s, mMarking)) {
          succ.add(-1);
          return succ;
        }
      }
    }
    return calculateTauReachable(succ, trans);
  }

  private int getState(final LCPair tup)
  {
    if (!mTupleCache.containsKey(tup)) {
      final int state = mStates.size();
      mTupleCache.put(tup, state);
      mStates.add(tup);
      final int[] sucs = new int[mFirstRelation.getNumberOfProperEvents() + 1];
      for (int i = 0; i < sucs.length; i++) {
        sucs[i] = -1;
      }
      mSuccessors.add(new TIntArrayList(sucs));
      mPredeccessors.add(new TIntHashSet[mFirstRelation.getNumberOfProperEvents() + 1]);
      if (tup.mFirstSet.contains(-1)) {
        mFirstLC.add(state);
      }
      final TIntIterator it = tup.mSecondSet.iterator();
      while (it.hasNext()) {
        final int num = it.next();
        if (mSecondBlocking.contains(num)) {
          mFirstLC.add(state); break;
        }
      }
    }
    final int state = mTupleCache.get(tup);
    return state;
  }

  private void expandStates()
    throws AnalysisAbortException
  {
    for (; mExpanded < mStates.size(); mExpanded++) {
      checkAbort();
      final int state = mExpanded;
      final LCPair tup = mStates.get(state);
      if (tup.mFirstSet.contains(-1) || tup.mSecondSet.contains(-1)) {
        continue;
      }
      final TIntIterator it = tup.mFirstSet.iterator();
      while (it.hasNext()) {
        final TIntHashSet f = new TIntHashSet();
        f.add(it.next());
        getState(createPair(f, tup.mSecondSet));
      }
      for (int e = 0; e < mFirstRelation.getNumberOfProperEvents() + 1; e++) {
        if (e == EventEncoding.TAU) {continue;}
        final TIntHashSet first = calculateSuccessor(tup.mFirstSet, e, mFirstRelation);
        final TIntHashSet second = calculateSuccessor(tup.mSecondSet, e, mSecondRelation);
        final int target = getState(createPair(first, second));
        mSuccessors.get(state).set(e, target);
        TIntHashSet preds = mPredeccessors.get(target)[e];
        if (preds == null) {
          preds = new TIntHashSet();
          final TIntHashSet[] predsarr = mPredeccessors.get(target);
          predsarr[e] = preds;
        }
        preds.add(state);
      }
      final TIntHashSet first = calculateSuccessor(tup.mFirstSet, mMarking, mFirstRelation);
      final TIntHashSet second = calculateSuccessor(tup.mSecondSet, mMarking, mSecondRelation);
      final int target = getState(createPair(first, second));
      mSuccessors.get(state).set(mMarking, target);
    }
  }

  private void calculateLCStates()
    throws AnalysisAbortException
  {
    boolean modified = true;
    int level = 0;
    while (modified) {
      mAnalysisResult.addLevel(level++, mFirstLC.size());
      modified = false;
      final TIntArrayList makelc = new TIntArrayList();
      final Set<MCTriple> mcTriples = new THashSet<MCTriple>();
      final List<MCTriple> tobeexpanded = new ArrayList<MCTriple>();
      for (int s = 0; s < mStates.size(); s++) {
        if (!mFirstLC.contains(s)) {
          checkAbort();
          makelc.add(s);
          final LCPair state = mStates.get(s);
          final TIntHashSet moreset = state.mSecondSet;
          if (moreset.contains(-1)) {
            final MCTriple triple = new MCTriple(mStates.get(s), -1);
            mcTriples.add(triple);
            tobeexpanded.add(triple);
          }
        }
      }
      while (!tobeexpanded.isEmpty()) {
        checkAbort();
        final MCTriple triple = tobeexpanded.remove(tobeexpanded.size() - 1);
        for (int e = 0; e < mFirstRelation.getNumberOfProperEvents() + 1; e++) {
          if (e == EventEncoding.TAU) {continue;}
          final TIntHashSet preds = mPredeccessors.get(mTupleCache.get(triple.mTuple))[e];
          if (preds == null) {continue;}
          final TIntIterator it = preds.iterator();
          while (it.hasNext()) {
            final int pred = it.next();
            if (mFirstLC.contains(pred)) {continue;}
            final LCPair predtuple = mStates.get(pred);
            final TIntHashSet moreset = predtuple.mSecondSet;
            final TIntIterator itstates = moreset.iterator();
            while (itstates.hasNext()) {
              final int state = itstates.next();
              final TIntHashSet newset = new TIntHashSet(); newset.add(state);
              final TIntHashSet statesuccessors = calculateSuccessor(newset, e, mSecondRelation);
              if (statesuccessors.contains(triple.mState)) {
                final MCTriple add = new MCTriple(mStates.get(pred), state);
                if (mcTriples.add(add)) {tobeexpanded.add(add);}
              }
            }
          }
        }
      }
      mAnalysisResult.addMCTriples(mcTriples.size());
      for (int i = 0; i < makelc.size(); i++) {
        final int state = makelc.get(i);
        final LCPair tup = mStates.get(state);
        final TIntIterator it2 = tup.mSecondSet.iterator();
        while (it2.hasNext()) {
          final int propstate = it2.next();
          final MCTriple triple = new MCTriple(tup, propstate);
          if (!mcTriples.contains(triple)) {
            mFirstLC.add(state);
            modified = true;
          }
        }
      }
    }
  }

  @SuppressWarnings("unused")
  private boolean isLessConflicting(final int s1, final int s2)
    throws AnalysisAbortException
  {
    return isLessConflicting(createPair(calculateTauReachable(s1, mFirstRelation),
                                        calculateTauReachable(s2, mSecondRelation)));
  }

  private boolean isLessConflicting(final LCPair tuple)
    throws AnalysisAbortException
  {
    final int initial = getState(tuple);
    // adds the certain conflict states to the calculation
    getState(createPair(new TIntHashSet(), tuple.mSecondSet));
    expandStates();
    mAnalysisResult.addLCPairs(mStates.size());
    calculateLCStates();
    final TIntHashSet explored = new TIntHashSet();
    final TIntArrayList toexplore = new TIntArrayList();
    explored.add(initial);
    toexplore.add(initial);
    while (!toexplore.isEmpty()) {
      checkAbort();
      final int s = toexplore.removeAt(toexplore.size() -1);
      LCPair state = mStates.get(s);
      if (state.mFirstSet.isEmpty()) {continue;}
      if (state.mFirstSet.size() > 1) {
        final TIntIterator it = state.mFirstSet.iterator();
        while (it.hasNext()) {
          final TIntHashSet set = new TIntHashSet();
          set.add(it.next());
          if (explored.add(getState(createPair(set, state.mSecondSet)))) {
            toexplore.add(getState(createPair(set, state.mSecondSet)));
          }
        }
        continue;
      }
      if (mFirstLC.contains(getState(createPair(new TIntHashSet(), state.mSecondSet)))) {continue;}
      if (!mFirstLC.contains(s)) {
        final TIntArrayList states = new TIntArrayList();
        final TIntHashSet visited2 = new TIntHashSet();
        visited2.add(s);
        states.add(s);
        while (!states.isEmpty()) {
          final int snum = states.removeAt(0);
          state = mStates.get(snum);
          for (int e = 0; e < mSuccessors.get(snum).size(); e++) {
            if (e == EventEncoding.TAU) {continue;}
            if (mSuccessors.get(snum).get(e) != -1) {
              final int tnum = mSuccessors.get(snum).get(e);
              if (visited2.add(tnum) && !mFirstLC.contains(tnum)) {
                states.add(tnum);
              }
            }
          }
        }
        return false;
      }
      final TIntArrayList succs = mSuccessors.get(s);
      for (int e = 0; e < succs.size(); e++) {
        if (e == EventEncoding.TAU) {continue;}
        final int suc = succs.get(e);
        if (suc == -1) {continue;}
        if (explored.add(suc)) {
          toexplore.add(suc);
        }
      }
    }
    return true;
  }

  private LCPair createPair(final TIntHashSet first, final TIntHashSet second)
  {
    return new LCPair(getUniqueSet(first), getUniqueSet(second));
  }

  private TIntHashSet getUniqueSet(final TIntHashSet set)
  {
    TIntHashSet tset = mSetCache.get(set);
    if (tset == null) {
      tset = set;
      mSetCache.put(set, set);
    }
    return tset;
  }


  //#########################################################################
  //# Inner Class LCPair
  private static class LCPair
  {
    //#######################################################################
    //# Constructor
    private LCPair(final TIntHashSet first, final TIntHashSet second)
    {
      assert first != null;
      assert second != null;
      mFirstSet = first;
      mSecondSet = second;
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public int hashCode()
    {
      return mFirstSet.hashCode() * 13 + mSecondSet.hashCode();
    }

    @Override
    public boolean equals(final Object other)
    {
      if (other instanceof LCPair) {
        final LCPair pair = (LCPair) other;
        return mFirstSet == pair.mFirstSet && mSecondSet == pair.mSecondSet;
      } else {
        return false;
      }
    }

    @Override
    public String toString()
    {
      return
        Arrays.toString(mFirstSet.toArray()) + " : " +
        Arrays.toString(mSecondSet.toArray());
    }

    //#######################################################################
    //# Data Members
    private final TIntHashSet mFirstSet;
    private final TIntHashSet mSecondSet;
  }


  //#########################################################################
  //# Inner Class MCTriple
  private static class MCTriple
  {
    //#######################################################################
    //# Constructor
    private MCTriple(final LCPair ptuple, final int s)
    {
      mTuple = ptuple;
      mState = s;
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public int hashCode()
    {
      return mTuple.hashCode() * 13 + mState;
    }

    @Override
    public boolean equals(final Object o)
    {
      final MCTriple other = (MCTriple)o;
      return mState == other.mState && mTuple.equals(other.mTuple);
    }

    //#######################################################################
    //# Data Members
    private final LCPair mTuple;
    private final int mState;
  }


  //#########################################################################
  //# Data Members
  private final ListBufferTransitionRelation mFirstRelation;
  private final ListBufferTransitionRelation mSecondRelation;
  private Map<TIntHashSet,TIntHashSet> mSetCache;
  private TObjectIntHashMap<LCPair> mTupleCache;
  private List<LCPair> mStates;
  private List<TIntArrayList> mSuccessors;
  private List<TIntHashSet[]> mPredeccessors;
  private TIntHashSet mFirstLC;
  private TIntHashSet mSecondBlocking;
  private final int mMarking;
  private int mExpanded;

  private ConflictPreorderResult mAnalysisResult;
  private long mStartTime;

}
