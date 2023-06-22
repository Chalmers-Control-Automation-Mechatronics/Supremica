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

package net.sourceforge.waters.gui.dialog;

import java.text.ParseException;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;


/**
 * <P>A text field to enter a double within a given range.</P>
 *
 * @author Robi Malik, Benjamin Wheeler
 */

public class DoubleInputCell
  extends ValidatingTextCell<Double>
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a double input cell for non-negative doubles.
   * The cell created by this constructor accepts values between 0 and
   * {@link Double#POSITIVE_INFINITY}, where an empty input is interpreted as
   * {@link Double#POSITIVE_INFINITY}.
   */
  public DoubleInputCell()
  {
    this(Double.NEGATIVE_INFINITY,
         Double.POSITIVE_INFINITY,
         Double.POSITIVE_INFINITY);
  }

  /**
   * Creates a double input cell for the given range.
   * This constructor creates an input cell that does not allow an empty input.
   * @param  minValue  The smallest acceptable number to be entered.
   * @param  maxValue  The largest acceptable number to be entered.
   */
  public DoubleInputCell(final double minValue,
                         final double maxValue)
  {
    super(new DoubleInputHandler(minValue, maxValue));
    setValue(minValue);
  }

  /**
   * Creates a double input cell for the given range.
   * @param  minValue  The smallest acceptable number to be entered.
   * @param  maxValue  The largest acceptable number to be entered.
   * @param  nullValue The value returned by the cell if the input is empty.
   *                   The cell created by this constructor allows an empty
   *                   input and returns the null value in this case.
   *                   Additionally, if the cell value is equal to the
   *                   null value, it is displayed as an empty string.
   */
  public DoubleInputCell(final double minValue,
                         final double maxValue,
                         final double nullValue)
  {
    super(new DoubleInputHandler(minValue, maxValue, nullValue));
    setValue(nullValue);
  }


  //#########################################################################
  //# Overrides for javax.swing.JFormattedTextField
  @Override
  public Double getValue()
  {
    return (Double) super.getValue();
  }


  //#########################################################################
  //# Inner Class DoubleInputHandler
  private static class DoubleInputHandler
    extends DocumentFilter
    implements FormattedInputHandler<Double>
  {
    //#######################################################################
    //# Constructors
    private DoubleInputHandler(final double minValue,
                               final double maxValue)
    {
      mMinValue = minValue;
      mMaxValue = maxValue;
      mNullValue = null;
    }

    private DoubleInputHandler(final double minValue,
                               final double maxValue,
                               final double nullValue)
    {
      mMinValue = minValue;
      mMaxValue = maxValue;
      mNullValue = nullValue;
    }


    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.gui.dialog.FormattedInputHandler<Double>
    @Override
    public String format(final Object value)
    {
      if (value == null || value.equals(mNullValue)) {
        return "";
      } else {
        return value.toString();
      }
    }

    @Override
    public Double parse(final String text) throws ParseException
    {
      try {
        if (mNullValue != null && text.length() == 0) {
          return mNullValue;
        }
        final double value = Double.parseDouble(text);
        if (value < mMinValue) {
          final StringBuilder builder = new StringBuilder();
          builder.append("Value must be at least ");
          builder.append(mMinValue);
          builder.append('.');
          throw new ParseException(builder.toString(), 0);
        } else if (value > mMaxValue) {
          final StringBuilder builder = new StringBuilder();
          builder.append("Value must be less or equal to ");
          builder.append(mMaxValue);
          builder.append('.');
          throw new ParseException(builder.toString(), 0);
        }
        return value;
      } catch (final NumberFormatException exception) {
        final StringBuilder builder = new StringBuilder();
        if (mMaxValue == Double.POSITIVE_INFINITY) {
          if (mMinValue == 0) {
            builder.append("Please enter a non-negative number.");
          } else if (mMinValue == Double.NEGATIVE_INFINITY) {
            builder.append("Please enter a number.");
          } else {
            builder.append("Please enter a number which is at least ");
            builder.append(mMinValue);
            builder.append('.');
          }
        } else {
          if (mMinValue == 0) {
            builder.append("Please enter a non-negative number which is at most ");
            builder.append(mMaxValue);
            builder.append('.');
          } else if (mMaxValue == Double.NEGATIVE_INFINITY) {
            builder.append("Please enter a number which is at most ");
            builder.append(mMaxValue);
            builder.append('.');
          } else {
            builder.append("Please enter a number between ");
            builder.append(mMinValue);
            builder.append(" and ");
            builder.append(mMaxValue);
            builder.append('.');
          }
        }
        throw new ParseException(builder.toString(), 0);
      }
    }

    @Override
    public DocumentFilter getDocumentFilter()
    {
      return this;
    }

    //#######################################################################
    //# Overrides for class javax.swing.DocumentFilter
    @Override
    public void insertString(final DocumentFilter.FilterBypass bypass,
                             final int offset,
                             final String text,
                             final AttributeSet attribs)
      throws BadLocationException
    {
      final String filtered = filter(text);
      if (filtered != null) {
        super.insertString(bypass, offset, filtered, attribs);
      }
    }

    @Override
    public void replace(final DocumentFilter.FilterBypass bypass,
                        final int offset,
                        final int length,
                        final String text,
                        final AttributeSet attribs)
      throws BadLocationException
    {
      final String filtered = filter(text);
      if (filtered != null) {
        super.replace(bypass, offset, length, filtered, attribs);
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private String filter(final String text)
    {
      if (text == null) {
        return null;
      } else {
        final int len = text.length();
        final StringBuilder builder = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
          final char ch = text.charAt(i);
          if (Character.isDigit(ch) ||
              ch == '-'  || ch == '.' || ch == 'e' || ch == 'E') {
            builder.append(ch);
          }
        }
        if (builder.length() == 0) {
          return null;
        } else {
          return builder.toString();
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final double mMinValue;
    private final double mMaxValue;
    private final Double mNullValue;
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -1315546517722080683L;

}
