package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.ModuleContainer;
import java.util.List;

public class CloseAction
	extends IDEAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CloseAction(List<IDEAction> actionList)
	{
		super(actionList);

		putValue(Action.NAME, "Close");
		putValue(Action.SHORT_DESCRIPTION, "Close module");
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		ModuleContainer moduleContainer = ide.getActiveModuleContainer();
		ide.remove(moduleContainer);
		ide.getIDE().getDocumentManager().unloadFromCache(moduleContainer.getModule());
	}
}
