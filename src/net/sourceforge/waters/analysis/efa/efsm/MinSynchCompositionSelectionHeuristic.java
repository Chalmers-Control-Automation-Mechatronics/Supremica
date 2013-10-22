//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MinSynchCompositionSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import java.util.List;

/**
 * The &quot;minimum synchronous product&quot; composition selection
 * heuristic for EFSMs. This heuristic gives preference to composition
 * candidates with the smallest number of states in the synchronous
 * product. Note that the number of states in the synchronous product of
 * EFSMs is easy to calculate as there are no shared events, so it is
 * equal to the product of the state numbers of the composed EFSMs.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class MinSynchCompositionSelectionHeuristic
  extends CompositionSelectionHeuristic
{

  //#######################################################################
  //# Invocation
  @Override
  public double getHeuristicValue(final List<EFSMTransitionRelation> candidate)
  {
    double size = 1;
    for (final EFSMTransitionRelation efsmTR : candidate) {
      size *= efsmTR.getTransitionRelation().getNumberOfStates();
    }
    return size;
  }

}
