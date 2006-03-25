package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.List;

public class EditorAddSimpleComponentAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorAddSimpleComponentAction(List<IDEAction> actionList)
	{
		super(actionList);

		setEditorActiveRequired(true);

		putValue(Action.NAME, "Add Simple Component...");
		putValue(Action.SHORT_DESCRIPTION, "Add Simple Component");
//		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/edge16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		System.err.println("Add Simple Component is not implemented yet!");
	}
}
