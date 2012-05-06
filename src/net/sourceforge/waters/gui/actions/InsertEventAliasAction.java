//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   InsertConstantAliasAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;

import net.sourceforge.waters.gui.EventAliasEditorDialog;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import org.supremica.gui.ide.IDE;


/**
 * The action to create a new event alias for a module.
 * This action merely popups the event alias creation dialog
 * ({@link EventAliasEditorDialog});
 * the actual alias creation is done when the dialog is committed.
 *
 * @author Carly Hona
 */

public class InsertEventAliasAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  InsertEventAliasAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "New Event Alias ...");
    putValue(Action.SHORT_DESCRIPTION, "Add an event alias to the module");
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final ModuleWindowInterface root = getActiveModuleWindowInterface();
    if (root != null) {
      new EventAliasEditorDialog(root);
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
