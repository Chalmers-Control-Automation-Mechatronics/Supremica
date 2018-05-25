//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.useractions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.gui.ActionMan;
import org.supremica.gui.VisualProject;
import org.supremica.workbench.Workbench;


public class WorkbenchAction
	extends AbstractAction
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(WorkbenchAction.class);
	private Workbench wb;

	public WorkbenchAction()
	{
		super("Workbench...", null);

		putValue(SHORT_DESCRIPTION, "Manual supervisor synthesis");
	}

	// Note, we avoid (short-circut) the ActionMan here... should we?
	@Override
  public void actionPerformed(final ActionEvent e)
	{
		// ActionMan.workbench_actionPerformed(ActionMan.getGui());

		final VisualProject theProject = ActionMan.getGui().getVisualProjectContainer().getActiveProject();
		final Automata selectedAutomata = ActionMan.getGui().getSelectedAutomata();

		try
		{
			execute(theProject, selectedAutomata);
		}
		catch (final Exception ex)
		{
			logger.error("Exception in Workbench. ", ex);
			logger.debug(ex.getStackTrace());
		}
	}

	public void execute(final VisualProject theProject, final Automata theAutomata)
		throws Exception
	{
		if (!theAutomata.sanityCheck(ActionMan.getGui().getComponent(), 1, true, true, true, true))
		{
			return;
		}

		wb = new Workbench(theProject, theAutomata);
		wb.setVisible(true);
	}
}
