//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariableCollector
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * A utility class to determine whether a given variable occurs
 * in an expression in primed or unprimed form.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

class EFSMVariableFinder
  extends DefaultModuleProxyVisitor
{


  //#########################################################################
  //# Constructor
  EFSMVariableFinder(final CompilerOperatorTable optable)
  {
    mNextOperator = optable.getNextOperator();
    mEqualityVisitor = ModuleEqualityVisitor.getInstance(false);
  }


  //#########################################################################
  //# Invocation
  /**
   * Determines if a given variable is in the given expression.
   * @param  expr     The expression to be searched.
   * @param  var      The variable to be searched for.
   */
  void findVariable(final SimpleExpressionProxy expr, final EFSMVariable var)
  {
    try {
      mContainsPrimedVariable = false;
      mContainsVariable = false;
      mCurrentVariable = var.getVariableName();
      find(expr);
    } finally {
      mCurrentVariable = null;
    }
  }

  void findVariable(final ConstraintList update, final EFSMVariable var)
  {
    try {
      mContainsPrimedVariable = false;
      mContainsVariable = false;
      mCurrentVariable = var.getVariableName();
      for (final SimpleExpressionProxy expr : update.getConstraints()) {
        find(expr);
        if (mContainsPrimedVariable && mContainsVariable) {
          break;
        }
      }
    } finally {
      mCurrentVariable = null;
    }
  }

  boolean containsVariable()
  {
    return mContainsVariable;
  }

  boolean containsPrimedVariable()
  {
    return mContainsPrimedVariable;
  }

  //#########################################################################
  //# Auxiliary Methods
  private void find(final SimpleExpressionProxy expr)
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
    if (mEqualityVisitor.equals(ident, mCurrentVariable)) {
      mContainsVariable = true;
    }
    return null;
  }

  @Override
  public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
    throws VisitorException
  {
    final SimpleExpressionProxy lhs = expr.getLeft();
    find(lhs);
    final SimpleExpressionProxy rhs = expr.getRight();
    find(rhs);
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
      if (mEqualityVisitor.equals(subterm, mCurrentVariable)) {
        mContainsPrimedVariable = true;
      }
    } else {
      find(subterm);
    }
    return null;
  }


  //#########################################################################
  //# Data Members
  private final ModuleEqualityVisitor mEqualityVisitor;
  private final UnaryOperator mNextOperator;

  private SimpleExpressionProxy mCurrentVariable;
  private boolean mContainsVariable;
  private boolean mContainsPrimedVariable;
}
