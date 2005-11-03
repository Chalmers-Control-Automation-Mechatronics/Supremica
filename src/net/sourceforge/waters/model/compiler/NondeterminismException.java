//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   NondeterminismException
//###########################################################################
//# $Id: NondeterminismException.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;


public class NondeterminismException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public NondeterminismException()
  {
  }

  /**
   * Constructs a new exception with the specified detail message.
   */
  public NondeterminismException(final String message)
  {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message
   * and originating expression.
   */
  public NondeterminismException(final String message, final Proxy location)
  {
    super(message, location);
  }

}