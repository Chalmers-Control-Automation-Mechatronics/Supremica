//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   InsertEventDeclAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.EventEditorDialog;
import org.supremica.gui.ide.IDE;


/**
 * The action to create a new event declaration for a module.
 * This action merely popups the variable creation dialog
 * ({@link EventEditorDialog});
 * the actual variable creation is done when the dialog is committed.
 *
 * @author Robi Malik
 */

public class InsertEventDeclAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  InsertEventDeclAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "New Event ...");
    putValue(Action.SHORT_DESCRIPTION, "Add an event to the module");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
    putValue(Action.SMALL_ICON, IconLoader.ICON_EVENT);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final ModuleWindowInterface root = getActiveModuleWindowInterface();
    if (root != null) {
      new EventEditorDialog(root);
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
