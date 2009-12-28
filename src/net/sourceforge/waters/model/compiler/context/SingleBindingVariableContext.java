//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   SingleBindingVariableContext
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.compiler.context;

import java.util.Collection;

import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * <P>A variable binding context that binds a single variable to an
 * expression. This works like a {@link SingleBindingContext}, except that
 * the {@link VariableContext} interface is also implemented.
 *
 * @see BindingContext
 * @author Robi Malik
 */

public class SingleBindingVariableContext
  extends SingleBindingContext
  implements VariableContext
{

  //#########################################################################
  //# Constructors
  public SingleBindingVariableContext(final ModuleProxyFactory factory,
                                      final String name,
                                      final SimpleExpressionProxy value,
                                      final VariableContext parent)
  {
    super(factory, name, value, parent);
  }

  public SingleBindingVariableContext(final SimpleExpressionProxy varname,
                                      final SimpleExpressionProxy value,
                                      final VariableContext parent)
  {
    super(varname, value, parent);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.VariableBindingContext
  public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
  {
    final VariableContext parent = getParent();
    return parent.getVariableRange(varname);
  }

  public Collection<SimpleExpressionProxy> getVariableNames()
  {
    final VariableContext parent = getParent();
    return parent.getVariableNames();
  }

  //#########################################################################
  //# Simple Access
  VariableContext getParent()
  {
    return (VariableContext) super.getParent();
  }

}

