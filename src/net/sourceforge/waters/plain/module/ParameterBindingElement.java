//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   ParameterBindingElement
//###########################################################################
//# $Id: ParameterBindingElement.java,v 1.4 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.base.NamedElement;


/**
 * An immutable implementation of the {@link ParameterBindingProxy} interface.
 *
 * @author Robi Malik
 */

public final class ParameterBindingElement
  extends NamedElement
  implements ParameterBindingProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new parameter binding.
   * @param name The name of the new parameter binding.
   * @param expression The expression of the new parameter binding.
   */
  public ParameterBindingElement(final String name,
                                 final ExpressionProxy expression)
  {
    super(name);
    mExpression = expression;
  }


  //#########################################################################
  //# Cloning
  public ParameterBindingElement clone()
  {
    return (ParameterBindingElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ParameterBindingElement downcast = (ParameterBindingElement) partner;
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
    return downcast.visitParameterBindingProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ParameterBindingProxy
  public ExpressionProxy getExpression()
  {
    return mExpression;
  }


  //#########################################################################
  //# Data Members
  private final ExpressionProxy mExpression;

}
