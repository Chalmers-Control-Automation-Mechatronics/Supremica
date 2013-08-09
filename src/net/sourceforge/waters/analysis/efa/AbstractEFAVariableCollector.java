//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   AbstractEFAVariableCollector
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import java.util.Collection;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.DescendingModuleProxyVisitor;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * A utility class to collect all the EFA variables (primed or not) in
 * an update or event encoding.
 *
 * @author Robi Malik
 */

public abstract class AbstractEFAVariableCollector<V extends AbstractEFAVariable>
  extends DescendingModuleProxyVisitor
{

  //#########################################################################
  //# Constructor
  protected AbstractEFAVariableCollector(final CompilerOperatorTable optable,
                                         final AbstractEFAVariableContext<V> context)
  {
    mNextOperator = optable.getNextOperator();
    mContext = context;
  }


  //#########################################################################
  //# Invocation
  /**
   * Collects all variables in the given expression.
   * @param  expr     The expression to be searched.
   * @param  vars     All variables will be added to this collection.
   */
  public void collectAllVariables(final SimpleExpressionProxy expr,
                                  final Collection<V> vars)
  {
    collectAllVariables(expr, vars, vars);
  }

  /**
   * Collects all variables in the given constraint list.
   * @param  update   The constraint list to be searched.
   * @param  vars     All variables will be added to this collection.
   */
  public void collectAllVariables(final ConstraintList update,
                                  final Collection<V> vars)
  {
    collectAllVariables(update, vars, vars);
  }


  /**
   * Collects all variables in the given expression.
   * @param  expr     The expression to be searched.
   * @param  unprimed Unprimed variables will be added to this collection.
   *                  This may be <CODE>null</CODE> to suppress collecting
   *                  unprimed variables.
   * @param  primed   Primed variables will be added to this collection.
   *                  This may be <CODE>null</CODE> to suppress collecting
   *                  primed variables.
   */
  public void collectAllVariables(final SimpleExpressionProxy expr,
                                  final Collection<V> unprimed,
                                  final Collection<V> primed)
  {
    try {
      mUnprimedVariables = unprimed;
      mPrimedVariables = primed;
      collect(expr);
    } finally {
      mUnprimedVariables = null;
      mPrimedVariables = null;
    }
  }

  /**
   * Collects all variables in the given constraint list.
   * @param  update   The constraint list to be searched.
   * @param  unprimed Unprimed variables will be added to this collection.
   *                  This may be <CODE>null</CODE> to suppress collecting
   *                  unprimed variables.
   * @param  primed   Primed variables will be added to this collection.
   *                  This may be <CODE>null</CODE> to suppress collecting
   *                  primed variables.
   */
  public void collectAllVariables(final ConstraintList update,
                                  final Collection<V> unprimed,
                                  final Collection<V> primed)
  {
    for (final SimpleExpressionProxy expr : update.getConstraints()) {
      collectAllVariables(expr, unprimed, primed);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void collect(final SimpleExpressionProxy expr)
  {
    try {
      expr.acceptVisitor(this);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public Object visitIdentifierProxy(final IdentifierProxy ident)
  {
    if (mUnprimedVariables != null) {
      final V var = mContext.getVariable(ident);
      if (var != null) {
        mUnprimedVariables.add(var);
      }
    }
    return null;
  }

  @Override
  public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
  {
    return null;
  }

  @Override
  public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
    throws VisitorException
  {
    final SimpleExpressionProxy subterm = expr.getSubTerm();
    if (expr.getOperator() == mNextOperator) {
      if (mPrimedVariables != null) {
        final V var = mContext.getVariable(subterm);
        if (var != null) {
          mPrimedVariables.add(var);
        }
      }
    } else {
      subterm.acceptVisitor(this);
    }
    return null;
  }


  //#########################################################################
  //# Data Members
  private final UnaryOperator mNextOperator;
  private final AbstractEFAVariableContext<V> mContext;

  private Collection<V> mUnprimedVariables;
  private Collection<V> mPrimedVariables;

}
