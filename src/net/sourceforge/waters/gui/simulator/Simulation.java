package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.HashMap;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

public class Simulation
{
  HashMap<AutomatonProxy, StateProxy> allAutomatons; // The Map object is the current state of the key
  ArrayList<TransitionProxy> validTransitions;
  HashMap<TransitionProxy, ArrayList<AutomatonProxy>> invalidTransitions; //The Map object is the list of all the Automatons which are blocking the key

  public Simulation(ProductDESProxy entireSimulation)
  {
    for (AutomatonProxy automaton : allAutomatons.keySet())
      for (StateProxy state : automaton.getStates())
        if (state.isInitial())
          allAutomatons.put(automaton, state);
    findTransitionClassification();
  }

  private boolean isInValidTrans (TransitionProxy trans)
  {
    for (TransitionProxy validTrans : validTransitions)
    {
      if (validTrans.getEvent().getName().compareTo(trans.getEvent().getName()) == 0)
        return true;
    }
    return false;
  }

  private boolean isInInvalidTrans(TransitionProxy trans)
  {
    for (TransitionProxy invalidTrans : invalidTransitions.keySet())
    {
      if (invalidTrans.getEvent().getName().compareTo(trans.getEvent().getName()) == 0)
        return true;
    }
    return false;
  }

  private void findTransitionClassification()
  {
    for (AutomatonProxy automaton : allAutomatons.keySet())
    {
      for (TransitionProxy transition : automaton.getTransitions())
      {
        if (transition.getSource() == allAutomatons.get(automaton))
        {
          if (!isInInvalidTrans(transition))
          {
            if (!isInValidTrans(transition))
            {
              validTransitions.add(transition);
            }
          }
        }
        else
        {
          if (isInInvalidTrans(transition))
          {
            ArrayList<AutomatonProxy> got = invalidTransitions.get(transition);
            got.add(automaton);
            invalidTransitions.put(transition, got);
          }
          else if (isInValidTrans(transition))
          {
            validTransitions.remove(transition);
            ArrayList<AutomatonProxy> failAutomaton = new ArrayList<AutomatonProxy>();
            failAutomaton.add(automaton);
            invalidTransitions.put(transition, failAutomaton);
          }
        }
      }
    }
  }

  public void singleStep(TransitionProxy transition) throws UncontrollableException
  {
    if (testForControlability() != null)
    {
      Pair<TransitionProxy, AutomatonProxy> invalidTrans = testForControlability();
      throw new UncontrollableException("ERROR: The transition " + invalidTrans.getFirst() + " is not controllable, inside the automaton " + invalidTrans.getSecond());
    }
    if (isInInvalidTrans(transition))
    {
      String errorMessage = "ERROR: The transition " + transition.getEvent().getName() +
        " cannot be compiled as the following automata are blocking it:";
      for (AutomatonProxy automata : invalidTransitions.get(transition))
        errorMessage += "\r\n" + automata.getName();
      throw new IllegalArgumentException(errorMessage);
    }
    else if (!isInValidTrans(transition))
    {
      String errorMessage = "ERROR: The transition " + transition.getEvent().getName() +
        " cannot be completed as it is not inside any automata";
      throw new IllegalArgumentException(errorMessage);
    }
    else
    {
      for (AutomatonProxy automata : allAutomatons.keySet())
      {
        for (TransitionProxy trans : automata.getTransitions())
        {
          if (trans.getEvent().getName().compareTo(transition.getEvent().getName()) == 0)
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

  private Pair<TransitionProxy, AutomatonProxy> testForControlability()
  {
    for (TransitionProxy trans : invalidTransitions.keySet())
    {
      for (AutomatonProxy automata : invalidTransitions.get(trans))
      {
        if (automata.getKind() == ComponentKind.SPEC)
        {
          return new Pair<TransitionProxy, AutomatonProxy> (trans, automata);
        }
      }
    }
    return null;
  }
}
