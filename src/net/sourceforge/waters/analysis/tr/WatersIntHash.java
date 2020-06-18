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

import gnu.trove.impl.hash.TPrimitiveHash;
import gnu.trove.procedure.TIntProcedure;

import net.sourceforge.waters.model.base.WatersRuntimeException;


/**
 * An open addressed hashing implementation for int primitives.
 *
 * This is a modified version of GNU Trove 2 class
 * <CODE>gnu.trove.TIntHash</CODE> that supports configurable equality of
 * keys.
 *
 * @author Eric D. Friedman, Robi Malik
 */

abstract public class WatersIntHash
  extends TPrimitiveHash
  implements WatersIntHashingStrategy
{

  /** the set of ints */
  protected transient int[] _set;

  /** strategy used to hash values in this collection */
  protected WatersIntHashingStrategy _hashingStrategy;

  /**
   * Creates a new <code>TIntHash</code> instance with the default
   * capacity and load factor.
   */
  public WatersIntHash() {
    super();
    this._hashingStrategy = this;
  }

  /**
   * Creates a new <code>TIntHash</code> instance whose capacity
   * is the next highest prime above <tt>initialCapacity + 1</tt>
   * unless that value is already prime.
   *
   * @param initialCapacity an <code>int</code> value
   */
  public WatersIntHash(final int initialCapacity) {
    super(initialCapacity);
    this._hashingStrategy = this;
  }

  /**
   * Creates a new <code>WatersIntHash</code> instance with a prime
   * value at or near the specified capacity and load factor.
   *
   * @param initialCapacity used to find a prime capacity for the table.
   * @param loadFactor used to calculate the threshold over which
   * rehashing takes place.
   */
  public WatersIntHash(final int initialCapacity, final float loadFactor) {
    super(initialCapacity, loadFactor);
    this._hashingStrategy = this;
  }

  /**
   * Creates a new <code>WatersIntHash</code> instance with the default
   * capacity and load factor.
   * @param strategy used to compute hash codes and to compare keys.
   */
  public WatersIntHash(final WatersIntHashingStrategy strategy) {
    super();
    this._hashingStrategy = strategy;
  }

  /**
   * Creates a new <code>WatersIntHash</code> instance whose capacity
   * is the next highest prime above <tt>initialCapacity + 1</tt>
   * unless that value is already prime.
   *
   * @param initialCapacity an <code>int</code> value
   * @param strategy used to compute hash codes and to compare keys.
   */
  public WatersIntHash(final int initialCapacity,
                       final WatersIntHashingStrategy strategy)
  {
    super(initialCapacity);
    this._hashingStrategy = strategy;
  }

  /**
   * Creates a new <code>WatersIntHash</code> instance with a prime
   * value at or near the specified capacity and load factor.
   *
   * @param initialCapacity used to find a prime capacity for the table.
   * @param loadFactor used to calculate the threshold over which
   * rehashing takes place.
   * @param strategy used to compute hash codes and to compare keys.
   */
  public WatersIntHash(final int initialCapacity,
                       final float loadFactor,
                       final WatersIntHashingStrategy strategy)
  {
    super(initialCapacity, loadFactor);
    this._hashingStrategy = strategy;
  }

  /**
   * @return a deep clone of this collection
   */
  @Override
  public Object clone()
  {
    WatersIntHash h;
    try {
      h = (WatersIntHash)super.clone();
      h._set = this._set.clone();
      return h;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  /**
   * Initialises the hash table to a prime capacity which is at least
   * <tt>initialCapacity + 1</tt>.
   *
   * @param initialCapacity an <code>int</code> value
   * @return the actual capacity chosen
   */
  @Override
  public int setUp(final int initialCapacity)
  {
    final int capacity = super.setUp(initialCapacity);
    _set = new int[capacity];
    return capacity;
  }

  /**
   * Searches the set for <tt>val</tt>
   *
   * @param val an <code>int</code> value
   * @return a <code>boolean</code> value
   */
  public boolean contains(final int val) {
    return index(val) >= 0;
  }

  /**
   * Executes <tt>procedure</tt> for each element in the set.
   *
   * @param procedure a <code>TObjectProcedure</code> value
   * @return false if the loop over the set terminated because
   * the procedure returned false for some value.
   */
  public boolean forEach(final TIntProcedure procedure) {
    final byte[] states = _states;
    final int[] set = _set;
    for (int i = set.length; i-- > 0;) {
      if (states[i] == FULL && ! procedure.execute(set[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Releases the element currently stored at <tt>index</tt>.
   *
   * @param index an <code>int</code> value
   */
  @Override
  protected void removeAt(final int index) {
    _set[index] = 0;
    super.removeAt(index);
  }

  /**
   * Locates the index of <tt>val</tt>.
   *
   * @param val an <code>int</code> value
   * @return the index of <tt>val</tt> or -1 if it isn't in the set.
   */
  protected int index(final int val) {
    int hash, probe, index, length;

    final byte[] states = _states;
    final int[] set = _set;
    length = states.length;
    hash = _hashingStrategy.computeHashCode(val) & 0x7fffffff;
    index = hash % length;
    if (states[index] != FREE &&
        (states[index] == REMOVED ||
         !_hashingStrategy.equals(set[index], val))) {
      // see Knuth, p. 529
      probe = 1 + (hash % (length - 2));
      do {
        index -= probe;
        if (index < 0) {
          index += length;
        }
      } while (states[index] != FREE &&
               (states[index] == REMOVED ||
                !_hashingStrategy.equals(set[index], val)));
    }
    return states[index] == FREE ? -1 : index;
  }

  /**
   * Locates the index at which <tt>val</tt> can be inserted.  if
   * there is already a value equal()ing <tt>val</tt> in the set,
   * returns that value as a negative integer.
   *
   * @param val an <code>int</code> value
   * @return an <code>int</code> value
   */
  protected int insertionIndex(final int val)
  {
    int hash, probe, index, length;
    final byte[] states = _states;
    final int[] set = _set;
    length = states.length;
    hash = _hashingStrategy.computeHashCode(val) & 0x7fffffff;
    index = hash % length;
    if (states[index] == FREE) {
      return index;       // empty, all done
    } else if (states[index] == FULL &&
               _hashingStrategy.equals(set[index], val)) {
      return -index -1;   // already stored
    } else {                // already FULL or REMOVED, must probe
      // compute the double hash
      probe = 1 + (hash % (length - 2));
      // if the slot we landed on is FULL (but not removed), probe
      // until we find an empty slot, a REMOVED slot, or an element
      // equal to the one we are trying to insert.
      // finding an empty slot means that the value is not present
      // and that we should use that slot as the insertion point;
      // finding a REMOVED slot means that we need to keep searching,
      // however we want to remember the offset of that REMOVED slot
      // so we can reuse it in case a "new" insertion (i.e. not an update)
      // is possible.
      // finding a matching value means that we've found that our desired
      // key is already in the table
      if (states[index] != REMOVED) {
        // starting at the natural offset, probe until we find an
        // offset that isn't full.
        do {
          index -= probe;
          if (index < 0) {
            index += length;
          }
        } while (states[index] == FULL &&
                 !_hashingStrategy.equals(set[index], val));
      }
      // if the index we found was removed: continue probing until we
      // locate a free location or an element which equal()s the
      // one we have.
      if (states[index] == REMOVED) {
        final int firstRemoved = index;
        while (states[index] != FREE &&
               (states[index] == REMOVED ||
                !_hashingStrategy.equals(set[index], val))) {
          index -= probe;
          if (index < 0) {
            index += length;
          }
        }
        return states[index] == FULL ? -index -1 : firstRemoved;
      }
      // if it's full, the key is already stored
      return states[index] == FULL ? -index -1 : index;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.WatersIntHashingStrategy
  /**
   * Default implementation of TIntHashingStrategy:
   * delegates hashing to HashFunctions.hash(int).
   */
  @Override
  public final int computeHashCode(final int val)
  {
    return HashFunctions.hash(val);
  }

  /**
   * Default implementation of TIntHashingStrategy:
   * considers primitive values equal if they are the same number.
   */
  @Override
  public boolean equals(final int val1, final int val2)
  {
    return val1 == val2;
  }


  //########################################################################
  //# Inner Class HashIterator
  final class HashIterator implements WatersIntIterator
  {
    //######################################################################
    //# Constructor
    HashIterator()
    {
      reset();
    }

    //######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.WatersIntIterator
    @Override
    public HashIterator clone()
    {
      try {
        return (HashIterator) super.clone();
      } catch (final CloneNotSupportedException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    @Override
    public void reset()
    {
      mIndex = -1;
    }

    @Override
    public boolean advance()
    {
      while (mIndex < _states.length) {
        mIndex++;
        if (_states[mIndex] == FULL) {
          return true;
        }
      }
      return false;
    }

    @Override
    public int getCurrentData()
    {
      return _set[mIndex];
    }

    @Override
    public void remove()
    {
      removeAt(mIndex);
    }

    //######################################################################
    //# Data Members
    private int mIndex;
  }

}
