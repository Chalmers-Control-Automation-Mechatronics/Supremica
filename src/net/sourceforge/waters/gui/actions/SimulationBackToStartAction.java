package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;

import org.supremica.gui.ide.IDE;

public class SimulationBackToStartAction extends WatersSimulationAction
{
  //#########################################################################
  //# Constructor
  SimulationBackToStartAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Jump To Start");
    putValue(Action.SHORT_DESCRIPTION, "Return the simulation to the initial state");
    putValue(Action.SMALL_ICON, IconLoader.ICON_SIMULATOR_TO_START);
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
  }

  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final SimulatorPanel panel = getActiveSimulatorPanel();
    if (panel != null) {
      final Simulation sim = getObservedSimulation();
      while (sim.getCurrentTime() != 0)
        sim.stepBack();
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
      setEnabled(sim.getCurrentTime() != 0);
    }
  }

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
}
