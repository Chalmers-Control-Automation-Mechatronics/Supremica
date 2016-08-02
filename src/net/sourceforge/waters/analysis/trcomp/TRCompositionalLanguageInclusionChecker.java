//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.cpp.analysis.NativeSafetyVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultAnalysisResult;
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
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;

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
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.SafetyVerifier
  @Override
  public SafetyDiagnostics getDiagnostics()
  {
    return mDiagnostics;
  }

  @Override
  public TRSafetyTraceProxy getCounterExample()
  {
    final VerificationResult result = getAnalysisResult();
    return (TRSafetyTraceProxy) result.getCounterExample();
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
    final long start = System.currentTimeMillis();
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
            setAnalysisResult(result);
          } else if (subResult != null) {
            result.merge(subResult);
          }
          if (result != null && !result.isSatisfied()) {
            final long stop = System.currentTimeMillis();
            result.setRuntime(stop - start);
          }
        }
      }
    }
    if (!hasProperty) {
      logger.debug("Did not find any properties to check, returning TRUE.");
      result = createAnalysisResult();
      result.setSatisfied(true);
      result.setNumberOfAutomata(0);
      result.setNumberOfStates(0);
      result.setNumberOfTransitions(0);
      final long usage = DefaultAnalysisResult.getCurrentMemoryUsage();
      result.updatePeakMemoryUsage(usage);
      setAnalysisResult(result);
    }
    final long stop = System.currentTimeMillis();
    result.setRuntime(stop - start);
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
    final TRAbstractionStepInput step;
    if (getPreservingEncodings() && mRawProperty instanceof TRAutomatonProxy) {
      final TRAutomatonProxy tr = (TRAutomatonProxy) mRawProperty;
      step = new TRAbstractionStepInput(tr);
    } else {
      final EventEncoding eventEnc = createInitialEventEncoding(mRawProperty);
      step = new TRAbstractionStepInput(mRawProperty, eventEnc);
    }
    final TransitionRelationSimplifier simplifier = getSimplifier();
    final int config = simplifier.getPreferredInputConfiguration();
    mCurrentProperty = step.createOutputAutomaton(config);
    if (isCounterExampleEnabled()) {
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
  protected int getMonolithicAutomataLimit()
  {
    if (mRawProperty instanceof TRAutomatonProxy) {
      final TRAutomatonProxy aut = (TRAutomatonProxy) mRawProperty;
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
      return iter.advance() ? 1 : 2;
    } else {
      final int numTrans = mRawProperty.getTransitions().size();
      return numTrans > 0 ? 1 : 2;
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
    recordMonolithicAttempt(des);
    final ModelVerifier mono = getMonolithicVerifier();
    mono.setModel(des);
    final CompositionalAnalysisResult combinedResult = getAnalysisResult();
    final AnalysisResult monolithicResult;
    try {
      mono.run();
    } finally {
      monolithicResult = mono.getAnalysisResult();
      combinedResult.addMonolithicAnalysisResult(monolithicResult);
    }
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
        final TRTraceProxy extension =
          TRAbstractionStepMonolithic.createTraceExtension(trace, preds, this);
        final TRAbstractionStep step =
          new TRAbstractionStepMonolithic(name, extension);
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
   * <P>The projection abstraction sequence. This transition relation
   * simplifier chain calculates a minimal deterministic recogniser of the
   * language of its input transition relation, after hiding local events.
   * It is implemented in the following steps.</P>
   * <OL>
   * <LI>{@link SpecialEventsTRSimplifier} to hide local events.</LI>
   * <LI>{@link TauLoopRemovalTRSimplifier} to remove loops of only local
   *     events. This is for performance improvement of the following steps.</LI>
   * <LI>{@link SubsetConstructionTRSimplifier} to perform powerset construction
   *     and obtain a deterministic recogniser without tau transitions that
   *     accepts the same language.</LI>
   * <LI>{@link ObservationEquivalenceTRSimplifier} to get the minimal
   *     language-equivalent deterministic automaton.</LI>
   * </OL>
   */
  public static final TRToolCreator<TransitionRelationSimplifier> PROJ =
    new TRToolCreator<TransitionRelationSimplifier>("PROJ")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalAnalyzer analyzer)
    {
      final ChainTRSimplifier chain = analyzer.startAbstractionChain();
      // startAbstractionChain() adds a SpecialEventsTRSimplifier.
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
