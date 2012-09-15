//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   OPSearchAutomatonSimplifier
//###########################################################################
//# $Id: OPSearchAutomatonSimplifier.java 5606 2010-05-03 03:02:04Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;


/**
 * A Determinizer.
 *
 * This class is superseded by {@link
 * net.sourceforge.waters.analysis.abstraction.SubsetConstructionTRSimplifier
 * SubsetConstructionTRSimplifier}.
 *
 * @author Simon Ware
 */

@Deprecated
public class Determinizer
{

  //#########################################################################
  //# Constructors
  public Determinizer(final ListBufferTransitionRelation aut,
                      final EventEncoding encoding, final int marking)
  {
    mAut = aut;
    mEncoding = encoding;
    mTrans = new ArrayList<int[]>();
    mMarked = new TIntArrayList();
    mMarking = marking;

    mStateSets = new THashSet<TIntHashSet>();
    mExplore = new ArrayList<TIntHashSet>();
    mStateSetInt = new TObjectIntHashMap<TIntHashSet>();
    mInitialToInitial = new TIntIntHashMap();
  }

  //#########################################################################
  //# Invocation
  public boolean run(final int[] states) throws AnalysisException
  {
    //System.out.println("states:" + Arrays.toString(states));
    try {
      for (int i = 0; i < states.length; i++) {
        final int state = states[i];
        final TIntHashSet sset = new TIntHashSet(1);
        sset.add(state);
        extendstate(sset);
        addStateSet(sset, true);
        mInitialToInitial.put(state, mStateSetInt.get(sset));
      }
      while (!mExplore.isEmpty()) {
        if (mStateSets.size() > mNodeLimit) {
          throw new AnalysisException("overflow");
        }
        final TIntHashSet sset = mExplore.remove(mExplore.size() - 1);
        expandstate(sset);
      }
    } finally {
    }
    return true;
  }

  public boolean run() throws AnalysisException
  {
    getInitialState();
    while (!mExplore.isEmpty()) {
      final TIntHashSet sset = mExplore.remove(mExplore.size() - 1);
      expandstate(sset);
    }
    return true;
  }

  private void getInitialState()
    throws AnalysisException
  {
    final TIntHashSet subset = new TIntHashSet();
    for (int i = 0; i < mAut.getNumberOfStates(); i++) {
      if (mAut.isInitial(i)) {subset.add(i);}
    }
    extendstate(subset);
    addStateSet(subset, true);
  }

  private boolean addStateSet(final TIntHashSet subset, final boolean initial)
    throws AnalysisException
  {
    if (mStateSets.add(subset)) {
      //System.out.println("added state");
      mExplore.add(subset);
      mStateSetInt.put(subset, mStateSets.size() - 1);
      if (initial) {mInitials.add(mStateSets.size() - 1);}
      if (mStateSets.size() > mNodeLimit) {
        throw new AnalysisException("overflow");
      }
      return true;
    }
    if (initial) {mInitials.add(mStateSetInt.get(subset));}
    return false;
  }

  private void expandstate(final TIntHashSet sset)
    throws AnalysisException
  {
    final int sourcestate = mStateSetInt.get(sset);
    final int[] arr = sset.toArray();
    boolean marked = false;
    for (int i = 0; i < arr.length; i++) {
      marked = mAut.isMarked(arr[i], mMarking) || marked;
      if (marked) {mMarked.add(sourcestate); break;}
    }
    for (int e = 0; e < mAut.getNumberOfProperEvents(); e++) {
      if (e == EventEncoding.TAU) {continue;}
      TIntHashSet targetstateset = null;
      for (int i = 0; i < arr.length; i++) {
        final int s = arr[i];
        final TransitionIterator ti = mAut.createSuccessorsReadOnlyIterator(s, e);
        while (ti.advance()) {
          targetstateset = targetstateset == null ? new TIntHashSet()
                                                  : targetstateset;
          targetstateset.add(ti.getCurrentTargetState());
        }
      }
      if (targetstateset != null) {
        extendstate(targetstateset);
        addStateSet(targetstateset, false);
        final int t = mStateSetInt.get(targetstateset);
        mTrans.add(new int[] {sourcestate, e, t});
        //System.out.println("trans: " + mTrans.size());
      }
    }
  }

  private void extendstate(final TIntHashSet tis)
  {
    final TIntArrayList tial = new TIntArrayList(tis.toArray());
    for (int i = 0; i < tial.size(); i++) {
      final int source = tial.get(i);
      final TransitionIterator ti =
        mAut.createSuccessorsReadOnlyIterator(source, EventEncoding.TAU);
      while (ti.advance()) {
        if (tis.add(ti.getCurrentTargetState())) {
          tial.add(ti.getCurrentTargetState());
        }
      }
    }
  }

  public ListBufferTransitionRelation getAutomaton() throws OverflowException
  {
    final ListBufferTransitionRelation det = new ListBufferTransitionRelation("det:" + mAut.getName(),
                                                                        mAut.getKind(),
                                                                        mEncoding,
                                                                        mStateSets.size(),
                                                                        ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    for (final int[] tran : mTrans) {
      det.addTransition(tran[0], tran[1], tran[2]);
    }
    for (int i = 0; i < mMarked.size(); i++) {
      det.setMarked(mMarked.get(i), mMarking, true);
    }
    for (int i = 0; i < mInitials.size(); i++) {
      det.setInitial(mInitials.get(i), true);
    }
    for (int i = 0; i < det.getNumberOfStates(); i++) {
      det.setReachable(i, true);
    }
    return det;
  }

  public TObjectIntHashMap<TIntHashSet> getSetStateMap()
  {
    return mStateSetInt;
  }

  public TIntIntHashMap getInitialToInitial()
  {
    return mInitialToInitial;
  }

  public void setNodeLimit(final int nodelimit)
  {
    mNodeLimit = nodelimit;
  }

  //#########################################################################
  //# Data Members
  private final ListBufferTransitionRelation mAut;
  private final EventEncoding mEncoding;
  private final List<int[]> mTrans;
  private final TIntArrayList mMarked;
  private final int mMarking;
  private int mNodeLimit = 10000;

  private final TIntArrayList mInitials = new TIntArrayList();

  private final Set<TIntHashSet> mStateSets;
  private final List<TIntHashSet> mExplore;

  private final TObjectIntHashMap<TIntHashSet> mStateSetInt;

  private final TIntIntHashMap mInitialToInitial;

}
