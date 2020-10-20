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

package net.sourceforge.waters.model.compiler.context;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorSet;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.MultiExceptionModuleProxyVisitor;
import net.sourceforge.waters.model.compiler.efa.ActionSyntaxException;
import net.sourceforge.waters.model.compiler.instance.NestedNextException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ConditionalProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * <P>A utility to simplify expressions while preserving the presence of
 * primed variables.</P>
 *
 * <P>Based on {@link SimpleExpressionCompiler}, the guard/action compiler
 * checks and simplifies Boolean expressions as much as possible while
 * ensuring that any primed variable that appears in the input is
 * still present in the output. This is achieved by adding equations of
 * the form <CODE>x'&nbsp;==&nbsp;x'</CODE> to the result if necessary.
 * Effort is made to re-use such equations if they are already present in
 * the input and only create them if necessary.</P>
 *
 * <P>Prior to simplification, structure checking is performed to ensure
 * reasonable use of primes and assignments:</P>
 * <UL>
 * <LI>The next-state (prime) operator cannot be used within assignment
 *     expressions, within array indexes, or nested within another prime.</LI>
 * <LI>Assignments are only allowed as actions, not guards within a
 *     guard/action block. They are allowed within conditionals, but
 *     only at the topmost level or as immediate subterms of a conjunction at
 *     the topmost level.</LI>
 * </UL>
 * <P>If an expression to be simplified fails to meet these requirements,
 * simplification will fail and a {@link NestedNextException} or {@link
 * ActionSyntaxException} will be produced.</P>
 *
 * @author Robi Malik
 */

public class GuardActionCompiler
{
  //#########################################################################
  //# Constructor
  public GuardActionCompiler(final ModuleProxyFactory factory,
                             final CompilerOperatorTable optable,
                             final CompilationInfo compilationInfo)
  {
    mInternalFactory = ModuleElementFactory.getInstance();
    mOutputFactory = factory;
    mSimplifier = new SimpleExpressionCompiler
      (mInternalFactory, compilationInfo, optable, false);
  }


  //#########################################################################
  //# Invocation
  public Result separateGuardActionBlock
    (final GuardActionBlockProxy ga,
     final BindingContext context)
    throws EvalException
  {
    try {
      mContext = context;
      mResult = new Result();
      final CompilationInfo compilationInfo = getCompilationInfo();
      final GuardActionSeparator separator =
        new GuardActionSeparator(compilationInfo);
      separator.separateGuardActionBlock(ga);
      simplify();
      return mResult;
    } finally {
      mContext = null;
      mResult = null;
    }
  }

  public Result separateCondition
    (final SimpleExpressionProxy cond,
     final BindingContext context,
     final boolean generatingConditionals)
    throws EvalException
  {
    try {
      mResult = new Result();
      if (cond != null) {
        mContext = context;
        final CompilationInfo compilationInfo = getCompilationInfo();
        final GuardActionSeparator separator =
          new GuardActionSeparator(compilationInfo);
        separator.separateCondition(cond);
        if (generatingConditionals) {
          mResult.clear();
          final SimpleExpressionProxy guard = simplify(cond, null);
          mResult.addGuard(guard);
        } else {
          simplify();
        }
      }
      return mResult;
    } finally {
      mContext = null;
      mResult = null;
    }
  }

  public SimpleExpressionProxy createSingleGuard
    (final List<SimpleExpressionProxy> guards)
  {
    return addToConjunction(mOutputFactory, null, guards);
  }

  public SimpleExpressionProxy createCondition
    (final List<SimpleExpressionProxy> guards,
     final List<BinaryExpressionProxy> actions)
  {
    final SimpleExpressionProxy guard =
      addToConjunction(mOutputFactory, null, guards);
    return addToConjunction(mOutputFactory, guard, actions);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void simplify()
    throws EvalException
  {
    final List<BinaryExpressionProxy> actions = mResult.getActions();
    final Collection<IdentifierProxy> assigned = new THashSet<>(actions.size());
    final ListIterator<BinaryExpressionProxy> iter = actions.listIterator();
    while (iter.hasNext()) {
      try {
        final BinaryExpressionProxy action = iter.next();
        final BinaryExpressionProxy simplified =
          (BinaryExpressionProxy) mSimplifier.simplify(action, mContext);
        if (SimpleExpressionCompiler.isBooleanFalse(simplified)) {
          mResult.clear();
          mResult.addGuard(simplified);
          return;
        }
        final IdentifierProxy ident = (IdentifierProxy) simplified.getLeft();
        assigned.add(ident);
        iter.set(simplified);
      } catch (final EvalException exception) {
        iter.remove();
        final CompilationInfo compilationInfo = getCompilationInfo();
        compilationInfo.raise(exception);
      }
    }
    final List<SimpleExpressionProxy> guards = mResult.getGuards();
    final SimpleExpressionProxy guard =
      addToConjunction(mInternalFactory, null, guards);
    guards.clear();
    if (guard != null) {
      try {
        final SimpleExpressionProxy simplified = simplify(guard, assigned);
        if (simplified != null) {
          guards.add(simplified);
          if (SimpleExpressionCompiler.isBooleanFalse(simplified)) {
            actions.clear();
          }
        }
      } catch (final EvalException exception) {
        final CompilationInfo compilationInfo = getCompilationInfo();
        compilationInfo.raise(exception);
      }
    }
  }

  private SimpleExpressionProxy simplify
    (final SimpleExpressionProxy expr,
     final Collection<IdentifierProxy> assigned)
    throws EvalException
  {
    final ModuleEqualityVisitor equality = getEquality();
    final ProxyAccessorSet<IdentifierProxy> before =
      new ProxyAccessorHashSet<>(equality);
    final Map<SimpleExpressionProxy,IdentifierProxy> map = new HashMap<>();
    SimpleExpressionProxy simplified =
      mSmartPrimeFinder.simplifyIndexesAndCollectPrimes(expr, before, map);
    simplified = mSimplifier.simplify(simplified, mContext);
    if (!before.isEmpty() &&
        !SimpleExpressionCompiler.isBooleanFalse(simplified)) {
      final ProxyAccessorSet<IdentifierProxy> after;
      if (assigned == null) {
        after = new ProxyAccessorHashSet<>(equality, before.size());
      } else {
        before.addAll(assigned);
        after = new ProxyAccessorHashSet<>(equality,
          before.size() + assigned.size());
        after.addAll(assigned);
      }
      mSmartPrimeFinder.collectPrimes(simplified, after);
      if (before.size() > after.size()) {
        boolean canImprove = false;
        for (final IdentifierProxy nondet : map.values()) {
          if (!after.containsProxy(nondet)) {
            canImprove = true;
            break;
          }
        }
        if (canImprove) {
          simplified = mSmartSimplifier.simplify(expr, after, map);
        }
        if (before.size() > after.size() &&
            !SimpleExpressionCompiler.isBooleanFalse(simplified)) {
          final int size = before.size() - after.size();
          final List<IdentifierProxy> list = new ArrayList<>(size);
          for (final IdentifierProxy ident : before) {
            if (!after.containsProxy(ident)) {
              list.add(ident);
            }
          }
          Collections.sort(list);
          final CompilerOperatorTable optable = getOperatorTable();
          final BinaryOperator and = optable.getAndOperator();
          final BinaryOperator eq = optable.getEqualsOperator();
          final UnaryOperator next = optable.getNextOperator();
          for (final IdentifierProxy ident : list) {
            final UnaryExpressionProxy nextIdent = mInternalFactory.
              createUnaryExpressionProxy(next, ident);
            final BinaryExpressionProxy eqn = mInternalFactory.
              createBinaryExpressionProxy(eq, nextIdent, nextIdent);
            if (SimpleExpressionCompiler.isBooleanTrue(simplified)) {
              simplified = eqn;
            } else {
              simplified = mInternalFactory.
                createBinaryExpressionProxy(and, simplified, eqn);
            }
          }
          getCompilationInfo().add(simplified, expr);
        }
      }
    }
    if (SimpleExpressionCompiler.isBooleanTrue(simplified)) {
      return null;
    } else {
      return simplified;
    }
  }

  private SimpleExpressionProxy addToConjunction
    (final ModuleProxyFactory factory,
     SimpleExpressionProxy conjunction,
     final List<? extends SimpleExpressionProxy> expressions)
  {
    final BinaryOperator op = getOperatorTable().getAndOperator();
    for (final SimpleExpressionProxy expr : expressions) {
      if (SimpleExpressionCompiler.isBooleanFalse(expr)) {
        return expr;
      } else if (SimpleExpressionCompiler.isBooleanTrue(expr)) {
        continue;
      } else if (conjunction == null) {
        conjunction = expr;
      } else {
        conjunction =
          factory.createBinaryExpressionProxy(op, conjunction, expr);
      }
    }
    return conjunction;
  }


  //#########################################################################
  //# Simple Access
  private CompilerOperatorTable getOperatorTable()
  {
    return mSimplifier.getOperatorTable();
  }

  private ModuleEqualityVisitor getEquality()
  {
    return mSimplifier.getEquality();
  }

  private CompilationInfo getCompilationInfo()
  {
    return mSimplifier.getCompilationInfo();
  }


  //#########################################################################
  //# Inner Class Result
  public static class Result
  {
    //#######################################################################
    //# Simple Access
    public List<SimpleExpressionProxy> getGuards()
    {
      return mGuards;
    }

    public List<BinaryExpressionProxy> getActions()
    {
      return mActions;
    }

    public boolean isBooleanTrue()
    {
      return mGuards.isEmpty() && mActions.isEmpty();
    }

    public boolean isBooleanFalse()
    {
      if (mGuards.isEmpty()) {
        return false;
      } else {
        final SimpleExpressionProxy guard = mGuards.get(0);
        return SimpleExpressionCompiler.isBooleanFalse(guard);
      }
    }

    //#######################################################################
    //# Data Gathering
    private void addAction(final BinaryExpressionProxy action)
    {
      assert action != null;
      mActions.add(action);
    }

    private void addGuard(final SimpleExpressionProxy guard)
    {
      if (guard != null) {
        mGuards.add(guard);
      }
    }

    private void clear()
    {
      mGuards.clear();
      mActions.clear();
    }

    //#######################################################################
    //# Data Members
    private final List<SimpleExpressionProxy> mGuards = new LinkedList<>();
    private final List<BinaryExpressionProxy> mActions = new LinkedList<>();
  }


  //#########################################################################
  //# Inner Class GuardActionSeparator
  private class GuardActionSeparator
    extends MultiExceptionModuleProxyVisitor
  {
    //#######################################################################
    //# Constructor
    private GuardActionSeparator(final CompilationInfo compilationInfo)
    {
      super(compilationInfo);
    }

    //#######################################################################
    //# Invocation
    /**
     * Checks and converts a condition to guards and actions.
     * @param  cond   The condition to be separated
     *                (typically from a {@link ConditionalProxy}).
     * @throws EvalException to report failures about incorrect nesting
     *                of primes or assignment operators.
     */
    void separateCondition(final SimpleExpressionProxy cond)
      throws EvalException
    {
      mAtTopLevel = true;
      mWhere = "nested subterm";
      try {
        final boolean stored = (Boolean) cond.acceptVisitor(this);
        if (!stored) {
          mResult.addGuard(cond);
        }
      } catch (final VisitorException exception) {
        recordCaughtException(exception, cond);
      }
    }

    /**
     * Checks and adds the guards and actions from a guard/action block to the
     * converter.
     * @param  ga     The guard/action block to be included,
     *                or <CODE>null</CODE>.
     * @throws EvalException to report failures about incorrect nesting
     *                of primes or assignment operators.
     */
    void separateGuardActionBlock(final GuardActionBlockProxy ga)
      throws EvalException
    {
      if (ga != null) {
        mWhere = "guard";
        final List<SimpleExpressionProxy> guards = ga.getGuards();
        for (final SimpleExpressionProxy guard : guards) {
          try {
            checkInner(guard);
            mResult.addGuard(guard);
          } catch (final VisitorException exception) {
            recordCaughtException(exception, guard);
          }
        }
        final CompilerOperatorTable optable = getOperatorTable();
        mWhere = "another assignment";
        final List<BinaryExpressionProxy> actions = ga.getActions();
        for (final BinaryExpressionProxy action : actions) {
          if (!optable.isAssignment(action)) {
            final ActionSyntaxException exception =
              new ActionSyntaxException(action);
            recordCaughtException(exception);
          } else {
            try {
              checkAssignment(action);
              mResult.addAction(action);
            } catch (final VisitorException exception) {
              recordCaughtException(exception, action);
            }
          }
        }
      }
    }

    //#######################################################################
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
        checkInner(lhs);
        try {
          assert mPrimeLockOut == null;
          mPrimeLockOut = action;
          final SimpleExpressionProxy rhs = action.getRight();
          checkInner(rhs);
        } finally {
          mPrimeLockOut = null;
        }
      }
    }

    private void checkInner(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      final boolean wasAtTopLevel = mAtTopLevel;
      try {
        mAtTopLevel = false;
        expr.acceptVisitor(this);
      } finally {
        mAtTopLevel = wasAtTopLevel;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Boolean visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final CompilerOperatorTable optable = getOperatorTable();
      if (mAtTopLevel) {
        try {
          if (optable.isAssignment(expr)) {
            checkAssignment(expr);
            mResult.addAction(expr);
            return true;
          } else if (expr.getOperator() == optable.getAndOperator()) {
            final SimpleExpressionProxy lhs = expr.getLeft();
            final boolean storedLHS = (Boolean) lhs.acceptVisitor(this);
            final SimpleExpressionProxy rhs = expr.getRight();
            final boolean storedRHS = (Boolean) rhs.acceptVisitor(this);
            if (storedLHS || storedRHS) {
              if (storedLHS && !storedRHS) {
                mResult.addGuard(rhs);
              } else if (!storedLHS && storedRHS) {
                mResult.addGuard(lhs);
              }
              return true;
            }
          } else {
            mAtTopLevel = false;
            final SimpleExpressionProxy lhs = expr.getLeft();
            lhs.acceptVisitor(this);
            final SimpleExpressionProxy rhs = expr.getRight();
            rhs.acceptVisitor(this);
          }
        } finally {
          mAtTopLevel = true;
        }
      } else {
        if (optable.isAssignment(expr)) {
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
      return false;
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
    public Boolean visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    {
      return false;
    }

    @Override
    public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final boolean wasAtTopLevel = mAtTopLevel;
      final SimpleExpressionProxy oldLockOut = mPrimeLockOut;
      try {
        final CompilerOperatorTable optable = getOperatorTable();
        if (expr.getOperator() == optable.getNextOperator()) {
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

    //#######################################################################
    //# Data Members
    private boolean mAtTopLevel;
    private String mWhere;
    private SimpleExpressionProxy mPrimeLockOut;
  }


  //#########################################################################
  //# Inner Class SmartPrimeFinder
  private class SmartPrimeFinder extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private SimpleExpressionProxy simplifyIndexesAndCollectPrimes
      (final SimpleExpressionProxy expr,
       final ProxyAccessorSet<IdentifierProxy> output,
       final Map<SimpleExpressionProxy,IdentifierProxy> map)
      throws EvalException
    {
      try {
        mCollectedIdentifiers = output;
        mNondeterminismMap = map;
        mAtTopLevel = true;
        mSimplifying = true;
        mWithinPrime = false;
        return (SimpleExpressionProxy) expr.acceptVisitor(this);
      } catch (final VisitorException exception) {
        if (exception.getCause() instanceof EvalException) {
          throw (EvalException) exception.getCause();
        } else {
          throw exception.getRuntimeException();
        }
      } finally {
        mCollectedIdentifiers = null;
        mNondeterminismMap = null;
        mWithinPrime = false;
      }
    }

    private void collectPrimes
      (final SimpleExpressionProxy expr,
       final ProxyAccessorSet<IdentifierProxy> output)
      throws EvalException
    {
      try {
        mCollectedIdentifiers = output;
        mNondeterminismMap = null;
        mAtTopLevel = false;
        mSimplifying = false;
        mWithinPrime = false;
        expr.acceptVisitor(this);
      } catch (final VisitorException exception) {
        if (exception.getCause() instanceof EvalException) {
          throw (EvalException) exception.getCause();
        } else {
          throw exception.getRuntimeException();
        }
      } finally {
        mCollectedIdentifiers = null;
        mWithinPrime = false;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public BinaryExpressionProxy visitBinaryExpressionProxy
      (BinaryExpressionProxy expr)
      throws VisitorException
    {
      final CompilerOperatorTable optable = getOperatorTable();
      final boolean wasAtTopLevel = mAtTopLevel;
      final BinaryOperator op = expr.getOperator();
      try {
        mAtTopLevel &= op == optable.getAndOperator();
        final SimpleExpressionProxy left0 = expr.getLeft();
        final SimpleExpressionProxy left1 =
          (SimpleExpressionProxy) left0.acceptVisitor(this);
        final SimpleExpressionProxy right0 = expr.getRight();
        final SimpleExpressionProxy right1 =
          (SimpleExpressionProxy) right0.acceptVisitor(this);
        if (left1 != left0 || right1 != right0) {
          final BinaryExpressionProxy newExpr =
            mInternalFactory.createBinaryExpressionProxy(op, left1, right1);
          getCompilationInfo().add(newExpr, expr);
          expr = newExpr;
        }
      } finally {
        mAtTopLevel = wasAtTopLevel;
      }
      if (mAtTopLevel && op == optable.getEqualsOperator()) {
        final ModuleEqualityVisitor eq = getEquality();
        final SimpleExpressionProxy left = expr.getLeft();
        final SimpleExpressionProxy right = expr.getRight();
        if (left instanceof UnaryExpressionProxy && eq.equals(left, right)) {
          final UnaryExpressionProxy unary = (UnaryExpressionProxy) left;
          final SimpleExpressionProxy subTerm = unary.getSubTerm();
          if (unary.getOperator() == optable.getNextOperator() &&
              subTerm instanceof IdentifierProxy) {
            final IdentifierProxy ident = (IdentifierProxy) subTerm;
            if (mCollectedIdentifiers.containsProxy(ident)) {
              mNondeterminismMap.put(expr, ident);
            }
          }
        }
      }
      if (optable.isAssignment(expr)) {
        final IdentifierProxy ident = (IdentifierProxy) expr.getLeft();
        mCollectedIdentifiers.addProxy(ident);
      }
      return expr;
    }

    @Override
    public FunctionCallExpressionProxy visitFunctionCallExpressionProxy
      (FunctionCallExpressionProxy expr)
      throws VisitorException
    {
      final List<SimpleExpressionProxy> args0 = expr.getArguments();
      final List<SimpleExpressionProxy> args1 = new ArrayList<>(args0.size());
      boolean change = false;
      for (final SimpleExpressionProxy arg0 : args0) {
        final SimpleExpressionProxy arg1 =
          (SimpleExpressionProxy) arg0.acceptVisitor(this);
        args1.add(arg0);
        change |= arg1 != arg0;
      }
      if (change) {
        final String name = expr.getFunctionName();
        final FunctionCallExpressionProxy newExpr =
          mInternalFactory.createFunctionCallExpressionProxy(name, args1);
        getCompilationInfo().add(newExpr, expr);
        expr = newExpr;
      }
      return expr;
    }

    @Override
    public SimpleExpressionProxy visitIdentifierProxy
      (final IdentifierProxy ident)
    {
      if (mWithinPrime &&
          !mWithinQualification &&
          isPossibleVariable(ident)) {
        mCollectedIdentifiers.addProxy(ident);
      }
      return ident;
    }

    @Override
    public SimpleExpressionProxy visitIndexedIdentifierProxy
      (IndexedIdentifierProxy ident)
      throws VisitorException
    {
      if (mWithinPrime && mSimplifying) {
        final List<SimpleExpressionProxy> indexes0 = ident.getIndexes();
        final List<SimpleExpressionProxy> indexes1 =
          new ArrayList<>(indexes0.size());
        boolean change = false;
        for (final SimpleExpressionProxy index0 : indexes0) {
          try {
            final SimpleExpressionProxy index1 =
              mSimplifier.simplify(index0, mContext);
            indexes1.add(index1);
            change |= index1 != index0;
          } catch (final EvalException exception) {
            throw wrap(exception);
          }
        }
        if (change) {
          final String name = ident.getName();
          final IndexedIdentifierProxy newIdent =
            mInternalFactory.createIndexedIdentifierProxy(name, indexes1);
          getCompilationInfo().add(newIdent, ident);
          ident = newIdent;
        }
      }
      return ident;
    }

    @Override
    public SimpleExpressionProxy visitQualifiedIdentifierProxy
      (QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      if (mWithinPrime && mSimplifying) {
        final boolean wasWithinQualification = mWithinQualification;
        try {
          mWithinQualification = true;
          final IdentifierProxy base0 = ident.getBaseIdentifier();
          final IdentifierProxy base1 =
            (IdentifierProxy) base0.acceptVisitor(this);
          final IdentifierProxy comp0 = ident.getComponentIdentifier();
          final IdentifierProxy comp1 =
            (IdentifierProxy) comp0.acceptVisitor(this);
          if (base1 != base0 || comp1 != comp0) {
            final QualifiedIdentifierProxy newIdent =
              mInternalFactory.createQualifiedIdentifierProxy(base1, comp1);
            getCompilationInfo().add(newIdent, ident);
            ident = newIdent;
          }
        } finally {
          mWithinQualification = wasWithinQualification;
        }
      }
      return visitIdentifierProxy(ident);
    }

    @Override
    public SimpleExpressionProxy visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
    {
      return expr;
    }

    @Override
    public UnaryExpressionProxy visitUnaryExpressionProxy
      (UnaryExpressionProxy expr)
      throws VisitorException
    {
      final boolean wasWithinPrime = mWithinPrime;
      try {
        final CompilerOperatorTable optable = getOperatorTable();
        final UnaryOperator op = expr.getOperator();
        mWithinPrime |= op == optable.getNextOperator();
        final SimpleExpressionProxy subTerm0 = expr.getSubTerm();
        final SimpleExpressionProxy subTerm1 =
          (SimpleExpressionProxy) subTerm0.acceptVisitor(this);
        if (subTerm1 != subTerm0) {
          final UnaryExpressionProxy newExpr =
            mInternalFactory.createUnaryExpressionProxy(op, subTerm1);
          getCompilationInfo().add(newExpr, expr);
          expr = newExpr;
        }
        return expr;
      } finally {
        mWithinPrime = wasWithinPrime;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean isPossibleVariable(final IdentifierProxy ident)
    {
      final SimpleExpressionProxy bound = mContext.getBoundExpression(ident);
      if (bound == null) {
        return true;
      } else if (bound instanceof IdentifierProxy) {
        final IdentifierProxy boundIdent = (IdentifierProxy) bound;
        return !mContext.isEnumAtom(boundIdent);
      } else {
        return false;
      }
    }

    //#######################################################################
    //# Data Members
    private ProxyAccessorSet<IdentifierProxy> mCollectedIdentifiers;
    private Map<SimpleExpressionProxy,IdentifierProxy> mNondeterminismMap;
    private boolean mSimplifying;
    private boolean mAtTopLevel;
    private boolean mWithinPrime;
    private boolean mWithinQualification;
  }


  //#########################################################################
  //# Inner Class SmartSimplifier
  private class SmartSimplifier extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private SimpleExpressionProxy simplify
      (final SimpleExpressionProxy expr,
       final ProxyAccessorSet<IdentifierProxy> retained,
       final Map<SimpleExpressionProxy,IdentifierProxy> map)
      throws EvalException
    {
      try {
        mRetainedIdentifiers = retained;
        mNondeterminismMap = map;
        mAtTopLevel = true;
        return (SimpleExpressionProxy) expr.acceptVisitor(this);
      } catch (final VisitorException exception) {
        if (exception.getCause() instanceof EvalException) {
          throw (EvalException) exception.getCause();
        } else {
          throw exception.getRuntimeException();
        }
      } finally {
        mRetainedIdentifiers = null;
        mNondeterminismMap = null;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      if (mAtTopLevel) {
        final CompilerOperatorTable optable = getOperatorTable();
        final BinaryOperator op = expr.getOperator();
        if (op == optable.getAndOperator()) {
          final SimpleExpressionProxy left0 = expr.getLeft();
          final SimpleExpressionProxy left1 =
            (SimpleExpressionProxy) left0.acceptVisitor(this);
          if (SimpleExpressionCompiler.isBooleanFalse(left1)) {
            return left1;
          }
          final SimpleExpressionProxy right0 = expr.getRight();
          final SimpleExpressionProxy right1 =
            (SimpleExpressionProxy) right0.acceptVisitor(this);
          if (left1 == left0 && right1 == right0) {
            return expr;
          } else if (SimpleExpressionCompiler.isBooleanTrue(left1) ||
                     SimpleExpressionCompiler.isBooleanFalse(right1)) {
            return right1;
          } else if (SimpleExpressionCompiler.isBooleanTrue(right1)) {
            return left1;
          } else {
            final BinaryExpressionProxy newExpr =
              mInternalFactory.createBinaryExpressionProxy(op, left1, right1);
            getCompilationInfo().add(newExpr, expr);
            return newExpr;
          }
        } else if (op == optable.getEqualsOperator()) {
          final IdentifierProxy ident = mNondeterminismMap.get(expr);
          if (ident != null && mRetainedIdentifiers.addProxy(ident)) {
            return expr;
          }
        }
        try {
          mAtTopLevel = false;
          return visitSimpleExpressionProxy(expr);
        } finally {
          mAtTopLevel = true;
        }
      } else {
        return visitSimpleExpressionProxy(expr);
      }
    }

    @Override
    public SimpleExpressionProxy visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
      throws VisitorException
    {
      try {
        return mSimplifier.simplify(expr, mContext);
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    //#######################################################################
    //# Data Members
    private boolean mAtTopLevel;
    private Map<SimpleExpressionProxy,IdentifierProxy> mNondeterminismMap;
    private ProxyAccessorSet<IdentifierProxy> mRetainedIdentifiers;
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mInternalFactory;
  private final ModuleProxyFactory mOutputFactory;
  private final SimpleExpressionCompiler mSimplifier;
  private final SmartPrimeFinder mSmartPrimeFinder = new SmartPrimeFinder();
  private final SmartSimplifier mSmartSimplifier = new SmartSimplifier();

  private BindingContext mContext;
  private Result mResult;

}
