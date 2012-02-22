//# -*- tab-width: 4  indent-tabs-mode: null  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   IDEMenuBar
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.gui.ide;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.sourceforge.waters.gui.actions.AnalyzeConflictCheckAction;
import net.sourceforge.waters.gui.actions.AnalyzeControlLoopAction;
import net.sourceforge.waters.gui.actions.AnalyzeControllabilityAction;
import net.sourceforge.waters.gui.actions.AnalyzeLanguageInclusionAction;
import net.sourceforge.waters.gui.actions.AnalyzeSDCThree_one_propertyAction;
import net.sourceforge.waters.gui.actions.AnalyzeSDControllabilityAction;
import net.sourceforge.waters.gui.actions.AnalyzeSDNSLActivityLoopAction;
import net.sourceforge.waters.gui.actions.AnalyzeSICProperty5Action;
import net.sourceforge.waters.gui.actions.AnalyzeSICProperty6Action;
import net.sourceforge.waters.gui.actions.GraphLayoutAction;
import net.sourceforge.waters.gui.actions.GraphSaveEPSAction;
import net.sourceforge.waters.gui.actions.IDECopyAction;
import net.sourceforge.waters.gui.actions.IDECutAction;
import net.sourceforge.waters.gui.actions.IDEDeleteAction;
import net.sourceforge.waters.gui.actions.IDEDeselectAllAction;
import net.sourceforge.waters.gui.actions.IDEPasteAction;
import net.sourceforge.waters.gui.actions.IDEPropertiesAction;
import net.sourceforge.waters.gui.actions.IDESelectAllAction;
import net.sourceforge.waters.gui.actions.InsertConstantAliasAction;
import net.sourceforge.waters.gui.actions.InsertEventAliasAction;
import net.sourceforge.waters.gui.actions.InsertEventDeclAction;
import net.sourceforge.waters.gui.actions.InsertForeachAction;
import net.sourceforge.waters.gui.actions.InsertInstanceAction;
import net.sourceforge.waters.gui.actions.InsertParameterBindingAction;
import net.sourceforge.waters.gui.actions.InsertSimpleComponentAction;
import net.sourceforge.waters.gui.actions.InsertVariableAction;
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
import net.sourceforge.waters.gui.actions.WatersRedoAction;
import net.sourceforge.waters.gui.actions.WatersUndoAction;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;

import org.supremica.automata.templates.TemplateGroup;
import org.supremica.automata.templates.TemplateItem;
import org.supremica.gui.ExampleTemplates;
import org.supremica.gui.ide.actions.Actions;
import org.supremica.gui.ide.actions.CloseAction;
import org.supremica.gui.ide.actions.ExitAction;
import org.supremica.gui.ide.actions.ImportAction;
import org.supremica.gui.ide.actions.NewAction;
import org.supremica.gui.ide.actions.OpenAction;
import org.supremica.gui.ide.actions.SaveAction;
import org.supremica.gui.ide.actions.SaveAsAction;
import org.supremica.properties.Config;
import org.supremica.properties.SupremicaPropertyChangeEvent;
import org.supremica.properties.SupremicaPropertyChangeListener;

import  net.sourceforge.waters.gui.actions.AnalyzeSDPlantCompletenessAction;
import net.sourceforge.waters.gui.actions.AnalyzeSDActivityLoopAction;
import net.sourceforge.waters.gui.actions.AnalyzeSDSingularPropertyAction;
import net.sourceforge.waters.gui.actions.AnalyzeSDCTwoApropertyAction;
import net.sourceforge.waters.gui.actions.AnalyzeSDCTwoBPropertyAction;
import net.sourceforge.waters.gui.actions.AnalyzeSDCFourPropertyAction;
import net.sourceforge.waters.gui.actions.AnalyzeProperTimeBehaviorPropertyAction;
import net.sourceforge.waters.gui.actions.AnalyzeNerodeEquivalentAction;

/**
 * <P>
 * The IDE's main menu bar.
 * </P>
 *
 * <P>
 * The menu bar is updated dynamically when panels are switched, e.g., when the
 * user changes from the editor to the analyser, or when certain configuration
 * options are changed. This is achieved by registering an {@link Observer} on
 * the {@link IDE}, and a {@link SupremicaPropertyChangeListener} on each of the
 * properties concerned.
 * </P>
 *
 * <P>
 * To add a new menu item, first implement its action, and then add it to a menu
 * in method {@link #createMenus()}. When creating menu items with hotkeys,
 * please check and update the list of used hotkeys in the comments below.
 * </P>
 *
 * <P>
 * To add a new menu, add a new data member for it at the end of the class,
 * initialise it in method {@link #createMenus()}, and add it to the menu bar in
 * method {@link #addMenus()}.
 * </P>
 *
 * @see net.sourceforge.waters.gui.actions.WatersActionManager
 * @see org.supremica.gui.ide.actions.Actions
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

//###########################################################################
//# ALT-Hotkeys:
//###########################################################################
//# ALT-a: Create/New automaton
//# ALT-b: Simulator/Step back
//# ALT-c: Create menu
//# ALT-d: Edit menu
//# ALT-e: Create/New event
//# ALT-f: File menu
//# ALT-g:
//# ALT-h: Help menu
//# ALT-i:
//# ALT-j:
//# ALT-k:
//# ALT-l:
//# ALT-m: Modules menu
//# ALT-n:
//# ALT-o: Configure menu
//# ALT-p: Examples menu
//# ALT-q:
//# ALT-r: Verify menu
//# ALT-s: Simulator menu
//# ALT-t: Tools menu
//# ALT-u:
//# ALT-v: Create/New variable
//# ALT-w:
//# ALT-x: File/Exit
//# ALT-y:
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
//# CTRL-l: Edit/Layout graph
//# CTRL-m: Edit/Show module comments
//# CTRL-n: File/New
//# CTRL-o: File/Open
//# CTRL-p: File/Print
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


public class IDEMenuBar extends JMenuBar
{

  // #########################################################################
  // # Constructor
  public IDEMenuBar(final IDE ide)
  {
    mIDE = ide;
    createMenus();
    addMenus();
    updateModulesMenu();
    final IDEListener ideListener = new IDEListener();
    ide.attach(ideListener);
    final SupremicaPropertyChangeListener createListener =
        new CreatePropertyListener();
    Config.INCLUDE_INSTANTION.addPropertyChangeListener(createListener);
    final SupremicaPropertyChangeListener toolsListener =
        new ToolsPropertyListener();
    final SupremicaPropertyChangeListener analyzeListener =
        new AnalyzePropertyListener();
    Config.INCLUDE_EXTERNALTOOLS.addPropertyChangeListener(toolsListener);
    Config.INCLUDE_SOCEDITOR.addPropertyChangeListener(toolsListener);
    Config.INCLUDE_ANIMATOR.addPropertyChangeListener(toolsListener);
    Config.INCLUDE_WATERS_SIMULATOR.addPropertyChangeListener(analyzeListener);
    Config.GUI_ANALYZER_INCLUDE_HISC.addPropertyChangeListener(analyzeListener);
    Config.GUI_ANALYZER_INCLUDE_SD.addPropertyChangeListener(analyzeListener);
    Config.GUI_ANALYZER_INCLUDE_SEAMLESS_SYNTHESIS.addPropertyChangeListener
      (analyzeListener);
  }

  // #########################################################################
  // # Initialisation
  private void createMenus()
  {
    final Actions actions = mIDE.getActions();

    // File
    if (mFileMenu == null) {
      mFileMenu = new JMenu("File");
      mFileMenu.setMnemonic(KeyEvent.VK_F); // ALT-F - Create module event?
      final Action newmod = actions.getAction(NewAction.class);
      mFileMenu.add(newmod);
      final Action open = actions.getAction(OpenAction.class);
      mFileMenu.add(open);
      final Action save = actions.getAction(SaveAction.class);
      mFileMenu.add(save);
      final Action saveas = actions.getAction(SaveAsAction.class);
      mFileMenu.add(saveas);
      final Action close = actions.getAction(CloseAction.class);
      mFileMenu.add(close);
      mFileMenu.addSeparator();
      final Action importAction = actions.getAction(ImportAction.class);
      mFileMenu.add(importAction);
      mFileMenu.add(mIDE.getActions().editorPrintAction.getMenuItem());
      mFileMenu.add(mIDE.getActions().editorSavePostscriptAction.getMenuItem());
      final Action epsprint = actions.getAction(GraphSaveEPSAction.class);
      mFileMenu.add(epsprint);
      mFileMenu.add(mIDE.getActions().editorSavePDFAction.getMenuItem());
      mFileMenu.addSeparator();
      final Action exit = actions.getAction(ExitAction.class);
      mFileMenu.add(exit);
    }

    // Edit
    if (mEditMenu == null) {
      mEditMenu = new JMenu("Edit");
      mEditMenu.setMnemonic(KeyEvent.VK_D); // ALT-E - Create component event?
      final Action undo = actions.getAction(WatersUndoAction.class);
      mEditMenu.add(undo);
      final Action redo = actions.getAction(WatersRedoAction.class);
      mEditMenu.add(redo);
      mEditMenu.addSeparator();
      final Action delete = actions.getAction(IDEDeleteAction.class);
      mEditMenu.add(delete);
      final Action cut = actions.getAction(IDECutAction.class);
      mEditMenu.add(cut);
      final Action copy = actions.getAction(IDECopyAction.class);
      mEditMenu.add(copy);
      final Action paste = actions.getAction(IDEPasteAction.class);
      mEditMenu.add(paste);
      mEditMenu.addSeparator();
      final Action select = actions.getAction(IDESelectAllAction.class);
      mEditMenu.add(select);
      final Action deselect = actions.getAction(IDEDeselectAllAction.class);
      mEditMenu.add(deselect);
      mEditMenu.addSeparator();
      final Action properties = actions.getAction(IDEPropertiesAction.class);
      mEditMenu.add(properties);
      final Action showgraph = actions.getAction(ShowGraphAction.class);
      mEditMenu.add(showgraph);
      final Action showcomment =
          actions.getAction(ShowModuleCommentAction.class);
      mEditMenu.add(showcomment);
      // Embedder should probably go to 'Tools' menu?
      final Action layout = actions.getAction(GraphLayoutAction.class);
      mEditMenu.add(layout);
    }

    final Component panel = getActivePanel();
    if (panel != null) {
      // Create
      // Why not "Insert"? All MS programs use insert. ~~~Robi
      if (mCreateMenu == null && panel instanceof EditorPanel) {
        mCreateMenu = new JMenu("Create");
        mCreateMenu.setMnemonic(KeyEvent.VK_C);
        final Action inscomp =
            actions.getAction(InsertSimpleComponentAction.class);
        mCreateMenu.add(inscomp);
        final Action insvar = actions.getAction(InsertVariableAction.class);
        mCreateMenu.add(insvar);
        if (Config.INCLUDE_INSTANTION.isTrue()) {
          final Action insforeach =
              actions.getAction(InsertForeachAction.class);
          mCreateMenu.add(insforeach);
          final Action insalias =
            actions.getAction(InsertConstantAliasAction.class);
          mCreateMenu.add(insalias);
          final Action inseventalias =
            actions.getAction(InsertEventAliasAction.class);
          mCreateMenu.add(inseventalias);
       // TODO Auto-generated method stub
          final Action insinstance =
            actions.getAction(InsertInstanceAction.class);
          mCreateMenu.add(insinstance);
          final Action inparam =
            actions.getAction(InsertParameterBindingAction.class);
          mCreateMenu.add(inparam);
        }
        final Action insevent = actions.getAction(InsertEventDeclAction.class);
        mCreateMenu.add(insevent);
        // menu.add(ide.getActions().editorAddInstanceAction.getMenuItem());
        // menu.add(ide.getActions().editorAddBindingAction.getMenuItem());
      }

      // Verify
      if (mVerifyMenu == null &&
          Config.INCLUDE_WATERS_SIMULATOR.isTrue() &&
          (panel instanceof EditorPanel || panel instanceof SimulatorPanel)) {
        mVerifyMenu = new JMenu("Verify");
        mVerifyMenu.setMnemonic(KeyEvent.VK_R);
        final Action conflict =
            actions.getAction(AnalyzeConflictCheckAction.class);
        mVerifyMenu.add(conflict);
        final Action controllability =
            actions.getAction(AnalyzeControllabilityAction.class);
        mVerifyMenu.add(controllability);
        final Action controlLoop =
            actions.getAction(AnalyzeControlLoopAction.class);
        mVerifyMenu.add(controlLoop);
        final Action languageInclusion =
            actions.getAction(AnalyzeLanguageInclusionAction.class);
        mVerifyMenu.add(languageInclusion);
        if (Config.GUI_ANALYZER_INCLUDE_HISC.isTrue()) {
          mVerifyMenu.addSeparator();
          final Action sic5 =
            actions.getAction(AnalyzeSICProperty5Action.class);
          mVerifyMenu.add(sic5);
          final Action sic6 =
            actions.getAction(AnalyzeSICProperty6Action.class);
          mVerifyMenu.add(sic6);
        }
	    if (Config.GUI_ANALYZER_INCLUDE_SD.isTrue()) {
           mVerifyMenu.addSeparator();
           final Action plantComplete =
           actions.getAction(AnalyzeSDPlantCompletenessAction.class);
            mVerifyMenu.add(plantComplete);
           final Action activityLoop =
           actions.getAction(AnalyzeSDActivityLoopAction.class);
            mVerifyMenu.add(activityLoop);
            final Action nslactivityLoop =
                actions.getAction(AnalyzeSDNSLActivityLoopAction.class);
                 mVerifyMenu.add(nslactivityLoop);
            final Action SSingular =
              actions.getAction(AnalyzeSDSingularPropertyAction.class);
            mVerifyMenu.add(SSingular);
            final Action Sdone =
              actions.getAction(AnalyzeSDControllabilityAction.class);
            mVerifyMenu.add(Sdone);
            final Action Sdtwoa =
              actions.getAction(AnalyzeSDCTwoApropertyAction.class);
            mVerifyMenu.add(Sdtwoa);
            final Action Sdtwob =
              actions.getAction(AnalyzeSDCTwoBPropertyAction.class);
            mVerifyMenu.add(Sdtwob);
            final Action Sdthree1 =
              actions.getAction(AnalyzeSDCThree_one_propertyAction.class);
            mVerifyMenu.add(Sdthree1);
                        final Action SDthree2 =
                actions.getAction(AnalyzeNerodeEquivalentAction.class);
              mVerifyMenu.add(SDthree2);
            final Action Sdfour =
              actions.getAction(AnalyzeSDCFourPropertyAction.class);
            mVerifyMenu.add(Sdfour);
            final Action PTimeBeh =
              actions.getAction(AnalyzeProperTimeBehaviorPropertyAction.class);
            mVerifyMenu.add(PTimeBeh);

		}
      }

      // Analyze
      if (mEdAnalyzeMenu == null &&
          Config.GUI_ANALYZER_INCLUDE_SEAMLESS_SYNTHESIS.isTrue() &&
          panel instanceof EditorPanel) {
        mEdAnalyzeMenu = new JMenu("Analyze");
        mEdAnalyzeMenu.setMnemonic(KeyEvent.VK_Z);
        mEdAnalyzeMenu.add(actions.editorSynthesizerAction.getMenuItem());
        mEdAnalyzeMenu.add(actions.editorReachabilityGraphAction.getMenuItem());
      }

      // Simulate
      if (mSimulateMenu == null && panel instanceof SimulatorPanel) {
        mSimulateMenu = new JMenu("Simulate");
        mSimulateMenu.setMnemonic(KeyEvent.VK_S);
        final Action reset = actions.getAction(SimulationResetAction.class);
        mSimulateMenu.add(reset);
        final Action stepBeginning =
          actions.getAction(SimulationBackToStartAction.class);
        mSimulateMenu.add(stepBeginning);
        final Action stepBack =
          actions.getAction(SimulationStepBackAction.class);
        mSimulateMenu.add(stepBack);
        final Action step = actions.getAction(SimulationStepAction.class);
        mSimulateMenu.add(step);
        final Action replayStep =
          actions.getAction(SimulationReplayStepAction.class);
        mSimulateMenu.add(replayStep);
        final Action endTrace =
          actions.getAction(SimulationJumpToEndAction.class);
        mSimulateMenu.add(endTrace);
        mSimulateMenu.addSeparator();
        final Action showAll =
          actions.getAction(SimulationShowAllAction.class);
        mSimulateMenu.add(showAll);
        final Action closeAll =
          actions.getAction(SimulationCloseAllAction.class);
        mSimulateMenu.add(closeAll);
        final Action cascade =
          actions.getAction(SimulationCascadeAction.class);
        mSimulateMenu.add(cascade);
      }

      // Analyze
      if (mAnalyzeMenu == null && panel instanceof AnalyzerPanel) {
        mAnalyzeMenu = new JMenu("Analyze");
        mAnalyzeMenu.setMnemonic(KeyEvent.VK_Z); // ALT-A - create automaton?
        // View (submenu)
        final JMenu viewMenu = new JMenu("View");
        {
          viewMenu.add(actions.analyzerViewAutomatonAction.getMenuItem());
          viewMenu.add(actions.analyzerViewAlphabetAction.getMenuItem());
          viewMenu.add(actions.analyzerViewStatesAction.getMenuItem());
          viewMenu
              .add(actions.analyzerViewModularStructureAction.getMenuItem());
        }
        mAnalyzeMenu.add(viewMenu);
        mAnalyzeMenu.add(actions.analyzerSynchronizerAction.getMenuItem());
        mAnalyzeMenu.add(actions.analyzerSynthesizerAction.getMenuItem());
        mAnalyzeMenu.add(actions.analyzerVerifierAction.getMenuItem());
        mAnalyzeMenu.add(actions.analyzerMinimizeAction.getMenuItem());
        mAnalyzeMenu.add(actions.analyzerEventHiderAction.getMenuItem());
        mAnalyzeMenu.add(actions.analyzerPurgeAction.getMenuItem());
        mAnalyzeMenu.addSeparator();
        mAnalyzeMenu.add(actions.analyzerExploreStatesAction.getMenuItem());
        mAnalyzeMenu.add(actions.analyzerFindStatesAction.getMenuItem());
        mAnalyzeMenu.add(actions.analyzerWorkbenchAction.getMenuItem());
        mAnalyzeMenu.addSeparator();
        mAnalyzeMenu.add(actions.analyzerStatisticsAction.getMenuItem());
        mAnalyzeMenu.add(actions.analyzerExportAction.getMenuItem());
        mAnalyzeMenu.addSeparator();
        mAnalyzeMenu.add(actions.analyzerDeleteSelectedAction.getMenuItem());
        mAnalyzeMenu.add(actions.analyzerDeleteAllAction.getMenuItem());
        mAnalyzeMenu.add(actions.analyzerRenameAction.getMenuItem());
        mAnalyzeMenu.add(actions.analyzerSendToEditorAction.getMenuItem());
      }
    }

    // Tools
    if (mToolsMenu == null && Config.INCLUDE_EXTERNALTOOLS.isTrue()) {
      if (Config.INCLUDE_SOCEDITOR.isTrue()) {
        mToolsMenu = new JMenu("Tools");
        mToolsMenu.add(actions.toolsSOCEditorAction.getMenuItem());
      }
      if (Config.INCLUDE_ANIMATOR.isTrue()) {
        if (mToolsMenu == null) {
          mToolsMenu = new JMenu("Tools");
        } else {
          mToolsMenu.addSeparator();
        }
        mToolsMenu.add(actions.simulatorLaunchAnimatorAction);
        mToolsMenu.add(actions.simulatorLaunchSimulatorAction);
        mToolsMenu.add(actions.simulatorClearSimulationData);
      }
      if (mToolsMenu != null) {
        mToolsMenu.setMnemonic(KeyEvent.VK_T);
      }
    }

    // Examples
    if (mExamplesMenu == null) {
      final ExampleTemplates exTempl = ExampleTemplates.getInstance();
      if (!exTempl.isEmpty()) {
        mExamplesMenu = new JMenu("Examples");
        mExamplesMenu.setMnemonic(KeyEvent.VK_P);
        mExamplesMenu.add(actions.toolsTestCasesAction.getMenuItem());
        for (final TemplateGroup currGroup : exTempl) {
          final JMenu menuFileNewFromTemplateGroup = new JMenu();
          menuFileNewFromTemplateGroup.setText(currGroup.getName());
          menuFileNewFromTemplateGroup.setToolTipText(currGroup
                                                      .getShortDescription());
          mExamplesMenu.add(menuFileNewFromTemplateGroup);
          for (final TemplateItem currItem : currGroup) {
            final JMenuItem menuItem = new JMenuItem();
            menuItem.setText(currItem.getName());
            menuItem.setToolTipText(currItem.getShortDescription());
            menuFileNewFromTemplateGroup.add(menuItem);
            menuItem.addActionListener(new NewFromTemplateHandler(currItem));
          }
        }
      }
    }

    // Modules
    if (mModulesMenu == null) {
      mModulesMenu = new JMenu("Modules");
      mModulesMenu.setMnemonic(KeyEvent.VK_M);
      mModulesMenu.setEnabled(false);
    }

    // Configure
    if (mConfigureMenu == null) {
      mConfigureMenu = new JMenu("Configure");
      mConfigureMenu.setMnemonic(KeyEvent.VK_O);
      mConfigureMenu.add(actions.analyzerOptionsAction.getMenuItem());
    }

    // Help
    if (mHelpMenu == null) {
      mHelpMenu = new JMenu("Help");
      mHelpMenu.setMnemonic(KeyEvent.VK_H);
      mHelpMenu.add(actions.helpWebAction.getMenuItem());
      mHelpMenu.addSeparator();
      mHelpMenu.add(actions.helpAboutAction.getMenuItem());
    }
  }

  private void addMenus()
  {
    add(mFileMenu);
    add(mEditMenu);
    final Component panel = getActivePanel();
    if (panel != null) {
      if (panel instanceof EditorPanel) {
        add(mCreateMenu);
        if (mVerifyMenu != null) {
          add(mVerifyMenu);
        }
        if (mEdAnalyzeMenu != null) {
          add(mEdAnalyzeMenu);
        }
      } else if (panel instanceof SimulatorPanel) {
        add(mSimulateMenu);
        if (mVerifyMenu != null) {
          add(mVerifyMenu);
        }
      } else if (panel instanceof AnalyzerPanel) {
        add(mAnalyzeMenu);
      }
    }
    if (mToolsMenu != null) {
      add(mToolsMenu);
    }
    if (mExamplesMenu != null) {
      add(mExamplesMenu);
    }
    add(mModulesMenu);
    add(mConfigureMenu);
    add(mHelpMenu);
  }

  private void rebuildMenus()
  {
    removeAll();
    createMenus();
    addMenus();
    revalidate();
    repaint();
  }

  private Component getActivePanel()
  {
    final DocumentContainer container = mIDE.getActiveDocumentContainer();
    if (container == null) {
      return null;
    } else {
      return container.getActivePanel();
    }
  }

  // #########################################################################
  // # Auxiliary Methods
  private void updateModulesMenu()
  {
    mModulesMenu.removeAll();
    int count = 0;
    final DocumentContainerManager manager = mIDE.getDocumentContainerManager();
    if (manager != null) {
      final DocumentContainer active = manager.getActiveContainer();
      for (final DocumentContainer container : manager.getRecent()) {
        final JMenuItem item = createModuleMenuItem(container);
        item.setEnabled(container != active);
        mModulesMenu.add(item);
        if (++count >= MAX_MODULES) {
          break;
        }
      }
    }
    mModulesMenu.setEnabled(count > 0);
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


  // #########################################################################
  // # Inner Class IDEListener
  private class IDEListener implements Observer
  {
    // #######################################################################
    // # Interface net.sourceforge.waters.gui.observer.Observer
    public void update(final EditorChangedEvent event)
    {
      switch (event.getKind()) {
      case CONTAINER_SWITCH:
        updateModulesMenu();
        rebuildMenus();
        break;
      case MAINPANEL_SWITCH:
        rebuildMenus();
        break;
      default:
        break;
      }
    }
  }


  // #########################################################################
  // # Inner Class CreatePropertyListener
  private class CreatePropertyListener implements
      SupremicaPropertyChangeListener
  {
    // #######################################################################
    // # Interface org.supremica.properties.SupremicaPropertyChangeListener
    public void propertyChanged(final SupremicaPropertyChangeEvent event)
    {
      mCreateMenu = null;
      rebuildMenus();
    }
  }


  // #########################################################################
  // # Inner Class ToolsPropertyListener
  private class ToolsPropertyListener implements
      SupremicaPropertyChangeListener
  {
    // #######################################################################
    // # Interface org.supremica.properties.SupremicaPropertyChangeListener
    public void propertyChanged(final SupremicaPropertyChangeEvent event)
    {
      mToolsMenu = null;
      rebuildMenus();
    }
  }


  private class AnalyzePropertyListener implements
      SupremicaPropertyChangeListener
  {
    // #######################################################################
    // # Interface org.supremica.properties.SupremicaPropertyChangeListener
    public void propertyChanged(final SupremicaPropertyChangeEvent event)
    {
      mVerifyMenu = null;
      mEdAnalyzeMenu = null;
      rebuildMenus();
    }
  }


  // #########################################################################
  // # Inner Class NewFromTemplateHandler
  class NewFromTemplateHandler implements ActionListener
  {
    // #######################################################################
    // # Constructor
    public NewFromTemplateHandler(final TemplateItem item)
    {
      mItem = item;
    }

    // #######################################################################
    // # Interface java.awt.event.ActionListener
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

    // #######################################################################
    // # Data Members
    private final TemplateItem mItem;
  }

  // #########################################################################
  // # Data Members
  private final IDE mIDE;

  private JMenu mFileMenu = null;
  private JMenu mEditMenu = null;
  private JMenu mCreateMenu = null;
  private JMenu mVerifyMenu = null;
  private JMenu mSimulateMenu = null;
  private JMenu mAnalyzeMenu = null;
  private JMenu mEdAnalyzeMenu = null;
  private JMenu mToolsMenu = null;
  private JMenu mExamplesMenu = null;
  private JMenu mModulesMenu = null;
  private JMenu mConfigureMenu = null;
  private JMenu mHelpMenu = null;

  // #########################################################################
  // # Class Constants
  private static final long serialVersionUID = 1L;

  private static final int MAX_MODULES = 24;

}
