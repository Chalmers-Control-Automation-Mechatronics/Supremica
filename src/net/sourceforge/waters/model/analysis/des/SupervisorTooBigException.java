//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   OverflowException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;


/**
 * An exception indicating that supervisor synthesis was aborted because
 * a supervisor to be constructed was found too big. This exception is
 * very much like an {@link OverflowException}, which it encapsulates,
 * but it is typically not recoverable.
 *
 * @author Robi Malik
 */

public class SupervisorTooBigException extends AnalysisException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a supervisor-too-big exception based on the given
   * {@link OverflowException}.
   */
  public SupervisorTooBigException(final OverflowException exception)
  {
    super(exception);
  }

  //#########################################################################
  //# Overrides for Base Class java.lang.Exception
  @Override
  public String getMessage()
  {
    final Throwable cause = getCause();
    return "Can't construct supervisor. " + cause.getMessage();
  }


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
