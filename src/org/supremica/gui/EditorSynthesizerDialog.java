//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2023 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.supremica.automata.algorithms.EditorSynthesizerOptions;


public class EditorSynthesizerDialog implements ActionListener
{
  //#########################################################################
  //# Data members
  private final JButton okButton;
  private final JButton cancelButton;
  private final EditorSynthesizerOptions synthesizerOptions;
  private final EditorSynthesizerDialogStandardPanel standardPanel;
  private final JDialog dialog;

  //#########################################################################
  //# Constructor
  /**
   * Creates modal dialog box for input of synthesizer and guard options.
   */
  public EditorSynthesizerDialog(final Frame parentFrame,
                                 final int numSelected,
                                 final EditorSynthesizerOptions synthesizerOptions,
                                 final Vector<String> events,
                                 final Vector<String> variables)
  {
    this.synthesizerOptions = synthesizerOptions;
    synthesizerOptions.setReachability(true);
    dialog = new JDialog(parentFrame, true); // model
    dialog.setTitle("Synthesizer options");
    standardPanel =
      new EditorSynthesizerDialogStandardPanel(numSelected,
                                               events,
                                               variables);
    final JPanel buttonPanel = new JPanel();
    okButton = addButton(buttonPanel, "OK");
    cancelButton = addButton(buttonPanel, "Cancel");
    dialog.add(standardPanel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);
    Utility.setDefaultButton(dialog, okButton);
    dialog.pack();
    dialog.setLocationRelativeTo(parentFrame);
    final Dimension size = dialog.getSize();
    dialog.setMinimumSize(size);
    update();
  }

  /**
   * Updates the information in the dialog from what is recorded in
   * EditorSynthesizerOptions.
   *
   * @see EditorSynthesizerOptions
   */
  public void update()
  {
    standardPanel.update(synthesizerOptions);
  }

  private JButton addButton(final Container container, final String name)
  {
    final JButton button = new JButton(name);

    button.addActionListener(this);
    container.add(button);

    return button;
  }

  public void show()
  {
    dialog.setVisible(true);
  }

  //#########################################################################
  //# Overridden methods
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final Object source = event.getSource();

    if (source == okButton) {
      standardPanel.regain(synthesizerOptions);

      if (synthesizerOptions.isValid()) {
        synthesizerOptions.saveOptions();
        synthesizerOptions.setDialogOK(true);

        dialog.setVisible(false);
        dialog.dispose();
      } else {
        final Container parent = dialog.getParent();
        JOptionPane
          .showMessageDialog(parent,
                             "Invalid combination of type and algorithm",
                             "Alert", JOptionPane.ERROR_MESSAGE);
      }
    } else if (source == cancelButton) {
      synthesizerOptions.setDialogOK(false);
      dialog.setVisible(false);
      dialog.dispose();
    }
  }
}
