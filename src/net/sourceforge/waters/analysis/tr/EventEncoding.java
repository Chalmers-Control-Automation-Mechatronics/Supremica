//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   EventEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import gnu.trove.TByteArrayList;
import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.EventKind;


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
 * Codes are assigned independently to proper events (non-propositions) and
 * propositions. This means that a given integer number may represent two
 * events, a proper event and a proposition.
 *
 * Silent (tau) events are handled specially. The proper event code
 * {@link #TAU}&nbsp;(0) is reserved for the silent event in every encoding,
 * even if no silent event is used. Multiple silent events can be specified to
 * implement hiding.
 *
 * @see ListBufferTransitionRelation
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
   * @param  aut         The automaton to be encoded.
   * @param  translator  Kind translator to distinguish propositions from
   *                     proper events.
   */
  public EventEncoding(final AutomatonProxy aut,
                       final KindTranslator translator)
  {
    this(aut, translator, null);
  }

  /**
   * Creates a new event encoding for the given automaton.
   * This method creates an event encoding for all events in the
   * given automaton.
   * @param  aut         The automaton to be encoded.
   * @param  translator  Kind translator to distinguish propositions from
   *                     proper events.
   * @param  tau         The silent event to be used,
   *                     or <CODE>null</CODE> if none is to be configured.
   */
  public EventEncoding(final AutomatonProxy aut,
                       final KindTranslator translator,
                       final EventProxy tau)
  {
    this(aut, translator, tau, null, FILTER_NONE);
  }

  /**
   * Creates a new event encoding for the given automaton.
   * This method creates an event encoding without any silent event.
   * @param  aut         The automaton to be encoded.
   * @param  translator  Kind translator to distinguish propositions from
   *                     proper events.
   * @param  filter      A collection of events to restrict the encoding to.
   * @param  filterMode  Flags defining how the filter is to be used,
   *                     should be one of {@link #FILTER_PROPER_EVENTS},
   *                     {@link #FILTER_PROPOSITIONS}, or {@link #FILTER_ALL}.
   */
  public EventEncoding(final AutomatonProxy aut,
                       final KindTranslator translator,
                       final Collection<EventProxy> filter,
                       final int filterMode)
  {
    this(aut.getEvents(), translator, filter, filterMode);
  }

  /**
   * Creates a new event encoding for the given automaton.
   * @param  aut         The automaton to be encoded.
   * @param  translator  Kind translator to distinguish propositions from
   *                     proper events.
   * @param  tau         The silent event to be used,
   *                     or <CODE>null</CODE> if none is to be configured.
   * @param  filter      A collection of events to restrict the encoding to.
   * @param  filterMode  Flags defining how the filter is to be used,
   *                     should be one of {@link #FILTER_PROPER_EVENTS},
   *                     {@link #FILTER_PROPOSITIONS}, or {@link #FILTER_ALL}.
   */
  public EventEncoding(final AutomatonProxy aut,
                       final KindTranslator translator,
                       final EventProxy tau,
                       final Collection<EventProxy> filter,
                       final int filterMode)
  {
    this(aut.getEvents(), translator, tau, filter, filterMode);
  }

  /**
   * Creates an empty encoding.
   * This method creates an event encoding without any events or
   * propositions. Events can be added using {@link
   * #addEvent(EventProxy,KindTranslator,byte) addEvent()} or {@link
   * #addSilentEvent(EventProxy) addSilentEvent()}.
   */
  public EventEncoding()
  {
    this(NO_EVENTS, null);
  }

  /**
   * Creates a new event encoding.
   * This method creates an event encoding consisting of all the given
   * events, and with no silent event.
   * @param  events      Collection of events constituting the new encoding.
   *                     Codes are assigned in the order given by the
   *                     collection, with code {@link #TAU}&nbsp;(0) reserved
   *                     to the silent event, even though it is not used.
   * @param  translator  Kind translator to distinguish propositions from
   *                     proper events.
   */
  public EventEncoding(final Collection<EventProxy> events,
                       final KindTranslator translator)
  {
    this(events, translator, null, FILTER_NONE);
  }

  /**
   * Creates a new event encoding.
   * This method creates an event encoding consisting of all the given events.
   * @param  events      Collection of events constituting the new encoding.
   *                     Codes are assigned in the order given by the
   *                     collection, with code {@link #TAU}&nbsp;(0) reserved
   *                     to the silent event, even if it is not used.
   * @param  translator  Kind translator to distinguish propositions from
   *                     proper events.
   * @param  tau         The silent event to be used,
   *                     or <CODE>null</CODE> if none is to be configured.
   */
  public EventEncoding(final Collection<EventProxy> events,
                       final KindTranslator translator,
                       final EventProxy tau)
  {
    this(events, translator, tau, null, FILTER_NONE);
  }

  /**
   * Creates a new event encoding.
   * This method creates an event encoding without any silent event.
   * @param  events      Collection of events constituting the new encoding.
   *                     Codes are assigned in the order given by the
   *                     collection, with code {@link #TAU}&nbsp;(0) reserved
   *                     to the silent event, even though it is not used.
   * @param  translator  Kind translator to distinguish propositions from
   *                     proper events.
   * @param  filter      A collection of events to restrict the encoding to.
   * @param  filterMode  Flags defining how the filter is to be used,
   *                     should be one of {@link #FILTER_PROPER_EVENTS},
   *                     {@link #FILTER_PROPOSITIONS}, or {@link #FILTER_ALL}.
   */
  public EventEncoding(final Collection<EventProxy> events,
                       final KindTranslator translator,
                       final Collection<EventProxy> filter,
                       final int filterMode)
  {
    this(events, translator, null, filter, filterMode);
  }

  /**
   * Creates a new event encoding.
   * @param  events      Collection of events constituting the new encoding.
   *                     Codes are assigned in the order given by the
   *                     collection, with code {@link #TAU}&nbsp;(0) reserved
   *                     to the silent event, even if it is not used.
   * @param  translator  Kind translator to distinguish propositions from
   *                     proper events.
   * @param  tau         The silent event to be used,
   *                     or <CODE>null</CODE> if none is to be configured.
   * @param  filter      A collection of events to restrict the encoding to.
   * @param  filterMode  Flags defining how the filter is to be used,
   *                     should be one of {@link #FILTER_PROPER_EVENTS},
   *                     {@link #FILTER_PROPOSITIONS}, or {@link #FILTER_ALL}.
   */
  public EventEncoding(final Collection<EventProxy> events,
                       final KindTranslator translator,
                       final EventProxy tau,
                       final Collection<EventProxy> filter,
                       final int filterMode)
  {
    final int numEvents = events.size();
    mProperEvents = new ArrayList<EventProxy>(numEvents);
    mPropositions = new ArrayList<EventProxy>(numEvents);
    mProperEventStatus = new TByteArrayList(numEvents);
    mPropositionStatus = new TByteArrayList(numEvents);
    mEventCodeMap = new TObjectIntHashMap<EventProxy>(numEvents);
    mProperEvents.add(tau);
    mProperEventStatus.add((byte)0);
    if (tau != null) {
      mEventCodeMap.put(tau, TAU);
    }
    for (final EventProxy event : events) {
      if (event != tau) {
        byte status = 0;
        switch (translator.getEventKind(event)) {
        case CONTROLLABLE:
          status = STATUS_CONTROLLABLE;
        case UNCONTROLLABLE:
          if ((filterMode & FILTER_PROPER_EVENTS) == 0 ||
              filter.contains(event)) {
            final int e = mProperEvents.size();
            mEventCodeMap.put(event, e);
            mProperEvents.add(event);
            mProperEventStatus.add(status);
          }
          break;
        case PROPOSITION:
          if ((filterMode & FILTER_PROPOSITIONS) == 0 ||
              filter.contains(event)) {
            final int p = mPropositions.size();
            mEventCodeMap.put(event, p);
            mPropositions.add(event);
            mPropositionStatus.add((byte) 0);
          }
          break;
        default:
          break;
        }
      }
    }
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    final StringBuffer buffer = new StringBuffer("{");
    int ecode = 0;
    EventProxy tau = null;
    if (mProperEvents != null) {
      for (final EventProxy event : mProperEvents) {
        if (ecode > 0) {
          buffer.append(", ");
        } else {
          tau = event;
        }
        buffer.append(ecode++);
        buffer.append('=');
        buffer.append(event == null ? "(null)" : event.getName());
        //Replace with complete status info
      //  if (mExtraSelfLoops != null && mExtraSelfLoops.contains(event)) {
      //   buffer.append('+');
      //  }
      }
    }
    final TObjectIntIterator<EventProxy> iter = mEventCodeMap.iterator();
    while (iter.hasNext()) {
      iter.advance();
      final EventProxy event = iter.key();
      final int code = iter.value();
      if (code == TAU && event != tau &&
          (event == null || event.getKind() != EventKind.PROPOSITION)) {
        if (ecode > 0) {
          buffer.append(", ");
        } else {
          ecode = 1;
        }
        buffer.append(code);
        buffer.append('=');
        buffer.append(event == null ? "(null)" : event.getName());
      //  if (mExtraSelfLoops != null && mExtraSelfLoops.contains(event)) {
      //    buffer.append('+');
      //  }
      }
    }
    if (mPropositions != null) {
      int pcode = 0;
      for (final EventProxy prop : mPropositions) {
        if (pcode > 0) {
          buffer.append(", ");
        } else if (ecode > 0) {
          buffer.append("; ");
        }
        buffer.append(pcode++);
        buffer.append('=');
        buffer.append(prop == null ? "(null)" : prop.getName());
       // if (mExtraSelfLoops != null && mExtraSelfLoops.contains(prop)) {
       //   buffer.append('+');
       // }
      }
    }
    buffer.append('}');
    return buffer.toString();
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the total number of events including propositions
   * in this encoding. The number of events always includes the silent (tau)
   * event, even if none has been specified.
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
   * @param  event       The event to be added, which may be a proper event
   *                     or a proposition.
   * @param  translator  Kind translator to distinguish propositions from
   *                     proper events.
   * @param  status      Collection of status flags providing additional
   *                     information about the event.
   * @return The event (or proposition) code that was assigned to the event.
   */
  public int addEvent(final EventProxy event,
                      final KindTranslator translator,
                      byte status)
  {
    final int code;
    switch (translator.getEventKind(event)) {
    case CONTROLLABLE:
      status |= STATUS_CONTROLLABLE;
    case UNCONTROLLABLE:
      code = mProperEvents.size();
      mEventCodeMap.put(event, code);
      mProperEvents.add(event);
      mProperEventStatus.add(status);
      break;
    case PROPOSITION:
      code = mPropositions.size();
      mEventCodeMap.put(event, code);
      mPropositions.add(event);
      mPropositionStatus.add(status);
      break;
    default:
      throw new IllegalArgumentException
        ("Unknown event kind " + event.getKind() + "!");
    }
    return code;
  }

  public byte getPropositionStatus(final int event)
  {
    return mPropositionStatus.get(event);
  }
  public void setPropositionStatus(final int event, final byte status)
  {
    mPropositionStatus.set(event, status);
  }

  public byte getProperEventStatus(final int event)
  {
    return mProperEventStatus.get(event);
  }
  public void setProperEventStatus(final int event, final byte status)
  {
    mProperEventStatus.set(event, status);
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
  private final TByteArrayList mProperEventStatus;
  private final TByteArrayList mPropositionStatus;

  //#########################################################################
  //# Class Constants
  /**
   * The code for the silent event used by all encodings and transition
   * relations. The value of TAU is 0 (zero), so the silent event appears
   * first in every encoding.
   */
  public static final int TAU = 0;
  /**
   * The code of the first non-silent event.
   */
  public static final int NONTAU = TAU + 1;

  /**
   * Filter mode defining that the filter list should be ignored.
   * All events in the input event list will be copied to the encoding.
   */
  public static final int FILTER_NONE = 0;
  /**
   * Filter mode defining that the filter list should only be used to
   * restrict proper events, i.e., non-proposition events. All proposition
   * events in the input event list will be copied to the encoding.
   * Proper events will only be included if they appear in the filter.
   */
  public static final int FILTER_PROPER_EVENTS = 0x01;
  /**
   * Filter mode defining that the filter list should only be used to
   * restrict proposition events. All proper, i.e., non-proposition
   * events in the input event list will be copied to the encoding.
   * Proposition events will only be included if they appear in the filter.
   */
  public static final int FILTER_PROPOSITIONS = 0x02;
  /**
   * Filter mode defining that the filter list should be applied to all
   * events. Events in the input event list will only be copied to the
   * encoding if they appear in the filter.
   */
  public static final int FILTER_ALL =
    FILTER_PROPER_EVENTS | FILTER_PROPOSITIONS;

  public static final byte STATUS_EXTRA_SELFLOOP = 0x01;
  public static final byte STATUS_CONTROLLABLE = 0x02;
  public static final byte STATUS_OUTSIDE_ONLY_SELFLOOP = 0x04;
  public static final byte STATUS_OUTSIDE_ALWAYS_ENABLED = 0x08;

  private static final Collection<EventProxy> NO_EVENTS =
    Collections.emptySet();

}
