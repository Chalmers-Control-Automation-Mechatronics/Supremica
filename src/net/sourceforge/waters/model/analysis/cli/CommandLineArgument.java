//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
import java.util.Collections;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.options.Configurable;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionEditor;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;

/**
 *
 * @author Benjamin Wheeler
 */
public abstract class CommandLineArgument<T> implements OptionEditor<T>,
  Comparable<CommandLineArgument<?>>
{

  //#########################################################################
  //# Constructors
  public CommandLineArgument(final CommandLineOptionContext context, final Option<T> option) {
    mOption = option;
    mName = option.getCommandLineOption();
  }

  //#########################################################################
  //# Interface java.util.Comparable
  @Override
  public int compareTo(final CommandLineArgument<?> arg)
  {
    return mName.compareTo(arg.getName());
  }

  //#########################################################################
  //# Simple Access
  public String getName()
  {
    return mName;
  }

  public Collection<String> getNames()
  {
    return Collections.singletonList(mName);
  }

  public boolean isRequired()
  {
    return mIsRequired;
  }

  public void setRequired(final boolean required) {
    mIsRequired = required;
  }

  public boolean isUsed()
  {
    return mIsUsed;
  }

  protected void setUsed(final boolean used) {
    mIsUsed = used;
  }

  protected String getArgumentTemplate()
  {
    return null;
  }

  @Override
  public Option<T> getOption()
  {
    return mOption;
  }

  public void setOption(final Configurable configurable) {
    if (mIsUsed) configurable.setOption(getOption());
  }

  public T getValue() {
    return getOption().getValue();
  }

  //#########################################################################
  //# Parsing
  public abstract void parse(final CommandLineOptionContext context,
                             final Collection<Configurable> configurables,
                             ListIterator<String> iter);

  public void postConfigure(final ModelAnalyzer analyzer)
    throws AnalysisException
  {
  }

  //#########################################################################
  //# Printing
  public void dump(final PrintStream stream)
  {
    final String name = getName();
    final String template = getArgumentTemplate();
    stream.print(name);
    int len = name.length();
    if (template != null) {
      stream.print(' ');
      stream.print(template);
      len += template.length() + 1;
    }

    final Option<T> option = getOption();
    final String description = option.getDescription();
    doIndent(stream, INDENT - len);
    int column = INDENT;
    boolean first = true;
    for (final String word : description.split(" ")) {
      final int wordLength = word.length();
      if (first) {
        first = false;
      } else {
        column++;
        if (column + wordLength > DUMP_WIDTH) {
          stream.println();
          column = 0;
        } else {
          stream.print(' ');
          column++;
        }
      }
      if (column == 0) {
        doIndent(stream, INDENT);
        column = INDENT;
      }
      stream.print(word);
      column += wordLength;
    }
    stream.println();
  }


  public static void doIndent(final PrintStream stream, final int spaces)
  {
    for (int i = 0; i < spaces; i++) {
      stream.print(' ');
    }
  }


  //#########################################################################
  //# Exception Handling
  public static void fail(final String msg)
  {
    System.err.println(msg);
    System.exit(1);
  }

  protected void failMissingValue()
  {
    fail("No value specified for command line argument "
      + mName + "!");
  }


  //#########################################################################
  //# Data Members
  private final Option<T> mOption;
  private final String mName;
  private boolean mIsUsed;
  private boolean mIsRequired;

  //#########################################################################
  //# Class Constants
  protected static final int INDENT = 20;
  protected static final int DUMP_WIDTH = 75;

}
