//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   BinaryExpressionProxy
//###########################################################################
//# $Id: ArithmeticBinaryExpressionProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.module.BinaryExpressionType;


/**
 * A binary expression operating on integers.
 * This class represents binary expressions that have two integer arguments
 * that produce another integer when evaluated, i.e., standard arithmetic
 * operations such as <CODE>+</CODE and&nbsp;<CODE>*</CODE>. It provides
 * the functionality to evaluate such expressions.
 *
 * @author Robi Malik
 */

public abstract class ArithmeticBinaryExpressionProxy
  extends BinaryExpressionProxy
{

  //#########################################################################
  //# Constructors
  ArithmeticBinaryExpressionProxy(final BinaryOperator op,
				  final SimpleExpressionProxy left,
				  final SimpleExpressionProxy right)
  {
    super(op, left, right);
  }

  ArithmeticBinaryExpressionProxy(final BinaryExpressionType expr,
				  final BinaryOperator op,
				  final ProxyFactory factory)
    throws ModelException
  {
    super(expr, op, factory);
  }


  //#########################################################################
  //# Evaluation
  public Value eval(final Context context)
    throws EvalException
  {
    final IntValue left = getLeft().evalToInt(context);
    final IntValue right = getRight().evalToInt(context);
    final int result = eval(left.getValue(), right.getValue());
    return new IntValue(result);
  }

  abstract int eval(int left, int right) throws EvalException;

}
