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

public class PrimedVariableCollector
  extends DescendingModuleProxyVisitor
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new primed variable collector.
   * @param  optable     The operator table that defines the prime operator.
   * @param  context     The context used to determine whether a symbol is a
   *                     variable. If a non-<CODE>null</CODE> context is
   *                     provided, only primed expression whose subterm
   *                     is identified as a variable by the context are
   *                     considered as primed variables. Otherwise, if the
   *                     context is <CODE>null</CODE>, all primed subterms
   *                     are assumed to be next-state variables.
   */
  public PrimedVariableCollector(final CompilerOperatorTable optable,
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
  public void collectPrimedVariables
    (final SimpleExpressionProxy expr,
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
   * @param  exprs       A collection of expressions to be searched.
   * @param  vars        Found variables will be added in their primed form
   *                     to this set.
   */
  public void collectPrimedVariables
    (final Collection<SimpleExpressionProxy> exprs,
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

  /**
   * Checks whether the given expression contains a primed variable.
   * @param  expr        The expression to be searched.
   * @return The first subterm representing a primed variable that was found
   *         in the expression, or <CODE>null</CODE>.
   */
  public UnaryExpressionProxy containsPrimedVariable
    (final SimpleExpressionProxy expr)
  {
    try {
      mPrimedVariables = null;
      expr.acceptVisitor(this);
      return null;
    } catch (final PrimeFoundNotication notification) {
      return notification.getExpression();
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
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
      if (mContext == null || mContext.getVariableRange(expr) != null) {
        if (mPrimedVariables != null) {
          mPrimedVariables.addProxy(expr);
        } else {
          throw new PrimeFoundNotication(expr);
        }
      }
    } else {
      final SimpleExpressionProxy subterm = expr.getSubTerm();
      subterm.acceptVisitor(this);
    }
    return null;
  }


  //#########################################################################
  //# Inner Class PrimeFound
  private static class PrimeFoundNotication extends VisitorException
  {
    //#########################################################################
    //# Constructor
    private PrimeFoundNotication(final UnaryExpressionProxy expr)
    {
      mExpression = expr;
    }

    //#########################################################################
    //# Simple Access
    private UnaryExpressionProxy getExpression()
    {
      return mExpression;
    }

    //#########################################################################
    //# Data Members
    private final UnaryExpressionProxy mExpression;

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 3242959084160093395L;
  }


  //#########################################################################
  //# Data Members
  private final UnaryOperator mNextOperator;
  private final VariableContext mContext;

  private ProxyAccessorSet<UnaryExpressionProxy> mPrimedVariables;

}
