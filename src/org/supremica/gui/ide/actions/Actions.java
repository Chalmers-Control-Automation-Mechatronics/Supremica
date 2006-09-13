
package org.supremica.gui.ide.actions;

import java.util.*;

public class Actions
{
    /** The ide to which the actions in this class applies. */
    private IDEActionInterface ide;
    
    /** A list of all actions. This is needed because we would like to set
     * the ideActionInterface automatically */
    private List<IDEAction> allActions = new LinkedList<IDEAction>();
    
    // General Actions
    public IDEAction newAction = new NewAction(allActions);
    public IDEAction openAction = new OpenAction(allActions);
    public IDEAction closeAction = new CloseAction(allActions);
    public IDEAction saveAction = new SaveAction(allActions);
    //public IDEAction printAction = new PrintAction(allActions);
    
    // Editor Actions
    public IDEAction editorPrintAction = new EditorPrintAction(allActions);
    public IDEAction editorPrintPDFAction = new EditorPrintPDFAction(allActions);
    public IDEAction exitAction = new ExitAction(allActions);
    public IDEAction editorCopyAction = new EditorCopyAction(allActions);
    public IDEAction editorUndoAction = new EditorUndoAction(allActions);
    public IDEAction editorRedoAction = new EditorRedoAction(allActions);
    public IDEAction editorSelectAction = new EditorSelectAction(allActions);
    public IDEAction editorAddNodeAction = new EditorAddNodeAction(allActions);
    public IDEAction editorAddNodeGroupAction = new EditorAddNodeGroupAction(allActions);
    public IDEAction editorAddEdgeAction = new EditorAddEdgeAction(allActions);
    public IDEAction editorAddEventAction = new EditorAddEventAction(allActions);
    public IDEAction editorOptionsAction = new EditorOptionsAction(allActions);
    public IDEAction editorAddSimpleComponentAction = new EditorAddSimpleComponentAction(allActions);
    public IDEAction editorAddForeachComponentAction = new EditorAddForeachComponentAction(allActions);
    public IDEAction editorAddInstanceAction = new EditorAddInstanceAction(allActions);
    public IDEAction editorAddBindingAction = new EditorAddBindingAction(allActions);
    
    // Analyzer Actions
    public IDEAction analyzerWorkbenchAction = new WorkbenchAction(allActions);
    public IDEAction analyzerSynchronizerAction = new AnalyzerSynchronizerAction(allActions);
    public IDEAction analyzerSynthesizerAction = new AnalyzerSynthesizerAction(allActions);
    public IDEAction analyzerVerifierAction = new AnalyzerVerifierAction(allActions);
    public IDEAction analyzerOptionsAction = new AnalyzerOptionsAction(allActions);
    
    // Tools Actions
    public IDEAction toolsTestCasesAction = new ToolsTestCasesAction(allActions);
    
    // Help Actions
    public IDEAction helpWebAction = new HelpWebAction(allActions);
    public IDEAction helpAboutAction = new HelpAboutAction(allActions);
    
    /**
     * Creates a new <code>Actions</code> instance.
     *
     * @param ide an <code>IDEActionInterface</code> value
     */
    public Actions(IDEActionInterface ide)
    {
        this.ide = ide;
        
        for (IDEAction action : allActions)
        {
            action.setIDEActionInterface(ide);
        }
    }
}
