//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   UnknownOperatorException
//###########################################################################
//# $Id: UnknownOperatorException.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.base.ModelException;


public class UnknownOperatorException extends ModelException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public UnknownOperatorException()
  {
  }

  /**
   * Constructs a new exception with the specified detail message.
   */
  public UnknownOperatorException(final String message)
  {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   */
  public UnknownOperatorException(final String message, final Throwable cause) 
  {
    super(message, cause);
  }

  /**
   * Constructs a new exception with a specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public UnknownOperatorException(final Throwable cause)
  {
    super(cause);
  }

}
