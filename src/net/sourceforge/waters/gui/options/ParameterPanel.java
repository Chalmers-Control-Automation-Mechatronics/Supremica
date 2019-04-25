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

package net.sourceforge.waters.gui.options;


import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class ParameterPanel extends JPanel
{

  private static final long serialVersionUID = 1L;
  private final Parameter mParameter;
  private final Component[] storedComponents; // don't need, just use getComponents()

  public ParameterPanel(final Parameter param, final int row)
  {
    final GridBagConstraints constraints = new GridBagConstraints();
    final GridBagLayout mainlayout = new GridBagLayout();

    mParameter = param;
    constraints.insets = new Insets(2, 4, 2, 4);
    constraints.weighty = 0.0;

    //Label
    constraints.gridx = 0;
    constraints.gridy = row;
    constraints.weightx = 0.0;
    constraints.anchor = GridBagConstraints.WEST;
    mainlayout.setConstraints(mParameter.createLabel(), constraints);
    this.add(mParameter.createLabel());

    //Component
    constraints.gridx = constraints.gridx + 2;
    constraints.gridwidth = 2;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mParameter.createComponent(), constraints);
    this.add(mParameter.createComponent());

    storedComponents = getComponents();
    for (final Component component : storedComponents)
      ((JComponent) component).setToolTipText(mParameter.getDescription());
  }

  /*
   * Updates the stored parameters value using the value stored in the
   * component, knows component passed on parameter class stored
   */
  public void updateParameter()
  {
    if (mParameter.getClass().equals(IntParameter.class))
      ((IntParameter) mParameter).setValue(Integer
        .parseInt(((JTextField) storedComponents[1]).getText()));
    else if (mParameter.getClass().equals(BoolParameter.class))
      ((BoolParameter) mParameter)
        .setValue(((JCheckBox) storedComponents[1]).isSelected());
  }

  public Parameter getParameter()
  {
    return mParameter;
  }

  public Component getEntryComponent()
  {
    return getComponents()[1];
  }

  public void setComponentValue(final int input)
  {
    ((JTextField) storedComponents[1]).setText(Integer.toString(input));
    updateParameter();
  }

  public void setComponentValue(final boolean input)
  {
    ((JCheckBox) storedComponents[1]).setSelected(input);
    updateParameter();
  }

}
