//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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
