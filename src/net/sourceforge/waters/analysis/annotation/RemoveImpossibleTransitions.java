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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;


public class RemoveImpossibleTransitions
{
  private final Map<AutomatonProxy, Map<EventProxy, Tuple>> mEventsCantHappen;
  private final EventProxy mMarking;
  public static int ENACTED = 0;
  public static int TIME = 0;

  public RemoveImpossibleTransitions(final EventProxy marking)
  {
    mMarking = marking;
    mEventsCantHappen = new THashMap<AutomatonProxy, Map<EventProxy, Tuple>>();
  }

  public static void clearStats()
  {
    ENACTED = 0;
    TIME = 0;
  }

  private Map<EventProxy, Set<Set<EventProxy>>> findEventsWhichAreImpossibleAfter(final EventProxy event)
  {
    final Map<EventProxy, Set<Set<EventProxy>>> eventRequires =
      new THashMap<EventProxy, Set<Set<EventProxy>>>();
    for (final AutomatonProxy aut : mEventsCantHappen.keySet()) {
      final Tuple tup = mEventsCantHappen.get(aut).get(event);
      if (tup == null) {continue;}
      for (final EventProxy e : tup.mImpossible) {
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

  public Map<EventProxy, Map<EventProxy, Set<Set<EventProxy>>>>
    findEventsWhichAreImpossibleAfter(final Set<EventProxy> events)
  {
    final Map<EventProxy, Map<EventProxy, Set<Set<EventProxy>>>> eventRequires =
      new THashMap<EventProxy, Map<EventProxy, Set<Set<EventProxy>>>>();
    for (final EventProxy e : events) {
      final Map<EventProxy, Set<Set<EventProxy>>> map = findEventsWhichAreImpossibleAfter(e);
      for (final EventProxy e2 : map.keySet()) {
        final Set<Set<EventProxy>> required = map.get(e2);
        final Iterator<Set<EventProxy>> it = required.iterator();
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

    public Tuple(final Set<EventProxy> leaves, final Set<EventProxy> impossible)
    {
      mLeaves = leaves;
      mImpossible = impossible;
    }

    @Override
    public String toString()
    {
      return mLeaves.toString() + mImpossible.toString();
    }
  }

  public void removeAutomata(final Collection<AutomatonProxy> automata)
  {
    for (final AutomatonProxy aut : automata) {
      mEventsCantHappen.remove(aut);
    }
  }

  public void addAutomata(final Collection<AutomatonProxy> automata)
  {
    for (final AutomatonProxy aut : automata) {
      findEventsCantHappen(aut);
    }
  }

  @SuppressWarnings("unlikely-arg-type")
  private void findEventsCantHappen(final AutomatonProxy automaton)
  {
    final TransitionRelation tr = new TransitionRelation(automaton, mMarking);
    final Map<EventProxy, Tuple> map = new THashMap<EventProxy, Tuple>();
    for (final EventProxy e : automaton.getEvents()) {
      map.put(e, new Tuple(new THashSet<EventProxy>(), new THashSet<EventProxy>(automaton.getEvents())));
    }
    for (int s = 0; s < tr.numberOfStates(); s++) {
      for (int e = 0; e < tr.numberOfEvents(); e++) {
        final TIntHashSet preds = tr.getPredecessors(s, e);
        if (preds == null || preds.isEmpty()) {continue;}
        final EventProxy event = tr.getEvent(e);
        final Tuple tup = map.get(event);
        if (tup == null) {continue;}
        for (int se = 0; se < tr.numberOfEvents(); se++) {
          final TIntHashSet succs = tr.getSuccessors(s, se);
          if (succs == null || succs.isEmpty()) {continue;}
          final EventProxy sevent = tr.getEvent(se);
          tup.mImpossible.remove(sevent);
          if (tup.mImpossible.isEmpty()) {
            // BUG Removing int from event map ???
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

  public void run(final TransitionRelation tr)
  {
    TIME -= System.currentTimeMillis();
    final Map<EventProxy, Map<EventProxy, Set<Set<EventProxy>>>> eventmap =
      findEventsWhichAreImpossibleAfter(tr.getEvents());
    boolean changed = true;
    while (changed) {
      changed = false;
      for (int s = 0; s < tr.numberOfStates(); s++) {
        if (tr.isInitial(s)) {continue;}
        final TIntHashSet active = tr.getActiveEvents(s);
        final int[] arr = active.toArray();
        ACTIVE:
        for (int i = 0; i < arr.length; i++) {
          final EventProxy event = tr.getEvent(arr[i]);
          if (event.equals(mMarking)) {continue;} //TODO make it so it removes marking
          final Map<EventProxy, Set<Set<EventProxy>>> tups = eventmap.get(event);
          if (tups == null || tups.isEmpty()) {continue;}
          final Set<EventProxy> selflooped = new THashSet<EventProxy>();
          final Set<EventProxy> incoming = new THashSet<EventProxy>();
          for (int e = 0; e < tr.numberOfEvents(); e++) {
            final TIntHashSet preds = tr.getPredecessors(s, e);
            if (preds == null || preds.isEmpty()) {continue;}
            final EventProxy eventpred = tr.getEvent(e);
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
          for (final EventProxy e : incoming) {
            final Set<Set<EventProxy>> required = tups.get(e);
            REQUIRED:
            for (final Set<EventProxy> req : required) {
              //if (!tr.getEvents().containsAll(required)) {continue;}
              for (final EventProxy self : selflooped) {
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
