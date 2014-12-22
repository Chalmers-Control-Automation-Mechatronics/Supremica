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
import java.util.Set;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;


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
  extends NumericSelectionHeuristic<EFSMPair>
{

  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic
  @Override
  public double getHeuristicValue(final EFSMPair candidate)
  {
    final Collection<EFSMVariable> vars1 = candidate.getFirst().getVariables();
    final Set<EFSMVariable> variables = new THashSet<EFSMVariable>(vars1);
    final Collection<EFSMVariable> vars2 = candidate.getSecond().getVariables();
    variables.addAll(vars2);
    return variables.size();
  }

}
