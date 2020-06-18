//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class AddRedundantTransitions
{
  private final TransitionRelation mTrans;
  private long mTime = 0;
  private boolean flag = false;
  //private final TIntHashSet[][][] mSuccs;
  //private final Set<TIntHashSet>[][] mAnn;

  public static int TRANSITIONSADDED = 0;
  public static int TIME = 0;


  public static void clearStats()
  {
    TRANSITIONSADDED = 0;
    TIME = 0;
  }


  public static String stats()
  {
    return "RedundantTransitions: TRANSITIONSREMOVED = " + TRANSITIONSADDED +
           " TIME = " + TIME;
  }


  public AddRedundantTransitions(final TransitionRelation transitionrelation)
  {
    // int events = transitionrelation.numberOfEvents();
    // int states = transitionrelation.numberOfStates();
    mTrans = transitionrelation;
    //mSuccs = new TIntHashSet[states][events][];
    //mAnn = new Set[states][events];
  }

  /*private void setup()
  {
    for (int s = 0; s < mSuccs.length; s++) {
      for (int e1 = 0; e1 < mSuccs[s].length; e1++) {
        TIntHashSet succs1 = mTransitionRelation.getSuccessors(s, e1);
        if (succs1 == null || succs1.isEmpty()) {continue;}
        mSuccs[s][e1] = new TIntHashSet[mTransitionRelation.numberOfEvents()];
        mAnn[s][e1] = new THashSet<TIntHashSet>();
        int[] arrsuc = succs1.toArray();
        for (int si = 0; si < arrsuc.length; si++) {
          int succ = arrsuc[si];
          mAnn[s][e1].addAll(mTransitionRelation.getAnnotations2(succ));
          for (int e2 = 0; e2 < mSuccs[succ].length; e2++) {
            if (mTransitionRelation.isMarkingEvent(e2)) {
              if (mTransitionRelation.isMarked(succ)) {
                mSuccs[s][e1][e2] = new TIntHashSet();
              }
              continue;
            }
            TIntHashSet succs2 = mTransitionRelation.getSuccessors(succ, e2);
            if (succs2 == null || succs2.isEmpty()) {continue;}
            if (mSuccs[s][e1][e2] == null) {mSuccs[s][e1][e2] = new TIntHashSet();}
            mSuccs[s][e1][e2].addAll(succs2.toArray());
          }
        }
      }
    }
  }

  public void run()
  {
    TIME -= System.currentTimeMillis();
    setup();
    for (int state = 0; state < mTransitionRelation.numberOfStates(); state++) {
      int[] active = mTransitionRelation.getActiveEvents(state).toArray();
      for (int stateorig = 0; stateorig < mTransitionRelation.numberOfStates();
           stateorig++) {
        int[] activeorig = mTransitionRelation.getActiveEvents(stateorig).toArray();
        EventOrig:
        for (int ei1 = 0; ei1 < activeorig.length; ei1++) {
          int event = activeorig[ei1];
          if (mTransitionRelation.isMarkingEvent(event)) {continue;}
          if (mSuccs[stateorig][event] == null) {continue;}
          if (mTransitionRelation.getSuccessors(stateorig, event).contains(state)) {
            continue;
          }
          Annotations:
          for (TIntHashSet ann1 : mTransitionRelation.getAnnotations2(state)) {
            for (TIntHashSet ann2 : mAnn[stateorig][event]) {
              if (ann1.containsAll(ann2.toArray())) {continue Annotations;}
            }
            continue EventOrig;
          }
          Event:
          for (int ei2 = 0; ei2 < active.length; ei2++) {
            int e = active[ei2];
            if (mTransitionRelation.isMarkingEvent(e)) {
              if (mSuccs[stateorig][event][e] == null) {continue EventOrig;}
              else {continue Event;}
            }
            if (mSuccs[stateorig][event][e] == null) {continue EventOrig;}
            if (stateorig == state) {
              if (!mSuccs[stateorig][event][e].contains(state)) {continue EventOrig;}
            }
            if (mSuccs[stateorig][event][e].containsAll(mTransitionRelation.getSuccessors(state, e).toArray())) {
              continue Event;
            } else {
              continue EventOrig;
            }
          }
          //System.out.println("Added Transition");
          TRANSITIONSADDED++;
          mTransitionRelation.addTransition(stateorig, event, state);
        }
      }
    }
    System.out.println("transitions added: " + TRANSITIONSADDED);
    TIME += System.currentTimeMillis();
  }*/

  private boolean explore(final int sub, final TIntHashSet sup,
                          final Map<TIntHashSet, TIntHashSet> checking,
                          final Map<TIntHashSet, TIntHashSet> covered,
                          final Map<TIntHashSet, TIntHashSet> doesnt,
                          int depth)
  {
    //System.out.println(time);
    //System.out.println(System.currentTimeMillis() - time);
    if (System.currentTimeMillis() - mTime > 1000) {System.out.println("too deep"); flag = true; return false;}
    //if (depth > 300) {System.out.println("too deep"); flag = true; return false;}
    depth++;
    if (sup.contains(sub)) {return true;}
    TIntHashSet not = doesnt.get(sup);
    if (not != null && not.contains(sub)) {return false;}
    TIntHashSet does = covered.get(sup);
    if (does != null && does.contains(sub)) {return true;}
    does = checking.get(sup);
    if (does != null && does.contains(sub)) {return true;}
    if (depth % 50 == 0) {//System.out.println("depth:  " + depth);
      for(final TIntHashSet set: checking.keySet()) {
        if (sup.containsAll(set.toArray())) {
          if (checking.get(set).contains(sub)) {/*System.out.println("subset")*/; return true;}
        }
      }
      for(final TIntHashSet set: covered.keySet()) {
        if (sup.containsAll(set.toArray())) {
          if (covered.get(set).contains(sub)) {/*System.out.println("subset")*/;  return true;}
        }
      }
    }
    boolean coveredbool = true;
    final TIntHashSet supact = new TIntHashSet();
    final int[] suparr = sup.toArray();
    for (int i = 0; i < suparr.length; i++) {
      supact.addAll(mTrans.getActiveEvents(suparr[i]).toArray());
    }
    if (coveredbool) {
      if (!supact.containsAll(mTrans.getActiveEvents(sub).toArray())) {coveredbool = false;}
    }
    if (coveredbool) {
      final Set<TIntHashSet> supanns = new THashSet<TIntHashSet>();
      for (int i = 0; i < suparr.length; i++) {
        supanns.addAll(mTrans.getAnnotations2(suparr[i]));
      }
      Annotations:
      for (final TIntHashSet ann1 : mTrans.getAnnotations2(sub)) {
        for (final TIntHashSet ann2 : supanns) {
          if (ann1.containsAll(ann2.toArray())) {continue Annotations;}
        }
        coveredbool = false; break;
      }
    }
    CheckSuccs:
    if (coveredbool) {
      TIntHashSet covs = checking.get(sup);
      if (covs == null) {
        covs = new TIntHashSet();
        checking.put(sup, covs);
      }
      covs.add(sub);
      final int[] act = mTrans.getActiveEvents(sub).toArray();
      for (int ei = 0; ei < act.length; ei++) {
        final int e = act[ei];
        if (mTrans.isMarkingEvent(e)) {continue;}
        final TIntHashSet supsuccs = new TIntHashSet();
        for (int supi = 0; supi < suparr.length; supi++) {
          final int supstate = suparr[supi];
          if (mTrans.getSuccessors(supstate, e) == null) {continue;}
          supsuccs.addAll(mTrans.getSuccessors(supstate, e).toArray());
        }
        final int[] subsuccs = mTrans.getSuccessors(sub, e).toArray();
        for (int subi = 0; subi < subsuccs.length; subi++) {
          final int subsucc = subsuccs[subi];
          if (!explore(subsucc, supsuccs, checking, covered, doesnt, depth)) {coveredbool = false; break CheckSuccs;}
        }
      }
    }
    if (!coveredbool) {
      //TIntHashSet covs = covered.get(sup);
      checking.clear();
      not = doesnt.get(sup);
      if (not == null) {
        not = new TIntHashSet();
        doesnt.put(sup, not);
      }
      not.add(sub);
    }
    return coveredbool;
  }

  public void run()
  {
    System.out.println("setup8");
    final TIntArrayList[] mHas = new TIntArrayList[mTrans.numberOfEvents()];
    final IntInt[] eventorder = new IntInt[mHas.length];
    for (int e = 0; e < mTrans.numberOfEvents(); e++) {
      mHas[e] = new TIntArrayList();
      eventorder[e] = new IntInt(e, mHas[e]);
    }
    for (int state = 0; state < mTrans.numberOfStates(); state++) {
      if (!mTrans.hasPredecessors(state)) {continue;}
      for (int ei = 0; ei < mTrans.numberOfEvents(); ei++) {
        if (mTrans.isMarkingEvent(ei)) {
          if (mTrans.isMarked(state)) {mHas[ei].add(state);}
        } else {
          final TIntHashSet succ = mTrans.getSuccessors(state, ei);
          if (succ == null || succ.isEmpty()) {continue;}
          mHas[ei].add(state);
        }
      }
    }
    Arrays.sort(eventorder);
    final Map<TIntHashSet, TIntHashSet> checked = new THashMap<TIntHashSet, TIntHashSet>();
    final Map<TIntHashSet, TIntHashSet> covered = new THashMap<TIntHashSet, TIntHashSet>();
    final Map<TIntHashSet, TIntHashSet> doesnt = new THashMap<TIntHashSet, TIntHashSet>();
    LOOP:
    for (int sub = 0; sub < mTrans.numberOfStates(); sub++) {
      if (sub % 100 == 0) {System.out.println(sub);}
      if (!mTrans.hasPredecessors(sub)) {continue;}
      TIntArrayList arr = null;
      for (int ei = 0; ei < mTrans.numberOfEvents(); ei++) {
        final int e = eventorder[ei].mInt1;
        if (mTrans.isMarkingEvent(e)) {
          if (mTrans.isMarked(sub)) {arr = mHas[e]; break;}
        } else {
          final TIntHashSet succ = mTrans.getSuccessors(sub, e);
          if (succ == null || succ.isEmpty()) {continue;}
          arr = mHas[e]; break;
        }
      }
      if (arr == null) {continue;}
      mTime = System.currentTimeMillis();
      for (int supi = 0; supi < arr.size(); supi++) {
        final int sup = arr.get(supi);
        if (!mTrans.hasPredecessors(sup)) {continue;}
        if (sup == sub) {continue;}
        for (int e = 0; e < mTrans.numberOfEvents(); e++) {
          final TIntHashSet preds = mTrans.getPredecessors(sup, e);
          if (preds == null) {continue;}
          final int[] predsarr = preds.toArray();
          for (int pi = 0; pi < predsarr.length; pi++) {
            final int pred = predsarr[pi];
            final TIntHashSet subset = new TIntHashSet(1);
            subset.add(sub);
            checked.clear();
            if (explore(sub, new TIntHashSet(mTrans.getSuccessors(pred, e).toArray()), checked, covered, doesnt, 0)) {
              if (mTrans.addTransition(pred, e, sub)) {TRANSITIONSADDED++;}
              for (final TIntHashSet set : checked.keySet()) {
                TIntHashSet cov = covered.get(set);
                if (cov == null) {
                  cov = new TIntHashSet();
                  covered.put(set, cov);
                }
                cov.addAll(checked.get(set).toArray());
              }
            }
            if (flag) {break LOOP;}
            checked.clear();
          }
        }
      }
    }
    if (flag) {run2();}
    System.out.println(TRANSITIONSADDED);
  }

  public void run2()
  {
    TIME -= System.currentTimeMillis();
    System.out.println("setup 1");
    //setup();
    System.out.println("setup 2");
    boolean changed = true;
    /*TIntHashSet[][] builtsuccs = new TIntHashSet[mPreds.length][mEventNum];
    for (int state = 0; state < mTrans.numberOfStates(); state++) {
      for (int ei = 0; ei < mTrans.numberOfEvents(); ei++) {
        TIntHashSet succ = mTrans.getSuccessors(state, ei);
        if (succ == null) {continue;}
        builtsuccs[state][ei] = new TIntHashSet(succ.toArray());
      }
    }*/
    while(changed) {
      System.out.println("loop");
      changed = false;
    for (int sub = 0; sub < mTrans.numberOfStates(); sub++) {
      if (!mTrans.hasPredecessors(sub)) {continue;}
      TIntHashSet poss = null;
      for (int ei = 0; ei < mTrans.numberOfEvents(); ei++) {
        final TIntHashSet succ = mTrans.getSuccessors(sub, ei);
        if (succ == null || succ.isEmpty()) {continue;}
        poss = mTrans.getPredecessors(succ.toArray()[0], ei);
      }
      if (poss == null) {continue;}
      final int[] possarr = poss.toArray();
      Possible:
      for (int si = 0; si < possarr.length; si++) {
        final int sup = possarr[si];
        if (!mTrans.hasPredecessors(sup)) {continue;}
        if (sup == sub) {continue;}
        Annotations:
        for (final TIntHashSet ann1 : mTrans.getAnnotations2(sub)) {
          for (final TIntHashSet ann2 : mTrans.getAnnotations2(sup)) {
            if (ann1.containsAll(ann2.toArray())) {continue Annotations;}
          }
          continue Possible;
        }
        //if (!mTrans.isSubsetOutgoing(sub, sup)) {continue;}
        if (mTrans.isMarked(sub) && !mTrans.isMarked(sup)) {continue;}
        for (int e = 0; e < mTrans.numberOfEvents(); e++) {
          final TIntHashSet subsuccs = mTrans.getSuccessors(sub, e);
          if (subsuccs == null || subsuccs.isEmpty()) {continue;}
          if (mTrans.getSuccessors(sup, e) == null) {continue Possible;}
          final int[] succs = subsuccs.toArray();
          for (int i = 0; i < succs.length; i++) {
            final int suc = succs[i];
            if (suc != sub) {
              if (!mTrans.getSuccessors(sup, e).contains(suc)) {continue Possible;}
            } else {
              if (!mTrans.getSuccessors(sup, e).contains(suc) &&
                  !mTrans.getSuccessors(sup, e).contains(sup)) {continue Possible;}
            }
          }
        }
        //System.out.println("Added Transition");
        //mTransitionRelation.addTransition(stateorig, event, state);
        for (int pe = 0; pe < mTrans.numberOfEvents(); pe++) {
          final TIntHashSet predevents = mTrans.getPredecessors(sup, pe);
          if (predevents == null) {continue;}
          final int[] preds = predevents.toArray();
          for (int pri = 0; pri < preds.length; pri++) {
            final int pr = preds[pri];
            if (mTrans.addTransition(pr, pe, sub)) {TRANSITIONSADDED++; changed = true;}
            //if (builtsuccs[pr][pe].add(sub)) {}
          }
        }
      }
    }
    }
    System.out.println("transitions added: " + TRANSITIONSADDED);
    TIME += System.currentTimeMillis();
  }

  private static class IntInt
    implements Comparable<IntInt>
  {
    final public int mInt1;
    public TIntArrayList mList;

    public IntInt(final int i, final TIntArrayList list)
    {
      mInt1 = i;
      mList = list;
    }

    public int compareTo(final IntInt id)
    {
      if (mList.size() < id.mList.size()) {
        return -1;
      } else if (mList.size() > id.mList.size()){
        return 1;
      }
      return 0;
    }
  }
}
