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

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.base.WatersException;


/**
 * Thrown when parsing of an expression string has failed.
 * These expection are thrown by the {@link ExpressionParser}.
 * In addition to an error message, they
 * contain information about the location of the error in the
 * string that was originally parsed.
 *
 * @author Robi Malik
 */

public class ParseException extends WatersException
{

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new parse exception with the specified detail message.
   * @param  message  The detail message for the new exception.
   * @param  position The position in the parsed string where the error
   *                  was detected.
   */
  public ParseException(final String message, final int position)
  {
    super(message);
    mPosition = position;
  }

  /**
   * Constructs a new parse exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   * @param  cause    Another exception that was the original cause of this
   *                  exception.
   * @param  position The position in the parsed string where the error
   *                  was detected.
   */
  public ParseException(final Throwable cause, final int position)
  {
    super(cause);
    mPosition = position;
  }

  /**
   * Constructs a new parse exception with the specified message and cause,
   * and the specified originating expression.
   * @param  message  The detail message for the new exception.
   * @param  cause    Another exception that was the original cause of this
   *                  exception.
   * @param  position The position in the parsed string where the error
   *                  was detected.
   */
  public ParseException(final String message,
			final Throwable cause,
			final int position)
  {
    super(message, cause);
    mPosition = position;
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the location of the error that caused this exception.
   * @return The index of the character in the input string, at which the
   *         error was detected. Indexes start at&nbsp;0, so a position
   *         of&nbsp;0 would indicate an error at the first character that
   *         was read.
   */
  public int getErrorOffset()
  {
    return mPosition;
  }


  //#########################################################################
  //# Rethrowing
  /**
   * Convert this exception to a {@link java.text.ParseException}.
   * @return A {@link java.text.ParseException} with the same error message
   *         and position as this exception, which has this exception as
   *         its cause.
   */
  public java.text.ParseException getJavaException()
  {
    final String msg = getMessage();
    final java.text.ParseException rethrown =
      new java.text.ParseException(msg, mPosition);
    rethrown.initCause(this);
    return rethrown;
  }


  //#########################################################################
  //# Data Members
  private final int mPosition;


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
