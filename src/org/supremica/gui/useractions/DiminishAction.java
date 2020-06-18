//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2020 Knut Akesson, Martin Fabian, Robi Malik
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
import java.util.LinkedList;

import javax.swing.AbstractAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.AutomatonPurge;
import org.supremica.gui.ActionMan;
import org.supremica.gui.Gui;

public class DiminishAction
	extends AbstractAction
{
    private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(DiminishAction.class);

	private Automata newautomata = new Automata();
	private int statenbr = 0;

	public DiminishAction()
	{
		super("Diminish", null);

		putValue(SHORT_DESCRIPTION, "Diminish selected automata (experimental)");
	}

	@Override
  public void actionPerformed(final ActionEvent e)
	{
		logger.debug("DiminishAction::actionPerformed");

		final Gui gui = ActionMan.getGui();
		final Automata automata = gui.getSelectedAutomata();

		// Iterate over all automata
		for (final Iterator<?> autit = automata.iterator(); autit.hasNext(); )
		{
			final Automaton automaton = new Automaton((Automaton) autit.next());    // make a copy

			if (diminish(automaton))    // If any states forbidden, remove them, but delicately
			{
				purgeDelicately(automaton);
				automaton.setComment("dim(" + automaton.getName() + ")");
				automaton.setName("");
				newautomata.addAutomaton(automaton);
			}
		}

		if (newautomata.nbrOfAutomata() > 0)
		{
			try
			{
				ActionMan.gui.addAutomata(newautomata);

				newautomata = new Automata();
			}
			catch (final Exception ex)
			{
				logger.debug("DiminishAction::actionPerformed() -- ", ex);
				logger.debug(ex.getStackTrace());
			}
		}

		logger.debug("DiminishAction::actionPerformed done");
	}

	private boolean diminish(final Automaton automaton)
	{
		boolean didit = false;

		for (final Iterator<State> stit = automaton.stateIterator(); stit.hasNext(); )
		{
			final State state = stit.next();

			didit |= diminish(state);
		}

		return didit;    // true if at least one state was forbidden
	}

	// We assume all labels look like <xxx>.hi or <xxx>.lo
	// If we for a state q find both .hi and .lo for the same <xxx>, then forbid q
	private boolean diminish(final State state)
	{
		logger.debug("State" + "(" + ++statenbr + "): " + state.getName());

		for (final Iterator<Arc> arcItA = state.outgoingArcsIterator(); arcItA.hasNext(); )
		{
			final Arc arc = arcItA.next();
			final LabeledEvent event = arc.getEvent();

			for (final Iterator<Arc> arcItB = state.outgoingArcsIterator();
					arcItB.hasNext(); )
			{
				if (hiloMatch(event, arcItB.next().getEvent()))
				{
					state.setForbidden(true);
					logger.debug("Forbidding state " + state.getName());

					return true;    // yes, we did something
				}
			}
		}

		logger.debug("Nada!");

		return false;    // nothing done
	}

	// This can be much improved, ok for now
	private boolean hiloMatch(final LabeledEvent ev1, final LabeledEvent ev2)
	{
		final String label1 = ev1.getLabel();
		final String label2 = ev2.getLabel();

		// Find the index of the "."
		final int idx1 = label1.indexOf(".");
		final int idx2 = label2.indexOf(".");

		logger.debug("label1: " + label1 + " =? label2: " + label2);

		if (idx1 != idx2)    // if the lengths of the prefixes are not the same, we have no match
		{
			return false;
		}

		if (!label1.regionMatches(0, label2, 0, idx2))    // if the prefixes are not equal, we have no match
		{
			return false;
		}

		// Ok, so the prefixes are equal, do we have a hi and lo match?
		if (label1.substring(idx1 + 1).equals("hi") && label2.substring(idx2 + 1).equals("lo"))
		{
			return true;    // hi + lo match
		}

		if (label1.substring(idx1 + 1).equals("lo") && label2.substring(idx2 + 1).equals("hi"))
		{
			return true;    // lo + hi match
		}

		return false;
	}

	// Now, adjust the arcs
	// We make two passes, one for removing arcs between forbidden states
	// Next pass for adjusting the remaining arcs
	// Finally, we purge the forbidden stuff
	private void purgeDelicately(final Automaton automaton)
	{
		logger.debug("Purging delicately...");
		automaton.beginTransaction();

		// Remove all arcs between two forbidden states (is it safe to do this?)
		final LinkedList<Arc> toBeRemoved = new LinkedList<Arc>();
		for (final Iterator<?> arcit = automaton.arcIterator(); arcit.hasNext(); )
		{
			final Arc arc = (Arc) arcit.next();

			if (arc.getFromState().isForbidden() && arc.getToState().isForbidden())
			{
				toBeRemoved.add(arc);
			}
		}
		while (toBeRemoved.size() != 0)
		{
			automaton.removeArc(toBeRemoved.remove(0));
		}

		// Now, lets adjust the remaining arcs.
		// If we reach a forbidden state, we need to take that arc further down the chain
		// Do we know that it is a chain? What about nondeterminism??
		for (final Iterator<?> arcit = automaton.arcIterator(); arcit.hasNext(); )
		{
			final Arc arc = (Arc) arcit.next();
			final State nextstate = arc.getToState();

			if (nextstate.isForbidden())
			{
				// We know that from the forbidden state, we reach a non-forbidden one
				// (otherwise this transition would have been removed above)
				// We don't know if there are multiple non-forbidden states reached, however!
				for (final Iterator<?> narcit = nextstate.outgoingArcsIterator();
						narcit.hasNext(); )
				{
					final Arc newarc = (Arc) narcit.next();

					logger.debug("Add arc<" + arc.getFromState().getName() + ", " + newarc.getToState().getName() + ", " + arc.getEvent().getLabel() + ">");
					automaton.addArc(new Arc(arc.getFromState(), newarc.getToState(), arc.getEvent()));
				}

				toBeRemoved.add(arc);    // Remove it (must not remove before adjusting!!)
			}
		}
		while (toBeRemoved.size() != 0)
		{
			automaton.removeArc(toBeRemoved.remove(0));
		}

		// Remove the forbidden states
		final AutomatonPurge automatonPurge = new AutomatonPurge(automaton);

		try
		{
			automatonPurge.execute();
		}
		catch (final Exception ex)
		{
			logger.error("Exception in AutomataPurge. Automaton: " + automaton.getName(), ex);
			logger.debug(ex.getStackTrace());
		}

		/* Remove unconnected states
		for(Iterator stit = automaton.safeStateIterator(); stit.hasNext(); )
		{
				State state = stit.next();
				if(state.nbrOfIncomingArcs() == 0 && state.nbr
		}
		*/
		automaton.endTransaction();
		logger.debug("Delicate purging done");
	}
}
