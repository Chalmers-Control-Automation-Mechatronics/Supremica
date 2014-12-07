//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRCompositionalLanguageInclusionChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsFinder;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SubsetConstructionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplificationListener;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.compositional.CompositionalAnalysisResult;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.cpp.analysis.NativeSafetyVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.TraceChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.apache.log4j.Logger;


/**
 * @author Robi Malik
 */

public class TRCompositionalLanguageInclusionChecker
  extends AbstractTRCompositionalAnalyzer
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public TRCompositionalLanguageInclusionChecker()
  {
    this(null);
  }

  public TRCompositionalLanguageInclusionChecker(final ProductDESProxy model)
  {
    this(model,
         LanguageInclusionKindTranslator.getInstance(),
         LanguageInclusionDiagnostics.getInstance());
  }

  public TRCompositionalLanguageInclusionChecker
    (final ProductDESProxy model,
     final KindTranslator translator,
     final SafetyDiagnostics diag)
  {
    super(model, translator,
          new NativeSafetyVerifier(translator, diag,
                                   ProductDESElementFactory.getInstance()));
    mDiagnostics = diag;
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.SafetyVerifier
  @Override
  public SafetyDiagnostics getDiagnostics()
  {
    return mDiagnostics;
  }

  @Override
  public SafetyTraceProxy getCounterExample()
  {
    final VerificationResult result = getAnalysisResult();
    return (SafetyTraceProxy) result.getCounterExample();
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
        register(PROJ);
      }
    };
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run()
    throws AnalysisException
  {
    final Logger logger = getLogger();
    final KindTranslator translator = getKindTranslator();
    final ProductDESProxy des = getModel();
    boolean hasProperty = false;
    AnalysisResult result = null;
    // Check one property at a time,
    // using full setUp-run-tearDown cycle each time ...
    for (final AutomatonProxy aut : des.getAutomata()) {
      if (translator.getComponentKind(aut) == ComponentKind.SPEC) {
        try {
          logger.debug("Checking property " + aut.getName() + " ...");
          hasProperty = true;
          mRawProperty = aut;
          if (!super.run()) {
            return false;
          }
        } finally {
          final AnalysisResult subResult = getAnalysisResult();
          if (result == null) {
            result = subResult;
          } else if (subResult != null) {
            result.merge(subResult);
          }
          if (result != null) {
            setAnalysisResult(result);
          }
        }
      }
    }
    if (!hasProperty) {
      logger.debug("Did not find any properties to check, returning TRUE.");
    }
    return true;
  }

  @Override
  protected void setUp()
    throws AnalysisException
  {
    // Set up plant automata ...
    super.setUp();
    final AnalysisResult result = getAnalysisResult();
    if (result.isFinished()) {
      return;
    }
    // Set up property automaton ...
    final Logger logger = getLogger();
    final EventEncoding eventEnc = createInitialEventEncoding(mRawProperty);
    final TransitionRelationSimplifier simplifier = getSimplifier();
    final int config = simplifier.getPreferredInputConfiguration();
    mCurrentProperty = new TRAutomatonProxy(mRawProperty, eventEnc, config);
    if (isCounterExampleEnabled()) {
      final EventEncoding clonedEnc = new EventEncoding(eventEnc);
      final TRAbstractionStepInput step =
        new TRAbstractionStepInput(mRawProperty, clonedEnc, mCurrentProperty);
      addAbstractionStep(step, mCurrentProperty);
    }
    if (!hasInitialState(mCurrentProperty)) {
      logger.debug("System fails property " + mRawProperty.getName() +
                   ", because it has no initial state.");
      result.setSatisfied(false);
      final TRSubsystemInfo subsys = getCurrentSubsystem();
      dropSubsystem(subsys);
      dropTrivialAutomaton(mCurrentProperty);
      return;
    }
    final ListBufferTransitionRelation rel =
      mCurrentProperty.getTransitionRelation();
    mHasStronglyFailingEvent = false;
    final SpecialEventsFinder finder = getSpecialEventsFinder();
    rel.removeProperSelfLoopEvents();
    finder.setBlockedEventsDetected(true);
    finder.setFailingEventsDetected(false);
    finder.setSelfloopOnlyEventsDetected(isSelfloopOnlyEventsEnabled());
    finder.setAlwaysEnabledEventsDetected(isAlwaysEnabledEventsEnabled());
    finder.setTransitionRelation(rel);
    finder.run();
    final byte[] status = finder.getComputedEventStatus();
    boolean trivial = true;
    for (int e = EventEncoding.NONTAU; e < status.length; e++) {
      if (EventStatus.isUsedEvent(status[e])) {
        status[e] = 0;
        if (EventStatus.isBlockedEvent(status[e])) {
          if (isFailingEventsEnabled()) {
            status[e] |= EventStatus.STATUS_FAILING;
          }
          if (isAlwaysEnabledEventsEnabled()) {
            status[e] |= EventStatus.STATUS_ALWAYS_ENABLED;
          }
          trivial = false;
          mHasStronglyFailingEvent =
            isFailingEventsEnabled() && isAlwaysEnabledEventsEnabled();
        } else {
          trivial &= EventStatus.isAlwaysEnabledEvent(status[e]);
        }
      }
    }
    if (trivial) {
      logger.debug("Skipping trivial property " + mRawProperty.getName() + ".");
      result.setSatisfied(true);
      return;
    }
    finder.setAlwaysEnabledEventsDetected(mHasStronglyFailingEvent);
    final TRSubsystemInfo subsys = getCurrentSubsystem();
    subsys.registerEvents(mCurrentProperty, status, true);
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mRawProperty = null;
    mCurrentProperty = null;
  }


  //#########################################################################
  //# Hooks
  @Override
  protected boolean isFailingEventsUsed()
  {
    return false;
  }

  @Override
  protected boolean isAlwaysEnabledEventsUsed()
  {
    return mHasStronglyFailingEvent;
  }

  @Override
  protected TRAutomatonProxy createInitialAutomaton(final AutomatonProxy aut)
    throws AnalysisException
  {
    final KindTranslator translator = getKindTranslator();
    if (translator.getComponentKind(aut) == ComponentKind.PLANT) {
      return super.createInitialAutomaton(aut);
    } else {
      return null;
    }
  }

  @Override
  protected boolean earlyTerminationCheck(final TRSubsystemInfo subsys)
  {
    for (final TREventInfo info : subsys.getEvents()) {
      if (info.isExternal()) {
        return false;
      }
    }
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      logger.debug("Dropping subsystem because it shares no events " +
                   "with property " + mCurrentProperty.getName() + ".");
    }
    dropSubsystem(subsys);
    return true;
  }

  @Override
  protected boolean analyseSubsystemMonolithically
    (final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    final String name;
    final List<TRAutomatonProxy> automata;
    if (subsys == null || subsys.getNumberOfAutomata() == 0) {
      name = mCurrentProperty.getName();
      automata = Collections.singletonList(mCurrentProperty);
    } else {
      final List<TRAutomatonProxy> plants = subsys.getAutomata();
      final int numAutomata = plants.size() + 1;
      automata = new ArrayList<>(numAutomata);
      automata.addAll(plants);
      automata.add(mCurrentProperty);
      name = AutomatonTools.getCompositionName(plants);
    }
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
      if (logger.isDebugEnabled()) {
        logger.debug("Subsystem satisfies property " +
                     mCurrentProperty.getName() + ".");
      }
      dropSubsystem(subsys);
      return setSatisfiedResult();
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("Subsystem fails property " +
                     mCurrentProperty.getName() + ".");
      }
      combinedResult.setSatisfied(false);
      if (isCounterExampleEnabled()) {
        dropPendingSubsystems();
        final List<TRAbstractionStep> preds = getAbstractionSteps(automata);
        final TraceProxy trace = mono.getCounterExample();
        final TRAbstractionStep step =
          new TRAbstractionStepMonolithic(name, preds, trace);
        addAbstractionStep(step);
      }
      return false;
    }
  }

  @Override
  protected TRTraceProxy computeCounterExample() throws AnalysisException
  {
    final TRTraceProxy trace = super.computeCounterExample();
    if (trace != null) {
      final TRSafetyTraceProxy safetyTrace = (TRSafetyTraceProxy) trace;
      safetyTrace.provideComment(mDiagnostics);
    }
    return trace;
  }

  @Override
  protected TRSafetyTraceProxy createEmptyTrace(final ProductDESProxy des)
  {
    return new TRSafetyTraceProxy(des, mDiagnostics);
  }

  @Override
  protected void checkIntermediateCounterExample(final TRTraceProxy trace)
    throws AnalysisException
  {
    final TRSafetyTraceProxy safetyTrace = (TRSafetyTraceProxy) trace;
    final TRSafetyTraceProxy cloned = new TRSafetyTraceProxy(safetyTrace);
    cloned.setUpForTraceChecking();
    final KindTranslator translator = getKindTranslator();
    TraceChecker.checkSafetyCounterExample(cloned, true, translator);
  }


  //#########################################################################
  //# Abstraction Chains
  /**
   * <P>The abstraction sequence that consists of only observation
   * equivalence. This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal {@link SpecialEventsTRSimplifier}</LI>
   * <LI>Tau-loop removal {@link TauLoopRemovalTRSimplifier}</LI>
   * <LI>Observation equivalence {@link ObservationEquivalenceTRSimplifier}</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> PROJ =
    new TRToolCreator<TransitionRelationSimplifier>("PROJ")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalAnalyzer analyzer)
    {
      final ChainTRSimplifier chain = analyzer.startAbstractionChain();
      final int stateLimit = analyzer.getInternalStateLimit();
      final int transitionLimit = analyzer.getInternalTransitionLimit();
      final TRSimplificationListener listener =
        analyzer.new PartitioningListener();
      final TransitionRelationSimplifier loopRemover =
        new TauLoopRemovalTRSimplifier();
      loopRemover.setSimplificationListener(listener);
      chain.add(loopRemover);
      final SubsetConstructionTRSimplifier subset =
        new SubsetConstructionTRSimplifier();
      chain.add(subset);
      subset.setStateLimit(stateLimit);
      subset.setTransitionLimit(transitionLimit);
      subset.setSimplificationListener(listener);
      final ObservationEquivalenceTRSimplifier bisimulator =
        new ObservationEquivalenceTRSimplifier();
      final ObservationEquivalenceTRSimplifier.Equivalence eq =
        analyzer.isSelfloopOnlyEventsUsed() ?
        ObservationEquivalenceTRSimplifier.Equivalence.BISIMULATION :
        ObservationEquivalenceTRSimplifier.Equivalence.DETERMINISTIC_MINSTATE;
      bisimulator.setEquivalence(eq);
      bisimulator.setTransitionLimit(stateLimit);
      bisimulator.setSimplificationListener(listener);
      chain.add(bisimulator);
      chain.setPreferredOutputConfiguration
        (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      return chain;
    }
  };


  //#########################################################################
  //# Data Members
  // Configuration
  private final SafetyDiagnostics mDiagnostics;

  // Data Structures
  private AutomatonProxy mRawProperty;
  private TRAutomatonProxy mCurrentProperty;
  private boolean mHasStronglyFailingEvent;

}
