//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   BindingContext
//###########################################################################
//# $Id: BindingContext.java,v 1.2 2008-06-19 21:26:59 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.compiler.context;


import net.sourceforge.waters.model.module.IdentifierProxy;
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
   * @param  ident   The name to be looked up.
   *                 It is given as an identifier, simple or indexes,
   *                 so array lookups can be supported.
   * @return A variable-free expression representing the concrete value
   *         bound to the given name in this context, or <CODE>null</CODE>
   *         if there is no binding for the given name.
   */
  public SimpleExpressionProxy getBoundExpression(IdentifierProxy ident);

  /**
   * Determines whether the given identifier represents an enumeration atom
   * in this context.
   */
  public boolean isEnumAtom(final IdentifierProxy ident);

  /**
   * Gets the module context of this binding.
   */
  public ModuleBindingContext getModuleBindingContext();

}
