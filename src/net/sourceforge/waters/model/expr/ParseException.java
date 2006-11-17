//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   ParseException
//###########################################################################
//# $Id: ParseException.java,v 1.5 2006-11-17 03:38:22 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.base.WatersException;


/**
 * Thrown when parsing of an expression string has failed.
 * These expection are thrown by the {@link ExpressionParser}.
 * In addition to an error message, they
 * contain information about the location of the error in the
 * string that was originally parsed.
 *
 * @author Robi Malik
 */

public class ParseException extends WatersException
{

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new parse exception with the specified detail message.
   * @param  message  The detail message for the new exception.
   * @param  position The position in the parsed string where the error
   *                  was detected.
   */
  public ParseException(final String message, final int position)
  {
    super(message);
    mPosition = position;
  }

  /**
   * Constructs a new parse exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   * @param  cause    Another exception that was the original cause of this
   *                  exception.
   * @param  position The position in the parsed string where the error
   *                  was detected.
   */
  public ParseException(final Throwable cause, final int position)
  {
    super(cause);
    mPosition = position;
  }

  /**
   * Constructs a new parse exception with the specified message and cause,
   * and the specified originating expression.
   * @param  message  The detail message for the new exception.
   * @param  cause    Another exception that was the original cause of this
   *                  exception.
   * @param  position The position in the parsed string where the error
   *                  was detected.
   */
  public ParseException(final String message,
			final Throwable cause,
			final int position)
  {
    super(message, cause);
    mPosition = position;
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the location of the error that caused this exception.
   * @return The index of the character in the input string, at which the
   *         error was detected. Indexes start at&nbsp;0, so a position
   *         of&nbsp;0 would indicate an error at the first character that
   *         was read.
   */
  public int getErrorOffset()
  {
    return mPosition;
  }


  //#########################################################################
  //# Rethrowing
  /**
   * Convert this exception to a {@link java.text.ParseException}.
   * @return A {@link java.text.ParseException} with the same error message
   *         and position as this exception, which has this exception as
   *         its cause.
   */
  public java.text.ParseException getJavaException()
  {
    final String msg = getMessage();
    final java.text.ParseException rethrown =
      new java.text.ParseException(msg, mPosition);
    rethrown.initCause(this);
    return rethrown;
  }


  //#########################################################################
  //# Data Members
  private final int mPosition;
  
  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
