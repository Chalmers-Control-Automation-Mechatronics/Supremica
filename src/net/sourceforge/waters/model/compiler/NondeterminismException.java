//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   NondeterminismException
//###########################################################################
//# $Id: NondeterminismException.java,v 1.1 2005-02-17 01:43:35 knut Exp $
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