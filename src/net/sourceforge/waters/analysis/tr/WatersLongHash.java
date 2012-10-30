//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   WatersLongHash
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import gnu.trove.HashFunctions;
import gnu.trove.TLongProcedure;
import gnu.trove.TPrimitiveHash;


/**
 * An open addressed hashing implementation for long primitives.
 *
 * This is a modified version of {@link gnu.trove.TIntHash}
 * that supports configurable equality of keys.
 *
 * @author Eric D. Friedman, Robi Malik
 */

abstract public class WatersLongHash
  extends TPrimitiveHash
  implements WatersLongHashingStrategy
{

  /** the set of longs */
  protected transient long[] _set;

  /** strategy used to hash values in this collection */
  protected WatersLongHashingStrategy _hashingStrategy;

  /**
   * Creates a new <code>TIntHash</code> instance with the default
   * capacity and load factor.
   */
  public WatersLongHash() {
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
  public WatersLongHash(final int initialCapacity) {
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
  public WatersLongHash(final int initialCapacity, final float loadFactor) {
    super(initialCapacity, loadFactor);
    this._hashingStrategy = this;
  }

  /**
   * Creates a new <code>WatersIntHash</code> instance with the default
   * capacity and load factor.
   * @param strategy used to compute hash codes and to compare keys.
   */
  public WatersLongHash(final WatersLongHashingStrategy strategy) {
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
  public WatersLongHash(final int initialCapacity,
                        final WatersLongHashingStrategy strategy)
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
  public WatersLongHash(final int initialCapacity,
                        final float loadFactor,
                        final WatersLongHashingStrategy strategy)
  {
    super(initialCapacity, loadFactor);
    this._hashingStrategy = strategy;
  }

  /**
   * @return a deep clone of this collection
   */
  public Object clone() {
    final WatersLongHash h = (WatersLongHash)super.clone();
    h._set = (long[])this._set.clone();
    return h;
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
    _set = new long[capacity];
    return capacity;
  }

  /**
   * Searches the set for <tt>val</tt>
   *
   * @param val an <code>int</code> value
   * @return a <code>boolean</code> value
   */
  public boolean contains(final long val)
  {
    return index(val) >= 0;
  }

  /**
   * Executes <tt>procedure</tt> for each element in the set.
   *
   * @param procedure a <code>TObjectProcedure</code> value
   * @return false if the loop over the set terminated because
   * the procedure returned false for some value.
   */
  public boolean forEach(final TLongProcedure procedure)
  {
    final byte[] states = _states;
    final long[] set = _set;
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
  protected void removeAt(final int index) {
    _set[index] = 0;
    super.removeAt(index);
  }

  /**
   * Locates the index of <tt>val</tt>.
   *
   * @param val a <code>long</code> value
   * @return the index of <tt>val</tt> or -1 if it isn't in the set.
   */
  protected int index(final long val)
  {
    int hash, probe, index, length;

    final byte[] states = _states;
    final long[] set = _set;
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
  protected int insertionIndex(final long val)
  {
    int hash, probe, index, length;
    final byte[] states = _states;
    final long[] set = _set;
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
  //# Interface net.sourceforge.waters.analysis.abstraction.
  //# WatersLongHashingStrategy
  /**
   * Default implementation of TLongHashingStrategy:
   * delegates hashing to HashFunctions.hash(long).
   */
  public final int computeHashCode(final long val)
  {
    return HashFunctions.hash(val);
  }

  /**
   * Default implementation of TIntHashingStrategy:
   * considers primitive values equal if they are the same number.
   */
  public boolean equals(final long val1, final long val2)
  {
    return val1 == val2;
  }


  //########################################################################
  //# Inner Class HashIterator
  final class HashIterator implements WatersLongIterator
  {
    //######################################################################
    //# Constructor
    HashIterator()
    {
      reset();
    }

    //######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.WatersLongIterator
    public void reset()
    {
      mIndex = -1;
    }

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

    public long getCurrentData()
    {
      return _set[mIndex];
    }

    public void remove()
    {
      removeAt(mIndex);
    }

    //######################################################################
    //# Data Members
    private int mIndex;
  }

}
