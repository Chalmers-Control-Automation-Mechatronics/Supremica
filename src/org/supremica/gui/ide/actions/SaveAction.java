//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   SaveAction
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.util.IconLoader;

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
    putValue(Action.SMALL_ICON, IconLoader.ICON_TOOL_SAVE);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final IDE ide = getIDE();
    final DocumentContainerManager manager = ide.getDocumentContainerManager();
    manager.saveActiveContainer();
  }


  //#########################################################################
  //# Enabling and Disabling
  @Override
  String getShortDescription(final String type)
  {
    return "Save the " + type;
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
