package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;

public class PrintAction
	extends IDEAction
{
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
