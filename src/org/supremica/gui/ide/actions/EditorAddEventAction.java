package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;

public class EditorAddEventAction
	extends IDEAction
{
	private IDE ide;

	public EditorAddEventAction(IDE ide)
	{
		this.ide = ide;

		putValue(Action.NAME, "Add Event");
		putValue(Action.SHORT_DESCRIPTION, "Add Event");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/event16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
//		EventEditorDialog diag = new EventEditorDialog(ide);
		//System.err.println("Add Event is not implemented yet!");
	}
}
