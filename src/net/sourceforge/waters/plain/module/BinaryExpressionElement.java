//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   BinaryExpressionElement
//###########################################################################
//# $Id: BinaryExpressionElement.java,v 1.7 2006-09-06 11:52:21 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.Proxy;
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
   * @param plainText The original text of the new binary expression, or <CODE>null</CODE>.
   * @param operator The operator of the new binary expression.
   * @param left The left subterm of the new binary expression.
   * @param right The right subterm of the new binary expression.
   */
  public BinaryExpressionElement(final String plainText,
                                 final BinaryOperator operator,
                                 final SimpleExpressionProxy left,
                                 final SimpleExpressionProxy right)
  {
    super(plainText);
    mOperator = operator;
    mLeft = left;
    mRight = right;
  }

  /**
   * Creates a new binary expression using default values.
   * This constructor creates a binary expression with
   * the original text set to <CODE>null</CODE>.
   * @param operator The operator of the new binary expression.
   * @param left The left subterm of the new binary expression.
   * @param right The right subterm of the new binary expression.
   */
  public BinaryExpressionElement(final BinaryOperator operator,
                                 final SimpleExpressionProxy left,
                                 final SimpleExpressionProxy right)
  {
    this(null,
         operator,
         left,
         right);
  }


  //#########################################################################
  //# Cloning
  public BinaryExpressionElement clone()
  {
    return (BinaryExpressionElement) super.clone();
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final BinaryExpressionElement downcast = (BinaryExpressionElement) partner;
      return
        mOperator.equals(downcast.mOperator) &&
        mLeft.equalsByContents(downcast.mLeft) &&
        mRight.equalsByContents(downcast.mRight);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final BinaryExpressionElement downcast = (BinaryExpressionElement) partner;
      return
        mOperator.equals(downcast.mOperator) &&
        mLeft.equalsWithGeometry(downcast.mLeft) &&
        mRight.equalsWithGeometry(downcast.mRight);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mOperator.hashCode();
    result *= 5;
    result += mLeft.hashCodeByContents();
    result *= 5;
    result += mRight.hashCodeByContents();
    return result;
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeWithGeometry();
    result *= 5;
    result += mOperator.hashCode();
    result *= 5;
    result += mLeft.hashCodeWithGeometry();
    result *= 5;
    result += mRight.hashCodeWithGeometry();
    return result;
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
