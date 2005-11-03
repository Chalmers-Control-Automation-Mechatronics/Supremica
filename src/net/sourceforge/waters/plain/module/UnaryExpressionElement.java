//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   UnaryExpressionElement
//###########################################################################
//# $Id: UnaryExpressionElement.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * An immutable implementation of the {@link UnaryExpressionProxy} interface.
 *
 * @author Robi Malik
 */

public final class UnaryExpressionElement
  extends SimpleExpressionElement
  implements UnaryExpressionProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new unary expression.
   * @param operator The operator of the new unary expression.
   * @param subTerm The subterm of the new unary expression.
   */
  public UnaryExpressionElement(final UnaryOperator operator,
                                final SimpleExpressionProxy subTerm)
  {
    mOperator = operator;
    mSubTerm = subTerm;
  }


  //#########################################################################
  //# Cloning
  public UnaryExpressionElement clone()
  {
    return (UnaryExpressionElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final UnaryExpressionElement downcast = (UnaryExpressionElement) partner;
      return
        mOperator.equals(downcast.mOperator) &&
        mSubTerm.equals(downcast.mSubTerm);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitUnaryExpressionProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.UnaryExpressionProxy
  public UnaryOperator getOperator()
  {
    return mOperator;
  }

  public SimpleExpressionProxy getSubTerm()
  {
    return mSubTerm;
  }


  //#########################################################################
  //# Data Members
  private final UnaryOperator mOperator;
  private final SimpleExpressionProxy mSubTerm;

}
