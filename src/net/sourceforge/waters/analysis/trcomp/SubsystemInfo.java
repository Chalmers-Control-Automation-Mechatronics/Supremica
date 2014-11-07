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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.DFSSearchSpace;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
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
    for (final TRAutomatonProxy aut : automata) {
      registerEvents(aut);
    }
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


  //#########################################################################
  //# Set up
  void registerEvents(final TRAutomatonProxy aut)
  {
    final EventEncoding eventEnc = aut.getEventEncoding();
    final int numEvents = eventEnc.getNumberOfProperEvents();
    final byte[] eventStatus = new byte[numEvents];
    Arrays.fill(eventStatus, (byte) (EventStatus.STATUS_BLOCKED |
                                     EventStatus.STATUS_FAILING |
                                     EventStatus.STATUS_OUTSIDE_ONLY_SELFLOOP));
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
    while (iter.advance()) {
      final int e = iter.getCurrentEvent();
      final int s = iter.getCurrentSourceState();
      final int t = iter.getCurrentTargetState();
      if (rel.isDeadlockState(t, TRCompositionalConflictChecker.DEFAULT_MARKING)) {
        eventStatus[e] &= EventStatus.STATUS_FAILING;
      } else if (s == t) {
        eventStatus[e] &= EventStatus.STATUS_OUTSIDE_ONLY_SELFLOOP;
      } else {
        eventStatus[e] = 0;
      }
    }
    for (int e = EventEncoding.TAU; e < numEvents; e++) {
      final byte status = eventEnc.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status)) {
        final EventProxy event = eventEnc.getProperEvent(e);
        final TREventInfo info = createEventInfo(event);
        info.addAutomaton(aut, eventStatus[e]);
      }
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
        } else if (!automataQueue.isEmpty()) {
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
        } else {
          final TREventInfo info = eventQueue.poll();
          automataQueue.addAll(info.getAutomata());
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
  private TREventInfo createEventInfo(final EventProxy event)
  {
    TREventInfo info = mEvents.get(event);
    if (info == null) {
      info = new TREventInfo(event);
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
