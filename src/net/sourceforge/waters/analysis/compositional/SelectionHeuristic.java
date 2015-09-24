//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import net.sourceforge.waters.model.analysis.AbstractAbortable;
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

public abstract class SelectionHeuristic<T extends Comparable<? super T>>
  extends AbstractAbortable
  implements Cloneable, Comparator<T>
{

  //#########################################################################
  //# Heuristics
  /**
   * Sets the context in which the heuristic runs.
   * This method is called when a heuristic is registered with a model analyser
   * ({@link net.sourceforge.waters.model.analysis.des.ModelAnalyzer
   * ModelAnalyzer} or similar object) to pass that model analyser as a
   * context into the heuristic. The default implementation does nothing,
   * but it can be overridden by specific heuristics that require access
   * to their model analyser.
   * @param  context  The context to be used by the selection heuristic,
   *                  or <CODE>null</CODE> to indicate that analysis has
   *                  completed and the context becomes invalid.
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
          checkAbort();
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
   * Creates a decisive selection heuristic based on this heuristic.
   * The returned heuristic first compares candidates according to this
   * selection heuristic. If two candidates are found equal, a sequence of
   * other heuristics is used to break the tie. The specific sequence is
   * determined individually by each subclass. The default implementation
   * return this heuristic if it is decisive, and otherwise creates a
   * one-step chain that uses standard comparison through the {@link
   * java.util.Comparable Comparable} interface if this heuristic fails to
   * distinguish two candidates.
   */
  public SelectionHeuristic<T> createDecisiveHeuristic()
  {
    if (isDecisive()) {
      return this;
    } else {
      return new ChainSelectionHeuristic<T>(this);
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

  /**
   * Returns whether this comparator is guaranteed to make a proper decision
   * for every two candidates. Most heuristics are not decisive because
   * the heuristic values of structurally isomorphic candidates usually are
   * equal. A decisive heuristic has to compare candidates by name as a final
   * resort.
   * @return <CODE>false</CODE>.
   */
  protected boolean isDecisive()
  {
    return false;
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
  public abstract String show(T candidate);

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

  @Override
  public Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }

}
