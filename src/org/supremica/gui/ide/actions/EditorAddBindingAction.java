package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;

public class EditorAddBindingAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorAddBindingAction(IDEActionInterface ide)
	{
		super(ide);

		putValue(Action.NAME, "Add Binding...");
		putValue(Action.SHORT_DESCRIPTION, "Add Binding");
//		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/edge16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		System.err.println("Add Binding is not implemented yet!");
	}
}
