package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.IDE;
import org.supremica.log.*;
import org.supremica.util.BrowserControl;


public class HelpWebAction
	extends IDEAction
{
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
