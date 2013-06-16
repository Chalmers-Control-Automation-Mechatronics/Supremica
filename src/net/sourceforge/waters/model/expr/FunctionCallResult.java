//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   FunctionCallResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * An intermediate result of the expression parser that produces an
 * identifier ({@link FunctionCallExpressionProxy}) object.
 *
 * @author Robi Malik
 */

class FunctionCallResult extends ParseResult {

  //#########################################################################
  //# Constructors
  FunctionCallResult(final BuiltInFunction function,
                     final List<ParseResult> args)
  {
    mFunction = function;
    mArguments = args;
  }


  //#########################################################################
  //# Overrides for Abstract Baseclass
  //# net.sourceforge.waters.model.expr.ParseResult
  @Override
  int getTypeMask()
  {
    final int size = mArguments.size();
    final int[] types = new int[size];
    int i = 0;
    for (final ParseResult result : mArguments) {
      types[i++] = result.getTypeMask();
    }
    return mFunction.getReturnTypes(types);
  }

  @Override
  FunctionCallExpressionProxy createProxy(final ModuleProxyFactory factory,
                                          final String text)
  {
    final int size = mArguments.size();
    final List<SimpleExpressionProxy> expressions =
      new ArrayList<SimpleExpressionProxy>(size);
    for (final ParseResult result : mArguments) {
      final SimpleExpressionProxy expr = result.createProxy(factory);
      expressions.add(expr);
    }
    final String name = mFunction.getName();
    return factory.createFunctionCallExpressionProxy(text, name, expressions);
  }


  //#########################################################################
  //# Data Members
  private final BuiltInFunction mFunction;
  private final List<ParseResult> mArguments;

}
