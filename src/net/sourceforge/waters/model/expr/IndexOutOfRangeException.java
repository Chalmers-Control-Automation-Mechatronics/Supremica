//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   IndexOutOfRangeException
//###########################################################################
//# $Id: IndexOutOfRangeException.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;


public class IndexOutOfRangeException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public IndexOutOfRangeException()
  {
  }

  /**
   * Constructs a new exception indicating that the given expression is not
   * well-typed.
   * @param  expr     The subterm that is not well-typed.
   * @param  subvalue The value to which that subterm has been evaluated.
   * @param  range    The type that was expected.
   */
  public IndexOutOfRangeException(final SimpleExpressionProxy expr,
				  final Value subvalue,
				  final RangeValue range)
  {
    super("Expression '" + expr +
	  "' (evaluated to " + subvalue + ") is not in range " +
	  range + "!", expr);
  }

}
