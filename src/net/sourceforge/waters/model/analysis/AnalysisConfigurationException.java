//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AnalysisConfigurationException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;


/**
 * An exception indicating that an analysis algorithm has been unable to
 * start because of invalid arguments.
 *
 * @author Robi Malik
 */

public class AnalysisConfigurationException extends AnalysisException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new overflow exception with a given message.
   */
  public AnalysisConfigurationException(final String msg)
  {
    super(msg);
  }


  //#########################################################################
  //# Static Class Variables
  private static final long serialVersionUID = -3131107766968296988L;

}
