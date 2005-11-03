//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ItemNotFoundException
//###########################################################################
//# $Id: ItemNotFoundException.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;


public class ItemNotFoundException extends ModelException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public ItemNotFoundException()
  {
  }

  /**
   * Constructs a new exception with the specified detail message.
   */
  public ItemNotFoundException(final String message)
  {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   */
  public ItemNotFoundException(final String message, final Throwable cause) 
  {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public ItemNotFoundException(final Throwable cause)
  {
    super(cause);
  }

}
