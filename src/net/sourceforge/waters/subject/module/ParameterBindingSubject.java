//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   ParameterBindingSubject
//###########################################################################
//# $Id: ParameterBindingSubject.java,v 1.3 2005-12-03 21:30:42 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.NamedSubject;


/**
 * The subject implementation of the {@link ParameterBindingProxy} interface.
 *
 * @author Robi Malik
 */

public final class ParameterBindingSubject
  extends NamedSubject
  implements ParameterBindingProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new parameter binding.
   * @param name The name of the new parameter binding.
   * @param expression The expression of the new parameter binding.
   */
  public ParameterBindingSubject(final String name,
                                 final ExpressionProxy expression)
  {
    super(name);
    mExpression = (ExpressionSubject) expression;
    mExpression.setParent(this);
  }


  //#########################################################################
  //# Cloning
  public ParameterBindingSubject clone()
  {
    final ParameterBindingSubject cloned = (ParameterBindingSubject) super.clone();
    cloned.mExpression = mExpression.clone();
    cloned.mExpression.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ParameterBindingSubject downcast = (ParameterBindingSubject) partner;
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
