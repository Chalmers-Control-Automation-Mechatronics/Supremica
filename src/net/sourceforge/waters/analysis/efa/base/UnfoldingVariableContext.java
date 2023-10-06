//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * A variable context for EFA unfolding.
 * Can assign special current and next state value of a variable.
 *
 * @author Robi Malik, Sahar Mohajerani
 */
public class UnfoldingVariableContext implements VariableContext
{

  public UnfoldingVariableContext(final CompilerOperatorTable op,
                                  final AbstractEFAVariableContext<?, ?> context,
                                  final AbstractEFAVariable<?> var)
  {
    mOperatorTable = op;
    mRootContext = context;
    mUnfoldedVariableName = var.getVariableName();
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.VariableContext
  @Override
  public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
  {
    return mRootContext.getVariableRange(varname);
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.BindingContext
  @Override
  public SimpleExpressionProxy getBoundExpression(final SimpleExpressionProxy varname)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    if (eq.equals(varname, mUnfoldedVariableName)) {
      return mCurrentValue;
    } else if (varname instanceof UnaryExpressionProxy) {
      final UnaryExpressionProxy unary = (UnaryExpressionProxy) varname;
      final UnaryOperator op = unary.getOperator();
      if (op == mOperatorTable.getNextOperator()) {
        final SimpleExpressionProxy subterm = unary.getSubTerm();
        if (eq.equals(subterm, mUnfoldedVariableName)) {
          return mPrimedValue;
        }
      }
    }
    return mRootContext.getBoundExpression(varname);
  }

  @Override
  public final boolean isEnumAtom(final IdentifierProxy ident)
  {
    return mRootContext.isEnumAtom(ident);
  }

  @Override
  public ModuleBindingContext getModuleBindingContext()
  {
    return mRootContext.getModuleBindingContext();
  }

  @Override
  public int getNumberOfVariables()
  {
    return mRootContext.getNumberOfVariables();
  }


  //#######################################################################
  //# Simple Access
  public void setCurrentValue(final SimpleExpressionProxy current)
  {
    mCurrentValue = current;
  }

  public void setPrimedValue(final SimpleExpressionProxy primed)
  {
    mPrimedValue = primed;
  }

  public void resetCurrentAndPrimedValue()
  {
    mCurrentValue = mPrimedValue = null;
  }


  //#######################################################################
  //# Data Members
  private final CompilerOperatorTable mOperatorTable;
  private final AbstractEFAVariableContext<?, ?> mRootContext;
  private final SimpleExpressionProxy mUnfoldedVariableName;

  private SimpleExpressionProxy mCurrentValue;
  private SimpleExpressionProxy mPrimedValue;

}
