package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulationObserver;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;

import org.supremica.gui.ide.IDE;

public class SimulationStepBackAction
  extends WatersSimulationAction
  implements SimulationObserver
{
  //#########################################################################
  //# Constructor
  SimulationStepBackAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Step Back");
    putValue(Action.SHORT_DESCRIPTION, "Travel back through the trace");
    putValue(Action.SMALL_ICON, IconLoader.ICON_SIMULATOR_BACK);
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.ALT_MASK));
  }

  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final SimulatorPanel panel = getActiveSimulatorPanel();
    if (panel != null) {
      final Simulation sim = getObservedSimulation();
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
