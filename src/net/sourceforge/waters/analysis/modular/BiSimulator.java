//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.modular;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.annotation.AnnotatedMemStateProxy;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * @author Simon Ware
 */

public class BiSimulator
{
  private final AutomatonProxy mAutomaton;
  private final int mEventNum;
  private final int[][][] mTransitionsPred;
  private final Set<EventProxy>[] mProps;
  private final EventProxy[] mEvents;
  private final boolean[] mIsInitial;
  @SuppressWarnings("unused")
  private final EventProxy mMark;
  private final SimpleEquivalenceClass[] mStateToClass;
  private final THashSet<SimpleEquivalenceClass> mWS;
  private final THashSet<ComplexEquivalenceClass> mWC;
  private final THashSet<SimpleEquivalenceClass> mP;
  private final ProductDESProxyFactory mFactory;

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
    final TIntArrayList[][] temppred =
      new TIntArrayList[automaton.getStates().size()][mEventNum];
    final Collection<TransitionProxy> trans = mAutomaton.getTransitions();
    StateProxy[] states = new StateProxy[automaton.getStates().size()];
    EventProxy[] events = new EventProxy[automaton.getEvents().size()];
    states = automaton.getStates().toArray(states);
    events = automaton.getEvents().toArray(events);
    Arrays.sort(events);
    final TObjectIntHashMap<StateProxy> sti =
      new TObjectIntHashMap<StateProxy>(states.length);
    final TObjectIntHashMap<EventProxy> eti =
      new TObjectIntHashMap<EventProxy>(events.length);
    mProps = new Set[automaton.getStates().size()];
    mIsInitial = new boolean[states.length];
    for (int i = 0; i < states.length; i++) {
      mIsInitial[i] = states[i].isInitial();
    }
    for (int i = 0; i < states.length; i++) {
      sti.put(states[i], i);
      mProps[i] = new HashSet<EventProxy>(states[i].getPropositions());
    }
    for (int i = 0; i < events.length; i++) {eti.put(events[i], i);}
    for (final TransitionProxy tran : trans) {
      final int t = sti.get(tran.getTarget());
      final int e = eti.get(tran.getEvent());
      final int s = sti.get(tran.getSource());
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
          mTransitionsPred[i][j] = temppred[i][j].toArray();
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
    final Map<Collection<EventProxy>, TIntArrayList> map =
      new THashMap<Collection<EventProxy>, TIntArrayList>();
    for (int i = 0; i < mProps.length; i++) {
      TIntArrayList p = map.get(mProps[i]);
      if (p == null) {p = new TIntArrayList(); map.put(mProps[i], p);}
      p.add(i);
    }
    for (final Collection<EventProxy> props : map.keySet()) {
      final TIntArrayList p = map.get(props);
      //System.out.println("props:" + props + "\tp:" +
      //                   Arrays.toString(p.toNativeArray()));
      mWS.add(new SimpleEquivalenceClass(p.toArray()));
    }
    /*System.out.println("partitions:" + mWS.size());
    System.out.println("maut size:" + mAutomaton.getStates().size());
    int i = 0;
    for (SimpleEquivalenceClass sec : mWS) {
      i++;
      System.out.println(i + ": " + sec.mStates.length);
    }*/
  }

  public AutomatonProxy run()
  {
    int size = -1;
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
      if (size == -1) {
        size = mP.size();
        mWS.addAll(mP);
        continue;
      } else if (size != mP.size()) {
        System.out.println("didn't fully partition");
        System.exit(4);
      } else {
        break;
      }
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
    //System.out.println("States Removed:" + (mAutomaton.getStates().size() - mP.size()));
    final THashMap<SimpleEquivalenceClass,StateProxy> classToState =
      new THashMap<SimpleEquivalenceClass,StateProxy>(mP.size());
    final Collection<StateProxy> states = new ArrayList<StateProxy>(mP.size());
    mP.forEach(new TObjectProcedure<SimpleEquivalenceClass>() {
      int state = 0;

      @Override
      public boolean execute(final SimpleEquivalenceClass sec) {
        final Collection<EventProxy> mark = mProps[sec.mStates[0]];
        boolean isInitial = false;
        for (int i = 0; i < sec.mStates.length; i++) {
          if (mIsInitial[sec.mStates[i]]) {isInitial = true; break;}
        }
        final StateProxy st = new AnnotatedMemStateProxy(state, mark, isInitial);
        state++;
        classToState.put(sec, st); states.add(st); return true;
      }
    });
    final Collection<TransitionProxy> trans = new ArrayList<TransitionProxy>();
    final THashSet<StateProxy> preds = new THashSet<StateProxy>();
    mP.forEach(new TObjectProcedure<SimpleEquivalenceClass>() {
      @Override
      public boolean execute(final SimpleEquivalenceClass sec) {
        final StateProxy target = classToState.get(sec);
        if (sec.mStates.length > 1) {
          //System.out.println(Arrays.toString(sec.mStates));
        }
        for (int i = 0; i < mEventNum; i++) {
          final EventProxy event = mEvents[i];
          sec.getInfo(i).forEachEntry(new TIntIntProcedure() {
            @Override
            public boolean execute(final int s, final int v) {
              if (v == 0) {
                System.out.println("zero value transitions");
                return true;
              }
              preds.add(classToState.get(mStateToClass[s])); return true;
            }
          });
          preds.forEach(new TObjectProcedure<StateProxy>() {
            @Override
            public boolean execute(final StateProxy source) {
              trans.add(mFactory.createTransitionProxy(source, event, target));
              return true;
            }
          });
          preds.clear();
        }
        return true;
      }
    });
    //System.out.println("Partitions:" + mP.size());
    //System.out.println("BiTransitions:" + trans.size());
    if (mP.size() == 0) {
      assert(false);
    }
    final AutomatonProxy result = mFactory.createAutomatonProxy(mAutomaton.getName(),
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
        mInfo[e] = new TIntIntHashMap();
        final TIntIntHashMap map = mInfo[e];
        for (int s = 0; s < mStates.length; s++) {
          final int targ = mStates[s];
          final int[] preds = mTransitionsPred[targ][e];
          if (preds == null || preds.length == 0) {continue;}
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
      for (int i = 0; i < mStates.length; i++) {
        final int[] preds = mTransitionsPred[mStates[i]][event];
        if (preds == null || preds.length == 0) { continue;}
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
        /*info.forEachEntry(new TIntIntProcedure() {
          public boolean execute(int state, int value) {
            process.put(state, value);
            return true;
          }
        });*/
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
}
