//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   CompositionalGeneralisedConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.MonolithicConflictChecker;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.op.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.op.ObserverProjectionTransitionRelation;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.SynchronousProductStateMap;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;


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
  // # Invocation
  public boolean run() throws AnalysisException
  {
    setUp();
    ProductDESProxy model = getModel();
    mapEventsToAutomata(model);
    final List<AutomatonProxy> remainingAut =
        new ArrayList<AutomatonProxy>(model.getAutomata());

    // TODO: later, need to consider when an automaton is too large to be a
    // candidate and so may not always be left with only one automaton
    while (remainingAut.size() > 1) {
      final Collection<Candidate> candidates = findCandidates(model);
      Candidate candidate = null;
      final int numCandidates = candidates.size();
      if (numCandidates > 1) {
        candidate = evaluateCandidates(candidates);
      } else if (numCandidates == 1) {
        final Iterator<Candidate> it = candidates.iterator();
        candidate = it.next();
      } else {
        break;
      }
      // TODO: candidate selection (i.e. heuristics) still need testing

      final AutomatonProxy syncProduct = composeSynchronousProduct(candidate);
      // TODO: added this next while loop to allow a new candidate to be chosen
      // if an overflow exception occurs, to get more tests passing. But this
      // causes tests to take too long to run, and of course the
      // testOverflowException test can not be run
      /*
       * while (syncProduct == null) { candidates.remove(candidate); final
       * Candidate nextCandidate = evaluateCandidates(candidates); syncProduct =
       * composeSynchronousProduct(nextCandidate); }
       */
      // TODO: Skip hiding step if there are no local events (?)
      final EventProxy tau = createTauEvent(syncProduct);
      final AutomatonProxy autToAbstract =
          hideLocalEvents(syncProduct, candidate.getLocalEvents(), tau);

      final AutomatonProxy abstractedAut =
          applyAbstractionRules(autToAbstract, tau);

      // removes the composed automata for this candidate from the set of
      // remaining automata and adds the newly composed candidate
      remainingAut.removeAll(candidate.getAutomata());
      remainingAut.add(abstractedAut);
      updateEventsToAutomata(abstractedAut, candidate.getAutomata());

      // updates the current model to find candidates from
      final Set<EventProxy> composedModelAlphabet =
          getEventsForNewModel(remainingAut);
      model =
          getFactory().createProductDESProxy(model.getName(),
                                             model.getComment(),
                                             model.getLocation(),
                                             composedModelAlphabet,
                                             remainingAut);
    }
    final ConflictChecker checker =
        new MonolithicConflictChecker(model, getUsedMarkingProposition(),
            getGeneralisedPrecondition(), getFactory());
    checker.setNodeLimit(1000000);
    final boolean result = checker.run();

    if (result) {
      setSatisfiedResult();
    } else {
      final ConflictTraceProxy counterexample = checker.getCounterExample();
      final int size = mModifyingSteps.size();
      ConflictTraceProxy convertedTrace = counterexample;
      convertedTrace = saturateTrace(counterexample);
      final ListIterator<Step> iter = mModifyingSteps.listIterator(size);
      while (iter.hasPrevious()) {
        final Step step = iter.previous();
        // You can use this new tool to check whether your counterexample
        // works in the intermediate steps. Throws exceptions if something
        // is wrong.
        // TraceChecker.checkCounterExample(convertedTrace);
        convertedTrace = step.convertTrace(convertedTrace);
        // TraceChecker.checkCounterExample(convertedTrace);
      }
      setFailedResult(convertedTrace);
    }
    tearDown();
    return result;
  }

  /**
   * Fills in the target states in the stateMaps for each step of the trace for
   * all automaton.
   */
  private ConflictTraceProxy saturateTrace(
                                           final ConflictTraceProxy counterexample)
  {
    final List<TraceStepProxy> traceSteps = counterexample.getTraceSteps();
    final List<TraceStepProxy> convertedSteps = new ArrayList<TraceStepProxy>();
    Map<AutomatonProxy,StateProxy> prevStepMap = null;

    for (final TraceStepProxy step : traceSteps) {
      final Map<AutomatonProxy,StateProxy> stepMap =
          new HashMap<AutomatonProxy,StateProxy>();
      final EventProxy stepEvent = step.getEvent();
      for (final AutomatonProxy aut : counterexample.getAutomata()) {
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
                                              counterexample.getAutomata(),
                                              convertedSteps,
                                              counterexample.getKind());
    System.out.println(counterexample);
    System.out.println("SATURATED---------" + saturatedCounterexample);
    return saturatedCounterexample;
  }

  /**
   * Finds the successor/target state in the given automaton, given a source
   * state and event. Used in deterministic cases only (in nondeterministic
   * cases the successors are already available in the step's stateMap).
   */
  private StateProxy findSuccessor(final AutomatonProxy aut,
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
   * Initialises required variables to default values if the user hasn't
   * configured them.
   */
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    if (mPreselectingHeuristic == null) {
      // final PreselectingHeuristic defaultHeuristic = new HeuristicMinT();
      // final PreselectingHeuristic defaultHeuristic = new HeuristicMaxS();
      final PreselectingHeuristic defaultHeuristic = new HeuristicMustL();
      setPreselectingHeuristic(defaultHeuristic);
    }
    if (mSelectingHeuristics == null) {
      final SelectingHeuristic defaultHeuristic = new HeuristicMinS();
      setSelectingHeuristic(defaultHeuristic);
    }
    mModifyingSteps = new ArrayList<Step>();
    mPropositions = new ArrayList<EventProxy>(2);
    if (getGeneralisedPrecondition() != null) {
      mPropositions.add(getGeneralisedPrecondition());
    }
    mPropositions.add(getMarkingProposition(getModel()));

    mAbstractionRules = new LinkedList<AbstractionRule>();

    final ObservationEquivalenceRule oeRule =
        new ObservationEquivalenceRule(getFactory(), mPropositions);
    mAbstractionRules.add(oeRule);

    /*
     * final RemovalOfAlphaMarkingsRule ramRule = new
     * RemovalOfAlphaMarkingsRule(getFactory(), mPropositions);
     * ramRule.setAlphaMarking(getGeneralisedPrecondition());
     * mAbstractionRules.add(ramRule);
     */

    /*
     * final RemovalOfDefaultMarkingsRule rdmRule = new
     * RemovalOfDefaultMarkingsRule(getFactory(), mPropositions);
     * rdmRule.setAlphaMarking(getGeneralisedPrecondition());
     * rdmRule.setDefaultMarking(getMarkingProposition());
     * mAbstractionRules.add(rdmRule);
     */

    /*
     * final RemovalOfNoncoreachableStatesRule rnsRule = new
     * RemovalOfNoncoreachableStatesRule(getFactory(), mPropositions);
     * rnsRule.setAlphaMarking(getGeneralisedPrecondition());
     * rnsRule.setDefaultMarking(getMarkingProposition());
     * mAbstractionRules.add(rnsRule);
     */

    /*
     * final RemovalOfTauTransitionsLeadingToNonAlphaStatesRule rttnsRule = new
     * RemovalOfTauTransitionsLeadingToNonAlphaStatesRule(getFactory(),
     * mPropositions); rttnsRule.setAlphaMarking(getGeneralisedPrecondition());
     * mAbstractionRules.add(rttnsRule);
     */
  }

  // #########################################################################
  // # Auxiliary Methods
  private AutomatonProxy applyAbstractionRules(AutomatonProxy autToAbstract,
                                               final EventProxy tau)
      throws OverflowException
  {

    final ListIterator<AbstractionRule> iter = mAbstractionRules.listIterator();
    AutomatonProxy abstractedAut = autToAbstract;
    while (iter.hasNext()) {
      final AbstractionRule rule = iter.next();
      abstractedAut = rule.applyRule(autToAbstract, tau);
      if (autToAbstract != abstractedAut) {
        final Step step = rule.createStep(this, abstractedAut);
        mModifyingSteps.add(step);
      }
      autToAbstract = abstractedAut;
    }
    return abstractedAut;
  }

  /**
   * Builds the synchronous product for a given candidate.
   */
  private AutomatonProxy composeSynchronousProduct(final Candidate candidate)
      throws AnalysisException
  {
    // creates a model which includes only the candidate, to build the
    // synchronous product of
    final Set<EventProxy> candidateEvents =
        getEventsForNewModel(candidate.getAutomata());
    final ProductDESProxy candidateModel =
        getFactory().createProductDESProxy("Candidate model", candidateEvents,
                                           candidate.getAutomata());
    final MonolithicSynchronousProductBuilder composer =
        new MonolithicSynchronousProductBuilder(candidateModel, getFactory());
    composer.setPropositions(mPropositions);
    composer.setNodeLimit(getNodeLimit());
    // try {
    composer.run();
    final AutomatonProxy syncProduct = composer.getComputedAutomaton();
    final CompositionStep step =
        new CompositionStep(syncProduct, composer.getStateMap());
    mModifyingSteps.add(step);
    return syncProduct;
    /*
     * } catch (final OverflowException e) { return null; }
     */// TODO: see comments in run()
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
    final Map<TransitionProxy,TransitionProxy> transitionMap =
        new HashMap<TransitionProxy,TransitionProxy>(automaton.getTransitions()
            .size());
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
        transitionMap.put(newTrans, transition);
      } else {
        newTransitions.add(transition);
      }
    }
    final Set<EventProxy> newEvents = new HashSet<EventProxy>();
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
    final HidingStep step = new HidingStep(newAut, automaton, tau);
    mModifyingSteps.add(step);
    return newAut;
  }

  /**
   * Returns a set of events for a new model which is the alphabet from a given
   * set of automata.
   */
  private Set<EventProxy> getEventsForNewModel(
                                               final List<AutomatonProxy> automataOfNewModel)
  {
    final Set<EventProxy> events = new HashSet<EventProxy>();
    for (final AutomatonProxy aut : automataOfNewModel) {
      events.addAll(aut.getEvents());
    }
    return events;
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
            final Set<AutomatonProxy> automata = new HashSet<AutomatonProxy>();
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
    for (final EventProxy event : autToAdd.getEvents()) {
      if (event.getKind() != EventKind.PROPOSITION) {
        if (!mEventsToAutomata.containsKey(event)) {
          final Set<AutomatonProxy> automata = new HashSet<AutomatonProxy>();
          mEventsToAutomata.put(event, automata);
        }
        mEventsToAutomata.get(event).add(autToAdd);
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
                                              final Map<EventProxy,Set<AutomatonProxy>> eventAutomaton,
                                              final List<AutomatonProxy> candidate)
  {
    final Set<EventProxy> localEvents = new HashSet<EventProxy>();
    for (final EventProxy event : eventAutomaton.keySet()) {
      final Set<AutomatonProxy> autWithEvent = eventAutomaton.get(event);
      if (candidate.containsAll(autWithEvent)) {
        localEvents.add(event);
      }
    }
    return localEvents;
  }

  /**
   * Uses a heuristic to evaluate the set of candidates to select a suitable
   * candidate to compose next.
   */
  private Candidate evaluateCandidates(Collection<Candidate> candidates)
  {

    final ListIterator<SelectingHeuristic> iter =
        mSelectingHeuristics.listIterator();
    List<Candidate> selectedCandidates = new ArrayList<Candidate>(candidates);
    while (iter.hasNext()) {
      final SelectingHeuristic heuristic = iter.next();
      selectedCandidates = heuristic.evaluate(selectedCandidates);
      if (selectedCandidates.size() == 1) {
        break;
      } else {
        candidates = new ArrayList<Candidate>(selectedCandidates);
      }
    }
    return selectedCandidates.get(0);
  }

  /**
   * Finds the set of candidates to compose for a given model.
   */
  private Collection<Candidate> findCandidates(final ProductDESProxy model)
  {
    return mPreselectingHeuristic.evaluate(model);
  }

  public HeuristicMinT createHeuristicMinT()
  {
    return new HeuristicMinT();
  }

  public HeuristicMaxS createHeuristicMaxS()
  {
    return new HeuristicMaxS();
  }

  public HeuristicMustL createHeuristicMustL()
  {
    return new HeuristicMustL();
  }

  public HeuristicMaxL createHeuristicMaxL()
  {
    return new HeuristicMaxL();
  }

  public HeuristicMaxC createHeuristicMaxC()
  {
    return new HeuristicMaxC();
  }

  public HeuristicMinS createHeuristicMinS()
  {
    return new HeuristicMinS();
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
    } else if (heuristic instanceof HeuristicMaxC) {
      mSelectingHeuristics.add(new HeuristicMaxL());
      mSelectingHeuristics.add(new HeuristicMinS());
    } else if (heuristic instanceof HeuristicMinS) {
      mSelectingHeuristics.add(new HeuristicMaxL());
      mSelectingHeuristics.add(new HeuristicMaxC());
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


  private interface PreselectingHeuristic
  {
    public Collection<Candidate> evaluate(final ProductDESProxy model);

  }


  private class HeuristicPairing
  {
    protected Collection<Candidate> pairAutomaton(
                                                  final AutomatonProxy chosenAut,
                                                  final Set<AutomatonProxy> automata)
    {
      final Collection<Candidate> candidates =
          new HashSet<Candidate>(automata.size() - 1);
      for (final AutomatonProxy a : automata) {
        if (a != chosenAut) {
          final List<AutomatonProxy> pair = new ArrayList<AutomatonProxy>(2);
          // Bring pair into defined ordering.
          // (Better do this here than as side effect of constructor.)
          if (chosenAut.compareTo(a) < 0) {
            pair.add(chosenAut);
            pair.add(a);
          } else {
            pair.add(a);
            pair.add(chosenAut);
          }
          final Set<EventProxy> localEvents =
              identifyLocalEvents(mEventsToAutomata, pair);

          if (localEvents.size() > 0) {
            final Candidate candidate = new Candidate(pair, localEvents);
            candidates.add(candidate);
          }
        }
      }
      return candidates;
    }
  }


  private class HeuristicMinT extends HeuristicPairing implements
      PreselectingHeuristic
  {
    public Collection<Candidate> evaluate(final ProductDESProxy model)
    {
      // Find automaton with fewest transitions
      final Set<AutomatonProxy> automata = model.getAutomata();
      final Iterator<AutomatonProxy> it = automata.iterator();
      AutomatonProxy chosenAut = it.next();
      int minTrans = chosenAut.getTransitions().size();
      while (it.hasNext()) {
        final AutomatonProxy nextAut = it.next();
        final int transCount = nextAut.getTransitions().size();
        if (transCount < minTrans) {
          minTrans = transCount;
          chosenAut = nextAut;
        }
      }
      // pairs chosen automaton with all others
      final Collection<Candidate> candidates =
          pairAutomaton(chosenAut, automata);
      return candidates;
    }
  }


  /**
   * Performs step 1 of the approach to select the automata to compose. A
   * candidate is produced by pairing the automaton with the most states to
   * every other automaton in the model.
   */
  private class HeuristicMaxS extends HeuristicPairing implements
      PreselectingHeuristic
  {

    public Collection<Candidate> evaluate(final ProductDESProxy model)
    {
      // Find automaton with the most states
      final Set<AutomatonProxy> automata = model.getAutomata();
      final Iterator<AutomatonProxy> it = automata.iterator();
      AutomatonProxy chosenAut = it.next();
      int maxStates = chosenAut.getStates().size();
      while (it.hasNext()) {
        final AutomatonProxy nextAut = it.next();
        final int statesCount = nextAut.getStates().size();
        if (statesCount > maxStates) {
          maxStates = statesCount;
          chosenAut = nextAut;
        }
      }
      // pairs chosen automaton with all others
      final Collection<Candidate> candidates =
          pairAutomaton(chosenAut, automata);
      return candidates;
    }
  }


  private class HeuristicMustL implements PreselectingHeuristic
  {
    public Collection<Candidate> evaluate(final ProductDESProxy model)
    {
      final Collection<Candidate> candidates =
          new HashSet<Candidate>(mEventsToAutomata.keySet().size());
      for (final EventProxy event : mEventsToAutomata.keySet()) {
        final List<AutomatonProxy> automata =
            new ArrayList<AutomatonProxy>(mEventsToAutomata.get(event));
        assert automata.size() > 0;
        if (automata.size() > 1 && automata.size() < model.getAutomata().size()) {
          // Bring automata into defined ordering.
          // (Better do this here than as side effect of constructor.)
          Collections.sort(automata);
          final Set<EventProxy> localEvents =
              identifyLocalEvents(mEventsToAutomata, automata);
          final Candidate candidate = new Candidate(automata, localEvents);
          candidates.add(candidate);
        }
      }
      return candidates;
    }
  }


  private interface SelectingHeuristic
  {
    public List<Candidate> evaluate(final List<Candidate> candidates);

  }


  /**
   * Performs step 2 of the approach to select the automata to compose. The
   * chosen candidate is the one with the highest proportion of local events.
   */
  private class HeuristicMaxL implements SelectingHeuristic
  {
    public List<Candidate> evaluate(final List<Candidate> candidates)
    {
      final Iterator<Candidate> it = candidates.iterator();
      List<Candidate> chosenCandidates = new ArrayList<Candidate>();
      Candidate chosenCandidate = it.next();
      chosenCandidates.add(chosenCandidate);
      double maxLocal =
          (double) chosenCandidate.getLocalEventCount()
              / (double) chosenCandidate.getNumberOfEvents();
      while (it.hasNext()) {
        final Candidate nextCan = it.next();
        final double proportion =
            (double) nextCan.getLocalEventCount()
                / (double) nextCan.getNumberOfEvents();
        if (proportion > maxLocal) {
          chosenCandidates = new ArrayList<Candidate>();
          maxLocal = proportion;
          chosenCandidate = nextCan;
          chosenCandidates.add(chosenCandidate);
        } else if (proportion == maxLocal) {
          chosenCandidate = nextCan;
          chosenCandidates.add(chosenCandidate);
        }
      }
      return chosenCandidates;
    }
  }


  /**
   * Performs step 2 of the approach to select the automata to compose. The
   * chosen candidate is the one with the highest proportion of common events.
   */
  private class HeuristicMaxC implements SelectingHeuristic
  {
    public List<Candidate> evaluate(final List<Candidate> candidates)
    {
      final ListIterator<Candidate> it = candidates.listIterator();
      List<Candidate> chosenCandidates = new ArrayList<Candidate>();
      Candidate chosenCandidate = it.next();
      chosenCandidates.add(chosenCandidate);
      double maxCommon =
          (double) (chosenCandidate.getNumberOfEvents() - chosenCandidate
              .getLocalEventCount())
              / (double) chosenCandidate.getNumberOfEvents();
      while (it.hasNext()) {
        final Candidate nextCan = it.next();
        final double proportion =
            (double) (nextCan.getNumberOfEvents() - nextCan
                .getLocalEventCount())
                / (double) nextCan.getNumberOfEvents();
        if (proportion > maxCommon) {
          chosenCandidates = new ArrayList<Candidate>();
          maxCommon = proportion;
          chosenCandidate = nextCan;
          chosenCandidates.add(chosenCandidate);
        } else if (proportion == maxCommon) {
          chosenCandidate = nextCan;
          chosenCandidates.add(chosenCandidate);
        }
      }
      return chosenCandidates;
    }
  }


  private class HeuristicMinS implements SelectingHeuristic
  {
    public List<Candidate> evaluate(final List<Candidate> candidates)
    {
      Candidate chosenCandidate = candidates.get(0);
      List<Candidate> chosenCandidates = new ArrayList<Candidate>();
      int smallestProduct = calculateProduct(chosenCandidate);
      chosenCandidates.add(chosenCandidate);
      for (int i = 1; i < candidates.size(); i++) {
        final Candidate candidate = candidates.get(i);
        final int newproduct = calculateProduct(candidate);
        if (smallestProduct > newproduct) {
          chosenCandidates = new ArrayList<Candidate>();
          smallestProduct = newproduct;
          chosenCandidate = candidate;
          chosenCandidates.add(chosenCandidate);
        } else if (smallestProduct == newproduct) {
          chosenCandidate = candidate;
          chosenCandidates.add(chosenCandidate);
        }
      }
      return chosenCandidates;
    }

    private int calculateProduct(final Candidate candidate)
    {
      int product = 1;
      for (final AutomatonProxy aut : candidate.getAutomata()) {
        product *= aut.getStates().size();
      }
      return product;
    }
  }


  /**
   * This heuristic is provided for when the other 3 fail to find one unique
   * candidate. The selection is made by comparing the candidates automata names
   * alphabetically.
   */
  private class HeuristicDefault implements SelectingHeuristic
  {
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
          // until
          // a point where one has run out of automata, the candidate with
          // more
          // automata is selected
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
  }

  public ObservationEquivalenceStep createObservationEquivalenceStep(
                                                                     final AutomatonProxy abstractedAut,
                                                                     final AutomatonProxy autToAbstract,
                                                                     final EventProxy tau,
                                                                     final ObserverProjectionTransitionRelation tr,
                                                                     final ObservationEquivalenceTRSimplifier biSimulator)
  {
    final ObservationEquivalenceStep oeStep =
        new ObservationEquivalenceStep(abstractedAut, autToAbstract, tau, tr
            .getOriginalIntToStateMap(), biSimulator.getStateClasses(), tr
            .getResultingStateToIntMap());
    return oeStep;
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

  public RemovalOfTauTransitionsLeadingToNonAlphaStatesStep createRemovalOfTauTransitionsLeadingToNonAlphaStatesStep(
                                                                                                                     final AutomatonProxy abstractedAut,
                                                                                                                     final AutomatonProxy autToAbstract,
                                                                                                                     final EventProxy tau,
                                                                                                                     final ObserverProjectionTransitionRelation tr)
  {
    return new RemovalOfTauTransitionsLeadingToNonAlphaStatesStep(
        abstractedAut, autToAbstract, tau, tr);
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
    // # Auxiliary Methods
    /**
     * Returns a collection containing all initial states of an automaton.
     */
    protected Collection<StateProxy> getInitialStates(final AutomatonProxy aut)
    {
      final Collection<StateProxy> initialstates = new HashSet<StateProxy>();
      for (final StateProxy state : aut.getStates()) {
        if (state.isInitial()) {
          initialstates.add(state);
        }
      }
      assert initialstates.size() > 0;
      return initialstates;
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
      final Set<AutomatonProxy> traceAutomata =
          new HashSet<AutomatonProxy>(conflictTrace.getAutomata());
      traceAutomata.remove(composed);
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
          final Collection<AutomatonProxy> autOfComposition =
              mStateMap.getInputAutomata();
          for (final AutomatonProxy aut : autOfComposition) {
            final StateProxy originalState =
                mStateMap.getOriginalState(convertedState, aut);
            convertedStepMap.put(aut, originalState);
            // Bug? How often do we add aut? What if there are no steps?
            traceAutomata.add(aut);
          }
          final TraceStepProxy convertedStep =
              getFactory().createTraceStepProxy(step.getEvent(),
                                                convertedStepMap);
          convertedSteps.add(convertedStep);
        } else {
          convertedSteps.add(step);
        }
      }
      final Set<EventProxy> events = new HashSet<EventProxy>();
      for (final AutomatonProxy aut : traceAutomata) {
        events.addAll(aut.getEvents());
      }
      final ConflictTraceProxy convertedTrace =
          getFactory().createConflictTraceProxy(conflictTrace.getName(),
                                                conflictTrace.getComment(),
                                                conflictTrace.getLocation(),
                                                getModel(), traceAutomata,
                                                convertedSteps,
                                                ConflictKind.CONFLICT);
      return convertedTrace;
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
                       final AutomatonProxy originalAut, final EventProxy tau)
    {
      super(result, originalAut);
      mTau = tau;
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
      final Set<AutomatonProxy> traceAutomata =
          new HashSet<AutomatonProxy>(conflictTrace.getAutomata());
      traceAutomata.remove(getResultAutomaton());
      traceAutomata.add(getOriginalAutomaton());
      final ConflictTraceProxy convertedTrace =
          getFactory().createConflictTraceProxy(conflictTrace.getName(),
                                                conflictTrace.getComment(),
                                                conflictTrace.getLocation(),
                                                getModel(), traceAutomata,
                                                convertedSteps,
                                                ConflictKind.CONFLICT);
      return convertedTrace;
    }

    /**
     * Finds the event which was in the original automaton before hiding was
     * used and the event was replaced with tau.
     */
    private EventProxy findOriginalEvent(final StateProxy source,
                                         final StateProxy target)
    {
      EventProxy originalEvent = null;
      for (final TransitionProxy transition : getOriginalAutomaton()
          .getTransitions()) {
        if (transition.getTarget() == target
            && transition.getSource() == source) {
          originalEvent = transition.getEvent();
          break;
        }
      }
      return originalEvent;
    }

    // #######################################################################
    // # Data Members
    private final EventProxy mTau;
  }


  // #########################################################################
  // # Inner Class ObservationEquivalenceStep
  private class ObservationEquivalenceStep extends Step
  {

    ObservationEquivalenceStep(
                               final AutomatonProxy resultAut,
                               final AutomatonProxy originalAut,
                               final EventProxy tau,
                               final StateProxy[] originalStates,
                               final TIntObjectHashMap<int[]> classMap,
                               final TObjectIntHashMap<StateProxy> reverseOutputStateMap)
    {
      super(resultAut, originalAut);
      mOriginalStates = originalStates;
      mClassMap = classMap;
      mReverseOutputStateMap = reverseOutputStateMap;
      mTransitionRelation =
          new ObserverProjectionTransitionRelation(originalAut, mPropositions);
      mCodeOfTau = mTransitionRelation.getEventInt(tau);
      mOriginalStatesMap = mTransitionRelation.getOriginalStateToIntMap();
    }

    ConflictTraceProxy convertTrace(final ConflictTraceProxy conflictTrace)
    {
      // TODO For later, may also have to consider the case that the
      // simplified automaton does not contain any tau event, and only
      // bisimulation was used.
      final List<TraceStepProxy> convertedSteps =
          new ArrayList<TraceStepProxy>();
      final List<TraceStepProxy> traceSteps = conflictTrace.getTraceSteps();
      final int originalSourceID;

      // makes the trace begin in the correct initial state
      final TIntArrayList initialStates =
          mTransitionRelation.getAllInitialStates();
      final StateProxy tracesInitialState =
          getInitialState(getResultAutomaton(), traceSteps.get(0));
      final List<SearchRecord> initialRecords =
          beginTrace(initialStates, mReverseOutputStateMap
              .get(tracesInitialState));
      assert initialRecords.size() > 0;
      final Map<AutomatonProxy,StateProxy> initialStepsStateMap =
          new HashMap<AutomatonProxy,StateProxy>(traceSteps.get(0)
              .getStateMap());
      final List<TraceStepProxy> initialSteps =
          createTraceSteps(initialStepsStateMap, initialRecords);
      convertedSteps.addAll(initialSteps);
      originalSourceID =
          initialRecords.get(initialRecords.size() - 1).getState();
      Map<AutomatonProxy,StateProxy> stepsPrevStateMap =
          new HashMap<AutomatonProxy,StateProxy>(traceSteps.get(0)
              .getStateMap());
      stepsPrevStateMap.remove(getResultAutomaton());

      StateProxy originalSource = mOriginalStates[originalSourceID];
      for (final TraceStepProxy step : traceSteps) {
        final Map<AutomatonProxy,StateProxy> stepsNewStateMap =
            new HashMap<AutomatonProxy,StateProxy>(step.getStateMap());

        final EventProxy stepEvent = step.getEvent();
        if (stepEvent != null) {
          // handles events not in the simplified automaton
          // TODO Fix bug.
          // If the event is not in the result automaton, it may still be
          // in the original automaton. The simplifier may have detected that
          // the event is selflooped in all states of the result, and removed
          // it. (You can disable this optimisation, but that is not a good
          // idea.)
          // If the event is not in the original automaton, consider it as a
          // selfloop in the original automaton. This seems to be what you are
          // doing below, but is it guaranteed in all cases that the unchanged
          // target state is stored in the trace?
          // If the event is in the original automaton but not in the
          // result automaton, consider it as a selfloop in the result
          // automaton and still search for a trace. We need to find a way
          // how the original automaton can execute the step event and then
          // return to the current state---this is not always just a selfloop.
          if (getResultAutomaton().getEvents().contains(stepEvent)
              || getOriginalAutomaton().getEvents().contains(stepEvent)) {
            final int eventID = mTransitionRelation.getEventInt(stepEvent);

            final StateProxy resultTargetState =
                stepsNewStateMap.get(getResultAutomaton());
            assert resultTargetState != null;
            stepsNewStateMap.remove(getResultAutomaton());
            final List<SearchRecord> subtrace =
                findSubTrace(mOriginalStatesMap.get(originalSource), eventID,
                             mReverseOutputStateMap.get(resultTargetState));
            final List<TraceStepProxy> substeps =
                createTraceSteps(stepsPrevStateMap, stepsNewStateMap, subtrace,
                                 stepEvent);
            convertedSteps.addAll(substeps);
            final int subsize = subtrace.size();
            if (subsize > 0) {
              final int originalTargetID = subtrace.get(subsize - 1).getState();
              originalSource = mOriginalStates[originalTargetID];
            }
          } else {
            stepsNewStateMap.remove(getResultAutomaton());
            stepsNewStateMap.put(getOriginalAutomaton(), originalSource);
            final TraceStepProxy convertedStep =
                getFactory().createTraceStepProxy(stepEvent, stepsNewStateMap);
            convertedSteps.add(convertedStep);
          }
          stepsPrevStateMap =
              new HashMap<AutomatonProxy,StateProxy>(stepsNewStateMap);
          stepsPrevStateMap.remove(getResultAutomaton());
        }
      }
      // makes the trace end in an alpha state
      final List<SearchRecord> finalSteps =
          completeTrace(mOriginalStatesMap.get(originalSource));
      if (finalSteps.size() > 0) {
        final Map<AutomatonProxy,StateProxy> finalStepsStateMap =
            new HashMap<AutomatonProxy,StateProxy>(traceSteps
                .get(traceSteps.size() - 1).getStateMap());
        final List<TraceStepProxy> substeps =
            createTraceSteps(finalStepsStateMap, finalSteps);
        convertedSteps.addAll(substeps);
      }

      final Set<AutomatonProxy> traceAutomata =
          new HashSet<AutomatonProxy>(conflictTrace.getAutomata());
      traceAutomata.remove(getResultAutomaton());
      traceAutomata.add(getOriginalAutomaton());
      final ConflictTraceProxy convertedTrace =
          getFactory().createConflictTraceProxy(conflictTrace.getName(),
                                                conflictTrace.getComment(),
                                                conflictTrace.getLocation(),
                                                getModel(), traceAutomata,
                                                convertedSteps,
                                                ConflictKind.CONFLICT);
      return convertedTrace;
    }

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
          stepStateMap.put(getOriginalAutomaton(),
                           mOriginalStates[subStepTargetStateID]);
        } else if (event != stepEvent && eventFound) {
          stepStateMap = stepsNewStateMap;
          stepStateMap.put(getOriginalAutomaton(),
                           mOriginalStates[subStepTargetStateID]);
        } else if (event == stepEvent) {
          eventFound = true;
          stepStateMap = stepsNewStateMap;
          stepStateMap.put(getOriginalAutomaton(),
                           mOriginalStates[subStepTargetStateID]);
        }
        final TraceStepProxy convertedStep =
            factory.createTraceStepProxy(event, stepStateMap);
        substeps.add(convertedStep);
      }
      return substeps;
    }

    private List<TraceStepProxy> createTraceSteps(
                                                  final Map<AutomatonProxy,StateProxy> stepsStateMap,
                                                  final List<SearchRecord> subtrace)
    {
      stepsStateMap.remove(getResultAutomaton());
      final ProductDESProxyFactory factory = getFactory();
      final List<TraceStepProxy> substeps = new LinkedList<TraceStepProxy>();
      for (final SearchRecord subStep : subtrace) {
        final int subStepTargetStateID = subStep.getState();
        stepsStateMap.put(getOriginalAutomaton(),
                          mOriginalStates[subStepTargetStateID]);
        final int subStepEventID = subStep.getEvent();
        final EventProxy event =
            subStepEventID >= 0 ? mTransitionRelation.getEvent(subStepEventID)
                : null;
        final TraceStepProxy convertedStep =
            factory.createTraceStepProxy(event, stepsStateMap);
        substeps.add(convertedStep);
      }
      return substeps;
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
    private List<SearchRecord> beginTrace(final TIntArrayList initialStateIDs,
                                          final int resultAutInitialStateClass)
    {
      final int[] targetArray = mClassMap.get(resultAutInitialStateClass);
      final TIntHashSet targetSet = new TIntHashSet(targetArray);
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
            mTransitionRelation.getSuccessors(source, mCodeOfTau);
        if (successors != null) {
          final TIntIterator iter = successors.iterator();
          while (iter.hasNext()) {
            final int target = iter.next();
            if (!visited.contains(target)) {
              final SearchRecord record =
                  new SearchRecord(target, false, mCodeOfTau, current);
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
    private List<SearchRecord> completeTrace(final int originalSource)
    {
      if (!getOriginalAutomaton().getEvents()
          .contains(getGeneralisedPrecondition())
          || mOriginalStates[originalSource].getPropositions()
              .contains(getGeneralisedPrecondition())) {
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
            mTransitionRelation.getSuccessors(source, mCodeOfTau);
        if (successors != null) {
          final TIntIterator iter = successors.iterator();
          while (iter.hasNext()) {
            final int target = iter.next();
            if (!visited.contains(target)) {
              record = new SearchRecord(target, false, mCodeOfTau, current);
              if (mOriginalStates[target].getPropositions()
                  .contains(getGeneralisedPrecondition())) {
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
     * Finds a partial trace in the original automaton before observation
     * equivalence. This method computes a sequence of tau transitions, followed
     * by a transition with the given event, followed by another sequence of tau
     * transitions linking the source state to some state in the class of the
     * target state in the simplified automaton.
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
    private List<SearchRecord> findSubTrace(final int originalSource,
                                            final int event,
                                            final int targetClass)
    {
      final int[] targetArray = mClassMap.get(targetClass);
      final TIntHashSet targetSet = new TIntHashSet(targetArray);
      final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
      final TIntHashSet visited0 = new TIntHashSet(); // event not in trace
      final TIntHashSet visited1 = new TIntHashSet(); // event in trace
      // The given event may be tau. In this case, we must search for a
      // (possibly empty) string of tau events. This is achieved here by
      // by creating a first search record with the 'hasevent' property,
      // i.e., pretending the trace already has an event.
      SearchRecord record;
      if (event != mCodeOfTau) {
        record = new SearchRecord(originalSource);
        visited0.add(originalSource);
      } else if (!targetSet.contains(originalSource)) {
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
        TIntHashSet successors =
            mTransitionRelation.getSuccessors(source, mCodeOfTau);
        if (successors != null) {
          final TIntIterator iter = successors.iterator();
          while (iter.hasNext()) {
            final int target = iter.next();
            if (!visited.contains(target)) {
              record = new SearchRecord(target, hasEvent, mCodeOfTau, current);
              if (hasEvent && targetSet.contains(target)) {
                return buildSearchRecordTrace(record);
              }
              open.add(record);
              visited.add(target);
            }
          }
        }
        if (!hasEvent) {
          successors = mTransitionRelation.getSuccessors(source, event);
          if (successors != null) {
            final TIntIterator iter = successors.iterator();
            while (iter.hasNext()) {
              final int target = iter.next();
              if (!visited1.contains(target)) {
                record = new SearchRecord(target, true, event, current);
                if (targetSet.contains(target)) {
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

    private List<SearchRecord> buildSearchRecordTrace(SearchRecord record)
    {
      final List<SearchRecord> trace = new LinkedList<SearchRecord>();
      do {
        trace.add(0, record);
        record = record.getPredecessor();
      } while (record.getPredecessor() != null);
      return trace;
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
     * Maps state codes of the output TransitionRelation to list of state codes
     * in input TransitionRelation. This gives the class of states merged to
     * form the given state in the simplified automaton. Obtained from
     * TransBiSimulator.
     */
    private final TIntObjectHashMap<int[]> mClassMap;
    /**
     * Reverse encoding of output states. Maps states in output automaton
     * (simplified automaton) to state code in output transition relation.
     * Obtained from TransitionRelation.
     */
    private final TObjectIntHashMap<StateProxy> mReverseOutputStateMap;

    private final ObserverProjectionTransitionRelation mTransitionRelation;
    private final int mCodeOfTau;
    private final Map<StateProxy,Integer> mOriginalStatesMap;
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
        if (stepEvent != null) {
          final StateProxy targetState =
              stepsNewStateMap.get(getResultAutomaton());

          stepsNewStateMap.remove(getResultAutomaton());
          final int stateID = mResultingStates.get(targetState);
          final StateProxy replacementState = mOriginalStates[stateID];
          stepsNewStateMap.put(getOriginalAutomaton(), replacementState);
          final TraceStepProxy convertedStep =
              getFactory().createTraceStepProxy(stepEvent, stepsNewStateMap);
          convertedSteps.add(convertedStep);
        } else {
          stepsNewStateMap.remove(getResultAutomaton());
          final TraceStepProxy convertedStep =
              getFactory().createTraceStepProxy(stepEvent, stepsNewStateMap);
          convertedSteps.add(convertedStep);
        }
      }
      final Set<AutomatonProxy> traceAutomata =
          new HashSet<AutomatonProxy>(conflictTrace.getAutomata());
      traceAutomata.remove(getResultAutomaton());
      traceAutomata.add(getOriginalAutomaton());
      final ConflictTraceProxy convertedTrace =
          getFactory().createConflictTraceProxy(conflictTrace.getName(),
                                                conflictTrace.getComment(),
                                                conflictTrace.getLocation(),
                                                getModel(), traceAutomata,
                                                convertedSteps,
                                                ConflictKind.CONFLICT);
      return convertedTrace;
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
   * RemovalOfTauTransitionsLeadingToNonAlphaStatesRule.
   */
  private class RemovalOfTauTransitionsLeadingToNonAlphaStatesStep extends Step
  {
    RemovalOfTauTransitionsLeadingToNonAlphaStatesStep(
                                                       final AutomatonProxy resultAut,
                                                       final AutomatonProxy originalAut,
                                                       final EventProxy tau,
                                                       final ObserverProjectionTransitionRelation tr)
    {
      super(resultAut, originalAut);
      mTR = tr;
      mCodeOfTau = tr.getEventInt(tau);
      mOriginalStates = mTR.getOriginalIntToStateMap();
      mResultingStatesMap = mTR.getResultingStateToIntMap();
      mOriginalStatesMap = mTR.getOriginalStateToIntMap();
    }

    ConflictTraceProxy convertTrace(final ConflictTraceProxy conflictTrace)
    {
      // TODO For later, may also have to consider the case that the
      // simplified automaton does not contain any tau event, and only
      // this rule was used.
      final List<TraceStepProxy> convertedSteps =
          new ArrayList<TraceStepProxy>();
      final List<TraceStepProxy> traceSteps = conflictTrace.getTraceSteps();
      final int originalSourceID;

      // makes the trace begin in the correct initial state
      final TIntArrayList initialStates = mTR.getAllInitialStates();
      final StateProxy tracesInitialState =
          getInitialState(getResultAutomaton(), traceSteps.get(0));
      final List<SearchRecord> initialRecords =
          beginTrace(initialStates, mResultingStatesMap.get(tracesInitialState));
      assert initialRecords.size() > 0;
      final Map<AutomatonProxy,StateProxy> initialStepsStateMap =
          new HashMap<AutomatonProxy,StateProxy>(traceSteps.get(0)
              .getStateMap());
      final List<TraceStepProxy> initialSteps =
          createTraceSteps(initialStepsStateMap, initialRecords);
      convertedSteps.addAll(initialSteps);
      originalSourceID =
          initialRecords.get(initialRecords.size() - 1).getState();
      Map<AutomatonProxy,StateProxy> stepsPrevStateMap =
          new HashMap<AutomatonProxy,StateProxy>(traceSteps.get(0)
              .getStateMap());
      stepsPrevStateMap.remove(getResultAutomaton());

      StateProxy originalSource = mOriginalStates[originalSourceID];
      for (final TraceStepProxy step : traceSteps) {
        final Map<AutomatonProxy,StateProxy> stepsNewStateMap =
            new HashMap<AutomatonProxy,StateProxy>(step.getStateMap());

        final EventProxy stepEvent = step.getEvent();
        if (stepEvent != null) {
          // handles events not in the simplified automaton
          if (getResultAutomaton().getEvents().contains(stepEvent)) {
            final int eventID = mTR.getEventInt(stepEvent);

            final StateProxy resultTargetState =
                stepsNewStateMap.get(getResultAutomaton());
            assert resultTargetState != null;
            stepsNewStateMap.remove(getResultAutomaton());
            final List<SearchRecord> subtrace =
                findSubTrace(mOriginalStatesMap.get(originalSource), eventID,
                             mResultingStatesMap.get(resultTargetState));
            final List<TraceStepProxy> substeps =
                createTraceSteps(stepsPrevStateMap, stepsNewStateMap, subtrace,
                                 stepEvent);
            convertedSteps.addAll(substeps);
            final int subsize = subtrace.size();
            if (subsize > 0) {
              final int originalTargetID = subtrace.get(subsize - 1).getState();
              originalSource = mOriginalStates[originalTargetID];
            }
          } else {
            stepsNewStateMap.remove(getResultAutomaton());
            final TraceStepProxy convertedStep =
                getFactory().createTraceStepProxy(stepEvent, stepsNewStateMap);
            convertedSteps.add(convertedStep);
          }
          stepsPrevStateMap =
              new HashMap<AutomatonProxy,StateProxy>(stepsNewStateMap);
          stepsPrevStateMap.remove(getResultAutomaton());
        }
      }
      // makes the trace end in an alpha state
      final List<SearchRecord> finalSteps =
          completeTrace(mOriginalStatesMap.get(originalSource));
      if (finalSteps.size() > 0) {
        final Map<AutomatonProxy,StateProxy> finalStepsStateMap =
            new HashMap<AutomatonProxy,StateProxy>(traceSteps
                .get(traceSteps.size() - 1).getStateMap());
        final List<TraceStepProxy> substeps =
            createTraceSteps(finalStepsStateMap, finalSteps);
        convertedSteps.addAll(substeps);
      }

      final Set<AutomatonProxy> traceAutomata =
          new HashSet<AutomatonProxy>(conflictTrace.getAutomata());
      traceAutomata.remove(getResultAutomaton());
      traceAutomata.add(getOriginalAutomaton());
      final ConflictTraceProxy convertedTrace =
          getFactory().createConflictTraceProxy(conflictTrace.getName(),
                                                conflictTrace.getComment(),
                                                conflictTrace.getLocation(),
                                                getModel(), traceAutomata,
                                                convertedSteps,
                                                ConflictKind.CONFLICT);
      return convertedTrace;
    }

    /**
     * Given a list of SearchRecord's a list of TraceStepProxy's is created and
     * returned. A TraceStepProxy is created for each SearchRecord.
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
            subStepEventID >= 0 ? mTR.getEvent(subStepEventID) : null;
        if (event != stepEvent && !eventFound) {
          stepStateMap = stepsPrevStateMap;
          stepStateMap.put(getOriginalAutomaton(),
                           mOriginalStates[subStepTargetStateID]);
        } else if (event != stepEvent && eventFound) {
          stepStateMap = stepsNewStateMap;
          stepStateMap.put(getOriginalAutomaton(),
                           mOriginalStates[subStepTargetStateID]);
        } else if (event == stepEvent) {
          eventFound = true;
          stepStateMap = stepsNewStateMap;
          stepStateMap.put(getOriginalAutomaton(),
                           mOriginalStates[subStepTargetStateID]);
        }
        final TraceStepProxy convertedStep =
            factory.createTraceStepProxy(event, stepStateMap);
        substeps.add(convertedStep);
      }
      return substeps;
    }

    private List<TraceStepProxy> createTraceSteps(
                                                  final Map<AutomatonProxy,StateProxy> stepsStateMap,
                                                  final List<SearchRecord> subtrace)
    {
      stepsStateMap.remove(getResultAutomaton());
      final ProductDESProxyFactory factory = getFactory();
      final List<TraceStepProxy> substeps = new LinkedList<TraceStepProxy>();
      for (final SearchRecord subStep : subtrace) {
        final int subStepTargetStateID = subStep.getState();
        stepsStateMap.put(getOriginalAutomaton(),
                          mOriginalStates[subStepTargetStateID]);
        final int subStepEventID = subStep.getEvent();
        final EventProxy event =
            subStepEventID >= 0 ? mTR.getEvent(subStepEventID) : null;
        final TraceStepProxy convertedStep =
            factory.createTraceStepProxy(event, stepsStateMap);
        substeps.add(convertedStep);
      }
      return substeps;
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
    private List<SearchRecord> beginTrace(final TIntArrayList initialStateIDs,
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
        final TIntHashSet successors = mTR.getSuccessors(source, mCodeOfTau);
        if (successors != null) {
          final TIntIterator iter = successors.iterator();
          while (iter.hasNext()) {
            final int target = iter.next();
            if (!visited.contains(target)) {
              final SearchRecord record =
                  new SearchRecord(target, false, mCodeOfTau, current);
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
    private List<SearchRecord> completeTrace(final int originalSource)
    {
      if (!getOriginalAutomaton().getEvents()
          .contains(getGeneralisedPrecondition())
          || mOriginalStates[originalSource].getPropositions()
              .contains(getGeneralisedPrecondition())) {
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
        final TIntHashSet successors = mTR.getSuccessors(source, mCodeOfTau);
        if (successors != null) {
          final TIntIterator iter = successors.iterator();
          while (iter.hasNext()) {
            final int target = iter.next();
            if (!visited.contains(target)) {
              record = new SearchRecord(target, false, mCodeOfTau, current);
              if (mOriginalStates[target].getPropositions()
                  .contains(getGeneralisedPrecondition())) {
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
     * Finds a partial trace in the original automaton before observation
     * equivalence. This method computes a sequence of tau transitions, followed
     * by a transition with the given event, followed by another sequence of tau
     * transitions linking the source state to some state in the class of the
     * target state in the simplified automaton.
     *
     * @param originalSource
     *          State number of the source state in the original automaton.
     * @param event
     *          Integer code of the event to be included in the trace.
     * @param resultTargetState
     *          State number of the state in the simplified automaton (code of
     *          state class).
     * @return List of search records describing the trace from source to
     *         target. The first entry in the list represents the first step
     *         after the source state, with its event and target state. The
     *         final step has a target state in the given target class. Events
     *         in the list can only be tau or the given event.
     */
    private List<SearchRecord> findSubTrace(final int originalSource,
                                            final int event,
                                            final int resultTargetState)
    {
      final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
      final TIntHashSet visited0 = new TIntHashSet(); // event not in trace
      final TIntHashSet visited1 = new TIntHashSet(); // event in trace
      // The given event may be tau. In this case, we must search for a
      // (possibly empty) string of tau events. This is achieved here by
      // by creating a first search record with the 'hasevent' property,
      // i.e., pretending the trace already has an event.
      SearchRecord record;
      if (event != mCodeOfTau) {
        record = new SearchRecord(originalSource);
        visited0.add(originalSource);
      } else if (resultTargetState != originalSource) {
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
        TIntHashSet successors = mTR.getSuccessors(source, mCodeOfTau);
        if (successors != null) {
          final TIntIterator iter = successors.iterator();
          while (iter.hasNext()) {
            final int target = iter.next();
            if (!visited.contains(target)) {
              record = new SearchRecord(target, hasEvent, mCodeOfTau, current);
              if (hasEvent && resultTargetState == target) {
                return buildSearchRecordTrace(record);
              }
              open.add(record);
              visited.add(target);
            }
          }
        }
        if (!hasEvent) {
          successors = mTR.getSuccessors(source, event);
          if (successors != null) {
            final TIntIterator iter = successors.iterator();
            while (iter.hasNext()) {
              final int target = iter.next();
              if (!visited1.contains(target)) {
                record = new SearchRecord(target, true, event, current);
                if (resultTargetState == target) {
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

    private List<SearchRecord> buildSearchRecordTrace(SearchRecord record)
    {
      final List<SearchRecord> trace = new LinkedList<SearchRecord>();
      do {
        trace.add(0, record);
        record = record.getPredecessor();
      } while (record.getPredecessor() != null);
      return trace;
    }

    // #######################################################################
    // # Data Members
    private final ObserverProjectionTransitionRelation mTR;
    private final int mCodeOfTau;
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
    private final TObjectIntHashMap<StateProxy> mResultingStatesMap;
    private final Map<StateProxy,Integer> mOriginalStatesMap;
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
  private Map<EventProxy,Set<AutomatonProxy>> mEventsToAutomata =
      new HashMap<EventProxy,Set<AutomatonProxy>>();
  private List<Step> mModifyingSteps;
  private PreselectingHeuristic mPreselectingHeuristic;
  private List<SelectingHeuristic> mSelectingHeuristics;
  private List<AbstractionRule> mAbstractionRules;
  private Collection<EventProxy> mPropositions;

  // #########################################################################
  // # Class Constants

}
