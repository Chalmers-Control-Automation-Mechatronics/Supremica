//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   EqualsExpressionProxy
//###########################################################################
//# $Id: EqualsExpressionProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
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
 * <P>An equality expression.
 * The equality operator in Waters is written as <CODE>==</CODE>. When
 * evaluated, an equality expression compares its two arguments and return
 * <CODE>1</CODE> if they are equal, and <CODE>0</CODE> if they are
 * not.</P>
 * 
 * <P>Examples:</P>
 * <UL>
 * <LI><CODE>1 == 2</CODE> returns <CODE>0</CODE>;
 * <LI><CODE>-10 == 10 - 20</CODE> returns <CODE>1</CODE>.
 * </UL>
 *
 * @author Robi Malik
 */

public class EqualsExpressionProxy extends BinaryExpressionProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an equality expression.
   * @param  left        The left subterm for the new expression.
   * @param  right       The right subterm for the new expression.
   */
  public EqualsExpressionProxy(final SimpleExpressionProxy left,
			       final SimpleExpressionProxy right)
  {
    super(getOperator(), left, right);
  }

  /**
   * Creates an equality expression from a parsed XML structure.
   * @param  expr        The parsed XML structure of the new expression.
   * @param  factory     The factory to be used for creating subterms.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  EqualsExpressionProxy(final BinaryExpressionType expr,
			final ProxyFactory factory)
    throws ModelException
  {
    super(expr, getOperator(), factory);
  }


  //#########################################################################
  //# Evaluation
  public Value eval(final Context context)
    throws EvalException
  {
    final Value left = getLeft().eval(context);
    final Value right = getRight().eval(context);
    final int result = left.equals(right) ? 1 : 0;
    return new IntValue(result);
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
      return new EqualsExpressionProxy(binary, factory);
    }

    public AbstractBinaryExpressionProxy createExpression
      (final SimpleExpressionProxy left,
       final SimpleExpressionProxy right)
    {
      return new EqualsExpressionProxy(left, right);
    }

    public String getName()
    {
      return OperatorTable.OPNAME_EQUALS;
    }

    public int getPriority()
    {
      return OperatorTable.PRIORITY_EQU;
    }

    public int getAssociativity()
    {
      return OperatorTable.ASSOC_NONE;
    }

    public int getLHSTypes()
    {
      return SimpleExpressionProxy.TYPE_INT | SimpleExpressionProxy.TYPE_ATOM;
    }

    public int getRHSTypes()
    {
      return SimpleExpressionProxy.TYPE_INT | SimpleExpressionProxy.TYPE_ATOM;
    }

  }

}
