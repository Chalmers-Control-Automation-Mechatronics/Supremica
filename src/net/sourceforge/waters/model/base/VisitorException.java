//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   VisitorException
//###########################################################################
//# $Id: VisitorException.java,v 1.3 2006-11-17 03:38:22 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;


public class VisitorException extends WatersException {

  //#########################################################################
  //# Constructors
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
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
