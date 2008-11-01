//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   InsertForeachComponentAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.ForeachComponentEditorDialog;
import org.supremica.gui.ide.IDE;


/**
 * The action to create a new foreach-component block for a module.
 * This action merely popups the foreach component creation dialog
 * ({@link ForeachComponentEditorDialog});
 * the actual component creation is done when the dialog is committed.
 *
 * @author Robi Malik
 */

public class InsertForeachComponentAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  InsertForeachComponentAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "New Foreach Block ...");
    putValue(Action.SHORT_DESCRIPTION, "Add a foreach block to the module");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final ModuleWindowInterface root = getActiveModuleWindowInterface();
    if (root != null) {
      new ForeachComponentEditorDialog(root);
    }
  }

}
