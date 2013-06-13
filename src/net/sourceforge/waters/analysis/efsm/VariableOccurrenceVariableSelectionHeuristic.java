//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   VariableOccurenceVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.OccursChecker;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * A variable selection heuristic used by the {@link EFSMConflictChecker}.
 * The Variable occurrence heuristics selects the local variable that
 * occurs in the largest number of updates in its EFSM.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class VariableOccurrenceVariableSelectionHeuristic
  extends VariableSelectionHeuristic
{

  //#########################################################################
  //# Constructors
  public VariableOccurrenceVariableSelectionHeuristic
    (final ModuleProxyFactory factory, final CompilerOperatorTable op)
  {
    super(factory, op);
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.efsm.VariableSelectionHeuristic
  @Override
  public double getHeuristicValue(final EFSMVariable var)
  {
    final OccursChecker checker = OccursChecker.getInstance();
    final SimpleExpressionProxy varname = var.getVariableName();
    final EFSMTransitionRelation efsmTR = var.getTransitionRelation();
    final EFSMEventEncoding encoding = efsmTR.getEventEncoding();
    int occurences = 0;
    for (int e = EventEncoding.NONTAU; e < encoding.size(); e++) {
      final ConstraintList update = encoding.getUpdate(e);
      if (checker.occurs(varname, update)) {
        occurences++;
      }
    }
    return -occurences;
  }

}
