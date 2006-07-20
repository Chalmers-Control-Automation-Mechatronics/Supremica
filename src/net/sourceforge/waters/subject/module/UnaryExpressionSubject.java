//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   UnaryExpressionSubject
//###########################################################################
//# $Id: UnaryExpressionSubject.java,v 1.6 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


/**
 * The subject implementation of the {@link UnaryExpressionProxy} interface.
 *
 * @author Robi Malik
 */

public final class UnaryExpressionSubject
  extends SimpleExpressionSubject
  implements UnaryExpressionProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new unary expression.
   * @param operator The operator of the new unary expression.
   * @param subTerm The subterm of the new unary expression.
   */
  public UnaryExpressionSubject(final UnaryOperator operator,
                                final SimpleExpressionProxy subTerm)
  {
    mOperator = operator;
    mSubTerm = (SimpleExpressionSubject) subTerm;
    mSubTerm.setParent(this);
  }


  //#########################################################################
  //# Cloning
  public UnaryExpressionSubject clone()
  {
    final UnaryExpressionSubject cloned = (UnaryExpressionSubject) super.clone();
    cloned.mSubTerm = mSubTerm.clone();
    cloned.mSubTerm.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final UnaryExpressionSubject downcast = (UnaryExpressionSubject) partner;
      return
        mOperator.equals(downcast.mOperator) &&
        mSubTerm.equalsByContents(downcast.mSubTerm);
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
    result += mSubTerm.hashCodeByContents();
    return result;
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

  public SimpleExpressionSubject getSubTerm()
  {
    return mSubTerm;
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the operator of this expression.
   */
  public void setOperator(final UnaryOperator operator)
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
   * Sets the subterm of this expression.
   */
  public void setSubTerm(final SimpleExpressionSubject subTerm)
  {
    if (mSubTerm == subTerm) {
      return;
    }
    subTerm.setParent(this);
    mSubTerm.setParent(null);
    mSubTerm = subTerm;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private UnaryOperator mOperator;
  private SimpleExpressionSubject mSubTerm;

}
