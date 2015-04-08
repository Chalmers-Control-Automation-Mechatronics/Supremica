//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRSubsystemInfo
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.DFSSearchSpace;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.marshaller.MarshallingTools;


/**
 * @author Robi Malik
 */

class TRSubsystemInfo
  implements Comparable<TRSubsystemInfo>
{

  //#########################################################################
  //# Constructors
  TRSubsystemInfo(final int numAutomata,
                  final int numEvents)
  {
    mAutomata = new ArrayList<>(numAutomata);
    mEvents = new HashMap<>(numEvents);
  }

  TRSubsystemInfo(final List<TRAutomatonProxy> automata,
                  final int numEvents)
  {
    mAutomata = automata;
    mEvents = new HashMap<>(numEvents);
  }

  TRSubsystemInfo(final List<TRAutomatonProxy> automata,
                  final Collection<TREventInfo> events)
  {
    mAutomata = automata;
    mEvents = new HashMap<>(events.size());
    for (final TREventInfo info : events) {
      final EventProxy event = info.getEvent();
      mEvents.put(event, info);
    }
  }


  //#########################################################################
  //# Simple Access
  int getNumberOfAutomata()
  {
    return mAutomata.size();
  }

  List<TRAutomatonProxy> getAutomata()
  {
    return mAutomata;
  }

  Collection<TREventInfo> getEvents()
  {
    return mEvents.values();
  }

  TREventInfo getEventInfo(final EventProxy event)
  {
    return mEvents.get(event);
  }


  //#########################################################################
  //# Update
  void addAutomata(final Collection<TRAutomatonProxy> automata)
  {
    mAutomata.addAll(automata);
  }

  void registerEvents(final TRAutomatonProxy aut,
                      final byte[] eventStatus)
  {
    registerEvents(aut, eventStatus, false);
  }

  void registerEvents(final TRAutomatonProxy aut,
                      final byte[] eventStatus,
                      final boolean external)
  {
    final EventEncoding enc = aut.getEventEncoding();
    for (int e = EventEncoding.NONTAU; e < eventStatus.length; e++) {
      final EventProxy event = enc.getProperEvent(e);
      final byte status = enc.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status)) {
        final TREventInfo info = createEventInfo(event, status);
        if (external) {
          info.addExternalStatus(eventStatus[e]);
        } else {
          info.setAutomatonStatus(aut, eventStatus[e]);
        }
      }
    }
  }

  void updateEvents(final TRAutomatonProxy aut,
                    final byte[] eventStatus,
                    final Queue<TRAutomatonProxy> needsSimplification)
  {
    final EventEncoding enc = aut.getEventEncoding();
    for (int e = EventEncoding.NONTAU; e < eventStatus.length; e++) {
      final EventProxy event = enc.getProperEvent(e);
      final TREventInfo info = getEventInfo(event);
      if (info != null) {
        info.updateAutomatonStatus(aut, eventStatus[e], needsSimplification);
        if (info.isEmpty()) {
          mEvents.remove(event);
        }
      }
    }
  }

  void addAutomaton(final TRAutomatonProxy aut)
  {
    mAutomata.add(aut);
  }

  void removeAutomaton(final TRAutomatonProxy aut,
                       final Queue<TRAutomatonProxy> needsSimplification)
  {
    final EventEncoding enc = aut.getEventEncoding();
    final int numEvents = enc.getNumberOfProperEvents();
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final byte status = enc.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status)) {
        final EventProxy event = enc.getProperEvent(e);
        final TREventInfo info = getEventInfo(event);
        info.updateAutomatonStatus
          (aut, EventStatus.STATUS_UNUSED, needsSimplification);
        if (info.isEmpty()) {
          mEvents.remove(event);
        }
      }
    }
    mAutomata.remove(aut);
  }

  void moveToEnd(final TRAutomatonProxy aut)
  {
    final boolean removed = mAutomata.remove(aut);
    assert removed : "Automaton not found in subsystem!";
    mAutomata.add(aut);
  }

  void removeEvent(final EventProxy event)
  {
    mEvents.remove(event);
  }


  //#########################################################################
  //# Interface java.util.Comparable<SubsystemInfo>
  @Override
  public int compareTo(final TRSubsystemInfo subsys)
  {
    int delta = mAutomata.size() - subsys.mAutomata.size();
    if (delta != 0) {
      return delta;
    }
    delta = getNumberOfStates() - subsys.getNumberOfStates();
    if (delta != 0) {
      return delta;
    }
    final Iterator<TRAutomatonProxy> iter1 = mAutomata.iterator();
    final Iterator<TRAutomatonProxy> iter2 = subsys.mAutomata.iterator();
    while (iter1.hasNext()) {
      final TRAutomatonProxy aut1 = iter1.next();
      final TRAutomatonProxy aut2 = iter2.next();
      delta = aut1.compareTo(aut2);
      if (delta != 0) {
        return delta;
      }
    }
    return 0;
  }


  //#########################################################################
  //# Splitting
  List<TRSubsystemInfo> findEventDisjointSubsystems()
  {
    List<TRSubsystemInfo> result = null;
    final List<TRAutomatonProxy> remainingAutomata =
      new ArrayList<>(mAutomata);
    Set<TREventInfo> remainingEvents = null;
    while (!remainingAutomata.isEmpty()) {
      final DFSSearchSpace<TRAutomatonProxy> automataQueue =
        new DFSSearchSpace<>(remainingAutomata.size());
      final DFSSearchSpace<TREventInfo> eventQueue =
        new DFSSearchSpace<>(mEvents.size());
      automataQueue.offer(remainingAutomata.get(0));
      while (!automataQueue.isEmpty() || !eventQueue.isEmpty()) {
        if (automataQueue.visitedSize() == remainingAutomata.size()) {
          break;
        } else if (!eventQueue.isEmpty()) {
          final TREventInfo info = eventQueue.poll();
          automataQueue.addAll(info.getAutomata());
        } else {
          final TRAutomatonProxy aut = automataQueue.poll();
          final EventEncoding enc = aut.getEventEncoding();
          for (int e = EventEncoding.NONTAU;
               e < enc.getNumberOfProperEvents(); e++) {
            final byte status = enc.getProperEventStatus(e);
            if (EventStatus.isUsedEvent(status)) {
              final EventProxy event = enc.getProperEvent(e);
              final TREventInfo info = mEvents.get(event);
              eventQueue.offer(info);
            }
          }
        }
      }
      if (result == null) {
        if (automataQueue.visitedSize() == remainingAutomata.size()) {
          return null;
        } else {
          result = new LinkedList<>();
          remainingEvents = new THashSet<>(mEvents.values());
        }
      }
      final Set<TRAutomatonProxy> automataSet = automataQueue.getVisitedSet();
      remainingAutomata.removeAll(automataSet);
      final List<TRAutomatonProxy> automataList = new ArrayList<>(automataSet);
      Collections.sort(automataList);
      final Collection<TREventInfo> events;
      if (remainingAutomata.isEmpty()) {
        events = remainingEvents;
      } else {
        events = eventQueue.getVisitedSet();
        remainingEvents.removeAll(events);
      }
      final TRSubsystemInfo subsys = new TRSubsystemInfo(automataList, events);
      result.add(subsys);
    }
    return result;
  }


  //#########################################################################
  //# Auxiliary Methods
  private TREventInfo createEventInfo(final EventProxy event,
                                      final byte status)
  {
    TREventInfo info = mEvents.get(event);
    if (info == null) {
      info = new TREventInfo(event, status);
      mEvents.put(event, info);
    }
    return info;
  }

  private int getNumberOfStates()
  {
    int result = 0;
    for (final TRAutomatonProxy aut : mAutomata) {
      result += aut.getTransitionRelation().getNumberOfStates();
    }
    return result;
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return AutomatonTools.getCompositionName(mAutomata);
  }

  public void saveModule(final String filename)
  {
    MarshallingTools.saveModule(mAutomata, filename);
  }


  //#########################################################################
  //# Data Members
  private final List<TRAutomatonProxy> mAutomata;
  private final Map<EventProxy,TREventInfo> mEvents;

}
