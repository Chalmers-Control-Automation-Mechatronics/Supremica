//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


/**
* A converter to translate models to different models for checking SD Properties.
* (S - Singular Prohibitable behaviour, SD four and Proper Time Behavior)
*
* @author Mahvash Baloch, Robi Malik
*/

public class SDPropertyBuilder
{

//#########################################################################
//# Constructors
public SDPropertyBuilder(final ProductDESProxyFactory factory)
{
  mFactory = factory;
  mModel = null;
}

public SDPropertyBuilder(final ProductDESProxy model,
                          final ProductDESProxyFactory factory)
{
  mModel = model;
  mFactory = factory;
}

/**
 * Gets all the Prohibitable events that belong to the model.
 */

public Collection<EventProxy> getHibEvents()
{
  final Set<EventProxy> allEvents = mModel.getEvents();
  final List<EventProxy> hibEvents = new ArrayList<EventProxy>(0);
  for (final EventProxy event : allEvents)
    if (event.getKind() == EventKind.CONTROLLABLE)
      if(!(event.getName().equals("tick")))
      hibEvents.add(event);

  return hibEvents;
}

  /**
 * Builds a model for checking S-Singular Prohibitable Behavior Property
 */
public ProductDESProxy createSingularModel(final EventProxy hib)
{

  final Collection<AutomatonProxy> oldAutomata = mModel.getAutomata();

  final int numaut = oldAutomata.size();
  final List<AutomatonProxy> newAutomata =
    new ArrayList<AutomatonProxy>(numaut);

  final Collection <EventProxy> tEvents = new ArrayList<EventProxy>();
  final Collection<EventProxy> allEvents = mModel.getEvents();
  getDefaultEvents();
  createEnevent(hib);

  final AutomatonProxy testAut = createS_SingularTest(hib);
  newAutomata.add(testAut);

  for (final AutomatonProxy oldAut : oldAutomata)
  {
    final AutomatonProxy newAut;

    if (oldAut.getKind()== ComponentKind.PLANT)
    {
      newAut = modifyAut(oldAut, hib);

            }
    else
    {
      newAut = oldAut;

    }
    newAutomata.add(newAut);
  }

  for(final EventProxy eve: allEvents)
   tEvents.add(eve);

  tEvents.add(enEvent);

  final String desname = mModel.getName();
  final String name = desname + "-Singular";
  final String comment =
    "Automatically generated from '" + desname +
    "' to check S-Singular Prohibitable Behavior";

  final ProductDESProxy newModel =
  mFactory.createProductDESProxy(name, comment, null,
                                        tEvents, newAutomata);

  return newModel;
}

  //#########################################################################
//# Auxiliary Methods
/**
 * Creates the test automaton added to the model to check
 * S-Singular Prohibitable Behavior Property
 */
private AutomatonProxy createS_SingularTest(final EventProxy hib)
{
  final Collection<EventProxy> newEvents = new ArrayList<EventProxy>();
  final List<EventProxy> propositions = new ArrayList<EventProxy>(1);
  final Collection<StateProxy> states = new ArrayList<StateProxy>(2);

  propositions.add(mMarking);

  // creates the 2 states needed
  // initial state has the default marking proposition

 final StateProxy initialState =
    mFactory.createStateProxy("S0", true, propositions);
    states.add(initialState);
  // next  state does not have any marking
  final StateProxy t2State =
      mFactory.createStateProxy("S1", false, null);
  states.add(t2State);


  // creates the transitions needed
  final Collection<TransitionProxy> transitions = new ArrayList<TransitionProxy>();

   // self loop tick on the initial state
    final TransitionProxy transition =
        mFactory.createTransitionProxy(initialState, tick, initialState);
    transitions.add(transition);


        // the transition which accepts the Sigma-hib event from the event alphabet
    final TransitionProxy SigmaTransition =
          mFactory.createTransitionProxy(initialState, hib, t2State);
      transitions.add(SigmaTransition);


    // the transitions which accepts only tick
    final TransitionProxy tickTransition =
          mFactory.createTransitionProxy(t2State, tick, initialState);
      transitions.add(tickTransition);

  // the transition for enabling of events
      final TransitionProxy enableTransition =
          mFactory.createTransitionProxy(initialState, enEvent, initialState);
      transitions.add(enableTransition);


  newEvents.add(hib);
  newEvents.add(enEvent);
  newEvents.add(tick);
  if(!mMarking.equals(null))
  newEvents.add(mMarking);

  final AutomatonProxy newTestAut =
      mFactory.createAutomatonProxy("TestSSPB", ComponentKind.PROPERTY,
                                   newEvents, states, transitions);

  return newTestAut;
}

/**
 * Modifies the Plant component to constructs the G-sigma automaton
 * to check S-Singular Prohibitable Behavior
 */

private AutomatonProxy modifyAut (final AutomatonProxy aut, final EventProxy hib)
{
  final List<TransitionProxy> newTransitions =
    new ArrayList<TransitionProxy>();

final List<EventProxy> newEvents = new ArrayList<EventProxy>();

final Collection<EventProxy> allEvents = aut.getEvents();

for (final EventProxy event: allEvents )
  newEvents.add(event);

final Collection<TransitionProxy> alltransitions = aut.getTransitions();
final Collection<StateProxy> allStates = aut.getStates();
final String hName = hib.getName();

for (final TransitionProxy transition : alltransitions) {
    newTransitions.add(transition);
    final EventProxy ev = transition.getEvent();

    final String name = ev.getName();
    if((name.equals(hName)))
    {
       final StateProxy state = transition.getSource();

       final TransitionProxy enableEvent =
             mFactory.createTransitionProxy(state, enEvent, state);
          newTransitions.add(enableEvent);
   }
    }

newEvents.add(enEvent);
return mFactory.createAutomatonProxy(aut.getName() , aut.getKind(),
                                     newEvents, allStates, newTransitions);
}

/* --------------------------------------------------------------------------------------------
* -----------------------------------------------------------------------------------------------------
* Creating the Model for SD property iv  ------------------------------------------------------
* -------------------------------------------------------------------------------------------
* -------------------------------------------------------------------------------------------
*/
public ProductDESProxy createModelSDFour()
{

  final Collection<AutomatonProxy> oldAutomata = mModel.getAutomata(); //get the automata
  final Collection<EventProxy> allEvents = mModel.getEvents();

  final int numaut = oldAutomata.size();                  //check how many r they
  final List<AutomatonProxy> newAutomata =               // create a new array for new ones
    new ArrayList<AutomatonProxy>(numaut);

  final Collection <EventProxy> tevents = new ArrayList<EventProxy>();  //total events

  final AutomatonProxy testAut = createT_SDfour();
  newAutomata.add(testAut);                                      //add test automata in new
  for (final AutomatonProxy oldAut : oldAutomata) {
     final AutomatonProxy newAut;
      newAut = modiAut(oldAut);
      newAutomata.add(newAut);
    }

  for(final EventProxy event: allEvents)
    tevents.add(event);
    tevents.add(Omega);

  final String desname = mModel.getName();
  final String name = desname + "-SDiv";
  final String comment =
    "Automatically generated from '" + desname +
    "' to check SD controllability";

  return mFactory.createProductDESProxy(name, comment, null,
                                        tevents, newAutomata);
}

  //#########################################################################
//# Auxiliary Methods
/**
 * Creates the test automaton added to the model to check
 * SD controllability Part Four Property
 */
private AutomatonProxy createT_SDfour()
{
  // gets the events from automaton event alphabet
  final Collection<EventProxy> allEvents = mModel.getEvents();

  final Collection<EventProxy> newEvents = new ArrayList<EventProxy>();
  final List<EventProxy> actEvents = new ArrayList<EventProxy>();
  final List<EventProxy> propositions = new ArrayList<EventProxy>(1);
  getDefaultEvents();

  for (final EventProxy event : allEvents) {

    final String name = event.getName();
    final EventKind kind= event.getKind();

    if((kind.equals(EventKind.CONTROLLABLE))) {
      newEvents.add(event);
      if(!(name.equals("tick")))
      actEvents.add(event);
    }
      }

    newEvents.add(mMarking);
    propositions.add(mMarking);

  // creates the 2 states needed
  final Collection<StateProxy> states = new ArrayList<StateProxy>(2);

  // initial state has the default marking proposition
  final StateProxy initialState =
    mFactory.createStateProxy("S1", true, propositions);
    states.add(initialState);
  // next 2 states do not have any marking
  final StateProxy s2State =
      mFactory.createStateProxy("S2", false, null);
    states.add(s2State);

   //create Omega event

   Omega =
   mFactory.createEventProxy("Omega", EventKind.CONTROLLABLE, true);
   newEvents.add(Omega);


  // creates the transitions needed
  final Collection<TransitionProxy> transitions = new ArrayList<TransitionProxy>();

     // self loop tick on the initial state
    final TransitionProxy transition =
        mFactory.createTransitionProxy(initialState, tick, initialState);
    transitions.add(transition);

    for (final EventProxy event : actEvents) {
        // the transition which accepts any event from the event alphabet
    final TransitionProxy actTransition1 =
          mFactory.createTransitionProxy(initialState, event, s2State);
    final TransitionProxy actTransition2 =
          mFactory.createTransitionProxy(s2State, event, s2State);
      transitions.add(actTransition1);
      transitions.add(actTransition2);
    }

    // the transitions which accepts only tick
    final TransitionProxy tickTransition =
          mFactory.createTransitionProxy(s2State, tick, initialState);
      transitions.add(tickTransition);

  // the transition for Omega event for the marked state
      final TransitionProxy OmTransition =
          mFactory.createTransitionProxy(initialState, Omega, initialState);
      transitions.add(OmTransition);

  final AutomatonProxy newTestAut =
      mFactory.createAutomatonProxy("TestSD4", ComponentKind.PROPERTY,
                                   newEvents, states, transitions);

  return newTestAut;
}

/**
 * Modifies the Plant component to construct the G-Omega automaton
 * to check SD controllability Part iv
 */

private AutomatonProxy modiAut (final AutomatonProxy aut)
{
    final Collection <TransitionProxy> newTransitions =
    new ArrayList<TransitionProxy>();

final List<EventProxy> newEvents = new ArrayList<EventProxy>();

final Collection<EventProxy> allEvents = aut.getEvents();

final Collection<TransitionProxy> allTransitions = aut.getTransitions();

for (final EventProxy event: allEvents )
{
  newEvents.add(event);
}
newEvents.add(Omega);

for (final TransitionProxy trans: allTransitions )
{
  newTransitions.add(trans);
}

final Collection<StateProxy> allStates = aut.getStates();

for (final StateProxy state : allStates) {
  final Collection <EventProxy> props = state.getPropositions();

  if (props.contains(mMarking))
  {
    final TransitionProxy OmTransition =
    mFactory.createTransitionProxy( state, Omega, state);
    newTransitions.add(OmTransition);

  }
    }

return mFactory.createAutomatonProxy(aut.getName(), aut.getKind(),
                                     newEvents, allStates, newTransitions);
}
/* ------------------------------------------------------------------------------
 * ----------------------------------------------------------------------------
 * END OF SD iv ..............................................................
 * -----------------------------------------------------------------------------
 */

 // PROPER TIME BeHAVIOUR
public ProductDESProxy createModelproperTimeB()
{

  final Collection<AutomatonProxy> oldAutomata = mModel.getAutomata(); //get the automata
  final Collection<EventProxy> allEvents = mModel.getEvents();

  final int numaut = oldAutomata.size();
  final List<AutomatonProxy> newAutomata =               // create a new array for new ones
    new ArrayList<AutomatonProxy>(numaut);

  final Collection <EventProxy> tevents = new ArrayList<EventProxy>();  //total events

  getDefaultEvents();
  final AutomatonProxy testAut = createT_pTime();
  newAutomata.add(testAut);     //add test automata in new

  for (final AutomatonProxy oldAut : oldAutomata) {
    final AutomatonProxy newAut;
    if (oldAut.getKind()== ComponentKind.PLANT)
        {
            newAut = modiAuto(oldAut);
            newAutomata.add(newAut);
          }
  }

  for(final EventProxy event: allEvents)
    tevents.add(event);
    tevents.add(que);

  final String desname = mModel.getName();
  final String name = desname + "-ProperTimeBehavior";
  final String comment =
    "Automatically generated from '" + desname +
    "' to check Proper Time Behaviour";

  return mFactory.createProductDESProxy(name, comment, null,
                                        tevents, newAutomata);
}

  //#########################################################################
//# Auxiliary Methods
/**
 * Creates the test automaton added to the model to check
 * Proper Time Behavior
 */
private AutomatonProxy createT_pTime()
{
  // gets the events from automaton event alphabet
  final Collection<EventProxy> allEvents = mModel.getEvents();

  final Collection<EventProxy> newEvents = new ArrayList<EventProxy>();
  final List<EventProxy> UpsilonEvents = new ArrayList<EventProxy>();
  final List<EventProxy> SigmaEvents = new ArrayList<EventProxy>();
  final List<EventProxy> propositions = new ArrayList<EventProxy>(1);


  for (final EventProxy event : allEvents) {

    final EventKind kind= event.getKind();

    if((kind.equals(EventKind.UNCONTROLLABLE)) || (event.equals(tick)))
      UpsilonEvents.add(event);

      if(!(kind.equals(EventKind.PROPOSITION)))
        SigmaEvents.add(event);

      newEvents.add(event);
   }

   propositions.add(mMarking);

  // creates the 2 states needed
  final Collection<StateProxy> states = new ArrayList<StateProxy>(2);

  // initial state has the default marking proposition
  final StateProxy initialState =
    mFactory.createStateProxy("S1", true, propositions);
    states.add(initialState);
  // next state does not have any marking
  final StateProxy s2State =
      mFactory.createStateProxy("S2", false, null);
    states.add(s2State);

   //create tau event

   que =
   mFactory.createEventProxy("que", EventKind.CONTROLLABLE, true);
   newEvents.add(que);


  // creates the transitions needed
  final Collection<TransitionProxy> newtransitions
  = new ArrayList<TransitionProxy>();

     // self loop all events from Sigma on the initial state
  for (final EventProxy event : SigmaEvents)
  {final TransitionProxy transitions =
        mFactory.createTransitionProxy(initialState, event, initialState);
    newtransitions.add(transitions);}

    for (final EventProxy event : UpsilonEvents) {
        // the transition which accepts any event from the Upsilon event alphabet
    final TransitionProxy upTransition =
          mFactory.createTransitionProxy(s2State, event, initialState);
          newtransitions.add(upTransition);
    }

    // the transitions which accepts only que
    final TransitionProxy tauTransition =
          mFactory.createTransitionProxy(initialState, que, s2State);
      newtransitions.add(tauTransition);

  final AutomatonProxy newTestAut =
      mFactory.createAutomatonProxy("TestPTB", ComponentKind.PLANT,
                                   newEvents, states, newtransitions);

  return newTestAut;
}

/**
 * Modifies the Plant component to construct the G-Omega automaton
 * to check Proper Time Behavior
 */

private AutomatonProxy modiAuto (final AutomatonProxy aut)
{

final Collection<EventProxy> allEvents = aut.getEvents();

Collection <EventProxy> mProps = null;
final Collection<StateProxy> oldStates = aut.getStates();
final int numStates = oldStates.size();
final Collection<StateProxy> newStates = new ArrayList<StateProxy>(numStates);
final ComponentKind kind = aut.getKind();

for (final StateProxy state : oldStates) {
  final Collection <EventProxy> props = state.getPropositions();
  if (props.contains(mMarking))
  {  mProps = props; }
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

return mFactory.createAutomatonProxy(aut.getName(), kind,
                                     allEvents, newStates, newTransitions);
}

/**
 * Replaces the source and target states of a transition with the new version
 * of the states stored in the map {@link #mStateMap}.
 */
private Collection<TransitionProxy> replaceTransitionStates
  (final AutomatonProxy aut)
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

/* gets the values of tick event and the marking in the model
 * gets the default values
 */
private void getDefaultEvents()
{
  final Collection<EventProxy> allEvents = mModel.getEvents();

  mMarking = null;

 for (final EventProxy event : allEvents) {

 final String name = event.getName();
 final EventKind kind=event.getKind();

if((kind.equals(EventKind.CONTROLLABLE)))
   if ((name.equals("tick")))
   {
     tick = event;
      }

if ((kind.equals(EventKind.PROPOSITION)))
  mMarking=event;
 }

if (mMarking == null) {
  mMarking =
      mFactory.createEventProxy(EventDeclProxy.DEFAULT_MARKING_NAME,
                                EventKind.PROPOSITION, true);
}
}

private void createEnevent
   (final EventProxy hibevent)
{
  final String name = hibevent.getName();
  final String eventName = "e"+name;
  enEvent =
  mFactory.createEventProxy(eventName, EventKind.CONTROLLABLE , true);
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

private EventProxy enEvent;
/**
 * The Default marking proposition
 */
private EventProxy mMarking;
/**
 * The tick event used for the test automaton for SD Singular property.
 */
private EventProxy tick;
private EventProxy Omega;
private EventProxy que;
}
