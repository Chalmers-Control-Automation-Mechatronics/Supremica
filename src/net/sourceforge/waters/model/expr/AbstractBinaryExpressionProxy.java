//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   AbstractBinaryExpressionProxy
//###########################################################################
//# $Id: AbstractBinaryExpressionProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.SimpleExpressionType;


/**
 * The abstract base class for all binary expressions.
 * A binary expression has two arguments, <I>left</I> and <I>right</I>,
 * and an <I>operator</I> applied to them. Typical examples are:
 * <UL>
 * <LI><CODE>1 + 1</CODE></LI>
 * <LI><CODE>17 == 0</CODE></LI>
 * <LI><CODE>1 .. 100</CODE></LI>
 * </UL>
 * This abstract base class provides access to the three members.
 * It is extended in several ways to cater for different XML
 * representations and to facilitate different ways of evaluation.
 *
 * @author Robi Malik
 */

public abstract class AbstractBinaryExpressionProxy
  extends SimpleExpressionProxy
{

  //#########################################################################
  //# Constructors
  AbstractBinaryExpressionProxy(final BinaryOperator op,
				final SimpleExpressionProxy left,
				final SimpleExpressionProxy right)
  {
    mOperator = op;
    mLeft = left;
    mRight = right;
  }

  AbstractBinaryExpressionProxy(final SimpleExpressionType expr,
				final BinaryOperator op,
				final ProxyFactory factory)
    throws ModelException
  {
    super(expr);
    mOperator = op;
    mLeft = (SimpleExpressionProxy) factory.createProxy(getLeft(expr));
    mRight = (SimpleExpressionProxy) factory.createProxy(getRight(expr));
  }


  //#########################################################################
  //# Getters
  /**
   * Gets the left subterm of this expression.
   */
  public SimpleExpressionProxy getLeft()
  {
    return mLeft;
  }

  /**
   * Gets the right subterm of this expression.
   */
  public SimpleExpressionProxy getRight()
  {
    return mRight;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final AbstractBinaryExpressionProxy expr =
	(AbstractBinaryExpressionProxy) partner;
      return
	mOperator == expr.mOperator &&
	mLeft.equals(expr.mLeft) &&
	mRight.equals(expr.mRight);
    } else {
      return false;
    }    
  }

  public int hashCode()
  {
    return
      125 * super.hashCode() +
      25 * mOperator.hashCode() +
      5 * mLeft.hashCode() +
      mRight.hashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final Object partner)
  {
    if (partner instanceof AbstractBinaryExpressionProxy) {
      final AbstractBinaryExpressionProxy expr =
	(AbstractBinaryExpressionProxy) partner;
      int result = mLeft.compareTo(expr.mLeft);
      if (result != 0) {
	return result;
      }
      final String name1 = mOperator.getName();
      final String name2 = expr.mOperator.getName();
      result = name1.compareToIgnoreCase(name2);
      if (result != 0) {
	return result;
      }
      return mRight.compareTo(expr.mRight);
    } else {
      return super.compareTo(partner);
    }
  }


  //#########################################################################
  //# Printing
  void pprint(final ModelPrinter printer,
	      final int outerpri,
	      final boolean assocbraces)
    throws IOException
  {
    final String opname = mOperator.getName();
    final int priority = mOperator.getPriority();
    final int associativity = mOperator.getAssociativity();
    final boolean needbraces =
      (priority < outerpri) || (priority == outerpri) && assocbraces;
    if (needbraces) {
      printer.print('(');
    }
    mLeft.pprint
      (printer, priority, associativity == OperatorTable.ASSOC_RIGHT);
    printer.print(opname);
    mRight.pprint
      (printer, priority, associativity == OperatorTable.ASSOC_LEFT);
    if (needbraces) {
      printer.print(')');
    }
  }


  //#########################################################################
  //# Marshalling
  public ElementType toJAXB(final ElementFactory factory)
    throws JAXBException
  {
    final SimpleExpressionType expr =
      (SimpleExpressionType) factory.createElement(this);
    setOperator(expr, mOperator);
    setLeft(expr, (SimpleExpressionType) mLeft.toJAXB(factory));
    setRight(expr, (SimpleExpressionType) mRight.toJAXB(factory));
    return expr;
  }


  //#########################################################################
  //# Comparing
  int getOrderIndex()
  {
    return SimpleExpressionProxy.ORDERINDEX_BINARYEXPRESSION;
  }


  //#########################################################################
  //# Provided by Subclasses
  abstract SimpleExpressionType getLeft(SimpleExpressionType expr);
  abstract SimpleExpressionType getRight(SimpleExpressionType expr);
  abstract void setLeft(SimpleExpressionType expr,
			SimpleExpressionType left);
  abstract void setRight(SimpleExpressionType expr,
			 SimpleExpressionType right);
  void setOperator(final SimpleExpressionType expr, final BinaryOperator op)
  {
  }


  //#########################################################################
  //# Data Members
  private final BinaryOperator mOperator;
  private final SimpleExpressionProxy mLeft;
  private final SimpleExpressionProxy mRight;

}
