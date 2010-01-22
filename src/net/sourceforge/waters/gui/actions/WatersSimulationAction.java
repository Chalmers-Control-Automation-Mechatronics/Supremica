//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   GraphLayoutAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.Component;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulationChangeEvent;
import net.sourceforge.waters.gui.simulator.SimulationObserver;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


public abstract class WatersSimulationAction
  extends WatersAction
  implements SimulationObserver
{

  //#########################################################################
  //# Constructor
  WatersSimulationAction(final IDE ide)
  {
    super(ide);
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  public void update(final EditorChangedEvent event)
  {
    System.out.println("DEBUG: Updated");
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
    case MAINPANEL_SWITCH:
      final SimulatorPanel panel = getActiveSimulatorPanel();
      if (panel == null) {
        setEnabled(false);
        observeSimulation(null);
      } else {
        final Simulation sim = panel.getSimulation();
        observeSimulation(sim);
        updateEnabledStatus();
      }
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.simulation.SimulationObserver
  public void simulationChanged(final SimulationChangeEvent event)
  {
    updateEnabledStatus();
  }


   //#########################################################################
  //# Auxiliary Methods
  SimulatorPanel getActiveSimulatorPanel()
  {
    final IDE ide = getIDE();
    final DocumentContainer container = ide.getActiveDocumentContainer();
    if (container == null || !(container instanceof ModuleContainer)) {
      return null;
    }
    final ModuleContainer mcontainer = (ModuleContainer) container;
    final Component panel = mcontainer.getActivePanel();
    if (panel instanceof SimulatorPanel) {
      return (SimulatorPanel) panel;
    } else {
      return null;
    }
  }

  Simulation getObservedSimulation()
  {
    return mObservedSimulation;
  }

  void observeSimulation(final Simulation sim)
  {
    if (sim != mObservedSimulation) {
      if (mObservedSimulation != null) {
        mObservedSimulation.detach(this);
      }
      mObservedSimulation = sim;
      if (mObservedSimulation != null) {
        mObservedSimulation.attach(this);
      }
    }
  }

  abstract void updateEnabledStatus();


  //#########################################################################
  //# Data Members
  private Simulation mObservedSimulation;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
