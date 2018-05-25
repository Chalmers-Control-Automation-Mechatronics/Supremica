//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.simulator.AutomatonStatus;
import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.supremica.gui.ide.IDE;


/**
 * <P>The action to disable (and enable again) automata in the simulator.</P>
 *
 * <P>This action is linked to context-sensitive popup menus of the simulator,
 * and allows the user to disable the selected automaton. A disabled
 * automaton is marked with the status {@link AutomatonStatus#DISABLED},
 * which causes the simulator to stop tracking its state, and not to
 * check the state of this automaton when it is determined whether events
 * are enabled. The disabled status is carried forward to all future
 * simulation steps.</P>
 *
 * <P>An automaton can also be enabled again through this action, if it is
 * invoked in the same state where the automaton was disabled. Enablement
 * in later states is not possible because the state information may be
 * lost. However, it is also possible to enable a disabled automaton by
 * resetting the simulation to an earlier state and simply resuming from
 * there.</P>
 *
 * @author Robi Malik
 */
public class SimulationDisableAutomatonAction
  extends WatersSimulationAction
{

  //#########################################################################
  //# Constructor
  SimulationDisableAutomatonAction(final IDE ide, final AutomatonProxy aut)
  {
    super(ide);
    mAutomaton = aut;
    boolean enable = false;
    final SimulatorPanel panel = getActiveSimulatorPanel();
    if (panel != null) {
      final Simulation sim = panel.getSimulation();
      if (sim.getCurrentState(mAutomaton) != null &&
          !sim.isAutomatonEnabled(mAutomaton)) {
        enable = true;
      }
    }
    final String operation = enable ? "Enable " : "Disable ";
    final ComponentKind kind = mAutomaton.getKind();
    final String kindName = ModuleContext.getComponentKindToolTip(kind);
    final String compName = mAutomaton.getName();
    if (compName.length() + kindName.length() <= 32) {
      putValue(Action.NAME, operation + kindName + " " + compName);
    } else {
      putValue(Action.NAME, operation + kindName);
    }
    putValue(Action.SHORT_DESCRIPTION,
             operation + "this automaton for simulation");
    if (enable) {
      putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_SIMULATOR_REPLAY);
    } else {
      putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_TABLE_DISABLED_PROPERTY);
    }
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final SimulatorPanel panel = getActiveSimulatorPanel();
    if (panel != null) {
      final Simulation sim = panel.getSimulation();
      final boolean enabled = sim.isAutomatonEnabled(mAutomaton);
      sim.setAutomatonEnabled(mAutomaton, !enabled);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  @Override
  void updateEnabledStatus()
  {
    final SimulatorPanel panel = getActiveSimulatorPanel();
    if (panel != null) {
      final Simulation sim = panel.getSimulation();
      final StateProxy state = sim.getCurrentState(mAutomaton);
      setEnabled(state != null);
    } else {
      setEnabled(false);
    }
  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -6517158301347074008L;

}
