//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   UnfoldingVariableContext
//###########################################################################
//# $Id$
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
    final ModuleEqualityVisitor eq =
      ModuleEqualityVisitor.getInstance(false);
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
