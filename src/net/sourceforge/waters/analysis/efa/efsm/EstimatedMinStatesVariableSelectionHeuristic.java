//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MinStatesVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class EstimatedMinStatesVariableSelectionHeuristic extends
  VariableSelectionHeuristic
{
  //#########################################################################
  //# Constructors
  public EstimatedMinStatesVariableSelectionHeuristic
  (final ModuleProxyFactory factory, final CompilerOperatorTable op)
  {
    super(factory, op);
  }


  @Override
  public double getHeuristicValue(final EFSMVariable var)
  {
    final EFSMTransitionRelation efsmTR = var.getTransitionRelation();
    final double efsmSize = efsmTR.getTransitionRelation().getNumberOfStates();
    final double varSize = var.getRange().size();
    return varSize*efsmSize;
  }

}
