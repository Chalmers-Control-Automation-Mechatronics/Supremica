package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.IDE;
import net.sourceforge.waters.gui.ControlledSurface;

public class EditorAddNodeAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorAddNodeAction(IDEActionInterface ide)
	{
		super(ide);

		putValue(Action.NAME, "Add Node");
		putValue(Action.SHORT_DESCRIPTION, "Add Node");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/node16.gif")));
		putValue(Action.ACTION_COMMAND_KEY, ControlledSurface.NODE);
	}

	public void actionPerformed(ActionEvent e)
	{
	    System.out.println("Node");
		doAction();
	}

	public void doAction()
	{
		ide.setEditorMode(this);

//		System.err.println("Add Node is not implemented yet!");
	}
}
