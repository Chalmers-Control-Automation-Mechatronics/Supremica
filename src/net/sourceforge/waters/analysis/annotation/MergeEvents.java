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

package net.sourceforge.waters.analysis.annotation;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
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
