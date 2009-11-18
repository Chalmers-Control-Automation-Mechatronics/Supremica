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
  HashMap<AutomatonProxy, StateProxy> allAutomatons; // The Map object is the current state of the key
  ArrayList<EventProxy> validEvents;
  HashMap<EventProxy, ArrayList<AutomatonProxy>> invalidEvents; //The Map object is the list of all the Automatons which are blocking the key

  @SuppressWarnings("unchecked")
  public ArrayList<EventProxy> getValidTransitions()
  {
    return (ArrayList<EventProxy>)validEvents.clone();
  }

  @SuppressWarnings("unchecked")
  public HashMap<EventProxy, ArrayList<AutomatonProxy>> getInvalidEvents()
  {
    return (HashMap<EventProxy,ArrayList<AutomatonProxy>>) invalidEvents.clone();
  }

  @SuppressWarnings("unchecked")
  public HashMap<AutomatonProxy, StateProxy> getCurrentStates()
  {
    return (HashMap<AutomatonProxy,StateProxy>) allAutomatons.clone();
  }

  public ArrayList<EventProxy> getAllEvents()
  {
    ArrayList<EventProxy> output = new ArrayList<EventProxy>();
    for (EventProxy e : validEvents)
      output.add(e);
    for (EventProxy e : invalidEvents.keySet())
      output.add(e);
    return output;
  }

  public Simulation(ModuleContainer module)
  {
    ProductDESProxy des = module.getCompiledDES();
    for (AutomatonProxy automaton : des.getAutomata())
      for (StateProxy state : automaton.getStates())
        if (state.isInitial())
          allAutomatons.put(automaton, state);
    findEventClassification();
  }

  private boolean isInValidEvent (EventProxy event)
  {
    for (EventProxy validEvent : validEvents)
    {
      if (event == validEvent)
        return true;
    }
    return false;
  }

  private boolean isInInvalidEvent(EventProxy event)
  {
    for (EventProxy invalidEvent : invalidEvents.keySet())
    {
      if (invalidEvent == event)
        return true;
    }
    return false;
  }

  private void findEventClassification()
  {
    for (AutomatonProxy automaton : allAutomatons.keySet())
    {
      for (EventProxy event : automaton.getEvents())
      {
        for (TransitionProxy transition : automaton.getTransitions())
        {
          if (transition.getSource() == allAutomatons.get(automaton) && transition.getEvent() == event)
          {
            if (!isInInvalidEvent(event))
            {
              if (!isInValidEvent(event))
              {
                validEvents.add(event);
              }
            }
          }
          else if (transition.getEvent() == event)
          {
            if (isInInvalidEvent(event))
            {
              ArrayList<AutomatonProxy> got = invalidEvents.get(event);
              got.add(automaton);
              invalidEvents.put(event, got);
            }
            else if (isInValidEvent(event))
            {
              validEvents.remove(event);
              ArrayList<AutomatonProxy> failAutomaton = new ArrayList<AutomatonProxy>();
              failAutomaton.add(automaton);
              invalidEvents.put(event, failAutomaton);
            }
          }
        }
      }
    }
  }

  public void singleStep(EventProxy event) throws UncontrollableException
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
      for (AutomatonProxy automata : invalidEvents.get(event))
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
      for (AutomatonProxy automata : allAutomatons.keySet())
      {
        for (TransitionProxy trans : automata.getTransitions())
        {
          if (trans.getEvent() == event)
          {
            if (trans.getSource() == allAutomatons.get(automata))
              allAutomatons.put(automata, trans.getTarget());
            else
              throw new IllegalArgumentException("DEBUG ERROR: Attempt to process an invalid transistion passed all tests. This shouldn't happen");
          }
        }
      }
    }
  }

  private Pair<EventProxy, AutomatonProxy> testForControlability()
  {
    for (EventProxy event : invalidEvents.keySet())
    {
      for (AutomatonProxy automata : invalidEvents.get(event))
      {
        if (automata.getKind() == ComponentKind.SPEC)
        {
          return new Pair<EventProxy, AutomatonProxy> (event, automata);
        }
      }
    }
    return null;
  }
}
