package net.sourceforge.waters.analysis.efa.efsm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

abstract class VariableSelectionHeuristic
{

  //#########################################################################
  //# Constructors
  public VariableSelectionHeuristic(final ModuleProxyFactory factory,
                                    final CompilerOperatorTable op)
  {
    mFactory = factory;
    mOperatorTable = op;
    mUnfolder = new EFSMPartialUnfolder(factory, op);
  }


  //#########################################################################
  //# Invocation
  public EFSMVariable selectVariable (final EFSMSystem system)
    throws AnalysisException, EvalException
  {
    final List<EFSMVariable> variableList = system.getVariables();
    switch (variableList.size()) {
    case 0:
      return null;
    case 1:
      return variableList.get(0);
    default:
      break;
    }
    mSystem = system;
    mUnfoldingCache =
      new HashMap<EFSMVariable,EFSMTransitionRelation>(variableList.size());
    double smallest = Double.POSITIVE_INFINITY;
    EFSMVariable smallestVar = null;
    for (final EFSMVariable var : variableList) {
      if (var.isLocal()) {
        final double varValue = getHeuristicValue(var);
        if (varValue < smallest) {
          smallest = varValue;
          smallestVar = var;
        }
      }
    }
    return smallestVar;
  }

  public abstract double getHeuristicValue(EFSMVariable var)
    throws AnalysisException, EvalException;


  //#########################################################################
  //# Simple Access
  protected ModuleProxyFactory getFactory(){
    return mFactory;
  }

  protected CompilerOperatorTable getOperatorTable()
  {
    return mOperatorTable;
  }


  //#########################################################################
  //# Auxiliary Methods
  protected EFSMTransitionRelation unfold(final EFSMVariable var)
    throws AnalysisException, EvalException
  {
    final EFSMTransitionRelation cached = mUnfoldingCache.get(var);
    if (cached != null) {
      return cached;
    } else {
      final EFSMTransitionRelation unfolded = mUnfolder.unfold(var, mSystem);
      mUnfoldingCache.put(var, unfolded);
      return unfolded;
    }
  }

  protected void setup(final EFSMSystem system,
                       final Map<EFSMVariable,EFSMTransitionRelation> cache)
  {
    mUnfoldingCache = cache;
    mSystem = system;
  }


  //#########################################################################
  //# Data Members
  private final EFSMPartialUnfolder mUnfolder;
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private EFSMSystem mSystem;
  private Map<EFSMVariable,EFSMTransitionRelation> mUnfoldingCache;
}
