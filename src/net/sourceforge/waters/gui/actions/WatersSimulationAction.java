//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
    case MAINPANEL_SWITCH:
      final SimulatorPanel panel = getActiveSimulatorPanel();
      if (panel == null) {
        setEnabled(false);
        observeSimulation(null);
      } else {
        observeSimulation(panel);
        updateEnabledStatus();
      }
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.simulation.SimulationObserver
  @Override
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

  SimulatorPanel getObservedSimulatorPanel()
  {
    return mObservedSimulatorPanel;
  }

  void observeSimulation(final SimulatorPanel panel)
  {
    if (panel != mObservedSimulatorPanel) {
      if (mObservedSimulatorPanel != null) {
        final Simulation sim = mObservedSimulatorPanel.getSimulation();
        sim.detach(this);
      }
      mObservedSimulatorPanel = panel;
      if (mObservedSimulatorPanel != null) {
        final Simulation sim = panel.getSimulation();
        sim.attach(this);
      }
    }
  }


  //#########################################################################
  //# Abstract Methods
  abstract void updateEnabledStatus();


  //#########################################################################
  //# Data Members
  private SimulatorPanel mObservedSimulatorPanel;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
