//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   SelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.log4j.Logger;


/**
 * <P>An abstract type to represent an arbitrary selection heuristic.
 * A heuristic is a comparator that can compare two objects of arbitrary
 * type T according to preference. The class provides support to select
 * the best from a collection of candidates, with some support for
 * caching.</P>
 *
 * <P>The subtype {@link NumericSelectionHeuristic} can be extended
 * to implement simple heuristics based on a numeric heuristic value.
 * The subtype {@link ChainSelectionHeuristic} can be used to combine several
 * heuristics into one.</P>
 *
 * @author Robi Malik
 *
 * @see NumericSelectionHeuristic
 * @see ChainSelectionHeuristic
 */

public abstract class SelectionHeuristic<T>
  implements Comparator<T>
{

  //#########################################################################
  //# Heuristics
  /**
   * Sets the context in which the heuristic runs.
   * This method is called when a heuristic is register with a model analyser
   * ({@link net.sourceforge.waters.model.analysis.ModelAnalyzer ModelAnalyzer}
   * or similar object) to pass that model analyser as a context into the
   * heuristic. The default implementation does nothing, but it can be
   * overridden by specific heuristics that require access to their model
   * analyser.
   */
  public void setContext(final Object context)
  {
  }

  /**
   * Selects the best candidate based on this heuristic.
   * @param  candidates  Collection of candidates to choose from.
   * @return The best candidate from the collection. If two candidates
   *         have equal preference, the first one is returned.
   */
  public T select(final Collection<? extends T> candidates)
  {
    final Iterator<? extends T> iter = candidates.iterator();
    if (iter.hasNext()) {
      T result = iter.next();
      while (iter.hasNext()) {
        final T next = iter.next();
        if (compare(result, next) > 0) {
          result = next;
        }
      }
      reset();
      return result;
    } else {
      return null;
    }
  }

  /**
   * Resets all cached comparison results stored on this object.
   * If this method is overridden by a subclass, the superclass method
   * should be called first.
   */
  protected void reset()
  {
  }

  /**
   * Replaces the cached best candidate by the given candidate.
   * This method only replaces the candidate and does not change any
   * other cached value. It is used by {@link ChainSelectionHeuristic}
   * when heuristic has made a tie-breaking decision after this heuristic
   * has found two candidates equal.
   * @param  best   The new best candidate.
   */
  protected void setBestCandidate(final T best)
  {
  }


  //#########################################################################
  //# Logging
  public Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }

}