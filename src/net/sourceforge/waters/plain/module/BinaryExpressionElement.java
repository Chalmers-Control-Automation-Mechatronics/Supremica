//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   BinaryExpressionElement
//###########################################################################
//# $Id: BinaryExpressionElement.java,v 1.4 2006-03-06 17:08:46 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * An immutable implementation of the {@link BinaryExpressionProxy} interface.
 *
 * @author Robi Malik
 */

public final class BinaryExpressionElement
  extends SimpleExpressionElement
  implements BinaryExpressionProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new binary expression.
   * @param operator The operator of the new binary expression.
   * @param left The left subterm of the new binary expression.
   * @param right The right subterm of the new binary expression.
   */
  public BinaryExpressionElement(final BinaryOperator operator,
                                 final SimpleExpressionProxy left,
                                 final SimpleExpressionProxy right)
  {
    mOperator = operator;
    mLeft = left;
    mRight = right;
  }


  //#########################################################################
  //# Cloning
  public BinaryExpressionElement clone()
  {
    return (BinaryExpressionElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final BinaryExpressionElement downcast = (BinaryExpressionElement) partner;
      return
        mOperator.equals(downcast.mOperator) &&
        mLeft.equals(downcast.mLeft) &&
        mRight.equals(downcast.mRight);
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
    return downcast.visitBinaryExpressionProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.BinaryExpressionProxy
  public BinaryOperator getOperator()
  {
    return mOperator;
  }

  public SimpleExpressionProxy getLeft()
  {
    return mLeft;
  }

  public SimpleExpressionProxy getRight()
  {
    return mRight;
  }


  //#########################################################################
  //# Data Members
  private final BinaryOperator mOperator;
  private final SimpleExpressionProxy mLeft;
  private final SimpleExpressionProxy mRight;

}
