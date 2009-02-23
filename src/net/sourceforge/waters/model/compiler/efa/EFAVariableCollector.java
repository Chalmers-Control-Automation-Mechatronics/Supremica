//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariableCollector
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.Collection;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * A utility class to collect all the EFA variables (primed or not) in
 * an expression.
 *
 * @author Robi Malik
 */

class EFAVariableCollector
  extends AbstractModuleProxyVisitor
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

  public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
    throws VisitorException
  {
    final SimpleExpressionProxy lhs = expr.getLeft();
    collect(lhs);
    final SimpleExpressionProxy rhs = expr.getRight();
    collect(rhs);
    return null;
  }

  public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
  {
    return null;
  }

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
      collect(subterm);
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
