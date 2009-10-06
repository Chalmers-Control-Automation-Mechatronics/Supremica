//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   TypeMismatchException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


public class TypeMismatchException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public TypeMismatchException()
  {
  }

  /**
   * Constructs a new exception indicating that the given expression is not
   * well-typed.
   * @param  expr     The subterm that is not well-typed.
   * @param  typename The name of the expected type.
   */
  public TypeMismatchException(final Proxy expr, final String typename)
  {
    super("Expression '" + expr + "' is not of type " + typename + "!", expr);
  }

  /**
   * Constructs a new exception indicating that a parameter does not have
   * an expected type.
   * @param  binding  The binding of the parameter that causes the problem. 
   * @param  typename The name of the expected type.
   */
  public TypeMismatchException(final ParameterBindingProxy binding,
                               final String typename)
  {
    super("Parameter '" + binding.getName() +
          "' is not bound to a " + typename + "!",
          binding);
  }
  
  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
