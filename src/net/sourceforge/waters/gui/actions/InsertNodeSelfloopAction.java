//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   InsertNodeSelfloopAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;

import net.sourceforge.waters.gui.GraphTools;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

import org.supremica.gui.ide.IDE;


/**
 * <P>An action to add a selfloop to a node.</P>
 *
 * @author Robi Malik
 */

public class InsertNodeSelfloopAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  InsertNodeSelfloopAction(final IDE ide, final Proxy arg)
  {
    super(ide);
    mActionArgument = arg;
    putValue(Action.NAME, "Add Selfloop");
    putValue(Action.SHORT_DESCRIPTION, "Add a selfloop to this node");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
    setEnabled(true);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final SimpleNodeSubject node = (SimpleNodeSubject) mActionArgument;
    final GraphSubject graph = (GraphSubject) node.getParent().getParent();
    final EdgeSubject edge =
      GraphTools.getCreatedEdge(graph, node, node, null, null);
    final SelectionOwner panel = getCurrentSelectionOwner();
    final Command cmd = new InsertCommand(edge, panel);
    final UndoInterface undoer = getActiveUndoInterface();
    undoer.executeCommand(cmd);
  }


  //#########################################################################
  //# Data Members
  private final Proxy mActionArgument;

}
