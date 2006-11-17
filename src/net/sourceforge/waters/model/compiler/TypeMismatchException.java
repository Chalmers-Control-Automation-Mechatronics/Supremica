//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   TypeMismatchException
//###########################################################################
//# $Id: TypeMismatchException.java,v 1.3 2006-11-17 03:38:22 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.Value;


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
   * Constructs a new exception indicating that the given value is not
   * of an expected type.
   * @param  value    The value that is not well-types.
   * @param  typename The name of the expected type.
   */
  public TypeMismatchException(final Value value, final String typename)
  {
    super("Value '" + value + "' is not of type " + typename + "!");
  }

  /**
   * Constructs a new exception indicating that the given expression is not
   * well-typed.
   * @param  expr     The subterm that is not well-typed.
   * @param  subvalue The value to which that subterm has been evaluated.
   * @param  typename The name of the expected type.
   */
  public TypeMismatchException(final Proxy expr,
                               final Value subvalue,
                               final String typename)
  {
    super("Expression '" + expr +
          "' (evaluated to " + subvalue + ") is not of type " +
          typename + "!", expr);
  }
  
  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
