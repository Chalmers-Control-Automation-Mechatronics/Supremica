
/*************************** ScheduleAction.java ***************/
package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.log.*;
import org.supremica.gui.*;

/**
 *      Implements the dialog box allowing to choose different scheduling techniques.
 */
public class ScheduleAction
	extends AbstractAction
{
	private static Logger logger = LoggerFactory.createLogger(ScheduleAction.class);

	public ScheduleAction()
	{
		super("Schedule...", null);

		putValue(SHORT_DESCRIPTION, "Schedule selected automata (experimental)");
	}

	/**
	 *      Calls the ScheduleDialog if the number of selected automata is equal to one.
	 *      (Otherwise, synchronization is strongly recommended.)
	 */
	public void actionPerformed(ActionEvent e)
	{
		ScheduleDialog dlg = null;

		try 
		{
			dlg = new ScheduleDialog();
			dlg.show();
		}
		catch (Exception ex) 
		{
			if (ex.getMessage().contains("javax") || ex.getMessage().contains("java.awt"))
			{
			}
			else if (dlg != null)
			{
				dlg.done();
			}
		}
	}

	private void launchScheduleDialog()
		throws Exception
	{
		try 
		{
			ScheduleDialog dlg = new ScheduleDialog();
			dlg.show();
		}
		catch (Exception ex) 
		{
			if (ex.getMessage().contains("javax") || ex.getMessage().contains("java.awt"))
			{
			}
			else
			{
				throw ex;
			}
		}
	}
}
