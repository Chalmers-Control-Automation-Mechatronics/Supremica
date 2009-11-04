
/********************* MakeDeterministicAction.java *****************/
package org.supremica.gui.useractions;

//import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

//import javax.help.*;
import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.gui.Gui;
import org.supremica.gui.ActionMan;

public class SplitAction
	extends AbstractAction
{
    private static final long serialVersionUID = 1L;
	private static final Logger logger =
	    LoggerFactory.createLogger(SplitAction.class);

	private Automata newautomata;

	public SplitAction()
	{
		super("Split Automaton", null);

		putValue(SHORT_DESCRIPTION, "Split automaton in two (experimental)");

		this.newautomata = new Automata();
	}

	public void actionPerformed(ActionEvent e)
	{
		logger.debug("SplitAction::actionPerformed");

		Gui gui = ActionMan.getGui();
		Automata automata = gui.getSelectedAutomata();

		// Iterate over all automata
		for (Iterator<Automaton> autit = automata.iterator(); autit.hasNext(); )
		{
			Automaton automaton = (Automaton) autit.next();

			split(new Automaton(automaton));
		}

		if (newautomata.nbrOfAutomata() > 0)
		{
			try
			{
				ActionMan.gui.addAutomata(newautomata);

				newautomata = new Automata();
			}
			catch (Exception ex)
			{
				logger.debug("SplitAction::actionPerformed() -- ", ex);
				logger.debug(ex.getStackTrace());
			}
		}

		logger.debug("SplitAction::actionPerformed done");
	}

	private void split(Automaton automaton)
	{
		Automata split = AutomatonSplit.split(automaton);

		newautomata.addAutomata(split);
	}
}
