package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;

import org.supremica.gui.ide.IDE;

public class SimulationJumpToEndAction extends WatersSimulationAction
{
  //#########################################################################
  //# Constructor
  SimulationJumpToEndAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Jump To End");
    putValue(Action.SHORT_DESCRIPTION, "Set the simulation to the final state");
    putValue(Action.SMALL_ICON, IconLoader.ICON_SIMULATOR_TO_END);
  }

  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final SimulatorPanel panel = getActiveSimulatorPanel();
    if (panel != null) {
      final Simulation sim = getObservedSimulation();
      while (sim.getCurrentTime() != sim.getEventHistory().size() - 1)
        sim.replayStep();
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
      setEnabled(sim.getEventHistory().size() != sim.getCurrentTime() + 1);
    }
  }

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
}
