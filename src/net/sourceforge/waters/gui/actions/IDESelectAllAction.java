//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   IDESelectAllAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;

import org.supremica.gui.ide.IDE;


/**
 * <P>The action associated with the 'select all' menu buttons.</P>
 *
 * <P>This selects all items the panel that currently owns the focus to the
 * system clipboard. To support this action, components including editable
 * items must implement the {@link SelectionOwner#canSelectMore()
 * canSelectMore()}, {@link SelectionOwner#getAllSelectableItems()
 * getAllSelectableItems()}, {@link SelectionOwner#canSelectMore()
 * canSelectMore()}, {@link SelectionOwner#replaceSelection(List<?
 * extends Proxy>) replaceSelection()} methods of the {@link SelectionOwner}
 * interface.</P>
 *
 * @author Robi Malik
 */

public class IDESelectAllAction
  extends IDEAction
{

    IDE ide;

  //#########################################################################
  //# Constructors
  IDESelectAllAction(final IDE ide)
  {
    super(ide);
    this.ide = ide;
    putValue(Action.NAME, "Select All");
    putValue(Action.SHORT_DESCRIPTION, "Select all items in the panel");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
    putValue(Action.ACCELERATOR_KEY,
	     KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
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
      final List<? extends Proxy> all = watersOwner.getAllSelectableItems();
      watersOwner.replaceSelection(all);
    } else if (swingOwner != null) {
      final int len = swingOwner.getText().length();
      swingOwner.setCaretPosition(0);
      swingOwner.moveCaretPosition(len);
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
        enabled = watersOwner.canSelectMore();
      } else if (swingOwner != null) {
        if (swingOwner.getSelectionStart() > 0) {
          enabled = true;
        } else {
          final int len = swingOwner.getText().length();
          enabled = swingOwner.getSelectionEnd() < len;
        }
      } else {
        enabled = false;
      }
      setEnabled(enabled);
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
