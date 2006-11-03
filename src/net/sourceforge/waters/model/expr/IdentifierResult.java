//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   IdentifierResult
//###########################################################################
//# $Id: IdentifierResult.java,v 1.2 2006-11-03 15:01:58 torda Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * An intermediate result of the expression parser that produces an
 * identifier ({@link SimpleIdentifierProxy} or {@link
 * IndexedIdentifierProxy}) object.
 *
 * @author Robi Malik
 */

class IdentifierResult extends ParseResult {

  //#########################################################################
  //# Constructors
  IdentifierResult(final String name, final List<ParseResult> indexes)
  {
    mName = name;
    mIndexes = indexes;
  }


  //#########################################################################
  //# Overrides for Abstract Baseclass
  //# net.sourceforge.waters.model.expr.ParseResult
  int getTypeMask()
  {
    if (mIndexes.isEmpty()) {
      return Operator.TYPE_ANY;
    } else {
      return Operator.TYPE_NAME;
    }
  }

  IdentifierProxy createProxy(final ModuleProxyFactory factory,
                              final String text)
  {
    final int size = mIndexes.size();
    if (size == 0) {
      return factory.createSimpleIdentifierProxy(text, mName);
    } else {
      final List<SimpleExpressionProxy> expressions =
        new ArrayList<SimpleExpressionProxy>(size);
      for (final ParseResult result : mIndexes) {
        final SimpleExpressionProxy expr = result.createProxy(factory);
        expressions.add(expr);
      }
      return factory.createIndexedIdentifierProxy(text, mName, expressions);
    }
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private final List<ParseResult> mIndexes;

}
