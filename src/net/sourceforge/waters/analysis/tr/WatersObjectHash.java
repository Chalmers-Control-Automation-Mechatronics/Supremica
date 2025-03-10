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

import gnu.trove.impl.hash.THash;
import gnu.trove.impl.hash.TObjectHash;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.strategy.HashingStrategy;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import net.sourceforge.waters.model.base.WatersRuntimeException;


/**
 * A modified version of GNU Trove's {@link TObjectHash} implementation to
 * allow hash sets to be created with a configurable hashing strategy.
 * Most of this code is taken from GNU Trove&nbsp;2.
 *
 * @author Eric D. Friedman, Robi Malik
 */

@SuppressWarnings("unchecked")
abstract public class WatersObjectHash<T>
  extends THash
  implements HashingStrategy<T>
{

  static final long serialVersionUID = -3461112548087185871L;

  /** the set of Objects */
  protected transient Object[] _set;

  /** the strategy used to hash objects in this collection. */
  protected HashingStrategy<? super T> _hashingStrategy;

  protected static final Object REMOVED = new Object(), FREE = new Object();

  /**
   * Creates a new <code>WatersObjectHash</code> instance with the default
   * capacity and load factor.
   */
  public WatersObjectHash()
  {
    super();
    this._hashingStrategy = this;
  }

  /**
   * Creates a new <code>WatersObjectHash</code> instance with the default capacity
   * and load factor and a custom hashing strategy.
   * @param strategy
   *          used to compute hash codes and to compare objects.
   */
  public WatersObjectHash(final HashingStrategy<? super T> strategy)
  {
    this._hashingStrategy = strategy;
  }

  /**
   * Creates a new <code>WatersObjectHash</code> instance whose capacity is
   * the next highest prime above <tt>initialCapacity + 1</tt> unless that
   * value is already prime.
   * @param initialCapacity
   *          an <code>int</code> value
   */
  public WatersObjectHash(final int initialCapacity)
  {
    super(initialCapacity);
    this._hashingStrategy = this;
  }

  /**
   * Creates a new <code>WatersObjectHash</code> instance whose capacity is
   * the next highest prime above <tt>initialCapacity + 1</tt> unless that
   * value is already prime. Uses the specified custom hashing strategy.
   * @param initialCapacity
   *          an <code>int</code> value
   * @param strategy
   *          used to compute hash codes and to compare objects.
   */
  public WatersObjectHash(final int initialCapacity,
                          final HashingStrategy<? super T> strategy)
  {
    super(initialCapacity);
    this._hashingStrategy = strategy;
  }

  /**
   * Creates a new <code>WatersObjectHash</code> instance with a prime value
   * at or near the specified capacity and load factor.
   * @param initialCapacity
   *          used to find a prime capacity for the table.
   * @param loadFactor
   *          used to calculate the threshold over which rehashing takes
   *          place.
   */
  public WatersObjectHash(final int initialCapacity, final float loadFactor)
  {
    super(initialCapacity, loadFactor);
    this._hashingStrategy = this;
  }

  /**
   * Creates a new <code>WatersObjectHash</code> instance with a prime value
   * at or near the specified capacity and load factor. Uses the specified
   * custom hashing strategy.
   * @param initialCapacity
   *          used to find a prime capacity for the table.
   * @param loadFactor
   *          used to calculate the threshold over which rehashing takes
   *          place.
   * @param strategy
   *          used to compute hash codes and to compare objects.
   */
  public WatersObjectHash(final int initialCapacity,
                          final float loadFactor,
                          final HashingStrategy<T> strategy)
  {
    super(initialCapacity, loadFactor);
    this._hashingStrategy = strategy;
  }

  /**
   * @return a shallow clone of this collection
   */
  @Override
  public WatersObjectHash<T> clone()
  {
    WatersObjectHash<T> h;
    try {
      h = (WatersObjectHash<T>) super.clone();
      h._set = this._set.clone();
      return h;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  @Override
  public int capacity()
  {
    return _set.length;
  }

  @Override
  protected void removeAt(final int index)
  {
    _set[index] = REMOVED;
    super.removeAt(index);
  }

  /**
   * Initialises the Object set of this hash table.
   *
   * @param initialCapacity
   *          an <code>int</code> value
   * @return an <code>int</code> value
   */
  @Override
  protected int setUp(final int initialCapacity)
  {
    int capacity;

    capacity = super.setUp(initialCapacity);
    _set = new Object[capacity];
    Arrays.fill(_set, FREE);
    return capacity;
  }

  /**
   * Executes <tt>procedure</tt> for each element in the set.
   *
   * @param procedure
   *          a <code>TObjectProcedure</code> value
   * @return false if the loop over the set terminated because the procedure
   *         returned false for some value.
   */
  public <X> boolean forEach(final TObjectProcedure<X> procedure)
  {
    final Object[] set = _set;
    for (int i = set.length; i-- > 0;) {
      if (set[i] != FREE && set[i] != REMOVED
          && !procedure.execute((X) set[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Searches the set for <tt>obj</tt>
   *
   * @param obj
   *          an <code>Object</code> value
   * @return a <code>boolean</code> value
   */
  public boolean contains(final Object obj)
  {
    return index((T) obj) >= 0;
  }

  public T getExisting(final T obj)
  {
    final int index = index(obj);
    return (T) (index >= 0 ? _set[index] : null);
  }

  /**
   * Locates the index of <tt>obj</tt>.
   *
   * @param obj
   *          an <code>Object</code> value
   * @return the index of <tt>obj</tt> or -1 if it isn't in the set.
   */
  protected int index(final T obj)
  {
    final HashingStrategy<? super T> hashing_strategy = _hashingStrategy;

    final Object[] set = _set;
    final int length = set.length;
    final int hash = hashing_strategy.computeHashCode(obj) & 0x7fffffff;
    int index = hash % length;
    Object cur = set[index];

    if (cur == FREE)
      return -1;

    // NOTE: here it has to be REMOVED or FULL (some user-given value)
    if (cur == REMOVED || !hashing_strategy.equals((T) cur, obj)) {
      // see Knuth, p. 529
      final int probe = 1 + (hash % (length - 2));

      do {
        index -= probe;
        if (index < 0) {
          index += length;
        }
        cur = set[index];
      } while (cur != FREE
               && (cur == REMOVED || !_hashingStrategy.equals((T) cur, obj)));
    }

    return cur == FREE ? -1 : index;
  }

  /**
   * Locates the index at which <tt>obj</tt> can be inserted. if there is
   * already a value equal()ing <tt>obj</tt> in the set, returns that value's
   * index as <tt>-index - 1</tt>.
   *
   * @param obj
   *          an <code>Object</code> value
   * @return the index of a FREE slot at which obj can be inserted or, if obj
   *         is already stored in the hash, the negative value of that index,
   *         minus 1: -index -1.
   */
  protected int insertionIndex(final T obj)
  {
    final HashingStrategy<? super T> hashing_strategy = _hashingStrategy;

    final Object[] set = _set;
    final int length = set.length;
    final int hash = hashing_strategy.computeHashCode(obj) & 0x7fffffff;
    int index = hash % length;
    Object cur = set[index];

    if (cur == FREE) {
      return index; // empty, all done
    } else if (cur != REMOVED && hashing_strategy.equals((T) cur, obj)) {
      return -index - 1; // already stored
    } else { // already FULL or REMOVED, must probe
      // compute the double hash
      final int probe = 1 + (hash % (length - 2));

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
      if (cur != REMOVED) {
        // starting at the natural offset, probe until we find an
        // offset that isn't full.
        do {
          index -= probe;
          if (index < 0) {
            index += length;
          }
          cur = set[index];
        } while (cur != FREE && cur != REMOVED
                 && !hashing_strategy.equals((T) cur, obj));
      }

      // if the index we found was removed: continue probing until we
      // locate a free location or an element which equal()s the
      // one we have.
      if (cur == REMOVED) {
        final int firstRemoved = index;
        while (cur != FREE
               && (cur == REMOVED || !hashing_strategy.equals((T) cur, obj))) {
          index -= probe;
          if (index < 0) {
            index += length;
          }
          cur = set[index];
        }
        // NOTE: cur cannot == REMOVED in this block
        return (cur != FREE) ? -index - 1 : firstRemoved;
      }
      // if it's full, the key is already stored
      // NOTE: cur cannot equal REMOVE here (would have retuned already (see above)
      return (cur != FREE) ? -index - 1 : index;
    }
  }

  /**
   * This is the default implementation of HashingStrategy: it delegates
   * hashing to the Object's hashCode method.
   *
   * @param o
   *          for which the hashcode is to be computed
   * @return the hashCode
   * @see Object#hashCode()
   */
  @Override
  public final int computeHashCode(final T o)
  {
    return o == null ? 0 : o.hashCode();
  }

  /**
   * This is the default implementation of HashingStrategy: it delegates
   * equality comparisons to the first parameter's equals() method.
   *
   * @param o1
   *          an <code>Object</code> value
   * @param o2
   *          an <code>Object</code> value
   * @return true if the objects are equal
   * @see Object#equals(Object)
   */
  @Override
  public final boolean equals(final T o1, final T o2)
  {
    return o1 == null ? o2 == null : o1.equals(o2);
  }

  /**
   * Convenience methods for subclasses to use in throwing exceptions about
   * badly behaved user objects employed as keys. We have to throw an
   * IllegalArgumentException with a rather verbose message telling the user
   * that they need to fix their object implementation to conform to the
   * general contract for java.lang.Object.
   *
   * @param o1
   *          the first of the equal elements with unequal hash codes.
   * @param o2
   *          the second of the equal elements with unequal hash codes.
   * @exception IllegalArgumentException
   *              the whole point of this method.
   */
  protected final void throwObjectContractViolation(final Object o1, final Object o2)
    throws IllegalArgumentException
  {
    throw new IllegalArgumentException(
                                       "Equal objects must have equal hashcodes. "
                                         + "During rehashing, Trove discovered that "
                                         + "the following two objects claim to be "
                                         + "equal (as in java.lang.Object.equals()) "
                                         + "but their hashCodes (or those calculated by "
                                         + "your HashingStrategy) are not equal."
                                         + "This violates the general contract of "
                                         + "java.lang.Object.hashCode().  See bullet point two "
                                         + "in that method's documentation. "
                                         + "object #1 =" + o1
                                         + "; object #2 =" + o2);
  }

  @Override
  public void writeExternal(final ObjectOutput out) throws IOException
  {
    super.writeExternal(out);

    // VERSION
    out.writeByte(0);

    // HASHING STRATEGY
    out.writeObject(_hashingStrategy);
  }

  @Override
  public void readExternal(final ObjectInput in) throws IOException,
    ClassNotFoundException
  {

    super.readExternal(in);

    // VERSION
    in.readByte();

    // HASHING STRATEGY
    //noinspection unchecked
    _hashingStrategy = (HashingStrategy<T>) in.readObject();
  }
} // TObjectHash
