//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.actions;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;

import org.supremica.gui.ide.IDE;


public abstract class WatersActionManager implements Observer
{

  // #########################################################################
  // # Constructor
  public WatersActionManager(final IDE ide)
  {
    final int SIZE = 64;
    mActionMap = new HashMap<Class<? extends IDEAction>,IDEAction>(SIZE);
    mKeyboardActionMap = new HashMap<Class<? extends IDEAction>,Action>(SIZE);
    addAction(new AnalyzeConflictCheckAction(ide));
    addAction(new AnalyzeControllabilityCheckAction(ide));
    addAction(new AnalyzeControlLoopAction(ide));
    addAction(new AnalyzeDeadlockCheckAction(ide));
    addAction(new AnalyzeDiagnosabilityCheckAction(ide));
    addAction(new AnalyzeHISCCPControllabilityAction(ide));
    try {
      addAction(new AnalyzeHISCCPInterfaceConsistencyAction(ide));
    } catch (final NoClassDefFoundError error) {
      // skip this one
    }
    addAction(new AnalyzeLanguageInclusionAction(ide));
    addAction(new AnalyzeSICProperty5Action(ide));
    addAction(new AnalyzeSICProperty6Action(ide));
    addAction(new AnalyzeSDPlantCompletenessAction(ide));
    addAction(new AnalyzeSDActivityLoopAction(ide));
    addAction(new AnalyzeSDSingularPropertyAction(ide));
    addAction(new AnalyzeSDControllabilityAction(ide));
    addAction(new AnalyzeSDCTwoApropertyAction(ide));
    addAction(new AnalyzeSDCTwoBPropertyAction(ide));
    addAction(new AnalyzeSDCThree_one_propertyAction(ide));
    addAction(new AnalyzeNerodeEquivalentAction(ide));
    addAction(new AnalyzeSDCFourPropertyAction(ide));
    addAction(new AnalyzeProperTimeBehaviorPropertyAction(ide));
    addAction(new AnalyzerSynchronousProductAction(ide));
    addAction(new EditEventLabelAction(ide));
    addAction(new GraphLayoutAction(ide));
    addAction(new GraphSaveEPSAction(ide));
    addAction(new GraphSavePDFAction(ide));
    addAction(new IDEAboutAction(ide));
    addAction(new IDECopyAction(ide));
    addAction(new IDECutAction(ide));
    addAction(new IDEDeleteAction(ide));
    addAction(new IDEDeselectAllAction(ide));
    addAction(new IDEPasteAction(ide));
    addAction(new IDEPropertiesAction(ide));
    addAction(new IDESelectAllAction(ide));
    addAction(new InsertConstantAliasAction(ide));
    addAction(new InsertEventAliasAction(ide));
    addAction(new InsertEventDeclAction(ide));
    addAction(new InsertEventLabelAction(ide));
    addAction(new InsertForeachAction(ide));
    addAction(new InsertInstanceAction(ide));
    addAction(new InsertParameterBindingAction(ide));
    addAction(new InsertSimpleComponentAction(ide));
    addAction(new InsertVariableAction(ide));
    addAction(new InstantiateModuleAction(ide));
    addAction(new LogClearAction(ide));
    addAction(new LogTestAction(ide));
    addAction(new RecompileAction(ide));
    addAction(new ShowGraphAction(ide));
    addAction(new ShowModuleCommentAction(ide));
    addAction(new SimulationBackToStartAction(ide));
    addAction(new SimulationCascadeAction(ide));
    addAction(new SimulationCloseAllAction(ide));
    addAction(new SimulationJumpToEndAction(ide));
    addAction(new SimulationReplayStepAction(ide));
    addAction(new SimulationResetAction(ide));
    addAction(new SimulationShowAllAction(ide));
    addAction(new SimulationStepAction(ide));
    addAction(new SimulationAutoStepAction(ide));
    addAction(new SimulationStepBackAction(ide));
    addAction(new ToolEdgeAction(ide));
    addAction(new ToolGroupNodeAction(ide));
    addAction(new ToolNodeAction(ide));
    addAction(new ToolSelectAction(ide));
    addAction(new WatersRedoAction(ide));
    addAction(new WatersUndoAction(ide));
    ide.attach(this);
  }

  // #########################################################################
  // # Access to Actions
  public IDEAction getAction(final Class<? extends IDEAction> clazz)
  {
    return mActionMap.get(clazz);
  }

  // #########################################################################
  // # Installing Keyboard Shortcuts
  public void installCutCopyPasteActions(final JComponent comp)
  {
    installAction(comp, IDECutAction.class);
    installAction(comp, IDECopyAction.class);
    installAction(comp, IDEPasteAction.class);
    installAction(comp, IDEDeselectAllAction.class);
  }

  public void installAction(final JComponent comp,
                            final Class<? extends IDEAction> clazz)
  {
    final Action action = getKeyboardAction(clazz);
    final String name = (String) action.getValue(Action.NAME);
    final KeyStroke key = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
    final InputMap imap = comp.getInputMap();
    final ActionMap amap = comp.getActionMap();
    imap.put(key, name);
    amap.put(name, action);
  }

  // #########################################################################
  // # Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    for (final IDEAction action : mActionMap.values()) {
      action.update(event);
    }
  }

  // #########################################################################
  // # Initialization
  protected void addAction(final IDEAction action)
  {
    final Class<? extends IDEAction> clazz = action.getClass();
    mActionMap.put(clazz, action);
  }

  // #######################################################################
  // # Keyboard Actions
  private Action getKeyboardAction(final Class<? extends IDEAction> clazz)
  {
    Action action = mKeyboardActionMap.get(clazz);
    if (action == null) {
      action = getAction(clazz);
      action = new KeyboardAction(action);
      mKeyboardActionMap.put(clazz, action);
    }
    return action;
  }

  // #######################################################################
  // # Data Members
  private final Map<Class<? extends IDEAction>,IDEAction> mActionMap;
  private final Map<Class<? extends IDEAction>,Action> mKeyboardActionMap;

}
