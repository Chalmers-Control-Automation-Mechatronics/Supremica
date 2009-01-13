//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica/Waters IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   Actions
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.gui.ide.actions;

import java.util.List;
import java.util.LinkedList;

import net.sourceforge.waters.gui.actions.WatersActionManager;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import org.supremica.gui.ide.IDE;


public class Actions extends WatersActionManager
{
    
    //#######################################################################
    //# Constructor
    public Actions(final IDE ide)
    {
        super(ide);
        addAction(new NewAction(ide));
        addAction(new OpenAction(ide));
        addAction(new SaveAction(ide));
        addAction(new SaveAsAction(ide));
        addAction(new CloseAction(ide));
        addAction(new ExitAction(ide));
        
        mIDE = ide;
        for (final IDEAction action : mIDEActions)
        {
            action.setIDEActionInterface(ide);
        }
    }
    
    
    //#######################################################################
    //# Interface net.sourceforge.waters.gui.observer.Observer
    public void update(final EditorChangedEvent event)
    {
        super.update(event);
        switch (event.getKind())
        {
            case CONTAINER_SWITCH:
            case MAINPANEL_SWITCH:
                updateEnabledStatus();
                break;
            default:
                break;
        }
    }
    
    
    //#######################################################################
    //# Enabling and Disabling
    /**
     * Enable/disable all actions that have been flagged as editor-only or
     * anlyzer-only.
     */
    private void updateEnabledStatus()
    {
        final boolean editorActive = mIDE.editorActive();
        final boolean analyzerActive = mIDE.analyzerActive();
        for (final IDEAction action : mIDEActions)
        {
            if (action.getEditorActiveRequired())
            {
                action.setEnabled(editorActive);
            }
            else if (action.getAnalyzerActiveRequired())
            {
                action.setEnabled(analyzerActive);
            }
        }
    }
    
    
    //#######################################################################
    //# Data Members
    /**
     * The IDE that uses these actions.
     */
    private final IDE mIDE;
    
    /**
     * The list of all actions. This is needed because we would like to set
     * the ideActionInterface automatically
     */
    private List<IDEAction> mIDEActions = new LinkedList<IDEAction>();
    
    // Printing Actions
    public IDEAction editorPrintAction = new EditorPrintAction(mIDEActions);
    public IDEAction editorSavePDFAction = new EditorSavePDFAction(mIDEActions);
    public IDEAction editorSaveSVGAction = new EditorSaveSVGAction(mIDEActions);
    public IDEAction editorSavePostscriptAction = new EditorSavePostscriptAction(mIDEActions);
    
    // Editor Actions
    public IDEAction editorCopyAsWMFAction = new EditorCopyAsWMFAction(mIDEActions);
    public IDEAction editorAddForeachComponentAction = new EditorAddForeachComponentAction(mIDEActions);
    public IDEAction editorAddInstanceAction = new EditorAddInstanceAction(mIDEActions);
    public IDEAction editorAddBindingAction = new EditorAddBindingAction(mIDEActions);
    public IDEAction editorStopEmbedderAction = new EditorStopEmbedderAction(mIDEActions);
    
    // Analyzer Options
    public IDEAction analyzerOptionsAction = new AnalyzerOptionsAction(mIDEActions);
    
    // Analyzer Actions
    public IDEAction analyzerStatisticsAction = new AnalyzerStatisticsAction(mIDEActions);
    public IDEAction analyzerExploreStatesAction = new AnalyzerExploreStatesAction(mIDEActions);
    public IDEAction analyzerFindStatesAction = new AnalyzerFindStatesAction(mIDEActions);
    
    // Analyzer View Actions
    public IDEAction analyzerViewAutomatonAction = new AnalyzerViewAutomatonAction(mIDEActions);
    public IDEAction analyzerViewAlphabetAction = new AnalyzerViewAlphabetAction(mIDEActions);
    public IDEAction analyzerViewStatesAction = new AnalyzerViewStatesAction(mIDEActions);
    public IDEAction analyzerViewModularStructureAction = new AnalyzerViewModularStructureAction(mIDEActions);
    
    
    public IDEAction analyzerWorkbenchAction = new WorkbenchAction(mIDEActions);
    public IDEAction analyzerSynchronizerAction = new AnalyzerSynchronizerAction(mIDEActions);
    public IDEAction analyzerSynthesizerAction = new AnalyzerSynthesizerAction(mIDEActions);
    public IDEAction analyzerVerifierAction = new AnalyzerVerifierAction(mIDEActions);
    public IDEAction analyzerSendToEditorAction = new AnalyzerSendToEditorAction(mIDEActions);
    public IDEAction analyzerDeleteSelectedAction = new AnalyzerDeleteSelectedAutomataAction(mIDEActions);
    public IDEAction analyzerDeleteAllAction = new AnalyzerDeleteAllAutomataAction(mIDEActions);
    public IDEAction analyzerMinimizeAction = new AnalyzerMinimizeAction(mIDEActions);
    public IDEAction analyzerPurgeAction = new AnalyzerPurgeAction(mIDEActions);
    public IDEAction analyzerEventHiderAction = new AnalyzerEventHiderAction(mIDEActions);
    public IDEAction analyzerRenameAction = new AnalyzerRenameAction(mIDEActions);
    public IDEAction analyzerExportAction = new AnalyzerExportAction(mIDEActions);
    public IDEAction analyzerPlantifyAction = new AnalyzerPlantifyAction(mIDEActions);
    public IDEAction analyzerGuardAction = new AnalyzerGuardAction(mIDEActions);
    
    // Simulator
    public IDEAction simulatorLaunchAnimatorAction = new SimulatorLaunchAnimatorAction(mIDEActions);
    public IDEAction simulatorLaunchSimulatorAction = new SimulatorLaunchSimulatorAction(mIDEActions);
    public IDEAction simulatorClearSimulationData = new SimulatorClearSimulationData(mIDEActions);
    
    // Analyzer experimental
    public IDEAction analyzerExperimentAction = new AnalyzerExperimentAction(mIDEActions);
    public IDEAction analyzerPredictSizeAction = new AnalyzerPredictSizeAction(mIDEActions);
    public IDEAction analyzerCountReachableAction = new AnalyzerCountReachableAction(mIDEActions);
    public IDEAction analyzerScheduleAction = new AnalyzerScheduleAction(mIDEActions);
    public IDEAction analyzerSatAction = new AnalyzerSatAction(mIDEActions);
    public IDEAction analyzerSMVAction = new AnalyzerSMVAction(mIDEActions);       
    public IDEAction analyzerDeadEventsDetectorAction = new AnalyzerDeadEventsDetectorAction(mIDEActions);
    
    // Examples Actions
    public IDEAction toolsTestCasesAction = new ToolsTestCasesAction(mIDEActions);
 
    // Tools Actions
    public IDEAction toolsSOCEditorAction = new ToolsSOCEditorAction(mIDEActions);
    
    // Help Actions
    public IDEAction helpWebAction = new HelpWebAction(mIDEActions);
    public IDEAction helpAboutAction = new HelpAboutAction(mIDEActions);
    
}
