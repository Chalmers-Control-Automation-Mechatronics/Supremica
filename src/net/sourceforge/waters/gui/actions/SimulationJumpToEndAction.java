package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

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
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
  }

  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final SimulatorPanel panel = getObservedSimulatorPanel();
    if (panel != null) {
      final Simulation sim = panel.getSimulation();
      while (sim.getCurrentTime() != sim.getHistorySize() - 1)
        sim.replayStep();
    }
  }

  //#########################################################################
  //# Auxiliary Methods
  @Override
  void updateEnabledStatus()
  {
    final SimulatorPanel panel = getObservedSimulatorPanel();
    if (panel == null) {
      setEnabled(false);
    } else {
      final Simulation sim = panel.getSimulation();
      setEnabled(sim.getHistorySize() != sim.getCurrentTime() + 1);
    }
  }

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
}
