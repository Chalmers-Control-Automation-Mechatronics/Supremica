//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import gnu.trove.list.array.TIntArrayList;

import net.sourceforge.waters.model.base.WatersRuntimeException;


/**
 * <P>A {@link TransitionIterator} that can iterate over groups of events
 * determined by their status.</P>
 *
 * <P>A status group transition iterator encapsulates a second iterator to
 * restrict it to events of a specific type, e.g., iterate only over
 * controllable events. The set of events is determined on creation of the
 * status group transition iterator, so any changes to the event status while
 * the iterator is in use will lead to incorrect results.</P>
 *
 * <P>The range of events can be further restricted by specifying boundaries
 * ({@link #resetEvents(int, int) resetEvents()}), but the chosen event status
 * cannot be changed after creation of the iterator. The changing of start
 * states and other functionality is supported through the encapsulated
 * iterator, however caching is not supported.</P>
 *
 * <P>The iterator does not require a particular order of the events in
 * the transition relation, however the best performance is achieved if
 * the events to be iterated over have consecutive numbers.</P>
 *
 * @author Robi Malik
 */

public class StatusGroupTransitionIterator implements TransitionIterator
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new status group transition iterator.
   * @param inner    The encapsulated iterator used to produce transitions.
   * @param provider The event status provider or event encoding that
   *                 determines the type of events.
   * @param flags    Status flags that specify what events are included in
   *                 iteration. This should be a sequence of the bits
   *                 {@link EventStatus#STATUS_CONTROLLABLE},
   *                 {@link EventStatus#STATUS_LOCAL},
   *                 {@link EventStatus#STATUS_ALWAYS_ENABLED},
   *                 {@link EventStatus#STATUS_SELFLOOP_ONLY},
   *                 {@link EventStatus#STATUS_BLOCKED}, and
   *                 {@link EventStatus#STATUS_FAILING}, or their
   *                 complements.<BR>
   *                 For example, to iterate over local controllable events,
   *                 two arguments {@link EventStatus#STATUS_LOCAL} and
   *                 {@link EventStatus#STATUS_CONTROLLABLE} are passed into
   *                 the constructor. To iterate over events not having a
   *                 given status, the flag is to be complemented. To iterate
   *                 over local uncontrollable events, the arguments
   *                 {@link EventStatus#STATUS_LOCAL} and
   *                 ~{@link EventStatus#STATUS_CONTROLLABLE} are used.<BR>
   *                 Independently of the arguments, unused events
   *                 ({@link EventStatus#STATUS_UNUSED} are always excluded.
   */
  public StatusGroupTransitionIterator(final TransitionIterator inner,
                                       final EventStatusProvider provider,
                                       final int... flags)
  {
    mInnerIterator = inner;
    setUpBoundaries(provider, flags);
    mFirstEvent = 0;
    mLastEvent = provider.getNumberOfProperEvents() - 1;
    mIndex = mBoundaries.length;
    reset();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
  @Override
  public StatusGroupTransitionIterator clone()
  {
    try {
      final StatusGroupTransitionIterator cloned =
        (StatusGroupTransitionIterator) super.clone();
      cloned.mInnerIterator = mInnerIterator.clone();
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  @Override
  public void reset()
  {
    if (findStartIndex()) {
      final int first = Math.max(mFirstEvent, mBoundaries[mIndex]);
      final int last = Math.min(mLastEvent, mBoundaries[mIndex + 1]);
      mInnerIterator.resetEvents(first, last);
    } else {
      mIndex = mBoundaries.length;
    }
  }

  @Override
  public void resetEvent(final int event)
  {
    mFirstEvent = mLastEvent = event;
    reset();
  }

  @Override
  public void resetEvents(final int first, final int last)
  {
    mFirstEvent = first;
    mLastEvent = last;
    reset();
  }

  @Override
  public void resetState(final int from)
  {
    mInnerIterator.resetState(from);
    reset();
  }

  @Override
  public void reset(final int from, final int event)
  {
    mFirstEvent = mLastEvent = event;
    resetState(from);
  }

  @Override
  public void resume(final int from)
  {
    resetState(from);
  }

  @Override
  public int getFirstEvent()
  {
    return mFirstEvent;
  }

  @Override
  public int getLastEvent()
  {
    return mLastEvent;
  }

  @Override
  public boolean advance()
  {
    if (mIndex >= mBoundaries.length) {
      return false;
    } else {
      while (!mInnerIterator.advance()) {
        mIndex += 2;
        if (mIndex >= mBoundaries.length || mBoundaries[mIndex] > mLastEvent) {
          return false;
        }
        final int first = mBoundaries[mIndex];
        final int last = Math.min(mLastEvent, mBoundaries[mIndex + 1]);
        mInnerIterator.resetEvents(first, last);
      }
      return true;
    }
  }

  @Override
  public boolean isValid()
  {
    return mInnerIterator.isValid();
  }

  @Override
  public int getCurrentEvent()
  {
    return mInnerIterator.getCurrentEvent();
  }

  @Override
  public int getCurrentSourceState()
  {
    return mInnerIterator.getCurrentSourceState();
  }

  @Override
  public int getCurrentToState()
  {
    return mInnerIterator.getCurrentToState();
  }

  @Override
  public void setCurrentToState(final int state)
  {
    mInnerIterator.setCurrentToState(state);
  }

  @Override
  public int getCurrentTargetState()
  {
    return mInnerIterator.getCurrentTargetState();
  }

  @Override
  public int getCurrentFromState()
  {
    return mInnerIterator.getCurrentFromState();
  }

  @Override
  public void remove()
  {
    mInnerIterator.remove();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setUpBoundaries(final EventStatusProvider provider,
                               final int... flags)
  {
    int mask = 0;
    int pattern = 0;
    for (int i = 0; i < flags.length; i++) {
      final int flag = flags[i];
      if ((flag & ~EventStatus.STATUS_ALL) == 0) {
        mask |= flag;
        pattern |= flag;
      } else {
        mask |= ~flag;
      }
    }
    final TIntArrayList boundaries = new TIntArrayList();
    int i = -1;
    for (int e = 0; e < provider.getNumberOfProperEvents(); e++) {
      final byte status = provider.getProperEventStatus(e);
      if (!EventStatus.isUsedEvent(status)) {
        // skip unused event
      } else if ((status & mask) != pattern) {
        // event with wrong status cancels range
        i = -1;
      } else if (i < 0) {
        // event with correct status starts new range
        boundaries.add(e);
        boundaries.add(e);
        i = boundaries.size() - 1;
      } else {
        // event with correct status added to existing range
        boundaries.set(i, e);
      }
    }
    mBoundaries = boundaries.toArray();
  }

  private boolean findStartIndex()
  {
    if (mBoundaries.length == 0) {
      return false;
    } else if (mFirstEvent <= mBoundaries[1]) {
      mIndex = 0;
      return true;
    } else if (mBoundaries.length == 2) {
      return false;
    } else {
      // binary search for start group ...
      int l = 2;
      int u = mBoundaries.length;
      while (l < u) {
        final int m = (l + u) >> 1;
        if (mFirstEvent <= mBoundaries[m]) {
          u = m;
        } else {
          l = m + 1;
        }
      }
      if ((l & 1) == 0) {
        mIndex = l;
        return l < mBoundaries.length;
      } else {
        mIndex = l - 1;
        return true;
      }
    }
  }


  //#########################################################################
  //# Data Members
  private TransitionIterator mInnerIterator;
  private int[] mBoundaries;
  private int mFirstEvent;
  private int mLastEvent;
  private int mIndex;

}
