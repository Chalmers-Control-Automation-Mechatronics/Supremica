package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;

public class EditorAddEdgeAction
	extends IDEAction
{
	private IDE ide;

	public EditorAddEdgeAction(IDE ide)
	{
		this.ide = ide;

		putValue(Action.NAME, "Add Edge");
		putValue(Action.SHORT_DESCRIPTION, "Add Edge");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/edge16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		ide.setEditorMode(this);

//		System.err.println("Add Edge is not implemented yet!");
	}
}
