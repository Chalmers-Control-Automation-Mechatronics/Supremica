//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.analysis.OverflowException;


/**
 * @author Simon Ware
 */

public class BiSimulatorLanguage
{
  private final SimpleEquivalenceClass[] mStateToClass;
  private final THashSet<SimpleEquivalenceClass> mWS;
  private final THashSet<ComplexEquivalenceClass> mWC;
  private final THashSet<SimpleEquivalenceClass> mP;
  private final TIntHashSet[][] mPreds;
  private final boolean[] mMarked;
  private int mStates;
  private final int mEventNum;
  private TIntHashSet[][][] mSuccs;
  private Set<TIntHashSet>[][] mAnn;

  private final TransitionRelation mTrans;

  public static int STATESREMOVED = 0;
  public static int TIME = 0;
  public static int TRANSITIONSADDED = 0;

  public static void clearStats()
  {
    STATESREMOVED = 0;
    TIME = 0;
  }

  public static String stats()
  {
    return "BISIMREDUNDANT: STATESREMOVED = " + STATESREMOVED +
            " TIME = " + TIME;
  }

  public long longify(final int state1, final int state2)
  {
    long merge = state1;
    merge <<= 32;
    merge |= state2;
    return merge;
  }

  public int getSub(long merge)
  {
    merge >>= 32;
    return (int) merge;
  }

  public int getSup(long merge)
  {
    merge <<= 32;
    merge >>= 32;
    return (int) merge;
  }

  //#########################################################################
  //# Constructor
  @SuppressWarnings("unchecked")
  public BiSimulatorLanguage(final TransitionRelation tr)
    throws OverflowException
  {
    mTrans = tr;
    mWS = new THashSet<SimpleEquivalenceClass>();
    mWC = new THashSet<ComplexEquivalenceClass>();
    mP = new THashSet<SimpleEquivalenceClass>();
    mStateToClass = new SimpleEquivalenceClass[mTrans.numberOfStates()];
    mStates = tr.numberOfStates();
    mEventNum = tr.numberOfEvents();
    mMarked = new boolean[mTrans.numberOfStates()];
    mSuccs = new TIntHashSet[mTrans.numberOfStates()][mTrans.numberOfEvents()][];
    mAnn = new Set[mTrans.numberOfStates()][mTrans.numberOfEvents()];
    mPreds = new TIntHashSet[mTrans.numberOfStates()][mTrans.numberOfEvents()];
    for (int s = 0; s < mPreds.length; s++) {
      mMarked[s] = mTrans.isMarked(s);
      for (int e = 0; e < mTrans.numberOfEvents(); e++) {
        final TIntHashSet preds = mTrans.getPredecessors(s, e);
        if (preds == null) {continue;}
        mPreds[s][e] = new TIntHashSet(preds.toArray());
      }
    }
    setup6();
    mSuccs = null;
    mAnn = null;
  }

  /*public AddRedundantTransitions(TransitionRelation transitionrelation)
  {
    int events = transitionrelation.numberOfEvents();
    int states = transitionrelation.numberOfStates();
    mTransitionRelation = transitionrelation;
    mSuccs = new TIntHashSet[states][events][];
    mAnn = new Set[states][events];
  }*/

  private void setup()
  {
    for (int s = 0; s < mSuccs.length; s++) {
      for (int e1 = 0; e1 < mSuccs[s].length; e1++) {
        final TIntHashSet succs1 = mTrans.getSuccessors(s, e1);
        if (succs1 == null || succs1.isEmpty()) {continue;}
        mSuccs[s][e1] = new TIntHashSet[mTrans.numberOfEvents()];
        mAnn[s][e1] = new THashSet<TIntHashSet>();
        final int[] arrsuc = succs1.toArray();
        for (int si = 0; si < arrsuc.length; si++) {
          final int succ = arrsuc[si];
          mAnn[s][e1].addAll(mTrans.getAnnotations2(succ));
          for (int e2 = 0; e2 < mSuccs[succ].length; e2++) {
            if (mTrans.isMarkingEvent(e2)) {
              if (mTrans.isMarked(succ)) {
                mSuccs[s][e1][e2] = new TIntHashSet();
              }
              continue;
            }
            final TIntHashSet succs2 = mTrans.getSuccessors(succ, e2);
            if (succs2 == null || succs2.isEmpty()) {continue;}
            if (mSuccs[s][e1][e2] == null) {mSuccs[s][e1][e2] = new TIntHashSet();}
            mSuccs[s][e1][e2].addAll(succs2.toArray());
          }
        }
      }
    }
  }

  public void setup2()
  {
    TIME -= System.currentTimeMillis();
    System.out.println("setup 1");
    setup();
    System.out.println("setup 2");
    boolean changed = true;
    while(changed) {
      changed = false;
    for (int state = 0; state < mTrans.numberOfStates(); state++) {
      if (!mTrans.hasPredecessors(state)) {continue;}
      final int[] active = mTrans.getActiveEvents(state).toArray();
      for (int stateorig = 0; stateorig < mTrans.numberOfStates();
           stateorig++) {
        if (!mTrans.hasPredecessors(stateorig)) {continue;}
        final int[] activeorig = mTrans.getActiveEvents(stateorig).toArray();
        EventOrig:
        for (int ei1 = 0; ei1 < activeorig.length; ei1++) {
          final int event = activeorig[ei1];
          if (mTrans.isMarkingEvent(event)) {continue;}
          if (mSuccs[stateorig][event] == null) {continue;}
          if (mTrans.getSuccessors(stateorig, event).contains(state)) {
            continue;
          }
          Annotations:
          for (final TIntHashSet ann1 : mTrans.getAnnotations2(state)) {
            for (final TIntHashSet ann2 : mAnn[stateorig][event]) {
              if (ann1.containsAll(ann2.toArray())) {continue Annotations;}
            }
            continue EventOrig;
          }
          Event:
          for (int ei2 = 0; ei2 < active.length; ei2++) {
            final int e = active[ei2];
            if (mTrans.isMarkingEvent(e)) {
              if (mSuccs[stateorig][event][e] == null) {continue EventOrig;}
              else {continue Event;}
            }
            if (mSuccs[stateorig][event][e] == null) {continue EventOrig;}
            if (stateorig == state) {
              if (!mSuccs[stateorig][event][e].contains(state)) {continue EventOrig;}
            }
            if (mSuccs[stateorig][event][e].containsAll(
                mTrans.getSuccessors(state, e).toArray())) {
              continue Event;
            } else {
              continue EventOrig;
            }
          }
          //System.out.println("Added Transition");
          TRANSITIONSADDED++;
          //mTransitionRelation.addTransition(stateorig, event, state);
          if (mPreds[state][event] == null) {
            mPreds[state][event] = new TIntHashSet();
          }
          mPreds[state][event].add(stateorig);
          for (int pe = 0; pe < mTrans.numberOfEvents(); pe++) {
            final TIntHashSet predevents = mTrans.getPredecessors(stateorig, pe);
            if (predevents == null) {continue;}
            final int[] preds = predevents.toArray();
            for (int pri = 0; pri < preds.length; pri++) {
              final int pr = preds[pri];
              if (mSuccs[pr][pe][event].add(state)) {changed = true;}
            }
          }
        }
      }
    }
    }
    System.out.println("transitions added: " + TRANSITIONSADDED);
    TIME += System.currentTimeMillis();
  }

  public void setup4()
  {
    TIME -= System.currentTimeMillis();
    System.out.println("setup 1");
    //setup();
    System.out.println("setup 2");
    boolean changed = true;
    final TIntHashSet[][] builtsuccs = new TIntHashSet[mPreds.length][mEventNum];
    for (int state = 0; state < mTrans.numberOfStates(); state++) {
      for (int ei = 0; ei < mTrans.numberOfEvents(); ei++) {
        final TIntHashSet succ = mTrans.getSuccessors(state, ei);
        if (succ == null) {continue;}
        builtsuccs[state][ei] = new TIntHashSet(succ.toArray());
      }
    }
    while(changed) {
      System.out.println("loop");
      changed = false;
    for (int sub = 0; sub < mTrans.numberOfStates(); sub++) {
      if (!mTrans.hasPredecessors(sub)) {continue;}
      TIntHashSet poss = null;
      for (int ei = 0; ei < mTrans.numberOfEvents(); ei++) {
        final TIntHashSet succ = builtsuccs[sub][ei];
        if (succ == null || succ.isEmpty()) {continue;}
        poss = mPreds[succ.toArray()[0]][ei];
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
        if (mMarked[sub] && !mMarked[sup]) {continue;}
        for (int e = 0; e < builtsuccs[sub].length; e++) {
          final TIntHashSet subsuccs = builtsuccs[sub][e];
          if (subsuccs == null || subsuccs.isEmpty()) {continue;}
          if (builtsuccs[sup][e] == null) {continue Possible;}
          final int[] succs = subsuccs.toArray();
          for (int i = 0; i < succs.length; i++) {
            final int suc = succs[i];
            if (suc != sub) {
              if (!builtsuccs[sup][e].contains(suc)) {continue Possible;}
            } else {
              if (!builtsuccs[sup][e].contains(suc) && !builtsuccs[sup][e].contains(sup)) {continue Possible;}
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
            if (mPreds[sub][pe] == null) {
              mPreds[sub][pe] = new TIntHashSet();
            }
            if (mPreds[sub][pe].add(pr)) {TRANSITIONSADDED++;}
            if (builtsuccs[pr][pe].add(sub)) {changed = true;}
          }
        }
      }
    }
    }
    System.out.println("transitions added: " + TRANSITIONSADDED);
    TIME += System.currentTimeMillis();
  }

  public void setup3()
  {
    int addedtrans = 0;
    final Map<TIntHashSet, TIntHashSet> stateswithannotation =
      new HashMap<TIntHashSet, TIntHashSet>();
    for (int s = 0; s < mTrans.numberOfStates(); s++) {
      if (!mTrans.hasPredecessors(s)) {continue;}
      for (final TIntHashSet ann : mTrans.getAnnotations2(s)) {
        TIntHashSet hasann = stateswithannotation.get(ann);
        if (hasann == null) {
          hasann = new TIntHashSet();
          stateswithannotation.put(ann, hasann);
        }
        hasann.add(s);
      }
    }
    for (int s = 0; s < mTrans.numberOfStates(); s++) {
      if (!mTrans.hasPredecessors(s)) {continue;}
      final TIntHashSet checked = new TIntHashSet();
      final TIntHashSet tobeadded = new TIntHashSet();
      checked.add(s);
      for (final TIntHashSet ann : mTrans.getAnnotations2(s)) {
        final int[] possstates = stateswithannotation.get(ann).toArray();
        for (int i = 0; i < possstates.length; i++) {
          final int pstate = possstates[i];
          if (!checked.add(pstate)) {continue;}
          if (!mTrans.getAnnotations2(s).containsAll(
              mTrans.getAnnotations2(pstate))) {continue;}
          if (!mTrans.isSubsetOutgoing(pstate, s)) {continue;}
          tobeadded.add(pstate);
        }
      }
      if (tobeadded.isEmpty()) {continue;}
      final int[] arradded = tobeadded.toArray();
      for (int e = 0; e < mTrans.numberOfEvents(); e++) {
        final TIntHashSet preds = mTrans.getPredecessors(s, e);
        if (preds == null) {continue;}
        final int[] predsarr = preds.toArray();
        for (int i = 0; i < predsarr.length; i++) {
          final int pred = predsarr[i];
          for (int j = 0; j < arradded.length; j++) {
            final int added = arradded[j];
            if (mPreds[added][e] == null) {
              mPreds[added][e] = new TIntHashSet();
            }
            if (mPreds[added][e].add(pred)) {addedtrans++;}
          }
        }
      }
    }
    System.out.println(addedtrans);
  }

  private void setupInitialPartitions()
  {
    mWS.clear();
    mWC.clear();
    mP.clear();
    final Map<Set<TIntHashSet>, TIntArrayList> map =
      new THashMap<Set<TIntHashSet>, TIntArrayList>();
    mStates = 0;
    for (int i = 0; i < mTrans.numberOfStates(); i++) {
      if (!mTrans.hasPredecessors(i)) {continue;}
      mStates++;
      final Set<TIntHashSet> prop = mTrans.getAnnotations2(i);
      TIntArrayList p = map.get(prop);
      if (p == null) {p = new TIntArrayList(); map.put(prop, p);}
      p.add(i);
    }
    for (final TIntArrayList p : map.values()) {
      final TIntArrayList marked = new TIntArrayList();
      final TIntArrayList notmarked = new TIntArrayList();
      for (int i = 0; i < p.size(); i++) {
        final int s = p.get(i);
        if (mMarked[s]) {
          marked.add(s);
        } else {
          notmarked.add(s);
        }
      }
      /*if (p.size() == 11 || p.size() == 22) {
        System.out.println("size:" + p.size());
        System.out.println(props);
      }*/
      if (!marked.isEmpty()) {mWS.add(new SimpleEquivalenceClass(marked.toArray()));}
      if (!notmarked.isEmpty()) {mWS.add(new SimpleEquivalenceClass(notmarked.toArray()));}
    }
    //System.out.println("initial partitions: " + mWS.size());
    //System.out.println("maut:" + mStates);
  }

  public void setup5()
  {
    final TIntHashSet[][] builtsuccs = new TIntHashSet[mPreds.length][mEventNum];
    for (int state = 0; state < mTrans.numberOfStates(); state++) {
      for (int ei = 0; ei < mTrans.numberOfEvents(); ei++) {
        final TIntHashSet succ = mTrans.getSuccessors(state, ei);
        if (succ == null) {continue;}
        builtsuccs[state][ei] = new TIntHashSet(succ.toArray());
      }
    }
    final TLongHashSet DoesntSatisfy = new TLongHashSet();
    final TLongObjectHashMap<TLongHashSet> rely = new TLongObjectHashMap<TLongHashSet>();
    final TLongObjectHashMap<Collection<TLongHashSet>> needatleastone =
      new TLongObjectHashMap<Collection<TLongHashSet>>();
    for (int sub = 0; sub < mTrans.numberOfStates(); sub++) {
      if (sub % 100 == 0) {System.out.println(sub);}
      if (!mTrans.hasPredecessors(sub)) {continue;}
      for (int sup = 0; sup < mTrans.numberOfStates(); sup++) {
        if (!mTrans.hasPredecessors(sup)) {continue;}
        if (sup == sub) {continue;}
        final long longed = longify(sub, sup);
        boolean covered = true;
        Annotations:
        for (final TIntHashSet ann1 : mTrans.getAnnotations2(sub)) {
          for (final TIntHashSet ann2 : mTrans.getAnnotations2(sup)) {
            if (ann1.containsAll(ann2.toArray())) {continue Annotations;}
          }
          covered = false; break;
        }
        //if (mMarked[sub] && !mMarked[sup]) {continue;}
        if (covered && !mTrans.getActiveEvents(sup).containsAll(mTrans.getActiveEvents(sub).toArray())) {covered = false;}
        if (covered) {
          final Collection<TLongHashSet> atleastone = new ArrayList<TLongHashSet>();
          Successors:
          for (int e = 0; e < builtsuccs[sub].length; e++) {
            final TIntHashSet subsuccs = builtsuccs[sub][e];
            if (subsuccs == null || subsuccs.isEmpty()) {continue;}
            final int[] succs = subsuccs.toArray();
            for (int i = 0; i < succs.length; i++) {
              final int suc = succs[i];
              boolean tran = true;
              if (suc != sub) {
                if (!builtsuccs[sup][e].contains(suc)) {tran = false;}
              } else {
                if (!builtsuccs[sup][e].contains(suc) && !builtsuccs[sup][e].contains(sup)) {tran = false;}
              }
              if (!tran) {
                final TLongHashSet leastone = new TLongHashSet();
                atleastone.add(leastone);
                final int[] poss = builtsuccs[sup][e].toArray();
                for (int k = 0; k < poss.length; k++) {
                  final int posssuc = poss[k];
                  final long longed2 = longify(suc, posssuc);
                  if (DoesntSatisfy.contains(longed2)) {continue;}
                  leastone.add(longed2);
                  TLongHashSet relies = rely.get(longed2);
                  if (relies == null) {
                    relies = new TLongHashSet();
                    rely.put(longed2, relies);
                  }
                  relies.add(longed);
                }
                if (leastone.isEmpty()) {covered = false; break Successors;}
              }
            }
          }
          if (covered) {needatleastone.put(longed, atleastone);}
        }
        if (!covered) {
          final TLongArrayList toberemmed = new TLongArrayList();
          toberemmed.add(longed);
          DoesntSatisfy.add(longed);
          while (!toberemmed.isEmpty()) {
            final int end = toberemmed.size() - 1;
            final long rem = toberemmed.get(end);
            toberemmed.removeAt(end);
            needatleastone.remove(rem);
            final TLongHashSet longset = rely.remove(rem);
            if (longset == null) {continue;}
            final long[] longarray = longset.toArray();
            for (int i = 0; i < longarray.length; i++) {
              final long reliant = longarray[i];
              if (DoesntSatisfy.contains(reliant)) {continue;}
              final Collection<TLongHashSet> leastone = needatleastone.get(reliant);
              for (final TLongHashSet one : leastone) {
                one.remove(rem);
                if (one.isEmpty()) {
                  DoesntSatisfy.add(reliant);
                  toberemmed.add(reliant);
                }
              }
            }
          }
        }
      }
    }
    for (int sub = 0; sub < mTrans.numberOfStates(); sub++) {
      if (!mTrans.hasPredecessors(sub)) {continue;}
      for (int sup = 0; sup < mTrans.numberOfStates(); sup++) {
        if (!mTrans.hasPredecessors(sup)) {continue;}
        if (sup == sub) {continue;}
        final long longed = longify(sub, sup);
        if (DoesntSatisfy.contains(longed)) {continue;}
        for (int pe = 0; pe < mTrans.numberOfEvents(); pe++) {
          final TIntHashSet predevents = mTrans.getPredecessors(sup, pe);
          if (predevents == null) {continue;}
          final int[] preds = predevents.toArray();
          for (int pri = 0; pri < preds.length; pri++) {
            final int pr = preds[pri];
            if (mPreds[sub][pe] == null) {
              mPreds[sub][pe] = new TIntHashSet();
            }
            if (mPreds[sub][pe].add(pr)) {System.out.println(pr + ", " + mTrans.getEvent(pe) + ", " + sub); TRANSITIONSADDED++;}
          }
        }
      }
    }
    System.out.println(TRANSITIONSADDED);
    if (TRANSITIONSADDED == 13) {System.exit(4);}
  }

  public void setup6()
  {
    System.out.println("setup6");
    final TIntHashSet[][] builtsuccs = new TIntHashSet[mPreds.length][mEventNum];
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
          builtsuccs[state][ei] = new TIntHashSet(succ.toArray());
          mHas[ei].add(state);
        }
      }
    }
    Arrays.sort(eventorder);
    final TLongHashSet DoesntSatisfy = new TLongHashSet();
    final TLongHashSet checked = new TLongHashSet();
    long time = System.currentTimeMillis();
    for (int sub = 0; sub < mTrans.numberOfStates(); sub++) {
      if (sub % 100 == 0) {System.out.println(sub);}
      if (!mTrans.hasPredecessors(sub)) {continue;}
      long memory = Runtime.getRuntime().totalMemory() -
                    Runtime.getRuntime().freeMemory();
      if (System.currentTimeMillis() - time > 10000) {return;}
      if (memory > 1000000000) {
        System.gc();
        memory = Runtime.getRuntime().totalMemory() -
                      Runtime.getRuntime().freeMemory();
        if (memory > 1000000000) {
          return;
        }
      }
      if (sub % 100 == 0) {System.out.println(memory);}
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
      for (int supi = 0; supi < arr.size(); supi++) {
        final int sup = arr.get(supi);
        if (!mTrans.hasPredecessors(sup)) {continue;}
        if (sup == sub) {continue;}
        final long longed = longify(sub, sup);
        boolean covered = true;
        if (covered && !mTrans.getActiveEvents(sup).containsAll(mTrans.getActiveEvents(sub).toArray())) {covered = false;}
        if (!covered) {
          DoesntSatisfy.add(longed);
        } else {
          checked.add(longed);
        }
      }
    }
    final long[] arr = checked.toArray();
    time = System.currentTimeMillis();
    for (int i = 0; i < arr.length; i++) {
      if (i % 100 == 0) {System.out.println(i + "/" + arr.length);}
      if (System.currentTimeMillis() - time > 10000) {return;}
      final long longed = arr[i];
      if (DoesntSatisfy.contains(longed)) {continue;}
      int sub = getSub(longed);
      int sup = getSup(longed);
      boolean covered = true;
      Successors:
      for (int e = 0; e < builtsuccs[sub].length; e++) {
        final TIntHashSet subsuccs = builtsuccs[sub][e];
        if (subsuccs == null || subsuccs.isEmpty()) {continue;}
        final int[] succs = subsuccs.toArray();
        for (int j = 0; j < succs.length; j++) {
          final int suc = succs[j];
          if (!builtsuccs[sup][e].contains(suc)) {
            covered = false;
            final int[] poss = builtsuccs[sup][e].toArray();
            for (int k = 0; k < poss.length; k++) {
              final int posssuc = poss[k];
              final long longed2 = longify(suc, posssuc);
              if (!checked.contains(longed2) || DoesntSatisfy.contains(longed2)) {continue;}
              covered = true; break;
            }
          }
          if (!covered) {break Successors;}
        }
      }
      if (!covered) {
        final TLongArrayList toberemmed = new TLongArrayList();
        toberemmed.add(longed);
        DoesntSatisfy.add(longed);
        while (!toberemmed.isEmpty()) {
          final int end = toberemmed.size() - 1;
          final long rem = toberemmed.get(end);
          toberemmed.removeAt(end);
          sub = getSub(rem);
          sup = getSup(rem);
          //System.out.println("notcovered" + sub + ", " + sup);
          for (int e = 0; e < mTrans.numberOfEvents(); e++) {
            final TIntHashSet subpreds = mPreds[sub][e];
            final TIntHashSet suppreds = mPreds[sup][e];
            if (subpreds == null || suppreds == null) {continue;}
            final int[] subarr = subpreds.toArray();
            final int[] suparr = suppreds.toArray();
            for (int k = 0; k < suparr.length; k++) {
              final int suppred = suparr[k];
              boolean needcheck = false;
              for (int j = 0; j < subarr.length; j++) {
                  final int subpred = subarr[j];
                  final long longedpred = longify(subpred, suppred);
                  if (!checked.contains(longedpred) || DoesntSatisfy.contains(longedpred)) {continue;}
                  needcheck = true;
              }
              if (!needcheck) {continue;}
              //System.out.println("sub: " + subpred);
              //System.out.println("sup: " + suppred);
              final int[] suppredsuccs = builtsuccs[suppred][e].toArray();
              covered = false;
              for (int l = 0; l < suppredsuccs.length; l++) {
                final int suppredsucc = suppredsuccs[l];
                if (suppredsucc == sub) {covered = true; break;}
                final long longedsucc = longify(sub, suppredsucc);
                if (checked.contains(longedsucc) && !DoesntSatisfy.contains(longedsucc)) {covered = true; break;}
              }
              if (!covered) {
                for (int j = 0; j < subarr.length; j++) {
                  final int subpred = subarr[j];
                  final long longedpred = longify(subpred, suppred);
                  if (!checked.contains(longedpred) || DoesntSatisfy.contains(longedpred)) {continue;}
                //System.out.println(covered);
                  DoesntSatisfy.add(longedpred);
                  toberemmed.add(longedpred);
                }
              }
            }
          }
        }
      }
    }
    for (int i = 0; i < arr.length; i++) {
      final long longed = arr[i];
      if (DoesntSatisfy.contains(longed)) {continue;}
      final int sub = getSub(longed);
      final int sup = getSup(longed);
      for (int pe = 0; pe < mTrans.numberOfEvents(); pe++) {
        final TIntHashSet predevents = mTrans.getPredecessors(sup, pe);
        if (predevents == null) {continue;}
        final int[] preds = predevents.toArray();
        for (int pri = 0; pri < preds.length; pri++) {
          final int pr = preds[pri];
          if (mPreds[sub][pe] == null) {
            mPreds[sub][pe] = new TIntHashSet();
          }
          if (mPreds[sub][pe].add(pr)) {TRANSITIONSADDED++;}
        }
      }
    }
    System.out.println(TRANSITIONSADDED);
  }

  public boolean run()
  {
    TIME -= System.currentTimeMillis();
    setupInitialPartitions();
    while (true) {
      //System.out.println("partitioning");
      while (true) {
        Iterator<? extends EquivalenceClass> it = null;
        if (!mWS.isEmpty()) {it = mWS.iterator();}
        else if (!mWC.isEmpty()) {it = mWC.iterator();}
        else {break;}
        final EquivalenceClass ec = it.next(); it.remove(); ec.splitOn();
      }
      break;
    }
    if (mP.size() > mStates) {
      System.out.println("WTF?");
      System.exit(4);
    }
    if (mP.size() == mStates) {
      return false;
    }
    for (final SimpleEquivalenceClass sec : mP) {
      if (sec.mStates.length == 1) {continue;}
      //System.out.println(Arrays.toString(sec.mStates));
      mTrans.mergewithannotations(sec.mStates);
      STATESREMOVED += sec.mStates.length -1;
    }
    //System.out.println("STATESREMOVED: " + (mStates - mP.size()));
    if (mP.size() == 0) {
      assert(false);
    }
    TIME += System.currentTimeMillis();
    return true;
  }

  private int[] getPredecessors(final int state, final int event)
  {
    final TIntHashSet preds = mPreds[state][event];
    if (preds == null) {return null;}
    return preds.toArray();
  }

  /*private void partition()
  {
    int start = mPartitions.size();
    int current = start;
    for (int i = 0; i < mPartitions.size(); i++) {
      if (mPartitions.get(i).size() > 1) {
        Map<Long, Integer> map = new HashMap<Long, Integer>();
        Iterator<Integer> it = mPartitions.get(i).iterator();
        map.put(stateHashCode(it.next()), i);
        while (it.hasNext()) {
          int state = it.next();
          long hash = stateHashCode(state);
          int partition;
          if (map.containsKey(hash)) {
            partition = map.get(hash);
          } else {
            partition = current;
            current++;
            map.put(hash, partition);
            mPartitions.add(new HashSet<Integer>());
          }
          if (partition != i) {
            it.remove();
            mPartitions.get(partition).add(state);
          }
        }
      }
    }
    for (int i = start; i < mPartitions.size(); i++) {
      for (Integer j : mPartitions.get(i)) {
        mStateToPart[j] = i;
      }
    }
  }

  private long stateHashCode(int state)
  {
    long hashCode = 1;
    for(int i = 0; i < mTransitionsSucc[state].length; i++) {
      int successor = mTransitionsSucc[state][i];
      int successorPartition = successor == -1 ? -1 : mStateToPart[successor];
      successorPartition++;
      hashCode = 3179 * hashCode + successorPartition * i;
    }
    return hashCode;
  } */

  /*private void addToW(SimpleEquivalenceClass sec, int[] X1, int[] X2)
  {
    SimpleEquivalenceClass child1 = new SimpleEquivalenceClass(X1);
    SimpleEquivalenceClass child2 = new SimpleEquivalenceClass(X2);
    mP.remove(sec);
    //if (mW.remove(sec)) {
      mW.add(child1);
      mW.add(child2);
  }*/

  private void addToW(final SimpleEquivalenceClass sec, final int[] X1, final int[] X2)
  {
    final SimpleEquivalenceClass child1 = new SimpleEquivalenceClass(X1);
    final SimpleEquivalenceClass child2 = new SimpleEquivalenceClass(X2);
    mP.remove(sec);
    if (mWS.remove(sec)) {
      mWS.add(child1);
      mWS.add(child2);
    } else {
      final ComplexEquivalenceClass comp = new ComplexEquivalenceClass(child1, child2);
      comp.mInfo = sec.mInfo;
      if (sec.mParent != null) {
        comp.mParent = sec.mParent;
        final ComplexEquivalenceClass p = sec.mParent;
        p.mChild1 = p.mChild1 == sec ? comp : p.mChild1;
        p.mChild2 = p.mChild2 == sec ? comp : p.mChild2;
      } else {
        mWC.add(comp);
      }
    }
  }

  private void addToW(final SimpleEquivalenceClass sec, int[] X1, int[] X2, int[] X3)
  {
    if (X2.length < X1.length) {
      final int[] t = X1; X1 = X2; X2 = t;
    }
    if (X3.length < X1.length) {
      final int[] t = X1; X1 = X3; X3 = t;
    }
    final SimpleEquivalenceClass child1 = new SimpleEquivalenceClass(X1);
    final SimpleEquivalenceClass child2 = new SimpleEquivalenceClass(X2);
    final SimpleEquivalenceClass child3 = new SimpleEquivalenceClass(X3);
    mP.remove(sec);
    final ComplexEquivalenceClass X23 = new ComplexEquivalenceClass(child2, child3);
    final ComplexEquivalenceClass X123 = new ComplexEquivalenceClass(child1, X23);
    X123.mInfo = sec.mInfo;
    if (sec.mParent != null) {
      X123.mParent = sec.mParent;
      final ComplexEquivalenceClass p = sec.mParent;
      p.mChild1 = p.mChild1 == sec ? X123 : p.mChild1;
      p.mChild2 = p.mChild2 == sec ? X123 : p.mChild2;
    } else {
      mWC.add(X123);
    }
  }

  private abstract class EquivalenceClass
  {
    ComplexEquivalenceClass mParent = null;
    TIntIntHashMap[] mInfo = null;
    int size;

    public abstract TIntIntHashMap getInfo(int event);

    public abstract void splitOn();
  }

  private class SimpleEquivalenceClass
    extends EquivalenceClass
  {
    int[] mStates;
    TIntHashSet mSplit1 = null;
    TIntArrayList X1 = null;
    TIntArrayList X2 = null;
    TIntArrayList X3 = null;
    boolean mSplit = false;

    public SimpleEquivalenceClass(final int[] states)
    {
      mStates = states;
      size = states.length; //TODO make this into function so less space
      for (int i = 0; i < states.length; i++) {
        mStateToClass[states[i]] = this;
      }
      mP.add(this);
    }

    //TODO maybe keep track of what events an equivalence class has no incoming events from
    @Override
    public void splitOn()
    {
      mInfo = new TIntIntHashMap[mEventNum];
      final List<SimpleEquivalenceClass> classes =
        new ArrayList<SimpleEquivalenceClass>();
      for (int e = 0; e < mEventNum; e++) {
        if (mTrans.isMarkingEvent(e)) {continue;}
        mInfo[e] = new TIntIntHashMap();
        final TIntIntHashMap map = mInfo[e];
        for (int s = 0; s < mStates.length; s++) {
          final int targ = mStates[s];
          final int[] preds = getPredecessors(targ, e);
          if (preds == null) {continue;}
          for (int p = 0; p < preds.length; p++) {
            final int pred = preds[p];
            final SimpleEquivalenceClass ec = mStateToClass[pred];
            TIntHashSet split = ec.mSplit1;
            if (split == null) {
              split = new TIntHashSet(ec.size);
              ec.mSplit1 = split;
              classes.add(ec);
            }
            split.add(pred);
            map.adjustOrPutValue(pred, 1, 1);
          }
        }
        for (int c = 0; c < classes.size(); c++) {
          final SimpleEquivalenceClass sec = classes.get(c);
          if (sec.mSplit1.size() != sec.size) {
            final int[] X1 = new int[sec.size - sec.mSplit1.size()];
            final int[] X2 = new int[sec.mSplit1.size()];
            int x1 = 0, x2 = 0;
            for (int s = 0; s < sec.mStates.length; s++) {
              final int state = sec.mStates[s];
              if (sec.mSplit1.contains(state)) {
                X2[x2] = state; x2++;
              } else {
                X1[x1] = state; x1++;
              }
            }
            addToW(sec, X1, X2);
          }
          sec.mSplit1 = null;
        }
        classes.clear();
      }
    }

    @Override
    public TIntIntHashMap getInfo(final int event)
    {
      if (mInfo == null) {
        mInfo = new TIntIntHashMap[mEventNum];
      }
      TIntIntHashMap info = mInfo[event];
      if (info != null) {
        return info;
      }
      info = new TIntIntHashMap();
      mInfo[event] = info;
      if (mTrans.isMarkingEvent(event)) {return info;}
      for (int i = 0; i < mStates.length; i++) {
        final int[] preds = getPredecessors(mStates[i], event);
        if (preds == null) {continue;}
        for (int j = 0; j < preds.length; j++) {
          info.adjustOrPutValue(preds[j], 1, 1);
        }
      }
      return info;
    }
  }

  private class ComplexEquivalenceClass
    extends EquivalenceClass
  {
    EquivalenceClass mChild1;
    EquivalenceClass mChild2;

    public ComplexEquivalenceClass(final EquivalenceClass child1,
                                   final EquivalenceClass child2)
    {
      if (child1.size < child2.size) {
        mChild1 = child1;
        mChild2 = child2;
      } else {
        mChild2 = child1;
        mChild1 = child2;
      }
      mChild1.mParent = this;
      mChild2.mParent = this;
      size = child1.size + child2.size;
    }

    @Override
    public void splitOn()
    {
      final ArrayList<SimpleEquivalenceClass> classes =
        new ArrayList<SimpleEquivalenceClass>();
      mChild2.mInfo = new TIntIntHashMap[mEventNum];
      for (int e = 0; e < mEventNum; e++) {
        final TIntIntHashMap info = getInfo(e);
        final TIntIntHashMap process = new TIntIntHashMap();
        final TIntIntHashMap info1 = mChild1.getInfo(e);
        info.forEachEntry(new TIntIntProcedure() {
          @Override
          public boolean execute(final int state, int value) {
            if (value == 0) {
              System.out.println("zero value split");
              info.remove(state); return true;
            }
            final int value1 = info1.get(state);
            final SimpleEquivalenceClass sec = mStateToClass[state];
            if (!sec.mSplit) {
              classes.add(sec); sec.mSplit = true;
            }
            if (value == value1) {
              TIntArrayList X1 = sec.X1;
              if (X1 == null) {
                X1 = new TIntArrayList(); sec.X1 = X1;
              }
              X1.add(state);
            } else if (value1 == 0) {
              TIntArrayList X2 = sec.X2;
              if (X2 == null) {
                X2 = new TIntArrayList(); sec.X2 = X2;
              }
              X2.add(state);
            } else {
              TIntArrayList X3 = sec.X3;
              if (X3 == null) {
                X3 = new TIntArrayList(); sec.X3 = X3;
              }
              X3.add(state);
            }
            value -= value1;
            if (value != 0) {process.put(state, value);}
            return true;
          }
        });
        mChild2.mInfo[e] = process;
        for (int c = 0; c < classes.size(); c++) {
          final SimpleEquivalenceClass sec = classes.get(c);
          int[] X1, X2, X3;
          int number = sec.X1 != null ? 1 : 0;
          number = sec.X2 != null ? number + 1 : number;
          number = sec.X3 != null ? number + 1 : number;
          /*System.out.println("number:" + number);
          System.out.println("X1:" + (sec.X1 != null) + " X2:" + (sec.X2 != null) +
                             " X3:" + (sec.X3 != null));
          X1 = sec.X1 == null ? null : sec.X1.toNativeArray();
          X2 = sec.X2 == null ? null : sec.X2.toNativeArray();
          X3 = sec.X3 == null ? null : sec.X3.toNativeArray();
          System.out.println("X1:" + Arrays.toString(X1));
          System.out.println("X2:" + Arrays.toString(X2));
          System.out.println("X3:" + Arrays.toString(X3));
          System.out.println("X:" + Arrays.toString(sec.mStates));*/
          if (number == 2) {
            X1 = sec.X1 == null ? null : sec.X1.toArray();
            X2 = sec.X2 == null ? null : sec.X2.toArray();
            X3 = sec.X3 == null ? null : sec.X3.toArray();
            if (X1 == null) {
              X1 = X3;
            } else if (X2 == null) {
              X2 = X3;
            }
            addToW(sec, X1, X2);
          } else if(number == 3) {
            X1 = sec.X1.toArray(); X2 = sec.X2.toArray();
            X3 = sec.X3.toArray(); sec.mSplit = false;
            addToW(sec, X1, X2, X3);
          }
          sec.X1 = null; sec.X2 = null; sec.X3 = null; sec.mSplit = false;
        }
        classes.clear();
      }
      //mChild2.mInfo = mInfo;
      mInfo = null; mChild1.mParent = null; mChild2.mParent = null;
      if (mChild1 instanceof ComplexEquivalenceClass) {mWC.add((ComplexEquivalenceClass)mChild1);}
      if (mChild2 instanceof ComplexEquivalenceClass) {mWC.add((ComplexEquivalenceClass)mChild2);}
    }

    @Override
    public TIntIntHashMap getInfo(final int event)
    {
      if (mInfo == null) {
        mInfo = new TIntIntHashMap[mEventNum];
      }
      TIntIntHashMap res = mInfo[event];
      if (res != null) {
        return res;
      }
      TIntIntHashMap info1 = mChild1.getInfo(event);
      TIntIntHashMap info2 = mChild2.getInfo(event);
      if (info1.size() < info2.size()) {
        final TIntIntHashMap t = info1; info1 = info2; info2 = t;
      }
      final TIntIntHashMap info = new TIntIntHashMap(info1.size());
      info1.forEachEntry(new TIntIntProcedure() {
        @Override
        public boolean execute(final int state, final int value) {
          info.put(state, value); return true;
        }
      });
      info2.forEachEntry(new TIntIntProcedure() {
        @Override
        public boolean execute(final int state, final int value) {
          info.adjustOrPutValue(state, value, value); return true;
        }
      });
      res = info;
      mInfo[event] = res;
      return res;
    }
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

    @Override
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
