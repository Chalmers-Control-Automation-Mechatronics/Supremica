package net.sourceforge.waters.analysis.efsm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

class ChainVariableSelectionHeuristic
{

  //#########################################################################
  //# Constructors
  public ChainVariableSelectionHeuristic(final ModuleProxyFactory factory,
                                         final CompilerOperatorTable op,
                                         final List<VariableSelectionHeuristic> list)
  {
    mVariableSelectionHeuristicList = list;
  }


  //#########################################################################
  //# Invocation
  public EFSMVariable selectVariable (final EFSMSystem system)
    throws AnalysisException, EvalException
  {
    mCache = null;
    final List<EFSMVariable> variableList = system.getVariables();
    switch (variableList.size()) {
    case 0:
      return null;
    case 1:
      return variableList.get(0);
    default:
      break;
    }
    final EFSMVariableContext context = system.getVariableContext();
    mCache =
      new HashMap<EFSMVariable,EFSMTransitionRelation>(variableList.size());
    for (final VariableSelectionHeuristic heuristic : mVariableSelectionHeuristicList) {
      heuristic.setup(context, mCache);
    }
    final int heuristicSize = mVariableSelectionHeuristicList.size();
    double[] smallest = new double[heuristicSize];
    Arrays.fill(smallest, Double.POSITIVE_INFINITY);
    double[] current = new double[heuristicSize];
    EFSMVariable smallestVar = null;
    for (final EFSMVariable var : variableList) {
      if (var.isLocal()) {
        Arrays.fill(current, Double.POSITIVE_INFINITY);
        int i;
        for (i=0; i < heuristicSize; i++) {
          final VariableSelectionHeuristic heuristic =
            mVariableSelectionHeuristicList.get(i);
          if (smallest[i] == Double.POSITIVE_INFINITY) {
            if (smallestVar != null) {
              smallest[i] = heuristic.getHeuristicValue(smallestVar);
            }
          }
          current[i] = heuristic.getHeuristicValue(var);
          if (current[i] < smallest[i]) {
            smallestVar = var;
            final double[] swap = current;
            current = smallest;
            smallest = swap;
            break;
          } else if (current[i] > smallest[i]){
            break;
          }
        }
        if (i == heuristicSize) {
          if (var.compareTo(smallestVar) < 0) {
            smallestVar = var;
          }
        }
      }
    }
    return smallestVar;
  }

  public EFSMTransitionRelation getUnfoldedResult(final EFSMVariable var)
  {
    if (mCache == null) {
      return null;
    } else {
      return mCache.get(var);
    }
  }


  //#########################################################################
  //# Data Members
  private final List<VariableSelectionHeuristic> mVariableSelectionHeuristicList;
  private Map<EFSMVariable,EFSMTransitionRelation> mCache;
}
