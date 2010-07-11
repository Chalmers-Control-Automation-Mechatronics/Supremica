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
import java.util.List;

import javax.swing.Action;
import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulationObserver;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;
import net.sourceforge.waters.gui.simulator.SimulatorStep;
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
    putValue(Action.SMALL_ICON, IconLoader.ICON_SIMULATOR_STEP);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final SimulatorPanel panel = getObservedSimulatorPanel();
    if (panel != null) {
      final Simulation sim = panel.getSimulation();
      final List<SimulatorStep> possibleEvents = sim.getEnabledSteps();
      if (possibleEvents.isEmpty()) {
        getIDE().error("No events are enabled.");
      } else {
        sim.step(possibleEvents);
      }
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
      setEnabled(sim.getEnabledSteps().size() != 0);
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
