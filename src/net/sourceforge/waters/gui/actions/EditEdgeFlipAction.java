//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   EditEdgeFlipAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.supremica.gui.ide.IDE;


/**
 * <P>An action to swap the source an target of an edge.</P>
 *
 * <P><STRONG>BUG.</STRONG> This should sometimes be disabled in a
 * deterministic graph ...</P>
 *
 * @author Robi Malik
 */

public class EditEdgeFlipAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  EditEdgeFlipAction(final IDE ide, final Proxy arg)
  {
    super(ide);
    mActionArgument = arg;
    putValue(Action.NAME, "Flip Edge");
    putValue(Action.SHORT_DESCRIPTION, "Swap source and target of this edge");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
    setEnabled(true);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
    final EdgeSubject edge = (EdgeSubject) mActionArgument;
    final EdgeSubject cloned = (EdgeSubject) cloner.getClone(edge);
    final NodeSubject source = cloned.getSource();
    final NodeSubject target = cloned.getTarget();
    final PointGeometrySubject start = cloned.getStartPoint();
    final PointGeometrySubject end = cloned.getEndPoint();
    cloned.setStartPoint(null);
    cloned.setEndPoint(null);
    cloned.setSource(target);
    cloned.setStartPoint(end);
    cloned.setTarget(source);
    cloned.setEndPoint(start);
    final SelectionOwner panel = getCurrentSelectionOwner();
    final Command cmd = new EditCommand(edge, cloned, panel, "Edge Flip");
    final UndoInterface undoer = getActiveUndoInterface();
    undoer.executeCommand(cmd);
  }


  //#########################################################################
  //# Data Members
  private final Proxy mActionArgument;

}
