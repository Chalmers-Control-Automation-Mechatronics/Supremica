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

package net.sourceforge.waters.analysis.modular;

import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.annotation.MemStateProxy;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


public class Splitter
{
  private final Set<EventProxy>[] mActive;
  private final AutomatonProxy mAut;
  private final Map<EventProxy, Set<EventProxy>> mPossible;
  private Classes mClasses1;
  private Classes mClasses2;
  private final ProductDESProxyFactory mFactory;
  private EventProxy mHide = null;

  public Splitter(final AutomatonProxy aut, final ProductDESProxyFactory factory)
  {
    mClasses1 = new Classes(aut);
    mClasses2 = mClasses1.clone();
    mActive = mClasses1.getActiveEvents();
    mAut = aut;
    mPossible = mClasses1.getPossible(mActive);
    mFactory = factory;
  }

  public EventProxy hidehide()
  {
    return mHide;
  }

  public AutomatonProxy[] split(final Set<EventProxy> hide)
  {
    final Set<EventProxy> hidden1 = new THashSet<EventProxy>();
    final Set<EventProxy> hidden2 = new THashSet<EventProxy>();
    boolean simplified = false;
    for (final EventProxy e1 : hide) {
      if (!mAut.getEvents().contains(e1)) {continue;}
      final Classes class1 = mClasses1.clone();
      class1.hide(e1);
      for (final EventProxy e2 : mPossible.get(e1)) {
        final Classes class2 = mClasses2.clone();
        class2.hide(e2);
        if (!conflicting(class1, class2)) {
          hidden1.add(e1); hidden2.add(e2); simplified = true;
          mClasses1 = class1; mClasses2 = class2;
        }
        if (e2 != e1) {
          mPossible.get(e2).remove(e1);
        }
      }
      if (simplified) {
        mHide = e1;
        System.out.println("hidden1" + hidden1.size() + "\thidden2" + hidden2.size());
        final AutomatonProxy[] auts = new AutomatonProxy[] {mClasses1.getAutomaton(1),
                                                      mClasses2.getAutomaton(2)};
        return auts;
      }
      mPossible.get(e1).clear();
    }
    return null;
  }

  public AutomatonProxy[] split()
  {
    final Set<EventProxy> hidden1 = new THashSet<EventProxy>();
    final Set<EventProxy> hidden2 = new THashSet<EventProxy>();
    boolean simplified = false;
    final List<EventProxy> events = new ArrayList<EventProxy>(mAut.getEvents());
    Collections.sort(events, new Comparator<EventProxy>()
      {
        @Override
        public int compare(final EventProxy e1, final EventProxy e2)
        {
          return mPossible.get(e1).size() - mPossible.get(e2).size();
        }
      });
    Main:
    for (final EventProxy e1 : events) {
      final Classes class1 = mClasses1.clone();
      class1.hide(e1);
      for (final EventProxy e2 : mPossible.get(e1)) {
        final Classes class2 = mClasses2.clone();
        class2.hide(e2);
        if (!conflicting(class1, class2)) {
          hidden1.add(e1); hidden2.add(e2); simplified = true;
          mClasses1 = class1; mClasses2 = class2; break Main;
        }
        if (e2 != e1) {
          mPossible.get(e2).remove(e1);
        }
      }
      mPossible.get(e1).clear();
    }
    if (!simplified) {System.out.println("not simplified"); return null;}
    for (final EventProxy e : events) {
      if (hidden1.size() < hidden2.size()) {
        if (!hidden1.contains(e)) {
          boolean possible = true;
          for (final EventProxy e2 : hidden2) {
            if (!mPossible.get(e2).contains(e)) {
              possible = false; break;
            }
            if (!mPossible.get(e).contains(e2)) {
              possible = false; break;
            }
          }
          if (possible) {
            final Classes classes = mClasses1.clone();
            classes.hide(e);
            if (!conflicting(classes, mClasses2)) {
              hidden1.add(e); mClasses1 = classes; continue;
            }
          }
        }
        if (!hidden2.contains(e)) {
          boolean possible = true;
          for (final EventProxy e2 : hidden1) {
            if (!mPossible.get(e2).contains(e)) {
              possible = false; break;
            }
            if (!mPossible.get(e).contains(e2)) {
              possible = false; break;
            }
          }
          if (possible) {
            final Classes classes = mClasses2.clone();
            classes.hide(e);
            if (!conflicting(classes, mClasses1)) {
              hidden2.add(e); mClasses2 = classes; continue;
            }
          }
        }
      } else {
        if (!hidden2.contains(e)) {
          boolean possible = true;
          for (final EventProxy e2 : hidden1) {
            if (!mPossible.get(e2).contains(e)) {
              possible = false; break;
            }
            if (!mPossible.get(e).contains(e2)) {
              possible = false; break;
            }
          }
          if (possible) {
            final Classes classes = mClasses2.clone();
            classes.hide(e);
            if (!conflicting(classes, mClasses1)) {
              hidden2.add(e); mClasses2 = classes; continue;
            }
          }
        }
        if (!hidden1.contains(e)) {
          boolean possible = true;
          for (final EventProxy e2 : hidden2) {
            if (!mPossible.get(e2).contains(e)) {
              possible = false; break;
            }
            if (!mPossible.get(e).contains(e2)) {
              possible = false; break;
            }
          }
          if (possible) {
            final Classes classes = mClasses1.clone();
            classes.hide(e);
            if (!conflicting(classes, mClasses2)) {
              hidden1.add(e); mClasses1 = classes; continue;
            }
          }
        }
      }
    }
    System.out.println("hidden1" + hidden1.size() + "\thidden2" + hidden2.size());
    final AutomatonProxy[] auts = new AutomatonProxy[] {mClasses1.getAutomaton(1),
                                                  mClasses2.getAutomaton(2)};
    return auts;
  }



  private boolean conflicting(final Classes c1, final Classes c2)
  {
    final Set<EventProxy>[] active1 = c1.getActiveEvents();
    final Set<EventProxy>[] active2 = c2.getActiveEvents();
    for (int s = 0; s < active1.length; s++) {
      final Set<EventProxy> temp = new THashSet<EventProxy>(active1[s]);
      temp.retainAll(active2[s]);
      if (!mActive[s].containsAll(temp)) {return true;}
    }
    return false;
  }

  private static long longify(final int state, final int event)
  {
    long res = event;
    res <<= 32;
    res |= state;
    return res;
  }

  private static int[] split(long comp)
  {
    final int[] res = new int[2];
    //long temp = comp % Integer.MAX_VALUE;
    //res[0] = temp;
    //temp = comp;
    //temp >>= 32;
    //temp %= Integer.MAX_VALUE;
    //res[1] = temp;
    res[0] = (int)comp;
    comp >>= 32;
    res[1] = (int)comp;
    return res;
  }

  private class Classes
  {
    private final List<Integer>[] mClassToStates;
    private final boolean[] mInitial;
    private final int[][] mSuccs;
    private final TLongHashSet[] mPreds;
    private final EventProxy[] mEvents;
    private final boolean[] mHidden;

    @SuppressWarnings("unchecked")
    private Classes(final Classes o)
    {
      mClassToStates = new List[o.mClassToStates.length];
      mInitial = new boolean[o.mInitial.length];
      mSuccs = new int[o.mSuccs.length][];
      mPreds = new TLongHashSet[o.mPreds.length];
      mEvents = new EventProxy[o.mEvents.length];
      mHidden = new boolean[o.mEvents.length];
      for (int s = 0; s < o.mClassToStates.length; s++) {
        if (o.mClassToStates[s] == null) {continue;}
        mClassToStates[s] = new LinkedList<Integer>(o.mClassToStates[s]);
        mInitial[s] = o.mInitial[s];
        mPreds[s] = new TLongHashSet(o.mPreds[s].toArray());
        mSuccs[s] = new int[o.mEvents.length];
        for (int e = 0; e < mSuccs[s].length; e++) {
          mSuccs[s][e] = o.mSuccs[s][e];
        }
      }
      /* System.out.println("size");
      System.out.println(mEvents.length);
      System.out.println(o.mEvents.length);
      System.out.println(mHidden.length);
      System.out.println(o.mHidden.length); */
      for (int e = 0; e < o.mEvents.length; e++) {
        mEvents[e] = o.mEvents[e];
        mHidden[e] = o.mHidden[e];
      }
    }

    @SuppressWarnings("unchecked")
    public Classes(final AutomatonProxy aut)
    {
      mClassToStates = new LinkedList[aut.getStates().size()];
      mSuccs = new int[aut.getStates().size()][aut.getEvents().size()];
      mPreds = new TLongHashSet[aut.getStates().size()];
      mEvents = new EventProxy[aut.getEvents().size()];
      mInitial = new boolean[aut.getStates().size()];
      mHidden = new boolean[aut.getEvents().size()];
      final TObjectIntHashMap<StateProxy> statetoint =
        new TObjectIntHashMap<StateProxy>();
      final TObjectIntHashMap<EventProxy> eventtoint =
        new TObjectIntHashMap<EventProxy>();
      int i = 0;
      for (int s = 0; s < mSuccs.length; s++) {
        for (int e = 0; e < mSuccs[s].length; e++) {
          mSuccs[s][e] = -1;
        }
      }
      for (final StateProxy state : aut.getStates()) {
        mPreds[i] = new TLongHashSet();
        if (state.isInitial()) {
          mInitial[i] = true;
        }
        mClassToStates[i] = new LinkedList<Integer>();
        mClassToStates[i].add(i);
        statetoint.put(state, i); i++;
      }
      i = 0;
      for (final EventProxy event : aut.getEvents()) {
        mEvents[i] = event;
        eventtoint.put(event, i);
        i++;
      }
      for (final TransitionProxy tran : aut.getTransitions()) {
        final int source = statetoint.get(tran.getSource());
        final int target = statetoint.get(tran.getTarget());
        final int event = eventtoint.get(tran.getEvent());
        mSuccs[source][event] = target;
        mPreds[target].add(longify(source, event));
      }
    }

    public void hide(final EventProxy event)
    {
      final Hide hide = new Hide();
      hide.hide(event);
    }

    public Map<EventProxy, Set<EventProxy>> getPossible(final Set<EventProxy>[] act)
    {
      final Map<EventProxy, Set<EventProxy>> poss =
        new HashMap<EventProxy, Set<EventProxy>>();
      for (int e = 0; e < mEvents.length; e++) {
        final EventProxy event = mEvents[e];
        poss.put(event, new THashSet<EventProxy>(Arrays.asList(mEvents)));
        for (int s = 0; s < mSuccs.length; s++) {
          final int t = mSuccs[s][e];
          if (t == -1) {continue;}
          for (int ei = 0; ei < mEvents.length; ei++) {
            final boolean ev1 = act[s].contains(mEvents[ei]);
            final boolean ev2 = act[t].contains(mEvents[ei]);
            if ((ev1 && !ev2) || (!ev1 && ev2)) {
              poss.get(event).remove(mEvents[ei]);
            }
          }
        }
      }
      return poss;
    }

    @SuppressWarnings("unchecked")
    public Set<EventProxy>[] getActiveEvents()
    {
      final Set<EventProxy>[] res = new Set[mSuccs.length];
      for (int s = 0; s < res.length; s++) {
        if (mSuccs[s] == null) {continue;}
        res[s] = new THashSet<EventProxy>();
        for (int e = 0; e < mSuccs[s].length; e++) {
          if (mHidden[e] || mSuccs[s][e] != -1) {
            res[s].add(mEvents[e]);
          }
        }
        for (final Integer si : mClassToStates[s]) {res[si] = res[s];}
      }
      return res;
    }

    @Override
    public Classes clone()
    {
      return new Classes(this);
    }

    public AutomatonProxy getAutomaton(final int autnum)
    {
      final List<StateProxy> states = new ArrayList<StateProxy>();
      final List<EventProxy> events = new ArrayList<EventProxy>();
      final List<TransitionProxy> transitions = new ArrayList<TransitionProxy>();
      final TIntObjectHashMap<StateProxy> inttostate =
        new TIntObjectHashMap<StateProxy>();
      for (int s = 0; s < mPreds.length; s++) {
        if (mPreds[s] != null) {
          final int snum = mInitial[s] ? 0 : s + 1;
          final StateProxy state = new MemStateProxy(snum);
          states.add(state);
          inttostate.put(s, state);
        }
      }
      for (int e = 0; e < mEvents.length; e++) {
        if (!mHidden[e]) {
          //System.out.println("Event: " + mEvents[e]);
          events.add(mEvents[e]);
        }
      }
      for (int s = 0; s < mSuccs.length; s++) {
        if (mSuccs[s] == null) {continue;}
        final StateProxy source = inttostate.get(s);
        for (int e = 0; e < mSuccs[s].length; e++) {
          if (mHidden[e]) {continue;}
          final int t = mSuccs[s][e];
          if (t == -1) {continue;}
          //System.out.println("tEvent: " + mEvents[e]);
          final EventProxy event = mEvents[e];
          final StateProxy target = inttostate.get(t);
          //System.out.println("target:" + target);
          //System.out.println(t);
          transitions.add(mFactory.createTransitionProxy(source, event, target));
        }
      }
      final AutomatonProxy result = mFactory.createAutomatonProxy(mAut.getName() + ":"+ autnum,
                                                            ComponentKind.PLANT,
                                                            events,
                                                            states, transitions);
      return result;
    }

    private class Hide
    {
      TLongHashSet edges = new TLongHashSet();
      TLongArrayList merge1 = new TLongArrayList();
      TLongArrayList merge2 = new TLongArrayList();

      public void merge(final int s1, final int s2)
      {
        if (s1 == s2) {return;}
        if (mPreds[s1] == null || mPreds[s2] == null) {return;}
        for (int e = 0; e < mEvents.length; e++) {
          if (mEvents[e] == null) {continue;}
          if (mSuccs[s1][e] == mSuccs[s2][e]) {continue;}
          if (mSuccs[s2][e] == -1) {continue;}
          if (mSuccs[s1][e] == -1) {
            mSuccs[s1][e] = mSuccs[s2][e];
            mPreds[mSuccs[s1][e]].add(longify(s1, e));
          }
          final long edge1 = longify(s1, e); final long edge2 = longify(s2, e);
          merge1.add(edge1); merge2.add(edge2);
          edges.add(edge1); edges.add(edge2);
        }
        final long[] arrayedges = mPreds[s2].toArray();
        for (int i = 0; i < arrayedges.length; i++) {
          final int[] edge = split(arrayedges[i]);
          //System.out.println("edge[0]="+ edge[0]  + " edge[1]="+edge[1] );
          if (mSuccs[edge[0]] == null) {continue;}
          mPreds[s1].add(arrayedges[i]);
          mSuccs[edge[0]][edge[1]] = s1;
        }
        mInitial[s1] = mInitial[s1] || mInitial[s2];
        mInitial[s2] = false;
        mClassToStates[s1].addAll(mClassToStates[s2]);
        mClassToStates[s2] = null;
        mPreds[s2] = null;
        for (int s = 0; s < mSuccs.length; s++) {
          if (mSuccs[s] == null) {continue;}
          for (int e = 0; e < mSuccs[s].length; e++) {
            if (mSuccs[s][e] == s2) {
              System.out.println("state:" + s + " event:" + e + " target:" + s2);
            }
          }
        }
      }

      public void hide(final EventProxy event)
      {
        int e = 0;
        for (; e < mEvents.length; e++) {
          if (mEvents[e] == event) {
            break;
          }
        }
        if (e >= mEvents.length) {return;}
        for (int s = 0; s < mSuccs.length; s++) {
          if (mSuccs[s] == null) {continue;}
          if (mSuccs[s][e] != -1 && mSuccs[s][e] != s) {
            merge(s, mSuccs[s][e]);
          }
        }
        while (merge1.size() != 0) {
          final int[] edge1 = split(merge1.removeAt(merge1.size() - 1));
          final int[] edge2 = split(merge2.removeAt(merge2.size() - 1));
          merge(mSuccs[edge1[0]][edge1[1]], mSuccs[edge2[0]][edge2[1]]);
        }
        mHidden[e] = true;
        for (int s = 0; s < mSuccs.length; s++) {
          if (mClassToStates[s] == null) {
            mPreds[s] = null;
            mSuccs[s] = null;
            mClassToStates[s] = null;
            continue;
          }
          if (mSuccs[s][e] == -1) {continue;}
          mPreds[s].remove(longify(s, e));
          mSuccs[s][e] = -1;
        }
      }
    }
  }
}
