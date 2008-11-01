//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   EnumSetExpressionResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.ArrayList;
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
    final int size = mItems.size();
    final List<SimpleIdentifierProxy> clones =
      new ArrayList<SimpleIdentifierProxy>(size);
    for (final SimpleIdentifierProxy item : mItems) {
      clones.add(item.clone());
    }
    return factory.createEnumSetExpressionProxy(text, clones);
  }


  //#########################################################################
  //# Data Members
  private final List<SimpleIdentifierProxy> mItems;

}
