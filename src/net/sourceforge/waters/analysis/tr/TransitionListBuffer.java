//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   TransitionListBuffer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntHashingStrategy;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A data structure that stores ordered lists of transitions in a compact
 * way.
 *
 * The transition list buffer uses linked lists of integers stored in
 * pre-allocated arrays to store lists of transitions in a memory efficient
 * way. Each transition is stored in a list under its from-state, with the
 * event and to-state stored packed together in a single integer. This limits
 * the size to automata whose states and events numbers together can be packed
 * in 32&nbsp;bits.
 *
 * In addition, a single hash map is used to map pairs of from-state and
 * event to their list of transitions. This leads to a memory requirement
 * of approximately 24 byte per transition in the worst case where there are
 * only deterministic transitions.
 *
 * Iterators are provided to access transitions indexed by their from-state
 * and/or their event. Individual transitions can be added efficiently.
 * The test for existence of a particular transition or the removal of
 * a transition requires a search of all transitions with the corresponding
 * from-state and event and therefore may be of linear complexity.
 *
 * The list construction methods ensure that transitions are added in a defined
 * ordering, which depends on the contents and encoding of input transition
 * lists, or on the ordering of other transition lists when they are merged.
 * All iterators obey the defined ordering.
 *
 * The transition list buffer recognises the silent event code
 * {@link EventEncoding#TAU} and automatically suppresses all selfloops using
 * this event.
 *
 * This implementation is a shared superclass for buffers of incoming and
 * outgoing transitions in a {@link ListBufferTransitionRelation}. The 'from'
 * states used for indexing can be either actual source or target states.
 * Two subclasses {@link OutgoingTransitionListBuffer} and {@link
 * IncomingTransitionListBuffer} are used to adjust the access to
 * source and target states for these two types from user's point of view.
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
   * @param  numEvents   The number of events that can be encoded in
   *                     transitions.
   * @param  numStates   The number of states that can be encoded in
   *                     transitions.
   * @throws OverflowException if the encoding for states and events does
   *         not fit in the 32 bits available.
   */
  public TransitionListBuffer(final int numEvents,
                              final int numStates)
    throws OverflowException
  {
    this(numEvents, numStates, MAX_BLOCK_SIZE);
  }


  /**
   * Creates a new transition list buffer.
   * The transition buffer is set up for a fixed number of states and events,
   * which defines an encoding and can no more be changed.
   * @param  numEvents   The number of events that can be encoded in
   *                     transitions.
   * @param  numStates   The number of states that can be encoded in
   *                     transitions.
   * @param  numTrans    Estimated number of transitions, used to determine
   *                     buffer size.
   * @throws OverflowException if the encoding for states and events does
   *         not fit in the 32 bits available.
   */
  public TransitionListBuffer(final int numEvents,
                              final int numStates,
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
    mNumEvents = numEvents;
    mStateShift = AutomatonTools.log2(mNumEvents);
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
    mNumEvents = buffer.mNumEvents;
    mStateShift = buffer.mStateShift;
    mEventMask = buffer.mEventMask;
    mBlocks = buffer.mBlocks;
    mStateTransitions = buffer.mStateTransitions;
    mStateEventTransitions = buffer.mStateEventTransitions;
    mRecycleStart = buffer.mRecycleStart;
    mNextFreeIndex = buffer.mNextFreeIndex;
  }


  //#########################################################################
  //# Simple Access
  public int getNumberOfEvents()
  {
    return mNumEvents;
  }

  int getNumberOfStates()
  {
    return mStateTransitions.length;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    final StringBuffer buffer = new StringBuffer("{");
    final TransitionIterator iter = createAllTransitionsReadOnlyIterator();
    boolean first = true;
    while (iter.advance()) {
      if (first) {
        first = false;
      } else {
        buffer.append(", ");
      }
      buffer.append(iter.getCurrentSourceState());
      buffer.append(" -");
      buffer.append(iter.getCurrentEvent());
      buffer.append("-> ");
      buffer.append(iter.getCurrentTargetState());
    }
    buffer.append('}');
    return buffer.toString();
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
   * <P>Adds a transition to this buffer.</P>
   * <P>The new transition is appended after any other transitions with the
   * same from-state and event.</P>
   * <P><I>Note.</I> This method checks whether the requested transition
   * already is present in the buffer using a list search. Its worst-case
   * complexity is linear in the number of to-states for the given from-state
   * and event.</P>
   * @param  from   The ID of the from-state of the new transition.
   * @param  event  The ID of the event of the new transition.
   * @param  to     The ID of the to-state of the new transition.
   * @return <CODE>true</CODE> if a transition was added, i.e., if it was
   *         not already present in the buffer;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean addTransition(final int from, final int event, final int to)
  {
    if (event == EventEncoding.TAU && from == to) {
      return false;
    }
    final int newData = (to << mStateShift) | event;
    int list = createList(from, event);
    int next = getNext(list);
    while (next != NULL) {
      final int[] block = mBlocks.get(next >>> mBlockShift);
      final int offset = next & mBlockMask;
      final int data = block[offset + OFFSET_DATA];
      if (data == newData) {
        return false;
      } else if ((data & mEventMask) != event) {
        break;
      }
      list = next;
      next = block[offset + OFFSET_NEXT];
    }
    list = prepend(list, newData);
    next = getNext(list);
    if (next != NULL) {
      final int nextEvent = getEvent(next);
      if (nextEvent != event) {
        final int key = (from << mStateShift) | nextEvent;
        mStateEventTransitions.put(key, list);
      }
    }
    return true;
  }

  /**
   * Adds several transition to this buffer. New transitions for the given
   * from-state and event are inserted after existing transitions with the
   * same from-state and event. This method checks whether transitions are
   * already present and suppresses any duplicates. Its worst-case complexity
   * is linear in the number of transitions for the given from-state and event
   * <I>after</I> the operation.
   * @param  from     The ID of the from-state of the new transitions.
   * @param  event    The ID of the event of the new transitions.
   * @param  toStates List of target states of the new transitions.
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
    final TIntHashSet existing = new TIntHashSet();
    if (event == EventEncoding.TAU) {
      existing.add(from);
    }
    int list = createList(from, event);
    int next = getNext(list);
    while (next != NULL) {
      final int[] block = mBlocks.get(next >>> mBlockShift);
      final int offset = next & mBlockMask;
      final int data = block[offset + OFFSET_DATA];
      if ((data & mEventMask) != event) {
        break;
      }
      existing.add(data >>> mStateShift);
      list = next;
      next = block[offset + OFFSET_NEXT];
    }
    boolean added = false;
    for (int i = 0; i < toStates.size(); i++) {
      final int to = toStates.get(i);
      if (existing.add(to)) {
        final int data = (to << mStateShift) | event;
        list = prepend(list, data);
        added = true;
      }
    }
    if (added) {
      next = getNext(list);
      if (next != NULL) {
        final int nextEvent = getEvent(next);
        final int key = (from << mStateShift) | nextEvent;
        mStateEventTransitions.put(key, list);
      }
    }
    return added;
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
   * earlier in the resultant list.
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
    int list1 = mStateTransitions[source];
    if (list1 == NULL) {
      return false;
    }
    final TIntHashSet existing = new TIntHashSet();
    boolean result = false;
    list1 = getNext(list1);
    if (list1 != NULL) {
      int[] block1 = mBlocks.get(list1 >>> mBlockShift);
      int offset1 = list1 & mBlockMask;
      int data1 = block1[offset1 + OFFSET_DATA];
      int event = data1 & mEventMask;
      do {
        int list2 = createList(dest, event);
        int next2 = getNext(list2);
        while (next2 != NULL) {
          final int[] block2 = mBlocks.get(next2 >>> mBlockShift);
          final int offset2 = next2 & mBlockMask;
          final int data2 = block2[offset2 + OFFSET_DATA];
          if ((data2 & mEventMask) != event) {
            break;
          }
          existing.add(data2 >>> mStateShift);
          list2 = next2;
          next2 = block2[offset2 + OFFSET_NEXT];
        }
        boolean added = false;
        do {
          final int state1 = data1 >>> mStateShift;
          if (existing.add(state1)) {
            list2 = prepend(list2, data1);
            added = true;
            if (reverse != null) {
              reverse.addTransition(state1, event, dest);
            }
          }
          list1 = block1[offset1 + OFFSET_NEXT];
          if (list1 == NULL) {
            break;
          }
          block1 = mBlocks.get(list1 >>> mBlockShift);
          offset1 = list1 & mBlockMask;
          data1 = block1[offset1 + OFFSET_DATA];
        } while ((data1 & mEventMask) == event);
        event = data1 & mEventMask;
        existing.clear();
        if (added) {
          if (next2 != NULL) {
            final int nextEvent = getEvent(next2);
            final int nextCode = (dest << mStateShift) | nextEvent;
            mStateEventTransitions.put(nextCode, list2);
          }
          result = true;
        }
      } while (list1 != NULL);
    }
    return result;
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
    if (oldID == newID) {
      return;
    } else if (newID < 0 || newID >= mNumEvents) {
      throw new IllegalArgumentException
        ("New event ID " + newID + " out of range in " +
         ProxyTools.getShortClassName(this) +
         " (only configured for " + mNumEvents + " events)!");
    }
    final boolean tau = (newID == EventEncoding.TAU);
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
        if (tau && state == to) {
          // nothing --- suppress tau selfloops ...
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
    final TransitionIterator iter = createReadOnlyIterator();
    for (int state = 0; state < getNumberOfStates(); state++) {
      iter.reset(state, event);
      if (iter.advance()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Helps to clean up tau selfloops. This method removes all selfloops
   * associated with {@link EventEncoding#TAU} and tests whether this results in
   * the event being redundant.
   * @return <CODE>true</CODE> if all transitions with the tau event
   *         were selfloops and have been removed, or if no tau transitions
   *         have been found; <CODE>false</CODE> otherwise.
   */
  public boolean removeTauSelfloops()
  {
    final int tau = EventEncoding.TAU;
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
    final TransitionIterator inner = createModifyingIterator(event);
    return new AllTransitionsIterator(inner, event);
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
   * @param  transitions  List of transitions to populate the new buffer.
   *                      The list will be reordered to match the order
   *                      chosen by the buffer.
   */
  public void setUpTransitions(final List<TransitionProxy> transitions,
                               final EventEncoding eventEnc,
                               final StateEncoding stateEnc)
  {
    final Comparator<TransitionProxy> comparator =
      new TransitionComparator(eventEnc, stateEnc);
    Collections.sort(transitions, comparator);
    final int tau = EventEncoding.TAU;
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
        if (e == tau && fromState == toState) {
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
    final List<EventProxy> extra = eventEnc.getExtraSelfloops();
    if (extra != null) {
      final int numStates = getNumberOfStates();
      for (final EventProxy event : extra) {
        if (event.getKind() != EventKind.PROPOSITION) {
          final int e = eventEnc.getEventCode(event);
          for (int s = 0; s < numStates; s++) {
            addTransition(s, e, s);
          }
        }
      }
    }
  }

  /**
   * Gets the from-state for the given transition. This method is used by
   * {@link #setUpTransitions(List,EventEncoding,StateEncoding)
   * setUpTransitions()} to interpret transitions. It is overridden by
   * subclasses to handle forward and backward transition buffers uniformly.
   */
  public abstract StateProxy getFromState(TransitionProxy trans);

  /**
   * Gets the to-state for the given transition. This method is used by
   * {@link #setUpTransitions(List,EventEncoding,StateEncoding)
   * setUpTransitions()} to interpret transitions. It is overridden by
   * subclasses to handle forward and backward transition buffers uniformly.
   */
  public abstract StateProxy getToState(TransitionProxy trans);


  //#########################################################################
  //# Automata Conversion Methods
  /**
   * Initialises this transition buffer with transitions from another
   * transition list buffer. This can be used to duplicate transition buffers,
   * or to convert from forward to backward indexing and vice versa.
   * This method replaces all transitions in the buffer by given transitions.
   * Transitions are added in a fixed order that is determined from the
   * given state and event encoding.
   * The method assumes that this transition buffer is empty when called,
   * if this is not the case, transitions will be overwritten without
   * releasing all memory. To correctly replace transitions in a used buffer,
   * call {@link #clear()} first.
   */
  public void setUpTransitions(final TransitionListBuffer other)
  {
    final TransitionIterator iter =
      other.createAllTransitionsReadOnlyIterator();
    if (getClass() == other.getClass()) {
      // If the source buffer is of the same type (predecessors/successors),
      // just copy the transitions in the same order.
      int from0 = -1;
      int event0 = -1;
      int list = NULL;
      while (iter.advance()) {
        final int from = getOtherIteratorFromState(iter);
        final int event = iter.getCurrentEvent();
        final int to = getOtherIteratorToState(iter);
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
      final long[] transitions = new long[numTrans];
      final int eventShift = AutomatonTools.log2(mNumStates);
      final int fromShift = eventShift + AutomatonTools.log2(mNumEvents);
      final int toMask = (1 << eventShift) - 1;
      int i = 0;
      while (iter.advance()) {
        final long from = getOtherIteratorFromState(iter);
        final long event = iter.getCurrentEvent();
        final long to = getOtherIteratorToState(iter);
        transitions[i++] = (from << fromShift) | (event << eventShift) | to;
      }
      Arrays.sort(transitions);
      int from0 = -1;
      int event0 = -1;
      int list = NULL;
      for (final long trans : transitions) {
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
   *          The partitioning to be imposed.
   *          Each array in the list defines the state codes comprising an
   *          equivalence class to be merged into a single state. The index
   *          position in the list identifies the state code given to
   *          the new merged state.
   * @see ListBufferTransitionRelation#merge(List) ListBufferTransitionRelation.merge()
   */
  public void merge(final List<int[]> partition, final int extraStates)
  {
    mStateEventTransitions.clear();
    final int[] recoding = new int[mStateTransitions.length];
    int code = 0;
    int size = 0;
    for (final int[] clazz : partition) {
      for (final int state : clazz) {
        recoding[state] = code;
      }
      code++;
      if (clazz.length > size) {
        size = clazz.length;
      }
    }
    final int numClasses = code + extraStates;
    final int[] newStateTransitions = new int[numClasses];
    final int eventShift = AutomatonTools.log2(mNumStates);
    final int stateMask = (1 << eventShift) - 1;
    final int tau = EventEncoding.TAU;
    final TIntHashSet transitions = new TIntHashSet();
    code = 0;
    for (final int[] clazz : partition) {
      int list;
      for (final int state : clazz) {
        list = mStateTransitions[state];
        int current = getNext(list);
        while (current != NULL) {
          final int[] block = mBlocks.get(current >>> mBlockShift);
          final int offset = current & mBlockMask;
          final int data = block[offset + OFFSET_DATA];
          final int event = data & mEventMask;
          final int target = recoding[data >>> mStateShift];
          if (event != tau || code != target) { // suppress tau-selfloops
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
      code++;
    }
    mStateTransitions = newStateTransitions;
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
          } else if (event < prevEvent) {
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
    if (event < 0 || event >= mNumEvents) {
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

  private int prepend(final int list, final int data)
  {
    final int tail = getNext(list);
    final int pair = allocatePair();
    setDataAndNext(pair, data, tail);
    setNext(list, pair);
    return pair;
  }

  public int getLength(final int list)
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
  //# Inner Class ReadOnlyIterator
  private class ReadOnlyIterator implements TransitionIterator
  {

    //#######################################################################
    //# Constructor
    private ReadOnlyIterator()
    {
      mCurrent = NULL;
      mFirstEvent = 0;
      mLastEvent = mNumEvents - 1;
      mFromState = -1;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.tr.TransitionIterator
    public void reset()
    {
      if (mFromState >= 0) {
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
      }
    }

    public void resetEvent(final int event)
    {
      if (event < 0) {
        mFirstEvent = 0;
        mLastEvent = mNumEvents - 1;
      } else {
        mFirstEvent = mLastEvent = event;
      }
      reset();
    }

    public void resetEvents(final int first, final int last)
    {
      mFirstEvent = first < 0 ? 0 : first;
      mLastEvent = last >= mNumEvents ? mNumEvents - 1 : last;
      reset();
    }

    public void resetState(final int state)
    {
      mFromState = state;
      reset();
    }

    public void reset(final int state, final int event)
    {
      mFromState = state;
      resetEvent(event);
    }

    public void resume(final int state)
    {
      resetState(state);
    }

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

    public int getCurrentSourceState()
    {
      return getIteratorSourceState(this);
    }

    public int getCurrentFromState()
    {
      return mFromState;
    }

    public int getCurrentEvent()
    {
      assert mCurrent != NULL;
      return mCurrentData & mEventMask;
    }

    public int getCurrentTargetState()
    {
      return getIteratorTargetState(this);
    }

    public int getCurrentToState()
    {
      assert mCurrent != NULL;
      return mCurrentData >>> mStateShift;
    }

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
    public boolean advance()
    {
      mPrevious = getCurrent();
      return super.advance();
    }

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
    public void reset()
    {
      mCurrentFromState = -1;
    }

    public void resetEvent(final int event)
    {
      mInnerIterator.reset(-1, event);
      reset();
    }

    public void resetEvents(final int first, final int last)
    {
      mInnerIterator.resetState(-1);
      mInnerIterator.resetEvents(first, last);
      reset();
    }

    public void resetState(final int from)
    {
      throw new UnsupportedOperationException
        ("All-transitions iterator cannot be reset to iterate " +
         "over only a part of the states!");
    }

    public void reset(final int from, final int event)
    {
      resetState(from);
    }

    public void resume(final int state)
    {
      resetState(state);
    }

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

    public int getCurrentSourceState()
    {
      return getIteratorSourceState(this);
    }

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

    public int getCurrentEvent()
    {
      return mInnerIterator.getCurrentEvent();
    }

    public int getCurrentTargetState()
    {
      return getIteratorTargetState(this);
    }

    public int getCurrentToState()
    {
      return mInnerIterator.getCurrentToState();
    }

    public void remove()
    {
      mInnerIterator.remove();
    }

    //#########################################################################
    //# Data Members
    private final TransitionIterator mInnerIterator;
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
  //# Inner Class IdentityHashingStrategy
  private static class IdentityHashingStrategy
    implements TIntHashingStrategy
  {

    //#######################################################################
    //# Interface gnu.trove.TIntHashingStrategy
    public int computeHashCode(final int key)
    {
      return key;
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Data Members
  private final int mBlockShift;
  private final int mBlockMask;
  private final int mBlockSize;

  private final int mNumStates;
  private final int mNumEvents;
  private final int mStateShift;
  private final int mEventMask;
  private final List<int[]> mBlocks;
  private int[] mStateTransitions;
  private final TIntIntHashMap mStateEventTransitions;

  private int mRecycleStart;
  private int mNextFreeIndex;


  //#########################################################################
  //# Class Constants
  public static final TIntHashingStrategy HASH_STRATEGY =
    new IdentityHashingStrategy();

  public static final int NULL = 0;

  private static final int OFFSET_NEXT = 0;
  private static final int OFFSET_DATA = 1;
  private static final int NODE_SIZE = 2;

  private static final int MIN_BLOCK_SIZE = 64;
  private static final int MAX_BLOCK_SIZE = 2048;

}
