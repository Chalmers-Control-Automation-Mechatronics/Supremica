
/*************************** ScheduleAction.java ***************/
package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.log.*;
import org.supremica.gui.ScheduleDialog;

public class ScheduleAction
	extends AbstractAction
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(ScheduleAction.class);

	public ScheduleAction()
	{
		super("Schedule...", null);

		putValue(SHORT_DESCRIPTION, "Schedule selected automata (experimental)");
	}

	public void actionPerformed(ActionEvent e)
	{
		ScheduleDialog dlg = new ScheduleDialog();

		dlg.setVisible(true);

/*              Automata automata = ActionMan.getGui().getSelectedAutomata();
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
