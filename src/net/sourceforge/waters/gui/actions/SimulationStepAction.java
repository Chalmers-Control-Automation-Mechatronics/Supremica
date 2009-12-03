//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   GraphLayoutAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulationObserver;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;

import org.supremica.gui.ide.IDE;


public class SimulationStepAction
  extends WatersSimulationAction
  implements SimulationObserver
{

  //#########################################################################
  //# Constructor
  SimulationStepAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Step");
    putValue(Action.SHORT_DESCRIPTION, "Execute an event");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
    putValue(Action.ACCELERATOR_KEY,
	         KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final SimulatorPanel panel = getActiveSimulatorPanel();
    if (panel != null) {
      // final Simulation sim = getObservedSimulation();
      // sim.step();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  void updateEnabledStatus()
  {
    final Simulation sim = getObservedSimulation();
    if (sim == null) {
      setEnabled(false);
    } else {
      // TODO Check the status of the simulation
      // and call setEnabled() to enable or disable this action.
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
