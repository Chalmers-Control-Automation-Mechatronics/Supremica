//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   DivisionByZeroException
//###########################################################################
//# $Id: DivisionByZeroException.java,v 1.2 2006-11-17 03:38:22 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;


public class DivisionByZeroException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with default detail message.
   */
  public DivisionByZeroException()
  {
    super("Division by zero in expression!");
  }

  /**
   * Constructs a new exception with default detail message.
   * @param  expr     The subterm that causes the error.
   */
  public DivisionByZeroException(final Proxy expr)
  {
    super("Division by zero in expression!", expr);
  }

  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
