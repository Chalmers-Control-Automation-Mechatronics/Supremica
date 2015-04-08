//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Compiler
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariableCollector
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

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
 * A utility class to collect all the EFSM variables (primed or not) in
 * an expression or constraint list.
 *
 * @author Robi Malik
 */

class EFAVariableCollector
  extends DescendingModuleProxyVisitor
{

  //#########################################################################
  //# Constructor
  EFAVariableCollector(final CompilerOperatorTable optable,
                       final EFAModuleContext context)
  {
    mNextOperator = optable.getNextOperator();
    mContext = context;
  }


  //#########################################################################
  //# Invocation
  /**
   * Collects all unprimed variables in the given expression.
   * @param  expr        The expression to be searched.
   * @param  vars        Found variables will be added to this collection.
   */
  void collectUnprimedVariables(final SimpleExpressionProxy expr,
                                final Collection<EFAVariable> vars)
  {
    try {
      mUnprimedVariables = vars;
      collect(expr);
    } finally {
      mUnprimedVariables = null;
    }
  }

  /**
   * Collects all primed variables in the given expression.
   * @param  expr        The expression to be searched.
   * @param  vars        Found variables will be added in their non-primed
   *                     form to this collection.
   */
  void collectPrimedVariables(final SimpleExpressionProxy expr,
                              final Collection<EFAVariable> vars)
  {
    try {
      mPrimedVariables = vars;
      collect(expr);
    } finally {
      mPrimedVariables = null;
    }
  }

  /**
   * Collects all variables in the given expression.
   * @param  expr        The expression to be searched.
   * @param  unprimed    Unprimed variables will be added to this collection.
   * @param  primed      Primed variables will be added in their non-primed
   *                     form to this collection.
   */
  void collectAllVariables(final SimpleExpressionProxy expr,
                           final Collection<EFAVariable> unprimed,
                           final Collection<EFAVariable> primed)
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
   * Collects all unprimed variables in the given constraint list.
   * @param  constraints Constraint list containing expression to be searched.
   * @param  vars        Found variables will be added to this collection.
   */
  void collectUnprimedVariables(final ConstraintList constraints,
                                final Collection<EFAVariable> vars)
  {
    try {
      mUnprimedVariables = vars;
      for (final SimpleExpressionProxy expr : constraints.getConstraints()) {
        collect(expr);
      }
    } finally {
      mUnprimedVariables = null;
    }
  }

  /**
   * Collects all primed variables in the given constraint list.
   * @param  constraints Constraint list containing expression to be searched.
   * @param  vars        Found variables will be added in their non-primed
   *                     form to this collection.
   */
  void collectPrimedVariables(final ConstraintList constraints,
                              final Collection<EFAVariable> vars)
  {
    try {
      mPrimedVariables = vars;
      for (final SimpleExpressionProxy expr : constraints.getConstraints()) {
        collect(expr);
      }
    } finally {
      mPrimedVariables = null;
    }
  }

  /**
   * Collects all variables in the given constraint list.
   * @param  constraints Constraint list containing expression to be searched.
   * @param  unprimed    Unprimed variables will be added to this collection.
   * @param  primed      Primed variables will be added in their non-primed
   *                     form to this collection.
   */
  void collectAllVariables(final ConstraintList constraints,
                           final Collection<EFAVariable> unprimed,
                           final Collection<EFAVariable> primed)
  {
    try {
      mUnprimedVariables = unprimed;
      mPrimedVariables = primed;
      for (final SimpleExpressionProxy expr : constraints.getConstraints()) {
        collect(expr);
      }
    } finally {
      mUnprimedVariables = null;
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
  public Object visitIdentifierProxy(final IdentifierProxy ident)
  {
    if (mUnprimedVariables != null) {
      final EFAVariable var = mContext.getVariable(ident);
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
        final EFAVariable var = mContext.getVariable(subterm);
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
  private final EFAModuleContext mContext;

  private Collection<EFAVariable> mUnprimedVariables;
  private Collection<EFAVariable> mPrimedVariables;

}
