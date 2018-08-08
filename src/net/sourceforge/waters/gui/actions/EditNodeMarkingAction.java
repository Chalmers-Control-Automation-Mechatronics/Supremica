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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CompoundCommand;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.gui.ide.IDE;


/**
 * <P>A popup menu action to add or remove a proposition to or from multiple
 * selected nodes in a graph.</P>
 *
 * <P>This action is invoked with a selected node in a graph and the name
 * of a proposition chosen from a submenu. The effects of the action depend
 * on the state of the node and the selection in the graph at the time when
 * the action is created, i.e., when the popup menu appears.</P>
 *
 * <P>If the selected node is marked with the proposition, then the action
 * removes the proposition from the node and all other nodes selected in
 * the graph. Otherwise, if the selected node is not marked with the
 * proposition, then the action marks the node and all other selected nodes
 * with the proposition.</P>
 *
 * @author Robi Malik
 */

public class EditNodeMarkingAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  EditNodeMarkingAction(final IDE ide,
                        final NodeSubject node,
                        final IdentifierSubject ident)
  {
    super(ide);
    mIdentifier = ident;
    final String name = ident.toString();
    putValue(Action.NAME, name);
    final List<AbstractSubject> props =
      node.getPropositions().getEventIdentifierListModifiable();
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mAdding = !eq.contains(props, ident);
    mNodes = new ArrayList<>();
    mNodes.add(node);
    mPanel = getCurrentSelectionOwner();
    for (final Proxy proxy : mPanel.getCurrentSelection()) {
      if (proxy instanceof SimpleNodeSubject && proxy != node) {
        final NodeSubject otherNode = (NodeSubject) proxy;
        mNodes.add(otherNode);
      }
    }
    final String description =
      mNodes.size() == 1 ? "this node" : "selected nodes";
    if (mAdding) {
      putValue(Action.SHORT_DESCRIPTION,
               "Mark " + description + " as " + name);
    } else {
      putValue(Action.SHORT_DESCRIPTION,
               "Remove marking " + name + " from " + description);
    }
    final ModuleContext context = getModuleContext();
    final Icon icon = context.guessPropositionIcon(ident);
    putValue(Action.SMALL_ICON, icon);
    setEnabled(true);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    final String name = mAdding ? "Node Marking" : "Node Unmarking";
    final List<Command> commands = new ArrayList<>(mNodes.size() + 1);
    for (final NodeSubject node : mNodes) {
      final List<AbstractSubject> props =
        node.getPropositions().getEventIdentifierListModifiable();
      final NodeSubject cloned;
      if (mAdding) {
        if (!eq.contains(props, mIdentifier)) {
          cloned = (NodeSubject) cloner.getClone(node);
          final List<AbstractSubject> clonedProps =
            cloned.getPropositions().getEventIdentifierListModifiable();
          final IdentifierSubject clonedIdent =
            (IdentifierSubject) cloner.getClone(mIdentifier);
          clonedProps.add(clonedIdent);
        } else {
          continue;
        }
      } else { // removing
        if (eq.contains(props, mIdentifier)) {
          cloned = (NodeSubject) cloner.getClone(node);
          final List<AbstractSubject> clonedProps =
            cloned.getPropositions().getEventIdentifierListModifiable();
          final Iterator<AbstractSubject> iter = clonedProps.iterator();
          while (iter.hasNext()) {
            final AbstractSubject prop = iter.next();
            if (eq.equals(prop, mIdentifier)) {
              iter.remove();
              break;
            }
          }
        } else {
          continue;
        }
      }
      final Command cmd = new EditCommand(node, cloned, mPanel, name);
      commands.add(cmd);
    }
    if (commands.isEmpty()) {
      return;
    }
    if (mAdding) {
      final Command cmd = getCreateDefaultPropositionCommand();
      if (cmd != null) {
        commands.add(cmd);
      }
    }
    final Command cmd;
    if (commands.size() == 1) {
      cmd = commands.get(0);
    } else {
      final CompoundCommand compound = new CompoundCommand(name);
      compound.addCommands(commands);
      cmd = compound;
    }
    final UndoInterface undoer = getActiveUndoInterface();
    undoer.executeCommand(cmd);
  }


  //#########################################################################
  //# Auxiliary Methods
  private Command getCreateDefaultPropositionCommand()
  {
    if (!(mIdentifier instanceof SimpleIdentifierSubject)) {
      return null;
    }
    final SimpleIdentifierSubject simple =
      (SimpleIdentifierSubject) mIdentifier;
    final String name = simple.getName();
    if (!EventDeclProxy.DEFAULT_MARKING_NAME.equals(name) &&
        !EventDeclProxy.DEFAULT_FORBIDDEN_NAME.equals(name)) {
      return null;
    }
    final ModuleWindowInterface root = getActiveModuleWindowInterface();
    final ModuleContext context = root.getModuleContext();
    final EventDeclProxy decl = context.getEventDecl(name);
    if (decl != null) {
      return null;
    }
    final EventDeclSubject newDecl =
      new EventDeclSubject(mIdentifier, EventKind.PROPOSITION);
    final SelectionOwner panel = root.getEventsPanel();
    return new InsertCommand(newDecl, panel, root, false);
  }


  //#########################################################################
  //# Data Members
  /**
   * The identifier (name) of the proposition to be added or removed.
   */
  private final IdentifierSubject mIdentifier;
  /**
   * Whether the proposition is added (<CODE>true</CODE>) or removed
   * (<CODE>false</CODE>) from nodes.
   */
  private final boolean mAdding;
  /**
   * The selected nodes that should be modified by the action.
   */
  private final Collection<NodeSubject> mNodes;
  /**
   * The graph editor panel containing the nodes to be modified.
   */
  private final SelectionOwner mPanel;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 7156899409631245265L;

}
