//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   UnaryExpressionProxy
//###########################################################################
//# $Id: UnaryExpressionProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.ObjectFactory;
import net.sourceforge.waters.xsd.module.SimpleExpressionType;
import net.sourceforge.waters.xsd.module.UnaryExpressionType;


public abstract class UnaryExpressionProxy extends SimpleExpressionProxy {

  //#########################################################################
  //# Constructors
  UnaryExpressionProxy(final UnaryExpressionType expr,
		       final UnaryOperator op,
		       final ProxyFactory factory)
    throws ModelException
  {
    super(expr);
    mOperator = op;
    mSubTerm = (SimpleExpressionProxy) factory.createProxy(expr.getSubTerm());
  }


  //#########################################################################
  //# Getters
  SimpleExpressionProxy getSubTerm()
  {
    return mSubTerm;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final UnaryExpressionProxy expr = (UnaryExpressionProxy) partner;
      return
	mOperator == expr.mOperator &&
	mSubTerm.equals(expr.mSubTerm);
    } else {
      return false;
    }    
  }

  public int hashCode()
  {
    return
      25 * super.hashCode() + 5 * mOperator.hashCode() + mSubTerm.hashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final Object partner)
  {
    if (partner instanceof UnaryExpressionProxy) {
      final UnaryExpressionProxy expr = (UnaryExpressionProxy) partner;
      final String name1 = mOperator.getName();
      final String name2 = expr.mOperator.getName();
      int result = name1.compareToIgnoreCase(name2);
      if (result != 0) {
	return result;
      }
      return mSubTerm.compareTo(expr.mSubTerm);
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
    final boolean needbraces =(priority < outerpri);
    if (needbraces) {
      printer.print('(');
    }
    printer.print(opname);
    mSubTerm.pprint(printer, priority, false);
    if (needbraces) {
      printer.print(')');
    }
  }


  //#########################################################################
  //# Marshalling
  public SimpleExpressionType createElement(final ObjectFactory factory)
    throws JAXBException
  {
    return factory.createUnaryExpression();
  }

  public ElementType toJAXB(final ElementFactory factory)
    throws JAXBException
  {
    final UnaryExpressionType expr =
      (UnaryExpressionType) factory.createElement(this);
    expr.setOperator(mOperator.getName());
    expr.setSubTerm((SimpleExpressionType) mSubTerm.toJAXB(factory));
    return expr;
  }


  //#########################################################################
  //# Evaluation
  public Value eval(final Context context)
    throws EvalException
  {
    final IntValue subvalue = getSubTerm().evalToInt(context);
    final int result = eval(subvalue.getValue());
    return new IntValue(result);
  }

  abstract int eval(int subvalue) throws EvalException;


  //#########################################################################
  //# Comparing
  int getOrderIndex()
  {
    return SimpleExpressionProxy.ORDERINDEX_UNARYEXPRESSION;
  }


  //#########################################################################
  //# Data Members
  private final UnaryOperator mOperator;
  private final SimpleExpressionProxy mSubTerm;

}
