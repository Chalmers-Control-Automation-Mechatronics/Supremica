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

package net.sourceforge.waters.analysis.tr;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * <P>A data structure that stores ordered lists of transitions in a compact
 * way.</P>
 *
 * <P>The transition list buffer uses linked lists of integers stored in
 * pre-allocated arrays to store lists of transitions in a memory efficient
 * way. Each transition is stored in a list under its from-state, with the
 * event and to-state stored packed together in a single integer. This limits
 * the size to automata whose states and events numbers together can be packed
 * in 32&nbsp;bits.</P>
 *
 * <P>In addition, a single hash map is used to map pairs of from-state and
 * event to their list of transitions. This leads to a memory requirement
 * of approximately 24 byte per transition in the worst case where there are
 * only deterministic transitions.</P>
 *
 * <P>Iterators ({@link TransitionIterator}) are provided to access transitions
 * indexed by their from-state and/or their event. Individual transitions can
 * be added efficiently. The test for existence of a particular transition or
 * the removal of a transition requires a search of all transitions with the
 * corresponding from-state and event and therefore may be of linear
 * complexity.</P>
 *
 * <P>If a nondeterministic transition relation has several to-states
 * associated with the same from-state and event, the successor states are
 * ordered. Most methods ensure that this ordering is maintained, with
 * {@link TransitionIterator#setCurrentToState(int)} being one of a few
 * exceptions. If the order is violated by such a method, the user is
 * expected to ensure the transition list buffer is returned to an ordered
 * form. This may be necessary to ensure that algorithms behave
 * deterministically. All iterators obey the ordering of the transitions in
 * the buffer.</P>
 *
 * <P>The transition list buffer recognises the <I>selfloop-only</I> event
 * status ({@link EventStatus#STATUS_SELFLOOP_ONLY}) and automatically
 * suppresses all selfloops using events with this status.</P>
 *
 * <P>This class is a shared superclass for buffers of incoming and
 * outgoing transitions in a {@link ListBufferTransitionRelation}. The 'from'
 * states used for indexing can be either actual source or target states.
 * Two subclasses {@link OutgoingTransitionListBuffer} and {@link
 * IncomingTransitionListBuffer} are used to adjust the access to
 * source and target states for these two types from user's point of view.</P>
 *
 * @author Robi Malik
 */

public abstract class TransitionListBuffer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new transition list buffer.
   * The transition buffer is set up for a fixed number of states and events,
   * which defines an encoding and can no more be changed.
   * @param  numStates    The number of states that can be encoded in
   *                      transitions.
   * @param  eventStatus  Status flags of events, based on constants defined
   *                      in {@link EventStatus}.
   * @throws OverflowException if the encoding for states and events does
   *         not fit in the 32 bits available.
   */
  public TransitionListBuffer(final int numStates,
                              final EventStatusProvider eventStatus)
    throws OverflowException
  {
    this(numStates, eventStatus, MAX_BLOCK_SIZE);
  }


  /**
   * Creates a new transition list buffer.
   * The transition buffer is set up for a fixed number of states and events,
   * which defines an encoding and can no more be changed.
   * @param  numStates   The number of states that can be encoded in
   *                     transitions.
   * @param  eventStatus  Status flags of events, based on constants defined
   *                      in {@link EventStatus}.
   * @param  numTrans    Estimated number of transitions, used to determine
   *                     buffer size.
   * @throws OverflowException if the encoding for states and events does
   *         not fit in the 32 bits available.
   */
  public TransitionListBuffer(final int numStates,
                              final EventStatusProvider eventStatus,
                              final int numTrans)
    throws OverflowException
  {
    final int estimate = 2 * (numTrans + numStates);
    if (estimate <= MIN_BLOCK_SIZE) {
      mBlockSize = MIN_BLOCK_SIZE;
    } else if (estimate >= MAX_BLOCK_SIZE) {
      mBlockSize = MAX_BLOCK_SIZE;
    } else {
      mBlockSize = 1 << AutomatonTools.log2(estimate);
    }
    mBlockShift = AutomatonTools.log2(mBlockSize);
    mBlockMask = mBlockSize - 1;
    mNumStates = numStates;
    final int numEvents = eventStatus.getNumberOfProperEvents();
    mStateShift = AutomatonTools.log2(numEvents);
    final int numBits = mStateShift + AutomatonTools.log2(mNumStates);
    if (numBits > 32) {
      throw new OverflowException
        ("Encoding requires " + numBits + " bits for states + events, but " +
         ProxyTools.getShortClassName(this) + " only has 32 bits available!");
    }
    mEventMask = (1 << mStateShift) - 1;
    mBlocks = new ArrayList<int[]>();
    mStateTransitions = new int[numStates];
    mStateEventTransitions = new TIntIntHashMap(numStates);
    final int[] block = new int[mBlockSize];
    mBlocks.add(block);
    mRecycleStart = NULL;
    mNextFreeIndex = NODE_SIZE;
    mEventStatus = eventStatus;
  }

  /**
   * Creates a copy of the given transition list buffer.
   * This method performs a shallow copy of the given source transition list
   * buffer. Data structures are shared, so the source should no longer be
   * used after the copy.
   */
  TransitionListBuffer(final TransitionListBuffer buffer)
  {
    mBlockShift = buffer.mBlockShift;
    mBlockMask = buffer.mBlockMask;
    mBlockSize = buffer.mBlockSize;
    mNumStates = buffer.mNumStates;
    mStateShift = buffer.mStateShift;
    mEventMask = buffer.mEventMask;
    mBlocks = buffer.mBlocks;
    mStateTransitions = buffer.mStateTransitions;
    mStateEventTransitions = buffer.mStateEventTransitions;
    mRecycleStart = buffer.mRecycleStart;
    mNextFreeIndex = buffer.mNextFreeIndex;
    mEventStatus = buffer.mEventStatus;
  }


  //#########################################################################
  //# Simple Access
  int getNumberOfStates()
  {
    return mStateTransitions.length;
  }


  //#########################################################################
  //# Transition Access Methods
  /**
   * Removes all transitions from this buffer, releasing memory to
   * garbage collection.
   */
  public void clear()
  {
    mBlocks.clear();
    final int[] block = new int[mBlockSize];
    mBlocks.add(block);
    Arrays.fill(mStateTransitions, NULL);
    mStateEventTransitions.clear();
    mRecycleStart = NULL;
    mNextFreeIndex = NODE_SIZE;
  }

  /**
   * Gets the total number of transitions currently stored in this buffer.
   * As the number of transitions is not recorded explicitly, this method is
   * of linear complexity.
   */
  public int getNumberOfTransitions()
  {
    int count = 0;
    for (final int list : mStateTransitions) {
      count += getLength(list);
    }
    return count;
  }

  /**
   * Returns whether there are any transitions originating from the given
   * state in this buffer.
   */
  public boolean hasTransitions(final int state)
  {
    return mStateTransitions[state] != NULL;
  }

/**
   * <P>Adds a transition to this buffer.</P>
   * <P>If the transition list is ordered, the new transition is inserted
   * in such a way that the order of nondeterministic to-states is maintained,
   * otherwise it is inserted at an unspecified location.</P>
   * <P><I>Note.</I> This method checks whether the requested transition
   * already is present in the buffer using a list search, which works even
   * if the list is not ordered by to-states. Its worst-case complexity is
   * linear in the number of to-states for the given from-state and event.</P>
   * @param  from   The ID of the from-state of the new transition.
   * @param  event  The ID of the event of the new transition.
   * @param  to     The ID of the to-state of the new transition.
   * @return <CODE>true</CODE> if a transition was added, i.e., if it was
   *         not already present in the buffer;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean addTransition(final int from, final int event, final int to)
  {
    final byte status = mEventStatus.getProperEventStatus(event);
    if (from == to && EventStatus.isSelfloopOnlyEvent(status)) {
      return false;
    }
    final int newData = (to << mStateShift) | event;
    int current = createList(from, event);
    int inspos = current;
    int next = getNext(current);
    while (next != NULL) {
      final int[] block = mBlocks.get(next >>> mBlockShift);
      final int offset = next & mBlockMask;
      final int data = block[offset + OFFSET_DATA];
      if (data == newData) {
        return false;
      } else if ((data & mEventMask) != event) {
        break;
      } else if (data < newData) {
        inspos = next;
      }
      current = next;
      next = block[offset + OFFSET_NEXT];
    }
    current = prepend(inspos, newData);
    next = getNext(current);
    if (next != NULL) {
      final int nextEvent = getEvent(next);
      if (nextEvent != event) {
        final int key = (from << mStateShift) | nextEvent;
        mStateEventTransitions.put(key, current);
      }
    }
    return true;
  }

  /**
   * Adds several transition to this buffer. New transitions for the given
   * from-state and event are inserted after existing transitions with the
   * same from-state and event. Insertion is performed by list merging,
   * so duplicate transitions are only avoided if the transition relation
   * and the given states are ordered. The worst-case complexity is linear in
   * the number of transitions for the given from-state and event <I>after</I>
   * the operation.
   * @param  from     The ID of the from-state of the new transitions.
   * @param  event    The ID of the event of the new transitions.
   * @param  toStates List of target states of the new transitions.
   *                  Must be ordered.
   * @return <CODE>true</CODE> if at least one transition was added;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean addTransitions(final int from,
                                final int event,
                                final TIntArrayList toStates)
  {
    if (toStates.size() == 0) {
      return false;
    }
    final byte status = mEventStatus.getProperEventStatus(event);
    final boolean selfloopOnly = EventStatus.isSelfloopOnlyEvent(status);
    int rpos = 0;
    int rstate = toStates.get(rpos);
    if (selfloopOnly) {
      while (rstate == from) {
        if (++rpos == toStates.size()) {
          return true;
        }
        rstate = toStates.get(rpos);
      }
    }
    int list = createList(from, event);
    int next = getNext(list);
    boolean added = false;
    while (next != NULL) {
      final int[] block = mBlocks.get(next >>> mBlockShift);
      final int offset = next & mBlockMask;
      final int data = block[offset + OFFSET_DATA];
      if ((data & mEventMask) != event) {
        break;
      }
      final int state = data >>> mStateShift;
      while (rstate < state) {
        final int newData = (rstate << mStateShift) | event;
        list = prepend(list, newData);
        added = true;
        if (++rpos == toStates.size()) {
          return true;
        }
        int nstate = toStates.get(rpos);
        while (nstate == rstate || selfloopOnly && nstate == from) {
          if (++rpos == toStates.size()) {
            return true;
          }
          nstate = toStates.get(rpos);
        }
        rstate = nstate;
      }
      while (rstate == state) {
        if (++rpos == toStates.size()) {
          return added;
        }
        rstate = toStates.get(rpos);
      }
      list = next;
      next = block[offset + OFFSET_NEXT];
    }
    postfix:
    while (true) {
      final int newData = (rstate << mStateShift) | event;
      list = prepend(list, newData);
      if (++rpos == toStates.size()) {
        break;
      }
      int nstate = toStates.get(rpos);
      while (nstate == rstate || selfloopOnly && nstate == from) {
        if (++rpos == toStates.size()) {
          break postfix;
        }
        nstate = toStates.get(rpos);
      }
      rstate = nstate;
    }
    next = getNext(list);
    if (next != NULL) {
      final int nextEvent = getEvent(next);
      final int key = (from << mStateShift) | nextEvent;
      mStateEventTransitions.put(key, list);
    }
    return true;
  }

  /**
   * <P>Removes a transition from this buffer.</P>
   * <P><I>Note.</I> This method locates the specified transition using a list
   * search. Its worst-case complexity is linear in the number of to-states for
   * the given from-state and event.</P>
   * @param  from   The ID of the from-state of the transition to be removed.
   * @param  event  The ID of the event of the transition to be removed.
   * @param  to     The ID of the to-state of the transition to be removed.
   * @return <CODE>true</CODE> if a transition was removed, i.e., if it was
   *         present in the buffer; <CODE>false</CODE> otherwise.
   */
  public boolean removeTransition(final int from,
                                  final int event,
                                  final int to)
  {
    final int fromCode = (from << mStateShift) | event;
    int list = mStateEventTransitions.get(fromCode);
    if (list == NULL) {
      return false;
    }
    final int toCode = (to << mStateShift) | event;
    boolean first = true;
    int next = getNext(list);
    while (next != NULL) {
      final int[] block = mBlocks.get(next >>> mBlockShift);
      final int offset = next & mBlockMask;
      final int data = block[offset + OFFSET_DATA];
      if (data == toCode) {
        final int victim = next;
        next = block[offset + OFFSET_NEXT];
        setNext(list, next);
        block[offset + OFFSET_NEXT] = mRecycleStart;
        mRecycleStart = victim;
        final int nextEvent = next == NULL ? -1 : getEvent(next);
        if (first) {
          if (next == NULL) {
            mStateEventTransitions.remove(fromCode);
            if (mStateTransitions[from] == list) {
              mStateTransitions[from] = NULL;
              dispose(list);
            }
          } else if (nextEvent != event) {
            mStateEventTransitions.remove(fromCode);
          }
        }
        if (nextEvent != event && nextEvent >= 0) {
          final int nextCode = (from << mStateShift) | nextEvent;
          mStateEventTransitions.put(nextCode, list);
        }
        return true;
      } else if ((data & mEventMask) != event) {
        return false;
      }
      list = next;
      next = block[offset + OFFSET_NEXT];
      first = false;
    }
    return false;
  }

  /**
   * Copies all transitions associated with the given source state to
   * the given destination state. This method rebuilds the transition list
   * of the destination state so it contains any event-to-state pairs that
   * originally are associated with at least one of the two states.
   * Duplicates are suppressed, and ordering is preserved such that
   * transitions originally associated with the source state appear
   * earlier in the resultant list. The operation is implemented as a
   * merge operation using a priority queue.
   * @param  source   From-state containing transitions to be copied.
   * @param  dest     From-state to receive transitions.
   * @param  reverse  Another transition list buffer containing the reverse
   *                  of this buffer's transitions. If non-<CODE>null</CODE>
   *                  any transitions added during this operation will also
   *                  be added to the reverse buffer, in reversed form.
   * @return <CODE>true</CODE> if at least one transition was copied;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean copyTransitions(final int source,
                                 final int dest,
                                 final TransitionListBuffer reverse)
  {
    if (source == dest) {
      return false;
    }
    final int list1 = mStateTransitions[source];
    if (list1 == NULL) {
      return false;
    }
    final TIntArrayList list = new TIntArrayList(1);
    list.add(source);
    return copyTransitions(list, dest, reverse);
  }

  /**
   * Copies all transitions associated with the given source states to
   * the given destination state. This method rebuilds the transition list
   * of the destination state so it contains any event-to-state pairs that
   * originally are associated with at least one of the two states.
   * Duplicates are suppressed, and ordering is preserved such that
   * transitions originally associated with the source state appear
   * earlier in the resultant list. The operation is implemented as a
   * merge operation using a priority queue, and assumes that all transition
   * list are ordered by nondeterministic to-states.
   * @param  sources  List of from-states containing transitions to be copied.
   * @param  dest     From-state to receive transitions.
   * @param  reverse  Another transition list buffer containing the reverse
   *                  of this buffer's transitions. If non-<CODE>null</CODE>
   *                  any transitions added during this operation will also
   *                  be added to the reverse buffer, in reversed form.
   * @return <CODE>true</CODE> if at least one transition was copied;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean copyTransitions(final TIntArrayList sources,
                                 final int dest,
                                 final TransitionListBuffer reverse)
  {
    if (sources.size() == 0 ||
        sources.size() == 1 && sources.get(0) == dest) {
      return false;
    }
    final int size = sources.size() + 1;
    final int[] lists = new int[size];
    final int list0 = mStateTransitions[dest];
    if (list0 != NULL) {
      lists[0] = getNext(list0);
    }
    boolean allNull = true;
    for (int i = 1; i < size; i++) {
      final int source = sources.get(i-1);
      final int list1 = mStateTransitions[source];
      if (list1 != NULL) {
        lists[i] = getNext(list1);
        allNull = false;
      }
    }
    if (allNull) {
      return false;
    }
    final WatersIntComparator comparator = new WatersIntComparator() {
      @Override
      public int compare(final int index1, final int index2)
      {
        final int list1 = lists[index1];
        final int data1 = getData(list1);
        final int event1 = data1 & mEventMask;
        final int list2 = lists[index2];
        final int data2 = getData(list2);
        final int event2 = data2 & mEventMask;
        if (event1 != event2) {
          return event1 - event2;
        } else if (data1 < data2) {
          return -1;
        } else if (data1 > data2) {
          return 1;
        } else {
          return index1 - index2;
        }
      }
    };
    final WatersIntHeap heap = new WatersIntHeap(size, comparator);
    for (int i = 0; i < size; i++) {
      if (lists[i] != NULL) {
        heap.add(i);
      }
    }
    int prevData = 0;
    final int newList = createList();
    int tail = newList;
    boolean addition = false;
    while (!heap.isEmpty()) {
      final int i = heap.removeFirst();
      final int list = lists[i];
      final int data = getData(list); // next item!!!
      final int next = getNext(list);
      lists[i] = next;
      if (next != NULL) {
        heap.add(i);
      }
      if (prevData == data && newList != tail) {
        // Suppress duplicates coming in from other lists
        continue;
      }
      final int state = data >>> mStateShift;
      final int event = data & mEventMask;
      final byte status = mEventStatus.getProperEventStatus(event);
      if (state == dest && EventStatus.isSelfloopOnlyEvent(status)) {
        // Suppress tau and outside selfloop-only selfloops
        continue;
      }
      final int pair = allocatePair();
      setNext(tail, pair);
      tail = pair;
      setDataAndNext(pair, data, NULL);
      prevData = data;
      if (i > 0) {
        addition = true;
        if (reverse != null) {
          reverse.addTransition(state, event, dest);
        }
      }
    }
    if (addition) {
      mStateTransitions[dest] = newList;
      dispose(lists[0]);
      final int destCode = dest << mStateShift;
      int prevEvent = -1;
      int current = newList;
      while (true) {
        final int next = getNext(current);
        if (next == NULL) {
          break;
        }
        final int event = getEvent(next);
        if (event != prevEvent) {
          final int code = destCode | event;
          mStateEventTransitions.put(code, current);
          prevEvent = event;
        }
        current = next;
      }
    } else {
      dispose(newList);
    }
    return addition;
  }

  /**
   * Removes all transitions associated with the given from-state.
   * @return <CODE>true</CODE> if at least one transition was removed;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean removeStateTransitions(final int from)
  {
    final int list = mStateTransitions[from];
    if (list == NULL) {
      return false;
    } else {
      final int fromShift = from << mStateShift;
      int current = list;
      int next = getNext(current);
      int e0 = -1;
      final boolean remove = next != NULL;
      while (next != NULL) {
        current = next;
        final int[] block = mBlocks.get(current >>> mBlockShift);
        final int offset = current & mBlockMask;
        final int data = block[offset + OFFSET_DATA];
        final int e = data & mEventMask;
        if (e != e0) {
          mStateEventTransitions.remove(fromShift | e);
          e0 = e;
        }
        next = block[offset + OFFSET_NEXT];
      }
      setNext(current, mRecycleStart);
      mRecycleStart = list;
      mStateTransitions[from] = NULL;
      return remove;
    }
  }

  /**
   * Removes all transitions associated with the given event.
   * @return <CODE>true</CODE> if at least one transition was removed;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean removeEventTransitions(final int event)
  {
    final TransitionIterator iter = createModifyingIterator();
    boolean remove = false;
    for (int state = 0; state < getNumberOfStates(); state++) {
      iter.reset(state, event);
      if (iter.advance()) {
        do {
          iter.remove();
        } while (iter.advance());
        remove = true;
      }
    }
    return remove;
  }

  /**
   * Replaces an event by another.
   * This method replaces all transitions with the given old event ID
   * by transitions with the given new event ID. Any new transitions with the
   * new event ID are inserted after any transitions already present in the
   * lists.
   * @param  oldID   The ID of the old event to be replaced.
   * @param  newID   The ID of the new event replacing the old event.
   */
  public void replaceEvent(final int oldID, final int newID)
  {
    final int numEvents = mEventStatus.getNumberOfProperEvents();
    if (oldID == newID) {
      return;
    } else if (newID < 0 || newID >= numEvents) {
      throw new IllegalArgumentException
        ("New event ID " + newID + " out of range in " +
         ProxyTools.getShortClassName(this) +
         " (only configured for " + numEvents + " events)!");
    }
    final byte status = mEventStatus.getProperEventStatus(newID);
    final boolean selfloop = EventStatus.isSelfloopOnlyEvent(status);
    final TIntHashSet found = new TIntHashSet();
    final TransitionIterator iter1 = createReadOnlyIterator();
    final TransitionIterator iter2 = createModifyingIterator();
    for (int state = 0; state < getNumberOfStates(); state++) {
      final int newCode = (state << mStateShift) | newID;
      int list = mStateEventTransitions.get(newCode);
      int next = getNext(list);
      while (next != NULL) {
        final int[] block = mBlocks.get(next >>> mBlockShift);
        final int offset = next & mBlockMask;
        final int data = block[offset + OFFSET_DATA];
        final int e = data & mEventMask;
        if (e != newID) {
          break;
        }
        found.add(data >>> mStateShift);
        list = next;
        next = block[offset + OFFSET_NEXT];
      }
      boolean added = false;
      iter1.reset(state, oldID);
      while (iter1.advance()) {
        final int to = iter1.getCurrentToState();
        if (selfloop && state == to) {
          // nothing --- suppress tau and outside selfloop-only selfloops ...
        } else if (found.add(to)) {
          if (list == NULL) {
            list = createList(state, newID);
          }
          final int code = (to << mStateShift) | newID;
          list = prepend(list, code);
          added = true;
        }
      }
      found.clear();
      if (added) {
        next = getNext(list);
        if (next != NULL) {
          final int nextEvent = getEvent(next);
          final int nextCode = (state << mStateShift) | nextEvent;
          mStateEventTransitions.put(nextCode, list);
        }
      }
      iter2.reset(state, oldID);
      while (iter2.advance()) {
        iter2.remove();
      }
    }
  }

  /**
   * Determines whether the given event is globally disabled in this buffer.
   * @param event
   *          The ID of the event to be tested.
   * @return <CODE>true</CODE> if the given event is disabled in every state.
   */
  public boolean isGloballyDisabled(final int event)
  {
    final TransitionIterator iter =
      createAllTransitionsReadOnlyIterator(event);
    return !iter.advance();
  }

  /**
   * Helps to clean up tau selfloops. This method removes all selfloops
   * associated with {@link EventEncoding#TAU} and tests whether this results in
   * the event being redundant.
   * @param  tau    The code of the tau event to be checked.
   * @return <CODE>true</CODE> if all transitions with the tau event
   *         were selfloops and have been removed, or if no tau transitions
   *         have been found; <CODE>false</CODE> otherwise.
   */
  public boolean removeTauSelfloops(final int tau)
  {
    boolean removable = true;
    final TransitionIterator iter = createModifyingIterator();
    for (int state = 0; state < getNumberOfStates(); state++) {
      iter.reset(state, tau);
      while (iter.advance()) {
        if (iter.getCurrentToState() == state) {
          iter.remove();
        } else {
          removable = false;
        }
      }
    }
    return removable;
  }


  //#########################################################################
  //# Iterators
  /**
   * Creates a read-only iterator for this buffer.
   * The iterator returned is not initialised, so one of the methods
   * {@link TransitionIterator#resetState(int)} or
   * {@link TransitionIterator#reset(int, int)} must be called before it can
   * be used. Being a read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createReadOnlyIterator()
  {
    return new ReadOnlyIterator();
  }

  /**
   * Creates a read-only iterator for this buffer that is set up to
   * iterate over the transitions associated with the given from-state.
   * The iterator returned produces all transitions with the given-from-state
   * in the buffer's defined ordering, no matter what event they use.
   * Being a read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createReadOnlyIterator(final int state)
  {
    final TransitionIterator iter = new ReadOnlyIterator();
    iter.resetState(state);
    return iter;
  }

  /**
   * Creates a read-only iterator for this buffer that is set up to iterate
   * over the transitions associated with the given from-state and event.
   * The iterator returned produces all transitions with the given-from-state
   * and event in the buffer's defined ordering.
   * Being a read-only iterator, it does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createReadOnlyIterator(final int state,
                                                   final int event)
  {
    final TransitionIterator iter = new ReadOnlyIterator();
    iter.reset(state, event);
    return iter;
  }

  /**
   * Creates a read-only iterator for this buffer to iterate
   * over the transitions associated with events with the given status
   * flags. The iterator returned is not initialised, so the method
   * {@link TransitionIterator#resetState(int)} needs to be called
   * to start iterating from a state.
   * @param flags
   *          Event status flags to specify the type of events,
   *          as passed to the
   *          {@link StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @see StatusGroupTransitionIterator
   */
  public TransitionIterator createReadOnlyIteratorByStatus(final int...flags)
  {
    final TransitionIterator inner = new ReadOnlyIterator();
    return new StatusGroupTransitionIterator(inner, mEventStatus, flags);
  }

  /**
   * Creates a read/write iterator for this buffer.
   * The iterator returned is not initialised, so one of the methods
   * {@link TransitionIterator#resetState(int)} or
   * {@link TransitionIterator#reset(int, int)} before it can be used.
   */
  public TransitionIterator createModifyingIterator()
  {
    return new ModifyingIterator();
  }

  /**
   * Creates a read/write iterator for this buffer that is set up to
   * iterate over the transitions associated with the given from-state.
   * The iterator returned produces all transitions with the given from-state
   * in the buffer's defined ordering, no matter what event they use.
   */
  public TransitionIterator createModifyingIterator(final int state)
  {
    final TransitionIterator iter = new ModifyingIterator();
    iter.resetState(state);
    return iter;
  }

  /**
   * Creates a read/write iterator for this buffer that is set up to iterate
   * over the transitions associated with the given from-state and event.
   * The iterator returned produces all transitions with the given from-state
   * and event in the buffer's defined ordering.
   */
  public TransitionIterator createModifyingIterator(final int state,
                                                    final int event)
  {
    final TransitionIterator iter = new ModifyingIterator();
    iter.reset(state, event);
    return iter;
  }

  /**
   * Creates a read/write iterator for this buffer to iterate
   * over the transitions associated with events with the given status
   * flags. The iterator returned is not initialised, so the method
   * {@link TransitionIterator#resetState(int)} needs to be called
   * to start iterating from a state.
   * @param flags
   *          Event status flags to specify the type of events,
   *          as passed to the
   *          {@link StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @see StatusGroupTransitionIterator
   */
  public TransitionIterator createModifyingIteratorByStatus(final int...flags)
  {
    final TransitionIterator inner = new ModifyingIterator();
    return new StatusGroupTransitionIterator(inner, mEventStatus, flags);
  }

  /**
   * Creates a read-only iterator over all transitions in this buffer.
   * The iterator returned is set up to return the first transition in
   * this buffer after calling {@link TransitionIterator#advance()}. It does
   * not implement the methods {@link TransitionIterator#resetState(int)}
   * or {@link TransitionIterator#reset(int,int)}, and
   * being a read-only iterator, it also does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createAllTransitionsReadOnlyIterator()
  {
    final TransitionIterator inner = createReadOnlyIterator();
    return new AllTransitionsIterator(inner);
  }

  /**
   * Creates a read-only iterator over all transitions with the given event.
   * The iterator returned is set up to return the first transition in
   * this buffer after calling {@link TransitionIterator#advance()}. It does
   * not implement the methods {@link TransitionIterator#resetState(int)}
   * or {@link TransitionIterator#reset(int,int)}, and
   * being a read-only iterator, it also does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createAllTransitionsReadOnlyIterator
    (final int event)
  {
    final TransitionIterator inner = createReadOnlyIterator();
    inner.resetEvent(event);
    return new AllTransitionsIterator(inner, event);
  }

  /**
   * Creates a read-only iterator for this buffer to iterate over all
   * transitions associated with events with the given status flags.
   * The iterator returned is set up to return the first transition in
   * this buffer after calling {@link TransitionIterator#advance()}. It does
   * not implement the methods {@link TransitionIterator#resetState(int)}
   * or {@link TransitionIterator#reset(int,int)}, and
   * being a read-only iterator, it also does not implement the
   * {@link TransitionIterator#remove()} method.
   * @param flags
   *          Event status flags to specify the type of events,
   *          as passed to the
   *          {@link StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @see StatusGroupTransitionIterator
   */
  public TransitionIterator createAllTransitionsReadOnlyIteratorByStatus
    (final int...flags)
  {
    final TransitionIterator inner = createAllTransitionsReadOnlyIterator();
    return new StatusGroupTransitionIterator(inner, mEventStatus, flags);
  }

  /**
   * Creates a read/write iterator over all transitions in this buffer.
   * The iterator returned is set up to return the first transition in
   * this buffer after calling {@link TransitionIterator#advance()}. It does
   * not implement the methods {@link TransitionIterator#resetState(int)}
   * or {@link TransitionIterator#reset(int,int)}.
   */
  public TransitionIterator createAllTransitionsModifyingIterator()
  {
    final TransitionIterator inner = createModifyingIterator();
    return new AllTransitionsIterator(inner);
  }

  /**
   * Creates a read/write iterator over all transitions with the given event
   * The iterator returned is set up to return the first transition in
   * this buffer after calling {@link TransitionIterator#advance()}. It does
   * not implement the methods {@link TransitionIterator#resetState(int)}
   * or {@link TransitionIterator#reset(int,int)}.
   */
  public TransitionIterator createAllTransitionsModifyingIterator
    (final int event)
  {
    final TransitionIterator inner = createModifyingIterator();
    inner.resetEvent(event);
    return new AllTransitionsIterator(inner, event);
  }

  /**
   * Creates a read/write iterator for this buffer to iterate over all
   * transitions associated with events with the given status flags.
   * The iterator returned is set up to return the first transition in
   * this buffer after calling {@link TransitionIterator#advance()}. It does
   * not implement the methods {@link TransitionIterator#resetState(int)}
   * or {@link TransitionIterator#reset(int,int)}.
   * @param flags
   *          Event status flags to specify the type of events,
   *          as passed to the
   *          {@link StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @see StatusGroupTransitionIterator
   */
  public TransitionIterator createAllTransitionsModifyingIteratorByStatus
    (final int...flags)
  {
    final TransitionIterator inner = createAllTransitionsModifyingIterator();
    return new StatusGroupTransitionIterator(inner, mEventStatus, flags);
  }

  /**
   * Gets the source state from the given iterator. This method is used by
   * local transition iterator implementations to interpret transitions.
   * It is overridden by subclasses to handle forward and backward transition
   * buffers uniformly.
   */
  public abstract int getIteratorSourceState(TransitionIterator iter);

  /**
   * Gets the target state from the given iterator. This method is used by
   * local transition iterator implementations to interpret transitions.
   * It is overridden by subclasses to handle forward and backward transition
   * buffers uniformly.
   */
  public abstract int getIteratorTargetState(TransitionIterator iter);


  //#########################################################################
  //# Automata Conversion Methods
  /**
   * Initialises this transition buffer with transitions from a given list.
   * This method replaces all transitions in the buffer by given transitions.
   * Transitions are added in a fixed order that is determined from the
   * given state and event encoding.
   * The method assumes that this transition buffer is empty when called,
   * if this is not the case, transitions will be overwritten without
   * releasing all memory. To correctly replace transitions in a used buffer,
   * call {@link #clear()} first.
   * @param  events       The event alphabet of the automaton to be
   *                      represented. This is used to determine which
   *                      additional selfloops are to be added.
   * @param  transitions  List of transitions to populate the new buffer.
   *                      The list will be reordered to match the order
   *                      chosen by the buffer.
   */
  public void setUpTransitions(final Set<EventProxy> events,
                               final List<TransitionProxy> transitions,
                               final EventEncoding eventEnc,
                               final StateEncoding stateEnc)
  {
    final Comparator<TransitionProxy> comparator =
      new TransitionComparator(eventEnc, stateEnc);
    Collections.sort(transitions, comparator);
    int from0 = -1;
    int e0 = -1;
    int data0 = -1;
    int list = NULL;
    for (final TransitionProxy trans : transitions) {
      final EventProxy event = trans.getEvent();
      final int e = eventEnc.getEventCode(event);
      if (e >= 0) {
        final StateProxy fromState = getFromState(trans);
        final StateProxy toState = getToState(trans);
        final byte status = mEventStatus.getProperEventStatus(e);
        if (fromState == toState &&
            EventStatus.isSelfloopOnlyEvent(status)) {
          // Suppress tau and outside selfloop-only selfloops
          continue;
        }
        final int from = stateEnc.getStateCode(fromState);
        final int to = stateEnc.getStateCode(toState);
        final int data = (to << mStateShift) | e;
        if (from != from0) {
          mStateTransitions[from] = list = createList();
          final int fromCode = (from << mStateShift) | e;
          mStateEventTransitions.put(fromCode, list);
          from0 = from;
          e0 = e;
        } else if (data0 == data) {
          continue;
        } else if (e0 != e) {
          final int fromCode = (from << mStateShift) | e;
          mStateEventTransitions.put(fromCode, list);
          e0 = e;
        }
        data0 = data;
        list = prepend(list, data);
      }
    }

    final int numStates = getNumberOfStates();
    final int numEvents = eventEnc.getNumberOfProperEvents();
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final byte status = eventEnc.getProperEventStatus(e);
      if ((status & EventStatus.STATUS_UNUSED) != 0) {
        final EventProxy event = eventEnc.getProperEvent(e);
        if (!events.contains(event)) {
          for (int s = 0; s < numStates; s++) {
            addTransition(s, e, s);
          }
        }
      }
    }
  }

  /**
   * Gets the from-state for the given transition. This method is used by
   * {@link #setUpTransitions(Set,List,EventEncoding,StateEncoding)
   * setUpTransitions()} to interpret transitions. It is overridden by
   * subclasses to handle forward and backward transition buffers uniformly.
   */
  public abstract StateProxy getFromState(TransitionProxy trans);

  /**
   * Gets the to-state for the given transition. This method is used by
   * {@link #setUpTransitions(Set,List,EventEncoding,StateEncoding)
   * setUpTransitions()} to interpret transitions. It is overridden by
   * subclasses to handle forward and backward transition buffers uniformly.
   */
  public abstract StateProxy getToState(TransitionProxy trans);


  /**
   * <P>Initialises this transition buffer with transitions from another
   * transition list buffer.</P>
   *
   * <P>This can be used to duplicate transition buffers,
   * or to convert from forward to backward indexing and vice versa.
   * This method replaces all transitions in the buffer by given transitions.
   * Transitions are added in a fixed order that is determined from the
   * given state and event encoding.</P>
   *
   * <P>The method assumes that this transition buffer is empty when called,
   * if this is not the case, transitions will be overwritten without
   * releasing all memory. To correctly replace transitions in a used buffer,
   * call {@link #clear()} first.</P>
   *
   * <P>The event encoding of the two transition buffers must be compatible.
   * Transitions with events that are not present or marked as unused in the
   * receiving transition are not copied. Selfloops by events marked as
   * selfloop-only are also suppressed.</P>
   */
  public void setUpTransitions(final TransitionListBuffer other)
  {
    final int numEvents = mEventStatus.getNumberOfProperEvents();
    final TransitionIterator iter =
      other.createAllTransitionsReadOnlyIterator();
    if (getClass() == other.getClass()) {
      // If the source buffer is of the same type (predecessors/successors),
      // just copy the transitions in the same order.
      int from0 = -1;
      int event0 = -1;
      int list = NULL;
      while (iter.advance()) {
        final int event = iter.getCurrentEvent();
        if (event >= numEvents) {
          continue;
        }
        final byte status = mEventStatus.getProperEventStatus(event);
        if (!EventStatus.isUsedEvent(status)) {
          continue;
        }
        final int from = getOtherIteratorFromState(iter);
        final int to = getOtherIteratorToState(iter);
        if (from == to && EventStatus.isSelfloopOnlyEvent(status)) {
          continue;
        }
        if (from != from0) {
          mStateTransitions[from] = list = createList();
          from0 = from;
          event0 = -1;
        }
        if (event0 != event) {
          final int fromCode = (from << mStateShift) | event;
          mStateEventTransitions.put(fromCode, list);
          event0 = event;
        }
        final int data = (to << mStateShift) | event;
        list = prepend(list, data);
      }
    } else {
      // If the buffers are of different type, we must bring the transitions
      // in the correct order for this buffer before building the lists.
      final int numTrans = other.getNumberOfTransitions();
      final TLongArrayList transitions = new TLongArrayList(numTrans);
      final int eventShift = AutomatonTools.log2(mNumStates);
      final int fromShift = eventShift + AutomatonTools.log2(numEvents);
      final int toMask = (1 << eventShift) - 1;
      while (iter.advance()) {
        final int e = iter.getCurrentEvent();
        if (e >= numEvents) {
          continue;
        }
        final byte status = mEventStatus.getProperEventStatus(e);
        if (!EventStatus.isUsedEvent(status)) {
          continue;
        }
        final long from = getOtherIteratorFromState(iter);
        final long to = getOtherIteratorToState(iter);
        if (from == to && EventStatus.isSelfloopOnlyEvent(status)) {
          continue;
        }
        final long event = e;
        transitions.add((from << fromShift) | (event << eventShift) | to);
      }
      transitions.sort();
      int from0 = -1;
      int event0 = -1;
      int list = NULL;
      for (int i = 0; i < transitions.size(); i++) {
        final long trans = transitions.get(i);
        final int from = (int) (trans >>> fromShift);
        final int event = (int) (trans >>> eventShift) & mEventMask;
        final int to = (int) (trans & toMask);
        if (from != from0) {
          mStateTransitions[from] = list = createList();
          from0 = from;
          event0 = -1;
        }
        if (event0 != event) {
          final int fromCode = (from << mStateShift) | event;
          mStateEventTransitions.put(fromCode, list);
          event0 = event;
        }
        final int data = (to << mStateShift) | event;
        list = prepend(list, data);
      }
    }
  }

  /**
   * Gets a from-state from the given iterator, which may belong to another
   * transition list buffer. This method is used by {@link
   * #setUpTransitions(TransitionListBuffer) setUpTransitions()} to interpret
   * transitions. It is overridden by subclasses to handle forward and backward
   * transition buffers uniformly.
   */
  public abstract int getOtherIteratorFromState(TransitionIterator iter);

  /**
   * Gets a to-state from the given iterator, which may belong to another
   * transition list buffer. This method is used by {@link
   * #setUpTransitions(TransitionListBuffer) setUpTransitions()} to interpret
   * transitions. It is overridden by subclasses to handle forward and backward
   * transition buffers uniformly.
   */
  public abstract int getOtherIteratorToState(TransitionIterator iter);


  /**
   * Merges the transitions in this buffer according to a merge operation
   * of the transition relation.
   * @param partition
   *          The partition to be imposed.
   * @see ListBufferTransitionRelation#merge(TRPartition) ListBufferTransitionRelation.merge()
   */
  public void merge(final TRPartition partition, final int extraStates)
  {
    mStateEventTransitions.clear();
    final int numClasses = partition.getNumberOfClasses() + extraStates;
    final int[] newStateTransitions = new int[numClasses];
    final int eventShift = AutomatonTools.log2(mNumStates);
    final int stateMask = (1 << eventShift) - 1;
    final TIntHashSet transitions = new TIntHashSet();
    int code = 0;
    for (final int[] clazz : partition.getClasses()) {
      if (clazz != null) {
        int list;
        for (final int state : clazz) {
          list = mStateTransitions[state];
          int current = getNext(list);
          while (current != NULL) {
            final int[] block = mBlocks.get(current >>> mBlockShift);
            final int offset = current & mBlockMask;
            final int data = block[offset + OFFSET_DATA];
            final int event = data & mEventMask;
            final int target = partition.getClassCode(data >>> mStateShift);
            final byte status = mEventStatus.getProperEventStatus(event);
            final boolean selfloop =
              EventStatus.isSelfloopOnlyEvent(status);
            if (!selfloop || code != target) {
              // suppress tau-selfloops and only-other selfloops
              final int trans = (event << eventShift) | target;
              transitions.add(trans);
            }
            final int next = block[offset + OFFSET_NEXT];
            if (next == NULL) { // delete list once read
              block[offset + OFFSET_NEXT] = mRecycleStart;
              mRecycleStart = list;
            }
            current = next;
          }
        }
        if (transitions.isEmpty()) {
          newStateTransitions[code] = NULL;
        } else {
          final int[] transarray = transitions.toArray();
          transitions.clear();
          Arrays.sort(transarray);
          newStateTransitions[code] = list = createList();
          int event0 = -1;
          for (final int trans : transarray) {
            final int event = (trans >>> eventShift);
            final int target = trans & stateMask;
            if (event0 != event) {
              final int fromCode = (code << mStateShift) | event;
              mStateEventTransitions.put(fromCode, list);
              event0 = event;
            }
            final int data = (target << mStateShift) | event;
            list = prepend(list, data);
          }
        }
      }
      code++;
    }
    mStateTransitions = newStateTransitions;
    mNumStates = numClasses;
  }


  //#########################################################################
  //# Auxiliary Access
  int makeKey(final int state, final int event)
  {
    return (state << mStateShift) | event;
  }


  //#########################################################################
  //# Debugging
  /**
   * Checks the integrity of this transition list buffer.
   * This method examines all transition lists and checks whether all lists
   * have consistent and well-ordered data and link structure and whether the
   * index structure is complete and correctly linked to the transition lists.
   * @throws AssertionError if the data structure is found to be in an
   *                        inconsistent state.
   */
  public void checkIntegrity()
  {
    final int[] block0 = mBlocks.get(0);
    if (block0[0] != 0 || block0[1] != 0) {
      throw new AssertionError("Zero-block was tampered with in " +
                               ProxyTools.getShortClassName(this) + "!");
    }

    final TIntIntIterator iter = mStateEventTransitions.iterator();
    while (iter.hasNext()) {
      iter.advance();
      final int key = iter.key();
      final int from = (key >>> mStateShift);
      checkState(from);
      final int event = key & mEventMask;
      checkEvent(event);
      final int list = iter.value();
      checkList(list);
      if (list == NULL) {
        throw new AssertionError("NULL list found for key " + from + "/" +
                                 event + " in " +
                                 ProxyTools.getShortClassName(this));
      }
      final int next = getNext(list);
      checkList(next);
      final int data = getData(next);
      final int to = (data >>> mStateShift);
      checkState(to);
      final int dataEvent = data & mEventMask;
      if (dataEvent != event) {
        throw new AssertionError("Unexpected event " + dataEvent +
                                 " found in list for key " + from + "/" +
                                 event + " in " +
                                 ProxyTools.getShortClassName(this));
      }
    }

    for (int from = 0; from < mNumStates; from++) {
      int list = mStateTransitions[from];
      if (list != NULL) {
        checkList(list);
        int prev = list;
        int prevEvent = -1;
        int prevTo = -1;
        list = getNext(list);
        checkList(list);
        if (list == NULL) {
          throw new AssertionError("Empty non-NULL list found for state " +
                                   from + " in " +
                                   ProxyTools.getShortClassName(this) + "!");
        }
        do {
          final int data = getData(list);
          final int to = (data >>> mStateShift);
          checkState(to);
          final int event = data & mEventMask;
          checkEvent(event);
          if (event > prevEvent) {
            final int key = (from << mStateShift) | event;
            final int lookup = mStateEventTransitions.get(key);
            if (lookup != prev) {
              throw new AssertionError("List for key " + from + "/" + event +
                                       " not indexed correctly in " +
                                       ProxyTools.getShortClassName(this) +
                                       "!");
            }
            prevEvent = event;
            prevTo = to;
          } else if (event == prevEvent) {
            if (to < prevTo) {
              throw new AssertionError("Nondeterministic states for key " +
                                       from + "/" + event +
                                       " not ordered in " +
                                       ProxyTools.getShortClassName(this) +
                                       ": found " + prevTo + " followed by " +
                                       to + "!");
            } else if (to == prevTo) {
              throw new AssertionError("To-state " + to +
                                       " encountered twice for key " +
                                       from + "/" + event + " in " +
                                       ProxyTools.getShortClassName(this) +
                                       "!");
            } else {
              prevTo = to;
            }
          } else {
            throw new AssertionError("Event number " + event +
                                     " out of sequence, after " + prevEvent +
                                     ", in list for " + from + " in " +
                                     ProxyTools.getShortClassName(this) + "!");
          }
          prev = list;
          list = getNext(list);
          checkList(list);
        } while (list != NULL);
      }
    }
  }

  private void checkState(final int state)
  {
    if (state < 0 || state >= mNumStates) {
      throw new AssertionError("Invalid state number " + state +
                               " in " + ProxyTools.getShortClassName(this));
    }
  }

  private void checkEvent(final int event)
  {
    final int numEvents = mEventStatus.getNumberOfProperEvents();
    if (event < 0 || event >= numEvents) {
      throw new AssertionError("Invalid event number " + event + " in " +
                               ProxyTools.getShortClassName(this) + "!");
    }
  }

  private void checkList(final int list)
  {
    if (list < 0 || list >= mNextFreeIndex) {
      throw new AssertionError("Invalid list address " + list +
                               " in " + ProxyTools.getShortClassName(this));
    }
  }


  //#########################################################################
  //# Raw List Access Methods
  private int createList()
  {
    final int list = allocatePair();
    setNext(list, NULL);
    return list;
  }

  /**
   * Inserts data as the second element of a given transition list.
   * This method creates a new list node and inserts it into a given list
   * as the first item after the head node (which is identified by list).
   * This results in the data becoming the first list node. The new list
   * node is returned and can be used as the tail of the list. To construct
   * a list from start to end, the prepend method is called repeatedly, each
   * time using the result of the previous call as the list.
   * @param  list  The transition list to be modified.
   * @param  data  The data (event/state pair) to be inserted.
   * @return The index of the new list node.
   */
  private int prepend(final int list, final int data)
  {
    final int tail = getNext(list);
    final int pair = allocatePair();
    setDataAndNext(pair, data, tail);
    setNext(list, pair);
    return pair;
  }

  private int getLength(final int list)
  {
    if (list == NULL) {
      return 0;
    } else {
      int count = 0;
      for (int next = getNext(list); next != NULL; next = getNext(next)) {
        count++;
      }
      return count;
    }
  }

  private void dispose(final int list)
  {
    if (list != NULL) {
      int current = list;
      int next = getNext(current);
      while (next != NULL) {
        current = next;
        next = getNext(current);
      }
      setNext(current, mRecycleStart);
      mRecycleStart = list;
    }
  }

  private int createList(final int from, final int event)
  {
    final int fromShift = from << mStateShift;
    final int fromCode = fromShift | event;
    int list = mStateEventTransitions.get(fromCode);
    if (list == NULL) {
      for (int e = event - 1; e >= 0; e--) {
        final int code = fromShift | e;
        list = mStateEventTransitions.get(code);
        if (list != NULL) {
          list = seek(list, event);
          break;
        }
      }
      if (list == NULL) {
        list = mStateTransitions[from];
        if (list == NULL) {
          list = mStateTransitions[from] = createList();
        }
      }
      mStateEventTransitions.put(fromCode, list);
    }
    return list;
  }

  private int seek(int list, final int event)
  {
    int next = getNext(list);
    while (next != NULL) {
      final int[] block = mBlocks.get(next >>> mBlockShift);
      final int offset = next & mBlockMask;
      final int data = block[offset + OFFSET_DATA];
      if ((data & mEventMask) >= event) {
        return list;
      }
      list = next;
      next = block[offset + OFFSET_NEXT];
    }
    return list;
  }


  //#########################################################################
  //# Auxiliary Methods
  @SuppressWarnings("unused")
  private int getState(final int list)
  {
    return getData(list) >>> mStateShift;
  }

  private int getEvent(final int list)
  {
    return getData(list) & mEventMask;
  }

  private int getData(final int list)
  {
    final int[] block = mBlocks.get(list >>> mBlockShift);
    return block[(list & mBlockMask) + OFFSET_DATA];
  }

  private int getNext(final int list)
  {
    final int[] block = mBlocks.get(list >>> mBlockShift);
    return block[(list & mBlockMask) + OFFSET_NEXT];
  }

  private void setNext(final int list, final int next)
  {
    final int[] block = mBlocks.get(list >>> mBlockShift);
    block[(list & mBlockMask) + OFFSET_NEXT] = next;
  }

  private void setDataAndNext(final int list, final int data, final int next)
  {
    final int[] block = mBlocks.get(list >>> mBlockShift);
    final int offset = list & mBlockMask;
    block[offset + OFFSET_NEXT] = next;
    block[offset + OFFSET_DATA] = data;
  }

  private int allocatePair()
  {
    if (mRecycleStart != NULL) {
      final int result = mRecycleStart;
      mRecycleStart = getNext(mRecycleStart);
      return result;
    } else {
      if ((mNextFreeIndex >>> mBlockShift) >= mBlocks.size()) {
        final int[] block = new int[mBlockSize];
        mBlocks.add(block);
      }
      final int result = mNextFreeIndex;
      mNextFreeIndex += 2;
      return result;
    }
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final StringWriter writer = new StringWriter();
    final PrintWriter printer = new PrintWriter(writer);
    dump(printer);
    return writer.toString();
  }

  public void dump(final PrintWriter printer)
  {
    printer.print('{');
    final TransitionIterator iter = createAllTransitionsReadOnlyIterator();
    boolean first = true;
    while (iter.advance()) {
      if (first) {
        first = false;
      } else {
        printer.print(", ");
      }
      printer.print(iter.getCurrentSourceState());
      printer.print(" -");
      printer.print(iter.getCurrentEvent());
      printer.print("-> ");
      printer.print(iter.getCurrentTargetState());
    }
    printer.print('}');
  }


  //#########################################################################
  //# Inner Class ReadOnlyIterator
  private class ReadOnlyIterator implements TransitionIterator
  {

    //#######################################################################
    //# Constructor
    private ReadOnlyIterator()
    {
      mCurrent = NULL;
      mFirstEvent = 0;
      mLastEvent = mEventStatus.getNumberOfProperEvents() - 1;
      mFromState = -1;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.tr.TransitionIterator
    @Override
    public ReadOnlyIterator clone()
    {
      try {
        return (ReadOnlyIterator) super.clone();
      } catch (final CloneNotSupportedException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    @Override
    public void reset()
    {
      if (mFromState >= 0 && mFirstEvent <= mLastEvent) {
        if (mFirstEvent == 0) {
          setCurrent(mStateTransitions[mFromState]);
        } else {
          int event = mFirstEvent;
          int code = (mFromState << mStateShift) | event;
          int current = mStateEventTransitions.get(code);
          while (current == NULL && event < mLastEvent) {
            event++;
            code++;
            current = mStateEventTransitions.get(code);
          }
          setCurrent(current);
        }
      } else {
        mCurrent = NULL;
      }
    }

    @Override
    public void resetEvent(final int event)
    {
      if (event < 0) {
        mFirstEvent = 0;
        mLastEvent = mEventStatus.getNumberOfProperEvents() - 1;
      } else {
        mFirstEvent = mLastEvent = event;
      }
      reset();
    }

    @Override
    public void resetEvents(final int first, final int last)
    {
      mFirstEvent = first < 0 ? 0 : first;
      final int numEvents = mEventStatus.getNumberOfProperEvents();
      mLastEvent = last >= numEvents ? numEvents - 1 : last;
      reset();
    }

    @Override
    public void resetState(final int state)
    {
      mFromState = state;
      reset();
    }

    @Override
    public void reset(final int state, final int event)
    {
      mFromState = state;
      resetEvent(event);
    }

    @Override
    public void resume(final int state)
    {
      resetState(state);
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
      if (mCurrent == NULL || mNext == NULL) {
        return false;
      }
      setCurrent(mNext);
      final int event = mCurrentData & mEventMask;
      if (event > mLastEvent) {
        mCurrent = NULL;
        return false;
      } else {
        return true;
      }
    }

    @Override
    public int getCurrentSourceState()
    {
      return getIteratorSourceState(this);
    }

    @Override
    public int getCurrentFromState()
    {
      return mFromState;
    }

    @Override
    public int getCurrentEvent()
    {
      assert mCurrent != NULL;
      return mCurrentData & mEventMask;
    }

    @Override
    public int getCurrentTargetState()
    {
      return getIteratorTargetState(this);
    }

    @Override
    public int getCurrentToState()
    {
      assert mCurrent != NULL;
      return mCurrentData >>> mStateShift;
    }

    @Override
    public void setCurrentToState(final int state)
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition modification!");
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        ("ReadOnlyIterator does not support removal of elements!");
    }

    //#######################################################################
    //# Auxiliary Methods
    int getCurrent()
    {
      return mCurrent;
    }

    void setCurrent(final int current)
    {
      mCurrent = current;
      if (current != NULL) {
        final int[] block = mBlocks.get(current >>> mBlockShift);
        final int offset = current & mBlockMask;
        mCurrentData = block[offset + OFFSET_DATA];
        mNext = block[offset + OFFSET_NEXT];
      }
    }

    void setCurrentData(final int data)
    {
      mCurrentData = data;
    }

    //#######################################################################
    //# Data Members
    private int mFromState;
    private int mCurrent;
    private int mFirstEvent;
    private int mLastEvent;
    private int mCurrentData;
    private int mNext;

  }


  //#########################################################################
  //# Inner Class ModifyingIterator
  private class ModifyingIterator extends ReadOnlyIterator
  {

    //#######################################################################
    //# Constructor
    private ModifyingIterator()
    {
      mPrevious = NULL;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.tr.TransitionIterator
    @Override
    public ModifyingIterator clone()
    {
      return (ModifyingIterator) super.clone();
    }

    @Override
    public boolean advance()
    {
      mPrevious = getCurrent();
      return super.advance();
    }

    @Override
    public void setCurrentToState(final int state)
    {
      final int current = getCurrent();
      if (current == NULL) {
        throw new IllegalStateException
          ("Attempting to modify TransitionListBuffer " +
           "without previous call to advance()!");
      }
      final int[] block = mBlocks.get(current >>> mBlockShift);
      final int offset = current & mBlockMask;
      final int olddata = block[offset + OFFSET_DATA];
      final int newdata = (state << mStateShift) | (olddata & mEventMask);
      block[offset + OFFSET_DATA] = newdata;
      setCurrentData(newdata);
    }

    @Override
    public void remove()
    {
      if (mPrevious == NULL) {
        throw new IllegalStateException
          ("Attempting to remove from TransitionListBuffer " +
           "without previous call to advance()!");
      }
      final int current = getCurrent();
      if (current == NULL) {
        throw new IllegalStateException
          ("Attempting to remove nonexistent element in TransitionListBuffer!");
      }
      final int state = getCurrentFromState();
      final boolean isRoot = (mStateTransitions[state] == mPrevious);
      final int prevEvent = isRoot ? -1 : getEvent(mPrevious);
      final int[] block = mBlocks.get(current >>> mBlockShift);
      final int offset = current & mBlockMask;
      final int next = block[offset + OFFSET_NEXT];
      setNext(mPrevious, next);
      block[offset + OFFSET_NEXT] = mRecycleStart;
      mRecycleStart = current;
      final int data = block[offset + OFFSET_DATA];
      final int event = data & mEventMask;
      final int nextEvent = next == NULL ? -1 : getEvent(next);
      if (event != prevEvent) {
        final int fromCode = (state << mStateShift) | event;
        if (next == NULL) {
          mStateEventTransitions.remove(fromCode);
          if (isRoot) {
            mStateTransitions[state] = NULL;
            dispose(mPrevious);
            mPrevious = NULL;
          }
        } else if (nextEvent != event) {
          mStateEventTransitions.remove(fromCode);
        }
      }
      if (nextEvent != event && nextEvent >= 0) {
        final int nextCode = (state << mStateShift) | nextEvent;
        mStateEventTransitions.put(nextCode, mPrevious);
      }
      setCurrent(mPrevious);
      mPrevious = NULL;
    }

    //#######################################################################
    //# Data Members
    private int mPrevious;

  }


  //#########################################################################
  //# Inner Class AllTransitionsIterator
  private class AllTransitionsIterator implements TransitionIterator
  {

    //#######################################################################
    //# Constructor
    private AllTransitionsIterator(final TransitionIterator inner)
    {
      this(inner, -1);
    }

    private AllTransitionsIterator(final TransitionIterator inner,
                                   final int event)
    {
      mInnerIterator = inner;
      mInnerIterator.resetEvent(event);
      mCurrentFromState = -1;
    }

    //#########################################################################
    //# Interface net.sourceforge.waters.tr.TransitionIterator
    @Override
    public AllTransitionsIterator clone()
    {
      try {
        final AllTransitionsIterator cloned =
          (AllTransitionsIterator) super.clone();
        cloned.mInnerIterator = mInnerIterator.clone();
        return cloned;
      } catch (final CloneNotSupportedException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    @Override
    public void reset()
    {
      mCurrentFromState = -1;
    }

    @Override
    public void resetEvent(final int event)
    {
      mInnerIterator.reset(-1, event);
      reset();
    }

    @Override
    public void resetEvents(final int first, final int last)
    {
      mInnerIterator.resetState(-1);
      mInnerIterator.resetEvents(first, last);
      reset();
    }

    @Override
    public void resetState(final int from)
    {
      throw new UnsupportedOperationException
        ("All-transitions iterator cannot be reset to iterate " +
         "over only a part of the states!");
    }

    @Override
    public void reset(final int from, final int event)
    {
      resetState(from);
    }

    @Override
    public void resume(final int state)
    {
      resetState(state);
    }

    @Override
    public int getFirstEvent()
    {
      return mInnerIterator.getFirstEvent();
    }

    @Override
    public int getLastEvent()
    {
      return mInnerIterator.getLastEvent();
    }

    @Override
    public boolean advance()
    {
      while (!mInnerIterator.advance()) {
        do {
          mCurrentFromState++;
          if (mCurrentFromState >= mStateTransitions.length) {
            return false;
          }
        } while (mStateTransitions[mCurrentFromState] == NULL);
        mInnerIterator.resetState(mCurrentFromState);
      }
      return true;
    }

    @Override
    public int getCurrentSourceState()
    {
      return getIteratorSourceState(this);
    }

    @Override
    public int getCurrentFromState()
    {
      if (mCurrentFromState < 0) {
        throw new NoSuchElementException
          ("Attempting to access transition in TransitionListBuffer " +
           "before calling advance()!");
      } else if (mCurrentFromState >= mStateTransitions.length) {
        throw new NoSuchElementException
          ("Reading past end of list in TransitionListBuffer!");
      } else {
        return mCurrentFromState;
      }
    }

    @Override
    public int getCurrentEvent()
    {
      return mInnerIterator.getCurrentEvent();
    }

    @Override
    public int getCurrentTargetState()
    {
      return getIteratorTargetState(this);
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
    public void remove()
    {
      mInnerIterator.remove();
    }

    //#########################################################################
    //# Data Members
    private TransitionIterator mInnerIterator;
    private int mCurrentFromState;

  }


  //#########################################################################
  //# Inner Class TransitionComparator
  private class TransitionComparator implements Comparator<TransitionProxy>
  {

    //#######################################################################
    //# Constructor
    private TransitionComparator(final EventEncoding eventEnc,
                                 final StateEncoding stateEnc)
    {
      mEventEncoding = eventEnc;
      mStateEncoding = stateEnc;
    }

    //#######################################################################
    //# Interface java.util.Comparator<TransitionProxy>
    @Override
    public int compare(final TransitionProxy trans1,
                       final TransitionProxy trans2)
    {
      final StateProxy from1 = getFromState(trans1);
      final StateProxy from2 = getFromState(trans2);
      int delta = mStateEncoding.getStateCode(from1) -
                  mStateEncoding.getStateCode(from2);
      if (delta != 0) {
        return delta;
      }
      final EventProxy event1 = trans1.getEvent();
      final EventProxy event2 = trans2.getEvent();
      delta = mEventEncoding.getEventCode(event1) -
              mEventEncoding.getEventCode(event2);
      if (delta != 0) {
        return delta;
      }
      final StateProxy to1 = getToState(trans1);
      final StateProxy to2 = getToState(trans2);
      return mStateEncoding.getStateCode(to1) -
             mStateEncoding.getStateCode(to2);
    }

    //#######################################################################
    //# Data Members
    private final EventEncoding mEventEncoding;
    private final StateEncoding mStateEncoding;

  }


  //#########################################################################
  //# Data Members
  private final int mBlockShift;
  private final int mBlockMask;
  private final int mBlockSize;

  private final EventStatusProvider mEventStatus;
  private final int mStateShift;
  private final int mEventMask;
  private final List<int[]> mBlocks;
  private int[] mStateTransitions;
  private final TIntIntHashMap mStateEventTransitions;

  private int mNumStates;
  private int mRecycleStart;
  private int mNextFreeIndex;


  //#########################################################################
  //# Class Constants
  public static final int NULL = 0;

  private static final int OFFSET_NEXT = 0;
  private static final int OFFSET_DATA = 1;
  private static final int NODE_SIZE = 2;

  private static final int MIN_BLOCK_SIZE = 64;
  private static final int MAX_BLOCK_SIZE = 2048;

}
