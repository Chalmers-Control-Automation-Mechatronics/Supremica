//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   WatersUndoAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import org.supremica.gui.ide.IDE;


public class WatersUndoAction
  extends WatersAction
{

  //#########################################################################
  //# Constructor
  WatersUndoAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Undo");
    putValue(Action.SHORT_DESCRIPTION, "Undo the last command");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U);
    putValue(Action.ACCELERATOR_KEY,
	     KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
    putValue(Action.SMALL_ICON,
	     new ImageIcon(IDE.class.getResource
			   ("/toolbarButtonGraphics/general/Undo16.gif")));
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final UndoInterface umanager = getActiveUndoInterface();
    if (umanager != null && umanager.canUndo()) {
      umanager.undo();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
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
    final boolean enabled = umanager != null && umanager.canUndo();
    setEnabled(enabled);
    final String text =
      enabled ? umanager.getUndoPresentationName() : "Can't undo";
    putValue(Action.NAME, text);
    putValue(Action.SHORT_DESCRIPTION, text);
  }

}
