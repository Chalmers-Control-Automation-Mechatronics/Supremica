//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.analysis.trcomp;

import gnu.trove.map.hash.TObjectByteHashMap;
import gnu.trove.procedure.TObjectByteProcedure;

import java.util.Arrays;
import java.util.Collections;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.compositional.CompositionalAnalysisResult;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;

import org.apache.logging.log4j.Logger;


/**
 * <P>Event information for compositional verification.</P>
 *
 * <P>An event information record remembers all automata using an event,
 * plus details of the special-event status. An event can be <I>blocked</I>,
 * <I>failing</I>, <I>always enabled</I>, or <I>selfloop-only</I>.
 * In addition, an event is <I>local</I> if it appears in only one
 * automaton.</P>
 *
 * <P>If an event is blocked in one automaton, it is blocked in the system.
 * Nevertheless, the blocked status is recorded with the automaton, to
 * ensure that the blocked event is only hidden from its blocking automaton
 * after it has been removed from all other automata.</P>
 *
 * <P>The failing status is recorded globally on the event information record
 * as it affects all automata in the same way.</P>
 *
 * <P>Events are recognised as <I>locally</I> selfloop-only or locally always
 * enabled, if they are selfloop-only or always enabled in all but one
 * automata. They are recognised as globally selfloop-only or globally always
 * enabled, if they are selfloop-only or always enabled in all automata.</P>
 *
 * <P>Events used outside of the system subject to compositional minimisation
 * may be marked with an <I>external status</I>. Such events will not be
 * reported as <I>local</I>, <I>selfloop-only</I>, or <I>always enabled</I>,
 * but they may still be <I>blocked</I> or <I>failing</I>.</P>
 *
 * @author Robi Malik
 */

class TREventInfo
{
  //#########################################################################
  //# Constructor
  /**
   * Creates a new event information record.
   * The newly created record has the default <I>external</I> status
   * indicating that the event is not used externally.
   * @param  event         The event object represented by the new event
   *                       information.
   * @param  status        Initial status flags to determine whether the new
   *                       event is to be marked as <I>controllable</I>.
   */
  TREventInfo(final EventProxy event, final byte status)
  {
    this(event, status, EventStatus.STATUS_FULLY_LOCAL);
  }

  /**
   * Creates a new event information record.
   * @param  event          The event object represented by the new event
   *                        information.
   * @param  status         Initial status flags to determine whether the new
   *                        event is to be marked as <I>controllable</I>.
   * @param  externalStatus Status flags indicating how the event is used
   *                        externally, i.e., outside of the system subject
   *                        to compositional minimisation.
   */
  TREventInfo(final EventProxy event,
              final byte status,
              final byte externalStatus)
  {
    mEvent = event;
    mStatusMap =
      new TObjectByteHashMap<>(0, 0.5f, EventStatus.STATUS_FULLY_LOCAL);
    mControllable = EventStatus.isControllableEvent(status);
    mExternalStatus = externalStatus;
    mBlocked = EventStatus.isBlockedEvent(externalStatus);
    mFailing = EventStatus.isFailingEvent(externalStatus);
    mNumNonSelfloopAutomata =
      EventStatus.isSelfloopOnlyEvent(externalStatus) ? 0 : 1;
    mNumDisablingAutomata =
      EventStatus.isAlwaysEnabledEvent(externalStatus) ? 0 : 1;
  }


  //#########################################################################
  //# Simple Access
  EventProxy getEvent()
  {
    return mEvent;
  }

  boolean isEmpty()
  {
    return mStatusMap.isEmpty();
  }

  /**
   * Returns the <I>external</I> status of this event. The external status
   * indicates how the event is used externally, i.e., outside of the system
   * subject to compositional minimisation.
   * The default value {@link EventStatus#STATUS_FULLY_LOCAL} indicates an
   * <I>internal</I> event used only within the system.
   * If the external status is not <I>local</I>, <I>selfloop-only</I>, or
   * <I>always enabled</I> then the methods {@link #getEventStatus(Set)}
   * and {@link #getEventStatus(TRAutomatonProxy)} will never report that
   * simplification is possible with such such status.
   */
  byte getExternalStatus()
  {
    return mExternalStatus;
  }

  /**
   * Returns whether this event is external.
   * @return <CODE>true</CODE> if the external status is something else than
   *         {@link EventStatus#STATUS_FULLY_LOCAL}.
   * @see #getExternalStatus()
   */
  boolean isExternal()
  {
    return mExternalStatus != EventStatus.STATUS_FULLY_LOCAL;
  }

  boolean isControllable()
  {
    return mControllable;
  }

  boolean isBlocked()
  {
    return mBlocked;
  }

  boolean isFailing()
  {
    return mFailing;
  }

  boolean isLocal()
  {
    return mStatusMap.size() == 1;
  }

  boolean isGloballySelfloopOnly()
  {
    return mNumNonSelfloopAutomata == 0;
  }

  boolean isLocallySelfloopOnly()
  {
    return mNumNonSelfloopAutomata == 1;
  }

  TRAutomatonProxy getNonSelfloopAutomaton()
  {
    final StatusFinder finder =
      new StatusFinder(EventStatus.STATUS_SELFLOOP_ONLY);
    mStatusMap.forEachEntry(finder);
    return finder.getResult();
  }

  boolean isGloballyAlwaysEnabled()
  {
    return mNumDisablingAutomata == 0;
  }

  boolean isLocallyAlwaysEnabled()
  {
    return mNumDisablingAutomata == 1;
  }

  TRAutomatonProxy getDisablingAutomaton()
  {
    final StatusFinder finder =
      new StatusFinder(EventStatus.STATUS_ALWAYS_ENABLED);
    mStatusMap.forEachEntry(finder);
    return finder.getResult();
  }

  Set<TRAutomatonProxy> getAutomata()
  {
    return mStatusMap.keySet();
  }

  byte getAutomatonStatus(final TRAutomatonProxy aut)
  {
    return mStatusMap.get(aut);
  }


  //#########################################################################
  //# Advanced Access
  byte getEventStatus(final TRAutomatonProxy aut)
  {
    final Set<TRAutomatonProxy> candidate = Collections.singleton(aut);
    return getEventStatus(candidate);
  }

  byte getEventStatus(final Set<TRAutomatonProxy> candidate)
  {
    // Note 'blocked' status is recorded in mStatusMap:
    // only return blocked when another automaton blocks the event.
    byte result;
    if (mNumNonSelfloopAutomata == 0 && !mBlocked) {
      result = EventStatus.STATUS_BLOCKED;
    } else {
      final StatusChecker checker =
        new StatusChecker(candidate, mExternalStatus);
      mStatusMap.forEachEntry(checker);
      result = checker.getResult();
    }
    if (mControllable) {
      result |= EventStatus.STATUS_CONTROLLABLE;
    }
    if (mFailing) {
      result |= EventStatus.STATUS_FAILING;
    }
    return result;
  }

  void countSpecialEvents(final byte usedStatus,
                          final CompositionalAnalysisResult stats,
                          final Logger logger)
  {
    if (EventStatus.isLocalEvent(usedStatus)) {
      reportEventStatus(logger, "local");
    } else if (EventStatus.isBlockedEvent(usedStatus)) {
      if (mBlocked) {
        stats.addBlockedEvents(1);
        reportEventStatus(logger, "blocked");
      } else {
        stats.addSelfloopOnlyEvents(1);
        reportEventStatus(logger, "globally selfloop-only");
      }
    } else if (EventStatus.isFailingEvent(usedStatus)) {
      stats.addFailingEvents(1);
      reportEventStatus(logger, "failing");
    } else if (EventStatus.isSelfloopOnlyEvent(usedStatus)) {
      stats.addSelfloopOnlyEvents(1);
      reportEventStatus(logger, "locally selfloop-only");
    } else if (EventStatus.isAlwaysEnabledEvent(usedStatus)) {
      stats.addAlwaysEnabledEvents(1);
      reportEventStatus(logger, "always enabled");
    }
  }

  void reportEventStatus(final Logger logger, final String status)
  {
    if (logger.isDebugEnabled()) {
      final String msg =
        "Event " + mEvent.getName() + " is considered as " + status + ".";
      logger.debug(msg);
    }
  }


  //#########################################################################
  //# Set up
  void updateAutomatonStatus(final TRAutomatonProxy aut,
                             final byte newStatus,
                             final Queue<TRAutomatonProxy> needsSimplification)
  {
    final boolean wasBlocked = mBlocked;
    final boolean wasFailing = mFailing;
    final int oldNumNonSelfloopAutomata = mNumNonSelfloopAutomata;
    final int oldNumDisablingAutomata = mNumDisablingAutomata;
    setAutomatonStatus(aut, newStatus);
    if (mBlocked && !wasBlocked) {
      enqueueAllAutomata(needsSimplification);
    } else if (mNumNonSelfloopAutomata == 0 && oldNumNonSelfloopAutomata > 0) {
      enqueueAllAutomata(needsSimplification);
    } else if (mFailing && !wasFailing) {
      enqueueAllAutomata(needsSimplification);
    } else if (mNumDisablingAutomata == 0 && oldNumDisablingAutomata > 0) {
      enqueueAllAutomata(needsSimplification);
    } else {
      if (mNumNonSelfloopAutomata == 1 && oldNumNonSelfloopAutomata > 1 &&
          EventStatus.isSelfloopOnlyEvent(mExternalStatus)) {
        final TRAutomatonProxy simplifiable = getNonSelfloopAutomaton();
        needsSimplification.offer(simplifiable);
      }
      if (mNumDisablingAutomata == 1 && oldNumDisablingAutomata > 1 &&
          EventStatus.isAlwaysEnabledEvent(mExternalStatus)) {
        final TRAutomatonProxy simplifiable = getDisablingAutomaton();
        needsSimplification.offer(simplifiable);
      }
    }
  }

  void addExternalStatus(byte status)
  {
    if (EventStatus.isUsedEvent(status)) {
      mBlocked |= EventStatus.isBlockedEvent(status);
      if (mBlocked) {
        mFailing = false;
        status &= ~EventStatus.STATUS_FAILING;
      } else {
        mFailing |= EventStatus.isFailingEvent(status);
      }
      if (EventStatus.isSelfloopOnlyEvent(mExternalStatus) &&
          !EventStatus.isSelfloopOnlyEvent(status)) {
        mNumNonSelfloopAutomata++;
      }
      if (EventStatus.isAlwaysEnabledEvent(mExternalStatus) &&
          !EventStatus.isAlwaysEnabledEvent(status)) {
        mNumDisablingAutomata++;
      }
      mExternalStatus = EventStatus.combine(mExternalStatus, status);
    }
  }

  void setAutomatonStatus(final TRAutomatonProxy aut, final byte newStatus)
  {
    if (EventStatus.isUsedEvent(newStatus)) {
      if (EventStatus.isBlockedEvent(newStatus)) {
        mBlocked = true;
        mFailing = false;
      } else if (EventStatus.isFailingEvent(newStatus)) {
        mFailing = true;
      }
      final byte oldStatus = mStatusMap.get(aut);
      final boolean oldSelfloopOnly =
        EventStatus.isSelfloopOnlyEvent(oldStatus);
      final boolean newSelfloopOnly =
        EventStatus.isSelfloopOnlyEvent(newStatus);
      if (oldSelfloopOnly && !newSelfloopOnly) {
        mNumNonSelfloopAutomata++;
      } else if (!oldSelfloopOnly && newSelfloopOnly) {
        mNumNonSelfloopAutomata--;
      }
      final boolean oldAlwaysEnabled =
        EventStatus.isAlwaysEnabledEvent(oldStatus);
      final boolean newAlwaysEnabled =
        EventStatus.isAlwaysEnabledEvent(newStatus);
      if (oldAlwaysEnabled && !newAlwaysEnabled) {
        mNumDisablingAutomata++;
      } else if (!oldAlwaysEnabled && newAlwaysEnabled) {
        mNumDisablingAutomata--;
      }
      mStatusMap.put(aut, (byte) (newStatus & USED_BITS));
    } else {
      removeAutomaton(aut);
    }
  }

  void removeAutomaton(final TRAutomatonProxy aut)
  {
    final byte oldStatus = mStatusMap.remove(aut);
    if (!EventStatus.isSelfloopOnlyEvent(oldStatus)) {
      mNumNonSelfloopAutomata--;
    }
    if (!EventStatus.isAlwaysEnabledEvent(oldStatus)) {
      mNumDisablingAutomata--;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void enqueueAllAutomata(final Queue<TRAutomatonProxy> queue)
  {
    final TRAutomatonProxy[] automata =
      new TRAutomatonProxy[mStatusMap.size()];
    mStatusMap.keys(automata);
    Arrays.sort(automata);
    for (final TRAutomatonProxy aut : automata) {
      queue.offer(aut);
    }
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return mEvent.getName();
  }


  //#########################################################################
  //# Inner Class StatusChecker
  private static class StatusChecker
    implements TObjectByteProcedure<TRAutomatonProxy>
  {
    //#######################################################################
    //# Constructor
    private StatusChecker(final Set<TRAutomatonProxy> candidate,
                          final byte externalStatus)
    {
      mCandidate = candidate;
      mBlocked = EventStatus.isBlockedEvent(externalStatus);
      mStatus = (byte) (externalStatus & EventStatus.STATUS_FULLY_LOCAL);
    }

    //#######################################################################
    //# Interface gnu.trove.procedure.TObjectByteProcedure<TRAutomatonProxy>
    @Override
    public boolean execute(final TRAutomatonProxy aut, final byte status)
    {
      if (!mCandidate.contains(aut)) {
        mBlocked |= EventStatus.isBlockedEvent(status);
        mStatus &= status;
      }
      return true;
    }

    //#######################################################################
    //# Simple Access
    private byte getResult()
    {
      if (mBlocked) {
        return (byte) (mStatus | EventStatus.STATUS_BLOCKED);
      } else {
        return mStatus;
      }
    }

    //#######################################################################
    //# Data Members
    private final Set<TRAutomatonProxy> mCandidate;
    private boolean mBlocked;
    private byte mStatus;
  }


  //#########################################################################
  //# Inner Class StatusFinder
  private static class StatusFinder
    implements TObjectByteProcedure<TRAutomatonProxy>
  {
    //#######################################################################
    //# Constructor
    private StatusFinder(final byte status)
    {
      mStatus = status;
      mResult = null;
    }

    //#######################################################################
    //# Interface gnu.trove.procedure.TObjectByteProcedure<TRAutomatonProxy>
    @Override
    public boolean execute(final TRAutomatonProxy aut, final byte status)
    {
      if ((status & mStatus) == 0) {
        mResult = aut;
        return false;
      } else {
        return true;
      }
    }

    //#######################################################################
    //# Simple Access
    private TRAutomatonProxy getResult()
    {
      return mResult;
    }

    //#######################################################################
    //# Data Members
    private final byte mStatus;
    private TRAutomatonProxy mResult;
  }


  //#########################################################################
  //# Data Members
  private final EventProxy mEvent;
  private final TObjectByteHashMap<TRAutomatonProxy> mStatusMap;
  private final boolean mControllable;
  private byte mExternalStatus;
  private boolean mBlocked;
  private boolean mFailing;
  private int mNumNonSelfloopAutomata;
  private int mNumDisablingAutomata;


  //#########################################################################
  //# Class Constants
  private static final byte USED_BITS =
    EventStatus.STATUS_FULLY_LOCAL | EventStatus.STATUS_BLOCKED;

}
