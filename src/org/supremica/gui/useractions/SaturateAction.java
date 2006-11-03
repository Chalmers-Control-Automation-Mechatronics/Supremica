/******************** SaturateAction.java **********************/

// Experimental
package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.gui.Gui;
import org.supremica.gui.ActionMan;

public class SaturateAction
	extends AbstractAction
{
	private static Logger logger = LoggerFactory.createLogger(SaturateAction.class);
	private Automata new_automata = new Automata();

	public SaturateAction()
	{
		super("Saturate", null);

		putValue(SHORT_DESCRIPTION, "Saturate selected automata (experimental)");
	}

	public void actionPerformed(ActionEvent e)
	{
		logger.debug("SaturateAction::actionPerformed");

		Gui gui = ActionMan.getGui();
		Automata automata = gui.getSelectedAutomata();

		// Iterate over all automata
		for (Iterator aut_it = automata.iterator(); aut_it.hasNext(); )
		{
			Automaton aut = (Automaton) aut_it.next();

			Automaton sat_dump = new Automaton(aut);	// make two copies, one...
			boolean b1 = sat_dump.saturateDump();
			logger.debug("sat_dump.saturateDump() did " + (b1 ? "something" : "nothing"));
			sat_dump.setComment("sat_dump(" + sat_dump.getName() + ")");
			sat_dump.setName(null);
			new_automata.addAutomaton(sat_dump);

			Automaton sat_loop = new Automaton(aut);	// two
			boolean b2 = sat_loop.saturateLoop();
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
		catch (Exception ex)
		{
			logger.debug("SaturateAction::actionPerformed() -- ", ex);
			logger.debug(ex.getStackTrace());
		}

		logger.debug("Added " + i + " automata, SaturateAction::actionPerformed done");
	}
}
