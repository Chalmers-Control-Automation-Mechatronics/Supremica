//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   AliasElement
//###########################################################################
//# $Id: AliasElement.java,v 1.4 2006-03-06 17:08:46 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;


/**
 * An immutable implementation of the {@link AliasProxy} interface.
 *
 * @author Robi Malik
 */

public final class AliasElement
  extends IdentifiedElement
  implements AliasProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new alias.
   * @param identifier The identifier defining the name of the new alias.
   * @param expression The expression of the new alias.
   */
  public AliasElement(final IdentifierProxy identifier,
                      final ExpressionProxy expression)
  {
    super(identifier);
    mExpression = expression;
  }


  //#########################################################################
  //# Cloning
  public AliasElement clone()
  {
    return (AliasElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final AliasElement downcast = (AliasElement) partner;
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
  public ExpressionProxy getExpression()
  {
    return mExpression;
  }


  //#########################################################################
  //# Data Members
  private final ExpressionProxy mExpression;

}
