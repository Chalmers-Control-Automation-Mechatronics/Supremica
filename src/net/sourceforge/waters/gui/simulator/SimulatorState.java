package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;

public class SimulatorState
{
  public SimulatorState(final Simulation sim)
  {
    mCurrentStates = new HashMap<AutomatonProxy, StateProxy>(sim.getCurrentStates());
    mEnabledEvents = new ArrayList<Step>(sim.getEnabledEvents());
    mInvalidEvents = new HashMap<EventProxy, ArrayList<AutomatonProxy>>(sim.getInvalidEvents());
    mCurrentEvent = sim.getCurrentEvent();
    currentTime = sim.getCurrentTime();
    if (sim.getAllBlocking() == null)
      mBlockingEvents = new ArrayList<Pair<EventProxy, AutomatonProxy>>();
    else
      mBlockingEvents = new ArrayList<Pair<EventProxy, AutomatonProxy>>(sim.getAllBlocking());
    mEnabledLastStep = new ArrayList<AutomatonProxy>(sim.getAutomatonActivity());
    mDisabledProperties = new ArrayList<AutomatonProxy>(sim.getDisabledProperties());
  }

  public Map<AutomatonProxy,StateProxy> mCurrentStates; // The Map object is the current state of the key
  public ArrayList<Step> mEnabledEvents;
  public HashMap<EventProxy, ArrayList<AutomatonProxy>> mInvalidEvents; //The Map object is the list of all the Automatons which are blocking the event
  public Step mCurrentEvent;
  public int currentTime; // The index representing the current index for the current version history.
  public ArrayList<Pair<EventProxy, AutomatonProxy>> mBlockingEvents;
  public ArrayList<AutomatonProxy> mEnabledLastStep;
  public ArrayList<AutomatonProxy> mDisabledProperties;
}
