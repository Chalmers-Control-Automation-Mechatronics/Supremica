//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   VisitorException
//###########################################################################
//# $Id: VisitorException.java,v 1.5 2007-12-04 03:22:55 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import net.sourceforge.waters.model.base.WatersRuntimeException;


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
