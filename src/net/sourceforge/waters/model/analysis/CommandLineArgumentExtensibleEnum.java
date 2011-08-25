//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractCommandLineArgumentEnum
//###########################################################################
//# $Id: CommandLineArgumentEnum.java 6060 2011-04-22 22:49:43Z robi $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.PrintStream;
import java.util.Iterator;


/**
 * <P>An enumeration-value command line argument passed to a
 * {@link ModelVerifierFactory}.</P>
 *
 * <P>Enumeration command line arguments are specified on the command line by
 * their name followed by a string that represents of the enumeration
 * object to be selected. The command line argument knows the enumeration
 * class of the value type and uses it to convert the parsed text to an
 * appropriate object, which is stored in the
 * <CODE>CommandLineArgumentEnum</CODE> object for retrieval.</P>
 *
 * <P>This abstract superclass is used to support extensible Java
 * enumerations through the {@link EnumFactory} interface.
 * Please use class {@link CommandLineArgumentEnum} instead for standard
 * Java enumerations.</P>
 *
 * @see EnumFactory
 * @see CommandLineArgumentEnum
 *
 * @author Robi Malik
 */

public class CommandLineArgumentExtensibleEnum<E>
  extends CommandLineArgument
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an optional command line argument of enumeration type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-heuristic&quot;</CODE>.
   * @param  description   A textual description of the argument.
   * @param  factory       The factory defining the enumeration values.
   */
  public CommandLineArgumentExtensibleEnum
    (final String name,
     final String description,
     final EnumFactory<? extends E> factory)
  {
    super(name, description);
    mEnumerationFactory = factory;
  }

  /**
   * Creates a command line argument of enumeration type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-heuristic&quot;</CODE>.
   * @param  description   A textual description of the argument.
   * @param  factory       The factory defining the enumeration values.
   * @param  required      A flag indicating whether this is a required
   *                       command line argument. The command line tool
   *                       will not accept command lines that fail to
   *                       specify all required arguments.
   */
  public CommandLineArgumentExtensibleEnum
    (final String name,
     final String description,
     final EnumFactory<? extends E> factory,
     final boolean required)
  {
    super(name, description, required);
    mEnumerationFactory = factory;
  }


  //#######################################################################
  //# Simple Access
  @Override
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
      final String name = iter.next();
      for (final E value : mEnumerationFactory.getEnumConstants()) {
        if (value.toString().equalsIgnoreCase(name)) {
          mValue = value;
          return;
        }
      }
      System.err.println("Bad value for " + getName() + " option!");
      dumpEnumeration(System.err, 0);
      System.exit(1);
    } else {
      failMissingValue();
    }
  }


  //#########################################################################
  //# Printing
  @Override
  protected void dump(final PrintStream stream)
  {
    super.dump(stream);
    dumpEnumeration(stream, INDENT);
  }

  protected void dumpEnumeration(final PrintStream stream, final int indent)
  {
    doIndent(stream, indent);
    stream.println("Possible values are:");
    int column = 0;
    boolean first = true;
    for (final E item : mEnumerationFactory.getEnumConstants()) {
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
  private final EnumFactory<? extends E> mEnumerationFactory;
  private E mValue;

}
