//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import gnu.trove.iterator.TIntIterator;
import gnu.trove.procedure.TIntProcedure;

import net.sourceforge.waters.model.base.ProxyTools;

/**
 * A binary heap of <CODE>int</CODE> primitives.
 * This is a modified version of code posted by Mark Allen Weiss at
 * <A HREF="http://www.java-tips.org/java-se-tips/java.lang/priority-queue-binary-heap-implementation-in-3.html">http://www.java-tips.org/</A>.
 *
 * @author Robi Malik, Mark Allen Weiss
 */

public class WatersIntHeap
  implements WatersIntComparator
{

  //#########################################################################
  //# Constructors
  /**
   * Constructs the binary heap.
   */
  public WatersIntHeap()
  {
    this(DEFAULT_CAPACITY, null);
  }

  /**
   * Constructs the binary heap.
   * @param capacity
   *          Initial capacity.
   */
  public WatersIntHeap(final int capacity)
  {
    this(capacity, null);
  }

  /**
   * Constructs the binary heap from an array.
   * @param items
   *          The initial items in the binary heap.
   */
  public WatersIntHeap(final int[] items)
  {
    this(items, null);
  }

  /**
   * Constructs the binary heap.
   * @param comparator
   *          Comparator to determine ordering, or <CODE>null</CODE>.
   */
  public WatersIntHeap(final WatersIntComparator comparator)
  {
    this(DEFAULT_CAPACITY, comparator);
  }

  /**
   * Constructs the binary heap.
   * @param capacity
   *          Initial capacity.
   * @param comparator
   *          Comparator to determine ordering, or <CODE>null</CODE>.
   */
  public WatersIntHeap(final int capacity,
                       final WatersIntComparator comparator)
  {
    mComparator = comparator == null ? this : comparator;
    mCurrentSize = 0;
    mData = new int[capacity + 1];
  }

  /**
   * Constructs the binary heap from an array.
   * @param items
   *          The initial items in the binary heap.
   * @param comparator
   *          Comparator to determine ordering, or <CODE>null</CODE>.
   */
  public WatersIntHeap(final int[] items,
                       final WatersIntComparator comparator)
  {
    this(items.length, comparator);
    mCurrentSize = items.length;
    for (int i = 0; i < items.length; i++) {
      mData[i + 1] = items[i];
    }
    buildHeap();
  }


  //#########################################################################
  //# Heap Access
  /**
   * Makes the priority queue logically empty.
   */
  public void clear()
  {
    mCurrentSize = 0;
  }

  /**
   * Tests if the priority queue is logically empty.
   * @return true if empty, false otherwise.
   */
  public boolean isEmpty()
  {
    return mCurrentSize == 0;
  }

  /**
   * Returns the current number of elements in the priority queue.
   */
  public int size()
  {
    return mCurrentSize;
  }

  /**
   * Inserts into the priority queue. Duplicates are allowed.
   * @param x
   *          the item to insert.
   */
  public void add(final int x)
  {
    if (mCurrentSize + 1 == mData.length) {
      grow();
    }
    // Percolate up
    int hole = ++mCurrentSize;
    mData[0] = x;
    while (true) {
      final int hole2 = hole >>> 1;
      if (mComparator.compare(x, mData[hole2]) >= 0) {
        break;
      }
      mData[hole] = mData[hole2];
      hole = hole2;
    }
    mData[hole] = x;
  }

  /**
   * Finds the smallest item in the priority queue.
   * @return The smallest item.
   */
  public int peekFirst()
  {
    if (isEmpty()) {
      throw new IllegalStateException("Empty binary heap!");
    }
    return mData[1];
  }

  /**
   * Removes multiple elements from the priority queue based on a condition.
   * This method iterates over the heap and checks each element for the
   * condition. Elements that satisfy the condition are marked for removal,
   * and afterwards the removal is performed while restoring the heap order.
   * This requires up to two traversals of the heap and takes linear time.
   * @param  condition  The {@link TIntProcedure#execute(int)} method is
   *                    applied to all heap elements. Elements for which the
   *                    call returns <CODE>true</CODE> are removed from the
   *                    heap, others are retained.
   * @return <CODE>true</CODE> if at least one element was removed,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean removeAll(final TIntProcedure condition)
  {
    boolean changed = false;
    int i = 1;
    while (i <= mCurrentSize) {
      if (condition.execute(mData[i])) {
        mData[i] = mData[mCurrentSize--];
        changed = true;
      } else {
        i++;
      }
    }
    if (changed) {
      buildHeap();
    }
    return changed;
  }

  /**
   * Removes the smallest item from the priority queue.
   * @return The smallest item.
   */
  public int removeFirst()
  {
    final int minItem = peekFirst();
    mData[1] = mData[mCurrentSize--];
    percolateDown(1);
    return minItem;
  }

  /**
   * Returns an array containing all elements in this priority queue.
   * @return A newly allocated array containing the elements in no
   *         specified order.
   */
  public int[] toArray()
  {
    final int[] temp = new int[mCurrentSize];
    for (int i = 0; i < mCurrentSize; i++) {
      temp[i] = mData[i+1];
    }
    return temp;
  }

  /**
   * Returns an iterator over the elements in this priority queue.
   * @return A newly allocated iterator that produces the heap elements in no
   *         specified order.
   */
  public TIntIterator unorderedIterator()
  {
    return new WatersIntHeapIterator();
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Establishes heap order property from an arbitrary arrangement of items.
   * Runs in linear time.
   */
  private void buildHeap()
  {
    for (int i = mCurrentSize >>> 1; i > 0; i--) {
      percolateDown(i);
    }
  }

  /**
   * Internal method to percolate down in the heap.
   * @param hole
   *          the index at which the percolate begins.
   */
  private void percolateDown(int hole)
  {
    int child;
    final int tmp = mData[hole];
    for (; hole << 1 <= mCurrentSize; hole = child) {
      child = hole << 1;
      if (child != mCurrentSize &&
          mComparator.compare(mData[child + 1], mData[child]) < 0) {
        child++;
      }
      if (mComparator.compare(mData[child], tmp) < 0) {
        mData[hole] = mData[child];
      } else {
        break;
      }
    }
    mData[hole] = tmp;
  }

  /**
   * Internal method to extend array.
   */
  private void grow()
  {
    final int[] newArray = new int[mData.length << 1];
    for (int i = 1; i <= mCurrentSize; i++) {
      newArray[i] = mData[i];
    }
    mData = newArray;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.tr.WatersIntComparator
  @Override
  public int compare(final int val1, final int val2)
  {
    return val1 - val2;
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder("[");
    for (int i = 0; i < mCurrentSize; i++) {
      if (i > 0) {
        builder.append(',');
      }
      builder.append(mData[i]);
    }
    builder.append(']');
    return builder.toString();
  }


  //#########################################################################
  //# Inner Class WatersIntHeapIterator
  private class WatersIntHeapIterator implements TIntIterator
  {
    //#########################################################################
    //# Constructor
    private WatersIntHeapIterator()
    {
      mIndex = 0;
    }

    //#########################################################################
    //# Interface gnu.trove.iterator.TIntIterator
    @Override
    public boolean hasNext()
    {
      return mIndex < mCurrentSize;
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) + " does not support removal!");
    }

    @Override
    public int next()
    {
      return mData[++mIndex];
    }

    //#########################################################################
    //# Data Members
    private int mIndex;
  }


  //#########################################################################
  //# Data Members
  private final WatersIntComparator mComparator;

  private int mCurrentSize; // Number of elements in heap
  private int[] mData; // The heap array


  //#########################################################################
  //# Class Constants
  private static final int DEFAULT_CAPACITY = 255;

}
