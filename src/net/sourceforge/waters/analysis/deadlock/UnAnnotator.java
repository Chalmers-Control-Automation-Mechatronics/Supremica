//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.deadlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.annotation.AnnotatedMemStateProxy;
import net.sourceforge.waters.analysis.annotation.TransitionRelation;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;


public class UnAnnotator
{
  private final TransitionRelation mTransitionRelation;
  private final EventProxy mMarked;
  private EventProxy mTau;

  public static int STATESADDED = 0;
  public static int TIME = 0;

  public static void clearStats()
  {
    STATESADDED = 0;
    TIME = 0;
  }

  public static String stats()
  {
    return "UnAnnotateGraph: STATESADDED = " + STATESADDED +
            " TIME = " + TIME;
  }

  public UnAnnotator(final TransitionRelation transitionrelation)
  {
    mTransitionRelation = transitionrelation;
    mMarked = null;
  }


  public AutomatonProxy run(final ProductDESProxyFactory factory)
  {
    //mTransitionRelation.printstuff();
    TIME -= System.currentTimeMillis();
    final boolean containsmarked = mTransitionRelation.getEvents().contains(mMarked);
    final Map<TIntHashSet, TIntArrayList> statesWithAnnotation =
      new THashMap<TIntHashSet, TIntArrayList>();
    final Map<TIntHashSet, TIntArrayList> statesWithActiveEvents =
      new THashMap<TIntHashSet, TIntArrayList>();
    final List<TransitionProxy> newTransitions = new ArrayList<TransitionProxy>();
    final List<StateProxy> nextStates = new ArrayList<StateProxy>();
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      Set<TIntHashSet> annotations = mTransitionRelation.getAnnotation(s);
      if (annotations != null) {
        annotations.remove(mTransitionRelation.getActiveEvents(s));
        annotations = annotations.isEmpty() ? null : annotations;
      }
      if (annotations == null) {
        final TIntHashSet active = mTransitionRelation.getActiveEvents(s);
        TIntArrayList withActiveEvents = statesWithActiveEvents.get(active);
        if (withActiveEvents == null) {
          withActiveEvents = new TIntArrayList();
          statesWithActiveEvents.put(active, withActiveEvents);
        }
        withActiveEvents.add(s);
      } else {
        //System.out.println("has annotation size:" + annotations.size());
        for (final TIntHashSet ann : annotations) {
          if (ann.equals(mTransitionRelation.getActiveEvents(s))) {
            continue;
          }
          TIntArrayList states = statesWithAnnotation.get(ann);
          if (states == null) {
            states = new TIntArrayList();
            statesWithAnnotation.put(ann, states);
          }
          states.add(s);
        }
      }
    }
    final TIntObjectHashMap<TIntArrayList> newStates =
      new TIntObjectHashMap<TIntArrayList>();
    // slightly confusing the source refers to state in new automaton
    // the successor refers to original
    final Collection<EventProxy> markedcol = Collections.singleton(mMarked);
    final Collection<EventProxy> notmarked = Collections.emptySet();
    final List<int[]> newtransitionsI = new ArrayList<int[]>();
    int statenum = 0;
    for (int s = 0; s < mTransitionRelation.numberOfStates(); s++) {
      final TIntArrayList states = new TIntArrayList();
      states.add(statenum);
      newStates.put(s, states);
      for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
        final TIntHashSet succs = mTransitionRelation.getSuccessors(s, e);
        if (succs == null) {continue;}
        final int[] array = succs.toArray();
        for (int ti = 0; ti < array.length; ti++) {
          final int t = array[ti];
          newtransitionsI.add(new int[]{s, e, t});
        }
      }
      Collection<EventProxy> used = mTransitionRelation.isMarked(s) ?
                                    markedcol : notmarked;
      final boolean isInitial = mTransitionRelation.isInitial(s);
      if (!containsmarked) {
        used = notmarked;
      }
      final StateProxy sp = new AnnotatedMemStateProxy(statenum, used, isInitial);
      nextStates.add(sp);
      statenum++;
      assert(statenum == nextStates.size());
    }
    for (final TIntHashSet ann : statesWithAnnotation.keySet()) {
      final TIntHashSet states = new TIntHashSet(statesWithAnnotation.get(ann).toArray());
      final TIntArrayList withActiveEvents = statesWithActiveEvents.get(ann);
      if (withActiveEvents != null) {
        final int[] arr = states.toArray();
        final int[] eventarr = ann.toArray();
        STATES:
        for (int i = 0; i < arr.length; i++) {
          final int state = arr[i];
          WITHACTIVE:
          for (int j = 0; j < withActiveEvents.size(); j++) {
            final int acstate = withActiveEvents.get(j);
            for (int k = 0; k < eventarr.length; k++) {
              final int event = eventarr[k];
              if (mTransitionRelation.isMarkingEvent(event)) {continue;}
              final TIntHashSet succs1 = mTransitionRelation.getSuccessors(state, event);
              final TIntHashSet succs2 = mTransitionRelation.getSuccessors(acstate, event);
              if (!succs1.containsAll(succs2.toArray())) {
                continue WITHACTIVE;
              }
            }
            final TIntArrayList sharedstates = newStates.get(state);
            sharedstates.add(acstate);
            states.remove(state);
            continue STATES;
          }
        }
      }
      while (!states.isEmpty()) {
        final TIntObjectHashMap<TIntArrayList> indexToSuccessor =
          new TIntObjectHashMap<TIntArrayList>();
        TIntArrayList tocheck = new TIntArrayList(states.toArray());
        final TIntIterator it = ann.iterator();
        Collection<EventProxy> used = notmarked;
        while (it.hasNext()) {
          final int event = it.next();
          if (mTransitionRelation.isMarkingEvent(event)) {
            used = markedcol;
            continue;
          }
          for (int its = 0; its < tocheck.size(); its++) {
            final int s = tocheck.get(its);
            final TIntHashSet succs = mTransitionRelation.getSuccessors(s, event);
            if (succs == null) {
              for (int e = 0; e < mTransitionRelation.numberOfEvents(); e++) {
                if (mTransitionRelation.getPredecessors(s, e) == null) {System.out.println("null"); continue;}
                System.out.println(Arrays.toString(mTransitionRelation.getPredecessors(s, e).toArray()));
              }
              System.out.println(mTransitionRelation.isInitial(s));
              System.out.println("state = " + s);
            }
            final int[] array = succs.toArray();
            for (int suci = 0; suci < array.length; suci++) {
              final int suc = array[suci];
              TIntArrayList indexs = indexToSuccessor.get(suc);
              if (indexs == null) {
                indexs = new TIntArrayList();
                indexToSuccessor.put(suc, indexs);
              }
              indexs.add(s);
            }
          }

          TIntArrayList biggestlist = null;
          int bestsuc = -1;
          final TIntObjectIterator<TIntArrayList> it2 = indexToSuccessor.iterator();
          while (it2.hasNext()) {
            it2.advance();
            final TIntArrayList list = it2.value();
            if (biggestlist == null || biggestlist.size() < list.size()) {
              biggestlist = list;
              bestsuc = it2.key();
            }
          }
          if (biggestlist == null) {
            System.out.println("number of things:" + indexToSuccessor.size());
          }
          newtransitionsI.add(new int[]{statenum, event, bestsuc});
          /*System.out.println("statenum:" + statenum);
          System.out.println("numstates:" + mTransitionRelation.numberOfStates());
          System.out.println("event: " + mTransitionRelation.getEvent(event));
          System.out.println("tocheck.size()" + tocheck.size() + "biggestlist.size()" + biggestlist.size());*/
          tocheck = biggestlist;
          for (int i = 0; i < biggestlist.size(); i++) {
            final int state = biggestlist.get(i);
            if (!mTransitionRelation.getSuccessors(state, event).contains(bestsuc)) {
              System.out.println("doesn't contain successor");
              System.exit(2);
            }
          }
          indexToSuccessor.clear();
        }
        final boolean isInitial = false;
        for (int its = tocheck.size() - 1; its >= 0; its--) {
          final int state = tocheck.get(its);
          final TIntArrayList sharedstates = newStates.get(state);
          //isInitial = mTransitionRelation.isInitial(state) ? true : isInitial;
          sharedstates.add(statenum);
          states.remove(state);
        }
        if (!containsmarked) {
          used = notmarked;
        }
        final StateProxy sp = new AnnotatedMemStateProxy(statenum, used, isInitial);
        //System.out.println("marking:" + used);
        nextStates.add(sp);
        statenum++;
        STATESADDED++;
        assert(statenum == nextStates.size());
      }
    }
    for (final int[] t : newtransitionsI) {
      final int s = t[0];
      final int e = t[1];
      final int ot = t[2];
      final StateProxy source = nextStates.get(s);
      final EventProxy event = mTransitionRelation.getEvent(e);
      /*for (int ti = 0; ti < targets.size(); ti++) {
        int ta = targets.get(ti);
        StateProxy target = nextStates.get(ta);
        newTransitions.add(factory.createTransitionProxy(source, event, target));
      }*/
      final StateProxy target = nextStates.get(ot);
      newTransitions.add(factory.createTransitionProxy(source, event, target));
    }
    //mTau = factory.createEventProxy("tau:" + mTransitionRelation.getName(), EventKind.UNCONTROLLABLE);
    for (int i = 0; i < newStates.size(); i++) {
      final StateProxy source = nextStates.get(i);
      final TIntArrayList taus = newStates.get(i);
      for (int j = 0; j < taus.size(); j++) {
        final int state = taus.get(j);
        if (i == state) {continue;}
        final StateProxy target = nextStates.get(state);
        newTransitions.add(factory.createTransitionProxy(source, mTau, target));
      }
    }
    final Collection<EventProxy> eventsset = mTransitionRelation.getEvents();
    eventsset.add(mTau);
    TIME += System.currentTimeMillis();
    final AutomatonProxy aut= factory.createAutomatonProxy(mTransitionRelation.getName(),
                                                     ComponentKind.PLANT,
                                                     eventsset,
                                                     nextStates,
                                                     newTransitions);
    return aut;
  }
}
