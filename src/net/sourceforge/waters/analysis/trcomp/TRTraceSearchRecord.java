//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRTraceSearchRecord
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.LinkedList;
import java.util.List;


/**
 * A record for trace expansion after compositional verification.
 * Each search record represents a state, and in addition contains a
 * reference to its first predecessor and the event that links the
 * predecessor to this state.
 *
 * @author Robi Malik
 */

class TRTraceSearchRecord
{
  //#########################################################################
  //# Constructors
  TRTraceSearchRecord(final int state)
  {
    this(state, 0, -1, null);
  }

  TRTraceSearchRecord(final int state,
                      final TRTraceSearchRecord pred,
                      final int event,
                      final int moreConsumed)
  {
    this(state, pred.mNumConsumedEvents + moreConsumed, event, pred);
  }

  TRTraceSearchRecord(final int state,
                      final int numConsumedEvents,
                      final int event,
                      final TRTraceSearchRecord pred)
  {
    mState = state;
    mNumConsumedEvents = numConsumedEvents;
    mEvent = event;
    mPredecessor = pred;
  }


  //#########################################################################
  //# Simple Access
  int getState()
  {
    return mState;
  }

  int getNumberOfConsumedEvents()
  {
    return mNumConsumedEvents;
  }

  int getEvent()
  {
    return mEvent;
  }


  //#########################################################################
  //# Trace Construction
  List<TRTraceSearchRecord> getSearchRecordTrace()
  {
    final List<TRTraceSearchRecord> list = new LinkedList<>();
    TRTraceSearchRecord record = this;
    while (record != null) {
      list.add(0, record);
      record = record.mPredecessor;
    }
    return list;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public boolean equals(final Object other)
  {
    if (other != null && getClass() == other.getClass()) {
      final TRTraceSearchRecord record = (TRTraceSearchRecord) other;
      return mState == record.mState &&
             mNumConsumedEvents == record.mNumConsumedEvents;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode()
  {
    return mState + 5 * mNumConsumedEvents;
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return mState + "@" + mNumConsumedEvents;
  }


  //#########################################################################
  //# Data Members
  private final int mState;
  private final int mNumConsumedEvents;
  private final int mEvent;
  private final TRTraceSearchRecord mPredecessor;

}
