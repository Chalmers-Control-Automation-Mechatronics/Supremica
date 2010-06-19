//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Simulator
//# PACKAGE: net.sourceforge.waters.gui.simulator
//# CLASS:   SimulatorState
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.simulator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


class SimulatorState
{

  //#########################################################################
  //# Factory Methods
  static SimulatorState createInitialState
    (final Collection<AutomatonProxy> automata, final TraceStepProxy step)
  {
    final int numAutomata = automata.size();
    final SimulatorState result = new SimulatorState(null, numAutomata);
    final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
    for (final AutomatonProxy aut : automata) {
      StateProxy state = stepMap.get(aut);
      if (state == null) {
        for (final StateProxy altState : aut.getStates()) {
          if (altState.isInitial()) {
            state = altState;
            break;
          }
        }
        assert state != null :
          "No initial state found in automaton " + aut.getName() + "!";
      }
      result.setState(aut, state, AutomatonStatus.IGNORED);
    }
    return result;
  }

  static SimulatorState createSuccessorState
    (final SimulatorState pred, final EventProxy event,
     final TraceStepProxy step)
  {
    final Map<AutomatonProxy,Entry> predMap = pred.mStateMap;
    final int numAutomata = predMap.size();
    final SimulatorState result = new SimulatorState(event, numAutomata);
    final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
    for (final Map.Entry<AutomatonProxy,Entry> entry : predMap.entrySet()) {
      final AutomatonProxy aut = entry.getKey();
      StateProxy target = stepMap.get(aut);
      final Collection<EventProxy> local = aut.getEvents();
      final boolean relevant = local.contains(event);
      final StateProxy source = entry.getValue().getState();
      if (target == null && source != null) {
        if (relevant) {
          for (final TransitionProxy trans : aut.getTransitions()) {
            if (trans.getSource() == source && trans.getEvent() == event) {
              target = trans.getTarget();
              break;
            }
          }
        } else {
          target = source;
        }
      }
      final AutomatonStatus status;
      if (target == null || pred.getStatus(aut) == AutomatonStatus.DISABLED) {
        status = AutomatonStatus.DISABLED;
        target = null;
      } else if (!relevant) {
        status = AutomatonStatus.IGNORED;
      } else if (target == source) {
        status = AutomatonStatus.SELFLOOPED;
      } else {
        status = AutomatonStatus.OK;
      }
      result.setState(aut, target, status);
    }
    return result;
  }

  static SimulatorState createSuccessorState
    (final SimulatorState pred,
     final EventProxy event,
     final Map<AutomatonProxy,StateProxy> nextMap)
{
  final Map<AutomatonProxy,Entry> predMap = pred.mStateMap;
  final int numAutomata = predMap.size();
  final SimulatorState result = new SimulatorState(event, numAutomata);
  for (final Map.Entry<AutomatonProxy,StateProxy> entry : nextMap.entrySet()) {
    final AutomatonProxy aut = entry.getKey();
    final StateProxy source = pred.getState(aut);
    final Collection<EventProxy> local = aut.getEvents();
    final boolean relevant = local.contains(event);
    StateProxy target = entry.getValue();
    final AutomatonStatus status;
    if (target == null || pred.getStatus(aut) == AutomatonStatus.DISABLED) {
      status = AutomatonStatus.DISABLED;
      target = null;
    } else if (!relevant) {
      status = AutomatonStatus.IGNORED;
    } else if (target == source) {
      status = AutomatonStatus.SELFLOOPED;
    } else {
      status = AutomatonStatus.OK;
    }
    result.setState(aut, target, status);
  }
  return result;
}


  //#########################################################################
  //# Constructors
  SimulatorState(final Collection<AutomatonProxy> automata)
  {
    this(null, automata.size());
    for (final AutomatonProxy aut : automata) {
      for (final StateProxy state : aut.getStates()) {
        if (state.isInitial()) {
          setState(aut, state, AutomatonStatus.IGNORED);
          break;
        }
      }
    }
  }

  SimulatorState(final EventProxy event, final SimulatorState source)
  {
    this(event, source.size());
    for (final Map.Entry<AutomatonProxy,Entry> entry :
         source.mStateMap.entrySet()) {
      final AutomatonProxy aut = entry.getKey();
      final StateProxy state = entry.getValue().getState();
      setState(aut, state, AutomatonStatus.IGNORED);
    }
  }

  SimulatorState(final EventProxy event, final int numAutomata)
  {
    mEvent = event;
    mStateMap = new HashMap<AutomatonProxy,Entry>(numAutomata);
  }


  //#########################################################################
  //# Simple Access
  EventProxy getEvent()
  {
    return mEvent;
  }

  StateProxy getState(final AutomatonProxy aut)
  {
    final Entry entry = mStateMap.get(aut);
    return entry.getState();
  }

  AutomatonStatus getStatus(final AutomatonProxy aut)
  {
    final Entry entry = mStateMap.get(aut);
    return entry.getStatus();
  }

  void setState(final AutomatonProxy aut,
                final StateProxy state,
                final AutomatonStatus status)
  {
    final Entry entry = new Entry(state, status);
    mStateMap.put(aut, entry);
  }

  void addStatus(final AutomatonProxy aut, final AutomatonStatus status)
  {
    final Entry entry = mStateMap.get(aut);
    if (status.compareTo(entry.getStatus()) > 0) {
      final StateProxy state = entry.getState();
      final Entry newEntry = new Entry(state, status);
      mStateMap.put(aut, newEntry);
    }
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    final StringBuffer buffer = new StringBuffer(mEvent.getName());
    buffer.append(" -> [");
    boolean first = true;
    for (final Map.Entry<AutomatonProxy,Entry> entry : mStateMap.entrySet()) {
      final AutomatonProxy aut = entry.getKey();
      final StateProxy state = entry.getValue().getState();
      if (first) {
        first = false;
      } else {
        buffer.append(", ");
      }
      buffer.append(aut.getName());
      buffer.append("=");
      buffer.append(state.getName());
    }
    buffer.append("]");
    return buffer.toString();
  }


  //#########################################################################
  //# Auxiliary Methods
  int size()
  {
    return mStateMap.size();
  }


  //#########################################################################
  //# Inner Class Entry
  private static class Entry
  {
    //#######################################################################
    //# Constructor
    private Entry(final StateProxy state, final AutomatonStatus status)
    {
      mState = state;
      mStatus = status;
    }

    //#######################################################################
    //# Simple Access
    private StateProxy getState()
    {
      return mState;
    }

    private AutomatonStatus getStatus()
    {
      return mStatus;
    }

    //#######################################################################
    //# Data Members
    private final StateProxy mState;
    private final AutomatonStatus mStatus;
  }


  //#########################################################################
  //# Data Members
  /**
   * The event that took the simulation into this state, or <CODE>null</CODE>
   * to indicate an initial state.
   */
  private final EventProxy mEvent;

  /**
   * Maps automata to their current states.
   * Missing entries may exist for disabled properties.
   */
  private final Map<AutomatonProxy,Entry> mStateMap;

}
