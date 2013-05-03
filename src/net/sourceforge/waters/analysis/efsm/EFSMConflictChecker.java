//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFSMConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.module.AbstractModuleConflictChecker;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
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
      final MaxTrueVariableSelectionHeuristic maxT =
        new MaxTrueVariableSelectionHeuristic(factory, mCompilerOperatorTable);
      defaultVariableSelectionHeuristicList.add(maxT);
      final MinStatesVariableSelectionHeuristic minS =
        new MinStatesVariableSelectionHeuristic(factory, mCompilerOperatorTable);
      defaultVariableSelectionHeuristicList.add(minS);
      mChainVariableSelectionHeuristic=
        new ChainVariableSelectionHeuristic
        (factory, mCompilerOperatorTable, defaultVariableSelectionHeuristicList);
    }

    if (mCompositionSelectionHeuristic == null) {
      mCompositionSelectionHeuristic =
        new MaxTrueCompositionSelectionHeuristic(factory, mCompilerOperatorTable);
    }

    if (mEFSMTRSimplifierFactory == null) {
      mEFSMTRSimplifierFactory = EFSMTRSimplifierFactory.GNB;
    }
    mSimplifier = mEFSMTRSimplifierFactory.createAbstractionProcedure(this);
    mPartialUnfolder = new PartialUnfolder(factory, mCompilerOperatorTable);
    mEFSMSynchronization = new EFSMSynchronization(factory);
    mNonblockingChecker = new EFSMTRNonblockingChecker();
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
    mPartialUnfolder = null;
    mEFSMSynchronization = null;
    mNonblockingChecker = null;
    super.tearDown();
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
      final EFSMSystem system = compiler.compile(binding);
      final EFSMVariableContext context = system.getVariableContext();
      final List<EFSMTransitionRelation> efsmTransitionRelationList =
        system.getTransitionRelations();
      final ListIterator<EFSMTransitionRelation> iter =
        efsmTransitionRelationList.listIterator();
      while (iter.hasNext()) {
        final EFSMTransitionRelation currentEFSMTransitionRelation =
          iter.next();
        final EFSMTransitionRelation result =
          mSimplifier.run(currentEFSMTransitionRelation, context);
        if (result != null) {
          iter.set(result);
          currentEFSMTransitionRelation.dispose();
        }
      }
      return simplifierChain(system, context);
    } finally {
      tearDown();
    }
  }

  private boolean simplifierChain(final EFSMSystem system,
                                  final EFSMVariableContext context)
    throws AnalysisException, EvalException
  {
    final List<EFSMTransitionRelation> efsmTransitionRelationList =
      system.getTransitionRelations();
    while (!efsmTransitionRelationList.isEmpty()) {
      final EFSMVariable varSelected =
        mChainVariableSelectionHeuristic.selectVariable(system);
      if (varSelected != null) {
        final EFSMTransitionRelation varEFSMTransitionRelation =
          varSelected.getTransitionRelation();
        EFSMTransitionRelation unfoldTR =
          mChainVariableSelectionHeuristic.getUnfoldedResult(varSelected);
        if (unfoldTR == null) {
          unfoldTR = mPartialUnfolder.unfold(varEFSMTransitionRelation,
                                             varSelected, context);
        }
        EFSMTransitionRelation unfoldSimplified = null;
        if (efsmTransitionRelationList.size() > 1) {
          unfoldSimplified = mSimplifier.run(unfoldTR, context);
        }
        varEFSMTransitionRelation.dispose();
        final ListIterator<EFSMTransitionRelation> unfoldIter =
          efsmTransitionRelationList.listIterator();
        while (unfoldIter.hasNext()) {
          final EFSMTransitionRelation currentEFSMTransitionRelation =
            unfoldIter.next();
          if (currentEFSMTransitionRelation == varEFSMTransitionRelation) {
            if (unfoldSimplified != null) {
              unfoldIter.set(unfoldSimplified);
              unfoldTR.dispose();
            } else {
              unfoldIter.set(unfoldTR);
            }
          }
        }
      } else if (efsmTransitionRelationList.size() > 1){
        final List<EFSMTransitionRelation> selectedTR =
          mCompositionSelectionHeuristic.selectComposition(system);
        final EFSMTransitionRelation TR1 = selectedTR.get(0);
        final EFSMTransitionRelation TR2 = selectedTR.get(1);
        final EFSMTransitionRelation synchTR =
          mEFSMSynchronization.synchronize(TR1, TR2);
        final EFSMTransitionRelation synchSimplified =
          mSimplifier.run(synchTR, context);
        efsmTransitionRelationList.remove(TR1);
        efsmTransitionRelationList.remove(TR2);
        TR1.dispose();
        TR2.dispose();
        if (synchSimplified == null) {
          efsmTransitionRelationList.add(synchTR);
        } else {
          efsmTransitionRelationList.add(synchSimplified);
          synchTR.dispose();
        }
      } else if (efsmTransitionRelationList.size() == 1){
        final EFSMTransitionRelation finalEFSMTR = efsmTransitionRelationList.get(0);
        final boolean result = mNonblockingChecker.run(finalEFSMTR);
        return setBooleanResult(result);
      }
    }
    return setSatisfiedResult();
  }



    @Override
    public boolean supportsNondeterminism()
    {
      return true;
    }

    public int getInternalTransitionLimit()
    {
      // TODO Auto-generated method stub
      return 0;
    }

    public CompilerOperatorTable getOperatorTable()
    {
      // TODO Auto-generated method stub
      return null;
    }
    //#########################################################################
    //# Data Members

    private DocumentManager mDocumentManager;
    private EFSMTRSimplifier mSimplifier;
    private CompilerOperatorTable mCompilerOperatorTable;
    private EFSMTRSimplifierFactory mEFSMTRSimplifierFactory;
    private ChainVariableSelectionHeuristic mChainVariableSelectionHeuristic;
    private PartialUnfolder mPartialUnfolder;
    private CompositionSelectionHeuristic mCompositionSelectionHeuristic;
    private EFSMSynchronization mEFSMSynchronization;
    private EFSMTRNonblockingChecker mNonblockingChecker;
    }
