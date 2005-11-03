//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   EvalException
//###########################################################################
//# $Id: EvalException.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersException;


public class EvalException extends WatersException
{

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public EvalException()
  {
    mLocation = null;
  }

  /**
   * Constructs a new exception with the specified detail message.
   */
  public EvalException(final String message)
  {
    super(message, null);
  }

  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message
   * and a specified originating expression.
   */
  public EvalException(final Proxy location)
  {
    mLocation = location;
  }

  /**
   * Constructs a new exception with the specified detail message
   * and originating expression.
   */
  public EvalException(final String message, final Proxy location)
  {
    super(message);
    mLocation = location;
  }

  /**
   * Constructs a new exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public EvalException(final Throwable cause)
  {
    this(cause, null);
  }

  /**
   * Constructs a new exception with the specified cause and originating
   * expression. The detail message will be <CODE>(cause==null ? null :
   * cause.toString())</CODE> (which typically contains the class and
   * detail message of cause), and the specified originating expression.
   */
  public EvalException(final Throwable cause, final Proxy location)
  {
    super(cause);
    mLocation = location;
  }

  /**
   * Constructs a new exception with the specified message and cause,
   * and the specified originating expression.
   */
  public EvalException(final String message,
		       final Throwable cause,
		       final Proxy location)
  {
    super(message, cause);
    mLocation = location;
  }


  //#########################################################################
  //# Getters and Setters
  public Proxy getLocation()
  {
    return mLocation;
  }

  public void provideLocation(final Proxy location)
  {
    if (mLocation == null) {
      mLocation = location;
    }
  }


  //#########################################################################
  //# Data Members
  private Proxy mLocation;

}
