//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2023 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.gui.actions.WatersActionManager;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;


public class Actions extends WatersActionManager
{

    //#######################################################################
    //# Constructor
    public Actions(final IDE ide)
    {
        super(ide);
        addAction(new NewAction(ide));
        addAction(new OpenRASAction(ide));
        addAction(new OpenFTAction(ide));
        addAction(new OpenFTSpecAction(ide));
        addAction(new SaveAction(ide));
        addAction(new SaveAsAction(ide));
        addAction(new ImportAction(ide));
        addAction(new ExitAction(ide));

        mIDE = ide;
        for (final IDEAction action : mIDEActions)
        {
            action.setIDEActionInterface(ide);
        }
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.gui.observer.Observer
    @Override
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
        boolean editorActive = false;
        boolean analyzerActive = false;
        final DocumentContainer container = mIDE.getActiveDocumentContainer();
        if (container != null) {
          final Component active = container.getActivePanel();
          if (active == container.getEditorPanel()) {
            editorActive = true;
          } else if (active == container.getSupremicaAnalyzerPanel()) {
            analyzerActive = true;
          }
        }
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
    private final List<IDEAction> mIDEActions = new LinkedList<IDEAction>();

    // Editor Actions
    public IDEAction editorSynthesizerAction = new EditorSynthesizerAction(mIDEActions);
    public IDEAction editorGenerateTextLabelAction = new EditorGenerateTextLabelAction(mIDEActions);
    public IDEAction editorRemoveGABlocksAction = new EditorRemoveGABlocksAction(mIDEActions);
    public IDEAction editorEFASynchEval = new EditorEFASynchAndEvalAction(mIDEActions);
    public IDEAction editorEFASynch = new EditorEFASynchAction(mIDEActions);
    public IDEAction editorEFAPE = new EditorEFAPEAction(mIDEActions);
    public IDEAction editorIISC = new EditorEFAIISCAction(mIDEActions);
    public IDEAction editorReadSpecAction = new EditorReadSpecAction(mIDEActions);
    public IDEAction editorReadFTAction = new EditorReadFisherThompsonAction(mIDEActions);

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
    public IDEAction analyzerEnumerateAction = new AnalyzerEnumerateAction(mIDEActions);
    public IDEAction analyzerEventHiderAction = new AnalyzerEventHiderAction(mIDEActions);
    public IDEAction analyzerRenameAction = new AnalyzerRenameAction(mIDEActions);
    public IDEAction analyzerExportAction = new AnalyzerExportAction(mIDEActions);
    public IDEAction analyzerPlantifyAction = new AnalyzerPlantifyAction(mIDEActions);
    public IDEAction analyzerComputeMinimalCutSetsAction = new AnalyzerComputeMinimalCutSetsAction(mIDEActions);

    // Simulator
    public IDEAction simulatorLaunchAnimatorAction = new SimulatorLaunchAnimatorAction(mIDEActions);
    public IDEAction simulatorLaunchSimulatorAction = new SimulatorLaunchSimulatorAction(mIDEActions);
    public IDEAction simulatorClearSimulationData = new SimulatorClearSimulationData(mIDEActions);


    // Analyzer experimental
    public IDEAction analyzerExperimentAction = new AnalyzerExperimentAction(mIDEActions);
    public IDEAction analyzerPredictSizeAction = new AnalyzerPredictSizeAction(mIDEActions);
    public IDEAction analyzerScheduleAction = new AnalyzerScheduleAction(mIDEActions);
    public IDEAction analyzerSatAction = new AnalyzerSatAction(mIDEActions);
    public IDEAction analyzerSMVAction = new AnalyzerSMVAction(mIDEActions);
    public IDEAction analyzerDeadEventsDetectorAction = new AnalyzerDeadEventsDetectorAction(mIDEActions);
    public IDEAction analyzerModularForbidder = new AnalyzerModularForbidderAction(mIDEActions);

    // Examples Actions
    public IDEAction toolsTestCasesAction = new ToolsTestCasesAction(mIDEActions);

    // Help Actions
    public IDEAction helpWebAction = new HelpWebAction(mIDEActions);

}
