//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   BinaryExpressionProxy
//###########################################################################
//# $Id: BinaryExpressionProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.module.BinaryExpressionType;
import net.sourceforge.waters.xsd.module.ObjectFactory;
import net.sourceforge.waters.xsd.module.SimpleExpressionType;


/**
 * A binary expression.
 * This class represents all those binary expressions that are represented
 * using a <CODE>&lt;BinaryExpression&gt;</CODE> element in the waters XML
 * files. It provides the functionality to unmarshal these expressions.
 *
 * @author Robi Malik
 */

public abstract class BinaryExpressionProxy
  extends AbstractBinaryExpressionProxy
{

  //#########################################################################
  //# Constructors
  BinaryExpressionProxy(final BinaryOperator op,
			final SimpleExpressionProxy left,
			final SimpleExpressionProxy right)
  {
    super(op, left, right);
  }

  BinaryExpressionProxy(final BinaryExpressionType expr,
			final BinaryOperator op,
			final ProxyFactory factory)
    throws ModelException
  {
    super(expr, op, factory);
  }


  //#########################################################################
  //# Marshalling
  public SimpleExpressionType createElement(final ObjectFactory factory)
    throws JAXBException
  {
    return factory.createBinaryExpression();
  }

  SimpleExpressionType getLeft(final SimpleExpressionType expr)
  {
    final BinaryExpressionType bexpr = (BinaryExpressionType) expr;
    return bexpr.getLeft();
  }

  SimpleExpressionType getRight(final SimpleExpressionType expr)
  {
    final BinaryExpressionType bexpr = (BinaryExpressionType) expr;
    return bexpr.getRight();
  }

  void setLeft(final SimpleExpressionType expr,
	       final SimpleExpressionType left)
  {
    final BinaryExpressionType bexpr = (BinaryExpressionType) expr;
    bexpr.setLeft(left);
  }

  void setRight(final SimpleExpressionType expr,
		final SimpleExpressionType right)
  {
    final BinaryExpressionType bexpr = (BinaryExpressionType) expr;
    bexpr.setRight(right);
  }

  void setOperator(final SimpleExpressionType expr, final BinaryOperator op)
  {
    final BinaryExpressionType bexpr = (BinaryExpressionType) expr;
    bexpr.setOperator(op.getName());    
  }


  //#########################################################################
  //# Evaluation
  public int getResultTypes()
  {
    return SimpleExpressionProxy.TYPE_INT;
  }

}
