//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   InsertVariableAction
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
import net.sourceforge.waters.gui.VariableEditorDialog;
import org.supremica.gui.ide.IDE;


/**
 * The action to create a new variable component for a module.
 * This action merely popups the variable creation dialog
 * ({@link VariableEditorDialog});
 * the actual variable creation is done when the dialog is committed.
 *
 * @author Robi Malik
 */

public class InsertVariableAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  InsertVariableAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "New Variable ...");
    putValue(Action.SHORT_DESCRIPTION, "Add a variable to the module");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_V);
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.ALT_MASK));
    putValue(Action.SMALL_ICON, IconLoader.ICON_VARIABLE);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final ModuleWindowInterface root = getActiveModuleWindowInterface();
    if (root != null) {
      new VariableEditorDialog(root);
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
