//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   IDEDeselectAllAction
//###########################################################################
//# $Id: IDEDeselectAllAction.java,v 1.4 2008-03-09 21:52:09 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.SelectionOwner;

import org.supremica.gui.ide.IDE;


/**
 * <P>The action associated with the 'deselect all' menu buttons.</P>
 *
 * <P>This deselects all items the panel that currently owns the focus to
 * the system clipboard. To support this action, components including
 * editable items must implement the {@link
 * SelectionOwner#hasNonEmptySelection() hasNonEmptySelection()} and {@link
 * SelectionOwner#clearSelection(boolean) clearSelection()} methods of the
 * {@link SelectionOwner} interface.</P>
 *
 * @author Robi Malik
 */

public class IDEDeselectAllAction
  extends IDEAction
{

  //#########################################################################
  //# Constructors
  IDEDeselectAllAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Deselect All");
    putValue(Action.SHORT_DESCRIPTION, "Clear the selection");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_ESCAPE);
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
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
      watersOwner.clearSelection(false);
    } else if (swingOwner != null) {
      final int pos = swingOwner.getSelectionStart();
      swingOwner.setCaretPosition(pos);
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
        enabled = watersOwner.hasNonEmptySelection();
      } else if (swingOwner != null) {
        enabled =
          swingOwner.getSelectionStart() < swingOwner.getSelectionEnd();
      } else {
        enabled = false;
      }
      setEnabled(enabled);
    }
  }

}
