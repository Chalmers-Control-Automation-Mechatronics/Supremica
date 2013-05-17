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
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

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
          mCurrentEFSMSystem.removeVariable(varSelected);
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
        mCurrentEFSMSystem.removeVariable(varSelected);
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
      } else if (efsmTransitionRelationList.size() == 1){
        final EFSMTransitionRelation finalEFSMTR = efsmTransitionRelationList.get(0);
        final boolean nonblocking = mNonblockingChecker.run(finalEFSMTR);
        return setBooleanResult(nonblocking);
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
    final EFSMEventEncoding selfloops = mCurrentEFSMSystem.getSelfloops();
    for (final ConstraintList update : mSimplifier.getSelfloopedUpdates()) {
      selfloops.createEventId(update);
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
        for (int i=0; i < rel.getNumberOfStates(); i++) {
          if (rel.isMarked(i, 0)) {
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
    final List<EFSMTransitionRelation> efsmTRList =
      system.getTransitionRelations();
    final int efsmTRListSize = efsmTRList.size();
    if (efsmTRListSize == 0) {
      return system;
    }
    final Set<EFSMTransitionRelation> visitedEFSMTRSet =
      new THashSet<EFSMTransitionRelation>(efsmTRListSize);
    final Queue<EFSMTransitionRelation> visitedEFSMTRList =
      new LinkedList<EFSMTransitionRelation>();
    final List<EFSMVariable> efsmVariableList =
      system.getVariables();
    final int efsmVariableListSize = efsmVariableList.size();
    final Set<EFSMVariable> visitedEFSMVariableSet =
      new THashSet<EFSMVariable>(efsmVariableListSize);
    final Queue<EFSMVariable> visitedEFSMVariableList =
      new LinkedList<EFSMVariable>();
    List<Set<EFSMVariable>> selfloopData = null;
    final TIntHashSet visitedSelfloopSet = new TIntHashSet();
    final EFSMTransitionRelation firstTR = efsmTRList.get(0);
    visitedEFSMTRSet.add(firstTR);
    visitedEFSMTRList.add(firstTR);
    while (!visitedEFSMTRList.isEmpty() || !visitedEFSMVariableList.isEmpty()) {
      while (!visitedEFSMTRList.isEmpty()) {
        final EFSMTransitionRelation nextTR = visitedEFSMTRList.remove();
        final Collection<EFSMVariable> nextVariableList = nextTR.getVariables();
        for (final EFSMVariable var : nextVariableList) {
          if (visitedEFSMVariableSet.add(var)) {
            if (visitedEFSMVariableSet.size() < efsmVariableListSize) {
              visitedEFSMVariableList.add(var);
            } else if (visitedEFSMTRSet.size() < efsmTRListSize) {
              visitedEFSMVariableList.add(var);
              break;
            } else {
              return system;
            }
          }
        }
      }
      while (!visitedEFSMVariableList.isEmpty()) {
        final EFSMVariable nextVariable = visitedEFSMVariableList.remove();
        final Collection<EFSMTransitionRelation> nextEFSMTRList =
          nextVariable.getTransitionRelations();
        for (final EFSMTransitionRelation tr : nextEFSMTRList) {
          if (visitedEFSMTRSet.add(tr)) {
            if (visitedEFSMTRSet.size() < efsmTRListSize) {
              visitedEFSMTRList.add(tr);
            } else if (visitedEFSMVariableSet.size() < efsmVariableListSize) {
              visitedEFSMTRList.add(tr);
              break;
            } else {
              return system;
            }
          }
        }
      }
      if (visitedEFSMTRList.isEmpty()) {
        final EFSMEventEncoding selfloops = system.getSelfloops();
        if (selfloopData == null) {
          selfloopData = new ArrayList<Set<EFSMVariable>>(selfloops.size());
          selfloopData.add(null);  // this is for tau
          for (int event=EventEncoding.NONTAU; event < selfloops.size(); event++) {
            final ConstraintList update = selfloops.getUpdate(event);
            final Set<EFSMVariable> variables = new THashSet<EFSMVariable>();
            mEFSMVariableCollector.collectAllVariables(update, variables);
            selfloopData.add(variables);
          }
        }
        for (int event=EventEncoding.NONTAU; event < selfloops.size(); event++) {
          final Set<EFSMVariable> variables = selfloopData.get(event);
          boolean found = false;
          for (final EFSMVariable var : variables) {
            if (visitedEFSMVariableSet.contains(var)){
              found = true;
            }
          }
          if (found) {
            visitedSelfloopSet.add(event);
            for (final EFSMVariable var : variables) {
              if (visitedEFSMVariableSet.add(var)) {
                if (visitedEFSMTRSet.size() == efsmTRListSize &&
                    visitedEFSMVariableSet.size() == efsmVariableListSize) {
                  return system;
                }
                visitedEFSMVariableList.add(var);
              }
            }
          }
        }
      }
    }

    final List<EFSMTransitionRelation> efsmTRList1 =
      new ArrayList<EFSMTransitionRelation>(visitedEFSMTRSet.size());
    final List<EFSMTransitionRelation> efsmTRList2 =
      new ArrayList<EFSMTransitionRelation>(efsmTRListSize - visitedEFSMTRSet.size());
    for (final EFSMTransitionRelation tr : efsmTRList) {
      if (visitedEFSMTRSet.contains(tr)) {
        efsmTRList1.add(tr);
      } else {
        efsmTRList2.add(tr);
      }
    }
    final List<EFSMVariable> efsmVariableList1 =
      new ArrayList<EFSMVariable>(visitedEFSMVariableSet.size());
    final List<EFSMVariable> efsmVariableList2 =
      new ArrayList<EFSMVariable>(efsmVariableListSize - visitedEFSMVariableSet.size());
    for (final EFSMVariable var : efsmVariableList) {
      if (visitedEFSMVariableSet.contains(var)) {
        efsmVariableList1.add(var);
      } else {
        efsmVariableList2.add(var);
      }
    }
    final EFSMEventEncoding selfloops = system.getSelfloops();
    final EFSMEventEncoding selfloops1 =
      new EFSMEventEncoding(visitedSelfloopSet.size() + 1);
    final EFSMEventEncoding selfloops2 =
      new EFSMEventEncoding(selfloops.size() - visitedSelfloopSet.size() + 1);
    for (int event=EventEncoding.NONTAU; event < selfloops.size(); event++) {
      final ConstraintList update = selfloops.getUpdate(event);
      if (visitedSelfloopSet.contains(event)) {
        selfloops1.createEventId(update);
      } else {
        selfloops2.createEventId(update);
      }
    }

    final String name1 = mSystemName + "-" + mNextSubsystemNumber;
    mNextSubsystemNumber++;
    final String name2 = mSystemName + "-" + mNextSubsystemNumber;
    mNextSubsystemNumber ++;
    final EFSMVariableContext context = system.getVariableContext();
    final EFSMSystem efsmSystem1 =
      new EFSMSystem(name1, efsmVariableList1, efsmTRList1, selfloops1, context);
    final EFSMSystem efsmSystem2 =
      new EFSMSystem(name2, efsmVariableList2, efsmTRList2, selfloops2, context);
    mEFSMSystemQueue.add(efsmSystem1);
    return splitSubsystem(efsmSystem2);
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
