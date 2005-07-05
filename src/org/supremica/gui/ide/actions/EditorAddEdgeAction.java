package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.IDE;
import net.sourceforge.waters.gui.ControlledSurface;

public class EditorAddEdgeAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorAddEdgeAction(IDEActionInterface ide)
	{
		super(ide);

		putValue(Action.NAME, "Add Edge");
		putValue(Action.SHORT_DESCRIPTION, "Add Edge");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/edge16.gif")));
		putValue(Action.ACTION_COMMAND_KEY, ControlledSurface.EDGE);
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
