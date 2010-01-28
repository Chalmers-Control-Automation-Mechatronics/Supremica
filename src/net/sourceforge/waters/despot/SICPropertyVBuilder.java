package net.sourceforge.waters.despot;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;
import net.sourceforge.waters.despot.HISCAttributes;


public class SICPropertyVBuilder
{

  public SICPropertyVBuilder(final ProductDESProxyFactory factory)
  {
    mFactory = factory;
    mModel = null;
  }

  public SICPropertyVBuilder(final ProductDESProxy model,
                             final ProductDESProxyFactory factory)
  {
    mModel = model;
    mFactory = factory;
  }

  /**
   * Sets the model which is having SIC Property V checked.
   *
   * @param model
   */
  public void setInputModel(final ProductDESProxy model)
  {
    mModel = model;
    mLowLevelAutomata = null;
  }

  public ProductDESProxy getUnchangedModel()
  {
    return mModel;
  }

  public void setMarkingProposition(final EventProxy marking)
  {
    mMarking = marking;
  }

  public EventProxy getMarkingProposition()
  {
    return mMarking;
  }

  public void setGeneralisedPrecondition(final EventProxy marking)
  {
    mPreconditionMarking = marking;
  }

  public EventProxy getGeneralisedPrecondition()
  {
    return mPreconditionMarking;
  }

  /**
   * Gets all the answer events that belong to the model.
   */
  public Collection<EventProxy> getAnswerEvents()
  {
    final Set<EventProxy> allEvents = mModel.getEvents();
    final List<EventProxy> answerEvents = new ArrayList<EventProxy>(0);
    for (final EventProxy event : allEvents) {
      if (HISCAttributes.getEventType(event.getAttributes()) == HISCAttributes.EventType.ANSWER) {
        answerEvents.add(event);
      }
    }
    return answerEvents;
  }

  /**
   * Builds a model for a given answer event.
   *
   * @param answer
   *          The name of the answer event.
   */
  public ProductDESProxy createModelForAnswer(final EventProxy answer)
  {
    if (mMarking == null) {
      mMarking =
          mFactory.createEventProxy(EventDeclProxy.DEFAULT_MARKING_NAME,
                                    EventKind.PROPOSITION, true);
    }
    if (mPreconditionMarking == null) {
      final String alphaNm = ":alpha";
      mPreconditionMarking =
          mFactory.createEventProxy(alphaNm, EventKind.PROPOSITION, true);
    }
    List<AutomatonProxy> newAutomaton = new ArrayList<AutomatonProxy>();
    // the low level automaton of a model only need modifying once because they
    // don't depend on the answer event
    if (mLowLevelAutomata == null) {
      mLowLevelAutomata = new ArrayList<AutomatonProxy>();
      for (final AutomatonProxy aut : mModel.getAutomata()) {
        if (!HISCAttributes.isInterface(aut.getAttributes())) {
          newAutomaton.add(createModifiedLowLevelAutomaton(aut));

        }
      }
    } else {
      newAutomaton = new ArrayList<AutomatonProxy>(mLowLevelAutomata);
    }
    // modifies the models interfaces dependent on the answer event specified
    for (final AutomatonProxy aut : mModel.getAutomata()) {
      if (HISCAttributes.isInterface(aut.getAttributes())) {
        newAutomaton.add(createModifiedInterfaceAutomaton(aut, answer));
      }
    }
    newAutomaton.add(createTestForAnswer(answer));

    // removes markings from automaton event alphabet
    final List<EventProxy> newEvents =
        removeAlphabetMarkings(mModel.getEvents());
    newEvents.add(mMarking);
    newEvents.add(mPreconditionMarking);

    final ProductDESProxy newModel =
        mFactory.createProductDESProxy(mModel.getName(), mModel.getComment(),
                                       mModel.getLocation(), newEvents,
                                       newAutomaton);
    return newModel;
  }

  /**
   * Removes all proposition markings from a given event alphabet.
   *
   * @param events
   * @return
   */
  private List<EventProxy> removeAlphabetMarkings(final Set<EventProxy> events)
  {
    // removes markings from automaton event alphabet
    final List<EventProxy> newEvents = new ArrayList<EventProxy>();
    for (final EventProxy event : events) {
      if (event.getKind() != EventKind.PROPOSITION) {
        newEvents.add(event);
      }
    }
    return newEvents;
  }

  /**
   * Creates the automaton that is used to test whether the specified answer
   * event occurs after a request event.
   *
   * @param answer
   */
  private AutomatonProxy createTestForAnswer(final EventProxy answer)
  {
    // removes markings from automaton event alphabet
    final List<EventProxy> newEvents =
        removeAlphabetMarkings(mModel.getEvents());

    // creates the 3 states needed (the 3rd is optional, not needed if only
    // request and answer events exist)
    final List<StateProxy> states = new ArrayList<StateProxy>(2);
    List<EventProxy> propositions = new ArrayList<EventProxy>(1);
    // initial state has the default marking proposition
    propositions.add(mMarking);
    final StateProxy initialState =
        mFactory.createStateProxy("T1", true, propositions);
    states.add(initialState);
    // next state has the precondition marking
    propositions = new ArrayList<EventProxy>(1);
    propositions.add(mPreconditionMarking);
    final StateProxy alphaState =
        mFactory.createStateProxy("T2", false, propositions);
    states.add(alphaState);
    StateProxy t3State = null;

    // creates the transitions needed
    final List<TransitionProxy> transitions = new ArrayList<TransitionProxy>();
    for (final EventProxy event : newEvents) {
      // self loop on the initial state that includes entire event alphabet
      final TransitionProxy transition =
          mFactory.createTransitionProxy(initialState, event, initialState);
      transitions.add(transition);

      // the transition which accepts any request event
      if (event.getAttributes().equals(HISCAttributes.ATTRIBUTES_REQUEST)) {
        final TransitionProxy requestTransition =
            mFactory.createTransitionProxy(initialState, event, alphaState);
        transitions.add(requestTransition);
      }

      // the transitions which accepts any local event (i.e. non request, non
      // answer events)
      else if (!event.getAttributes().equals(HISCAttributes.ATTRIBUTES_ANSWER)
          && !event.getAttributes().equals(HISCAttributes.ATTRIBUTES_REQUEST)) {
        if (states.size() < 3) {
          // third state has no propositions
          propositions = null;
          t3State = mFactory.createStateProxy("T3", false, null);
          states.add(t3State);
        }
        final TransitionProxy localTransition =
            mFactory.createTransitionProxy(alphaState, event, t3State);
        transitions.add(localTransition);
        final TransitionProxy localSelfLoop =
            mFactory.createTransitionProxy(t3State, event, t3State);
        transitions.add(localSelfLoop);
      }

    }
    // creates the two answer transitions
    final TransitionProxy immediateAnswer =
        mFactory.createTransitionProxy(alphaState, answer, initialState);
    transitions.add(immediateAnswer);
    if (t3State != null) {
      final TransitionProxy finallyAnswer =
          mFactory.createTransitionProxy(t3State, answer, initialState);
      transitions.add(finallyAnswer);
    }

    // adds the two marking propositions to the automaton alphabet
    newEvents.add(mMarking);
    newEvents.add(mPreconditionMarking);

    final AutomatonProxy newTestAut =
        mFactory.createAutomatonProxy("Test:Aut", ComponentKind.SPEC,
                                      newEvents, states, transitions);

    return newTestAut;
  }

  private AutomatonProxy createModifiedInterfaceAutomaton(
                                                          final AutomatonProxy aut,
                                                          final EventProxy answer)
  {
    // removes all marking propositions from the event alphabet (all
    // states are required to be marked with the default marking propositionS,
    // so
    // by removing the marking all states are implicitly marked)
    final List<EventProxy> newEvents = removeAlphabetMarkings(aut.getEvents());
    newEvents.add(mPreconditionMarking);

    // collects all the transitions that use the specified answer event
    final List<TransitionProxy> answerTransitions =
        new ArrayList<TransitionProxy>();
    for (final TransitionProxy transition : aut.getTransitions()) {
      if (transition.getEvent().equals(answer)) {
        answerTransitions.add(transition);
      }
    }

    final List<StateProxy> newStates = new ArrayList<StateProxy>();

    for (final StateProxy state : aut.getStates()) {
      // removes all marking propositions from all states by creating an empty
      // list of propositions
      final List<EventProxy> newPropositions = new ArrayList<EventProxy>();

      // mark states that have the specified answer event enabled with the
      // precondition marking proposition :alpha
      for (final TransitionProxy transition : answerTransitions) {
        if (transition.getSource() == state) {
          if (!newPropositions.contains(mPreconditionMarking)) {
            newPropositions.add(mPreconditionMarking);
          }
          break;
        }
      }
      final StateProxy newState =
          mFactory.createStateProxy(state.getName(), state.isInitial(),
                                    newPropositions);
      newStates.add(newState);
      mStates.put(state, newState);
    }
    final List<TransitionProxy> newTransitions = replaceTransitionStates(aut);
    mStates.clear();
    return mFactory.createAutomatonProxy(aut.getName(), aut.getKind(),
                                         newEvents, newStates, newTransitions,
                                         aut.getAttributes());
  }

  private AutomatonProxy createModifiedLowLevelAutomaton(
                                                         final AutomatonProxy aut)
  {
    // removes markings from automaton event alphabet
    final List<EventProxy> newEvents = removeAlphabetMarkings(aut.getEvents());
    // removes markings from the states
    final List<StateProxy> newStates = new ArrayList<StateProxy>();
    for (final StateProxy state : aut.getStates()) {
      final List<EventProxy> propositions = new ArrayList<EventProxy>();
      final StateProxy newState =
          mFactory.createStateProxy(state.getName(), state.isInitial(),
                                    propositions);
      newStates.add(newState);
      mStates.put(state, newState);
    }
    final List<TransitionProxy> newTransitions = replaceTransitionStates(aut);

    final AutomatonProxy modifiedLowLevelAutomaton =
        mFactory.createAutomatonProxy(aut.getName(), aut.getKind(), newEvents,
                                      newStates, newTransitions, aut
                                          .getAttributes());
    mLowLevelAutomata.add(modifiedLowLevelAutomaton);
    mStates.clear();
    return modifiedLowLevelAutomaton;
  }

  /**
   * Replaces the source and target states of a transition with the new version
   * of the states.
   *
   * @return
   */
  private List<TransitionProxy> replaceTransitionStates(final AutomatonProxy aut)
  {
    final List<TransitionProxy> newTransitions =
        new ArrayList<TransitionProxy>();
    for (final TransitionProxy transition : aut.getTransitions()) {
      final StateProxy source = mStates.get(transition.getSource());
      final StateProxy target = mStates.get(transition.getTarget());
      final TransitionProxy newTransition =
          mFactory.createTransitionProxy(source, transition.getEvent(), target);
      newTransitions.add(newTransition);
    }
    return newTransitions;
  }

  @SuppressWarnings("unchecked")
  public ConflictTraceProxy convertTraceToOriginalModel(
                                                        final ConflictTraceProxy conflictTrace,
                                                        final EventProxy answer)
      throws MalformedURLException
  {
    // creates a map of the original model's automaton names to the object for
    // that automaton and a map of the names of the states for that automaton to
    // the state proxy objects
    final Map<String,AutomatonProxy> autMap =
        new HashMap<String,AutomatonProxy>();
    final Map<String,StateProxy> innerStateMap =
        new HashMap<String,StateProxy>();
    final Map<String,Map<String,StateProxy>> stateMap =
        new HashMap<String,Map<String,StateProxy>>();
    for (final AutomatonProxy aut : mModel.getAutomata()) {
      final String autName = aut.getName();
      autMap.put(autName, aut);

      final Set<StateProxy> states = aut.getStates();
      for (final StateProxy state : states) {
        final String statenm = state.getName();
        innerStateMap.put(statenm, state);
      }
      stateMap.put(autName, new HashMap(innerStateMap));
      innerStateMap.clear();
    }
    final List<TraceStepProxy> traceSteps = conflictTrace.getTraceSteps();
    final List<TraceStepProxy> convertedSteps = new ArrayList<TraceStepProxy>();
    for (final TraceStepProxy step : traceSteps) {
      final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
      final Map<AutomatonProxy,StateProxy> convertedStepMap =
          new HashMap<AutomatonProxy,StateProxy>();
      for (final Map.Entry<AutomatonProxy,StateProxy> e : stepMap.entrySet()) {
        final AutomatonProxy convertedAut = e.getKey();
        final String autName = convertedAut.getName();
        final AutomatonProxy originalAut = autMap.get(autName);
        if (originalAut != null) {
          final StateProxy convertedState = e.getValue();
          final String stateName = convertedState.getName();
          final StateProxy originalState = stateMap.get(autName).get(stateName);
          convertedStepMap.put(originalAut, originalState);
        }
      }
      final TraceStepProxy convertedStep =
          mFactory.createTraceStepProxy(step.getEvent(), convertedStepMap);
      convertedSteps.add(convertedStep);
    }
    final String tracename = conflictTrace.getFileLocation().toString();
    final String modelname = mModel.getName();
    final String mainComment = modelname + " does not satisfy SIC Property V. ";
    final String comment =
        mainComment + "The answer event, " + answer.getName()
            + ", caused the failure.";
    final ConflictTraceProxy convertedTrace =
        mFactory.createConflictTraceProxy(tracename, comment, conflictTrace
            .getLocation(), mModel, mModel.getAutomata(), convertedSteps,
                                          ConflictKind.CONFLICT);
    return convertedTrace;
  }

  public void setDefaultMarkings()
  {
    mMarking =
        mFactory.createEventProxy(EventDeclProxy.DEFAULT_MARKING_NAME,
                                  EventKind.PROPOSITION, true);
    final String alphaNm = ":alpha";
    mPreconditionMarking =
        mFactory.createEventProxy(alphaNm, EventKind.PROPOSITION, true);
  }

  // #########################################################################
  // # Data Members
  /**
   * The model which is being changed.
   */
  private ProductDESProxy mModel;

  private final ProductDESProxyFactory mFactory;

  /**
   * A list of the low level automaton that are created with the new marking
   * rules.
   */
  private List<AutomatonProxy> mLowLevelAutomata;

  /**
   * A map of the original states to the new version of the state (which is
   * either a copy or has a proposition added/removed).
   */
  private final Map<StateProxy,StateProxy> mStates =
      new HashMap<StateProxy,StateProxy>();

  // the default marking proposition
  private EventProxy mMarking;
  // the precondition marking
  private EventProxy mPreconditionMarking;

}
