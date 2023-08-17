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

package net.sourceforge.waters.model.analysis;

import java.io.PrintStream;
import java.util.List;

import net.sourceforge.waters.model.analysis.cli.OptionCommandLineArgument;

/**
 * An enumeration interface to support enumerated command line arguments.
 * There are different implementations of this interface to support standard
 * Java enumerations and user-defined extensible enumerations.
 *
 * @author Robi Malik
 */

public abstract class EnumFactory<E>
{

  //#######################################################################
  //# Access
  /**
   * Gets an immutable list the items in this enumeration.
   */
  public abstract List<E> getEnumConstants();

  /**
   * Gets the enumeration value corresponding to the given string.
   * String comparison is case-insensitive and uses the console name.
   * @return Enumeration value if present, or <CODE>null</CODE>.
   * @see #getConsoleName(Object) getConsoleName()
   */
  public E getEnumValue(final String name)
  {
    for (final E value : getEnumConstants()) {
      final String eName = getConsoleName(value);
      if (eName.equalsIgnoreCase(name)) {
        return value;
      }
    }
    return null;
  }

  /**
   * Gets the enumeration value corresponding to the given string.
   * String comparison is case-insensitive and uses {@link #toString()}.
   * This method is not normally used for parsing but subclasses can
   * call it for legacy processing.
   * @return Enumeration value if present, or <CODE>null</CODE>.
   * @see #getEnumValue(String)
   */
  protected E getEnumValueFallback(final String name)
  {
    for (final E value : getEnumConstants()) {
      final String eName = value.toString();
      if (eName.equalsIgnoreCase(name)) {
        return value;
      }
    }
    return null;
  }

  /**
   * Gets the default value for this factory. The default implementation
   * returns the first element of the list of items.
   */
  public E getDefaultValue()
  {
    final List<? extends E> items = getEnumConstants();
    return items.get(0);
  }

  /**
   * Prints a list of the possible values of the underlying enumeration
   * to the given stream so as to provide a listing of possible values
   * for an error message.
   */
  public void dumpEnumeration(final PrintStream stream,
                              final int indent,
                              final String choiceName,
                              final boolean showDefault)
  {
    OptionCommandLineArgument.doIndent(stream, indent);
    stream.print("Possible ");
    stream.print(choiceName);
    stream.println(" are:");
    int column = 0;
    boolean first = true;
    for (final E item : getEnumConstants()) {
      if (!isDisplayedInConsole(item)) {
        continue;
      }
      String label = getConsoleName(item);
      if (showDefault && item == getDefaultValue()) {
        label += " (default)";
      }
      final int len = label.length();
      if (first) {
        first = false;
      } else {
        stream.print(',');
        column++;
        if (column + 1 + len > 75) {
          stream.println();
          column = 0;
        } else {
          stream.print(' ');
          column++;
        }
      }
      if (column == 0) {
        OptionCommandLineArgument.doIndent(stream, indent);
        column = indent;
      }
      stream.print(label);
      column += len;
    }
    stream.println();
  }

  /**
   * Gets the raw name of the given enumeration element, which can be
   * displayed in the console or used for parsing. The default
   * implementation simply calls {@link #toString()}, but this may be
   * overridden in subclasses that distinguish pretty GUI names from
   * console names.
   */
  public String getConsoleName(final E item)
  {
    return item.toString();
  }

  /**
   * Returns whether the given enumeration element will be displayed
   * when listing the enumeration by the {@link
   * #dumpEnumeration(PrintStream,int,String,boolean)
   * dumpEnumeration()} method.
   * @return  <CODE>true</CODE> by default but can be overriden.
   */
  public boolean isDisplayedInConsole(final E item)
  {
    return true;
  }

}
