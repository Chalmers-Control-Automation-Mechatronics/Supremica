//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MinVariablesCompositionSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * The &quot;minimum variables&quot; composition selection
 * heuristic for EFSMs. This heuristic gives preference to composition
 * candidates with the smallest number of variables (local or shared)
 * mentioned in the EFSMs to be composed.
 *
 * @see MinSharedVariablesCompositionSelectionHeuristic
 * @author Robi Malik, Sahar Mohajerani
 */

public class MinVariablesCompositionSelectionHeuristic
  extends CompositionSelectionHeuristic
{

  //#########################################################################
  //# Invocation
  @Override
  public double getHeuristicValue(final List<EFSMTransitionRelation> candidate)
  {
    final Set<EFSMVariable> variables = new THashSet<EFSMVariable>();
    for (final EFSMTransitionRelation efsmTR : candidate) {
      final Collection<EFSMVariable> efsmVariables = efsmTR.getVariables();
      variables.addAll(efsmVariables);
    }
    return variables.size();
  }

}
