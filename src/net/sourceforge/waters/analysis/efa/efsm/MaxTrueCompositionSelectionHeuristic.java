//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MaxTrueCompositionSelectionHeuristic
//###########################################################################
//# $Id$
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
  protected double getHeuristicValue(final EFSMPair candidate)
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