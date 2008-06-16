//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   ModuleBindingContext
//###########################################################################
//# $Id: ModuleBindingContext.java,v 1.1 2008-06-16 07:09:51 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.compiler.context;


import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * A binding context constructed when compiling a module.
 * A module binding context contains a symbol map with the bindings of any
 * parameters and constant aliases used throughout the compilation of the
 * module. If the module was compiled as an instance {@link
 * net.sourceforge.waters.model.module.InstanceProxy InstanceProxy} within
 * another module, information about the location of that instance is also
 * available.
 *
 * @see BindingContext
 * @author Robi Malik
 */

public class ModuleBindingContext implements BindingContext
{

  //#########################################################################
  //# Constructors
  public ModuleBindingContext(final ModuleProxy module)
  {
    this(module, null, null);
  }

  public ModuleBindingContext(final ModuleProxy module,
                              final IdentifierProxy prefix,
                              final SourceInfo info)
  {
    mMap = new HashMap<ProxyAccessor<IdentifierProxy>,SimpleExpressionProxy>();
    mModule = module;
    mPrefix = prefix;
    mInstanceSource = info;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.BindingContext
  public SimpleExpressionProxy getBoundExpression(final IdentifierProxy ident)
  {
    final ProxyAccessor<IdentifierProxy> key =
      new ProxyAccessorByContents<IdentifierProxy>(ident);
    return mMap.get(key);
  }

  public ModuleBindingContext getModuleBindingContext()
  {
    return this;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the module that was compiled.
   */
  public ModuleProxy getModule()
  {
    return mModule;
  }

  /**
   * Gets the prefix attached to all identifiers in the compiled module.
   * @return An {@link IdentifierProxy} encoding the prefix, or
   *         <CODE>null</CODE>, if the module was not obtained by
   *         instantiation.
   */
  public IdentifierProxy getPrefix()
  {
    return mPrefix;
  }

  /**
   * Gets the source information record from which the module has been
   * instantiated.
   * @return A {@link SourceInfo} record pointing to an {@link
   *         net.sourceforge.waters.model.module.InstanceProxy
   *         InstanceProxy}, or <CODE>null</CODE>, if the module was not
   *         obtained by instantiation.
   */
  public SourceInfo getInstanceSource()
  {
    return mInstanceSource;
  }


  //#########################################################################
  //# Compilation
  public void addBinding(final IdentifierProxy ident,
                         final SimpleExpressionProxy value)
  {
    final ProxyAccessor<IdentifierProxy> key =
      new ProxyAccessorByContents<IdentifierProxy>(ident);
    mMap.put(key, value);
  }

  public void insertBinding(final IdentifierProxy ident,
                            final SimpleExpressionProxy value)
    throws DuplicateIdentifierException
  {
    final ProxyAccessor<IdentifierProxy> key =
      new ProxyAccessorByContents<IdentifierProxy>(ident);
    if (mMap.containsKey(key)) {
      final String name = ident.toString();
      throw new DuplicateIdentifierException(name);
    } else {
      mMap.put(key, value);
    }
  }


  //#########################################################################
  //# Data Members
  private final Map<ProxyAccessor<IdentifierProxy>,SimpleExpressionProxy> mMap;
  private final ModuleProxy mModule;
  private final IdentifierProxy mPrefix;
  private final SourceInfo mInstanceSource;

}

