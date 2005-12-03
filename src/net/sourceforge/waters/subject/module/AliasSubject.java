//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   AliasSubject
//###########################################################################
//# $Id: AliasSubject.java,v 1.3 2005-12-03 21:30:42 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


/**
 * The subject implementation of the {@link AliasProxy} interface.
 *
 * @author Robi Malik
 */

public final class AliasSubject
  extends IdentifiedSubject
  implements AliasProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new alias.
   * @param identifier The identifier defining the name of the new alias.
   * @param expression The expression of the new alias.
   */
  public AliasSubject(final IdentifierProxy identifier,
                      final ExpressionProxy expression)
  {
    super(identifier);
    mExpression = (ExpressionSubject) expression;
    mExpression.setParent(this);
  }


  //#########################################################################
  //# Cloning
  public AliasSubject clone()
  {
    final AliasSubject cloned = (AliasSubject) super.clone();
    cloned.mExpression = mExpression.clone();
    cloned.mExpression.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final AliasSubject downcast = (AliasSubject) partner;
      return
        mExpression.equals(downcast.mExpression);
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
    return downcast.visitAliasProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.AliasProxy
  public ExpressionSubject getExpression()
  {
    return mExpression;
  }


  //#########################################################################
  //# Setters
  public void setExpression(final ExpressionSubject expression)
  {
    if (mExpression == expression) {
      return;
    }
    expression.setParent(this);
    mExpression.setParent(null);
    mExpression = expression;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private ExpressionSubject mExpression;

}
