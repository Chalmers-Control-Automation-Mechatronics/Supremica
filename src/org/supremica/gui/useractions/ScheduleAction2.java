
/*************************** ScheduleAction2.java ***************/
package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.log.*;
import org.supremica.gui.*;

/**
 *      Implements the dialog box allowing to choose different scheduling techniques.
 */
public class ScheduleAction2
	extends AbstractAction
{
	private static Logger logger = LoggerFactory.createLogger(ScheduleAction2.class);

	public ScheduleAction2()
	{
		super("Schedule_AK...", null);

		putValue(SHORT_DESCRIPTION, "Schedule selected automata (experimental)");
	}

	/**
	 *      Calls the ScheduleDialog if the number of selected automata is equal to one.
	 *      (Otherwise, synchronization is strongly recommended.)
	 */
	public void actionPerformed(ActionEvent e)
	{
		Gui gui = ActionMan.getGui();

		if (gui.getSelectedAutomata().size() == 1)
		{
			ScheduleDialog2 dlg = new ScheduleDialog2();

			dlg.show();
		}
		else
		{
			JOptionPane.showMessageDialog(gui.getComponent(), "Scheduling is not implemented for (x>1) automata. Synchronize first!", "Alert", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
