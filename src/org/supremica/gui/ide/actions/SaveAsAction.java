//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   SaveAsAction
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;

import net.sourceforge.waters.gui.util.IconLoader;

import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;


public class SaveAsAction extends AbstractSaveAction
{

  //#########################################################################
  //# Constructor
  public SaveAsAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Save As ...");
    putValue(Action.SHORT_DESCRIPTION, "Save the module using a new name");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
    putValue(Action.SMALL_ICON, IconLoader.ICON_TOOL_SAVE_AS);
  }

  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final IDE ide = getIDE();
    final DocumentContainerManager manager = ide.getDocumentContainerManager();
    manager.saveActiveContainerAs();
  }

  //#########################################################################
  //# Enabling and Disabling
  @Override
  String getShortDescription(final String type)
  {
    return "Save the " + type + "using a new name";
  }

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
