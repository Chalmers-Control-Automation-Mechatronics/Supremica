//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   IntConstantResult
//###########################################################################
//# $Id: IntConstantResult.java,v 1.1 2006-09-06 11:52:21 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.IntConstantProxy;


/**
 * An intermediate result of the expression parser that produces an
 * integer contant ({@link IntConstantProxy}) object.
 *
 * @author Robi Malik
 */

class IntConstantResult extends ParseResult {

  //#########################################################################
  //# Constructors
  IntConstantResult(final int value)
  {
    mValue = value;
  }


  //#########################################################################
  //# Overrides for Abstract Baseclass
  //# net.sourceforge.waters.model.expr.ParseResult
  int getTypeMask()
  {
    switch (mValue) {
    case 0:
    case 1:
      return Operator.TYPE_INT | Operator.TYPE_BOOLEAN;
    default:
      return Operator.TYPE_INT;
    }
  }

  IntConstantProxy createProxy(final ModuleProxyFactory factory,
                               final String text)
  {
    return factory.createIntConstantProxy(text, mValue);
  }


  //#########################################################################
  //# Data Members
  private final int mValue;

}
