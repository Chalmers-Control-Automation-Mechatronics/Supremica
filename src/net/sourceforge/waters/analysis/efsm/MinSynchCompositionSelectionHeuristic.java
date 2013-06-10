//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   MinStatesVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class MinSynchCompositionSelectionHeuristic extends
  CompositionSelectionHeuristic
{
//#########################################################################
  //# Constructors
  public MinSynchCompositionSelectionHeuristic(final ModuleProxyFactory factory,
                                             final CompilerOperatorTable op)
  {
    super(factory, op);
  }

  //#########################################################################
  //# Invocation

  @Override
  public double getHeuristicValue(final List<EFSMTransitionRelation> candidate)
    throws AnalysisException, EvalException
  {
    double size = 1;
    for (final EFSMTransitionRelation efsmTR : candidate) {
      size = size * efsmTR.getTransitionRelation().getNumberOfStates();
    }
    return size;
  }
}
