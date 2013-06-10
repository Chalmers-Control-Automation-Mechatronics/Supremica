//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   WatersHashFunctions
//###########################################################################
//# $Id$
//###########################################################################

// Copyright (c) 1999 CERN - European Organization for Nuclear Research.

// Permission to use, copy, modify, distribute and sell this software and
// its documentation for any purpose is hereby granted without fee,
// provided that the above copyright notice appear in all copies and that
// both that copyright notice and this permission notice appear in
// supporting documentation. CERN makes no representations about the
// suitability of this software for any purpose. It is provided "as is"
// without expressed or implied warranty.

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
