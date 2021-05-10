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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;

import net.sourceforge.waters.gui.GraphTools;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CompoundCommand;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.command.UpdateCommand;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

import org.supremica.gui.ide.IDE;


/**
 * <P>A popup menu action change the initial-state status of multiple
 * selected nodes in a graph.</P>
 *
 * <P>This action is invoked with a selected action argument node in a graph.
 * The effects of the action depend on the state of the node and the selection
 * in the graph at the time when the action is created, i.e., when the popup
 * menu appears, and on whether the automaton being edited is deterministic.</P>
 * <UL>
 * <LI>In a deterministic automaton, the action sets the argument node to
 * initial and removes the initial state status from all other nodes in the
 * graph. If the action argument already is the only initial node, the action
 * is disabled.</LI>
 * <LI>If the action argument node is not initial in a nondeterministic
 * automaton, the initial status is added to the action argument node and all
 * other nodes selected in the graph.</LI>
 * <LI>If the action argument node is initial in a nondeterministic automaton,
 * the initial status is removed from the action argument node and all other
 * nodes selected in the graph.</LI>
 * </UL>
 *
 * @author Robi Malik
 */

public class EditNodeInitialAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  EditNodeInitialAction(final IDE ide, final Proxy arg)
  {
    super(ide);
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
    putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_INITIAL);
    final SimpleNodeSubject node = (SimpleNodeSubject) arg;
    mInitial = node.isInitial();
    final GraphSubject graph = (GraphSubject) node.getParent().getParent();
    mDeterministic = graph.isDeterministic();
    mPanel = getCurrentSelectionOwner();
    if (mDeterministic) {
      putValue(Action.NAME, "Set Initial");
      putValue(Action.SHORT_DESCRIPTION,
               "Set this state to be the initial state");
      if (!mInitial || GraphTools.hasMultipleInitialStates(graph)) {
        setEnabled(true);
        mNodes = Collections.singletonList(node);
      } else {
        mNodes = null;
      }
    } else {
      mNodes = new ArrayList<>();
      mNodes.add(node);
      for (final Proxy proxy : mPanel.getCurrentSelection()) {
        if (proxy instanceof SimpleNodeSubject && proxy != node) {
          final SimpleNodeSubject otherNode = (SimpleNodeSubject) proxy;
          mNodes.add(otherNode);
        }
      }
      final String description =
        mNodes.size() == 1 ? "this state" : "the selected states";
      if (mInitial) {
        putValue(Action.NAME, "Clear Initial");
        putValue(Action.SHORT_DESCRIPTION,
                 "Remove the initial state status from " + description);
      } else {
        putValue(Action.NAME, "Set Initial");
        putValue(Action.SHORT_DESCRIPTION,
                 "Set " + description + " to be initial");
      }
      setEnabled(true);
    }
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final List<SimpleNodeSubject> changed = new LinkedList<>();
    final List<Command> commands = new LinkedList<Command>();
    final String name;
    if (mDeterministic) {
      final SimpleNodeSubject node = mNodes.get(0);
      createCommand(node, true, changed, commands);
      final GraphSubject graph = (GraphSubject) node.getParent().getParent();
      for (final NodeSubject other : graph.getNodesModifiable()) {
        if (other != node && other instanceof SimpleNodeSubject) {
          final SimpleNodeSubject simple = (SimpleNodeSubject) other;
          createCommand(simple, false, changed, commands);
        }
      }
      name = "Initial State Change";
    } else {
      final boolean newState = !mInitial;
      for (final SimpleNodeSubject node : mNodes) {
        createCommand(node, newState, changed, commands);
      }
      final String action = mInitial ? "Clear" : "Set";
      name = action + " Initial State";
    }
    if (!commands.isEmpty()) {
      final CompoundCommand compound =
        new UpdateCommand(changed, mPanel, name, true);
      compound.addCommands(commands);
      final UndoInterface undoer = mPanel.getUndoInterface(this);
      undoer.executeCommand(compound);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void createCommand(final SimpleNodeSubject node,
                             final boolean newState,
                             final List<SimpleNodeSubject> changed,
                             final List<Command> commands)
  {
    final boolean initial = node.isInitial();
    if (initial != newState) {
      changed.add(node);
      final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
      final SimpleNodeSubject cloned = (SimpleNodeSubject) cloner.getClone(node);
      cloned.setInitial(newState);
      cloned.setInitialArrowGeometry(null);
      final SelectionOwner panel = getCurrentSelectionOwner();
      final Command cmd =
        new EditCommand(node, cloned, panel, "Initial State Change");
      cmd.setUpdatesSelection(false);
      commands.add(cmd);
    }
  }


  //#########################################################################
  //# Data Members
  /**
   * Whether or not the action argument node has the initial state status
   * at the time the action is invoked.
   */
  private final boolean mInitial;
  /**
   * Whether or not the edited graph is deterministic at the time the
   * action is invoked.
   */
  private final boolean mDeterministic;
  /**
   * The selected nodes that should be modified by the action.
   * The action argument appears first in the list.
   */
  private final List<SimpleNodeSubject> mNodes;
  /**
   * The graph editor panel containing the nodes to be modified.
   */
  private final SelectionOwner mPanel;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -1299013497320466998L;

}
