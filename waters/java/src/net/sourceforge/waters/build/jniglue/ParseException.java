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

package net.sourceforge.waters.build.jniglue;


/**
 * Thrown when parsing of a glue file has failed.
 * These exceptions are thrown by the {@link GlueFileParser} and
 * {@link GlueFileScanner}. In addition to an error message, they
 * contain information about the location of the error in the
 * file that was parsed.
 *
 * @author Robi Malik
 */

class ParseException extends Exception
{

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new parse exception with the specified detail message.
   * @param  message  The detail message for the new exception.
   * @param  lineno   The line number in the input file where the error
   *                  was detected.
   */
  ParseException(final String message, final int lineno)
  {
    super(message);
    mLineNo = lineno;
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the location of the error that caused this exception.
   * @return The line number in the input file where the
   *         error was detected. The first line is numbered&nbsp;1.
   */
  int getLineNo()
  {
    return mLineNo;
  }


  //#########################################################################
  //# Data Members
  public static final long serialVersionUID = 1;

  private final int mLineNo;

}
