//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AnalysisException
//###########################################################################
//# $Id: AnalysisException.java,v 1.2 2006-11-17 03:38:22 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.base.WatersException;


/**
 * The superclass of al exceptions thrown by analysis algorithms.
 *
 * @author Robi Malik
 */

public class AnalysisException extends WatersException
{

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public AnalysisException()
  {
  }

  /**
   * Constructs a new exception with the specified detail message.
   */
  public AnalysisException(final String message)
  {
    super(message);
  }

  /**
   * Constructs a new exception with the specified cause. The detail
   * message will be <CODE>(cause==null ? null : cause.toString())</CODE>
   * (which typically contains the class and detail message of cause).
   */
  public AnalysisException(final Throwable cause)
  {
    super(cause);
  }

  /**
   * Constructs a new exception with the specified message and cause.
   */
  public AnalysisException(final String message,
			   final Throwable cause)
  {
    super(message, cause);
  }

  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
