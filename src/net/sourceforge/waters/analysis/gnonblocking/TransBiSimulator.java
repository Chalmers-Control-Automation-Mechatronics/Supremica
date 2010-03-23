//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   BiSimulator
//###########################################################################
//# $Id: BiSimulator.java 4514 2008-11-11 20:26:15Z js173 $
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntProcedure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.gnonblocking.TransitionRelation;
import net.sourceforge.waters.model.analysis.OverflowException;


/**
 * @author Simon Ware and Rachel Francis
 */

public class TransBiSimulator
{
  private final SimpleEquivalenceClass[] mStateToClass;
  private final Map<Integer,int[]> mClassMap;

  private final THashSet<SimpleEquivalenceClass> mWS;
  private final THashSet<ComplexEquivalenceClass> mWC;
  private final THashSet<SimpleEquivalenceClass> mP;
  private final boolean[] mMarked;
  private final boolean[] mPreMarked;
  private final int[][] mTauPreds;
  private int mStateNum;
  private final int mEventNum;
  private final int mTau;
  private final TransitionRelation mTransitionRelation;

  public static int STATESREMOVED = 0;
  public static int TIME = 0;

  public static void clearStats()
  {
    STATESREMOVED = 0;
    TIME = 0;
  }

  public static String stats()
  {
    return "TRANBISIM: STATESREMOVED = " + STATESREMOVED + " TIME = " + TIME;
  }

  // #########################################################################
  // # Constructor
  public TransBiSimulator(final TransitionRelation tr, final int tau)
      throws OverflowException
  {
    mTau = tau;
    mTransitionRelation = tr;
    mWS = new THashSet<SimpleEquivalenceClass>();
    mWC = new THashSet<ComplexEquivalenceClass>();
    mP = new THashSet<SimpleEquivalenceClass>();
    mStateToClass =
        new SimpleEquivalenceClass[mTransitionRelation.numberOfStates()];
    mClassMap = new HashMap<Integer,int[]>();
    mStateNum = tr.numberOfStates();
    mEventNum = tr.numberOfEvents();
    mMarked = new boolean[mTransitionRelation.numberOfStates()];
    mPreMarked = new boolean[mTransitionRelation.numberOfStates()];
    mTauPreds = new int[mTransitionRelation.numberOfStates()][];
    for (int s = 0; s < mTauPreds.length; s++) {
      final TIntHashSet hashtaupreds = new TIntHashSet();
      final TIntArrayList list = new TIntArrayList();
      hashtaupreds.add(s);
      list.add(s);
      while (!list.isEmpty()) {
        final int taupred = list.remove(list.size() - 1);
        mMarked[taupred] =
            mMarked[taupred] || mMarked[s]
                || mTransitionRelation.isMarked(taupred);
        mPreMarked[taupred] =
            mPreMarked[taupred] || mPreMarked[s]
                || mTransitionRelation.isPreMarked(taupred);
        final TIntHashSet taupreds =
            mTransitionRelation.getPredecessors(taupred, mTau);
        if (taupreds == null) {
          continue;
        }
        final int[] arrpreds = taupreds.toArray();
        for (int i = 0; i < arrpreds.length; i++) {
          final int ttaupred = arrpreds[i];
          if (hashtaupreds.add(ttaupred)) {
            list.add(ttaupred);
          }
        }
      }
      mTauPreds[s] = hashtaupreds.toArray();
    }
  }

  private void setupInitialPartitions()
  {
    // System.out.println("setup part");
    mWS.clear();
    mWC.clear();
    mP.clear();
    final TIntArrayList marked = new TIntArrayList();
    final TIntArrayList premarked = new TIntArrayList();
    final TIntArrayList bothmarkings = new TIntArrayList();
    final TIntArrayList notmarked = new TIntArrayList();
    mStateNum = 0;
    for (int i = 0; i < mTransitionRelation.numberOfStates(); i++) {
      if (!mTransitionRelation.hasPredecessors(i)) {
        continue;
      }
      if (mMarked[i] && !mPreMarked[i]) {
        marked.add(i);
        mStateNum++;
        continue;
      } else if (!mMarked[i] && mPreMarked[i]) {
        premarked.add(i);
        mStateNum++;
        continue;
      } else if (mMarked[i] && mPreMarked[i]) {
        bothmarkings.add(i);
        mStateNum++;
        continue;
      } else {
        notmarked.add(i);
        mStateNum++;
        continue;
      }
    }
    // System.out.println("marked: " + marked.size());
    if (!marked.isEmpty()) {
      mWS.add(new SimpleEquivalenceClass(marked.toNativeArray()));
    }
    if (!notmarked.isEmpty()) {
      mWS.add(new SimpleEquivalenceClass(notmarked.toNativeArray()));
    }
    if (!premarked.isEmpty()) {
      mWS.add(new SimpleEquivalenceClass(premarked.toNativeArray()));
    }
    if (!bothmarkings.isEmpty()) {
      mWS.add(new SimpleEquivalenceClass(bothmarkings.toNativeArray()));
    }
  }

  public boolean run()
  {
    TIME -= System.currentTimeMillis();
    setupInitialPartitions();
    while (true) {
      // System.out.println("partitioning");
      while (true) {
        Iterator<? extends EquivalenceClass> it = null;
        if (!mWS.isEmpty()) {
          it = mWS.iterator();
        } else if (!mWC.isEmpty()) {
          it = mWC.iterator();
        } else {
          break;
        }
        final EquivalenceClass ec = it.next();
        it.remove();
        ec.splitOn();
      }
      break;
    }
    if (mP.size() > mStateNum) {
      System.out.println("WTF?");
      System.exit(4);
    }
    if (mP.size() == mStateNum) {
      return false;
    }
    for (final SimpleEquivalenceClass sec : mP) {
      // System.out.println("sec:" + Arrays.toString(sec.mStates));
      mClassMap.put(sec.mStates[0], sec.mStates);
      if (sec.mStates.length == 1) {
        continue;
      }
      mTransitionRelation.merge(sec.mStates);
      STATESREMOVED += sec.mStates.length - 1;
    }
    // System.out.println("OBSSTATESREMOVED: " + (mStates - mP.size()));
    if (mP.size() == 0) {
      assert (false);
    }
    TIME += System.currentTimeMillis();
    return true;
  }

  public Map<Integer,int[]> getStateClasses()
  {
    return mClassMap;
  }

  private int[] getPredecessors(final int state, final int event)
  {
    if (event == mTau) {
      return mTauPreds[state];
    }
    final TIntHashSet preds = new TIntHashSet();
    for (int i = 0; i < mTauPreds[state].length; i++) {
      final int taupred = mTauPreds[state][i];
      final TIntHashSet tpreds =
          mTransitionRelation.getPredecessors(taupred, event);
      if (tpreds == null) {
        continue;
      }
      preds.addAll(tpreds.toArray());
    }
    if (preds.isEmpty()) {
      return null;
    }
    final int[] arrpreds = preds.toArray();
    for (int i = 0; i < arrpreds.length; i++) {
      final int pred = arrpreds[i];
      preds.addAll(mTauPreds[pred]);
    }
    return preds.toArray();
  }

  /*
   * private void partition() { int start = mPartitions.size(); int current =
   * start; for (int i = 0; i < mPartitions.size(); i++) { if
   * (mPartitions.get(i).size() > 1) { Map<Long, Integer> map = new
   * HashMap<Long, Integer>(); Iterator<Integer> it =
   * mPartitions.get(i).iterator(); map.put(stateHashCode(it.next()), i); while
   * (it.hasNext()) { int state = it.next(); long hash = stateHashCode(state);
   * int partition; if (map.containsKey(hash)) { partition = map.get(hash); }
   * else { partition = current; current++; map.put(hash, partition);
   * mPartitions.add(new HashSet<Integer>()); } if (partition != i) {
   * it.remove(); mPartitions.get(partition).add(state); } } } } for (int i =
   * start; i < mPartitions.size(); i++) { for (Integer j : mPartitions.get(i))
   * { mStateToPart[j] = i; } } }
   *
   * private long stateHashCode(int state) { long hashCode = 1; for(int i = 0; i
   * < mTransitionsSucc[state].length; i++) { int successor =
   * mTransitionsSucc[state][i]; int successorPartition = successor == -1 ? -1 :
   * mStateToPart[successor]; successorPartition++; hashCode = 3179 * hashCode +
   * successorPartition * i; } return hashCode; }
   */

  /*
   * private void addToW(SimpleEquivalenceClass sec, int[] X1, int[] X2) {
   * SimpleEquivalenceClass child1 = new SimpleEquivalenceClass(X1);
   * SimpleEquivalenceClass child2 = new SimpleEquivalenceClass(X2);
   * mP.remove(sec); //if (mW.remove(sec)) { mW.add(child1); mW.add(child2); }
   */

  private void addToW(final SimpleEquivalenceClass sec, final int[] X1,
                      final int[] X2)
  {
    final SimpleEquivalenceClass child1 = new SimpleEquivalenceClass(X1);
    final SimpleEquivalenceClass child2 = new SimpleEquivalenceClass(X2);
    mP.remove(sec);
    if (mWS.remove(sec)) {
      mWS.add(child1);
      mWS.add(child2);
    } else {
      final ComplexEquivalenceClass comp =
          new ComplexEquivalenceClass(child1, child2);
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

  private void addToW(final SimpleEquivalenceClass sec, int[] X1, int[] X2,
                      int[] X3)
  {
    if (X2.length < X1.length) {
      final int[] t = X1;
      X1 = X2;
      X2 = t;
    }
    if (X3.length < X1.length) {
      final int[] t = X1;
      X1 = X3;
      X3 = t;
    }
    final SimpleEquivalenceClass child1 = new SimpleEquivalenceClass(X1);
    final SimpleEquivalenceClass child2 = new SimpleEquivalenceClass(X2);
    final SimpleEquivalenceClass child3 = new SimpleEquivalenceClass(X3);
    mP.remove(sec);
    final ComplexEquivalenceClass X23 =
        new ComplexEquivalenceClass(child2, child3);
    final ComplexEquivalenceClass X123 =
        new ComplexEquivalenceClass(child1, X23);
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


  private class SimpleEquivalenceClass extends EquivalenceClass
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
      size = states.length; // TODO make this into function so less space
      for (int i = 0; i < states.length; i++) {
        mStateToClass[states[i]] = this;
      }
      mP.add(this);
    }

    // TODO maybe keep track of what events an equivalence class has no incoming
    // events from
    public void splitOn()
    {
      mInfo = new TIntIntHashMap[mEventNum];
      final List<SimpleEquivalenceClass> classes =
          new ArrayList<SimpleEquivalenceClass>();
      for (int e = 0; e < mEventNum; e++) {
        if (mTransitionRelation.isMarkingEvent(e)) {
          continue;
        }
        mInfo[e] = new TIntIntHashMap();
        final TIntIntHashMap map = mInfo[e];
        for (int s = 0; s < mStates.length; s++) {
          final int targ = mStates[s];
          final int[] preds = getPredecessors(targ, e);
          if (preds == null) {
            continue;
          }
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
                X2[x2] = state;
                x2++;
              } else {
                X1[x1] = state;
                x1++;
              }
            }
            addToW(sec, X1, X2);
          }
          sec.mSplit1 = null;
        }
        classes.clear();
      }
    }

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
      if (mTransitionRelation.isMarkingEvent(event)) {
        return info;
      }
      for (int i = 0; i < mStates.length; i++) {
        final int[] preds = getPredecessors(mStates[i], event);
        if (preds == null) {
          continue;
        }
        for (int j = 0; j < preds.length; j++) {
          info.adjustOrPutValue(preds[j], 1, 1);
        }
      }
      return info;
    }
  }


  private class ComplexEquivalenceClass extends EquivalenceClass
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
          public boolean execute(final int state, int value)
          {
            if (value == 0) {
              System.out.println("zero value split");
              info.remove(state);
              return true;
            }
            final int value1 = info1.get(state);
            final SimpleEquivalenceClass sec = mStateToClass[state];
            if (!sec.mSplit) {
              classes.add(sec);
              sec.mSplit = true;
            }
            if (value == value1) {
              TIntArrayList X1 = sec.X1;
              if (X1 == null) {
                X1 = new TIntArrayList();
                sec.X1 = X1;
              }
              X1.add(state);
            } else if (value1 == 0) {
              TIntArrayList X2 = sec.X2;
              if (X2 == null) {
                X2 = new TIntArrayList();
                sec.X2 = X2;
              }
              X2.add(state);
            } else {
              TIntArrayList X3 = sec.X3;
              if (X3 == null) {
                X3 = new TIntArrayList();
                sec.X3 = X3;
              }
              X3.add(state);
            }
            value -= value1;
            if (value != 0) {
              process.put(state, value);
            }
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
          /*
           * System.out.println("number:" + number); System.out.println("X1:" +
           * (sec.X1 != null) + " X2:" + (sec.X2 != null) + " X3:" + (sec.X3 !=
           * null)); X1 = sec.X1 == null ? null : sec.X1.toNativeArray(); X2 =
           * sec.X2 == null ? null : sec.X2.toNativeArray(); X3 = sec.X3 == null
           * ? null : sec.X3.toNativeArray(); System.out.println("X1:" +
           * Arrays.toString(X1)); System.out.println("X2:" +
           * Arrays.toString(X2)); System.out.println("X3:" +
           * Arrays.toString(X3)); System.out.println("X:" +
           * Arrays.toString(sec.mStates));
           */
          if (number == 2) {
            X1 = sec.X1 == null ? null : sec.X1.toNativeArray();
            X2 = sec.X2 == null ? null : sec.X2.toNativeArray();
            X3 = sec.X3 == null ? null : sec.X3.toNativeArray();
            if (X1 == null) {
              X1 = X3;
            } else if (X2 == null) {
              X2 = X3;
            }
            addToW(sec, X1, X2);
          } else if (number == 3) {
            X1 = sec.X1.toNativeArray();
            X2 = sec.X2.toNativeArray();
            X3 = sec.X3.toNativeArray();
            sec.mSplit = false;
            addToW(sec, X1, X2, X3);
          }
          sec.X1 = null;
          sec.X2 = null;
          sec.X3 = null;
          sec.mSplit = false;
        }
        classes.clear();
      }
      // mChild2.mInfo = mInfo;
      mInfo = null;
      mChild1.mParent = null;
      mChild2.mParent = null;
      if (mChild1 instanceof ComplexEquivalenceClass) {
        mWC.add((ComplexEquivalenceClass) mChild1);
      }
      if (mChild2 instanceof ComplexEquivalenceClass) {
        mWC.add((ComplexEquivalenceClass) mChild2);
      }
    }

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
        final TIntIntHashMap t = info1;
        info1 = info2;
        info2 = t;
      }
      final TIntIntHashMap info = new TIntIntHashMap(info1.size());
      info1.forEachEntry(new TIntIntProcedure() {
        public boolean execute(final int state, final int value)
        {
          info.put(state, value);
          return true;
        }
      });
      info2.forEachEntry(new TIntIntProcedure() {
        public boolean execute(final int state, final int value)
        {
          info.adjustOrPutValue(state, value, value);
          return true;
        }
      });
      res = info;
      mInfo[event] = res;
      return res;
    }
  }
}
