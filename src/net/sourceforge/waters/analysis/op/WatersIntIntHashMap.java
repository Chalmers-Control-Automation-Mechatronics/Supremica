//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   WatersIntIntHashMap
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.HashFunctions;
import gnu.trove.TIntFunction;
import gnu.trove.TIntIntProcedure;
import gnu.trove.TIntProcedure;

import java.lang.reflect.Array;
import java.util.Arrays;


/**
 * An open addressed map implementation for int keys and int values.
 *
 * This is a modified version of {@link gnu.trove.TIntIntHashMap} that supports
 * configurable equality of keys.
 *
 * @author Eric D. Friedman, Robi Malik
 */

public class WatersIntIntHashMap
  extends WatersIntHash
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new <code>WatersIntIntHashMap</code> instance with the default
   * capacity and load factor.
   */
  public WatersIntIntHashMap()
  {
    super();
    mDefaultValue = 0;
  }

  /**
   * Creates a new <code>WatersIntIntHashMap</code> instance with a prime
   * capacity equal to or greater than <tt>initialCapacity</tt> and with the
   * default load factor.
   * @param initialCapacity
   *          used to find a prime capacity for the table.
   */
  public WatersIntIntHashMap(final int initialCapacity)
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
   *          value to be returned when looking up a nonexistent key.
   */
  public WatersIntIntHashMap(final int initialCapacity, final int defaultValue)
  {
    super(initialCapacity);
    mDefaultValue = defaultValue;
  }

  /**
   * Creates a new <code>WatersIntIntHashMap</code> instance with a prime
   * capacity equal to or greater than <tt>initialCapacity</tt> and with the
   * specified load factor.
   * @param initialCapacity
   *          used to find a prime capacity for the table.
   * @param defaultValue
   *          value to be returned when looking up a nonexistent key.
   */
  public WatersIntIntHashMap(final int initialCapacity, final int defaultValue,
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
  public WatersIntIntHashMap(final WatersIntHashingStrategy strategy)
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
  public WatersIntIntHashMap(final int initialCapacity,
                             final WatersIntHashingStrategy strategy)
  {
    super(initialCapacity, strategy);
    mDefaultValue = 0;
  }

  /**
   * Creates a new <code>WatersIntIntHashMap</code> instance with a prime value
   * at or near the specified capacity and load factor.
   * @param initialCapacity
   *          used to find a prime capacity for the table.
   * @param defaultValue
   *          value to be returned when looking up a nonexistent key.
   * @param strategy
   *          used to compute hash codes and to compare keys.
   */
  public WatersIntIntHashMap(final int initialCapacity, final int defaultValue,
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
   * @param defaultValue
   *          value to be returned when looking up a nonexistent key.
   * @param loadFactor
   *          used to calculate the threshold over which rehashing takes place.
   * @param strategy
   *          used to compute hash codes and to compare keys.
   */
  public WatersIntIntHashMap(final int initialCapacity, final int defaultValue,
                             final float loadFactor,
                             final WatersIntHashingStrategy strategy)
  {
    super(initialCapacity, loadFactor, strategy);
    mDefaultValue = defaultValue;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  /**
   * @return a deep clone of this collection
   */
  public Object clone()
  {
    final WatersIntIntHashMap m = (WatersIntIntHashMap) super.clone();
    m.mValues = (int[]) this.mValues.clone();
    return m;
  }

  /**
   * Initialises the hash table to a prime capacity which is at least
   * <tt>initialCapacity + 1</tt>.
   *
   * @param initialCapacity
   *          an <code>int</code> value
   * @return the actual capacity chosen
   */
  protected int setUp(final int initialCapacity)
  {
    int capacity;

    capacity = super.setUp(initialCapacity);
    mValues = new int[capacity];
    return capacity;
  }

  /**
   * Inserts a key/value pair into the map.
   *
   * @param key
   *          an <code>int</code> value
   * @param value
   *          an <code>int</code> value
   * @return the previous value associated with <tt>key</tt>, or (int)0 if none
   *         was found.
   */
  public int put(final int key, final int value)
  {
    final int index = insertionIndex(key);
    return doPut(key, value, index);
  }

  /**
   * Inserts a key/value pair into the map if the specified key is not already
   * associated with a value.
   *
   * @param key
   *          an <code>int</code> value
   * @param value
   *          an <code>int</code> value
   * @return the previous value associated with <tt>key</tt>, or (int)0 if none
   *         was found.
   */
  public int putIfAbsent(final int key, final int value)
  {
    final int index = insertionIndex(key);
    if (index < 0)
      return mValues[-index - 1];
    return doPut(key, value, index);
  }

  private int doPut(final int key, final int value, int index)
  {
    byte previousState;
    int previous = (int) 0;
    boolean isNewMapping = true;
    if (index < 0) {
      index = -index - 1;
      previous = mValues[index];
      isNewMapping = false;
    }
    previousState = _states[index];
    _set[index] = key;
    _states[index] = FULL;
    mValues[index] = value;
    if (isNewMapping) {
      postInsertHook(previousState == FREE);
    }

    return previous;
  }

  /**
   * Put all the entries from the given map into this map.
   *
   * @param map
   *          The map from which entries will be obtained to put into this map.
   */
  public void putAll(final WatersIntIntHashMap map)
  {
    map.forEachEntry(PUT_ALL_PROC);
  }

  /**
   * rehashes the map to the new capacity.
   *
   * @param newCapacity
   *          an <code>int</code> value
   */
  protected void rehash(final int newCapacity)
  {
    final int oldCapacity = _set.length;
    final int oldKeys[] = _set;
    final int oldVals[] = mValues;
    final byte oldStates[] = _states;

    _set = new int[newCapacity];
    mValues = new int[newCapacity];
    _states = new byte[newCapacity];

    for (int i = oldCapacity; i-- > 0;) {
      if (oldStates[i] == FULL) {
        final int o = oldKeys[i];
        final int index = insertionIndex(o);
        _set[index] = o;
        mValues[index] = oldVals[i];
        _states[index] = FULL;
      }
    }
  }

  /**
   * retrieves the value for <tt>key</tt>
   *
   * @param key
   *          an <code>int</code> value
   * @return the value of <tt>key</tt> or (int)0 if no such mapping exists.
   */
  public int get(final int key)
  {
    final int index = index(key);
    return index < 0 ? mDefaultValue : mValues[index];
  }

  /**
   * Empties the map.
   *
   */
  public void clear()
  {
    super.clear();
    Arrays.fill(_set, 0, _set.length, (int) 0);
    Arrays.fill(mValues, 0, mValues.length, (int) 0);
    Arrays.fill(_states, 0, _states.length, FREE);
  }

  /**
   * Deletes a key/value pair from the map.
   *
   * @param key
   *          an <code>int</code> value
   * @return an <code>int</code> value, or (int)0 if no mapping for key exists
   */
  public int remove(final int key)
  {
    int prev = (int) 0;
    final int index = index(key);
    if (index >= 0) {
      prev = mValues[index];
      removeAt(index); // clear key,state; adjust size
    }
    return prev;
  }

  /**
   * Compares this map with another map for equality of their stored entries.
   *
   * @param other
   *          an <code>Object</code> value
   * @return a <code>boolean</code> value
   */
  public boolean equals(final Object other)
  {
    if (!(other instanceof WatersIntIntHashMap)) {
      return false;
    }
    final WatersIntIntHashMap that = (WatersIntIntHashMap) other;
    if (that.size() != this.size()) {
      return false;
    }
    return forEachEntry(new EqProcedure(that));
  }

  public int hashCode()
  {
    final HashProcedure p = new HashProcedure();
    forEachEntry(p);
    return p.getHashCode();
  }


  private final class HashProcedure implements TIntIntProcedure
  {
    private int h = 0;

    public int getHashCode()
    {
      return h;
    }

    public final boolean execute(final int key, final int value)
    {
      h += (_hashingStrategy.computeHashCode(key) ^ HashFunctions.hash(value));
      return true;
    }
  }


  private static final class EqProcedure implements TIntIntProcedure
  {
    private final WatersIntIntHashMap _otherMap;

    EqProcedure(final WatersIntIntHashMap otherMap)
    {
      _otherMap = otherMap;
    }

    public final boolean execute(final int key, final int value)
    {
      final int index = _otherMap.index(key);
      if (index >= 0 && eq(value, _otherMap.get(key))) {
        return true;
      }
      return false;
    }

    /**
     * Compare two ints for equality.
     */
    private final boolean eq(final int v1, final int v2)
    {
      return v1 == v2;
    }

  }

  /**
   * removes the mapping at <tt>index</tt> from the map.
   *
   * @param index
   *          an <code>int</code> value
   */
  protected void removeAt(final int index)
  {
    mValues[index] = mDefaultValue;
    super.removeAt(index); // clear key, state; adjust size
  }

  /**
   * Returns the values of the map.
   *
   * @return a <code>Collection</code> value
   */
  public int[] getValues()
  {
    final int[] vals = new int[size()];
    final int[] v = mValues;
    final byte[] states = _states;

    for (int i = v.length, j = 0; i-- > 0;) {
      if (states[i] == FULL) {
        vals[j++] = v[i];
      }
    }
    return vals;
  }

  /**
   * returns the keys of the map.
   *
   * @return a <code>Set</code> value
   */
  public int[] keys()
  {
    final int[] keys = new int[size()];
    final int[] k = _set;
    final byte[] states = _states;
    for (int i = k.length, j = 0; i-- > 0;) {
      if (states[i] == FULL) {
        keys[j++] = k[i];
      }
    }
    return keys;
  }

  /**
   * returns the keys of the map.
   *
   * @param a
   *          the array into which the elements of the list are to be stored, if
   *          it is big enough; otherwise, a new array of the same type is
   *          allocated for this purpose.
   * @return a <code>Set</code> value
   */
  public int[] keys(int[] a)
  {
    final int size = size();
    if (a.length < size) {
      a = (int[]) Array.newInstance(a.getClass().getComponentType(), size);
    }
    final int[] k = (int[]) _set;
    final byte[] states = _states;
    for (int i = k.length, j = 0; i-- > 0;) {
      if (states[i] == FULL) {
        a[j++] = k[i];
      }
    }
    return a;
  }

  /**
   * checks for the presence of <tt>val</tt> in the values of the map.
   *
   * @param val
   *          an <code>int</code> value
   * @return a <code>boolean</code> value
   */
  public boolean containsValue(final int val)
  {
    final byte[] states = _states;
    final int[] vals = mValues;
    for (int i = vals.length; i-- > 0;) {
      if (states[i] == FULL && val == vals[i]) {
        return true;
      }
    }
    return false;
  }

  /**
   * checks for the present of <tt>key</tt> in the keys of the map.
   *
   * @param key
   *          an <code>int</code> value
   * @return a <code>boolean</code> value
   */
  public boolean containsKey(final int key)
  {
    return contains(key);
  }

  /**
   * Executes <tt>procedure</tt> for each key in the map.
   *
   * @param procedure
   *          a <code>TIntProcedure</code> value
   * @return false if the loop over the keys terminated because the procedure
   *         returned false for some key.
   */
  public boolean forEachKey(final TIntProcedure procedure)
  {
    return forEach(procedure);
  }

  /**
   * Executes <tt>procedure</tt> for each value in the map.
   *
   * @param procedure
   *          a <code>TIntProcedure</code> value
   * @return false if the loop over the values terminated because the procedure
   *         returned false for some value.
   */
  public boolean forEachValue(final TIntProcedure procedure)
  {
    final byte[] states = _states;
    final int[] values = mValues;
    for (int i = values.length; i-- > 0;) {
      if (states[i] == FULL && !procedure.execute(values[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Executes <tt>procedure</tt> for each key/value entry in the map.
   *
   * @param procedure
   *          a <code>TOIntIntProcedure</code> value
   * @return false if the loop over the entries terminated because the procedure
   *         returned false for some entry.
   */
  public boolean forEachEntry(final TIntIntProcedure procedure)
  {
    final byte[] states = _states;
    final int[] keys = _set;
    final int[] values = mValues;
    for (int i = keys.length; i-- > 0;) {
      if (states[i] == FULL && !procedure.execute(keys[i], values[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Retains only those entries in the map for which the procedure returns a
   * true value.
   *
   * @param procedure
   *          determines which entries to keep
   * @return true if the map was modified.
   */
  public boolean retainEntries(final TIntIntProcedure procedure)
  {
    boolean modified = false;
    final byte[] states = _states;
    final int[] keys = _set;
    final int[] values = mValues;
    // Temporarily disable compaction. This is a fix for bug #1738760
    tempDisableAutoCompaction();
    try {
      for (int i = keys.length; i-- > 0;) {
        if (states[i] == FULL && !procedure.execute(keys[i], values[i])) {
          removeAt(i);
          modified = true;
        }
      }
    } finally {
      reenableAutoCompaction(true);
    }
    return modified;
  }

  /**
   * Transform the values in this map using <tt>function</tt>.
   *
   * @param function
   *          a <code>TIntFunction</code> value
   */
  public void transformValues(final TIntFunction function)
  {
    final byte[] states = _states;
    final int[] values = mValues;
    for (int i = values.length; i-- > 0;) {
      if (states[i] == FULL) {
        values[i] = function.execute(values[i]);
      }
    }
  }

  /**
   * Increments the primitive value mapped to key by 1
   *
   * @param key
   *          the key of the value to increment
   * @return true if a mapping was found and modified.
   */
  public boolean increment(final int key)
  {
    return adjustValue(key, (int) 1);
  }

  /**
   * Adjusts the primitive value mapped to key.
   *
   * @param key
   *          the key of the value to increment
   * @param amount
   *          the amount to adjust the value by.
   * @return true if a mapping was found and modified.
   */
  public boolean adjustValue(final int key, final int amount)
  {
    final int index = index(key);
    if (index < 0) {
      return false;
    } else {
      mValues[index] += amount;
      return true;
    }
  }

  /**
   * Adjusts the primitive value mapped to the key if the key is present in the
   * map. Otherwise, the <tt>initial_value</tt> is put in the map.
   *
   * @param key
   *          the key of the value to increment
   * @param adjust_amount
   *          the amount to adjust the value by
   * @param put_amount
   *          the value put into the map if the key is not initial present
   * @return the value present in the map after the adjustment or put operation
   */
  public int adjustOrPutValue(final int key, final int adjust_amount,
                              final int put_amount)
  {
    int index = insertionIndex(key);
    final boolean isNewMapping;
    final int newValue;
    if (index < 0) {
      index = -index - 1;
      newValue = (mValues[index] += adjust_amount);
      isNewMapping = false;
    } else {
      newValue = (mValues[index] = put_amount);
      isNewMapping = true;
    }
    final byte previousState = _states[index];
    _set[index] = key;
    _states[index] = FULL;
    if (isNewMapping) {
      postInsertHook(previousState == FREE);
    }
    return newValue;
  }

  public String toString()
  {
    final StringBuilder buf = new StringBuilder("{");
    forEachEntry(new TIntIntProcedure() {
      private boolean first = true;

      public boolean execute(final int key, final int value)
      {
        if (first)
          first = false;
        else
          buf.append(",");

        buf.append(key);
        buf.append("=");
        buf.append(value);
        return true;
      }
    });
    buf.append("}");
    return buf.toString();
  }


  //########################################################################
  //# Data Members
  /**
   * The values of the map.
   */
  private transient int[] mValues;

  /**
   * The default value for nonexistent keys
   */
  private final int mDefaultValue;


  //########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

  private final TIntIntProcedure PUT_ALL_PROC = new TIntIntProcedure() {
    public boolean execute(final int key, final int value)
    {
      put(key, value);
      return true;
    }
  };

}
