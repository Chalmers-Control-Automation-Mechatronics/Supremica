package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;

public class CloseAction
	extends IDEAction
{
	private IDE ide;

	public CloseAction(IDE ide)
	{
		this.ide = ide;

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
	}
}
