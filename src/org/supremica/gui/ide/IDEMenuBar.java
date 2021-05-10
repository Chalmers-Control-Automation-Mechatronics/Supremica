//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2021 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui.ide;

import gnu.trove.set.hash.THashSet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.sourceforge.waters.analysis.options.AnalysisAlgorithmOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionChangeEvent;
import net.sourceforge.waters.analysis.options.OptionChangeListener;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.gui.actions.AnalyzerConflictCheckAction;
import net.sourceforge.waters.gui.actions.AnalyzerControlLoopCheckAction;
import net.sourceforge.waters.gui.actions.AnalyzerControllabilityCheckAction;
import net.sourceforge.waters.gui.actions.AnalyzerCountStatesAction;
import net.sourceforge.waters.gui.actions.AnalyzerDeadlockCheckAction;
import net.sourceforge.waters.gui.actions.AnalyzerDiagnosabilityCheckAction;
import net.sourceforge.waters.gui.actions.AnalyzerHideAction;
import net.sourceforge.waters.gui.actions.AnalyzerLanguageInclusionCheckAction;
import net.sourceforge.waters.gui.actions.AnalyzerSimplifierAction;
import net.sourceforge.waters.gui.actions.AnalyzerSynchronousProductAction;
import net.sourceforge.waters.gui.actions.AnalyzerSynthesisAction;
import net.sourceforge.waters.gui.actions.AnalyzerWorkbenchAction;
import net.sourceforge.waters.gui.actions.GraphLayoutAction;
import net.sourceforge.waters.gui.actions.GraphSaveEPSAction;
import net.sourceforge.waters.gui.actions.GraphSavePDFAction;
import net.sourceforge.waters.gui.actions.IDEAboutAction;
import net.sourceforge.waters.gui.actions.IDECloseAction;
import net.sourceforge.waters.gui.actions.IDECopyAction;
import net.sourceforge.waters.gui.actions.IDECutAction;
import net.sourceforge.waters.gui.actions.IDEDeleteAction;
import net.sourceforge.waters.gui.actions.IDEDeselectAllAction;
import net.sourceforge.waters.gui.actions.IDEHelpAction;
import net.sourceforge.waters.gui.actions.IDEPasteAction;
import net.sourceforge.waters.gui.actions.IDEPropertiesAction;
import net.sourceforge.waters.gui.actions.IDESelectAllAction;
import net.sourceforge.waters.gui.actions.InsertConditionalAction;
import net.sourceforge.waters.gui.actions.InsertConstantAliasAction;
import net.sourceforge.waters.gui.actions.InsertEventAliasAction;
import net.sourceforge.waters.gui.actions.InsertEventDeclAction;
import net.sourceforge.waters.gui.actions.InsertForeachAction;
import net.sourceforge.waters.gui.actions.InsertInstanceAction;
import net.sourceforge.waters.gui.actions.InsertParameterBindingAction;
import net.sourceforge.waters.gui.actions.InsertSimpleComponentAction;
import net.sourceforge.waters.gui.actions.InsertVariableAction;
import net.sourceforge.waters.gui.actions.InstantiateModuleAction;
import net.sourceforge.waters.gui.actions.OptionsAction;
import net.sourceforge.waters.gui.actions.RecompileAction;
import net.sourceforge.waters.gui.actions.ShowGraphAction;
import net.sourceforge.waters.gui.actions.ShowModuleCommentAction;
import net.sourceforge.waters.gui.actions.SimulationBackToStartAction;
import net.sourceforge.waters.gui.actions.SimulationCascadeAction;
import net.sourceforge.waters.gui.actions.SimulationCloseAllAction;
import net.sourceforge.waters.gui.actions.SimulationJumpToEndAction;
import net.sourceforge.waters.gui.actions.SimulationReplayStepAction;
import net.sourceforge.waters.gui.actions.SimulationResetAction;
import net.sourceforge.waters.gui.actions.SimulationShowAllAction;
import net.sourceforge.waters.gui.actions.SimulationStepAction;
import net.sourceforge.waters.gui.actions.SimulationStepBackAction;
import net.sourceforge.waters.gui.actions.VerifyConflictCheckAction;
import net.sourceforge.waters.gui.actions.VerifyControlLoopAction;
import net.sourceforge.waters.gui.actions.VerifyControllabilityAction;
import net.sourceforge.waters.gui.actions.VerifyDeadlockCheckAction;
import net.sourceforge.waters.gui.actions.VerifyDiagnosabilityCheckAction;
import net.sourceforge.waters.gui.actions.VerifyHISCCPControllabilityAction;
import net.sourceforge.waters.gui.actions.VerifyHISCCPInterfaceConsistencyAction;
import net.sourceforge.waters.gui.actions.VerifyLanguageInclusionAction;
import net.sourceforge.waters.gui.actions.VerifyNerodeEquivalenceAction;
import net.sourceforge.waters.gui.actions.VerifyProperTimeBehaviorAction;
import net.sourceforge.waters.gui.actions.VerifySDActivityLoopAction;
import net.sourceforge.waters.gui.actions.VerifySDCFourPropertyAction;
import net.sourceforge.waters.gui.actions.VerifySDCThree_one_propertyAction;
import net.sourceforge.waters.gui.actions.VerifySDCTwoApropertyAction;
import net.sourceforge.waters.gui.actions.VerifySDCTwoBPropertyAction;
import net.sourceforge.waters.gui.actions.VerifySDControllabilityAction;
import net.sourceforge.waters.gui.actions.VerifySDPlantCompletenessAction;
import net.sourceforge.waters.gui.actions.VerifySDSingularPropertyAction;
import net.sourceforge.waters.gui.actions.VerifySICProperty5Action;
import net.sourceforge.waters.gui.actions.VerifySICProperty6Action;
import net.sourceforge.waters.gui.actions.WatersAction;
import net.sourceforge.waters.gui.actions.WatersActionManager;
import net.sourceforge.waters.gui.actions.WatersRedoAction;
import net.sourceforge.waters.gui.actions.WatersUndoAction;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;

import org.supremica.automata.templates.TemplateGroup;
import org.supremica.automata.templates.TemplateItem;
import org.supremica.gui.ExampleTemplates;
import org.supremica.gui.ide.actions.Actions;
import org.supremica.gui.ide.actions.ExitAction;
import org.supremica.gui.ide.actions.ImportAction;
import org.supremica.gui.ide.actions.NewAction;
import org.supremica.gui.ide.actions.OpenAction;
import org.supremica.gui.ide.actions.OpenFTAction;
import org.supremica.gui.ide.actions.OpenFTSpecAction;
import org.supremica.gui.ide.actions.OpenRASAction;
import org.supremica.gui.ide.actions.SaveAction;
import org.supremica.gui.ide.actions.SaveAsAction;
import org.supremica.gui.simulator.ExternalEventExecuter;
import org.supremica.properties.Config;

/**
 * <P>
 * The IDE's main menu bar.
 * </P>
 *
 * <P>
 * The menu bar is updated dynamically when panels are switched, e.g., when the
 * user changes from the editor to the analyser, or when certain configuration
 * options are changed. This is achieved by registering an {@link Observer} on
 * the {@link IDE}, and a {@link OptionChangeListener} on each of the
 * properties concerned.
 * </P>
 *
 * <P>To add a new menu item, first implement its action as a subclass of
 * {@link WatersAction}. Then register it in {@link WatersActionManager} and
 * and add it to a menu in method {@link #createMenus()}. When creating menu
 * items with hotkeys, please check and update the list of used hotkeys in
 * the comments below.</P>
 *
 * <P>
 * To add a new menu, create a new method like {@link #createFileMenu()}
 * and call it from {@link #createMenus()}. It is also possible to create
 * menus that are specific to certain panel times (e.g., Simulate), which
 * is achieved by calling the menu creation method from the {@link
 * MainPanel#createPanelSpecificMenus(IDEMenuBar) createPanelSpecificMenus()}
 * method of the relevant panel.
 * </P>
 *
 * @see WatersActionManager
 * @see Actions
 *
 * @author Knut &Aring;kesson, Robi Malik
 */

//###########################################################################
//# Function keys:
//###########################################################################
//# F1:
//# F2: <??? has system binding ???>
//# F3: Simulator/Jump to start
//# F4: Simulator/Step back
//# F5: Simulator/Replay step
//# F6: <??? has system binding ???>
//# F7: Simulator/Jump to end
//# F8:
//# F9:
//# F10:
//# F11:
//# F12:
//# ESC: Edit/Deselect all

//###########################################################################
//# ALT-Hotkeys:
//# (Beware that toolbar items are also triggered through Alt+mnemonic)
//###########################################################################
//# ALT-a: Create/New automaton (toolbar)
//# ALT-b: Simulator/Step back
//# ALT-c: Create menu
//# ALT-d: Edit menu
//# ALT-e: Create/New event (toolbar)
//# ALT-f: File menu
//# ALT-g: Configure menu
//# ALT-h: Help menu
//# ALT-i: Simulate menu
//# ALT-j:
//# ALT-k:
//# ALT-l: Analyze/Synchronize
//# ALT-m: Modules menu
//# ALT-n: File/New (toolbar)
//# ALT-o: File/Open (toolbar)
//# ALT-p: Examples menu
//# ALT-q:
//# ALT-r: Verify menu
//# ALT-s: File/Save (toolbar)
//# ALT-t: Tools menu
//# ALT-u:
//# ALT-v: Create/New variable (toolbar)
//# ALT-w:
//# ALT-x: File/Exit
//# ALT-y: Analyze/Synthesize
//# ALT-z: Analyze menu

//###########################################################################
//# CTRL-Hotkeys:
//###########################################################################
//# CTRL-a: Edit/Select all
//# CTRL-b:
//# CTRL-c: Edit/Copy
//# CTRL-d:
//# CTRL-e:
//# CTRL-f:
//# CTRL-g:
//# CTRL-h:
//# CTRL-i:
//# CTRL-j:
//# CTRL-k:
//# CTRL-l: Tools/Layout graph
//# CTRL-m: Edit/Show module comments
//# CTRL-n:
//# CTRL-o:
//# CTRL-p: File/Print (not used at the moment)
//# CTRL-q:
//# CTRL-r:
//# CTRL-s: File/Save
//# CTRL-t: Examples/Dynamic examples
//# CTRL-u:
//# CTRL-v: Edit/Paste, Analyze/Verify
//# CTRL-w: Analyze/Workbench
//# CTRL-x: Edit/Cut
//# CTRL-y: Edit/Redo
//# CTRL-z: Edit/Undo


public class IDEMenuBar
  extends JMenuBar
  implements Observer, OptionChangeListener
{

  //#########################################################################
  //# Constructor
  public IDEMenuBar(final IDE ide)
  {
    mIDE = ide;
    mOptions = new THashSet<>();
    createMenus();
    ide.attach(this);
  }


  //#########################################################################
  //# Initialisation
  public Actions getActions()
  {
    return mIDE.getActions();
  }

  public void addOption(final Option<?> option)
  {
    if (mOptions.add(option)) {
      option.addOptionChangeListener(this);
    }
  }


  //#######################################################################
  //# Interface org.supremica.properties.SupremicaPropertyChangeListener
  @Override
  public void optionChanged(final OptionChangeEvent event)
  {
    rebuildMenus();
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
    case MAINPANEL_SWITCH:
      rebuildMenus();
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Shared Menus
  public void createCreateMenu()
  {
    final Actions actions = getActions();
    final JMenu menu = new JMenu("Create");
    menu.setMnemonic(KeyEvent.VK_C);
    final Action insComp =
      actions.getAction(InsertSimpleComponentAction.class);
    menu.add(insComp);
    final Action insVar = actions.getAction(InsertVariableAction.class);
    menu.add(insVar);
    addOption(Config.INCLUDE_INSTANTIATION);
    if (Config.INCLUDE_INSTANTIATION.getBooleanValue()) {
      final Action insConditional =
        actions.getAction(InsertConditionalAction.class);
      menu.add(insConditional);
      final Action insForeach = actions.getAction(InsertForeachAction.class);
      menu.add(insForeach);
      final Action insAlias =
        actions.getAction(InsertConstantAliasAction.class);
      menu.add(insAlias);
      final Action insEventAlias =
        actions.getAction(InsertEventAliasAction.class);
      menu.add(insEventAlias);
      final Action insInstance = actions.getAction(InsertInstanceAction.class);
      menu.add(insInstance);
      final Action inParam =
        actions.getAction(InsertParameterBindingAction.class);
      menu.add(inParam);
    }
    final Action insEvent = actions.getAction(InsertEventDeclAction.class);
    menu.add(insEvent);
    add(menu);
  }

  public void createVerifyMenu()
  {
    final Actions actions = getActions();
    final JMenu menu = new JMenu("Verify");
    menu.setMnemonic(KeyEvent.VK_R);
    final Action controllability =
      actions.getAction(VerifyControllabilityAction.class);
    menu.add(controllability);
    final Action conflict =
      actions.getAction(VerifyConflictCheckAction.class);
    menu.add(conflict);
    final Action deadlock =
      actions.getAction(VerifyDeadlockCheckAction.class);
    menu.add(deadlock);
    final Action controlLoop =
        actions.getAction(VerifyControlLoopAction.class);
    menu.add(controlLoop);
    final Action languageInclusion =
        actions.getAction(VerifyLanguageInclusionAction.class);
    menu.add(languageInclusion);
    final AnalysisAlgorithmOption diagnosabilityOption =
      OptionPage.DiagnosabilityCheck.getTopSelectorOption();
    addOption(diagnosabilityOption);
    if (diagnosabilityOption.getValue() != ModelAnalyzerFactoryLoader.Disabled) {
      final Action diagnosability =
        actions.getAction(VerifyDiagnosabilityCheckAction.class);
      menu.add(diagnosability);
    }
    addOption(Config.GUI_ANALYZER_INCLUDE_HISC);
    if (Config.GUI_ANALYZER_INCLUDE_HISC.getValue()) {
      menu.addSeparator();
      final Action sic5 =
        actions.getAction(VerifySICProperty5Action.class);
      menu.add(sic5);
      final Action sic6 =
        actions.getAction(VerifySICProperty6Action.class);
      menu.add(sic6);
      try {
        final Action hiscCp =
          actions.getAction(VerifyHISCCPInterfaceConsistencyAction.class);
        menu.add(hiscCp);
      } catch (final NoClassDefFoundError error) {
        // skip this if it can't be loaded
      }
      final Action hiscCpCont =
        actions.getAction(VerifyHISCCPControllabilityAction.class);
      menu.add(hiscCpCont);
    }
    addOption(Config.GUI_ANALYZER_INCLUDE_SD);
    if (Config.GUI_ANALYZER_INCLUDE_SD.getValue()) {
      menu.addSeparator();
      final Action plantComplete =
        actions.getAction(VerifySDPlantCompletenessAction.class);
      menu.add(plantComplete);
      final Action activityLoop =
        actions.getAction(VerifySDActivityLoopAction.class);
      menu.add(activityLoop);
      final Action sSingular =
        actions.getAction(VerifySDSingularPropertyAction.class);
      menu.add(sSingular);
      final Action ad1 =
        actions.getAction(VerifySDControllabilityAction.class);
      menu.add(ad1);
      final Action sd2a =
        actions.getAction(VerifySDCTwoApropertyAction.class);
      menu.add(sd2a);
      final Action sd2b =
        actions.getAction(VerifySDCTwoBPropertyAction.class);
      menu.add(sd2b);
      final Action sd31 =
        actions.getAction(VerifySDCThree_one_propertyAction.class);
      menu.add(sd31);
      final Action sd32 =
        actions.getAction(VerifyNerodeEquivalenceAction.class);
      menu.add(sd32);
      final Action sd4 =
        actions.getAction(VerifySDCFourPropertyAction.class);
      menu.add(sd4);
      final Action properTimeBehavior =
        actions.getAction(VerifyProperTimeBehaviorAction.class);
      menu.add(properTimeBehavior);
    }
    add(menu);
  }

  public void createEditorAnalyzeMenu()
  {
    addOption(Config.GUI_ANALYZER_INCLUDE_SEAMLESS_SYNTHESIS);
    if (Config.GUI_ANALYZER_INCLUDE_SEAMLESS_SYNTHESIS.getValue()) {
      final Actions actions = getActions();
      final JMenu menu = new JMenu("Analyze");
      menu.setMnemonic(KeyEvent.VK_Z);  // ALT-A - create automaton?
      menu.add(actions.editorSynthesizerAction.getMenuItem());
      addOption(Config.TUM_EXTERNAL_ON);
      if (Config.TUM_EXTERNAL_ON.getValue()) {
        menu.add(actions.editorGenerateTextLabelAction.getMenuItem());
        menu.add(actions.editorRemoveGABlocksAction.getMenuItem());
      }
      addOption(Config.INCLUDE_EXPERIMENTAL_ALGORITHMS);
      if (Config.INCLUDE_EXPERIMENTAL_ALGORITHMS.getValue()) {
        //IISC Algorithms
        menu.addSeparator();
        menu.add(actions.editorEFASynch.getMenuItem());
        menu.add(actions.editorEFASynchEval.getMenuItem());
        menu.add(actions.editorEFAPE.getMenuItem());
        menu.add(actions.editorIISC.getMenuItem());
      }
      add(menu);
    }
  }

  public void createEditorToolsMenu()
  {
    final Actions actions = getActions();
    final JMenu menu = new JMenu("Tools");
    menu.setMnemonic(KeyEvent.VK_T);
    final Action layout = actions.getAction(GraphLayoutAction.class);
    menu.add(layout);
    addOption(Config.INCLUDE_INSTANTIATION);
    if (Config.INCLUDE_INSTANTIATION.getBooleanValue()) {
      final Action instantiation =
        actions.getAction(InstantiateModuleAction.class);
      menu.add(instantiation);
      final Action recompile = actions.getAction(RecompileAction.class);
      menu.add(recompile);
    }
    add(menu);
  }

  public void createSupremicaAnalyzeMenu()
  {
    final Actions actions = getActions();
    final JMenu menu = new JMenu("Analyze");
    menu.setMnemonic(KeyEvent.VK_Z); // ALT-A - create automaton?
    // View (submenu)
    final JMenu viewMenu = new JMenu("View");
    {
      viewMenu.add(actions.analyzerViewAutomatonAction.getMenuItem());
      viewMenu.add(actions.analyzerViewAlphabetAction.getMenuItem());
      viewMenu.add(actions.analyzerViewStatesAction.getMenuItem());
      viewMenu.add(actions.analyzerViewModularStructureAction.getMenuItem());
    }
    menu.add(viewMenu);
    menu.add(actions.analyzerSynchronizerAction.getMenuItem());
    menu.add(actions.analyzerSynthesizerAction.getMenuItem());
    menu.add(actions.analyzerVerifierAction.getMenuItem());
    menu.add(actions.analyzerMinimizeAction.getMenuItem());
    menu.add(actions.analyzerEventHiderAction.getMenuItem());
    menu.add(actions.analyzerPurgeAction.getMenuItem());
    menu.addSeparator();
    menu.add(actions.analyzerExploreStatesAction.getMenuItem());
    menu.add(actions.analyzerFindStatesAction.getMenuItem());
    menu.add(actions.analyzerWorkbenchAction.getMenuItem());
    menu.addSeparator();
    menu.add(actions.analyzerStatisticsAction.getMenuItem());
    menu.add(actions.analyzerExportAction.getMenuItem());
    menu.addSeparator();
    menu.add(actions.analyzerDeleteSelectedAction.getMenuItem());
    menu.add(actions.analyzerDeleteAllAction.getMenuItem());
    menu.add(actions.analyzerRenameAction.getMenuItem());
    menu.add(actions.analyzerSendToEditorAction.getMenuItem());
    add(menu);
  }

  public void createSupremicaToolsMenu()
  {
    addOption(Config.INCLUDE_ANIMATOR);
    if (Config.INCLUDE_ANIMATOR.getValue()) {
      final Actions actions = getActions();
      final JMenu menu = new JMenu("Tools");
      menu.setMnemonic(KeyEvent.VK_T);
      menu.add(actions.simulatorLaunchAnimatorAction);
      try {
        if (ExternalEventExecuter.isLibraryLoadable()) {
          menu.add(actions.simulatorLaunchSimulatorAction);
        }
      } catch (final UnsatisfiedLinkError | NoClassDefFoundError error) {
        // skip
      }
      menu.add(actions.simulatorClearSimulationData);
      add(menu);
    }
  }

  public void createWatersAnalyzeMenu()
  {
    final Actions actions = getActions();
    final JMenu menu = new JMenu("Analyze");
    menu.setMnemonic(KeyEvent.VK_Z); // ALT-A - create automaton?
    final Action sync =
      actions.getAction(AnalyzerSynchronousProductAction.class);
    menu.add(sync);
    final Action synth = actions.getAction(AnalyzerSynthesisAction.class);
    menu.add(synth);
    final Action workbench =
      actions.getAction(AnalyzerWorkbenchAction.class);
    menu.add(workbench);
    menu.addSeparator();
    final Action control =
      actions.getAction(AnalyzerControllabilityCheckAction.class);
    menu.add(control);
    final Action conflict =
      actions.getAction(AnalyzerConflictCheckAction.class);
    menu.add(conflict);
    final Action deadlock =
      actions.getAction(AnalyzerDeadlockCheckAction.class);
    menu.add(deadlock);
    final Action controllability =
      actions.getAction(AnalyzerControlLoopCheckAction.class);
    menu.add(controllability);
    final Action languageInclusion =
      actions.getAction(AnalyzerLanguageInclusionCheckAction.class);
    menu.add(languageInclusion);
    final AnalysisAlgorithmOption diagnosabilityOption =
      OptionPage.DiagnosabilityCheck.getTopSelectorOption();
    addOption(diagnosabilityOption);
    if (diagnosabilityOption.getValue() != ModelAnalyzerFactoryLoader.Disabled) {
      final Action diagnosability =
        actions.getAction(AnalyzerDiagnosabilityCheckAction.class);
      menu.add(diagnosability);
    }
    menu.addSeparator();
    final Action hide =
      actions.getAction(AnalyzerHideAction.class);
    menu.add(hide);
    final Action simplifier =
      actions.getAction(AnalyzerSimplifierAction.class);
    menu.add(simplifier);
    final Action stateCounter =
      actions.getAction(AnalyzerCountStatesAction.class);
    menu.add(stateCounter);
    add(menu);
  }

  public void createSimulateMenu()
  {
    final Actions actions = getActions();
    final JMenu menu = new JMenu("Simulate");
    menu.setMnemonic(KeyEvent.VK_I);
    final Action reset = actions.getAction(SimulationResetAction.class);
    menu.add(reset);
    final Action stepBeginning =
      actions.getAction(SimulationBackToStartAction.class);
    menu.add(stepBeginning);
    final Action stepBack =
      actions.getAction(SimulationStepBackAction.class);
    menu.add(stepBack);
    final Action step = actions.getAction(SimulationStepAction.class);
    menu.add(step);
    final Action replayStep =
      actions.getAction(SimulationReplayStepAction.class);
    menu.add(replayStep);
    final Action endTrace =
      actions.getAction(SimulationJumpToEndAction.class);
    menu.add(endTrace);
    menu.addSeparator();
    final Action showAll =
      actions.getAction(SimulationShowAllAction.class);
    menu.add(showAll);
    final Action closeAll =
      actions.getAction(SimulationCloseAllAction.class);
    menu.add(closeAll);
    final Action cascade =
      actions.getAction(SimulationCascadeAction.class);
    menu.add(cascade);
    add(menu);
  }


  //#########################################################################
  //# Fixed Menus
  private void createFileMenu()
  {
    final Actions actions = getActions();
    final JMenu menu = new JMenu("File");
    menu.setMnemonic(KeyEvent.VK_F); // ALT-F - Create module event?
    final Action newmod = actions.getAction(NewAction.class);
    menu.add(newmod);
    final Action open = actions.getAction(OpenAction.class);
    menu.add(open);
    addOption(Config.INCLUDE_RAS_SUPPORT);
    if (Config.INCLUDE_RAS_SUPPORT.getBooleanValue()) {
      final Action openRas = actions.getAction(OpenRASAction.class);
      menu.add(openRas);
    }
    final Action save = actions.getAction(SaveAction.class);
    menu.add(save);
    final Action saveas = actions.getAction(SaveAsAction.class);
    menu.add(saveas);
    final Action close = actions.getAction(IDECloseAction.class);
    menu.add(close);
    menu.addSeparator();
    final Action importAction = actions.getAction(ImportAction.class);
    menu.add(importAction);
    addOption(Config.INCLUDE_FT_SUPPORT);
    if (Config.INCLUDE_FT_SUPPORT.getBooleanValue()) {
      final Action openFT = actions.getAction(OpenFTAction.class);
      menu.add(openFT);
      final Action openFTSpec = actions.getAction(OpenFTSpecAction.class);
      menu.add(openFTSpec);
    }
    //mFileMenu.add(mIDE.getActions().editorPrintAction.getMenuItem());
    final Action epsprint = actions.getAction(GraphSaveEPSAction.class);
    menu.add(epsprint);
    final Action pdfprint = actions.getAction(GraphSavePDFAction.class);
    menu.add(pdfprint);
    menu.addSeparator();
    final Action exit = actions.getAction(ExitAction.class);
    menu.add(exit);
    add(menu);
  }

  private void createEditMenu()
  {
    final Actions actions = getActions();
    final JMenu menu = new JMenu("Edit");
    menu.setMnemonic(KeyEvent.VK_D); // ALT-E - Create component event?
    final Action undo = actions.getAction(WatersUndoAction.class);
    menu.add(undo);
    final Action redo = actions.getAction(WatersRedoAction.class);
    menu.add(redo);
    menu.addSeparator();
    final Action delete = actions.getAction(IDEDeleteAction.class);
    menu.add(delete);
    final Action cut = actions.getAction(IDECutAction.class);
    menu.add(cut);
    final Action copy = actions.getAction(IDECopyAction.class);
    menu.add(copy);
    final Action paste = actions.getAction(IDEPasteAction.class);
    menu.add(paste);
    menu.addSeparator();
    final Action select = actions.getAction(IDESelectAllAction.class);
    menu.add(select);
    final Action deselect = actions.getAction(IDEDeselectAllAction.class);
    menu.add(deselect);
    menu.addSeparator();
    final Action properties = actions.getAction(IDEPropertiesAction.class);
    menu.add(properties);
    final Action showgraph = actions.getAction(ShowGraphAction.class);
    menu.add(showgraph);
    final Action showcomment = actions.getAction(ShowModuleCommentAction.class);
    menu.add(showcomment);
    add(menu);
  }

  private void createExamplesMenu()
  {
    if (!Config.GENERAL_STUDENT_VERSION.getBooleanValue()) {
      final Actions actions = getActions();
      final ExampleTemplates exTempl = ExampleTemplates.getRefreshedInstance();
      if (!exTempl.isEmpty()) {
        final JMenu menu = new JMenu("Examples");
        menu.setMnemonic(KeyEvent.VK_P);
        menu.add(actions.toolsTestCasesAction.getMenuItem());
        for (final TemplateGroup currGroup : exTempl) {
          final JMenu menuFileNewFromTemplateGroup = new JMenu();
          menuFileNewFromTemplateGroup.setText(currGroup.getName());
          menuFileNewFromTemplateGroup.setToolTipText
          (currGroup.getShortDescription());
          menu.add(menuFileNewFromTemplateGroup);
          for (final TemplateItem currItem : currGroup) {
            final JMenuItem menuItem = new JMenuItem();
            menuItem.setText(currItem.getName());
            menuItem.setToolTipText(currItem.getShortDescription());
            menuFileNewFromTemplateGroup.add(menuItem);
            menuItem.addActionListener(new NewFromTemplateHandler(currItem));
          }
        }
        add(menu);
      }
    }
  }

  private void createModulesMenu()
  {
    final JMenu menu = new JMenu("Modules");
    menu.setMnemonic(KeyEvent.VK_M);
    int count = 0;
    final DocumentContainerManager manager = mIDE.getDocumentContainerManager();
    if (manager != null) {
      final DocumentContainer active = manager.getActiveContainer();
      for (final DocumentContainer container : manager.getRecent()) {
        final JMenuItem item = createModuleMenuItem(container);
        item.setEnabled(container != active);
        menu.add(item);
        if (++count >= MAX_MODULES) {
          break;
        }
      }
    }
    menu.setEnabled(count > 0);
    add(menu);
  }

  private JMenuItem createModuleMenuItem(final DocumentContainer container)
  {
    final JMenuItem item = new JMenuItem();
    final File file = container.getFileLocation();
    if (file == null) {
      final DocumentProxy doc = container.getDocument();
      final String name = doc.getName();
      if (name == null || name.equals("")) {
        item.setText("<nameless>");
      } else {
        item.setText(name);
      }
    } else {
      final String path = file.getPath();
      final int index = path.lastIndexOf(File.separatorChar);
      final String tail = index >= 0 ? path.substring(index + 1) : path;
      item.setText(tail);
      item.setToolTipText(path);
    }
    final ActionListener listener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        final DocumentContainerManager manager =
          mIDE.getDocumentContainerManager();
        manager.setActiveContainer(container);
      }
    };
    item.addActionListener(listener);
    return item;
  }

  private void createConfigureMenu()
  {
    final Actions actions = getActions();
    final JMenu menu = new JMenu("Configure");
    menu.setMnemonic(KeyEvent.VK_G);
    final Action options = actions.getAction(OptionsAction.class);
    menu.add(options);
    add(menu);
  }

  private void createHelpMenu()
  {
    final Actions actions = getActions();
    final JMenu menu = new JMenu("Help");
    menu.setMnemonic(KeyEvent.VK_H);
    final Action about = actions.getAction(IDEAboutAction.class);
    menu.add(about);
    menu.add(actions.helpWebAction.getMenuItem());
    final Action help = actions.getAction(IDEHelpAction.class);
    menu.add(help);
    add(menu);
  }


  //#########################################################################
  //# Menu Building
  private void rebuildMenus()
  {
    removeAll();
    removeListeners();
    createMenus();
    revalidate();
    repaint();
  }

  private void createMenus()
  {
    createFileMenu();
    createEditMenu();
    final MainPanel panel = getActivePanel();
    if (panel != null) {
      panel.createPanelSpecificMenus(this);
    }
    createExamplesMenu();
    createModulesMenu();
    createConfigureMenu();
    createHelpMenu();
  }

  private void removeListeners()
  {
    for (final Option<?> option : mOptions) {
      option.removeOptionChangeListener(this);
    }
    mOptions.clear();
  }

  private MainPanel getActivePanel()
  {
    final DocumentContainer container = mIDE.getActiveDocumentContainer();
    if (container == null) {
      return null;
    } else {
      return container.getActivePanel();
    }
  }


  //#########################################################################
  //# Inner Class NewFromTemplateHandler
  class NewFromTemplateHandler implements ActionListener
  {
    //#######################################################################
    //# Constructor
    public NewFromTemplateHandler(final TemplateItem item)
    {
      mItem = item;
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    @Override
    public void actionPerformed(final ActionEvent event)
    {
      try {
        final String path = mItem.getPath();
        final URL url = TemplateItem.class.getResource(path);
        final URI uri = url.toURI();
        final DocumentContainerManager manager =
            mIDE.getDocumentContainerManager();
        manager.openContainer(uri);
      } catch (final URISyntaxException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    //#######################################################################
    //# Data Members
    private final TemplateItem mItem;
  }


  //#########################################################################
  //# Data Members
  private final IDE mIDE;
  private final Collection<Option<?>> mOptions;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -5837298363631259731L;

  private static final int MAX_MODULES = 24;

}
