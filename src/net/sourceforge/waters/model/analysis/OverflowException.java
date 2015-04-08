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
 * because a set limit has been exceeded.
 *
 * @author Robi Malik
 */

public class OverflowException extends AnalysisException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new state limit overflow exception with a default message.
   */
  public OverflowException()
  {
    this(OverflowKind.STATE, -1);
  }

  /**
   * Constructs a state limit overflow exception with a message indicating the
   * number of states reached.
   */
  public OverflowException(final int limit)
  {
    this(OverflowKind.STATE, limit);
  }

  /**
   * Constructs an overflow exception with a message indicating the
   * type of limit (states or transitions).
   */
  public OverflowException(final OverflowKind kind)
  {
    this(kind, -1);
  }

  /**
   * Constructs an overflow exception with a message indicating the
   * type of limit and the number of states or transitions reached.
   */
  public OverflowException(final OverflowKind kind, final int limit)
  {
    mKind = kind;
    mLimit = limit;
  }

  /**
   * Constructs a new overflow exception with a given message.
   */
  public OverflowException(final String msg)
  {
    super(msg);
    mKind = null;
    mLimit = -1;
  }

  /**
   * Constructs a new exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public OverflowException(final Throwable cause)
  {
    super(cause);
    mKind = null;
    mLimit = -1;
  }

  /**
   * Constructs a new exception indicating out of memory.
   */
  public OverflowException(final OutOfMemoryError cause)
  {
    super(cause);
    mKind = OverflowKind.MEMORY;
    mLimit = -1;
  }

  /**
   * Constructs a new exception indicating stack overflow.
   */
  public OverflowException(final StackOverflowError cause)
  {
    super(cause);
    mKind = OverflowKind.STACK;
    mLimit = -1;
  }


  //#########################################################################
  //# Simple Access
  public OverflowKind getOverflowKind()
  {
    return mKind;
  }

  public int getLimit()
  {
    return mLimit;
  }


  //#########################################################################
  //# Overrides for Base Class java.lang.Exception
  @Override
  public String getMessage()
  {
    if (mKind != null) {
      return mKind.getMessage(mLimit);
    } else {
      return super.getMessage();
    }
  }


  //#########################################################################
  //# Data Members
  private final OverflowKind mKind;
  private final int mLimit;


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
