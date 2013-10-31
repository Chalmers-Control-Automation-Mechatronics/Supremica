//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MaxLocalVariablesCompositionSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;


/**
 * The &quot;maximum local variables&quot; composition selection
 * heuristic for EFSMs. This heuristic gives preference to composition
 * candidates with the largest number local variables in the EFSMs to be
 * composed.
 *
 * @see MinVariablesCompositionSelectionHeuristic
 * @author Robi Malik
 */

public class MaxLocalVariablesCompositionSelectionHeuristic
  extends NumericSelectionHeuristic<EFSMPair>
{

  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.compositional.AbstractNumericSelectionHeuristic
  @Override
  protected double getHeuristicValue(final EFSMPair candidate)
  {
    final Set<EFSMVariable> vars = new THashSet<EFSMVariable>();
    for (final EFSMTransitionRelation efsmTR : candidate.asArray()) {
      for (final EFSMVariable var : efsmTR.getVariables()) {
        final Set<EFSMTransitionRelation> trs = var.getTransitionRelations();
        if (candidate.containsAll(trs)) {
          vars.add(var);
        }
      }
    }
    return -vars.size();
  }

}