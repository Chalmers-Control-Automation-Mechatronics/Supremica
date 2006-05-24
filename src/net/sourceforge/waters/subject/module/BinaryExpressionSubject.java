//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   BinaryExpressionSubject
//###########################################################################
//# $Id: BinaryExpressionSubject.java,v 1.5 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


/**
 * The subject implementation of the {@link BinaryExpressionProxy} interface.
 *
 * @author Robi Malik
 */

public final class BinaryExpressionSubject
  extends SimpleExpressionSubject
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
  public BinaryExpressionSubject(final BinaryOperator operator,
                                 final SimpleExpressionProxy left,
                                 final SimpleExpressionProxy right)
  {
    mOperator = operator;
    mLeft = (SimpleExpressionSubject) left;
    mLeft.setParent(this);
    mRight = (SimpleExpressionSubject) right;
    mRight.setParent(this);
  }


  //#########################################################################
  //# Cloning
  public BinaryExpressionSubject clone()
  {
    final BinaryExpressionSubject cloned = (BinaryExpressionSubject) super.clone();
    cloned.mLeft = mLeft.clone();
    cloned.mLeft.setParent(cloned);
    cloned.mRight = mRight.clone();
    cloned.mRight.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final BinaryExpressionSubject downcast = (BinaryExpressionSubject) partner;
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

  public SimpleExpressionSubject getLeft()
  {
    return mLeft;
  }

  public SimpleExpressionSubject getRight()
  {
    return mRight;
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the operator of this expression.
   */
  public void setOperator(final BinaryOperator operator)
  {
    if (mOperator.equals(operator)) {
      return;
    }
    mOperator = operator;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  /**
   * Sets the left subterm of this expression.
   */
  public void setLeft(final SimpleExpressionSubject left)
  {
    if (mLeft == left) {
      return;
    }
    left.setParent(this);
    mLeft.setParent(null);
    mLeft = left;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  /**
   * Sets the right subterm of this expression.
   */
  public void setRight(final SimpleExpressionSubject right)
  {
    if (mRight == right) {
      return;
    }
    right.setParent(this);
    mRight.setParent(null);
    mRight = right;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private BinaryOperator mOperator;
  private SimpleExpressionSubject mLeft;
  private SimpleExpressionSubject mRight;

}
