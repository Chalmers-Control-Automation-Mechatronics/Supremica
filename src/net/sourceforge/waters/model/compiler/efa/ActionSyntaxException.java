//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   ActionSyntaxException
//###########################################################################
//# $Id: ActionSyntaxException.java,v 1.1 2008-06-29 04:01:44 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


public class ActionSyntaxException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public ActionSyntaxException()
  {
  }

  /**
   * Constructs a new exception indicating that the given expression does
   * not form a valid action.
   */
  public ActionSyntaxException(final SimpleExpressionProxy expr)
  {
    super("Expression '" + expr + "' does not form a valid action!", expr);
  }

  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}