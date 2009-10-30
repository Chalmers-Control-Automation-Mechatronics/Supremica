package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.supremica.log.*;
import org.supremica.util.BrowserControl;
import java.util.List;
import javax.swing.ImageIcon;
import org.supremica.gui.ide.IDE;

public class HelpWebAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(HelpWebAction.class);

	public HelpWebAction(List<IDEAction> actionList)
	{
		super(actionList);

		putValue(Action.NAME, "Supremica Website...");
		putValue(Action.SHORT_DESCRIPTION, "Supremica on the Web");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/development/WebComponent16.gif")));
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
