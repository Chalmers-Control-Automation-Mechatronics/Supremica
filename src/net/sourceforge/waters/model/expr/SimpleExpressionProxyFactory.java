//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   SimpleExpressionProxyFactory
//###########################################################################
//# $Id: SimpleExpressionProxyFactory.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.List;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.BinaryExpressionType;
import net.sourceforge.waters.xsd.module.EnumSetExpressionType;
import net.sourceforge.waters.xsd.module.IndexedIdentifierType;
import net.sourceforge.waters.xsd.module.IntConstantType;
import net.sourceforge.waters.xsd.module.IntRangeExpressionType;
import net.sourceforge.waters.xsd.module.ObjectFactory;
import net.sourceforge.waters.xsd.module.SimpleExpressionType;
import net.sourceforge.waters.xsd.module.SimpleIdentifierType;
import net.sourceforge.waters.xsd.module.UnaryExpressionType;


/**
 * <P>A factory for unmarshalling expression objects.
 * The simple expression proxy factory provides a general means to create
 * all types of expressions from their XML objects.</P>
 *
 * <P>This class is used only internally for unmarshalling.</P>
 *
 * @author Robi Malik
 */

public class SimpleExpressionProxyFactory implements ProxyFactory
{

  //#########################################################################
  //# Interface waters.model.module.ProxyFactory
  public Proxy createProxy(final ElementType expr)
    throws ModelException
  {
    if (expr instanceof SimpleIdentifierType) {
      return new SimpleIdentifierProxy((SimpleIdentifierType) expr);
    } else if (expr instanceof IndexedIdentifierType) {
      final IndexedIdentifierType ident = (IndexedIdentifierType) expr;
      return new IndexedIdentifierProxy(ident, this);
    } else if (expr instanceof IntConstantType) {
      return new IntConstantProxy((IntConstantType) expr);
    } else if (expr instanceof UnaryExpressionType) {
      final UnaryExpressionType uexpr = (UnaryExpressionType) expr;
      final String opname = uexpr.getOperator();
      final UnaryOperator op = OperatorTable.findUnaryOperator(opname);
      return op.createProxy(uexpr, this);
    } else if (expr instanceof BinaryExpressionType) {
      final BinaryExpressionType bexpr = (BinaryExpressionType) expr;
      final String opname = bexpr.getOperator();
      final BinaryOperator op = OperatorTable.findBinaryOperator(opname);
      return op.createProxy(bexpr, this);
    } else if (expr instanceof IntRangeExpressionType) {
      final IntRangeExpressionType range = (IntRangeExpressionType) expr;
      return new IntRangeExpressionProxy(range, this);
    } else if (expr instanceof EnumSetExpressionType) {
      final EnumSetExpressionType range = (EnumSetExpressionType) expr;
      return new EnumSetExpressionProxy(range, this);
    } else {
      throw new ClassCastException
	("Unknown expression type " + expr.getClass().getName() + "!");
    }
  }

  public List getList(ElementType parent)
  {
    throw new UnsupportedOperationException
      ("No default list implemented in " + getClass().getName() + "!");
  }

}
