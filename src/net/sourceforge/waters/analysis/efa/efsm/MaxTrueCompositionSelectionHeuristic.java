//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MinStatesVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class MaxTrueCompositionSelectionHeuristic extends
  CompositionSelectionHeuristic
{
//#########################################################################
  //# Constructors
  public MaxTrueCompositionSelectionHeuristic(final ModuleProxyFactory factory,
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
    double numStates = 1;
    double trueUpdates = 0;
    double numTransitions = 0;
    for (final EFSMTransitionRelation efsmTR : candidate) {
      numStates = numStates * efsmTR.getTransitionRelation().getNumberOfStates();
    }

    for (final EFSMTransitionRelation efsmTR : candidate) {
      final ListBufferTransitionRelation rel = efsmTR.getTransitionRelation();
      final TransitionIterator iter =
        rel.createAllTransitionsReadOnlyIterator(EventEncoding.TAU);
      while (iter.advance()) {
        trueUpdates ++;
      }
      numTransitions = numTransitions +
        rel.getNumberOfTransitions() * numStates/rel.getNumberOfStates();

    }
    return - trueUpdates/numTransitions;
  }
}
