//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   GraphLayoutAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.Action;

import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;


public class GotoModuleAction
  extends WatersGraphAction
{

  //#########################################################################
  //# Constructor
  GotoModuleAction(final IDE ide, final String name, final URI uri)
  {
    super(ide);
    mURI = uri;
    putValue(Action.NAME, "Show module " + name);
    putValue(Action.SHORT_DESCRIPTION, "Show module:" + uri.toString());
    setEnabled(true);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final IDE ide = getIDE();
    final DocumentContainerManager manager = ide.getDocumentContainerManager();
    manager.openContainer(mURI);
  }


  //#########################################################################
  //# Data Members
  private final URI mURI;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
