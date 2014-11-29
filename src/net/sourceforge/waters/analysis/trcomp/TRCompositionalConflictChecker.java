//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRCompositionalConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplificationListener;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.compositional.CompositionalAnalysisResult;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.TraceChecker;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.apache.log4j.Logger;


/**
 * @author Robi Malik
 */

public class TRCompositionalConflictChecker
  extends AbstractTRCompositionalVerifier
  implements ConflictChecker
{

  //#########################################################################
  //# Constructors
  public TRCompositionalConflictChecker()
  {
    this(null);
  }

  public TRCompositionalConflictChecker(final ProductDESProxy model)
  {
    super(model,
          ConflictKindTranslator.getInstanceControllable(),
          new NativeConflictChecker(ProductDESElementFactory.getInstance()));
    final TransitionRelationSimplifier chain = createDefaultAbstractionChain();
    setSimplifier(chain);
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.ConflictChecker
  @Override
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mConfiguredDefaultMarking = marking;
  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    return mConfiguredDefaultMarking;
  }

  @Override
  public void setConfiguredPreconditionMarking(final EventProxy marking)
  {
    mConfiguredPreconditionMarking = marking;
  }

  @Override
  public EventProxy getConfiguredPreconditionMarking()
  {
    return mConfiguredPreconditionMarking;
  }

  @Override
  public ConflictTraceProxy getCounterExample()
  {
    final VerificationResult result = getAnalysisResult();
    return (ConflictTraceProxy) result.getCounterExample();
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void tearDown()
  {
    super.tearDown();
    mUsedDefaultMarking = null;
  }


  //#########################################################################
  //# Hooks
  @Override
  protected boolean isFailingEventsUsed()
  {
    return isFailingEventsEnabled() && getUsedPreconditionMarking() == null;
  }

  @Override
  protected ChainTRSimplifier createDefaultAbstractionChain()
  {
    final ChainTRSimplifier chain = super.createDefaultAbstractionChain();
    final TRSimplificationListener listener = new PartitioningListener();
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    loopRemover.setSimplificationListener(listener);
    chain.add(loopRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(ObservationEquivalenceTRSimplifier.
                               Equivalence.OBSERVATION_EQUIVALENCE);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.SATURATE);
    final int limit = getInternalTransitionLimit();
    bisimulator.setTransitionLimit(limit);
    bisimulator.setSimplificationListener(listener);
    chain.add(bisimulator);
    chain.setPropositions(PRECONDITION_MARKING, DEFAULT_MARKING);
    chain.setPreferredOutputConfiguration
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    return chain;
  }

  @Override
  protected EventProxy getUsedDefaultMarking()
    throws EventNotFoundException
  {
    if (mUsedDefaultMarking == null) {
      if (mConfiguredDefaultMarking == null) {
        final ProductDESProxy model = getModel();
        mUsedDefaultMarking = AbstractConflictChecker.getMarkingProposition(model);
      } else {
        mUsedDefaultMarking = mConfiguredDefaultMarking;
      }
    }
    return mUsedDefaultMarking;
  }

  @Override
  protected EventProxy getUsedPreconditionMarking()
  {
    // TODO Create precondition marking for GNB chain
    return getConfiguredPreconditionMarking();
  }

  @Override
  protected boolean earlyTerminationCheck(final TRSubsystemInfo subsys)
  {
    boolean allStatesMarked = true;
    for (final TRAutomatonProxy aut : subsys.getAutomata()) {
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      if (rel.isPropositionUsed(DEFAULT_MARKING)) {
        boolean noStatesMarked = true;
        for (int s = 0; s < rel.getNumberOfStates(); s++) {
          if (rel.isReachable(s)) {
            if (rel.isMarked(s, DEFAULT_MARKING)) {
              noStatesMarked = false;
            } else {
              allStatesMarked = false;
            }
            if (!allStatesMarked && !noStatesMarked) {
              break;
            }
          }
        }
        if (noStatesMarked) {
          final AnalysisResult result = getAnalysisResult();
          result.setSatisfied(false);
          getLogger().debug("Subsystem is blocking, because " + aut.getName() +
                            " has no marked states.");
          dropSubsystem(subsys);
          return true;
        }
      }
    }
    // TODO Generalised nonblocking stuff ...
    if (allStatesMarked) {
      final Logger logger = getLogger();
      logger.debug("Subsystem is nonblocking, because all states are marked.");
      dropSubsystem(subsys);
    }
    return allStatesMarked;
  }

  @Override
  protected void analyseSubsystemMonolithically(final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    final List<TRAutomatonProxy> automata = subsys.getAutomata();
    if (!automata.isEmpty()) {
      final String name = AutomatonTools.getCompositionName(automata);
      final ProductDESProxyFactory factory = getFactory();
      final ProductDESProxy des =
        AutomatonTools.createProductDESProxy(name, automata, factory);
      recordMonolithicAttempt(automata);
      final ModelVerifier mono = getMonolithicVerifier();
      mono.setModel(des);
      mono.run();
      final AnalysisResult monolithicResult = mono.getAnalysisResult();
      final CompositionalAnalysisResult combinedResult = getAnalysisResult();
      combinedResult.addMonolithicAnalysisResult(monolithicResult);
      final Logger logger = getLogger();
      if (monolithicResult.isSatisfied()) {
        logger.debug("Subsystem is nonblocking.");
        dropSubsystem(subsys);
      } else {
        logger.debug("Subsystem is blocking.");
        combinedResult.setSatisfied(false);
        if (isCounterExampleEnabled()) {
          dropPendingSubsystems();
          final List<TRAbstractionStep> preds = getAbstractionSteps(automata);
          final TraceProxy trace = mono.getCounterExample();
          final TRAbstractionStep step =
            new TRAbstractionStepMonolithic(name, preds, trace);
          addAbstractionStep(step);
        }
      }
    }
  }

  @Override
  protected TRConflictTraceProxy createEmptyTrace(final ProductDESProxy des)
  {
    return new TRConflictTraceProxy(des);
  }

  @Override
  protected void checkIntermediateCounterExample(final TRTraceProxy trace)
    throws AnalysisException
  {
    final TRConflictTraceProxy conflictTrace = (TRConflictTraceProxy) trace;
    final TRConflictTraceProxy cloned =
      new TRConflictTraceProxy(conflictTrace);
    cloned.setUpForTraceChecking();
    final KindTranslator translator = getKindTranslator();
    TraceChecker.checkConflictCounterExample(cloned,
                                             getUsedPreconditionMarking(),
                                             getUsedDefaultMarking(),
                                             true,
                                             translator);
  }


  //#########################################################################
  //# Data Members
  // Configuration
  private EventProxy mConfiguredDefaultMarking;
  private EventProxy mConfiguredPreconditionMarking;

  private EventProxy mUsedDefaultMarking;

}
