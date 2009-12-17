package net.sourceforge.waters.despot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.despot.HISCAttributes;


public class SICPropertyVBuilder
{

  public SICPropertyVBuilder(final ProductDESProxyFactory factory)
  {
    mFactory = factory;
    mModel = null;
  }

  public SICPropertyVBuilder(final ProductDESProxyFactory factory,
      final ProductDESProxy model)
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
  }

  /**
   * Gets all the answer events that belong to the model.
   *
   * @return
   */
  public Collection<EventProxy> getAnswerEvents()
  {
    final Set<EventProxy> allEvents = mModel.getEvents();
    final List<EventProxy> answerEvents = new ArrayList<EventProxy>(0);
    for (final EventProxy event : allEvents) {
      if (event.getAttributes().equals(HISCAttributes.ATTRIBUTES_ANSWER)) {
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
   * @return
   */
  public ProductDESProxy createModelForAnswer(final EventProxy answer)
  {
    List<AutomatonProxy> newAutomaton = new ArrayList<AutomatonProxy>();
    // the low level automaton of a model only need modifying once because they
    // don't depend on the answer event
    if (mLowLevelAutomata == null) {
      mLowLevelAutomata = new ArrayList<AutomatonProxy>();
      for (final AutomatonProxy aut : mModel.getAutomata()) {
        if (!aut.getAttributes().equals(HISCAttributes.ATTRIBUTES_INTERFACE)) {
          newAutomaton.add(createModifiedLowLevelAutomaton(aut));
        }
      }
    } else {
      newAutomaton = mLowLevelAutomata;
    }
    // modifies the models interfaces dependent on the answer event specified
    for (final AutomatonProxy aut : mModel.getAutomata()) {
      if (aut.getAttributes().equals(HISCAttributes.ATTRIBUTES_INTERFACE)) {
        newAutomaton.add(createModifiedInterfaceAutomaton(aut, answer));
      }
    }
    newAutomaton.add(createTestForAnswer(answer));

    final ProductDESProxy newModel =
        mFactory.createProductDESProxy(mModel.getName(), mModel.getComment(),
            mModel.getLocation(), mModel.getEvents(), newAutomaton);
    return newModel;
  }

  /**
   * Creates the automaton that is used to test whether the specified answer
   * event occurs after a request event.
   *
   * @param answer
   * @return
   */
  private AutomatonProxy createTestForAnswer(final EventProxy answer)
  {
    // create the default marking proposition
    final EventProxy defaultMark =
        mFactory.createEventProxy(EventDeclProxy.DEFAULT_MARKING_NAME,
            EventKind.PROPOSITION, true);
    // create the precondition marking :alpha
    final EventProxy alpha =
        mFactory.createEventProxy(mAlpha, EventKind.PROPOSITION, true);

    // creates the 3 states needed
    final List<StateProxy> states = new ArrayList<StateProxy>(3);
    List<EventProxy> propositions = new ArrayList<EventProxy>(1);
    // initial state has the default marking proposition
    propositions.add(defaultMark);
    final StateProxy initialState =
        mFactory.createStateProxy("T1", true, propositions);
    states.add(initialState);
    // next state has the precondition marking
    propositions = new ArrayList<EventProxy>(1);
    propositions.add(alpha);
    final StateProxy alphaState =
        mFactory.createStateProxy("T2", false, propositions);
    states.add(alphaState);
    // third state has no propositions
    propositions = null;
    final StateProxy t3State = mFactory.createStateProxy("T3", false, null);
    states.add(t3State);

    // creates the transitions needed
    final List<TransitionProxy> transitions = new ArrayList<TransitionProxy>();
    for (final EventProxy event : mModel.getEvents()) {
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

      // the transition which accepts any local event (i.e. non request, non
      // answer events)
      else if (!event.getAttributes().equals(HISCAttributes.ATTRIBUTES_ANSWER)) {
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
    final TransitionProxy finallyAnswer =
        mFactory.createTransitionProxy(t3State, answer, initialState);
    transitions.add(finallyAnswer);

    final AutomatonProxy newTestAut =
        mFactory.createAutomatonProxy("TestAut", ComponentKind.PROPERTY, mModel
            .getEvents(), states, transitions);

    return newTestAut;
  }

  private AutomatonProxy createModifiedInterfaceAutomaton(
      final AutomatonProxy aut, final EventProxy answer)
  {
    // removes the default marking proposition from the event alphabet (all
    // states are required to be marked with it, so by removing the marking all
    // states are implicitly marked)
    final List<EventProxy> newEvents = new ArrayList<EventProxy>();
    for (final EventProxy event : aut.getEvents()) {
      if (event.getKind().equals(EventKind.PROPOSITION)) {
        if (!event.getName().equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
          newEvents.add(event);
        }
      }
    }
    // removes the default marking proposition from any states it is currently
    // applied to
    List<StateProxy> newStates = new ArrayList<StateProxy>();
    for (final StateProxy state : aut.getStates()) {
      final List<EventProxy> propositions = new ArrayList<EventProxy>();
      for (final EventProxy proposition : state.getPropositions()) {
        if (!proposition.getName().equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
          propositions.add(proposition);
        }
      }
      newStates.add(mFactory.createStateProxy(state.getName(), state
          .isInitial(), propositions));
    }
    AutomatonProxy modifiedInterface =
        mFactory.createAutomatonProxy(aut.getName(), aut.getKind(), newEvents,
            newStates, aut.getTransitions(), aut.getAttributes());

    // mark states that have the specified answer event enabled with the
    // precondition marking proposition :alpha
    newStates = new ArrayList<StateProxy>();
    // collects all the transitions that use the specified answer event
    final List<TransitionProxy> answerTransitions =
        new ArrayList<TransitionProxy>();
    for (final TransitionProxy transition : modifiedInterface.getTransitions()) {
      if (transition.getEvent().equals(answer)) {
        answerTransitions.add(transition);
      }
    }
    for (final StateProxy state : modifiedInterface.getStates()) {
      for (final TransitionProxy transition : answerTransitions) {
        if (transition.getSource().equals(state)) {
          final List<EventProxy> propositions =
              (List<EventProxy>) state.getPropositions();
          final EventProxy alpha =
              mFactory.createEventProxy(mAlpha, EventKind.PROPOSITION, true);
          if (!propositions.contains(alpha)) {
            propositions.add(alpha);
          }
          newStates.add(mFactory.createStateProxy(state.getName(), state
              .isInitial(), propositions));
        }
      }
    }
    modifiedInterface =
        mFactory.createAutomatonProxy(aut.getName(), aut.getKind(), newEvents,
            newStates, aut.getTransitions(), aut.getAttributes());

    return modifiedInterface;
  }

  private AutomatonProxy createModifiedLowLevelAutomaton(
      final AutomatonProxy aut)
  {
    // removes markings from automaton event alphabet
    final List<EventProxy> newEvents = new ArrayList<EventProxy>();
    for (final EventProxy event : aut.getEvents()) {
      if (event.getKind().equals(EventKind.PROPOSITION)) {
        if (!event.getName().equals(mAlpha)
            && !event.getName().equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
          newEvents.add(event);
        }
      }
    }
    // removes markings from the states
    final List<StateProxy> newStates = new ArrayList<StateProxy>();
    for (final StateProxy state : aut.getStates()) {
      final List<EventProxy> propositions = new ArrayList<EventProxy>();
      for (final EventProxy proposition : state.getPropositions()) {
        if (!proposition.getName().equals(mAlpha)
            && !proposition.getName().equals(
                EventDeclProxy.DEFAULT_MARKING_NAME)) {
          propositions.add(proposition);
        }
      }
      newStates.add(mFactory.createStateProxy(state.getName(), state
          .isInitial(), propositions));
    }
    final AutomatonProxy modifiedLowLevelAutomaton =
        mFactory.createAutomatonProxy(aut.getName(), aut.getKind(), newEvents,
            newStates, aut.getTransitions(), aut.getAttributes());
    mLowLevelAutomata.add(modifiedLowLevelAutomaton);
    return modifiedLowLevelAutomaton;
  }

  // #########################################################################
  // # Data Members
  /**
   * The model which is being changed.
   */
  private ProductDESProxy mModel;

  private final ProductDESProxyFactory mFactory;

  private final String mAlpha = ":alpha";

  /**
   * A list of the low level automaton that are created with the new marking
   * rules.
   */
  private List<AutomatonProxy> mLowLevelAutomata;

}
