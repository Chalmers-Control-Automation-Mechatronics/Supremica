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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsFinder;
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
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.TraceChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.apache.log4j.Logger;


/**
 * @author Robi Malik
 */

public class TRCompositionalLanguageInclusionChecker
  extends AbstractTRCompositionalVerifier
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
    final TransitionRelationSimplifier chain = createDefaultAbstractionChain();
    setSimplifier(chain);
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
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    mPropertyAutomata = new ArrayList<>();
    mPropertiesHaveInitialState = true;
    super.setUp();
    final AnalysisResult result = getAnalysisResult();
    if (!result.isFinished()) {
      final Logger logger = getLogger();
      if (mPropertyAutomata.isEmpty()) {
        logger.debug
          ("Terminating early as all properties are trivially satisfied.");
        result.setSatisfied(true);
      } else if (!mPropertiesHaveInitialState) {
        logger.debug
          ("Terminating early as the properties have no initial state.");
        result.setSatisfied(false);
        final TRSubsystemInfo subsys = getCurrentSubsystem();
        dropSubsystem(subsys);
        for (final TRAutomatonProxy prop : mPropertyAutomata) {
          dropTrivialAutomaton(prop);
        }
      }
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mPropertyAutomata = null;
  }


  //#########################################################################
  //# Hooks
  @Override
  protected Collection<EventProxy> getUsedPropositions()
  {
    return Collections.emptyList();
  }

  @Override
  protected boolean isFailingEventsUsed()
  {
    return false;
  }

  @Override
  protected boolean isAlwaysEnabledEventsUsed()
  {
    return false;
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
    final SubsetConstructionTRSimplifier subset =
      new SubsetConstructionTRSimplifier();
    chain.add(subset);
    subset.setStateLimit(getInternalStateLimit());
    subset.setTransitionLimit(getInternalStateLimit());
    subset.setSimplificationListener(listener);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    final ObservationEquivalenceTRSimplifier.Equivalence eq =
      isSelfloopOnlyEventsUsed() ?
      ObservationEquivalenceTRSimplifier.Equivalence.BISIMULATION :
      ObservationEquivalenceTRSimplifier.Equivalence.DETERMINISTIC_MINSTATE;
    bisimulator.setEquivalence(eq);
    bisimulator.setTransitionLimit(getInternalStateLimit());
    bisimulator.setSimplificationListener(listener);
    chain.add(bisimulator);
    return chain;
  }

  @Override
  protected TRAutomatonProxy createInitialAutomaton(final AutomatonProxy aut,
                                                    final int config)
    throws AnalysisException
  {
    final KindTranslator translator = getKindTranslator();
    switch (translator.getComponentKind(aut)) {
    case PLANT:
      return super.createInitialAutomaton(aut, config);
    case SPEC:
      if (!mPropertiesHaveInitialState) {
        return null;
      }
      final Logger logger = getLogger();
      final EventEncoding eventEnc = createInitialEventEncoding(aut);
      final TRAutomatonProxy tr = new TRAutomatonProxy(aut, eventEnc, config);
      if (hasInitialState(tr)) {
        final ListBufferTransitionRelation rel = tr.getTransitionRelation();
        final SpecialEventsFinder finder = getSpecialEventsFinder();
        rel.removeProperSelfLoopEvents();
        finder.setBlockedEventsDetected(true);
        finder.setFailingEventsDetected(false);
        finder.setSelfloopOnlyEventsDetected(isSelfloopOnlyEventsEnabled());
        finder.setAlwaysEnabledEventsDetected(isAlwaysEnabledEventsEnabled());
        finder.setTransitionRelation(rel);
        finder.run();
        final byte[] status = finder.getComputedEventStatus();
        final byte failingPattern = (byte)
          ((isFailingEventsEnabled() ? EventStatus.STATUS_FAILING : 0) |
           (isAlwaysEnabledEventsEnabled() ? EventStatus.STATUS_ALWAYS_ENABLED : 0));
        boolean trivial = true;
        for (int e = EventEncoding.NONTAU; e < status.length; e++) {
          if (!EventStatus.isUsedEvent(status[e])) {
            // skip
          } else if (EventStatus.isBlockedEvent(status[e])) {
            status[e] = failingPattern;
            trivial = false;
          } else {
            status[e] = 0;  // normal external event
            trivial &= EventStatus.isAlwaysEnabledEvent(status[e]);
          }
        }
        if (trivial) {
          logger.debug("Dropping trivial property " + aut.getName() + ".");
          return null;
        }
        final TRSubsystemInfo subsys = getCurrentSubsystem();
        subsys.registerEvents(tr, status, true);
      } else {
        logger.debug("Property " + aut.getName() + " has no initial state.");
        mPropertyAutomata.clear();
        mPropertiesHaveInitialState = false;
      }
      mPropertyAutomata.add(tr);
      if (isCounterExampleEnabled()) {
        final EventEncoding clonedEnc = new EventEncoding(eventEnc);
        final TRAbstractionStepInput step =
          new TRAbstractionStepInput(aut, clonedEnc, tr);
        addAbstractionStep(step, tr);
      }
      return null;
    default:
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
    logger.debug("Subsystem satisfies properties " +
                 "because it shares no events with property automata.");
    dropSubsystem(subsys);
    return true;
  }

  @Override
  protected void analyseSubsystemMonolithically(final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    final List<TRAutomatonProxy> plants = subsys.getAutomata();
    final String name = AutomatonTools.getCompositionName(plants);
    final int numAutomata = plants.size() + mPropertyAutomata.size();
    final List<TRAutomatonProxy> automata = new ArrayList<>(numAutomata);
    automata.addAll(plants);
    automata.addAll(mPropertyAutomata);
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
      logger.debug("Subsystem satisfies properties.");
      dropSubsystem(subsys);
    } else {
      logger.debug("Subsystem fails properties.");
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
  //# Data Members
  // Configuration
  private final SafetyDiagnostics mDiagnostics;

  // Data Structures
  private List<TRAutomatonProxy> mPropertyAutomata;
  private boolean mPropertiesHaveInitialState;

}
