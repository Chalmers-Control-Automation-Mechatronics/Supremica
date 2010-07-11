//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   SimulationShowAllAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.waters.gui.simulator.AutomatonDesktopPane;
import net.sourceforge.waters.gui.simulator.InternalFrameEvent;
import net.sourceforge.waters.gui.simulator.InternalFrameObserver;
import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;

import org.supremica.gui.ide.IDE;

public class SimulationShowAllAction
  extends WatersSimulationAction
  implements InternalFrameObserver
{

  //#########################################################################
  //# Constructor
  SimulationShowAllAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Show All Automata");
    putValue(Action.SHORT_DESCRIPTION, "Open all automata windows");
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent e)
  {
    final SimulatorPanel panel = getObservedSimulatorPanel();
    if (panel != null) {
      final AutomatonDesktopPane desktop = panel.getDesktop();
      desktop.showAllAutomata();
    }
  }

  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.simulator.InternalFrameObserver
  public void onFrameEvent(final InternalFrameEvent event)
  {
    updateEnabledStatus();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.actions.WatersSimulationAction
  @Override
  void updateEnabledStatus()
  {
    final SimulatorPanel panel = getObservedSimulatorPanel();
    if (panel == null) {
      setEnabled(false);
    } else {
      final Simulation sim = panel.getSimulation();
      final int numAutomata = sim.getOrderedAutomata().size();
      final AutomatonDesktopPane desktop = panel.getDesktop();
      final int numOpen = desktop.getNumberOfOpenAutomata();
      setEnabled(numOpen < numAutomata);
    }
  }

  @Override
  void observeSimulation(final SimulatorPanel panel)
  {
    final SimulatorPanel observed = getObservedSimulatorPanel();
    if (panel != observed) {
      if (observed != null) {
        final AutomatonDesktopPane desktop = observed.getDesktop();
        desktop.detach(this);
      }
      if (panel != null) {
        final AutomatonDesktopPane desktop = panel.getDesktop();
        desktop.attach(this);
      }
      super.observeSimulation(panel);
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -1644229513613033199L;

}