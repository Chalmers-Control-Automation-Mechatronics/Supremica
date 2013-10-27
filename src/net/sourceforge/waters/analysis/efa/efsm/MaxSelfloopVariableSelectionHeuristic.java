//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MaxSelfloopVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;


/**
 * A variable selection heuristic used by the {@link EFSMConflictChecker}.
 * This heuristics selects the local variable that with the largest number
 * of recorded selfloops.
 *
 * @author Robi Malik
 */

public class MaxSelfloopVariableSelectionHeuristic
  extends NumericSelectionHeuristic<EFSMVariable>
{

  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.compositional.AbstractNumericSelectionHeuristic
  @Override
  protected double getHeuristicValue(final EFSMVariable var)
  {
    return -var.getSelfloops().size();
  }

}
