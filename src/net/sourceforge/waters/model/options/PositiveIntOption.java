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

package net.sourceforge.waters.model.options;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.expr.ParseException;


/**
 * A configurable parameter of a {@link ModelAnalyzer} representing a
 * positive integer. This parameter is represented as an <CODE>int</CODE>
 * value, with an allowable range from 0 to {@link Integer#MAX_VALUE}.
 * The maximum value is typically the default and represents an undefined
 * parameter value.
 *
 * @author Brandon Bassett, Robi Malik
 */

public class PositiveIntOption extends Option<Integer>
{
  //#########################################################################
  //# Constructors
  /**
   * Creates a positive integer parameter with {@link Integer#MAX_VALUE}
   * as its default.
   */
  public PositiveIntOption(final String id,
                           final String shortName,
                           final String description,
                           final String commandLineOption)
  {
    this(id, shortName, description, commandLineOption, Integer.MAX_VALUE);
  }

  /**
   * Creates a positive integer parameter with a specified default.
   */
  public PositiveIntOption(final String id,
                           final String shortName,
                           final String description,
                           final String commandLineOption,
                           final int defaultValue)
  {
    this(id, shortName, description, commandLineOption, defaultValue,
         0, Integer.MAX_VALUE);
  }

  /**
   * Creates a positive integer parameter with a specified default,
   * minimum, and maximum.
   */
  public PositiveIntOption(final String id,
                           final String shortName,
                           final String description,
                           final String commandLineOption,
                           final int defaultValue,
                           final int minValue,
                           final int maxValue)
  {
    super(id, shortName, description, commandLineOption, defaultValue);
    mMinValue = minValue;
    mMaxValue = maxValue;
  }

  /**
   * Creates a positive integer parameter with a specified default
   * and editable status.
   */
  public PositiveIntOption(final String id,
                           final String shortName,
                           final String description,
                           final String commandLineOption,
                           final int defaultValue,
                           final boolean editable)
  {
    this(id, shortName, description, commandLineOption, defaultValue);
    setEditable(editable);
  }


  //#########################################################################
  //# Type-specific Access
  public int getIntValue()
  {
    return getValue().intValue();
  }

  public int getMinValue()
  {
    return mMinValue;
  }

  public int getMaxValue()
  {
    return mMaxValue;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.options.Option
  @Override
  public OptionEditor<Integer> createEditor(final OptionContext context)
  {
    return context.createPositiveIntEditor(this);
  }

  @Override
  public void set(final String text) throws ParseException
  {
    try {
      final int value = Integer.parseUnsignedInt(text);
      if (value >= mMinValue && value <= mMaxValue) {
        setValue(value);
      } else {
        final String error = "Value "+ value +
          " is out of range; must be between " + mMinValue + " and " +
          mMaxValue;
        throw new ParseException(error, 0);
      }
    } catch (final NumberFormatException exception) {
      throw new ParseException(exception, 0);
    }
  }


  //#########################################################################
  //# Data Members
  private final int mMinValue;
  private final int mMaxValue;

}
