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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.MultiExceptionModuleProxyVisitor;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.efa.ActionSyntaxException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ConditionalProxy;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>A utility class to help with the conversion of conditional blocks
 * ({@link ConditionalProxy}) to guard/action blocks ({@link
 * GuardActionBlockProxy}) and vice versa.</P>
 *
 * <P>This converter scans the top-level conjunction of a condition
 * and separates assignments from non-assignments. Non-assignments are
 * recorded as guards, and additionally traversed to check for nested
 * assignments, which are reported as errors. Assignments are recorded
 * as actions, and additionally their left and right subterms are traversed
 * to search for nested assignments.</P>
 *
 * <P>This class also performs validation of guard/action blocks.
 * Guards are traversed to check for any nested assignments, which are
 * reported as errors. Actions are reported as errors, unless they are
 * assignments, and their left and right subterms are traversed
 * to search for nested assignments.</P>
 *
 * @author Robi Malik
 */

class ConditionToGuardActionBlockConverter
  extends MultiExceptionModuleProxyVisitor
{
  //#########################################################################
  //# Constructor
  ConditionToGuardActionBlockConverter(final ModuleProxyFactory factory,
                                       final CompilerOperatorTable optable,
                                       final CompilationInfo info)
  {
    super(info);
    mFactory = factory;
    mOpTable = optable;
    mGuards = new LinkedList<>();
    mActions = new LinkedList<>();
  }

  ConditionToGuardActionBlockConverter
    (final ConditionToGuardActionBlockConverter converter)
  {
    super(converter.getCompilationInfo());
    mFactory = converter.mFactory;
    mOpTable = converter.mOpTable;
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
    mWhere = "subterm";
    try {
      cond.acceptVisitor(this);
    } catch (final VisitorException exception) {
      recordCaughtExceptionInVisitor(exception);
    }
  }

  /**
   * Adds the guards and actions from a guard/action block to the
   * converter.
   * @param  ga  The guard/action block to be included or <CODE>null</CODE>.
   */
  void addGuardActionBlock(final GuardActionBlockProxy ga)
    throws VisitorException
  {
    if (ga != null) {
      mWhere = "guard";
      final List<SimpleExpressionProxy> guards = ga.getGuards();
      for (final SimpleExpressionProxy guard : guards) {
        try {
          check(guard);
          mGuards.add(guard);
        } catch (final VisitorException exception) {
          recordCaughtExceptionInVisitor(exception);
        }
      }
      mWhere = "subterm";
      final List<BinaryExpressionProxy> actions = ga.getActions();
      for (final BinaryExpressionProxy action : actions) {
        if (!mOpTable.isAssignment(action)) {
          final ActionSyntaxException exception =
            new ActionSyntaxException(action);
          raiseInVisitor(exception);
        } else {
          try {
            checkAssignment(action);
            mActions.add(action);
          } catch (final VisitorException exception) {
            recordCaughtExceptionInVisitor(exception);
          }
        }
      }
    }
  }

  /**
   * Creates a guard/action block representing all formulas added by
   * previous calls to {@link #addCondition(SimpleExpressionProxy)
   * addCondition()} and {@link #addGuardActionBlock(GuardActionBlockProxy)
   * addGuardActionBlock()}.
   * @return The created guard/action block, or <CODE>null</CODE> if no
   *         guards or actions have been added.
   */
  GuardActionBlockProxy createGuardActionBlock()
  {
    if (mGuards.isEmpty() && mActions.isEmpty()) {
      return null;
    } else {
      final SimpleExpressionProxy conjuction = addToConjunction(null, mGuards);
      final List<SimpleExpressionProxy> guards =
        conjuction == null ? null : Collections.singletonList(conjuction);
      return mFactory.createGuardActionBlockProxy(guards, mActions, null);
    }
  }

  /**
   * Creates a formula representing the conjunction of all formulas added by
   * previous calls to {@link #addCondition(SimpleExpressionProxy)
   * addCondition()} and {@link #addGuardActionBlock(GuardActionBlockProxy)
   * addGuardActionBlock()}.
   * @return The condition formula, or <CODE>null</CODE> if no
   *         guards or actions have been added.
   */
  SimpleExpressionProxy createCondition()
  {
    if (mGuards.isEmpty() && mActions.isEmpty()) {
      return null;
    } else {
      final SimpleExpressionProxy guard = addToConjunction(null, mGuards);
      return addToConjunction(guard, mActions);
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
  //# Auxiliary Methods
  private void checkAssignment(final BinaryExpressionProxy action)
    throws VisitorException
  {
    final SimpleExpressionProxy lhs = action.getLeft();
    if (!(lhs instanceof IdentifierProxy)) {
      final ActionSyntaxException exception =
        new ActionSyntaxException(action, lhs);
      throw wrap(exception);
    } else {
      check(lhs);
      try {
        assert mPrimeLockOut == null;
        mPrimeLockOut = action;
        final SimpleExpressionProxy rhs = action.getRight();
        check(rhs);
      } finally {
        mPrimeLockOut = null;
      }
    }
  }

  private void check(final SimpleExpressionProxy expr)
    throws VisitorException
  {
    try {
      mAtTopLevel = false;
      expr.acceptVisitor(this);
    } catch (final VisitorException exception) {
      if (exception.getCause() instanceof EvalException) {
        final EvalException cause = (EvalException) exception.getCause();
        cause.provideLocation(expr);
      }
      throw exception;
    }
  }

  private SimpleExpressionProxy addToConjunction
    (SimpleExpressionProxy conjuction,
     final List<? extends SimpleExpressionProxy> expressions)
  {
    final BinaryOperator op = mOpTable.getAndOperator();
    for (final SimpleExpressionProxy expr : expressions) {
      if (conjuction == null) {
        conjuction = expr;
      } else {
        conjuction =
          mFactory.createBinaryExpressionProxy(op, conjuction, expr);
      }
    }
    return conjuction;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
    throws VisitorException
  {
    if (mAtTopLevel) {
      try {
        if (mOpTable.isAssignment(expr)) {
          checkAssignment(expr);
          mActions.add(expr);
        } else if (expr.getOperator() == mOpTable.getAndOperator()) {
          final SimpleExpressionProxy lhs = expr.getLeft();
          lhs.acceptVisitor(this);
          final SimpleExpressionProxy rhs = expr.getRight();
          rhs.acceptVisitor(this);
        } else {
          mAtTopLevel = false;
          final SimpleExpressionProxy lhs = expr.getLeft();
          lhs.acceptVisitor(this);
          final SimpleExpressionProxy rhs = expr.getRight();
          rhs.acceptVisitor(this);
          mGuards.add(expr);
        }
      } finally {
        mAtTopLevel = true;
      }
    } else {
      if (mOpTable.isAssignment(expr)) {
        final ActionSyntaxException exception =
          new ActionSyntaxException(expr, mWhere);
        throw wrap(exception);
      } else {
        final SimpleExpressionProxy lhs = expr.getLeft();
        lhs.acceptVisitor(this);
        final SimpleExpressionProxy rhs = expr.getRight();
        rhs.acceptVisitor(this);
      }
    }
    return null;
  }

  @Override
  public Object visitFunctionCallExpressionProxy
    (final FunctionCallExpressionProxy expr)
    throws VisitorException
  {
    final boolean wasAtTopLevel = mAtTopLevel;
    try {
      mAtTopLevel = false;
      final List<SimpleExpressionProxy> args = expr.getArguments();
      for (final SimpleExpressionProxy arg : args) {
        arg.acceptVisitor(this);
      }
      return visitSimpleExpressionProxy(expr);
    } finally {
      mAtTopLevel = wasAtTopLevel;
    }
  }

  @Override
  public Object visitIndexedIdentifierProxy
    (final IndexedIdentifierProxy ident)
    throws VisitorException
  {
    final boolean wasAtTopLevel = mAtTopLevel;
    final SimpleExpressionProxy oldLockOut = mPrimeLockOut;
    try {
      mAtTopLevel = false;
      if (mPrimeLockOut == null) {
        mPrimeLockOut = ident;
      }
      final List<SimpleExpressionProxy> indexes = ident.getIndexes();
      for (final SimpleExpressionProxy index : indexes) {
        index.acceptVisitor(this);
      }
      return visitIdentifierProxy(ident);
    } finally {
      mAtTopLevel = wasAtTopLevel;
      mPrimeLockOut = oldLockOut;
    }
  }

  @Override
  public Object visitQualifiedIdentifierProxy
    (final QualifiedIdentifierProxy ident)
    throws VisitorException
  {
    final boolean wasAtTopLevel = mAtTopLevel;
    try {
      mAtTopLevel = false;
      final IdentifierProxy base = ident.getBaseIdentifier();
      base.acceptVisitor(this);
      final IdentifierProxy comp = ident.getComponentIdentifier();
      comp.acceptVisitor(this);
      return visitSimpleExpressionProxy(ident);
    } finally {
      mAtTopLevel = wasAtTopLevel;
    }
  }

  @Override
  public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
  {
    if (mAtTopLevel) {
      mGuards.add(expr);
    }
    return null;
  }

  @Override
  public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
    throws VisitorException
  {
    final boolean wasAtTopLevel = mAtTopLevel;
    final SimpleExpressionProxy oldLockOut = mPrimeLockOut;
    try {
      if (expr.getOperator() == mOpTable.getNextOperator()) {
        if (mPrimeLockOut != null) {
          final NestedNextException exception;
          if (mPrimeLockOut instanceof BinaryExpressionProxy) {
            final BinaryExpressionProxy assignment =
              (BinaryExpressionProxy) mPrimeLockOut;
            exception = new NestedNextException(expr, assignment);
          } else if (mPrimeLockOut instanceof IndexedIdentifierProxy) {
            final IndexedIdentifierProxy ident =
              (IndexedIdentifierProxy) mPrimeLockOut;
            exception = new NestedNextException(expr, ident);
          } else if (mPrimeLockOut instanceof UnaryExpressionProxy) {
            final UnaryExpressionProxy primed =
              (UnaryExpressionProxy) mPrimeLockOut;
            exception = new NestedNextException(expr, primed);
          } else {
            throw new IllegalStateException
              ("Unknown prime lock-out class " +
               ProxyTools.getShortClassName(mPrimeLockOut) + "!");
          }
          throw wrap(exception);
        } else {
          mPrimeLockOut = expr;
        }
      }
      mAtTopLevel = false;
      final SimpleExpressionProxy subTerm = expr.getSubTerm();
      subTerm.acceptVisitor(this);
      return visitSimpleExpressionProxy(expr);
    } finally {
      mAtTopLevel = wasAtTopLevel;
      mPrimeLockOut = oldLockOut;
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOpTable;

  private final List<SimpleExpressionProxy> mGuards;
  private final List<BinaryExpressionProxy> mActions;

  private boolean mAtTopLevel;
  private String mWhere;
  private SimpleExpressionProxy mPrimeLockOut;

}
