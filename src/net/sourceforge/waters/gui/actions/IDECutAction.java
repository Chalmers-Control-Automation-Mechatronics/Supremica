//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   IDECutAction
//###########################################################################
//# $Id: IDECutAction.java,v 1.2 2007-12-04 03:22:54 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.DeleteCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.ClipboardChangedEvent;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;

import org.supremica.gui.ide.IDE;


/**
 * <P>The action associated with the 'cut' key and menu buttons.</P>
 *
 * <P>This action copies all selected items from the panel that currently
 * owns the focus to the system clipboard. To support this action,
 * components including editable items must implement the {@link
 * SelectionOwner#getCurrentSelection() getCurrentSelection()}, {@link
 * SelectionOwner#canCut(List<? extends Proxy>) canCopy()}, {@link
 * SelectionOwner#canCut(List<? extends Proxy>) canDelete()}, {@link
 * SelectionOwner#createTransferable(List<? extends Proxy>)
 * createTransferable()}, {@link
 * SelectionOwner#getDeletionVictims(List<Proxy>) getDeletionVictims()},
 * {@link
 * SelectionOwner#insertItems(List<net.sourceforge.waters.gui.transfer.InsertInfo>)
 * insertItems()} and {@link
 * SelectionOwner#deleteItems(List<net.sourceforge.waters.gui.transfer.InsertInfo>)
 * deleteItems()} methods of the {@link SelectionOwner} interface.</P>
 *
 * @author Robi Malik
 */

public class IDECutAction
  extends IDEAction
  implements ClipboardOwner
{

  //#########################################################################
  //# Constructors
  IDECutAction(final IDE ide)
  {
    super(ide);
    mDefaultAction = new DefaultEditorKit.CutAction();
    putValue(Action.NAME, "Cut");
    putValue(Action.SHORT_DESCRIPTION,
             "Delete selection and place into clipboard");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
    putValue(Action.ACCELERATOR_KEY,
	     KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
    putValue(Action.SMALL_ICON,
	     new ImageIcon(IDE.class.getResource
			   ("/toolbarButtonGraphics/general/Cut16.gif")));
    setEnabled(false);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final FocusTracker tracker = getFocusTracker();
    final SelectionOwner watersOwner = tracker.getWatersSelectionOwner();
    final JTextComponent swingOwner = tracker.getSwingSelectionOwner();
    if (watersOwner != null) {
      final List<? extends Proxy> selection =
        watersOwner.getCurrentSelection();
      final Transferable transferable =
        watersOwner.createTransferable(selection);
      final Clipboard clipboard =
        Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(transferable, this);
      fireClipboardChanged();
      final List<InsertInfo> deletes =
        watersOwner.getDeletionVictims(selection);
      final Command cmd = new DeleteCommand(deletes, watersOwner);
      final UndoInterface undoer = watersOwner.getUndoInterface();
      if (undoer == null) {
        // If there is no undo interface, just delete them ...
        cmd.execute();
      } else {
        // Otherwise register the command ...
        undoer.executeCommand(cmd);
      }
    } else if (swingOwner != null) {
      mDefaultAction.actionPerformed(event);
      fireClipboardChanged();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.SELECTION_CHANGED) {
      final FocusTracker tracker = getFocusTracker();
      final SelectionOwner watersOwner = tracker.getWatersSelectionOwner();
      final JTextComponent swingOwner = tracker.getSwingSelectionOwner();
      final boolean enabled;
      if (watersOwner != null) {
        final List<? extends Proxy> selection =
          watersOwner.getCurrentSelection();
        enabled =
          watersOwner.canCopy(selection) &&
          watersOwner.canDelete(selection);
      } else if (swingOwner != null) {
        enabled = swingOwner.getSelectedText() != null;
      } else {
        enabled = false;
      }
      setEnabled(enabled);
    }
  }


  //#########################################################################
  //# Interface java.awt.datatransfer.ClipboardOwner
  public void lostOwnership(final Clipboard clipboard,
                            final Transferable contents)
  {
  }


  //#########################################################################
  //# Auxiliary Methods
  private void fireClipboardChanged()
  {
    final IDE ide = getIDE();
    final EditorChangedEvent event = new ClipboardChangedEvent(this);
    ide.fireEditorChangedEvent(event);
  }


  //#########################################################################
  //# Data Members
  private final Action mDefaultAction;

}
