
/********************* StateEnumerator.java ***************/

// Action for implementing the state enumeration
package org.supremica.gui;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.automata.algorithms.EnumerateStates;
import org.supremica.automata.Automata;

public class StateEnumerator
	extends AbstractAction
{
	private static Logger logger = LoggerFactory.createLogger(StateEnumerator.class);

	public StateEnumerator()
	{
		putValue(NAME, "Enumerate States");
		putValue(SHORT_DESCRIPTION, "Rename states with q0, q1, etc");
	}

	public void actionPerformed(ActionEvent event)
	{

		// Get the selected automata
		Automata automata = ActionMan.getGui().getSelectedAutomata();

		// No dialog just yet, prefix is always 'q'
		EnumerateStates enumer = new EnumerateStates(automata, "q");

		enumer.execute();
	}
}
