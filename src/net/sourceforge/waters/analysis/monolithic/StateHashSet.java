//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
 * An implementation of the {@link java.util.Set Set} interface that uses an
 * open-addressed hash table to store its contents.
 *
 * Created: Sat Nov  3 10:38:17 2001
 *
 * @author Eric D. Friedman
 * @version $Id$
 */

public class StateHashSet<E> extends StateObjectHash<E> implements Serializable {

    static final long serialVersionUID = -2353400642617702135L;

    /**
     * Creates a new <code>StateHashSet</code> instance with the default
     * capacity and load factor.
     */
    public StateHashSet(final Class<? extends E> clazz) {
        super(clazz);
    }

    /**
     * Creates a new <code>StateHashSet</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the default load factor.
     *
     * @param initialCapacity an <code>int</code> value
     */
    public StateHashSet(final Class<? extends E> clazz,final int initialCapacity) {
        super(clazz,initialCapacity);
    }

    /**
     * Expands the set to accomodate new values.
     *
     * @param newCapacity an <code>int</code> value
     */
    @SuppressWarnings("unchecked")
    protected void rehash(final int newCapacity) {
        final int oldCapacity = _set.length;
        final E oldSet[] = _set;

        _set = (E[]) Array.newInstance(_clazz, newCapacity);

        for (int i = oldCapacity; i-- > 0;) {
            if(oldSet[i] != null) {
                final E o = oldSet[i];
                final int index = insertionIndex(o);
                if (index < 0) { // everyone pays for this because some people can't RTFM
                    throwObjectContractViolation(_set[(-index -1)], o);
                }
                _set[index] = o;
            }
        }
    }

} // StateHashSet
