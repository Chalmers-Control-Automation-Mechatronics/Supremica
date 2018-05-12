//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.TraceChecker;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <P>The core algorithm for compositional safety verification.</P>
 *
 * <P>This is the compositional part of the language inclusion check, which
 * performs compositional minimisation to check a single property. This class
 * is not usually called directly, as problems with more them one property
 * and controllability checks must be implemented by transforming the model
 * first. For a general language inclusion check, please use {@link
 * TRLanguageInclusionChecker}.</P>
 *
 * <P>The input model is expected to contain an arbitrary number of {@link
 * ComponentKind#PLANT PLANT} automata and a single {@link ComponentKind#SPEC
 * SPEC} automaton (as determined by the kind translator).</P>
 *
 * <P>The algorithm is compositional minimisation of the plant using natural
 * projection, implemented by an abstraction sequence consisting of:</P>
 * <OL>
 * <LI>&tau;-Loop Removal ({@link TauLoopRemovalTRSimplifier})</LI>
 * <LI>Subset Construction ({@link SubsetConstructionTRSimplifier})</LI>
 * <LI>Minimisation ({@link ObservationEquivalenceTRSimplifier})</LI>
 * </OL>
 * <P>After minimisation, a configurable monolithic {@link
 * LanguageInclusionChecker} is called to solve the abstracted problem.</P>
 *
 * <P>The simplifiers support the following special event types:</P>
 * <DL>
 * <DT><STRONG>Blocked events</STRONG></DT>
 * <DD>Events found to be disabled in a plant component (not in the
 * specification) are removed from the model.</DD>
 * <DT><STRONG>Failing events</STRONG></DT>
 * <DD>An event is considered failing if it is always disabled in the
 * specification and always enabled in all plants other than the one
 * considered. If such an event is possible, it is clear that the property
 * fails, so exploration beyond these events is unnecessary.</DD>
 * <DT><STRONG>Selfloop-only events</STRONG></DT>
 * <DD>If an event only appears on selfloops in the plants and not at all
 * in the specification, it can be removed from the model. If it appears only
 * on selfloops in all plants other than the one considered and not at all
 * in the specification, this fact is exploited for better minimisation.</DD>
 * </DL>
 *
 * <P><I>References:</I><BR>
 * Simon Ware, Robi Malik. The Use of Language Projection for Compositional
 * Verification of Discrete Event Systems. Proc. 9th International Workshop
 * on Discrete Event Systems (WODES'08), 322-327, G&ouml;teborg, Sweden,
 * 2008.<BR>
 * Colin Pilbrow, Robi Malik. Compositional Nonblocking Verification with
 * Always Enabled Events and Selfloop-only Events. Proc. 2nd International
 * Workshop on Formal Techniques for Safety-Critical Systems, FTSCS 2013,
 * 147-162, Queenstown, New Zealand, 2013.</P>
 *
 * @see TRLanguageInclusionChecker
 *
 * @author Robi Malik
 */

class TRCompositionalOnePropertyChecker
  extends AbstractTRCompositionalVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  TRCompositionalOnePropertyChecker(final SafetyDiagnostics diag)
  {
    super(null, null,
          new NativeSafetyVerifier(null, diag,
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
  protected void setUp()
    throws AnalysisException
  {
    // Set up plant automata ...
    super.setUp();
    final AnalysisResult result = getAnalysisResult();
    if (result.isFinished()) {
      return;
    }
    // Find the property ...
    final Logger logger = LogManager.getLogger();
    final KindTranslator translator = getKindTranslator();
    final ProductDESProxy des = getModel();
    mRawProperty = null;
    for (final AutomatonProxy aut : des.getAutomata()) {
      if (translator.getComponentKind(aut) == ComponentKind.SPEC) {
        if (mRawProperty == null) {
          mRawProperty = aut;
        } else {
          throw new AnalysisConfigurationException
            ("Calling " + ProxyTools.getShortClassName(this) +
             " with more than one property!");
        }
      }
    }
    if (mRawProperty == null) {
      logger.debug("No property given to check, returning TRUE.");
      setSatisfiedResult();
      return;
    }

    // Set up property automaton ...
    final TRAbstractionStepInput step;
    if (isPreservingEncodings() && mRawProperty instanceof TRAutomatonProxy) {
      final TRAutomatonProxy tr = (TRAutomatonProxy) mRawProperty;
      step = new TRAbstractionStepInput(tr);
    } else {
      final EventEncoding eventEnc = createInitialEventEncoding(mRawProperty);
      step = new TRAbstractionStepInput(mRawProperty, eventEnc);
    }
    final TransitionRelationSimplifier simplifier = getSimplifier();
    final int config = simplifier.getPreferredInputConfiguration();
    mConvertedProperty = step.getOutputAutomaton(config);
    mConvertedProperty.setKind(ComponentKind.SPEC);
    if (isCounterExampleEnabled()) {
      addAbstractionStep(step, mConvertedProperty);
    }
    if (!hasInitialState(mConvertedProperty)) {
      logger.debug("System fails property {}, which has no initial state.",
                   mRawProperty.getName());
      result.setSatisfied(false);
      final TRSubsystemInfo subsys = getCurrentSubsystem();
      dropSubsystem(subsys);
      dropTrivialAutomaton(mConvertedProperty);
      return;
    }
    final ListBufferTransitionRelation rel =
      mConvertedProperty.getTransitionRelation();
    rel.removeProperSelfLoopEvents();
    final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
    mMonolithicAutomataLimit = iter.advance() ? 1 : 2;
    getPreselectionHeuristic().setContext(this);
    mHasStronglyFailingEvent = false;
    final SpecialEventsFinder finder = getSpecialEventsFinder();
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
      logger.debug("Skipping trivial property {}.", mRawProperty.getName());
      result.setSatisfied(true);
      return;
    }
    finder.setAlwaysEnabledEventsDetected(mHasStronglyFailingEvent);

    final TRSubsystemInfo subsys = getCurrentSubsystem();
    subsys.registerEvents(mConvertedProperty, status, true);
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mRawProperty = null;
    mConvertedProperty = null;
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

  /**
   * Override that ensures only the plants (all automata except the property)
   * are initialised by the call to superclass {@link #setUp()} method.
   */
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

  /**
   * Override to calculate the minimum number of components in a final
   * monolithic check. The property should always be included, if present.
   * If the property is a one-state blocking automaton, it is optimised out
   * and the final monolithic check has only one automaton.
   */
  @Override
  protected int getMonolithicAutomataLimit()
  {
    return mMonolithicAutomataLimit;
  }

  @Override
  protected boolean earlyTerminationCheck(final TRSubsystemInfo subsys)
  {
    for (final TREventInfo info : subsys.getEvents()) {
      if (info.isExternal()) {
        return false;
      }
    }
    final Logger logger = LogManager.getLogger();
    logger.debug("Dropping subsystem because it shares no events with property {}.",
                 mConvertedProperty.getName());
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
      name = mConvertedProperty.getName();
      automata = Collections.singletonList(mConvertedProperty);
    } else {
      final List<TRAutomatonProxy> plants = subsys.getAutomata();
      final int numAutomata = plants.size() + 1;
      automata = new ArrayList<>(numAutomata);
      automata.addAll(plants);
      automata.add(mConvertedProperty);
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
    final Logger logger = LogManager.getLogger();
    if (monolithicResult.isSatisfied()) {
      logger.debug("Subsystem satisfies property {}.", mConvertedProperty.getName());
      dropSubsystem(subsys);
      return setSatisfiedResult();
    } else {
      logger.debug("Subsystem fails property {}.", mConvertedProperty.getName());
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
    final KindTranslator translator = new PropertyKindTranslator();
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
      bisimulator.setTransitionLimit(transitionLimit);
      bisimulator.setSimplificationListener(listener);
      chain.add(bisimulator);
      chain.setPreferredOutputConfiguration
        (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      return chain;
    }
  };


  //#########################################################################
  //# Inner Class PropertyKindTranslator
  private class PropertyKindTranslator implements KindTranslator
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      if (aut == mConvertedProperty || aut == mRawProperty) {
        return ComponentKind.SPEC;
      } else {
        return ComponentKind.PLANT;
      }
    }

    @Override
    public EventKind getEventKind(final EventProxy event)
    {
      final KindTranslator translator = getKindTranslator();
      return translator.getEventKind(event);
    }
  }


  //#########################################################################
  //# Data Members
  // Configuration
  private final SafetyDiagnostics mDiagnostics;

  // Data Structures
  private AutomatonProxy mRawProperty;
  private TRAutomatonProxy mConvertedProperty;
  private int mMonolithicAutomataLimit;
  private boolean mHasStronglyFailingEvent;

}
