//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   IntArrayHashingStrategy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import gnu.trove.strategy.HashingStrategy;

import java.util.Arrays;

/**
 * A utility class to implement GNU Trove hash tables with integer arrays
 * as keys.
 *
 * @author Robi Malik
 */

public class IntArrayHashingStrategy
  implements HashingStrategy<int[]>
{

  //#######################################################################
  //# Interface gnu.trove.TObjectHashingStrategy
  @Override
  public int computeHashCode(final int[] array)
  {
    return Arrays.hashCode(array);
  }

  @Override
  public boolean equals(final int[] array1, final int[] array2)
  {
    return Arrays.equals(array1, array2);
  }

  //#######################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
