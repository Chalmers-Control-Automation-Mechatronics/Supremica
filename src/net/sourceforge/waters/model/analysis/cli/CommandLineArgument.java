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

package net.sourceforge.waters.model.analysis.cli;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.options.Configurable;

/**
 *
 * @author Benjamin Wheeler
 */
public abstract class CommandLineArgument
  implements Comparable<CommandLineArgument>
{

  //#########################################################################
  //# Interface java.util.Comparable<CommandLineArgument<?>>
  @Override
  public int compareTo(final CommandLineArgument arg)
  {
    final Iterator<String> iter1 = getKeys().iterator();
    final Iterator<String> iter2 = arg.getKeys().iterator();
    while (iter1.hasNext() && iter2.hasNext()) {
      final String key1 = iter1.next();
      final String key2 = iter2.next();
      final int result = key1.compareToIgnoreCase(key2);
      if (result != 0) {
        return result;
      }
    }
    if (iter1.hasNext()) {
      return 1;
    } else if (iter2.hasNext()) {
      return -1;
    } else {
      return 0;
    }
  }


  //#########################################################################
  //# Simple Access
  public boolean isUsed()
  {
    return mUsed;
  }

  protected void setUsed(final boolean used)
  {
    mUsed = used;
  }


  //#########################################################################
  //# Hooks
  public abstract String getCommandLineCode();

  public Collection<String> getKeys()
  {
    final String code = getCommandLineCode();
    return Collections.singletonList(code);
  }

  public abstract String getDescription();

  protected String getArgumentTemplate()
  {
    return null;
  }

  public void configure(final Configurable configurable)
  {
  }


  //#########################################################################
  //# Parsing
  public abstract void parse(CommandLineOptionContext context,
                             ListIterator<String> iter)
    throws AnalysisException;

  public void updateContext(final CommandLineOptionContext context)
    throws AnalysisException
  {
  }


  //#########################################################################
  //# Printing
  @Override
  public String toString()
  {
    return getCommandLineCode();
  }

  public void dump(final PrintStream stream)
  {
    final String name = getCommandLineCode();
    if (name.startsWith("@")) {
      return;
    }
    final String template = getArgumentTemplate();
    stream.print(name);
    int len = name.length();
    if (template != null) {
      stream.print(' ');
      stream.print(template);
      len += template.length() + 1;
    }

    final String description = getDescription();
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
    if (spaces > 0) {
      for (int i = 0; i < spaces; i++) {
        stream.print(' ');
      }
    } else {
      stream.print(' ');
    }
  }


  //#########################################################################
  //# Exception Handling
  public static void fail(final String msg)
  {
    System.err.println(msg);
    ExitException.testFriendlyExit(1);
  }

  protected void failMissingValue()
  {
    fail("No value specified for command line argument " +
         getCommandLineCode() + ".");
  }


  //#########################################################################
  //# Data Members
  private boolean mUsed;


  //#########################################################################
  //# Class Constants
  protected static final int INDENT = 20;
  protected static final int DUMP_WIDTH = 75;

}
