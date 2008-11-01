//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   UnaryExpressionResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * An intermediate result of the expression parser that produces an
 * unary expression ({@link UnaryExpressionProxy}) object.
 *
 * @author Robi Malik
 */

class UnaryExpressionResult extends ParseResult {

  //#########################################################################
  //# Constructors
  UnaryExpressionResult(final UnaryOperator op, final ParseResult subresult)
  {
    mOperator = op;
    mSubResult = subresult;
  }


  //#########################################################################
  //# Overrides for Abstract Baseclass
  //# net.sourceforge.waters.model.expr.ParseResult
  int getTypeMask()
  {
    final int subtypes = mSubResult.getTypeMask();
    return mOperator.getReturnTypes(subtypes);
  }

  UnaryExpressionProxy createProxy(final ModuleProxyFactory factory,
				   final String text)
  {
    final SimpleExpressionProxy subterm = mSubResult.createProxy(factory);
    return factory.createUnaryExpressionProxy(text, mOperator, subterm);
  }


  //#########################################################################
  //# Data Members
  private final UnaryOperator mOperator;
  private final ParseResult mSubResult;

}
