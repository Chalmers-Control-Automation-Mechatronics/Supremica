package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;

public class EditorAddNodeGroupAction
	extends AbstractAction
	implements IDEAction
{
	private IDE ide;

	public EditorAddNodeGroupAction(IDE ide)
	{
		this.ide = ide;

		putValue(Action.NAME, "Add Group State");
		putValue(Action.SHORT_DESCRIPTION, "Add Group State");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/nodegroup.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		System.err.println("Add Group State is not implemented yet!");
	}
}
