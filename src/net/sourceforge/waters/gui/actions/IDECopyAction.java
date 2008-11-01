//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   IDECopyAction
//###########################################################################
//# $Id$
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

import net.sourceforge.waters.gui.observer.ClipboardChangedEvent;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;

import org.supremica.gui.ide.IDE;


/**
 * <P>The action associated with the 'copy' key and menu buttons.</P>
 *
 * <P>This action copies all selected items from the panel that currently
 * owns the focus to the system clipboard. To support this action,
 * components including editable items must implement the {@link
 * SelectionOwner#getCurrentSelection() getCurrentSelection()}, {@link
 * SelectionOwner#canCopy(List<? extends Proxy>) canCopy()}, and {@link
 * SelectionOwner#createTransferable(List<? extends Proxy>)
 * createTransferable()} methods of the {@link SelectionOwner}
 * interface.</P>
 *
 * @author Robi Malik
 */

public class IDECopyAction
  extends IDEAction
  implements ClipboardOwner
{

  //#########################################################################
  //# Constructors
  IDECopyAction(final IDE ide)
  {
    super(ide);
    mDefaultAction = new DefaultEditorKit.CopyAction();
    putValue(Action.NAME, "Copy");
    putValue(Action.SHORT_DESCRIPTION, "Copy selection to clipboard");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
    putValue(Action.ACCELERATOR_KEY,
	     KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
    putValue(Action.SMALL_ICON,
	     new ImageIcon(IDE.class.getResource
			   ("/toolbarButtonGraphics/general/Copy16.gif")));
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
        enabled = watersOwner.canCopy(selection);
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
