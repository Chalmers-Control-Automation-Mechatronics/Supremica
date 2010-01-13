package net.sourceforge.waters.gui.simulator;

import java.util.HashMap;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

public class Step implements Comparable<Step>
{

  public Step(final EventProxy event, final HashMap<AutomatonProxy, StateProxy> source, final HashMap<AutomatonProxy, StateProxy> dest)
  {
    mEvent = event;
    mSource = source;
    mDest = dest;
    determineDeterminism();
  }
  public Step(final EventProxy event)
  {
    mEvent = event;
    mSource = new HashMap<AutomatonProxy, StateProxy>();
    mDest = new HashMap<AutomatonProxy, StateProxy>();
    determineDeterminism();
  }

  @SuppressWarnings("unchecked")
  public Step addNewTransition(final AutomatonProxy auto, final TransitionProxy trans)
  {
    final HashMap<AutomatonProxy, StateProxy> newSource = (HashMap<AutomatonProxy, StateProxy>) mSource.clone();
    final HashMap<AutomatonProxy, StateProxy> newDest = (HashMap<AutomatonProxy, StateProxy>) mDest.clone();
    if (mSource.keySet().contains(auto))
    {
      newSource.remove(auto);
      newDest.remove(auto);
    }
    newSource.put(auto, trans.getSource());
    newDest.put(auto, trans.getTarget());
    return new Step(mEvent, newSource, newDest);
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

  public TransitionProxy getTransition(final AutomatonProxy aut, final StateProxy destinationState)
  {
    if (mSource.get(aut) != null && mDest.get(aut) != null)
    {
      for (final TransitionProxy trans : aut.getTransitions())
      {
        if (trans.getSource() == mSource.get(aut) && trans.getTarget() == mDest.get(aut) && trans.getEvent() == mEvent)
          return trans;
      }
      return null;
    }
    else
    {
      for (final TransitionProxy trans : aut.getTransitions())
      {
        if (trans.getEvent() == mEvent && trans.getTarget() == destinationState)
          return trans;
      }
      return null;
    }
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


  public int compareTo(final Step o)
  {
    return this.getEvent().compareTo(o.getEvent());
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
