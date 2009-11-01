// This library is partially derived from the hash table classes in
// GNU Trove. It is licensed under the GNU LGPL 2.1:
//
// Copyright (c) 2009, Sam Douglas
// Portions Copyright (c) 2001, Eric D. Friedman All Rights Reserved. [GNU Trove]
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.waters.analysis.distributed.hashkit;

/**
 * The actual data for the hash table. This uses open addressing,
 * using a separate array of bytes to store the slot state.
 *
 * The methods in this class DO NOT do bounds checking. That's someone
 * else's job.
 * 
 * @author Sam Douglas
 */
public class IntHashData
{
  /**
   * Construct a hashtable using the supplied capacity and hashing
   * strategy. For good performance, the capacity should be a prime
   * number. The <code>hashkit.PrimeFinder</code> class can be used to
   * find a suitable size.
   * @param Exact capacity to allocate
   * @param Strategy for accessing data to compute equality and hash
   *        functions.
   */
  public IntHashData(int capacity, HashStrategy strategy)
  {
    mStrategy = strategy;
    mData = new int[capacity];
    mState = new byte[capacity];
  }
  
  /**
   * Gets the full capacity for the hash table. This is the total number
   * of slots allocated.
   * @return The total capacity of the hash table.
   */
  public int getCapacity()
  {
    return mState.length;
  }

  /**
   * Deletes the item at a certain position in the hashtable. This
   * value will be marked as deleted. The actual value will not be
   * changed (but should be considered invalid), and the entry will be
   * marked as deleted. This preserves any further items that may have
   * the same hash code.
   * @param index to remove.
   */
  public void delete(int index)
  {
    mState[index] = DELETED;
  }

  /**
   * Gets the data for a slot in the hash table. This function does no
   * bounds checking beyond the underlying array, so it can retrieve
   * deleted and unallocated data too.
   * @param index of slot to get data for
   * @return The data at this position in the hash table.
   */
  public int getData(int index)
  {
    return mData[index];
  }

  /**
   * Gets the state of a slot in the hashtable. This should have the
   * value <code>IntHashData.EMPTY</code>,
   * <code>IntHashData.OCCUPIED</code>, or
   * <code>IntHashData.DELETED</code>.
   * @param index of slot to get state for
   * @return state of the slot
   */
  public byte getState(int index)
  {
    return mState[index];
  }

  /**
   * Sets the state of a slot in the hashtable. The valid state values
   * are <code>IntHashData.EMPTY</code>,
   * <code>IntHashData.OCCUPIED</code>, and
   * <code>IntHashData.DELETED</code>.
   * @param index of slot to set state for
   * @param state value to set
   */
  public void setState(int index, byte state)
  {
    mState[index] = state;
  }

  /**
   * Sets the data for a slot in the hashtable.
   * @param index of slot to set data for
   * @param data value to set.
   */
  public void setData(int index, int data)
  {
    mData[index] = data;
  }

  /**
   * Stores a value into the specified hash table location and updates
   * the state to be occupied.
   * @param index of the slot to set data for
   * @data data to store in slot
   */ 
  public void store(int index, int data)
  {
    mData[index] = data;
    mState[index] = OCCUPIED;
  }

  /**
   * Finds the index into the hash data where this value is
   * located. If the object is not in the hash table, then -1 will be
   * returned.
   * @param value to look up in the hash.
   * @return The index the value is located at.
   */
  public int index(Object value)
  {
    final byte[] state = mState;
    final int[] data = mData;
    final int length = state.length;
    final int hash = mStrategy.computeHash(value) & 0x7fffffff;
    int index = hash % length;

    //Locate the value in the hash, using double hashing to avoid
    //clustering. This is done as according to TAOCP v3 p. 529 The
    //code is very similar to the primitive hash template in GNU Trove
    //2.1.0
    if (state[index] != EMPTY && 
	(state[index] == DELETED || 
	 !mStrategy.equal(value, mData[index])))
      {
	final int probe = 1 + (hash % (length - 2));
	do
	  {
	    index -= probe;
	    if (index < 0)
	      index += length;
	  }
	while (state[index] != EMPTY && 
	       (state[index] == DELETED || 
		!mStrategy.equal(value, mData[index])));
      }

    return state[index] == EMPTY ? -1 : index;
  }


  /**
   * Gets the index where a value should be inserted in the hash
   * table. If the value is discovered in the hash table, it returns
   * <code>-index -1</code>
   * @param value to find in the hash table
   * @return The index to insert at if not found, or the negated
   * index.
   */
  public int getInsertionIndex(Object value)
  {
    final byte[] state = mState;
    final int[] data = mData;
    final int length = state.length;
    final int hash =  mStrategy.computeHash(value) & 0x7fffffff;
    int index = hash % length;

    if (state[index] == EMPTY)
      return index;
    else if (state[index] == OCCUPIED && mStrategy.equal(value, mData[index]))
      return -index - 1;  //Value is already present in the hash table.
    else
      {
	final int probe = 1 + (hash % (length - 2));
	
	//Find the first entry that isn't full. If the current slot is
	//free, it would have been taken care of by a previous check.
	if (state[index] != DELETED)
	  {
	    do
	      {
		index -= probe;
		if (index < 0)
		  index += length;
	      }
	    while (state[index] == OCCUPIED && !mStrategy.equal(value, mData[index]));
	  }

	//If the index was removed, continue probing to ensure the
	//value does not appear in the table, but store the 
	//index of the first removed item.
	if (state[index] == DELETED)
	  {
	    int firstRemoved = index;
	    while (state[index] != EMPTY &&
		   (state[index] == DELETED || !mStrategy.equal(value, data[index])))
	      {
		index -= probe;
		if (index < 0)
		  index += length;
	      }

	    return state[index] == OCCUPIED ? -index -1 : firstRemoved;
	  }

	return state[index] == OCCUPIED ? -index -1 : index;
      }
  }

  /**
   * This duplicates the normal getInsertionIndex method, except it
   * uses the indirect value rather than an object. It is primarily
   * used for rehashing the table.
   *
   * Java's type system makes it difficult to unify these two cases
   * nicely, and so to maintain any hope of performance, they are
   * duplicated.
   *
   * @param value to find in the hash table
   * @return The index to insert at if not found, or the negated
   * index.
   */
  public int getIndirectInsertionIndex(int value)
  {
    final byte[] state = mState;
    final int[] data = mData;
    final int length = state.length;
    final int hash =  mStrategy.computeIndirectHash(value) & 0x7fffffff;
    int index = hash % length;
    if (state[index] == EMPTY)
      return index;
    else if (state[index] == OCCUPIED && mStrategy.equalIndirect(value, mData[index]))
      return -index - 1;  //Value is already present in the hash table.
    else
      {
	final int probe = 1 + (hash % (length - 2));
	
	//Find the first entry that isn't full. If the current slot is
	//free, it would have been taken care of by a previous check.
	if (state[index] != DELETED)
	  {
	    do
	      {
		index -= probe;
		if (index < 0)
		  index += length;
	      }
	    while (state[index] == OCCUPIED && !mStrategy.equalIndirect(value, mData[index]));
	  }

	//If the index was removed, continue probing to ensure the
	//value does not appear in the table, but store the 
	//index of the first removed item.
	if (state[index] == DELETED)
	  {
	    int firstRemoved = index;
	    while (state[index] != EMPTY &&
		   (state[index] == DELETED || !mStrategy.equalIndirect(value, data[index])))
	      {
		index -= probe;
		if (index < 0)
		  index += length;
	      }

	    return state[index] == OCCUPIED ? -index -1 : firstRemoved;
	  }

	return state[index] == OCCUPIED ? -index -1 : index;
      }
  }

  public void rehash(int newCapacity)
  {
    final int[] oldData = mData;
    final byte[] oldState = mState;
    final int oldLength = oldState.length;
    
    mData = new int[newCapacity];
    mState = new byte[newCapacity];

    for (int i = oldLength; i-- > 0;)
      {
	if (oldState[i] == OCCUPIED)
	  {
	    final int o = oldData[i];
	    final int index = getIndirectInsertionIndex(o);
	    mData[index] = o;
	    mState[index] = OCCUPIED;
	  }
      }
  }

  private final HashStrategy mStrategy;
  private byte[] mState;
  private int[] mData;

  /**
   * State for an unused slot in the hash table.
   */
  public static final byte EMPTY = 0;

  /**
   * State for an occupied slot in the hash table.
   */
  public static final byte OCCUPIED = 1;

  /**
   * State for a deleted slot in the hash table.
   */
  public static final byte DELETED = 2;
}