//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   OverflowException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;


/**
 * An exception indicating that an analysis algorithm has been aborted
 * because the set state limit has been exceeded.
 *
 * @author Robi Malik
 */

public class OverflowException extends AnalysisException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new overflow exception with a default message.
   */
  public OverflowException()
  {
    this("State limit exceeded!");
  }

  /**
   * Constructs a new overflow exception with a given message.
   */
  public OverflowException(final String msg)
  {
    super(msg);
    mLimit = -1;
  }

  /**
   * Constructs an overflow exception with a message indicating the
   * number of states reached.
   */
  public OverflowException(final int limit)
  {
    mLimit = limit;
  }

  /**
   * Constructs a new exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public OverflowException(final Throwable cause)
  {
    super(cause);
    mLimit = -1;
  }


  //#########################################################################
  //# Overrides for Baseclass java.lang.Exception
  public String getMessage()
  {
    if (mLimit >= 0) {
      return "State limit of " + mLimit + " states exceeded!";
    } else {
      return super.getMessage();
    }
  }


  //#########################################################################
  //# Data Members
  private final int mLimit;
  
  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
