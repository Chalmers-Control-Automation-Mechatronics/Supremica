package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.AboutDialog;
import org.supremica.log.*;
import java.util.List;

public class HelpAboutAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(HelpAboutAction.class);

	public HelpAboutAction(List<IDEAction> actionList)
	{
		super(actionList);

		putValue(Action.NAME, "About Supremica...");
		putValue(Action.SHORT_DESCRIPTION, "About Supremica");
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		AboutDialog aboutDialog = new AboutDialog(ide.getFrame());
		aboutDialog.setVisible(true);
	}
}

