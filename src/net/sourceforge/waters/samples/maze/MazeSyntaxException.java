//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   MazeSyntaxException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.samples.maze;

import java.io.IOException;


public class MazeSyntaxException extends IOException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public MazeSyntaxException()
  {
  }

  /**
   * Constructs a new exception with the specified detail message.
   */
  public MazeSyntaxException(final String message)
  {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   */
  public MazeSyntaxException(final String message,
			     final Throwable cause) 
  {
    super(message);
    initCause(cause);
  }

  /**
   * Constructs a new exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public MazeSyntaxException(final Throwable cause)
  {
    super(cause == null ? null : cause.toString());
    initCause(cause);
  }

  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
