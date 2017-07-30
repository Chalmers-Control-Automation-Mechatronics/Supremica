//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.gui.ide.IDE;


/**
 * <P>An action to add or remove a proposition to or from a node.</P>
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
    mNode = node;
    mIdentifier = ident;
    final String name = ident.toString();
    putValue(Action.NAME, name);
    final List<AbstractSubject> props =
      node.getPropositions().getEventIdentifierListModifiable();
    if (props.contains(ident)) {
      putValue(Action.SHORT_DESCRIPTION,
               "Remove marking " + name + " from this node");
    } else {
      putValue(Action.SHORT_DESCRIPTION, "Mark this node as " + name);
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
    final NodeSubject cloned = (NodeSubject) cloner.getClone(mNode);
    final List<AbstractSubject> props =
      cloned.getPropositions().getEventIdentifierListModifiable();
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    final Iterator<AbstractSubject> iter = props.iterator();
    boolean removed = false;
    while (iter.hasNext()) {
      final AbstractSubject prop = iter.next();
      if (eq.equals(mIdentifier, prop)) {
        iter.remove();
        removed = true;
        break;
      }
    }
    if (!removed) {
      props.add(mIdentifier.clone());
    }
    final String name = removed ? "Node Unmarking" : "Node Marking";
    final SelectionOwner panel = getCurrentSelectionOwner();
    final Command editcmd = new EditCommand(mNode, cloned, panel, name);
    final Command cmd;
    if (removed) {
      cmd = editcmd;
    } else {
      final Command declcmd = getCreateDefaultPropositionCommand();
      if (declcmd == null) {
        cmd = editcmd;
      } else {
        final CompoundCommand compound =
          new CompoundCommand(editcmd.getName());
        compound.addCommand(declcmd);
        compound.addCommand(editcmd);
        compound.end();
        cmd = compound;
      }
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
    final EventDeclSubject newdecl =
      new EventDeclSubject(mIdentifier, EventKind.PROPOSITION);
    final SelectionOwner panel = root.getEventsPanel();
    return new InsertCommand(newdecl, panel, root, false);
  }


  //#########################################################################
  //# Data Members
  private final NodeSubject mNode;
  private final IdentifierSubject mIdentifier;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
