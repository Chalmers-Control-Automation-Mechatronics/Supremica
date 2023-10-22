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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.expr.ParseException;


/**
 * A configurable parameter for a {@link ModelAnalyzer} whose value
 * is represented by a list of items from an enumerable type.
 *
 * @author Robi Malik
 */

public class EnumListOption<E> extends Option<List<E>>
{
  //#########################################################################
  //# Constructors
  public EnumListOption(final String id,
                        final String shortName,
                        final EnumFactory<E> factory)
  {
    this(id, shortName, null, null, factory);
  }

  public EnumListOption(final String id,
                        final String shortName,
                        final String description,
                        final String commandLineOption,
                        final EnumFactory<E> factory)
  {
    super(id, shortName, description, commandLineOption,
          Collections.singletonList(factory.getDefaultValue()));
    mEnumFactory = factory;
  }


  //#########################################################################
  //# Type-specific Access
  public List<E> getEnumConstants()
  {
    return mEnumFactory.getEnumConstants();
  }

  public void dumpEnumeration(final PrintStream stream,
                              final int indent)
  {
    dumpEnumeration(stream, indent, "values", true);
  }

  public void dumpEnumeration(final PrintStream stream,
                              final int indent,
                              final String choiceName,
                              final boolean showDefault)
  {
    mEnumFactory.dumpEnumeration(stream, indent, choiceName, showDefault);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.options.Option<E>
  @Override
  public OptionEditor<List<E>> createEditor(final OptionContext context)
  {
    return context.createEnumListEditor(this);
  }

  @Override
  public void set(final String text)
    throws ParseException
  {
    final String[] words = text.split(",");
    final List<E> values = new ArrayList<>(words.length);
    for (final String word : words) {
      final E value = mEnumFactory.getEnumValue(word);
      if (value != null) {
        values.add(value);
      } else {
        final String message =
          "Unsupported value '" + word + "' for " + getShortName() + ".";
        throw new ParseException(message, 0);
      }
    }
    setValue(values);
  }

  @Override
  public String getGuiName(final List<E> list)
  {
    if (list.isEmpty()) {
      return "";
    } else {
      final E first = list.get(0);
      return mEnumFactory.getConsoleName(first);
    }
  }

  @Override
  public String getConsoleName(final List<E> list)
  {
    final StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (final E value : list) {
      if (first) {
        first = false;
      } else {
        builder.append(',');
      }
      final String name = mEnumFactory.getConsoleName(value);
      builder.append(name);
    }
    return builder.toString();
  }


  //#########################################################################
  //# Data Members
  private final EnumFactory<E> mEnumFactory;

}
