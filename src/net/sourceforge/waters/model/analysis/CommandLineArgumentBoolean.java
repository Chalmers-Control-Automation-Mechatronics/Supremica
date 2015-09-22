//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;


/**
 * An Boolean command line argument passed to a {@link ModelAnalyzerFactory}.
 * Boolean command line arguments are specified on the command line by their
 * name to specify a <I>true</I> value, or by their name preceded
 * with&nbsp;&quot;n&quot; to specify a <I>false</I> value. For example
 * <I>quot;failing events&quot;</I> might be enabled by&nbsp;<CODE>-fe</CODE>
 * and disabled by <CODE>-nfe</CODE>. The recognised value (<I>true</I>
 * or&nbsp;<I>false</I>)is stored in the <CODE>CommandLineArgumentBoolean</CODE>
 * object for retrieval.
 *
 * @author Robi Malik
 */

public abstract class CommandLineArgumentBoolean
  extends CommandLineArgument
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an optional command line argument of Boolean type with
   * with default value <CODE>false</CODE>
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-limit&quot;</CODE>.
   * @param  description   A textual description of the argument.
   */
  protected CommandLineArgumentBoolean(final String name,
                                       final String description)
  {
    this(name, description, false);
  }

  /**
   * Creates an optional command line argument of Boolean type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-limit&quot;</CODE>.
   * @param  description   A textual description of the argument.
   * @param  value         Default value for argument.
   */
  protected CommandLineArgumentBoolean(final String name,
                                       final String description,
                                       final boolean value)
  {
    super(name, description);
    mValue = value;
  }


  //#######################################################################
  //# Simple Access
  @Override
  public String getName()
  {
    return getOnName() + "|" + getOffName();
  }

  @Override
  public Collection<String> getNames()
  {
    final Collection<String> names = new ArrayList<>(2);
    names.add(getOnName());
    names.add(getOffName());
    return names;
  }

  protected String getOnName()
  {
    return super.getName();
  }

  protected String getOffName()
  {
    final String onName = getOnName();
    return "-n" + onName.substring(1);
  }

  protected boolean getValue()
  {
    return mValue;
  }


  //#######################################################################
  //# Parsing
  @Override
  public void parse(final ListIterator<String> iter)
  {
    final String parsed = iter.previous();
    final String onName = getOnName();
    mValue = parsed.equals(onName);
    iter.remove();
    setUsed(true);
  }


  //#########################################################################
  //# Data Members
  private boolean mValue;

}








