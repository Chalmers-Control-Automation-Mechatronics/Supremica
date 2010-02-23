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
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.waters.analysis.monolithic.MonolithicConflictChecker;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.EventKind;


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
    mapEventsToAutomata(model);
    final Set<AutomatonProxy> remainingAut =
        new HashSet<AutomatonProxy>(model.getAutomata());

    // TODO: later, need to consider when an automaton is too large to be a
    // candidate and so may not always be left with only one automaton
    while (remainingAut.size() > 1) {
      final Set<Candidate> candidates = findCandidates(model);
      final Candidate candidate = evaluateCandidates(candidates);

      final AutomatonProxy syncProduct = composeSynchronousProduct(candidate);

      final Set<EventProxy> localEvents =
          identifyLocalEvents(mEventsToAutomata, candidate.getAutomata());
      candidate.setLocalEvents(localEvents);
      // TODO: currently the candidate is changed and the original form is not
      // stored
      final AutomatonProxy autToAbstract =
          hideLocalEvents(syncProduct, localEvents);

      // TODO Abstraction rules here

      // removes the composed automata for this candidate from the set of
      // remaining automata and adds the newly composed candidate
      remainingAut.removeAll(candidate.getAutomata());
      remainingAut.add(autToAbstract);

      // updates the current model to find candidates from
      final Set<EventProxy> composedModelAlphabet =
          getEventsForNewModel(remainingAut);
      model =
          getFactory().createProductDESProxy("Composed Model",
                                             composedModelAlphabet,
                                             remainingAut);
    }
    final ConflictChecker checker =
        new MonolithicConflictChecker(model, getMarkingProposition(),
            getGeneralisedPrecondition(), getFactory());
    System.out.println(model);
    final boolean result = checker.run();

    if (result) {
      setSatisfiedResult();
    } else {
      // TODO: counterexample needs to be for original model
      final TraceProxy counterexample = checker.getCounterExample();
      setFailedResult(counterexample);
    }
    return result;
  }

  /**
   * Builds the synchronous product for a given candidate.
   *
   * @param candidate
   * @return
   * @throws AnalysisException
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

    final NonDeterministicComposer composer =
        new NonDeterministicComposer(candidateModel,
            new ArrayList<AutomatonProxy>(candidateModel.getAutomata()),
            getFactory(), getMarkingProposition(), getGeneralisedPrecondition());
    return composer.run();
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
    final EventProxy tau =
        getFactory().createEventProxy("tau", EventKind.UNCONTROLLABLE);

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
    System.out.println(newAut);
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
  private Candidate evaluateCandidates(final Set<Candidate> candidates)
  {
    // returns one random candidate initially
    return candidates.iterator().next();
    // TODO: needs proper implementation
  }

  /**
   * Finds the set of candidates to compose for a given model.
   *
   * @param model
   * @return
   */
  private Set<Candidate> findCandidates(final ProductDESProxy model)
  {
    // initially all automaton are in a set as a candidate
    final Candidate candidate = new Candidate(model.getAutomata(), 1);
    final Set<Candidate> candidates = new HashSet<Candidate>();
    candidates.add(candidate);
    return candidates;
    // TODO: needs proper implementation
  }


  private interface Heuristic
  {
    public Collection<Candidate> evaluate(final ProductDESProxy model);

  }


  @SuppressWarnings("unused")
  private class HeuristicMustL implements Heuristic
  {
    /**
     * There is a candidate for every event in the model, each candidate
     * contains the set of automaton which use that event.
     */
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


  @SuppressWarnings("unused")
  private class HeuristicMinS implements Heuristic
  {
    public Candidate evaluate(final List<Candidate> candidates)
    {
      Candidate chosenCandidate = candidates.get(0);
      int smallestProduct = calculateProduct(chosenCandidate);
      for (int i = 1; i < candidates.size(); i++) {
        final Candidate candidate = candidates.get(i);
        final int newproduct = calculateProduct(candidate);
        if (smallestProduct > newproduct) {
          smallestProduct = newproduct;
          chosenCandidate = candidate;
        }
      }
      return chosenCandidate;
    }

    private int calculateProduct(final Candidate candidate)
    {
      int smallestProduct = 1;
      for (final AutomatonProxy aut : candidate.getAutomata()) {
        smallestProduct *= aut.getStates().size();
      }
      return smallestProduct;
    }

    public Collection<Candidate> evaluate(final ProductDESProxy model)
    {
      // TODO Auto-generated method stub
      return null;
    }

  }

  // #########################################################################
  // # Data Members
  private Map<EventProxy,Set<AutomatonProxy>> mEventsToAutomata =
      new HashMap<EventProxy,Set<AutomatonProxy>>();

  // #########################################################################
  // # Class Constants

}
