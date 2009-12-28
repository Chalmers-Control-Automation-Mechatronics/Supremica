//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   PlaceHolder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


class PlaceHolder {

  //#########################################################################
  //# Constructors
  PlaceHolder(final ModuleProxyFactory factory, final String name)
  {
    this(factory.createSimpleIdentifierProxy(name));
  }

  PlaceHolder(final SimpleIdentifierProxy ident)
  {
    mIdentifier = ident;
  }


  //#########################################################################
  //# Simple Access
  SimpleIdentifierProxy getIdentifier()
  {
    return mIdentifier;
  }


  //#########################################################################
  //# Matching
  void reset()
  {
    mBoundExpression = null;
  }

  SimpleExpressionProxy getBoundExpression()
  {
    return mBoundExpression;
  }

  boolean match(final SimpleExpressionProxy expr,
                final ConstraintPropagator propagator)
    throws EvalException
  {
    if (mBoundExpression == null && accepts(expr, propagator)) {
      mBoundExpression = expr;
      return true;
    } else {
      final ModuleEqualityVisitor eq =
        ModuleEqualityVisitor.getInstance(false);
      return eq.equals(expr, mBoundExpression);
    }
  }

  boolean accepts(final SimpleExpressionProxy expr,
                  final ConstraintPropagator propagator)
    throws EvalException
  {
    return true;
  }


  //#########################################################################
  //# Data Members
  private final SimpleIdentifierProxy mIdentifier;

  private SimpleExpressionProxy mBoundExpression;

}