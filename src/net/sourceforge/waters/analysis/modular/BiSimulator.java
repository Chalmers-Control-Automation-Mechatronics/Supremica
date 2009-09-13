//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   Minimizer
//###########################################################################
//# $Id: Minimizer.java 4514 2008-11-11 20:26:15Z js173 $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIterator;
import gnu.trove.TIntIntIterator;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.analysis.LightWeightGraph;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntProcedure;
import gnu.trove.TLinkedList;
import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectProcedure;
import gnu.trove.TIntProcedure;
import gnu.trove.THashSet;
import gnu.trove.THashMap;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.analysis.AnnotatedMemStateProxy;

public class BiSimulator
{
  private AutomatonProxy mAutomaton;
  private final int mEventNum;
  private int[][][] mTransitionsPred;
  private final Collection<EventProxy>[] mProps;
  private final EventProxy[] mEvents;
  private final boolean[] mIsInitial;
  private EventProxy mMark;
  private SimpleEquivalenceClass[] mStateToClass;
  private THashSet<SimpleEquivalenceClass> mWS;
  private THashSet<ComplexEquivalenceClass> mWC;
  private THashSet<SimpleEquivalenceClass> mP;
  private ProductDESProxyFactory mFactory;
  
  //#########################################################################
  //# Constructor
  @SuppressWarnings("unchecked")
  public BiSimulator(final AutomatonProxy automaton, final EventProxy mark,
                     final ProductDESProxyFactory factory)
    throws OverflowException
  {
    mAutomaton = automaton;
    mFactory = factory;
    mMark = mark;
    mEventNum = automaton.getEvents().size();
    mTransitionsPred = new int[automaton.getStates().size()][mEventNum][];
    TIntArrayList[][] temppred =
      new TIntArrayList[automaton.getStates().size()][mEventNum];
    Collection<TransitionProxy> trans = mAutomaton.getTransitions();
    StateProxy[] states = new StateProxy[automaton.getStates().size()];
    EventProxy[] events = new EventProxy[automaton.getEvents().size()];
    states = automaton.getStates().toArray(states);
    events = automaton.getEvents().toArray(events);
    Arrays.sort(events);
    TObjectIntHashMap<StateProxy> sti =
      new TObjectIntHashMap<StateProxy>(states.length);
    TObjectIntHashMap<EventProxy> eti =
      new TObjectIntHashMap<EventProxy>(events.length);
    mProps = new Collection[automaton.getStates().size()];
    mIsInitial = new boolean[states.length];
    for (int i = 0; i < states.length; i++) {
      mIsInitial[i] = states[i].isInitial();
    }
    for (int i = 0; i < states.length; i++) {
      sti.put(states[i], i);
      mProps[i] = new THashSet<EventProxy>(states[i].getPropositions());
    }
    for (int i = 0; i < events.length; i++) {eti.put(events[i], i);}
    for (TransitionProxy tran : trans) {
      int t = sti.get(tran.getTarget());
      int e = eti.get(tran.getEvent());
      int s = sti.get(tran.getSource());
      mProps[s].add(tran.getEvent());
      TIntArrayList preds = temppred[t][e];
      if (preds == null) {
        preds = new TIntArrayList();
        temppred[t][e] = preds;
      }
      preds.add(s);
    }
    for (int i = 0; i < temppred.length; i++) {
      for (int j = 0; j < temppred[i].length; j++) {
        if (temppred[i][j] != null) {
          mTransitionsPred[i][j] = temppred[i][j].toNativeArray();
        }
      }
    }
    mEvents = events;
    mWS = new THashSet<SimpleEquivalenceClass>();
    mWC = new THashSet<ComplexEquivalenceClass>();
    mP = new THashSet<SimpleEquivalenceClass>();
    mStateToClass = new SimpleEquivalenceClass[mAutomaton.getStates().size()];
  }
  
  private void setupInitialPartitions()
  {
    mWS.clear();
    mWC.clear();
    mP.clear();
    Map<Collection<EventProxy>, TIntArrayList> map =
      new THashMap<Collection<EventProxy>, TIntArrayList>();
    for (int i = 0; i < mProps.length; i++) {
      TIntArrayList p = map.get(mProps[i]);
      if (p == null) {p = new TIntArrayList(); map.put(mProps[i], p);}
      p.add(i);
    }
    for (Collection<EventProxy> props : map.keySet()) {
      TIntArrayList p = map.get(props);
      //System.out.println("props:" + props + "\tp:" +
      //                   Arrays.toString(p.toNativeArray()));
      mWS.add(new SimpleEquivalenceClass(p.toNativeArray()));
    }
  }

  public AutomatonProxy run()
  {
    setupInitialPartitions();
    int size = -1;
    while (true) {
      System.out.println("partitioning");
      while (true) {
        Iterator<? extends EquivalenceClass> it = null;
        if (!mWS.isEmpty()) {it = mWS.iterator();}
        else if (!mWC.isEmpty()) {it = mWC.iterator();}
        else {break;}
        EquivalenceClass ec = it.next(); it.remove(); ec.splitOn();
      }/*
      if (size == -1) {
        size = mP.size();
        mWS.addAll(mP);
        continue;
      } else if (size != mP.size()) {
        System.out.println("didn't fully partition");
        System.exit(4);
      } else {*/
        break;
      //}
    }
    /*while (true) {
      int size = mP.size();
      mW.addAll(mP);
      while (!mW.isEmpty()) {
        Iterator<EquivalenceClass> it = mW.iterator();
        EquivalenceClass ec = it.next(); it.remove(); ec.splitOn();
      }
      if (size != mP.size()) {
        System.out.println("didn't fully partition");
        System.exit(4);
      } else {
        break;
      }
    }*/
    if (mP.size() > mAutomaton.getStates().size()) {
      System.out.println("WTF?");
      System.exit(4);
    }
    if (mP.size() == mAutomaton.getStates().size()) {
      return mAutomaton;
    }
    final THashMap<SimpleEquivalenceClass,StateProxy> classToState =
      new THashMap<SimpleEquivalenceClass,StateProxy>(mP.size());
    final TIntArrayList marked = new TIntArrayList();
    final Collection<StateProxy> states = new ArrayList<StateProxy>(mP.size());
    mP.forEach(new TObjectProcedure<SimpleEquivalenceClass>() {
      int state = 0;
      
      public boolean execute(SimpleEquivalenceClass sec) {
        Collection<EventProxy> mark = mProps[sec.mStates[0]];
        boolean isInitial = false;
        for (int i = 0; i < sec.mStates.length; i++) {
          if (mIsInitial[sec.mStates[i]]) {isInitial = true; break;}
        }
        StateProxy st = new AnnotatedMemStateProxy(state, mark, isInitial);
        state++;
        classToState.put(sec, st); states.add(st); return true;
      }
    });
    final Collection<TransitionProxy> trans = new ArrayList<TransitionProxy>();
    final THashSet<StateProxy> preds = new THashSet<StateProxy>();
    mP.forEach(new TObjectProcedure<SimpleEquivalenceClass>() {
      public boolean execute(SimpleEquivalenceClass sec) {
        final StateProxy target = classToState.get(sec);
        for (int i = 0; i < mEventNum; i++) {
          final EventProxy event = mEvents[i];
          sec.getInfo(i).forEachEntry(new TIntIntProcedure() {
            public boolean execute(int s, int v) {
              if (v == 0) {
                System.out.println("zero value transitions");
                return true;
              }
              preds.add(classToState.get(mStateToClass[s])); return true;
            }
          });
          preds.forEach(new TObjectProcedure<StateProxy>() {
            public boolean execute(StateProxy source) {
              trans.add(mFactory.createTransitionProxy(source, event, target));
              return true;
            }
          });
          preds.clear();
        }
        return true;
      }
    });
    System.out.println("Partitions:" + mP.size());
    System.out.println("BiTransitions:" + trans.size());
    if (mP.size() == 0) {
      assert(false);
    }
    AutomatonProxy result = mFactory.createAutomatonProxy(mAutomaton.getName(),
                                                          ComponentKind.PLANT,
                                                          mAutomaton.getEvents(),
                                                          states, trans);
    /*if (mAutomaton.getStates().size() < 10) {
      System.out.println("first");
      System.out.println(mAutomaton);
      System.out.println("result");
      System.out.println(result);
      System.exit(1);
    }*/
    return result;
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
  
  private void addToW(SimpleEquivalenceClass sec, int[] X1, int[] X2)
  {
    SimpleEquivalenceClass child1 = new SimpleEquivalenceClass(X1);
    SimpleEquivalenceClass child2 = new SimpleEquivalenceClass(X2);
    mP.remove(sec);
    if (mWS.remove(sec)) {
      mWS.add(child1);
      mWS.add(child2);
    } else {
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
    }
  }
  
  private void addToW(SimpleEquivalenceClass sec, int[] X1, int[] X2, int[] X3)
  {
    if (X2.length < X1.length) {
      int[] t = X1; X1 = X2; X2 = t;
    }
    if (X3.length < X1.length) {
      int[] t = X1; X1 = X3; X3 = t;
    }
    SimpleEquivalenceClass child1 = new SimpleEquivalenceClass(X1);
    SimpleEquivalenceClass child2 = new SimpleEquivalenceClass(X2);
    SimpleEquivalenceClass child3 = new SimpleEquivalenceClass(X3);
    mP.remove(sec);
    ComplexEquivalenceClass X23 = new ComplexEquivalenceClass(child2, child3);
    ComplexEquivalenceClass X123 = new ComplexEquivalenceClass(child1, X23);
    X123.mInfo = sec.mInfo;
    if (sec.mParent != null) {
      X123.mParent = sec.mParent;
      ComplexEquivalenceClass p = sec.mParent;
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
    
    public SimpleEquivalenceClass(int[] states)
    {
      mStates = states;
      size = states.length; //TODO make this into function so less space
      for (int i = 0; i < states.length; i++) {
        mStateToClass[states[i]] = this;
      }
      mP.add(this);
    }
    
    //TODO maybe keep track of what events an equivalence class has no incoming events from
    public void splitOn()
    {
      mInfo = new TIntIntHashMap[mEventNum];
      List<SimpleEquivalenceClass> classes =
        new ArrayList<SimpleEquivalenceClass>();
      for (int e = 0; e < mEventNum; e++) {
        mInfo[e] = new TIntIntHashMap();
        TIntIntHashMap map = mInfo[e];
        for (int s = 0; s < mStates.length; s++) {
          int targ = mStates[s];
          int[] preds = mTransitionsPred[targ][e];
          if (preds == null) { continue;}
          for (int p = 0; p < preds.length; p++) {
            int pred = preds[p];
            SimpleEquivalenceClass ec = mStateToClass[pred];
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
          SimpleEquivalenceClass sec = classes.get(c);
          if (sec.mSplit1.size() != sec.size) {
            int[] X1 = new int[sec.size - sec.mSplit1.size()];
            int[] X2 = new int[sec.mSplit1.size()];
            int x1 = 0, x2 = 0;
            for (int s = 0; s < sec.mStates.length; s++) {
              int state = sec.mStates[s];
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
    
    public TIntIntHashMap getInfo(int event)
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
      for (int i = 0; i < mStates.length; i++) {
        int[] preds = mTransitionsPred[mStates[i]][event];
        if (preds == null) { continue;}
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
    
    public ComplexEquivalenceClass(EquivalenceClass child1,
                                   EquivalenceClass child2)
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
        /*info.forEachEntry(new TIntIntProcedure() {
          public boolean execute(int state, int value) {
            process.put(state, value);
            return true;
          }
        });*/
        final TIntIntHashMap info1 = mChild1.getInfo(e);
        info.forEachEntry(new TIntIntProcedure() {
          public boolean execute(int state, int value) {
            if (value == 0) {
              System.out.println("zero value split");
              info.remove(state); return true;
            }
            int value1 = info1.get(state);
            SimpleEquivalenceClass sec = mStateToClass[state];
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
          SimpleEquivalenceClass sec = classes.get(c);
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
    
    public TIntIntHashMap getInfo(int event)
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
        TIntIntHashMap t = info1; info1 = info2; info2 = t;
      }
      final TIntIntHashMap info = new TIntIntHashMap(info1.size());
      info1.forEachEntry(new TIntIntProcedure() {
        public boolean execute(int state, int value) {
          info.put(state, value); return true;
        }
      });
      info2.forEachEntry(new TIntIntProcedure() {
        public boolean execute(int state, int value) {
          info.adjustOrPutValue(state, value, value); return true;
        }
      });
      res = info;
      mInfo[event] = res;
      return res;
    }
  }
}
