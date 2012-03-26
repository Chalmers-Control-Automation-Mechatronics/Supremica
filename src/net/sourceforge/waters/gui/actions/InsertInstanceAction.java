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

import net.sourceforge.waters.gui.InstanceEditorDialog;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import org.supremica.gui.ide.IDE;


/**
 * The action to create a new instance for a module.
 * This action merely popups the instance creation dialog
 * ({@link InstanceEditorDialog});
 * the actual instance creation is done when the dialog is committed.
 *
 * @author Carly Hona
 */

public class InsertInstanceAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  InsertInstanceAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "New Instance ...");
    putValue(Action.SHORT_DESCRIPTION, "Add an instance to the module");
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final ModuleWindowInterface root = getActiveModuleWindowInterface();
    if (root != null) {
      new InstanceEditorDialog(root);
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
