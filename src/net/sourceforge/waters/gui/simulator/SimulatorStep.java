//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Simulator
//# PACKAGE: net.sourceforge.waters.gui.simulator
//# CLASS:   SimulatorStep
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.simulator;

import java.util.List;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * A possible step that may be executed by the simulator.
 * This helper class mainly contains the new simulator state reached
 * after execution of the step, which contains an event and successor
 * states for each automaton.
 * The SimulatorStep in addition contains the information about automata
 * that have nondeterminism, to facilitate display in the {@link
 * EventChooserDialog}.
 *
 * @author Andrew Holland, Robi Malik
 */

public class SimulatorStep
{

  //#########################################################################
  //# Constructor
  SimulatorStep(final SimulatorState next, final List<AutomatonProxy> nondet)
  {
    mNextSimulatorState = next;
    mNonDeterministicAutomata = nondet;
  }


  //#########################################################################
  //# Simple Access
  SimulatorState getNextSimulatorState()
  {
    return mNextSimulatorState;
  }

  EventProxy getEvent()
  {
    return mNextSimulatorState.getEvent();
  }

  StateProxy getTargetState(final AutomatonProxy aut)
  {
    return mNextSimulatorState.getState(aut);
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    final String name = getEvent().getName();
    if (mNonDeterministicAutomata == null) {
      return name;
    } else {
      final StringBuilder buffer = new StringBuilder(name);
      buffer.append(" {");
      boolean first = true;
      for (final AutomatonProxy aut : mNonDeterministicAutomata) {
        if (first) {
          first = false;
        } else {
          buffer.append(", ");
        }
        buffer.append(aut.getName());
        buffer.append('=');
        final StateProxy state = mNextSimulatorState.getState(aut);
        buffer.append(state.getName());
      }
      buffer.append('}');
      return buffer.toString();
    }
  }


  //#########################################################################
  //# Data Members
  private final SimulatorState mNextSimulatorState;
  private final List<AutomatonProxy> mNonDeterministicAutomata;

}
