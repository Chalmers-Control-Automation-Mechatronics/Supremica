//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   OrderingInfo
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TIntArrayList;


/**
 * Ordering information for an {@link EventEncoding}.
 * The ordering information records the positions of the events
 * with the specific status flags in an encoding sorted by event
 * status. This can be used to iterate over events with specific
 * type more efficiently.
 * @see EventEncoding#sortProperEvents(byte...)
 * @see TransitionIterator#resetEvents(int, int)
 */

public class OrderingInfo
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
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
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


  //#########################################################################
  //# Access Methods
  /**
   * Gets the index of the first event with the given status flags
   * in the ordering.
   * @param  flags  List of event status flags, represented by
   *                a sequence of the bits or bit combinations
   *                {@link EventStatus#STATUS_CONTROLLABLE}, {@link EventStatus#STATUS_LOCAL},
   *                {@link EventStatus#STATUS_ALWAYS_ENABLED},
   *                {@link EventStatus#STATUS_SELFLOOP_ONLY},
   *                {@link EventStatus#STATUS_BLOCKED}, {@link EventStatus#STATUS_FAILING}, and
   *                {@link EventStatus#STATUS_UNUSED} or their complements.<BR>
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
   *                {@link EventStatus#STATUS_CONTROLLABLE}, {@link EventStatus#STATUS_LOCAL},
   *                {@link EventStatus#STATUS_ALWAYS_ENABLED},
   *                {@link EventStatus#STATUS_SELFLOOP_ONLY},
   *                {@link EventStatus#STATUS_BLOCKED}, {@link EventStatus#STATUS_FAILING}, and
   *                {@link EventStatus#STATUS_UNUSED} or their complements.<BR>
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

  /**
   * Determines whether this ordering supports queries based on the given
   * flags.
   * @param  flags  List of event status flags, as passed to the {@link
   *                #getFirstEventIndex(int...) getFirstEventIndex()}
   *                method.
   * @return <CODE>true</CODE> if the {@link #getFirstEventIndex(int...)
   *         getFirstEventIndex()} and {@link #getLastEventIndex(int...)
   *         getLastEventIndex()} can provide ordering indexes based on
   *         the given sequence of flags, <CODE>false</CODE> otherwise.
   */
  public boolean isSupportedOrdering(final int...flags)
  {
    if (flags.length <= mFlags.length) {
      for (int i = 0; i < flags.length; i++) {
        if (flags[i] != mFlags[i] && flags[i] != ~mFlags[i]) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Computes a set of group boundaries to cover all events with
   * the given ordering flags.
   * @param  flags  List of event status flags, as passed to the {@link
   *                #getFirstEventIndex(int...) getFirstEventIndex()}
   *                method.
   * @return Array of event index pairs. Alternates between start and end
   *         of one or more event sequences. An iteration over all
   *         these sequences covers all events with the specified flags.
   * @see StatusGroupTransitionIterator
   */
  public int[] getBoundaries(final int... flags)
  {
    int count = 0;
    int end = -1;
    for (int i = 0; i < mFlags.length; i++) {
      if (findFlag(mFlags[i], flags) >= 0) {
        count++;
        end = i;
      }
    }
    assert flags.length == count :
      "Ordering does not support all requested flags!";
    end++;
    final int groups = 1 << (end - count);
    final TIntArrayList boundaries = new TIntArrayList(2 * groups);
    final int[] pattern = new int[end];
    for (int g = 0; g < groups; g++) {
      int bit = 1;
      for (int i = 0; i < end; i++) {
        final int flag = mFlags[i];
        final int f = findFlag(flag, flags);
        if (f >= 0) {
          pattern[i] = flags[f];
        } else {
          if ((g & bit) != 0) {
            pattern[i] = flag;
          } else {
            pattern[i] = ~flag;
          }
          bit <<= 1;
        }
      }
      final int first = getFirstEventIndex(pattern);
      final int last = getLastEventIndex(pattern);
      if (first <= last) {
        boundaries.add(first);
        boundaries.add(last);
      }
    }
    return boundaries.toArray();
  }


  //#########################################################################
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
      if ((flag & ~EventStatus.STATUS_ALL) == 0) {
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

  private static int findFlag(final int flag, final int[] flags)
  {
    for (int i = 0; i < flags.length; i++) {
      if (flags[i] == flag || flags[i] == ~flag) {
        return i;
      }
    }
    return -1;
  }


  //#########################################################################
  //# Data Members
  private final byte[] mFlags;
  private final int[] mIndexes;

}