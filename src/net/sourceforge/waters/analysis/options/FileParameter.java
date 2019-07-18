//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;

/**
 * A configurable parameter of a {@link ModelAnalyzer} of <CODE>file</CODE> type.
 *
 * @author Brandon Bassett
 */
public class FileParameter extends Parameter
{
  public FileParameter(final FileParameter template)
  {
    super(template.getID(), template.getName(), template.getDescription());
  }

  public FileParameter(final int id, final String name,
                       final String description)
  {
    super(id, name, description);
  }

  @Override
  public Component createComponent(final ProductDESContext model)
  {
    final JPanel panel = new JPanel();
    final JButton button = new JButton("...");
    final JTextField text = new JTextField();

    text.setColumns(10);

    if (getValue() != null)
      text.setText(getValue().getAbsolutePath());

    text.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(final FocusEvent e){}

      @Override
      public void focusLost(final FocusEvent e)
      {
        final File tmp = new File(text.getText());

        //no parent, default to desktop
        if (!text.getText().equals("") && tmp.getParent() == null) {
          mValue = new File(System.getProperty("user.home") + File.separator
                            + "Desktop", text.getText());
          text.setText(mValue.getAbsolutePath());
        }//file has a parent
        else if (!text.getText().equals("") && tmp.getParent() != null){

          //does parent exist
          if(new File(tmp.getParent()).exists()) {
            mValue = new File(text.getText());
            text.setText(mValue.getAbsolutePath());
          }
          else {
            JOptionPane.showMessageDialog(new JFrame(), "Invalid File");
            text.setText(mValue.getAbsolutePath());
          }

        }//empty textField
        else
          mValue = null;
      }
    });

    final ActionListener saveFile = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        JFileChooser jfc;

        //Go to parent directory, text field set to file name
        if (mValue != null) {
          jfc = new JFileChooser(mValue.getParent());
          jfc.setSelectedFile(mValue);
        } else
          jfc = new JFileChooser();

        if (jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
          mValue = jfc.getSelectedFile();
          text.setText(getValue().getAbsolutePath());
        }
      }
    };

    button.addActionListener(saveFile);
    panel.add(text);
    panel.add(button);

    return panel;
  }

  @Override
  public void updateFromGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JPanel compPanel = (JPanel) comp;
    final JTextField text = (JTextField) compPanel.getComponent(0);
    //if text empty default to null file
    if (!text.getText().equals(""))
      mValue = new File(text.getText());
    else
      mValue = null;
  }

  @Override
  public void updateFromParameter(final Parameter p)
  {
    mValue = ((FileParameter) p).getValue();
  }

  public File getValue()
  {
    return mValue;
  }

  @Override
  public String toString()
  {
    return ("ID: " + getID() + " Name: " + getName() + " Value: " + getValue());
  }

  //#########################################################################
  //# Data Members
  private File mValue;
}
