//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   BiSimulator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.annotation;

import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntProcedure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.analysis.OverflowException;


/**
 * @author Simon Ware
 */

public class OptimisticBiSimulatorRedundant
{
  private final SimpleEquivalenceClass[] mStateToClass;
  private final THashSet<SimpleEquivalenceClass> mWS;
  private final THashSet<ComplexEquivalenceClass> mWC;
  private final THashSet<SimpleEquivalenceClass> mP;
  private final TIntHashSet[][] mPreds;
  private TIntHashSet[][] mUSuccs;
  private final boolean[] mMarked;
  private int mStates;
  private final int mEventNum;
  private final TIntHashSet[][][] mSuccs;
  private final Set<TIntHashSet>[][] mAnn;

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

  //#########################################################################
  //# Constructor
  @SuppressWarnings("unchecked")
  public OptimisticBiSimulatorRedundant(final TransitionRelation tr)
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
    setup4();
    setup();
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
        final TIntHashSet succs1 = mUSuccs[s][e1];
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
            final TIntHashSet succs2 = mUSuccs[succ][e2];
            if (succs2 == null || succs2.isEmpty()) {continue;}
            if (mSuccs[s][e1][e2] == null) {mSuccs[s][e1][e2] = new TIntHashSet();}
            mSuccs[s][e1][e2].addAll(succs2.toArray());
          }
        }
      }
    }
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
        if (mMarked[sub] && mMarked[sup]) {continue;}
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
    mUSuccs = builtsuccs;
    System.out.println("transitions added: " + TRANSITIONSADDED);
    TIME += System.currentTimeMillis();
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
      if (!marked.isEmpty()) {mWS.add(new SimpleEquivalenceClass(marked.toNativeArray()));}
      if (!notmarked.isEmpty()) {mWS.add(new SimpleEquivalenceClass(notmarked.toNativeArray()));}
    }
    //System.out.println("initial partitions: " + mWS.size());
    //System.out.println("maut:" + mStates);
  }

  public boolean run()
  {
    TIME -= System.currentTimeMillis();
    setupInitialPartitions();
    if (mStates == 1) {return false;}
    while (true) {
      System.out.println("partitioning");
      final int partitions = mP.size();
      while (true) {
        Iterator<? extends EquivalenceClass> it = null;
        if (!mWS.isEmpty()) {it = mWS.iterator();}
        else if (!mWC.isEmpty()) {it = mWC.iterator();}
        else {break;}
        final EquivalenceClass ec = it.next(); it.remove(); ec.splitOn();
      }
      if (partitions == mP.size()) {break;}
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
    //if (mWS.remove(sec)) {
      mWS.add(child1);
      mWS.add(child2);
    /*} else {
      ComplexEquivalenceClass comp = new ComplexEquivalenceClass(child1, child2);
      comp.mInfo = sec.mInfo;
      if (sec.mParent != null) {
        comp.mParent = sec.mParent;
        ComplexEquivalenceClass p = sec.mParent;
        p.mChild1 = p.mChild1 == sec ? comp : p.mChild1;
        p.mChild2 = p.mChild2 == sec ? comp : p.mChild2;
      } else {
        mWC.add(comp);
      }
    }*/
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
    @SuppressWarnings("unchecked")
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
            final TIntArrayList tiX1 = new TIntArrayList();
            final TIntArrayList tiX2 = new TIntArrayList();
            for (int s = 0; s < sec.mStates.length; s++) {
              final int state = sec.mStates[s];
              if (sec.mSplit1.contains(state)) {
                tiX2.add(state);
              } else {
                boolean covered = true;
                if (mSuccs[state][e] == null) {covered = false;}
                if (covered && mTrans.isMarked(mStates[0])) {
                  for (int ei = 0; ei < mTrans.numberOfEvents(); ei++) {
                    if (!mTrans.isMarkingEvent(ei)) {continue;}
                    if (mSuccs[state][e][ei] == null) {covered = false;}
                    break;
                  }
                }
                if (covered) {
                  final Set<TIntHashSet> anns = mTrans.getAnnotations2(mStates[0]);
                  Annotations:
                  for (final TIntHashSet ann1 : anns) {
                    for (final TIntHashSet ann2 : mAnn[state][e]) {
                      if (ann1.containsAll(ann2.toArray())) {continue Annotations;}
                    }
                    covered = false;
                    break;
                  }
                }
                if (covered) {
                  //System.out.println("covered anns");
                  covered = false;
                  final Set<SimpleEquivalenceClass>[] succseq = new Set[mTrans.numberOfEvents()];
                  for (int ei = 0; ei < mTrans.numberOfEvents(); ei++) {
                    final TIntHashSet succs = mSuccs[state][e][ei];
                    if (succs == null) {continue;}
                    succseq[ei] = new THashSet<SimpleEquivalenceClass>();
                    final int[] succsarr = succs.toArray();
                    for (int j = 0; j < succsarr.length; j++) {
                      final int succ = succsarr[j];
                      succseq[ei].add(mStateToClass[succ]);
                    }
                  }
                  Possstates:
                  for (int j = 0; j < mStates.length; j++) {
                    final int posstate = mStates[j];
                    for (int ei = 0; ei < mTrans.numberOfEvents(); ei++) {
                      if (mTrans.isMarkingEvent(ei)) {continue;}
                      final TIntHashSet possstatesuccs = mUSuccs[posstate][ei];
                      if (possstatesuccs == null) {continue;}
                      final Set<SimpleEquivalenceClass> candidatesuccs = succseq[ei];
                      if (candidatesuccs == null) {continue Possstates;}
                      final int[] possarr = possstatesuccs.toArray();
                      for (int k = 0; k < possarr.length; k++) {
                        final int posssucc = possarr[k];
                        if (!candidatesuccs.contains(mStateToClass[posssucc])) {
                          continue Possstates;
                        }
                      }
                    }
                    covered = true; break;
                  }
                }
                if (!covered) {
                  tiX1.add(state);
                } else {
                  System.out.println("could place here");
                  tiX2.add(state);
                }
              }
            }
            if (tiX2.size() != sec.size) {
              final int[] X1 = tiX1.toNativeArray();
              final int[] X2 = tiX2.toNativeArray();
              addToW(sec, X1, X2);
            }
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
            X1 = sec.X1 == null ? null : sec.X1.toNativeArray();
            X2 = sec.X2 == null ? null : sec.X2.toNativeArray();
            X3 = sec.X3 == null ? null : sec.X3.toNativeArray();
            if (X1 == null) {
              X1 = X3;
            } else if (X2 == null) {
              X2 = X3;
            }
            addToW(sec, X1, X2);
          } else if(number == 3) {
            X1 = sec.X1.toNativeArray(); X2 = sec.X2.toNativeArray();
            X3 = sec.X3.toNativeArray(); sec.mSplit = false;
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
        public boolean execute(final int state, final int value) {
          info.put(state, value); return true;
        }
      });
      info2.forEachEntry(new TIntIntProcedure() {
        public boolean execute(final int state, final int value) {
          info.adjustOrPutValue(state, value, value); return true;
        }
      });
      res = info;
      mInfo[event] = res;
      return res;
    }
  }
}
