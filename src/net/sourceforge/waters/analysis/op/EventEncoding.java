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

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * An event encoding for transition relations.
 *
 * The event encoding assigns to each event of an automaton an integer
 * used as its code for indexing or compact storage. The class provides
 * methods to find the event codes for given {@link EventProxy} objects
 * and versa.
 *
 * Event codes are assigned in a deterministic manner depending only on
 * the order in which they are found in the input. There is support to
 * add events to an encoding after it is created. However, transition
 * relations are not expected to store the event encoding after creation,
 * so changes to the event encoding will not affect any transition relations
 * that have been created.
 *
 * Separate codes are assigned to proper events (non-propositions) and
 * propositions. This means that a given integer number may represent two
 * events, a proper event and a proposition.
 *
 * Silent (tau) events are handled specially. The proper event code
 * {@link #TAU} is reserved for the silent event in every encoding, even
 * if no silent event is used. Multiple silent events can be specified to
 * implement hiding.
 *
 * @see {@link ListBufferTransitionRelation}
 *
 * @author Robi Malik
 */

public class EventEncoding
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new event encoding for the given automaton.
   * This method creates an event encoding for all events in the
   * given automaton, and with no silent event.
   */
  public EventEncoding(final AutomatonProxy aut)
  {
    this(aut, null);
  }

  /**
   * Creates a new event encoding for the given automaton.
   * This method creates an event encoding for all events in the
   * given automaton.
   * @param  aut     The automaton to be encoded.
   * @param  tau     The silent event to be used,
   *                 or <CODE>null</CODE> if none is to be configured.
   */
  public EventEncoding(final AutomatonProxy aut, final EventProxy tau)
  {
    this(aut, tau, null, FILTER_NONE);
  }

  /**
   * Creates a new event encoding for the given automaton.
   * This method creates an event encoding without any silent event.
   * @param  aut         The automaton to be encoded.
   * @param  filter      A collection of events to restrict the encoding to.
   * @param  filterMode  Flags defining how the filter is to be used,
   *                     should be one of {@link #FILTER_PROPER_EVENTS},
   *                     {@link #FILTER_PROPOSITIONS}, or {@link #FILTER_ALL}.
   */
  public EventEncoding(final AutomatonProxy aut,
                       final Collection<EventProxy> filter,
                       final int filterMode)
  {
    this(aut.getEvents(), filter, filterMode);
  }

  /**
   * Creates a new event encoding for the given automaton.
   * @param  aut         The automaton to be encoded.
   * @param  tau         The silent event to be used,
   *                     or <CODE>null</CODE> if none is to be configured.
   * @param  filter      A collection of events to restrict the encoding to.
   * @param  filterMode  Flags defining how the filter is to be used,
   *                     should be one of {@link #FILTER_PROPER_EVENTS},
   *                     {@link #FILTER_PROPOSITIONS}, or {@link #FILTER_ALL}.
   */
  public EventEncoding(final AutomatonProxy aut,
                       final EventProxy tau,
                       final Collection<EventProxy> filter,
                       final int filterMode)
  {
    this(aut.getEvents(), tau, filter, filterMode);
  }

  /**
   * Creates a new event encoding.
   * This method creates an event encoding consisting of all the given
   * events, and with no silent event.
   */
  public EventEncoding(final Collection<EventProxy> events)
  {
    this(events, null, FILTER_NONE);
  }

  /**
   * Creates a new event encoding.
   * This method creates an event encoding consisting of all the given events.
   * @param  events      Collection of events constituting the new encoding.
   * @param  tau         The silent event to be used,
   *                     or <CODE>null</CODE> if none is to be configured.
   */
  public EventEncoding(final Collection<EventProxy> events,
                       final EventProxy tau)
  {
    this(events, tau, null, FILTER_NONE);
  }

  /**
   * Creates a new event encoding.
   * This method creates an event encoding without any silent event.
   * @param  events      Collection of events constituting the new encoding.
   * @param  filter      A collection of events to restrict the encoding to.
   * @param  filterMode  Flags defining how the filter is to be used,
   *                     should be one of {@link #FILTER_PROPER_EVENTS},
   *                     {@link #FILTER_PROPOSITIONS}, or {@link #FILTER_ALL}.
   */
  public EventEncoding(final Collection<EventProxy> events,
                       final Collection<EventProxy> filter,
                       final int filterMode)
  {
    this(events, null, filter, filterMode);
  }

  /**
   * Creates a new event encoding.
   * @param  events      Collection of events constituting the new encoding.
   * @param  tau         The silent event to be used,
   *                     or <CODE>null</CODE> if none is to be configured.
   * @param  filter      A collection of events to restrict the encoding to.
   * @param  filterMode  Flags defining how the filter is to be used,
   *                     should be one of {@link #FILTER_PROPER_EVENTS},
   *                     {@link #FILTER_PROPOSITIONS}, or {@link #FILTER_ALL}.
   */
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
  /**
   * Gets the total number of events in this encoding.
   * The number of events always includes the silent (tau) event,
   * even if none has been specified.
   */
  public int getNumberOfEvents()
  {
    return getNumberOfProperEvents() + getNumberOfPropositions();
  }

  /**
   * Gets the number of proper events, i.e., non-proposition events,
   * in this encoding. The number of proper events always includes the
   * silent (tau) event, even if none has been specified.
   */
  public int getNumberOfProperEvents()
  {
    return mProperEvents.size();
  }

  /**
   * Gets the number of proposition events in this encoding.
   */
  public int getNumberOfPropositions()
  {
    return mPropositions.size();
  }

  /**
   * Returns the event code for the given event.
   * @return The integer encoding the given event, or <CODE>-1</CODE>
   *         if the event is not in the encoding.
   */
  public int getEventCode(final EventProxy event)
  {
    if (mEventCodeMap.containsKey(event)) {
      return mEventCodeMap.get(event);
    } else {
      return -1;
    }
  }

  /**
   * Gets the silent (tau) event of this encoding.
   * @return The event representing the silent event, or <CODE>null</CODE>
   *         if none was specified.
   */
  public EventProxy getTauEvent()
  {
    return mProperEvents.get(TAU);
  }

  /**
   * Gets the proper event, i.e., non-proposition event, with the given code.
   * @param  code   Code of proper event to be looked up.
   *                Must be in the range from 0 to
   *                {@link #getNumberOfProperEvents()}-1.
   * @return The proper event assigned to the given code, or <CODE>null</CODE>
   *         if the code is {@link #TAU}, and no silent event was specified.
   */
  public EventProxy getProperEvent(final int code)
  {
    return mProperEvents.get(code);
  }

  /**
   * Gets the proposition event with the given code.
   * @param  code   Code of proposition event to be looked up.
   *                Must be in the range from 0 to
   *                {@link #getNumberOfPropositions()}-1.
   * @return The proposition event assigned to the given code.
   */
  public EventProxy getProposition(final int code)
  {
    return mPropositions.get(code);
  }

  /**
   * Gets a list containing all events in this encoding.
   * The list returned contains all proper events and propositions;
   * the silent (tau) event is only included if it was specified.
   */
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
  /**
   * Adds an event to this encoding. This method enlarges the event encoding
   * with a new event, assigning a new code.
   * @param  event    The event to be added, which may be a proper event
   *                  or a proposition.
   * @param  selfloop A flag, indicating whether the additional event should
   *                  be selflooped (or marked, for propositions) in all states
   *                  of an automaton using this encoding. This information
   *                  is stored on the event encoding and needs to be read
   *                  by any procedures creating transition relations using
   *                  the encoding.
   * @return The event (or proposition) code that was assigned to the event.
   * @see {@link #getExtraSelfloops()}
   */
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

  /**
   * Gets the list of all events that are considered selflooped in all states
   * by this encoding. Procedures building up transition relations query this
   * method to add additional selfloops.
   * @return The list of events and propositions passed into the
   *         {@link #addEvent(EventProxy, boolean) addEvent()} with the
   *         selfloop parameter set to true.
   * @see {@link #addEvent(EventProxy, boolean) addEvent()}
   */
  public List<EventProxy> getExtraSelfloops()
  {
    return mExtraSelfLoops;
  }

  /**
   * <P>Adds a silent event to this event encoding.</P>
   * <P>If the encoding does not yet have any silent event, the new event is
   * added to the encoding and bound to the silent event code {@link #TAU}.</P>
   * <P>If the encoding already has a silent event, the new event is mapped
   * to the silent event code {@link #TAU}, without adding it to the
   * encoding. This provides a simple means to implement hiding: by adding
   * a silent event followed by another set events to be hidden. all these
   * events will be encoded as {@link #TAU}, and can later all be decoded to
   * the first silent event that was added.</P>
   * @param  event    A new silent event.
   */
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
  /**
   * The code for the silent event used by all encodings and transition
   * relations. The value of TAU is 0 (zero), so the silent event appears
   * first in every encoding.
   */
  public static final int TAU = 0;

  /**
   * Filter mode defining that the filter list should be ignored.
   * All events in the input event list will be copied to the encoding.
   */
  public static final int FILTER_NONE = 0;
  /**
   * Filter mode defining that the filter list should only be used to
   * restrict proper events, i.e., non-proposition events. All proposition
   * events in the input event list will be copied to the encoding.
   * Proper events will only be included if the appear in the filter.
   */
  public static final int FILTER_PROPER_EVENTS = 0x01;
  /**
   * Filter mode defining that the filter list should only be used to
   * restrict proposition events. All proper, i.e., non-proposition
   * events in the input event list will be copied to the encoding.
   * Proposition events will only be included if the appear in the filter.
   */
  public static final int FILTER_PROPOSITIONS = 0x02;
  /**
   * Filter mode defining that the filter list should be applied to all
   * events. Events in the input event list will only be copied to the
   * encoding if the appear in the filter.
   */
  public static final int FILTER_ALL =
    FILTER_PROPER_EVENTS | FILTER_PROPOSITIONS;

}
