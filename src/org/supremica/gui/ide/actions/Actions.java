
package org.supremica.gui.ide.actions;

import org.supremica.gui.ide.IDE;

public class Actions
{
	private IDEActionInterface ide;

	public NewAction newAction;
	public OpenAction openAction;
	public CloseAction closeAction;
	public SaveAction saveAction;
	public ExitAction exitAction;

	public EditorSelectAction editorSelectAction;

	public EditorAddNodeAction editorAddNodeAction;
	public EditorAddNodeGroupAction editorAddNodeGroupAction;
	public EditorAddEdgeAction editorAddEdgeAction;
	public EditorAddEventAction editorAddEventAction;

	public EditorOptionsAction editorOptionsAction;

	public Actions(IDEActionInterface ide)
	{
		this.ide = ide;

		newAction = new NewAction(ide);
		openAction = new OpenAction(ide);
		closeAction = new CloseAction(ide);
		saveAction = new SaveAction(ide);
		exitAction = new ExitAction(ide);

		editorSelectAction = new EditorSelectAction(ide);

		editorAddNodeAction = new EditorAddNodeAction(ide);
		editorAddNodeGroupAction = new EditorAddNodeGroupAction(ide);
		editorAddEdgeAction = new EditorAddEdgeAction(ide);
		editorAddEventAction = new EditorAddEventAction(ide);

		editorOptionsAction = new EditorOptionsAction(ide);

	}

}
