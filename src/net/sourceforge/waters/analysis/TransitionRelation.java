package net.sourceforge.waters.analysis;

import gnu.trove.TIntHashSet;
import java.util.Set;
import net.sourceforge.waters.model.des.EventProxy;
import gnu.trove.TIntProcedure;
import gnu.trove.THashSet;
import java.util.Iterator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import gnu.trove.TObjectIntHashMap;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import java.util.Collection;
import java.util.Map;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import java.util.ArrayList;
import net.sourceforge.waters.xsd.base.ComponentKind;
import java.util.List;
import gnu.trove.THashMap;
import java.util.HashSet;
import net.sourceforge.waters.xsd.base.EventKind;
import java.util.Collections;
import java.util.Arrays;

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
  
  public TransitionRelation(AutomatonProxy aut, EventProxy marked)
  {
    this(aut, marked, aut.getEvents());
  }
  
  public TransitionRelation(AutomatonProxy aut, EventProxy marked,
                            Set<EventProxy> eventsall)
  {
    eventsall = new THashSet<EventProxy>(eventsall);
    eventsall.addAll(aut.getEvents());
    Set<EventProxy> allselflooped = new THashSet<EventProxy>(eventsall);
    allselflooped.removeAll(aut.getEvents());
    allselflooped.remove(marked);
    mName = aut.getName();
    mMarkedEvent = marked;
    EventProxy[] events = new EventProxy[aut.getEvents().size()];
    mEvents = eventsall.toArray(events);
    TObjectIntHashMap<EventProxy> eventToInt = 
      new TObjectIntHashMap<EventProxy>(mEvents.length);
    for (int i = 0; i < mEvents.length; i++) {
      eventToInt.put(mEvents[i], i);
    }
    mEventToInt = eventToInt;
    TObjectIntHashMap<StateProxy> stateToInt = 
      new TObjectIntHashMap<StateProxy>();
    int numstates = 0;
    mSuccessors = new TIntHashSet[aut.getStates().size()][mEvents.length];
    mPredecessors = new TIntHashSet[aut.getStates().size()][mEvents.length];
    mActiveEvents = new TIntHashSet[aut.getStates().size()];
    mAnnotations = new Set[aut.getStates().size()];
    mMarked = new boolean[aut.getStates().size()];
    mIsInitial = new boolean[aut.getStates().size()];
    for (StateProxy s : aut.getStates()) {
      stateToInt.put(s, numstates);
      if (s.getPropositions().contains(marked) || !aut.getEvents().contains(marked)) {
        markState(numstates, true);
      }
      if (s.isInitial()) {
        makeInitialState(numstates, true);
      }
      Set<Set<EventProxy>> anns = getAnnotations(s.getPropositions());
      //System.out.println("build annotation:" + anns);
      if (anns != null) {
        Set<TIntHashSet> annints = new HashSet<TIntHashSet>(anns.size());
        for (Set<EventProxy> ann : anns) {
          TIntHashSet annint = new TIntHashSet(ann.size());
          for (EventProxy event : ann) {
            annint.add(eventToInt.get(event));
          }
          annints.add(annint);
        }
        mAnnotations[numstates] = annints;
      }
      // TODO work out annotations
      numstates++;
    }
    for (TransitionProxy tran : aut.getTransitions()) {
      int s = stateToInt.get(tran.getSource());
      int t = stateToInt.get(tran.getTarget());
      int e = eventToInt.get(tran.getEvent());
      TIntHashSet succ = getFromArray(s, e, mSuccessors);
      succ.add(t);
      TIntHashSet pred = getFromArray(t, e, mPredecessors);
      pred.add(s);
      TIntHashSet active = getFromArray(s, mActiveEvents);
      active.add(e);
    }
    for (EventProxy event : allselflooped) {
      int e = eventToInt.get(event);
      for (int s = 0; s < numberOfStates(); s++) {
        addTransition(s, e, s);
      }
    }
    mAnnToEvent = new THashMap<Set<Set<EventProxy>>, EventProxy>();
  }
  
  public void setMarkingToStatesWithOutgoing(Collection<EventProxy> events)
  {
    int[] evs = new int[events.size()];
    int i = 0;
    for (EventProxy e : events) {evs[i] = mEventToInt.get(e); i++;}
    STATES:
    for (int s = 0; s < mSuccessors.length; s++) {
      markState(s, false);
      for (i = 0; i < evs.length; i++) {
        int e = evs[i];
        if (mSuccessors[s][e] != null && !mSuccessors[s][e].isEmpty()) {
          markState(s, true); continue STATES;
        }
      }
    }
  }
  
  public Set<TIntHashSet> getAnnotation(int state)
  {
    return mAnnotations[state];
  }
 
  public void setAnnotation(int state, Set<TIntHashSet> annotation)
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
  
  private static Set<Set<EventProxy>> getAnnotations(Collection<EventProxy> props)
  {
    Iterator<EventProxy> it = props.iterator();
    while (it.hasNext()) {
      EventProxy e = it.next();
      if (e instanceof AnnotationEvent) {
        AnnotationEvent a = (AnnotationEvent)e;
        return a.getAnnotations();
      }
    }
    return null;
  }
  
  public EventProxy getAnnotationEvent(Set<TIntHashSet> annotations) {
    if (annotations == null || annotations.isEmpty()) {return null;}
    Set<Set<EventProxy>> res = new THashSet<Set<EventProxy>>();
    for (TIntHashSet ann : annotations) {
      final Set<EventProxy> set = new THashSet<EventProxy>();
      ann.forEach(new TIntProcedure() {
        public boolean execute(int e) {
          EventProxy event = mEvents[e];
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
      Set<EventProxy> props = new THashSet<EventProxy>();
      Set<TIntHashSet> anns = mAnnotations[s];
      if (anns != null) {
        anns.remove(getActiveEvents(s));
      }
      EventProxy annotation = getAnnotationEvent(anns);
      if (annotation != null) {
        props.add(annotation);
      }
      if (isMarked(s) && mMarkedEvent != null) {
        props.add(mMarkedEvent);
      }
      boolean isInitial = isInitial(s);
      states.add(new AnnotatedMemStateProxy(s, props, isInitial));
    }
    //System.out.println(mAnnToEvent.size());
    for (int s = 0; s < mSuccessors.length; s++) {
      if (!hasPredecessors(s)) {continue;}
      final StateProxy source = states.get(s);
      for (int e = 0; e < mSuccessors[s].length; e++) {
        final EventProxy event = mEvents[e];
        if (event == mMarkedEvent || event == null) {continue;}
        TIntHashSet succs = mSuccessors[s][e];
        if (succs == null) {continue;}
        succs.forEach(new TIntProcedure() {
          public boolean execute(int succ) {
            StateProxy target = states.get(succ);
            trans.add(factory.createTransitionProxy(source, event, target));
            return true;
          }
        });
      }
    }
    for (int e = 0; e < mEvents.length; e++) {
      if (mEvents[e] != null) {events.add(mEvents[e]);}
    }
    List<StateProxy> tempstates = new ArrayList<StateProxy>();;
    for (int s = 0; s < states.size(); s++) {
      if (hasPredecessors(s)) {
        tempstates.add(states.get(s));
      }
    }
    return factory.createAutomatonProxy(mName, ComponentKind.PLANT,
                                        events, tempstates, trans);
  }
  
  public boolean isMarked(int state)
  {
    return mMarked[state];
  }
  
  public boolean isInitial(int state)
  {
    return mIsInitial[state];
  }
  
  public void markState(int state, boolean value)
  {
    mMarked[state] = value;
    TIntHashSet active = getFromArray(state, mActiveEvents);
    if (mEventToInt.containsKey(mMarkedEvent)) {
      if (value) {
        active.add(mEventToInt.get(mMarkedEvent));
      } else {
        active.remove(mEventToInt.get(mMarkedEvent));
      }
    }
  }
  
  public void makeInitialState(int state, boolean value)
  {
    mIsInitial[state] = value;
  }
  
  public TIntHashSet getActiveEvents(int state)
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
    TIntHashSet ae = new TIntHashSet();
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
  
  public void removeOutgoing(int state, int event)
  {
    TIntHashSet succs = mSuccessors[state][event];
    if (succs == null) {return;}
    int[] arr = succs.toArray();
    for (int i = 0; i < arr.length; i++) {
      int suc = arr[i];
      removeTransition(state, event, suc);
    }
  }
  
  public void removeSharedSuccessors(int has, int remove)
  {
    for (int e = 0; e < mSuccessors[has].length; e++) {
      TIntHashSet hassuccs = mSuccessors[has][e];
      if (hassuccs == null) {continue;}
      int[] succs = hassuccs.toArray();
      for (int i = 0; i < succs.length; i++) {
        removeTransition(remove, e, succs[i]);
      }
    }
  }
  
  public void removeEvent(int event)
  {
    mEvents[event] = null;
    for (int s = 0; s < mSuccessors.length; s++) {
      TIntHashSet succ = mSuccessors[s][event];
      if (succ == null) {continue;}
      int[] intsuccs = succ.toArray();
      for (int i = 0; i < intsuccs.length; i++) {
        int t = intsuccs[i];
        removeTransition(s, event, t);
      }
    }
  }
  
  public String getName()
  {
    return mName;
  }
  
  public EventProxy getEvent(int event)
  {
    return mEvents[event];
  }
  
  public int getEventInt(EventProxy event)
  {
    return mEventToInt.get(event);
  }
  
  public Set<EventProxy> getEvents()
  {
    Set<EventProxy> events = new HashSet<EventProxy>();
    for (int i = 0; i < mEvents.length; i++) {
      if (mEvents[i] != null) {
        events.add(mEvents[i]);
      }
    }
    return events;
  }
  
  public void removeAllIncoming(int s)
  {
    markState(s, false);
    for (int e = 0;  e < mPredecessors[s].length; e++) {
      final int event = e;
      TIntHashSet preds = mPredecessors[s][e];
      if (preds == null) {continue;}
      int[] arpreds = preds.toArray();
      for (int i = 0; i < arpreds.length; i++) {
        int pred = arpreds[i];
        removeTransition(pred, e, s);
      }
    }
  }
  
  public void removeAllAnnotations(int event)
  {
    System.out.println("remove annotations: " + event);
    for (int s = 0; s < mAnnotations.length; s++) {
      Set<TIntHashSet> anns = mAnnotations[s];
      if (anns != null) {
        Iterator<TIntHashSet> it = anns.iterator();
        while (it.hasNext()) {
          TIntHashSet ann = it.next();
          System.out.println(Arrays.toString(ann.toArray()));
          if (ann.contains(event)) {
            System.out.println("removed");
            it.remove();
          }
        }
      }
    }
  }
  
  public boolean isMarkingEvent(int event)
  {
    return mEvents[event] == mMarkedEvent;
  }
  
  public int eventToInt(EventProxy event)
  {
    return mEventToInt.get(event);
  }
  
  public int mergeEvents(Collection<EventProxy> events)
  {
    Iterator<EventProxy> it = events.iterator();
    EventProxy first = it.next();
    int f = mEventToInt.get(first);
    while (it.hasNext()) {
      int next = mEventToInt.get(it.next());
      for (int s = 0; s < mSuccessors.length; s++) {
        TIntHashSet toremove = mSuccessors[s][next];
        if (toremove != null) {
          TIntHashSet tau = getFromArray(s, f, mSuccessors);
          tau.addAll(toremove.toArray());
          mSuccessors[s][next] = null;
        }
        toremove = mPredecessors[s][next];
        if (toremove != null) {
          TIntHashSet tau = getFromArray(s, f, mPredecessors);
          tau.addAll(toremove.toArray());
          mPredecessors[s][next] = null;
        }
      }
      mEvents[next] = null;
    }
    return f;
  }
  
  public int mergeEvents(Collection<EventProxy> events,
                         ProductDESProxyFactory factory)
  {
    Iterator<EventProxy> it = events.iterator();
    EventProxy first = it.next();
    int f = mEventToInt.get(first);
    while (it.hasNext()) {
      int next = mEventToInt.get(it.next());
      for (int s = 0; s < mSuccessors.length; s++) {
        TIntHashSet toremove = mSuccessors[s][next];
        if (toremove != null) {
          TIntHashSet tau = getFromArray(s, f, mSuccessors);
          tau.addAll(toremove.toArray());
          mSuccessors[s][next] = null;
        }
        toremove = mPredecessors[s][next];
        if (toremove != null) {
          TIntHashSet tau = getFromArray(s, f, mPredecessors);
          tau.addAll(toremove.toArray());
          mPredecessors[s][next] = null;
        }
      }
      mEvents[next] = null;
    }
    mEvents[f] = factory.createEventProxy("tau:" + mName, EventKind.UNCONTROLLABLE);
    return f;
  }
  
  public TIntHashSet[] getAllSuccessors(int state)
  {
    return mSuccessors[state];
  }
  
  public TIntHashSet getSuccessors(int state, int event)
  {
    return mSuccessors[state][event];
  }
  
  public TIntHashSet[] getAllPredecessors(int state)
  {
    return mPredecessors[state];
  }
  
  public TIntHashSet getPredecessors(int state, int event)
  {
    return mPredecessors[state][event];
  }
  
  public Set<TIntHashSet> subsets(Collection<TIntHashSet> from,
                                         Set<TIntHashSet> to)
  {
    Set<TIntHashSet> tobeadded = new THashSet<TIntHashSet>();
    outside:
    for (TIntHashSet ann : from) {
      boolean subset = false;
      Iterator<TIntHashSet> it = to.iterator();
      while(it.hasNext()) {
        TIntHashSet ann2 = it.next();
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
  
  public boolean hasPredecessors(int state)
  {
    if (isInitial(state)) {return true;}
    if (mPredecessors[state] != null) {
      for (int e = 0; e < mPredecessors[state].length; e++) {
        TIntHashSet preds = mPredecessors[state][e];
        if (preds != null && !preds.isEmpty()) {return true;}
      }
    }
    return false;
  }
  
  public boolean equivalentIncoming(int state1, int state2)
  {
    if (isInitial(state1) != isInitial(state2)) {return false;}
    for (int e = 0; e < mEvents.length; e++) {
      TIntHashSet preds1 = mPredecessors[state1][e];
      TIntHashSet preds2 = mPredecessors[state2][e];
      boolean empty1 = preds1 == null || preds1.isEmpty();
      boolean empty2 = preds2 == null || preds2.isEmpty();
      if (empty1 && empty2) {continue;}
      if (empty1 != empty2) {return false;}
      if (!preds1.equals(preds2)) {return false;}
    }
    return true;
  }
  
  public void addAnnotations(int from, int to)
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
  
  private TIntHashSet getFromArray(int i, TIntHashSet[] array)
  {
    TIntHashSet intset = array[i];
    if (intset == null) {
      intset = new TIntHashSet();
      array[i] = intset;
    }
    return intset;
  }
  
  private TIntHashSet getFromArray(int i, int j, TIntHashSet[][] array)
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
      final int event = e;
      TIntHashSet succs = mSuccessors[from][e];
      if (succs == null) {continue;}
      int[] arsuccs = succs.toArray();
      for (int i = 0; i < arsuccs.length; i++) {
        int succ = arsuccs[i];
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
      final int event = e;
      TIntHashSet succs = mSuccessors[from][e];
      if (succs == null) {continue;}
      int[] arsuccs = succs.toArray();
      for (int i = 0; i < arsuccs.length; i++) {
        int succ = arsuccs[i];
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
      final int event = e;
      TIntHashSet preds = mPredecessors[from][e];
      if (preds != null) {
        int[] arpreds = preds.toArray();
        for (int i = 0; i < arpreds.length; i++) {
          int pred = arpreds[i];
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
      final int event = e;
      TIntHashSet preds = mPredecessors[from][e];
      if (preds != null) {
        int[] arpreds = preds.toArray();
        for (int i = 0; i < arpreds.length; i++) {
          int pred = arpreds[i];
          addTransition(pred, e, to);
        }
      }
    }
  }
  
  public void removeAllSelfLoops(int e)
  {
    for (int s = 0; s < mSuccessors.length; s++) {
      TIntHashSet succs = mSuccessors[s][e];
      if (succs == null) {continue;}
      if (succs.contains(s)) {removeTransition(s, e, s);}
    }
  }
  
  public void removeTransition(int s, int e, int t)
  {
    TIntHashSet succ = getFromArray(s, e, mSuccessors);
    TIntHashSet pred = getFromArray(t, e, mPredecessors);
    succ.remove(t);
    pred.remove(s);
    if (succ.isEmpty()) {
      mSuccessors[s][e] = null;
      TIntHashSet active = getFromArray(s, mActiveEvents);
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
      final int event = e;
      TIntHashSet succs = mSuccessors[s][e];
      if (succs == null) {continue;}
      int[] arsuccs = succs.toArray();
      for (int i = 0; i < arsuccs.length; i++) {
        int succ = arsuccs[i];
        removeTransition(s, e, succ);
      }
    }
  }
  
  public void addTransition(int s, int e, int t)
  {
    TIntHashSet succ = getFromArray(s, e, mSuccessors);
    TIntHashSet pred = getFromArray(t, e, mPredecessors);
    succ.add(t);
    pred.add(s);
    TIntHashSet active = getFromArray(s, mActiveEvents);
    active.add(e);
  }
  
  public void mergewithannotations(int[] stuff)
  {
    int to = stuff[0];
    for (int i = 1; i < stuff.length; i++) {
      int from = stuff[i];
      addAnnotations(from, to);
      moveAllSuccessors(from, to);
      moveAllPredeccessors(from, to);
    }
  }
  
  public void merge(int[] stuff)
  {
    int to = stuff[0];
    for (int i = 1; i < stuff.length; i++) {
      int from = stuff[i];
      moveAllSuccessors(from, to);
      moveAllPredeccessors(from, to);
    }
  }
  
  public Collection<EventProxy> getAllSelfLoops()
  {
    Collection<EventProxy> selfs = new ArrayList<EventProxy>();
    SELFLOOPS:
    for (int e = 0; e < mEvents.length; e++) {
      if (mEvents[e] == null || mEvents[e].equals(mMarkedEvent)) {continue;} 
      for (int s = 0; s < mSuccessors.length; s++) {
        if (!hasPredecessors(s)) {continue;}
        TIntHashSet succs = mSuccessors[s][e];
        if (succs == null || succs.isEmpty()) {continue;}
        if (succs.size() > 1 || !succs.contains(s)) {continue SELFLOOPS;}
      }
      selfs.add(mEvents[e]);
    }
    return selfs;
  }
  
  public Collection<EventProxy> getAllwaysEnabled()
  {
    Collection<EventProxy> selfs = new ArrayList<EventProxy>();
    ENABLED:
    for (int e = 0; e < mEvents.length; e++) {
      if (mEvents[e] == null) {continue;} 
      for (int s = 0; s < mSuccessors.length; s++) {
        if (!hasPredecessors(s)) {continue;}
        Set<TIntHashSet> annotations = getAnnotations2(s);
        for (TIntHashSet ann : annotations) {
          if (!ann.contains(e)) {continue ENABLED;}
        }
      }
      selfs.add(mEvents[e]);
    }
    System.out.println(selfs);
    return selfs;
  }
  
  private Set<TIntHashSet> getAnnotations2(int state)
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
