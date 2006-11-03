///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
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
///////////////////////////////////////////////////////////////////////////////

package net.sourceforge.waters.analysis;

import java.io.Serializable;

/**
 * An open addressed hashing implementation for Object types.
 *
 * Created: Sun Nov  4 08:56:06 2001
 *
 * @author Eric D. Friedman
 * @version $Id: StateObjectHash.java,v 1.5 2006-11-03 15:01:57 torda Exp $
 */
abstract public class StateObjectHash extends StateHash implements Serializable, StateObjectHashingStrategy {
    /** the set of Objects */
    protected transient EncodedStateTuple[] _set;
    
    /** the strategy used to hash objects in this collection. */
    protected StateObjectHashingStrategy _hashingStrategy;

    /**
     * Creates a new <code>StateObjectHash</code> instance with the
     * default capacity and load factor.
     */
    public StateObjectHash() {
        super();
        this._hashingStrategy = this;
    }

    /**
     * Creates a new <code>StateObjectHash</code> instance whose capacity
     * is the next highest prime above <tt>initialCapacity + 1</tt>
     * unless that value is already prime.
     *
     * @param initialCapacity an <code>int</code> value
     */
    public StateObjectHash(int initialCapacity) {
        super(initialCapacity);
        this._hashingStrategy = this;
    }

    /**
     * initializes the Object set of this hash table.
     *
     * @param initialCapacity an <code>int</code> value
     * @return an <code>int</code> value
     */
    protected int setUp(int initialCapacity) {
        int capacity;

        capacity = super.setUp(initialCapacity);
        _set = new EncodedStateTuple[capacity];
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
    public boolean contains(EncodedStateTuple obj) {
        return index(obj) >= 0;
    }
    
    /**
     * Return <tt>obj</tt> from the set
     *
     * @param obj an <code>Object</code> value
     * @return an <code>Object</code> value. if not exists, return null
     */
    public EncodedStateTuple get(EncodedStateTuple obj) {
	int i = index(obj);

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
    public EncodedStateTuple getOrAdd(EncodedStateTuple obj) {
	int i = index(obj);

	if(i >= 0){
	    return _set[i];
	}
	
        i = insertionIndex(obj);

        EncodedStateTuple old = _set[i];
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
    protected int index(EncodedStateTuple obj) {
        int hash, probe, index, length;
        EncodedStateTuple[] set;
        EncodedStateTuple cur;

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
    protected int insertionIndex(EncodedStateTuple obj) {
        int hash, probe, index, length;
        EncodedStateTuple[] set;
        EncodedStateTuple cur;

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
    public final int computeHashCode(EncodedStateTuple o) {
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
    public final boolean equals(EncodedStateTuple o1, EncodedStateTuple o2) {
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
    protected final void throwObjectContractViolation(EncodedStateTuple o1, EncodedStateTuple o2) 
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
