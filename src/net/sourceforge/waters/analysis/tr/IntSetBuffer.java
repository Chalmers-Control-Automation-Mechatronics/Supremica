//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   IntListBuffer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonTools;


/**
 * <P>A memory efficient container to store several sets of integers.</P>
 *
 * <P>An <I>integer set buffer</I> maintains a collection of sets of integers.
 * Each set is identified by its <I>index</I>, an integer that is assigned
 * by the set buffer upon creation of the set and remains a unique
 * identifier throughout the set's lifetime.</P>
 *
 * <P>Each set is represented by its size followed by its ordered list of
 * elements. These numbers are bit-packed into into pre-allocated integer
 * arrays to minimise memory usage and allocation.</P>
 *
 * <P>A hash table is used to identify sets already present in the buffer.
 * If the same set is added a second time, the already existing index is
 * found and used instead of adding a second copy.</P>
 *
 * <P>The integer set buffer does not support deletion or modification of
 * sets after they have been created.</P>
 *
 * @author Robi Malik
 */

public class IntSetBuffer implements WatersIntHashingStrategy
{

  //#########################################################################
  //# Constructors
  public IntSetBuffer(final int numValues)
  {
    this(numValues, 0);
  }

  public IntSetBuffer(final int numValues, final int initialSize)
  {
    this(numValues, 0, 0);
  }

  public IntSetBuffer(final int numValues, final int initialSize, final int defaultHashSetValue)
  {
    mSizeShift = AutomatonTools.log2(numValues + 1);
    mSizeMask = (1 << mSizeShift) - 1;
    mDataShift = AutomatonTools.log2(numValues);
    mDataMask = (1 << mDataShift) - 1;
    mBlocks = new ArrayList<int[]>();
    final int[] block = new int[BLOCK_SIZE];
    mBlocks.add(block);
    mDirectory = new WatersIntHashSet(initialSize, defaultHashSetValue, this);
    mSize = mNextFreeOffset = 0;
  }


  //#########################################################################
  //# Access Methods
  /**
   * Adds the data in the given array as a set to this integer set buffer.
   * @param  data   Array of integers forming a new set. The array must be
   *                ordered and free from duplicates.
   * @return A unique set index identifying a set with the given contents in
   *         this buffer. This may be a newly created or an already existent
   *         set.
   */
  public int add(final int[] data)
  {
    final int count = data.length;
    final int words = getNumberOfWords(count);
    ensureCapacity(words);
    int blockno = mNextFreeOffset >>> BLOCK_SHIFT;
    int[] block = mBlocks.get(blockno);
    int offset = mNextFreeOffset & BLOCK_MASK;
    long current = count;
    int shift = mSizeShift;
    for (final long value : data) {
      current |= (value << shift);
      shift += mDataShift;
      if (shift >= 32) {
        if (offset >= BLOCK_SIZE) {
          offset = 0;
          block = mBlocks.get(++blockno);
        }
        block[offset++] = (int) (current & 0xffffffffL);
        current >>>= 32;
        shift -= 32;
      }
    }
    if (shift > 0) {
      if (offset >= BLOCK_SIZE) {
        offset = 0;
        block = mBlocks.get(++blockno);
      }
      block[offset] = (int) (current & 0xffffffffL);
    }
    final int result = mDirectory.getOrAdd(mNextFreeOffset);
    if (result == mNextFreeOffset) {
      mSize++;
      mNextFreeOffset += words;
    }
    return result;
  }

  /**
   * Adds the contents of the given set in this buffer to the given hash set.
   * @param  set     The unique set index identifying the set to be examined
   *                 in this integer set buffer.
   * @param  output  Hash set to which data is to be added.
   */
  public void collect(final int set, final TIntHashSet output)
  {
    int blockno = set >>> BLOCK_SHIFT;
    int[] block = mBlocks.get(blockno);
    int offset = set & BLOCK_MASK;
    long data = block[offset] & 0xffffffffL;
    final int count = (int) (data & mSizeMask);
    int remainingBits = 32 - mSizeShift;
    data >>>= mSizeShift;
    for (int i = 0; i < count; i++) {
      if (remainingBits < mDataShift) {
        offset++;
        if (offset > BLOCK_MASK) {
          offset = 0;
          blockno++;
          block = mBlocks.get(blockno);
        }
        data |= ((block[offset] & 0xffffffffL) << remainingBits);
        remainingBits += 32;
      }
      final int value = (int) (data & mDataMask);
      output.add(value);
      data >>>= mDataShift;
      remainingBits -= mDataShift;
    }
  }

  public int get(final int[] data) {

    final int count = data.length;
    final int words = getNumberOfWords(count);
    ensureCapacity(words);
    int blockno = mNextFreeOffset >>> BLOCK_SHIFT;
    int[] block = mBlocks.get(blockno);
    int offset = mNextFreeOffset & BLOCK_MASK;
    long current = count;
    int shift = mSizeShift;
    for (final long value : data) {
      current |= (value << shift);
      shift += mDataShift;
      if (shift >= 32) {
        if (offset >= BLOCK_SIZE) {
          offset = 0;
          block = mBlocks.get(++blockno);
        }
        block[offset++] = (int) (current & 0xffffffffL);
        current >>>= 32;
        shift -= 32;
      }
    }
    if (shift > 0) {
      if (offset >= BLOCK_SIZE) {
        offset = 0;
        block = mBlocks.get(++blockno);
      }
      block[offset] = (int) (current & 0xffffffffL);
    }
   return mDirectory.get(mNextFreeOffset);
  }

  /**
   * Gets an array containing the contents of the given set.
   * @param  set     The unique set index identifying the set to be examined
   *                 in this integer set buffer.
   * @return A newly allocated array containing the numbers in the set.
   */
  public int[] getSetContents(final int set)
  {
    int blockno = set >>> BLOCK_SHIFT;
    int[] block = mBlocks.get(blockno);
    int offset = set & BLOCK_MASK;
    long data = block[offset] & 0xffffffffL;
    final int count = (int) (data & mSizeMask);
    final int[] result = new int[count];
    int remainingBits = 32 - mSizeShift;
    data >>>= mSizeShift;
    for (int i = 0; i < count; i++) {
      if (remainingBits < mDataShift) {
        offset++;
        if (offset > BLOCK_MASK) {
          offset = 0;
          blockno++;
          block = mBlocks.get(blockno);
        }
        data |= ((block[offset] & 0xffffffffL) << remainingBits);
        remainingBits += 32;
      }
      result[i] = (int) (data & mDataMask);
      data >>>= mDataShift;
      remainingBits -= mDataShift;
    }
    return result;
  }

  /**
   * Adds the data in the given list as a set to this integer set buffer.
   * @param  data   List of integers forming a new set. The list must be
   *                ordered and free from duplicates.
   * @return A unique set index identifying a set with the given contents in
   *         this buffer. This may be a newly created or an already existent
   *         set.
   */
  public int add(final TIntArrayList data)
  {
    final int count = data.size();
    final int words = getNumberOfWords(count);
    ensureCapacity(words);
    int blockno = mNextFreeOffset >>> BLOCK_SHIFT;
    int[] block = mBlocks.get(blockno);
    int offset = mNextFreeOffset & BLOCK_MASK;
    long current = count;
    int shift = mSizeShift;
    for (int i = 0; i < count; i++) {
      final long value = data.get(i);
      current |= (value << shift);
      shift += mDataShift;
      if (shift >= 32) {
        if (offset >= BLOCK_SIZE) {
          offset = 0;
          block = mBlocks.get(++blockno);
        }
        block[offset++] = (int) (current & 0xffffffffL);
        current >>>= 32;
        shift -= 32;
      }
    }
    if (shift > 0) {
      if (offset >= BLOCK_SIZE) {
        offset = 0;
        block = mBlocks.get(++blockno);
      }
      block[offset] = (int) (current & 0xffffffffL);
    }
    final int result = mDirectory.getOrAdd(mNextFreeOffset);
    if (result == mNextFreeOffset) {
      mSize++;
      mNextFreeOffset += words;
    }
    return result;
  }

  /**
   * Adds the data in the given hash set as a set to this integer set buffer.
   * @param  data   Contents of the new set.
   * @return A unique set index identifying a set with the given contents in
   *         this buffer. This may be a newly created or an already existent
   *         set.
   */
  public int add(final TIntHashSet data)
  {
    final int[] array = data.toArray();
    Arrays.sort(array);
    return add(array);
  }

  /**
   * Returns an iterator over the indexes of the sets contained in this buffer.
   * The iterator returned by this methods produces the offsets of valid
   * sets in the buffer, in the order in which they have been created.
   */
  public WatersIntIterator globalIterator()
  {
    return new GlobalIterator();
  }

  /**
   * Returns an iterator that can be used to iterate over the individual
   * sets in this buffer. The iterator returned by this method is not
   * initialised, so its {@link IntSetIterator#reset(int) reset()} method
   * should be used before starting to iterate.
   */
  public IntSetIterator iterator()
  {
    return new IntSetIterator();
  }

  /**
   * Returns an iterator to iterate over the given set in this buffer. The
   * iterator returned by this method is initialised to iterate over the given
   * set. Its {@link IntSetIterator#reset(int) reset()} method can be used to
   * iterate over other sets.
   * @param   set    The unique set index identifying the set to be examined
   *                 in this integer set buffer.
   */
  public IntSetIterator iterator(final int set)
  {
    return new IntSetIterator(set);
  }

  /**
   * Gets the number of sets currently stored in this integer set buffer.
   */
  public int size()
  {
    return mSize;
  }

  /**
   * Gets the number of elements of the given set.
   * @param   set    The unique set index identifying the set to be examined
   *                 in this integer set buffer.
   */
  public int size(final int set)
  {
    final int[] block = mBlocks.get(set >>> BLOCK_SHIFT);
    final int offset = set & BLOCK_MASK;
    final int data = block[offset];
    return data & mSizeMask;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.tr.WatersIntHashingStrategy
  /**
   * Computes a hash code for the given set.
   * @param   set    The unique set index identifying the set to be examined
   *                 in this integer set buffer.
   */
  @Override
  public int computeHashCode(final int set)
  {
    int blockno = set >>> BLOCK_SHIFT;
    int[] block = mBlocks.get(blockno);
    int offset = set & BLOCK_MASK;
    int result = block[offset];
    final int count = result & mSizeMask;
    final int words = getNumberOfWords(count);
    for (int w = 1; w < words; w++) {
      if (++offset >= BLOCK_SIZE) {
        offset = 0;
        blockno++;
        block = mBlocks.get(blockno);
      }
      result = 5 * result + block[offset];
    }
    return result;
  }

  /**
   * Determines whether the two given sets have equal contents.
   * @param   set1   The unique set index identifying the first set to be
   *                 compared in this integer set buffer.
   * @param   set2   The unique set index identifying the second set to be
   *                 compared in this integer set buffer.
   */
  @Override
  public boolean equals(final int set1, final int set2)
  {
    int blockno1 = set1 >>> BLOCK_SHIFT;
    int[] block1 = mBlocks.get(blockno1);
    int offset1 = set1 & BLOCK_MASK;
    final int data1 = block1[offset1];
    int blockno2 = set2 >>> BLOCK_SHIFT;
    int[] block2 = mBlocks.get(blockno2);
    int offset2 = set2 & BLOCK_MASK;
    final int data2 = block2[offset2];
    if (data1 != data2) {
      return false;
    }
    final int count = data1 & mSizeMask;
    final int words = getNumberOfWords(count);
    for (int w = 1; w < words; w++) {
      if (++offset1 >= BLOCK_SIZE) {
        offset1 = 0;
        blockno1++;
        block1 = mBlocks.get(blockno1);
      }
      if (++offset2 >= BLOCK_SIZE) {
        offset2 = 0;
        blockno2++;
        block2 = mBlocks.get(blockno2);
      }
      if (block1[offset1] != block2[offset2]) {
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
    final int[] sets = mDirectory.toArray();
    Arrays.sort(sets);
    for (final int set : sets) {
      printer.print(set);
      printer.print(" : ");
      dump(printer, set);
      printer.println();
    }
    printer.close();
    return writer.toString();
  }

  public String toString(final int set)
  {
    final StringWriter writer = new StringWriter();
    final PrintWriter printer = new PrintWriter(writer);
    dump(printer, set);
    printer.close();
    return writer.toString();
  }

  public void dump(final PrintWriter printer, final int set)
  {
    final IntSetIterator iter = iterator(set);
    printer.print('{');
    boolean first = true;
    while (iter.advance()) {
      if (first) {
        first = false;
      } else {
        printer.print(", ");
      }
      final int state = iter.getCurrentData();
      printer.print(state);
    }
    printer.print('}');
  }


  //#########################################################################
  //# Auxiliary Methods
  private int getNumberOfWords(final int count)
  {
    final int bits = mSizeShift + mDataShift * count;
    return (bits + 31) >>> 5;
  }

  private void ensureCapacity(final int additionalWords)
  {
    final int needed = mNextFreeOffset + additionalWords;
    int capacity = mBlocks.size() * BLOCK_SIZE;
    while (needed >= capacity) {
      final int[] block = new int[BLOCK_SIZE];
      mBlocks.add(block);
      capacity += BLOCK_SIZE;
    }
  }


  //#########################################################################
  //# Inner Class GlobalIterator
  public final class GlobalIterator implements WatersIntIterator
  {
    //#######################################################################
    //# Constructors
    private GlobalIterator()
    {
      reset();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.WatersIntIterator
    @Override
    public void reset()
    {
      mCurrentOffset = -1;
    }

    @Override
    public boolean advance()
    {
      if (mCurrentOffset >= mNextFreeOffset) {
        return false;
      } else if (mCurrentOffset >= 0) {
        final int count = size(mCurrentOffset);
        mCurrentOffset += getNumberOfWords(count);
        return mCurrentOffset < mNextFreeOffset;
      } else {
        mCurrentOffset = 0;
        return true;
      }
    }

    @Override
    public int getCurrentData()
    {
      return mCurrentOffset;
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support removal of elements!");
    }

    //#######################################################################
    //# Data Members
    private int mCurrentOffset;
  }


  //#########################################################################
  //# Inner Class IntSetIterator
  /**
   * An iterator that can iterate over individual sets in an integer list
   * buffer. The iteration over a set produces its elements in ascending
   * order. As the integer set buffer does not support deletions, the
   * {@link #remove()} method is not implemented.
   */
  public final class IntSetIterator implements WatersIntIterator
  {
    //#######################################################################
    //# Constructors
    private IntSetIterator()
    {
    }

    private IntSetIterator(final int set)
    {
      reset(set);
    }

    //#######################################################################
    //# Specific Access
    /**
     * Resets this iterator to iterate over the given set.
     * @param   set    The unique set index identifying the set to be examined
     *                 in the iterator's integer set buffer.
     */
    public void reset(final int set)
    {
      mHeadOffset = set;
      reset();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.WatersIntIterator
    @Override
    public void reset()
    {
      mCurrentOffset = mHeadOffset;
      final int[] block = mBlocks.get(mCurrentOffset >> BLOCK_SHIFT);
      final int offset = mCurrentOffset & BLOCK_MASK;
      final int data = block[offset];
      mRemainingCount = data & mSizeMask;
      mCurrentShift = mSizeShift - mDataShift;
      mCurrentData = (data & 0xffffffffL) >>> mCurrentShift;
    }

    @Override
    public boolean advance()
    {
      if (mRemainingCount == 0) {
        return false;
      } else {
        mRemainingCount--;
        mCurrentShift += mDataShift;
        mCurrentData >>>= mDataShift;
        return true;
      }
    }

    @Override
    public int getCurrentData()
    {
      if (mCurrentShift >= 32) {
        mCurrentOffset += mCurrentShift >>> 5;
        mCurrentShift &= 0x1f;
        final int[] block = mBlocks.get(mCurrentOffset >> BLOCK_SHIFT);
        final int offset = mCurrentOffset & BLOCK_MASK;
        final long data = block[offset] & 0xffffffffL;
        mCurrentData = data >>> mCurrentShift;
      }
      if (mCurrentShift + mDataShift > 32) {
        mCurrentOffset++;
        final int[] block = mBlocks.get(mCurrentOffset >> BLOCK_SHIFT);
        final int offset = mCurrentOffset & BLOCK_MASK;
        final long data = block[offset] & 0xffffffffL;
        mCurrentData |= data << (32 - mCurrentShift);
        mCurrentShift -= 32;
      }
      return (int) (mCurrentData & mDataMask);
    }

    /**
     * @throws UnsupportedOperationException because deletions are not
     *         supported by integer set buffers.
     */
    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support removal of elements!");
    }

    //#######################################################################
    //# Data Members
    private int mHeadOffset;
    private int mRemainingCount;
    private int mCurrentOffset;
    private int mCurrentShift;
    private long mCurrentData;
  }


  //#########################################################################
  //# Data Members
  private final int mSizeShift;
  private final int mSizeMask;
  private final int mDataShift;
  private final int mDataMask;
  private final List<int[]> mBlocks;
  private final WatersIntHashSet mDirectory;

  private int mSize;
  private int mNextFreeOffset;


  //#########################################################################
  //# Class Constants
  private static final int BLOCK_SHIFT = 10;
  private static final int BLOCK_SIZE = 1 << BLOCK_SHIFT;
  private static final int BLOCK_MASK = BLOCK_SIZE - 1;

  private static final long serialVersionUID = 1L;

}

