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

import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.procedure.array.ToObjectArrayProceedure;
import gnu.trove.set.hash.THashSet;
import gnu.trove.strategy.HashingStrategy;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;


/**
 * A modified version of GNU Trove's {@link THashSet} implementation to allow
 * hash sets to be created with a configurable hashing strategy. Most of this
 * code is taken from GNU Trove&nbsp;2.
 *
 * @author Eric D. Friedman, Robi Malik
 */

public class WatersHashSet<E>
  extends WatersObjectHash<E> implements Set<E>, Iterable<E>
{

  /**
   * Creates a new <code>WatersHashSet</code> instance with the default
   * capacity and load factor.
   */
  public WatersHashSet()
  {
  }

  /**
   * Creates a new <code>WatersHashSet</code> instance with the default
   * capacity and load factor.
   * @param strategy
   *          used to compute hash codes and to compare objects.
   */
  public WatersHashSet(final HashingStrategy<? super E> strategy)
  {
    super(strategy);
  }

  /**
   * Creates a new <code>WatersHashSet</code> instance with a prime capacity
   * equal to or greater than <tt>initialCapacity</tt> and with the default
   * load factor.
   * @param initialCapacity
   *          an <code>int</code> value
   */
  public WatersHashSet(final int initialCapacity)
  {
    super(initialCapacity);
  }

  /**
   * Creates a new <code>WatersHashSet</code> instance with a prime capacity
   * equal to or greater than <tt>initialCapacity</tt> and with the default
   * load factor.
   * @param initialCapacity
   *          an <code>int</code> value
   * @param strategy
   *          used to compute hash codes and to compare objects.
   */
  public WatersHashSet(final int initialCapacity,
                       final HashingStrategy<? super E> strategy)
  {
    super(initialCapacity, strategy);
  }

  /**
   * Creates a new <code>WatersHashSet</code> instance with a prime capacity
   * equal to or greater than <tt>initialCapacity</tt> and with the specified
   * load factor.
   * @param initialCapacity
   *          an <code>int</code> value
   * @param loadFactor
   *          a <code>float</code> value
   */
  public WatersHashSet(final int initialCapacity, final float loadFactor)
  {
    super(initialCapacity, loadFactor);
  }

  /**
   * Creates a new <code>WatersHashSet</code> instance with a prime capacity
   * equal to or greater than <tt>initialCapacity</tt> and with the specified
   * load factor.
   * @param initialCapacity
   *          an <code>int</code> value
   * @param loadFactor
   *          a <code>float</code> value
   * @param strategy
   *          used to compute hash codes and to compare objects.
   */
  public WatersHashSet(final int initialCapacity,
                       final float loadFactor,
                       final HashingStrategy<E> strategy)
  {
    super(initialCapacity, loadFactor, strategy);
  }

  /**
   * Creates a new <code>WatersHashSet</code> instance containing the elements
   * of <tt>collection</tt>.
   * @param collection
   *          a <code>Collection</code> value
   */
  public WatersHashSet(final Collection<? extends E> collection)
  {
    this(collection.size());
    addAll(collection);
  }

  /**
   * Creates a new <code>WatersHashSet</code> instance containing the elements
   * of <tt>collection</tt>.
   * @param collection
   *          a <code>Collection</code> value
   * @param strategy
   *          used to compute hash codes and to compare objects.
   */
  public WatersHashSet(final Collection<? extends E> collection,
                       final HashingStrategy<E> strategy)
  {
    this(collection.size(), strategy);
    addAll(collection);
  }

  /**
   * Inserts a value into the set.
   *
   * @param obj
   *          an <code>Object</code> value
   * @return true if the set was modified by the add operation
   */
  @Override
  public boolean add(final E obj)
  {
    final int index = insertionIndex(obj);

    if (index < 0) {
      return false; // already present in set, nothing to add
    }

    final Object old = _set[index];
    _set[index] = obj;

    postInsertHook(old == FREE);
    return true; // yes, we added something
  }

  @Override
  public boolean equals(final Object other)
  {
    if (!(other instanceof Set)) {
      return false;
    }
    final Set<?> that = (Set<?>) other;
    if (that.size() != this.size()) {
      return false;
    }
    return containsAll(that);
  }

  @Override
  public int hashCode()
  {
    final HashProcedure p = new HashProcedure();
    forEach(p);
    return p.getHashCode();
  }


  private final class HashProcedure implements TObjectProcedure<E>
  {
    private int h = 0;

    public int getHashCode()
    {
      return h;
    }

    @Override
    public final boolean execute(final E key)
    {
      h += _hashingStrategy.computeHashCode(key);
      return true;
    }
  }

  /**
   * Expands the set to accommodate new values.
   *
   * @param newCapacity
   *          an <code>int</code> value
   */
  @Override
  protected void rehash(final int newCapacity)
  {
    final int oldCapacity = _set.length;
    final Object oldSet[] = _set;

    _set = new Object[newCapacity];
    Arrays.fill(_set, FREE);

    for (int i = oldCapacity; i-- > 0;) {
      if (oldSet[i] != FREE && oldSet[i] != REMOVED) {
        @SuppressWarnings("unchecked")
        final E o = (E) oldSet[i];
        final int index = insertionIndex(o);
        if (index < 0) { // everyone pays for this because some people can't RTFM
          throwObjectContractViolation(_set[(-index - 1)], o);
        }
        _set[index] = o;
      }
    }
  }

  /**
   * Returns a new array containing the objects in the set.
   *
   * @return an <code>Object[]</code> value
   */
  @Override
  public Object[] toArray()
  {
    @SuppressWarnings("unchecked")
    final E[] result = (E[]) new Object[size()];
    forEach(new ToObjectArrayProceedure<E>(result));
    return result;
  }

  /**
   * Returns a typed array of the objects in the set.
   *
   * @param a
   *          an <code>Object[]</code> value
   * @return an <code>Object[]</code> value
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] a)
  {
    final int size = size();
    if (a.length < size) {
      a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
    }
    forEach(new ToObjectArrayProceedure<T>(a));

    // If this collection fits in the specified array with room to
    // spare (i.e., the array has more elements than this
    // collection), the element in the array immediately following
    // the end of the collection is set to null. This is useful in
    // determining the length of this collection only if the
    // caller knows that this collection does not contain any null
    // elements.)

    if (a.length > size) {
      a[size] = null;
    }

    return a;
  }

  /**
   * Empties the set.
   */
  @Override
  public void clear()
  {
    super.clear();

    Arrays.fill(_set, 0, _set.length, FREE);
  }

  /**
   * Removes <tt>obj</tt> from the set.
   *
   * @param obj
   *          an <code>Object</code> value
   * @return true if the set was modified by the remove operation.
   */
  @Override
  public boolean remove(final Object obj)
  {
    @SuppressWarnings("unchecked")
    final int index = index((E) obj);
    if (index >= 0) {
      removeAt(index);
      return true;
    }
    return false;
  }

  /**
   * Creates an iterator over the values of the set. The iterator supports
   * element deletion.
   *
   * @return an <code>Iterator</code> value
   */
  @Override
  public Iterator<E> iterator()
  {
    return new WatersHashIterator<E>(this);
  }

  /**
   * Tests the set to determine if all of the elements in <tt>collection</tt>
   * are present.
   *
   * @param collection
   *          a <code>Collection</code> value
   * @return true if all elements were present in the set.
   */
  @Override
  public boolean containsAll(final Collection<?> collection)
  {
    for (final Iterator<?> i = collection.iterator(); i.hasNext();) {
      if (!contains(i.next())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Adds all of the elements in <tt>collection</tt> to the set.
   *
   * @param collection
   *          a <code>Collection</code> value
   * @return true if the set was modified by the add all operation.
   */
  @Override
  public boolean addAll(final Collection<? extends E> collection)
  {
    boolean changed = false;
    int size = collection.size();

    ensureCapacity(size);
    final Iterator<? extends E> it = collection.iterator();
    while (size-- > 0) {
      if (add(it.next())) {
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Removes all of the elements in <tt>collection</tt> from the set.
   *
   * @param collection
   *          a <code>Collection</code> value
   * @return true if the set was modified by the remove all operation.
   */
  @Override
  public boolean removeAll(final Collection<?> collection)
  {
    boolean changed = false;
    int size = collection.size();
    final Iterator<?> it = collection.iterator();
    while (size-- > 0) {
      if (remove(it.next())) {
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Removes any values in the set which are not contained in
   * <tt>collection</tt>.
   *
   * @param collection
   *          a <code>Collection</code> value
   * @return true if the set was modified by the retain all operation
   */
  @Override
  public boolean retainAll(final Collection<?> collection)
  {
    boolean changed = false;
    int size = size();
    final Iterator<E> it = iterator();
    while (size-- > 0) {
      if (!collection.contains(it.next())) {
        it.remove();
        changed = true;
      }
    }
    return changed;
  }

  @Override
  public String toString()
  {
    final StringBuilder buf = new StringBuilder("{");
    forEach(new TObjectProcedure<E>() {
      private boolean first = true;

      @Override
      public boolean execute(final Object value)
      {
        if (first)
          first = false;
        else
          buf.append(",");

        buf.append(value);
        return true;
      }
    });
    buf.append("}");
    return buf.toString();
  }

}
