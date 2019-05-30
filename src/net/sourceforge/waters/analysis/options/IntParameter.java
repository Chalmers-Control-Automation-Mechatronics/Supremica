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

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;



/**
 * A configurable parameter of a {@link ModelAnalyzer} of <CODE>int</CODE> type.
 *
 * @author Brandon Bassett
 */
public class IntParameter extends Parameter {

    private final int min;
    private final int max;
    private int value;

    public IntParameter(final int id, final String name, final String description,
                        final int min, final int max) {
        super(id, name, description);
        this.min = min;
        this.max = max;
        value = min + max / 2;
    }

    @Override
    public Component createComponent() {
    	final JTextField ret = new JTextField();
    	ret.setText(String.valueOf(value));
    	ret.setColumns(20);
    	return ret;
    }

    public void clamp() {

        if(value > max) {
        	value = max;
        }
        else if(value < min) {
        	value = min;
        }
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getValue() {
        return value;
    }

    public void setValue(final int val) {
    	value = val;
    	clamp();
    }

  //Updates parameter value using the component stored in the passed panel
    @Override
    public void updateFromGUI(final ParameterPanel panel)
    {
      final Component comp = panel.getEntryComponent();
      final JTextField textField = (JTextField) comp;
      int val;
      try {
        val = (Integer.parseInt(textField.getText()));
        setValue(val);
      }
      catch(final NumberFormatException e) {
        value = min;
        textField.setText(String.valueOf(value));
        final JFrame frame = new JFrame();
        JOptionPane.showMessageDialog(frame, "Invalid value in " + this.getName() + " field, resetting to minimum value.");
      }
    }

    //Updates a ParameterPanels component with parameter value
    @Override
    public void displayInGUI(final ParameterPanel panel)
    {
      final Component comp = panel.getEntryComponent();
      final JTextField textField = (JTextField) comp;
      textField.setText(String.valueOf(value));
    }
}
