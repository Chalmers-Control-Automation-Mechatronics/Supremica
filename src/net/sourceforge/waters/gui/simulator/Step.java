package net.sourceforge.waters.gui.simulator;

import java.util.HashMap;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

public class Step
{

  public Step(final EventProxy event, final HashMap<AutomatonProxy, StateProxy> source, final HashMap<AutomatonProxy, StateProxy> dest)
  {
    mEvent = event;
    mSource = source;
    mDest = dest;
    determineDeterminism();
  }

  public EventProxy getEvent()
  {
    return mEvent;
  }

  public HashMap<AutomatonProxy, StateProxy> getSource()
  {
    return mSource;
  }
  public HashMap<AutomatonProxy, StateProxy> getDest()
  {
    return mDest;
  }

  public TransitionProxy getTransition(final AutomatonProxy aut)
  {
    if (mSource.get(aut) != null && mDest.get(aut) != null)
    {
      for (final TransitionProxy trans : aut.getTransitions())
      {
        if (trans.getSource() == mSource.get(aut) && trans.getTarget() == mDest.get(aut) && trans.getEvent() == mEvent)
          return trans;
      }
    }
    else
    {
      return null;
    }
    throw new IllegalArgumentException("ERROR: Somehow, the transition could not be found.");
  }

  private void determineDeterminism()
  {
    deterministic = true;
    for (final AutomatonProxy aut : mSource.keySet())
    {
      boolean valid = false;
      for (final TransitionProxy trans : aut.getTransitions())
      {
        if (trans.getSource() == mSource.get(aut) && trans.getEvent() == mEvent)
        {
          if (!valid)
            valid = true;
          else
            deterministic = false;
        }
      }
    }
  }

  public boolean isDeterministic()
  {
    return deterministic;
  }

  public String toString()
  {
    if (deterministic)
    {
      return mEvent.getName();
    }
    else
    {
      String output = mEvent.getName() + "(";
      for (final AutomatonProxy endingAut : mDest.keySet())
      {
        output += endingAut.getName() + " goes to " + mDest.get(endingAut) + ",";
      }
      return output + ")";
    }
  }

  private final EventProxy mEvent;
  private final HashMap<AutomatonProxy,StateProxy> mSource;
  private final HashMap<AutomatonProxy,StateProxy> mDest;
  private boolean deterministic;
}
