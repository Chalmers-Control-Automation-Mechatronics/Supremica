package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.HashMap;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.supremica.gui.ide.ModuleContainer;

public class Simulation
{
  //###################################################################################
  //# Accessor Functions
  @SuppressWarnings("unchecked")
  public ArrayList<EventProxy> getValidTransitions()
  {
    return (ArrayList<EventProxy>)mValidStates.clone();
  }

  @SuppressWarnings("unchecked")
  public HashMap<EventProxy, ArrayList<AutomatonProxy>> getInvalidEvents()
  {
    return (HashMap<EventProxy,ArrayList<AutomatonProxy>>) mInvalidEvents.clone();
  }

  @SuppressWarnings("unchecked")
  public HashMap<AutomatonProxy, StateProxy> getCurrentStates()
  {
    return (HashMap<AutomatonProxy,StateProxy>) mAllAutomatons.clone();
  }

  public ArrayList<EventProxy> getAllEvents()
  {
    ArrayList<EventProxy> output = new ArrayList<EventProxy>();
    for (EventProxy e : mValidStates)
      output.add(e);
    for (EventProxy e : mInvalidEvents.keySet())
      output.add(e);
    return output;
  }

  //###################################################################################
  //# Constructor
  public Simulation(ModuleContainer module)
  {
    ProductDESProxy des = module.getCompiledDES();
    mAllAutomatons = new HashMap<AutomatonProxy, StateProxy>();
    mValidStates = new ArrayList<EventProxy>();
    mInvalidEvents = new HashMap<EventProxy, ArrayList<AutomatonProxy>> ();
    if (des != null)
    {
      for (AutomatonProxy automaton : des.getAutomata())
        for (StateProxy state : automaton.getStates())
          if (state.isInitial())
            mAllAutomatons.put(automaton, state);
      findEventClassification();
    }
    mModule = module;
    mPreviousEvents = new ArrayList<EventProxy>();
  }

  public Simulation(ModuleContainer module, ArrayList<EventProxy> events) throws UncontrollableException
  {
    this(module);
    for (EventProxy event : events)
      singleStepMutable(event);
  }

  //#####################################################################################################
  //# Mutator Methods

  public void setState(AutomatonProxy automaton, StateProxy state)
  {
    if (!automaton.getStates().contains(state))
    {
      throw new IllegalArgumentException("ERROR: This state does not belong to this automaton");
    }
    if (mAllAutomatons.containsKey(automaton))
      mAllAutomatons.put(automaton, state);
    else
      throw new IllegalArgumentException("ERROR: This automaton is not in this program");
  }



  //#####################################################################################
  //# Creation Methods
  @SuppressWarnings("unchecked")
  public Simulation singleStep(EventProxy event) throws UncontrollableException
  {
    ArrayList<EventProxy> events = (ArrayList<EventProxy>) mPreviousEvents.clone();
    events.add(event);
    Simulation output = new Simulation (mModule, events);
    return output;
  }

  //###########################################################################################
  //# Interface Object

  public boolean Equals(Object e)
  {
    if (e.getClass() != Simulation.class) return false;
    Simulation comparer = (Simulation)e;
    HashMap<AutomatonProxy,StateProxy> comparerStates = comparer.getCurrentStates();
    for (AutomatonProxy comparerAuto : comparerStates.keySet())
    {
      boolean found = false;
      for (AutomatonProxy thisAuto : mAllAutomatons.keySet())
      {
        if ((comparerAuto == thisAuto) && (comparerStates.get(comparerAuto) == mAllAutomatons.get(thisAuto)))
          found = true;
      }
      if (found == false)
        return false;
    }
    return true;
  }

  //###########################################################################################
  //# Auxillery Functions
  private Pair<EventProxy, AutomatonProxy> testForControlability()
  {
    for (EventProxy event : mInvalidEvents.keySet())
    {
      for (AutomatonProxy automata : mInvalidEvents.get(event))
      {
        if (automata.getKind() == ComponentKind.SPEC)
        {
          return new Pair<EventProxy, AutomatonProxy> (event, automata);
        }
      }
    }
    return null;
  }

  private boolean isInValidEvent (EventProxy event)
  {
    for (EventProxy validEvent : mValidStates)
    {
      if (event == validEvent)
        return true;
    }
    return false;
  }

  private boolean isInInvalidEvent(EventProxy event)
  {
    for (EventProxy invalidEvent : mInvalidEvents.keySet())
    {
      if (invalidEvent == event)
        return true;
    }
    return false;
  }

  private void findEventClassification()
  {
    for (AutomatonProxy automaton : mAllAutomatons.keySet())
    {
      for (EventProxy event : automaton.getEvents())
      {
        for (TransitionProxy transition : automaton.getTransitions())
        {
          if (transition.getSource() == mAllAutomatons.get(automaton) && transition.getEvent() == event)
          {
            if (!isInInvalidEvent(event))
            {
              if (!isInValidEvent(event))
              {
                mValidStates.add(event);
              }
            }
          }
          else if (transition.getEvent() == event)
          {
            if (isInInvalidEvent(event))
            {
              ArrayList<AutomatonProxy> got = mInvalidEvents.get(event);
              got.add(automaton);
              mInvalidEvents.put(event, got);
            }
            else if (isInValidEvent(event))
            {
              mValidStates.remove(event);
              ArrayList<AutomatonProxy> failAutomaton = new ArrayList<AutomatonProxy>();
              failAutomaton.add(automaton);
              mInvalidEvents.put(event, failAutomaton);
            }
          }
        }
      }
    }
  }

  private void singleStepMutable(EventProxy event) throws UncontrollableException
  {
    if (testForControlability() != null)
    {
      Pair<EventProxy, AutomatonProxy> invalidEvent = testForControlability();
      throw new UncontrollableException("ERROR: The event " + invalidEvent.getFirst().getName() + " is not controllable, inside the automaton " + invalidEvent.getSecond().getName());
    }
    if (isInInvalidEvent(event))
    {
      String errorMessage = "ERROR: The event " + event.getName() +
        " cannot be compiled as the following automata are blocking it:";
      for (AutomatonProxy automata : mInvalidEvents.get(event))
        errorMessage += "\r\n" + automata.getName();
      throw new IllegalArgumentException(errorMessage);
    }
    else if (!isInValidEvent(event))
    {
      String errorMessage = "ERROR: The event " + event.getName() +
        " cannot be completed as it is not inside any automata";
      throw new IllegalArgumentException(errorMessage);
    }
    else
    {
      mPreviousEvents.add(event);
      for (AutomatonProxy automata : mAllAutomatons.keySet())
      {
        for (TransitionProxy trans : automata.getTransitions())
        {
          if (trans.getEvent() == event)
          {
            if (trans.getSource() == mAllAutomatons.get(automata))
              mAllAutomatons.put(automata, trans.getTarget());
            else
              throw new IllegalArgumentException("DEBUG ERROR: Attempt to process an invalid transistion passed all tests. This shouldn't happen");
          }
        }
      }
    }
  }

  //##################################################################################################
  //# Data Members
  HashMap<AutomatonProxy, StateProxy> mAllAutomatons; // The Map object is the current state of the key
  ArrayList<EventProxy> mValidStates;
  HashMap<EventProxy, ArrayList<AutomatonProxy>> mInvalidEvents; //The Map object is the list of all the Automatons which are blocking the key
  ArrayList<EventProxy> mPreviousEvents;
  final ModuleContainer mModule;

}
