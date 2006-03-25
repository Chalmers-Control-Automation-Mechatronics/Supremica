package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.IDE;
import java.util.List;

public class EditorRedoAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorRedoAction(List<IDEAction> actionList)
	{
		super(actionList);

		setEditorActiveRequired(true);

		putValue(Action.NAME, "Redo");
		putValue(Action.SHORT_DESCRIPTION, "Redo");
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
	    if (ide.getActiveModuleContainer() != null) {
		if (ide.getActiveModuleContainer().canRedo()) {
		    ide.getActiveModuleContainer().redo();
		}		
	    }
	}
}
