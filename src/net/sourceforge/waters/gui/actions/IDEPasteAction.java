//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   IDEPasteAction
//###########################################################################
//# $Id$
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
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
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
 * SelectionOwner#insertItems(List<InsertInfo>) insertItems()}, and {@link
 * SelectionOwner#deleteItems(List<InsertInfo>) deleteItems()} methods of
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
    putValue(Action.SMALL_ICON,
	     new ImageIcon(IDE.class.getResource
			   ("/toolbarButtonGraphics/general/Paste16.gif")));
    setEnabled(false);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
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
          final InsertCommand cmd = new InsertCommand(info, watersOwner);
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
    } else if (swingOwner != null) {
      enabled = transferable.isDataFlavorSupported(DataFlavor.stringFlavor);
    } else {
      enabled = false;
    }
    setEnabled(enabled);
  }


  //#########################################################################
  //# Data Members
  private final Action mDefaultAction;

}
