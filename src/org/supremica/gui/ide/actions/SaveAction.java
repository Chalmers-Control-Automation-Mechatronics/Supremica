package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import org.supremica.gui.ide.IDE;
import org.supremica.log.*;


public class SaveAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(SaveAction.class);

	public SaveAction(List<IDEAction> actionList)
	{
		super(actionList);

		putValue(Action.NAME, "Save...");
		putValue(Action.SHORT_DESCRIPTION, "Save the project");
		putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Save16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		logger.error("Save Action selected - not implemented");
	}
}
