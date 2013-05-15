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

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.dialog.ConstantAliasEditorDialog;
import net.sourceforge.waters.gui.util.IconLoader;

import org.supremica.gui.ide.IDE;


/**
 * The action to create a new constant alias for a module.
 * This action merely popups the constant alias creation dialog
 * ({@link ConstantAliasEditorDialog});
 * the actual alias creation is done when the dialog is committed.
 *
 * @author Carly Hona
 */

public class InsertConstantAliasAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  InsertConstantAliasAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "New Named Constant ...");
    putValue(Action.SHORT_DESCRIPTION, "Add a constant definition to the module");
    putValue(Action.SMALL_ICON, IconLoader.ICON_NEW_CONSTANT);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final ModuleWindowInterface root = getActiveModuleWindowInterface();
    if (root != null) {
      new ConstantAliasEditorDialog(root);
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
