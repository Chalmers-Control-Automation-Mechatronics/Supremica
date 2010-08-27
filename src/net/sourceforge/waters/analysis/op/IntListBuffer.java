//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   IntListBuffer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

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
   * Creates a new empty list of integers.
   * @return The unique list number that identifies the new list in this
   *         buffer.
   */
  public int createList()
  {
    final int list = allocatePair();
    setNext(list, NULL);
    return list;
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
    setNext(list, pair);
  }

  /**
   * Adds the given data to the front of the specified list, but only if the
   * current first list element is different from the new data.
   * @param  list   The unique list number that identifies the list to be modified
   *                in this buffer.
   * @param  data   The integer data to be stored as the new first element of
   *                the list.
   */
  public void prependUnique(final int list, final int data)
  {
    final int tail = getNext(list);
    if (getData(tail) != data) {
      final int pair = allocatePair();
      setDataAndNext(pair, data, tail);
      setNext(list, pair);
    }
  }

  public Iterator createReadOnlyIterator()
  {
    return new ReadOnlyIterator();
  }

  public Iterator createReadOnlyIterator(final int list)
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


  public boolean isEmpty(final int list)
  {
    return getNext(list) == NULL;
  }

  /**
   * Computes the length of the given list. This method iterates over the
   * list to count it, so its complexity is linear.
   * @param   The list number identifying the list to be examined
   *          in this buffer.
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

  public boolean remove(final int list, final int data)
  {
    if (list != NULL) {
      int prev = list;
      int current = getNext(prev);
      while (current != NULL) {
        final int[] block = mBlocks.get(current >> BLOCK_SHIFT);
        final int offset = current & BLOCK_MASK;
        final int next = block[offset + OFFSET_NEXT];
        if (block[offset + OFFSET_NEXT] == data) {
          setNext(prev, next);
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

  public void dispose(final int list)
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
  private class ReadOnlyIterator implements Iterator
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
      mPrevious = mCurrent = NULL;
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
        mCurrent = list;
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
        setNext(mPrevious, next);
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
