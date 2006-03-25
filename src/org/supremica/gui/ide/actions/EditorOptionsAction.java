package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.ModuleContainer;
import java.util.List;

public class EditorOptionsAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorOptionsAction(List<IDEAction> actionList)
	{
		super(actionList);

		setEditorActiveRequired(true);

		putValue(Action.NAME, "Editor Options...");
		putValue(Action.SHORT_DESCRIPTION, "Editor Options");
//		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/edge16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		ModuleContainer moduleContainer = ide.getActiveModuleContainer();

//			root.getControlledSurface().setOptionsVisible(true);
	}
}
