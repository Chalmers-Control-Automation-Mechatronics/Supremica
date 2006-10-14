
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
    public IDEAction editorPrintPDFAction = new EditorSavePDFAction(allActions);
    public IDEAction exitAction = new ExitAction(allActions);
    public IDEAction editorCopyAction = new EditorCopyAction(allActions);
    public IDEAction editorUndoAction = new EditorUndoAction(allActions);
    public IDEAction editorRedoAction = new EditorRedoAction(allActions);
    public IDEAction editorSelectAction = new EditorSelectAction(allActions);
    public IDEAction editorAddNodeAction = new EditorAddNodeAction(allActions);
    public IDEAction editorAddNodeGroupAction = new EditorAddNodeGroupAction(allActions);
    public IDEAction editorAddEdgeAction = new EditorAddEdgeAction(allActions);
    public IDEAction editorAddComponentEventAction = new EditorAddComponentEventAction(allActions);
    public IDEAction editorAddModuleEventAction = new EditorAddModuleEventAction(allActions);
    public IDEAction editorOptionsAction = new EditorOptionsAction(allActions);
    public IDEAction editorAddSimpleComponentAction = new EditorAddSimpleComponentAction(allActions);
    public IDEAction editorAddForeachComponentAction = new EditorAddForeachComponentAction(allActions);
    public IDEAction editorAddInstanceAction = new EditorAddInstanceAction(allActions);
    public IDEAction editorAddBindingAction = new EditorAddBindingAction(allActions);
    public IDEAction editorRunEmbedderAction = new EditorRunEmbedderAction(allActions);
    public IDEAction editorStopEmbedderAction = new EditorStopEmbedderAction(allActions);


    // Analyzer Options
    public IDEAction analyzerOptionsAction = new AnalyzerOptionsAction(allActions);

    // Analyzer Actions
    public IDEAction analyzerStatisticsAction = new AnalyzerStatisticsAction(allActions);
    public IDEAction analyzerExploreStatesAction = new AnalyzerExploreStatesAction(allActions);
    public IDEAction analyzerFindStatesAction = new AnalyzerFindStatesAction(allActions);

    // Analyzer View Actions
    public IDEAction analyzerViewAutomatonAction = new AnalyzerViewAutomatonAction(allActions);
    public IDEAction analyzerViewAlphabetAction = new AnalyzerViewAlphabetAction(allActions);
    public IDEAction analyzerViewStatesAction = new AnalyzerViewStatesAction(allActions);
    public IDEAction analyzerViewModularStructureAction = new AnalyzerViewModularStructureAction(allActions);


    public IDEAction analyzerWorkbenchAction = new WorkbenchAction(allActions);
    public IDEAction analyzerSynchronizerAction = new AnalyzerSynchronizerAction(allActions);
    public IDEAction analyzerSynthesizerAction = new AnalyzerSynthesizerAction(allActions);
    public IDEAction analyzerVerifierAction = new AnalyzerVerifierAction(allActions);
    public IDEAction analyzerSendToEditorAction = new AnalyzerSendToEditorAction(allActions);
    public IDEAction analyzerDeleteSelectedAction = new AnalyzerDeleteSelectedAutomataAction(allActions);
    public IDEAction analyzerDeleteAllAction = new AnalyzerDeleteAllAutomataAction(allActions);
    public IDEAction analyzerMinimizeAction = new AnalyzerMinimizeAction(allActions);
    public IDEAction analyzerPurgeAction = new AnalyzerPurgeAction(allActions);
    public IDEAction analyzerEventHiderAction = new AnalyzerEventHiderAction(allActions);
    public IDEAction analyzerRenameAction = new AnalyzerRenameAction(allActions);
    public IDEAction analyzerExportAction = new AnalyzerExportAction(allActions);


    // Examples Actions
    public IDEAction toolsTestCasesAction = new ToolsTestCasesAction(allActions);
    public IDEAction examplesStaticAction = new ExamplesStaticAction(allActions);


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
