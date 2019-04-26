//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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



/**
 * A selection heuristic based on the computation of a numeric heuristic
 * value. A new heuristic is created by creating a subclass of this class
 * and implementing the {@link #getHeuristicValue(Comparable)
 * getHeuristicValue()} method.
 *
 * @author Robi Malik
 */

public abstract class NumericSelectionHeuristic<T extends Comparable<? super T>>
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
  public abstract double getHeuristicValue(T candidate);


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
