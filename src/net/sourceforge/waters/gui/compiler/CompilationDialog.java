//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.gui.compiler;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import net.sourceforge.waters.gui.HTMLPrinter;
import net.sourceforge.waters.model.expr.EvalException;

import org.supremica.gui.ide.IDE;


/**
 * A dialog which initially says "Compiling..." with an Abort button and can
 * later be changed to display an error message with an OK button.
 */
public class CompilationDialog extends JDialog
{

  //##########################################################################
  //# Constructors
  /**
   * Creates a compilation dialog.
   * @param ide     The parent window.
   * @param action  The action to perform when the Abort/OK button is pressed.
   *                If <CODE>null</CODE>, the button closes the dialog.
   */
  public CompilationDialog(final IDE ide, final ActionListener action)
  {
    super(ide, "Compilation");
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(ide);
    mInformationLabel = new JLabel();
    mInformationLabel.setHorizontalAlignment(JLabel.CENTER);
    HTMLPrinter.setLabelText(mInformationLabel, "Compiling ...", DEFAULT_WIDTH);
    final Border outer = BorderFactory.createRaisedBevelBorder();
    final Border inner = BorderFactory.createEmptyBorder(4, 4, 4, 4);
    final Border border = BorderFactory.createCompoundBorder(outer, inner);
    mInformationLabel.setBorder(border);
    mExitButton = new JButton("Abort");
    if (action != null) {
      mExitButton.addActionListener(action);
    } else {
      mExitButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          dispose();
        }
      });
    }
    final JPanel exitPanel = new JPanel();
    exitPanel.add(mExitButton);
    final Container pane = getContentPane();
    pane.add(mInformationLabel, BorderLayout.CENTER);
    pane.add(exitPanel, BorderLayout.SOUTH);
    pack();
    setLocationRelativeTo(getParent());
    setVisible(true);
  }

  /**
   * Displays an error message.
   * @param exception   The <CODE>EvalException</CODE> for which to display
   *                    the message.
   * @param taskVerb    Should fit in the sentence "The module cannot be
   *                    <I>verb</I> because it has errors".
   */
  public void setEvalException(final EvalException exception,
                               final String taskVerb)
  {
    final StringBuilder sb = new StringBuilder();
    sb.append("The module cannot be ");
    sb.append(taskVerb);
    sb.append(" because it has ");
    final List<EvalException> all = exception.getAll();
    if (all.size() == 1) {
      sb.append("an error: " );
      sb.append(all.get(0).getMessage());
    } else {
      sb.append(all.size());
      sb.append(" errors.");
    }
    HTMLPrinter.setLabelText(mInformationLabel, sb.toString(), DEFAULT_WIDTH);
    mExitButton.setText("OK");
    pack();
    setLocationRelativeTo(getParent());
  }


  //##########################################################################
  //# Data Members
  private final JButton mExitButton;
  private final JLabel mInformationLabel;


  //##########################################################################
  //# Class Constants
  private static final long serialVersionUID = -561416722989926299L;

  private static final int DEFAULT_WIDTH = 290;

}
