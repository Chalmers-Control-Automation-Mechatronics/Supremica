//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   EstimatedMinStatesVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class EstimatedMinStatesVariableSelectionHeuristic
  extends NumericSelectionHeuristic<EFSMVariable>
{

  //#########################################################################
  //# Constructors
  @Override
  protected double getHeuristicValue(final EFSMVariable var)
  {
    final EFSMTransitionRelation efsmTR = var.getTransitionRelation();
    final double efsmSize = efsmTR.getTransitionRelation().getNumberOfStates();
    final double varSize = var.getRange().size();
    return varSize*efsmSize;
  }

}
