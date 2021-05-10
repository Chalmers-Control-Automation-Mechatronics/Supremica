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

package net.sourceforge.waters.analysis.tr;

/**
 * Provides various hash functions. This code is taken from GNU Trove&nbsp;2.
 *
 * @author wolfgang.hoschek@cern.ch
 */

public final class HashFunctions
{
  /**
   * Returns a hash code for the specified value.
   *
   * @return a hash code value for the specified value.
   */
  public static int hash(final double value)
  {
    assert !Double.isNaN(value) : "Values of NaN are not supported.";

    final long bits = Double.doubleToLongBits(value);
    return (int) (bits ^ (bits >>> 32));
    //return (int) Double.doubleToLongBits(value*663608941.737);
    //this avoids excessive hashCollisions in the case values are
    //of the form (1.0, 2.0, 3.0, ...)
  }

  /**
   * Returns a hash code for the specified value.
   *
   * @return a hash code value for the specified value.
   */
  public static int hash(final float value)
  {
    assert !Float.isNaN(value) : "Values of NaN are not supported.";

    return Float.floatToIntBits(value * 663608941.737f);
    // this avoids excessive hashCollisions in the case values are
    // of the form (1.0, 2.0, 3.0, ...)
  }

  /**
   * Returns a hash code for the specified value.
   *
   * @return a hash code value for the specified value.
   */
  public static int hash(final int value)
  {
    // Multiply by prime to make sure hash can't be negative (see Knuth v3, p. 515-516)
    return value * 31;
  }

  /**
   * Returns a hash code for the specified value.
   *
   * @return a hash code value for the specified value.
   */
  public static int hash(final long value)
  {
    // Multiply by prime to make sure hash can't be negative (see Knuth v3, p. 515-516)
    return ((int) (value ^ (value >>> 32))) * 31;
  }

  /**
   * Returns a hash code for the specified object.
   *
   * @return a hash code value for the specified object.
   */
  public static int hash(final Object object)
  {
    return object == null ? 0 : object.hashCode();
  }

}
