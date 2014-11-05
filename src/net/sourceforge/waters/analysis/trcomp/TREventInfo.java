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
  TREventInfo(final EventProxy event)
  {
    mEvent = event;
    mIsBlocked = mIsFailing = false;
    mStatusMap = new TObjectByteHashMap<>();
  }


  //#########################################################################
  //# Simple Access
  EventProxy getEvent()
  {
    return mEvent;
  }

  boolean isBlocked()
  {
    return mIsBlocked;
  }

  boolean isFailing()
  {
    return mIsFailing;
  }

  Set<TRAutomatonProxy> getAutomata()
  {
    return mStatusMap.keySet();
  }


  //#########################################################################
  //# Advanced Access
  byte getEventStatus(final Set<TRAutomatonProxy> candidate)
  {
    final StatusChecker checker = new StatusChecker(candidate);
    mStatusMap.forEachEntry(checker);
    byte result = checker.getResult();
    if (mIsBlocked) {
      result |= EventStatus.STATUS_BLOCKED;
    } else if (mIsFailing) {
      result |= EventStatus.STATUS_FAILING;
    }
    return result;
  }


  //#########################################################################
  //# Set up
  void addAutomaton(final TRAutomatonProxy aut, final byte status)
  {
    if (EventStatus.isBlockedEvent(status)) {
      mIsBlocked = true;
      mIsFailing = false;
    } else if (EventStatus.isFailingEvent(status)) {
      mIsFailing = true;
    }
    mStatusMap.put(aut, status);
  }

  void removeAutomaton(final TRAutomatonProxy aut)
  {
    mStatusMap.remove(aut);
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
  //# Data Members
  private final EventProxy mEvent;
  private boolean mIsBlocked;
  private boolean mIsFailing;
  private final TObjectByteHashMap<TRAutomatonProxy> mStatusMap;

}
