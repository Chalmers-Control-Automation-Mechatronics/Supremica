//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   ObserverProjectionConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
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

import net.sourceforge.waters.analysis.gnonblocking.Candidate;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.SynchronousProductBuilder;
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


public class ObserverProjectionConflictChecker
  extends AbstractConflictChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict checker without a model or marking proposition.
   */
  public ObserverProjectionConflictChecker
    (final ProductDESProxyFactory factory)
  {
    super(null, factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model is
   * nonblocking.
   * @param model
   *          The model to be checked by this conflict checker.
   * @param factory
   *          Factory used for trace construction.
   */
  public ObserverProjectionConflictChecker
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  /**
   * Creates a new conflict checker to check a particular model for
   * nonblocking.
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
  public ObserverProjectionConflictChecker
    (final ProductDESProxy model,
     final EventProxy marking,
     final ProductDESProxyFactory factory)
  {
    super(model, marking, factory);
  }


  //#########################################################################
  //# Configuration
  public void setMonolithicConflictChecker(final ConflictChecker checker)
  {
    mMonolithicConflictChecker = checker;
  }


  //#########################################################################
  //# Heuristics
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
   *
   * @param heuristic
   */
  public void setSelectingHeuristic(final SelectingHeuristic heuristic)
  {
    mSelectingHeuristics = new ArrayList<SelectingHeuristic>(4);
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
    mSelectingHeuristics.add(new HeuristicDefault());
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
    mSelectingHeuristics.add(new HeuristicDefault());
  }


  //#########################################################################
  //# Invocation
  public boolean run() throws AnalysisException
  {
    setUp();
    mapEventsToAutomata();
    final ProductDESProxyFactory factory = getFactory();
    ProductDESProxy model = getModel();
    final List<AutomatonProxy> remainingAut =
      new ArrayList<AutomatonProxy>(model.getAutomata());

    // TODO: later, need to consider when an automaton is too large to be a
    // candidate and so may not always be left with only one automaton
    loop:
    while (remainingAut.size() > 1) {
      final Collection<Candidate> candidates = findCandidates(model);
      final Candidate candidate;
      switch (candidates.size()) {
      case 0:
        break loop;
      case 1:
        candidate = candidates.iterator().next();
        break;
      default:
        candidate = evaluateCandidates(candidates);
        break;
      }
      // TODO: candidate selection (i.e. heuristics) still need testing
      final CompositionStep step =
        composeSynchronousProduct(candidate);
      mModifyingSteps.add(step);
      final EventProxy tau = step.getHiddenEvent();
      AutomatonProxy autToAbstract = step.getResultAutomaton();
      autToAbstract = applyObservationEquivalence(autToAbstract, tau);
      // Remove the composed automata for this candidate from the set of
      // remaining automata and adds the newly composed candidate.
      remainingAut.removeAll(candidate.getAutomata());
      remainingAut.add(autToAbstract);
      updateEventsToAutomata(autToAbstract, candidate.getAutomata());
      // Updates the current model to find candidates from.
      final Set<EventProxy> composedModelAlphabet =
        getEventsForNewModel(remainingAut);
      model = factory.createProductDESProxy(model.getName(), null, null,
                                            composedModelAlphabet,
                                            remainingAut);
    }

    if (mMonolithicConflictChecker == null) {
      mMonolithicConflictChecker = new NativeConflictChecker(factory);
    }
    final int limit = getNodeLimit();
    mMonolithicConflictChecker.setNodeLimit(limit);
    mMonolithicConflictChecker.setModel(model);
    final EventProxy marking = getMarkingProposition();
    mMonolithicConflictChecker.setMarkingProposition(marking);
    final boolean result = mMonolithicConflictChecker.run();

    if (result) {
      return setSatisfiedResult();
    } else {
      final ConflictTraceProxy trace0 =
        mMonolithicConflictChecker.getCounterExample();
      final ConflictTraceProxy trace1 = expandTrace(trace0);
      return setFailedResult(trace1);
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.ModelAnalyser
  /**
   * Initialises required variables to default values if the user hasn't
   * configured them.
   */
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    if (getMarkingProposition() == null) {
      setMarkingProposition(getUsedMarkingProposition());
    }
    final EventProxy marking = getMarkingProposition();
    mPropositions = Collections.singletonList(marking);
    if (mPreselectingHeuristic == null) {
      final PreselectingHeuristic defaultHeuristic = new HeuristicMinT();
      setPreselectingHeuristic(defaultHeuristic);
    }
    if (mSelectingHeuristics == null) {
      final SelectingHeuristic defaultHeuristic = new HeuristicMaxL();
      setSelectingHeuristic(defaultHeuristic);
    }
    final ProductDESProxyFactory factory = getFactory();
    mSynchronousProductBuilder =
      new MonolithicSynchronousProductBuilder(factory);
    mSynchronousProductBuilder.setPropositions(mPropositions);
    final int limit = getNodeLimit();
    mSynchronousProductBuilder.setNodeLimit(limit);
    mModifyingSteps = new ArrayList<Step>();
  }


  //#########################################################################
  //# Candidate Selection
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
  private void mapEventsToAutomata()
  {
    final ProductDESProxy model = getModel();
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


  //#########################################################################
  //# Abstraction Steps
  /**
   * Builds the synchronous product for a given candidate.
   */
  private CompositionStep composeSynchronousProduct(final Candidate candidate)
    throws AnalysisException
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = candidate.createProductDESProxy(factory);
    final Collection<EventProxy> local = candidate.getLocalEvents();
    final String tauname = "tau:" + des.getName();
    final EventProxy tau =
      factory.createEventProxy(tauname, EventKind.UNCONTROLLABLE, false);
    mSynchronousProductBuilder.setModel(des);
    mSynchronousProductBuilder.addMask(local, tau);
    mSynchronousProductBuilder.run();
    final AutomatonProxy sync =
      mSynchronousProductBuilder.getComputedAutomaton();
    final SynchronousProductStateMap stateMap =
      mSynchronousProductBuilder.getStateMap();
    mSynchronousProductBuilder.clearMask();
    return new CompositionStep(sync, local, tau, stateMap);
  }

  private AutomatonProxy applyObservationEquivalence(final AutomatonProxy aut,
                                                     final EventProxy tau)
  {
    final ObserverProjectionTransitionRelation rel =
        new ObserverProjectionTransitionRelation(aut, mPropositions);
    final int codeOfTau = rel.getEventInt(tau);
    final ObserverProjectionBisimulator bisimulator =
        new ObserverProjectionBisimulator(rel, codeOfTau);
    final boolean modified = bisimulator.run();
    if (modified) {
      final ProductDESProxyFactory factory = getFactory();
      final AutomatonProxy convertedAut = rel.createAutomaton(factory);
      final StateProxy[] inputMap = rel.getOriginalIntToStateMap();
      final Map<Integer,int[]> partition = bisimulator.getStateClasses();
      final TObjectIntHashMap<StateProxy> outputMap =
        rel.getResultingStateToIntMap();
      final ObservationEquivalenceStep step =
          new ObservationEquivalenceStep(convertedAut, aut, tau,
                                         inputMap, partition, outputMap);
      mModifyingSteps.add(step);
      return convertedAut;
    } else {
      return aut;
    }
  }


  //#########################################################################
  //# Trace Computation
  private ConflictTraceProxy expandTrace(final ConflictTraceProxy trace)
  {
    List<TraceStepProxy> traceSteps = getSaturatedTraceSteps(trace);
    final int size = mModifyingSteps.size();
    final ListIterator<Step> iter = mModifyingSteps.listIterator(size);
    while (iter.hasPrevious()) {
      final Step step = iter.previous();
      traceSteps = step.convertTraceSteps(traceSteps);
    }
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    return factory.createConflictTraceProxy(trace.getName(),
                                            trace.getComment(),
                                            trace.getLocation(),
                                            model,
                                            automata,
                                            traceSteps,
                                            trace.getKind());
  }

  /**
   * Fills in the target states in the state maps for each step of the trace
   * for the result automaton.
   */
  private List<TraceStepProxy> getSaturatedTraceSteps
    (final ConflictTraceProxy trace)
  {
    final ProductDESProxyFactory factory = getFactory();
    final Collection<AutomatonProxy> automata = trace.getAutomata();
    final int numAutomata = automata.size();
    final List<TraceStepProxy> steps = trace.getTraceSteps();
    final int numSteps = steps.size();
    final List<TraceStepProxy> convertedSteps =
        new ArrayList<TraceStepProxy>(numSteps);
    final Iterator<TraceStepProxy> iter = steps.iterator();

    final TraceStepProxy firstStep = iter.next();
    final Map<AutomatonProxy,StateProxy> firstMap = firstStep.getStateMap();
    final Map<AutomatonProxy,StateProxy> convertedFirstMap =
      new HashMap<AutomatonProxy,StateProxy>(numAutomata);
    for (final AutomatonProxy aut : automata) {
      final StateProxy state = getInitialState(aut, firstMap);
      convertedFirstMap.put(aut, state);
    }
    final TraceStepProxy convertedFirstStep =
      factory.createTraceStepProxy(null, convertedFirstMap);
    convertedSteps.add(convertedFirstStep);
    Map<AutomatonProxy,StateProxy> previousStepMap = convertedFirstMap;
    while (iter.hasNext()) {
      final TraceStepProxy step = iter.next();
      final EventProxy event = step.getEvent();
      final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
      final Map<AutomatonProxy,StateProxy> convertedStepMap =
        new HashMap<AutomatonProxy,StateProxy>(numAutomata);
      for (final AutomatonProxy aut : automata) {
        final StateProxy prev = previousStepMap.get(aut);
        final StateProxy state = findSuccessor(aut, event, prev, stepMap);
        convertedStepMap.put(aut, state);
      }
      final TraceStepProxy convertedStep =
        factory.createTraceStepProxy(event, convertedStepMap);
      convertedSteps.add(convertedStep);
      previousStepMap = convertedStepMap;
    }
    return convertedSteps;
  }

  /**
   * Finds the initial state of an automaton in a trace.
   * A trace step's map is passed for the case of multiple initial states.
   */
  private StateProxy getInitialState
    (final AutomatonProxy aut, final Map<AutomatonProxy,StateProxy> stepMap)
  {
    // If there is more than one initial state, the trace has the info.
    StateProxy initial = stepMap.get(aut);
    // Otherwise there is only one initial state.
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

  /**
   * Finds the successor state in trace, from a given state in an automaton.
   * A trace step's map is passed for the case of multiple successor states.
   */
  private StateProxy findSuccessor(final AutomatonProxy aut,
                                   final EventProxy event,
                                   final StateProxy sourceState,
                                   final Map<AutomatonProxy,StateProxy> stepMap)
  {
    // If there is more than one successor state, the trace has the info.
    final StateProxy targetState = stepMap.get(aut);
    // Otherwise there is only one successor state.
    if (targetState == null) {
      if (aut.getEvents().contains(event)) {
        for (final TransitionProxy trans : aut.getTransitions()) {
          if (trans.getEvent() == event && trans.getSource() == sourceState) {
            return trans.getTarget();
          }
        }
      } else {
        return sourceState;
      }
    }
    return targetState;
  }


  //#########################################################################
  //# Local Interface PreselectingHeuristic
  private interface PreselectingHeuristic
  {
    public Collection<Candidate> evaluate(final ProductDESProxy model);

  }


  //#########################################################################
  //# Inner Class HeuristicPairing
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
          pair.add(a);
          pair.add(chosenAut);
          final Set<EventProxy> localEvents =
              identifyLocalEvents(mEventsToAutomata, pair);
          final Candidate candidate = new Candidate(pair, localEvents);
          candidates.add(candidate);
        }
      }
      return candidates;
    }
  }


  //#########################################################################
  //# Inner Class HeuristicPairing
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


  //#########################################################################
  //# Inner Class HeuristicPairing
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


  //#########################################################################
  //# Inner Class HeuristicPairing
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
        if (automata.size() > 1) {
          final Set<EventProxy> localEvents =
              identifyLocalEvents(mEventsToAutomata, automata);
          final Candidate candidate = new Candidate(automata, localEvents);
          candidates.add(candidate);
        }
      }
      return candidates;
    }
  }


  //#########################################################################
  //# Inner Class HeuristicPairing
  private interface SelectingHeuristic
  {
    public List<Candidate> evaluate(final List<Candidate> candidates);

  }


  //#########################################################################
  //# Inner Class HeuristicPairing
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


  //#########################################################################
  //# Inner Class HeuristicPairing
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


  //#########################################################################
  //# Inner Class HeuristicPairing
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


  //#########################################################################
  //# Inner Class HeuristicPairing
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


  //#########################################################################
  //# Inner Class Step
  private abstract class Step
  {

    //#######################################################################
    //# Constructors
    Step(final AutomatonProxy aut, final Collection<AutomatonProxy> originals)
    {
      mResultAutomaton = aut;
      mOriginalAutomata = originals;
    }

    Step(final AutomatonProxy resultAut, final AutomatonProxy originalAut)
    {
      this(resultAut, Collections.singletonList(originalAut));
    }

    //#######################################################################
    //# Simple Access
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

    //#######################################################################
    //# Trace Computation
    /**
     * Assumes that a saturated trace is being passed.
     */
    abstract List<TraceStepProxy> convertTraceSteps
      (final List<TraceStepProxy> steps);

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mResultAutomaton;
    private final Collection<AutomatonProxy> mOriginalAutomata;

  }


  // #########################################################################
  // # Inner Class CompositionStep
  private class CompositionStep extends Step
  {

    //#######################################################################
    //# Constructor
    private CompositionStep(final AutomatonProxy composedAut,
                            final Collection<EventProxy> localEvents,
                            final EventProxy tau,
                            final SynchronousProductStateMap stateMap)
    {
      super(composedAut, stateMap.getInputAutomata());
      mLocalEvents = localEvents;
      mHiddenEvent = tau;
      mStateMap = stateMap;
    }

    //#######################################################################
    //# Simple Access
    EventProxy getHiddenEvent()
    {
      return mHiddenEvent;
    }

    //#######################################################################
    //# Trace Computation
    List<TraceStepProxy> convertTraceSteps(final List<TraceStepProxy> steps)
    {
      final ProductDESProxyFactory factory = getFactory();
      final AutomatonProxy resultAutomaton = getResultAutomaton();
      final Collection<AutomatonProxy> originalAutomata = getOriginalAutomata();
      final int convertedNumAutomata =
        steps.iterator().next().getStateMap().size() +
        originalAutomata.size() - 1;
      final int numSteps = steps.size();
      final List<TraceStepProxy> convertedSteps =
          new ArrayList<TraceStepProxy>(numSteps);
      Map<AutomatonProxy,StateProxy> previousMap = null;
      for (final TraceStepProxy step : steps) {
        final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
        final Map<AutomatonProxy,StateProxy> convertedStepMap =
          new HashMap<AutomatonProxy,StateProxy>(convertedNumAutomata);
        convertedStepMap.putAll(stepMap);
        convertedStepMap.remove(resultAutomaton);
        final StateProxy convertedState = stepMap.get(resultAutomaton);
        for (final AutomatonProxy aut : originalAutomata) {
          final StateProxy originalState =
            mStateMap.getOriginalState(convertedState, aut);
          convertedStepMap.put(aut, originalState);
        }
        EventProxy event = step.getEvent();
        if (event == mHiddenEvent) {
          event = findEvent(previousMap, convertedStepMap);
        }
        final TraceStepProxy convertedStep =
          factory.createTraceStepProxy(event, convertedStepMap);
        convertedSteps.add(convertedStep);
        previousMap = convertedStepMap;
      }
      return convertedSteps;
    }

    private EventProxy findEvent(final Map<AutomatonProxy,StateProxy> sources,
                                 final Map<AutomatonProxy,StateProxy> targets)
    {
      final Collection<EventProxy> possible =
        new LinkedList<EventProxy>(mLocalEvents);
      for (final AutomatonProxy aut : getOriginalAutomata()) {
        if (possible.size() <= 1) {
          break;
        }
        final StateProxy source = sources.get(aut);
        final StateProxy target = targets.get(aut);
        final Collection<EventProxy> alphabet =
          new THashSet<EventProxy>(aut.getEvents());
        final int size = alphabet.size();
        final Collection<EventProxy> retained = new THashSet<EventProxy>(size);
        for (final TransitionProxy trans : aut.getTransitions()) {
          if (trans.getSource() == source && trans.getTarget() == target) {
            final EventProxy event = trans.getEvent();
            retained.add(event);
          }
        }
        final Iterator<EventProxy> iter = possible.iterator();
        while (iter.hasNext()) {
          final EventProxy event = iter.next();
          if (alphabet.contains(event)) {
            if (!retained.contains(event)) {
              iter.remove();
            }
          } else {
            if (source != target) {
              iter.remove();
            }
          }
        }
      }
      return possible.iterator().next();
    }

    //#######################################################################
    //# Data Members
    private final Collection<EventProxy> mLocalEvents;
    private final EventProxy mHiddenEvent;
    private final SynchronousProductStateMap mStateMap;
  }


  //#########################################################################
  //# Inner Class ObservationEquivalenceStep
  private class ObservationEquivalenceStep extends Step
  {

    //#######################################################################
    //# Constructor
    private ObservationEquivalenceStep
      (final AutomatonProxy resultAut,
       final AutomatonProxy originalAut,
       final EventProxy tau,
       final StateProxy[] originalStates,
       final Map<Integer,int[]> classMap,
       final TObjectIntHashMap<StateProxy> reverseOutputStateMap)
    {
      super(resultAut, originalAut);
      mOriginalStates = originalStates;
      mClassMap = classMap;
      mReverseOutputStateMap = reverseOutputStateMap;
      mTransitionRelation =
        new ObserverProjectionTransitionRelation(originalAut, mPropositions);
      mCodeOfTau = mTransitionRelation.getEventInt(tau);
    }

    //#######################################################################
    //# Trace Computation
    List<TraceStepProxy> convertTraceSteps
      (final List<TraceStepProxy> traceSteps)
    {
      final AutomatonProxy resultAutomaton = getResultAutomaton();
      final List<TraceStepProxy> convertedSteps =
        new LinkedList<TraceStepProxy>();

      // Make the trace begin in the correct initial state ...
      final TIntArrayList initialStates =
        mTransitionRelation.getAllInitialStates();
      final Iterator<TraceStepProxy> iter = traceSteps.iterator();
      final TraceStepProxy initialStep = iter.next();
      Map<AutomatonProxy,StateProxy> stepsNewStateMap =
        new HashMap<AutomatonProxy,StateProxy>(initialStep.getStateMap());
      final StateProxy tracesInitialState =
        stepsNewStateMap.get(resultAutomaton);
      stepsNewStateMap.remove(resultAutomaton);
      final int tracesInitialStateID =
        mReverseOutputStateMap.get(tracesInitialState);
      final List<SearchRecord> initialRecords =
        beginTrace(initialStates, tracesInitialStateID);
      assert initialRecords.size() > 0;
      appendTraceSteps(initialRecords, stepsNewStateMap, convertedSteps);

      // Append internal steps, with intermittent tau as needed ...
      int originalSourceID =
        initialRecords.get(initialRecords.size() - 1).getState();
      while (iter.hasNext()) {
        final TraceStepProxy step = iter.next();
        stepsNewStateMap =
          new HashMap<AutomatonProxy,StateProxy>(step.getStateMap());
        final EventProxy stepEvent = step.getEvent();
        final StateProxy resultTargetState =
          stepsNewStateMap.get(resultAutomaton);
        assert resultTargetState != null;
        stepsNewStateMap.remove(resultAutomaton);
        final List<SearchRecord> subtrace =
          findSubTrace(originalSourceID,
                       mTransitionRelation.getEventInt(stepEvent),
                       mReverseOutputStateMap.get(resultTargetState));
        appendTraceSteps(subtrace, stepsNewStateMap, convertedSteps);
        final int subsize = subtrace.size();
        if (subsize > 0) {
          originalSourceID = subtrace.get(subsize - 1).getState();
        }
      }

      return convertedSteps;
    }

    /**
     * Creates the beginning of a trace by doing a breadth-first search to find
     * the correct initial state of the original automaton. Steps are added for
     * tau transitions (if necessary) until the initial state of the result
     * automaton is reached.
     *
     * @return A list of SearchRecords that represent each extra step needed
     *         for the start of the trace. (The first item being the very first
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

    /**
     * Given a list of SearchRecord objects, a list of {@link TraceStepProxy}
     * objects is created and appended to a given list.
     * A {@link TraceStepProxy} is created for each SearchRecord.
     * @param recordTrace
     *          The list of search records to convert into steps of a trace.
     * @param stepsStateMap
     *          The state map for the step before adding the new information.
     * @param outputTrace
     *          Trace steps created are appended to this list.
     */
    private void appendTraceSteps
      (final List<SearchRecord> recordTrace,
       final Map<AutomatonProxy,StateProxy> stepsStateMap,
       final List<TraceStepProxy> outputTrace)
    {
      final ProductDESProxyFactory factory = getFactory();
      final AutomatonProxy originalAutomaton = getOriginalAutomaton();
      for (final SearchRecord record : recordTrace) {
        final int subStepTargetStateID = record.getState();
        stepsStateMap.put(originalAutomaton,
                          mOriginalStates[subStepTargetStateID]);
        final int subStepEventID = record.getEvent();
        final EventProxy event;
        if (subStepEventID >= 0) {
          event = mTransitionRelation.getEvent(subStepEventID);
        } else {
          event = null;
        }
        final TraceStepProxy convertedStep =
          factory.createTraceStepProxy(event, stepsStateMap);
        outputTrace.add(convertedStep);
      }
    }

    //#######################################################################
    //# Data Members
    /**
     * Array of original states. Maps state codes in the input
     * transition relation to state objects in the input automaton.
     */
    private final StateProxy[] mOriginalStates;
    /**
     * Maps state codes of the output transition relation to list of state codes
     * in input transition relation. This gives the class of states merged to
     * form the given state in the simplified automaton.
     */
    private final Map<Integer,int[]> mClassMap;
    /**
     * Reverse encoding of output states. Maps states in output automaton
     * (simplified automaton) to state code in output transition relation.
     */
    private final TObjectIntHashMap<StateProxy> mReverseOutputStateMap;

    private final ObserverProjectionTransitionRelation mTransitionRelation;
    private final int mCodeOfTau;
  }


  // #########################################################################
  // # Inner Class SearchRecord
  private static class SearchRecord
  {

    //#######################################################################
    //# Constructors
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

    //#######################################################################
    //# Getters
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

    //#######################################################################
    //# Data Members
    private final int mState;
    private final boolean mHasProperEvent;
    private final int mEvent;
    private final SearchRecord mPredecessor;
  }


  //#########################################################################
  //# Data Members
  private Collection<EventProxy> mPropositions;
  private SynchronousProductBuilder mSynchronousProductBuilder;
  private ConflictChecker mMonolithicConflictChecker;

  private Map<EventProxy,Set<AutomatonProxy>> mEventsToAutomata =
      new HashMap<EventProxy,Set<AutomatonProxy>>();
  private List<Step> mModifyingSteps;
  private PreselectingHeuristic mPreselectingHeuristic;
  private List<SelectingHeuristic> mSelectingHeuristics;

}
