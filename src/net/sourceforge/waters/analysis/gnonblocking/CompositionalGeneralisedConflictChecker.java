//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   CompositionalGeneralisedConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TObjectIntHashMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.op.ObserverProjectionTransitionRelation;
import net.sourceforge.waters.analysis.op.StateEncoding;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.EventNotFoundException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.SynchronousProductStateMap;
import net.sourceforge.waters.model.analysis.TraceChecker;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;

import org.apache.log4j.Logger;


/**
 * @author Rachel Francis
 */

public class CompositionalGeneralisedConflictChecker extends
    AbstractConflictChecker
{

  // #########################################################################
  // # Constructors
  /**
   * Creates a new conflict checker without a model or marking proposition.
   */
  public CompositionalGeneralisedConflictChecker(
                                                 final ProductDESProxyFactory factory)
  {
    super(null, factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model satisfies
   * generalised nonblocking with respect to multiple marking propositions.
   *
   * @param model
   *          The model to be checked by this conflict checker.
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalGeneralisedConflictChecker(
                                                 final ProductDESProxy model,
                                                 final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  /**
   * Creates a new conflict checker to check a particular model for generalised
   * nonblocking.
   *
   * @param model
   *          The model to be checked by this conflict checker.
   * @param marking
   *          The proposition event that defines which states are marked. Every
   *          state has a list of propositions attached to it; the conflict
   *          checker considers only those states as marked that are labelled by
   *          <CODE>marking</CODE>, i.e., their list of propositions must
   *          contain this event(exactly the same object).
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalGeneralisedConflictChecker(
                                                 final ProductDESProxy model,
                                                 final EventProxy marking,
                                                 final ProductDESProxyFactory factory)
  {
    super(model, marking, factory);
  }

  /**
   * Creates a new conflict checker to check a particular model for generalised
   * nonblocking.
   *
   * @param model
   *          The model to be checked by this conflict checker.
   * @param marking
   *          The proposition event that defines which states are marked. Every
   *          state has a list of propositions attached to it; the conflict
   *          checker considers only those states as marked that are labelled by
   *          <CODE>marking</CODE>, i.e., their list of propositions must
   *          contain this event(exactly the same object).
   * @param preMarking
   *          The proposition event that defines which states have alpha
   *          (precondition) markings.
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalGeneralisedConflictChecker(
                                                 final ProductDESProxy model,
                                                 final EventProxy marking,
                                                 final EventProxy preMarking,
                                                 final ProductDESProxyFactory factory)
  {
    super(model, marking, preMarking, factory);
  }

  // #########################################################################
  // # Overrides for net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  public CompositionalGeneralisedConflictCheckerVerificationResult getAnalysisResult()
  {
    return (CompositionalGeneralisedConflictCheckerVerificationResult) super
        .getAnalysisResult();
  }

  @Override
  protected CompositionalGeneralisedConflictCheckerVerificationResult createAnalysisResult()
  {
    return new CompositionalGeneralisedConflictCheckerVerificationResult();
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final CompositionalGeneralisedConflictCheckerVerificationResult stats =
        getAnalysisResult();
    stats.setSuccessfulCompositionCount(mSuccessfulCompositionCount);
    stats.setUnsuccessfulCompositionCount(mUnsuccessfulCompositionCount);
    stats.setAbstractionRuleStats(mAbstractionRules);
    stats.setPeakNumberOfStates(mPeakNumberOfStates);
    stats.setTotalNumberOfStates(mTotalNumberOfStates);
    stats.setPeakNumberOfTransitions(mPeakNumberOfTransitions);
    stats.setTotalNumberOfTransitions(mTotalNumberOfTransitions);
    stats.setComposedModelStateCount(mComposedModelNumberOfStates);
    stats.setComposedModelTransitionCount(mComposedModelNumberOfTransitions);
  }

  // Ugly override to make this method visible within package.
  @Override
  protected EventProxy getUsedMarkingProposition()
      throws EventNotFoundException
  {
    return super.getUsedMarkingProposition();
  }

  @Override
  public void setGeneralisedPrecondition(final EventProxy marking)
  {
    super.setGeneralisedPrecondition(marking);
    mUsedPreconditionMarking = null;
  }

  /**
   * Gets the precondition marking proposition to be used. This method returns
   * the marking proposition specified by the
   * {@link #setGeneralisedPrecondition(EventProxy)
   * setGeneralisedPrecondition()} method, if non-null, or creates an alpha
   * marking if the model does not contain one.
   */
  protected EventProxy getUsedPreconditionMarkingProposition()
  {
    if (mUsedPreconditionMarking == null) {
      if (getGeneralisedPrecondition() == null) {
        final ProductDESProxyFactory factory = getFactory();
        final EventProxy alpha =
            factory.createEventProxy(":alpha", EventKind.PROPOSITION);

        mUsedPreconditionMarking = alpha;
      } else {
        mUsedPreconditionMarking = getGeneralisedPrecondition();
      }
    }
    return mUsedPreconditionMarking;
  }

  // #########################################################################
  // # Invocation
  public boolean run() throws AnalysisException
  {
    setUp();
    try {
      ProductDESProxy model = getModel();
      mapEventsToAutomata(model);
      final List<Step> modifyingSteps = new ArrayList<Step>();

      // performs hiding and abstraction for each automaton individually
      final KindTranslator translator = getKindTranslator();
      final List<AutomatonProxy> remainingAut =
          new ArrayList<AutomatonProxy>(model.getAutomata().size());
      boolean modified = false;
      for (final AutomatonProxy aut : model.getAutomata()) {
        if (translator.getComponentKind(aut) == ComponentKind.PROPERTY) {
          modified = true;
        } else {
          final List<AutomatonProxy> autAsList = Collections.singletonList(aut);
          final Set<EventProxy> localEvents = identifyLocalEvents(autAsList);
          if (localEvents.size() > 0) {
            try {
              final AutomatonProxy abstractedAut =
                  hideAndAbstract(aut, localEvents);
              remainingAut.add(abstractedAut);
              modified = true;
              modifyingSteps.addAll(mTemporaryModifyingSteps);
            } catch (final OverflowException exception) {
              remainingAut.add(aut);
            }
            mTemporaryModifyingSteps.clear();
          } else {
            remainingAut.add(aut);
          }
        }
      }
      if (modified) {
        final Candidate candidate = new Candidate(remainingAut, null);
        model = candidate.createProductDESProxy(getFactory());
        mapEventsToAutomata(model);
      }

      outer: while (remainingAut.size() > 1) {
        final List<Candidate> candidates = findCandidates(model);
        Candidate candidate;
        AutomatonProxy syncProduct = null;
        while (true) {
          mTemporaryModifyingSteps = new ArrayList<Step>();
          candidate = evaluateCandidates(candidates);
          if (candidate == null) {
            break outer;
          }
          try {
            syncProduct = composeSynchronousProduct(candidate);
            final AutomatonProxy abstractedAut =
                hideAndAbstract(syncProduct, candidate.getLocalEvents());

            // removes the composed automata for this candidate from the set of
            // remaining automata and adds the newly composed candidate if it
            // was not a trivial automaton
            remainingAut.removeAll(candidate.getAutomata());
            if (checkTrivial(abstractedAut)) {
              updateEventsToAutomata(null, candidate.getAutomata());
              mTrivialAbstractedAutomata.add(abstractedAut);
            } else {
              remainingAut.add(abstractedAut);
              updateEventsToAutomata(abstractedAut, candidate.getAutomata());
            }
            // updates the current model to find candidates from
            final Candidate newModel = new Candidate(remainingAut, null);
            model = newModel.createProductDESProxy(getFactory());
            mSuccessfulCompositionCount++;
            modifyingSteps.addAll(mTemporaryModifyingSteps);
            break;
          } catch (final OverflowException e) {
            mUnsuccessfulCompositionCount++;
            candidates.remove(candidate);
            mUnsuccessfulCandidates.add(candidate);
          }
        }
      }
      final ConflictChecker checker =
          new NativeConflictChecker(model, getUsedMarkingProposition(),
              getFactory());
      // final ConflictChecker checker = new MonolithicConflictChecker(model,
      // getUsedMarkingProposition(), getFactory());
      checker
          .setGeneralisedPrecondition(getUsedPreconditionMarkingProposition());
      checker.setNodeLimit(mFinalStepNodeLimit);
      checker.setTransitionLimit(mFinalStepTransitionLimit);
      final boolean result = checker.run();
      mComposedModelNumberOfStates =
          checker.getAnalysisResult().getTotalNumberOfStates();
      mComposedModelNumberOfTransitions =
          checker.getAnalysisResult().getTotalNumberOfTransitions();
      if (result) {
        setSatisfiedResult();
      } else {
        final ConflictTraceProxy counterexample = checker.getCounterExample();
        final int size = modifyingSteps.size();
        ConflictTraceProxy convertedTrace = counterexample;
        convertedTrace = saturateTrace(counterexample);
        TraceChecker.checkCounterExample(convertedTrace, true);
        final ListIterator<Step> iter = modifyingSteps.listIterator(size);
        while (iter.hasPrevious()) {
          final Step step = iter.previous();
          final ConflictTraceProxy newTrace = step.convertTrace(convertedTrace);
          TraceChecker.checkCounterExample(newTrace, true);
          convertedTrace = newTrace;
        }
        setFailedResult(convertedTrace);
      }
      return result;
    } finally {
      tearDown();
    }
  }

  /**
   * Checks if an automaton is trivial. Trivial in this case is an automaton
   * with no transitions and one state which has the generalised precondition
   * marking and default marking.
   */
  private boolean checkTrivial(final AutomatonProxy abstractedAut)
  {
    // checks alphabet for propositions
    if (abstractedAut.getEvents().containsAll(mPropositions)) {
      if (abstractedAut.getStates().size() == 1) {
        final Iterator<StateProxy> stateIter =
            abstractedAut.getStates().iterator();
        final StateProxy singleState = stateIter.next();
        assert stateIter.hasNext() == false;
        if (singleState.getPropositions().containsAll(mPropositions)) {
          if (abstractedAut.getTransitions().size() == 0) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private AutomatonProxy hideAndAbstract(final AutomatonProxy aut,
                                         final Set<EventProxy> localEvents)
      throws AnalysisException
  {
    final AutomatonProxy autToAbstract;
    final EventProxy tau = createTauEvent(aut);
    if (localEvents != null && localEvents.size() > 0) {
      autToAbstract = hideLocalEvents(aut, localEvents, tau);
    } else {
      autToAbstract = aut;
    }

    final AutomatonProxy abstractedAut =
        applyAbstractionRules(autToAbstract, tau);
    return abstractedAut;
  }

  /**
   * Fills in the target states in the state maps for each step of the trace for
   * all automata.
   */
  private ConflictTraceProxy saturateTrace(
                                           final ConflictTraceProxy counterexample)
  {
    Set<AutomatonProxy> traceAutomata = counterexample.getAutomata();
    if (mTrivialAbstractedAutomata.size() > 0) {
      traceAutomata = new HashSet<AutomatonProxy>(counterexample.getAutomata());
      traceAutomata.addAll(mTrivialAbstractedAutomata);
    }
    final List<TraceStepProxy> traceSteps = counterexample.getTraceSteps();
    final List<TraceStepProxy> convertedSteps = new ArrayList<TraceStepProxy>();
    Map<AutomatonProxy,StateProxy> prevStepMap = null;

    for (final TraceStepProxy step : traceSteps) {
      final Map<AutomatonProxy,StateProxy> stepMap =
          new HashMap<AutomatonProxy,StateProxy>();
      final EventProxy stepEvent = step.getEvent();
      for (final AutomatonProxy aut : traceAutomata) {
        StateProxy targetState = step.getStateMap().get(aut);
        if (targetState == null) {
          if (stepEvent != null) {
            targetState = findSuccessor(aut, prevStepMap.get(aut), stepEvent);
          } else {
            targetState = getInitialState(aut, step);
          }
          stepMap.put(aut, targetState);
        } else {
          stepMap.put(aut, targetState);
        }
      }
      final TraceStepProxy convertedStep =
          getFactory().createTraceStepProxy(stepEvent, stepMap);
      convertedSteps.add(convertedStep);
      prevStepMap = new HashMap<AutomatonProxy,StateProxy>(stepMap);
    }
    final ConflictTraceProxy saturatedCounterexample =
        getFactory().createConflictTraceProxy(counterexample.getName(),
                                              counterexample.getComment(),
                                              counterexample.getLocation(),
                                              counterexample.getProductDES(),
                                              traceAutomata, convertedSteps,
                                              counterexample.getKind());
    return saturatedCounterexample;
  }

  /**
   * Finds the successor/target state in the given automaton, given a source
   * state and event. Used in deterministic cases only (in nondeterministic
   * cases the successors are already available in the step's stateMap).
   */
  private static StateProxy findSuccessor(final AutomatonProxy aut,
                                          final StateProxy sourceState,
                                          final EventProxy stepEvent)
  {
    StateProxy targetState = sourceState;
    for (final TransitionProxy transition : aut.getTransitions()) {
      if (transition.getEvent() == stepEvent
          && transition.getSource() == sourceState) {
        targetState = transition.getTarget();
        break;
      }
    }
    return targetState;
  }

  /**
   * Finds the initial state of an automaton. A TraceStepProxy object is passed
   * for the case of multiple initial states.
   */
  protected StateProxy getInitialState(final AutomatonProxy aut,
                                       final TraceStepProxy traceStep)
  {
    // if there is more than one initial state, the trace has the info
    final Map<AutomatonProxy,StateProxy> stepMap = traceStep.getStateMap();
    StateProxy initial = stepMap.get(aut);
    // else there is only one initial state
    if (initial == null) {
      for (final StateProxy state : aut.getStates()) {
        if (state.isInitial()) {
          initial = state;
          break;
        }
      }
    }
    return initial;
  }

  // #########################################################################
  // # Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  /**
   * Initialises required variables to default values if the user has not
   * configured them.
   */
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    mEventsToAutomata = new HashMap<EventProxy,Set<AutomatonProxy>>();
    mUnsuccessfulCandidates = new HashSet<Candidate>();
    mTrivialAbstractedAutomata = new HashSet<AutomatonProxy>();
    if (mPreselectingHeuristic == null) {
      final PreselectingHeuristic defaultHeuristic = new HeuristicMinTa();
      // final PreselectingHeuristic defaultHeuristic = new HeuristicMaxS();
      // final PreselectingHeuristic defaultHeuristic = new HeuristicMustL();
      setPreselectingHeuristic(defaultHeuristic);
    }
    if (mSelectingHeuristics == null) {
      final SelectingHeuristic defaultHeuristic = new HeuristicMaxL();
      setSelectingHeuristic(defaultHeuristic);
    }
    // reset statistics
    mSuccessfulCompositionCount = 0;
    mUnsuccessfulCompositionCount = 0;
    mTotalNumberOfStates = 0;
    mPeakNumberOfStates = 0;
    mTotalNumberOfTransitions = 0;
    mPeakNumberOfTransitions = 0;
    mComposedModelNumberOfStates = 1;
    mComposedModelNumberOfTransitions = 1;

    mTemporaryModifyingSteps = new ArrayList<Step>();
    mUsedPreconditionMarking = null;
    final EventProxy alpha = getUsedPreconditionMarkingProposition();
    final EventProxy omega = getUsedMarkingProposition();
    mPropositions = new ArrayList<EventProxy>(2);
    mPropositions.add(alpha);
    mPropositions.add(omega);

    if (mAbstractionRules == null) {
      mAbstractionRules = new LinkedList<AbstractionRule>();

      final TauLoopRemovalRule tlrRule =
          new TauLoopRemovalRule(getFactory(), mPropositions);
      mAbstractionRules.add(tlrRule);

      final ObservationEquivalenceRule oeRule =
          new ObservationEquivalenceRule(getFactory(), mPropositions);
      oeRule.setTransitionLimit(getInternalStepTransitionLimit());
      mAbstractionRules.add(oeRule);

      final RemovalOfAlphaMarkingsRule ramRule =
          new RemovalOfAlphaMarkingsRule(getFactory(), mPropositions);
      ramRule.setAlphaMarking(alpha);
      mAbstractionRules.add(ramRule);

      final RemovalOfDefaultMarkingsRule rdmRule =
          new RemovalOfDefaultMarkingsRule(getFactory(), mPropositions);
      rdmRule.setAlphaMarking(alpha);
      rdmRule.setDefaultMarking(omega);
      mAbstractionRules.add(rdmRule);

      final RemovalOfNoncoreachableStatesRule rnsRule =
          new RemovalOfNoncoreachableStatesRule(getFactory(), mPropositions);
      rnsRule.setAlphaMarking(alpha);
      rnsRule.setDefaultMarking(omega);
      mAbstractionRules.add(rnsRule);

      final DeterminisationOfNonAlphaStatesRule dnasRule =
          new DeterminisationOfNonAlphaStatesRule(getFactory(), mPropositions);
      dnasRule.setAlphaMarking(alpha);
      dnasRule.setTransitionLimit(getInternalStepTransitionLimit());
      mAbstractionRules.add(dnasRule);

      final RemovalOfTauTransitionsLeadingToNonAlphaStatesRule rttlnsRule =
          new RemovalOfTauTransitionsLeadingToNonAlphaStatesRule(getFactory(),
              mPropositions);
      rttlnsRule.setAlphaMarking(alpha);
      mAbstractionRules.add(rttlnsRule);

      final RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule rttonsRule =
          new RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule(
              getFactory(), mPropositions);
      rttonsRule.setAlphaMarking(alpha);
      rttonsRule.setDefaultMarking(omega);
      mAbstractionRules.add(rttonsRule);
    }
  }

  // #########################################################################
  // # Auxiliary Methods
  private AutomatonProxy applyAbstractionRules(AutomatonProxy autToAbstract,
                                               final EventProxy tau)
      throws AnalysisException
  {
    final Logger logger = getLogger();
    AutomatonProxy abstractedAut = autToAbstract;
    for (final AbstractionRule rule : mAbstractionRules) {
      if (logger.isDebugEnabled()) {
        final String msg =
            "Applying " + ProxyTools.getShortClassName(rule) + " to automaton "
                + abstractedAut.getName() + " with "
                + abstractedAut.getStates().size() + " states and "
                + abstractedAut.getTransitions().size() + " transitions ...";
        logger.debug(msg);
      }
      abstractedAut = rule.applyRule(autToAbstract, tau);
      if (logger.isDebugEnabled()) {
        final String msg =
            "Finished " + ProxyTools.getShortClassName(rule)
                + " for automaton " + abstractedAut.getName() + ", now "
                + abstractedAut.getStates().size() + " states and "
                + abstractedAut.getTransitions().size() + " transitions.";
        logger.debug(msg);
      }
      if (autToAbstract != abstractedAut) {
        final Step step = rule.createStep(this, abstractedAut);
        mTemporaryModifyingSteps.add(step);
      }
      autToAbstract = abstractedAut;
      rule.cleanup();
    }
    return abstractedAut;
  }

  /**
   * Builds the synchronous product for a given candidate. Returns null if
   * building the synchronous product causes an overflow exception.
   */
  private AutomatonProxy composeSynchronousProduct(final Candidate candidate)
      throws AnalysisException
  {
    // creates a model which includes only the candidate, to build the
    // synchronous product of
    final ProductDESProxy candidateModel =
        candidate.createProductDESProxy(getFactory());

    final MonolithicSynchronousProductBuilder composer =
        new MonolithicSynchronousProductBuilder(candidateModel, getFactory());
    composer.setPropositions(mPropositions);
    composer.setTransitionLimit(mSyncProductTransitionLimit);
    composer.setNodeLimit(mSyncProductNodeLimit);

    composer.run();

    final AutomatonProxy syncProduct = composer.getComputedAutomaton();

    // records statistics
    final double numberOfStates = syncProduct.getStates().size();
    final double numberOfTransitions = syncProduct.getTransitions().size();
    mTotalNumberOfStates += numberOfStates;
    mTotalNumberOfTransitions += numberOfTransitions;
    if (numberOfStates > mPeakNumberOfStates) {
      mPeakNumberOfStates = numberOfStates;
    }
    if (numberOfTransitions > mPeakNumberOfTransitions) {
      mPeakNumberOfTransitions = numberOfTransitions;
    }

    final CompositionStep step =
        new CompositionStep(syncProduct, composer.getStateMap());
    mTemporaryModifyingSteps.add(step);
    return syncProduct;
  }

  /**
   * Creates a tau event with a name that reflects the automaton's alphabet it
   * will becomes part of.
   */
  private EventProxy createTauEvent(final AutomatonProxy automaton)
  {
    final String tauStateName = "tau:" + automaton.getName();
    final EventProxy tau =
        getFactory().createEventProxy(tauStateName, EventKind.UNCONTROLLABLE);
    return tau;
  }

  /**
   * Hides the local events for a given candidate (replaces the events with a
   * silent event "tau").
   */
  private AutomatonProxy hideLocalEvents(final AutomatonProxy automaton,
                                         final Set<EventProxy> localEvents,
                                         final EventProxy tau)
  {
    // replaces events on transitions with silent event and removes the local
    // events from the automaton alphabet
    final Collection<TransitionProxy> newTransitions =
        new ArrayList<TransitionProxy>();
    for (final TransitionProxy transition : automaton.getTransitions()) {
      final EventProxy event = transition.getEvent();
      if (localEvents.contains(event)) {
        final TransitionProxy newTrans =
            getFactory().createTransitionProxy(transition.getSource(), tau,
                                               transition.getTarget());
        newTransitions.add(newTrans);
      } else {
        newTransitions.add(transition);
      }
    }
    final ArrayList<EventProxy> newEvents = new ArrayList<EventProxy>();
    for (final EventProxy event : automaton.getEvents()) {
      if (!localEvents.contains(event)) {
        newEvents.add(event);
      }
    }
    newEvents.add(tau);
    final AutomatonProxy newAut =
        getFactory()
            .createAutomatonProxy(automaton.getName(), automaton.getKind(),
                                  newEvents, automaton.getStates(),
                                  newTransitions);
    final HidingStep step = new HidingStep(newAut, automaton, localEvents, tau);
    mTemporaryModifyingSteps.add(step);
    return newAut;
  }

  /**
   * Maps the events in the model to a set of the automaton that contain the
   * event in their alphabet.
   */
  private void mapEventsToAutomata(final ProductDESProxy model)
  {
    mEventsToAutomata =
        new HashMap<EventProxy,Set<AutomatonProxy>>(model.getEvents().size());
    for (final AutomatonProxy aut : model.getAutomata()) {
      for (final EventProxy event : aut.getEvents()) {
        if (event.getKind() != EventKind.PROPOSITION) {
          if (!mEventsToAutomata.containsKey(event)) {
            final Set<AutomatonProxy> automata = new THashSet<AutomatonProxy>();
            mEventsToAutomata.put(event, automata);
          }
          mEventsToAutomata.get(event).add(aut);
        }
      }
    }
  }

  private void updateEventsToAutomata(final AutomatonProxy autToAdd,
                                      final List<AutomatonProxy> autToRemove)
  {
    // adds the new automaton to the events it contains
    if (autToAdd != null) {
      for (final EventProxy event : autToAdd.getEvents()) {
        if (event.getKind() != EventKind.PROPOSITION) {
          if (!mEventsToAutomata.containsKey(event)) {
            final Set<AutomatonProxy> automata = new THashSet<AutomatonProxy>();
            mEventsToAutomata.put(event, automata);
          }
          mEventsToAutomata.get(event).add(autToAdd);
        }
      }
    }
    // removes the automata which have been composed
    final Set<EventProxy> eventsToRemove = new HashSet<EventProxy>();
    for (final EventProxy event : mEventsToAutomata.keySet()) {
      mEventsToAutomata.get(event).removeAll(autToRemove);
      if (mEventsToAutomata.get(event).size() == 0) {
        eventsToRemove.add(event);
      }
    }
    for (final EventProxy event : eventsToRemove) {
      mEventsToAutomata.remove(event);
    }
  }

  /**
   * Finds the set of events that are local to a candidate (i.e. a set of
   * automata).
   */
  private Set<EventProxy> identifyLocalEvents(
                                              final List<AutomatonProxy> candidate)
  {
    final Set<EventProxy> localEvents = new HashSet<EventProxy>();
    for (final EventProxy event : mEventsToAutomata.keySet()) {
      final Set<AutomatonProxy> autWithEvent = mEventsToAutomata.get(event);
      if (candidate.containsAll(autWithEvent)) {
        localEvents.add(event);
      }
    }
    return localEvents;
  }

  /**
   * Uses a heuristic to evaluate the set of candidates to select a suitable
   * candidate to compose next.
   *
   * @param candidates
   * @return null when there are no candidates, or else the selected candidate.
   */
  private Candidate evaluateCandidates(List<Candidate> candidates)
  {
    if (candidates.size() == 0) {
      return null;
    }
    if (candidates.size() > 1) {
      final ListIterator<SelectingHeuristic> iter =
          mSelectingHeuristics.listIterator();
      while (iter.hasNext()) {
        final SelectingHeuristic heuristic = iter.next();
        candidates = heuristic.evaluate(candidates);
        if (candidates.size() == 1) {
          break;
        }
      }
    }
    return candidates.get(0);
  }

  /**
   * Finds the set of candidates to compose for a given model.
   */
  private List<Candidate> findCandidates(final ProductDESProxy model)
  {
    return mPreselectingHeuristic.evaluate(model);
  }

  /**
   * Sets the maximum number of states for an automaton being constructed by the
   * synchronous product.
   *
   * @param limit
   */
  public void setInternalStepNodeLimit(final int limit)
  {
    mSyncProductNodeLimit = limit;
  }

  public int getInternalStepNodeLimit()
  {
    return mSyncProductNodeLimit;
  }

  /**
   * Sets the maximum number of states for the final composed automaton which is
   * passed to the monolithic conflict checker.
   *
   * @param limit
   *          Maximum number of states for the automaton.
   */
  public void setFinalStepNodeLimit(final int limit)
  {
    mFinalStepNodeLimit = limit;
  }

  public int getFinalStepNodeLimit()
  {
    return mFinalStepNodeLimit;
  }

  @Override
  public int getNodeLimit()
  {
    if (mFinalStepNodeLimit < mSyncProductNodeLimit) {
      return mFinalStepNodeLimit;
    } else {
      return mFinalStepNodeLimit;
    }
  }

  @Override
  public void setNodeLimit(final int limit)
  {
    mFinalStepNodeLimit = limit;
    mSyncProductNodeLimit = limit;
  }

  /**
   * Sets the maximum number of Transition for an automaton being constructed by
   * the synchronous product.
   *
   * @param limit
   */
  public void setInternalStepTransitionLimit(final int limit)
  {
    mSyncProductTransitionLimit = limit;
  }

  public int getInternalStepTransitionLimit()
  {
    return mSyncProductTransitionLimit;
  }

  /**
   * Sets the maximum number of Transition for the final composed automaton
   * which is passed to another conflict checker.
   *
   * @param limit
   *          Maximum number of Transitions for the automaton.
   */
  public void setFinalStepTransitionLimit(final int limit)
  {
    mFinalStepTransitionLimit = limit;
  }

  public int getFinalStepTransitionLimit()
  {
    return mFinalStepTransitionLimit;
  }

  @Override
  public int getTransitionLimit()
  {
    if (mFinalStepTransitionLimit < mSyncProductTransitionLimit) {
      return mFinalStepTransitionLimit;
    } else {
      return mFinalStepTransitionLimit;
    }
  }

  @Override
  public void setTransitionLimit(final int limit)
  {
    mFinalStepTransitionLimit = limit;
    mSyncProductTransitionLimit = limit;
  }

  public HeuristicMinT createHeuristicMinT()
  {
    return new HeuristicMinT();
  }

  public HeuristicMinTa createHeuristicMinTa()
  {
    return new HeuristicMinTa();
  }

  public HeuristicMaxS createHeuristicMaxS()
  {
    return new HeuristicMaxS();
  }

  public HeuristicMustL createHeuristicMustL()
  {
    return new HeuristicMustL();
  }

  public HeuristicMinA createHeuristicMinA()
  {
    return new HeuristicMinA();
  }

  public HeuristicMaxL createHeuristicMaxL()
  {
    return new HeuristicMaxL();
  }

  public HeuristicMaxLt createHeuristicMaxLt()
  {
    return new HeuristicMaxLt();
  }

  public HeuristicMaxLOnTransitions createHeuristicMaxLOnTransitions()
  {
    return new HeuristicMaxLOnTransitions();
  }

  public HeuristicMaxC createHeuristicMaxC()
  {
    return new HeuristicMaxC();
  }

  public HeuristicMaxCt createHeuristicMaxCt()
  {
    return new HeuristicMaxCt();
  }

  public HeuristicMaxCOnTransitions createHeuristicMaxCOnTransitions()
  {
    return new HeuristicMaxCOnTransitions();
  }

  public HeuristicMinS createHeuristicMinS()
  {
    return new HeuristicMinS();
  }

  public HeuristicMinS createHeuristicMinSCommon()
  {
    return new HeuristicMinSCommon();
  }

  public void setPreselectingHeuristic(final PreselectingHeuristic heuristic)
  {
    mPreselectingHeuristic = heuristic;
  }

  /**
   * The given heuristic is used first to select a candidate to compose.
   */
  public void setSelectingHeuristic(final SelectingHeuristic heuristic)
  {
    mSelectingHeuristics = new ArrayList<SelectingHeuristic>(4);
    mSelectingHeuristics.add(heuristic);
    if (heuristic instanceof HeuristicMaxL) {
      mSelectingHeuristics.add(new HeuristicMaxC());
      mSelectingHeuristics.add(new HeuristicMinS());
      mSelectingHeuristics.add(new HeuristicMaxLOnTransitions());
      mSelectingHeuristics.add(new HeuristicMaxCOnTransitions());
    } else if (heuristic instanceof HeuristicMaxC) {
      mSelectingHeuristics.add(new HeuristicMaxL());
      mSelectingHeuristics.add(new HeuristicMinS());
      mSelectingHeuristics.add(new HeuristicMaxLOnTransitions());
      mSelectingHeuristics.add(new HeuristicMaxCOnTransitions());
    } else if (heuristic instanceof HeuristicMinS) {
      mSelectingHeuristics.add(new HeuristicMaxL());
      mSelectingHeuristics.add(new HeuristicMaxC());
      mSelectingHeuristics.add(new HeuristicMaxLOnTransitions());
      mSelectingHeuristics.add(new HeuristicMaxCOnTransitions());
    } else if (heuristic instanceof HeuristicMaxLOnTransitions) {
      mSelectingHeuristics.add(new HeuristicMaxL());
      mSelectingHeuristics.add(new HeuristicMaxC());
      mSelectingHeuristics.add(new HeuristicMinS());
      mSelectingHeuristics.add(new HeuristicMaxCOnTransitions());
    } else if (heuristic instanceof HeuristicMaxCOnTransitions) {
      mSelectingHeuristics.add(new HeuristicMaxL());
      mSelectingHeuristics.add(new HeuristicMaxC());
      mSelectingHeuristics.add(new HeuristicMinS());
      mSelectingHeuristics.add(new HeuristicMaxLOnTransitions());
    }
    mSelectingHeuristics.add(new HeuristicDefault());
  }

  /**
   * The first item in the list should be the first heuristic used to select a
   * candidate to compose, the last item in the list should be the last option.
   */
  public void setSelectingHeuristic(final List<SelectingHeuristic> heuristicList)
  {
    mSelectingHeuristics = heuristicList;
    mSelectingHeuristics.add(new HeuristicDefault());
  }

  /**
   * Sets the abstraction rules to apply and in which order.
   *
   * @param ruleList
   *          Rules are applied in order from the first item in the list through
   *          until the last.
   */
  public void setAbstractionRules(final List<AbstractionRule> ruleList)
  {
    mAbstractionRules = ruleList;
  }


  private abstract class PreselectingHeuristic
  {
    /**
     * Checks if a candidate is valid. To satisfy being a valid candidate it
     * must not have been previously tried and marked as unsuccessful, must have
     * at least one local event and its automata must have at least one shared
     * event.
     *
     * @param candidate
     *          The candidate to check.
     * @return True = valid, false = suppress candidate.
     */
    boolean validateCandidate(final Candidate candidate)
    {
      if (!mUnsuccessfulCandidates.contains(candidate)) {
        // if (checkForLocalEvent(candidate)) {
        // TODO:checkForLocalEvent() has to be disabled for pairing heuristics,
        // because there may be cases where there are no two automata containing
        // a local event. Sometimes every event is in at least three automata.
        // In that case, we have to pair automata without local events
        if (checkForSharedEvent(candidate)) {
          return true;
          // }
        }
      }
      return false;
    }

    @SuppressWarnings("unused")
    boolean checkForLocalEvent(final Candidate candidate)
    {
      if (candidate.getLocalEventCount() > 0) {
        return true;
      }
      return false;
    }

    /**
     * Checks if the automata of a candidate share at least one event.
     *
     * @param candidate
     *          Candidate to check.
     * @return True = at least one shared event, false = no shared event.
     */
    protected boolean checkForSharedEvent(final Candidate candidate)
    {
      final List<AutomatonProxy> candidateAut = candidate.getAutomata();
      for (final EventProxy event : mEventsToAutomata.keySet()) {
        if (event.getKind() != EventKind.PROPOSITION) {
          final Set<AutomatonProxy> autWithEvent = mEventsToAutomata.get(event);
          if (autWithEvent.containsAll(candidateAut)) {
            return true;
          }
        }
      }
      return false;
    }

    protected abstract List<Candidate> evaluate(final ProductDESProxy model);
  }


  /**
   * This class is to be used by a preselecting heuristic which requires pairing
   * a chosen automaton with all other to create candidates.
   *
   * @author rmf18
   */
  private abstract class PreselectingPairingHeuristic extends
      PreselectingHeuristic
  {
    /**
     * Finds a list of candidates based on a heuristic which requires pairing If
     * no candidates is eligible the automaton being paired is removed and
     * another pairing is attempted.
     */
    protected List<Candidate> evaluate(final ProductDESProxy model)
    {
      Collection<AutomatonProxy> automata = model.getAutomata();
      AutomatonProxy chosenAut = getHeuristicProperty(automata);
      List<Candidate> candidates = pairAutomata(chosenAut, automata);
      final int minCandidateSize = 3;
      if (candidates.size() == 0 && automata.size() >= minCandidateSize) {
        automata = new ArrayList<AutomatonProxy>(model.getAutomata());
        while (candidates.size() == 0 && automata.size() >= minCandidateSize) {
          automata.remove(chosenAut);
          chosenAut = Collections.min(automata, new AutomataComparator());
          candidates = pairAutomata(chosenAut, automata);
        }
      }
      return candidates;
    }

    /**
     * Pairs the chosen automaton with every other automaton in 'automata' and
     * creates a candidate for each.
     *
     * @param chosenAut
     *          The automaton to be paired.
     * @param automata
     *          The automata to pair chosenAut with.
     * @return The list of candidates creating by pairing chosenAut.
     */
    protected List<Candidate> pairAutomata(
                                           final AutomatonProxy chosenAut,
                                           final Collection<AutomatonProxy> automata)
    {
      final List<Candidate> candidates =
          new ArrayList<Candidate>(automata.size() - 1);
      if (automata.size() > 2) {
        for (final AutomatonProxy a : automata) {
          if (a != chosenAut) {
            final List<AutomatonProxy> pair = new ArrayList<AutomatonProxy>(2);
            // Bring pair into defined ordering.
            if (chosenAut.compareTo(a) < 0) {
              pair.add(chosenAut);
              pair.add(a);
            } else {
              pair.add(a);
              pair.add(chosenAut);
            }
            final Set<EventProxy> localEvents = identifyLocalEvents(pair);
            final Candidate candidate = new Candidate(pair, localEvents);
            candidate.setLocalEvents(localEvents);
            if (validateCandidate(candidate)) {
              candidates.add(candidate);
            }
          }
        }
      }
      return candidates;
    }


    /**
     * Compares two automata based on the type of figure the heuristic specifies
     * (i.e. number of transitions, or number of states).
     *
     * @author rmf18
     */
    protected class AutomataComparator implements Comparator<AutomatonProxy>
    {
      public int compare(final AutomatonProxy aut1, final AutomatonProxy aut2)
      {
        final int aut1Count = getHeuristicFigure(aut1);
        final int aut2Count = getHeuristicFigure(aut2);
        if (aut1Count < aut2Count)
          return -1;
        else if (aut1Count > aut2Count)
          return 1;
        else
          return 0;
      }
    }

    /**
     * The type of figure the heuristic uses (i.e. number of transitions, or
     * number of states).
     *
     * @param aut
     * @return
     */
    protected abstract int getHeuristicFigure(final AutomatonProxy aut);

    /**
     * The min or max of a collection.
     *
     * @param automata
     * @return
     */
    protected abstract AutomatonProxy getHeuristicProperty(
                                                           final Collection<AutomatonProxy> automata);

  }


  private class HeuristicMinT extends PreselectingPairingHeuristic
  {
    protected int getHeuristicFigure(final AutomatonProxy aut)
    {
      return aut.getTransitions().size();
    }

    protected AutomatonProxy getHeuristicProperty(
                                                  final Collection<AutomatonProxy> automata)
    {
      return (AutomatonProxy) Collections.min(automata,
                                              new AutomataComparator());
    }

  }


  private class HeuristicMinTa extends HeuristicMinT
  {
    @Override
    protected AutomatonProxy getHeuristicProperty(
                                                  final Collection<AutomatonProxy> automata)
    {
      final Collection<AutomatonProxy> autWithAlpha =
          new ArrayList<AutomatonProxy>();
      final EventProxy alpha = getUsedPreconditionMarkingProposition();
      for (final AutomatonProxy aut : automata) {
        if (aut.getEvents().contains(alpha)) {
          autWithAlpha.add(aut);
        }
      }
      if (autWithAlpha.size() == 0) {
        return super.getHeuristicProperty(automata);
      } /*
         * else if (autWithAlpha.size() == 1) { return
         * autWithAlpha.iterator().next(); }
         */
      return (AutomatonProxy) Collections.min(autWithAlpha,
                                              new AutomataComparator());
    }
  }


  /**
   * Performs step 1 of the approach to select the automata to compose. A
   * candidate is produced by pairing the automaton with the most states to
   * every other automaton in the model.
   */
  private class HeuristicMaxS extends PreselectingPairingHeuristic
  {
    protected int getHeuristicFigure(final AutomatonProxy aut)
    {
      return -aut.getStates().size();
    }

    protected AutomatonProxy getHeuristicProperty(
                                                  final Collection<AutomatonProxy> automata)
    {
      return (AutomatonProxy) Collections.max(automata,
                                              new AutomataComparator());
    }
  }


  private class HeuristicMustL extends PreselectingHeuristic
  {
    protected List<Candidate> evaluate(final ProductDESProxy model)
    {
      final List<Candidate> candidates =
          new ArrayList<Candidate>(mEventsToAutomata.keySet().size());
      for (final EventProxy event : mEventsToAutomata.keySet()) {
        final List<AutomatonProxy> automata =
            new ArrayList<AutomatonProxy>(mEventsToAutomata.get(event));
        assert automata.size() > 0;
        if ((automata.size() > 1)
            && (automata.size() < model.getAutomata().size())) {
          // Bring automata into defined ordering.
          Collections.sort(automata);
          final Set<EventProxy> localEvents = identifyLocalEvents(automata);
          final Candidate candidate = new Candidate(automata, localEvents);
          if (!candidates.contains(candidate)) {
            if (validateCandidate(candidate)) {
              candidates.add(candidate);
            }
          }
        }
      }
      return candidates;
    }

    /**
     * Returns true without searching for a shared event, since for this
     * heuristic candidates are automata which share an event.
     *
     * @param candidate
     * @return true, there is definitely a shared event.
     */
    @Override
    protected boolean checkForSharedEvent(final Candidate candidate)
    {
      return true;
    }

    /**
     * Returns true without searching for a local event.
     *
     * @param candidate
     * @return true, there is definitely a local event.
     */
    @Override
    protected boolean checkForLocalEvent(final Candidate candidate)
    {
      return true;
    }

  }


  private abstract class SelectingHeuristic
  {
    protected abstract double getHeuristicValue(Candidate candidate);

    protected abstract boolean getMaxOrMin(double currentMaxMin, double newValue);

    /**
     * Gets the number of events for this candidate, excluding tau.
     *
     * @param candidate
     * @return
     */
    protected int countCandidatesTotalEvents(final Candidate candidate)
    {
      int count = 0;
      for (final EventProxy event : mEventsToAutomata.keySet()) {
        if (event.getKind() != EventKind.PROPOSITION) {
          final Set<AutomatonProxy> autWithEvent = mEventsToAutomata.get(event);
          if (autWithEvent.size() > 1) {
            final List<AutomatonProxy> candidateAutomata =
                candidate.getAutomata();
            for (final AutomatonProxy candidateAut : candidateAutomata) {
              if (autWithEvent.contains(candidateAut)) {
                count++;
                break;
              }
            }
          }
        }
      }
      return count;
    }

    /**
     * Counts the number of events which are shared by automata of this
     * candidate, excluding tau.
     *
     * @param candidate
     * @return
     */
    protected int countCandidatesSharedEvents(final Candidate candidate)
    {
      int count = 0;
      final List<AutomatonProxy> candidateAut = candidate.getAutomata();
      for (final EventProxy event : mEventsToAutomata.keySet()) {
        if (event.getKind() != EventKind.PROPOSITION) {
          final Set<AutomatonProxy> autWithEvent = mEventsToAutomata.get(event);
          if (autWithEvent.size() > 1) {
            if (autWithEvent.containsAll(candidateAut)) {
              count++;
            }
          }
        }
      }
      return count;
    }

    /**
     * Gets the number of local events for this candidate, excluding tau.
     *
     * @param candidate
     * @return
     */
    protected int countCandidatesLocalEvents(final Candidate candidate)
    {
      int count = 0;
      for (final EventProxy event : candidate.getLocalEvents()) {
        assert event.getKind() != EventKind.PROPOSITION;
        final Set<AutomatonProxy> autWithEvent = mEventsToAutomata.get(event);
        if (autWithEvent.size() > 1) {
          count++;
        }
      }
      return count;
    }

    public List<Candidate> evaluate(final List<Candidate> candidates)
    {
      final Iterator<Candidate> it = candidates.iterator();
      List<Candidate> chosenCandidates = new ArrayList<Candidate>();
      final Candidate chosenCandidate = it.next();
      chosenCandidates.add(chosenCandidate);
      double value = getHeuristicValue(chosenCandidate);

      while (it.hasNext()) {
        final Candidate nextCan = it.next();
        final double newProportion = getHeuristicValue(nextCan);
        if (getMaxOrMin(value, newProportion)) {
          chosenCandidates = new ArrayList<Candidate>();
          value = newProportion;
          chosenCandidates.add(nextCan);
        } else if (newProportion == value) {
          chosenCandidates.add(nextCan);
        }
      }
      return chosenCandidates;

    }
  }


  private abstract class MinSelectingHeuristic extends SelectingHeuristic
  {
    protected boolean getMaxOrMin(final double currentMaxMin,
                                  final double newValue)
    {
      if (currentMaxMin > newValue) {
        return true;
      }
      return false;
    }
  }


  private abstract class MaxSelectingHeuristic extends SelectingHeuristic
  {
    protected boolean getMaxOrMin(final double currentMaxMin,
                                  final double newValue)
    {
      if (currentMaxMin < newValue) {
        return true;
      }
      return false;
    }
  }


  /**
   * Performs step 2 of the approach to select the automata to compose. The
   * chosen candidate is the one with the lowest proportion of alpha-marked
   * states.
   *
   * @author rmf18
   */
  private class HeuristicMinA extends MinSelectingHeuristic
  {

    protected double getHeuristicValue(final Candidate candidate)
    {
      // TODO
      return 0;
    }

  }


  /**
   * Performs step 2 of the approach to select the automata to compose. The
   * chosen candidate is the one with the highest proportion of local events.
   */
  private class HeuristicMaxLt extends MaxSelectingHeuristic
  {
    protected double getHeuristicValue(final Candidate candidate)
    {
      return (double) candidate.getLocalEventCount()
          / (double) candidate.getNumberOfEvents();
    }
  }


  /**
   * Performs step 2 of the approach to select the automata to compose. The
   * chosen candidate is the one with the highest proportion of local events
   * excluding tau from calculations.
   */
  private class HeuristicMaxL extends MaxSelectingHeuristic
  {
    protected double getHeuristicValue(final Candidate candidate)
    {
      final double localEvents = countCandidatesLocalEvents(candidate);
      final double totalEvents = countCandidatesTotalEvents(candidate);
      return localEvents / totalEvents;
    }
  }


  private abstract class TransitionHeuristic extends MaxSelectingHeuristic
  {
    private int mLocalTransitions;
    private int mTotalTransitions;

    protected int getLocalTransitionCount()
    {
      return mLocalTransitions;
    }

    protected int getTotalTransitionCount()
    {
      return mTotalTransitions;
    }

    protected void countTransitions(final Candidate candidate)
    {
      final List<AutomatonProxy> automata = candidate.getAutomata();
      final Set<EventProxy> localEvents = candidate.getLocalEvents();
      mLocalTransitions = 0;
      mTotalTransitions = 0;
      for (final AutomatonProxy aut : automata) {
        final Collection<TransitionProxy> transitions = aut.getTransitions();
        mTotalTransitions = transitions.size();
        for (final TransitionProxy transition : transitions) {
          if (localEvents.contains(transition.getEvent())) {
            mLocalTransitions++;
          }
        }
      }
    }
  }


  /**
   * Performs step 2 of the approach to select the automata to compose. The
   * chosen candidate is the one with the highest proportion of transitions
   * which are labelled by a local event.
   */
  private class HeuristicMaxLOnTransitions extends TransitionHeuristic
  {

    protected double getHeuristicValue(final Candidate candidate)
    {
      countTransitions(candidate);
      return (double) getLocalTransitionCount()
          / (double) getTotalTransitionCount();
    }
  }


  /**
   * Performs step 2 of the approach to select the automata to compose. The
   * chosen candidate is the one with the highest proportion of events which are
   * shared between that candidates automata.
   */
  private class HeuristicMaxCt extends MaxSelectingHeuristic
  {

    protected double getHeuristicValue(final Candidate candidate)
    {
      final int candidatesTotalEvents = candidate.getNumberOfEvents();
      return (double) candidate.getCommonEventCount()
          / (double) candidatesTotalEvents;
    }
  }


  /**
   * Performs step 2 of the approach to select the automata to compose. The
   * chosen candidate is the one with the highest proportion of events which are
   * shared between that candidates automata excluding tau.
   */
  private class HeuristicMaxC extends MaxSelectingHeuristic
  {

    protected double getHeuristicValue(final Candidate candidate)
    {
      final double candidatesTotalEvents =
          countCandidatesTotalEvents(candidate);
      final double sharedEvents = countCandidatesSharedEvents(candidate);
      return sharedEvents / candidatesTotalEvents;
    }
  }


  /**
   * Performs step 2 of the approach to select the automata to compose. The
   * chosen candidate is the one with the highest proportion of transitions
   * which are shared events.
   */
  private class HeuristicMaxCOnTransitions extends TransitionHeuristic
  {

    protected double getHeuristicValue(final Candidate candidate)
    {
      countTransitions(candidate);
      return (double) (getTotalTransitionCount() - getLocalTransitionCount())
          / (double) getTotalTransitionCount();
    }
  }


  private class HeuristicMinS extends MinSelectingHeuristic
  {
    /**
     * Predicts number of states for synchronous product with respect to the
     * number of local events.
     */
    protected double getHeuristicValue(final Candidate candidate)
    {
      double product = 1;
      for (final AutomatonProxy aut : candidate.getAutomata()) {
        product *= aut.getStates().size();
      }
      final int totalEvents = candidate.getNumberOfEvents();
      final int nonLocalEvents = totalEvents - candidate.getLocalEventCount();
      return product * (double) nonLocalEvents / (double) totalEvents;
    }
  }


  private class HeuristicMinSCommon extends HeuristicMinS
  {
    /**
     * Predicts number of states for synchronous product with respect to the
     * number of events shared within the automata of the candidate.
     */
    protected double getHeuristicValue(final Candidate candidate)
    {
      double product = 1;
      for (final AutomatonProxy aut : candidate.getAutomata()) {
        product *= aut.getStates().size();
      }
      final int totalEvents = candidate.getNumberOfEvents();
      final int commonEvents = candidate.getCommonEventCount();
      return product * (double) commonEvents / (double) totalEvents;
    }
  }


  /**
   * This heuristic is provided for when the others fail to find one unique
   * candidate. The selection is made by comparing the candidates automata names
   * alphabetically.
   */
  private class HeuristicDefault extends SelectingHeuristic
  {
    @Override
    public List<Candidate> evaluate(final List<Candidate> candidates)
    {
      ListIterator<Candidate> iter = candidates.listIterator();
      List<Candidate> chosenCandidates = new ArrayList<Candidate>();
      Candidate chosen = iter.next();
      chosenCandidates.add(chosen);
      String chosenAutName = chosen.getAutomata().get(0).getName();
      boolean found = false;
      int index = 0;
      while (!found) {
        while (iter.hasNext()) {
          final Candidate nextCandidate = iter.next();
          // currently if two candidates have the same automaton names up
          // until a point where one has run out of automata, the candidate with
          // more automata is selected
          // TODO Consider using Candidate.compareTo(). Not exactly the same
          // ordering, though ...
          if (index < nextCandidate.getAutomata().size()) {
            final String nextAutName =
                nextCandidate.getAutomata().get(index).getName();
            if (chosenAutName.compareTo(nextAutName) > 0) {
              chosenAutName = nextAutName;
              chosen = nextCandidate;
              chosenCandidates = new ArrayList<Candidate>();
              chosenCandidates.add(chosen);
            } else if (chosenAutName.compareTo(nextAutName) == 0) {
              chosenCandidates.add(nextCandidate);
            }
          }
        }
        if (chosenCandidates.size() == 1) {
          found = true;
          break;
        } else {
          iter = candidates.listIterator(0);
          chosenCandidates = new ArrayList<Candidate>();
          chosen = iter.next();
          chosenCandidates.add(chosen);
          chosenAutName = chosen.getAutomata().get(0).getName();
        }
        index++;
      }
      return chosenCandidates;
    }

    // not used
    protected double getHeuristicValue(final Candidate candidate)
    {
      return 0;
    }

    // not used
    protected boolean getMaxOrMin(final double currentMaxMin,
                                  final double newValue)
    {
      return false;
    }
  }

  public ObservationEquivalenceStep createObservationEquivalenceStep(
                                                                     final AutomatonProxy abstractedAut,
                                                                     final AutomatonProxy autToAbstract,
                                                                     final EventProxy tau,
                                                                     final StateEncoding inputEnc,
                                                                     final List<int[]> partition,
                                                                     final StateEncoding outputEnc)
  {
    return new ObservationEquivalenceStep(abstractedAut, autToAbstract, tau,
        inputEnc, partition, outputEnc);
  }

  public DeterminisationOfNonAlphaStatesStep createDeterminisationOfNonAlphaStatesStep(
                                                                                       final AutomatonProxy abstractedAut,
                                                                                       final AutomatonProxy autToAbstract,
                                                                                       final EventProxy tau,
                                                                                       final StateEncoding inputEnc,
                                                                                       final List<int[]> partition,
                                                                                       final StateEncoding outputEnc)
  {
    return new DeterminisationOfNonAlphaStatesStep(abstractedAut,
        autToAbstract, tau, inputEnc, partition, outputEnc);
  }

  public RemovalOfMarkingsOrNoncoreachableStatesStep createRemovalOfMarkingsStep(
                                                                                 final AutomatonProxy abstractedAut,
                                                                                 final AutomatonProxy autToAbstract,
                                                                                 final StateProxy[] originalStates,
                                                                                 final TObjectIntHashMap<StateProxy> resultingStates)
  {
    return new RemovalOfMarkingsOrNoncoreachableStatesStep(abstractedAut,
        autToAbstract, originalStates, resultingStates);
  }

  public RemovalOfTauTransitionsStep createRemovalOfTauTransitionsStep(
                                                                       final AutomatonProxy abstractedAut,
                                                                       final AutomatonProxy autToAbstract,
                                                                       final EventProxy tau,
                                                                       final ObserverProjectionTransitionRelation tr)
  {
    return new RemovalOfTauTransitionsStep(abstractedAut, autToAbstract, tau,
        tr);
  }


  // #########################################################################
  // # Inner Class Step
  abstract class Step
  {

    // #######################################################################
    // # Constructor
    Step(final AutomatonProxy aut, final Collection<AutomatonProxy> originals)
    {
      mResultAutomaton = aut;
      mOriginalAutomata = originals;
    }

    Step(final AutomatonProxy resultAut, final AutomatonProxy originalAut)
    {
      this(resultAut, Collections.singletonList(originalAut));
    }

    // #######################################################################
    // # Simple Access
    AutomatonProxy getResultAutomaton()
    {
      return mResultAutomaton;
    }

    Collection<AutomatonProxy> getOriginalAutomata()
    {
      return mOriginalAutomata;
    }

    AutomatonProxy getOriginalAutomaton()
    {
      if (mOriginalAutomata.size() == 1) {
        return mOriginalAutomata.iterator().next();
      } else {
        throw new IllegalStateException(
            "Attempting to get a single input automaton from "
                + ProxyTools.getShortClassName(this) + " with "
                + mOriginalAutomata.size() + " input automata!");
      }
    }

    // #######################################################################
    ConflictTraceProxy createConvertedTrace(
                                            final List<AutomatonProxy> traceAutomata,
                                            final List<TraceStepProxy> convertedSteps)
    {
      final String traceName = getModel().getName() + "-conflicting";
      final ConflictTraceProxy convertedTrace =
          getFactory().createConflictTraceProxy(traceName, null, null,
                                                getModel(), traceAutomata,
                                                convertedSteps,
                                                ConflictKind.CONFLICT);
      return convertedTrace;
    }

    // #######################################################################
    // # Trace Computation
    /**
     * Assumes that a saturated trace is being passed.
     */
    abstract ConflictTraceProxy convertTrace(
                                             final ConflictTraceProxy counterexample);

    // #######################################################################
    // # Data Members
    private final AutomatonProxy mResultAutomaton;
    private final Collection<AutomatonProxy> mOriginalAutomata;

  }


  // #########################################################################
  // # Inner Class CompositionStep
  private class CompositionStep extends Step
  {

    // #######################################################################
    // # Constructor
    private CompositionStep(final AutomatonProxy composedAut,
                            final SynchronousProductStateMap stateMap)
    {
      super(composedAut, stateMap.getInputAutomata());
      mStateMap = stateMap;
    }

    // #######################################################################
    // # Trace Computation
    ConflictTraceProxy convertTrace(final ConflictTraceProxy conflictTrace)
    {
      final AutomatonProxy composed = getResultAutomaton();
      final Collection<AutomatonProxy> autOfComposition =
          mStateMap.getInputAutomata();

      final List<AutomatonProxy> traceAutomata =
          new ArrayList<AutomatonProxy>(conflictTrace.getAutomata().size() - 1);
      for (final AutomatonProxy aut : conflictTrace.getAutomata()) {
        if (aut != getResultAutomaton()) {
          traceAutomata.add(aut);
        }
      }
      for (final AutomatonProxy aut : autOfComposition) {
        traceAutomata.add(aut);
      }

      final List<TraceStepProxy> convertedSteps =
          new ArrayList<TraceStepProxy>();
      final List<TraceStepProxy> traceSteps = conflictTrace.getTraceSteps();
      for (final TraceStepProxy step : traceSteps) {
        final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
        if (stepMap.containsKey(composed)) {
          final Map<AutomatonProxy,StateProxy> convertedStepMap =
              new HashMap<AutomatonProxy,StateProxy>(stepMap);
          convertedStepMap.remove(composed);
          final StateProxy convertedState = stepMap.get(composed);
          // add original automata and states
          for (final AutomatonProxy aut : autOfComposition) {
            final StateProxy originalState =
                mStateMap.getOriginalState(convertedState, aut);
            convertedStepMap.put(aut, originalState);
          }
          final TraceStepProxy convertedStep =
              getFactory().createTraceStepProxy(step.getEvent(),
                                                convertedStepMap);
          convertedSteps.add(convertedStep);
        } else {
          convertedSteps.add(step);
        }
      }
      return createConvertedTrace(traceAutomata, convertedSteps);
    }

    // #######################################################################
    // # Data Members
    private final SynchronousProductStateMap mStateMap;
  }


  // #########################################################################
  // # Inner Class HidingStep
  private class HidingStep extends Step
  {

    // #######################################################################
    // # Constructor
    private HidingStep(final AutomatonProxy result,
                       final AutomatonProxy originalAut,
                       final Set<EventProxy> localEvents, final EventProxy tau)
    {
      super(result, originalAut);
      mTau = tau;
      mLocalEvents = localEvents;
    }

    // #######################################################################
    // # Trace Computation
    ConflictTraceProxy convertTrace(final ConflictTraceProxy conflictTrace)
    {
      final List<TraceStepProxy> convertedSteps =
          new ArrayList<TraceStepProxy>();
      final List<TraceStepProxy> traceSteps = conflictTrace.getTraceSteps();
      StateProxy sourceState =
          getInitialState(getResultAutomaton(), traceSteps.get(0));
      for (final TraceStepProxy step : traceSteps) {
        // replaces automaton in step's step map
        final Map<AutomatonProxy,StateProxy> stepsNewStateMap =
            new HashMap<AutomatonProxy,StateProxy>(step.getStateMap());
        if (stepsNewStateMap.containsKey(getResultAutomaton())) {
          stepsNewStateMap.put(getOriginalAutomaton(), stepsNewStateMap
              .get(getResultAutomaton()));
        }
        // replaces tau events with original event before hiding
        final EventProxy stepEvent = step.getEvent();
        if (stepEvent != null) {
          final StateProxy targetState =
              stepsNewStateMap.get(getResultAutomaton());
          assert targetState != null;
          stepsNewStateMap.remove(getResultAutomaton());
          TraceStepProxy convertedStep;
          if (stepEvent == mTau) {
            final EventProxy originalEvent =
                findOriginalEvent(sourceState, targetState);
            convertedStep =
                getFactory().createTraceStepProxy(originalEvent,
                                                  stepsNewStateMap);
          } else {
            convertedStep =
                getFactory().createTraceStepProxy(stepEvent, stepsNewStateMap);
          }
          convertedSteps.add(convertedStep);
          sourceState = targetState;
        } else {
          stepsNewStateMap.remove(getResultAutomaton());
          final TraceStepProxy convertedStep =
              getFactory().createTraceStepProxy(stepEvent, stepsNewStateMap);
          convertedSteps.add(convertedStep);
        }
      }
      final List<AutomatonProxy> traceAutomata =
          new ArrayList<AutomatonProxy>(conflictTrace.getAutomata().size());
      for (final AutomatonProxy aut : conflictTrace.getAutomata()) {
        if (aut != getResultAutomaton()) {
          traceAutomata.add(aut);
        }
      }
      traceAutomata.add(getOriginalAutomaton());
      return createConvertedTrace(traceAutomata, convertedSteps);

    }

    /**
     * Finds the event which was in the original automaton before hiding was
     * used and the event was replaced with tau.
     */
    private EventProxy findOriginalEvent(final StateProxy source,
                                         final StateProxy target)
    {
      EventProxy transitionEvent = null;
      for (final TransitionProxy transition : getOriginalAutomaton()
          .getTransitions()) {
        transitionEvent = transition.getEvent();
        if (transition.getTarget() == target
            && transition.getSource() == source
            && mLocalEvents.contains(transitionEvent)) {
          break;
        }
      }
      return transitionEvent;
    }

    // #######################################################################
    // # Data Members
    private final EventProxy mTau;
    private final Set<EventProxy> mLocalEvents;
  }


  // #########################################################################
  // # Inner Class RemovalOfTransitionsStep
  abstract class RemovalOfTransitionsStep extends Step
  {
    RemovalOfTransitionsStep(final AutomatonProxy resultAut,
                             final AutomatonProxy originalAut,
                             final EventProxy tau,
                             final StateEncoding inputEnc,
                             final StateEncoding outputEnc)
    {
      super(resultAut, originalAut);
      mOriginalStates = inputEnc.getStatesArray();
      mTau = tau;
      mReverseOutputStateMap = outputEnc.getStateCodeMap();
      mTransitionRelation = null;
      mOriginalStatesMap = null;
      mCodeOfTau = -1;
    }

    RemovalOfTransitionsStep(final AutomatonProxy resultAut,
                             final AutomatonProxy originalAut,
                             final EventProxy tau,
                             final ObserverProjectionTransitionRelation tr)

    {
      super(resultAut, originalAut);
      mOriginalStates = tr.getOriginalIntToStateMap();
      mTau = tau;

      mReverseOutputStateMap = tr.getResultingStateToIntMap();
      mTransitionRelation = null;
      mOriginalStatesMap = null;
      mCodeOfTau = -1;
    }

    // #######################################################################
    // # Simple Access
    int getTauCode()
    {
      return mCodeOfTau;
    }

    int getAlphaCode()
    {
      return mCodeOfAlpha;
    }

    StateProxy[] getOriginalStates()
    {
      return mOriginalStates;
    }

    Map<StateProxy,Integer> getOriginalStateToIntMap()
    {
      return mOriginalStatesMap;
    }

    TObjectIntHashMap<StateProxy> getReverseOutputStateMap()
    {
      return mReverseOutputStateMap;
    }

    ObserverProjectionTransitionRelation getTransitionRelation()
    {
      return mTransitionRelation;
    }

    void createTransitionRelation()
    {
      mTransitionRelation =
          new ObserverProjectionTransitionRelation(getOriginalAutomaton(),
              mPropositions);
      mCodeOfTau = mTransitionRelation.getEventInt(mTau);
      final EventProxy alpha = getUsedPreconditionMarkingProposition();
      mCodeOfAlpha = mTransitionRelation.getEventInt(alpha);
      mOriginalStatesMap = mTransitionRelation.getOriginalStateToIntMap();
    }

    void clearTransitionRelation()
    {
      mTransitionRelation = null;
      mOriginalStatesMap = null;
    }

    int[] createClassMap(final Collection<int[]> partition)
    {
      final AutomatonProxy aut = getOriginalAutomaton();
      final int size = aut.getStates().size();
      final int[] result = new int[size];
      if (partition == null) {
        for (int s = 0; s < size; s++) {
          result[s] = s;
        }
      } else {
        int c = 0;
        for (final int[] clazz : partition) {
          for (final int s : clazz) {
            result[s] = c;
          }
          c++;
        }
      }
      return result;
    }

    // #######################################################################
    /**
     * This performs a forward search over trace steps to convert a given trace.
     */
    ConflictTraceProxy convertTrace(final ConflictTraceProxy conflictTrace)
    {
      createTransitionRelation();
      final List<TraceStepProxy> convertedSteps =
          new ArrayList<TraceStepProxy>();
      final List<TraceStepProxy> traceSteps = conflictTrace.getTraceSteps();

      // makes the trace begin in the correct initial state
      StateProxy originalAutSource = beginTrace(traceSteps, convertedSteps);

      Map<AutomatonProxy,StateProxy> stepsPrevStateMap =
          new HashMap<AutomatonProxy,StateProxy>(traceSteps.get(0)
              .getStateMap());
      stepsPrevStateMap.remove(getResultAutomaton());
      for (final TraceStepProxy step : traceSteps) {
        originalAutSource =
            expandStep(step, originalAutSource, stepsPrevStateMap,
                       convertedSteps);
        stepsPrevStateMap =
            new HashMap<AutomatonProxy,StateProxy>(step.getStateMap());
        stepsPrevStateMap.remove(getResultAutomaton());
      }
      // makes the trace end in the correct state
      endTrace(conflictTrace, originalAutSource, convertedSteps);
      clearTransitionRelation();
      return buildTrace(conflictTrace, convertedSteps);
    }

    /**
     * Finds any extra required steps between the given step and its successor
     * step. Converts all step information to be valid for the original
     * automaton.
     *
     * @param traceSteps
     *          The trace step of the given conflict trace to be converted.
     * @param originalAutSource
     *          The state of the original automaton to search from.
     * @param stepsPrevStateMap
     *          The state map of the step in the trace preceding this one.
     * @param convertedSteps
     *          The list of converted steps.
     * @return The state in the original automaton to search from in the next
     *         step.
     */
    protected StateProxy expandStep(
                                    final TraceStepProxy traceStep,
                                    StateProxy originalAutSource,
                                    final Map<AutomatonProxy,StateProxy> stepsPrevStateMap,
                                    final List<TraceStepProxy> convertedSteps)
    {
      final Map<AutomatonProxy,StateProxy> stepsNewStateMap =
          new HashMap<AutomatonProxy,StateProxy>(traceStep.getStateMap());

      final EventProxy stepEvent = traceStep.getEvent();
      if (stepEvent != null) {
        if (getResultAutomaton().getEvents().contains(stepEvent)
            || getOriginalAutomaton().getEvents().contains(stepEvent)) {
          final int eventID = mTransitionRelation.getEventInt(stepEvent);

          final StateProxy resultTargetState =
              stepsNewStateMap.get(getResultAutomaton());
          assert resultTargetState != null;
          stepsNewStateMap.remove(getResultAutomaton());
          final List<SearchRecord> subtrace =
              findSubTrace(mOriginalStatesMap.get(originalAutSource), eventID,
                           mReverseOutputStateMap.get(resultTargetState));
          final List<TraceStepProxy> substeps =
              createTraceSteps(stepsPrevStateMap, stepsNewStateMap, subtrace,
                               stepEvent);
          convertedSteps.addAll(substeps);
          final int subsize = subtrace.size();
          if (subsize > 0) {
            final int originalTargetID = subtrace.get(subsize - 1).getState();
            originalAutSource = mOriginalStates[originalTargetID];
          }
        } else {
          stepsNewStateMap.remove(getResultAutomaton());
          stepsNewStateMap.put(getOriginalAutomaton(), originalAutSource);
          final TraceStepProxy convertedStep =
              getFactory().createTraceStepProxy(stepEvent, stepsNewStateMap);
          convertedSteps.add(convertedStep);
        }
      }
      return originalAutSource;
    }

    /**
     * Uses the state in the original automaton which is found to be in the last
     * step of the trace, calls {@link #completeEndOfTrace(int)
     * completeEndOfTrace()} to find any steps needed to reach the actual end
     * state of the trace according to the subclasses requirements. Steps are
     * then created for these final steps.
     */
    protected void endTrace(final ConflictTraceProxy conflictTrace,
                            final StateProxy originalAutSource,
                            final List<TraceStepProxy> convertedSteps)
    {
      final List<TraceStepProxy> traceSteps = conflictTrace.getTraceSteps();
      final List<SearchRecord> finalSteps =
          completeEndOfTrace(mOriginalStatesMap.get(originalAutSource));
      if (finalSteps != null && finalSteps.size() > 0) {
        final Map<AutomatonProxy,StateProxy> finalStepsStateMap =
            new HashMap<AutomatonProxy,StateProxy>(traceSteps
                .get(traceSteps.size() - 1).getStateMap());
        finalStepsStateMap.remove(getResultAutomaton());
        final List<TraceStepProxy> substeps =
            createTraceSteps(finalStepsStateMap, finalStepsStateMap,
                             finalSteps, null);
        convertedSteps.addAll(substeps);
      }
    }

    /**
     * Creates the list of automata that are part of the trace and the finished
     * trace is created.
     *
     * @return Converted conflict trace.
     */
    protected ConflictTraceProxy buildTrace(
                                            final ConflictTraceProxy conflictTrace,
                                            final List<TraceStepProxy> convertedSteps)
    {
      final List<AutomatonProxy> traceAutomata =
          new ArrayList<AutomatonProxy>(conflictTrace.getAutomata().size());
      for (final AutomatonProxy aut : conflictTrace.getAutomata()) {
        if (aut != getResultAutomaton()) {
          traceAutomata.add(aut);
        }
      }
      traceAutomata.add(getOriginalAutomaton());
      return createConvertedTrace(traceAutomata, convertedSteps);
    }

    /**
     * Gets the first step of the trace, calls completeStartOfTrace to find the
     * steps to the initial state in the trace according to the subclasses
     * requirements. Steps are then created for these intermediate steps and the
     * state is returned for the initial state of the original automaton.
     *
     * @param traceSteps
     *          The steps of the trace to be converted.
     * @param convertedSteps
     *          The list which holds the converted steps.
     * @return The initial state to start a forward search from in the original
     *         automaton.
     */
    protected StateProxy beginTrace(final List<TraceStepProxy> traceSteps,
                                    final List<TraceStepProxy> convertedSteps)
    {
      final TIntArrayList initialStates =
          mTransitionRelation.getAllInitialStates();
      final StateProxy tracesInitialState =
          getInitialState(getResultAutomaton(), traceSteps.get(0));
      final List<SearchRecord> initialRecords =
          completeStartOfTrace(initialStates, mReverseOutputStateMap
              .get(tracesInitialState));
      assert initialRecords.size() > 0;
      final Map<AutomatonProxy,StateProxy> initialStepsStateMap =
          new HashMap<AutomatonProxy,StateProxy>(traceSteps.get(0)
              .getStateMap());
      initialStepsStateMap.remove(getResultAutomaton());
      final List<TraceStepProxy> initialSteps =
          createTraceSteps(initialStepsStateMap, initialStepsStateMap,
                           initialRecords, null);
      convertedSteps.addAll(initialSteps);
      final int originalInitialStateID =
          initialRecords.get(initialRecords.size() - 1).getState();
      final StateProxy originalInitialState =
          mOriginalStates[originalInitialStateID];
      return originalInitialState;
    }

    protected List<SearchRecord> buildSearchRecordTrace(SearchRecord record)
    {
      final List<SearchRecord> trace = new LinkedList<SearchRecord>();
      do {
        trace.add(0, record);
        record = record.getPredecessor();
      } while (record.getPredecessor() != null);
      return trace;
    }

    /**
     * Finds a partial trace in the original automaton. This method computes a
     * sequence of tau transitions, followed by a transition with the given
     * event, followed by another sequence of tau transitions linking the source
     * state to some state in the class of the target state in the simplified
     * automaton.
     *
     * @param originalSource
     *          State number of the source state in the original automaton.
     * @param event
     *          Integer code of the event to be included in the trace.
     * @param targetClass
     *          State number of the state in the simplified automaton (code of
     *          state class).
     * @return List of search records describing the trace from source to
     *         target. The first entry in the list represents the first step
     *         after the source state, with its event and target state. The
     *         final step has a target state in the given target class. Events
     *         in the list can only be tau or the given event.
     */
    protected List<SearchRecord> findSubTrace(final int originalSource,
                                              final int event,
                                              final int resultAutTarget)
    {

      final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
      final TIntHashSet visited0 = new TIntHashSet(); // event not in trace
      final TIntHashSet visited1 = new TIntHashSet(); // event in trace
      // The given event may be tau. In this case, we must search for a
      // (possibly empty) string of tau events. This is achieved here by
      // by creating a first search record with the 'hasevent' property,
      // i.e., pretending the trace already has an event.
      SearchRecord record;
      final int tau = getTauCode();
      if (event != tau) {
        record = new SearchRecord(originalSource);
        visited0.add(originalSource);
      } else if (!isTargetState(originalSource, resultAutTarget)) {
        record = new SearchRecord(originalSource, true, -1, null);
        visited1.add(originalSource);
      } else {
        return Collections.emptyList();
      }
      open.add(record);
      while (true) {
        final SearchRecord current = open.remove();
        final int source = current.getState();
        final boolean hasEvent = current.hasProperEvent();
        final TIntHashSet visited = hasEvent ? visited1 : visited0;
        if (tau >= 0) {
          final TIntHashSet successors =
              getSuccessorsOrPredecessors(source, tau);
          if (successors != null) {
            final TIntIterator iter = successors.iterator();
            while (iter.hasNext()) {
              final int target = iter.next();
              if (!visited.contains(target)) {
                record =
                    new SearchRecord(target, hasEvent, getTauCode(), current);
                if (hasEvent && isTargetState(target, resultAutTarget)) {
                  return buildSearchRecordTrace(record);
                }
                open.add(record);
                visited.add(target);
              }
            }
          }
        }
        if (!hasEvent) {
          final TIntHashSet successors =
              getSuccessorsOrPredecessors(source, event);
          if (successors != null) {
            final TIntIterator iter = successors.iterator();
            while (iter.hasNext()) {
              final int target = iter.next();
              if (!visited1.contains(target)) {
                record = new SearchRecord(target, true, event, current);
                if (isTargetState(target, resultAutTarget)) {
                  return buildSearchRecordTrace(record);
                }
                open.add(record);
                visited1.add(target);
              }
            }
          }
        }
      }
    }

    /**
     * Completes a trace by adding steps for tau transitions (if necessary)
     * until the end state of the trace has the alpha marking.
     *
     * @param originalSource
     *          The original end state, which may or may not have the alpha
     *          marking proposition.
     * @return A list of SearchRecord's that represent each extra step added of
     *         the trace. (The last item being the end state of the trace).
     */
    protected List<SearchRecord> completeEndOfTrace(final int originalSource)
    {
      final EventProxy alpha = getUsedPreconditionMarkingProposition();
      if (!getOriginalAutomaton().getEvents().contains(alpha)
          || isTraceEndState(originalSource)) {
        return Collections.emptyList();
      }
      final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
      final TIntHashSet visited = new TIntHashSet();
      SearchRecord record = new SearchRecord(originalSource);
      open.add(record);
      visited.add(originalSource);
      while (true) {
        final SearchRecord current = open.remove();
        final int source = current.getState();
        final TIntHashSet successors =
            getSuccessorsOrPredecessors(source, getTauCode());
        if (successors != null) {
          final TIntIterator iter = successors.iterator();
          while (iter.hasNext()) {
            final int target = iter.next();
            if (!visited.contains(target)) {
              record = new SearchRecord(target, false, getTauCode(), current);
              if (isTraceEndState(target)) {
                return buildSearchRecordTrace(record);
              }
              open.add(record);
              visited.add(target);
            }
          }
        }
      }
    }

    // #######################################################################
    // # Trace Computation
    protected abstract TIntHashSet getSuccessorsOrPredecessors(int stateID,
                                                               int event);

    protected abstract boolean isTargetState(int stateFound, int resultAutTarget);

    /**
     * Checks whether the given state is a valid end state of the trace being
     * constructed.
     *
     * @param stateFound
     *          State ID in the original automaton.
     */
    protected abstract boolean isTraceEndState(int stateFound);

    protected abstract List<SearchRecord> completeStartOfTrace(
                                                               final TIntArrayList originalInitialStateIDs,
                                                               final int resultAutInitialState);

    // #######################################################################
    // # Auxiliary Methods
    /**
     * Given a list of {@link SearchRecord} objects a list of
     * {@link TraceStepProxy} objects is created and returned. A TraceStepProxy
     * is created for each SearchRecord.
     *
     * @param stepsNewStateMap
     *          The state map for the step before adding the new information.
     * @param subtrace
     *          The list of search records to convert into steps of a trace.
     * @return A list of steps for a trace.
     */
    private List<TraceStepProxy> createTraceSteps(
                                                  final Map<AutomatonProxy,StateProxy> stepsPrevStateMap,
                                                  final Map<AutomatonProxy,StateProxy> stepsNewStateMap,
                                                  final List<SearchRecord> subtrace,
                                                  final EventProxy stepEvent)
    {
      Map<AutomatonProxy,StateProxy> stepStateMap = null;
      boolean eventFound = false;
      final ProductDESProxyFactory factory = getFactory();
      final List<TraceStepProxy> substeps = new LinkedList<TraceStepProxy>();
      for (final SearchRecord subStep : subtrace) {
        final int subStepTargetStateID = subStep.getState();
        final int subStepEventID = subStep.getEvent();
        final EventProxy event =
            subStepEventID >= 0 ? mTransitionRelation.getEvent(subStepEventID)
                : null;
        if (event != stepEvent && !eventFound) {
          stepStateMap = stepsPrevStateMap;
        } else if (event != stepEvent && eventFound) {
          stepStateMap = stepsNewStateMap;
        } else if (event == stepEvent) {
          eventFound = true;
          stepStateMap = stepsNewStateMap;
        }
        stepStateMap.put(getOriginalAutomaton(),
                         mOriginalStates[subStepTargetStateID]);
        final TraceStepProxy convertedStep =
            factory.createTraceStepProxy(event, stepStateMap);
        substeps.add(convertedStep);
      }
      return substeps;
    }

    // #######################################################################
    // # Data Members
    /**
     * Array of original states. Maps state codes in the input
     * TransitionRelation to state objects in the input automaton. Obtained from
     * TransitionRelation.
     */
    private final StateProxy[] mOriginalStates;
    /**
     * Reverse encoding of output states. Maps states in output automaton
     * (simplified automaton) to state code in output transition relation.
     * Obtained from TransitionRelation.
     */
    private final TObjectIntHashMap<StateProxy> mReverseOutputStateMap;

    private ObserverProjectionTransitionRelation mTransitionRelation;
    private int mCodeOfTau;
    private int mCodeOfAlpha;
    private final EventProxy mTau;
    private Map<StateProxy,Integer> mOriginalStatesMap;
  }


  // #########################################################################
  // # Inner Class ObservationEquivalenceStep
  private class ObservationEquivalenceStep extends RemovalOfTransitionsStep
  {

    // #########################################################################
    // # Constructor
    private ObservationEquivalenceStep(final AutomatonProxy resultAut,
                                       final AutomatonProxy originalAut,
                                       final EventProxy tau,
                                       final StateEncoding inputEnc,
                                       final List<int[]> partition,
                                       final StateEncoding outputEnc)
    {
      super(resultAut, originalAut, tau, inputEnc, outputEnc);
      mClassMap = createClassMap(partition);
    }

    // #########################################################################
    // # Overrides for RemovalOfTransitionsStep
    /**
     * Creates the beginning of a trace by doing a breadth-first search to find
     * the correct initial state of the original automaton. Steps are added for
     * tau transitions (if necessary) until the initial state of the result
     * automaton is reached.
     *
     * @return A list of SearchRecords that represent each extra step needed for
     *         the start of the trace. (The first item being the very first
     *         state of the trace).
     */
    protected List<SearchRecord> completeStartOfTrace(
                                                      final TIntArrayList initialStateIDs,
                                                      final int resultAutInitialStateClass)
    {
      final TIntHashSet targetSet = new TIntHashSet();
      for (int s = 0; s < mClassMap.length; s++) {
        if (mClassMap[s] == resultAutInitialStateClass) {
          targetSet.add(s);
        }
      }
      final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
      final TIntHashSet visited = new TIntHashSet();
      // The dummy record ensures that the first real search record will
      // later be included in the trace.
      final SearchRecord dummy = new SearchRecord(-1);
      final int numInit = initialStateIDs.size();
      for (int i = 0; i < numInit; i++) {
        final int initStateID = initialStateIDs.get(i);
        final SearchRecord record =
            new SearchRecord(initStateID, false, -1, dummy);
        if (targetSet.contains(initStateID)) {
          return Collections.singletonList(record);
        }
        open.add(record);
        visited.add(initStateID);
      }
      while (true) {
        final SearchRecord current = open.remove();
        final int source = current.getState();
        final TIntHashSet successors =
            getTransitionRelation().getSuccessors(source, getTauCode());
        if (successors != null) {
          final TIntIterator iter = successors.iterator();
          while (iter.hasNext()) {
            final int target = iter.next();
            if (!visited.contains(target)) {
              final SearchRecord record =
                  new SearchRecord(target, false, getTauCode(), current);
              if (targetSet.contains(target)) {
                return buildSearchRecordTrace(record);
              }
              open.add(record);
              visited.add(target);
            }
          }
        }
      }
    }

    protected TIntHashSet getSuccessorsOrPredecessors(final int source,
                                                      final int event)
    {
      return getTransitionRelation().getSuccessors(source, event);
    }

    protected boolean isTargetState(final int stateFound,
                                    final int resultAutTarget)
    {
      return mClassMap[stateFound] == resultAutTarget;
    }

    protected boolean isTraceEndState(final int stateFound)
    {
      final ObserverProjectionTransitionRelation rel = getTransitionRelation();
      final int alpha = getAlphaCode();
      return rel.isMarked(stateFound, alpha);
    }

    // #######################################################################
    // # Data Members
    /**
     * Maps state codes of the input transition relation to state codes (i.e.,
     * class numbers) in the output transition relation. Obtained from
     * observation equivalence minimiser.
     */
    private final int[] mClassMap;

  }


  // #########################################################################
  // # Inner Class DeterminisationOfNonAlphaStatesStep
  private class DeterminisationOfNonAlphaStatesStep extends
      RemovalOfTransitionsStep
  {
    DeterminisationOfNonAlphaStatesStep(final AutomatonProxy resultAut,
                                        final AutomatonProxy originalAut,
                                        final EventProxy tau,
                                        final StateEncoding inputEnc,
                                        final List<int[]> partition,
                                        final StateEncoding outputEnc)
    {
      super(resultAut, originalAut, tau, inputEnc, outputEnc);
      mClassMap = createClassMap(partition);
    }

    /**
     * This performs a backward search over trace steps to convert a given
     * trace.
     */
    @Override
    ConflictTraceProxy convertTrace(final ConflictTraceProxy conflictTrace)
    {
      createTransitionRelation();
      final List<TraceStepProxy> traceSteps = conflictTrace.getTraceSteps();
      final int stepCount = traceSteps.size();
      final ListIterator<TraceStepProxy> iter =
          traceSteps.listIterator(stepCount);
      TraceStepProxy step = iter.previous();
      final Map<AutomatonProxy,StateProxy> endMap =
          new HashMap<AutomatonProxy,StateProxy>(step.getStateMap());
      final StateProxy tracesEndState = endMap.remove(getResultAutomaton());
      final int tracesEndStateCode =
          getReverseOutputStateMap().get(tracesEndState);
      int originialEndStateCode = -1;
      for (int s = 0; s < mClassMap.length; s++) {
        if (mClassMap[s] == tracesEndStateCode) {
          // There must be exactly only one state in the class of the trace's
          // end state, because that state has to be marked alpha, and non-alpha
          // determinisation never merges alpha-marked states.
          originialEndStateCode = s;
          break;
        }
      }
      // To start the process of building the trace from the end, we create a
      // step containing the known end state of the trace and a null event. This
      // step will be replaced by a step with event information as soon as a
      // proper step is discovered.
      final StateProxy originalAutState =
          getOriginalStates()[originialEndStateCode];
      endMap.put(getOriginalAutomaton(), originalAutState);
      final ProductDESProxyFactory factory = getFactory();
      final TraceStepProxy newEndStep =
          factory.createTraceStepProxy(null, endMap);
      final List<TraceStepProxy> convertedSteps =
          new LinkedList<TraceStepProxy>();
      convertedSteps.add(newEndStep);
      while (iter.hasPrevious()) {
        final TraceStepProxy pred = iter.previous();
        final Map<AutomatonProxy,StateProxy> stepsPredecessorStateMap =
            pred.getStateMap();
        expandStep(step, stepsPredecessorStateMap, convertedSteps);
        step = pred;
      }
      createInitialSteps(step, convertedSteps);
      final ConflictTraceProxy trace =
          buildTrace(conflictTrace, convertedSteps);
      clearTransitionRelation();
      return trace;
    }

    /**
     * Dummy method. Not needed for non-alpha determinisation.
     */
    protected List<SearchRecord> completeStartOfTrace(
                                                      final TIntArrayList initialStateIDs,
                                                      final int resultAutInitialStateClass)
    {
      return null;
    }

    protected TIntHashSet getSuccessorsOrPredecessors(final int source,
                                                      final int event)
    {
      return getTransitionRelation().getPredecessors(source, event);
    }

    protected boolean isTargetState(final int stateFound,
                                    final int resultAutTarget)
    {
      return mClassMap[stateFound] == resultAutTarget;
    }

    protected boolean isTraceEndState(final int stateFound)
    {
      final ObserverProjectionTransitionRelation rel = getTransitionRelation();
      return rel.isInitial(stateFound);
    }

    // #######################################################################
    // # Auxiliary Methods
    private void expandStep(
                            final TraceStepProxy traceStep,
                            final Map<AutomatonProxy,StateProxy> stepsPrevStateMap,
                            final List<TraceStepProxy> convertedSteps)
    {
      final AutomatonProxy orig = getOriginalAutomaton();
      final TraceStepProxy first = convertedSteps.iterator().next();
      final Map<AutomatonProxy,StateProxy> firstMap = first.getStateMap();
      final StateProxy originalAutState = firstMap.get(orig);
      final Map<AutomatonProxy,StateProxy> newPrevStateMap =
          new HashMap<AutomatonProxy,StateProxy>(stepsPrevStateMap);
      final StateProxy resultTargetState =
          newPrevStateMap.remove(getResultAutomaton());
      assert resultTargetState != null;
      final EventProxy stepEvent = traceStep.getEvent();
      if (stepEvent != null) {
        if (orig.getEvents().contains(stepEvent)) {
          final Map<AutomatonProxy,StateProxy> newStateMap =
              new HashMap<AutomatonProxy,StateProxy>(traceStep.getStateMap());
          final int eventID = getTransitionRelation().getEventInt(stepEvent);
          newStateMap.remove(getResultAutomaton());
          final List<SearchRecord> subtrace =
              findSubTrace(getOriginalStateToIntMap().get(originalAutState),
                           eventID, getReverseOutputStateMap()
                               .get(resultTargetState));
          final List<TraceStepProxy> substeps =
              createTraceSteps(newPrevStateMap, newStateMap, subtrace,
                               stepEvent);
          prependSteps(substeps, convertedSteps);
        } else {
          final ProductDESProxyFactory factory = getFactory();
          final TraceStepProxy oldConvertedStep2 = convertedSteps.remove(0);
          final Map<AutomatonProxy,StateProxy> oldConvertedStepMap2 =
              oldConvertedStep2.getStateMap();
          final TraceStepProxy newConvertedStep2 =
              factory.createTraceStepProxy(stepEvent, oldConvertedStepMap2);
          convertedSteps.add(0, newConvertedStep2);
          newPrevStateMap.put(orig, originalAutState);
          final TraceStepProxy newConvertedStep1 =
              factory.createTraceStepProxy(null, newPrevStateMap);
          convertedSteps.add(0, newConvertedStep1);
        }
      }
    }

    /**
     * Uses the state in the original automaton which is found to be in the
     * first step of the trace, calls {@link #completeEndOfTrace(int)
     * completeEndOfTrace()} to find any steps needed to reach the actual start
     * state of the trace according. Steps are then created and prepended to the
     * converted steps list.
     */
    private void createInitialSteps(final TraceStepProxy step0,
                                    final List<TraceStepProxy> convertedSteps)
    {
      final AutomatonProxy orig = getOriginalAutomaton();
      final TraceStepProxy first = convertedSteps.iterator().next();
      final Map<AutomatonProxy,StateProxy> firstMap = first.getStateMap();
      final StateProxy originalAutState = firstMap.get(orig);
      final List<SearchRecord> initSteps =
          completeEndOfTrace(getOriginalStateToIntMap().get(originalAutState));
      if (initSteps.size() > 0) {
        final Map<AutomatonProxy,StateProxy> stepMap =
            new HashMap<AutomatonProxy,StateProxy>(step0.getStateMap());
        stepMap.remove(getResultAutomaton());
        final List<TraceStepProxy> substeps =
            createTraceSteps(stepMap, stepMap, initSteps, null);
        prependSteps(substeps, convertedSteps);
      }
    }

    private List<TraceStepProxy> createTraceSteps(
                                                  final Map<AutomatonProxy,StateProxy> prevStateMap,
                                                  final Map<AutomatonProxy,StateProxy> succStateMap,
                                                  final List<SearchRecord> subtrace,
                                                  final EventProxy stepEvent)
    {
      if (subtrace.isEmpty()) {
        return Collections.emptyList();
      } else {
        final ProductDESProxyFactory factory = getFactory();
        final AutomatonProxy aut = getOriginalAutomaton();
        final ObserverProjectionTransitionRelation rel =
            getTransitionRelation();
        final StateProxy[] originalStates = getOriginalStates();
        final List<TraceStepProxy> substeps = new LinkedList<TraceStepProxy>();
        // Note. Subtrace is presented in reverse order from the search,
        // which gives the proper order in the original automaton,
        // because the search was performed on a reverse transition relation.
        final Iterator<SearchRecord> iter = subtrace.iterator();
        Map<AutomatonProxy,StateProxy> stepStateMap = succStateMap;
        SearchRecord record = null;
        while (iter.hasNext()) {
          // Each search record contains a source state and corresponding
          // event. We must create a trace step consisting of target state
          // and event.
          record = iter.next();
          final int eventID = record.getEvent();
          final EventProxy event = rel.getEvent(eventID);
          final SearchRecord succRecord = record.getPredecessor();
          final int targetStateID = succRecord.getState();
          final StateProxy targetState = originalStates[targetStateID];
          stepStateMap.put(aut, targetState);
          final TraceStepProxy convertedStep =
              factory.createTraceStepProxy(event, stepStateMap);
          substeps.add(0, convertedStep);
          if (event == stepEvent) {
            stepStateMap = prevStateMap;
          }
        }
        // Finally, create a trace step consisting of the trace's start state,
        // i.e. the state found in the first search record, and associate it
        // with no event. This step will be replaced by a step with a proper
        // event when prepending the next trace segment in prependSteps().
        final int startStateID = record.getState();
        final StateProxy startState = originalStates[startStateID];
        prevStateMap.put(aut, startState);
        final TraceStepProxy startStep =
            factory.createTraceStepProxy(null, prevStateMap);
        substeps.add(0, startStep);
        return substeps;
      }
    }

    private void prependSteps(final List<TraceStepProxy> prefix,
                              final List<TraceStepProxy> result)
    {
      if (!prefix.isEmpty()) {
        final int size = prefix.size();
        final ListIterator<TraceStepProxy> iter = prefix.listIterator(size);
        // The first step of the result contains the state information,
        // the last step of the prefix contains the event.
        // We must merge them into a single trace step.
        final TraceStepProxy lastStepOfPrefix = iter.previous();
        final EventProxy event = lastStepOfPrefix.getEvent();
        final TraceStepProxy firstStepOfResult = result.remove(0);
        final Map<AutomatonProxy,StateProxy> map =
            firstStepOfResult.getStateMap();
        final ProductDESProxyFactory factory = getFactory();
        final TraceStepProxy merged = factory.createTraceStepProxy(event, map);
        result.add(0, merged);
        // Now we can prepend the other steps.
        while (iter.hasPrevious()) {
          final TraceStepProxy step = iter.previous();
          result.add(0, step);
        }
      }
    }

    // #######################################################################
    // # Data Members
    /**
     * Maps state codes of the input transition relation to state codes (i.e.,
     * class numbers) in the output transition relation. Obtained from
     * observation equivalence minimiser.
     */
    private final int[] mClassMap;

  }


  // #########################################################################
  // # Inner Class RemovalOfMarkingsStep
  /**
   * This step class performs correct counterexample trace conversion for both
   * the removal of alpha markings and the removal of omega markings (even
   * though the application of these rules is different).
   */
  private class RemovalOfMarkingsOrNoncoreachableStatesStep extends Step
  {
    RemovalOfMarkingsOrNoncoreachableStatesStep(
                                                final AutomatonProxy resultAut,
                                                final AutomatonProxy originalAut,
                                                final StateProxy[] originalStates,
                                                final TObjectIntHashMap<StateProxy> resultingStates)
    {
      super(resultAut, originalAut);
      mOriginalStates = originalStates;
      mResultingStates = resultingStates;

    }

    ConflictTraceProxy convertTrace(final ConflictTraceProxy conflictTrace)
    {
      final List<TraceStepProxy> traceSteps = conflictTrace.getTraceSteps();
      final int numSteps = traceSteps.size();
      final List<TraceStepProxy> convertedSteps =
          new ArrayList<TraceStepProxy>(numSteps);
      for (final TraceStepProxy step : traceSteps) {
        final EventProxy stepEvent = step.getEvent();
        final Map<AutomatonProxy,StateProxy> stepsNewStateMap =
            new HashMap<AutomatonProxy,StateProxy>(step.getStateMap());
        final StateProxy targetState =
            stepsNewStateMap.get(getResultAutomaton());

        stepsNewStateMap.remove(getResultAutomaton());
        final int stateID = mResultingStates.get(targetState);
        final StateProxy replacementState = mOriginalStates[stateID];
        stepsNewStateMap.put(getOriginalAutomaton(), replacementState);
        final TraceStepProxy convertedStep =
            getFactory().createTraceStepProxy(stepEvent, stepsNewStateMap);
        convertedSteps.add(convertedStep);
      }
      final List<AutomatonProxy> traceAutomata =
          new ArrayList<AutomatonProxy>(conflictTrace.getAutomata().size());
      for (final AutomatonProxy aut : conflictTrace.getAutomata()) {
        if (aut != getResultAutomaton()) {
          traceAutomata.add(aut);
        }
      }
      traceAutomata.add(getOriginalAutomaton());
      return createConvertedTrace(traceAutomata, convertedSteps);

    }

    // #######################################################################
    // # Data Members
    private final TObjectIntHashMap<StateProxy> mResultingStates;
    private final StateProxy[] mOriginalStates;
  }


  // #########################################################################
  // # Inner Class RemovalOfTauTransitionsLeadingToNonAlphaStatesStep
  /**
   * This step class performs correct counterexample trace conversion for
   * {@link RemovalOfTauTransitionsLeadingToNonAlphaStatesRule} and.
   * {@link RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule}.
   */
  private class RemovalOfTauTransitionsStep extends RemovalOfTransitionsStep
  {
    RemovalOfTauTransitionsStep(final AutomatonProxy resultAut,
                                final AutomatonProxy originalAut,
                                final EventProxy tau,
                                final ObserverProjectionTransitionRelation tr)
    {
      super(resultAut, originalAut, tau, tr);
    }

    /**
     * Creates the beginning of a trace by doing a breadth-first search to find
     * the correct initial state of the original automaton. Steps are added for
     * tau transitions (if necessary) until the initial state of the result
     * automaton is reached.
     *
     * @return A list of SearchRecords that represent each extra step needed for
     *         the start of the trace. (The first item being the very first
     *         state of the trace).
     */
    protected List<SearchRecord> completeStartOfTrace(
                                                      final TIntArrayList initialStateIDs,
                                                      final int resultTraceInitialState)
    {
      final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
      final TIntHashSet visited = new TIntHashSet();
      // The dummy record ensures that the first real search record will
      // later be included in the trace.
      final SearchRecord dummy = new SearchRecord(-1);
      final int numInit = initialStateIDs.size();
      for (int i = 0; i < numInit; i++) {
        final int initStateID = initialStateIDs.get(i);
        final SearchRecord record =
            new SearchRecord(initStateID, false, -1, dummy);
        if (resultTraceInitialState == initStateID) {
          return Collections.singletonList(record);
        }
        open.add(record);
        visited.add(initStateID);
      }
      while (true) {
        final SearchRecord current = open.remove();
        final int source = current.getState();
        final TIntHashSet successors =
            getTransitionRelation().getSuccessors(source, getTauCode());
        if (successors != null) {
          final TIntIterator iter = successors.iterator();
          while (iter.hasNext()) {
            final int target = iter.next();
            if (!visited.contains(target)) {
              final SearchRecord record =
                  new SearchRecord(target, false, getTauCode(), current);
              if (resultTraceInitialState == target) {
                return buildSearchRecordTrace(record);
              }
              open.add(record);
              visited.add(target);
            }
          }
        }
      }
    }

    /**
     * Completes a trace by adding steps for tau transitions (if necessary)
     * until the end state of the trace has the alpha marking.
     *
     * @param originalSource
     *          The original end state, which may or may not have the alpha
     *          marking proposition.
     * @return A list of SearchRecord's that represent each extra step added of
     *         the trace. (The last item being the end state of the trace).
     */
    protected List<SearchRecord> completeEndOfTrace(final int originalSource)
    {
      if (!getOriginalAutomaton().getEvents()
          .contains(getUsedPreconditionMarkingProposition())
          || getOriginalStates()[originalSource].getPropositions()
              .contains(getUsedPreconditionMarkingProposition())) {
        return Collections.emptyList();
      }
      final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
      final TIntHashSet visited = new TIntHashSet();
      SearchRecord record = new SearchRecord(originalSource);
      open.add(record);
      visited.add(originalSource);
      while (true) {
        final SearchRecord current = open.remove();
        final int source = current.getState();
        final TIntHashSet successors =
            getTransitionRelation().getSuccessors(source, getTauCode());
        if (successors != null) {
          final TIntIterator iter = successors.iterator();
          while (iter.hasNext()) {
            final int target = iter.next();
            if (!visited.contains(target)) {
              record = new SearchRecord(target, false, getTauCode(), current);
              if (getOriginalStates()[target].getPropositions()
                  .contains(getUsedPreconditionMarkingProposition())) {
                return buildSearchRecordTrace(record);
              }
              open.add(record);
              visited.add(target);
            }
          }
        }
      }
    }

    protected TIntHashSet getSuccessorsOrPredecessors(final int source,
                                                      final int event)
    {
      return getTransitionRelation().getSuccessors(source, event);
    }

    protected boolean isTargetState(final int stateFound,
                                    final int resultAutTarget)
    {
      return stateFound == resultAutTarget;
    }

    protected boolean isTraceEndState(final int stateFound)
    {
      final ObserverProjectionTransitionRelation rel = getTransitionRelation();
      final int alpha = getAlphaCode();
      return rel.isMarked(stateFound, alpha);
    }

  }


  // #########################################################################
  // # Inner Class SearchRecord
  private static class SearchRecord
  {

    // #######################################################################
    // # Constructors
    SearchRecord(final int state)
    {
      this(state, false, -1, null);
    }

    SearchRecord(final int state, final boolean hasEvent, final int event,
                 final SearchRecord pred)
    {
      mState = state;
      mHasProperEvent = hasEvent;
      mEvent = event;
      mPredecessor = pred;
    }

    // #######################################################################
    // # Getters
    boolean hasProperEvent()
    {
      return mHasProperEvent;
    }

    int getState()
    {
      return mState;
    }

    SearchRecord getPredecessor()
    {
      return mPredecessor;
    }

    int getEvent()
    {
      return mEvent;
    }

    // #######################################################################
    // # Data Members
    private final int mState;
    private final boolean mHasProperEvent;
    private final int mEvent;
    private final SearchRecord mPredecessor;
  }

  // #########################################################################
  // # Data Members
  private Map<EventProxy,Set<AutomatonProxy>> mEventsToAutomata;
  private Set<Candidate> mUnsuccessfulCandidates;
  private Set<AutomatonProxy> mTrivialAbstractedAutomata;
  private List<Step> mTemporaryModifyingSteps;
  private Collection<EventProxy> mPropositions;
  private EventProxy mUsedPreconditionMarking;

  // configuration
  private PreselectingHeuristic mPreselectingHeuristic;
  private List<SelectingHeuristic> mSelectingHeuristics;
  private List<AbstractionRule> mAbstractionRules;
  private int mSyncProductNodeLimit = Integer.MAX_VALUE;
  private int mFinalStepNodeLimit = Integer.MAX_VALUE;
  private int mSyncProductTransitionLimit = Integer.MAX_VALUE;
  private int mFinalStepTransitionLimit = Integer.MAX_VALUE;

  // statistics
  private int mSuccessfulCompositionCount;
  private int mUnsuccessfulCompositionCount;
  private double mTotalNumberOfStates;
  private double mPeakNumberOfStates;
  private double mTotalNumberOfTransitions;
  private double mPeakNumberOfTransitions;
  private double mComposedModelNumberOfStates;
  private double mComposedModelNumberOfTransitions;
}
