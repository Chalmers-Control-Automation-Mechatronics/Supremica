
/******************** WorkbenchAction.java *****************/

// Action class for the Workbench action
// Owner: MF
package org.supremica.gui.useractions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.supremica.log.*;
import org.supremica.automata.Automata;
import org.supremica.gui.ActionMan;
import org.supremica.gui.VisualProject;
import org.supremica.workbench.Workbench;

public class WorkbenchAction
	extends AbstractAction
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(WorkbenchAction.class);
	private Workbench wb;

	public WorkbenchAction()
	{
		super("Workbench...", null);

		putValue(SHORT_DESCRIPTION, "Manual supervisor synthesis");
	}

	// Note, we avoid (short-circut) the ActionMan here... should we?
	public void actionPerformed(ActionEvent e)
	{
		// ActionMan.workbench_actionPerformed(ActionMan.getGui());

		VisualProject theProject = ActionMan.getGui().getVisualProjectContainer().getActiveProject();
		Automata selectedAutomata = ActionMan.getGui().getSelectedAutomata();

		try
		{
			execute(theProject, selectedAutomata);
		}
		catch (Exception ex)
		{
			logger.error("Exception in Workbench. ", ex);
			logger.debug(ex.getStackTrace());
		}
	}

	public void execute(VisualProject theProject, Automata theAutomata)
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
