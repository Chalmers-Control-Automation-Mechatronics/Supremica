package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.IDE;

public class EditorAddNodeGroupAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorAddNodeGroupAction(IDEActionInterface ide)
	{
		super(ide);

		putValue(Action.NAME, "Add Node Group");
		putValue(Action.SHORT_DESCRIPTION, "Add Node Group");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/nodegroup16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		ide.setEditorMode(this);
//		System.err.println("Add Node Group is not implemented yet!");
	}
}
