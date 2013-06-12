//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   MinStatesVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class VariableOccurenceStatesVariableSelectionHeuristic extends
  VariableSelectionHeuristic
{
  //#########################################################################
  //# Constructors
  public VariableOccurenceStatesVariableSelectionHeuristic
  (final ModuleProxyFactory factory, final CompilerOperatorTable op)
  {
    super(factory, op);
  }


  @Override
  public double getHeuristicValue(final EFSMVariable var)
  {
    int occurence =  0;
    final EFSMTransitionRelation efsmTR = var.getTransitionRelation();
    final EFSMEventEncoding encoding = efsmTR.getEventEncoding();
    for(int i=0; i<encoding.size();i++) {
      final ConstraintList update = encoding.getUpdate(i);
      final SimpleExpressionProxy prime = var.getPrimedVariableName();
      if (update.getConstraints().contains(var) || update.getConstraints().contains(prime)){
        occurence--;
      }
    }
    return occurence;
  }
}
