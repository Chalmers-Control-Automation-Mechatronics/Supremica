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
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.supremica.automata.Automata;
import org.supremica.automata.IO.AutomataToXML;
import org.supremica.automata.IO.SupremicaUnmarshaller;
import org.supremica.gui.ide.AutomataContainer;
import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

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
    putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource
              ("/toolbarButtonGraphics/general/SaveAs16.gif")));
  }

  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final IDE ide = getIDE();
    final DocumentContainerManager manager = ide.getDocumentContainerManager();
    manager.saveActiveContainerAs();
  }

  //#########################################################################
  //# Enabling and Disabling
  String getShortDescription(final String type)
  {
    return "Save the " + type + "using a new name";
  }
}
