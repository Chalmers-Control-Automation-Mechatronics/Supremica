//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;
import java.util.List;

import javax.swing.Action;

import net.sourceforge.waters.gui.util.IconLoader;
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
    putValue(Action.SMALL_ICON, IconLoader.ICON_TOOL_IMPORT);
    mCurrentSource = null;
    mCurrentTarget = null;
  }

  // #########################################################################
  // # Interface java.awt.event.ActionListener
  @Override
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





