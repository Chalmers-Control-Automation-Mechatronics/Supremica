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
 * A selection heuristic formed as a combination of several other
 * heuristics. To decide which of two given candidates should be given
 * preference, the chain selection heuristic uses a sequence of heuristics
 * until one of them can make a decision. If all heuristics in the chain
 * fail, the final resort is to compare the candidates using their
 * {@link Comparable#compareTo(Object) compareTo()} method, which typically
 * implements default comparison by name.
 *
 * @author Robi Malik
 *
 * @see SelectionHeuristic
 */

public class ChainSelectionHeuristic<T extends Comparable<? super T>>
  extends SelectionHeuristic<T>
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new chain selection heuristics
   * @param  steps   The heuristics to be invoked in the chain,
   *                 in the order on which they are to be invoked.
   */
  @SafeVarargs
  public ChainSelectionHeuristic(final SelectionHeuristic<T>... steps)
  {
    mSteps = steps;
  }

  //#########################################################################
  //# Interface java.util.Comparator<T>
  @Override
  public int compare(final T candidate1, final T candidate2)
  {
    SelectionHeuristic<T> decider = null;
    int result = 0;
    for (final SelectionHeuristic<T> step : mSteps) {
      result = step.compare(candidate1, candidate2);
      if (result != 0) {
        decider = step;
        break;
      }
    }
    if (result == 0) {
      result = candidate1.compareTo(candidate2);
    }
    if (result > 0) {
      boolean before = true;
      for (final SelectionHeuristic<T> step : mSteps) {
        if (step == decider) {
          before = false;
        } else if (before) {
          step.setBestCandidate(candidate2);
        } else {
          step.reset();
        }
      }
    }
    return result;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.analysis.compositional.AbstractHeuristic
  @Override
  public void setContext(final Object context)
  {
    for (final SelectionHeuristic<? extends T> step : mSteps) {
      step.setContext(context);
    }
  }

  @Override
  public void reset()
  {
    super.reset();
    for (final SelectionHeuristic<? extends T> step : mSteps) {
      step.reset();
    }
  }


  //#########################################################################
  //# Data Members
  private final SelectionHeuristic<T>[] mSteps;

}