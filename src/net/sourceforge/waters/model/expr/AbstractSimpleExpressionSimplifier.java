//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
