//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
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
import java.util.Iterator;

import javax.swing.AbstractAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.gui.ActionMan;
import org.supremica.gui.Gui;

public class SaturateAction
	extends AbstractAction
{
    private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(SaturateAction.class);
	private Automata new_automata = new Automata();

	public SaturateAction()
	{
		super("Saturate", null);

		putValue(SHORT_DESCRIPTION, "Saturate selected automata (experimental)");
	}

	@Override
  public void actionPerformed(final ActionEvent e)
	{
		logger.debug("SaturateAction::actionPerformed");

		final Gui gui = ActionMan.getGui();
		final Automata automata = gui.getSelectedAutomata();

		// Iterate over all automata
		for (final Iterator<Automaton> aut_it = automata.iterator(); aut_it.hasNext(); )
		{
			final Automaton aut = aut_it.next();

			final Automaton sat_dump = new Automaton(aut);	// make two copies, one...
			final boolean b1 = sat_dump.saturateDump();
			logger.debug("sat_dump.saturateDump() did " + (b1 ? "something" : "nothing"));
			sat_dump.setComment("sat_dump(" + sat_dump.getName() + ")");
			sat_dump.setName(null);
			new_automata.addAutomaton(sat_dump);

			final Automaton sat_loop = new Automaton(aut);	// two
			final boolean b2 = sat_loop.saturateLoop();
			logger.debug("sat_loop.saturateLoop() did " + (b2 ? "something" : "nothing"));
			sat_loop.setComment("sat_loop(" + sat_loop.getName() + ")");
			sat_loop.setName(null);
			new_automata.addAutomaton(sat_loop);
		}

		logger.debug("Adding " + new_automata.size() + " automata");

		int i = 0;
		try
		{
			i = ActionMan.gui.addAutomata(new_automata);
			new_automata = new Automata();	// else the garbage collector won't do its job
		}
		catch (final Exception ex)
		{
			logger.debug("SaturateAction::actionPerformed() -- ", ex);
			logger.debug(ex.getStackTrace());
		}

		logger.debug("Added " + i + " automata, SaturateAction::actionPerformed done");
	}
}
