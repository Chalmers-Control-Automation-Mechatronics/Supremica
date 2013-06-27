//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   MaxSelfloopVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


/**
 * A variable selection heuristic used by the {@link EFSMConflictChecker}.
 * This heuristics selects the local variable that with the largest number
 * of recorded selfloops.
 *
 * @author Robi Malik
 */

public class MaxSelfloopVariableSelectionHeuristic
  extends VariableSelectionHeuristic
{

  //#########################################################################
  //# Constructors
  public MaxSelfloopVariableSelectionHeuristic
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
    return -var.getSelfloops().size();
  }

}
