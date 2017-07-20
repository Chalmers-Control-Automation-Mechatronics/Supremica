//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

import java.util.ListIterator;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;


/**
 * A floating point number command line argument passed to a
 * {@link ModelAnalyzerFactory}. Double command line arguments are specified on
 * the command line by their name followed by a floating point number, e.g.,
 * <CODE>-part 2.5</CODE>. The parsed value is stored in the
 * <CODE>CommandLineArgumentDouble</CODE> object for retrieval.
 *
 * @author Robi Malik
 */

public abstract class CommandLineArgumentDouble
  extends CommandLineArgument
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an optional command line argument of double type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-limit&quot;</CODE>.
   * @param  description   A textual description of the argument.
   */
  protected CommandLineArgumentDouble(final String name,
                                      final String description)
  {
    super(name, description);
  }

  /**
   * Creates an optional command line argument of double type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;-limit&quot;</CODE>.
   * @param  description   A textual description of the argument.
   * @param  value         Default value for argument.
   */
  protected CommandLineArgumentDouble(final String name,
                                      final String description,
                                      final double value)
  {
    super(name, description);
    mValue = value;
  }

  /**
   * Creates a command line argument of double type.
   * @param  name          The name of the argument,
   *                       for example <CODE>&quot;limit&quot;</CODE>.
   * @param  description   A textual description of the argument.
   * @param  required      A flag indicating whether this is a required
   *                       command line argument. The command line tool
   *                       will not accept command lines that fail to
   *                       specify all required arguments.
   */
  protected CommandLineArgumentDouble(final String name,
                                      final String description,
                                      final boolean required)
  {
    super(name, description, required);
  }


  //#######################################################################
  //# Simple Access
  @Override
  protected String getArgumentTemplate()
  {
    return "<n>";
  }

  protected double getValue()
  {
    return mValue;
  }


  //#######################################################################
  //# Parsing
  @Override
  public void parse(final ListIterator<String> iter)
  {
    iter.remove();
    if (iter.hasNext()) {
      final String value = iter.next();
      mValue = Double.parseDouble(value);
      iter.remove();
      setUsed(true);
    } else {
      failMissingValue();
    }
  }


  //#########################################################################
  //# Data Members
  private double mValue;

}
