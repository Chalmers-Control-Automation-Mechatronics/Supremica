//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   InsertSimpleComponentAction
//###########################################################################
//# $Id: InsertSimpleComponentAction.java,v 1.2 2007-12-04 03:22:54 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.SimpleComponentEditorDialog;
import org.supremica.gui.ide.IDE;


/**
 * The action to create a new simple component for a module.
 * This action merely popups the component creation dialog
 * ({@link SimpleComponentEditorDialog});
 * the actual component creation is done when the dialog is committed.
 *
 * @author Robi Malik
 */

public class InsertSimpleComponentAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  InsertSimpleComponentAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "New Automaton ...");
    putValue(Action.SHORT_DESCRIPTION, "Add an automaton to the module");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
    putValue(Action.SMALL_ICON, IconLoader.ICON_AUTOMATON);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final ModuleWindowInterface root = getActiveModuleWindowInterface();
    if (root != null) {
      new SimpleComponentEditorDialog(root);
    }
  }

}
