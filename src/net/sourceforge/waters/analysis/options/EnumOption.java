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

package net.sourceforge.waters.analysis.options;

import java.util.Arrays;
import java.util.List;

import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;


/**
 * A configurable parameter of a {@link ModelAnalyzer} of
 * enumeration type.
 *
 * @author Brandon Bassett
 */

public class EnumOption<E> extends Option<E>
{
  //#########################################################################
  //# Constructors
  public EnumOption(final String id,
                    final String shortName,
                    final String description,
                    final String commandLineOption,
                    final EnumFactory<? extends E> factory)
  {
    super(id, shortName, description, commandLineOption,
          factory.getDefaultValue());
    mEnumConstants = factory.getEnumConstants();
  }

  public EnumOption(final String id,
                    final String name,
                    final List<E> enumConstants)
  {
    this(id, name, enumConstants, enumConstants.get(0));
  }

  public EnumOption(final String id,
                    final String name,
                    final List<E> enumConstants,
                    final E defaultValue)
  {
    super(id, name, null, null, defaultValue);
    assert enumConstants.contains(defaultValue);
    mEnumConstants = enumConstants;
  }

  public EnumOption(final String id,
                    final String shortName,
                    final String description,
                    final String commandLineOption,
                    final E[] enumConstants)
  {
    this(id, shortName, description, commandLineOption,
         enumConstants, enumConstants[0]);
  }

  public EnumOption(final String id,
                    final String shortName,
                    final String description,
                    final String commandLineOption,
                    final E[] enumConstants,
                    final E defaultValue)
  {
    super(id, shortName, description, commandLineOption, defaultValue);
    mEnumConstants = Arrays.asList(enumConstants);
  }

  public EnumOption(final EnumOption<E> template,
                    final EnumFactory<? extends E> factory)
  {
    super(template, factory.getDefaultValue());
    mEnumConstants = factory.getEnumConstants();
  }


  //#########################################################################
  //# Type-specific Access
  public List<? extends E> getEnumConstants()
  {
    return mEnumConstants;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.options.Option
  @Override
  public OptionEditor<E> createEditor(final OptionContext context)
  {
    return context.createEnumEditor(this);
  }

  @Override
  public void set(final String text)
  {
    final E value = parseObject(text);
    if (value != null) {
      setValue(value);
    }
    else {
      final String message = "Enum value '"+value+"' could not be found.";
      throw new IllegalArgumentException(message);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Parses a string into the enumeration type associated with this property.
   */
  private E parseObject(final String name)
  {
    for (final E value : mEnumConstants) {
      if (value.toString().equalsIgnoreCase(name)) {
        return value;
      }
    }
    return null;
  }


  //#########################################################################
  //# Data Members
  private final List<? extends E> mEnumConstants;

}
