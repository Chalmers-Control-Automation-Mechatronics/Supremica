//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   BinaryOperator
//###########################################################################
//# $Id: BinaryOperator.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.module.ExpressionType;


/**
 * <P>A binary operator.</P>
 *
 * @see Operator
 *
 * @author Robi Malik
 */

interface BinaryOperator extends Operator {

  public AbstractBinaryExpressionProxy createProxy(ExpressionType expr,
						   ProxyFactory factory)
    throws ModelException;

  public AbstractBinaryExpressionProxy createExpression
    (SimpleExpressionProxy left,
     SimpleExpressionProxy right);
  
  public int getAssociativity();

  public int getLHSTypes();

  public int getRHSTypes();

}
