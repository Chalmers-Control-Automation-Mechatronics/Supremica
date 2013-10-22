//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   CompositionSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


/**
 * The abstract superclass of all EFSM composition selection heuristics.
 * A composition selection heuristics chooses the best candidate among
 * different pairs of transition relations to be composed.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

abstract class CompositionSelectionHeuristic
{

  //#########################################################################
  //# Constructors
  public CompositionSelectionHeuristic(final ModuleProxyFactory factory,
                                       final CompilerOperatorTable op)
  {
  }

  //#########################################################################
  //# Invocation
  public List<EFSMTransitionRelation> selectComposition(final EFSMSystem system)
    throws AnalysisException, EvalException
  {
    final List<EFSMVariable> variablesList = system.getVariables();
    final Collection<List<EFSMTransitionRelation>> visitedCandidates =
      new THashSet<List<EFSMTransitionRelation>>();
    double smallestValue = Double.POSITIVE_INFINITY;
    List<EFSMTransitionRelation> smallestCandidate = null;
    for (final EFSMVariable var : variablesList) {
      final Collection<EFSMTransitionRelation> efsmTRSet =
        var.getTransitionRelations();
      final List<EFSMTransitionRelation> efsmTRList =
        new ArrayList<EFSMTransitionRelation>(efsmTRSet);
      Collections.sort(efsmTRList);
      for (int i = 0; i < efsmTRList.size(); i++) {
        for (int j = i + 1; j < efsmTRList.size(); j++) {
          final EFSMTransitionRelation efsmTR1 = efsmTRList.get(i);
          final EFSMTransitionRelation efsmTR2 = efsmTRList.get(j);
          final List<EFSMTransitionRelation> candidate =
            new ArrayList<EFSMTransitionRelation>(2);
          if (efsmTR1.compareTo(efsmTR2) <= 0) {
            candidate.add(efsmTR1);
            candidate.add(efsmTR2);
          } else {
            candidate.add(efsmTR2);
            candidate.add(efsmTR1);
          }
          if (visitedCandidates.add(candidate)) {
            final double candidateValue = getHeuristicValue(candidate);
            if (candidateValue < smallestValue) {
              smallestValue = candidateValue;
              smallestCandidate = candidate;
            }
          }
        }
      }
    }
    return smallestCandidate;
  }

  public abstract double getHeuristicValue(List<EFSMTransitionRelation> candidate)
    throws AnalysisException, EvalException;


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    final String className = ProxyTools.getShortClassName(this);
    if (className.endsWith("CompositionSelectionHeuristic")) {
      final int len = "CompositionSelectionHeuristic".length();
      return className.substring(0, className.length()-len);
    } else {
      return className;
    }
  }

}
