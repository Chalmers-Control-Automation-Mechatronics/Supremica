package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.IDE;

public class EditorSelectAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorSelectAction(IDEActionInterface ide)
	{
		super(ide);

		putValue(Action.NAME, "Select");
		putValue(Action.SHORT_DESCRIPTION, "Select");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/select16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		ide.setEditorMode(this);
		//System.err.println("Select is not implemented yet!");
	}
}
