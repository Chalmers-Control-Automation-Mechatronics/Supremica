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

package net.sourceforge.waters.model.analysis;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;


/**
 * An enumeration-value command line argument passed to a
 * {@link ModelAnalyzerFactory}.
 * Enumeration command line arguments are specified on the command line by
 * their name followed by a string that represents one of the enumeration
 * objects to be selected. The command line argument knows the enumeration
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
    this(name, description, eclass, false);
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
    mEnumFactory = new JavaEnumFactory<E>(eclass);
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


  //#########################################################################
  //# Parsing
  @Override
  public void parse(final ListIterator<String> iter)
  {
    iter.remove();
    if (iter.hasNext()) {
      final String arg = iter.next();
      parse(arg);
      iter.remove();
      setUsed(true);
    } else {
      failMissingValue();
    }
  }

  public void parse(final String arg)
  {
    mValue = mEnumFactory.getEnumValue(arg);
    if (mValue == null) {
      final String msg = getErrorMessage();
      System.err.println(msg);
      mEnumFactory.dumpEnumeration(System.err, 0);
      System.exit(1);
    }
  }


  //#########################################################################
  //# Printing
  @Override
  public void dump(final PrintStream stream, final Object analyzer)
  {
    super.dump(stream, analyzer);
    mEnumFactory.dumpEnumeration(stream, INDENT);
  }

  protected String getErrorMessage()
  {
    return "Bad value for " + getName() + " option!";
  }


  //#########################################################################
  //# Static Enum Parsing
  public static <E extends Enum<E>> E parse(final Class<E> eclass,
                                            final String name,
                                            final String value)
  {
    final CommandLineArgumentEnum<E> parser =
      new CommandLineArgumentEnum<E>(name, name, eclass) {
      @Override
      protected String getErrorMessage()
      {
        return "Bad value for " + name + "!";
      }
    };
    parser.parse(value);
    return parser.getValue();
  }


  //#########################################################################
  //# Inner Class JavaEnumFactory
  private static class JavaEnumFactory<E extends Enum<E>>
    extends EnumFactory<E>
  {
    //#######################################################################
    //# Constructors
    private JavaEnumFactory(final Class<E> clazz)
    {
      mEnumerationClass = clazz;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.ExtensibleEnumFactory
    @Override
    public List<? extends E> getEnumConstants()
    {
      final E[] array = mEnumerationClass.getEnumConstants();
      return Arrays.asList(array);
    }

    @Override
    public String getConsoleName(final E item)
    {
      return item.name();
    }

    //#######################################################################
    //# Data Members
    private final Class<E> mEnumerationClass;

  }


  //#########################################################################
  //# Data Members
  private final EnumFactory<E> mEnumFactory;
  private E mValue;

}
