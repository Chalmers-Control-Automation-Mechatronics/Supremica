package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;

public class EditorAddTransitionAction
	extends AbstractAction
	implements IDEAction
{
	private IDE ide;

	public EditorAddTransitionAction(IDE ide)
	{
		this.ide = ide;

		putValue(Action.NAME, "Add Transition");
		putValue(Action.SHORT_DESCRIPTION, "Add Transition");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/edge.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		System.err.println("Add Transition is not implemented yet!");
	}
}
