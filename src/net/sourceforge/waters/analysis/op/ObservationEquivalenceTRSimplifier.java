//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   ObserverProjectionBisimulator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntProcedure;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectProcedure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Simon Ware, Rachel Francis, Robi Malik
 */

public class ObservationEquivalenceTRSimplifier
  implements TransitionRelationSimplifier
{

  //#########################################################################
  //# Constructors
  public ObservationEquivalenceTRSimplifier
    (final ObserverProjectionTransitionRelation tr)
  {
    this(tr, -1);
  }

  public ObservationEquivalenceTRSimplifier
    (final ObserverProjectionTransitionRelation tr, final int tau)
  {
    mTau = tau;
    mTransitionRelation = tr;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  public ObserverProjectionTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  public void setTransitionRelation(final ObserverProjectionTransitionRelation rel)
  {
    mTransitionRelation = rel;
  }

  public boolean run()
  {
    setUp();
    setupInitialPartitions();
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
    assert mP.size() >= 0;
    assert mP.size() <= mNumStates;
    if (mP.size() == mNumStates) {
      return false;
    }
    mClassMap = new TIntObjectHashMap<int[]>();
    for (final SimpleEquivalenceClass sec : mP) {
      mClassMap.put(sec.mStates[0], sec.mStates);
      if (sec.mStates.length == 1) {
        continue;
      }
      mTransitionRelation.merge(sec.mStates, mTau);
    }
    return true;
  }

  public TIntObjectHashMap<int[]> getStateClasses()
  {
    return mClassMap;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setUp()
  {
    mNumStates = mTransitionRelation.getNumberOfStates();
    mNumEvents = mTransitionRelation.getNumberOfProperEvents();
    mTauPreds = new int[mNumStates][];
    for (int s = 0; s < mNumStates; s++) {
      if (mTau >= 0) {
        final TIntHashSet hashTauPreds = new TIntHashSet();
        final TIntArrayList list = new TIntArrayList();
        hashTauPreds.add(s);
        list.add(s);
        while (!list.isEmpty()) {
          final int taupred = list.remove(list.size() - 1);
          mTransitionRelation.copyMarkings(s, taupred);
          final TIntHashSet preds =
            mTransitionRelation.getPredecessors(taupred, mTau);
          if (preds != null) {
            final TIntIterator iter = preds.iterator();
            while (iter.hasNext()) {
              final int pred = iter.next();
              if (hashTauPreds.add(pred)) {
                list.add(pred);
              }
            }
          }
        }
        mTauPreds[s] = hashTauPreds.toArray();
      } else {
        final int[] array = new int[1];
        array[0] = s;
        mTauPreds[s] = array;
      }
    }
  }

  private void setupInitialPartitions()
  {
    mWS = new THashSet<SimpleEquivalenceClass>();
    mWC = new THashSet<ComplexEquivalenceClass>();
    mP = new THashSet<SimpleEquivalenceClass>();
    mStateToClass = new SimpleEquivalenceClass[mNumStates];
    final TIntObjectHashMap<TIntArrayList> tempClassMap =
      new TIntObjectHashMap<TIntArrayList>();
    for (int state = 0; state < mNumStates; state++) {
      if (mTransitionRelation.hasPredecessors(state)) {
        final int m = mTransitionRelation.getMarkingsInt(state);
        TIntArrayList list = tempClassMap.get(m);
        if (list == null) {
          list = new TIntArrayList();
          tempClassMap.put(m, list);
        }
        list.add(state);
      }
    }
    tempClassMap.forEachValue(new TObjectProcedure<TIntArrayList>() {
      public boolean execute(final TIntArrayList list)
      {
        final int[] array = list.toNativeArray();
        final SimpleEquivalenceClass clazz =
          new SimpleEquivalenceClass(array);
        mWS.add(clazz);
        for (final int state : array) {
          mStateToClass[state] = clazz;
        }
        return true;
      }
    });
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

  private void addToW(final SimpleEquivalenceClass sec,
                      final int[] X1,
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


  //#########################################################################
  //# Inner Class EquivalenceClass
  private abstract class EquivalenceClass
  {
    ComplexEquivalenceClass mParent = null;
    TIntIntHashMap[] mInfo = null;
    int size;

    public abstract TIntIntHashMap getInfo(int event);

    public abstract void splitOn();
  }


  //#########################################################################
  //# Inner Class EquivalenceClass
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
      mInfo = new TIntIntHashMap[mNumEvents];
      final List<SimpleEquivalenceClass> classes =
          new ArrayList<SimpleEquivalenceClass>();
      for (int e = 0; e < mNumEvents; e++) {
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
        mInfo = new TIntIntHashMap[mNumEvents];
      }
      TIntIntHashMap info = mInfo[event];
      if (info != null) {
        return info;
      }
      info = new TIntIntHashMap();
      mInfo[event] = info;
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


  //#########################################################################
  //# Inner Class EquivalenceClass
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
      mChild2.mInfo = new TIntIntHashMap[mNumEvents];
      for (int e = 0; e < mNumEvents; e++) {
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
        mInfo = new TIntIntHashMap[mNumEvents];
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


  //#########################################################################
  //# Data Members
  private final int mTau;
  private ObserverProjectionTransitionRelation mTransitionRelation;
  private int mNumStates;
  private int mNumEvents;

  private int[][] mTauPreds;
  private THashSet<SimpleEquivalenceClass> mWS;
  private THashSet<ComplexEquivalenceClass> mWC;
  private THashSet<SimpleEquivalenceClass> mP;
  private SimpleEquivalenceClass[] mStateToClass;
  private TIntObjectHashMap<int[]> mClassMap;

}
