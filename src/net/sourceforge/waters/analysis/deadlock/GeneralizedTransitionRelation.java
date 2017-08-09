//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.annotation.AnnotatedMemStateProxy;
import net.sourceforge.waters.analysis.annotation.AnnotationEvent;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

public class GeneralizedTransitionRelation
{
  //note make certain
  private final TIntHashSet[][] mSuccessors;
  private final TIntHashSet[][] mPredecessors;
  private final TIntHashSet[] mActiveEvents;
  private final boolean[] mMarked;
  private final boolean[] mIsInitial;
  private final EventProxy[] mEvents;
  private final Set<TIntHashSet>[] mAnnotations;
  private final TObjectIntHashMap<EventProxy> mEventToInt;
  private final TObjectIntHashMap<StateProxy> mStateToInt;
  private final List<StateProxy> mStateProxyList;
  private final EventProxy mMarkedEvent;
  private final String mName;
  private final Map<Set<TIntHashSet>, EventProxy> mAnnToEvent;

  private final String ACCEPTING_PROP= ":accepting";
  private final String TAU=":tau";

  public GeneralizedTransitionRelation(final ProductDESProxy des, final net.sourceforge.waters.model.des.AutomatonProxy aut)
  {
    this(des,aut, aut.getEvents());
  }

  @SuppressWarnings("unchecked")
  public GeneralizedTransitionRelation(final ProductDESProxy des, final AutomatonProxy aut,
                            Set<EventProxy> eventsall)
  {

    mMarkedEvent = null;    // No need for this ..; to avoid compile errors.

    eventsall = new TreeSet<EventProxy>(eventsall);
    eventsall.addAll(aut.getEvents());
    final Set<EventProxy> allselflooped = new THashSet<EventProxy>(eventsall);
    allselflooped.removeAll(aut.getEvents());
    mName = aut.getName();
    mEvents= shiftAutEvents(eventsall, des); // adds tau event in first pos.
    final TObjectIntHashMap<EventProxy> eventToInt =
        new TObjectIntHashMap<EventProxy>(mEvents.length);
    for (int i = 0; i < mEvents.length; i++) {
      eventToInt.put(mEvents[i], i);
    }
    mEventToInt = eventToInt;
    mStateToInt = new TObjectIntHashMap<StateProxy>();

    int numstates = 0;
    mSuccessors = new TIntHashSet[aut.getStates().size()][mEvents.length];
    mPredecessors = new TIntHashSet[aut.getStates().size()][mEvents.length];
    mActiveEvents = new TIntHashSet[aut.getStates().size()];
    mAnnotations = new Set[aut.getStates().size()];
    mMarked = new boolean[aut.getStates().size()];
    mIsInitial = new boolean[aut.getStates().size()];
    mStateProxyList = new ArrayList<StateProxy>(aut.getStates());
    Collections.sort(mStateProxyList);
    for (final StateProxy s : mStateProxyList) {
      mStateToInt.put(s, numstates);
    /*  if (s.getPropositions().contains(marked) || !aut.getEvents().contains(marked)) {
        markState(numstates, true);
      }*/
      if (s.isInitial()) {
        makeInitialState(numstates, true);
      }
/*
      if (!getPrevAnnotations(s.getPropositions()).isEmpty())
        mAnnotations[numstates] = getPrevAnnotations(s.getPropositions());*/

      numstates++;
    }
    for (final TransitionProxy tran : aut.getTransitions()) {
      final int s = mStateToInt.get(tran.getSource());
      final int t = mStateToInt.get(tran.getTarget());
      final int e = eventToInt.get(tran.getEvent());
      final TIntHashSet succ = getFromArray(s, e, mSuccessors);
      succ.add(t);
      final TIntHashSet pred = getFromArray(t, e, mPredecessors);
      pred.add(s);
      final TIntHashSet active = getFromArray(s, mActiveEvents);
      active.add(e);
    }
    for (final EventProxy event : allselflooped) {
      final int e = eventToInt.get(event);
      for (int s = 0; s < numberOfStates(); s++) {
        addTransition(s, e, s);
      }
    }
    mAnnToEvent = new THashMap<Set<TIntHashSet>, EventProxy>();
  }

  public void setMarkingToStatesWithOutgoing(final Collection<EventProxy> events)
  {
    final int[] evs = new int[events.size()];
    int i = 0;
    for (final EventProxy e : events) {evs[i] = mEventToInt.get(e); i++;}
    STATES:
    for (int s = 0; s < mSuccessors.length; s++) {
      markState(s, false);
      for (i = 0; i < evs.length; i++) {
        final int e = evs[i];
        if (mSuccessors[s][e] != null && !mSuccessors[s][e].isEmpty()) {
          markState(s, true);
          continue STATES;
        }
      }
    }
  }

  public Set<TIntHashSet> getAnnotation(final int state)
  {
    return mAnnotations[state];
  }

  public void setAnnotation(final int state, final Set<TIntHashSet> annotation)
  {
    mAnnotations[state] = annotation;
    // System.out.println("annotation size:" + annotation.size());
    /*
     * for (TIntHashSet hash : annotation) { int[] array = hash.toArray(); for
     * (int i = 0; i < array.length; i++) { int e = array[i]; TIntHashSet succ =
     * mSuccessors[state][e]; if (mEvents[e] == mMarkedEvent) { if
     * (!mMarked[state]) { System.out.println("should be marked");
     * System.exit(1); } continue; } if (succ == null || succ.isEmpty()) {
     * System.out.println("should have outgoing"); System.exit(1); } } }
     */
  }

  @SuppressWarnings("unused")
  private static Set<Set<EventProxy>> getAnnotations(final Collection<EventProxy> props)
  {
    final Iterator<EventProxy> it = props.iterator();
    while (it.hasNext()) {
      final EventProxy e = it.next();
      if (e instanceof AnnotationEvent) {
        final AnnotationEvent a = (AnnotationEvent)e;
        return a.getAnnotations();
      }
    }
    return null;
  }

  public EventProxy getAnnotationEvent(final Set<TIntHashSet> annotations)
  {
    if (annotations == null || annotations.isEmpty()) {return null;}
    EventProxy event = mAnnToEvent.get(annotations);
    if (event == null) {
      final Set<Set<EventProxy>> res = new THashSet<Set<EventProxy>>();
      for (final TIntHashSet ann : annotations) {
        final Set<EventProxy> set = new THashSet<EventProxy>();
        ann.forEach(new TIntProcedure() {
          @Override
          public boolean execute(final int e) {
            final EventProxy event = mEvents[e];
            assert(event != null);
            set.add(event);
            return true;
          }
        });
        res.add(set);
      }
      event = new AnnotationEvent(res, mAnnToEvent.size() + "");
      mAnnToEvent.put(annotations, event);
    }
    // System.out.println("annotation:" + res);
    return event;
  }

  public int numberOfStates()
  {
    return mSuccessors.length;
  }

  public int numberOfEvents()
  {
    return mEvents.length;
  }

  public AutomatonProxy getAutomaton(final ProductDESProxyFactory factory)
  {
    final Collection<TransitionProxy> trans = new ArrayList<TransitionProxy>();
    final List<StateProxy> states = new ArrayList<StateProxy>();
    final Collection<EventProxy> events = new ArrayList<EventProxy>();
    for (int s = 0; s < mAnnotations.length; s++) {
      final Set<EventProxy> props = new THashSet<EventProxy>();
      final Set<TIntHashSet> anns = getAnnotations2(s); //mAnnotations[s];
      /*if (anns != null) {
        anns.remove(getActiveEvents(s));
        if (anns.isEmpty()) {anns = null;}
      }*/
      final EventProxy annotation = getAnnotationEvent(anns);
      if (annotation != null) {
        props.add(annotation);
      }
      if (isMarked(s) && mMarkedEvent != null) {
        props.add(mMarkedEvent);
      }
      //if (isInitial(s)) {System.out.println(s+":initial");}
      final boolean isInitial = isInitial(s);
      states.add(new AnnotatedMemStateProxy(s, props, isInitial));
    }
    // System.out.println(mAnnToEvent.size());
    for (int s = 0; s < mSuccessors.length; s++) {
      if (!hasPredecessors(s)) {
        continue;
      }
      final StateProxy source = states.get(s);
      for (int e = 0; e < mSuccessors[s].length; e++) {
        final EventProxy event = mEvents[e];
        if (event == mMarkedEvent || event == null) {
          continue;
        }
        final TIntHashSet succs = mSuccessors[s][e];
        if (succs == null) {
          continue;
        }
        succs.forEach(new TIntProcedure() {
          @Override
          public boolean execute(final int succ) {
            final StateProxy target = states.get(succ);
            trans.add(factory.createTransitionProxy(source, event, target));
            return true;
          }
        });
      }
    }
    for (int e = 0; e < mEvents.length; e++) {
      if (mEvents[e] != null) {
        events.add(mEvents[e]);
      }
    }
    final List<StateProxy> tempstates = new ArrayList<StateProxy>();;
    for (int s = 0; s < states.size(); s++) {
      if (hasPredecessors(s)) {
        tempstates.add(states.get(s));
      }
    }
    return factory.createAutomatonProxy(mName, ComponentKind.PLANT, events,
                                        tempstates, trans);
  }

  public boolean isMarked(final int state)
  {
    return mMarked[state];
  }

  public boolean isInitial(final int state)
  {
    return mIsInitial[state];
  }

  public void markState(final int state, final boolean value)
  {
    mMarked[state] = value;
    final TIntHashSet active = getFromArray(state, mActiveEvents);
    if (mEventToInt.containsKey(mMarkedEvent)) {
      if (value) {
        active.add(mEventToInt.get(mMarkedEvent));
      } else {
        active.remove(mEventToInt.get(mMarkedEvent));
      }
    }
  }

  public void makeInitialState(final int state, final boolean value)
  {
    mIsInitial[state] = value;
  }

  public TIntHashSet getActiveEvents(final int state)
  {
    /*
     * if (mActiveEvents[state] != null) { int[] array =
     * mActiveEvents[state].toArray(); for (int i = 0; i < array.length; i++) {
     * int event = array[i]; if (mEvents[event] == mMarkedEvent) { if
     * (!mMarked[state]) { System.out.println("should be marked");
     * System.exit(2); } continue; } TIntHashSet succs =
     * mSuccessors[state][event]; if (succs == null || succs.isEmpty()) {
     * System.out.println("should have outgoing event"); System.exit(2); } } }
     * return new TIntHashSet(mActiveEvents[state].toArray());
     */
    final TIntHashSet ae = new TIntHashSet();
    for (int e = 0; e < mSuccessors[state].length; e++) {
      if (mEvents[e] == mMarkedEvent) {
        if (mMarked[state] && mEvents[e] != null) {
          ae.add(e);
        }
      } else if (mSuccessors[state][e] != null
          && !mSuccessors[state][e].isEmpty()) {
        ae.add(e);
      }
    }
    return ae;
  }

  public void removeOutgoing(final int state, final int event)
  {
    final TIntHashSet succs = mSuccessors[state][event];
    if (succs == null) {
      return;
    }
    final int[] arr = succs.toArray();
    for (int i = 0; i < arr.length; i++) {
      final int suc = arr[i];
      removeTransition(state, event, suc);
    }
  }

  public void removeSharedSuccessors(final int has, final int remove)
  {
    if (isMarked(has)) {markState(remove, false);}
    for (int e = 0; e < mSuccessors[has].length; e++) {
      final TIntHashSet hassuccs = mSuccessors[has][e];
      if (hassuccs == null) {
        continue;
      }
      final int[] succs = hassuccs.toArray();
      for (int i = 0; i < succs.length; i++) {
        removeTransition(remove, e, succs[i]);
      }
    }
  }

  public boolean isSubsetOutgoing(final int sub, final int sup)
  {
    if (isMarked(sub) && !isMarked(sup)) {return false;}
    for (int e = 0; e < mSuccessors[sub].length; e++) {
      final TIntHashSet subsuccs = mSuccessors[sub][e];
      if (subsuccs == null || subsuccs.isEmpty()) {continue;}
      if (mSuccessors[sup][e] == null) {return false;}
      final int[] succs = subsuccs.toArray();
      for (int i = 0; i < succs.length; i++) {
        final int suc = succs[i];
        if (suc != sub) {
          if (!mSuccessors[sup][e].contains(suc)) {return false;}
        } else {
          if (!mSuccessors[sup][e].contains(suc) && !mSuccessors[sup][e].contains(sup)) {return false;}
        }
      }
    }
    return true;
  }

  public void removeSharedPredeccessors(final int has, final int remove)
  {
    if (isInitial(has)) {makeInitialState(remove, false);}
    for (int e = 0; e < mPredecessors[has].length; e++) {
      final TIntHashSet haspreds = mPredecessors[has][e];
      if (haspreds == null) {continue;}
      final int[] preds = haspreds.toArray();
      for (int i = 0; i < preds.length; i++) {
        removeTransition(preds[i], e, remove);
      }
    }
  }

  public void removeEvent(final int event)
  {
    mEvents[event] = null;
    for (int s = 0; s < mSuccessors.length; s++) {
      final TIntHashSet succ = mSuccessors[s][event];
      if (succ == null) {
        continue;
      }
      final int[] intsuccs = succ.toArray();
      for (int i = 0; i < intsuccs.length; i++) {
        final int t = intsuccs[i];
        removeTransition(s, event, t);
      }
    }
  }

  public String getName()
  {
    return mName;
  }

  public EventProxy getEvent(final int event)
  {
    return mEvents[event];
  }

  public void setEvent(final EventProxy event, final int pos)
  {
    mEvents[pos]=event;
  }


  public int getEventInt(final EventProxy event)
  {
    return mEventToInt.get(event);
  }


  public Set<EventProxy> getEvents()
  {
    final Set<EventProxy> events = new THashSet<EventProxy>();
    for (int i = 0; i < mEvents.length; i++) {
      // ignore :accepting prop
      if (mEvents[i] != null && !mEvents[i].getName().equals(ACCEPTING_PROP)) {
        events.add(mEvents[i]);
      }
    }
    return events;
  }

  public void removeAllAnnotations(final int event)
  {
    //System.out.println("remove annotations: " + event);
    for (int s = 0; s < mAnnotations.length; s++) {
      final Set<TIntHashSet> anns = mAnnotations[s];
      if (anns != null) {
        final Iterator<TIntHashSet> it = anns.iterator();
        while (it.hasNext()) {
          final TIntHashSet ann = it.next();
          if (ann.contains(event)) {
            it.remove();
          }
        }
        if (anns.isEmpty()) {
          mAnnotations[s] = null;
        }
      }
    }
  }

  public boolean isMarkingEvent(final int event)
  {
    return mEvents[event] == mMarkedEvent;
  }

  public int eventToInt(final EventProxy event)
  {
    return mEventToInt.get(event);
  }

  public int mergeEvents(final Collection<EventProxy> events)
  {
    final Iterator<EventProxy> it = events.iterator();
    final EventProxy first = it.next();
    final int f = mEventToInt.get(first);
    while (it.hasNext()) {
      final int next = mEventToInt.get(it.next());
      for (int s = 0; s < mSuccessors.length; s++) {
        TIntHashSet toremove = mSuccessors[s][next];
        if (toremove != null) {
          final TIntHashSet tau = getFromArray(s, f, mSuccessors);
          tau.addAll(toremove.toArray());
          mSuccessors[s][next] = null;
        }
        toremove = mPredecessors[s][next];
        if (toremove != null) {
          final TIntHashSet tau = getFromArray(s, f, mPredecessors);
          tau.addAll(toremove.toArray());
          mPredecessors[s][next] = null;
        }
      }
      mEvents[next] = null;
    }
    return f;
  }

  public int mergeEvents(final Collection<EventProxy> events,
                         final ProductDESProxyFactory factory)
  {
    final Iterator<EventProxy> it = events.iterator();
    final EventProxy first = it.next();
    final int f = mEventToInt.get(first);
    while (it.hasNext()) {
      final int next = mEventToInt.get(it.next());
      for (int s = 0; s < mSuccessors.length; s++) {
        TIntHashSet toremove = mSuccessors[s][next];
        if (toremove != null) {
          final TIntHashSet tau = getFromArray(s, f, mSuccessors);
          tau.addAll(toremove.toArray());
          mSuccessors[s][next] = null;
        }
        toremove = mPredecessors[s][next];
        if (toremove != null) {
          final TIntHashSet tau = getFromArray(s, f, mPredecessors);
          tau.addAll(toremove.toArray());
          mPredecessors[s][next] = null;
        }
      }
      mEvents[next] = null;
    }
    mEvents[f] =
        factory.createEventProxy("tau:" + mName, EventKind.UNCONTROLLABLE);
    return f;
  }

  public TIntHashSet[] getAllSuccessors(final int state)
  {
    return mSuccessors[state];
  }

  public TIntHashSet getSuccessors(final int state, final int event)
  {
    return mSuccessors[state][event];
  }

  public TIntHashSet[] getAllPredecessors(final int state)
  {
    return mPredecessors[state];
  }

  public TIntHashSet getPredecessors(final int state, final int event)
  {
    return mPredecessors[state][event];
  }

  public static Set<TIntHashSet> subsets(final Collection<TIntHashSet> from,
                                         final Set<TIntHashSet> to)
  {
    final Set<TIntHashSet> tobeadded = new THashSet<TIntHashSet>();
    outside:
    for (final TIntHashSet ann : from) {
      boolean subset = false;
      final Iterator<TIntHashSet> it = to.iterator();
      while (it.hasNext()) {
        final TIntHashSet ann2 = it.next();
        if (ann2.size() >= ann.size()) {
          // TODO can be optimized so not creating the array everytime
          if (ann2.containsAll(ann.toArray())) {
            subset = true;
            it.remove();
            if (ann2.size() == ann.size()) {
              break;
            }
          }
        } else {
          // if a subset already can't be done again
          if (subset) {
            continue;
          }
          if (ann.containsAll(ann2.toArray())) {
            continue outside;
          }
        }
      }
      tobeadded.add(ann);
    }
    to.addAll(tobeadded);
    return to;
  }

  public boolean hasPredecessors(final int state)
  {
    if (isInitial(state)) {
      return true;
    }
    if (mPredecessors[state] != null) {
      for (int e = 0; e < mPredecessors[state].length; e++) {
        final TIntHashSet preds = mPredecessors[state][e];
        if (preds != null && !preds.isEmpty()) {
          if (!preds.contains(state) || preds.size() > 1) {return true;}
        }
      }
    }
    return false;
  }

  public boolean equivalentIncoming(final int state1, final int state2)
  {
    if (isInitial(state1) != isInitial(state2)) {
      return false;
    }
    for (int e = 0; e < mEvents.length; e++) {
      final TIntHashSet preds1 = mPredecessors[state1][e];
      final TIntHashSet preds2 = mPredecessors[state2][e];
      final boolean empty1 = preds1 == null || preds1.isEmpty();
      final boolean empty2 = preds2 == null || preds2.isEmpty();
      if (empty1 && empty2) {
        continue;
      }
      if (empty1 != empty2) {
        return false;
      }
      if (!preds1.equals(preds2)) {
        return false;
      }
    }
    return true;
  }

  public boolean removeEventFromAnnotations(final int event, final int state)
  {
    Set<TIntHashSet> anns = mAnnotations[state];
    //System.out.println(anns);
    if (anns == null) {return false;}
    anns = new THashSet<TIntHashSet>(anns);
    final Set<TIntHashSet> remmed = new THashSet<TIntHashSet>();
    final Iterator<TIntHashSet> it = anns.iterator();
    while (it.hasNext()) {
      final TIntHashSet ann = it.next();
      if (ann.remove(event)) {
        if (ann.isEmpty()) {
          mAnnotations[state] = new THashSet<TIntHashSet>();
          mAnnotations[state].add(ann); return true;
        }
        it.remove(); remmed.add(ann);
      }
    }
    for (final TIntHashSet ann : remmed) {
      final THashSet<TIntHashSet> set = new THashSet<TIntHashSet>();
      set.add(ann);
      mAnnotations[state] = subsets(anns, set);
    }
    mAnnotations[state] = anns;
    return false;
  }

  public void addAnnotations(final int from, final int to)
  {
    Set<TIntHashSet> fann = mAnnotations[from];
    if (fann == null) {
      fann = new THashSet<TIntHashSet>(1);
      fann.add(getActiveEvents(from));
    }
    Set<TIntHashSet> tann = mAnnotations[to];
    if (tann == null) {
      tann = new THashSet<TIntHashSet>(1);
      tann.add(getActiveEvents(to));
    }
    /*System.out.println(from + "," + to);
    for (TIntHashSet ann : fann) {
      System.out.println("fann: " + Arrays.toString(ann.toArray()));
    }
    for (TIntHashSet ann : tann) {
      System.out.println("tann: " + Arrays.toString(ann.toArray()));
    }*/
    tann = subsets(fann, tann);
    mAnnotations[from] = null;
    mAnnotations[to] = tann;
  }

  public void addAnnotations2(final int from, final int to)
  {
    Set<TIntHashSet> fann = mAnnotations[from];
    if (fann == null) {
      fann = new THashSet<TIntHashSet>(1);
      fann.add(getActiveEvents(from));
    }
    Set<TIntHashSet> tann = mAnnotations[to];
    if (tann == null) {
      tann = new THashSet<TIntHashSet>(1);
      tann.add(getActiveEvents(to));
    }
    tann = subsets(fann, tann);
    mAnnotations[to] = tann;
  }

  private TIntHashSet getFromArray(final int i, final TIntHashSet[] array)
  {
    TIntHashSet intset = array[i];
    if (intset == null) {
      intset = new TIntHashSet();
      array[i] = intset;
    }
    return intset;
  }

  private TIntHashSet getFromArray(final int i, final int j, final TIntHashSet[][] array)
  {
    TIntHashSet intset = array[i][j];
    if (intset == null) {
      intset = new TIntHashSet();
      array[i][j] = intset;
    }
    return intset;
  }

  public void moveAllSuccessors(final int from, final int to)
  {
    if (from == to) {
      return;
    }
    markState(to, mMarked[to] || mMarked[from]);
    markState(from, false);
    for (int e = 0; e < mEvents.length; e++) {
      final TIntHashSet succs = mSuccessors[from][e];
      if (succs == null) {
        continue;
      }
      final int[] arsuccs = succs.toArray();
      for (int i = 0; i < arsuccs.length; i++) {
        final int succ = arsuccs[i];
        removeTransition(from, e, succ);
        addTransition(to, e, succ);
      }
    }
  }

  public void addAllSuccessors(final int from, final int to)
  {
    if (from == to) {
      return;
    }
    markState(to, mMarked[to] || mMarked[from]);
    for (int e = 0; e < mEvents.length; e++) {
      final TIntHashSet succs = mSuccessors[from][e];
      if (succs == null) {
        continue;
      }
      final int[] arsuccs = succs.toArray();
      for (int i = 0; i < arsuccs.length; i++) {
        final int succ = arsuccs[i];
        addTransition(to, e, succ);
      }
    }
  }

  public void moveAllPredeccessors(final int from, final int to)
  {
    if (from == to) {
      return;
    }
    makeInitialState(to, mIsInitial[to] || mIsInitial[from]);
    makeInitialState(from, false);
    for (int e = 0; e < mEvents.length; e++) {
      final TIntHashSet preds = mPredecessors[from][e];
      if (preds != null) {
        final int[] arpreds = preds.toArray();
        for (int i = 0; i < arpreds.length; i++) {
          final int pred = arpreds[i];
          removeTransition(pred, e, from);
          addTransition(pred, e, to);
        }
      }
    }
  }

  public void addAllPredeccessors(final int from, final int to)
  {
    if (from == to) {
      return;
    }
    makeInitialState(to, mIsInitial[to] || mIsInitial[from]);
    for (int e = 0; e < mEvents.length; e++) {
      final TIntHashSet preds = mPredecessors[from][e];
      if (preds != null) {
        final int[] arpreds = preds.toArray();
        for (int i = 0; i < arpreds.length; i++) {
          final int pred = arpreds[i];
          addTransition(pred, e, to);
        }
      }
    }
  }

  public void removeAllTransitionsWithEvent(final int e)
  {
    for (int s = 0; s < numberOfStates(); s++) {
      final TIntHashSet preds = mPredecessors[s][e];
      if (preds != null) {
        final int[] arpreds = preds.toArray();
        for (int i = 0; i < arpreds.length; i++) {
          final int pred = arpreds[i];
          removeTransition(pred, e, s);
        }
      }
    }
  }

  public void removeAllSelfLoops(final int e)
  {
    for (int s = 0; s < mSuccessors.length; s++) {
      final TIntHashSet succs = mSuccessors[s][e];
      if (succs == null) {
        continue;
      }
      if (succs.contains(s)) {
        removeTransition(s, e, s);
      }
    }
  }

  public void removeTransition(final int s, final int e, final int t)
  {
    final TIntHashSet succ = getFromArray(s, e, mSuccessors);
    final TIntHashSet pred = getFromArray(t, e, mPredecessors);
    succ.remove(t);
    pred.remove(s);
    if (succ.isEmpty()) {
      mSuccessors[s][e] = null;
      final TIntHashSet active = getFromArray(s, mActiveEvents);
      active.remove(e);
    }
    if (pred.isEmpty()) {
      mPredecessors[t][e] = null;
    }
    /*if (!hasPredecessors(t)) {
      removeAllOutgoing(t);
    }*/
  }


  public void removeAllUnreachable()
  {
    final TIntHashSet tobecheckedset = new TIntHashSet();
    final TIntArrayList tobechecked = new TIntArrayList();
    for (int s = 0; s < numberOfStates(); s++) {
      if (tobecheckedset.add(s)) {tobechecked.add(s);}
    }
    while (!tobechecked.isEmpty()) {
      final int state = tobechecked.removeAt(tobechecked.size() - 1);
      if (!hasPredecessors(state)) {
        final int[] succs = removeAllOutgoing(state).toArray();
        for (int i = 0; i < succs.length; i++) {
          final int succ = succs[i];
          if (tobecheckedset.add(succ)) {tobechecked.add(succ);}
        }
      }
      tobecheckedset.remove(state);
    }
  }

  public TIntHashSet removeAllOutgoing(final int s)
  {
    markState(s, false);
    mAnnotations[s] = null;
    final TIntHashSet succsset = new TIntHashSet();
    for (int e = 0;  e < mSuccessors[s].length; e++) {
      final TIntHashSet succs = mSuccessors[s][e];
      if (succs == null) {
        continue;
      }
      final int[] arsuccs = succs.toArray();
      for (int i = 0; i < arsuccs.length; i++) {
        final int succ = arsuccs[i];
        removeTransition(s, e, succ);
        succsset.add(succ);
      }
    }
    return succsset;
  }

  public void removeAllIncoming(final int s)
  {
    makeInitialState(s, false);
    for (int e = 0;  e < mPredecessors[s].length; e++) {
      final TIntHashSet preds = mPredecessors[s][e];
      if (preds == null) {continue;}
      final int[] arpreds = preds.toArray();
      for (int i = 0; i < arpreds.length; i++) {
        final int pred = arpreds[i];
        removeTransition(pred, e, s);
      }
    }
  }

  public boolean addTransition(final int s, final int e, final int t)
  {
    final TIntHashSet succ = getFromArray(s, e, mSuccessors);
    final TIntHashSet pred = getFromArray(t, e, mPredecessors);
    final boolean result = succ.add(t);
    pred.add(s);
    final TIntHashSet active = getFromArray(s, mActiveEvents);
    active.add(e);
    return result;
  }

  public void mergewithannotations(final int[] stuff)
  {
    final int to = stuff[0];
    for (int i = 1; i < stuff.length; i++) {
      final int from = stuff[i];
      addAnnotations(from, to);
      moveAllSuccessors(from, to);
      moveAllPredeccessors(from, to);
    }
  }

  public void merge(final int[] stuff)
  {
    final int to = stuff[0];
    for (int i = 1; i < stuff.length; i++) {
      final int from = stuff[i];
      moveAllSuccessors(from, to);
      moveAllPredeccessors(from, to);
    }
  }

  /**
   * Returns a collection of all the events which are only ever self looped in
   * this automaton.
   *
   * @return Collection of self looped events.
   */
  public Collection<EventProxy> getAllSelfLoops()
  {
    final Collection<EventProxy> selfs = new ArrayList<EventProxy>();
    SELFLOOPS:
    for (int e = 0; e < mEvents.length; e++) {
      if (mEvents[e] == null || mEvents[e].equals(mMarkedEvent)) {continue;}
      for (int s = 0; s < mSuccessors.length; s++) {
        if (!hasPredecessors(s)) {
          continue;
        }
        final TIntHashSet succs = mSuccessors[s][e];
        if (succs == null || succs.isEmpty()) {
          continue;
        }
        if (succs.size() > 1 || !succs.contains(s)) {
          continue SELFLOOPS;
        }
      }
      selfs.add(mEvents[e]);
    }
    return selfs;
  }

  public Collection<EventProxy> getAllwaysEnabled()
  {
    final Collection<EventProxy> selfs = new ArrayList<EventProxy>();
    ENABLED:
    for (int e = 0; e < mEvents.length; e++) {
      if (mEvents[e] == null) {continue;}
      for (int s = 0; s < mSuccessors.length; s++) {
        if (!hasPredecessors(s)) {
          continue;
        }
        final Set<TIntHashSet> annotations = getAnnotations2(s);
        for (final TIntHashSet ann : annotations) {
          if (!ann.contains(e)) {
            continue ENABLED;
          }
        }
      }
      selfs.add(mEvents[e]);
    }
    //System.out.println(selfs);
    return selfs;
  }

  public Set<TIntHashSet> getAnnotations2(final int state)
  {
    return mAnnotations[state] == null ? Collections
        .singleton(getActiveEvents(state)) : mAnnotations[state];
  }

  public void makeObservationEquivalent(final int tau)
  {
    //TIntHashSet[] reachable = statesreachablewithtau(tau);
    for (int s = 0; s < numberOfStates(); s++) {
      mAnnotations[s] = null;
      saturatetaus(s, tau);
    }
    for (int s = 0; s < numberOfStates(); s++) {
      mAnnotations[s] = null;
      saturatesigma(s, tau);
    }
    /*for (int s = 0; s < numberOfStates(); s++) {
      mAnnotations[s] = null;
      addAllPossibleSuccessors(s, tau);
    }*/
    /*for (int s = 0; s < numberOfStates(); s++) {
      TIntHashSet tausucc = mSuccessors[s][tau];
      if (tausucc == null) {continue;}
      int[] tausuccarray = tausucc.toArray();
      for (int i = 0; i < tausuccarray.length; i++) {
        int succ = tausuccarray[i];
        removeTransition(s, tau, succ);
      }
    }*/
    /*for (int s = 0; s < numberOfStates(); s++) {
      int tausize = 0;
      while (true) {
        TIntHashSet taus = getSuccessors(s, tau);
        if (taus == null) {break;}
        if (taus.size() == tausize) {break;}
        tausize = taus.size();
        int[] tausarray = taus.toArray();
        for (int i = 0; i < tausarray.length; i++) {
          int tausucc = tausarray[i];
          if (tausucc != s) {
            addAllPredeccessors(s, tausucc);
            addAllSuccessors(tausucc, s);
          }
          //removeTransition(s, tau, tausucc);
        }
      }
    }*/
  }

  public void addAllTauReachable(final TIntHashSet[][] transitionRelation,
                                 final int tau, final int state, final int sourcestate,
                                 final boolean succbool, final TIntHashSet set)
  {
    //if (set.contains(state)) {return;}
    set.add(state);
    final TIntHashSet tausuccs = transitionRelation[state][tau];
    if (tausuccs == null) {return;}
    final int[] tauarray = tausuccs.toArray();
    for (int i = 0; i < tauarray.length; i++) {
      final int succ = tauarray[i];
      if (succ == state) {continue;}
      addAllTauReachable(transitionRelation, tau, succ, sourcestate, succbool, set);
    }
    for (int e = 0; e < numberOfEvents(); e++) {
      //if (e != tau) {continue;}
      final TIntHashSet succs = transitionRelation[state][e];
      if (succs == null) {continue;}
      final int[] succsarray = succs.toArray();
      for (int i = 0; i < succsarray.length; i++) {
        final int succ = succsarray[i];
        if (succbool) {
          addTransition(sourcestate, e, succ);
        } else {
          addTransition(succ, e, sourcestate);
        }
      }
    }
    if (succbool) {
      if (isMarked(state)) {
        markState(sourcestate, true);
      }
    }/* else {
      if (isInitial(state)) {
        makeInitialState(sourcestate, true);
      }
    }*/
  }

  public void removeAnnotations(final int state, final Set<TIntHashSet> remanns)
  {
    mAnnotations[state].removeAll(remanns);
    if (mAnnotations[state].isEmpty()) {mAnnotations[state] = null;}
  }

  public void saturatetaus(final int state, final int tau)
  {
    final TIntHashSet taureachable = new TIntHashSet();
    final TIntArrayList tobevisited = new TIntArrayList();
    taureachable.add(state); tobevisited.add(state);
    while (!tobevisited.isEmpty()) {
      final int visit = tobevisited.removeAt(tobevisited.size() - 1);
      addTransition(state, tau, visit);
      final TIntHashSet taus = getSuccessors(visit, tau);
      if (taus == null) {continue;}
      final int[] tausarr = taus.toArray();
      for (int i = 0; i < tausarr.length; i++) {
        final int tausucc = tausarr[i];
        if (isInitial(state)) {makeInitialState(tausucc, true);}
        if (taureachable.add(tausucc)) {tobevisited.add(tausucc);}
      }
    }
  }

  public void saturatesigma(final int state, final int tau)
  {
    final TIntHashSet taus = getSuccessors(state, tau);
    if (taus != null) {
      final int[] tausarr = taus.toArray();
      for (int i = 0; i < tausarr.length; i++) {
        final int tausucc = tausarr[i];
        addAllSuccessors(tausucc, state);
      }
    }
    for (int e = 0; e < numberOfEvents(); e++) {
      if (e == tau) {continue;}
      if (isMarkingEvent(e)) {continue;}
      final TIntHashSet succs = getSuccessors(state, e);
      if (succs == null) {continue;}
      final int[] succsarr = succs.toArray();
      for (int i = 0; i < succsarr.length; i++) {
        final int succ = succsarr[i];
        final TIntHashSet tausuccs = getSuccessors(succ, tau);
        if (tausuccs == null)  {continue;}
        final int[] tausuccsarr = tausuccs.toArray();
        for (int ti = 0; ti < tausuccsarr.length; ti++) {
          final int tausucc = tausuccsarr[ti];
          addTransition(state, e, tausucc);
        }
      }
    }
  }

  public void addAllPossibleSuccessors(final int s, final int tau)
  {
    addAllTauReachable(mSuccessors, tau, s, s, true, new TIntHashSet());
    addAllTauReachable(mPredecessors, tau, s, s, false, new TIntHashSet());
    addTransition(s, tau, s);
  }


  public void printstuff()
  {
    System.out.println("marking: " + mEventToInt.get(mMarkedEvent));
    for (int s = 0; s < mPredecessors.length; s++) {
      System.out.print(s + ": " + Arrays.toString(getActiveEvents(s).toArray()));
      System.out.print("ANNS:" + getAnnotations2(s).size());
      System.out.println();
      for (final TIntHashSet ann : getAnnotations2(s)) {
        System.out.print(Arrays.toString(ann.toArray()) + ", ");
      }
      System.out.println();
    }
  }

  public int unreachableStates()
  {
    int num = 0;
    STATES: for (int s = 0; s < mPredecessors.length; s++) {
      if (mIsInitial[s]) {
        continue;
      }
      for (int e = 0; e < mPredecessors[s].length; e++) {
        if (mPredecessors[s][e] != null) {
          if (!mPredecessors[s][e].isEmpty()) {
            continue STATES;
          }
        }
      }
      num++;
    }
    return num;
  }

  public EventProxy getTau(final ProductDESProxy des) {

    final Set<EventProxy> desEvents = des.getEvents();
    for (final EventProxy ep : desEvents) {
      if (ep.getName().equals(TAU)) {
          return ep;
      }
    }
    return null;
  }

  public Boolean isTau(final EventProxy event) {
    return event.getName().equals(TAU);
  }


  public EventProxy[] shiftAutEvents(final Set<EventProxy> eventSet,
                                     final ProductDESProxy des)
  {
    final List<EventProxy> eventslist = new ArrayList<EventProxy>();
    if(getTau(des) != null)
      eventslist.add(getTau(des));
    for (final EventProxy ep : eventSet) {
      if (!isTau(ep) && !ep.getName().equals(ACCEPTING_PROP)) {
        //eventsArr[pos] = ep;
        eventslist.add(ep);
      }
    }
    return eventslist.toArray(new EventProxy[eventslist.size()]);
  }

  private Set<TIntHashSet> getPrevAnnotations(final Collection<EventProxy> props)
  {
    final Set<TIntHashSet> annints = new HashSet<TIntHashSet>();
    final Iterator<EventProxy> it = props.iterator();
    while (it.hasNext()) {
      final TIntHashSet annint = new TIntHashSet();
      final EventProxy e = it.next();
      if (!e.getName().equals(ACCEPTING_PROP)) {
        final String[] tokens = e.getName().split(":");
        int propLength = tokens.length - 1;
        for (int i = 0; i < mEvents.length && propLength > 0; i++) {
          if (Arrays.asList(tokens).contains(mEvents[i].getName())
              && mEvents[i].getKind() != EventKind.PROPOSITION) {
            annint.add(mEventToInt.get(mEvents[i]));
            propLength--;
          }
        }
        annints.add(annint);
      }
    }
    return annints;
  }

  public void annotateWithActiveEvents() {
    final Annotator annotatedAutomaton= new Annotator(this);
    annotatedAutomaton.run();
  }

  public AutomatonProxy unannotate(final ProductDESProxy des, final ProductDESProxyFactory factory){
    final UnAnnotator ua = new UnAnnotator(this);
    final AutomatonProxy aut = ua.run(factory, des);
    return aut;
  }

  public void annotateWithProps() {
    for(final StateProxy s : mStateProxyList) {
      getPrevAnnotations(s.getPropositions());
    }
  }

  // Setters
  public void setSuccessors(final int succ, final int oldEvent, final int newEvent) {
    //TODO check if values within the length limit
    mSuccessors[succ][oldEvent]= mSuccessors[succ][newEvent];
  }

  public void setPredecessors(final int pred, final int oldEvent, final int newEvent) {
    //TODO check if values within the length limit
    mPredecessors[pred][oldEvent]= mPredecessors[pred][newEvent];
  }

  public void setActiveEvents(final int oldEvent, final int newEvent) {
    //TODO check if values within the length limit
    mActiveEvents[oldEvent]= mActiveEvents[newEvent];
  }

  // Getters
  public List<StateProxy> getStateProxyList(){
    return mStateProxyList;
  }
}
