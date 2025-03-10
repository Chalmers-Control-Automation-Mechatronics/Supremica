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

package net.sourceforge.waters.model.expr;

import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.Proxy;


public class EvalException extends AnalysisException
{

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public EvalException()
  {
    mLocation = null;
  }

  /**
   * Constructs a new exception with the specified detail message.
   */
  public EvalException(final String message)
  {
    super(message, null);
  }

  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message
   * and a specified originating expression.
   */
  public EvalException(final Proxy location)
  {
    mLocation = location;
  }

  /**
   * Constructs a new exception with the specified detail message
   * and originating expression.
   */
  public EvalException(final String message, final Proxy location)
  {
    super(message);
    mLocation = location;
  }

  /**
   * Constructs a new exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public EvalException(final Throwable cause)
  {
    this(cause, null);
  }

  /**
   * Constructs a new exception with the specified cause and originating
   * expression. The detail message will be <CODE>(cause==null ? null :
   * cause.toString())</CODE> (which typically contains the class and
   * detail message of cause), and the specified originating expression.
   */
  public EvalException(final Throwable cause, final Proxy location)
  {
    super(cause);
    mLocation = location;
  }

  /**
   * Constructs a new exception with the specified message and cause,
   * and the specified originating expression.
   */
  public EvalException(final String message,
		       final Throwable cause,
		       final Proxy location)
  {
    super(message, cause);
    mLocation = location;
  }


  //#########################################################################
  //# Getters and Setters
  public Proxy getLocation()
  {
    return mLocation;
  }

  public void replaceLocation(final Proxy location)
  {
    mLocation = location;
  }

  public void provideLocation(final Proxy location)
  {
    if (mLocation == null) {
      mLocation = location;
    }
  }


  //#########################################################################
  //# Support for Multi Exceptions
  @Override
  public List<? extends EvalException> getLeafExceptions()
  {
    return Collections.singletonList(this);
  }


  //#########################################################################
  //# Data Members
  private Proxy mLocation;


  //#########################################################################
  //# Static Class Variables
  private static final long serialVersionUID = -4434736943295920333L;

}
