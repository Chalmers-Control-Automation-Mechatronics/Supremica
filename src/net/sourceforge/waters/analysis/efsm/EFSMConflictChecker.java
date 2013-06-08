//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFSMConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import net.sourceforge.waters.analysis.tr.BFSSeachSpace;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.module.AbstractModuleConflictChecker;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class EFSMConflictChecker extends AbstractModuleConflictChecker
{

  /**
   * @param factory
   */
  public EFSMConflictChecker(final ModuleProxyFactory factory)
  {
    super(factory);
  }

  /**
   * @param model
   * @param factory
   */
  public EFSMConflictChecker(final ModuleProxy model, final ModuleProxyFactory factory)
  {
    super(model, factory);
  }

  /**
   * @param model
   * @param marking
   * @param factory
   */
  public EFSMConflictChecker(final ModuleProxy model, final IdentifierProxy marking,
                             final ModuleProxyFactory factory)
  {
    super(model, marking, factory);
  }


  //#########################################################################
  //# Simple Access
  public DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  public void setDocumentManager(final DocumentManager document)
  {
    mDocumentManager = document;
  }

  public CompilerOperatorTable getCompilerOperatorTable()
  {
    return mCompilerOperatorTable;
  }

  public void setCompilerOperatorTable(final CompilerOperatorTable op)
  {
    mCompilerOperatorTable = op;
  }

  public EFSMTRSimplifierFactory getSimplifierFactory()
  {
    return mEFSMTRSimplifierFactory;
  }

  public void setSimplifierFactory(final EFSMTRSimplifierFactory factory)
  {
    mEFSMTRSimplifierFactory = factory;
  }

  public ChainVariableSelectionHeuristic getChainVariableSelectionHeuristic()
  {
    return mChainVariableSelectionHeuristic;
  }

  public void setChainVariableSelectionHeuristic(final ChainVariableSelectionHeuristic chain)
  {
    mChainVariableSelectionHeuristic = chain;
  }

  public CompositionSelectionHeuristic getCompositionSelectionHeuristic()
  {
    return mCompositionSelectionHeuristic;
  }

  public void setCompositionSelectionHeuristic(final CompositionSelectionHeuristic heuristic)
  {
    mCompositionSelectionHeuristic = heuristic;
  }


  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  public int getInternalTransitionLimit()
  {
    return mInternalTransitionLimit;
  }

  public void setInternalTransitionLimit(final int limit)
  {
    mInternalTransitionLimit = limit;
  }

  public CompilerOperatorTable getOperatorTable()
  {
    return mCompilerOperatorTable;
  }


  //#########################################################################
  //# Invocation
  @Override
  public void setUp()
    throws EvalException, AnalysisException
  {
    super.setUp();
    final ModuleProxyFactory factory = getFactory();
    if (mCompilerOperatorTable == null) {
      mCompilerOperatorTable = CompilerOperatorTable.getInstance();
    }
    if (mDocumentManager == null) {
      mDocumentManager = new DocumentManager();
    }
    if (mChainVariableSelectionHeuristic == null) {
      final List <VariableSelectionHeuristic> defaultVariableSelectionHeuristicList =
        new ArrayList<VariableSelectionHeuristic>();
//      final MaxTrueVariableSelectionHeuristic maxT =
//        new MaxTrueVariableSelectionHeuristic(factory, mCompilerOperatorTable);
//      defaultVariableSelectionHeuristicList.add(maxT);
//      final MinStatesVariableSelectionHeuristic minS =
//        new MinStatesVariableSelectionHeuristic(factory, mCompilerOperatorTable);
//      defaultVariableSelectionHeuristicList.add(minS);
      final EstimatedMinStatesVariableSelectionHeuristic minES =
        new EstimatedMinStatesVariableSelectionHeuristic(factory, mCompilerOperatorTable);
      defaultVariableSelectionHeuristicList.add(minES);
      mChainVariableSelectionHeuristic=
        new ChainVariableSelectionHeuristic
        (factory, mCompilerOperatorTable, defaultVariableSelectionHeuristicList);
    }

    if (mCompositionSelectionHeuristic == null) {
      mCompositionSelectionHeuristic =
        new MinSynchCompositionSelectionHeuristic(factory, mCompilerOperatorTable);
    }

    if (mEFSMTRSimplifierFactory == null) {
      mEFSMTRSimplifierFactory = EFSMTRSimplifierFactory.NB;
    }
    final EFSMConflictCheckerAnalysisResult result = getAnalysisResult();
    mEFSMSynchronization = new EFSMSynchronization(factory);
    result.addSynchronousProductStatistics(mEFSMSynchronization.getStatistics());
    mVariablePartitionComputer =
      new EFSMVariablePartitionComputer(factory, mCompilerOperatorTable);
    result.addPartitioningStatistics(mVariablePartitionComputer.getStatistics());
    mPartialUnfolder = new PartialUnfolder(factory, mCompilerOperatorTable);
    result.addUnfoldingStatistics(mPartialUnfolder.getStatistics());
    mSimplifier = mEFSMTRSimplifierFactory.createAbstractionProcedure(this);
    result.setSimplifierStatistics(mSimplifier);
    mNonblockingChecker = new EFSMTRNonblockingChecker();
    mEFSMSystemQueue = new PriorityQueue<EFSMSystem>();
    mNextSubsystemNumber = 1;
  }

  @Override
  public void tearDown()
  {
    mCompilerOperatorTable = null;
    mDocumentManager = null;
    mChainVariableSelectionHeuristic = null;
    mCompositionSelectionHeuristic = null;
    mEFSMTRSimplifierFactory = null;
    mSimplifier = null;
    mVariablePartitionComputer = null;
    mPartialUnfolder = null;
    mEFSMSynchronization = null;
    mNonblockingChecker = null;
    mEFSMVariableCollector = null;
    mCurrentEFSMSystem = null;
    mEFSMSystemQueue = null;
    super.tearDown();
  }

  @Override
  protected EFSMConflictCheckerAnalysisResult createAnalysisResult()
  {
    return new EFSMConflictCheckerAnalysisResult();
  }

  @Override
  public EFSMConflictCheckerAnalysisResult getAnalysisResult()
  {
    return (EFSMConflictCheckerAnalysisResult) super.getAnalysisResult();
  }


  @Override
  public boolean run()
    throws EvalException, AnalysisException
  {
    try {
      setUp();
      final ModuleProxy module = getModel();
      final List<ParameterBindingProxy> binding = getBinding();
      final EFSMCompiler compiler =
        new EFSMCompiler(mDocumentManager, module);
      compiler.setConfiguredDefaultMarking(getConfiguredDefaultMarking());
      mCurrentEFSMSystem = compiler.compile(binding);
      final EFSMConflictCheckerAnalysisResult result = getAnalysisResult();
      result.setEFSMSystem(mCurrentEFSMSystem);
      mSystemName = mCurrentEFSMSystem.getName();
      final EFSMVariableContext context = mCurrentEFSMSystem.getVariableContext();
      mEFSMVariableCollector =
        new EFSMVariableCollector(mCompilerOperatorTable, context);
      final List<EFSMTransitionRelation> efsmTransitionRelationList =
        mCurrentEFSMSystem.getTransitionRelations();
      final ListIterator<EFSMTransitionRelation> iter =
        efsmTransitionRelationList.listIterator();
      while (iter.hasNext()) {
        final EFSMTransitionRelation currentEFSMTransitionRelation =
          iter.next();
        final EFSMTransitionRelation efsmTR =
          simplify(currentEFSMTransitionRelation);
        if (efsmTR != null) {
          iter.set(efsmTR);
          efsmTR.register();
          currentEFSMTransitionRelation.dispose();
        }
      }
      if (isCurrentSubsystemTrivial()) {
        if (getAnalysisResult().isFinished()) {
          return false;
        } else {
          return setSatisfiedResult();
        }
      }
      splitCurrentSubsystem();
      return runCompositionalMinimization();
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean runCompositionalMinimization()
    throws AnalysisException, EvalException
  {
    final EFSMConflictCheckerAnalysisResult result = getAnalysisResult();
    while (!mCurrentEFSMSystem.getTransitionRelations().isEmpty()) {
      if (isCurrentSubsystemTrivial()) {
        if (getAnalysisResult().isFinished()) {
          return false;
        } else if (mEFSMSystemQueue.isEmpty()) {
          return setSatisfiedResult();
        } else {
          mCurrentEFSMSystem = mEFSMSystemQueue.remove();
          continue;
        }
      }
      final List<EFSMTransitionRelation> efsmTransitionRelationList =
        mCurrentEFSMSystem.getTransitionRelations();
      final EFSMVariable varSelected =
        mChainVariableSelectionHeuristic.selectVariable(mCurrentEFSMSystem);
      if (varSelected != null) {
        result.addCompositionAttempt();
        final EFSMTransitionRelation varEFSMTransitionRelation =
          varSelected.getTransitionRelation();
        if (varEFSMTransitionRelation == null) {
          removeVariable(varSelected);
          continue;
        }
        final List<int[]> partition =
          mVariablePartitionComputer.computePartition(varSelected, mCurrentEFSMSystem);
        final EFSMTransitionRelation unfoldTR =
          mPartialUnfolder.unfold(varEFSMTransitionRelation, varSelected,
                                  mCurrentEFSMSystem, partition);
        result.addEFSMTransitionRelation(unfoldTR);
        EFSMTransitionRelation unfoldSimplified = null;
        if (efsmTransitionRelationList.size() > 1) {
          unfoldSimplified = simplify(unfoldTR);
        }
        if (unfoldSimplified == null) {
          unfoldSimplified = unfoldTR;
        }
        removeVariable(varSelected);
        unfoldSimplified.register();
        varEFSMTransitionRelation.dispose();
        final ListIterator<EFSMTransitionRelation> unfoldIter =
          efsmTransitionRelationList.listIterator();
        while (unfoldIter.hasNext()) {
          final EFSMTransitionRelation currentEFSMTransitionRelation =
            unfoldIter.next();
          if (currentEFSMTransitionRelation == varEFSMTransitionRelation) {
            unfoldIter.set(unfoldSimplified);
            break;
          }
        }
        if (unfoldSimplified.getVariables().size() + 1 <
            varEFSMTransitionRelation.getVariables().size()) {
          splitCurrentSubsystem();
        }
      } else if (efsmTransitionRelationList.size() > 1) {
        result.addCompositionAttempt();
        final List<EFSMTransitionRelation> selectedTR =
          mCompositionSelectionHeuristic.selectComposition(mCurrentEFSMSystem);
        final EFSMTransitionRelation TR1 = selectedTR.get(0);
        final EFSMTransitionRelation TR2 = selectedTR.get(1);
        final EFSMTransitionRelation synchTR =
          mEFSMSynchronization.synchronize(TR1, TR2);
        result.addEFSMTransitionRelation(synchTR);
        final EFSMVariableContext context =
          mCurrentEFSMSystem.getVariableContext();
        EFSMTransitionRelation synchSimplified =
          mSimplifier.run(synchTR, context);
        final boolean splitting;
        if (synchSimplified == null) {
          synchSimplified = synchTR;
          splitting = false;
        } else {
          splitting =
            synchTR.getVariables().size() < synchSimplified.getVariables().size();
        }
        efsmTransitionRelationList.remove(TR1);
        efsmTransitionRelationList.remove(TR2);
        TR1.dispose();
        TR2.dispose();
        efsmTransitionRelationList.add(synchSimplified);
        synchSimplified.register();
        if (splitting) {
          splitCurrentSubsystem();
        }
      } else if (efsmTransitionRelationList.size() == 1) {
        // If there is only one EFSM left:
        // - if it is blocking, then we are done.
        // - if it is nonblocking, check the next disjoint subsystem.
        final EFSMTransitionRelation finalEFSMTR =
          efsmTransitionRelationList.get(0);
        if (!mNonblockingChecker.run(finalEFSMTR)) {
          return setBooleanResult(false);
        } else {
          mCurrentEFSMSystem.removeTransitionRelation(finalEFSMTR);
        }
      }
    }
    return setSatisfiedResult();
  }

  private EFSMTransitionRelation simplify
    (final EFSMTransitionRelation currentEFSMTransitionRelation)
  throws AnalysisException
  {
    final EFSMVariableContext context = mCurrentEFSMSystem.getVariableContext();
    final EFSMTransitionRelation result =
      mSimplifier.run(currentEFSMTransitionRelation, context);
    // If the simplifier has detected and removed selfloops,
    // i.e., updates that appear as selfloop and all states and nowhere else,
    // we must record them on the variables, so they can be considered later
    // in partial unfolding.
    final Collection<ConstraintList> selfloops =
      mSimplifier.getSelfloopedUpdates();
    if (!selfloops.isEmpty()) {
      for (final ConstraintList update : mSimplifier.getSelfloopedUpdates()) {
        final Collection<EFSMVariable> unprimed = new THashSet<EFSMVariable>();
        final Collection<EFSMVariable> primed = new THashSet<EFSMVariable>();
        mEFSMVariableCollector.collectAllVariables(update, unprimed, primed);
        // Skipping pure guards ...
        if (!primed.isEmpty()) {
          for (final EFSMVariable var : unprimed) {
            var.addSelfloop(update);
          }
          for (final EFSMVariable var : primed) {
            var.addSelfloop(update);
          }
        }
      }
    }
    return result;
  }

  private boolean isCurrentSubsystemTrivial()
  {
    final List<EFSMTransitionRelation> efsmTRList =
      mCurrentEFSMSystem.getTransitionRelations();
    boolean allMarked = true;
    for (final EFSMTransitionRelation efsmTR : efsmTRList) {
      final ListBufferTransitionRelation rel = efsmTR.getTransitionRelation();
      if (rel.isUsedProposition(0)) {
        boolean someMarked = false;
        for (int s = 0; s < rel.getNumberOfStates(); s++) {
          if (rel.isMarked(s, 0)) {
            someMarked = true;
            break;
          }
        }
        if (!someMarked) {
          setBooleanResult(false);
          return true;
        }
        allMarked = false;
      }
    }
    return allMarked;
  }

  private boolean splitCurrentSubsystem()
  {
    final EFSMSystem splitResult = splitSubsystem(mCurrentEFSMSystem);
    if (splitResult == mCurrentEFSMSystem) {
      return false;
    } else {
      mEFSMSystemQueue.add(splitResult);
      mCurrentEFSMSystem = mEFSMSystemQueue.remove();
      return true;
    }
  }

  private EFSMSystem splitSubsystem(final EFSMSystem system)
  {
    final List<EFSMTransitionRelation> trList =
      system.getTransitionRelations();
    final int trListSize = trList.size();
    if (trListSize == 0) {
      return system;
    }
    final BFSSeachSpace<EFSMTransitionRelation> trSearchSpace =
      new BFSSeachSpace<EFSMTransitionRelation>(trListSize);
    final EFSMTransitionRelation firstTR = trList.get(0);
    trSearchSpace.add(firstTR);
    final List<EFSMVariable> varList = system.getVariables();
    final int varListSize = varList.size();
    final BFSSeachSpace<EFSMVariable> varSearchSpace =
      new BFSSeachSpace<EFSMVariable>(varListSize);
    while (!trSearchSpace.isEmpty() || !varSearchSpace.isEmpty()) {
      if (trSearchSpace.isEmpty()) {
        final EFSMVariable var = varSearchSpace.remove();
        trSearchSpace.addAll(var.getTransitionRelations());
        final EFSMEventEncoding selfloops = var.getSelfloops();
        mEFSMVariableCollector.collectAllVariables(selfloops, varSearchSpace);
      } else {
        final EFSMTransitionRelation tr = trSearchSpace.remove();
        varSearchSpace.addAll(tr.getVariables());
      }
      if (trSearchSpace.visitedSize() == trListSize &&
          varSearchSpace.visitedSize() == varListSize) {
        return system;
      }
    }
    final String name1 = mSystemName + "-" + mNextSubsystemNumber;
    mNextSubsystemNumber++;
    final String name2 = mSystemName + "-" + mNextSubsystemNumber;
    mNextSubsystemNumber ++;
    final int trVisitedSize = trSearchSpace.visitedSize();
    final List<EFSMTransitionRelation> trList1 =
      new ArrayList<EFSMTransitionRelation>(trVisitedSize);
    final List<EFSMTransitionRelation> trList2 =
      new ArrayList<EFSMTransitionRelation>(trListSize - trVisitedSize);
    for (final EFSMTransitionRelation tr : trList) {
      if (trSearchSpace.isVisited(tr)) {
        trList1.add(tr);
      } else {
        trList2.add(tr);
      }
    }
    final int varVisitedSize = varSearchSpace.visitedSize();
    final List<EFSMVariable> varList1 =
      new ArrayList<EFSMVariable>(varVisitedSize);
    final List<EFSMVariable> varList2 =
      new ArrayList<EFSMVariable>(varListSize - varVisitedSize);
    for (final EFSMVariable var : varList) {
      if (varSearchSpace.isVisited(var)) {
        varList1.add(var);
      } else {
        varList2.add(var);
      }
    }
    final EFSMVariableContext context = system.getVariableContext();
    final EFSMSystem system1 =
      new EFSMSystem(name1, varList1, trList1, context);
    final EFSMSystem system2 =
      new EFSMSystem(name2, varList2, trList2, context);
    mEFSMSystemQueue.add(system1);
    return splitSubsystem(system2);
  }

  /**
   * Removes the given variable from the current EFSM system,
   * and cleans up references to its selfloops mentioned in other variables.
   * This method is called after partial unfolding.
   * Any selfloops will be added back in after simplification.
   * @param var
   */
  private void removeVariable(final EFSMVariable var)
  {
    final EFSMEventEncoding selfloops = var.getSelfloops();
    if (selfloops.size() > 1) {
      final Map<EFSMVariable,List<ConstraintList>> victims =
        new HashMap<EFSMVariable,List<ConstraintList>>();
      for (int e = EventEncoding.NONTAU; e < selfloops.size(); e++) {
        final ConstraintList update = selfloops.getUpdate(e);
        final Collection<EFSMVariable> vars = new THashSet<EFSMVariable>();
        mEFSMVariableCollector.collectAllVariables(update, vars);
        for (final EFSMVariable otherVar : vars) {
          if (otherVar != var) {
            List<ConstraintList> list = victims.get(otherVar);
            if (list == null) {
              list = new LinkedList<ConstraintList>();
              victims.put(otherVar, list);
            }
            list.add(update);
          }
        }
      }
      for (final Map.Entry<EFSMVariable,List<ConstraintList>> entry :
           victims.entrySet()) {
        final EFSMVariable otherVar = entry.getKey();
        final List<ConstraintList> list = entry.getValue();
        otherVar.removeSelfloops(list);
      }
    }
    mCurrentEFSMSystem.removeVariable(var);
  }


  //#########################################################################
  //# Data Members
  private CompilerOperatorTable mCompilerOperatorTable;
  private EFSMTRSimplifierFactory mEFSMTRSimplifierFactory;
  private ChainVariableSelectionHeuristic mChainVariableSelectionHeuristic;
  private int mInternalTransitionLimit = Integer.MAX_VALUE;

  private DocumentManager mDocumentManager;
  private EFSMTRSimplifier mSimplifier;
  private EFSMVariablePartitionComputer mVariablePartitionComputer;
  private PartialUnfolder mPartialUnfolder;
  private CompositionSelectionHeuristic mCompositionSelectionHeuristic;
  private EFSMSynchronization mEFSMSynchronization;
  private EFSMTRNonblockingChecker mNonblockingChecker;
  private EFSMVariableCollector mEFSMVariableCollector;
  private Queue<EFSMSystem> mEFSMSystemQueue;
  private String mSystemName;
  private int mNextSubsystemNumber;
  private EFSMSystem mCurrentEFSMSystem;



}
