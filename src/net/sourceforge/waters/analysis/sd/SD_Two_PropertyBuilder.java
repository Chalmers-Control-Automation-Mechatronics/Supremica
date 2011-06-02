
//###########################################################################

package net.sourceforge.waters.analysis.sd;


import java.util.ArrayList;
import java.util.Collection;
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
* A converter to translate models to different models
* for checking SD Properties.
*
* @author Mahvash Baloch, Robi Malik
*/

public class SD_Two_PropertyBuilder
{

//#########################################################################
//# Constructors
public SD_Two_PropertyBuilder(final ProductDESProxyFactory factory)
{
  mFactory = factory;
  mModel = null;
}

public SD_Two_PropertyBuilder(final ProductDESProxy model,
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
 * Builds a model for checking  ii (a) Property
 */
public ProductDESProxy createSDTwoModel()
{

  final Collection<AutomatonProxy> oldAutomata = mModel.getAutomata();

  final int numaut = oldAutomata.size();
  final List<AutomatonProxy> newAutomata =
    new ArrayList<AutomatonProxy>(numaut);

  final Collection <EventProxy> tEvents = new ArrayList<EventProxy>();
  final Collection<EventProxy> allEvents = mModel.getEvents();

  getDefaultEvents();
  createEnEvent();

  for (final AutomatonProxy oldAut : oldAutomata)
  {
    final AutomatonProxy newAut;
    newAut = modifyAut(oldAut);
    newAutomata.add(newAut);
   }

  for(final EventProxy eve: allEvents)
   tEvents.add(eve);
   tEvents.add(enEvent);


   // create the Test Automata
  final AutomatonProxy testAut = createSD2_Test();
  newAutomata.add(testAut);

  final String desname = mModel.getName();
  final String name = desname + "-SD-ii";
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
 * SD controllability part ii.a
 */
private AutomatonProxy createSD2_Test()
{
  final Collection<EventProxy> allEvents = mModel.getEvents();

  final Collection<EventProxy> newEvents = new ArrayList<EventProxy>();
  final Collection<StateProxy> states = new ArrayList<StateProxy>(2);

  // creates the 2 states needed

  //State 1
 final StateProxy initialState =
    mFactory.createStateProxy("S0", true, null);
    states.add(initialState);
  // next  state
  final StateProxy t2State =
      mFactory.createStateProxy("S1", false, null);
  states.add(t2State);

  // creates the transitions needed
  final Collection<TransitionProxy> transitions = new ArrayList<TransitionProxy>();

   // self loop all Sigma Events on the initial state
  for(final EventProxy ev: allEvents)
  {
    final TransitionProxy Sigmatransition =
          mFactory.createTransitionProxy(initialState, ev, initialState);
    transitions.add(Sigmatransition);
    newEvents.add(ev);
  }

    // the transitions which accepts the etick event
    final TransitionProxy etickTransition1 =
          mFactory.createTransitionProxy(initialState, enEvent, t2State);
      transitions.add(etickTransition1);

    final TransitionProxy etickTransition2 =
        mFactory.createTransitionProxy(t2State, enEvent, t2State);
    transitions.add(etickTransition2);

    final Collection<EventProxy> hibEvents = getHibEvents();

    // the transitions which accepts non-prohibitable events from the event list
    for(final EventProxy ev: allEvents)
    {
      if(!(hibEvents.contains(ev)))
        {
        final TransitionProxy nhibTransition =
          mFactory.createTransitionProxy(t2State, ev, initialState);
        transitions.add(nhibTransition);
        }
    }

  newEvents.add(enEvent);

  final AutomatonProxy newTestAut =
      mFactory.createAutomatonProxy("TestSD2", ComponentKind.PROPERTY,
                                   newEvents, states, transitions);

  return newTestAut;
}

/**
 * Modifies the Plant component to constructs the E-tick automaton
 * to check SD controllability property ii
 */

private AutomatonProxy modifyAut (final AutomatonProxy aut)
{
  final List<TransitionProxy> newTransitions =
    new ArrayList<TransitionProxy>();

final List<EventProxy> newEvents = new ArrayList<EventProxy>();

final Collection<EventProxy> allEvents = aut.getEvents();

for (final EventProxy event: allEvents )
  newEvents.add(event);

final Collection<TransitionProxy> alltransitions = aut.getTransitions();
final Collection<StateProxy> allStates = aut.getStates();

for (final TransitionProxy transition : alltransitions) {
  newTransitions.add(transition);
  final EventProxy ev = transition.getEvent();

  if((ev.equals(tick)))
  {
     final StateProxy state = transition.getSource();

     final TransitionProxy enableEvent =
           mFactory.createTransitionProxy(state, enEvent, state);
        newTransitions.add(enableEvent);
 }
  }

newEvents.add(enEvent);

return mFactory.createAutomatonProxy(aut.getName(), aut.getKind(),
                                     newEvents, allStates, newTransitions);
}

/* ------------------------------------------------------------------------------------
 * PART B
 */


/**
 * Builds a model for checking  ii (b) Property
 */

public ProductDESProxy createSDTwo_bModel()
{

  final Collection<AutomatonProxy> oldAutomata = mModel.getAutomata(); //get the automata
  final Collection<EventProxy> allEvents = mModel.getEvents();

  final int numaut = oldAutomata.size();
  final List<AutomatonProxy> newAutomata =               // create a new array for new ones
    new ArrayList<AutomatonProxy>(numaut);
  final List<AutomatonProxy> tempnewAutomata = new ArrayList<AutomatonProxy>(numaut);
  final Collection <EventProxy> tevents = new ArrayList<EventProxy>();  //total events

  getDefaultEvents();
  disablEvents.clear();
  pCounta = 0;
  for (final AutomatonProxy oldyAut : oldAutomata) {
    AutomatonProxy newAut;
   if (oldyAut.getKind()== ComponentKind.PLANT)
    { pCounta++;
      createDEvent();
      newAut = modiAutSD2(oldyAut);
      final Collection<EventProxy> autEvents = newAut.getEvents();
      if(autEvents.contains(dEvent))
      {
        disablEvents.add(dEvent);
        tevents.add(dEvent);
      }
      }
   else { newAut = oldyAut; }
   tempnewAutomata.add(newAut);
  }

 pCounta =0;
  for (final AutomatonProxy oldAut : tempnewAutomata)
  { AutomatonProxy newAut;
    newAut = markAut(oldAut);
    newAutomata.add(newAut);
  }

  final AutomatonProxy testAut = createSD2Test();
  newAutomata.add(testAut);

  for(final EventProxy event: allEvents)
    tevents.add(event);
    tevents.add(que);

  final String desname = mModel.getName();
  final String name = desname + "-SD ii";
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
 * SD ii part b
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

    final EventKind kind= event.getKind();

    if((kind.equals(EventKind.CONTROLLABLE)))
      UpsilonEvents.add(event);


      if(!(kind.equals(EventKind.PROPOSITION)))
        SigmaEvents.add(event);

      newEvents.add(event);
   }

  for (final EventProxy ev : disablEvents) {
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
 * Modifies the Plant component to construct the D-tick automaton with all states
 * marked to check Sd ii part b
 */

private AutomatonProxy modiAutSD2 (final AutomatonProxy aut)
{
  final Collection<EventProxy> allEvents = aut.getEvents();
  final Collection<EventProxy> newEvents = new ArrayList<EventProxy>();

  final Collection<StateProxy> oldStates = aut.getStates();

  final List<TransitionProxy> allTransitions = new ArrayList<TransitionProxy>();

 for (final EventProxy event: allEvents )
  newEvents.add(event);

 final Collection<TransitionProxy> oldTransitions = aut.getTransitions();
 final List<StateProxy> mState=new ArrayList<StateProxy>();

for (final TransitionProxy transition : oldTransitions) {
  allTransitions.add(transition);
  final EventProxy ev = transition.getEvent();

  final StateProxy state = transition.getSource();
  if((ev.equals(tick)))
  {
    mState.add(state);
      }
  }

 for (final StateProxy st: oldStates) {
   if (mState == null)
   {
     final TransitionProxy disEvent =
       mFactory.createTransitionProxy(st, dEvent, st);
       allTransitions.add(disEvent);
   }
   else
   if (!mState.contains(st))
    {
     final TransitionProxy disEvent =
      mFactory.createTransitionProxy(st, dEvent, st);
      allTransitions.add(disEvent);
    }
}

 if (!(mState.containsAll(oldStates)))
 newEvents.add(dEvent);

return mFactory.createAutomatonProxy(aut.getName(), aut.getKind(),
                                     newEvents, oldStates, allTransitions);
}

// marks all states in the Automata
private AutomatonProxy markAut (final AutomatonProxy aut)
{

final Collection<EventProxy> allEvents = aut.getEvents();
final ComponentKind kind = aut.getKind();
Collection <EventProxy> mProps = null;
final Collection<StateProxy> oldStates = aut.getStates();
final int numStates = oldStates.size();
final Collection<StateProxy> newStates = new ArrayList<StateProxy>(numStates);

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

if (mMarking == null) {
  mMarking =
      mFactory.createEventProxy(EventDeclProxy.DEFAULT_MARKING_NAME,
                                EventKind.PROPOSITION, true);
}}

}
private void createEnEvent()
{
  final String name = tick.getName();
  final String eEventName = "e"+name;
    enEvent =
  mFactory.createEventProxy(eEventName, EventKind.CONTROLLABLE , true);

 }

private void createDEvent()
{
  final String name = "tick";
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

private final Map<StateProxy,StateProxy> mStateMap =
  new HashMap<StateProxy,StateProxy>();

private final ProductDESProxyFactory mFactory;
List<EventProxy> disablEvents = new ArrayList<EventProxy>();
private EventProxy enEvent;
private EventProxy dEvent;
/**
 * The Default marking proposition
 */
 private EventProxy mMarking;
/**
 * The tick event used for the test automaton for SD Singular property.
 */
private EventProxy tick;
private EventProxy que;

private int pCounta=0;    // Counts for naming disable events
}
