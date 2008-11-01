//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   EditNodeMarkingAction
//###########################################################################
//# $Id$
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
      node.getPropositions().getEventListModifiable();
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
  public void actionPerformed(final ActionEvent event)
  {
    final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
    final NodeSubject cloned = (NodeSubject) cloner.getClone(mNode);
    final List<AbstractSubject> props =
      cloned.getPropositions().getEventListModifiable();
    final Iterator<AbstractSubject> iter = props.iterator();
    boolean removed = false;
    while (iter.hasNext()) {
      final AbstractSubject prop = iter.next();
      if (mIdentifier.equalsByContents(prop)) {
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
    return new InsertCommand(newdecl, panel, false);
  }


  //#########################################################################
  //# Data Members
  private final NodeSubject mNode;
  private final IdentifierSubject mIdentifier;

}
