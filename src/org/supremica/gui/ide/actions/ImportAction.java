//# -*- tab-width: 2  indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   ImportAction
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;
import java.util.List;

import javax.swing.Action;

import net.sourceforge.waters.model.marshaller.CopyingProxyUnmarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;

import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ImportDialog;


/**
 * The action triggering the import dialog ({@link ImportDialog}). The import
 * dialog enables the user to import and convert external files containing a
 * hierarchy of modules, as opposed to a single module.
 * 
 * @see ImportDialog, OpenAction
 * @author Robi Malik
 */

public class ImportAction extends net.sourceforge.waters.gui.actions.IDEAction
{

  // #########################################################################
  // # Constructor
  ImportAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Import ...");
    putValue(Action.SHORT_DESCRIPTION, "Import modules");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
    mCurrentSource = null;
    mCurrentTarget = null;
  }

  // #########################################################################
  // # Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final IDE ide = getIDE();
    final DocumentContainerManager manager = ide.getDocumentContainerManager();
    final List<CopyingProxyUnmarshaller<ModuleProxy>> importers =
        manager.getModuleImporters();
    if (mCurrentTarget == null) {
      final String cwd = System.getProperty("user.dir");
      mCurrentTarget = new File(cwd);
    }
    final ImportDialog<ModuleProxy> dialog =
        new ImportDialog<ModuleProxy>(ide, importers, mCurrentSource,
            mCurrentTarget);
    dialog.setTitle("Import Modules ...");
    dialog.setModal(true);
    dialog.setVisible(true);
    final ModuleProxy module = dialog.getDocument();
    if (module == null) {
      mCurrentSource = dialog.getSource();
    } else {
      final URI uri = module.getLocation();
      manager.openContainer(uri);
      mCurrentSource = null;
    }
    mCurrentTarget = dialog.getTarget();
  }

  // #########################################################################
  // # Data Members
  private File mCurrentSource;
  private File mCurrentTarget;

  // #########################################################################
  // # Class Constants
  private static final long serialVersionUID = 1L;

}
