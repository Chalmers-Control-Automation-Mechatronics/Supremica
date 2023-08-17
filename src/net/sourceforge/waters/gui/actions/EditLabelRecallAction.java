//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
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
    extends DefaultModuleProxyVisitor
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
    extends DefaultModuleProxyVisitor
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
    @Override
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

    @Override
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


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
