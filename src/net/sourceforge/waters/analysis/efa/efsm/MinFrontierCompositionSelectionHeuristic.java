//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MinFrontierCompositionSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;


/**
 * The &quot;minimum frontier&quot; composition selection
 * heuristic for EFSMs. This heuristic gives preference to composition
 * candidates with the smallest EFSMs sharing variables with the EFSMs
 * in the candidate.
 *
 * @author Robi Malik
 */

public class MinFrontierCompositionSelectionHeuristic
  extends NumericSelectionHeuristic<EFSMPair>
{

  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.compositional.AbstractNumericSelectionHeuristic
  @Override
  protected double getHeuristicValue(final EFSMPair candidate)
  {
    final Set<EFSMTransitionRelation> frontier = new THashSet<>();
    for (final EFSMTransitionRelation efsmTR : candidate.asArray()) {
      for (final EFSMVariable var : efsmTR.getVariables()) {
        for (final EFSMTransitionRelation neighbour : var.getTransitionRelations()) {
          if (!candidate.contains(neighbour)) {
            frontier.add(neighbour);
          }
        }
      }
    }
    return frontier.size();
  }

}