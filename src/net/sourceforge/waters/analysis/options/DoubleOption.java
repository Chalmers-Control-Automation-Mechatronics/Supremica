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
 * value, with an allowable range from 0 to {@link Double#MAX_VALUE}.
 * The maximum value is typically the default and represents an undefined
 * parameter value.
 *
 * @author Brandon Bassett, Robi Malik, Benjamin Wheeler
 */

public class DoubleOption extends Option<Double>
{
  //#########################################################################
  //# Constructors
  /**
   * Creates a double parameter with {@link Double#MAX_VALUE}
   * as its default.
   */
  public DoubleOption(final String id,
                           final String shortName,
                           final String description,
                           final String commandLineOption)
  {
    this(id, shortName, description, commandLineOption, Double.MAX_VALUE);
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
    super(id, shortName, description, commandLineOption, defaultValue);
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

  public void setValue(final double value)
  {
    super.setValue(value);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.options.Option
  @Override
  public OptionEditor<Double> createEditor(final OptionContext context)
  {
    return context.createDoubleEditor(this);
  }

  Double mMinValue = null;
  Double mMaxValue = null;

  public Double getMin() {
    return mMinValue;
  }

  public Double getMax() {
    return mMaxValue;
  }

}
