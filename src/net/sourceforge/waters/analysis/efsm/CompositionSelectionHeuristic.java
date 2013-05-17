package net.sourceforge.waters.analysis.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


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
    final Set<List<EFSMTransitionRelation>> candidates =
      new THashSet<List<EFSMTransitionRelation>>();
    for (final EFSMVariable var : variablesList) {
      final Collection<EFSMTransitionRelation> efsmTRSet =
        var.getTransitionRelations();
      final List<EFSMTransitionRelation> efsmTRList =
        new ArrayList<EFSMTransitionRelation>(efsmTRSet);
      for (int i = 0; i < efsmTRList.size(); i++) {
        for (int j = i + 1; j < efsmTRList.size(); j++) {
          final EFSMTransitionRelation efsmTR1 = efsmTRList.get(i);
          final EFSMTransitionRelation efsmTR2 = efsmTRList.get(j);
          final List<EFSMTransitionRelation> list =
            new ArrayList<EFSMTransitionRelation>(2);
          if (efsmTR1.compareTo(efsmTR2) <= 0) {
            list.add(efsmTR1);
            list.add(efsmTR2);
          } else {
            list.add(efsmTR2);
            list.add(efsmTR1);
          }
          candidates.add(list);
        }
      }
    }
    double smallest = Double.POSITIVE_INFINITY;
    List<EFSMTransitionRelation> smallestpair = null;
    for (final List<EFSMTransitionRelation> candidate : candidates) {
      final double candidateValue = getHeuristicValue(candidate);
      if (candidateValue < smallest) {
        smallest = candidateValue;
        smallestpair = candidate;
      }
    }
    return smallestpair;
  }

  public abstract double getHeuristicValue(List<EFSMTransitionRelation> candidate)
    throws AnalysisException, EvalException;


  //#########################################################################
  //# Invocation
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
