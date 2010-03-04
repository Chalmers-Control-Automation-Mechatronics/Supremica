//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   CompositionalGeneralisedConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.MonolithicConflictChecker;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.SynchronousProductStateMap;
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
    AbstractConflictChecker implements ConflictChecker
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
    if (getMarkingProposition() == null) {
      setMarkingProposition(getUsedMarkingProposition());
    }
    ProductDESProxy model = getModel();
    mModifyingSteps = new ArrayList<Step>();
    final List<SynchronousProductStateMap> stateMaps =
        new ArrayList<SynchronousProductStateMap>();
    final List<AutomatonProxy> composedAut = new ArrayList<AutomatonProxy>();
    mapEventsToAutomata(model);
    final Set<AutomatonProxy> remainingAut =
        new HashSet<AutomatonProxy>(model.getAutomata());

    // TODO: later, need to consider when an automaton is too large to be a
    // candidate and so may not always be left with only one automaton
    while (remainingAut.size() > 1) {
      final Collection<Candidate> candidates = findCandidates(model);
      final Candidate candidate = evaluateCandidates(candidates);

      final NonDeterministicComposer composer =
          composeSynchronousProduct(candidate);
      final AutomatonProxy syncProduct = composer.run();
      stateMaps.add(composer.getStateMap());
      composedAut.add(syncProduct);
      final CompositionStep step =
          new CompositionStep(syncProduct, composer.getStateMap());
      mModifyingSteps.add(step);

      final Set<EventProxy> localEvents =
          identifyLocalEvents(mEventsToAutomata, candidate.getAutomata());
      candidate.setLocalEvents(localEvents);
      final AutomatonProxy autToAbstract =
          hideLocalEvents(syncProduct, localEvents);
      // final AutomatonProxy autToAbstract = syncProduct;

      // TODO Abstraction rules here

      // removes the composed automata for this candidate from the set of
      // remaining automata and adds the newly composed candidate
      remainingAut.removeAll(candidate.getAutomata());
      remainingAut.add(autToAbstract);

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
        new MonolithicConflictChecker(model, getMarkingProposition(),
            getGeneralisedPrecondition(), getFactory());
    final boolean result = checker.run();

    if (result) {
      setSatisfiedResult();
    } else {
      final ConflictTraceProxy counterexample = checker.getCounterExample();
      ConflictTraceProxy convertedTrace = counterexample;
      final int size = mModifyingSteps.size();
      final ListIterator<Step> iter = mModifyingSteps.listIterator(size);
      while (iter.hasPrevious()) {
        final Step step = iter.previous();
        convertedTrace = step.convertTrace(convertedTrace);
      }
      setFailedResult(convertedTrace);
    }
    return result;
  }

  /**
   * Builds the synchronous product for a given candidate.
   *
   * @param candidate
   *
   * @return
   *
   * @throws AnalysisException
   */
  private NonDeterministicComposer composeSynchronousProduct(
                                                             final Candidate candidate)
      throws AnalysisException
  {
    // creates a model which includes only the candidate, to build the
    // synchronous product of
    final Set<EventProxy> candidateEvents =
        getEventsForNewModel(candidate.getAutomata());
    final ProductDESProxy candidateModel =
        getFactory().createProductDESProxy("Candidate model", candidateEvents,
                                           candidate.getAutomata());

    final NonDeterministicComposer composer =
        new NonDeterministicComposer(new ArrayList<AutomatonProxy>(
            candidateModel.getAutomata()), getFactory(),
            getMarkingProposition(), getGeneralisedPrecondition());
    return composer;
  }

  /**
   * Hides the local events for a given candidate (replaces the events with a
   * silent event "tau").
   *
   * @param syncProduct
   */
  private AutomatonProxy hideLocalEvents(final AutomatonProxy automaton,
                                         final Set<EventProxy> localEvents)
  {
    final String tauStateName = "tau:" + automaton.getName();
    final EventProxy tau =
        getFactory().createEventProxy(tauStateName, EventKind.UNCONTROLLABLE);

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
   *
   * @param automataOfNewModel
   * @return
   */
  private Set<EventProxy> getEventsForNewModel(
                                               final Set<AutomatonProxy> automataOfNewModel)
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

  /**
   * <P>
   * Finds the set of events that are local to a candidate (i.e. a set of
   * automata).
   * </P>
   */
  private Set<EventProxy> identifyLocalEvents(
                                              final Map<EventProxy,Set<AutomatonProxy>> eventAutomaton,
                                              final Set<AutomatonProxy> candidate)
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
   *
   * @param candidates
   * @return
   */
  private Candidate evaluateCandidates(final Collection<Candidate> candidates)
  {
    // returns one random candidate initially
    return candidates.iterator().next();
    // TODO: needs proper implementation
    /*
     * final ListIterator<SelectingHeuristic> iter =
     * mSelectingHeuristics.listIterator(); List<Candidate> selectedCandidates =
     * null; while (iter.hasNext()) { final SelectingHeuristic heuristic =
     * iter.next(); selectedCandidates = heuristic.evaluate((List<Candidate>)
     * candidates); if (selectedCandidates.size() == 1) { break; } else {
     * candidates = new ArrayList<Candidate>(selectedCandidates); } } return
     * selectedCandidates.get(0);
     */
  }

  /**
   * Finds the set of candidates to compose for a given model.
   *
   * @param model
   * @return
   */
  private Collection<Candidate> findCandidates(final ProductDESProxy model)
  {
    // initially all automaton are in a set as a candidate
    final Candidate candidate = new Candidate(model.getAutomata());
    final Collection<Candidate> candidates = new HashSet<Candidate>();
    candidates.add(candidate);
    return candidates;
    // TODO: needs proper implementation
    // return mPreselectingHeuristic.evaluate(model);
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

  public void setPreselctingHeuristic(final PreselectingHeuristic heuristic)
  {
    mPreselectingHeuristic = heuristic;
  }

  /**
   * The given heuristic is used first to select a candidate to compose.
   *
   * @param heuristic
   */
  public void setSelectingHeuristic(final SelectingHeuristic heuristic)
  {
    mSelectingHeuristics = new ArrayList<SelectingHeuristic>(3);
    mSelectingHeuristics.add(heuristic);
    if (heuristic instanceof HeuristicMaxL) {
      mSelectingHeuristics.add(new HeuristicMaxC());
      mSelectingHeuristics.add(new HeuristicMinS());
    } else if (heuristic instanceof HeuristicMaxS) {
      mSelectingHeuristics.add(new HeuristicMaxL());
      mSelectingHeuristics.add(new HeuristicMinS());
    } else if (heuristic instanceof HeuristicMinS) {
      mSelectingHeuristics.add(new HeuristicMaxL());
      mSelectingHeuristics.add(new HeuristicMaxC());
    }
  }

  /**
   * The first item in the list should be the first heuristic used to select a
   * candidate to compose, the last item in the list should be the last option.
   *
   * @param heuristicList
   */
  public void setSelectingHeuristic(final List<SelectingHeuristic> heuristicList)
  {
    mSelectingHeuristics = heuristicList;
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
      final Set<AutomatonProxy> aut = new HashSet<AutomatonProxy>(automata);
      aut.remove(chosenAut);
      final Collection<Candidate> candidates =
          new HashSet<Candidate>(aut.size());
      for (final AutomatonProxy a : aut) {
        final Set<AutomatonProxy> pair = new HashSet<AutomatonProxy>(2);
        pair.add(a);
        pair.add(chosenAut);
        final Candidate candidate = new Candidate(pair);
        candidates.add(candidate);
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
      final HashMap<EventProxy,Candidate> eventCandidates =
          new HashMap<EventProxy,Candidate>(mEventsToAutomata.keySet().size());
      for (final EventProxy event : mEventsToAutomata.keySet()) {
        final Set<AutomatonProxy> automata = new HashSet<AutomatonProxy>();
        final Candidate candidate = new Candidate(automata);
        eventCandidates.put(event, candidate);
        for (final AutomatonProxy aut : mEventsToAutomata.get(event)) {
          eventCandidates.get(event).getAutomata().add(aut);
        }
      }
      return eventCandidates.values();
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
      int maxLocal =
          chosenCandidate.getLocalEventCount()
              / chosenCandidate.getNumberOfEvents();
      while (it.hasNext()) {
        final Candidate nextCan = it.next();
        final int proportion =
            nextCan.getLocalEventCount() / nextCan.getNumberOfEvents();
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
      final Iterator<Candidate> it = candidates.iterator();
      List<Candidate> chosenCandidates = new ArrayList<Candidate>();
      Candidate chosenCandidate = it.next();
      int maxCommon =
          (chosenCandidate.getNumberOfEvents() - chosenCandidate
              .getLocalEventCount())
              / chosenCandidate.getNumberOfEvents();
      while (it.hasNext()) {
        final Candidate nextCan = it.next();
        final int proportion =
            (nextCan.getNumberOfEvents() - nextCan.getLocalEventCount())
                / nextCan.getNumberOfEvents();
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


  // #########################################################################
  // # Inner Class Step
  private abstract class Step
  {

    // #######################################################################
    // # Constructor
    Step(final AutomatonProxy aut)
    {
      mResultAutomaton = aut;
    }

    Step(final AutomatonProxy resultAut, final AutomatonProxy originalAut)
    {
      mResultAutomaton = resultAut;
      mOriginalAutomaton = originalAut;
    }

    // #######################################################################
    // # Simple Access
    AutomatonProxy getResultAutomaton()
    {
      return mResultAutomaton;
    }

    AutomatonProxy getOriginalAutomaton()
    {
      return mOriginalAutomaton;
    }

    // #######################################################################
    // # Trace Computation
    abstract ConflictTraceProxy convertTrace(
                                             final ConflictTraceProxy counterexample);

    // #######################################################################
    // # Data Members
    private final AutomatonProxy mResultAutomaton;
    private AutomatonProxy mOriginalAutomaton;

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
      super(composedAut);
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
      StateProxy sourceState = getInitialState(getOriginalAutomaton());
      for (final TraceStepProxy step : traceSteps) {
        // replaces automaton in step's step map
        final Map<AutomatonProxy,StateProxy> stepStateMap = step.getStateMap();
        final Map<AutomatonProxy,StateProxy> stepsNewStateMap =
            new HashMap<AutomatonProxy,StateProxy>(stepStateMap.size());
        if (stepStateMap.containsKey(getResultAutomaton())) {
          stepsNewStateMap.put(getOriginalAutomaton(), stepStateMap
              .get(getResultAutomaton()));

        }
        final EventProxy stepEvent = step.getEvent();
        final Map<StateProxy,TransitionProxy> successors =
            getSuccessorStates(sourceState, stepEvent);
        final Iterator<StateProxy> it = successors.keySet().iterator();

        // replaces tau events with original event before hiding
        if (stepEvent == mTau) {
          final StateProxy[] targetStates =
              stepStateMap.values().toArray(new StateProxy[0]);
          final StateProxy targetState = targetStates[0];
          for (final StateProxy succ : successors.keySet()) {
            if (succ == targetState) {
              final TraceStepProxy convertedStep =
                  getFactory().createTraceStepProxy(
                                                    successors.get(succ)
                                                        .getEvent(),
                                                    stepsNewStateMap);
              convertedSteps.add(convertedStep);
              sourceState = targetState;
              break;
            }
          }
        } else {
          convertedSteps.add(step);
        }
        if (it.hasNext()) {
          sourceState = (StateProxy) it.next();
        }
      }

      final Set<AutomatonProxy> traceAutomata = new HashSet<AutomatonProxy>(1);
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

    private StateProxy getInitialState(final AutomatonProxy aut)
    {// TODO: currently only works for one initial state
      StateProxy initial = null;
      for (final StateProxy state : aut.getStates()) {
        if (state.isInitial()) {
          initial = state;
          break;
        }
      }
      return initial;
    }

    private Map<StateProxy,TransitionProxy> getSuccessorStates(
                                                               final StateProxy currentState,
                                                               final EventProxy event)
    {
      final Map<StateProxy,TransitionProxy> successor =
          new HashMap<StateProxy,TransitionProxy>();
      for (final TransitionProxy transition : getOriginalAutomaton()
          .getTransitions()) {
        if (transition.getEvent() == event
            && transition.getSource() == currentState) {
          successor.put(transition.getTarget(), transition);
        }
      }
      return successor;
    }

    // #######################################################################
    // # Data Members
    private final EventProxy mTau;
  }

  // #########################################################################
  // # Data Members
  private Map<EventProxy,Set<AutomatonProxy>> mEventsToAutomata =
      new HashMap<EventProxy,Set<AutomatonProxy>>();
  private List<Step> mModifyingSteps;
  @SuppressWarnings("unused")
  private PreselectingHeuristic mPreselectingHeuristic;
  private List<SelectingHeuristic> mSelectingHeuristics;

  // #########################################################################
  // # Class Constants

}
