//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   BadFileTypeException
//###########################################################################
//# $Id: BadFileTypeException.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.io.File;


public class BadFileTypeException extends ModelException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public BadFileTypeException()
  {
  }

  /**
   * Constructs a new exception with the specified detail message.
   */
  public BadFileTypeException(final String message)
  {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   */
  public BadFileTypeException(final String message, final Throwable cause) 
  {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public BadFileTypeException(final Throwable cause)
  {
    super(cause);
  }

  /**
   * Constructs a new exception indicating that the specified file
   * has an unknown type.
   */
  public BadFileTypeException(final File filename)
  {
    this("Can't determine contents type for file '" + filename + "'!");
  }

}
