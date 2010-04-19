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

import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * @author Robi Malik
 */

public abstract class TransitionListBuffer
{

  //#########################################################################
  //# Constructors
  public TransitionListBuffer(final int numEvents, final int numStates)
  {
    mNumEvents = numEvents;
    mStateShift = AutomatonTools.log2(numStates);
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
   * @param  stateMap     State encoding map, assigns to each state used by
   *                      the given transitions an integer to encode it.
   *                      The state code must be within the range of valid
   *                      state codes passed to the constructor.
   * @param  eventMap     Event encoding map, assigns to each event used by
   *                      the given transitions an integer to encode it.
   *                      The event code must be within the range of valid
   *                      event codes passed to the constructor.
   */
  public void setUpTransitions(final List<TransitionProxy> transitions,
                               final TObjectIntHashMap<StateProxy> stateMap,
                               final TObjectIntHashMap<EventProxy> eventMap)
  {
    final Comparator<TransitionProxy> comparator =
      new TransitionComparator(stateMap, eventMap);
    Collections.sort(transitions, comparator);
    int from0 = -1;
    int e0 = -1;
    int toCode0 = -1;
    int list = NULL;
    for (final TransitionProxy trans : transitions) {
      final StateProxy fromState = getFromState(trans);
      final int from = stateMap.get(fromState);
      final EventProxy event = trans.getEvent();
      final int e = eventMap.get(event);
      final StateProxy toState = getToState(trans);
      final int to = stateMap.get(toState);
      final int toCode = (to << mStateShift) | e;
      if (from != from0) {
        mStateTransitions[from] = list = createList();
        final int fromCode = (from << mStateShift) | e;
        mStateEventTransitions.put(fromCode, list);
        from0 = from;
        e0 = e;
      } else if (toCode0 == toCode) {
        continue;
      } else if (e0 != e) {
        final int fromCode = (from << mStateShift) | e;
        mStateEventTransitions.put(fromCode, list);
        e0 = e;
      }
      toCode0 = toCode;
      list = prepend(list, toCode);
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


  public void merge(final List<int[]> partition, final int tau)
  {
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
   * of the destination states so it contains any event-to-state pairs that
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
    int list2 = mStateTransitions[dest];
    if (list2 == NULL) {
      return false;
    }
    list2 = getNext(list2);
    final int fromShift1 = source << mStateShift;
    int list1 = mStateTransitions[source];
    list1 = getNext(list1);
    int end1 = list1;
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
      if (e != e0) {
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
  public boolean removeTransitions(final int from)
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
   * and event in the buffer's defined ordering, no matter what event they use.
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
   * The iterator returned produces all transitions with the given-from-state
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
   * The iterator returned produces all transitions with the given-from-state
   * and event in the buffer's defined ordering, no matter what event they use.
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

    //#########################################################################
    //# Constructor
    private ReadOnlyIterator()
    {
      mCurrent = NULL;
      mState = mEvent = -1;
    }

    //#########################################################################
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

    //#########################################################################
    //# Auxiliary Methods
    void resetRaw(final int state, final int event, final int list)
    {
      if (list == NULL) {
        mCurrent = NULL;
      } else {
        mCurrent = getNext(list);
      }
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

    //#########################################################################
    //# Data Members
    private int mState;
    private int mCurrent;
    private int mEvent;

  }


  //#########################################################################
  //# Inner Class ModifyingIterator
  private class ModifyingIterator extends ReadOnlyIterator
  {

    //#########################################################################
    //# Constructor
    private ModifyingIterator()
    {
      mPrevious = NULL;
      mFirst = true;
    }

    //#########################################################################
    //# Interface net.sourceforge.waters.op.TransitionIterator
    public boolean advance()
    {
      mPrevious = getCurrent();
      mFirst = false;
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
        if (mFirst) {
          mStateEventTransitions.remove(fromCode);
          if (mStateTransitions[state] == mPrevious) {
            mStateTransitions[state] = NULL;
            dispose(mPrevious);
          }
        }
      } else {
        final int nextEvent = TransitionListBuffer.this.getEvent(next);
        if (nextEvent == getEvent()) {
          if (mFirst) {
            mStateEventTransitions.put(fromCode, next);
          }
        } else {
          if (mFirst) {
            mStateEventTransitions.remove(fromCode);
          }
          final int nextCode = (state << mStateShift) | nextEvent;
          mStateEventTransitions.put(nextCode, mPrevious);
        }
      }
      setCurrent(mPrevious);
      mPrevious = NULL;
    }

    //#########################################################################
    //# Auxiliary Methods
    void resetRaw(final int state, final int event, final int list)
    {
      super.resetRaw(state, event, list);
      mFirst = true;
      mPrevious = list;
    }

    //#########################################################################
    //# Data Members
    private boolean mFirst;
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
        mInnerIterator.reset(mStateTransitions[mCurrentFromState]);
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
    private TransitionComparator(final TObjectIntHashMap<StateProxy> stateMap,
                                 final TObjectIntHashMap<EventProxy> eventMap)
    {
      mStateMap = stateMap;
      mEventMap = eventMap;
    }

    //#######################################################################
    //# Interface java.util.Comparator<TransitionProxy>
    public int compare(final TransitionProxy trans1,
                       final TransitionProxy trans2)
    {
      final StateProxy from1 = getFromState(trans1);
      final StateProxy from2 = getFromState(trans2);
      int delta = mStateMap.get(from1) - mStateMap.get(from2);
      if (delta != 0) {
        return delta;
      }
      final EventProxy event1 = trans1.getEvent();
      final EventProxy event2 = trans2.getEvent();
      delta = mEventMap.get(event1) - mEventMap.get(event2);
      if (delta != 0) {
        return delta;
      }
      final StateProxy to1 = getToState(trans1);
      final StateProxy to2 = getToState(trans2);
      return mStateMap.get(to1) - mStateMap.get(to2);
    }

    //#######################################################################
    //# Data Members
    private final TObjectIntHashMap<StateProxy> mStateMap;
    private final TObjectIntHashMap<EventProxy> mEventMap;

  }


  //#########################################################################
  //# Data Members
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
