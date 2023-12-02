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

package net.sourceforge.waters.model.analysis;

import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.expr.MultiEvalException;


/**
 * The superclass of all exceptions thrown by analysis algorithms.
 *
 * @author Robi Malik
 */

public class AnalysisException extends WatersException
{

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public AnalysisException()
  {
  }

  /**
   * Constructs a new exception with the specified detail message.
   */
  public AnalysisException(final String message)
  {
    super(message);
  }

  /**
   * Constructs a new exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public AnalysisException(final Throwable cause)
  {
    super(cause);
  }

  /**
   * Constructs a new exception with the specified message and cause.
   */
  public AnalysisException(final String message,
                           final Throwable cause)
  {
    super(message, cause);
  }


  //#########################################################################
  //# Support for Multi Exceptions
  /**
   * Returns all the leaf exceptions associated with this instance.
   * The default implementation returns a singleton list containing
   * <CODE>this</CODE>, but a {@link MultiEvalException} may return a
   * list containing several exceptions.
   * @return The exceptions associated with this instance.
   */
  public List<? extends AnalysisException> getLeafExceptions()
  {
    return Collections.singletonList(this);
  }


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
