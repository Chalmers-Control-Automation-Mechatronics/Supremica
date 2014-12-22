//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MaxOccurrenceVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.OccursChecker;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * A variable selection heuristic used by the {@link EFSMConflictChecker}.
 * The Variable occurrence heuristics selects the local variable that
 * occurs in the largest number of updates in its EFSM.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class MaxOccurrenceVariableSelectionHeuristic
  extends NumericSelectionHeuristic<EFSMVariable>
{

  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic
  @Override
  public double getHeuristicValue(final EFSMVariable var)
  {
    final EFSMTransitionRelation efsmTR = var.getTransitionRelation();
    if (efsmTR == null) {
      return 0;
    } else {
      final OccursChecker checker = OccursChecker.getInstance();
      final SimpleExpressionProxy varname = var.getVariableName();
      final EFSMEventEncoding encoding = efsmTR.getEventEncoding();
      int occurrences = 0;
      for (int e = EventEncoding.NONTAU; e < encoding.size(); e++) {
        final ConstraintList update = encoding.getUpdate(e);
        if (checker.occurs(varname, update)) {
          occurrences++;
        }
      }
      return -occurrences;
    }
  }

}
