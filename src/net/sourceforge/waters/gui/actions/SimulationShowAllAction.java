//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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
