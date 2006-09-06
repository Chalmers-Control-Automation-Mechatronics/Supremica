//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   ParseResult
//###########################################################################
//# $Id: ParseResult.java,v 1.1 2006-09-06 11:52:21 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * An auxiliary class representing an intermediate result of the
 * expression parser. It contains the recognised type of the expression,
 * and all information about contents and subterms needed to construct
 * a {@link SimpleExpressionProxy} object. Actual creation of these
 * objects is delayed until the optional text argument can be provided.
 * There are individual subclasses for each type of expression.
 *
 * @author Robi Malik
 */

abstract class ParseResult {

  //#########################################################################
  //# Provided by Subclasses
  abstract int getTypeMask();

  abstract SimpleExpressionProxy createProxy(final ModuleProxyFactory factory,
                                             final String text);


  //#########################################################################
  //# Convenience Methods
  SimpleExpressionProxy createProxy(final ModuleProxyFactory factory)
  {
    return createProxy(factory, null);
  }

}
