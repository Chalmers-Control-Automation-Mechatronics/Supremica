//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   InvalidModelException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;


/**
 * An exception indicating failure of analysis because some aspect of
 * the input model does not satisfy the preconditions of an algorithm.
 *
 * @author Robi Malik
 */
public class InvalidModelException extends AnalysisException
{

  //#########################################################################
  //# Constructors
  public InvalidModelException()
  {
  }

  public InvalidModelException(final String message)
  {
    super(message);
  }

  public InvalidModelException(final Throwable cause)
  {
    super(cause);
  }

  public InvalidModelException(final String message, final Throwable cause)
  {
    super(message, cause);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
