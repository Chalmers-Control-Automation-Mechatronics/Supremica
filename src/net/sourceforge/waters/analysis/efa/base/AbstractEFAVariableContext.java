//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.efa.base;

import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;

/**
 * A variable context for EFA compilation. Contains ranges of all variables, and
 * identifies enumeration atoms.
 *
 * @author Robi Malik
 */
public abstract class AbstractEFAVariableContext<L,
                                                 V extends AbstractEFAVariable<L>>
 implements VariableContext
{

  //#######################################################################
  //# Constructor
  public AbstractEFAVariableContext(final ModuleProxy module,
                                    final CompilerOperatorTable op)
  {

    mModuleContext = new ModuleBindingContext(module);
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mGlobalVariableMap = new ProxyAccessorHashMap<>(eq);
    mNextOperator = op.getNextOperator();
  }

  //#######################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.VariableContext
  @Override
  public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
  {
    if (varname instanceof IdentifierProxy) {
      final IdentifierProxy ident = (IdentifierProxy) varname;
      final AbstractEFAVariable<L> variable =
       mGlobalVariableMap.getByProxy(ident);
      if (variable != null) {
        return variable.getRange();
      }
    } else if (varname instanceof UnaryExpressionProxy) {
      final UnaryExpressionProxy unary = (UnaryExpressionProxy) varname;
      if (unary.getOperator() == mNextOperator) {
        return getVariableRange(unary.getSubTerm());
      }
    }
    return null;
  }

  @Override
  public SimpleExpressionProxy getBoundExpression(
   final SimpleExpressionProxy ident)
  {
    return mModuleContext.getBoundExpression(ident);
  }

  @Override
  public boolean isEnumAtom(final IdentifierProxy ident)
  {
    return mModuleContext.isEnumAtom(ident);
  }

  @Override
  public ModuleBindingContext getModuleBindingContext()
  {
    return mModuleContext;
  }

  @Override
  public int getNumberOfVariables()
  {
    return mGlobalVariableMap.size();
  }

  //#######################################################################
  //# Simple Access
  public V getVariable(final SimpleExpressionProxy varName)
  {
    if (varName instanceof IdentifierProxy) {
      final IdentifierProxy ident = (IdentifierProxy) varName;
      return mGlobalVariableMap.getByProxy(ident);
    } else {
      return null;
    }
  }

  public void addVariable(final V var)
  {
    final IdentifierProxy ident = var.getVariableName();
    mGlobalVariableMap.putByProxy(ident, var);
  }

  public void insertEnumAtom(final SimpleIdentifierProxy ident)
    throws DuplicateIdentifierException, TypeMismatchException
  {
    mModuleContext.insertEnumAtom(ident);
  }


  //#######################################################################
  //# Data Members
  private final ModuleBindingContext mModuleContext;
  private final UnaryOperator mNextOperator;

  protected final ProxyAccessorMap<IdentifierProxy, V> mGlobalVariableMap;

}
