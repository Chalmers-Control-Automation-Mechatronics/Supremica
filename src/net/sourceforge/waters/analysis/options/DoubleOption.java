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

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;


/**
 * A configurable parameter of a {@link ModelAnalyzer} representing a
 * double. This parameter is represented as a <CODE>double</CODE>
 * value.
 *
 * @author Brandon Bassett, Robi Malik, Benjamin Wheeler
 */

public class DoubleOption extends Option<Double>
{
  //#########################################################################
  //# Constructors
  /**
   * Creates a double parameter with {@link Double#POSITIVE_INFINITY}
   * as its default.
   */
  public DoubleOption(final String id,
                      final String shortName,
                      final String description,
                      final String commandLineOption)
  {
    this(id, shortName, description, commandLineOption,
         Double.POSITIVE_INFINITY);
  }

  /**
   * Creates a double parameter with a specified default.
   */
  public DoubleOption(final String id,
                      final String shortName,
                      final String description,
                      final String commandLineOption,
                      final double defaultValue)
  {
    this(id, shortName, description, commandLineOption,
         Double.POSITIVE_INFINITY,
         Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
  }

  /**
   * Creates a double parameter with a specified default and range.
   */
  public DoubleOption(final String id,
                      final String shortName,
                      final String description,
                      final String commandLineOption,
                      final double defaultValue,
                      final double minValue,
                      final double maxValue)
  {
    super(id, shortName, description, commandLineOption, defaultValue);
    mMinValue = minValue;
    mMaxValue = maxValue;
  }


  //#########################################################################
  //# Type-specific Access
  public double getDoubleValue()
  {
    return getValue().doubleValue();
  }

  public double getMin()
  {
    return mMinValue;
  }

  public double getMax()
  {
    return mMaxValue;
  }

  @Override
  public void set(final String text)
  {
    try {
      final double value = Double.parseDouble(text);
      this.setValue(value);
    } catch(final NumberFormatException e) {
      throw new IllegalArgumentException(e);
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.options.Option
  @Override
  public OptionEditor<Double> createEditor(final OptionContext context)
  {
    return context.createDoubleEditor(this);
  }


  //#########################################################################
  //# Data Members
  private final double mMinValue;
  private final double mMaxValue;


}
