//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   WatersLongHashingStrategy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import java.io.Serializable;


/**
 * Interface to support pluggable hashing and equality strategies in maps
 * and sets. Implementors can use this interface to customise the way
 * hash codes are computed or how primitive values are considered as
 * equal. This is a modified version of interface
 * {@link gnu.trove.TLongHashingStrategy}.
 *
 * @author Eric D. Friedman, Robi Malik
 */

public interface WatersLongHashingStrategy extends Serializable
{

  /**
   * Computes a hash code for the specified long.  Implementors
   * can use the int's own value or a custom scheme designed to
   * minimise collisions for a known set of input.
   * @param val long for which the hash code is to be computed.
   * @return the hash cod.e
   */
  public int computeHashCode(long val);

  /**
   * Returns whether two long values are considered as equal in hash table.
   */
  public boolean equals(long val1, long val2);

}
