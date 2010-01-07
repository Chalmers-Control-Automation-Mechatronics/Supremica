//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbortException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;


/**
 * An exception indicating that an analysis algorithm has been aborted
 * in response to a user request.
 *
 * @author Robi Malik
 */

public class AbortException extends AnalysisException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new overflow exception with a default message.
   */
  public AbortException()
  {
    this("Analysis aborted!");
  }

  /**
   * Constructs a new overflow exception with a given message.
   */
  public AbortException(final String msg)
  {
    super(msg);
  }


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
