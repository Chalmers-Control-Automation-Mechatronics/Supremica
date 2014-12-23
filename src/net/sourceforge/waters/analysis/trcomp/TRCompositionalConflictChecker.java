//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRCompositionalConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.abstraction.AlphaDeterminisationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.CoreachabilityTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.IncomingEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.LimitedCertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.MarkingSaturationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.NonAlphaDeterminisationTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.OmegaRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.OnlySilentOutgoingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SilentIncomingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplificationListener;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRemovalTRSimplifier;
import net.sourceforge.waters.analysis.compositional.CompositionalAnalysisResult;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.TraceChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.log4j.Logger;


/**
 * <P>A compositional conflict checker based on {@link TRAutomatonProxy}
 * that can be configured to use different abstraction sequences for its
 * simplification steps.</P>
 *
 * <P><I>References:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, <STRONG>48</STRONG>(3),
 * 1914-1938, 2009.<BR>
 * Robi Malik, Ryan Leduc. Compositional Nonblocking Verification Using
 * Generalised Nonblocking Abstractions, IEEE Transactions on Automatic
 * Control <STRONG>58</STRONG>(8), 1-13, 2013.<BR>
 * Colin Pilbrow, Robi Malik. Compositional Nonblocking Verification with
 * Always Enabled Events and Selfloop-only Events. Proc. 2nd International
 * Workshop on Formal Techniques for Safety-Critical Systems, FTSCS 2013,
 * 147-162, Queenstown, New Zealand, 2013.</P>
 *
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
        register(GNB);
        register(GNBw);
      }
    };
  }

  @Override
  public ConflictChecker getMonolithicAnalyzer()
  {
    return (ConflictChecker) super.getMonolithicAnalyzer();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mLanguageInclusionChecker != null) {
      mLanguageInclusionChecker.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mLanguageInclusionChecker != null) {
      mLanguageInclusionChecker.resetAbort();
    }
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp() throws AnalysisException
  {
    mUsedPreconditionMarking = mConfiguredPreconditionMarking;
    super.setUp();
    final ConflictChecker mono = getMonolithicAnalyzer();
    mono.setConfiguredDefaultMarking(getUsedDefaultMarking());
    mono.setConfiguredPreconditionMarking(getUsedPreconditionMarking());
    if (mConfiguredPreconditionMarking != null) {
      mNonblockingSubsystems = new LinkedList<>();
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mUsedDefaultMarking = null;
    mUsedPreconditionMarking = null;
    mLanguageInclusionChecker = null;
    mNonblockingSubsystems = null;
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
        mUsedDefaultMarking =
          AbstractConflictChecker.getMarkingProposition(model);
      } else {
        mUsedDefaultMarking = mConfiguredDefaultMarking;
      }
    }
    return mUsedDefaultMarking;
  }

  @Override
  protected EventProxy getUsedPreconditionMarking()
  {
    return mUsedPreconditionMarking;
  }

  @Override
  protected void addAuxiliaryEvents(final EventEncoding enc)
    throws AnalysisException
  {
    final EventProxy defaultMarking = getUsedDefaultMarking();
    enc.addProposition(defaultMarking, false);
    final EventProxy preconditionMarking = getUsedPreconditionMarking();
    if (preconditionMarking != null) {
      enc.addProposition(preconditionMarking, false);
    }
  }

  @Override
  protected StateProxy findDumpState(final AutomatonProxy aut)
    throws EventNotFoundException
  {
    if (mConfiguredPreconditionMarking == null) {
      final EventProxy marking = getUsedDefaultMarking();
      final Collection<EventProxy> markings = Collections.singletonList(marking);
      return AutomatonTools.findDumpState(aut, markings);
    } else {
      return null;
    }
  }

  @Override
  protected boolean earlyTerminationCheck(final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    final Logger logger = getLogger();
    boolean allAutomataOmega = true;
    TRAutomatonProxy omegaBlocker = null;
    for (final TRAutomatonProxy aut : subsys.getAutomata()) {
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      boolean noStatesReachable = true;
      boolean noStatesOmega = rel.isPropositionUsed(DEFAULT_MARKING);
      boolean noStatesAlpha = rel.isPropositionUsed(PRECONDITION_MARKING);
      for (int s = 0; s < rel.getNumberOfStates(); s++) {
        if (rel.isReachable(s)) {
          noStatesReachable = false;
          final boolean alpha = rel.isMarked(s, PRECONDITION_MARKING);
          final boolean omega = rel.isMarked(s, DEFAULT_MARKING);
          if (alpha) {
            noStatesAlpha = false;
            if (!omega) {
              allAutomataOmega = false;
            }
          }
          if (omega) {
            noStatesOmega = false;
          }
        }
      }
      if (noStatesReachable) {
        logger.debug("The system is generalised nonblocking, because " +
                     aut.getName() + " has no reachable states.");
        return setSatisfiedResult();
      } else if (noStatesAlpha) {
        logger.debug("The system is generalised nonblocking, because " +
                     aut.getName() + " has no precondition-marked states.");
        return setSatisfiedResult();
      } else if (noStatesOmega) {
        if (mUsedPreconditionMarking == null) {
          final AnalysisResult result = getAnalysisResult();
          result.setSatisfied(false);
          logger.debug("Subsystem is blocking, because " + aut.getName() +
                       " has no marked states.");
          dropPendingSubsystems();
          subsys.moveToEnd(aut);
          dropSubsystem(subsys);
          return true;
        } else if (omegaBlocker == null) {
          omegaBlocker = aut;
        }
      }
    }
    if (allAutomataOmega) {
      if (mConfiguredPreconditionMarking == null) {
        logger.debug("Subsystem is nonblocking, because all states are marked.");
      } else {
        logger.debug("Subsystem is generalised nonblocking, because all " +
                     "precondition-marked states have the default marking.");
      }
      dropSubsystem(subsys);
      return true;
    } else if (omegaBlocker != null) {
      logger.debug("Subsystem is blocking, because " +
                   omegaBlocker.getName() + " has no marked state.");
      subsys.moveToEnd(omegaBlocker);
      dropSubsystem(subsys);
      final ProductDESProxy des = getModel();
      final TRTraceProxy trace = new TRConflictTraceProxy(des);
      final boolean extended = extendToPreconditionMarkedState(trace);
      final AnalysisResult result = getAnalysisResult();
      result.setSatisfied(!extended);
      if (extended && isCounterExampleEnabled() &&
          !trace.getCoveredAbstractionSteps().isEmpty()) {
        final TRAbstractionStep step = new TRAbstractionStepMonolithic(trace);
        addAbstractionStep(step);
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected boolean analyseSubsystemMonolithically
    (final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    if (subsys == null || subsys.getNumberOfAutomata() == 0) {
      return true;
    } else {
      final List<TRAutomatonProxy> automata = subsys.getAutomata();
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
        return true;
      } else if (mUsedPreconditionMarking == null) {
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
        return false;
      } else {
        logger.debug("Subsystem is generalised blocking.");
        final List<TRAbstractionStep> preds = getAbstractionSteps(automata);
        final TraceProxy monoTrace = mono.getCounterExample();
        final TRAbstractionStepMonolithic monoStep =
          new TRAbstractionStepMonolithic(name, preds, monoTrace);
        final TRTraceProxy trace = new TRConflictTraceProxy(des);
        monoStep.expandTrace(trace, this);
        for (final TRAutomatonProxy aut : automata) {
          final TRAbstractionStep step = getAbstractionStep(aut);
          trace.setInputAutomaton(aut, step);
        }
        final boolean extended = extendToPreconditionMarkedState(trace);
        combinedResult.setSatisfied(!extended);
        if (extended && isCounterExampleEnabled()) {
          final TRAbstractionStep step =
            new TRAbstractionStepMonolithic(name, trace);
          addAbstractionStep(step);
        }
        return !extended;
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

  @Override
  protected void dropSubsystem(final TRSubsystemInfo subsys)
  {
    if (mNonblockingSubsystems == null) {
      super.dropSubsystem(subsys);
    } else {
      mNonblockingSubsystems.add(subsys);
    }
  }


  //#########################################################################
  //# Generalised Nonblocking Trace Extension
  private boolean extendToPreconditionMarkedState(final TRTraceProxy trace)
    throws AnalysisException
  {
    final Collection<TRSubsystemInfo> pending = getPendingSubsystems();
    final int nonblockingSize =
      mNonblockingSubsystems == null ? 0 : mNonblockingSubsystems.size();
    final int size = pending.size() + nonblockingSize;
    if (size == 0) {
      return true;
    }
    final Logger logger = getLogger();
    logger.debug("Searching for reachable precondition-marked state ...");
    final List<TRSubsystemInfo> subsystems = new ArrayList<>(size);
    subsystems.addAll(pending);
    if (mNonblockingSubsystems != null) {
      subsystems.addAll(mNonblockingSubsystems);
    }
    Collections.sort(subsystems);
    for (final TRSubsystemInfo subsys : subsystems) {
      if (!extendToPreconditionMarkedState(trace, subsys)) {
        return false;
      }
    }
    return true;
  }

  private boolean extendToPreconditionMarkedState(final TRTraceProxy trace,
                                                  final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    final TRCompositionalLanguageInclusionChecker checker =
      getLanguageInclusionChecker();
    final TRToolCreator<TransitionRelationSimplifier> creator =
      checker.getSimplifierCreator();
    final TransitionRelationSimplifier simplifier =
      creator.create(mLanguageInclusionChecker);
    final int config = simplifier.getPreferredInputConfiguration();
    final int numAutomata = subsys.getNumberOfAutomata();
    final List<TRAutomatonProxy> langAutomata = new ArrayList<>(numAutomata + 1);
    final Map<TRAutomatonProxy,TRAutomatonProxy> langMap =
      new HashMap<>(numAutomata);
    boolean trivial = true;
    for (final TRAutomatonProxy aut : subsys.getAutomata()) {
      final TRAutomatonProxy langAut =
        createPreconditionCheckAutomaton(aut, config);
      langAutomata.add(langAut);
      langMap.put(langAut, aut);
      trivial &= aut == langAut;
    }
    if (trivial) {
      super.dropSubsystem(subsys);
      return true;
    }
    final TRAutomatonProxy property = getPreconditionPropertyAutomaton(config);
    langAutomata.add(property);
    final ProductDESProxyFactory factory = getFactory();
    final String name = AutomatonTools.getCompositionName(langAutomata);
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy(name, langAutomata, factory);
    checker.setModel(des);
    if (checker.run()) {
      return false;
    } else {
      final TRSafetyTraceProxy langTrace = checker.getCounterExample();
      final TRConflictTraceProxy confTrace = new TRConflictTraceProxy(langTrace);
      for (final TRAutomatonProxy langAut : langAutomata) {
        final TRAutomatonProxy aut = langMap.get(langAut);
        final TRAbstractionStep step = getAbstractionStep(aut);
        if (step == null) {
          confTrace.removeInputAutomaton(langAut);
        } else {
          confTrace.replaceInputAutomaton(langAut, step);
          trace.setInputAutomaton(aut, step);
        }
      }
      trace.widenAndAppend(confTrace);
      return true;
    }
  }

  private TRAutomatonProxy createPreconditionCheckAutomaton
    (final TRAutomatonProxy aut, final int config)
  {
    final EventEncoding enc = aut.getEventEncoding();
    if (enc.isPropositionUsed(PRECONDITION_MARKING)) {
      final EventEncoding langEnc = new EventEncoding(enc);
      langEnc.setPropositionUsed(PRECONDITION_MARKING, false);
      final EventProxy event = getPreconditionEvent();
      final int e = langEnc.addProperEvent(event, EventStatus.STATUS_NONE);
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      final ListBufferTransitionRelation langRel =
        new ListBufferTransitionRelation(rel, langEnc, config);
      final int numStates = rel.getNumberOfStates();
      final int dumpIndex = rel.getDumpStateIndex();
      langRel.setReachable(dumpIndex, true);
      boolean allMarked = true;
      for (int s = 0; s < numStates; s++) {
        if (rel.isReachable(s)) {
          if (rel.isMarked(s, PRECONDITION_MARKING)) {
            langRel.addTransition(s, e, dumpIndex);
          } else {
            allMarked = false;
          }
        }
      }
      return allMarked ? aut : new TRAutomatonProxy(langEnc, langRel);
    } else {
      return aut;
    }
  }

  private EventProxy getPreconditionEvent()
  {
    if (mPreconditionEvent == null) {
      final ProductDESProxyFactory factory = getFactory();
      final String name = mConfiguredPreconditionMarking.getName();
      mPreconditionEvent =
        factory.createEventProxy(name, EventKind.UNCONTROLLABLE);
    }
    return mPreconditionEvent;
  }

  private TRAutomatonProxy getPreconditionPropertyAutomaton(final int config)
    throws OverflowException
  {
    if (mPreconditionPropertyAutomaton == null) {
      final EventEncoding enc = new EventEncoding();
      final EventProxy event = getPreconditionEvent();
      enc.addProperEvent(event, EventStatus.STATUS_NONE);
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(PROPERTY_NAME, ComponentKind.PROPERTY,
                                         enc, 1, config);
      rel.setInitial(0, true);
      mPreconditionPropertyAutomaton = new TRAutomatonProxy(enc, rel);
    }
    return mPreconditionPropertyAutomaton;
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
   * <LI>Certain Conflicts Rule ({@link LimitedCertainConflictsTRSimplifier})</LI>
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
   * <LI>Certain Conflicts Rule ({@link LimitedCertainConflictsTRSimplifier})</LI>
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
   * <LI>Certain Conflicts Rule ({@link LimitedCertainConflictsTRSimplifier})</LI>
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
   * <LI>Certain Conflicts Rule ({@link LimitedCertainConflictsTRSimplifier})</LI>
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
   * <LI>Certain Conflicts Rule ({@link LimitedCertainConflictsTRSimplifier})</LI>
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
   * <LI>Certain Conflicts Rule ({@link LimitedCertainConflictsTRSimplifier})</LI>
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

  /**
   * <P>An abstraction sequence for generalised nonblocking verification.
   * This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Marking removal ({@link MarkingRemovalTRSimplifier})</LI>
   * <LI>Omega-removal ({@link OmegaRemovalTRSimplifier})</LI>
   * <LI>Silent Incoming Rule ({@link SilentIncomingTRSimplifier})</LI>
   * <LI>Only Silent Outgoing Rule ({@link OnlySilentOutgoingTRSimplifier})</LI>
   * <LI>Observation equivalence ({@link ObservationEquivalenceTRSimplifier})</LI>
   * <LI>Non-alpha determinisation ({@link NonAlphaDeterminisationTRSimplifier})</LI>
   * <LI>Alpha determinisation ({@link AlphaDeterminisationTRSimplifier})</LI>
   * <LI>Marking saturation ({@link MarkingSaturationTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> GNB =
    new TRToolCreator<TransitionRelationSimplifier>("GNB")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalAnalyzer analyzer)
    {
      final TRCompositionalConflictChecker checker =
        (TRCompositionalConflictChecker) analyzer;
      return checker.createGeneralisedNonblockingChain
        (ObservationEquivalenceTRSimplifier.
         Equivalence.OBSERVATION_EQUIVALENCE,
         true);
    }
  };

  /**
   * <P>An abstraction sequence for generalised nonblocking verification.
   * This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Marking removal ({@link MarkingRemovalTRSimplifier})</LI>
   * <LI>Omega-removal ({@link OmegaRemovalTRSimplifier})</LI>
   * <LI>Silent Incoming Rule ({@link SilentIncomingTRSimplifier})</LI>
   * <LI>Only Silent Outgoing Rule ({@link OnlySilentOutgoingTRSimplifier})</LI>
   * <LI>Weak observation equivalence
   *     ({@link ObservationEquivalenceTRSimplifier})</LI>
   * <LI>Non-alpha determinisation ({@link NonAlphaDeterminisationTRSimplifier})</LI>
   * <LI>Alpha determinisation ({@link AlphaDeterminisationTRSimplifier})</LI>
   * <LI>Marking saturation ({@link MarkingSaturationTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> GNBw =
    new TRToolCreator<TransitionRelationSimplifier>("GNBw")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalAnalyzer analyzer)
    {
      final TRCompositionalConflictChecker checker =
        (TRCompositionalConflictChecker) analyzer;
      return checker.createGeneralisedNonblockingChain
        (ObservationEquivalenceTRSimplifier.
         Equivalence.WEAK_OBSERVATION_EQUIVALENCE,
         true);
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
    final LimitedCertainConflictsTRSimplifier certainConflictsRemover =
      new LimitedCertainConflictsTRSimplifier();
    final TRSimplificationListener certainConflictsListener =
      new CertainConflictsListener();
    certainConflictsRemover.setSimplificationListener(certainConflictsListener);
    chain.add(certainConflictsRemover);
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
    chain.setDefaultMarkingID(DEFAULT_MARKING);
    chain.setPreferredOutputConfiguration
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    return chain;
  }

  protected ChainTRSimplifier createGeneralisedNonblockingChain
    (final ObservationEquivalenceTRSimplifier.Equivalence equivalence,
     final boolean earlyTransitionRemoval)
  {
    if (mUsedPreconditionMarking == null) {
      final ProductDESProxy model = getModel();
      final ProductDESProxyFactory factory = getFactory();
      mUsedPreconditionMarking =
        AbstractConflictChecker.createNewPreconditionMarking(model, factory);
    }
    final int limit = getInternalTransitionLimit();
    final ChainTRSimplifier chain = startAbstractionChain();
    final TRSimplificationListener markingListener = new MarkingListener();
    final TRSimplificationListener omegaRemovalListener =
      new OmegaRemovalListener();
    final TRSimplificationListener partitioningListener =
      new PartitioningListener();
    final TauLoopRemovalTRSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
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
    final MarkingRemovalTRSimplifier alphaRemover =
      new MarkingRemovalTRSimplifier();
    alphaRemover.setSimplificationListener(markingListener);
    chain.add(alphaRemover);
    final OmegaRemovalTRSimplifier omegaRemover =
      new OmegaRemovalTRSimplifier();
    omegaRemover.setSimplificationListener(omegaRemovalListener);
    chain.add(omegaRemover);
    if (mConfiguredPreconditionMarking != null) {
      final CoreachabilityTRSimplifier nonCoreachableRemover =
        new CoreachabilityTRSimplifier();
      nonCoreachableRemover.setSimplificationListener(partitioningListener);
      chain.add(nonCoreachableRemover);
    }
    final SilentIncomingTRSimplifier silentInRemover =
      new SilentIncomingTRSimplifier();
    silentInRemover.setRestrictsToUnreachableStates(true);
    silentInRemover.setSimplificationListener(partitioningListener);
    chain.add(silentInRemover);
    final OnlySilentOutgoingTRSimplifier silentOutRemover =
      new OnlySilentOutgoingTRSimplifier();
    silentOutRemover.setSimplificationListener(partitioningListener);
    chain.add(silentOutRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode(trMode);
    bisimulator.setTransitionLimit(limit);
    bisimulator.setSimplificationListener(partitioningListener);
    chain.add(bisimulator);
    final NonAlphaDeterminisationTRSimplifier nonAlphaDeterminiser =
      new NonAlphaDeterminisationTRSimplifier();
    nonAlphaDeterminiser.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER_IF_CHANGED);
    nonAlphaDeterminiser.setTransitionLimit(limit);
    nonAlphaDeterminiser.setSimplificationListener(partitioningListener);
    chain.add(nonAlphaDeterminiser);
    if (mConfiguredPreconditionMarking != null) {
      final AlphaDeterminisationTRSimplifier alphaDeterminiser =
        new AlphaDeterminisationTRSimplifier();
      alphaDeterminiser.setTransitionRemovalMode
        (ObservationEquivalenceTRSimplifier.TransitionRemoval.AFTER_IF_CHANGED);
      alphaDeterminiser.setTransitionLimit(limit);
      alphaDeterminiser.setSimplificationListener(partitioningListener);
      chain.add(alphaDeterminiser);
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
  //# Auxiliary Methods
  TRCompositionalLanguageInclusionChecker getLanguageInclusionChecker()
  {
    if (mLanguageInclusionChecker == null) {
      mLanguageInclusionChecker = new TRCompositionalLanguageInclusionChecker();
      mLanguageInclusionChecker.
        setInternalStateLimit(getInternalStateLimit());
      mLanguageInclusionChecker.
        setInternalTransitionLimit(getInternalTransitionLimit());
      mLanguageInclusionChecker.
        setMonolithicStateLimit(getMonolithicStateLimit());
      mLanguageInclusionChecker.
        setMonolithicTransitionLimit(getMonolithicTransitionLimit());
      mLanguageInclusionChecker.
        setBlockedEventsEnabled(isBlockedEventsEnabled());
      mLanguageInclusionChecker.
        setFailingEventsEnabled(isFailingEventsEnabled());
      mLanguageInclusionChecker.
        setSelfloopOnlyEventsEnabled(isSelfloopOnlyEventsEnabled());
      mLanguageInclusionChecker.
        setAlwaysEnabledEventsEnabled(isAlwaysEnabledEventsEnabled());
      mLanguageInclusionChecker.setPreservingEncodings(true);
    }
    return mLanguageInclusionChecker;
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
        final ListBufferTransitionRelation rel =
          simplifier.getTransitionRelation();
        if (simplifier instanceof MarkingRemovalTRSimplifier &&
            isCounterExampleEnabled()) {
          mNumberOfDefaultMarkingsAtStart =
            rel.getNumberOfMarkings(DEFAULT_MARKING, true);
          mNumberOfPreconditionMarkingsAtStart =
            rel.getNumberOfMarkings(PRECONDITION_MARKING, true);
        } else if (simplifier instanceof MarkingSaturationTRSimplifier) {
          mNumberOfPreconditionMarkingsBeforeSaturate =
            rel.getNumberOfMarkings(PRECONDITION_MARKING, true);
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
      final IntermediateAbstractionSequence seq =
        getIntermediateAbstractionSequence();
      if (result && isCounterExampleEnabled() && seq != null) {
        if (simplifier instanceof MarkingRemovalTRSimplifier) {
          mEventEncodingAtStart = seq.getCurrentEventEncoding();
        } else if (simplifier instanceof MarkingSaturationTRSimplifier) {
          final ListBufferTransitionRelation rel =
            simplifier.getTransitionRelation();
          final int numPreconditionMarkings =
            rel.getNumberOfMarkings(PRECONDITION_MARKING, true);
          final TransitionRelationSimplifier last =
            seq.getLastPartitionSimplifier();
          EventEncoding enc = mEventEncodingAtStart;
          mEventEncodingAtStart = null;
          if (last instanceof MarkingRemovalTRSimplifier) {
            seq.removeLastPartitionSimplifier(enc);
            if (mNumberOfPreconditionMarkingsAtStart ==
                numPreconditionMarkings &&
                rel.getNumberOfMarkings(DEFAULT_MARKING, true) ==
                mNumberOfDefaultMarkingsAtStart) {
              return;
            }
            mNumberOfPreconditionMarkingsBeforeSaturate =
              mNumberOfPreconditionMarkingsAtStart;
          }
          if (numPreconditionMarkings >
              mNumberOfPreconditionMarkingsBeforeSaturate) {
            if (enc == null) {
              enc = seq.getCurrentEventEncoding();
            }
            final TRAbstractionStep pred =
              seq.getLastIntermediateStepOrPredecessor();
            final TRAbstractionStep step =
              new TRAbstractionStepPreconditionSaturation(pred, enc, simplifier);
            seq.append(step);
            return;
          }
        }
      } else {
        mEventEncodingAtStart = null;
      }
      super.onSimplificationFinish(simplifier, result);
    }

    //#######################################################################
    //# Data Members
    private EventEncoding mEventEncodingAtStart;
    private int mNumberOfDefaultMarkingsAtStart;
    private int mNumberOfPreconditionMarkingsAtStart;
    private int mNumberOfPreconditionMarkingsBeforeSaturate;
  }


  //#########################################################################
  //# Inner Class CertainConflictsListener
  class CertainConflictsListener extends PartitioningListener
  {
    @Override
    public void onSimplificationFinish
      (final TransitionRelationSimplifier simplifier, final boolean result)
    {
      final IntermediateAbstractionSequence seq =
        getIntermediateAbstractionSequence();
      if (result && isCounterExampleEnabled() && seq != null) {
        final LimitedCertainConflictsTRSimplifier certainConflictsRemover =
          (LimitedCertainConflictsTRSimplifier) simplifier;
        if (certainConflictsRemover.hasCertainConflictTransitions()) {
          final TRAbstractionStep pred =
            seq.getLastIntermediateStepOrPredecessor();
          final EventEncoding enc = seq.getCurrentEventEncoding();
          final TRAbstractionStep step =
            new TRAbstractionStepCertainConflicts(pred, enc,
                                                  DEFAULT_MARKING,
                                                  certainConflictsRemover);
          seq.append(step);
        } else {
          super.onSimplificationFinish(simplifier, result);
        }
      }
    }
  }


  //#########################################################################
  //# Inner Class OmegaRemovalListener
  class OmegaRemovalListener extends PartitioningListener
  {
    @Override
    public void onSimplificationFinish
      (final TransitionRelationSimplifier simplifier, final boolean result)
    {
      super.onSimplificationFinish(simplifier, result);
      final IntermediateAbstractionSequence seq =
        getIntermediateAbstractionSequence();
      final ListBufferTransitionRelation rel =
        simplifier.getTransitionRelation();
      if (result && isCounterExampleEnabled() && seq != null &&
          rel.isPropositionUsed(PRECONDITION_MARKING)) {
        final TransitionRelationSimplifier chain = getSimplifier();
        final TRPartition partition = chain.getResultPartition();
        final int numStates = partition == null ?
          rel.getNumberOfStates() : partition.getNumberOfStates();
        final BitSet markings = new BitSet(numStates);
        for (int s = 0; s < numStates; s++) {
          final int clazz = partition == null ? s : partition.getClassCode(s);
          if (clazz >= 0 && rel.isMarked(clazz, PRECONDITION_MARKING)) {
            markings.set(s);
          }
        }
        final TRAbstractionStepPartition step = (TRAbstractionStepPartition)
          seq.getLastIntermediateStepOrPredecessor();
        step.setRelevantPreconditionMarkings(markings);
      }
    }
  }


  //#########################################################################
  //# Data Members
  // Configuration
  private EventProxy mConfiguredDefaultMarking;
  private EventProxy mConfiguredPreconditionMarking;

  // Auxiliary events, status, and tools
  private EventProxy mUsedDefaultMarking;
  private EventProxy mUsedPreconditionMarking;
  private TRCompositionalLanguageInclusionChecker mLanguageInclusionChecker;
  private List<TRSubsystemInfo> mNonblockingSubsystems;

  private EventProxy mPreconditionEvent;
  private TRAutomatonProxy mPreconditionPropertyAutomaton;


  //#########################################################################
  //# Class Constants
  private static final String PROPERTY_NAME = ":never";

}
