//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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
 * A factory class to obtain {@link SelectionHeuristic} objects.
 * The selection heuristic creator is obtained from a
 * {@link CompositionalSelectionHeuristicFactory} and produces the actual
 * selection heuristics through its {@link #createBaseHeuristic()} and
 * {@link #createChainHeuristic()} methods.
 *
 * @author Robi Malik
 */

public abstract class SelectionHeuristicCreator
{
  //#########################################################################
  //# Constructors
  protected SelectionHeuristicCreator(final String name)
  {
    mName = name;
  }


  //#########################################################################
  //# Override for java.lang.Object
  @Override
  public String toString()
  {
    return mName;
  }


  //#########################################################################
  //# Factory Methods
  /**
   * Creates a comparator to implement this selection heuristic.
   * This returns an implementation of only one heuristic, which
   * may consider two candidates as equal.
   */
  abstract SelectionHeuristic<Candidate> createBaseHeuristic();

  /**
   * Creates a chain selection heuristic based on this method.
   * The returned heuristic first compares candidates according to the base
   * selection heuristic. If two candidates are found equal, a sequence of
   * other heuristics is used to break the tie. The specific sequence is
   * determined individually by each subclass. The default implementation
   * merely uses a one-step chain that uses standard name-based comparison
   * if the base heuristic fails to distinguish two candidates.
   */
  SelectionHeuristic<Candidate> createChainHeuristic()
  {
    @SuppressWarnings("unchecked")
    final SelectionHeuristic<Candidate>[] heuristics =
      new SelectionHeuristic[1];
    heuristics[0] = createBaseHeuristic();
    return new ChainSelectionHeuristic<>(heuristics);
  }


  //#########################################################################
  //# Data Members
  private final String mName;

}
