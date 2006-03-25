package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.List;

public class EditorPrintAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorPrintAction(List<IDEAction> actionList)
	{
		super(actionList);

		setEditorActiveRequired(true);

		putValue(Action.NAME, "Print...");
		putValue(Action.SHORT_DESCRIPTION, "Print");
//		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Print16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		System.err.println("Print is not implemented yet!");
	}
}
