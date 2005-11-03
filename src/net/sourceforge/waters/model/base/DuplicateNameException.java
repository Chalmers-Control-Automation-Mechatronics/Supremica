//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   DuplicateNameException
//###########################################################################
//# $Id: DuplicateNameException.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;


public class DuplicateNameException extends ModelException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public DuplicateNameException()
  {
  }

  /**
   * Constructs a new exception with the specified detail message.
   */
  public DuplicateNameException(final String message)
  {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   */
  public DuplicateNameException(final String message, final Throwable cause) 
  {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public DuplicateNameException(final Throwable cause)
  {
    super(cause);
  }

}
