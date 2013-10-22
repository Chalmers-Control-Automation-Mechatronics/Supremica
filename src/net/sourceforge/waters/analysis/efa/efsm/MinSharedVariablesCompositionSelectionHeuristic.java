//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MinSharedVariablesCompositionSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


/**
 * The &quot;minimum shared variables&quot; composition selection
 * heuristic for EFSMs. This heuristic gives preference to composition
 * candidates with the smallest possible number of non-local variables
 * mentioned in the EFSMs to be composed.
 *
 * @author Robi Malik
 */

public class MinSharedVariablesCompositionSelectionHeuristic
  extends CompositionSelectionHeuristic
{

  //#########################################################################
  //# Constructors
  public MinSharedVariablesCompositionSelectionHeuristic
    (final ModuleProxyFactory factory, final CompilerOperatorTable op)
  {
    super(factory, op);
  }


  //#########################################################################
  //# Invocation
  @Override
  public double getHeuristicValue(final List<EFSMTransitionRelation> candidate)
  {
    final Set<EFSMVariable> vars = new THashSet<EFSMVariable>();
    for (final EFSMTransitionRelation efsmTR : candidate) {
      for (final EFSMVariable var : efsmTR.getVariables()) {
        final Collection<EFSMTransitionRelation> trs =
          var.getTransitionRelations();
        if (!candidate.containsAll(trs)) {
          vars.add(var);
        }
      }
    }
    return vars.size();
  }

}