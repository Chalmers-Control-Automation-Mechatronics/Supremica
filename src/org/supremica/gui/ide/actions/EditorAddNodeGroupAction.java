package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.util.List;
import org.supremica.gui.ide.IDE;
import net.sourceforge.waters.gui.ControlledSurface;

public class EditorAddNodeGroupAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorAddNodeGroupAction(List<IDEAction> actionList)
	{
		super(actionList);

		setEditorActiveRequired(true);

		putValue(Action.NAME, "Group Nodes");
		putValue(Action.SHORT_DESCRIPTION, "Group nodes");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/nodegroup16.gif")));
		putValue(Action.ACTION_COMMAND_KEY, ControlledSurface.Tool.NODEGROUP.toString());
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
