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
 * An implementation of the <tt>Set</tt> interface that uses an
 * open-addressed hash table to store its contents.
 *
 * Created: Sat Nov  3 10:38:17 2001
 *
 * @author Eric D. Friedman
 * @version $Id$
 */

public class StateHashSet extends StateObjectHash implements Serializable {

    static final long serialVersionUID = -2353400642617702135L;

    /**
     * Creates a new <code>StateHashSet</code> instance with the default
     * capacity and load factor.
     */
    public StateHashSet() {
        super();
    }

    /**
     * Creates a new <code>StateHashSet</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the default load factor.
     *
     * @param initialCapacity an <code>int</code> value
     */
    public StateHashSet(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Expands the set to accomodate new values.
     *
     * @param newCapacity an <code>int</code> value
     */
    protected void rehash(int newCapacity) {
        int oldCapacity = _set.length;
        EncodedStateTuple oldSet[] = _set;

        _set = new EncodedStateTuple[newCapacity];

        for (int i = oldCapacity; i-- > 0;) {
            if(oldSet[i] != null) {
                EncodedStateTuple o = oldSet[i];
                int index = insertionIndex(o);
                if (index < 0) { // everyone pays for this because some people can't RTFM
                    throwObjectContractViolation(_set[(-index -1)], o);
                }
                _set[index] = o;
            }
        }
    }
} // StateHashSet
