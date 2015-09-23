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

package net.sourceforge.waters.model.base;


/**
 * The class of exceptions throw by WATERS visitors.
 * These exceptions typically are wrapped around another exception
 * that are unpacked by the visitor's entrance method.
 * The visitor and visitor exception interfaces provides support for the
 * wrapping and rethrowing of visitor exceptions.
 *
 * @see ProxyVisitor
 * @author Robi Malik
 */

public class VisitorException extends WatersException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a visitor exception without cause or message.
   */
  public VisitorException()
  {
  }

  /**
   * Constructs a new exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public VisitorException(final Throwable cause)
  {
    super(cause);
  }

  /**
   * Constructs a new exception with the specified message.
   */
  public VisitorException(final String msg)
  {
    super(msg);
  }


  //#########################################################################
  //# Rethrowing
  /**
   * Converts this visitor exception to a runtime exception for rethrowing.
   * This method either returns the cause of this exception, if the cause
   * is a runtime exception, or creates a new runtime exception with this
   * visitor exception's cause as its cause.
   */
  public RuntimeException getRuntimeException()
  {
    final Throwable cause = getCause();
    if (cause == null) {
      return new WatersRuntimeException(this);
    } else if (cause instanceof RuntimeException) {
      return (RuntimeException) cause;
    } else {
      return new WatersRuntimeException(cause);
    }
  }


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
