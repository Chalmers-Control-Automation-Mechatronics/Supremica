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

import java.awt.event.ActionEvent;
import javax.swing.Action;
import net.sourceforge.waters.gui.simulator.EventStatus;
import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.util.IconLoader;
import net.sourceforge.waters.model.des.EventProxy;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

class EventExecuteAction extends WatersAction
{

  EventExecuteAction(final IDE ide, final EventProxy event)
  {
    super(ide);
    mEvent = event;
    final String name = event.getName();
    if (name.length() <= 32) {
      putValue(Action.NAME, "Execute Event " + event.getName());
    } else {
      putValue(Action.NAME, "Execute Event");
    }
    putValue(Action.SHORT_DESCRIPTION, "Execute this event");
    putValue(Action.SMALL_ICON, IconLoader.ICON_SIMULATOR_STEP);
    updateEnabledStatus();
  }

  public void actionPerformed(final ActionEvent e)
  {
    final Simulation sim = getObservedSimulation();
    sim.step(mEvent);
  }

  void updateEnabledStatus()
  {
    setEnabled(eventCanBeFired());
  }

  private Simulation getObservedSimulation()
  {
    return ((ModuleContainer)getIDE().getActiveDocumentContainer()).getSimulatorPanel().getSimulation();
  }

  private boolean eventCanBeFired()
  {
    final Simulation sim = getObservedSimulation();
    if (sim == null) {
      getIDE().error("Simulation has not been set!");
      return false;
    } else {
      final EventStatus status = sim.getEventStatus(mEvent);
      return status.canBeFired();
    }
  }

  private final EventProxy mEvent;

  private static final long serialVersionUID = -4783316648203187306L;

}
