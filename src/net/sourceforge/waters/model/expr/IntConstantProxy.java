//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   IntConstantProxy
//###########################################################################
//# $Id: IntConstantProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
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
import net.sourceforge.waters.xsd.module.IntConstantType;


/**
 * An expression representing an integer constant.
 *
 * @author Robi Malik
 */

public class IntConstantProxy extends SimpleExpressionProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an integer constant.
   * @param  value       The integer value of the new constant.
   */
  public IntConstantProxy(final int value)
  {
    mValue = value;
  }

  /**
   * Creates an integer constant from a parsed XML structure.
   * @param  expr        The parsed XML structure of the new expression.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  IntConstantProxy(final IntConstantType expr)
  {
    super(expr);
    mValue = expr.getValue();
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the integer value of this constant.
   */
  public int getValue()
  {
    return mValue;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final IntConstantProxy expr = (IntConstantProxy) partner;
      return mValue == expr.mValue;
    } else {
      return false;
    }    
  }

  public int hashCode()
  {
    return 5 * super.hashCode() + mValue;
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final Object partner)
  {
    if (partner instanceof IntConstantProxy) {
      final IntConstantProxy expr = (IntConstantProxy) partner;
      if (mValue < expr.mValue) {
	return -1;
      } else if (mValue > expr.mValue) {
	return 1;
      } else {
	return 0;
      }
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
    printer.print(mValue);
  }


  //#########################################################################
  //# Marshalling
  public SimpleExpressionType createElement(final ObjectFactory factory)
    throws JAXBException
  {
    return factory.createIntConstant();
  }

  public ElementType toJAXB(final ElementFactory factory)
    throws JAXBException
  {
    final IntConstantType expr =
      (IntConstantType) factory.createElement(this);
    expr.setValue(mValue);
    return expr;
  }


  //#########################################################################
  //# Evaluation
  public Value eval(final Context context)
  {
    return new IntValue(mValue);
  }

  public int getResultTypes()
  {
    return SimpleExpressionProxy.TYPE_INT;
  }


  //#########################################################################
  //# Comparing
  int getOrderIndex()
  {
    return SimpleExpressionProxy.ORDERINDEX_INTCONSTANT;
  }


  //#########################################################################
  //# Data Members
  private final int mValue;

}
