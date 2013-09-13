//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFSMConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import net.sourceforge.waters.analysis.efa.base.EFANonblockingChecker;
import net.sourceforge.waters.analysis.tr.BFSSearchSpace;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
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

  //#########################################################################
  //# Constructors
  public EFSMConflictChecker(final ModuleProxyFactory factory)
  {
    super(factory);
  }

  public EFSMConflictChecker(final ModuleProxy model,
                             final ModuleProxyFactory factory)
  {
    super(model, factory);
  }

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
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mEFSMSynchronizer != null) {
      mEFSMSynchronizer.requestAbort();
    }
    if (mVariablePartitionComputer != null) {
      mVariablePartitionComputer.requestAbort();
    }
    if (mPartialUnfolder != null) {
      mPartialUnfolder.requestAbort();
    }
    if (mSimplifier != null) {
      mSimplifier.requestAbort();
    }
    if (mNonblockingChecker != null) {
      mNonblockingChecker.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mEFSMSynchronizer != null) {
      mEFSMSynchronizer.resetAbort();
    }
    if (mVariablePartitionComputer != null) {
      mVariablePartitionComputer.resetAbort();
    }
    if (mPartialUnfolder != null) {
      mPartialUnfolder.resetAbort();
    }
    if (mSimplifier != null) {
      mSimplifier.resetAbort();
    }
    if (mNonblockingChecker != null) {
      mNonblockingChecker.resetAbort();
    }
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
//      final VariableSelectionHeuristic maxT =
//        new MaxTrueVariableSelectionHeuristic(factory, mCompilerOperatorTable);
//      defaultVariableSelectionHeuristicList.add(maxT);
//      final VariableSelectionHeuristic minS =
//        new MinStatesVariableSelectionHeuristic(factory, mCompilerOperatorTable);
//      defaultVariableSelectionHeuristicList.add(minS);
      final VariableSelectionHeuristic maxOc =
        new MaxOccurrenceVariableSelectionHeuristic(factory, mCompilerOperatorTable);
      defaultVariableSelectionHeuristicList.add(maxOc);
      final VariableSelectionHeuristic maxSelfloop =
        new MaxSelfloopVariableSelectionHeuristic(factory, mCompilerOperatorTable);
      defaultVariableSelectionHeuristicList.add(maxSelfloop);
      final VariableSelectionHeuristic minES =
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
    mEFSMSynchronizer = new EFSMSynchronizer(factory);
    result.addSynchronousProductStatistics(mEFSMSynchronizer.getStatistics());
    mVariablePartitionComputer =
      new EFSMVariablePartitionComputer(factory, mCompilerOperatorTable);
    result.addPartitioningStatistics(mVariablePartitionComputer.getStatistics());
    mPartialUnfolder = new EFSMPartialUnfolder(factory, mCompilerOperatorTable);
    result.addUnfoldingStatistics(mPartialUnfolder.getStatistics());
    mSimplifier = mEFSMTRSimplifierFactory.createAbstractionProcedure(this);
    result.setSimplifierStatistics(mSimplifier);
    mNonblockingChecker = new EFANonblockingChecker();
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
    mEFSMSynchronizer = null;
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
      final List<ParameterBindingProxy> binding = getBindings();
      final EFSMCompiler compiler =
        new EFSMCompiler(mDocumentManager, module);
      compiler.setConfiguredDefaultMarking(getConfiguredDefaultMarking());
      final List<String> none = Collections.emptyList();
      compiler.setEnabledPropertyNames(none);
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
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
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
        final TRPartition partition =
          mVariablePartitionComputer.computePartition(varSelected,
                                                      mCurrentEFSMSystem);
        final EFSMTransitionRelation unfoldTR =
          mPartialUnfolder.unfold(varSelected, mCurrentEFSMSystem, partition);
        result.addEFSMTransitionRelation(unfoldTR);
        recordSelfloops(mPartialUnfolder);
        EFSMTransitionRelation unfoldSimplified = null;
        if (efsmTransitionRelationList.size() > 1 ||
            mCurrentEFSMSystem.getVariables().size() > 1) {
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
          mEFSMSynchronizer.synchronize(TR1, TR2);
        result.addEFSMTransitionRelation(synchTR);
        EFSMTransitionRelation synchSimplified = simplify(synchTR);
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
    recordSelfloops(mSimplifier);
    return result;
  }

  /**
   * Stores selfloop information. If a simplifier has detected and removed
   * selfloops, i.e., updates that appear as selfloop on all states and
   * nowhere else, they must be recorded on the variables, so they can be
   * considered later in partial unfolding.
   * @param simplifier A simplifier that has just completed execution
   *                   and may contain selfloops.
   */
  private void recordSelfloops(final AbstractEFSMAlgorithm simplifier)
  {
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
    final BFSSearchSpace<EFSMTransitionRelation> trSearchSpace =
      new BFSSearchSpace<EFSMTransitionRelation>(trListSize);
    final EFSMTransitionRelation firstTR = trList.get(0);
    trSearchSpace.add(firstTR);
    final List<EFSMVariable> varList = system.getVariables();
    final int varListSize = varList.size();
    final BFSSearchSpace<EFSMVariable> varSearchSpace =
      new BFSSearchSpace<EFSMVariable>(varListSize);
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
  //# Debugging
  @SuppressWarnings("unused")
  private void checkBlocking(final EFSMTransitionRelation efsmTR)
    throws AnalysisAbortException
  {
    if (efsmTR != null) {
      final boolean nonblocking = mNonblockingChecker.run(efsmTR);
      if (!nonblocking) {
        getLogger().debug("BLOCKING!!!");
      }
    }
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
  private EFSMPartialUnfolder mPartialUnfolder;
  private CompositionSelectionHeuristic mCompositionSelectionHeuristic;
  private EFSMSynchronizer mEFSMSynchronizer;
  private EFANonblockingChecker mNonblockingChecker;
  private EFSMVariableCollector mEFSMVariableCollector;
  private Queue<EFSMSystem> mEFSMSystemQueue;
  private String mSystemName;
  private int mNextSubsystemNumber;
  private EFSMSystem mCurrentEFSMSystem;

}
