//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbortException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.expr.EvalException;


/**
 * An exception indicating that compilation has been aborted
 * in response to a user request or timeout.
 *
 * @author Robi Malik
 */

public class EvalAbortException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new overflow exception with a default message.
   */
  public EvalAbortException()
  {
    this("Compilation aborted!");
  }

  /**
   * Constructs a new overflow exception with a given message.
   */
  public EvalAbortException(final String msg)
  {
    super(msg);
  }


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
