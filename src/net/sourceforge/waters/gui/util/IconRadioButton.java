//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.gui.util;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


/**
 * A radio button replacement to produce radio buttons with additional icons.
 *
 * In standard Swing, the icon of a radio button shows whether the button
 * is selected, so no additional image can be shown. This class provides
 * buttons that show their selection status, an additional icon, and the
 * descriptive text.
 *
 * It is implemented as a {@link JPanel} containing a {@link JRadioButton}
 * without label and a borderless {@link JButton} with an image and a textual
 * label, which is linked to the radio button.
 * 
 * @author Robi Malik
 */

public class IconRadioButton
  extends JPanel
  implements ActionListener
{

  //#########################################################################
  //# Constructor
  public IconRadioButton(final String text,
                         final Icon icon,
                         final ButtonGroup group)
  {
    final GridBagLayout layout = new GridBagLayout();
    setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    mButton = new JRadioButton();
    mButton.setFocusable(false);
    mButton.setRequestFocusEnabled(false);
    group.add(mButton);
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 0;
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    layout.setConstraints(mButton, constraints);
    add(mButton);
    final JButton label = new JButton(text, icon);
    final Insets margin = new Insets(0, 0, 0, 0);
    label.setMargin(margin);
    label.setBorderPainted(false);
    label.setContentAreaFilled(false);
    label.setRequestFocusEnabled(false);
    label.addActionListener(this);
    constraints.weightx = 1.0;
    layout.setConstraints(label, constraints);
    add(label);
  }


  //#########################################################################
  //# Simple Access
  public boolean isSelected()
  {
    return mButton.isSelected();
  }

  public void setSelected(final boolean selected)
  {
    mButton.setSelected(selected);
  }

  public void addActionListener(final ActionListener listener)
  {
    mButton.addActionListener(listener);
  }

  public void removeActionListener(final ActionListener listener)
  {
    mButton.removeActionListener(listener);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    mButton.doClick();
  }


  //#########################################################################
  //# Data Members
  private final JRadioButton mButton;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}








