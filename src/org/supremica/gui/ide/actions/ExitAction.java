package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;

public class ExitAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

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
