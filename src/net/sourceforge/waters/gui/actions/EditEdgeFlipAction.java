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


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
