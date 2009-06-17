//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   CommandLineArgumentEnum
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.PrintStream;
import java.util.EnumSet;
import java.util.Iterator;


/**
 * An enumeration-value command line argument passed to a
 * {@link ModelVerifierFactory}.
 * Enumeration command line arguments are specified on the command line by
 * their name followed by a string that represents of the enumeration
 * object to be selected. The command line argument knows the enumeration
 * class of the value type and uses it to convert the parsed text to an
 * appropriate object, which is stored in the
 * <CODE>CommandLineArgumentEnum</CODE> object for retrieval.
 *
 * @author Robi Malik
 */

public abstract class CommandLineArgumentEnum<E extends Enum<E>>
  extends CommandLineArgument
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an optional command line argument of enumeration type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-heuristic&quot;</CODE>.
   * @param  description   A textual description of the argument.
   * @param  eclass        The class of the enumeration type for the
   *                       argument values.
   */
  protected CommandLineArgumentEnum(final String name,
                                    final String description,
                                    final Class<E> eclass)
  {
    super(name, description);
    mEnumerationClass = eclass;
  }

  /**
   * Creates a command line argument of enumeration type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-heuristic&quot;</CODE>.
   * @param  description   A textual description of the argument.
   * @param  eclass        The class of the enumeration type for the
   *                       argument values.
   * @param  required      A flag indicating whether this is a required
   *                       command line argument. The command line tool
   *                       will not accept command lines that fail to
   *                       specify all required arguments.
   */
  protected CommandLineArgumentEnum(final String name,
                                    final String description,
                                    final Class<E> eclass,
                                    final boolean required)
  {
    super(name, description, required);
    mEnumerationClass = eclass;
  }


  //#######################################################################
  //# Simple Access
  protected String getArgumentTemplate()
  {
    return "<value>";
  }

  protected E getValue()
  {
    return mValue;
  }


  //#######################################################################
  //# Parsing
  protected void parse(final Iterator<String> iter)
  {
    if (iter.hasNext()) {
      try {
        final String value = iter.next();
        mValue = Enum.valueOf(mEnumerationClass, value);
      } catch (final IllegalArgumentException exception) {
        System.err.println("Bad value for " + getName() + " option!");
        dumpEnumeration(System.err, 0);
        System.exit(1);
      }
    } else {
      failMissingValue();
    }
  }


  //#########################################################################
  //# Printing
  protected void dump(final PrintStream stream)
  {
    super.dump(stream);
    dumpEnumeration(stream, INDENT);
  }

  protected void dumpEnumeration(final PrintStream stream, final int indent)
  {
    doIndent(stream, indent);
    stream.println("Possible values are:");
    final EnumSet<E> set = EnumSet.allOf(mEnumerationClass);
    int column = 0;
    boolean first = true;
    for (final E item : set) {
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
        doIndent(stream, indent);
        column = indent;
      }
      stream.print(label);
      column += len;
    }
    stream.println();
  }


  //#########################################################################
  //# Data Members
  private final Class<E> mEnumerationClass;
  private E mValue;

}
