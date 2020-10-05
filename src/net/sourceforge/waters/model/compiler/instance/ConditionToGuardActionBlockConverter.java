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

package net.sourceforge.waters.model.compiler.instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ConditionalProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>A utility class to help with the conversion of conditional blocks
 * ({@link ConditionalProxy}) to guard/action blocks ({@link
 * GuardActionBlockProxy}).</P>
 *
 * <P>This converter identifies assignments within an expression and
 * replaces them with equalities involving the next operator, for example,
 * replacing <CODE>x&nbsp;+=&nbsp;1</CODE> by <CODE>x'&nbsp;==&nbsp;x+1</CODE>.
 * Additionally, it recognises the top level of an AND expression, where
 * assignments are detected and converted to actions of a guard/action
 * block.</P>
 *
 * @author Robi Malik
 */

class ConditionToGuardActionBlockConverter
  extends DefaultModuleProxyVisitor
{
  //#########################################################################
  //# Constructor
  ConditionToGuardActionBlockConverter(final ModuleProxyFactory factory,
                                       final CompilerOperatorTable optable,
                                       final CompilationInfo info)
  {
    mFactory = factory;
    mOpTable = optable;
    mCompilationInfo = info;
    mGuards = new LinkedList<>();
    mActions = new LinkedList<>();
  }

  ConditionToGuardActionBlockConverter
    (final ConditionToGuardActionBlockConverter converter)
  {
    mFactory = converter.mFactory;
    mOpTable = converter.mOpTable;
    mCompilationInfo = converter.mCompilationInfo;
    mGuards = new LinkedList<>(converter.mGuards);
    mActions = new LinkedList<>(converter.mActions);
  }


  //#########################################################################
  //# Invocation
  /**
   * Converts an expression to guards and actions.
   */
  void addCondition(final SimpleExpressionProxy cond)
    throws VisitorException
  {
    mAtTopLevel = true;
    cond.acceptVisitor(this);
  }

  /**
   * Adds the guards and actions from a guard/action block to the
   * converter.
   * @param  ga  The guard/action block to be included or <CODE>null</CODE>.
   */
  void addGuardActionBlock(final GuardActionBlockProxy ga)
  {
    if (ga != null) {
      final List<SimpleExpressionProxy> guards = ga.getGuards();
      mGuards.addAll(guards);
      final List<BinaryExpressionProxy> actions = ga.getActions();
      mActions.addAll(actions);
    }
  }

  /**
   * Creates a guard/action block representing all formulas added by
   * previous calls to {@link #addCondition(SimpleExpressionProxy)
   * addCondition()} and {@link #addGuardActionBlock(GuardActionBlockProxy)
   * addGuardActionBlock()}.
   * @return The created guard/action block, or <CODE>null</CODE> if no
   *         guards and actions have been added.
   */
  GuardActionBlockProxy createGuardActionBlock()
  {
    if (mGuards.isEmpty() && mActions.isEmpty()) {
      return null;
    } else {
      final BinaryOperator op = mOpTable.getAndOperator();
      SimpleExpressionProxy conjuction = null;
      for (final SimpleExpressionProxy guard : mGuards) {
        if (conjuction == null) {
          conjuction = guard;
        } else {
          conjuction =
            mFactory.createBinaryExpressionProxy(op, conjuction, guard);
        }
      }
      final List<SimpleExpressionProxy> guards =
        conjuction == null ? null : Collections.singletonList(conjuction);
      return mFactory.createGuardActionBlockProxy(guards, mActions, null);
    }
  }

  /**
   * Resets the converter, removing any guards and actions found in
   * previous calls to {@link #addCondition(SimpleExpressionProxy)
   * addCondition()} or {@link #addGuardActionBlock(GuardActionBlockProxy)
   * addGuardActionBlock()}.
   */
  void clear()
  {
    mGuards.clear();
    mActions.clear();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public BinaryExpressionProxy visitBinaryExpressionProxy
    (final BinaryExpressionProxy expr)
    throws VisitorException
  {
    final SimpleExpressionProxy lhs0 = expr.getLeft();
    final SimpleExpressionProxy rhs0 = expr.getRight();
    final BinaryOperator op = expr.getOperator();
    if (mOpTable.isAssignment(expr)) {
      if (mAtTopLevel) {
        mActions.add(expr);
        return null;
      } else {
        final BinaryOperator modifier = mOpTable.getAssigningOperator(op);
        final UnaryOperator prime = mOpTable.getNextOperator();
        final UnaryExpressionProxy lhs1 =
          mFactory.createUnaryExpressionProxy(prime, lhs0);
        final SimpleExpressionProxy rhs1;
        if (modifier == null) {
          rhs1 = rhs0;
        } else {
          rhs1 = mFactory.createBinaryExpressionProxy(modifier, lhs0, rhs0);
        }
        final BinaryOperator eq = mOpTable.getEqualsOperator();
        final BinaryExpressionProxy expr1 =
          mFactory.createBinaryExpressionProxy(eq, lhs1, rhs1);
        mCompilationInfo.add(expr1, expr);
        return expr1;
      }
    }
    if (!mAtTopLevel) {
      final SimpleExpressionProxy lhs1 =
        (SimpleExpressionProxy) lhs0.acceptVisitor(this);
      final SimpleExpressionProxy rhs1 =
        (SimpleExpressionProxy) rhs0.acceptVisitor(this);
      if (lhs0 == lhs1 && rhs0 == rhs1) {
        return expr;
      } else {
        final BinaryExpressionProxy expr1 =
          mFactory.createBinaryExpressionProxy(op, lhs1, rhs1);
        mCompilationInfo.add(expr1, expr);
        return expr1;
      }
    } else if (op == mOpTable.getAndOperator()) {
      lhs0.acceptVisitor(this);
      rhs0.acceptVisitor(this);
      return null;
    } else {
      try {
        mAtTopLevel = false;
        final SimpleExpressionProxy lhs1 =
          (SimpleExpressionProxy) lhs0.acceptVisitor(this);
        final SimpleExpressionProxy rhs1 =
          (SimpleExpressionProxy) rhs0.acceptVisitor(this);
        if (lhs0 == lhs1 && rhs0 == rhs1) {
          mGuards.add(expr);
        } else {
          final BinaryExpressionProxy expr1 =
            mFactory.createBinaryExpressionProxy(op, lhs1, rhs1);
          mCompilationInfo.add(expr1, expr);
          mGuards.add(expr1);
        }
      } finally {
        mAtTopLevel = true;
      }
      return null;
    }
  }

  @Override
  public FunctionCallExpressionProxy visitFunctionCallExpressionProxy
    (final FunctionCallExpressionProxy expr)
    throws VisitorException
  {
    final List<SimpleExpressionProxy> args0 = expr.getArguments();
    final List<SimpleExpressionProxy> args1 = new ArrayList<>(args0.size());
    boolean change = false;
    for (final SimpleExpressionProxy arg0 : args0) {
      final SimpleExpressionProxy arg1 =
        (SimpleExpressionProxy) arg0.acceptVisitor(this);
      args1.add(arg0);
      change |= arg0 != arg1;
    }
    final FunctionCallExpressionProxy expr1;
    if (!change) {
      expr1 = expr;
    } else {
      final String function = expr.getFunctionName();
      expr1 = mFactory.createFunctionCallExpressionProxy(function, args1);
      mCompilationInfo.add(expr1, expr);
    }
    if (mAtTopLevel) {
      mGuards.add(expr1);
      return null;
    } else {
      return expr1;
    }
  }

  @Override
  public SimpleExpressionProxy visitSimpleExpressionProxy
    (final SimpleExpressionProxy expr)
  {
    if (mAtTopLevel) {
      mGuards.add(expr);
      return null;
    } else {
      return expr;
    }
  }

  @Override
  public UnaryExpressionProxy visitUnaryExpressionProxy
    (final UnaryExpressionProxy expr)
    throws VisitorException
  {
    final SimpleExpressionProxy subTerm0 = expr.getSubTerm();
    final SimpleExpressionProxy subTerm1 =
      (SimpleExpressionProxy) subTerm0.acceptVisitor(this);
    final UnaryExpressionProxy expr1;
    if (subTerm0 == subTerm1) {
      expr1 = expr;
    } else {
      final UnaryOperator op = expr.getOperator();
      expr1 = mFactory.createUnaryExpressionProxy(op, subTerm1);
      mCompilationInfo.add(expr1, expr);
    }
    if (mAtTopLevel) {
      mGuards.add(expr1);
      return null;
    } else {
      return expr1;
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOpTable;
  private final CompilationInfo mCompilationInfo;

  private final List<SimpleExpressionProxy> mGuards;
  private final List<BinaryExpressionProxy> mActions;

  private boolean mAtTopLevel;

}
