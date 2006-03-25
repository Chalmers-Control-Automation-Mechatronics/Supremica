package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.List;

public class ExitAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public ExitAction(List<IDEAction> actionList)
	{
		super(actionList);

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
