//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   EnumSetExpressionResult
//###########################################################################
//# $Id: EnumSetExpressionResult.java,v 1.1 2006-09-06 11:52:21 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.List;

import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * An intermediate result of the expression parser that produces an
 * enumerated set ({@link EnumSetExpressionProxy}) object.
 *
 * @author Robi Malik
 */

class EnumSetExpressionResult extends ParseResult {

  //#########################################################################
  //# Constructors
  EnumSetExpressionResult(final List<SimpleIdentifierProxy> items)
  {
    mItems = items;
  }


  //#########################################################################
  //# Overrides for Abstract Baseclass
  //# net.sourceforge.waters.model.expr.ParseResult
  int getTypeMask()
  {
    return Operator.TYPE_RANGE;
  }

  EnumSetExpressionProxy createProxy(final ModuleProxyFactory factory,
                                     final String text)
  {
    return factory.createEnumSetExpressionProxy(text, mItems);
  }


  //#########################################################################
  //# Data Members
  private final List<SimpleIdentifierProxy> mItems;

}
