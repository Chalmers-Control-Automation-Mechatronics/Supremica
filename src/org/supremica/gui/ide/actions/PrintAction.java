package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.ModuleContainer;

public class PrintAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public PrintAction(IDEActionInterface ide)
	{
		super(ide);

		putValue(Action.NAME, "Print...");
		putValue(Action.SHORT_DESCRIPTION, "Print");
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		ModuleContainer moduleContainer = ide.getActiveModuleContainer();
//		ide.remove(moduleContainer);
	}
}
