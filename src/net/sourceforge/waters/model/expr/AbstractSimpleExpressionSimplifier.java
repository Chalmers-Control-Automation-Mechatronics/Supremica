//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   AbstractSimpleExpressionSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.Comparator;

import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


public abstract class AbstractSimpleExpressionSimplifier
{

  //#########################################################################
  //# Constructor
  protected AbstractSimpleExpressionSimplifier
    (final ModuleProxyFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Simple Access
  public ModuleProxyFactory getFactory()
  {
    return mFactory;
  }

  public ModuleProxyCloner getCloner()
  {
    return mFactory.getCloner();
  }


  //#########################################################################
  //# Invocation
  public abstract SimpleExpressionProxy simplify(SimpleExpressionProxy expr)
    throws EvalException;

  public abstract boolean isAtomicValue(SimpleExpressionProxy expr);

  public abstract Comparator<SimpleExpressionProxy> getExpressionComparator();


  //#########################################################################
  //# Auxiliary Methods
  public boolean isBooleanValue(final SimpleExpressionProxy expr)
  {
    if (expr instanceof IntConstantProxy) {
      final IntConstantProxy intconst = (IntConstantProxy) expr;
      final int value = intconst.getValue();
      return value == 0 || value == 1;
    } else {
      return false;
    }
  }

  public int getIntValue(final SimpleExpressionProxy expr)
    throws TypeMismatchException
  {
    if (expr instanceof IntConstantProxy) {
      final IntConstantProxy intconst = (IntConstantProxy) expr;
      return intconst.getValue();
    } else {
      throw new TypeMismatchException(expr, "INTEGER");
    }
  }

  public boolean getBooleanValue(final SimpleExpressionProxy expr)
    throws TypeMismatchException
  {
    if (expr instanceof IntConstantProxy) {
      final IntConstantProxy intconst = (IntConstantProxy) expr;
      switch (intconst.getValue()) {
      case 0:
        return false;
      case 1:
        return true;
      default:
        break;
      }
    }
    throw new TypeMismatchException(expr, "BOOLEAN");
  }

  public IdentifierProxy getIdentifierValue(final SimpleExpressionProxy expr)
    throws TypeMismatchException
  {
    if (expr instanceof IdentifierProxy) {
      return (IdentifierProxy) expr;
    } else {
      throw new TypeMismatchException(expr, "IDENTIFIER");
    }
  }

  public IntConstantProxy createIntConstantProxy(final int value)
  {
    return mFactory.createIntConstantProxy(value);
  }

  public IntConstantProxy createBooleanConstantProxy(final boolean value)
  {
    return createIntConstantProxy(value ? 1 : 0);
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;

}
