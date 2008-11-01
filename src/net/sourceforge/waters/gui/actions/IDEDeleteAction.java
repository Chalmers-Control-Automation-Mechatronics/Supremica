//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   IDEDeleteAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.DeleteCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;

import org.supremica.gui.ide.IDE;


/**
 * <P>The action associated with the 'delete' key and menu buttons.</P>
 *
 * <P>This action deletes all selected items in the panel that currently
 * owns the focus. To support this action, components including editable
 * items must implement the {@link SelectionOwner#getCurrentSelection()
 * getCurrentSelection()}, {@link SelectionOwner#canDelete(List<? extends
 * Proxy>) canDelete()}, {@link SelectionOwner#getDeletionVictims(List<?
 * extends Proxy>) getDeletionVictims()}, {@link
 * SelectionOwner#deleteItems(List<net.sourceforge.waters.gui.transfer.InsertInfo>)
 * deleteItems()} and {@link
 * SelectionOwner#insertItems(List<net.sourceforge.waters.gui.transfer.InsertInfo>)
 * insertItems()} methods of the {@link SelectionOwner} interface.</P>
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
    putValue(Action.SMALL_ICON,
	     new ImageIcon(IDE.class.getResource
			   ("/toolbarButtonGraphics/general/Delete16.gif")));
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
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

}
