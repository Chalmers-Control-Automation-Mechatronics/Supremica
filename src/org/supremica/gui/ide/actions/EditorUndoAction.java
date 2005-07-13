package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.IDE;

public class EditorUndoAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorUndoAction(IDEActionInterface ide)
	{
		super(ide);

		putValue(Action.NAME, "Undo");
		putValue(Action.SHORT_DESCRIPTION, "Undo");
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
	    if (ide.getActiveModuleContainer() != null) {
		if (ide.getActiveModuleContainer().canUndo()) {
		    ide.getActiveModuleContainer().undo();
		}		
	    }
	}
}
