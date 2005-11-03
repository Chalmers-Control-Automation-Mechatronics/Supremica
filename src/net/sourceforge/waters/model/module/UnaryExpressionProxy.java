//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   UnaryExpressionProxy
//###########################################################################
//# $Id: UnaryExpressionProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.model.expr.UnaryOperator;


public interface UnaryExpressionProxy extends SimpleExpressionProxy {

  //#########################################################################
  //# Getters
  /**
   * Gets the operator of this expression.
   */
  public UnaryOperator getOperator();

  /**
   * Gets the subterm of this expression.
   */
  public SimpleExpressionProxy getSubTerm();

}
