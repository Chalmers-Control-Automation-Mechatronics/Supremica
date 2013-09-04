//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariableCollector
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.base;

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
 * A utility class to determine whether a given variable occurs in an expression
 * in primed or unprimed form.
 * <p/>
 * @author Robi Malik
 */
public abstract class AbstractEFAVariableFinder<L, 
                                                V extends AbstractEFAVariable<L>>
 extends DefaultModuleProxyVisitor
{

  //#########################################################################
  //# Constructor
  protected AbstractEFAVariableFinder(final CompilerOperatorTable optable)
  {
    mNextOperator = optable.getNextOperator();
    mEqualityVisitor = ModuleEqualityVisitor.getInstance(false);
  }

  //#########################################################################
  //# Invocation
  /**
   * Determines if a given variable is in the given expression.
   * <p/>
   * @param expr The expression to be searched.
   * @param var  The variable to be searched for.
   * <p/>
   * @return <CODE>true</CODE> if the variable has been found in its primed or
   *         unprimed form. More detailed results can be queried using the
   *         {@link #containsVariable()} and {@link #containsPrimedVariable()}
   *         methods.
   */
  public boolean findVariable(final SimpleExpressionProxy expr, final V var)
  {
    try {
      mCurrentVariable = var.getVariableName();
      mContainsVariable = false;
      mContainsPrimedVariable = false;
      find(expr);
      return mContainsVariable || mContainsPrimedVariable;
    } finally {
      mCurrentVariable = null;
    }
  }

  /**
   * Determines if a given variable is in the given constraint list.
   * <p/>
   * @param constraints The constraint list to be searched.
   * @param var    The variable to be searched for.
   * <p/>
   * @return <CODE>true</CODE> if the variable has been found in its primed or
   *         unprimed form. More detailed results can be queried using the
   *         {@link #containsVariable()} and {@link #containsPrimedVariable()}
   *         methods.
   */
  public boolean findVariable(final ConstraintList constraints, final V var)
  {
    try {
      mCurrentVariable = var.getVariableName();
      mContainsVariable = false;
      mContainsPrimedVariable = false;
      for (final SimpleExpressionProxy expr : constraints.getConstraints()) {
        find(expr);
        if (mContainsPrimedVariable && mContainsVariable) {
          return true;
        }
      }
      return mContainsVariable || mContainsPrimedVariable;
    } finally {
      mCurrentVariable = null;
    }
  }

  /**
   * Determines if a given expression contains a primed identifier.
   * <p/>
   * @param expr The expression to be searched.
   */
  public boolean findPrime(final SimpleExpressionProxy expr)
  {
    try {
      mCurrentVariable = null;
      mContainsVariable = true;
      mContainsPrimedVariable = false;
      find(expr);
      return mContainsPrimedVariable;
    } finally {
      mContainsVariable = false;
    }
  }

  /**
   * Determines if a given constraint list contains a primed identifier.
   * <p/>
   * @param constraints The constraint list to be searched.
   */
  public boolean findPrime(final ConstraintList constraints)
  {
    try {
      mCurrentVariable = null;
      mContainsVariable = true;
      mContainsPrimedVariable = false;
      for (final SimpleExpressionProxy expr : constraints.getConstraints()) {
        find(expr);
        if (mContainsPrimedVariable) {
          return true;
        }
      }
      return false;
    } finally {
      mContainsVariable = false;
    }
  }

  /**
   * Determines if a given expression contains a primed identifier.
   * <p/>
   * @param expr The expression to be searched.
   */
  public boolean findPrimeVariable(final SimpleExpressionProxy expr, final V var)
  {
    try {
      mCurrentVariable = var.getVariableName();
      mContainsVariable = true;
      mContainsPrimedVariable = false;
      find(expr);
      return mContainsPrimedVariable;
    } finally {
      mContainsVariable = false;
      mCurrentVariable = null;
    }
  }

  /**
   * Determines if a given constraint list contains a primed identifier.
   * <p/>
   * @param constraints The constraint list to be searched.
   */
  public boolean findPrimeVariable(final ConstraintList constraints, final V var)
  {
    try {
      mCurrentVariable = var.getVariableName();
      mContainsVariable = true;
      mContainsPrimedVariable = false;
      for (final SimpleExpressionProxy expr : constraints.getConstraints()) {
        find(expr);
        if (mContainsPrimedVariable) {
          return true;
        }
      }
      return false;
    } finally {
      mContainsVariable = false;
      mCurrentVariable = null;
    }
  }

  /**
   * Returns whether the last search has found an unprimed variable.
   */
  public boolean containsVariable()
  {
    return mContainsVariable;
  }

  /**
   * Returns whether the last search has found a primed variable.
   */
  public boolean containsPrimedVariable()
  {
    return mContainsPrimedVariable;
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public Object visitIdentifierProxy(final IdentifierProxy ident)
  {
    if (mCurrentVariable != null && mEqualityVisitor.equals(ident,
                                                            mCurrentVariable)) {
      mContainsVariable = true;
    }
    return null;
  }
  @Override
  public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
   throws VisitorException
  {
    final SimpleExpressionProxy lhs = expr.getLeft();
    lhs.acceptVisitor(this);
    if (mContainsVariable && mContainsPrimedVariable) {
      return null;
    }
    final SimpleExpressionProxy rhs = expr.getRight();
    rhs.acceptVisitor(this);
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
      if (mCurrentVariable == null || mEqualityVisitor.equals(subterm,
                                                              mCurrentVariable)) {
        mContainsPrimedVariable = true;
      }
    } else {
      subterm.acceptVisitor(this);
    }
    return null;
  }

  //#########################################################################
    private void find(final SimpleExpressionProxy expr)
  {
    try {
      expr.acceptVisitor(this);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }
  
  //#########################################################################
  //# Data Members
  private final ModuleEqualityVisitor mEqualityVisitor;
  private final UnaryOperator mNextOperator;
  private SimpleExpressionProxy mCurrentVariable;
  private boolean mContainsVariable;
  private boolean mContainsPrimedVariable;
}
