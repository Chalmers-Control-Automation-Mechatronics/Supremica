package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;

public class EditorAddBindingAction
	extends IDEAction
{
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
