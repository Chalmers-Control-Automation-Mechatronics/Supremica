package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;


import org.supremica.gui.ide.IDE;

public class ExitAction
	extends IDEAction
{
	public ExitAction(IDEActionInterface ide)
	{
		super(ide);

		putValue(Action.NAME, "Exit");
		putValue(Action.SHORT_DESCRIPTION, "Exit");
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		System.exit(0);
	}
}
