//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   NumericSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;


/**
 * A selection heuristic based on the computation of a numeric heuristic
 * value. A new heuristic is created by creating a subclass of this class
 * and implementing the {@link #getHeuristicValue(Object) getHeuristicValue()}
 * method.
 *
 * @author Robi Malik
 */

public abstract class NumericSelectionHeuristic<T>
  extends SelectionHeuristic<T>
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
    if (value1 <= value2) {
      mBestCandidate = candidate1;
      mBestValue = value1;
      return value1 < value2 ? -1 : 0;
    } else {
      mBestCandidate = candidate2;
      mBestValue = value2;
      return 1;
    }
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.analysis.compositional.AbstractHeuristic
  @Override
  protected void reset()
  {
    mBestCandidate = null;
    mBestValue = Double.POSITIVE_INFINITY;
  }

  @Override
  protected void setBestCandidate(final T best)
  {
    mBestCandidate = best;
  }


  //#########################################################################
  //# Abstract Methods
  /**
   * Computes the heuristic value of the given candidate.
   * The numeric heuristic gives preference to the candidate with the
   * smallest heuristic value.
   */
  protected abstract double getHeuristicValue(T candidate);


  //#########################################################################
  //# Debugging
  @Override
  public String show(final T candidate)
  {
    return getName() + ":" + getHeuristicValue(candidate);
  }


  //#########################################################################
  //# Data Members
  private T mBestCandidate = null;
  private double mBestValue = Double.POSITIVE_INFINITY;

}