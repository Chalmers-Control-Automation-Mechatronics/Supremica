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

package net.sourceforge.waters.analysis.monolithic;

import java.io.Serializable;
import java.lang.reflect.Array;

/**
 * An open addressed hashing implementation for Object types.
 *
 * Created: Sun Nov  4 08:56:06 2001
 *
 * @author Eric D. Friedman
 * @version $Id$
 */
abstract class StateObjectHash<E>
  extends StateHash<E>
  implements Serializable, StateObjectHashingStrategy<E>
{
    private static final long serialVersionUID = 1L;

    /** the set of Objects */
    protected transient E[] _set;

    /** the strategy used to hash objects in this collection. */
    protected StateObjectHashingStrategy<E> _hashingStrategy;


    /**
     * Creates a new <code>StateObjectHash</code> instance with the
     * default capacity and load factor.
     */
    public StateObjectHash(final Class<? extends E> clazz) {
        super(clazz);
        this._hashingStrategy = this;
    }

    /**
     * Creates a new <code>StateObjectHash</code> instance whose capacity
     * is the next highest prime above <tt>initialCapacity + 1</tt>
     * unless that value is already prime.
     *
     * @param initialCapacity an <code>int</code> value
     */
    public StateObjectHash(final Class<? extends E> clazz,
                           final int initialCapacity) {
        super(clazz,initialCapacity);
        this._hashingStrategy = this;
    }

    /**
     * initializes the Object set of this hash table.
     *
     * @param initialCapacity an <code>int</code> value
     * @return an <code>int</code> value
     */
    @SuppressWarnings("unchecked")
    protected int setUp(final int initialCapacity) {
        int capacity;

        capacity = super.setUp(initialCapacity);
        _set = (E[]) Array.newInstance(_clazz, capacity);
        return capacity;
    }

    protected int capacity() {
        return _set.length;
    }

    /**
     * Searches the set for <tt>obj</tt>
     *
     * @param obj an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public boolean contains(final E obj) {
        return index(obj) >= 0;
    }

    /**
     * Return <tt>obj</tt> from the set
     *
     * @param obj an <code>Object</code> value
     * @return an <code>Object</code> value. if not exists, return null
     */
    public E get(final E obj) {
	final int i = index(obj);

	if(i >= 0){
	    return _set[i];
	}

        return null;
    }

    /**
     * Finds a value from the set, if not exists, inserts a value into the set.
     *
     * @param obj an <code>Object</code> value
     * @return null if the set was modified by the add operation, the found value if it is found.
     */
    public E getOrAdd(final E obj) {
	int i = index(obj);

	if(i >= 0){
	    return _set[i];
	}

        i = insertionIndex(obj);

        final E old = _set[i];
        _set[i] = obj;

        postInsertHook(old == null);
        return null;            // yes, we added something
    }


    /**
     * Locates the index of <tt>obj</tt>.
     *
     * @param obj an <code>Object</code> value
     * @return the index of <tt>obj</tt> or -1 if it isn't in the set.
     */
    protected int index(final E obj) {
        int hash, probe, index, length;
        E[] set;
        E cur;

        set = _set;
        length = set.length;
        hash = _hashingStrategy.computeHashCode(obj) & 0x7fffffff;
        index = hash % length;
        cur = set[index];

        if (cur != null
            && (! _hashingStrategy.equals(cur, obj))) {
            // see Knuth, p. 529
            probe = 1 + (hash % (length - 2));

            do {
                index -= probe;
                if (index < 0) {
                    index += length;
                }
                cur = set[index];
	    } while (cur != null
                     && (! _hashingStrategy.equals(cur, obj)));
        }

        return cur == null ? -1 : index;
    }

    /**
     * Locates the index at which <tt>obj</tt> can be inserted.  if
     * there is already a value equal()ing <tt>obj</tt> in the set,
     * returns that value's index as <tt>-index - 1</tt>.
     *
     * @param obj an <code>Object</code> value
     * @return the index of a FREE slot at which obj can be inserted
     * or, if obj is already stored in the hash, the negative value of
     * that index, minus 1: -index -1.
     */
    protected int insertionIndex(final E obj) {
        int hash, probe, index, length;
        E[] set;
        E cur;

        set = _set;
        length = set.length;
        hash = _hashingStrategy.computeHashCode(obj) & 0x7fffffff;
        index = hash % length;
        cur = set[index];

        if (cur == null) {
            return index;       // empty, all done
        } else if (_hashingStrategy.equals(cur, obj)) {
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

	    // starting at the natural offset, probe until we find an
	    // offset that isn't full.
	    do {
		index -= probe;
		if (index < 0) {
		    index += length;
		}
		cur = set[index];
	    } while (cur != null && ! _hashingStrategy.equals(cur, obj));

            // if it's full, the key is already stored
            return (cur != null) ? -index -1 : index;
	}
    }

    /**
     * This is the default implementation of StateObjectHashingStrategy:
     * it delegates hashing to the Object's hashCode method.
     *
     * @param o state tuple for which the hash code is to be computed
     * @return the hashCode
     * @see Object#hashCode()
     */
    public final int computeHashCode(final E o) {
        return o == null ? 0 : o.hashCode();
    }

    /**
     * This is the default implementation of StateObjectHashingStrategy:
     * it delegates equality comparisons to the first parameter's
     * equals() method.
     *
     * @param o1 an <code>Object</code> value
     * @param o2 an <code>Object</code> value
     * @return true if the objects are equal
     * @see Object#equals(Object)
     */
    public final boolean equals(final E o1, final E o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    /**
     * Convenience methods for subclasses to use in throwing exceptions about
     * badly behaved user objects employed as keys.  We have to throw an
     * IllegalArgumentException with a rather verbose message telling the
     * user that they need to fix their object implementation to conform
     * to the general contract for java.lang.Object.
     *
     * @param o1 the first of the equal elements with unequal hash codes.
     * @param o2 the second of the equal elements with unequal hash codes.
     * @exception IllegalArgumentException the whole point of this method.
     */
    protected final void throwObjectContractViolation(final E o1, final E o2)
        throws IllegalArgumentException {
        throw new IllegalArgumentException("Equal objects must have equal hashcodes. "
                                           + "During rehashing, Trove discovered that "
                                           + "the following two objects claim to be "
                                           + "equal (as in java.lang.Object.equals()) "
                                           + "but their hashCodes (or those calculated by "
                                           + "your StateObjectHashingStrategy) are not equal."
                                           + "This violates the general contract of "
                                           + "java.lang.Object.hashCode().  See bullet point two "
                                           + "in that method's documentation. "
                                           + "object #1 =" + o1
                                           + "; object #2 =" + o2);
    }
} // StateObjectHash
