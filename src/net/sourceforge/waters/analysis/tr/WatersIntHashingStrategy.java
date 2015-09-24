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

import java.io.Serializable;


/**
 * Interface to support pluggable hashing and equality strategies in maps
 * and sets. Implementors can use this interface to customise the way
 * hash codes are computed or how primitive values are considered as
 * equal. This is a modified version of interface TIntHashingStrategy in
 * GNU Trove&nbsp;2.
 *
 * @author Eric D. Friedman, Robi Malik
 */

public interface WatersIntHashingStrategy extends Serializable
{

  /**
   * Computes a hash code for the specified int.  Implementors
   * can use the int's own value or a custom scheme designed to
   * minimise collisions for a known set of input.
   * @param val int for which the hash code is to be computed.
   * @return the hash cod.e
   */
  public int computeHashCode(int val);

  /**
   * Returns whether two int values are considered as equal in hash table.
   */
  public boolean equals(int val1, int val2);

}
