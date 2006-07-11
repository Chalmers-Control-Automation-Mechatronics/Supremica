package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.util.List;
import org.supremica.gui.ide.IDE;
import net.sourceforge.waters.gui.ControlledSurface;

public class EditorAddEventAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorAddEventAction(List<IDEAction> actionList)
	{
		super(actionList);

		setEditorActiveRequired(true);

		putValue(Action.NAME, "Add Event");
		putValue(Action.SHORT_DESCRIPTION, "Add Event");
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/event16.gif")));
		putValue(Action.ACTION_COMMAND_KEY, ControlledSurface.EVENT);
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
//		EventEditorDialog diag = new EventEditorDialog(ide);
//		System.err.println("Add Event is not implemented yet!");
	}
}
