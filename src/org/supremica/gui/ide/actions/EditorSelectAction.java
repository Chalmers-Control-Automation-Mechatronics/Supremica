package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.IDE;
import net.sourceforge.waters.gui.ControlledSurface;
import java.util.List;

public class EditorSelectAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorSelectAction(List<IDEAction> actionList)
	{
		super(actionList);

		setEditorActiveRequired(true);

		putValue(Action.NAME, "Select");
		putValue(Action.SHORT_DESCRIPTION, "Select");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/select16.gif")));
		putValue(Action.ACTION_COMMAND_KEY, ControlledSurface.SELECT);
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
