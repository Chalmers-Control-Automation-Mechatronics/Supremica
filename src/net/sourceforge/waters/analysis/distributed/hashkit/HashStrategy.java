// Copyright (c) 2009, Sam Douglas
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