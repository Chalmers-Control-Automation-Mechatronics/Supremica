//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   BindingContext
//###########################################################################
//# $Id: BindingContext.java,v 1.1 2008-03-17 02:08:21 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.compiler;


import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * Context information to show how an item was instantiated during
 * compilation. A binding context basically maps names to their bound
 * values. In addition, it provides access to a module context ({@link
 * ModuleBindingContext}) that can provide further information if the item
 * was compiled as part of an instance ({@link
 * net.sourceforge.waters.model.module.InstanceProxy InstanceProxy}).
 *
 * @see SourceInfo
 * @author Robi Malik
 */

public interface BindingContext
{

  /**
   * Gets the value bound to the given name.
   * @param  name    The name to be looked up.
   * @return A variable-free expression representing the concrete value
   *         bound to the given name in this context, or <CODE>null</CODE>
   *         if there is no binding for the given name.
   */
  public SimpleExpressionProxy getBoundExpression(final String name);

  /**
   * Gets the module context of this binding.
   */
  public ModuleBindingContext getModuleBindingContext();

}
