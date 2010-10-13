//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   IntListBuffer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


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
 * node. Each list index points to a dummy head node, which contains no data,
 * but whose next entry references the first actual list node. When lists are
 * deleted, unused nodes are not cleared from memory but retained for possible
 * use in other lists.</P>
 *
 * @author Robi Malik
 */

public class IntListBuffer
{

  //#########################################################################
  //# Constructors
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
   * This method merges the two lists destructively, and there is no
   * guarantee that either input list continues to exists after the call.
   * @param  list1   The unique list number that identifies the
   *                 first list to be catenated.
   * @param  list2   The unique list number that identifies the
   *                 second list to be catenated.
   * @return list    A unique list number that identifying a new list
   *                 containing the elements of list1 followed by list2.
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

  public ReadOnlyIterator createReadOnlyIterator()
  {
    return new ReadOnlyIterator();
  }

  public ReadOnlyIterator createReadOnlyIterator(final int list)
  {
    return new ReadOnlyIterator(list);
  }

  public Iterator createModifyingIterator()
  {
    return new ModifyingIterator();
  }

  public Iterator createModifyingIterator(final int list)
  {
    return new ModifyingIterator(list);
  }

  public void dispose(final int list)
  {
    if (list != NULL) {
      final int tail = getNext(list);
      final int last = tail == NULL ? list : tail;
      setNext(last, mRecycleStart);
      mRecycleStart = list;
    }
  }

  public boolean isEmpty(final int list)
  {
    return getNext(list) == NULL;
  }

  /**
   * Gets the first data element from the given list.
   * @param  list   The unique list number that identifies the list to be
   *                examined in this buffer.
   * @throws  IllegalArgumentException to indicate that the list
   *                 is {@link #NULL} or empty.
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
   * Computes the length of the given list. This method iterates over the
   * list to count it, so its complexity is linear.
   * @param  list   The unique list number that identifies the list to be
   *                examined in this buffer.
   * @return  The number of elements in the given list.
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
   * @param  list   The unique list number that identifies the list to be modified
   *                in this buffer.
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


  //#########################################################################
  //# Debugging
  public void dumpList(final PrintWriter writer, final int list)
  {
    if (list == NULL) {
      writer.print("NULL");
    } else {
      writer.print('[');
      int next = getNext(list);
      while (next != NULL) {
        final int[] block = mBlocks.get(next >> BLOCK_SHIFT);
        final int offset = next & BLOCK_MASK;
        final int data = block[offset + OFFSET_DATA];
        writer.print(data);
        next = block[offset + OFFSET_NEXT];
        if (next != NULL) {
          writer.print(',');
        }
      }
      writer.print(']');
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private int getData(final int list)
  {
    final int[] block = mBlocks.get(list >> BLOCK_SHIFT);
    return block[(list & BLOCK_MASK) + OFFSET_DATA];
  }

  private int getNext(final int list)
  {
    final int[] block = mBlocks.get(list >> BLOCK_SHIFT);
    return block[(list & BLOCK_MASK) + OFFSET_NEXT];
  }

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
        final int[] block = new int[BLOCK_SIZE];
        mBlocks.add(block);
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
  //# Inner Interface Iterator
  public interface Iterator
  {
    public void reset(int list);
    public boolean advance();
    public int getCurrentData();
    public void setCurrentData(int data);
    public void remove();
  }


  //#########################################################################
  //# Inner Class ReadOnlyIterator
  public class ReadOnlyIterator implements Iterator
  {
    //#########################################################################
    //# Constructor
    private ReadOnlyIterator()
    {
      mCurrent = NULL;
    }

    private ReadOnlyIterator(final int list)
    {
      reset(list);
    }

    //#########################################################################
    //# Interface Iterator
    public void reset(final int list)
    {
      if (list != NULL) {
        mCurrent = list;
      } else {
        throw new IllegalArgumentException("List head cannot be NULL!");
      }
    }

    public boolean advance()
    {
      mCurrent = getNext(mCurrent);
      return mCurrent != NULL;
    }

    public int getCurrentData()
    {
      if (mCurrent != NULL) {
        return getData(mCurrent);
      } else {
        throw new NoSuchElementException
          ("Reading past end of list in IntListBuffer!");
      }
    }

    public void setCurrentData(final int data)
    {
      if (mCurrent != NULL) {
        setData(mCurrent, data);
      } else {
        throw new NoSuchElementException
          ("Writing past end of list in IntListBuffer!");
      }
    }

    public void remove()
    {
      throw new UnsupportedOperationException
        ("ReadOnlyIterator does not support removal of elements!");
    }

    //#########################################################################
    //# Specific Access
    public void reset(final ReadOnlyIterator iter)
    {
      mCurrent = iter.mCurrent;
    }

    //#########################################################################
    //# Data Members
    private int mCurrent;

  }


  //#########################################################################
  //# Inner Class ModifyingIterator
  private class ModifyingIterator implements Iterator
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
    //# Data Members
    public void reset(final int list)
    {
      if (list != NULL) {
        mPrevious = NULL;
        mHead = mCurrent = list;
      } else {
        throw new IllegalArgumentException("List head cannot be NULL!");
      }
    }

    public boolean advance()
    {
      mPrevious = mCurrent;
      mCurrent = getNext(mCurrent);
      return mCurrent != NULL;
    }

    public int getCurrentData()
    {
      if (mCurrent != NULL) {
        return getData(mCurrent);
      } else {
        throw new NoSuchElementException
          ("Reading past end of list in IntListBuffer!");
      }
    }

    public void setCurrentData(final int data)
    {
      if (mCurrent != NULL) {
        setData(mCurrent, data);
      } else {
        throw new NoSuchElementException
          ("Writing past end of list in IntListBuffer!");
      }
    }

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
