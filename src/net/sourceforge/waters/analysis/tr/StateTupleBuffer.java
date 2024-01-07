//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;


/**
 * <P>A memory efficient container to store state tuples represented as
 * arrays of integers, with a unique lookup facility.</P>
 *
 * <P>An <I>state tuple buffer</I> maintains a collection of fixed-size
 * integer arrays. Each array is identified by its <I>index</I>, an integer
 * that is assigned by the set buffer upon creation and remains a unique
 * identifier throughout the array's lifetime. These indexes are assigned
 * starting from&nbsp;0 and incremented by&nbsp;1 each time a new array is
 * added.</P>
 *
 * <P>The state tuples are stored in large pre-allocated buffers to avoid
 * memory overhead of allocating several small arrays. A hash table is used to
 * identify state tuples already present in the buffer. If the same state
 * tuples is added a second time, the already existing index is found and used
 * instead of adding a second copy.</P>
 *
 * <P>The state tuple buffer does not support deletion or modification of
 * entries after they have been created.</P>

 * <P>It is possible to allocate memory for extra <CODE>int</CODE> words
 * stored after each state tuple, which can be retrieved separately given
 * the state tuple's index, and which are not including when inserting,
 * retrieving, or comparing state tuples.</P>
 *
 * @author Robi Malik
 */

public class StateTupleBuffer implements WatersIntHashingStrategy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new state tuple buffer that does not store additional data
   * after each state tuple.
   * @param  tupleSize   The size of all state tuple to be stored.
   */
  public StateTupleBuffer(final int tupleSize)
  {
    this(tupleSize, 0);
  }

  /**
   * Creates a new state tuple buffer.
   * @param  tupleSize   The size of all state tuple to be stored.
   * @param  extraWords  The number of additional <CODE>int</CODE> entries
   *                     to be stored after each state tuple.
   */
  public StateTupleBuffer(final int tupleSize,
                          final int extraWords)
  {
    this(tupleSize, extraWords, Integer.MAX_VALUE);
  }

  /**
   * Creates a new state tuple buffer.
   * @param  tupleSize   The size of all state tuple to be stored.
   * @param  extraWords  The number of additional <CODE>int</CODE> entries
   *                     to be stored after each state tuple.
   * @param  limit       Maximum number of state tuples that can be added.
   *                     Adding more entries than the limit results
   *                     in an {@link OverflowException} being thrown.
   */
  public StateTupleBuffer(final int tupleSize,
                          final int extraWords,
                          final int limit)
  {
    this(tupleSize, extraWords, limit, -1);
  }

  /**
   * Creates a new state tuple buffer.
   * @param  tupleSize   The size of all state tuple to be stored.
   * @param  extraWords  The number of additional <CODE>int</CODE> entries
   *                     to be stored after each state tuple.
   * @param  limit       Maximum number of state tuples that can be added.
   *                     Adding more entries than the limit results
   *                     in an {@link OverflowException} being thrown.
   * @param  defaultHashSetValue
   *                     The value to be returned by the {@link #getIndex(int[])
   *                     get()} method if an entry is not found. The default
   *                     return value is&nbsp;-1, but it can be overridden by
   *                     this constructor.
   */
  public StateTupleBuffer(final int tupleSize,
                          final int extraWords,
                          final int limit,
                          final int defaultHashSetValue)
  {
    mAllocatedTupleSize = tupleSize + extraWords;
    mRetrievedTupleSize = tupleSize;
    mBlockSize = tupleSize < 1 ? 1 : mAllocatedTupleSize * BLOCK_SIZE;
    mSizeLimit = limit;
    mDefaultHashValue = defaultHashSetValue;
    mBlocks = new ArrayList<int[]>();
    final int[] block = createBlock();
    mBlocks.add(block);
    final int initialSize = Math.min(limit, MAX_TABLE_SIZE);
    mDirectory = new WatersIntHashSet(initialSize, defaultHashSetValue, this);
    mNextFreeIndex = 0;
  }


  //#########################################################################
  //# Access Methods
  /**
   * Adds the data in the given array to this integer array buffer.
   * @param  data   Array of integers to be added.
   * @return A unique index identifying a state tuple with the given
   *         contents in this buffer. This may be a newly created or an
   *         already existent state tuple.
   */
  public int add(final int[] data) throws OverflowException
  {
    ensureCapacity(1);
    final int blockno = mNextFreeIndex >>> BLOCK_SHIFT;
    final int[] block = mBlocks.get(blockno);
    int offset = mAllocatedTupleSize * (mNextFreeIndex & BLOCK_MASK);
    for (int i = 0; i < data.length; i++) {
      block[offset++] = data[i];
    }
    final int result = mDirectory.getOrAdd(mNextFreeIndex);
    if (result == mNextFreeIndex) {
      if (mNextFreeIndex >= mSizeLimit) {
        throw new OverflowException(OverflowKind.STATE, mSizeLimit);
      }
      mNextFreeIndex++;
    }
    return result;
  }

  /**
   * Adds the data in the given array list to this state tuple buffer.
   * @param  data   Array list of integers to be added.
   * @return A unique index identifying a state tuple with the given
   *         contents in this buffer. This may be a newly created or an
   *         already existent state tuple.
   */
  public int add(final TIntArrayList data) throws OverflowException
  {
    ensureCapacity(1);
    final int blockno = mNextFreeIndex >>> BLOCK_SHIFT;
    final int[] block = mBlocks.get(blockno);
    final int offset = mAllocatedTupleSize * (mNextFreeIndex & BLOCK_MASK);
    for (int i = 0, j = offset; i < mRetrievedTupleSize; i++, j++) {
      block[j] = data.get(i);
    }
    final int result = mDirectory.getOrAdd(mNextFreeIndex);
    if (result == mNextFreeIndex) {
      if (mNextFreeIndex >= mSizeLimit) {
        throw new OverflowException(OverflowKind.STATE, mSizeLimit);
      }
      mNextFreeIndex++;
    }
    return result;
  }

  /**
   * Gets the unique index of a state tuple.
   * @param  data   Array containing data to be looked up.
   * @return The unique index identifying a state tuple with the given
   *         contents in this buffer, or the configured default value
   *         (usually&nbsp;-1) if not found.
   */
  public int getIndex(final int[] data)
  {
    ensureCapacity(mAllocatedTupleSize);
    final int blockno = mNextFreeIndex >>> BLOCK_SHIFT;
    final int[] block = mBlocks.get(blockno);
    int offset = mAllocatedTupleSize * (mNextFreeIndex & BLOCK_MASK);
    for (int i = 0; i < data.length; i++) {
      block[offset++] = data[i];
    }
    final int result = mDirectory.get(mNextFreeIndex);
    return result == mNextFreeIndex ? mDefaultHashValue : result;
  }

  /**
   * Retrieves the contents of the given array.
   * @param  index   The unique index identifying the state tuple to be
   *                 examined.
   * @param  data    Array to receive data.
   */
  public void getContents(final int index, final int[] data)
  {
    final int blockno = index >>> BLOCK_SHIFT;
    final int[] block = mBlocks.get(blockno);
    final int offset = mAllocatedTupleSize * (index & BLOCK_MASK);
    for (int i = 0, j = offset; i < data.length; i++, j++) {
      data[i] = block[j];
    }
  }

  /**
   * Gets the size of the state tuples allocated in the buffer.
   * This value indicates the number of <CODE>int</CODE> entries allocated
   * under a given index.
   */
  public int getAllocatedTupleSize()
  {
    return mAllocatedTupleSize;
  }

  /**
   * Gets the size of the state tuples retrieved using
   * {@link #getContents(int,int[]) getContents()} and similar methods.
   * The retrieved size may be smaller than the allocated size to allow for
   * the storage of extra data after state tuples, which is not retrieved
   * automatically with each access.
   * @see #getAllocatedTupleSize()
   * @see #getExtraWord(int, int)
   * @see #setExtraWord(int, int, int)
   */
  public int getRetrievedTupleSize()
  {
    return mRetrievedTupleSize;
  }

  /**
   * Gets the number of arrays currently stored in this state tuple buffer.
   */
  public int size()
  {
    return mNextFreeIndex;
  }

  /**
   * Retrieves an additional word after a state tuple. This method is
   * used to access data that is stored after state tuples when the allocated
   * array tuple is greater than the retrieved size.
   * @param  index   The unique index identifying the state tuple to be
   *                 examined.
   * @param  wordno  The index of the additional word to be retrieved, where
   *                 <CODE>0</CODE> indicates the first word stored after
   *                 the state tuple data.
   * @return The current value of the extra word.
   */
  public int getExtraWord(final int index, final int wordno)
  {
    final int blockno = index >>> BLOCK_SHIFT;
    final int[] block = mBlocks.get(blockno);
    final int offset = mAllocatedTupleSize * (index & BLOCK_MASK) +
      mRetrievedTupleSize + wordno;
    return block[offset];
  }

  /**
   * Stores an additional word after a state tuple. This method is
   * used to access data that is stored after state tuples when the allocated
   * array tuple is greater than the retrieved size.
   * @param  index   The unique index identifying the state tuple to be
   *                 examined.
   * @param  wordno  The index of the additional word to be stored, where
   *                 <CODE>0</CODE> indicates the first word stored after
   *                 the state tuple data.
   * @param  value   The value to be stored as extra word.
   */
  public void setExtraWord(final int index, final int wordno, final int value)
  {
    final int blockno = index >>> BLOCK_SHIFT;
    final int[] block = mBlocks.get(blockno);
    final int offset = mAllocatedTupleSize * (index & BLOCK_MASK) +
      mRetrievedTupleSize + wordno;
    block[offset] = value;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.tr.WatersIntHashingStrategy
  /**
   * Computes a hash code for the given array.
   * @param  index   The unique index identifying the state tuple to be
   *                 examined.
   */
  @Override
  public int computeHashCode(final int index)
  {
    final int blockno = index >>> BLOCK_SHIFT;
    final int[] block = mBlocks.get(blockno);
    int offset = mAllocatedTupleSize * (index & BLOCK_MASK);
    int result = block[offset];
    for (int j = 1; j < mRetrievedTupleSize; j++) {
      result = 5 * result + block[offset++];
    }
    return result;
  }

  /**
   * Determines whether the two given state tuples have equal contents.
   * @param   index1 The unique index identifying the first state tuple to be
   *                 compared.
   * @param   index2 The unique index identifying the second state tuple to be
   *                 compared.
   */
  @Override
  public boolean equals(final int index1, final int index2)
  {
    final int blockno1 = index1 >>> BLOCK_SHIFT;
    final int[] block1 = mBlocks.get(blockno1);
    int offset1 = mAllocatedTupleSize * (index1 & BLOCK_MASK);
    final int blockno2 = index2 >>> BLOCK_SHIFT;
    final int[] block2 = mBlocks.get(blockno2);
    int offset2 = mAllocatedTupleSize * (index2 & BLOCK_MASK);
    for (int j = 0; j < mRetrievedTupleSize; j++) {
      if (block1[offset1++] != block2[offset2++]) {
        return false;
      }
    }
    return true;
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final StringWriter writer = new StringWriter();
    final PrintWriter printer = new PrintWriter(writer);
    for (int array = 0; array < mNextFreeIndex; array++) {
      printer.print(array);
      printer.print(": ");
      dump(printer, array);
      printer.println();
    }
    printer.close();
    return writer.toString();
  }

  public String toString(final int array)
  {
    final StringWriter writer = new StringWriter();
    final PrintWriter printer = new PrintWriter(writer);
    dump(printer, array);
    printer.close();
    return writer.toString();
  }

  public void dump(final PrintWriter printer, final int index)
  {
    printer.print('[');
    boolean first = true;
    final int blockno = index >>> BLOCK_SHIFT;
    final int[] block = mBlocks.get(blockno);
    int offset = mAllocatedTupleSize * (index & BLOCK_MASK);
    int j = 0;
    for (; j < mRetrievedTupleSize; j++) {
      if (first) {
        first = false;
      } else {
        printer.print(", ");
      }
      printer.print(block[offset++]);
    }
    first = true;
    for (; j < mAllocatedTupleSize; j++) {
      if (first) {
        first = false;
        printer.print("; ");
      } else {
        printer.print(", ");
      }
      printer.print(block[offset++]);
    }
    printer.print(']');
  }


  //#########################################################################
  //# Auxiliary Methods
  private void ensureCapacity(final int additionalItems)
  {
    final int needed = mNextFreeIndex + additionalItems;
    int capacity = mBlocks.size() * BLOCK_SIZE;
    while (needed >= capacity) {
      final int[] block = createBlock();
      mBlocks.add(block);
      capacity += BLOCK_SIZE;
    }
  }

  private int[] createBlock()
  {
    return new int[mBlockSize];
  }


  //#########################################################################
  //# Data Members
  /**
   * The number of <CODE>int</CODE> words allocated for each state tuple,
   * including extra words.
   */
  private final int mAllocatedTupleSize;
  /**
   * The number of <CODE>int</CODE> words used when retrieving a each state
   * tuple, <I>not</I> including extra words.
   */
  private final int mRetrievedTupleSize;
  /**
   * The maximum number of state tuples that can be stored before a
   * {@link OverflowException} is thrown.
   */
  private final int mSizeLimit;
  /**
   * The size of the <CODE>int[]</CODE> array for each block.
   */
  private final int mBlockSize;
  /**
   * The default value for the hash table {@link #mDirectory}.
   */
  private final int mDefaultHashValue;
  /**
   * The list of blocks containing the data. Each entry is an
   * <CODE>int[]</CODE> array is of size {@link #mBlockSize} that can
   * hold up to {@link #BLOCK_SIZE} state tuples including extra words.
   */
  private final List<int[]> mBlocks;
  /**
   * The hash set for unique lookup. Contains integers representing
   * state tuple indexes.
   */
  private final WatersIntHashSet mDirectory;
  /**
   * The index of the first unused state tuple. Refers to a spare entry
   * used as auxiliary space to prepare for lookups or insertions.
   * Equal to the number of state tuples currently stored.
   */
  private int mNextFreeIndex;


  //#########################################################################
  //# Class Constants
  /**
   * The number of bits needed to address all state tuples in a block.
   */
  private static final int BLOCK_SHIFT = 10;
  /**
   * The number of state tuples stored in each block.
   * Equal to 2<SUP>{@link #BLOCK_SHIFT}</SUP>.
   */
  private static final int BLOCK_SIZE = 1 << BLOCK_SHIFT;
  /**
   * Bit mask for addressing within a block.
   * Equal to {@link #BLOCK_SIZE}&ndash;1.
   */
  private static final int BLOCK_MASK = BLOCK_SIZE - 1;
  /**
   * Maximum hash table size for initialisation of {@link #mDirectory}.
   */
  private static final int MAX_TABLE_SIZE = 500000;

  private static final long serialVersionUID = 6116902981378805132L;

}
