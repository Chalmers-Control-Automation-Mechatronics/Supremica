//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   UnaryOperator
//###########################################################################
//# $Id: UnaryOperator.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.module.ExpressionType;


/**
 * <P>A unary operator.</P>
 *
 * @see Operator
 *
 * @author Robi Malik
 */

interface UnaryOperator extends Operator {

  public UnaryExpressionProxy createProxy(ExpressionType expr,
					  ProxyFactory factory)
    throws ModelException;

  public UnaryExpressionProxy createExpression(SimpleExpressionProxy subterm);

  public int getArgTypes();

}
