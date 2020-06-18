//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2020 Knut Akesson, Martin Fabian, Robi Malik
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
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.marshaller.DocumentManager;

import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;


/**
 * The standard action to open a module. The open action is triggered from the
 * IDE's <CODE>File&nbsp;-&gt;&nbsp;Open</CODE> menu item and pops up a file
 * dialog asking for a file containing a single module to be opened. In addition
 * to WATERS and Supremica modules, the action also supports some external file
 * formats as long as their contents can be converted into a single module. For
 * external files containing more than one module, the import action
 * ({@link ImportAction}) must be used.
 *
 * @author Robi Malik
 */

public class OpenAction extends net.sourceforge.waters.gui.actions.IDEAction
{

  // #######################################################################
  // # Constructor
  OpenAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Open ...");
    putValue(Action.SHORT_DESCRIPTION, "Open/import a module");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
    putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_TOOL_OPEN);
  }

  // #######################################################################
  // # Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    // Get the state and dialog ...
    final IDE ide = getIDE();
    final DocumentManager dmanager = ide.getDocumentManager();
    final JFileChooser chooser = ide.getFileChooser();
    final FileFilter current = chooser.getFileFilter();
    // Set up the dialog ...
    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
    chooser.setMultiSelectionEnabled(true);
    chooser.resetChoosableFileFilters();
    final List<FileFilter> filters = dmanager.getSupportedFileFilters();
    boolean reselect = false;
    for (final FileFilter filter : filters) {
      chooser.addChoosableFileFilter(filter);
      if (filter == current) {
        reselect = true;
      }
    }
    // Select the first filter ...
    final FileFilter first = reselect ? current : filters.iterator().next();
    chooser.setFileFilter(first);
    // Show the dialog ...
    final JFrame frame = ide.getFrame();
    final int choice = chooser.showOpenDialog(frame);
    // Load the files ...
    if (choice == JFileChooser.APPROVE_OPTION) {
      final File[] filearray = chooser.getSelectedFiles();
      final List<File> filelist = Arrays.asList(filearray);
      final DocumentContainerManager cmanager =
          ide.getDocumentContainerManager();
      cmanager.openContainers(filelist);
    }
  }

  // #########################################################################
  // # Class Constants
  private static final long serialVersionUID = 1L;

}
