//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   EventEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
    if (tau == null) {
      mProperEventStatus.add((byte) (STATUS_FULLY_LOCAL | STATUS_UNUSED));
    } else {
      mProperEventStatus.add(STATUS_FULLY_LOCAL);
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
            mPropositionStatus.add(STATUS_NONE);
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
        buffer.append(ecode);
        buffer.append('=');
        buffer.append(event == null ? "(null)" : event.getName());
        final byte status = mProperEventStatus.get(ecode++);
        appendStatusInfo(buffer, status);
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
        final byte status = mProperEventStatus.get(code);
        appendStatusInfo(buffer, status);
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
        buffer.append(pcode);
        buffer.append('=');
        buffer.append(prop == null ? "(null)" : prop.getName());
        final byte status = mPropositionStatus.get(pcode++);
        appendStatusInfo(buffer, status);
      }
    }
    buffer.append('}');
    return buffer.toString();
  }

  private void appendStatusInfo(final StringBuffer buffer, final byte status)
  {
    if (status != STATUS_NONE) {
      char sep = '<';
      byte bit = 1;
      for (final String name : STATUS_NAMES) {
        if ((status & bit) != 0) {
          buffer.append(sep);
          sep = ',';
          buffer.append(name);
        }
        bit <<= 1;
      }
      buffer.append('>');
    }
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
   * Returns the event code for the given event or proposition.
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

  /**
   * Gets the current ordering information for this event encoding.
   * If the event encoding has been ordered, the ordering information
   * can be used to iterate over events more efficiently.
   * @return Ordering information if available,
   *         <CODE>null</CODE> otherwise.
   * @see #sortProperEvents(byte...) sortProperEvents()
   */
  public OrderingInfo getOrderInfo()
  {
    return mOrderingInfo;
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
   *                     information about the event. Should be a combination
   *                     of the bits {@link #STATUS_CONTROLLABLE},
   *                     {@link #STATUS_LOCAL},
   *                     {@link #STATUS_OUTSIDE_ALWAYS_ENABLED},
   *                     {@link #STATUS_OUTSIDE_ONLY_SELFLOOP}, and
   *                     {@link #STATUS_UNUSED}.
   * @return The event (or proposition) code that was assigned to the event.
   */
  public int addEvent(final EventProxy event,
                      final KindTranslator translator,
                      int status)
  {
    final int code;
    switch (translator.getEventKind(event)) {
    case CONTROLLABLE:
      status |= STATUS_CONTROLLABLE;
    case UNCONTROLLABLE:
      code = mProperEvents.size();
      mEventCodeMap.put(event, code);
      mProperEvents.add(event);
      mProperEventStatus.add((byte) status);
      break;
    case PROPOSITION:
      code = mPropositions.size();
      mEventCodeMap.put(event, code);
      mPropositions.add(event);
      mPropositionStatus.add((byte) status);
      break;
    default:
      throw new IllegalArgumentException
        ("Unknown event kind " + event.getKind() + "!");
    }
    return code;
  }

  /**
   * Retrieves the status flags for the given proper event.
   * @param  event  Code of the proper event to be looked up.
   *                Must be in the range from 0 to
   *                {@link #getNumberOfProperEvents()}-1.
   * @return A combination of the bits {@link #STATUS_CONTROLLABLE},
   *         {@link #STATUS_OUTSIDE_ALWAYS_ENABLED},
   *         {@link #STATUS_OUTSIDE_ONLY_SELFLOOP}, and
   *         {@link #STATUS_UNUSED}.
   */
  public byte getProperEventStatus(final int event)
  {
    return mProperEventStatus.get(event);
  }

  /**
   * Assigns new status flags to the given proper event.
   * @param  event  Code of the proper event to be modified.
   *                Must be in the range from 0 to
   *                {@link #getNumberOfProperEvents()}-1.
   * @param  status A combination of the bits {@link #STATUS_CONTROLLABLE},
   *                {@link #STATUS_OUTSIDE_ALWAYS_ENABLED},
   *                {@link #STATUS_OUTSIDE_ONLY_SELFLOOP}, and
   *                {@link #STATUS_UNUSED}.
   */
  public void setProperEventStatus(final int event, final int status)
  {
    mProperEventStatus.set(event, (byte) status);
  }

  /**
   * Retrieves the status flags for the given proposition.
   * @param  prop   Code of the proposition to be looked up.
   *                Must be in the range from 0 to
   *                {@link #getNumberOfPropositions()}-1.
   * @return A combination of the bits {@link #STATUS_CONTROLLABLE},
   *         {@link #STATUS_OUTSIDE_ALWAYS_ENABLED},
   *         {@link #STATUS_OUTSIDE_ONLY_SELFLOOP}, and
   *         {@link #STATUS_UNUSED}.
   */
  public byte getPropositionStatus(final int prop)
  {
    return mPropositionStatus.get(prop);
  }

  /**
   * Assigns new status flags to the given proposition.
   * @param  prop   Code of the proposition to be modified.
   *                Must be in the range from 0 to
   *                {@link #getNumberOfPropositions()}-1.
   * @param  status A combination of the bits {@link #STATUS_CONTROLLABLE},
   *                {@link #STATUS_OUTSIDE_ALWAYS_ENABLED},
   *                {@link #STATUS_OUTSIDE_ONLY_SELFLOOP}, and
   *                {@link #STATUS_UNUSED}.
   */
  public void setPropositionStatus(final int prop, final int status)
  {
    mPropositionStatus.set(prop, (byte) status);
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
   * @param  event  A new silent event.
   */
  public void addSilentEvent(final EventProxy event)
  {
    if (mProperEvents.get(TAU) == null) {
      mProperEvents.set(TAU, event);
      final byte status = mProperEventStatus.get(TAU);
      mProperEventStatus.set(TAU, (byte) (status & ~STATUS_UNUSED));
    }
    mEventCodeMap.put(event, TAU);
  }

  /**
   * Reorders the proper events in this encoding based on their status.
   * @param  flags  List of flags to define the ordering, represented by
   *                a sequence of the bits or bit combinations
   *                {@link #STATUS_CONTROLLABLE}, {@link #STATUS_LOCAL},
   *                {@link #STATUS_OUTSIDE_ALWAYS_ENABLED},
   *                {@link #STATUS_OUTSIDE_ONLY_SELFLOOP}, and
   *                {@link #STATUS_UNUSED} or their complements.<BR>
   *                For example, to sort events by controllability first
   *                and second by locality, two arguments STATUS_CONTROLLABLE
   *                and&nbsp;STATUS_LOCAL are passed into the method.<BR>
   *                This considers uncontrollable events as "smaller" than
   *                controllable events. To consider controllable events as
   *                "smaller", ~STATUS_CONTROLLABLE is used as the first
   *                argument.
   * @return Ordering information that can be used to iterate over events
   *         of a certain type based on the new ordering.
   */
  public OrderingInfo sortProperEvents(final byte... flags)
  {
    final EventOrdering ordering = new EventOrdering(flags);
    ordering.sortProperEvents();
    return mOrderingInfo = new OrderingInfo(mProperEventStatus, flags);
  }


  //#########################################################################
  //# Event Status
  /**
   * Returns whether the given event status bits identify an event as
   * a controllable in an event encoding.
   */
  public static boolean isControllableEvent(final byte status)
  {
    return (status & STATUS_CONTROLLABLE) != 0;
  }

  /**
   * Returns whether the given event status bits identify an event as
   * a local event in an event encoding.
   */
  public static boolean isLocalEvent(final byte status)
  {
    return (status & STATUS_LOCAL) != 0;
  }

  /**
   * Returns whether the given event status bits identify an event as
   * outside only-selfloop in an event encoding.
   * @see #STATUS_OUTSIDE_ONLY_SELFLOOP
   */
  public static boolean isOutsideOnlySelfloopEvent(final byte status)
  {
    return (status & STATUS_OUTSIDE_ONLY_SELFLOOP) != 0;
  }

  /**
   * Returns whether the given event status bits identify an event as
   * outside always enabled in an event encoding.
   * @see #STATUS_OUTSIDE_ALWAYS_ENABLED
   */
  public static boolean isOutsideAlwaysEnabledEvent(final byte status)
  {
    return (status & STATUS_OUTSIDE_ALWAYS_ENABLED) != 0;
  }

  /**
   * Returns whether the given event status bits identify an event as
   * used (i.e., not unused) in an event encoding.
   * @see #STATUS_UNUSED
   */
  public static boolean isUsedEvent(final byte status)
  {
    return (status & STATUS_UNUSED) == 0;
  }


  //#########################################################################
  //# Inner Class OrderingInfo
  /**
   * Ordering information for an {@link EventEncoding}.
   * The ordering information records the positions of the events
   * with the specific status flags in an encoding sorted by event
   * status. This can be used to iterate over events with specific
   * type more efficiently.
   * @see EventEncoding#sortProperEvents(byte...)
   * @see TransitionIterator#resetEvents(int, int)
   */
  public static class OrderingInfo
  {
    //#######################################################################
    //# Constructor
    /**
     * Creates new ordering information.
     * @param  statusArrayList  Array list containing event status bytes,
     *                          contains one entry for each event.
     * @param  flags            Ordering flags used to establish the
     *                          ordering, as passed to the {@link
     *                          EventEncoding#sortProperEvents(byte...)
     *                          sortProperEvents()} method.
     */
    public OrderingInfo(final TByteArrayList statusArrayList,
                        final byte... flags)
    {
      mFlags = flags;
      mIndexes = new int[(1 << flags.length) + 1];
      final int numEvents = statusArrayList.size();
      int lastIndex = -1;
      for (int e = NONTAU; e < numEvents; e++) {
        final byte status = statusArrayList.get(e);
        final int offset = getOffset(status);
        assert offset >= lastIndex : "Events not in required order!";
        while (lastIndex < offset) {
          mIndexes[++lastIndex] = e;
        }
      }
      lastIndex++;
      while (lastIndex < mIndexes.length) {
        mIndexes[lastIndex++] = numEvents;
      }
    }

    //#######################################################################
    //# Access Methods
    /**
     * Gets the index of the first event with the given status flags
     * in the ordering.
     * @param  flags  List of event status flags, represented by
     *                a sequence of the bits or bit combinations
     *                {@link #STATUS_CONTROLLABLE}, {@link #STATUS_LOCAL},
     *                {@link #STATUS_OUTSIDE_ALWAYS_ENABLED},
     *                {@link #STATUS_OUTSIDE_ONLY_SELFLOOP}, and
     *                {@link #STATUS_UNUSED} or their complements.<BR>
     *                The flags must appear in the ordering that matches
     *                the original call to the {@link
     *                EventEncoding#sortProperEvents(byte...)
     *                sortProperEvents()} method. If a flag is negated,
     *                the method looks for the first event without what
     *                property, otherwise for the first event with the
     *                property.
     */
    public int getFirstEventIndex(final int... flags)
    {
      final int offset = getOffset(flags);
      return mIndexes[offset];
    }

    /**
     * Gets the index of the last event with the given status flags
     * in the ordering.
     * @param  flags  List of event status flags, represented by
     *                a sequence of the bits or bit combinations
     *                {@link #STATUS_CONTROLLABLE}, {@link #STATUS_LOCAL},
     *                {@link #STATUS_OUTSIDE_ALWAYS_ENABLED},
     *                {@link #STATUS_OUTSIDE_ONLY_SELFLOOP}, and
     *                {@link #STATUS_UNUSED} or their complements.<BR>
     *                The flags must appear in the ordering that matches
     *                the original call to the {@link
     *                EventEncoding#sortProperEvents(byte...)
     *                sortProperEvents()} method. If a flag is negated,
     *                the method looks for the last event without what
     *                property, otherwise for the last event with the
     *                property.
     */
    public int getLastEventIndex(final int... flags)
    {
      int offset = getOffset(flags);
      offset += 1 << (mFlags.length - flags.length);
      return mIndexes[offset] - 1;
    }

    //#######################################################################
    //# Auxiliary Methods
    private int getOffset(final int... flags)
    {
      int offset = 0;
      int shift = mFlags.length - 1;
      for (int i = 0; i < flags.length; i++, shift--) {
        final int flag = flags[i];
        if (flag == mFlags[i]) {
          offset |= (1 << shift);
        } else {
          assert flag == ~mFlags[i] : "Unexpected event ordering flag!";
        }
      }
      return offset;
    }

    private int getOffset(final byte status)
    {
      int offset = 0;
      int shift = mFlags.length - 1;
      for (final byte flag : mFlags) {
        final boolean bit;
        if ((flag & ~STATUS_ALL) == 0) {
          bit = (status & flag) != 0;
        } else {
          bit = (status & ~flag) == 0;
        }
        if (bit) {
          offset |= (1 << shift);
        }
        shift--;
      }
      return offset;
    }

    //#######################################################################
    //# Data Members
    private final byte[] mFlags;
    private final int[] mIndexes;
  }


  //#########################################################################
  //# Inner Class EventOrdering
  private class EventOrdering implements Comparator<EventProxy>
  {
    //#######################################################################
    //# Constructor
    private EventOrdering(final byte... flags)
    {
      mMasks = new byte[flags.length];
      mReverse = new boolean[flags.length];
      for (int i = 0; i < flags.length; i++) {
        final byte flag = flags[i];
        if ((flag & ~STATUS_ALL) == 0) {
          mMasks[i] = flag;
        } else {
          mMasks[i] = (byte) ~flag;
          mReverse[i] = true;
        }
      }
    }

    //#######################################################################
    //# Invocation
    private void sortProperEvents()
    {
      Collections.sort(mProperEvents, this);
      final int numEvents = mProperEvents.size();
      final TByteArrayList oldStatus = new TByteArrayList(mProperEventStatus);
      for (int e = NONTAU; e < numEvents; e++) {
        final EventProxy event = mProperEvents.get(e);
        final int oldCode = mEventCodeMap.get(event);
        mEventCodeMap.put(event, e);
        final byte status = oldStatus.get(oldCode);
        mProperEventStatus.set(e, status);
      }
    }

    //#######################################################################
    //# Interface java.util.Comparator<EventProxy>
    @Override
    public int compare(final EventProxy event1, final EventProxy event2)
    {
      if (event1 == event2) {
        return 0;
      }
      final int e1 = mEventCodeMap.get(event1);
      final int e2 = mEventCodeMap.get(event2);
      if (e1 == TAU) {
        return -1;
      } else if (e2 == TAU) {
        return 1;
      }
      final byte status1 = mProperEventStatus.get(e1);
      final byte status2 = mProperEventStatus.get(e2);
      if (status1 == status2) {
        return 0;
      }
      for (int i = 0; i < mMasks.length; i++) {
        final boolean bit1 = (status1 & mMasks[i]) != 0;
        final boolean bit2 = (status2 & mMasks[i]) != 0;
        final int result = Boolean.compare(bit1, bit2);
        if (result != 0) {
          return mReverse[i] ? -result : result;
        }
      }
      return 0;
    }

    //#######################################################################
    //# Data Members
    private final byte[] mMasks;
    private final boolean[] mReverse;
  }


  //#########################################################################
  //# Data Members
  private final List<EventProxy> mProperEvents;
  private final List<EventProxy> mPropositions;
  private final TObjectIntHashMap<EventProxy> mEventCodeMap;
  private final TByteArrayList mProperEventStatus;
  private final TByteArrayList mPropositionStatus;
  private OrderingInfo mOrderingInfo;


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

  /**
   * An empty status byte to define an event with none of the available
   * status bits sets.
   */
  public static final byte STATUS_NONE = 0x00;
  /**
   * A status flag indicating a controllable event.
   */
  public static final byte STATUS_CONTROLLABLE = 0x01;
  /**
   * A status flags indicating a local event.
   * A local event is assumed not to be used in any other automaton
   * except the current one.
   * Unlike {@link #STATUS_OUTSIDE_ALWAYS_ENABLED} and
   * {@link #STATUS_OUTSIDE_ONLY_SELFLOOP}, this flag is purely informational.
   */
  public static final byte STATUS_LOCAL = 0x02;
  /**
   * A status flag indicating an event that outside of the current automaton
   * only appears in selfloop transitions.
   */
  public static final byte STATUS_OUTSIDE_ONLY_SELFLOOP = 0x04;
  /**
   * A status flag indicating an event that outside of the current automaton
   * is always enabled. The only automaton ever disabling this event is the
   * current automaton. Selfloops by events with this status flag are
   * automatically suppressed in a {@link ListBufferTransitionRelation}.
   */
  public static final byte STATUS_OUTSIDE_ALWAYS_ENABLED = 0x08;
  /**
   * A status flag indicating an event not in the alphabet of the current
   * transition relation. This event is assumed to be implicitly selflooped
   * in all states.
   */
  public static final byte STATUS_UNUSED = 0x10;

  /**
   * Status flags indicating a local event.
   * This is a combination of the bits {@link STATUS_LOCAL},
   * {@link #STATUS_OUTSIDE_ALWAYS_ENABLED}, and
   * {@link #STATUS_OUTSIDE_ONLY_SELFLOOP}.
   * Although {@link STATUS_LOCAL} usually implies the other two flags,
   * it is separated from the other two for synthesis and other applications,
   * where the automatic suppression of local selfloops is not desired.
   */
  public static final byte STATUS_FULLY_LOCAL =
    STATUS_LOCAL | STATUS_OUTSIDE_ONLY_SELFLOOP | STATUS_OUTSIDE_ALWAYS_ENABLED;

  /**
   * All status flags combined.
   */
  public static final byte STATUS_ALL =
    STATUS_CONTROLLABLE | STATUS_LOCAL | STATUS_OUTSIDE_ONLY_SELFLOOP |
    STATUS_OUTSIDE_ALWAYS_ENABLED | STATUS_UNUSED;

  private static final String[] STATUS_NAMES = {
    "CONTROLLABLE", "LOCAL", "OUTSIDE_ONLY_SELFLOOP",
    "OUTSIDE_ALWAYS_ENABLED", "UNUSED"
  };

  private static final Collection<EventProxy> NO_EVENTS =
    Collections.emptySet();

}
