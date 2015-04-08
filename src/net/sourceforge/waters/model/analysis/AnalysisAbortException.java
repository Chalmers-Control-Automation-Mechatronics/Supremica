//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AnalysisAbortException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;


/**
 * An exception indicating that an analysis algorithm has been aborted
 * in response to a user request or timeout.
 *
 * @author Robi Malik
 */

public class AnalysisAbortException extends AnalysisException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new overflow exception with a default message.
   */
  public AnalysisAbortException()
  {
    this("Analysis aborted!");
  }

  /**
   * Constructs a new overflow exception with a given message.
   */
  public AnalysisAbortException(final String msg)
  {
    super(msg);
  }


  //#########################################################################
  //# Static Class Variables
  private static final long serialVersionUID = -2601097829012752986L;

}
