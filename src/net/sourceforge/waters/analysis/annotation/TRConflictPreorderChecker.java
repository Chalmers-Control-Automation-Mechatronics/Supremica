//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.annotation
//# CLASS:   TRConflictPreorderChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.annotation;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TObjectIntHashMap;

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
import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.Abortable;


/**
 * <P>An implementation of the conflict preorder algorithm.</P>
 *
 * <P><I>Reference:</I><BR>
 * Simon Ware, Robi Malik. A State-Based Characterisation of the Conflict
 * Preorder. Proc. 10th Workshop on the Foundations of Coordination Languages
 * and Software Architecture, FOCLASA 2011, 34-48, Aachen, Germany, 2011.</P>
 *
 * @author Simon Ware
 */

public class TRConflictPreorderChecker
  implements Abortable
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
    mIsAborting = false;
  }


  //#########################################################################
  //# Configuration
  public static int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
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
    throws AbortException
  {
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
    return isLessConflicting(createPair(calculateTauReachable(first, mFirstRelation),
                                        calculateTauReachable(second, mSecondRelation)));
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mIsAborting = true;
  }

  @Override
  public boolean isAborting()
  {
    return mIsAborting;
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
      final int state = togo.remove(togo.size() - 1);
      final TransitionIterator ti = trans.createSuccessorsReadOnlyIterator(state,
                                                                     EventEncoding.TAU);
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
    throws AbortException
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
    throws AbortException
  {
    boolean modified = true;
    while (modified) {
      modified = false;
      final TIntArrayList makelc = new TIntArrayList();
      final Set<MCTriple> MCTriples = new THashSet<MCTriple>();
      final List<MCTriple> tobeexpanded = new ArrayList<MCTriple>();
      for (int s = 0; s < mStates.size(); s++) {
        if (!mFirstLC.contains(s)) {
          checkAbort();
          makelc.add(s);
          final LCPair state = mStates.get(s);
          final TIntHashSet moreset = state.mSecondSet;
          if (moreset.contains(-1)) {
            final MCTriple triple = new MCTriple(mStates.get(s), -1);
            MCTriples.add(triple);
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
                if (MCTriples.add(add)) {tobeexpanded.add(add);}
              }
            }
          }
        }
      }
      for (int i = 0; i < makelc.size(); i++) {
        final int state = makelc.get(i);
        final LCPair tup = mStates.get(state);
        final TIntIterator it2 = tup.mSecondSet.iterator();
        while (it2.hasNext()) {
          final int propstate = it2.next();
          final MCTriple triple = new MCTriple(tup, propstate);
          if (!MCTriples.contains(triple)) {
            mFirstLC.add(state);
            modified = true;
          }
        }
      }
    }
  }

  @SuppressWarnings("unused")
  private boolean isLessConflicting(final int s1, final int s2)
    throws AbortException
  {
    return isLessConflicting(createPair(calculateTauReachable(s1, mFirstRelation),
                                        calculateTauReachable(s2, mSecondRelation)));
  }

  private boolean isLessConflicting(final LCPair tuple)
    throws AbortException
  {
    final int initial = getState(tuple);
    // adds the certain conflict states to the calculation
    getState(createPair(new TIntHashSet(), tuple.mSecondSet));
    expandStates();
    calculateLCStates();
    final TIntHashSet explored = new TIntHashSet();
    final TIntArrayList toexplore = new TIntArrayList();
    explored.add(initial);
    toexplore.add(initial);
    while (!toexplore.isEmpty()) {
      checkAbort();
      final int s = toexplore.remove(toexplore.size() -1);
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
          final int snum = states.remove(0);
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

  /**
   * Checks whether the model analyser has been requested to abort,
   * and if so, performs the abort by throwing an {@link AbortException}.
   * This method should be called periodically by any model analyser that
   * supports being aborted by user request.
   */
  private void checkAbort()
    throws AbortException
  {
    if (mIsAborting) {
      final AbortException exception = new AbortException();
      throw exception;
    }
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
  private final Map<TIntHashSet,TIntHashSet> mSetCache;
  private final TObjectIntHashMap<LCPair> mTupleCache;
  private final List<LCPair> mStates;
  private final List<TIntArrayList> mSuccessors;
  private final List<TIntHashSet[]> mPredeccessors;
  private final TIntHashSet mFirstLC;
  private final TIntHashSet mSecondBlocking;
  private final int mMarking;
  private int mExpanded;
  private boolean mIsAborting;

}
