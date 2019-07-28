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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;


/**
 * A configurable parameter of a {@link ModelAnalyzer} of <CODE>int</CODE> type.
 *
 * @author Brandon Bassett
 */
public class IntParameter extends AbstractTextFieldParameter
{
  //#########################################################################
  //# Constructors
  public IntParameter(final IntParameter template)
  {
    this(template.getID(), template.getName(),
         template.getDescription(), template.getMin(),
         template.getMax(), template.getValue());
  }

  IntParameter(final int id, final String name, final String description,
               final int min, final int max, final int defValue)
  {
    super(id, name, description);
    mMin = min;
    mMax = max;
    defaultValue = defValue;
    mValue = defValue;
    mFormat = NumberFormat.getIntegerInstance();
    mFormat.setGroupingUsed(false);
    if(min < 0) {
      mAlphabet = "-?[0-9]+";
    } else {
      mAlphabet = "[0-9]+";
    }
  }


  //#########################################################################
  //# Overrides for ney.sourceforge.waters.analysis.options.Parameter
  @Override
  public Component createComponent(final ProductDESContext model)
  {
    final JTextField textField = (JTextField) super.createComponent(model);

    setTextField(textField, mValue);
    textField.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(final FocusEvent e){}

      @Override
      public void focusLost(final FocusEvent e)
      {
        //very valid integer if there is text
        if (!textField.getText().isEmpty()) {
          try {
            final Integer tmp = Integer.parseInt(textField.getText());
            if (tmp < mMin || tmp > mMax) {
              JOptionPane
                .showMessageDialog(new JFrame(),
                                   "Integer outside of range." + "\n"
                                                 + "Value must be a number between "
                                                 + "(" + mMin + ")" + " - "
                                                 + "(" + mMax + ")");
              setTextField(textField, defaultValue);
            }
          } catch (final NumberFormatException exception) {
            JOptionPane
              .showMessageDialog(new JFrame(),
                                 "Input is not a valid Integer." + "\n"
                                               + "Value must be a number between "
                                               + "(" + mMin + ")" + " - "
                                               + "(" + mMax + ")");
            setTextField(textField, defaultValue);
          }
        }
      }
    });

    return textField;
  }

  @Override
  protected boolean testAlphabet(final String text) {

    //only verify in alphabet

      //special case where minimum is negative
      if (text.length() == 1 && text.equals("-") && mAlphabet.substring(0,1).contains("-")) {
        return true;
      }
      else if(text.isEmpty()) {
        return true;
      }
      else if (text.matches(mAlphabet)) {
        return true;
      }
     return false;
 }

  public int getValue() {
    return mValue;
  }

  public int getMin() {
    return mMin;
  }

  public int getMax() {
    return mMax;
  }

  //Sets the text of the textField to the desired value, empty string if value is Integer.MAX_VALUE
  private void setTextField(final JTextField ret, final int value) {
    if (value == Integer.MAX_VALUE)
      ret.setText("");
    else
      ret.setText(String.valueOf(value));
  }

  //Updates parameter value using the component stored in the passed panel
  //Used when commit a parameter from panel
  @Override
  public void updateFromGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JTextField textField = (JTextField) comp;
    //empty field default to max
    if(textField.getText().equals(""))
      mValue = Integer.MAX_VALUE;
    else
      mValue = Integer.parseInt(textField.getText());
  }

  //Updates a ParameterPanels component with parameter value
  @Override
  public void displayInGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JTextField textField = (JTextField) comp;
    setTextField(textField, mValue);
  }

  @Override
  public void updateFromParameter(final Parameter p)
  {
    mValue = ((IntParameter) p).getValue();
  }

  @Override
  public String toString()
  {
    return ("ID: " + getID() + " Name: " + getName() +" Value: " + getValue());
  }


  //#########################################################################
  //# Data Members
  private final int mMin;
  private final int mMax;
  private int mValue;
  private final int defaultValue;
  private final NumberFormat mFormat;
  private String mAlphabet;
}
