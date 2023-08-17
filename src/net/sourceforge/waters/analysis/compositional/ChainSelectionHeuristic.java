//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;



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
    mPreOrder = null;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Sets the preordering method. If non-null, the list of candidates passed
   * to the {@link #select(Collection) select()} is sorted according to this
   * order on invocation of the method.
   * @param  preorder  Selection heuristic defining ordering
   *                   or <CODE>null</CODE>.
   */
  public void setPreOrder(final SelectionHeuristic<T> preorder)
  {
    if (preorder == null || preorder.isDecisive()) {
      mPreOrder = preorder;
    } else {
      mPreOrder = new ChainSelectionHeuristic<T>(preorder);
    }
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
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    for (final SelectionHeuristic<T> step : mSteps) {
      step.requestAbort();
    }
    if (mPreOrder != null) {
      mPreOrder.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    for (final SelectionHeuristic<T> step : mSteps) {
      step.resetAbort();
    }
    if (mPreOrder != null) {
      mPreOrder.resetAbort();
    }
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.analysis.compositional.SelectionHeuristic
  @Override
  public T select(final Collection<? extends T> candidates)
    throws AnalysisException
  {
    if (mPreOrder == null) {
      return super.select(candidates);
    } else {
      final List<T> list = new ArrayList<>(candidates);
      Collections.sort(list, mPreOrder);
      mPreOrder.reset();
      return super.select(list);
    }
  }

  @Override
  public void setContext(final Object context)
  {
    if (mPreOrder != null) {
      mPreOrder.setContext(context);
    }
    for (final SelectionHeuristic<? extends T> step : mSteps) {
      step.setContext(context);
    }
  }

  @Override
  public void reset()
  {
    super.reset();
    if (mPreOrder != null) {
      mPreOrder.reset();
    }
    for (final SelectionHeuristic<? extends T> step : mSteps) {
      step.reset();
    }
  }

  @Override
  protected boolean isDecisive()
  {
    return true;
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  @Override
  public ChainSelectionHeuristic<T> clone()
  {
    @SuppressWarnings("unchecked")
    final SelectionHeuristic<T>[] clonedSteps =
      new SelectionHeuristic[mSteps.length];
    for (int i = 0; i < mSteps.length; i++) {
      clonedSteps[i] = mSteps[i].clone();
    }
    final ChainSelectionHeuristic<T> cloned =
      new ChainSelectionHeuristic<T>(clonedSteps);
    cloned.setPreOrder(mPreOrder);
    return cloned;
  }


  //#########################################################################
  //# Debugging
  @Override
  public String show(final T candidate)
  {
    final StringBuilder builder = new StringBuilder();
    for (final SelectionHeuristic<T> step : mSteps) {
      if (builder.length() > 0) {
        builder.append("; ");
      }
      final String text = step.show(candidate);
      builder.append(text);
    }
    return builder.toString();
  }

  @Override
  public String getName()
  {
    return mSteps[0].getName();
  }


  //#########################################################################
  //# Data Members
  private SelectionHeuristic<T> mPreOrder;
  private final SelectionHeuristic<T>[] mSteps;

}
