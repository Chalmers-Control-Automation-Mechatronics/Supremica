/*************************** ScheduleAction.java ***************/
package org.supremica.gui.useractions;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.help.*;

import org.supremica.log.*;

import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.gui.Gui;
import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;
import org.supremica.gui.ScheduleDialog;

import org.supremica.automata.algorithms.scheduling.*;

public class ScheduleAction
	extends AbstractAction
{
	private static Logger logger = LoggerFactory.createLogger(ScheduleAction.class);

	public ScheduleAction()
	{
		super("Schedule...", null);
		putValue(SHORT_DESCRIPTION, "Schedule selected automata (experimental)");
	}

	public void actionPerformed(ActionEvent e)
	{
		ScheduleDialog dlg = new ScheduleDialog();

		dlg.show();

/*		Automata automata = ActionMan.getGui().getSelectedAutomata();
		try
		{
			ModifiedAstar mastar = new ModifiedAstar(automata);
			Element elem = mastar.walk1();
			logger.info(mastar.trace(elem));
			Automaton automaton = mastar.getAutomaton(elem);
			ActionMan.getGui().addAutomaton(automaton);
		}
		catch(Exception excp)
		{
			logger.error(excp);
		}
*/	
	}

}
