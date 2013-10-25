//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   AbstractNumericSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;


/**
 * @author Robi Malik
 */

public abstract class AbstractNumericSelectionHeuristic<T>
  extends AbstractSelectionHeuristic<T>
{

  //#########################################################################
  //# Interface java.util.Comparator<T>
  @Override
  public int compare(final T candidate1, final T candidate2)
  {
    final double value1;
    if (candidate1 == mBestCandidate) {
      value1 = mBestValue;
    } else {
      value1 = getHeuristicValue(candidate1);
    }
    final double value2;
    if (candidate2 == mBestCandidate) {
      value2 = mBestValue;
    } else {
      value2 = getHeuristicValue(candidate2);
    }
    if (value1 < value2) {
      mBestCandidate = candidate1;
      mBestValue = value1;
      return -1;
    } else if (value2 < value1) {
      mBestCandidate = candidate2;
      mBestValue = value2;
      return 1;
    } else {
      mBestCandidate = candidate1;
      mBestValue = value1;
      return 0;
    }
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.analysis.compositional.AbstractHeuristic
  @Override
  public void reset()
  {
    mBestCandidate = null;
    mBestValue = Double.POSITIVE_INFINITY;
  }


  //#########################################################################
  //# Abstract Methods
  public abstract double getHeuristicValue(T candidate);


  //#########################################################################
  //# Data Members
  private T mBestCandidate = null;
  private double mBestValue = Double.POSITIVE_INFINITY;

}