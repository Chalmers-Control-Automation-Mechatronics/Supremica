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
import java.util.List;
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
* A converter to translate models to different models
* for checking SD Properties.
*
* @author Mahvash Baloch, Robi Malik
*/

public class SD_three_PropertyBuilder
{

//#########################################################################
//# Constructors
public SD_three_PropertyBuilder(final ProductDESProxyFactory factory)
{
  mFactory = factory;
  mModel = null;
}

public SD_three_PropertyBuilder(final ProductDESProxy model,
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
 * Builds a model for checking  iii.1 (a) Property
 */
public ProductDESProxy createSDThreeModel(final EventProxy hib)
{

  final Collection<AutomatonProxy> oldAutomata = mModel.getAutomata();

  final int numaut = oldAutomata.size();
  final List<AutomatonProxy> newAutomata =
    new ArrayList<AutomatonProxy>(numaut);

  final Collection <EventProxy> tEvents = new ArrayList<EventProxy>();
  final Collection<EventProxy> allEvents = mModel.getEvents();
  pCounta = 0;
  disablEvents.clear();

  getDefaultEvents();
  createEnEvent(hib);


  for (final AutomatonProxy oldAut : oldAutomata)
  {
    final AutomatonProxy newAut;
    final Collection<EventProxy> autEvents = oldAut.getEvents();
    if (autEvents.contains(hib))
      {
      pCounta++;
      createDEvent(hib);
      newAut = modifyAut(oldAut, hib);
      disablEvents.add(dEvent);
      tEvents.add(dEvent);
      }
    else
      newAut = oldAut;

  newAutomata.add(newAut);
  }

  for(final EventProxy eve: allEvents)
   tEvents.add(eve);
   tEvents.add(enEvent);


   // create the Test Automata
  final AutomatonProxy testAut = createSD3_Test(hib);
  newAutomata.add(testAut);

  final String desname = mModel.getName();
  final String name = desname + "-SD-iii.1";
  final String comment =
    "Automatically generated from '" + desname +
    "' to check SD controllable Behavior";

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
private AutomatonProxy createSD3_Test(final EventProxy hib)
{
  final Collection<EventProxy> newEvents = new ArrayList<EventProxy>();
  final Collection<StateProxy> states = new ArrayList<StateProxy>(3);

  // creates the 3 states needed

  //State 1
 final StateProxy initialState =
    mFactory.createStateProxy("S0", true, null);
    states.add(initialState);
  // next  state
  final StateProxy t2State =
      mFactory.createStateProxy("S1", false, null);
  states.add(t2State);
  // next state
  final StateProxy t3State =
    mFactory.createStateProxy("S2", false, null);
  states.add(t3State);


  // creates the transitions needed
  final Collection<TransitionProxy> transitions = new ArrayList<TransitionProxy>();

   // self loop tick on the initial state
    final TransitionProxy transition =
        mFactory.createTransitionProxy(initialState, tick, initialState);
    transitions.add(transition);


    //self loop all disable events for Sigma-hib Event on initial state and State 3
    for(final EventProxy ev: disablEvents)
    {
      final TransitionProxy dtransition1 =
      mFactory.createTransitionProxy(initialState, ev, initialState);
    transitions.add(dtransition1);

    final TransitionProxy dtransition2 =
      mFactory.createTransitionProxy(t3State, ev, t3State);
    transitions.add(dtransition2);

    }

        // the transitions which accepts the Sigma-hib event from the event alphabet
    final TransitionProxy SigmaTransition1 =
          mFactory.createTransitionProxy(initialState, hib, t3State);
      transitions.add(SigmaTransition1);

    final TransitionProxy SigmaTransition2 =
        mFactory.createTransitionProxy(t2State, hib, t3State);
    transitions.add(SigmaTransition2);

    final TransitionProxy SigmaTransition3 =
      mFactory.createTransitionProxy(t3State, hib, t3State);
  transitions.add(SigmaTransition3);



    // the transitions which accepts only tick
    final TransitionProxy tickTransition1 =
          mFactory.createTransitionProxy(t3State, tick, initialState);
    transitions.add(tickTransition1);
    final TransitionProxy tickTransition2 =
          mFactory.createTransitionProxy(t2State, tick, initialState);
    transitions.add(tickTransition2);

  // the transitions which accepts the enable event
    final TransitionProxy enableTransition1 =
          mFactory.createTransitionProxy(t3State, enEvent, t3State);
     transitions.add(enableTransition1);
    final TransitionProxy enableTransition2 =
        mFactory.createTransitionProxy(t2State, enEvent, t2State);
    transitions.add(enableTransition2);
    final TransitionProxy enableTransition3 =
      mFactory.createTransitionProxy(initialState, enEvent, t2State);
    transitions.add(enableTransition3);


  newEvents.add(hib);
  newEvents.add(enEvent);
  newEvents.add(tick);

  for (final EventProxy ev: disablEvents)
    newEvents.add(ev);


  final AutomatonProxy newTestAut =
      mFactory.createAutomatonProxy("TestSD3", ComponentKind.PROPERTY,
                                   newEvents, states, transitions);

  return newTestAut;
}

/**
 * Modifies the Plant component to constructs the ED-sigma automaton
 * to check SD controllability property iii.1
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
final List<StateProxy> mState=new ArrayList<StateProxy>();

for (final TransitionProxy transition : alltransitions) {
  newTransitions.add(transition);
  final EventProxy ev = transition.getEvent();

  final String name = ev.getName();

  final StateProxy state = transition.getSource();
  if((name.equals(hName)))
  {
    mState.add(state);
  }
}

 for (final StateProxy st: allStates) {
   if (mState == null)
   {
     final TransitionProxy disEvent =
       mFactory.createTransitionProxy(st, dEvent, st);
       newTransitions.add(disEvent);
   }
   else
     if(mState.contains(st))
     { final TransitionProxy disEvent =
         mFactory.createTransitionProxy(st, enEvent, st);
         newTransitions.add(disEvent);}
     else
   if (!mState.contains(st))
    { final TransitionProxy disEvent =
      mFactory.createTransitionProxy(st, dEvent, st);
      newTransitions.add(disEvent);
    }

}

newEvents.add(enEvent);
newEvents.add(dEvent);

return mFactory.createAutomatonProxy(aut.getName(), aut.getKind(),
                                     newEvents, allStates, newTransitions);
}

/* ------------------------------------------------------------------------------------
 * PART B
 */


/**
 * Builds a model for checking  iii.1 (b) Property
 */
public ProductDESProxy createSDThree_bModel(final EventProxy hib)
{

  final Collection<AutomatonProxy> oldAutomata = mModel.getAutomata();

  final int numaut = oldAutomata.size();
  final List<AutomatonProxy> newAutomata =
    new ArrayList<AutomatonProxy>(numaut);

  final Collection <EventProxy> tEvents = new ArrayList<EventProxy>();
  final Collection<EventProxy> allEvents = mModel.getEvents();
  pCounta = 0;
  disablEvents.clear();

  getDefaultEvents();

  for (final AutomatonProxy oldAut : oldAutomata)
  {
    final AutomatonProxy newAut;
    final Collection<EventProxy> autEvents = oldAut.getEvents();
    if (autEvents.contains(hib))
      {
      pCounta++;
      createDEvent(hib);
      newAut = modifyAutb(oldAut, hib);
      disablEvents.add(dEvent);
      tEvents.add(dEvent);
      }
    else
      newAut = oldAut;

  newAutomata.add(newAut);
  }

  for(final EventProxy eve: allEvents)
   tEvents.add(eve);



   // create the Test Automata
  final AutomatonProxy testAut = createSD3b_Test(hib);
  newAutomata.add(testAut);

  final String desname = mModel.getName();
  final String name = desname + "-SD-iii.1";
  final String comment =
    "Automatically generated from '" + desname +
    "' to check SD controllable Behavior";

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
private AutomatonProxy createSD3b_Test(final EventProxy hib)
{
  final Collection<EventProxy> newEvents = new ArrayList<EventProxy>();
  final Collection<StateProxy> states = new ArrayList<StateProxy>(3);

  // creates the 3 states needed

  //State 1
 final StateProxy initialState =
    mFactory.createStateProxy("S0", true, null);
    states.add(initialState);
  // next  state
  final StateProxy t2State =
      mFactory.createStateProxy("S1", false, null);
  states.add(t2State);
  // next state
  final StateProxy t3State =
    mFactory.createStateProxy("S2", false, null);
  states.add(t3State);


  // creates the transitions needed
  final Collection<TransitionProxy> transitions = new ArrayList<TransitionProxy>();

   // self loop tick on the initial state
    final TransitionProxy transition =
        mFactory.createTransitionProxy(initialState, tick, initialState);
    transitions.add(transition);


    //self loop all disable events for Sigma-hib Event on initial state and State 3
    for(final EventProxy ev: disablEvents)
    {
      final TransitionProxy dtransition1 =
      mFactory.createTransitionProxy(t2State, ev, t2State);
    transitions.add(dtransition1);

    final TransitionProxy dtransition2 =
      mFactory.createTransitionProxy(t3State, ev, t3State);
    transitions.add(dtransition2);

    final TransitionProxy dtransition3 =
      mFactory.createTransitionProxy(initialState, ev, t2State);
    transitions.add(dtransition3);

    }

        // the transitions which accepts the Sigma-hib event from the event alphabet
    final TransitionProxy SigmaTransition1 =
          mFactory.createTransitionProxy(initialState, hib, t3State);
      transitions.add(SigmaTransition1);

    final TransitionProxy SigmaTransition3 =
      mFactory.createTransitionProxy(t3State, hib, t3State);
  transitions.add(SigmaTransition3);



    // the transitions which accepts only tick
    final TransitionProxy tickTransition1 =
          mFactory.createTransitionProxy(t3State, tick, initialState);
    transitions.add(tickTransition1);
    final TransitionProxy tickTransition2 =
          mFactory.createTransitionProxy(t2State, tick, initialState);
    transitions.add(tickTransition2);

  newEvents.add(hib);
  newEvents.add(tick);

  for (final EventProxy ev: disablEvents)
    newEvents.add(ev);


  final AutomatonProxy newTestAut =
      mFactory.createAutomatonProxy("Test:Aut", ComponentKind.PROPERTY,
                                   newEvents, states, transitions);

  return newTestAut;
}

/**
 * Modifies the Plant component to constructs the ED-sigma automaton
 * to check SD controllability property iii.1
 */

private AutomatonProxy modifyAutb (final AutomatonProxy aut, final EventProxy hib)
{
  final List<TransitionProxy> newTransitions =
    new ArrayList<TransitionProxy>();

final List<EventProxy> newEvents = new ArrayList<EventProxy>();

final Collection<EventProxy> allEvents = aut.getEvents();

for (final EventProxy event: allEvents )
  newEvents.add(event);

final Collection<TransitionProxy> alltransitions = aut.getTransitions();
final Collection<StateProxy> allStates = aut.getStates();
final List<StateProxy> mState=new ArrayList<StateProxy>();
final String hName = hib.getName();


for (final TransitionProxy transition : alltransitions) {
  newTransitions.add(transition);
  final EventProxy ev = transition.getEvent();

  final String name = ev.getName();

  final StateProxy state = transition.getSource();
  if((name.equals(hName)))
  {
    mState.add(state);
  }
}

 for (final StateProxy st: allStates) {
   if (mState == null)
   {
     final TransitionProxy disEvent =
       mFactory.createTransitionProxy(st, dEvent, st);
       newTransitions.add(disEvent);
   }
   else
   if (!mState.contains(st))
    { final TransitionProxy disEvent =
      mFactory.createTransitionProxy(st, dEvent, st);
      newTransitions.add(disEvent);
    }

}

newEvents.add(dEvent);

return mFactory.createAutomatonProxy(aut.getName(), aut.getKind(),
                                     newEvents, allStates, newTransitions);
}



// --------------------- END OF PART B ---------------------------------------------

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

private void createEnEvent
   (final EventProxy hibevent)
{
  final String name = hibevent.getName();
  final String eEventName = "e"+name;
    enEvent =
  mFactory.createEventProxy(eEventName, EventKind.CONTROLLABLE , true);

 }

private void createDEvent (final EventProxy hibevent)
{
  final String name = hibevent.getName();
  final String dEventName = "d"+name+ pCounta;
  dEvent =
    mFactory.createEventProxy(dEventName, EventKind.CONTROLLABLE, true);
  }
//#########################################################################
//# Data Members
/**
 * The model which is being changed.
 */
private final ProductDESProxy mModel;

private final ProductDESProxyFactory mFactory;

private EventProxy enEvent;
private EventProxy dEvent;

List<EventProxy> disablEvents = new ArrayList<EventProxy>(0);
/**
 * The Default marking proposition
 */
 private EventProxy mMarking;
/**
 * The tick event used for the test automaton for SD Singular property.
 */
private EventProxy tick;

private int pCounta=0;    // Counts for naming disable events
}
