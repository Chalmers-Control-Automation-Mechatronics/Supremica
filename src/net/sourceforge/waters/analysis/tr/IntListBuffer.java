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

import gnu.trove.TIntCollection;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;


/**
 * <P>A memory efficient container to store several linked lists of
 * integers.</P>
 *
 * <P>An <I>integer list buffer</I> maintains a collection of lists of integers.
 * Each list is identified by its <I>index</I>, an integer that is assigned
 * by the list buffer upon creation of each list and remains a unique list
 * identifier throughout the list's lifetime.</P>
 *
 * <P>Lists are stored in large blocks of integer arrays. Each list node
 * occupies two subsequent array elements for the data and the successor
 * node. Each list index points to a dummy head node, whose next entry
 * references the first actual list node, and which contains the last
 * node of the list instead of data. When lists are deleted, unused nodes are
 * not cleared from memory but retained for possible use in other lists.</P>
 *
 * @author Robi Malik
 */

public class IntListBuffer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new empty integer list buffer.
   */
  public IntListBuffer()
  {
    mBlocks = new ArrayList<int[]>();
    final int[] block = new int[BLOCK_SIZE];
    mBlocks.add(block);
    mRecycleStart = NULL;
    mNextFreeIndex = NODE_SIZE;
  }


  //#########################################################################
  //# Access Methods
  /**
   * Adds the given data to the end of the specified list if it is not already
   * contained. This method does a full list search to check for containment
   * and therefore is of linear complexity.
   * @param  list   The unique list number that identifies the list to be
   *                modified in this buffer.
   * @param  data   The integer data to be stored as the new last element of
   *                the list.
   * @return <CODE>true</CODE> if the data was not contained in the list and
   *         added, <CODE>false</CODE> otherwise.
   */
  public boolean add(final int list, final int data)
  {
    if (contains(list, data)) {
      return false;
    } else {
      append(list, data);
      return true;
    }
  }

  /**
   * Adds the given data to the end of the specified list.
   * @param  list   The unique list number that identifies the list to be modified
   *                in this buffer.
   * @param  data   The integer data to be stored as the new last element of
   *                the list.
   */
  public void append(final int list, final int data)
  {
    final int pair = allocatePair();
    setDataAndNext(pair, data, NULL);
    final int tail = getData(list);
    if (tail == NULL) {
      setDataAndNext(list, pair, pair);
    } else {
      setNext(tail, pair);
      setData(list, pair);
    }
  }

  /**
   * Creates a new list from the data of two given lists.
   * This method merges the two lists destructively, possibly
   * destroying one of them. If one of the two lists is empty or {@link #NULL},
   * then the other is returned. If both lists are nonempty, the first is
   * modified by appending the second, and the second is destroyed.
   * @param  list1   The unique list number that identifies the
   *                 first list to be catenated.
   * @param  list2   The unique list number that identifies the
   *                 second list to be catenated.
   * @return A unique list number that identifies a new list
   *         containing the elements of list1 followed by list2.
   */
  public int catenateDestructively(final int list1, final int list2)
  {
    final int tail1 = getData(list1);
    if (tail1 == NULL) {
      recyclePair(list1);
      return list2;
    }
    final int head2 = getNext(list2);
    if (head2 == NULL) {
      recyclePair(list2);
      return list1;
    }
    setNext(tail1, head2);
    final int tail2 = getData(list2);
    setData(list1, tail2);
    recyclePair(list2);
    return list1;
  }

  /**
   * Clears this buffer.
   * This method removes all information stored in the buffer.
   * All list identifiers become invalid and can be allocated again.
   */
  public void clear()
  {
    mRecycleStart = NULL;
    mNextFreeIndex = NODE_SIZE;
  }

  /**
   * Tests whether the given list contains the given data element
   * This method does a full list search and is of linear complexity.
   * @param  list   The unique list number that identifies the list to be
   *                examined in this buffer.
   * @param  data   The integer data to be searched for in the list.
   * @return <CODE>true</CODE> if the data is contained in the list,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean contains(final int list, final int data)
  {
    int next = getNext(list);
    while (next != NULL) {
      final int[] block = mBlocks.get(next >> BLOCK_SHIFT);
      final int offset = next & BLOCK_MASK;
      if (block[offset + OFFSET_DATA] == data) {
        return true;
      }
      next = block[offset + OFFSET_NEXT];
    }
    return false;
  }

  /**
   * Duplicates a list in this buffer.
   * This method creates a new list containing exactly the same data as
   * the given list. All nodes of the new list are allocated freshly,
   * so the original and the copy have no structure in common.
   * @param  list    The unique list number that identifies the
   *                 list to be duplicated.
   * @return A unique list number that identifies a new list
   *         containing exactly the same elements as the given list.
   */
  public int copy(final int list)
  {
    return copy(list, this);
  }

  /**
   * Duplicates a list from another integer list buffer.
   * This method creates a new list in this buffer containing exactly the
   * same data as the given list.
   * @param  list    The unique list number that identifies the
   *                 list to be duplicated in its buffer
   * @param  other   The list buffer containing the list to be copied.
   * @return A unique list number that identifies a new list
   *         containing exactly the same elements as the given list.
   */
  public int copy(final int list, final IntListBuffer other)
  {
    final int result = allocatePair();
    int prev = result;
    int prevdata = NULL;
    int next = other.getNext(list);
    final List<int[]> otherBlocks = other.mBlocks;
    while (next != NULL) {
      final int[] block = otherBlocks.get(next >> BLOCK_SHIFT);
      final int offset = next & BLOCK_MASK;
      final int data = block[offset + OFFSET_DATA];
      next = block[offset + OFFSET_NEXT];
      final int current = allocatePair();
      setDataAndNext(prev, prevdata, current);
      prev = current;
      prevdata = data;
    }
    setDataAndNext(prev, prevdata, NULL);
    setData(result, prev);
    return result;
  }

  /**
   * Creates a new empty list of integers.
   * @return The unique list number that identifies the new list in this
   *         buffer.
   */
  public int createList()
  {
    final int list = allocatePair();
    setDataAndNext(list, NULL, NULL);
    return list;
  }

  /**
   * Creates a new list and initialises it with data from the given array.
   * @return The unique list number that identifies the new list in this
   *         buffer.
   */
  public int createList(final int[] array)
  {
    if (array.length == 0) {
      return createList();
    } else {
      final int list = allocatePair();
      int prev = list;
      int prevdata = NULL;
      for (final int data : array) {
        final int current = allocatePair();
        setDataAndNext(prev, prevdata, current);
        prev = current;
        prevdata = data;
      }
      setDataAndNext(prev, prevdata, NULL);
      setData(list, prev);
      return list;
    }
  }

  /**
   * Creates a read-only iterator for lists in this buffer.
   * The returned iterator is uninitialised, so the {@link Iterator#reset(int)
   * reset()} needs to be called before it can be used.
   */
  public ReadOnlyIterator createReadOnlyIterator()
  {
    return new ReadOnlyIterator();
  }

  /**
   * Creates a read-only iterator to iterate over the given list in this
   * buffer.
   */
  public ReadOnlyIterator createReadOnlyIterator(final int list)
  {
    return new ReadOnlyIterator(list);
  }

  /**
   * Creates a read/write iterator for lists in this buffer.
   * The returned iterator is uninitialised, so the {@link Iterator#reset(int)
   * reset()} needs to be called before it can be used.
   */
  public ModifyingIterator createModifyingIterator()
  {
    return new ModifyingIterator();
  }

  /**
   * Creates a read/write iterator to iterate over the given list in this
   * buffer.
   */
  public ModifyingIterator createModifyingIterator(final int list)
  {
    return new ModifyingIterator(list);
  }

  /**
   * Releases memory for the given list. This method marks the list as
   * deleted and enqueues its nodes for reuse in future operations. No
   * memory is returned to the operation system.
   * @param  list   The unique list number that identifies the list to be
   *                deleted in this buffer. Accessing the list after this
   *                disposal can produce undefined behaviour.
   */
  public void dispose(final int list)
  {
    if (list != NULL) {
      final int tail = getData(list);
      final int last = tail == NULL ? list : tail;
      setNext(last, mRecycleStart);
      mRecycleStart = list;
    }
  }

  /**
   * Checks whether the given list is empty.
   * @param  list   The unique list number that identifies the list to be
   *                examined in this buffer.
   */
  public boolean isEmpty(final int list)
  {
    return getNext(list) == NULL;
  }

  /**
   * Gets the first data element from the given list.
   * @param  list   The unique list number that identifies the list to be
   *                examined in this buffer.
   * @throws IllegalArgumentException to indicate that the list
   *                is {@link #NULL} or empty.
   */
  public int getFirst(final int list)
  {
    final int head = getNext(list);
    if (head == NULL) {
      throw new IllegalArgumentException
        ("Attempting to get element from NULL or empty list!");
    }
    return getData(head);
  }

  /**
   * Determines whether the given list has more than the specified number
   * of elements. This method iterates over the list to count it, so its
   * complexity is linear.
   * @param  list   The unique list number that identifies the list to be
   *                examined in this buffer.
   * @param  size   Number of elements to be checked in the list.
   * @return <CODE>true</CODE> if the number of elements in the list is
   *         greater than the given <CODE>size</CODE>;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isStrictlyLongerThan(final int list, final int size)
  {
    int count = 0;
    for (int next = getNext(list); next != NULL; next = getNext(next)) {
      if (++count > size) {
        return true;
      }
    }
    return false;
  }

  /**
   * Computes the length of the given list. This method iterates over the
   * list to count it, so its complexity is linear.
   * @param  list   The unique list number that identifies the list to be
   *                examined in this buffer.
   * @return The number of elements in the given list.
   */
  public int getLength(final int list)
  {
    int count = 0;
    for (int next = getNext(list); next != NULL; next = getNext(next)) {
      count++;
    }
    return count;
  }

  /**
   * Adds the given data to the front of the specified list.
   * @param  list   The unique list number that identifies the list to be
   *                modified in this buffer.
   * @param  data   The integer data to be stored as the new first element of
   *                the list.
   */
  public void prepend(final int list, final int data)
  {
    final int tail = getNext(list);
    final int pair = allocatePair();
    setDataAndNext(pair, data, tail);
    setHead(list, pair);
  }

  /**
   * Adds the given data to the front of the specified list, but only if the
   * current first list element is different from the new data.
   * @param  list   The unique list number that identifies the list to be
   *                modified in this buffer.
   * @param  data   The integer data to be stored as the new first element of
   *                the list.
   */
  public void prependUnique(final int list, final int data)
  {
    final int tail = getNext(list);
    if (tail == NULL || getData(tail) != data) {
      final int pair = allocatePair();
      setDataAndNext(pair, data, tail);
      setHead(list, pair);
    }
  }

  /**
   * Removes the given data from the specified list.
   * This method performs a sequential search of the list to find the item
   * to be removed, and therefore is of linear complexity.
   * @param  list   The unique list number that identifies the list to be
   *                modified in this buffer.
   * @param  data   The integer data to be removed from the list.
   */
  public boolean remove(final int list, final int data)
  {
    if (list != NULL) {
      int prev = list;
      int current = getNext(prev);
      while (current != NULL) {
        final int[] block = mBlocks.get(current >> BLOCK_SHIFT);
        final int offset = current & BLOCK_MASK;
        final int next = block[offset + OFFSET_NEXT];
        if (block[offset + OFFSET_DATA] == data) {
          if (next != NULL) {
            setNext(prev, next);
          } else if (prev == list) {
            setDataAndNext(list, NULL, NULL);
          } else {
            setNext(prev, next);
            setData(list, prev);
          }
          block[offset + OFFSET_NEXT] = mRecycleStart;
          mRecycleStart = current;
          return true;
        }
        prev = current;
        current = next;
      }
    }
    return false;
  }

  /**
   * Removes the first element from the specified list.
   * This method destructively changes the given list to remove its first
   * element, and returns that element.
   * @param  list   The unique list number that identifies the list to be
   *                modified in this buffer, which must not be empty.
   * @return The first list element that was removed.
   */
  public int removeFirst(final int list)
  {
    final int head = getNext(list);
    if (head == NULL) {
      throw new IllegalArgumentException
        ("Attempting to remove element from NULL or empty list!");
    }
    final int item = getData(head);
    final int next = getNext(head);
    if (next == NULL) {
      setDataAndNext(list, NULL, NULL);
    } else {
      setNext(list, next);
    }
    setNext(head, mRecycleStart);
    mRecycleStart = head;
    return item;
  }

  /**
   * Constructs an array containing the elements in the given list.
   * @param  list   The unique list number that identifies the list to be
   *                examined in this buffer.
   */
  public int[] toArray(final int list)
  {
    final int count = getLength(list);
    final int[] result = new int[count];
    int index = 0;
    int next = getNext(list);
    while (next != NULL) {
      final int[] block = mBlocks.get(next >> BLOCK_SHIFT);
      final int offset = next & BLOCK_MASK;
      result[index++] = block[offset + OFFSET_DATA];
      next = block[offset + OFFSET_NEXT];
    }
    return result;
  }

  /**
   * Adds the contents of the given list to {@link TIntCollection},
   * such as {@link TIntArrayList} or {@link TIntHashSet}. This method
   * iterates over the given list and calls the collection's {@link
   * TIntCollection#add(int) add()} method for each element.
   * @param  list        The unique list number that identifies the list to be
   *                     examined in this buffer.
   * @param  collection  The collection to receive the data.
   */
  public void toTIntCollection(final int list, final TIntCollection collection)
  {
    int next = getNext(list);
    while (next != NULL) {
      final int[] block = mBlocks.get(next >> BLOCK_SHIFT);
      final int offset = next & BLOCK_MASK;
      collection.add(block[offset + OFFSET_DATA]);
      next = block[offset + OFFSET_NEXT];
    }
  }


  //#########################################################################
  //# Low Level Access
  public int getHead(final int list)
  {
    return getNext(list);
  }

  public int getTail(final int list)
  {
    return getData(list);
  }

  public int getData(final int node)
  {
    final int[] block = mBlocks.get(node >> BLOCK_SHIFT);
    return block[(node & BLOCK_MASK) + OFFSET_DATA];
  }

  public int getNext(final int node)
  {
    final int[] block = mBlocks.get(node >> BLOCK_SHIFT);
    return block[(node & BLOCK_MASK) + OFFSET_NEXT];
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setData(final int list, final int data)
  {
    final int[] block = mBlocks.get(list >> BLOCK_SHIFT);
    block[(list & BLOCK_MASK) + OFFSET_DATA] = data;
  }

  private void setNext(final int list, final int next)
  {
    final int[] block = mBlocks.get(list >> BLOCK_SHIFT);
    block[(list & BLOCK_MASK) + OFFSET_NEXT] = next;
  }

  private void setHead(final int list, final int head)
  {
    final int[] block = mBlocks.get(list >> BLOCK_SHIFT);
    final int offset = list & BLOCK_MASK;
    if (block[offset + OFFSET_NEXT] == NULL) {
      block[offset + OFFSET_NEXT] = block[offset + OFFSET_DATA] = head;
    } else {
      block[offset + OFFSET_NEXT] = head;
    }
  }

  private void setDataAndNext(final int list, final int data, final int next)
  {
    final int[] block = mBlocks.get(list >> BLOCK_SHIFT);
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
      if ((mNextFreeIndex & BLOCK_MASK) == 0) {
        final int blockno = mNextFreeIndex >>> BLOCK_SHIFT;
        if (blockno >= mBlocks.size()) {
          final int[] block = new int[BLOCK_SIZE];
          mBlocks.add(block);
        }
      }
      final int result = mNextFreeIndex;
      mNextFreeIndex += 2;
      return result;
    }
  }

  public void recyclePair(final int list)
  {
    setNext(list, mRecycleStart);
    mRecycleStart = list;
  }


  //#########################################################################
  //# Debugging
  public String toString(final int list)
  {
    final StringWriter writer = new StringWriter();
    final PrintWriter printer = new PrintWriter(writer);
    dumpList(printer, list);
    return writer.toString();
  }

  public void dumpList(final PrintWriter printer, final int list)
  {
    if (list == NULL) {
      printer.print("NULL");
    } else {
      printer.print('[');
      int next = getNext(list);
      while (next != NULL) {
        final int[] block = mBlocks.get(next >> BLOCK_SHIFT);
        final int offset = next & BLOCK_MASK;
        final int data = block[offset + OFFSET_DATA];
        printer.print(data);
        next = block[offset + OFFSET_NEXT];
        if (next != NULL) {
          printer.print(',');
        }
      }
      printer.print(']');
    }
  }


  //#########################################################################
  //# Inner Interface Iterator
  /**
   * <P>Interface for iterators over lists in an {@link IntListBuffer}.</P>
   *
   * <P>Iterators are needed to process lists by visiting their elements.
   * After an iterator has been obtained by one of the methods
   * {@link IntListBuffer#createReadOnlyIterator() createReadOnlyIterator()}
   * or {@link IntListBuffer#createModifyingIterator()
   * createModifyingIterator()}, the list elements can be retrieved using a
   * combination of the methods {@link #advance()} and
   * {@link #getCurrentData()}.</P>
   *
   * <PRE>
   * IntListBuffer.Iterator iter = {@link IntListBuffer#createReadOnlyIterator() createReadOnlyIterator()};
   * while (iter.{@link #advance()}) {
   *   int data = iter.{@link #getCurrentData()};
   *   // process data ...
   * }</PRE>
   *
   * <P>Iterators can be reset, so the same iterator object can
   * be reused for several iterations. There are read-only iterators
   * ({@link ReadOnlyIterator}) for efficient read access to lists, and
   * read/write iterators ({@link ModifyingIterator}) that support several
   * operations to remove, replace, or move list elements during iteration.</P>
   */
  public interface Iterator extends WatersIntIterator
  {
    /**
     * Creates a copy of this iterator. The cloned iterator becomes an
     * independent new iterator with the same capabilities as this iterator,
     * and starts off in the same state.
     */
    @Override
    public Iterator clone();

    /**
     * Resets iteration to the start of the given list.
     * The next call to {@link #advance()} will set the iterator to the
     * first list element.
     */
    public void reset(int list);

    /**
     * Resets iteration to the position preceding <CODE>prev</CODE> in the
     * given list.
     * @param  list   The unique list number that identifies the list to be
     *                examined by the iterator.
     * @param  prev   The index of the node preceding the next node to be
     *                made accessible when {@link #advance()} is called.
     *                A value of {@link IntListBuffer#NULL NULL} resets the
     *                iterator to the start of the list.
     */
    public void reset(int list, int prev);

    /**
     * Stores the given value in the list at the current position of this
     * iterator.
     */
    public void setCurrentData(int data);

  }


  //#########################################################################
  //# Inner Class ReadOnlyIterator
  public class ReadOnlyIterator implements Iterator
  {
    //#########################################################################
    //# Constructor
    private ReadOnlyIterator()
    {
      mHead = mCurrent = NULL;
    }

    private ReadOnlyIterator(final int list)
    {
      reset(list);
    }

    //#########################################################################
    //# Interface WatersIntIterator
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
      mCurrent = mHead;
    }

    @Override
    public boolean advance()
    {
      mCurrent = getNext(mCurrent);
      return mCurrent != NULL;
    }

    @Override
    public int getCurrentData()
    {
      if (mCurrent != NULL) {
        return getData(mCurrent);
      } else {
        throw new NoSuchElementException
          ("Reading past end of list in IntListBuffer!");
      }
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support removal of elements!");
    }

    //#########################################################################
    //# Interface Iterator
    @Override
    public void reset(final int list)
    {
      if (list != NULL) {
        mHead = mCurrent = list;
      } else {
        throw new IllegalArgumentException("List head cannot be NULL!");
      }
    }

    @Override
    public void reset(final int list, final int prev)
    {
      if (list == NULL || prev == NULL) {
        reset(list);
      } else {
        mCurrent = prev;
      }
    }

    @Override
    public void setCurrentData(final int data)
    {
      if (mCurrent != NULL) {
        setData(mCurrent, data);
      } else {
        throw new NoSuchElementException
          ("Writing past end of list in IntListBuffer!");
      }
    }

    //#########################################################################
    //# Specific Access
    public void reset(final ReadOnlyIterator iter)
    {
      mCurrent = iter.mCurrent;
    }

    //#########################################################################
    //# Data Members
    private int mHead;
    private int mCurrent;

  }


  //#########################################################################
  //# Inner Class ModifyingIterator
  public class ModifyingIterator implements Iterator
  {
    //#########################################################################
    //# Constructor
    private ModifyingIterator()
    {
      mHead = mPrevious = mCurrent = NULL;
    }

    private ModifyingIterator(final int list)
    {
      reset(list);
    }

    //#########################################################################
    //# Interface WatersIntIterator
    @Override
    public ModifyingIterator clone()
    {
      try {
        return (ModifyingIterator) super.clone();
      } catch (final CloneNotSupportedException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    @Override
    public void reset()
    {
      mPrevious = NULL;
      mCurrent = mHead;
    }

    @Override
    public boolean advance()
    {
      mPrevious = mCurrent;
      mCurrent = getNext(mCurrent);
      return mCurrent != NULL;
    }

    @Override
    public int getCurrentData()
    {
      if (mCurrent != NULL) {
        return getData(mCurrent);
      } else {
        throw new NoSuchElementException
          ("Reading past end of list in IntListBuffer!");
      }
    }

    @Override
    public void remove()
    {
      if (mPrevious != NULL) {
        final int[] block = mBlocks.get(mCurrent >> BLOCK_SHIFT);
        final int offset = mCurrent & BLOCK_MASK;
        final int next = block[offset + OFFSET_NEXT];
        if (next != NULL) {
          setNext(mPrevious, next);
        } else if (mPrevious == mHead) {
          setDataAndNext(mHead, NULL, NULL);
        } else {
          setNext(mPrevious, next);
          setData(mHead, mPrevious);
        }
        block[offset + OFFSET_NEXT] = mRecycleStart;
        mRecycleStart = mCurrent;
        mCurrent = mPrevious;
        mPrevious = NULL;
      } else {
        throw new IllegalStateException
          ("Attempting to remove without previous call to advance()!");
      }
    }

    //#########################################################################
    //# Interface Iterator
    @Override
    public void reset(final int list)
    {
      if (list != NULL) {
        mPrevious = NULL;
        mHead = mCurrent = list;
      } else {
        throw new IllegalArgumentException("List head cannot be NULL!");
      }
    }

    @Override
    public void reset(final int list, final int prev)
    {
      if (list == NULL || prev == NULL) {
        reset(list);
      } else {
        mPrevious = NULL;
        mHead = list;
        mCurrent = prev;
      }
    }

    @Override
    public void setCurrentData(final int data)
    {
      if (mCurrent != NULL) {
        setData(mCurrent, data);
      } else {
        throw new NoSuchElementException
          ("Writing past end of list in IntListBuffer!");
      }
    }

    //#########################################################################
    //# Specific Access
    /**
     * Removes the current item from the list being iterated over,
     * and adds it at the start of the specified list.
     * @param  list   The unique list number that identifies the list
     *                receiving the data.
     */
    public void moveToStart(final int list)
    {
      if (mPrevious != NULL) {
        final int[] block = mBlocks.get(mCurrent >> BLOCK_SHIFT);
        final int offset = mCurrent & BLOCK_MASK;
        final int next = block[offset + OFFSET_NEXT];
        if (next != NULL) {
          setNext(mPrevious, next);
        } else if (mPrevious == mHead) {
          setDataAndNext(mHead, NULL, NULL);
        } else {
          setNext(mPrevious, next);
          setData(mHead, mPrevious);
        }
        block[offset + OFFSET_NEXT] = NULL;
        final int head = getNext(list);
        if (head == NULL) {
          setDataAndNext(list, mCurrent, mCurrent);
        } else {
          setNext(list, mCurrent);
          setNext(mCurrent, head);
        }
        mCurrent = mPrevious;
        mPrevious = NULL;
      } else {
        throw new IllegalStateException
          ("Attempting to move without previous call to advance()!");
      }
    }

    /**
     * Removes the current item from the list being iterated over,
     * and adds it at the end of the specified list.
     * @param  list   The unique list number that identifies the list
     *                receiving the data.
     */
    public void moveToEnd(final int list)
    {
      if (mPrevious != NULL) {
        final int[] block = mBlocks.get(mCurrent >> BLOCK_SHIFT);
        final int offset = mCurrent & BLOCK_MASK;
        final int next = block[offset + OFFSET_NEXT];
        if (next != NULL) {
          setNext(mPrevious, next);
        } else if (mPrevious == mHead) {
          setDataAndNext(mHead, NULL, NULL);
        } else {
          setNext(mPrevious, next);
          setData(mHead, mPrevious);
        }
        block[offset + OFFSET_NEXT] = NULL;
        final int tail = getData(list);
        if (tail == NULL) {
          setDataAndNext(list, mCurrent, mCurrent);
        } else {
          setNext(tail, mCurrent);
          setData(list, mCurrent);
        }
        mCurrent = mPrevious;
        mPrevious = NULL;
      } else {
        throw new IllegalStateException
          ("Attempting to move without previous call to advance()!");
      }
    }

    //#########################################################################
    //# Data Members
    private int mHead;
    private int mPrevious;
    private int mCurrent;

  }


  //#########################################################################
  //# Data Members
  private final List<int[]> mBlocks;

  private int mRecycleStart;
  private int mNextFreeIndex;


  //#########################################################################
  //# Class Constants
  /**
   * A list index identifying a <CODE>null</CODE> or undefined list.
   * <CODE>NULL</CODE> is reserved to recognise non-existent lists or the
   * end of a list, but this constant cannot be used to represent any empty
   * list.
   */
  public static final int NULL = 0;

  private static final int OFFSET_NEXT = 0;
  private static final int OFFSET_DATA = 1;
  private static final int NODE_SIZE = 2;

  private static final int BLOCK_SHIFT = 10;
  private static final int BLOCK_SIZE = 1 << BLOCK_SHIFT;
  private static final int BLOCK_MASK = BLOCK_SIZE - 1;

}
