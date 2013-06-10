//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   MinStatesVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class MinUpdatesCompositionSelectionHeuristic extends
  CompositionSelectionHeuristic
{
//#########################################################################
  //# Constructors
  public MinUpdatesCompositionSelectionHeuristic
    (final ModuleProxyFactory factory, final CompilerOperatorTable op)
  {
    super(factory, op);
  }

  //#########################################################################
  //# Invocation
  @Override
  public double getHeuristicValue(final List<EFSMTransitionRelation> candidate)
    throws AnalysisException, EvalException
  {
    final Set<ConstraintList> updates = new THashSet<ConstraintList>();
    for (final EFSMTransitionRelation efsmTR : candidate) {
      final EFSMEventEncoding efsmEncoding = efsmTR.getEventEncoding();
      for (int up=EventEncoding.NONTAU; up < efsmEncoding.size(); up++) {
        updates.add(efsmEncoding.getUpdate(up));
      }
    }
    return updates.size();
  }
}
