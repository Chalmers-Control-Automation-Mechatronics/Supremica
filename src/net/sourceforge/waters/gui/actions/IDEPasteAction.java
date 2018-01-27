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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.base.WatersRuntimeException;

import org.supremica.gui.ide.IDE;


/**
 * <P>The action associated with the 'paste' key and menu buttons.</P>
 *
 * <P>This action copies the contents of the system clipboard into the
 * panel that currently owns the keyboard focus. To support this action,
 * components including editable items must implement the {@link
 * SelectionOwner#canPaste(Transferable) canPaste()}, {@link
 * SelectionOwner#getInsertInfo(Transferable) getInsertInfo()}, {@link
 * SelectionOwner#insertItems(List) insertItems()}, and {@link
 * SelectionOwner#deleteItems(List) deleteItems()} methods of
 * the {@link SelectionOwner} interface.</P>
 *
 * @author Robi Malik
 */

public class IDEPasteAction
  extends IDEAction
{

  //#########################################################################
  //# Constructors
  IDEPasteAction(final IDE ide)
  {
    super(ide);
    mDefaultAction = new DefaultEditorKit.PasteAction();
    putValue(Action.NAME, "Paste");
    putValue(Action.SHORT_DESCRIPTION, "Insert the clipboard contents");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
    putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_TOOL_PASTE);
    setEnabled(false);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    try {
      final FocusTracker tracker = getFocusTracker();
      final SelectionOwner watersOwner = tracker.getWatersSelectionOwner();
      final JTextComponent swingOwner = tracker.getSwingSelectionOwner();
      if (watersOwner != null) {
        final Clipboard clipboard =
          Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable transferable = clipboard.getContents(this);
        final List<InsertInfo> info = watersOwner.getInsertInfo(transferable);
        if (info != null) {
          final ModuleWindowInterface root = getActiveModuleWindowInterface();
          final InsertCommand cmd = new InsertCommand(info, watersOwner, root);
          final UndoInterface undoer = watersOwner.getUndoInterface(this);
          if (undoer == null) {
            // If there is no undo interface, just insert them ...
            cmd.execute();
          } else {
            // Otherwise register the command ...
            undoer.executeCommand(cmd);
          }
        }
      } else if (swingOwner != null) {
        mDefaultAction.actionPerformed(event);
      }
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final UnsupportedFlavorException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case SELECTION_CHANGED:
    case CLIPBOARD_CHANGED:
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
    final Clipboard clipboard =
      Toolkit.getDefaultToolkit().getSystemClipboard();
    final Transferable transferable = clipboard.getContents(this);
    final FocusTracker tracker = getFocusTracker();
    final SelectionOwner watersOwner = tracker.getWatersSelectionOwner();
    final JTextComponent swingOwner = tracker.getSwingSelectionOwner();
    final boolean enabled;
    if (watersOwner != null) {
      enabled = watersOwner.canPaste(transferable);
    } else if (swingOwner != null && swingOwner.isEditable()) {
      enabled = transferable.isDataFlavorSupported(DataFlavor.stringFlavor);
    } else {
      enabled = false;
    }
    setEnabled(enabled);
  }


  //#########################################################################
  //# Data Members
  private final Action mDefaultAction;


  //#######################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
