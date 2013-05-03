//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariableCollector
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.util.Collection;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * A utility class to collect all the EFSM variables (primed or not) in
 * an update or event encoding.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

class EFSMVariableCollector
  extends DefaultModuleProxyVisitor
{


  //#########################################################################
  //# Constructor
  EFSMVariableCollector(final CompilerOperatorTable optable,
                        final EFSMVariableContext context)
  {
    mNextOperator = optable.getNextOperator();
    mContext = context;
  }


  //#########################################################################
  //# Invocation
  /**
   * Collects all unprimed variables in the given expression.
   * @param  expr     The expression to be searched.
   * @param  vars     Found variables will be added to this collection.
   */
  void collectUnprimedVariables(final SimpleExpressionProxy expr,
                                final Collection<EFSMVariable> vars)
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
   * @param  expr     The expression to be searched.
   * @param  vars     Found variables will be added in their non-primed form
   *                  to this collection.
   */
  void collectPrimedVariables(final SimpleExpressionProxy expr,
                              final Collection<EFSMVariable> vars)
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
   * @param  expr     The expression to be searched.
   * @param  variables All variables will be added to this collection.
   */
  void collectAllVariables(final SimpleExpressionProxy expr,
                           final Collection<EFSMVariable> variables)
  {
    try {
      mUnprimedVariables = variables;
      mPrimedVariables = variables;
      collect(expr);
    } finally {
      mUnprimedVariables = null;
      mPrimedVariables = null;
    }
  }

  void collectAllVariables(final ConstraintList update,
                           final Collection<EFSMVariable> variables)
  {
    for (final SimpleExpressionProxy expr : update.getConstraints()) {
      collectAllVariables(expr, variables);
    }
  }

  void collectAllVariables(final EFSMEventEncoding encoding,
                           final Collection<EFSMVariable> variables)
  {
    for (int i = 0; i < encoding.size(); i++) {
      final ConstraintList update = encoding.getUpdate(i);
      collectAllVariables(update, variables);
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
      final EFSMVariable var = mContext.getVariable(ident);
      if (var != null) {
        mUnprimedVariables.add(var);
      }
    }
    return null;
  }

  @Override
  public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
    throws VisitorException
  {
    final SimpleExpressionProxy lhs = expr.getLeft();
    collect(lhs);
    final SimpleExpressionProxy rhs = expr.getRight();
    collect(rhs);
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
        final EFSMVariable var = mContext.getVariable(subterm);
        if (var != null) {
          mPrimedVariables.add(var);
        }
      }
    } else {
      collect(subterm);
    }
    return null;
  }


  //#########################################################################
  //# Data Members
  private final UnaryOperator mNextOperator;
  private final EFSMVariableContext mContext;

  private Collection<EFSMVariable> mUnprimedVariables;
  private Collection<EFSMVariable> mPrimedVariables;

}
