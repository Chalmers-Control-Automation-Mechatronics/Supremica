//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   ModuleBindingContext
//###########################################################################
//# $Id: ModuleBindingContext.java,v 1.3 2008-06-10 18:56:29 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.compiler;


import java.util.HashMap;
import java.util.Map;

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
  ModuleBindingContext(final ModuleProxy module)
  {
    this(module, null, null);
  }

  ModuleBindingContext(final ModuleProxy module,
                       final IdentifierProxy prefix,
                       final SourceInfo info)
  {
    mMap = new HashMap<String,SimpleExpressionProxy>();
    mModule = module;
    mPrefix = prefix;
    mInstanceSource = info;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.BindingContext
  public SimpleExpressionProxy getBoundExpression(final String name)
  {
    return mMap.get(name);
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
  void addBinding(final String name, final SimpleExpressionProxy value)
  {
    mMap.put(name, value);
  }


  //#########################################################################
  //# Data Members
  private final Map<String,SimpleExpressionProxy> mMap;
  private final ModuleProxy mModule;
  private final IdentifierProxy mPrefix;
  private final SourceInfo mInstanceSource;

}

