//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.options;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.gui.dialog.FileInputCell;
import net.sourceforge.waters.model.options.FileOption;

import org.supremica.properties.Config;


/**
 * An option panel to edit a {@link FileOption} through the GUI.
 * Consists of a text field to enter a file name and a button to display
 * a {@link JFileChooser}.
 *
 * @author Brandon Bassett, Robi Malik
 */

class FileOptionPanel
  extends OptionPanel<File>
{
  //#########################################################################
  //# Constructors
  FileOptionPanel(final GUIOptionContext context,
                  final FileOption option)
  {
    super(context, option);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.options.OptionEditor
  @Override
  public FileOption getOption()
  {
    return (FileOption) super.getOption();
  }

  @Override
  public void commitValue()
  {
    if (mCell.shouldYieldFocus()) {
      final FileOption option = getOption();
      final File file = getCurrentFile();
      option.setValue(file);
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.options.OptionPanel
  @Override
  JPanel createEntryComponent()
  {
    final FileOption option = getOption();
    final File file = option.getValue();

    final JPanel panel = new JPanel();
    final GridBagLayout layout = new GridBagLayout();
    panel.setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridy = 0;

    final File defaultDirectory;
    if (option.getType() == FileOption.Type.OUTPUT_FILE) {
      defaultDirectory = Config.FILE_SAVE_PATH.getValue();
    } else {
      defaultDirectory = Config.FILE_OPEN_PATH.getValue();
    }
    mCell = new FileInputCell(defaultDirectory, true);
    mCell.setValue(file);
    mCell.setColumns(15);
    final GUIOptionContext context = getContext();
    mCell.setErrorDisplay(context.getErrorDisplay());
    constraints.weightx = 1.0;
    panel.add(mCell, constraints);

    final JButton button = new JButton("...");
    final ActionListener listener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        showFileChooser();
      }
    };
    button.addActionListener(listener);
    constraints.weightx = 0.0;
    panel.add(button, constraints);

    return panel;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void showFileChooser()
  {
    final FileOption option = getOption();
    final JFileChooser chooser = new JFileChooser();
    File file = mCell.getValue();
    if (file != null) {
      chooser.setSelectedFile(file);
    } else {
      chooser.setCurrentDirectory(mCell.getDefaultDirectory());
    }
    chooser.resetChoosableFileFilters();
    final FileFilter[] filters = option.getFileFilters();
    if (filters != null) {
      for (final FileFilter filter : filters) {
        chooser.addChoosableFileFilter(filter);
      }
      chooser.setFileFilter(filters[0]);
    }
    chooser.setDialogTitle(option.getShortName());
    switch (option.getType()) {
    case INPUT_FILE:
      chooser.setDialogType(JFileChooser.OPEN_DIALOG);
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      break;
    case OUTPUT_FILE:
      chooser.setDialogType(JFileChooser.SAVE_DIALOG);
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      break;
    case DIRECTORY:
      chooser.setDialogType(JFileChooser.OPEN_DIALOG);
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      if (file != null) {
        chooser.setCurrentDirectory(file);
      }
      break;
    }
    final GUIOptionContext context = getContext();
    final Component parent = context.getDialogParent();
    if (chooser.showDialog(parent, "OK") == JFileChooser.APPROVE_OPTION) {
      file = chooser.getSelectedFile();
      mCell.setValue(file);
      mCell.shouldYieldFocus();
    }
  }

  private File getCurrentFile()
  {
    final FileOption option = getOption();
    final File file = mCell.getValue();
    if (file == null && option.getType() == FileOption.Type.DIRECTORY) {
      return mCell.getDefaultDirectory();
    } else {
      return file;
    }
  }


  //#########################################################################
  //# Data Members
  private FileInputCell mCell;

}
