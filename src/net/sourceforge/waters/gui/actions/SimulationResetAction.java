package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulationObserver;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;

import org.supremica.gui.ide.IDE;

public class SimulationResetAction
  extends WatersSimulationAction
  implements SimulationObserver
{
  //#########################################################################
  //# Constructor
  SimulationResetAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Reset");
    putValue(Action.SHORT_DESCRIPTION, "Reset the Simulation");
    putValue(Action.SMALL_ICON, IconLoader.ICON_SIMULATOR_RESET);
  }

  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final SimulatorPanel panel = getObservedSimulatorPanel();
    if (panel != null) {
      final Simulation sim = panel.getSimulation();
      sim.resetState(false);
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
      putValue(Action.NAME, "Reset");
      putValue(Action.SHORT_DESCRIPTION, "Reset the Simulation");
    } else {
      final Simulation sim = panel.getSimulation();
      setEnabled(sim.getHistorySize() != 0);
      if (sim.getTrace() != null) {
        putValue(Action.NAME, "Restore Trace");
        putValue(Action.SHORT_DESCRIPTION,
                 "Restore the Trace to its original state");
      } else {
        putValue(Action.NAME, "Reset");
        putValue(Action.SHORT_DESCRIPTION, "Reset the Simulation");
      }
    }
  }

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
}
