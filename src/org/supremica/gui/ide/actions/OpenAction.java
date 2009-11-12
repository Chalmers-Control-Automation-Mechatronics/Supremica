//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   OpenAction
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

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
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
        ActionEvent.CTRL_MASK));
    putValue(Action.SMALL_ICON, new ImageIcon(IDE.class
        .getResource("/toolbarButtonGraphics/general/Open16.gif")));
  }

  // #######################################################################
  // # Interface java.awt.event.ActionListener
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
