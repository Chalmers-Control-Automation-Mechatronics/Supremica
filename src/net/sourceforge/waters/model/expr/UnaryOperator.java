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
  public boolean isPrefix();

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
