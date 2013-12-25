//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters SD Analysis
//# PACKAGE: net.sourceforge.waters.analysis.sd
//# CLASS:   SDCTwoPropertyBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A converter to translate models for checking SD Controllability Properties
 * II.a and II.b.
 *
 * @author Mahvash Baloch, Robi Malik
 */

class SDCTwoPropertyBuilder
{

  //#########################################################################
  //# Constructors
  SDCTwoPropertyBuilder(final ProductDESProxyFactory factory)
  {
    mFactory = factory;
    mModel = null;
  }

  SDCTwoPropertyBuilder(final ProductDESProxy model,
                        final ProductDESProxyFactory factory)
  {
    mModel = model;
    mFactory = factory;
  }


  //#########################################################################
  //# Invocation II.a
  /**
   * Builds a model for checking SD Controllability Property II.a.
   * The model created by this method contains a single test automaton
   * to cover all prohibitable events in the model.
   */
  ProductDESProxy createSDCTwoAModel()
  {
    final Collection<EventProxy> hibEvents = getProhibitableEvents(mModel);
    return createSDCTwoAModel(hibEvents, null);
  }

  /**
   * Builds a model for checking SD Controllability Property II.a.
   * The model created by this method contains a test automaton
   * to cover only the given prohibitable event. Other events are
   * tested by creating separate models.
   * @param  event      The prohibitable event to be tested for.
   */
  ProductDESProxy createSDCTwoAModel(final EventProxy event)
  {
    final Collection<EventProxy> hibEvents = Collections.singletonList(event);
    return createSDCTwoAModel(hibEvents, event.getName());
  }


  /**
   * Builds a model for checking SD Controllability Property II.a.
   * @param  hibEvents  Prohibitable events to be used in the test.
   * @String hibName    Name of a prohibitable event added to model name,
   *                    or <CODE>null</CODE>.
   */
  ProductDESProxy createSDCTwoAModel(final Collection<EventProxy> hibEvents,
                                     final String hibName)
  {
    final Collection<AutomatonProxy> oldAutomata = mModel.getAutomata();
    final int numaut = oldAutomata.size();
    final List<AutomatonProxy> newAutomata =
      new ArrayList<AutomatonProxy>(numaut);
    final Collection<EventProxy> allEvents = mModel.getEvents();
    final Collection<EventProxy> newEvents =
      new ArrayList<EventProxy>(allEvents);
    getDefaultEvents();
    createEnTickEvent();
    newEvents.add(mEnTickEvent);
    for (final AutomatonProxy oldAut : oldAutomata) {
      final AutomatonProxy newAut;
      newAut = addEnTickTransitions(oldAut);
      newAutomata.add(newAut);
    }
    final AutomatonProxy testAut = createSDCTwoATest(hibEvents);
    newAutomata.add(testAut);
    final String desname = mModel.getName();
    final String hibSuffix = hibName == null ? "" : "-" + hibName;
    final String name = desname + "-SD-IIa" + hibSuffix;
    final String hibComment =
      hibName == null ? "" : " with respect to prohibitable event " + hibName;
    final String comment =
      "Automatically generated from '" + desname +
      "' to check SD Controllability Property II.a" + hibComment + ".";
    final ProductDESProxy newModel =
      mFactory.createProductDESProxy(name, comment, null, newEvents,
                                     newAutomata);
    return newModel;
  }


  //#########################################################################
  //# Auxiliary Methods II.a
  /**
   * Creates the test automaton added to the model to check SD
   * Controllability Property II.a
   * @param  hibEvents  Prohibitable events to be used in the test.
   */
  private AutomatonProxy createSDCTwoATest
    (final Collection<EventProxy> hibEvents)
  {
    final Collection<EventProxy> allEvents = mModel.getEvents();
    final Collection<EventProxy> newEvents = new ArrayList<EventProxy>();
    final Collection<StateProxy> states = new ArrayList<StateProxy>(2);

    // 1. Create the 2 states needed
    // State 1
    final StateProxy initialState =
      mFactory.createStateProxy("S0", true, null);
    states.add(initialState);
    // State 2
    final StateProxy t2State = mFactory.createStateProxy("S1", false, null);
    states.add(t2State);

    // 2. Create the transitions needed
    final Collection<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>();
    // Selfloop all non-prohibitable events and the given prohibitable events
    // on the initial state
    for (final EventProxy event : allEvents) {
      if (!isProhibitableEvent(event) &&
          event.getKind() != EventKind.PROPOSITION) {
        final TransitionProxy trans =
          mFactory.createTransitionProxy(initialState, event, initialState);
        transitions.add(trans);
        newEvents.add(event);
      }
    }
    for (final EventProxy event : hibEvents) {
      final TransitionProxy trans =
        mFactory.createTransitionProxy(initialState, event, initialState);
      transitions.add(trans);
    }
    newEvents.addAll(hibEvents);
    // The transitions which accept the etick event
    newEvents.add(mEnTickEvent);
    final TransitionProxy etickTransition1 =
      mFactory.createTransitionProxy(initialState, mEnTickEvent, t2State);
    transitions.add(etickTransition1);
    final TransitionProxy etickTransition2 =
      mFactory.createTransitionProxy(t2State, mEnTickEvent, t2State);
    transitions.add(etickTransition2);
    // The transitions which accept non-prohibitable events
    for (final EventProxy event : allEvents) {
      if (!isProhibitableEvent(event) &&
          event.getKind() != EventKind.PROPOSITION) {
        final TransitionProxy trans =
          mFactory.createTransitionProxy(t2State, event, initialState);
        transitions.add(trans);
      }
    }

    // 3. Create the automaton
    final AutomatonProxy newTestAut =
      mFactory.createAutomatonProxy("TestSD2", ComponentKind.PROPERTY,
                                    newEvents, states, transitions);
    return newTestAut;
  }

  /**
   * Creates a an automaton by adding enTick-transitions to all states
   * with the tick event enabled.
   */
  private AutomatonProxy addEnTickTransitions(final AutomatonProxy aut)
  {
    final Collection<EventProxy> allEvents = aut.getEvents();
    if (!allEvents.contains(mTickEvent)) {
      return aut;
    }
    final Collection<TransitionProxy> allTransitions = aut.getTransitions();
    final Collection<StateProxy> allStates = aut.getStates();
    final List<EventProxy> newEvents = new ArrayList<EventProxy>(allEvents);
    newEvents.add(mEnTickEvent);
    final List<TransitionProxy> newTransitions =
      new ArrayList<TransitionProxy>(allTransitions);
    for (final TransitionProxy transition : allTransitions) {
      final EventProxy event = transition.getEvent();
      if (event == mTickEvent) {
        final StateProxy state = transition.getSource();
        final TransitionProxy trans =
          mFactory.createTransitionProxy(state, mEnTickEvent, state);
        newTransitions.add(trans);
      }
    }
    return mFactory.createAutomatonProxy
      (aut.getName(), aut.getKind(), newEvents, allStates, newTransitions);
  }


  //#########################################################################
  //# Invocation II.b
  /**
   * Builds a model for checking ii (b) Property
   */
  public ProductDESProxy createSDTwo_bModel()
  {

    final Collection<AutomatonProxy> oldAutomata = mModel.getAutomata(); //get the automata
    final Collection<EventProxy> allEvents = mModel.getEvents();

    final int numaut = oldAutomata.size();
    final List<AutomatonProxy> newAutomata = // create a new array for new ones
      new ArrayList<AutomatonProxy>(numaut);
    final List<AutomatonProxy> tempnewAutomata =
      new ArrayList<AutomatonProxy>(numaut);
    final Collection<EventProxy> tevents = new ArrayList<EventProxy>(); //total events

    getDefaultEvents();
    mDisabledEvents.clear();
    pCounta = 0;
    for (final AutomatonProxy oldyAut : oldAutomata) {
      AutomatonProxy newAut;
      if (oldyAut.getKind() == ComponentKind.PLANT) {
        pCounta++;
        createDEvent();
        newAut = modiAutSD2(oldyAut);
        final Collection<EventProxy> autEvents = newAut.getEvents();
        if (autEvents.contains(mDisTickEvent)) {
          mDisabledEvents.add(mDisTickEvent);
          tevents.add(mDisTickEvent);
        }
      } else {
        newAut = oldyAut;
      }
      tempnewAutomata.add(newAut);
    }

    pCounta = 0;
    for (final AutomatonProxy oldAut : tempnewAutomata) {
      AutomatonProxy newAut;
      newAut = markAut(oldAut);
      newAutomata.add(newAut);
    }

    final AutomatonProxy testAut = createSD2Test();
    newAutomata.add(testAut);

    for (final EventProxy event : allEvents)
      tevents.add(event);
    tevents.add(que);

    final String desname = mModel.getName();
    final String name = desname + "-SD ii";
    final String comment =
      "Automatically generated from '" + desname
        + "' to check SD controllability";

    return mFactory.createProductDESProxy(name, comment, null, tevents,
                                          newAutomata);
  }

  //#########################################################################
  //# Auxiliary Methods
  /**
   * Creates the test automaton added to the model to check SD ii part b
   */
  private AutomatonProxy createSD2Test()
  {
    // gets the events from automaton event alphabet
    final Collection<EventProxy> allEvents = mModel.getEvents();

    final Collection<EventProxy> newEvents = new ArrayList<EventProxy>();
    final List<EventProxy> UpsilonEvents = new ArrayList<EventProxy>();
    final List<EventProxy> SigmaEvents = new ArrayList<EventProxy>();
    final List<EventProxy> propositions = new ArrayList<EventProxy>(1);
    getDefaultEvents();

    for (final EventProxy event : allEvents) {

      final EventKind kind = event.getKind();

      if ((kind.equals(EventKind.CONTROLLABLE)))
        UpsilonEvents.add(event);

      if (!(kind.equals(EventKind.PROPOSITION)))
        SigmaEvents.add(event);

      newEvents.add(event);
    }

    for (final EventProxy ev : mDisabledEvents) {
      UpsilonEvents.add(ev);
      newEvents.add(ev);
    }

    propositions.add(mMarking);

    // creates the 2 states needed
    final Collection<StateProxy> states = new ArrayList<StateProxy>(2);

    // initial state has the default marking proposition
    final StateProxy initialState =
      mFactory.createStateProxy("S1", true, propositions);
    states.add(initialState);
    // next state does not have any marking
    final StateProxy s2State = mFactory.createStateProxy("S2", false, null);
    states.add(s2State);

    //create tau event

    que = mFactory.createEventProxy("que", EventKind.CONTROLLABLE, true);
    newEvents.add(que);

    // creates the transitions needed
    final Collection<TransitionProxy> newtransitions =
      new ArrayList<TransitionProxy>();

    // self loop all events from Sigma on the initial state
    for (final EventProxy event : SigmaEvents) {
      final TransitionProxy transitions =
        mFactory.createTransitionProxy(initialState, event, initialState);
      newtransitions.add(transitions);
    }

    for (final EventProxy event : UpsilonEvents) {
      // the transition which accepts any event from the Upsilon event alphabet
      final TransitionProxy upTransition =
        mFactory.createTransitionProxy(s2State, event, initialState);
      newtransitions.add(upTransition);
    }

    // the transitions which accepts only que events
    final TransitionProxy tauTransition =
      mFactory.createTransitionProxy(initialState, que, s2State);
    newtransitions.add(tauTransition);

    final AutomatonProxy newTestAut =
      mFactory.createAutomatonProxy("TestSD2b", ComponentKind.PLANT,
                                    newEvents, states, newtransitions);

    return newTestAut;
  }

  /**
   * Modifies the Plant component to construct the D-tick automaton with all
   * states marked to check Sd ii part b
   */

  private AutomatonProxy modiAutSD2(final AutomatonProxy aut)
  {
    final Collection<EventProxy> allEvents = aut.getEvents();
    final Collection<EventProxy> newEvents = new ArrayList<EventProxy>();

    final Collection<StateProxy> oldStates = aut.getStates();

    final List<TransitionProxy> allTransitions =
      new ArrayList<TransitionProxy>();

    for (final EventProxy event : allEvents)
      newEvents.add(event);

    final Collection<TransitionProxy> oldTransitions = aut.getTransitions();
    final List<StateProxy> mState = new ArrayList<StateProxy>();

    for (final TransitionProxy transition : oldTransitions) {
      allTransitions.add(transition);
      final EventProxy ev = transition.getEvent();

      final StateProxy state = transition.getSource();
      if ((ev.equals(mTickEvent))) {
        mState.add(state);
      }
    }

    for (final StateProxy st : oldStates) {
      if (mState == null) {
        final TransitionProxy disEvent =
          mFactory.createTransitionProxy(st, mDisTickEvent, st);
        allTransitions.add(disEvent);
      } else if (!mState.contains(st)) {
        final TransitionProxy disEvent =
          mFactory.createTransitionProxy(st, mDisTickEvent, st);
        allTransitions.add(disEvent);
      }
    }

    if (!(mState.containsAll(oldStates)))
      newEvents.add(mDisTickEvent);

    return mFactory
      .createAutomatonProxy(aut.getName(), aut.getKind(), newEvents,
                            oldStates, allTransitions);
  }

  // marks all states in the Automata
  private AutomatonProxy markAut(final AutomatonProxy aut)
  {

    final Collection<EventProxy> allEvents = aut.getEvents();
    final ComponentKind kind = aut.getKind();
    Collection<EventProxy> mProps = null;
    final Collection<StateProxy> oldStates = aut.getStates();
    final int numStates = oldStates.size();
    final Collection<StateProxy> newStates =
      new ArrayList<StateProxy>(numStates);

    for (final StateProxy state : oldStates) {
      final Collection<EventProxy> props = state.getPropositions();
      if (props.contains(mMarking)) {
        mProps = props;
      }
    }

    for (final StateProxy oldState : oldStates) {

      final StateProxy newState =
        mFactory.createStateProxy(oldState.getName(), oldState.isInitial(),
                                  mProps);
      newStates.add(newState);
      mStateMap.put(oldState, newState);

    }
    final Collection<TransitionProxy> newTransitions =
      replaceTransitionStates(aut);
    mStateMap.clear();

    return mFactory.createAutomatonProxy(aut.getName(), kind, allEvents,
                                         newStates, newTransitions);
  }

  /**
   * Replaces the source and target states of a transition with the new
   * version of the states stored in the map {@link #mStateMap}.
   */
  private Collection<TransitionProxy> replaceTransitionStates(final AutomatonProxy aut)
  {
    final Collection<TransitionProxy> oldTransitions = aut.getTransitions();
    final int numTransitions = oldTransitions.size();
    final List<TransitionProxy> newTransitions =
      new ArrayList<TransitionProxy>(numTransitions);
    for (final TransitionProxy transition : oldTransitions) {
      final StateProxy source = mStateMap.get(transition.getSource());
      final StateProxy target = mStateMap.get(transition.getTarget());
      final TransitionProxy newTransition =
        mFactory.createTransitionProxy(source, transition.getEvent(), target);
      newTransitions.add(newTransition);
    }
    return newTransitions;
  }


  //#########################################################################
  //# Event Creation
  private void getDefaultEvents()
  {
    final Collection<EventProxy> allEvents = mModel.getEvents();
    mMarking = null;
    mTickEvent = null;
    for (final EventProxy event : allEvents) {
      final String name = event.getName();
      switch (event.getKind()) {
      case CONTROLLABLE:
        if (name.equals(TICK_NAME)) {
          mTickEvent = event;
        }
        break;
      case PROPOSITION:
        if (name.equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
          mMarking = event;
        }
        break;
      default:
        break;
      }
    }
    if (mMarking == null) {
      mMarking =
        mFactory.createEventProxy(EventDeclProxy.DEFAULT_MARKING_NAME,
                                  EventKind.PROPOSITION, true);
    }
  }

  private void createEnTickEvent()
  {
    final String eEventName = "e:" + TICK_NAME;
    mEnTickEvent =
      mFactory.createEventProxy(eEventName, EventKind.CONTROLLABLE, true);

  }

  private void createDEvent()
  {
    final String name = "tick";
    final String dEventName = "d" + name + pCounta;
    mDisTickEvent =
      mFactory.createEventProxy(dEventName, EventKind.CONTROLLABLE, true);
  }


  //#########################################################################
  //# Static Methods
  /**
   * Gets all the prohibitable events that belong to the model.
   */
  static Collection<EventProxy> getProhibitableEvents(final ProductDESProxy des)
  {
    final Collection<EventProxy> allEvents = des.getEvents();
    final List<EventProxy> hibEvents = new ArrayList<EventProxy>();
    for (final EventProxy event : allEvents) {
      if (isProhibitableEvent(event)) {
        hibEvents.add(event);
      }
    }
    return hibEvents;
  }

  /**
   * Returns whether the given event is considered as prohibitable.
   */
  static boolean isProhibitableEvent(final EventProxy event)
  {
    return event.getKind() == EventKind.CONTROLLABLE &&
           !event.getName().equals(TICK_NAME);
  }

  /**
   * Returns whether the given event qualifies the tick event.
   */
  static boolean isTickEvent(final EventProxy event)
  {
    return event.getKind() == EventKind.CONTROLLABLE &&
           event.getName().equals(TICK_NAME);
  }


  //#########################################################################
  //# Data Members
  /**
   * The model which is being changed.
   */
  private final ProductDESProxy mModel;

  private final Map<StateProxy,StateProxy> mStateMap =
    new HashMap<StateProxy,StateProxy>();

  private final ProductDESProxyFactory mFactory;
  /**
   * The default marking proposition
   */
  private EventProxy mMarking;
  /**
   * The tick event.
   */
  private EventProxy mTickEvent;
  /**
   * The event used to signify that the tick event is enabled.
   */
  private EventProxy mEnTickEvent;

  private EventProxy mDisTickEvent;
  private EventProxy que;
  private final List<EventProxy> mDisabledEvents = new ArrayList<EventProxy>();
  private int pCounta = 0; // Counts for naming disable events


  //#########################################################################
  //# Class Constants
  private static final String TICK_NAME = "tick";

}
