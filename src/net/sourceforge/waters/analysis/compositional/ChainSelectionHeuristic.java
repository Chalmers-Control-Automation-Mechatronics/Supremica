//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ChainSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;


/**
 * @author Robi Malik
 */

public class ChainSelectionHeuristic<T extends Comparable<? super T>>
  extends AbstractSelectionHeuristic<T>
{

  //#########################################################################
  //# Constructor
  @SafeVarargs
  public ChainSelectionHeuristic(final AbstractSelectionHeuristic<T>... steps)
  {
    mSteps = steps;
  }

  //#########################################################################
  //# Interface java.util.Comparator<T>
  @Override
  public int compare(final T candidate1, final T candidate2)
  {
    for (final AbstractSelectionHeuristic<T> step : mSteps) {
      final int result = step.compare(candidate1, candidate2);
      if (result != 0) {
        return result;
      }
    }
    return candidate1.compareTo(candidate2);
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.analysis.compositional.AbstractHeuristic
  @Override
  public void reset()
  {
    for (final AbstractSelectionHeuristic<? extends T> step : mSteps) {
      step.reset();
    }
  }


  //#########################################################################
  //# Data Members
  private final AbstractSelectionHeuristic<T>[] mSteps;

}