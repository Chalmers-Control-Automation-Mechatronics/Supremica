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
import net.sourceforge.waters.analysis.abstraction.IncomingEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingSaturationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.NonAlphaDeterminisationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.OnlySilentOutgoingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SilentIncomingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplificationListener;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRemovalTRSimplifier;
import net.sourceforge.waters.analysis.compositional.CompositionalAnalysisResult;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
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
  extends AbstractTRCompositionalAnalyzer
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
  //# Configuration
  @Override
  public EnumFactory<TRToolCreator<TransitionRelationSimplifier>>
    getTRSimplifierFactory()
  {
    return
      new ListedEnumFactory<TRToolCreator<TransitionRelationSimplifier>>() {
      {
        register(OEQ);
        register(WOEQ);
        register(NB0);
        register(NB0w);
        register(NB1);
        register(NB1w);
        register(NB2);
        register(NB2w);
      }
    };
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
  //# Abstraction Chains
  /**
   * <P>An abstraction sequence for standard nonblocking verification.
   * This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Marking removal ({@link MarkingRemovalTRSimplifier})</LI>
   * <LI>Silent Incoming Rule ({@link SilentIncomingTRSimplifier})</LI>
   * <LI>Only Silent Outgoing Rule ({@link OnlySilentOutgoingTRSimplifier})</LI>
   * <LI>Incoming equivalence ({@link IncomingEquivalenceTRSimplifier};
   *     Silent Continuation plus Active Events Rules)</LI>
   * <LI>Observation equivalence ({@link ObservationEquivalenceTRSimplifier})</LI>
   * <LI>Marking saturation ({@link MarkingSaturationTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> NB0 =
    new TRToolCreator<TransitionRelationSimplifier>("NB0")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalAnalyzer analyzer)
    {
      final TRCompositionalConflictChecker checker =
        (TRCompositionalConflictChecker) analyzer;
      return checker.createConflictEquivalenceChain
        (ObservationEquivalenceTRSimplifier.
         Equivalence.OBSERVATION_EQUIVALENCE,
         false, false);
    }
  };

  /**
   * <P>An abstraction sequence for standard nonblocking verification.
   * This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Marking removal ({@link MarkingRemovalTRSimplifier})</LI>
   * <LI>Silent Incoming Rule ({@link SilentIncomingTRSimplifier})</LI>
   * <LI>Only Silent Outgoing Rule ({@link OnlySilentOutgoingTRSimplifier})</LI>
   * <LI>Incoming equivalence ({@link IncomingEquivalenceTRSimplifier};
   *     Silent Continuation plus Active Events Rules)</LI>
   * <LI>Weak observation equivalence
   *     ({@link ObservationEquivalenceTRSimplifier})</LI>
   * <LI>Marking saturation ({@link MarkingSaturationTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> NB0w =
    new TRToolCreator<TransitionRelationSimplifier>("NB0w")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalAnalyzer analyzer)
    {
      final TRCompositionalConflictChecker checker =
        (TRCompositionalConflictChecker) analyzer;
      return checker.createConflictEquivalenceChain
        (ObservationEquivalenceTRSimplifier.
         Equivalence.WEAK_OBSERVATION_EQUIVALENCE,
         false, false);
    }
  };

  /**
   * <P>An abstraction sequence for standard nonblocking verification.
   * This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Transition removal ({@link TransitionRemovalTRSimplifier})</LI>
   * <LI>Marking removal ({@link MarkingRemovalTRSimplifier})</LI>
   * <LI>Silent Incoming Rule ({@link SilentIncomingTRSimplifier})</LI>
   * <LI>Only Silent Outgoing Rule ({@link OnlySilentOutgoingTRSimplifier})</LI>
   * <LI>Incoming equivalence ({@link IncomingEquivalenceTRSimplifier};
   *     Silent Continuation plus Active Events Rules)</LI>
   * <LI>Observation equivalence ({@link ObservationEquivalenceTRSimplifier})</LI>
   * <LI>Marking saturation ({@link MarkingSaturationTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> NB1 =
    new TRToolCreator<TransitionRelationSimplifier>("NB1")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalAnalyzer analyzer)
    {
      final TRCompositionalConflictChecker checker =
        (TRCompositionalConflictChecker) analyzer;
      return checker.createConflictEquivalenceChain
        (ObservationEquivalenceTRSimplifier.
         Equivalence.OBSERVATION_EQUIVALENCE,
         true, false);
    }
  };

  /**
   * <P>An abstraction sequence for standard nonblocking verification.
   * This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Transition removal ({@link TransitionRemovalTRSimplifier})</LI>
   * <LI>Marking removal ({@link MarkingRemovalTRSimplifier})</LI>
   * <LI>Silent Incoming Rule ({@link SilentIncomingTRSimplifier})</LI>
   * <LI>Only Silent Outgoing Rule ({@link OnlySilentOutgoingTRSimplifier})</LI>
   * <LI>Incoming equivalence ({@link IncomingEquivalenceTRSimplifier};
   *     Silent Continuation plus Active Events Rules)</LI>
   * <LI>Weak observation equivalence
   *     ({@link ObservationEquivalenceTRSimplifier})</LI>
   * <LI>Marking saturation ({@link MarkingSaturationTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> NB1w =
    new TRToolCreator<TransitionRelationSimplifier>("NB1w")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalAnalyzer analyzer)
    {
      final TRCompositionalConflictChecker checker =
        (TRCompositionalConflictChecker) analyzer;
      return checker.createConflictEquivalenceChain
        (ObservationEquivalenceTRSimplifier.
         Equivalence.WEAK_OBSERVATION_EQUIVALENCE,
         true, false);
    }
  };

  /**
   * <P>An abstraction sequence for standard nonblocking verification.
   * This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Transition removal ({@link TransitionRemovalTRSimplifier})</LI>
   * <LI>Marking removal ({@link MarkingRemovalTRSimplifier})</LI>
   * <LI>Silent Incoming Rule ({@link SilentIncomingTRSimplifier})</LI>
   * <LI>Only Silent Outgoing Rule ({@link OnlySilentOutgoingTRSimplifier})</LI>
   * <LI>Incoming equivalence ({@link IncomingEquivalenceTRSimplifier};
   *     Silent Continuation plus Active Events Rules)</LI>
   * <LI>Observation equivalence ({@link ObservationEquivalenceTRSimplifier})</LI>
   * <LI>Non-alpha determinisation ({@link NonAlphaDeterminisationTRSimplifier})</LI>
   * <LI>Marking saturation ({@link MarkingSaturationTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> NB2 =
    new TRToolCreator<TransitionRelationSimplifier>("NB2")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalAnalyzer analyzer)
    {
      final TRCompositionalConflictChecker checker =
        (TRCompositionalConflictChecker) analyzer;
      return checker.createConflictEquivalenceChain
        (ObservationEquivalenceTRSimplifier.
         Equivalence.OBSERVATION_EQUIVALENCE,
         true, true);
    }
  };

  /**
   * <P>An abstraction sequence for standard nonblocking verification.
   * This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Transition removal ({@link TransitionRemovalTRSimplifier})</LI>
   * <LI>Marking removal ({@link MarkingRemovalTRSimplifier})</LI>
   * <LI>Silent Incoming Rule ({@link SilentIncomingTRSimplifier})</LI>
   * <LI>Only Silent Outgoing Rule ({@link OnlySilentOutgoingTRSimplifier})</LI>
   * <LI>Incoming equivalence ({@link IncomingEquivalenceTRSimplifier};
   *     Silent Continuation plus Active Events Rules)</LI>
   * <LI>Weak observation equivalence
   *     ({@link ObservationEquivalenceTRSimplifier})</LI>
   * <LI>Non-alpha determinisation ({@link NonAlphaDeterminisationTRSimplifier})</LI>
   * <LI>Marking saturation ({@link MarkingSaturationTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> NB2w =
    new TRToolCreator<TransitionRelationSimplifier>("NB2w")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalAnalyzer analyzer)
    {
      final TRCompositionalConflictChecker checker =
        (TRCompositionalConflictChecker) analyzer;
      return checker.createConflictEquivalenceChain
        (ObservationEquivalenceTRSimplifier.
         Equivalence.WEAK_OBSERVATION_EQUIVALENCE,
         true, true);
    }
  };


  protected ChainTRSimplifier createConflictEquivalenceChain
    (final ObservationEquivalenceTRSimplifier.Equivalence equivalence,
     final boolean earlyTransitionRemoval, final boolean nonAlphaDeterminisation)
  {
    final int limit = getInternalTransitionLimit();
    final ChainTRSimplifier chain = startAbstractionChain();
    final TRSimplificationListener markingListener = new MarkingListener();
    final TRSimplificationListener partitioningListener =
      new PartitioningListener();
    final TauLoopRemovalTRSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    loopRemover.setDumpStateAware(true);
    loopRemover.setSimplificationListener(partitioningListener);
    chain.add(loopRemover);
    final ObservationEquivalenceTRSimplifier.TransitionRemoval trMode;
    if (earlyTransitionRemoval) {
      final TransitionRemovalTRSimplifier transitionRemover =
        new TransitionRemovalTRSimplifier();
      transitionRemover.setTransitionLimit(limit);
      transitionRemover.setSimplificationListener(partitioningListener);
      chain.add(transitionRemover);
      trMode = ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER;
    } else {
      trMode = ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL;
    }
    final MarkingRemovalTRSimplifier markingRemover =
      new MarkingRemovalTRSimplifier();
    markingRemover.setSimplificationListener(markingListener);
    chain.add(markingRemover);
    final SilentIncomingTRSimplifier silentInRemover =
      new SilentIncomingTRSimplifier();
    silentInRemover.setRestrictsToUnreachableStates(true);
    silentInRemover.setDumpStateAware(true);
    silentInRemover.setSimplificationListener(partitioningListener);
    chain.add(silentInRemover);
    final OnlySilentOutgoingTRSimplifier silentOutRemover =
      new OnlySilentOutgoingTRSimplifier();
    silentOutRemover.setDumpStateAware(true);
    silentOutRemover.setSimplificationListener(partitioningListener);
    chain.add(silentOutRemover);
    final IncomingEquivalenceTRSimplifier incomingEquivalenceSimplifier =
      new IncomingEquivalenceTRSimplifier();
    incomingEquivalenceSimplifier.setSimplificationListener(partitioningListener);
    chain.add(incomingEquivalenceSimplifier);
    /*
    final LimitedCertainConflictsTRSimplifier limitedCertainConflictsRemover;
    limitedCertainConflictsRemover =
      new LimitedCertainConflictsTRSimplifier();
    */
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode(trMode);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.UNCHANGED);
    bisimulator.setTransitionLimit(limit);
    bisimulator.setDumpStateAware(true);
    bisimulator.setSimplificationListener(partitioningListener);
    chain.add(bisimulator);
    if (nonAlphaDeterminisation) {
      final NonAlphaDeterminisationTRSimplifier nonAlphaDeterminiser =
        new NonAlphaDeterminisationTRSimplifier();
      nonAlphaDeterminiser.setTransitionRemovalMode
        (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER_IF_CHANGED);
      nonAlphaDeterminiser.setTransitionLimit(limit);
      nonAlphaDeterminiser.setDumpStateAware(true);
      nonAlphaDeterminiser.setSimplificationListener(partitioningListener);
      chain.add(nonAlphaDeterminiser);
    }
    final MarkingSaturationTRSimplifier saturator =
      new MarkingSaturationTRSimplifier();
    saturator.setSimplificationListener(markingListener);
    chain.add(saturator);
    chain.setPropositions(PRECONDITION_MARKING, DEFAULT_MARKING);
    chain.setPreferredOutputConfiguration
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    return chain;
  }


  //#########################################################################
  //# Inner Class MarkingListener
  class MarkingListener extends PartitioningListener
  {
    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.analysis.abstraction.TRSimplificationListener
    @Override
    public boolean onSimplificationStart
      (final TransitionRelationSimplifier simplifier)
    {
      if (super.onSimplificationStart(simplifier)) {
        if (simplifier instanceof MarkingRemovalTRSimplifier &&
            isCounterExampleEnabled()) {
          final ListBufferTransitionRelation rel =
            simplifier.getTransitionRelation();
          mOldNumberOfMarkings = rel.getNumberOfMarkings(true);
        }
        return true;
      } else {
        return false;
      }
    }

    @Override
    public void onSimplificationFinish
      (final TransitionRelationSimplifier simplifier, final boolean result)
    {
      if (simplifier instanceof MarkingSaturationTRSimplifier) {
        final IntermediateAbstractionSequence seq =
          getIntermediateAbstractionSequence();
        if (result && isCounterExampleEnabled() && seq != null &&
            seq.getLastPartitionSimplifier() instanceof MarkingRemovalTRSimplifier) {
          seq.removeLastPartitionSimplifier();
          final ListBufferTransitionRelation rel =
            simplifier.getTransitionRelation();
          if (rel.getNumberOfMarkings(true) == mOldNumberOfMarkings) {
            return;
          }
        }
      }
      super.onSimplificationFinish(simplifier, result);
    }

    //#######################################################################
    //# Data Members
    private int mOldNumberOfMarkings;
  }


  //#########################################################################
  //# Data Members
  // Configuration
  private EventProxy mConfiguredDefaultMarking;
  private EventProxy mConfiguredPreconditionMarking;

  private EventProxy mUsedDefaultMarking;

}
