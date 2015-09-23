//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.tr;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
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
  implements EventStatusProvider
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty encoding.
   * This method creates an event encoding without any events or
   * propositions. Events can be added using {@link
   * #addEvent(EventProxy,KindTranslator,int) addEvent()} or {@link
   * #addSilentEvent(EventProxy) addSilentEvent()}.
   */
  public EventEncoding()
  {
    mProperEvents = new ArrayList<>();
    mProperEvents.add(null);
    mProperEventStatus = new TByteArrayList();
    mProperEventStatus.add((byte) (EventStatus.STATUS_FULLY_LOCAL |
                                   EventStatus.STATUS_UNUSED));
    mPropositions = new ArrayList<>();
    mUsedPropositions = 0;
    mEventCodeMap = new TObjectIntHashMap<>(0, 0.5f, -1);
  }

  /**
   * Creates a new event encoding for the given automaton.
   * This method creates an event encoding for all events in the
   * given automaton, and with no silent event.
   * @param  aut         The automaton to be encoded.
   * @param  translator  Kind translator to distinguish propositions from
   *                     proper events.
   * @throws OverflowException to indicate that the number of propositions
   *                    exceeds the supported maximum
   *                    {@link EventStatusProvider#MAX_PROPOSITIONS}.
   */
  public EventEncoding(final AutomatonProxy aut,
                       final KindTranslator translator)
    throws OverflowException
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
   * @throws OverflowException to indicate that the number of propositions
   *                    exceeds the supported maximum
   *                    {@link EventStatusProvider#MAX_PROPOSITIONS}.
   */
  public EventEncoding(final AutomatonProxy aut,
                       final KindTranslator translator,
                       final EventProxy tau)
    throws OverflowException
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
   * @throws OverflowException to indicate that the number of propositions
   *                    exceeds the supported maximum
   *                    {@link EventStatusProvider#MAX_PROPOSITIONS}.
   */
  public EventEncoding(final AutomatonProxy aut,
                       final KindTranslator translator,
                       final Collection<EventProxy> filter,
                       final int filterMode)
    throws OverflowException
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
   * @throws OverflowException to indicate that the number of propositions
   *                    exceeds the supported maximum
   *                    {@link EventStatusProvider#MAX_PROPOSITIONS}.
   */
  public EventEncoding(final AutomatonProxy aut,
                       final KindTranslator translator,
                       final EventProxy tau,
                       final Collection<EventProxy> filter,
                       final int filterMode)
    throws OverflowException
  {
    this(aut.getEvents(), translator, tau, filter, filterMode);
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
   * @throws OverflowException to indicate that the number of propositions
   *                    exceeds the supported maximum
   *                    {@link EventStatusProvider#MAX_PROPOSITIONS}.
   */
  public EventEncoding(final Collection<EventProxy> events,
                       final KindTranslator translator) throws OverflowException
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
   * @throws OverflowException to indicate that the number of propositions
   *                    exceeds the supported maximum
   *                    {@link EventStatusProvider#MAX_PROPOSITIONS}.
   */
  public EventEncoding(final Collection<EventProxy> events,
                       final KindTranslator translator,
                       final EventProxy tau) throws OverflowException
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
   * @throws OverflowException to indicate that the number of propositions
   *                    exceeds the supported maximum
   *                    {@link EventStatusProvider#MAX_PROPOSITIONS}.
   */
  public EventEncoding(final Collection<EventProxy> events,
                       final KindTranslator translator,
                       final Collection<EventProxy> filter,
                       final int filterMode)
    throws OverflowException
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
   * @throws OverflowException to indicate that the number of propositions
   *                    exceeds the supported maximum
   *                    {@link EventStatusProvider#MAX_PROPOSITIONS}.
   */
  public EventEncoding(final Collection<EventProxy> events,
                       final KindTranslator translator,
                       final EventProxy tau,
                       final Collection<EventProxy> filter,
                       final int filterMode)
    throws OverflowException
  {
    final int numEvents = events.size();
    mProperEvents = new ArrayList<>(numEvents);
    mPropositions = new ArrayList<>(numEvents);
    mProperEventStatus = new TByteArrayList(numEvents);
    mUsedPropositions = 0;
    mEventCodeMap = new TObjectIntHashMap<>(numEvents, 0.5f, -1);
    mProperEvents.add(tau);
    if (tau == null) {
      mProperEventStatus.add((byte) (EventStatus.STATUS_FULLY_LOCAL |
                                     EventStatus.STATUS_UNUSED));
    } else {
      mProperEventStatus.add(EventStatus.STATUS_FULLY_LOCAL);
      mEventCodeMap.put(tau, TAU);
    }
    for (final EventProxy event : events) {
      if (event != tau) {
        byte status = 0;
        switch (translator.getEventKind(event)) {
        case CONTROLLABLE:
          status = EventStatus.STATUS_CONTROLLABLE;
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
            checkNumberOfPropositions(1);
            final int p = mPropositions.size();
            mEventCodeMap.put(event, p);
            mPropositions.add(event);
            mUsedPropositions |= (1 << p);
          }
          break;
        default:
          break;
        }
      }
    }
  }

  /**
   * Creates a new event encoding by copying another.
   */
  public EventEncoding(final EventEncoding enc)
  {
    mProperEvents = new ArrayList<>(enc.mProperEvents);
    mPropositions = new ArrayList<>(enc.mPropositions);
    mProperEventStatus = new TByteArrayList(enc.mProperEventStatus);
    mUsedPropositions = enc.mUsedPropositions;
    mEventCodeMap = new TObjectIntHashMap<>(enc.mEventCodeMap);
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    final StringBuilder buffer = new StringBuilder("{");
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
        if (status != EventStatus.STATUS_NONE) {
          buffer.append('<');
          EventStatus.appendStatusInfo(buffer, status);
          buffer.append('>');
        }
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
        if (status != EventStatus.STATUS_NONE) {
          buffer.append('<');
          EventStatus.appendStatusInfo(buffer, status);
          buffer.append('>');
        }
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
        if ((mUsedPropositions & (1 << pcode)) == 0) {
          buffer.append("<UNUSED>");
        }
        pcode++;
      }
    }
    buffer.append('}');
    return buffer.toString();
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  @Override
  public EventEncoding clone()
  {
    return new EventEncoding(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.tr.EventStatusProvider
  @Override
  public byte getProperEventStatus(final int event)
  {
    return mProperEventStatus.get(event);
  }

  @Override
  public void setProperEventStatus(final int event, final int status)
  {
    mProperEventStatus.set(event, (byte) status);
  }

  @Override
  public boolean isPropositionUsed(final int prop)
  {
    return (mUsedPropositions & (1 << prop)) != 0;
  }

  @Override
  public void setPropositionUsed(final int prop, final boolean used)
  {
    if (used) {
      mUsedPropositions |= (1 << prop);
    } else {
      mUsedPropositions &= ~(1 << prop);
    }
  }

  @Override
  public int getUsedPropositions()
  {
    return mUsedPropositions;
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
  @Override
  public int getNumberOfProperEvents()
  {
    return mProperEvents.size();
  }

  /**
   * Gets the number of proposition events in this encoding.
   */
  @Override
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
    return mEventCodeMap.get(event);
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
   * Gets a collection containing all events marked as used in this encoding.
   * The returned collection is backed by the event encoding and updates if
   * it changes. It contains proper events and propositions, the silent (tau)
   * event is only included if it was specified.
   */
  public Set<EventProxy> getUsedEvents()
  {
    return new UsedEventList();
  }


  //#########################################################################
  //# Alphabet extension
  /**
   * Adds an event to this encoding. If the given event is not
   * in the encoding, this method enlarges it with a new event,
   * assigning a new code.
   * @param  event       The event to be added, which may be a proper event
   *                     or a proposition.
   * @param  translator  Kind translator to get event kind information.
   * @param  status      Collection of status flags providing additional
   *                     information about the event. Should be a combination
   *                     of the bits {@link EventStatus#STATUS_CONTROLLABLE},
   *                     {@link EventStatus#STATUS_LOCAL},
   *                     {@link EventStatus#STATUS_ALWAYS_ENABLED},
   *                     {@link EventStatus#STATUS_SELFLOOP_ONLY},
   *                     {@link EventStatus#STATUS_BLOCKED},
   *                     {@link EventStatus#STATUS_FAILING},
   *                     and {@link EventStatus#STATUS_UNUSED}.
   * @return The event (or proposition) code assigned to the event.
   * @throws OverflowException to indicate that the number of propositions
   *                     exceeds the supported maximum
   *                     {@link EventStatusProvider#MAX_PROPOSITIONS}.
   */
  public int addEvent(final EventProxy event,
                      final KindTranslator translator,
                      final int status)
    throws OverflowException
  {
    switch (translator.getEventKind(event)) {
    case CONTROLLABLE:
      return addProperEvent(event, status | EventStatus.STATUS_CONTROLLABLE);
    case UNCONTROLLABLE:
      return addProperEvent(event, status & ~EventStatus.STATUS_CONTROLLABLE);
    case PROPOSITION:
      return addProposition(event, EventStatus.isUsedEvent((byte) status));
    default:
      throw new IllegalArgumentException
        ("Unknown event kind " + event.getKind() + "!");
    }
  }

  /**
   * Adds a proper event to this encoding. If the given event is not
   * in the encoding, this method enlarges it with a new event, assigning a
   * new code.
   * @param  event       The event to be added, which must be a proper event
   *                     and not a proposition.
   * @param  status      Collection of status flags providing additional
   *                     information about the event. Should be a combination
   *                     of the bits {@link EventStatus#STATUS_CONTROLLABLE},
   *                     {@link EventStatus#STATUS_LOCAL},
   *                     {@link EventStatus#STATUS_ALWAYS_ENABLED},
   *                     {@link EventStatus#STATUS_SELFLOOP_ONLY},
   *                     {@link EventStatus#STATUS_BLOCKED},
   *                     {@link EventStatus#STATUS_FAILING},
   *                     and {@link EventStatus#STATUS_UNUSED}.
   * @return The event code assigned to the event.
   */
  public int addProperEvent(final EventProxy event, int status)
  {
    int e = mEventCodeMap.get(event);
    if (e >= 0) {
      if (EventStatus.isUsedEvent((byte) status)) {
        status = mProperEventStatus.get(e);
        status &= ~EventStatus.STATUS_UNUSED;
        mProperEventStatus.set(e, (byte) status);
      }
    } else {
      e = mProperEvents.size();
      mEventCodeMap.put(event, e);
      mProperEvents.add(event);
      mProperEventStatus.add((byte) status);
    }
    return e;
  }

  /**
   * Adds a proposition to this encoding. If the given event is not
   * in the encoding, this method enlarges it with a new proposition,
   * assigning a new code.
   * @param  event       The event to be added, which must be a proposition.
   * @param  used        Flag indicating whether the proposition should be
   *                     marked as used after addition.
   * @return The proposition code assigned to the event.
   * @throws OverflowException to indicate that the number of propositions
   *                     exceeds the supported maximum
   *                     {@link EventStatusProvider#MAX_PROPOSITIONS}.
   */
  public int addProposition(final EventProxy event, final boolean used)
    throws OverflowException
  {
    int p = mEventCodeMap.get(event);
    if (p >= 0) {
      if (used) {
        mUsedPropositions |= (1 << p);
      }
    } else {
      checkNumberOfPropositions(1);
      p = mPropositions.size();
      mEventCodeMap.put(event, p);
      mPropositions.add(event);
      if (used) {
        mUsedPropositions |= (1 << p);
      }
    }
    return p;
  }

  /**
   * Adds an event renaming pair to this encoding.
   * This method first adds the given event to the encoding.
   * Then the given alias event is mapped to the same event code without
   * adding it to the encoding. This provides a simple means to implement
   * renaming, as it allows several events to use the same code.
   * @param  alias       The alias to be added, which should be a proper
   *                     event and not a proposition.
   * @param  event       The event the alias should map to, which should be
   *                     a proper event and not a proposition. If this event
   *                     is not yet present in the encoding, it is added.
   *                     Otherwise the existing event code is used.
   * @param  translator  Kind translator to get event kind information.
   * @param  status      Collection of status flags providing additional
   *                     information about the event. Should be a combination
   *                     of the bits {@link EventStatus#STATUS_CONTROLLABLE},
   *                     {@link EventStatus#STATUS_LOCAL},
   *                     {@link EventStatus#STATUS_ALWAYS_ENABLED},
   *                     {@link EventStatus#STATUS_SELFLOOP_ONLY},
   *                     {@link EventStatus#STATUS_BLOCKED}, {@link EventStatus#STATUS_FAILING},
   *                     and {@link EventStatus#STATUS_UNUSED}.
   * @return The event code that was assigned to the event.
   * @throws OverflowException to indicate that the number of propositions
   *                     exceeds the supported maximum
   *                     {@link EventStatusProvider#MAX_PROPOSITIONS}.
   */
  public int addEventAlias(final EventProxy alias,
                           final EventProxy event,
                           final KindTranslator translator,
                           final int status)
    throws OverflowException
  {
    final int code = addEvent(event, translator, status);
    mEventCodeMap.put(alias, code);
    return code;
  }


  /**
   * <P>Adds a silent event to this event encoding.</P>
   * <P>If the encoding does not yet have any silent event, the new event is
   * added to the encoding and bound to the silent event code {@link #TAU}.</P>
   * <P>If the encoding already has a silent event, the new event is mapped
   * to the silent event code {@link #TAU}, without adding it to the
   * encoding. This provides a simple means to implement hiding: by adding
   * a silent event followed by another set events to be hidden. All these
   * events will be encoded as {@link #TAU}, and can later all be decoded to
   * the first silent event that was added.</P>
   * @param  event  A new silent event.
   */
  public void addSilentEvent(final EventProxy event)
  {
    final byte status = mProperEventStatus.get(TAU);
    if (!EventStatus.isUsedEvent(status)) {
      mProperEvents.set(TAU, event);
      mProperEventStatus.set(TAU, (byte) (status & ~EventStatus.STATUS_UNUSED));
    }
    mEventCodeMap.put(event, TAU);
  }

  /**
   * Ensures that the event encoding contains a silent event with code
   * {@link #TAU} if it does not already have such an event.
   * @param  suffix    The name suffix for the silent event.
   *                   If a silent event is created, its name will be
   *                   <CODE>&quot;tau:suffix&quot;</CODE>.
   * @return The silent event, whether previously existing or newly created.
   */
  public EventProxy provideTauEvent(final String suffix)
  {
    EventProxy tau = mProperEvents.get(TAU);
    if (tau == null) {
      final ProductDESProxyFactory factory =
        ProductDESElementFactory.getInstance();
      final String name = "tau:" + suffix;
      final byte status = mProperEventStatus.get(TAU);
      final EventKind kind = EventStatus.isControllableEvent(status) ?
        EventKind.CONTROLLABLE : EventKind.UNCONTROLLABLE;
      tau = factory.createEventProxy(name, kind, false);
      mProperEvents.set(TAU, tau);
      mEventCodeMap.put(tau, TAU);
    }
    return tau;
  }

  /**
   * Sets a new silent event. This method replaces the silent event
   * with code {@link #TAU} by the given new event. The event status
   * is not changed by this method.
   */
  public void setTauEvent(final EventProxy newTau)
  {
    final EventProxy oldTau = mProperEvents.get(TAU);
    if (oldTau != newTau) {
      if (oldTau != null) {
        mEventCodeMap.remove(oldTau);
      }
      mProperEvents.set(TAU, newTau);
      mEventCodeMap.put(newTau, TAU);
    }
  }

  /**
   * Removes all proper events and aliases from this event encoding.
   */
  public void removeAllProperEvents()
  {
    mProperEvents.clear();
    mProperEventStatus.clear();
    mEventCodeMap.clear();
  }

  /**
   * Removes all propositions from this event encoding.
   */
  public void removeAllPropositions()
  {
    mPropositions.clear();
    mUsedPropositions = 0;
  }

  /**
   * Reorders the proper events in this encoding based on their status.
   * @param  flags  List of flags to define the ordering, represented by
   *                a sequence of the bits or bit combinations
   *                {@link EventStatus#STATUS_CONTROLLABLE},
   *                {@link EventStatus#STATUS_LOCAL},
   *                {@link EventStatus#STATUS_ALWAYS_ENABLED},
   *                {@link EventStatus#STATUS_SELFLOOP_ONLY},
   *                {@link EventStatus#STATUS_BLOCKED},
   *                {@link EventStatus#STATUS_FAILING}, and
   *                {@link EventStatus#STATUS_UNUSED} or their complements.<BR>
   *                For example, to sort events by controllability first
   *                and second by locality, two arguments STATUS_CONTROLLABLE
   *                and&nbsp;STATUS_LOCAL are passed into the method.<BR>
   *                This considers uncontrollable events as "smaller" than
   *                controllable events. To consider controllable events as
   *                "smaller", ~STATUS_CONTROLLABLE is used as the first
   *                argument.
   */
  public void sortProperEvents(final int... flags)
  {
    // Prepare new lists
    final int numEvents = mProperEvents.size();
    final List<EventProxy> newEvents = new ArrayList<>(numEvents);
    final TByteArrayList newStatus = new TByteArrayList(numEvents);
    newEvents.add(mProperEvents.get(TAU));
    newStatus.add(mProperEventStatus.get(TAU));

    // Collect event groups ...
    final int numPasses = 1 << flags.length;
    int mask = 0;
    for (final int flag : flags) {
      mask |= (flag & ~EventStatus.STATUS_ALL) == 0 ? flag : ~flag;
    }
    for (int pass = 0; pass < numPasses; pass++) {
      int pattern = 0;
      int bit = 1 << flags.length;
      for (final int flag : flags) {
        bit >>= 1;
        if ((flag & ~EventStatus.STATUS_ALL) == 0) {
          if ((pass & bit) != 0) {
            pattern |= flag;
          }
        } else {
          if ((pass & bit) == 0) {
            pattern |= ~flag;
          }
        }
      }
      for (int e = NONTAU; e < numEvents; e++) {
        final byte status = mProperEventStatus.get(e);
        if ((status & mask) == pattern) {
          final int newE = newEvents.size();
          final EventProxy event = mProperEvents.get(e);
          newEvents.add(event);
          newStatus.add(status);
          mEventCodeMap.put(event, newE);
        }
      }
    }

    // Correct aliases in event-code map
    mEventCodeMap.forEachEntry(new TObjectIntProcedure<EventProxy>() {
      @Override
      public boolean execute(final EventProxy event, final int e)
      {
        if (e < newEvents.size() && newEvents.get(e) == event) {
          return true;
        } else if (e < mPropositions.size() && mPropositions.get(e) == event) {
          return true;
        } else {
          final EventProxy oldEvent = mProperEvents.get(e);
          final int newE = mEventCodeMap.get(oldEvent);
          mEventCodeMap.put(event, newE);
          return true;
        }
      }
    });

    // Install new lists
    mProperEvents = newEvents;
    mProperEventStatus = newStatus;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void checkNumberOfPropositions(final int extra)
    throws OverflowException
  {
    final int numProps = mPropositions.size() + extra;
    if (numProps > EventStatusProvider.MAX_PROPOSITIONS) {
      throw new OverflowException
        ("Encoding has " + numProps + " propositions, but " +
         ProxyTools.getShortClassName(this) + " can only handle up to " +
         MAX_PROPOSITIONS + " different propositions!");
    }
  }


  //#########################################################################
  //# Inner Class EventList
  private class UsedEventList extends AbstractSet<EventProxy>
  {
    //#######################################################################
    //# Interface java.util.Set<EventProxy>
    @Override
    public Iterator<EventProxy> iterator()
    {
      return new UsedEventListIterator();
    }

    @Override
    public int size()
    {
      int count = 0;
      for (int e = 0; e < mProperEvents.size(); e++) {
        final byte status = mProperEventStatus.get(e);
        if (EventStatus.isUsedEvent(status)) {
          count++;
        }
      }
      for (int p = 0; p < mPropositions.size(); p++) {
        if ((mUsedPropositions & (1 << p)) != 0) {
          count++;
        }
      }
      return count;
    }
  }


  //#########################################################################
  //# Inner Class UsedEventListIterator
  private class UsedEventListIterator implements Iterator<EventProxy>
  {
    //#######################################################################
    //# Constructor
    private UsedEventListIterator()
    {
      mIndex = 0;
    }

    //#######################################################################
    //# Interface java.util.Iterator<EventProxy>
    @Override
    public boolean hasNext()
    {
      final int numProperEvents = mProperEvents.size();
      byte status;
      while (true) {
        if (mIndex < numProperEvents) {
          status = mProperEventStatus.get(mIndex);
          if (EventStatus.isUsedEvent(status)) {
            return true;
          }
        } else if (mIndex < numProperEvents + mPropositions.size()) {
          final int p = mIndex - numProperEvents;
          if ((mUsedPropositions & (1 << p)) != 0) {
            return true;
          }
        } else {
          return false;
        }
        mIndex++;
      }
    }

    @Override
    public EventProxy next()
    {
      if (hasNext()) {
        final int numProperEvents = mProperEvents.size();
        if (mIndex < numProperEvents) {
          return mProperEvents.get(mIndex++);
        } else {
          return mPropositions.get(mIndex++ - numProperEvents);
        }
      } else {
        throw new NoSuchElementException
          ("Attempting to read past end of event encoding!");
      }
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        ("Event encoding does not support event removal!");
    }

    //#######################################################################
    //# Data Members
    private int mIndex;
  }


  //#########################################################################
  //# Data Members
  private List<EventProxy> mProperEvents;
  private final List<EventProxy> mPropositions;
  private final TObjectIntHashMap<EventProxy> mEventCodeMap;
  private TByteArrayList mProperEventStatus;
  private int mUsedPropositions;


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

}
