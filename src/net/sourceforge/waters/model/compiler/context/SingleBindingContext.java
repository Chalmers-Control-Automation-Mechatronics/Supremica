//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
  @Override
  public SimpleExpressionProxy getBoundExpression
    (final SimpleExpressionProxy ident)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    if (eq.equals(mBoundVariableName, ident)) {
      return mBoundValue;
    } else {
      return mParent.getBoundExpression(ident);
    }
  }

  @Override
  public boolean isEnumAtom(final IdentifierProxy ident)
  {
    if (ident instanceof SimpleIdentifierProxy &&
        new ModuleEqualityVisitor(false).equals(mBoundVariableName, ident)) {
      return false;
    } else {
      return mParent.isEnumAtom(ident);
    }
  }

  @Override
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
