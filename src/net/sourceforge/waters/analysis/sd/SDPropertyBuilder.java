//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   SDPropertyBuilder
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.analysis.sd;


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
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
* A converter to translate models to different models 
* for checking SD Properties.(S - Singular Prohibitable behaviour and SD four)
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
  pCounta = 0;
  getDefaultEvents();
  createEnevent(hib);

  final AutomatonProxy testAut = createS_SingularTest(hib);
  newAutomata.add(testAut);

  for (final AutomatonProxy oldAut : oldAutomata)
  {
    final AutomatonProxy newAut;

    if (oldAut.getKind()== ComponentKind.PLANT)
    {
      pCounta++;

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
      mFactory.createAutomatonProxy("Test:Aut", ComponentKind.PROPERTY,
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
return mFactory.createAutomatonProxy("PlantSigma"+ pCounta, ComponentKind.PLANT,
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
      pCountw++;
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
      mFactory.createAutomatonProxy("Test:Aut", ComponentKind.PROPERTY,
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

return mFactory.createAutomatonProxy("PlantOmega"+ pCountw, ComponentKind.PLANT,
                                     newEvents, allStates, newTransitions);
}

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
private int pCounta=0, pCountw=0;     // a Count for naming the modified Plant Automata
}
