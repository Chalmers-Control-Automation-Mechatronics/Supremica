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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.Option;


/**
 * <P>A command line argument to configure a Boolean option.</P>
 *
 * <P>Unlike multi-valued arguments, which typicalled are configured by
 * specifying <CODE>-<I>key</I>&nbsp;&lt;<I>value</I>&gt;</CODE>,
 * Boolean command options can be configured with different keys to
 * set a value of <CODE>true</CODE> or <CODE>false</CODE>. This is
 * implement by parsing and interpreting the option's command line
 * code ({@link Option#getCommandLineCode()}) in a special way.</P>
 *
 * <P>The option string is split into a sequence of keys separated by
 * vertical bar '<CODE>|</CODE>' characters. Each key starts with a
 * '<CODE>+</CODE>' or'<CODE>-</CODE>'. Keys of the form
 * <CODE>+<I>key</I></CODE> provide the means to set the option to
 * <CODE>true</CODE> by specifying <CODE>-<I>key</I></CODE> on the command
 * line. Keys of the form <CODE>-<I>key</I></CODE> provide the means to set
 * the option to <CODE>true</CODE> by specifying <CODE>-<I>key</I></CODE>,
 * and the means to set the option to <CODE>false</CODE> by specifying
 * <CODE>-n<I>key</I></CODE>.</P>
 *
 * <P>For example, the command line code &quot;+v|+verbose&quot; creates
 * keys <CODE>-v</CODE> and <CODE>+verbose</CODE> both setting an option to
 * <CODE>true</CODE>. And the command line code &quot;-opt&quot; creates
 * keys <CODE>-opt</CODE> setting an option to <CODE>true</CODE> and
 * <CODE>-nopt</CODE> setting it to <CODE>false</CODE>.</P>
 *
 * @author Benjamin Wheeler
 */

public class BooleanCommandLineArgument extends CommandLineArgument<Boolean>
{

  //#######################################################################
  //# Constructor
  /**
   * Creates a Boolean command line argument.
   * @param  option  The option to be configured by this command line
   *                 argument. It command line code ({@link
   *                 Option#getCommandLineCode()}) is interpreted to
   *                 generate one or more keys as explained in the
   *                 class documentation.
   * @see BooleanCommandLineArgument
   */
  public BooleanCommandLineArgument(final BooleanOption option)
  {
    super(option);
    final String commandLineCode = option.getCommandLineCode();
    if (commandLineCode.indexOf('|') > 0) {
      final String[] codes = commandLineCode.split("\\|");
      mKeyMap = new LinkedHashMap<>(2 * codes.length);
      for (final String code : codes) {
        register(code);
      }
    } else if (commandLineCode.startsWith("+")) {
      final String key = "-" + commandLineCode.substring(1);
      mKeyMap = Collections.singletonMap(key, true);
    } else {
      mKeyMap = new LinkedHashMap<>(2);
      register(commandLineCode);
    }
  }

  private void register(final String code)
  {
    if (code.startsWith("+")) {
      final String key = "-" + code.substring(1);
      mKeyMap.put(key, true);
    } else {
      mKeyMap.put(code, true);
      final String key = "-n" + code.substring(1);
      mKeyMap.put(key, false);
    }
  }


  //#######################################################################
  //# Simple Access
  @Override
  public String getCommandLineCode()
  {
    final StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (final String key : getKeys()) {
      if (first) {
        first = false;
      } else {
        builder.append('|');
      }
      builder.append(key);
    }
    return builder.toString();
  }

  @Override
  public Collection<String> getKeys()
  {
    return mKeyMap.keySet();
  }


  //#######################################################################
  //# Parsing
  @Override
  public void parse(final CommandLineOptionContext context,
                    final ListIterator<String> iter)
  {
    final String parsed = iter.previous();
    final boolean value = mKeyMap.get(parsed);
    getOption().setValue(value);
    iter.remove();
    setUsed(true);
  }


  //#######################################################################
  //# Printing
  @Override
  public void dump(final PrintStream stream)
  {
    super.dump(stream);
    if (mKeyMap.containsValue(false)) {
      doIndent(stream, INDENT);
      stream.print("(enable or disable, ");
      if (getOption().getDefaultValue()) {
        stream.print("en");
      } else {
        stream.print("dis");
      }
      stream.println("abled by default)");
    }
  }


  //#######################################################################
  //# Data Members
  private final Map<String,Boolean> mKeyMap;

}
