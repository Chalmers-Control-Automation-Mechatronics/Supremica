//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   DuplicateNameException
//###########################################################################
//# $Id: DuplicateNameException.java,v 1.1 2005-02-17 01:43:35 knut Exp $
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
