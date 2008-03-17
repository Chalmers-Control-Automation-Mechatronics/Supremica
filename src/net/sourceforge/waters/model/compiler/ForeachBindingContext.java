//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   ForeachBindingContext
//###########################################################################
//# $Id: ForeachBindingContext.java,v 1.1 2008-03-17 02:08:21 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.compiler;


import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * A binding context constructed when compiling a foreach block.
 * A foreach binding context consists of a single name and bound value,
 * representing the index variable of the foreach block and its current
 * value, and a reference to an enclosing context that may contain
 * further bindings.
 *
 * @see BindingContext
 * @author Robi Malik
 */

public class ForeachBindingContext implements BindingContext
{

  //#########################################################################
  //# Constructors
  ForeachBindingContext(final String name,
                        final SimpleExpressionProxy value,
                        final BindingContext parent)
  {
    mBoundName = name;
    mBoundValue = value;
    mParent = parent;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.BindingContext
  public SimpleExpressionProxy getBoundExpression(final String name)
  {
    if (mBoundName.equals(name)) {
      return mBoundValue;
    } else {
      return mParent.getBoundExpression(name);
    }
  }

  public ModuleBindingContext getModuleBindingContext()
  {
    return mParent.getModuleBindingContext();
  }


  //#########################################################################
  //# Simple Access
  BindingContext getParent()
  {
    return mParent;
  }


  //#########################################################################
  //# Data Members
  private final String mBoundName;
  private final SimpleExpressionProxy mBoundValue;
  private final BindingContext mParent;

}

