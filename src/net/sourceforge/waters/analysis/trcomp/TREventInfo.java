//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TREventInfo
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import gnu.trove.map.hash.TObjectByteHashMap;
import gnu.trove.procedure.TObjectByteProcedure;

import java.util.Arrays;
import java.util.Collections;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author Robi Malik
 */

class TREventInfo
{
  //#########################################################################
  //# Constructor
  TREventInfo(final EventProxy event, final byte status)
  {
    mEvent = event;
    mStatusMap =
      new TObjectByteHashMap<>(0, 0.5f, EventStatus.STATUS_FULLY_LOCAL);
    mIsControllable = EventStatus.isControllableEvent(status);
    mIsBlocked = mIsFailing = false;
    mNumNonSelfloopAutomata = mNumDisablingAutomata = 0;
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

  boolean isControllable()
  {
    return mIsControllable;
  }

  boolean isBlocked()
  {
    return mIsBlocked;
  }

  boolean isFailing()
  {
    return mIsFailing;
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
      new StatusFinder(EventStatus.STATUS_OUTSIDE_ONLY_SELFLOOP);
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
      new StatusFinder(EventStatus.STATUS_OUTSIDE_ALWAYS_ENABLED);
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
  byte getEventStatus(final TRAutomatonProxy candidate)
  {
    final Set<TRAutomatonProxy> set = Collections.singleton(candidate);
    return getEventStatus(set);
  }

  byte getEventStatus(final Set<TRAutomatonProxy> candidate)
  {
    if (mIsBlocked || mNumNonSelfloopAutomata == 0) {
      if (mIsControllable) {
        return EventStatus.STATUS_BLOCKED | EventStatus.STATUS_CONTROLLABLE;
      } else {
        return EventStatus.STATUS_BLOCKED;
      }
    } else {
      final StatusChecker checker = new StatusChecker(candidate);
      mStatusMap.forEachEntry(checker);
      byte result = checker.getResult();
      if (mIsControllable) {
        result |= EventStatus.STATUS_CONTROLLABLE;
      }
      if (mIsFailing) {
        result |= EventStatus.STATUS_FAILING;
      }
      return result;
    }
  }


  //#########################################################################
  //# Set up
  void updateAutomatonStatus(final TRAutomatonProxy aut,
                             final byte newStatus,
                             final Queue<TRAutomatonProxy> needsSimplification)
  {
    final boolean wasBlocked = mIsBlocked;
    final boolean wasFailing = mIsFailing;
    final int oldNumNonSelfloopAutomata = mNumNonSelfloopAutomata;
    final int oldNumDisablingAutomata = mNumDisablingAutomata;
    setAutomatonStatus(aut, newStatus);
    if (mIsBlocked && !wasBlocked) {
      enqueueAutomataExcept(needsSimplification, null);
    } else if (mNumNonSelfloopAutomata == 0 && oldNumNonSelfloopAutomata > 0) {
      enqueueAutomataExcept(needsSimplification, null);
    } else if (mIsFailing && !wasFailing) {
      enqueueAutomataExcept(needsSimplification, aut);
    } else if (mNumDisablingAutomata == 0 && oldNumDisablingAutomata > 0) {
      enqueueAutomataExcept(needsSimplification, aut);
    } else {
      if (mNumNonSelfloopAutomata == 1 && oldNumNonSelfloopAutomata > 1) {
        final TRAutomatonProxy simplifiable = getNonSelfloopAutomaton();
        needsSimplification.offer(simplifiable);
      }
      if (mNumDisablingAutomata == 1 && oldNumDisablingAutomata > 1) {
        final TRAutomatonProxy simplifiable = getDisablingAutomaton();
        needsSimplification.offer(simplifiable);
      }
    }
  }

  void setAutomatonStatus(final TRAutomatonProxy aut, final byte newStatus)
  {
    if (EventStatus.isUsedEvent(newStatus)) {
      if (mIsBlocked) {
        // nothing
      } else if (EventStatus.isBlockedEvent(newStatus)) {
        mIsBlocked = true;
        mIsFailing = false;
      } else if (EventStatus.isFailingEvent(newStatus)) {
        mIsFailing = true;
      }
      final byte oldStatus = mStatusMap.get(aut);
      final boolean oldSelfloopOnly =
        EventStatus.isOutsideOnlySelfloopEvent(oldStatus);
      final boolean newSelfloopOnly =
        EventStatus.isOutsideOnlySelfloopEvent(newStatus);
      if (oldSelfloopOnly && !newSelfloopOnly) {
        mNumNonSelfloopAutomata++;
      } else if (!oldSelfloopOnly && newSelfloopOnly) {
        mNumNonSelfloopAutomata--;
      }
      final boolean oldAlwaysEnabled =
        EventStatus.isOutsideAlwaysEnabledEvent(oldStatus);
      final boolean newAlwaysEnabled =
        EventStatus.isOutsideAlwaysEnabledEvent(newStatus);
      if (oldAlwaysEnabled && !newAlwaysEnabled) {
        mNumDisablingAutomata++;
      } else if (!oldAlwaysEnabled && newAlwaysEnabled) {
        mNumDisablingAutomata--;
      }
      mStatusMap.put(aut, (byte) (newStatus & EventStatus.STATUS_FULLY_LOCAL));
    } else {
      removeAutomaton(aut);
    }
  }

  void removeAutomaton(final TRAutomatonProxy aut)
  {
    final byte oldStatus = mStatusMap.remove(aut);
    if (!EventStatus.isOutsideOnlySelfloopEvent(oldStatus)) {
      mNumNonSelfloopAutomata--;
    }
    if (!EventStatus.isOutsideAlwaysEnabledEvent(oldStatus)) {
      mNumDisablingAutomata--;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void enqueueAutomataExcept(final Queue<TRAutomatonProxy> queue,
                                     final TRAutomatonProxy except)
  {
    final TRAutomatonProxy[] automata =
      new TRAutomatonProxy[mStatusMap.size()];
    mStatusMap.keys(automata);
    Arrays.sort(automata);
    for (final TRAutomatonProxy aut : automata) {
      if (aut != except) {
        queue.offer(aut);
      }
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
    private StatusChecker(final Set<TRAutomatonProxy> candidate)
    {
      mCandidate = candidate;
    }

    //#######################################################################
    //# Interface gnu.trove.procedure.TObjectByteProcedure<TRAutomatonProxy>
    @Override
    public boolean execute(final TRAutomatonProxy aut, final byte status)
    {
      if (mCandidate.contains(aut)) {
        return true;
      } else {
        mResult &= status;
        return mResult != 0;
      }
    }

    //#######################################################################
    //# Simple Access
    private byte getResult()
    {
      return mResult;
    }

    //#######################################################################
    //# Data Members
    private final Set<TRAutomatonProxy> mCandidate;
    private byte mResult = EventStatus.STATUS_FULLY_LOCAL;
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
  private final boolean mIsControllable;
  private boolean mIsBlocked;
  private boolean mIsFailing;
  private int mNumNonSelfloopAutomata;
  private int mNumDisablingAutomata;

}
