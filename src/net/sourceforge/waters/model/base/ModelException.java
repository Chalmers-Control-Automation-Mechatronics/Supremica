//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ModelException
//###########################################################################
//# $Id: ModelException.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;


public class ModelException extends IllegalArgumentException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public ModelException()
  {
  }

  /**
   * Constructs a new exception with the specified detail message.
   */
  public ModelException(final String message)
  {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   */
  public ModelException(final String message, final Throwable cause) 
  {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public ModelException(final Throwable cause)
  {
    super(cause);
  }

}
