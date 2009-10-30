//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   ShowModuleCommentAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import org.supremica.gui.ide.IDE;


/**
 * The action to edit the comments of a module.
 * This simply displays the comment editor of the current module.
 *
 * @author Robi Malik
 */

public class ShowModuleCommentAction
  extends WatersAction
{

  //#########################################################################
  //# Constructors
  ShowModuleCommentAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Show Module Comments");
    putValue(Action.SHORT_DESCRIPTION,
	     "Edit the name and description of the current module");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_M);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final ModuleWindowInterface root = getActiveModuleWindowInterface();
    root.showComment();
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
