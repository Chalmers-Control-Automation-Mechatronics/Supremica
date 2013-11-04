//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   SelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;

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
  implements Cloneable, Comparator<T>
{

  //#########################################################################
  //# Heuristics
  /**
   * Sets the context in which the heuristic runs.
   * This method is called when a heuristic is register with a model analyser
   * ({@link net.sourceforge.waters.model.analysis.des.ModelAnalyzer
   * ModelAnalyzer} or similar object) to pass that model analyser as a
   * context into the heuristic. The default implementation does nothing,
   * but it can be overridden by specific heuristics that require access
   * to their model analyser.
   * @param  context  The context to be used by the selection heuristic.
   * @throws ClassCastException to indicate that the given context is not
   *         of a type supported by this selection heuristic.
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
    throws AnalysisException
  {
    final Iterator<? extends T> iter = candidates.iterator();
    if (iter.hasNext()) {
      try {
        T result = iter.next();
        while (iter.hasNext()) {
          final T next = iter.next();
          if (compare(result, next) > 0) {
            result = next;
          }
        }
        return result;
      } catch (final WatersRuntimeException exception) {
        if (exception.getCause() instanceof AnalysisException) {
          throw (AnalysisException) exception.getCause();
        } else {
          throw exception;
        }
      } finally {
        reset();
      }
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
  //# Interface java.lang.Cloneable
  @Override
  public SelectionHeuristic<T> clone()
  {
    try {
      @SuppressWarnings("unchecked")
      final Class<? extends SelectionHeuristic<T>> clazz =
        (Class<? extends SelectionHeuristic<T>>) getClass();
      final Constructor<? extends SelectionHeuristic<T>> constructor =
        clazz.getConstructor();
      return constructor.newInstance();
    } catch (final NoSuchMethodException | SecurityException |
                   InstantiationException | IllegalAccessException |
                   InvocationTargetException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Debugging
  public String getName()
  {
    final String KEY = "SelectionHeuristic";
    final String clazzName = getClass().getName();
    final int pos = clazzName.lastIndexOf(KEY);
    if (pos >= 0 && pos + KEY.length() < clazzName.length()) {
      return clazzName.substring(pos + KEY.length());
    } else {
      return ProxyTools.getShortClassName(this);
    }
  }

  @Override
  public String toString()
  {
    return getName();
  }

  public Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }

}