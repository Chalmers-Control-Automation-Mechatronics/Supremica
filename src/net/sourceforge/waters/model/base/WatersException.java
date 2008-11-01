//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   WatersException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;


public class WatersException extends Exception {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public WatersException()
  {
  }

  /**
   * Constructs a new exception with the specified detail message.
   */
  public WatersException(final String message)
  {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   */
  public WatersException(final String message, final Throwable cause) 
  {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public WatersException(final Throwable cause)
  {
    super(cause);
  }

  
  //#########################################################################
  //# Rethrowing
  /**
   * Converts this exception to a runtime exception for rethrowing.
   * Creates and returns a new runtime exception with this exception as its
   * cause.
   */
  public RuntimeException getRuntimeException()
  {
    return new WatersRuntimeException(this);
  }


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
