//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   WatersIntHashingStrategy
//###########################################################################
//# $Id$
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
