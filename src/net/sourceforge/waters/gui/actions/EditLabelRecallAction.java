//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   EditLabelRecallAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.geom.Point2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.renderer.LabelBlockProxyShape;
import net.sourceforge.waters.gui.renderer.SimpleNodeProxyShape;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

import org.supremica.gui.ide.IDE;


/**
 * <P>An action to return the label of a node or edge to its default
 * position.</P>
 *
 * @author Robi Malik
 */

public class EditLabelRecallAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  EditLabelRecallAction(final IDE ide, final Proxy arg)
  {
    super(ide);
    mActionArgument = arg;
    mRecallEnabledVisitor = new RecallEnabledVisitor();
    mRecallVisitor = new RecallVisitor();
    final String named = ProxyNamer.getItemClassName(arg);
    putValue(Action.NAME, "Recall Label");
    putValue(Action.SHORT_DESCRIPTION,
             "Return the label of this " + named + " to its default position");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final Command cmd = mRecallVisitor.getRecallCommand(mActionArgument);
    final UndoInterface undoer = getActiveUndoInterface();
    undoer.executeCommand(cmd);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void updateEnabledStatus()
  {
    final boolean enabled = mRecallEnabledVisitor.canRecall(mActionArgument);
    setEnabled(enabled);
  }


  //#########################################################################
  //# Inner Class RecallEnabledVisitor
  private class RecallEnabledVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private boolean canRecall(final Proxy proxy)
    {
      try {
	return (Boolean) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
	throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    public Boolean visitProxy(final Proxy proxy)
    {
      return false;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    public Boolean visitEdgeProxy(final EdgeProxy edge)
    {
      final LabelBlockProxy block = edge.getLabelBlock();
      final LabelGeometryProxy geo = block.getGeometry();
      if (geo == null) {
        return false;
      } else {
        final Point2D offset = geo.getOffset();
        return !offset.equals(LabelBlockProxyShape.DEFAULT_OFFSET);
      }
    }

    public Boolean visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      final LabelGeometryProxy geo = node.getLabelGeometry();
      if (geo == null) {
        return false;
      } else {
        final Point2D offset = geo.getOffset();
        return !offset.equals(SimpleNodeProxyShape.DEFAULT_OFFSET);
      }
    }

  }


  //#########################################################################
  //# Inner Class RecallVisitor
  private class RecallVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private Command getRecallCommand(final Proxy proxy)
    {
      try {
	return (Command) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
	throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    public Command visitEdgeProxy(final EdgeProxy proxy)
    {
      final ModuleProxyCloner cloner =
        ModuleSubjectFactory.getCloningInstance();
      final EdgeSubject edge = (EdgeSubject) proxy;
      final LabelBlockSubject block = edge.getLabelBlock();
      final LabelGeometrySubject geo = block.getGeometry();
      final LabelGeometrySubject cloned =
        (LabelGeometrySubject) cloner.getClone(geo);
      cloned.setOffset(LabelBlockProxyShape.DEFAULT_OFFSET);
      final SelectionOwner panel = getCurrentSelectionOwner();
      return new EditCommand(geo, cloned, panel, "Label Movement");
    }

    public Command visitSimpleNodeProxy(final SimpleNodeProxy proxy)
    {
      final ModuleProxyCloner cloner =
        ModuleSubjectFactory.getCloningInstance();
      final SimpleNodeSubject node = (SimpleNodeSubject) proxy;
      final LabelGeometrySubject geo = node.getLabelGeometry();
      final LabelGeometrySubject cloned =
        (LabelGeometrySubject) cloner.getClone(geo);
      cloned.setOffset(SimpleNodeProxyShape.DEFAULT_OFFSET);
      final SelectionOwner panel = getCurrentSelectionOwner();
      return new EditCommand(geo, cloned, panel, "Label Movement");
    }


  }


  //#########################################################################
  //# Data Members
  private final Proxy mActionArgument;
  private final RecallEnabledVisitor mRecallEnabledVisitor;
  private final RecallVisitor mRecallVisitor;

}
