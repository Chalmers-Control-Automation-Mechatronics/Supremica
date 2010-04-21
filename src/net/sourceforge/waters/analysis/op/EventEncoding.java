//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   EventEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gnu.trove.TObjectIntHashMap;

import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author Robi Malik
 */

public class EventEncoding
{

  //#########################################################################
  //# Constructor
  public EventEncoding(final AutomatonProxy aut)
  {
    this(aut, null);
  }

  public EventEncoding(final AutomatonProxy aut, final EventProxy tau)
  {
    this(aut, tau, null, FILTER_NONE);
  }

  public EventEncoding(final AutomatonProxy aut,
                       final Collection<EventProxy> filter,
                       final int filterMode)
  {
    this(aut.getEvents(), filter, filterMode);
  }

  public EventEncoding(final AutomatonProxy aut,
                       final EventProxy tau,
                       final Collection<EventProxy> filter,
                       final int filterMode)
  {
    this(aut.getEvents(), tau, filter, filterMode);
  }

  public EventEncoding(final Collection<EventProxy> events)
  {
    this(events, null, FILTER_NONE);
  }

  public EventEncoding(final Collection<EventProxy> events,
                       final EventProxy tau)
  {
    this(events, tau, null, FILTER_NONE);
  }

  public EventEncoding(final Collection<EventProxy> events,
                       final Collection<EventProxy> filter,
                       final int filterMode)
  {
    this(events, null, filter, filterMode);
  }

  public EventEncoding(final Collection<EventProxy> events,
                       final EventProxy tau,
                       final Collection<EventProxy> filter,
                       final int filterMode)
  {
    final int numEvents = events.size();
    mProperEvents = new ArrayList<EventProxy>(numEvents);
    mPropositions = new ArrayList<EventProxy>(numEvents);
    mEventCodeMap = new TObjectIntHashMap<EventProxy>(numEvents);
    mProperEvents.add(tau);
    if (tau != null) {
      mEventCodeMap.put(tau, TAU);
    }
    for (final EventProxy event : events) {
      if (event != tau) {
        switch (event.getKind()) {
        case CONTROLLABLE:
        case UNCONTROLLABLE:
          if ((filterMode & FILTER_PROPER_EVENTS) == 0 ||
              filter.contains(event)) {
            final int e = mProperEvents.size();
            mEventCodeMap.put(event, e);
            mProperEvents.add(event);
          }
          break;
        case PROPOSITION:
          if ((filterMode & FILTER_PROPOSITIONS) == 0 ||
              filter.contains(event)) {
            final int p = mPropositions.size();
            mEventCodeMap.put(event, p);
            mPropositions.add(event);
          }
          break;
        default:
          break;
        }
      }
    }
    mExtraSelfLoops = null;
  }


  //#########################################################################
  //# Simple Access
  public int getNumberOfEvents()
  {
    return getNumberOfProperEvents() + getNumberOfPropositions();
  }

  public int getNumberOfProperEvents()
  {
    if (mProperEvents.get(TAU) != null) {
      return mProperEvents.size();
    } else {
      return mProperEvents.size() - 1;
    }
  }

  public int getNumberOfPropositions()
  {
    return mPropositions.size();
  }

  public int getEventCode(final EventProxy event)
  {
    if (mEventCodeMap.containsKey(event)) {
      return mEventCodeMap.get(event);
    } else {
      return -1;
    }
  }

  public EventProxy getTauEvent()
  {
    return mProperEvents.get(TAU);
  }

  public EventProxy getProperEvent(final int code)
  {
    final EventProxy event = mProperEvents.get(code);
    if (event == null) {
      throw new IndexOutOfBoundsException("No silent (tau) event defined in " +
                                          ProxyTools.getShortClassName(this) +
                                          "!");
    }
    return event;
  }

  public EventProxy getProposition(final int code)
  {
    return mPropositions.get(code);
  }

  public List<EventProxy> getEvents()
  {
    final int numEvents = getNumberOfEvents();
    final List<EventProxy> list = new ArrayList<EventProxy>(numEvents);
    for (final EventProxy event : mProperEvents) {
      if (event != null) {
        list.add(event);
      }
    }
    list.addAll(mPropositions);
    return list;
  }


  //#########################################################################
  //# Alphabet extension
  public int addEvent(final EventProxy event, final boolean selfloop)
  {
    final int code;
    switch (event.getKind()) {
    case CONTROLLABLE:
    case UNCONTROLLABLE:
      code = mProperEvents.size();
      mEventCodeMap.put(event, code);
      mProperEvents.add(event);
      break;
    case PROPOSITION:
      code = mPropositions.size();
      mEventCodeMap.put(event, code);
      mPropositions.add(event);
      break;
    default:
      throw new IllegalArgumentException
        ("Unknown event kind " + event.getKind() + "!");
    }
    if (selfloop) {
      if (mExtraSelfLoops == null) {
        mExtraSelfLoops = new ArrayList<EventProxy>();
      }
      mExtraSelfLoops.add(event);
    }
    return code;
  }

  public List<EventProxy> getExtraSelfloops()
  {
    return mExtraSelfLoops;
  }

  public void addSilentEvent(final EventProxy event)
  {
    if (mProperEvents.get(TAU) == null) {
      mProperEvents.set(TAU, event);
    }
    mEventCodeMap.put(event, TAU);
  }


  //#########################################################################
  //# Data Members
  private final List<EventProxy> mProperEvents;
  private final List<EventProxy> mPropositions;
  private final TObjectIntHashMap<EventProxy> mEventCodeMap;
  private List<EventProxy> mExtraSelfLoops;


  //#########################################################################
  //# Class Constants
  public static final int TAU = 0;

  public static final int FILTER_NONE = 0;
  public static final int FILTER_PROPER_EVENTS = 0x01;
  public static final int FILTER_PROPOSITIONS = 0x02;
  public static final int FILTER_ALL =
    FILTER_PROPER_EVENTS | FILTER_PROPOSITIONS;

}
