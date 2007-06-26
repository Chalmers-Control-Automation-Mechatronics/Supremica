//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   SaveAction
//###########################################################################
//# $Id: SaveAction.java,v 1.14 2007-06-26 20:45:14 robi Exp $
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;


public class SaveAction extends AbstractSaveAction
{

  //#########################################################################
  //# Constructor
  SaveAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Save");
    putValue(Action.SHORT_DESCRIPTION, "Save the module");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
	putValue(Action.ACCELERATOR_KEY,
			 KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource
              ("/toolbarButtonGraphics/general/Save16.gif")));
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final IDE ide = getIDE();
    final DocumentContainerManager manager = ide.getDocumentContainerManager();
    manager.saveActiveContainer();
  }


  //#########################################################################
  //# Enabling and Disabling
  String getShortDescription(final String type)
  {
    return "Save the " + type;
  }

}
