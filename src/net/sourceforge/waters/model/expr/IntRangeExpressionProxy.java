//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   IntRangeExpressionProxy
//###########################################################################
//# $Id: IntRangeExpressionProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.module.ExpressionType;
import net.sourceforge.waters.xsd.module.IntRangeExpressionType;
import net.sourceforge.waters.xsd.module.ObjectFactory;
import net.sourceforge.waters.xsd.module.SimpleExpressionType;


/**
 * <P>An expression that represents an integer range.
 * The range operator in Waters is written as <CODE>..</CODE>. 
 * An integer range expression takes two integers as arguments,
 * denoting the upper and lower bound of a range. When
 * evaluated, it produces the range of all numbers between the
 * upper and lower bounds, inclusively.</P>
 * 
 * <P>Examples:</P>
 * <UL>
 * <LI><CODE>1 .. 2</CODE> returns {1,2};
 * <LI><CODE>-10 .. 10</CODE> returns {-10,-9,...,9,10}.
 * </UL>
 *
 * @author Robi Malik
 */

public class IntRangeExpressionProxy extends AbstractBinaryExpressionProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an integer range expression.
   * @param  left        The subterm representing the lower bound.
   * @param  right       The subterm representing the upper bound.
   */
  public IntRangeExpressionProxy(final SimpleExpressionProxy left,
				 final SimpleExpressionProxy right)
  {
    super(getOperator(), left, right);
  }

  /**
   * Creates an integer range expression from a parsed XML structure.
   * @param  expr        The parsed XML structure of the new expression.
   * @param  factory     The factory to be used for creating subterms.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  IntRangeExpressionProxy(final IntRangeExpressionType expr,
			  final ProxyFactory factory)
    throws ModelException
  {
    super(expr, getOperator(), factory);
  }


  //#########################################################################
  //# Marshalling
  public SimpleExpressionType createElement(final ObjectFactory factory)
    throws JAXBException
  {
    return factory.createIntRangeExpression();
  }

  SimpleExpressionType getLeft(final SimpleExpressionType expr)
  {
    final IntRangeExpressionType range = (IntRangeExpressionType) expr;
    return range.getLower();
  }

  SimpleExpressionType getRight(final SimpleExpressionType expr)
  {
    final IntRangeExpressionType range = (IntRangeExpressionType) expr;
    return range.getUpper();
  }

  void setLeft(final SimpleExpressionType expr,
	       final SimpleExpressionType left)
  {
    final IntRangeExpressionType range = (IntRangeExpressionType) expr;
    range.setLower(left);
  }

  void setRight(final SimpleExpressionType expr,
		final SimpleExpressionType right)
  {
    final IntRangeExpressionType range = (IntRangeExpressionType) expr;
    range.setUpper(right);
  }


  //#########################################################################
  //# Evaluation
  public Value eval(final Context context)
    throws EvalException
  {
    final IntValue lower = getLeft().evalToInt(context);
    final IntValue upper = getRight().evalToInt(context);
    return new IntRangeValue(lower.getValue(), upper.getValue());
  }

  public int getResultTypes()
  {
    return SimpleExpressionProxy.TYPE_RANGE;
  }


  //#########################################################################
  //# The Operator
  static BinaryOperator getOperator()
  {
    return sOperator;
  }

  private static final BinaryOperator sOperator = new Operator();


  //#########################################################################
  //# Local Class Operator
  private static class Operator implements BinaryOperator {

    public AbstractBinaryExpressionProxy createProxy(ExpressionType expr,
						     ProxyFactory factory)
      throws ModelException
    {
      final IntRangeExpressionType range = (IntRangeExpressionType) expr;
      return new IntRangeExpressionProxy(range, factory);
    }

    public AbstractBinaryExpressionProxy createExpression
      (final SimpleExpressionProxy left,
       final SimpleExpressionProxy right)
    {
      return new IntRangeExpressionProxy(left, right);
    }

    public String getName()
    {
      return OperatorTable.OPNAME_RANGE;
    }

    public int getPriority()
    {
      return OperatorTable.PRIORITY_RANGE;
    }

    public int getAssociativity()
    {
      return OperatorTable.ASSOC_NONE;
    }

    public int getLHSTypes()
    {
      return SimpleExpressionProxy.TYPE_INT;
    }

    public int getRHSTypes()
    {
      return SimpleExpressionProxy.TYPE_INT;
    }

  }

}
