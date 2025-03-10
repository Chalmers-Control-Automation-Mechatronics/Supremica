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

import gnu.trove.procedure.TIntProcedure;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;


/**
 * An open addressed hash set implementation for <CODE>int</CODE> values.
 *
 * This is a modified version of GNU Trove 2 class
 * <CODE>gnu.trove.TIntHashSet</CODE> that supports configurable equality of
 * keys.
 *
 * @author Eric D. Friedman, Robi Malik
 */

public class WatersIntHashSet
  extends WatersIntHash
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new <code>WatersIntHashSet</code> instance with the default
   * capacity and load factor.
   */
  public WatersIntHashSet()
  {
    mDefaultValue = 0;
  }

  /**
   * Creates a new <code>WatersIntHashSet</code> instance with a prime
   * capacity equal to or greater than <tt>initialCapacity</tt> and with the
   * default load factor.
   * @param initialCapacity
   *          used to find a prime capacity for the table.
   */
  public WatersIntHashSet(final int initialCapacity)
  {
    super(initialCapacity);
    mDefaultValue = 0;
  }

  /**
   * Creates a new <code>WatersIntIntHashMap</code> instance with a prime
   * capacity equal to or greater than <tt>initialCapacity</tt> and with the
   * specified load factor.
   * @param initialCapacity
   *          used to find a prime capacity for the table.
   * @param defaultValue
   *          value to be returned when looking up a nonexistent value
   *          using {@link #get(int) get()}.
   */
  public WatersIntHashSet(final int initialCapacity, final int defaultValue)
  {
    super(initialCapacity);
    mDefaultValue = defaultValue;
  }

  /**
   * Creates a new <code>WatersIntHashSet</code> instance with a prime
   * capacity equal to or greater than <tt>initialCapacity</tt> and with the
   * specified load factor.
   * @param initialCapacity
   *          used to find a prime capacity for the table.
   * @param loadFactor
   *          used to calculate the threshold over which rehashing takes place.
   */
  public WatersIntHashSet(final int initialCapacity, final float loadFactor)
  {
    super(initialCapacity, loadFactor);
    mDefaultValue = 0;
  }

  /**
   * Creates a new <code>WatersIntHashSet</code> instance with a prime
   * capacity equal to or greater than <tt>initialCapacity</tt> and with the
   * specified load factor.
   * @param initialCapacity
   *          used to find a prime capacity for the table.
   * @param defaultValue
   *          value to be returned when looking up a nonexistent value
   *          using {@link #get(int) get()}.
   * @param loadFactor
   *          used to calculate the threshold over which rehashing takes place.
   */
  public WatersIntHashSet(final int initialCapacity,
                          final int defaultValue,
                          final float loadFactor)
  {
    super(initialCapacity, loadFactor);
    mDefaultValue = defaultValue;
  }

  /**
   * Creates a new <code>WatersIntIntHashMap</code> instance with the default
   * capacity and load factor.
   * @param strategy
   *          used to compute hash codes and to compare keys.
   */
  public WatersIntHashSet(final WatersIntHashingStrategy strategy)
  {
    super(strategy);
    mDefaultValue = 0;
  }

  /**
   * Creates a new <code>WatersIntIntHashMap</code> instance whose capacity is
   * the next highest prime above <tt>initialCapacity + 1</tt> unless that value
   * is already prime.
   * @param initialCapacity
   *          used to find a prime capacity for the table.
   * @param strategy
   *          used to compute hash codes and to compare keys.
   */
  public WatersIntHashSet(final int initialCapacity,
                          final WatersIntHashingStrategy strategy)
  {
    super(initialCapacity, strategy);
    mDefaultValue = 0;
  }

  /**
   * Creates a new <code>WatersIntIntHashMap</code> instance whose capacity is
   * the next highest prime above <tt>initialCapacity + 1</tt> unless that value
   * is already prime.
   * @param initialCapacity
   *          used to find a prime capacity for the table.
   * @param defaultValue
   *          value to be returned when looking up a nonexistent value
   *          using {@link #get(int) get()}.
   * @param strategy
   *          used to compute hash codes and to compare keys.
   */
  public WatersIntHashSet(final int initialCapacity,
                          final int defaultValue,
                          final WatersIntHashingStrategy strategy)
  {
    super(initialCapacity, strategy);
    mDefaultValue = defaultValue;
  }

  /**
   * Creates a new <code>WatersIntIntHashMap</code> instance with a prime value
   * at or near the specified capacity and load factor.
   * @param initialCapacity
   *          used to find a prime capacity for the table.
   * @param loadFactor
   *          used to calculate the threshold over which rehashing takes place.
   * @param strategy
   *          used to compute hash codes and to compare keys.
   */
  public WatersIntHashSet(final int initialCapacity,
                          final float loadFactor,
                          final WatersIntHashingStrategy strategy)
  {
    super(initialCapacity, loadFactor, strategy);
    mDefaultValue = 0;
  }

  /**
   * Creates a new <code>WatersIntIntHashMap</code> instance with a prime value
   * at or near the specified capacity and load factor.
   * @param initialCapacity
   *          used to find a prime capacity for the table.
   * @param defaultValue
   *          value to be returned when looking up a nonexistent value
   *          using {@link #get(int) get()}.
   * @param loadFactor
   *          used to calculate the threshold over which rehashing takes place.
   * @param strategy
   *          used to compute hash codes and to compare keys.
   */
  public WatersIntHashSet(final int initialCapacity,
                          final int defaultValue,
                          final float loadFactor,
                          final WatersIntHashingStrategy strategy)
  {
    super(initialCapacity, loadFactor, strategy);
    mDefaultValue = defaultValue;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public boolean equals(final Object other)
  {
    if (other.getClass() == getClass()) {
      final WatersIntHashSet that = (WatersIntHashSet) other;
      if (that.size() != size()) {
        return false;
      }
      final EqProcedure proc = that.new EqProcedure();
      return forEach(proc);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode()
  {
    final HashProcedure proc = new HashProcedure();
    forEach(proc);
    return proc.getHashCode();
  }

  @Override
  public String toString()
  {
    final StringWriter writer = new StringWriter();
    final PrintWriter printer = new PrintWriter(writer);
    final PrintProcedure proc = new PrintProcedure(printer);
    printer.print('{');
    forEach(proc);
    printer.print('}');
    return writer.toString();
  }


  //#########################################################################
  //# Overrides for gnu.trove.THash
  @Override
  protected void rehash(final int newCapacity)
  {
    final int oldCapacity = _set.length;
    final int oldKeys[] = _set;
    final byte oldStates[] = _states;
    _set = new int[newCapacity];
    _states = new byte[newCapacity];
    for (int i = oldCapacity; i-- > 0;) {
      if (oldStates[i] == FULL) {
        final int o = oldKeys[i];
        final int index = insertionIndex(o);
        _set[index] = o;
        _states[index] = FULL;
      }
    }
  }

  @Override
  public void clear()
  {
    super.clear();
    Arrays.fill(_states, 0, _states.length, FREE);
  }


  //#########################################################################
  //# Set Access
  /**
   * Adds the given value to this set.
   * @return <CODE>true</CODE> if the value was not yet present and added;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean add(final int value)
  {
    final int index = insertionIndex(value);
    if (index >= 0) {
      final byte previousState = _states[index];
      _set[index] = value;
      _states[index] = FULL;
      postInsertHook(previousState == FREE);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Adds all of the elements in the given array from this set.
   * @return <CODE>true</CODE> if at least one value was not yet present and
   *         added; <CODE>false</CODE> otherwise.
   */
  public boolean addAll(final int[] array)
  {
    boolean result = false;
    for (final int value : array) {
      result |= add(value);
    }
    return result;
  }

  /**
   * Retrieves the entry equal to value in this hash set, if present,
   * or the set's default value if not present.
   */
  public int get(final int value)
  {
    final int index = index(value);
    return index < 0 ? mDefaultValue : _set[index];
  }

  /**
   * Attempts to add the given value to this set.
   * @return The number equal to the given value now present in the set.
   *         If the set already contained a number considered equal to the
   *         value before the call, that number is returned. Otherwise, the
   *         new value is added and returned.
   */
  public int getOrAdd(final int value)
  {
    final int index = insertionIndex(value);
    if (index >= 0) {
      final byte previousState = _states[index];
      _set[index] = value;
      _states[index] = FULL;
      postInsertHook(previousState == FREE);
      return value;
    } else {
      return _set[-index - 1];
    }
  }

  /**
   * Returns an iterator over the <CODE>int</CODE> values in this hash set.
   * The returned iterator produces the elements in the hash set in no
   * defined order. It implements the {@link WatersIntIterator#remove()}
   * operation, but auto-compaction needs to be disabled before using it.
   * @see #tempDisableAutoCompaction()
   * @see #reenableAutoCompaction(boolean) reenableAutoCompaction()
   */
  public WatersIntIterator iterator()
  {
    return new HashIterator();
  }

  /**
   * Deletes the given value from this set.
   * @return <CODE>true</CODE> if the value was found and removed,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean remove(final int value)
  {
    final int index = index(value);
    if (index >= 0) {
      removeAt(index); // clear key, state; adjust size
      return true;
    } else {
      return false;
    }
  }

  /**
   * Removes all of the elements in the given array from this set.
   * @return <CODE>true</CODE> if at least one value was found and removed;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean removeAll(final int[] array)
  {
    tempDisableAutoCompaction();
    boolean result = false;
    for (final int value : array) {
      result |= remove(value);
    }
    reenableAutoCompaction(result);
    return result;
  }

  /**
   * Removes any values from this set which are not contained in the given
   * array.
   */
  public boolean retainAll(final int[] array)
  {
    final WatersIntHashSet keep =
      new WatersIntHashSet(array.length, _hashingStrategy);
    keep.addAll(array);
    tempDisableAutoCompaction();
    boolean result = false;
    for (int i = 0; i < _states.length; i++) {
      if (_states[i] == FULL && !keep.contains(_set[i])) {
        removeAt(i);
        result = true;
      }
    }
    reenableAutoCompaction(result);
    return result;
  }

  /**
   * Returns a new array containing the values in the set.
   */
  public int[] toArray()
  {
    final int[] result = new int[size()];
    final byte[] states = _states;
    for (int i = states.length, j = 0; i-- > 0;) {
      if (states[i] == FULL) {
        result[j++] = _set[i];
      }
    }
    return result;
  }


  //########################################################################
  //# Inner Class EqProcedure
  private final class EqProcedure implements TIntProcedure
  {
    //######################################################################
    //# Interface gnu.trove.TIntProcedure
    @Override
    public final boolean execute(final int value)
    {
      return contains(value);
    }
  }


  //########################################################################
  //# Inner Class HashProcedure
  private final class HashProcedure implements TIntProcedure
  {
    //######################################################################
    //# Simple Access
    public int getHashCode()
    {
      return mResult;
    }

    //######################################################################
    //# Interface gnu.trove.TIntProcedure
    @Override
    public final boolean execute(final int value)
    {
      mResult += _hashingStrategy.computeHashCode(value);
      return true;
    }

    //######################################################################
    //# Data Members
    private int mResult = 0;
  }


  //########################################################################
  //# Inner Class PrintProcedure
  private static final class PrintProcedure implements TIntProcedure
  {
    //######################################################################
    //# Constructor
    private PrintProcedure(final PrintWriter printer)
    {
      mPrinter = printer;
      mFirst = true;
    }

    //######################################################################
    //# Interface gnu.trove.TIntProcedure
    @Override
    public final boolean execute(final int value)
    {
      if (mFirst) {
        mFirst = false;
      } else {
        mPrinter.print(',');
      }
      mPrinter.print(value);
      return true;
    }

    //######################################################################
    //# Data Members
    private final PrintWriter mPrinter;
    private boolean mFirst;
  }


  //########################################################################
  //# Data Members
  /**
   * The default value for nonexistent values.
   */
  private final int mDefaultValue;


  //########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
