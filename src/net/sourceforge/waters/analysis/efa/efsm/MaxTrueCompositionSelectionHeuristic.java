//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.analysis.efa.efsm;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;


/**
 * The &quot;maximum true&quot; composition selection
 * heuristic for EFSMs. This heuristic gives preference to composition
 * candidates with the maximum ratio of true updates (tau transitions)
 * over the total number of transitions in the synchronous product of
 * the EFSMs in the candidate.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class MaxTrueCompositionSelectionHeuristic
  extends NumericSelectionHeuristic<EFSMPair>
{

  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic
  @Override
  public double getHeuristicValue(final EFSMPair candidate)
  {
    final ListBufferTransitionRelation rel1 =
      candidate.getFirst().getTransitionRelation();
    final ListBufferTransitionRelation rel2 =
      candidate.getSecond().getTransitionRelation();
    final double numStates =
      rel1.getNumberOfStates() * rel2.getNumberOfStates();
    final double numTransitions =
      rel1.getNumberOfTransitions() * numStates / rel1.getNumberOfStates() +
      rel2.getNumberOfTransitions() * numStates / rel2.getNumberOfStates();
    int trueUpdates = 0;
    final TransitionIterator iter1 =
      rel1.createAllTransitionsReadOnlyIterator(EventEncoding.TAU);
    while (iter1.advance()) {
      trueUpdates++;
    }
    final TransitionIterator iter2 =
      rel1.createAllTransitionsReadOnlyIterator(EventEncoding.TAU);
    while (iter2.advance()) {
      trueUpdates++;
    }
    return - trueUpdates / numTransitions;
  }

}
