//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   UnexpectedWatersException
//###########################################################################
//# $Id: UnexpectedWatersException.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.base;


public class UnexpectedWatersException extends RuntimeException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public UnexpectedWatersException()
  {
  }

  /**
   * Constructs a new exception with the specified detail message.
   */
  public UnexpectedWatersException(final String message)
  {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   */
  public UnexpectedWatersException(final String message,
				   final Throwable cause) 
  {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public UnexpectedWatersException(final Throwable cause)
  {
    super(cause);
  }

}
