package net.sourceforge.waters.analysis;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;
import java.util.ArrayList;
import java.util.Collection;
import gnu.trove.TIntArrayList;
import java.util.Set;
import gnu.trove.THashSet;
import java.util.Map;
import net.sourceforge.waters.model.des.AutomatonProxy;
import gnu.trove.THashMap;
import net.sourceforge.waters.model.des.EventProxy;
import java.util.Iterator;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

public class MergeEvents
{
  private Map<AutomatonProxy, Map<EventProxy, Set<EventProxy>>> mEventsAllwaysHappenTogether;
  private final Set<EventProxy> mEvents;
  private final EventProxy mMarking;
  public static int ENACTED = 0;
  public static int TIME = 0;
  
  public MergeEvents(EventProxy marking, Set<EventProxy> events)
  {
    mEventsAllwaysHappenTogether = 
      new THashMap<AutomatonProxy, Map<EventProxy, Set<EventProxy>>>();
    mEvents = events;
    mMarking = marking;
  }
  
  public static void clearStats()
  {
    ENACTED = 0;
    TIME = 0;
  }
  
  private Map<EventProxy, Set<EventProxy>> calculateEquivalent(AutomatonProxy aut)
  {
    Map<EventProxy, Set<EventProxy>> map =
      new THashMap<EventProxy, Set<EventProxy>>();
    for (EventProxy e : mEvents) {
      map.put(e, new THashSet<EventProxy>(mEvents));
      if (!aut.getEvents().contains(e)) {map.get(e).removeAll(aut.getEvents());}
    }
    TransitionRelation tr = new TransitionRelation(aut, mMarking);
    for (int s = 0; s < tr.numberOfStates(); s++) {
      TIntHashSet active = tr.getActiveEvents(s);
      Set<EventProxy> events = new THashSet<EventProxy>(active.size());
      int[] arr = active.toArray();
      for (int i = 0; i < arr.length; i++) {
        int e = arr[i];
        EventProxy ev = tr.getEvent(e);
        if (mEvents.contains(ev)) {events.add(ev);}
      }
      for (EventProxy e : events) {
        map.get(e).retainAll(events);
      }
    }
    mEventsAllwaysHappenTogether.put(aut, map);
    return map;
  }
  
  public static String stats()
  {
    return "MergeEvents: ENACTED = " + ENACTED + 
           " TIME = " + TIME;
  }
  
  public Set<AutomatonProxy> run(Set<AutomatonProxy> automata,
                                 ProductDESProxyFactory factory)
  {
    TIME -= System.currentTimeMillis();
    Set<AutomatonProxy> res = new THashSet<AutomatonProxy>();
    Map<AutomatonProxy, Map<EventProxy, Set<EventProxy>>> newmap =
      mEventsAllwaysHappenTogether;
    for (AutomatonProxy aut : automata) {
      Map<EventProxy, Set<EventProxy>> map = mEventsAllwaysHappenTogether.get(aut);
      if (map == null) {
        map = calculateEquivalent(aut);
      }
      newmap.put(aut, map);
    }
    Set<EventProxy> events = new THashSet<EventProxy>(mEvents);
    Collection<Set<EventProxy>> tomerge = new ArrayList<Set<EventProxy>>();
    while(!events.isEmpty()) {
      EventProxy e = events.iterator().next();
      Set<EventProxy> equiv = new THashSet<EventProxy>(mEvents);
      for (AutomatonProxy aut : automata) {
        equiv.retainAll(newmap.get(aut).get(e));
        if (equiv.size() == 1) {break;}
      }
      if (equiv.size() > 1) {tomerge.add(equiv);}
      events.removeAll(equiv);
    }
    for (AutomatonProxy aut : automata) {
      TransitionRelation tr = new TransitionRelation(aut, mMarking);
      for (Set<EventProxy> equiv : tomerge) {
        if (aut.getEvents().containsAll(equiv)) {
          tr.mergeEvents(equiv);
          ENACTED++;
        }
      }
      Map<EventProxy, Set<EventProxy>> map = newmap.remove(aut);
      aut = tr.getAutomaton(factory);
      newmap.put(aut, map);
      res.add(aut);
    }
    mEventsAllwaysHappenTogether = newmap;
    TIME += System.currentTimeMillis();
    return res;
  }
}
