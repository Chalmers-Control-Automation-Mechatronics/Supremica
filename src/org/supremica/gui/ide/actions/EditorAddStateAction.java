package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;

public class EditorAddStateAction
	extends AbstractAction
	implements IDEAction
{
	private IDE ide;

	public EditorAddStateAction(IDE ide)
	{
		this.ide = ide;

		putValue(Action.NAME, "Add State");
		putValue(Action.SHORT_DESCRIPTION, "Add State");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/node.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		System.err.println("Add State is not implemented yet!");
	}
}
