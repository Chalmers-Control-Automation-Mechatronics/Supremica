package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.IDE;

public class EditorAddEventAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorAddEventAction(IDEActionInterface ide)
	{
		super(ide);

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
		System.err.println("Add Event is not implemented yet!");
	}
}
