//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   PlusExpressionProxy
//###########################################################################
//# $Id: PlusExpressionProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.module.ExpressionType;
import net.sourceforge.waters.xsd.module.BinaryExpressionType;
import net.sourceforge.waters.xsd.module.ObjectFactory;
import net.sourceforge.waters.xsd.module.SimpleExpressionType;


/**
 * <P>An addition expression.
 * The addition operator in Waters is written as <CODE>+</CODE>. When
 * evaluated, an addition expression returns the sum of its two integer
 * arguments.</P>
 * 
 * <P>Examples:</P>
 * <UL>
 * <LI><CODE>1 + 2</CODE> returns <CODE>3</CODE>.
 * </UL>
 *
 * @author Robi Malik
 */

public class PlusExpressionProxy extends ArithmeticBinaryExpressionProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an addition expression.
   * @param  left        The left subterm for the new expression.
   * @param  right       The right subterm for the new expression.
   */
  public PlusExpressionProxy(final SimpleExpressionProxy left,
			     final SimpleExpressionProxy right)
  {
    super(getOperator(), left, right);
  }

  /**
   * Creates an addition expression from a parsed XML structure.
   * @param  expr        The parsed XML structure of the new expression.
   * @param  factory     The factory to be used for creating subterms.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  PlusExpressionProxy(final BinaryExpressionType expr,
		      final ProxyFactory factory)
    throws ModelException
  {
    super(expr, getOperator(), factory);
  }


  //#########################################################################
  //# Evaluation
  int eval(final int left, final int right)
  {
    return left + right;
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
      final BinaryExpressionType binary = (BinaryExpressionType) expr;
      return new PlusExpressionProxy(binary, factory);
    }

    public AbstractBinaryExpressionProxy createExpression
      (final SimpleExpressionProxy left,
       final SimpleExpressionProxy right)
    {
      return new PlusExpressionProxy(left, right);
    }

    public String getName()
    {
      return OperatorTable.OPNAME_PLUS;
    }

    public int getPriority()
    {
      return OperatorTable.PRIORITY_PLUS;
    }

    public int getAssociativity()
    {
      return OperatorTable.ASSOC_LEFT;
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
