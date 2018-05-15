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

package net.sourceforge.waters.analysis.sd;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


/**
 * A converter to translate models for checking SD Controllability Properties
 * II.a and&nbsp;II.b.
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
   * <P>Computes a set of prohibitable events needed to check SD
   * Controllability Property&nbsp;II.a.</P>
   *
   * <P>Property&nbsp;II.a checks whether it is possible for a prohibitable
   * event and the <I>tick</I> event to be enabled simultaneously. Yet in many
   * cases, it can be shown for a prohibitable event&nbsp;<I>e</I> that there
   * exists an automaton with both <I>e</I> and&nbsp;<I>tick</I> in the
   * alphabet but without any state that enables both events together. Then
   * <I>e</I> and&nbsp;<I>tick</I> are exclusive events, and there is no need
   * to check SD Controllability Property&nbsp;II.a for event&nbsp;<I>e</I>.</P>
   *
   * <P>This method returns a list of events that cannot be shown to be
   * exclusive, and for which a proper check needs to be carried out.</P>
   */
  Collection<EventProxy> getSDCTwoAEvents()
  {
    createDefaultEvents();
    final Set<EventProxy> hibSet = new THashSet<>();
    collectProhibitableEvents(mModel, hibSet);
    if (hibSet.isEmpty()) {
      return Collections.emptyList();
    }
    for (final AutomatonProxy aut : mModel.getAutomata()) {
      if (removeNonSDCTwoAEvents(aut, hibSet) && hibSet.isEmpty()) {
        return Collections.emptyList();
      }
    }
    if (hibSet.size() == 1) {
      return hibSet;
    } else {
      final List<EventProxy> hibList = new ArrayList<>(hibSet);
      Collections.sort(hibList);
      return hibList;
    }
  }

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
   */
  ProductDESProxy createSDCTwoAModel(final Collection<EventProxy> hibEvents)
  {
    return createSDCTwoAModel(hibEvents, null);
  }


  //#########################################################################
  //# Auxiliary Methods II.a
  private boolean removeNonSDCTwoAEvents(final AutomatonProxy aut,
                                         final Set<EventProxy> hibEvents)
  {
    final Collection<EventProxy> events = aut.getEvents();
    final int numEvents = events.size();
    final TObjectIntHashMap<EventProxy> eventMap =
      new TObjectIntHashMap<>(numEvents, 0.5f, -1);
    final EventProxy[] eventArray = new EventProxy[numEvents + 1];
    boolean hasTick = false;
    int numHibs = 0;
    for (final EventProxy event : events) {
      if (event == mTickEvent) {
        hasTick = true;
        eventMap.put(event, 0);
      } else if (hibEvents.contains(event)) {
        numHibs++;
        eventMap.put(event, numHibs);
        eventArray[numHibs] = event;
      }
    }
    if (!hasTick && numHibs == 0) {
      return false;
    }
    final Collection<StateProxy> states = aut.getStates();
    final int numStates = states.size();
    final TObjectIntHashMap<StateProxy> stateMap =
      new TObjectIntHashMap<>(numStates);
    int s = 0;
    for (final StateProxy state : states) {
      stateMap.put(state, s++);
    }
    final boolean[][] enabled = new boolean[numHibs + 1][numStates];
    boolean tickEnabled = !hasTick;
    for (final TransitionProxy trans : aut.getTransitions()) {
      final EventProxy event = trans.getEvent();
      final int e = eventMap.get(event);
      if (e >= 0) {
        final StateProxy source = trans.getSource();
        s = stateMap.get(source);
        enabled[e][s] = true;
        tickEnabled |= event == mTickEvent;
      }
    }
    if (!tickEnabled) {
      hibEvents.clear();
      return true;
    }
    boolean removed = false;
    for (int e = 1; e <= numHibs; e++) {
      boolean exclusive = true;
      for (s = 0; s < numStates; s++) {
        if ((!hasTick || enabled[0][s]) && enabled[e][s]) {
          exclusive = false;
          break;
        }
      }
      if (exclusive) {
        final EventProxy event = eventArray[e];
        hibEvents.remove(event);
        removed = true;
      }
    }
    return removed;
  }

  /**
   * Builds a model for checking SD Controllability Property II.a.
   * @param  hibEvents  Prohibitable events to be used in the test.
   * @String hibName    Name of a prohibitable event added to model name,
   *                    or <CODE>null</CODE>.
   */
  private ProductDESProxy createSDCTwoAModel
    (final Collection<EventProxy> hibEvents, final String hibName)
  {
    final Collection<AutomatonProxy> oldAutomata = mModel.getAutomata();
    final int numaut = oldAutomata.size();
    final List<AutomatonProxy> newAutomata = new ArrayList<>(numaut);
    final Collection<EventProxy> allEvents = mModel.getEvents();
    final Collection<EventProxy> newEvents = new ArrayList<>(allEvents);
    createDefaultEvents();
    createConflictEvents(hibEvents, newEvents);
    for (final AutomatonProxy oldAut : oldAutomata) {
      final AutomatonProxy newAut;
      newAut = addConflictTransitions(oldAut, hibEvents);
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

  /**
   * Creates the test automaton added to the model to check SD
   * Controllability Property&nbsp;II.a. This method creates a one-state
   * property that disables all conflict events for the given prohibitable
   * events.
   * @param  hibEvents  Prohibitable events to be considered in the test.
   */
  private AutomatonProxy createSDCTwoATest
    (final Collection<EventProxy> hibEvents)
  {
    final int numHibs = hibEvents.size();
    final Collection<EventProxy> events = new ArrayList<EventProxy>(numHibs);
    for (final EventProxy hib : hibEvents) {
      final EventProxy conf = mConflictEvents.get(hib);
      events.add(conf);
    }
    final StateProxy state = mFactory.createStateProxy("never", true, null);
    final Collection<StateProxy> states = Collections.singletonList(state);
    return
      mFactory.createAutomatonProxy(":never", ComponentKind.PROPERTY,
                                    events, states, null);
  }

  /**
   * Creates a an automaton by adding conflict-event selfloops to all states
   * with <I>tick</I> and the corresponding prohibitable event enabled
   * simultaneously.
   */
  private AutomatonProxy addConflictTransitions
    (final AutomatonProxy aut, final Collection<EventProxy> hibEvents)
  {
    final Collection<EventProxy> allEvents = aut.getEvents();
    final int numEvents = allEvents.size();
    final TObjectIntHashMap<EventProxy> eventMap =
      new TObjectIntHashMap<>(numEvents, 0.5f, -1);
    boolean hasTick = false;
    int numHibs = 0;
    for (final EventProxy event : allEvents) {
      if (event == mTickEvent) {
        hasTick = true;
        eventMap.put(event, 0);
      } else if (hibEvents.contains(event)) {
        numHibs++;
        eventMap.put(event, numHibs);
      }
    }
    if (!hasTick && numHibs == 0) {
      return aut;
    }
    final Collection<StateProxy> allStates = aut.getStates();
    final Collection<TransitionProxy> allTransitions = aut.getTransitions();
    final int numStates = allStates.size();
    final TObjectIntHashMap<StateProxy> stateMap =
      new TObjectIntHashMap<>(numStates);
    int s = 0;
    for (final StateProxy state : allStates) {
      stateMap.put(state, s++);
    }
    final boolean[][] enabled = new boolean[numHibs + 1][numStates];
    for (final TransitionProxy trans : allTransitions) {
      final EventProxy event = trans.getEvent();
      final int e = eventMap.get(event);
      if (e >= 0) {
        final StateProxy source = trans.getSource();
        s = stateMap.get(source);
        enabled[e][s] = true;
      }
    }
    final boolean[] disabled = new boolean[numHibs + 1];
    for (int e = 0; e <= numHibs; e++) {
      for (s = 0; s < numStates; s++) {
        if (!enabled[e][s]) {
          disabled[e] = true;
          break;
        }
      }
    }
    final List<EventProxy> newEvents = new ArrayList<>(allEvents);
    final List<TransitionProxy> newTransitions =
      new ArrayList<>(allTransitions);
    for (final EventProxy hib : hibEvents) {
      final int e = eventMap.get(hib);
      if ((e >= 0 && disabled[e]) || (hasTick && disabled[0])) {
        final EventProxy conf = mConflictEvents.get(hib);
        newEvents.add(conf);
        for (final StateProxy state : allStates) {
          s = stateMap.get(state);
          if ((e < 0 || enabled[e][s]) && (!hasTick || enabled[0][s])) {
            final TransitionProxy trans =
              mFactory.createTransitionProxy(state, conf, state);
            newTransitions.add(trans);
          }
        }
      }
    }
    if (newEvents.size() == allEvents.size()) {
      return aut;
    } else {
      return mFactory.createAutomatonProxy
        (aut.getName(), aut.getKind(), newEvents, allStates, newTransitions);
    }
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

    createDefaultEvents();
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
    createDefaultEvents();

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
  private void createDefaultEvents()
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

  /**
   * Creates conflict events for the given prohibitable events.
   * @param  hibEvents  Prohibitable events to create conflict events for.
   * @param  alphabet   Created events are added to this collection.
   * @see #mConflictEvents
   */
  private void createConflictEvents(final Collection<EventProxy> hibEvents,
                                    final Collection<EventProxy> alphabet)
  {
    final int numHibs = hibEvents.size();
    mConflictEvents = new HashMap<>(numHibs);
    for (final EventProxy hib : hibEvents) {
      final String hibName = hib.getName();
      final String confName = "c:" + TICK_NAME + ":" + hibName;
      final EventProxy conf =
        mFactory.createEventProxy(confName, EventKind.CONTROLLABLE, true);
      mConflictEvents.put(hib, conf);
      alphabet.add(conf);
    }
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
   * Gets all the prohibitable events that belong to given model.
   */
  static Collection<EventProxy> getProhibitableEvents(final ProductDESProxy des)
  {
    final List<EventProxy> hibs = new ArrayList<EventProxy>();
    collectProhibitableEvents(des, hibs);
    return hibs;
  }

  /**
   * Collects the prohibitable events in the given model to the given
   * collection.
   */
  static void collectProhibitableEvents(final ProductDESProxy des,
                                        final Collection<EventProxy> hibs)
  {
    final Collection<EventProxy> allEvents = des.getEvents();
    for (final EventProxy event : allEvents) {
      if (isProhibitableEvent(event)) {
        hibs.add(event);
      }
    }
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
  private final ProductDESProxyFactory mFactory;
  /**
   * The model which is being changed.
   */
  private final ProductDESProxy mModel;
  /**
   * The default marking proposition
   */
  private EventProxy mMarking;
  /**
   * The tick event.
   */
  private EventProxy mTickEvent;
  /**
   * The map to identify conflict events. Map to each prohibitable
   * event&nbsp;<I>e</I> a conflict event <I>c:tick:e</I>, which is to signify
   * that <I>e</I> and&nbsp;<I>tick</I> are enabled together.
   */
  private Map<EventProxy,EventProxy> mConflictEvents;

  private EventProxy mDisTickEvent;
  private EventProxy que;
  private final List<EventProxy> mDisabledEvents = new ArrayList<EventProxy>();
  private final Map<StateProxy,StateProxy> mStateMap =
    new HashMap<StateProxy,StateProxy>();
  private int pCounta = 0; // Counts for naming disable events


  //#########################################################################
  //# Class Constants
  private static final String TICK_NAME = "tick";

}
