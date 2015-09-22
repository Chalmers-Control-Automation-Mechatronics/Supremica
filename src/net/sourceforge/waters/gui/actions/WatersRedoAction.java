//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.util.IconLoader;

import org.supremica.gui.ide.IDE;


public class WatersRedoAction
  extends WatersAction
{

  //#########################################################################
  //# Constructor
  WatersRedoAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Redo");
    putValue(Action.SHORT_DESCRIPTION, "Redo the last command");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
    putValue(Action.SMALL_ICON, IconLoader.ICON_TOOL_REDO);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final UndoInterface umanager = getActiveUndoInterface();
    if (umanager != null && umanager.canRedo()) {
      umanager.redo();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
    case MAINPANEL_SWITCH:
    case UNDOREDO:
      updateEnabledStatus();
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void updateEnabledStatus()
  {
    final UndoInterface umanager = getActiveUndoInterface();
    final boolean enabled = umanager != null && umanager.canRedo();
    setEnabled(enabled);
    final String text =
      enabled ? umanager.getRedoPresentationName() : "Can't redo";
    putValue(Action.NAME, text);
    putValue(Action.SHORT_DESCRIPTION, text);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}








