//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MinStatesVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class MinStatesVariableSelectionHeuristic extends
  VariableSelectionHeuristic
{
  //#########################################################################
  //# Constructors
  public MinStatesVariableSelectionHeuristic(final ModuleProxyFactory factory,
                                             final CompilerOperatorTable op)
  {
    super(factory, op);
  }

  //#########################################################################
  //# Invocation
  @Override
  public double getHeuristicValue(final EFSMVariable var)
    throws AnalysisException, EvalException
  {
    final EFSMTransitionRelation unfoldTR =  unfold(var);
    return unfoldTR.getTransitionRelation().getNumberOfStates();
  }

}
