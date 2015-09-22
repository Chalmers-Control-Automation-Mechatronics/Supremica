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


/**
 * An exception indicating that an analysis algorithm has been aborted
 * because a set limit has been exceeded.
 *
 * @author Robi Malik
 */

public class OverflowException extends AnalysisException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new state limit overflow exception with a default message.
   */
  public OverflowException()
  {
    this(OverflowKind.STATE, -1);
  }

  /**
   * Constructs a state limit overflow exception with a message indicating the
   * number of states reached.
   */
  public OverflowException(final int limit)
  {
    this(OverflowKind.STATE, limit);
  }

  /**
   * Constructs an overflow exception with a message indicating the
   * type of limit (states or transitions).
   */
  public OverflowException(final OverflowKind kind)
  {
    this(kind, -1);
  }

  /**
   * Constructs an overflow exception with a message indicating the
   * type of limit and the number of states or transitions reached.
   */
  public OverflowException(final OverflowKind kind, final int limit)
  {
    mKind = kind;
    mLimit = limit;
  }

  /**
   * Constructs a new overflow exception with a given message.
   */
  public OverflowException(final String msg)
  {
    super(msg);
    mKind = null;
    mLimit = -1;
  }

  /**
   * Constructs a new exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public OverflowException(final Throwable cause)
  {
    super(cause);
    mKind = null;
    mLimit = -1;
  }

  /**
   * Constructs a new exception indicating out of memory.
   */
  public OverflowException(final OutOfMemoryError cause)
  {
    super(cause);
    mKind = OverflowKind.MEMORY;
    mLimit = -1;
  }

  /**
   * Constructs a new exception indicating stack overflow.
   */
  public OverflowException(final StackOverflowError cause)
  {
    super(cause);
    mKind = OverflowKind.STACK;
    mLimit = -1;
  }


  //#########################################################################
  //# Simple Access
  public OverflowKind getOverflowKind()
  {
    return mKind;
  }

  public int getLimit()
  {
    return mLimit;
  }


  //#########################################################################
  //# Overrides for Base Class java.lang.Exception
  @Override
  public String getMessage()
  {
    if (mKind != null) {
      return mKind.getMessage(mLimit);
    } else {
      return super.getMessage();
    }
  }


  //#########################################################################
  //# Data Members
  private final OverflowKind mKind;
  private final int mLimit;


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}








