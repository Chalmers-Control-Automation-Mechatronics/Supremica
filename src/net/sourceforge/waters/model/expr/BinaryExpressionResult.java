//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   BinaryExpressionResult
//###########################################################################
//# $Id: BinaryExpressionResult.java,v 1.2 2008-02-15 02:17:19 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * An intermediate result of the expression parser that produces an binary
 * expression ({@link
 * net.sourceforge.waters.model.module.BinaryExpressionProxy
 * BinaryExpressionProxy}) object.
 *
 * @author Robi Malik
 */

class BinaryExpressionResult extends ParseResult {

  //#########################################################################
  //# Constructors
  BinaryExpressionResult(final BinaryOperator op,
                         final ParseResult lhs,
                         final ParseResult rhs)
  {
    mOperator = op;
    mLHS = lhs;
    mRHS = rhs;
  }


  //#########################################################################
  //# Overrides for Abstract Baseclass
  //# net.sourceforge.waters.model.expr.ParseResult
  int getTypeMask()
  {
    final int lhstypes = mLHS.getTypeMask();
    final int rhstypes = mRHS.getTypeMask();
    return mOperator.getReturnTypes(lhstypes, rhstypes);
  }

  SimpleExpressionProxy createProxy(final ModuleProxyFactory factory,
                                    final String text)
  {
    final SimpleExpressionProxy lhs = mLHS.createProxy(factory);
    final SimpleExpressionProxy rhs = mRHS.createProxy(factory);
    return mOperator.createExpression(factory, lhs, rhs, text);
  }


  //#########################################################################
  //# Data Members
  private final BinaryOperator mOperator;
  private final ParseResult mLHS;
  private final ParseResult mRHS;

}
