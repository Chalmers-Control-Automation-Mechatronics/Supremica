package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.supremica.log.*;
import org.supremica.util.BrowserControl;


public class HelpWebAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(HelpWebAction.class);

	public HelpWebAction(IDEActionInterface ide)
	{
		super(ide);

		putValue(Action.NAME, "Supremica Website...");
		putValue(Action.SHORT_DESCRIPTION, "Supremica on the Web");
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		BrowserControl.displayURL("http://www.supremica.org");
	}
}
