//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   TypeMismatchException
//###########################################################################
//# $Id: TypeMismatchException.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.base.Proxy;


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
   * @param  mask     A bit mask identifying the expected type,
   *                  using the type constants defined in class
   *                  {@link SimpleExpressionProxy}.
   */
  public TypeMismatchException(final Proxy expr, final int mask)
  {
    this(expr, SimpleExpressionProxy.getTypeName(mask));
  }

  /**
   * Constructs a new exception indicating that the given expression is not
   * well-typed.
   * @param  expr     The subterm that is not well-typed.
   * @param  typename The name of the expected type.
   */
  public TypeMismatchException(final Proxy expr, final String typename)
  {
    super("Expression '" + expr + "' is not of type" + typename + "!", expr);
  }

  /**
   * Constructs a new exception indicating that the given expression is not
   * well-typed.
   * @param  expr     The subterm that is not well-typed.
   * @param  subvalue The value to which that subterm has been evaluated.
   * @param  mask     A bit mask identifying the expected type,
   *                  using the type constants defined in class
   *                  {@link SimpleExpressionProxy}.
   */
  public TypeMismatchException(final Proxy expr,
			       final Value subvalue,
			       final int mask)
  {
     this(expr, subvalue, SimpleExpressionProxy.getTypeName(mask));
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
	  "' (evaluated to " + subvalue + ") is not of type" +
	  typename + "!", expr);
  }

}
