//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulationObserver;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;
import net.sourceforge.waters.gui.util.IconLoader;

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
    updateEnabledStatus();
  }

  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final SimulatorPanel panel = getObservedSimulatorPanel();
    if (panel != null) {
      final Simulation sim = panel.getSimulation();
      sim.resetState(false);
      SimulationAutoStepAction.toggle = false;
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
