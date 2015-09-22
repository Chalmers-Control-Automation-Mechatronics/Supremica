//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Implements all iterator functions for the hashed object set. Subclasses may
 * override objectAtIndex to vary the object returned by calls to next() (e.g.
 * for values, and Map.Entry objects).
 *
 * <p>
 * Note that iteration is fastest if you forego the calls to <tt>hasNext</tt>
 * in favor of checking the size of the structure yourself and then call
 * next() that many times:
 *
 * <pre>
 * Iterator i = collection.iterator();
 * for (int size = collection.size(); size-- &gt; 0;) {
 *   Object o = i.next();
 * }
 * </pre>
 *
 * <p>
 * You may, of course, use the hasNext(), next() idiom too if you aren't in a
 * performance critical spot.
 * </p>
 *
 * @author Eric D. Friedman
 */

class WatersHashIterator<V> implements Iterator<V>
{

  //#########################################################################
  //# Constructors
  /**
   * Create an instance of THashIterator over the values of the TObjectHash
   */
  public WatersHashIterator(final WatersObjectHash<V> hash)
  {
    mObjectHash = hash;
    mExpectedSize = mObjectHash.size();
    mIndex = mObjectHash.capacity();
  }


  //#########################################################################
  //# Interface java.util.Iterator
  /**
   * Moves the iterator to the next Object and returns it.
   *
   * @return an <code>Object</code> value
   * @exception ConcurrentModificationException
   *              if the structure was changed using a method that isn't on
   *              this iterator.
   * @exception NoSuchElementException
   *              if this is called on an exhausted iterator.
   */
  @Override
  public V next()
  {
    moveToNextIndex();
    return objectAtIndex(mIndex);
  }

  /**
   * Returns true if the iterator can be advanced past its current location.
   *
   * @return a <code>boolean</code> value
   */
  @Override
  public boolean hasNext()
  {
    return nextIndex() >= 0;
  }

  /**
   * Removes the last entry returned by the iterator. Invoking this method
   * more than once for a single entry will leave the underlying data
   * structure in a confused state.
   */
  @Override
  public void remove()
  {
    if (mExpectedSize != mObjectHash.size()) {
      throw new ConcurrentModificationException();
    }
    // Disable auto compaction during the remove. This is a workaround for bug 1642768.
    try {
      mObjectHash.tempDisableAutoCompaction();
      mObjectHash.removeAt(mIndex);
    } finally {
      mObjectHash.reenableAutoCompaction(false);
    }
    mExpectedSize--;
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Returns the object at the specified index. Subclasses should implement
   * this to return the appropriate object for the given index.
   *
   * @param index
   *          the index of the value to return.
   * @return an <code>Object</code> value
   */
  @SuppressWarnings("unchecked")
  private V objectAtIndex(final int index)
  {
    return (V) mObjectHash._set[index];
  }

  /**
   * Returns the index of the next value in the data structure or a negative
   * value if the iterator is exhausted.
   *
   * @return an <code>int</code> value
   * @exception ConcurrentModificationException
   *              if the underlying collection's size has been modified since
   *              the iterator was created.
   */
  private final int nextIndex()
  {
    if (mExpectedSize != mObjectHash.size()) {
      throw new ConcurrentModificationException();
    }

    final Object[] set = mObjectHash._set;
    int i = mIndex;
    while (i-- > 0 && (set[i] == WatersObjectHash.FREE ||
                       set[i] == WatersObjectHash.REMOVED)) {
    }
    return i;
  }

  /**
   * Sets the internal <tt>index</tt> so that the `next' object can be
   * returned.
   */
  private final void moveToNextIndex()
  {
    // doing the assignment && < 0 in one line shaves
    // 3 opcodes...
    if ((mIndex = nextIndex()) < 0) {
      throw new NoSuchElementException();
    }
  }


  //#########################################################################
  //# Data Members
  /**
   * The data structure this iterator traverses.
   */
  private final WatersObjectHash<V> mObjectHash;
  /**
   * The number of elements this iterator believes are in the data structure
   * it accesses.
   */
  private int mExpectedSize;
  /**
   * The index used for iteration.
   */
  private int mIndex;

}







