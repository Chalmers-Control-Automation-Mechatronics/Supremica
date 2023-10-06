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
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.DeleteCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.base.Proxy;

import org.supremica.gui.ide.IDE;


/**
 * <P>The action associated with the 'delete' key and menu buttons.</P>
 *
 * <P>This action deletes all selected items in the panel that currently
 * owns the focus. To support this action, components including editable
 * items must implement the {@link SelectionOwner#getCurrentSelection()
 * getCurrentSelection()}, {@link SelectionOwner#canDelete(List) canDelete()},
 * {@link SelectionOwner#getDeletionVictims(List) getDeletionVictims()},
 * {@link SelectionOwner#deleteItems(List) deleteItems()} and {@link
 * SelectionOwner#insertItems(List) insertItems()} methods of the
 * {@link SelectionOwner} interface.</P>
 *
 * @author Robi Malik
 */

public class IDEDeleteAction
  extends IDEAction
{

  //#########################################################################
  //# Constructors
  IDEDeleteAction(final IDE ide)
  {
    this(ide, null);
  }

  IDEDeleteAction(final IDE ide, final Proxy arg)
  {
    super(ide);
    if (arg == null) {
      mActionArgument = null;
    } else {
      final SelectionOwner panel = getCurrentSelectionOwner();
      mActionArgument = panel.isSelected(arg) ? null : arg;
    }
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_TOOL_DELETE);
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final SelectionOwner panel = getCurrentSelectionOwner();
    final List<? extends Proxy> selection =
      mActionArgument == null ?
      panel.getCurrentSelection() :
      Collections.singletonList(mActionArgument);
    final List<InsertInfo> deletes = panel.getDeletionVictims(selection);
    // The user may now have cancelled the deletion ...
    if (deletes != null) {
      final Command cmd = new DeleteCommand(deletes, panel);
      final UndoInterface undoer = panel.getUndoInterface(this);
      if (undoer == null) {
        // If there is no undo interface, just delete them ...
        cmd.execute();
      } else {
        // Otherwise register the command ...
        undoer.executeCommand(cmd);
      }
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.SELECTION_CHANGED) {
      updateEnabledStatus();
    }
  }


  //#########################################################################
  //# Auxilary Methods
  private void updateEnabledStatus()
  {
    if (mActionArgument != null) {
      setEnabled(true);
      final String name = ProxyNamer.getItemClassName(mActionArgument);
      final String lname = name.toLowerCase();
      putValue(Action.NAME, "Delete " + name);
      putValue(Action.SHORT_DESCRIPTION, "Delete this " + lname);
    } else {
      final SelectionOwner panel = getCurrentSelectionOwner();
      final String named;
      if (panel != null) {
        final List<? extends Proxy> selection = panel.getCurrentSelection();
        final boolean enabled = panel.canDelete(selection);
        setEnabled(enabled);
        named = enabled ? ProxyNamer.getCollectionClassName(selection) : null;
      } else {
        setEnabled(false);
        named = null;
      }
      putValue(Action.NAME, named == null ? "Delete" : "Delete " + named);
      putValue(Action.SHORT_DESCRIPTION, "Delete selected items");
    }
  }


  //#########################################################################
  //# Data Members
  private final Proxy mActionArgument;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
