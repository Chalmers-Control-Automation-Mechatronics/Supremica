
package org.supremica.gui.ide.actions;

import org.supremica.gui.ide.IDE;
import java.util.*;

public class Actions
{
	private IDEActionInterface ide;

	private List analyzerActions = new LinkedList();
	private List editorActions = new LinkedList();

	public NewAction newAction;
	public OpenAction openAction;
	public CloseAction closeAction;
	public SaveAction saveAction;

	public PrintAction printAction;
	public EditorPrintAction editorPrintAction;
	public EditorPrintPDFAction editorPrintPDFAction;

	public ExitAction exitAction;

	public EditorCopyAction editorCopyAction;

	public EditorSelectAction editorSelectAction;

	public EditorAddNodeAction editorAddNodeAction;
	public EditorAddNodeGroupAction editorAddNodeGroupAction;
	public EditorAddEdgeAction editorAddEdgeAction;
	public EditorAddEventAction editorAddEventAction;

	public EditorOptionsAction editorOptionsAction;

	public EditorAddSimpleComponentAction editorAddSimpleComponentAction;
	public EditorAddForeachComponentAction editorAddForeachComponentAction;
	public EditorAddInstanceAction editorAddInstanceAction;
	public EditorAddBindingAction editorAddBindingAction;

	public HelpWebAction helpWebAction;
	public HelpAboutAction helpAboutAction;

	public Actions(IDEActionInterface ide)
	{
		this.ide = ide;

		newAction = new NewAction(ide);
		openAction = new OpenAction(ide);
		closeAction = new CloseAction(ide);
		saveAction = new SaveAction(ide);

		printAction = new PrintAction(ide);

		editorPrintAction = new EditorPrintAction(ide);
		editorActions.add(editorPrintAction);

		editorPrintPDFAction = new EditorPrintPDFAction(ide);
		editorActions.add(editorPrintPDFAction);

		exitAction = new ExitAction(ide);

		editorCopyAction = new EditorCopyAction(ide);
		editorActions.add(editorCopyAction);

		editorSelectAction = new EditorSelectAction(ide);
		editorActions.add(editorSelectAction);

		editorAddNodeAction = new EditorAddNodeAction(ide);
		editorActions.add(editorAddNodeAction);

		editorAddNodeGroupAction = new EditorAddNodeGroupAction(ide);
		editorActions.add(editorAddNodeGroupAction);

		editorAddEdgeAction = new EditorAddEdgeAction(ide);
		editorActions.add(editorAddEdgeAction);

		editorAddEventAction = new EditorAddEventAction(ide);
		editorActions.add(editorAddEventAction);

		editorOptionsAction = new EditorOptionsAction(ide);

		editorAddSimpleComponentAction = new EditorAddSimpleComponentAction(ide);
		editorActions.add(editorAddSimpleComponentAction);

		editorAddForeachComponentAction = new EditorAddForeachComponentAction(ide);
		editorActions.add(editorAddForeachComponentAction);

		editorAddInstanceAction = new EditorAddInstanceAction(ide);
		editorActions.add(editorAddInstanceAction);

		editorAddBindingAction = new EditorAddBindingAction(ide);
		editorActions.add(editorAddBindingAction);

		helpWebAction = new HelpWebAction(ide);
		helpAboutAction = new HelpAboutAction(ide);

	}

	public void enableEditorActions(boolean enabled)
	{
		actionsSetEnabled(editorActions, enabled);
	}

	public void enableAnalyzerActions(boolean enabled)
	{
		actionsSetEnabled(analyzerActions, enabled);
	}

	private void actionsSetEnabled(List theActions, boolean enabled)
	{
		for (Iterator actIt = theActions.iterator(); actIt.hasNext(); )
		{
			IDEAction currAction = (IDEAction)actIt.next();
			currAction.setEnabled(enabled);
		}
	}
}
