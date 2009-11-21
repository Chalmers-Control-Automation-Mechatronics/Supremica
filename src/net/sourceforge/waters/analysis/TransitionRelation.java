//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   TransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis;

import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntProcedure;
import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class TransitionRelation
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
  private final EventProxy mMarkedEvent;
  private final String mName;
  private final Map<Set<Set<EventProxy>>, EventProxy> mAnnToEvent;

  public TransitionRelation(final AutomatonProxy aut, final EventProxy marked)
  {
    this(aut, marked, aut.getEvents());
  }

  @SuppressWarnings("unchecked")
  public TransitionRelation(final AutomatonProxy aut, final EventProxy marked,
                            Set<EventProxy> eventsall)
  {
    eventsall = new THashSet<EventProxy>(eventsall);
    eventsall.addAll(aut.getEvents());
    final Set<EventProxy> allselflooped = new THashSet<EventProxy>(eventsall);
    allselflooped.removeAll(aut.getEvents());
    allselflooped.remove(marked);
    mName = aut.getName();
    mMarkedEvent = marked;
    final EventProxy[] events = new EventProxy[aut.getEvents().size()];
    mEvents = eventsall.toArray(events);
    final TObjectIntHashMap<EventProxy> eventToInt =
      new TObjectIntHashMap<EventProxy>(mEvents.length);
    for (int i = 0; i < mEvents.length; i++) {
      eventToInt.put(mEvents[i], i);
    }
    mEventToInt = eventToInt;
    final TObjectIntHashMap<StateProxy> stateToInt =
      new TObjectIntHashMap<StateProxy>();
    int numstates = 0;
    mSuccessors = new TIntHashSet[aut.getStates().size()][mEvents.length];
    mPredecessors = new TIntHashSet[aut.getStates().size()][mEvents.length];
    mActiveEvents = new TIntHashSet[aut.getStates().size()];
    mAnnotations = new Set[aut.getStates().size()];
    mMarked = new boolean[aut.getStates().size()];
    mIsInitial = new boolean[aut.getStates().size()];
    for (final StateProxy s : aut.getStates()) {
      stateToInt.put(s, numstates);
      if (s.getPropositions().contains(marked) ||
	  !aut.getEvents().contains(marked)) {
        markState(numstates, true);
      }
      if (s.isInitial()) {
        makeInitialState(numstates, true);
      }
      final Set<Set<EventProxy>> anns = getAnnotations(s.getPropositions());
      //System.out.println("build annotation:" + anns);
      if (anns != null) {
        final Set<TIntHashSet> annints = new HashSet<TIntHashSet>(anns.size());
        for (final Set<EventProxy> ann : anns) {
          final TIntHashSet annint = new TIntHashSet(ann.size());
          for (final EventProxy event : ann) {
            annint.add(eventToInt.get(event));
          }
          annints.add(annint);
        }
        mAnnotations[numstates] = annints;
      }
      // TODO work out annotations
      numstates++;
    }
    for (final TransitionProxy tran : aut.getTransitions()) {
      final int s = stateToInt.get(tran.getSource());
      final int t = stateToInt.get(tran.getTarget());
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
    mAnnToEvent = new THashMap<Set<Set<EventProxy>>, EventProxy>();
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
          markState(s, true); continue STATES;
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
    //System.out.println("annotation size:" + annotation.size());
    /*for (TIntHashSet hash : annotation) {
      int[] array = hash.toArray();
      for (int i = 0; i < array.length; i++) {
        int e = array[i];
        TIntHashSet succ = mSuccessors[state][e];
        if (mEvents[e] == mMarkedEvent) {
          if (!mMarked[state]) {
            System.out.println("should be marked");
            System.exit(1);
          }
          continue;
        }
        if (succ == null || succ.isEmpty()) {
          System.out.println("should have outgoing");
          System.exit(1);
        }
      }
    }*/
  }

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

  public EventProxy getAnnotationEvent(final Set<TIntHashSet> annotations) {
    if (annotations == null || annotations.isEmpty()) {return null;}
    final Set<Set<EventProxy>> res = new THashSet<Set<EventProxy>>();
    for (final TIntHashSet ann : annotations) {
      final Set<EventProxy> set = new THashSet<EventProxy>();
      ann.forEach(new TIntProcedure() {
        public boolean execute(final int e) {
          final EventProxy event = mEvents[e];
          assert(event != null);
          set.add(event);
          return true;
        }
      });
      res.add(set);
    }
    EventProxy event = mAnnToEvent.get(res);
    if (event == null) {
      event = new AnnotationEvent(res, mAnnToEvent.size() + "");
      mAnnToEvent.put(res, event);
    }
    //System.out.println("annotation:" + res);
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
      final Set<TIntHashSet> anns = mAnnotations[s];
      if (anns != null) {
        anns.remove(getActiveEvents(s));
      }
      final EventProxy annotation = getAnnotationEvent(anns);
      if (annotation != null) {
        props.add(annotation);
      }
      if (isMarked(s) && mMarkedEvent != null) {
        props.add(mMarkedEvent);
      }
      final boolean isInitial = isInitial(s);
      states.add(new AnnotatedMemStateProxy(s, props, isInitial));
    }
    //System.out.println(mAnnToEvent.size());
    for (int s = 0; s < mSuccessors.length; s++) {
      if (!hasPredecessors(s)) {continue;}
      final StateProxy source = states.get(s);
      for (int e = 0; e < mSuccessors[s].length; e++) {
        final EventProxy event = mEvents[e];
        if (event == mMarkedEvent || event == null) {continue;}
        final TIntHashSet succs = mSuccessors[s][e];
        if (succs == null) {continue;}
        succs.forEach(new TIntProcedure() {
          public boolean execute(final int succ) {
            final StateProxy target = states.get(succ);
            trans.add(factory.createTransitionProxy(source, event, target));
            return true;
          }
        });
      }
    }
    for (int e = 0; e < mEvents.length; e++) {
      if (mEvents[e] != null) {events.add(mEvents[e]);}
    }
    final List<StateProxy> tempstates = new ArrayList<StateProxy>();;
    for (int s = 0; s < states.size(); s++) {
      if (hasPredecessors(s)) {
        tempstates.add(states.get(s));
      }
    }
    return factory.createAutomatonProxy(mName, ComponentKind.PLANT,
                                        events, tempstates, trans);
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
    /*if (mActiveEvents[state] != null) {
      int[] array = mActiveEvents[state].toArray();
      for (int i = 0; i < array.length; i++) {
        int event = array[i];
        if (mEvents[event] == mMarkedEvent) {
          if (!mMarked[state]) {
            System.out.println("should be marked");
            System.exit(2);
          }
          continue;
        }
        TIntHashSet succs = mSuccessors[state][event];
        if (succs == null || succs.isEmpty()) {
          System.out.println("should have outgoing event");
          System.exit(2);
        }
      }
    }
    return new TIntHashSet(mActiveEvents[state].toArray());*/
    final TIntHashSet ae = new TIntHashSet();
    for (int e = 0; e < mSuccessors[state].length; e++) {
      if (mEvents[e] == mMarkedEvent) {
        if (mMarked[state]) {
          ae.add(e);
        }
      } else if (mSuccessors[state][e] != null && !mSuccessors[state][e].isEmpty()) {
        ae.add(e);
      }
    }
    return ae;
  }

  public void removeOutgoing(final int state, final int event)
  {
    final TIntHashSet succs = mSuccessors[state][event];
    if (succs == null) {return;}
    final int[] arr = succs.toArray();
    for (int i = 0; i < arr.length; i++) {
      final int suc = arr[i];
      removeTransition(state, event, suc);
    }
  }

  public void removeSharedSuccessors(final int has, final int remove)
  {
    for (int e = 0; e < mSuccessors[has].length; e++) {
      final TIntHashSet hassuccs = mSuccessors[has][e];
      if (hassuccs == null) {continue;}
      final int[] succs = hassuccs.toArray();
      for (int i = 0; i < succs.length; i++) {
        removeTransition(remove, e, succs[i]);
      }
    }
  }

  public void removeEvent(final int event)
  {
    mEvents[event] = null;
    for (int s = 0; s < mSuccessors.length; s++) {
      final TIntHashSet succ = mSuccessors[s][event];
      if (succ == null) {continue;}
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

  public int getEventInt(final EventProxy event)
  {
    return mEventToInt.get(event);
  }

  public Set<EventProxy> getEvents()
  {
    final Set<EventProxy> events = new HashSet<EventProxy>();
    for (int i = 0; i < mEvents.length; i++) {
      if (mEvents[i] != null) {
        events.add(mEvents[i]);
      }
    }
    return events;
  }

  public void removeAllIncoming(final int s)
  {
    markState(s, false);
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

  public void removeAllAnnotations(final int event)
  {
    System.out.println("remove annotations: " + event);
    for (int s = 0; s < mAnnotations.length; s++) {
      final Set<TIntHashSet> anns = mAnnotations[s];
      if (anns != null) {
        final Iterator<TIntHashSet> it = anns.iterator();
        while (it.hasNext()) {
          final TIntHashSet ann = it.next();
          System.out.println(Arrays.toString(ann.toArray()));
          if (ann.contains(event)) {
            System.out.println("removed");
            it.remove();
          }
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
    mEvents[f] = factory.createEventProxy("tau:" + mName, EventKind.UNCONTROLLABLE);
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

  public Set<TIntHashSet> subsets(final Collection<TIntHashSet> from,
                                         final Set<TIntHashSet> to)
  {
    final Set<TIntHashSet> tobeadded = new THashSet<TIntHashSet>();
    outside:
    for (final TIntHashSet ann : from) {
      boolean subset = false;
      final Iterator<TIntHashSet> it = to.iterator();
      while(it.hasNext()) {
        final TIntHashSet ann2 = it.next();
        if (ann2.size() >= ann.size()) {
          // TODO can be optimized so not creating the array everytime
          if (ann2.containsAll(ann.toArray())) {
            subset = true; it.remove();
            if (ann2.size() == ann.size()) {
              break;
            }
          }
        } else {
          // if a subset already can't be done again
          if (subset) {continue;}
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
    if (isInitial(state)) {return true;}
    if (mPredecessors[state] != null) {
      for (int e = 0; e < mPredecessors[state].length; e++) {
        final TIntHashSet preds = mPredecessors[state][e];
        if (preds != null && !preds.isEmpty()) {return true;}
      }
    }
    return false;
  }

  public boolean equivalentIncoming(final int state1, final int state2)
  {
    if (isInitial(state1) != isInitial(state2)) {return false;}
    for (int e = 0; e < mEvents.length; e++) {
      final TIntHashSet preds1 = mPredecessors[state1][e];
      final TIntHashSet preds2 = mPredecessors[state2][e];
      final boolean empty1 = preds1 == null || preds1.isEmpty();
      final boolean empty2 = preds2 == null || preds2.isEmpty();
      if (empty1 && empty2) {continue;}
      if (empty1 != empty2) {return false;}
      if (!preds1.equals(preds2)) {return false;}
    }
    return true;
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
    tann = subsets(fann, tann);
    mAnnotations[from] = null;
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
    if (from == to) {return;}
    markState(to, mMarked[to] || mMarked[from]);
    markState(from, false);
    for (int e = 0; e < mEvents.length; e++) {
      final TIntHashSet succs = mSuccessors[from][e];
      if (succs == null) {continue;}
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
    if (from == to) {return;}
    markState(to, mMarked[to] || mMarked[from]);
    for (int e = 0; e < mEvents.length; e++) {
      final TIntHashSet succs = mSuccessors[from][e];
      if (succs == null) {continue;}
      final int[] arsuccs = succs.toArray();
      for (int i = 0; i < arsuccs.length; i++) {
        final int succ = arsuccs[i];
        addTransition(to, e, succ);
      }
    }
  }

  public void moveAllPredeccessors(final int from, final int to)
  {
    if (from == to) {return;}
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
    if (from == to) {return;}
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

  public void removeAllSelfLoops(final int e)
  {
    for (int s = 0; s < mSuccessors.length; s++) {
      final TIntHashSet succs = mSuccessors[s][e];
      if (succs == null) {continue;}
      if (succs.contains(s)) {removeTransition(s, e, s);}
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
  }

  public void removeAllOutgoing(final int s)
  {
    markState(s, false);
    for (int e = 0;  e < mSuccessors[s].length; e++) {
      final TIntHashSet succs = mSuccessors[s][e];
      if (succs == null) {continue;}
      final int[] arsuccs = succs.toArray();
      for (int i = 0; i < arsuccs.length; i++) {
        final int succ = arsuccs[i];
        removeTransition(s, e, succ);
      }
    }
  }

  public void addTransition(final int s, final int e, final int t)
  {
    final TIntHashSet succ = getFromArray(s, e, mSuccessors);
    final TIntHashSet pred = getFromArray(t, e, mPredecessors);
    succ.add(t);
    pred.add(s);
    final TIntHashSet active = getFromArray(s, mActiveEvents);
    active.add(e);
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

  public Collection<EventProxy> getAllSelfLoops()
  {
    final Collection<EventProxy> selfs = new ArrayList<EventProxy>();
    SELFLOOPS:
    for (int e = 0; e < mEvents.length; e++) {
      if (mEvents[e] == null || mEvents[e].equals(mMarkedEvent)) {continue;}
      for (int s = 0; s < mSuccessors.length; s++) {
        if (!hasPredecessors(s)) {continue;}
        final TIntHashSet succs = mSuccessors[s][e];
        if (succs == null || succs.isEmpty()) {continue;}
        if (succs.size() > 1 || !succs.contains(s)) {continue SELFLOOPS;}
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
        if (!hasPredecessors(s)) {continue;}
        final Set<TIntHashSet> annotations = getAnnotations2(s);
        for (final TIntHashSet ann : annotations) {
          if (!ann.contains(e)) {continue ENABLED;}
        }
      }
      selfs.add(mEvents[e]);
    }
    System.out.println(selfs);
    return selfs;
  }

  private Set<TIntHashSet> getAnnotations2(final int state)
  {
    return mAnnotations[state] == null ? Collections.singleton(getActiveEvents(state))
                                       : mAnnotations[state];
  }

  public int unreachableStates()
  {
    int num = 0;
    STATES:
    for (int s = 0; s < mPredecessors.length; s++) {
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
}
