//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MaxTrueCompositionSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import java.util.List;

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
  extends CompositionSelectionHeuristic
{

  //#########################################################################
  //# Invocation
  @Override
  public double getHeuristicValue(final List<EFSMTransitionRelation> candidate)
  {
    double numStates = 1.0;
    double trueUpdates = 0.0;
    double numTransitions = 0.0;
    for (final EFSMTransitionRelation efsmTR : candidate) {
      numStates *= efsmTR.getTransitionRelation().getNumberOfStates();
    }
    for (final EFSMTransitionRelation efsmTR : candidate) {
      final ListBufferTransitionRelation rel = efsmTR.getTransitionRelation();
      final TransitionIterator iter =
        rel.createAllTransitionsReadOnlyIterator(EventEncoding.TAU);
      while (iter.advance()) {
        trueUpdates++;
      }
      numTransitions +=
        rel.getNumberOfTransitions() * numStates / rel.getNumberOfStates();
    }
    return - trueUpdates / numTransitions;
  }

}
