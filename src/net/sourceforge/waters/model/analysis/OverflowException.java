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
    this(Kind.STATE, -1);
  }

  /**
   * Constructs an overflow exception with a message indicating the
   * number of states reached.
   */
  public OverflowException(final int limit)
  {
    this(Kind.STATE, limit);
  }

  /**
   * Constructs an overflow exception with a message indicating the
   * type of limit (states or transitions).
   */
  public OverflowException(final Kind kind)
  {
    this(kind, -1);
  }

  /**
   * Constructs an overflow exception with a message indicating the
   * number of states or transitions reached.
   */
  public OverflowException(final Kind kind, final int limit)
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


  //#########################################################################
  //# Overrides for Base Class java.lang.Exception
  public String getMessage()
  {
    if (mKind != null) {
      return mKind.getMessage(mLimit);
    } else {
      return super.getMessage();
    }
  }


  //#########################################################################
  //# Inner Class Kind
  public static enum Kind
  {
    STATE,
    TRANSITION;

    //#########################################################################
    //# Display
    private String getMessage(final int limit)
    {
      final StringBuffer buffer = new StringBuffer();
      final String name = toString();
      final int namelen = name.length();
      buffer.append(name.charAt(0));
      for (int i = 1; i < namelen; i++) {
        final char ch = name.charAt(i);
        buffer.append(Character.toLowerCase(ch));
      }
      buffer.append(" limit ");
      if (limit >= 0) {
        buffer.append("of ");
        buffer.append(limit);
        buffer.append(' ');
        for (int i = 0; i < namelen; i++) {
          final char ch = name.charAt(i);
          buffer.append(Character.toLowerCase(ch));
        }
        buffer.append("s ");
      }
      buffer.append("exceeded!");
      return buffer.toString();
    }
  }


  //#########################################################################
  //# Data Members
  private final Kind mKind;
  private final int mLimit;


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
