//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.distributed.hashkit;

/**
 * Contains hash functions and equality predicates for the hash
 * table. The indirect variants of these functions should be
 * consistent with the direct version and are used when rehashing the
 * table.
 * 
 * The hash strategy should implement indirect access to the data
 * store.
 *
 * @author Sam Douglas
 */
public interface HashStrategy
{
  /**
   * Checks if the supplied object is equal to the object designated by
   * x.
   * @param obj Object to check equality for
   * @param x Pointer to value to check equality for
   * @return true if object and indirect pointer value are equal.
   */
  public boolean equal(Object obj, int x);

  /**
   * Checks if the indirect value of two pointers should be considered
   * equal.
   * @param x Pointer to an object
   * @param y Pointer to an object
   * @return True if the indirect pointer values are equal.
   */
  public boolean equalIndirect(int x, int y);

  /**
   * Computes a hash code for the object.
   * @param x the object to compute the hash code for
   * @return The hashcode for the object.
   */
  public int computeHash(Object x);

  /**
   * Computes a hash code for the object corresponding to the value of
   * ptr.  This is necessary to support rehashing without having to
   * store the original object. It is important that this hash
   * function is consistent with the computeHash function.
   * @param ptr Address of the object that should be hashed.
   * @return Hash value for the indirect value of ptr.
   */
  public int computeIndirectHash(int ptr);
}
