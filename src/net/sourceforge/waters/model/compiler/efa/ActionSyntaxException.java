//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   ActionSyntaxException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


public class ActionSyntaxException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception indicating that the given expression does
   * not form a valid action.
   */
  public ActionSyntaxException(final SimpleExpressionProxy expr)
  {
    super("Expression '" + expr + "' does not form a valid action!", expr);
  }

  /**
   * Constructs a new exception indicating that an assignment operator
   * was encountered inside a guard or an expression where it should not
   * occur.
   * @param  assignment  The offending assignment expression.
   * @param  where       A string indicating where the assignment was
   *                     found, either &quot;guard&quot; or
   *                     &quot;expression&quot;.
   */
  public ActionSyntaxException(final BinaryExpressionProxy assignment,
                               final String where)
  {
    super("Assignment operator " + assignment.getOperator().getName() +
          " encountered in " + where + "! Please use == instead.",
          assignment);
  }

  /**
   * Constructs a new exception indicating that the given expression does
   * not form a valid action, because it is attempting to assign to a
   * non-identifier.
   */
  public ActionSyntaxException(final SimpleExpressionProxy expr,
                               final SimpleExpressionProxy nonident)
  {
    super("Attempting to assign to non-identifier " +
          nonident + " in action!", nonident);
  }

  /**
   * Constructs a new exception with the given message and location.
   */
  public ActionSyntaxException(final String msg, final Proxy location)
  {
    super(msg, location);
  }


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}