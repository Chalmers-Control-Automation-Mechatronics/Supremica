//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   MinStatesVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class MaxTrueVariableSelectionHeuristic extends
  VariableSelectionHeuristic
{
//#########################################################################
  //# Constructors
  public MaxTrueVariableSelectionHeuristic(final ModuleProxyFactory factory,
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
    final EFSMTransitionRelation unfoldTR = unfold(var);
    final ListBufferTransitionRelation rel = unfoldTR.getTransitionRelation();
    final double transSize = rel.getNumberOfTransitions();
    final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
    double trueCount = 0;
    while (iter.advance()) {
      if (iter.getCurrentEvent() == 0) {
        trueCount ++;
      }
    }
    return - trueCount/transSize;
  }
}
