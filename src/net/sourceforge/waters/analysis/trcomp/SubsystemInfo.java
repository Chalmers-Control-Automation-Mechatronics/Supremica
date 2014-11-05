//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   SubsystemInfo
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

  private TREventInfo createEventInfo(final EventProxy event)
  {
    TREventInfo info = mEvents.get(event);
    if (info == null) {
      info = new TREventInfo(event);
      mEvents.put(event, info);
    }
    return info;
  }


  //#########################################################################
  //# Data Members
  @SuppressWarnings("unused")
  private final Collection<TRAutomatonProxy> mAutomata;
  private final Map<EventProxy,TREventInfo> mEvents;

}
