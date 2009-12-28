//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   SingleBindingContext
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.compiler.context;

import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * <P>A binding context that binds a single variable to an expression.</P>
 *
 * <P>A single-variable binding context consists of an expression
 * representing a single variable name and a bound value, plus a reference
 * to an enclosing context that may contain further bindings.</P>
 *
 * <P>This binding context is used to bind the index variable of a forach
 * block in the instance compiler, or to bind values to EFA variable in the
 * EFA compiler.</P>
 *
 * @see BindingContext
 * @author Robi Malik
 */

public class SingleBindingContext implements BindingContext
{

  //#########################################################################
  //# Constructors
  public SingleBindingContext(final ModuleProxyFactory factory,
                              final String name,
                              final SimpleExpressionProxy value,
                              final BindingContext parent)
  {
    this(factory.createSimpleIdentifierProxy(name), value, parent);
  }

  public SingleBindingContext(final SimpleExpressionProxy varname,
                              final SimpleExpressionProxy value,
                              final BindingContext parent)
  {
    mBoundVariableName = varname;
    mBoundValue = value;
    mParent = parent;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.BindingContext
  public SimpleExpressionProxy getBoundExpression
    (final SimpleExpressionProxy ident)
  {
    final ModuleEqualityVisitor eq =
      ModuleEqualityVisitor.getInstance(false);
    if (eq.equals(mBoundVariableName, ident)) {
      return mBoundValue;
    } else {
      return mParent.getBoundExpression(ident);
    }
  }

  public boolean isEnumAtom(final IdentifierProxy ident)
  {
    if (ident instanceof SimpleIdentifierProxy &&
        ModuleEqualityVisitor.getInstance(false).
          equals(mBoundVariableName, ident)) {
      return false;
    } else {
      return mParent.isEnumAtom(ident);
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
  private final SimpleExpressionProxy mBoundVariableName;
  private final SimpleExpressionProxy mBoundValue;
  private final BindingContext mParent;

}

