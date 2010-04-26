//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   TransitionListBuffer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TObjectIntHashMap;

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
  public TransitionListBuffer(final int numEvents, final int numStates)
    throws OverflowException
  {
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
    final int[] block = new int[BLOCK_SIZE];
    mBlocks.add(block);
    mRecycleStart = NULL;
    mNextFreeIndex = NODE_SIZE;
  }


  //#########################################################################
  //# Simple Access
  public int getNumberOfEvents()
  {
    return mNumEvents;
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
    final int[] block = new int[BLOCK_SIZE];
    mBlocks.add(block);
    Arrays.fill(mStateTransitions, NULL);
    mStateEventTransitions.clear();
    mRecycleStart = NULL;
    mNextFreeIndex = NODE_SIZE;
  }

  /**
   * Gets the total number of transitions currently stored in this buffer.
   * As the number of transitions is not stored, this method is of linear
   * complexity.
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
   * Adds a transition to this buffer. The new transition is inserted in
   * front of any other transitions with the same from-state and event.
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
    final int fromShift = from << mStateShift;
    final int fromCode = fromShift | event;
    final int toCode = (to << mStateShift) | event;
    int list = mStateEventTransitions.get(fromCode);
    if (list != NULL) {
      if (contains(list, toCode)) {
        return false;
      } else {
        prepend(list, toCode);
        return true;
      }
    } else {
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
      list = prepend(list, toCode);
      final int next = getNext(list);
      if (next != NULL) {
        final int nextEvent = getEvent(next);
        final int code = fromShift | nextEvent;
        mStateEventTransitions.put(code, list);
      }
      return true;
    }
  }

  /**
   * Removes a transition from this buffer.
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
      final int[] block = mBlocks.get(next >>> BLOCK_SHIFT);
      final int offset = next & BLOCK_MASK;
      final int data = block[offset + OFFSET_DATA];
      if (data == toCode) {
        final int victim = next;
        next = block[offset + OFFSET_NEXT];
        setNext(list, next);
        block[offset + OFFSET_NEXT] = mRecycleStart;
        mRecycleStart = victim;
        if (next == NULL) {
          if (first) {
            mStateEventTransitions.remove(fromCode);
            if (mStateTransitions[from] == list) {
              mStateTransitions[from] = NULL;
              dispose(list);
            }
          }
        } else {
          final int nextEvent = getEvent(next);
          if (nextEvent == event) {
            if (first) {
              mStateEventTransitions.put(fromCode, next);
            }
          } else {
            if (first) {
              mStateEventTransitions.remove(fromCode);
            }
            final int nextCode = (from << mStateShift) | nextEvent;
            mStateEventTransitions.put(nextCode, list);
          }
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
   * @return <CODE>true</CODE> if at least one transition was copied;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean copyTransitions(final int source, final int dest)
  {
    if (source == dest) {
      return false;
    }
    int list2 = mStateTransitions[dest];
    if (list2 == NULL) {
      return false;
    }
    list2 = getNext(list2);
    final int fromShift1 = source << mStateShift;
    int list1 = mStateTransitions[source];
    list1 = getNext(list1);
    int end1 = list1;
    final int tau = EventEncoding.TAU;
    int list = NULL;
    final TIntHashSet successors = new TIntHashSet();
    int e0 = -1;
    int data1 = 0;
    int data2 = 0;
    boolean copied = false;
    while (list1 != NULL || list2 != NULL) {
      final int e1;
      if (list1 == NULL) {
        e1 = Integer.MAX_VALUE;
      } else {
        data1 = getData(list1);
        e1 = data1 & mEventMask;
      }
      final int e2;
      if (list2 == NULL) {
        e2 = Integer.MAX_VALUE;
      } else {
        data2 = getData(list2);
        e2 = data2 & mEventMask;
      }
      final int e;
      final int data;
      if (e1 <= e2) {
        e = e1;
        data = data1;
        end1 = list1;
        list1 = getNext(list1);
      } else {
        e = e2;
        data = data2;
        list2 = getNext(list2);
        copied = true;
      }
      final int to = data >> mStateShift;
      if (e == tau && to == dest) {
        continue;
      } else if (e != e0) {
        successors.clear();
        successors.add(to);
        e0 = e;
      } else if (!successors.add(to)) {
        continue;
      }
      if (list == NULL) {
        list = createList();
      }
      if (successors.size() == 1) {
        mStateEventTransitions.put(fromShift1 | e, list);
      }
      list = prepend(list, data);
    }
    setNext(end1, mRecycleStart);
    mRecycleStart = mStateTransitions[source];
    mStateTransitions[source] = list;
    return copied;
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
        final int[] block = mBlocks.get(current >>> BLOCK_SHIFT);
        final int offset = current & BLOCK_MASK;
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
   * @param  old     The ID of the old event to be replaced.
   * @param  new     The ID of the new event replacing the old event.
   */
  public void replaceEvent(final int oldID, final int newID)
  {
    if (newID >= mNumEvents) {
      throw new IllegalArgumentException
        ("New event ID " + newID + " out of range in " +
         ProxyTools.getShortClassName(this) +
         " (only configured for " + mNumEvents + " events)!");
    }
    final boolean tau = (newID == EventEncoding.TAU);
    final TIntHashSet successors = new TIntHashSet();
    final TransitionIterator iter1 = createReadOnlyIterator();
    final TransitionIterator iter2 = createModifyingIterator();
    for (int state = 0; state < getNumberOfStates(); state++) {
      final int oldCode = (state << mStateShift) | oldID;
      int list = mStateEventTransitions.get(oldCode);
      int next = getNext(list);
      while (next != NULL) {
        final int[] block = mBlocks.get(next >>> BLOCK_SHIFT);
        final int offset = next & BLOCK_MASK;
        final int data = block[offset + OFFSET_DATA];
        final int e = data & mEventMask;
        if (e != newID) {
          break;
        }
        successors.add(data >>> mStateShift);
        list = next;
        next = block[offset + OFFSET_NEXT];
      }
      final int stateShifted = state << mStateShift;
      iter1.reset(state, newID);
      while (iter1.advance()) {
        final int succ = iter1.getCurrentToState();
        if (tau && state == succ) {
          // nothing --- suppress tau selfloops ...
        } else if (successors.add(succ)) {
          final int code = stateShifted | oldID;
          list = prepend(list, code);
        }
      }
      iter2.reset(state, newID);
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
   * associated with the given event and tests whether this results in
   * the event being redundant. Tau events are recognised by their standard
   * code {@link EventEncoding#TAU}.
   * @return <CODE>true</CODE> if all transitions with the tau event
   *         were selfloops and have been removed, <CODE>false</CODE>
   *         otherwise.
   */
  public boolean removeTauSelfloops()
  {
    final int tau = EventEncoding.TAU;
    boolean removable = true;
    final TransitionIterator iter = createReadOnlyIterator();
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
   * {@link TransitionIterator#reset(int)} or
   * {@link TransitionIterator#reset(int, int)} before it can be used.
   * Being a read-only iterator, it does not implement the
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
    iter.reset(state);
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
   * {@link TransitionIterator#reset(int)} or
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
    iter.reset(state);
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
   * this buffer after calling {@link TransitionIterator#advance()}.
   * It does not implement the methods {@link TransitionIterator#reset(int)}
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
   * Creates a read/write iterator over all transitions in this buffer.
   * The iterator returned is set up to return the first transition in
   * this buffer after calling {@link TransitionIterator#advance()}.
   * It does not implement the methods {@link TransitionIterator#reset(int)}
   * or {@link TransitionIterator#reset(int,int)}.
   */
  public TransitionIterator createAllTransitionsModifyingIterator()
  {
    final TransitionIterator inner = createModifyingIterator();
    return new AllTransitionsIterator(inner);
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
   * {@link #setUpTransitions(List,TObjectIntHashMap,TObjectIntHashMap)
   * setUpTransitions()} to interpret transitions. It is overridden by
   * subclasses to handle forward and backward transition buffers uniformly.
   */
  public abstract StateProxy getFromState(TransitionProxy trans);

  /**
   * Gets the to-state for the given transition. This method is used by
   * {@link #setUpTransitions(List,TObjectIntHashMap,TObjectIntHashMap)
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
   * @param  transitions  List of transitions to populate the new buffer.
   *                      The list will be reordered to match the order
   *                      chosen by the buffer.
   */
  public void setUpTransitions(final TransitionListBuffer other)
  {
    final int numTrans = other.getNumberOfTransitions();
    final long[] transitions = new long[numTrans];
    final int eventShift = AutomatonTools.log2(mNumStates);
    final int fromShift = eventShift + AutomatonTools.log2(mNumEvents);
    final int toMask = (1 << eventShift) - 1;
    final TransitionIterator iter =
      other.createAllTransitionsReadOnlyIterator();
    int i = 0;
    while (iter.advance()) {
      final int from = getOtherIteratorFromState(iter);
      final int event = iter.getCurrentEvent();
      final int to = getOtherIteratorToState(iter);
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


  public void merge(final List<int[]> partition)
  {
    final int tau = EventEncoding.TAU;
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
    final int[] newStateTransitions = new int[code];
    mStateEventTransitions.clear();
    final int[] iter = new int[size];
    final TIntHashSet successors = new TIntHashSet(partition.size());
    int from = 0;
    int list = NULL;
    for (final int[] clazz : partition) {
      size = clazz.length;
      for (int i = 0; i < size; i++) {
        final int oldState = clazz[i];
        final int oldList = mStateTransitions[oldState];
        iter[i] = getNext(oldList);
      }
      final int fromShift = from << mStateShift;
      for (int e = 0; e < mNumEvents; e++) {
        successors.clear();
        for (int i = 0; i < size; i++) {
          int current = iter[i];
          while (current != NULL) {
            final int[] block = mBlocks.get(current >>> BLOCK_SHIFT);
            final int offset = current & BLOCK_MASK;
            final int data = block[offset + OFFSET_DATA];
            if ((data & mEventMask) != e) {
              break;
            }
            final int to = recoding[data >>> mStateShift];
            if (e == tau && from == to) {
              // Nothing --- suppress tau selfloop.
            } else if (successors.add(to)) {
              if (list == NULL) {
                newStateTransitions[from] = list = createList();
              }
              if (successors.size() == 1) {
                final int fromCode = fromShift | e;
                mStateEventTransitions.put(fromCode, list);
              }
              list = prepend(list, (to << mStateShift) | e);
            }
            final int next = block[offset + OFFSET_NEXT];
            if (next == NULL) {
              block[offset + OFFSET_NEXT] = mRecycleStart;
              final int oldState = clazz[i];
              mRecycleStart = mStateTransitions[oldState];
            }
            current = next;
          }
          iter[i] = current;
        }
      }
      from++;
    }
    mStateTransitions = newStateTransitions;
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

  private boolean contains(final int list, final int data)
  {
    final int event = data & mEventMask;
    int next = getNext(list);
    while (next != NULL) {
      final int[] block = mBlocks.get(next >>> BLOCK_SHIFT);
      final int offset = next & BLOCK_MASK;
      final int found = block[offset + OFFSET_DATA];
      if (found == data) {
        return true;
      } else if ((found & mEventMask) != event) {
        return false;
      }
      next = block[offset + OFFSET_NEXT];
    }
    return false;
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

  private int seek(int list, final int event)
  {
    int next = getNext(list);
    while (next != NULL) {
      final int[] block = mBlocks.get(next >>> BLOCK_SHIFT);
      final int offset = next & BLOCK_MASK;
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
  private int getNumberOfStates()
  {
    return mStateTransitions.length;
  }

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
    final int[] block = mBlocks.get(list >>> BLOCK_SHIFT);
    return block[(list & BLOCK_MASK) + OFFSET_DATA];
  }

  private int getNext(final int list)
  {
    final int[] block = mBlocks.get(list >>> BLOCK_SHIFT);
    return block[(list & BLOCK_MASK) + OFFSET_NEXT];
  }

  private void setNext(final int list, final int next)
  {
    final int[] block = mBlocks.get(list >>> BLOCK_SHIFT);
    block[(list & BLOCK_MASK) + OFFSET_NEXT] = next;
  }

  private void setDataAndNext(final int list, final int data, final int next)
  {
    final int[] block = mBlocks.get(list >>> BLOCK_SHIFT);
    final int offset = list & BLOCK_MASK;
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
      if ((mNextFreeIndex & BLOCK_MASK) >= BLOCK_SIZE) {
        final int[] block = new int[BLOCK_SIZE];
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
      mState = mEvent = -1;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.op.TransitionIterator
    public void reset()
    {
      if (mState < 0) {
        throw new IllegalStateException("From-state not defined for reset!");
      } else if (mEvent < 0) {
        reset(mState);
      } else {
        reset(mState, mEvent);
      }
    }

    public void reset(final int state)
    {
      resetRaw(state, -1, mStateTransitions[state]);
    }

    public void reset(final int state, final int event)
    {
      final int code = (state << mStateShift) | event;
      final int list = mStateEventTransitions.get(code);
      resetRaw(state, event, list);
    }

    public boolean advance()
    {
      mCurrent = getNext(mCurrent);
      if (mCurrent == NULL) {
        return false;
      } else if (mEvent >= 0 && getCurrentEvent() != mEvent) {
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
      if (mCurrent != NULL) {
        return mState;
      } else {
        throw new NoSuchElementException
          ("Reading past end of list in TransitionListBuffer!");
      }
    }

    public int getCurrentEvent()
    {
      return getCurrentData() & mEventMask;
    }

    public int getCurrentTargetState()
    {
      return getIteratorTargetState(this);
    }

    public int getCurrentToState()
    {
      return getCurrentData() >>> mStateShift;
    }

    public void remove()
    {
      throw new UnsupportedOperationException
        ("ReadOnlyIterator does not support removal of elements!");
    }

    //#######################################################################
    //# Auxiliary Methods
    void resetRaw(final int state, final int event, final int list)
    {
      mCurrent = list;
      mState = state;
      mEvent = event;
    }

    int getCurrent()
    {
      return mCurrent;
    }

    void setCurrent(final int current)
    {
      mCurrent = current;
    }

    int getCurrentData()
    {
      if (mCurrent != NULL) {
        return getData(mCurrent);
      } else {
        throw new NoSuchElementException
          ("Reading past end of list in TransitionListBuffer!");
      }
    }

    int getState()
    {
      return mState;
    }

    int getEvent()
    {
      return mEvent;
    }

    //#######################################################################
    //# Data Members
    private int mState;
    private int mCurrent;
    private int mEvent;

  }


  //#########################################################################
  //# Inner Class ModifyingIterator
  private class ModifyingIterator extends ReadOnlyIterator
  {

    //#######################################################################
    //# Constructor
    private ModifyingIterator()
    {
      mStart = mPrevious = NULL;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.op.TransitionIterator
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
      final int[] block = mBlocks.get(current >>> BLOCK_SHIFT);
      final int offset = current & BLOCK_MASK;
      final int next = block[offset + OFFSET_NEXT];
      setNext(mPrevious, next);
      block[offset + OFFSET_NEXT] = mRecycleStart;
      mRecycleStart = current;
      final int state = getState();
      final int fromCode = (state << mStateShift) | getEvent();
      if (next == NULL) {
        if (mPrevious == mStart) {
          mStateEventTransitions.remove(fromCode);
          if (mStateTransitions[state] == mPrevious) {
            mStateTransitions[state] = NULL;
            dispose(mPrevious);
          }
        }
      } else {
        final int nextEvent = TransitionListBuffer.this.getEvent(next);
        if (nextEvent == getEvent()) {
          if (mPrevious == mStart) {
            mStateEventTransitions.put(fromCode, next);
          }
        } else {
          if (mPrevious == mStart) {
            mStateEventTransitions.remove(fromCode);
          }
          final int nextCode = (state << mStateShift) | nextEvent;
          mStateEventTransitions.put(nextCode, mPrevious);
        }
      }
      setCurrent(mPrevious);
      mPrevious = NULL;
    }

    //#######################################################################
    //# Auxiliary Methods
    void resetRaw(final int state, final int event, final int list)
    {
      super.resetRaw(state, event, list);
      mStart = mPrevious = list;
    }

    //#######################################################################
    //# Data Members
    private int mStart;
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
      mInnerIterator = inner;
      mCurrentFromState = -1;
    }

    //#########################################################################
    //# Interface net.sourceforge.waters.op.TransitionIterator
    public void reset()
    {
      mCurrentFromState = -1;
    }

    public void reset(final int from)
    {
      throw new UnsupportedOperationException
        ("All-transitions iterator cannot be reset to iterate " +
         "over only a part of the transitions!");
    }

    public void reset(final int from, final int event)
    {
      reset(from);
    }

    public boolean advance()
    {
      if (mInnerIterator.advance()) {
        return true;
      } else {
        do {
          mCurrentFromState++;
          if (mCurrentFromState >= mStateTransitions.length) {
            return false;
          }
        } while (mStateTransitions[mCurrentFromState] == NULL);
        mInnerIterator.reset(mCurrentFromState);
        return mInnerIterator.advance();
      }
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
  //# Data Members
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
  public static final int NULL = 0;

  private static final int OFFSET_NEXT = 0;
  private static final int OFFSET_DATA = 1;
  private static final int NODE_SIZE = 2;

  private static final int BLOCK_SHIFT = 10;
  private static final int BLOCK_SIZE = 1 << BLOCK_SHIFT;
  private static final int BLOCK_MASK = BLOCK_SIZE - 1;

}
