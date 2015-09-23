//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.Action;

import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.module.NodeProxy;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


public class DesktopSwitchStateAction extends WatersAction
{

  //#########################################################################
  //# Constructors
  protected DesktopSwitchStateAction(final IDE ide,
                                     final AutomatonProxy autoToChange,
                                     final NodeProxy node)
  {
    super(ide);
    mAutomaton = autoToChange;
    mState = null;
    String name = null;
    final ModuleContainer container =
      (ModuleContainer) ide.getActiveDocumentContainer();
    final Map<Object,SourceInfo> infomap = container.getSourceInfoMap();
    for (final StateProxy state : mAutomaton.getStates()) {
      if (infomap.get(state).getSourceObject() == node) {
        mState = state;
        name = state.getName();
        if (name.length() > 32) {
          name = null;
        }
        break;
      }
    }
    if (name == null) {
      putValue(Action.NAME, "Change to this State");
    } else {
      putValue(Action.NAME, "Change to State " + name);
    }
    putValue(Action.SHORT_DESCRIPTION,
             "Change the automaton state to this state");
    setEnabled(true);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    getSimulation().setState(mAutomaton, mState);
  }


  //#########################################################################
  //# Auxiliary Methods
  public Simulation getSimulation()
  {
    final IDE ide = getIDE();
    final DocumentContainer container = ide.getActiveDocumentContainer();
    if (container == null || !(container instanceof ModuleContainer)) {
      return null;
    }
    final ModuleContainer mcontainer = (ModuleContainer) container;
    final Component panel = mcontainer.getActivePanel();
    if (panel instanceof SimulatorPanel) {
      return ((SimulatorPanel) panel).getSimulation();
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private StateProxy mState;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -1644229513613033199L;

}
