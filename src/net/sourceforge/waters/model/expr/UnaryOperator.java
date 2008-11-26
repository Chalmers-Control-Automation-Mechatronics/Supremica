//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   UnaryOperator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.expr;


import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>A unary operator.</P>
 *
 * @see Operator
 *
 * @author Robi Malik
 */

public interface UnaryOperator extends Operator {

  //#########################################################################
  //# Simple Access Methods
  /**
   * Returns whether this operator is to be parsed as a prefix or postfix
   * operator.
   * @return <CODE>true</CODE> for prefix operators,
   *         <CODE>false</CODE> for postfix operators.
   */
  public boolean isPrefix();

  /**
   * Gets the type mask for acceptable argument types of this operator.
   * @see ExpressionParser
   */
  public int getArgTypes();


  //#########################################################################
  //# Evaluation
  public int getReturnTypes(int argType);

  public SimpleExpressionProxy simplify
    (UnaryExpressionProxy expr,
     AbstractSimpleExpressionSimplifier simplifier)
    throws EvalException;

  public Value eval(Value arg) throws EvalException;

}
