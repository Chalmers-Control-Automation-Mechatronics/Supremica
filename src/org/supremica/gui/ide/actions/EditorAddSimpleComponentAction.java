package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.List;
import org.supremica.gui.ide.ModuleContainer;

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

		ModuleContainer activeModule = ide.getActiveModuleContainer();
		//activeModule.addSimpleComponent();
		//DefaultMutableTreeNode parentNode = null;
		//TreePath parentPath = moduleSelectTree.getSelectionPath();

		/*if (parentPath == null) {
		//There's no selection. Default to the root node.
		parentNode = rootNode;
		} else {
		parentNode = (DefaultMutableTreeNode)
		(parentPath.getLastPathComponent());
		}*/
		//EditorNewDialog diag = new EditorNewDialog(this, parentNode);

		//logEntry("New Simple Component requested");
	}
}
