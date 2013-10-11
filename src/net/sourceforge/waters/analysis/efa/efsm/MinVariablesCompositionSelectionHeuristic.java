//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MinStatesVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class MinVariablesCompositionSelectionHeuristic extends
  CompositionSelectionHeuristic
{
//#########################################################################
  //# Constructors
  public MinVariablesCompositionSelectionHeuristic
    (final ModuleProxyFactory factory, final CompilerOperatorTable op)
  {
    super(factory, op);
  }

  //#########################################################################
  //# Invocation
  @Override
  public double getHeuristicValue(final List<EFSMTransitionRelation> candidate)
    throws AnalysisException, EvalException
  {
    final Set<EFSMVariable> variables = new THashSet<EFSMVariable>();
    for (final EFSMTransitionRelation efsmTR : candidate) {
      final Collection<EFSMVariable> efsmVariables = efsmTR.getVariables();
      variables.addAll(efsmVariables);
    }
    return variables.size();
  }
}
