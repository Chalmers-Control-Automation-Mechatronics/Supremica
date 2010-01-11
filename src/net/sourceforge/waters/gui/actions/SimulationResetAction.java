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
    final SimulatorPanel panel = getActiveSimulatorPanel();
    if (panel != null) {
      final Simulation sim = getObservedSimulation();
      sim.reset();
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
      setEnabled(sim.getEventHistory().size() != 0);
    }
  }




  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
}
