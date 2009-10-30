package net.sourceforge.waters.analysis;

import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TIntHashSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


public class RemoveImpossibleTransitions
{
  private final Map<AutomatonProxy, Map<EventProxy, Tuple>> mEventsCantHappen;
  private final EventProxy mMarking;
  public static int ENACTED = 0;
  public static int TIME = 0;
  
  public RemoveImpossibleTransitions(EventProxy marking)
  {
    mMarking = marking;
    mEventsCantHappen = new THashMap<AutomatonProxy, Map<EventProxy, Tuple>>();
  }
  
  public static void clearStats()
  {
    ENACTED = 0;
    TIME = 0;
  }
  
  private Map<EventProxy, Set<Set<EventProxy>>> findEventsWhichAreImpossibleAfter(EventProxy event)
  {
    Map<EventProxy, Set<Set<EventProxy>>> eventRequires =
      new THashMap<EventProxy, Set<Set<EventProxy>>>();
    for (AutomatonProxy aut : mEventsCantHappen.keySet()) {
      Tuple tup = mEventsCantHappen.get(aut).get(event);
      if (tup == null) {continue;}
      for (EventProxy e : tup.mImpossible) {
        Set<Set<EventProxy>> requires = eventRequires.get(e);
        if (requires == null) {
          requires = new THashSet<Set<EventProxy>>();
          eventRequires.put(e, requires);
        }
        requires.add(tup.mLeaves);
      }
    }
    return eventRequires;
  }
  
  private Map<EventProxy, Map<EventProxy, Set<Set<EventProxy>>>> 
    findEventsWhichAreImpossibleAfter(Set<EventProxy> events)
  {
    Map<EventProxy, Map<EventProxy, Set<Set<EventProxy>>>> eventRequires =
      new THashMap<EventProxy, Map<EventProxy, Set<Set<EventProxy>>>>();
    for (EventProxy e : events) {
      Map<EventProxy, Set<Set<EventProxy>>> map = findEventsWhichAreImpossibleAfter(e);
      for (EventProxy e2 : map.keySet()) {
        Set<Set<EventProxy>> required = map.get(e2);
        Iterator<Set<EventProxy>> it = required.iterator();
        while(it.hasNext()) {
          if (!events.containsAll(it.next())) {it.remove();}
        }
        if (required.isEmpty()) {continue;}
        Map<EventProxy, Set<Set<EventProxy>>> tuples = eventRequires.get(e2);
        if (tuples == null) {
          tuples = new THashMap<EventProxy, Set<Set<EventProxy>>>();
          eventRequires.put(e2, tuples);
        }
        tuples.put(e, required);
      }
    }
    return eventRequires;
  }
  
  private class Tuple
  {
    final Set<EventProxy> mLeaves;
    final Set<EventProxy> mImpossible;
    
    public Tuple(Set<EventProxy> leaves, Set<EventProxy> impossible)
    {
      mLeaves = leaves;
      mImpossible = impossible;
    }
    
    public String toString()
    {
      return mLeaves.toString() + mImpossible.toString();
    }
  }
  
  public void removeAutomata(Collection<AutomatonProxy> automata)
  {
    for (AutomatonProxy aut : automata) {
      mEventsCantHappen.remove(aut);
    }
  }
  
  public void addAutomata(Collection<AutomatonProxy> automata)
  {
    for (AutomatonProxy aut : automata) {
      findEventsCantHappen(aut);
    }
  }
  
  private void findEventsCantHappen(AutomatonProxy automaton)
  {
    TransitionRelation tr = new TransitionRelation(automaton, mMarking);
    Map<EventProxy, Tuple> map = new THashMap<EventProxy, Tuple>();
    for (EventProxy e : automaton.getEvents()) {
      map.put(e, new Tuple(new THashSet<EventProxy>(), new THashSet<EventProxy>(automaton.getEvents())));
    }
    for (int s = 0; s < tr.numberOfStates(); s++) {
      for (int e = 0; e < tr.numberOfEvents(); e++) {
        TIntHashSet preds = tr.getPredecessors(s, e);
        if (preds == null || preds.isEmpty()) {continue;}
        EventProxy event = tr.getEvent(e);
        Tuple tup = map.get(event);
        if (tup == null) {continue;}
        for (int se = 0; se < tr.numberOfEvents(); se++) {
          TIntHashSet succs = tr.getSuccessors(s, se);
          if (succs == null || succs.isEmpty()) {continue;}
          EventProxy sevent = tr.getEvent(se);
          tup.mImpossible.remove(sevent);
          if (tup.mImpossible.isEmpty()) {
            map.remove(e); break;
          }
          // is self looped
          if (succs.size() == 1 && succs.contains(s)) {continue;}
          tup.mLeaves.add(sevent);
        }
      }
    }
    mEventsCantHappen.put(automaton, map);
    //System.out.println(automaton);
    //System.out.println(map);
  }
  
  public static String stats()
  {
    return "RemoveImpossibleTransitions: ENACTED = " + ENACTED + 
           " TIME = " + TIME;
  }
  
  public void run(TransitionRelation tr)
  {
    TIME -= System.currentTimeMillis();
    Map<EventProxy, Map<EventProxy, Set<Set<EventProxy>>>> eventmap =
      findEventsWhichAreImpossibleAfter(tr.getEvents());
    boolean changed = true;
    while (changed) {
      changed = false;
      for (int s = 0; s < tr.numberOfStates(); s++) {
        if (tr.isInitial(s)) {continue;}
        TIntHashSet active = tr.getActiveEvents(s);
        int[] arr = active.toArray();
        ACTIVE:
        for (int i = 0; i < arr.length; i++) {
          EventProxy event = tr.getEvent(arr[i]);
          if (event.equals(mMarking)) {continue;} //TODO make it so it removes marking
          Map<EventProxy, Set<Set<EventProxy>>> tups = eventmap.get(event);
          if (tups == null || tups.isEmpty()) {continue;}
          Set<EventProxy> selflooped = new THashSet<EventProxy>();
          Set<EventProxy> incoming = new THashSet<EventProxy>();
          for (int e = 0; e < tr.numberOfEvents(); e++) {
            TIntHashSet preds = tr.getPredecessors(s, e);
            if (preds == null || preds.isEmpty()) {continue;}
            EventProxy eventpred = tr.getEvent(e);
            if (preds.contains(s)) {
              selflooped.add(eventpred);
              if (preds.size() == 1) {continue;}
            }
            if (!tups.containsKey(eventpred)) {continue ACTIVE;}
            //System.out.println("event:" + eventpred);
            //System.out.println(tups.keySet());
            incoming.add(eventpred);
          }
          INCOMING:
          for (EventProxy e : incoming) {
            Set<Set<EventProxy>> required = tups.get(e);
            REQUIRED:
            for (Set<EventProxy> req : required) {
              //if (!tr.getEvents().containsAll(required)) {continue;}
              for (EventProxy self : selflooped) {
                if (req.contains(self)) {continue REQUIRED;}
              }
              // has no selfloops for this requires, next event
              continue INCOMING;
            }
            // failed each requires, can't do
            continue ACTIVE;
          }
          System.out.println("removed transitions");
          ENACTED++;
          tr.removeOutgoing(s, arr[i]);
          changed = true;
        }
      }
    }
    TIME += System.currentTimeMillis();
  }
}
