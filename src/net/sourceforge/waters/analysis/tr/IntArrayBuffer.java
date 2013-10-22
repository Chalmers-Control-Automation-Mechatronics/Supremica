//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   IntArrayBuffer
//###########################################################################
//# $Id$
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
 * <P>A memory efficient container to store several arrays of integers.</P>
 *
 * <P>An <I>integer array buffer</I> maintains a collection of fixed-size
 * integer arrays. Each array is identified by its <I>index</I>, an integer
 * that is assigned by the set buffer upon creation and remains a unique
 * identifier throughout the array's lifetime. These indexes are assigned
 * starting from&nbsp;0 and incremented by&nbsp;1 each time a new array is
 * added.</P>
 *
 * <P>The arrays are stored in large pre-allocated buffers to avoid memory
 * overhead of allocating several small arrays. A hash table is used to
 * identify arrays already present in the buffer. If the same array is added
 * a second time, the already existing index is found and used instead of
 * adding a second copy.</P>
 *
 * <P>The integer array buffer does not support deletion or modification of
 * array after they have been created.</P>
 *
 * @author Robi Malik
 */

public class IntArrayBuffer implements WatersIntHashingStrategy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new integer array buffer.
   * @param  arraySize   The size of all arrays to be stored.
   */
  public IntArrayBuffer(final int arraySize)
  {
    this(arraySize, Integer.MAX_VALUE);
  }

  /**
   * Creates a new integer array buffer.
   * @param  arraySize   The size of all arrays to be stored.
   * @param  limit       Maximum number of arrays that can be added.
   *                     Adding more entries than the limit results
   *                     in an {@link OverflowException} being thrown.
   */
  public IntArrayBuffer(final int arraySize, final int limit)
  {
    this(arraySize, limit, 0, -1);
  }

  /**
   * Creates a new integer array buffer.
   * @param  arraySize   The size of all arrays to be stored.
   * @param  limit       Maximum number of arrays that can be added.
   *                     Adding more entries than the limit results
   *                     in an {@link OverflowException} being thrown.
   * @param  initialSize The estimated initial capacity of the hash table.
   * @param  defaultHashSetValue
   *                     The value to be returned by the {@link #get(int[])
   *                     get()} method if an entry is not found. The default
   *                     return value is&nbsp;-1, but it can be overridden by
   *                     this constructor.
   */
  public IntArrayBuffer(final int arraySize,
                        final int limit,
                        final int initialSize,
                        final int defaultHashSetValue)
  {
    mArraySize = arraySize;
    mBlockSize = arraySize * BLOCK_SIZE;
    mSizeLimit = limit;
    mDefaultHashValue = defaultHashSetValue;
    mBlocks = new ArrayList<int[]>();
    final int[] block = new int[mBlockSize];
    mBlocks.add(block);
    mDirectory = new WatersIntHashSet(initialSize, defaultHashSetValue, this);
    mNextFreeIndex = 0;
  }


  //#########################################################################
  //# Access Methods
  /**
   * Adds the data in the given array to this integer array buffer.
   * @param  data   Array of integers to be added.
   * @return A unique array index identifying an array set with the given
   *         contents in this buffer. This may be a newly created or an
   *         already existent array.
   */
  public int add(final int[] data) throws OverflowException
  {
    ensureCapacity(1);
    final int blockno = mNextFreeIndex >>> BLOCK_SHIFT;
    final int[] block = mBlocks.get(blockno);
    int offset = mArraySize * (mNextFreeIndex & BLOCK_MASK);
    for (int i = 0; i < mArraySize; i++) {
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
   * Adds the data in the given array list to this integer array buffer.
   * @param  data   Array list of integers to be added.
   * @return A unique array index identifying an array with the given
   *         contents in this buffer. This may be a newly created or an
   *         already existent array.
   */
  public int add(final TIntArrayList data) throws OverflowException
  {
    ensureCapacity(1);
    final int blockno = mNextFreeIndex >>> BLOCK_SHIFT;
    final int[] block = mBlocks.get(blockno);
    final int offset = mArraySize * (mNextFreeIndex & BLOCK_MASK);
    for (int i = 0, j = offset; i < mArraySize; i++, j++) {
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
   * Gets the unique array index of the given array in this integer array
   * buffer.
   * @param  data   Array containing data to be looked up.
   * @return The unique array index identifying an array with the given
   *         contents in this buffer, or the configured default value
   *         (usually&nbsp;-1) if not found.
   */
  public int get(final int[] data)
  {
    ensureCapacity(mArraySize);
    final int blockno = mNextFreeIndex >>> BLOCK_SHIFT;
    final int[] block = mBlocks.get(blockno);
    int offset = mArraySize * (mNextFreeIndex & BLOCK_MASK);
    for (int i = 0; i < mArraySize; i++) {
      block[offset++] = data[i];
    }
    final int result = mDirectory.get(mNextFreeIndex);
    return result == mNextFreeIndex ? mDefaultHashValue : result;
  }

  /**
   * Gets an array containing the contents of the given set.
   * @param  set     The unique set index identifying the set to be examined
   *                 in this integer set buffer.
   * @return A newly allocated array containing the numbers in the set.
   */
  public void getContents(final int array, final int[] data)
  {
    final int blockno = array >>> BLOCK_SHIFT;
    final int[] block = mBlocks.get(blockno);
    final int offset = mArraySize * (array & BLOCK_MASK);
    for (int i = 0, j = offset; i < mArraySize; i++, j++) {
      data[i] = block[j];
    }
  }

  /**
   * Gets the number of arrays currently stored in this integer set buffer.
   */
  public int size()
  {
    return mNextFreeIndex;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.tr.WatersIntHashingStrategy
  /**
   * Computes a hash code for the given array.
   * @param   array  The unique array index identifying the array to be
   *                 examined in this integer array buffer.
   */
  @Override
  public int computeHashCode(final int array)
  {
    final int blockno = array >>> BLOCK_SHIFT;
    final int[] block = mBlocks.get(blockno);
    int offset = mArraySize * (array & BLOCK_MASK);
    int result = block[offset];
    for (int j = 0; j < mArraySize; j++) {
      result = 5 * result + block[offset++];
    }
    return result;
  }

  /**
   * Determines whether the two given arrays have equal contents.
   * @param   array1 The unique array index identifying the first array to be
   *                 compared in this integer array buffer.
   * @param   array2 The unique array index identifying the second array to be
   *                 compared in this integer array buffer.
   */
  @Override
  public boolean equals(final int array1, final int array2)
  {
    final int blockno1 = array1 >>> BLOCK_SHIFT;
    final int[] block1 = mBlocks.get(blockno1);
    int offset1 = mArraySize * (array1 & BLOCK_MASK);
    final int blockno2 = array2 >>> BLOCK_SHIFT;
    final int[] block2 = mBlocks.get(blockno2);
    int offset2 = mArraySize * (array2 & BLOCK_MASK);
    for (int j = 0; j < mArraySize; j++) {
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

  public void dump(final PrintWriter printer, final int array)
  {
    printer.print('[');
    boolean first = true;
    final int blockno = array >>> BLOCK_SHIFT;
    final int[] block = mBlocks.get(blockno);
    int offset = mArraySize * (array & BLOCK_MASK);
    for (int j = 0; j < mArraySize; j++) {
      if (first) {
        first = false;
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
      final int[] block = new int[mBlockSize];
      mBlocks.add(block);
      capacity += BLOCK_SIZE;
    }
  }


  //#########################################################################
  //# Data Members
  private final int mArraySize;
  private final int mSizeLimit;
  private final int mBlockSize;
  private final int mDefaultHashValue;
  private final List<int[]> mBlocks;
  private final WatersIntHashSet mDirectory;

  private int mNextFreeIndex;


  //#########################################################################
  //# Class Constants
  private static final int BLOCK_SHIFT = 10;
  private static final int BLOCK_SIZE = 1 << BLOCK_SHIFT;
  private static final int BLOCK_MASK = BLOCK_SIZE - 1;

  private static final long serialVersionUID = 1L;

}

