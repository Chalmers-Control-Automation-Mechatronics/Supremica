//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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
