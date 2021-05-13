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

package net.sourceforge.waters.model.analysis.cli;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.options.Configurable;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.EnumFactory;

/**
 *
 * @author Benjamin Wheeler
 */
public class EnumCommandLineArgument<E> extends CommandLineArgument<E>
{

  //#######################################################################
  //# Constructor
  public EnumCommandLineArgument(final EnumOption<E> option)
  {
    super(option);
    mEnumFactory = new JavaEnumFactory<E>(option);
  }


  //#######################################################################
  //# Simple Access
  @Override
  protected String getArgumentTemplate()
  {
    return "<value>";
  }


  //#########################################################################
  //# Parsing
  @Override
  public void parse(final CommandLineOptionContext context,
                    final Collection<Configurable> configurables,
                    final ListIterator<String> iter)
    throws AnalysisException
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
    final E value = mEnumFactory.getEnumValue(arg);
    if (value == null) {
      final String msg = getErrorMessage();
      System.err.println(msg);
      mEnumFactory.dumpEnumeration(System.err, 0);
      System.exit(1);
    }
    else getOption().setValue(value);
  }


  //#########################################################################
  //# Printing
  @Override
  public void dump(final PrintStream stream)
  {
    super.dump(stream);
    mEnumFactory.dumpEnumeration(stream, INDENT);
  }

  protected String getErrorMessage()
  {
    return "Bad value for " + getCommandLineCode() + " option!";
  }


  //#########################################################################
  //# Static Enum Parsing
  public static <E extends Enum<E>> E parse(final CommandLineOptionContext context,
                                            final Class<E> eclass,
                                            final String name,
                                            final String value)
  {
    final EnumOption<E> option =
      new EnumOption<E>(null, null, null, null, eclass.getEnumConstants());
    final EnumCommandLineArgument<E> parser =
      new EnumCommandLineArgument<E>(option) {
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
  private static class JavaEnumFactory<E>
    extends EnumFactory<E>
  {
    //#######################################################################
    //# Constructor
    private JavaEnumFactory(final EnumOption<E> enumOption)
    {
      mEnumOption = enumOption;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.ExtensibleEnumFactory
    @Override
    public List<? extends E> getEnumConstants()
    {
      return mEnumOption.getEnumConstants();
    }

    @Override
    public String getConsoleName(final E item)
    {
      return item.toString();
    }

    //#######################################################################
    //# Data Members
    private final EnumOption<E> mEnumOption;
  }


  //#########################################################################
  //# Data Members
  private final EnumFactory<E> mEnumFactory;

}
