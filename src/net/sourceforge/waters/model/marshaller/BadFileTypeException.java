//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller;
//# CLASS:   BadFileTypeException
//###########################################################################
//# $Id: BadFileTypeException.java,v 1.4 2006-11-17 03:38:22 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.File;
import java.net.URI;


public class BadFileTypeException extends WatersUnmarshalException {

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
   * Constructs a new exception indicating that the type of the contents
   * of the given URI canbnot be determined.
   */
  public BadFileTypeException(final URI uri)
  {
    this("Can't determine contents type for '" + uri + "'!");
  }

  /**
   * Constructs a new exception indicating that the specified file
   * has an unknown type.
   */
  public BadFileTypeException(final File filename)
  {
    this("Can't determine contents type for file '" + filename + "'!");
  }

  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
