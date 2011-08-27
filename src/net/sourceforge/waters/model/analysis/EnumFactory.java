//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ExtensibleEnumFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.PrintStream;
import java.util.List;

/**
 * An enumeration interface to support enumerated command line arguments.
 * There are different implementations of this interface to support standard
 * Java enumerations and user-defined extensible enumerations.
 *
 * @author Robi Malik
 */

public abstract class EnumFactory<E>
{

  /**
   * Gets an immutable list the items in this enumeration.
   */
  public abstract List<? extends E> getEnumConstants();

  /**
   * Gets the enumeration value corresponding to the given string
   * (case-insensitive).
   * @return Enumeration value if present, or <CODE>null</CODE>
   */
  public E getEnumValue(final String name)
  {
    for (final E value : getEnumConstants()) {
      if (value.toString().equalsIgnoreCase(name)) {
        return value;
      }
    }
    return null;
  }

  public void dumpEnumeration(final PrintStream stream, final int indent)
  {
    CommandLineArgument.doIndent(stream, indent);
    stream.println("Possible values are:");
    int column = 0;
    boolean first = true;
    for (final E item : getEnumConstants()) {
      final String label = item.toString();
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
        CommandLineArgument.doIndent(stream, indent);
        column = indent;
      }
      stream.print(label);
      column += len;
    }
    stream.println();
  }

}
