//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.Action;
import javax.swing.JComponent;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.EnumOption;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.NodeSubject;

import org.supremica.gui.ide.IDE;


public class WatersPopupActionManager
{

  //#########################################################################
  //# Constructor
  public WatersPopupActionManager(final IDE ide)
  {
    mIDE = ide;
  }

  //#########################################################################
  //# Simple Access
  public IDE getIDE()
  {
    return mIDE;
  }

  //#########################################################################
  //# Installing and Using Actions
  public void installCutCopyPasteActions(final JComponent comp)
  {
    final WatersActionManager master = mIDE.getActions();
    master.installCutCopyPasteActions(comp);
  }

  public void invokeMouseClickAction(final IDEAction action)
  {
    invokeMouseClickAction(action, null);
  }

  public void invokeMouseClickAction(final IDEAction action,
                                     final MouseEvent event)
  {
    if (action.isEnabled()) {
      final String key = (String) action.getValue(Action.ACTION_COMMAND_KEY);
      final int mods = event == null ? 0 : event.getModifiers();
      final ActionEvent newevent =
        new ActionEvent(this, ActionEvent.ACTION_PERFORMED, key, mods);
      action.actionPerformed(newevent);
    }
  }

  //#########################################################################
  //# Access to Actions
  /**
   * Creates an action to toggle a {@link BooleanOption} from Supremica's
   * configuration.
   *
   * @param option
   *          The property affected by the action.
   * @param shortName
   *          A short name to describe the action. The short name is displayed
   *          when the action appears in the menu, and the (longer) comment of
   *          the property is used as a tool tip.
   */
  public IDEAction getConfigBooleanPropertyAction(final BooleanOption option,
                                                  final String shortName)
  {
    return new ConfigBooleanPropertyAction(mIDE, option, shortName);
  }

  /**
   * Creates a new enumeration property action.
   *
   * @param option
   *          The property affected by the action.
   * @param value
   *          The value assigned to the property when the action is triggered.
   * @param comment
   *          A comment to explain the action. Menu items are labelled by the
   *          string representation of the value, while the comment is used as
   *          a tool tip.
   */
  public <E extends Enum<E>> IDEAction getConfigEnumPropertyAction(final EnumOption<E> option,
                                                                   final E value,
                                                                   final String comment)
  {
    return new ConfigEnumPropertyAction<E>(mIDE, option, value, comment);
  }

  public IDEAction getCopyAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(IDECopyAction.class);
  }

  public IDEAction getCutAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(IDECutAction.class);
  }

  public IDEAction getDeleteAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(IDEDeleteAction.class);
  }

  public IDEAction getDeleteAction(final Proxy arg)
  {
    if (arg == null || arg instanceof ModuleProxy) {
      return getDeleteAction();
    } else {
      return new IDEDeleteAction(mIDE, arg);
    }
  }

  public IDEAction getDeselectAllAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(IDEDeselectAllAction.class);
  }

  public IDEAction getEdgeFlipAction(final Proxy arg)
  {
    return new EditEdgeFlipAction(mIDE, arg);
  }

  public IDEAction getEditEventLabelAction(final Proxy arg)
  {
    return new EditEventLabelAction(mIDE, arg);
  }

  public IDEAction getGotoModuleAction(final ModuleProxy parent,
                                       final String name)
  {
    try {
      final DocumentManager docman = mIDE.getDocumentManager();
      final URI uri = docman.resolve(parent, name, ModuleProxy.class);
      return new GotoModuleAction(mIDE, name, uri);
    } catch (final WatersUnmarshalException exception) {
      return null;
    }
  }

  public IDEAction getGraphLayoutAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(GraphLayoutAction.class);
  }

  public IDEAction getInsertBlockedEventListAction(final Point point)
  {
    return new InsertBlockedEventListAction(mIDE, point);
  }

  public IDEAction getInsertEventLabelAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(InsertEventLabelAction.class);
  }

  public IDEAction getPasteAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(IDEPasteAction.class);
  }

  public IDEAction getPropertiesAction(final Proxy arg)
  {
    return new IDEPropertiesAction(mIDE, arg);
  }

  public IDEAction getLabelRecallAction(final Proxy arg)
  {
    return new EditLabelRecallAction(mIDE, arg);
  }

  public IDEAction getInsertConditionalAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(InsertConditionalAction.class);
  }

  public IDEAction getInsertConstantAliasAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(InsertConstantAliasAction.class);
  }

  public IDEAction getInsertEventAliasAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(InsertEventAliasAction.class);
  }

  public IDEAction getInsertEventDeclAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(InsertEventDeclAction.class);
  }

  public IDEAction getInsertForeachAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(InsertForeachAction.class);
  }

  public IDEAction getInsertInstanceAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(InsertInstanceAction.class);
  }

  public IDEAction getInsertParameterBindingAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(InsertParameterBindingAction.class);
  }

  public IDEAction getInsertSimpleComponentAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(InsertSimpleComponentAction.class);
  }

  public IDEAction getInsertVariableAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(InsertVariableAction.class);
  }

  public IDEAction getLogClearAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(LogClearAction.class);
  }

  public IDEAction getNodeInitialAction(final Proxy arg)
  {
    return new EditNodeInitialAction(mIDE, arg);
  }

  public IDEAction getNodeMarkingAction(final NodeSubject node,
                                        final IdentifierSubject ident)
  {
    return new EditNodeMarkingAction(mIDE, node, ident);
  }

  public IDEAction getNodeSelfloopAction(final Proxy arg)
  {
    return new InsertNodeSelfloopAction(mIDE, arg);
  }

  public IDEAction getSelectAllAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(IDESelectAllAction.class);
  }

  public IDEAction getShowGraphAction(final Proxy arg)
  {
    return new ShowGraphAction(mIDE, arg);
  }

  public IDEAction getShowModuleCommentAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(ShowModuleCommentAction.class);
  }

  public IDEAction getDesktopCloseWindowAction(final AutomatonProxy auto)
  {
    return new DesktopCloseWindowAction(mIDE, auto);
  }

  public IDEAction getDesktopOpenWindowAction(final AutomatonProxy auto)
  {
    return new DesktopOpenWindowAction(mIDE, auto);
  }

  public IDEAction getDesktopExecuteAction(final AutomatonProxy automaton,
                                           final Proxy highlighted)
  {
    return new DesktopExecuteAction(mIDE, automaton, highlighted);
  }

  public IDEAction getDesktopEditAction(final AutomatonProxy aut)
  {
    return new DesktopEditAction(mIDE, aut);
  }

  public IDEAction getDesktopCloseAllAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(SimulationCloseAllAction.class);
  }

  public IDEAction getDesktopCloseOtherAction(final AutomatonProxy automaton)
  {
    return new DesktopCloseOtherAction(mIDE, automaton);
  }

  public IDEAction getDesktopShowAllAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(SimulationShowAllAction.class);
  }

  public IDEAction getDesktopCascadeAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(SimulationCascadeAction.class);
  }

  public IDEAction getResizeAction(final AutomatonProxy automaton)
  {
    return new DesktopOriginalSizeAction(mIDE, automaton);
  }

  public IDEAction getResizeOtherAction(final AutomatonProxy automaton)
  {
    return new DesktopOriginalSizeOtherAction(mIDE, automaton);
  }

  public IDEAction getEventExecuteAction(final EventProxy event)
  {
    return new EventExecuteAction(mIDE, event);
  }

  public IDEAction getTraceTravelAction(final int time)
  {
    return new TraceTravelAction(mIDE, time);
  }

  public IDEAction getSimulationDisableAutomatonAction(final AutomatonProxy aut)
  {
    return new SimulationDisableAutomatonAction(mIDE, aut);
  }

  public IDEAction getLanguageIncusionAction(final NamedProxy aut)
  {
    return new VerifyLanguageInclusionAction(mIDE, aut);
  }

  public IDEAction getDesktopSetStateAction(final AutomatonProxy automaton,
                                            final NodeProxy node)
  {
    return new DesktopSwitchStateAction(mIDE, automaton, node);
  }

  public IDEAction getAnalyzerSynchronousProductAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(AnalyzerSynchronousProductAction.class);
  }

  public IDEAction getAnalyzerSynthesizerAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(AnalyzerSynthesisAction.class);
  }

  public IDEAction getAnalyzerConflictCheckAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(AnalyzerConflictCheckAction.class);
  }

  public IDEAction getAnalyzerControlLoopCheckAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(AnalyzerLoopCheckAction.class);
  }

  public IDEAction getAnalyzerControllabilityCheckAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(AnalyzerControllabilityCheckAction.class);
  }

  public IDEAction getAnalyzerDeadlockCheckAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(AnalyzerDeadlockCheckAction.class);
  }

  public IDEAction getAnalyzerDiagnosabilityCheckAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(AnalyzerDiagnosabilityCheckAction.class);
  }

  public IDEAction getAnalyzerHideAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(AnalyzerHideAction.class);
  }

  public IDEAction getAnalyzerHideAction(final AutomatonProxy aut)
  {
    return new AnalyzerHideAction(mIDE, aut);
  }

  public IDEAction getAnalyzerLanguageInclusionCheckAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(AnalyzerLanguageInclusionCheckAction.class);
  }

  public IDEAction getAnalyzerStateCountAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(AnalyzerCountStatesAction.class);
  }

  public IDEAction getAnalyzerSimplificationAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(AnalyzerSimplificationAction.class);
  }

  public IDEAction getAnalyzerSimplificationAction(final AutomatonProxy aut)
  {
    return new AnalyzerSimplificationAction(mIDE, aut);
  }

  public IDEAction getAnalyzerWorkbenchAction()
  {
    final WatersActionManager master = mIDE.getActions();
    return master.getAction(AnalyzerWorkbenchAction.class);
  }

  //#########################################################################
  //# Data Members
  private final IDE mIDE;

}
