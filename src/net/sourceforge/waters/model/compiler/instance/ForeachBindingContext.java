//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   ForeachBindingContext
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.compiler.instance;

import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


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
  public SimpleExpressionProxy getBoundExpression
    (final SimpleExpressionProxy ident)
  {
    if (!(ident instanceof SimpleIdentifierProxy)) {
      return mParent.getBoundExpression(ident);
    }
    final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
    final String name = simple.getName();
    if (mBoundName.equals(name)) {
      return mBoundValue;
    } else {
      return mParent.getBoundExpression(ident);
    }
  }

  public boolean isEnumAtom(final IdentifierProxy ident)
  {
    if (ident instanceof SimpleIdentifierProxy) {
      final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
      if (simple.getName().equals(mBoundName)) {
        return false;
      }
    }
    return mParent.isEnumAtom(ident);
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

