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
      final AutomatonStatus status =
        findAutomatonStatus(pred, aut, event, target);
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
      if (pred.getStatus(aut) == AutomatonStatus.DISABLED) {
        result.setState(aut, null, AutomatonStatus.DISABLED);
      } else {
        final StateProxy target = entry.getValue();
        final AutomatonStatus status =
          findAutomatonStatus(pred, aut, event, target);
        result.setState(aut, target, status);
      }
    }
    return result;
  }

  static AutomatonStatus findAutomatonStatus(final SimulatorState current,
                                             final AutomatonProxy aut,
                                             final EventProxy event,
                                             final StateProxy target)
  {
    if (current == null) {
      return AutomatonStatus.IGNORED;
    }
    final StateProxy source = current == null ? null : current.getState(aut);
    final Collection<EventProxy> local = aut.getEvents();
    if (target == null) {
      return AutomatonStatus.DISABLED;
    } else if (!local.contains(event)) {
      return AutomatonStatus.IGNORED;
    } else if (target == source) {
      return AutomatonStatus.SELFLOOPED;
    } else {
      return AutomatonStatus.OK;
    }
  }


  //#########################################################################
  //# Constructors
  SimulatorState(final Collection<AutomatonProxy> automata)
  {
    this(null, automata.size());
    for (final AutomatonProxy aut : automata) {
      StateProxy init = null;
      for (final StateProxy state : aut.getStates()) {
        if (state.isInitial()) {
          init = state;
          break;
        }
      }
      if (init != null) {
        setState(aut, init, AutomatonStatus.IGNORED);
      } else {
        mNumDisabledAutomata++;
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
    mNumDisabledAutomata = 0;
  }


  //#########################################################################
  //# Simple Access
  EventProxy getEvent()
  {
    return mEvent;
  }

  int getNumberOfDisabledAutomata()
  {
    return mNumDisabledAutomata;
  }

  StateProxy getState(final AutomatonProxy aut)
  {
    final Entry entry = mStateMap.get(aut);
    return entry == null ? null : entry.getState();
  }

  AutomatonStatus getStatus(final AutomatonProxy aut)
  {
    final Entry entry = mStateMap.get(aut);
    return entry == null ? AutomatonStatus.DISABLED : entry.getStatus();
  }

  void setState(final AutomatonProxy aut,
                final StateProxy state,
                final AutomatonStatus status)
  {
    final Entry entry = new Entry(state, status);
    final Entry old = mStateMap.put(aut, entry);
    if (old != null && old.getStatus() == AutomatonStatus.DISABLED) {
      mNumDisabledAutomata--;
    }
    if (status == AutomatonStatus.DISABLED) {
      mNumDisabledAutomata++;
    }
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    final StringBuilder buffer = new StringBuilder();
    if (mEvent == null) {
      buffer.append("(init)");
    } else {
      buffer.append(mEvent.getName());
    }
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
  /**
   * The number of disabled automata, i.e., the number of automata with status
   * {@link AutomatonStatus#DISABLED}, in this state.
   */
  private int mNumDisabledAutomata;
}
