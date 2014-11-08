//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   SubsystemInfo
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.DFSSearchSpace;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author Robi Malik
 */

class SubsystemInfo
  implements Comparable<SubsystemInfo>
{

  //#########################################################################
  //# Constructors
  SubsystemInfo(final Collection<TRAutomatonProxy> automata,
                final int numEvents)
  {
    mAutomata = automata;
    mEvents = new HashMap<>(numEvents);
  }

  SubsystemInfo(final Collection<TRAutomatonProxy> automata,
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

  Collection<TRAutomatonProxy> getAutomata()
  {
    return mAutomata;
  }

  TREventInfo getEventInfo(final EventProxy event)
  {
    return mEvents.get(event);
  }


  //#########################################################################
  //# Set up
  void registerEvents(final TRAutomatonProxy aut,
                      final byte[] eventStatus)
  {
    final EventEncoding enc = aut.getEventEncoding();
    for (int e = EventEncoding.NONTAU; e < eventStatus.length; e++) {
      final EventProxy event = enc.getProperEvent(e);
      final byte status = enc.getProperEventStatus(e);
      final TREventInfo info = createEventInfo(event, status);
      info.setAutomatonStatus(aut, eventStatus[e]);
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
      info.setAutomatonStatus(aut, eventStatus[e]); // TODO needsSimplification
    }
  }


  //#########################################################################
  //# Interface java.util.Comparable<SubsystemInfo>
  @Override
  public int compareTo(final SubsystemInfo subsys)
  {
    final int delta = mAutomata.size() - subsys.mAutomata.size();
    if (delta != 0) {
      return delta;
    } else {
      return getNumberOfStates() - subsys.getNumberOfStates();
    }
  }


  //#########################################################################
  //# Splitting
  List<SubsystemInfo> findEventDisjointSubsystems()
  {
    List<SubsystemInfo> result = null;
    final List<TRAutomatonProxy> remainingAutomata =
      new ArrayList<>(mAutomata);
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
        }
      }
      final Set<TRAutomatonProxy> automataSet = automataQueue.getVisitedSet();
      final List<TRAutomatonProxy> automataList = new ArrayList<>(automataSet);
      Collections.sort(automataList);
      final Collection<TREventInfo> events = eventQueue.getVisitedSet();
      final SubsystemInfo subsys = new SubsystemInfo(automataList, events);
      result.add(subsys);
      remainingAutomata.removeAll(automataSet);
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
  //# Data Members
  private final Collection<TRAutomatonProxy> mAutomata;
  private final Map<EventProxy,TREventInfo> mEvents;

}
