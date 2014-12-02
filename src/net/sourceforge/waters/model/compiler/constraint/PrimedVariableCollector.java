//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Compiler
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   PrimedVariableCollector
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import java.util.Collection;

import net.sourceforge.waters.model.base.ProxyAccessorSet;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.DescendingModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * A utility class to collect all the primed EFSM variables in expressions.
 *
 * @author Robi Malik
 */

class PrimedVariableCollector
  extends DescendingModuleProxyVisitor
{

  //#########################################################################
  //# Constructor
  PrimedVariableCollector(final CompilerOperatorTable optable,
                          final VariableContext context)
  {
    mNextOperator = optable.getNextOperator();
    mContext = context;
  }


  //#########################################################################
  //# Invocation
  /**
   * Collects all primed variables in the given expression.
   * @param  expr        The expression to be searched.
   * @param  vars        Found variables will be added in their primed form
   *                     to this set.
   */
  void collectPrimedVariables(final SimpleExpressionProxy expr,
                              final ProxyAccessorSet<UnaryExpressionProxy> vars)
  {
    try {
      mPrimedVariables = vars;
      collect(expr);
    } finally {
      mPrimedVariables = null;
    }
  }

  /**
   * Collects all primed variables in the given expressions.
   * @param  expr        A collection of expressions to be searched.
   * @param  vars        Found variables will be added in their primed form
   *                     to this set.
   */
  void collectPrimedVariables(final Collection<SimpleExpressionProxy> exprs,
                              final ProxyAccessorSet<UnaryExpressionProxy> vars)
  {
    try {
      mPrimedVariables = vars;
      for (final SimpleExpressionProxy expr : exprs) {
        collect(expr);
      }
    } finally {
      mPrimedVariables = null;
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
  public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
  {
    return null;
  }

  @Override
  public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
    throws VisitorException
  {
    if (expr.getOperator() == mNextOperator) {
      if (mContext.getVariableRange(expr) != null) {
        mPrimedVariables.addProxy(expr);
      }
    } else {
      final SimpleExpressionProxy subterm = expr.getSubTerm();
      subterm.acceptVisitor(this);
    }
    return null;
  }


  //#########################################################################
  //# Data Members
  private final UnaryOperator mNextOperator;
  private final VariableContext mContext;

  private ProxyAccessorSet<UnaryExpressionProxy> mPrimedVariables;

}
