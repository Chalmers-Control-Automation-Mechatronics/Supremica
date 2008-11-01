//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   EditNodeInitialAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;

import net.sourceforge.waters.gui.GraphTools;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CompoundCommand;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.command.UpdateCommand;
import net.sourceforge.waters.gui.renderer.SimpleNodeProxyShape;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

import org.supremica.gui.ide.IDE;


/**
 * <P>An action to change the initial-state status of a node.</P>
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
    mActionArgument = arg;
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final SimpleNodeSubject node = (SimpleNodeSubject) mActionArgument;
    final GraphSubject graph = (GraphSubject) node.getParent().getParent();
    final EditCommand toggle = createToggleCommand(node);
    Command cmd = toggle;
    if (graph.isDeterministic()) {
      if (node.isInitial()) {
        if (!GraphTools.hasMultipleInitialStates(graph)) {
          return;
        }
      } else {
        final List<Command> cmds = new LinkedList<Command>();
        final List<SimpleNodeSubject> changed =
          new LinkedList<SimpleNodeSubject>();
        for (final NodeSubject other : graph.getNodesModifiable()) {
          if (other instanceof SimpleNodeSubject) {
            final SimpleNodeSubject simple = (SimpleNodeSubject) other;
            if (simple.isInitial()) {
              final EditCommand edit = createToggleCommand(simple);
              edit.setUpdatesSelection(false);
              cmds.add(edit);
              changed.add(simple);
            }
          }
        }
        if (!cmds.isEmpty()) {
          changed.add(node);
          final SelectionOwner panel = getCurrentSelectionOwner();
          final String name = cmd.getName();
          final CompoundCommand compound =
            new UpdateCommand(changed, panel, name, true);
          compound.addCommands(cmds);
          toggle.setUpdatesSelection(false);
          compound.addCommand(toggle);
          cmd = compound;
        }
      }
    }
    final UndoInterface undoer = getActiveUndoInterface();
    undoer.executeCommand(cmd);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void updateEnabledStatus()
  {
    final SimpleNodeSubject node = (SimpleNodeSubject) mActionArgument;
    final GraphSubject graph = (GraphSubject) node.getParent().getParent();
    if (graph.isDeterministic()) {
      if (node.isInitial() && GraphTools.hasMultipleInitialStates(graph)) {
        putValue(Action.NAME, "Clear Initial");
        putValue(Action.SHORT_DESCRIPTION,
                 "Remove the initial-state status of this node");
        setEnabled(true);
      } else {
        putValue(Action.NAME, "Set Initial");
        putValue(Action.SHORT_DESCRIPTION,
                 "Set this node to be the initial state");
        setEnabled(!node.isInitial());
      }
    } else {
      if (node.isInitial()) {
        putValue(Action.NAME, "Clear Initial");
        putValue(Action.SHORT_DESCRIPTION,
                 "Remove the initial-state status of this node");
      } else {
        putValue(Action.NAME, "Set Initial");
        putValue(Action.SHORT_DESCRIPTION,
                 "Set this node to be an initial state");
      }
      setEnabled(true);
    }
  }

  private EditCommand createToggleCommand(final SimpleNodeSubject node)
  {
    final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
    final SimpleNodeSubject cloned = (SimpleNodeSubject) cloner.getClone(node);
    final boolean initial = cloned.isInitial();
    cloned.setInitial(!initial);
    if (initial) {
      cloned.setInitialArrowGeometry(null);
    } else {
      final PointGeometrySubject geo =
        new PointGeometrySubject(SimpleNodeProxyShape.DEFAULT_INITARROW);
      cloned.setInitialArrowGeometry(geo);
    }
    final SelectionOwner panel = getCurrentSelectionOwner();
    return new EditCommand(node, cloned, panel, "Initial State Change");
  }


  //#########################################################################
  //# Data Members
  private final Proxy mActionArgument;

}
