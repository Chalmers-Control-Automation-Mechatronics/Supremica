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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorSet;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * <P>A utility to simplify expressions while preserving the presence of
 * primed variables.</P>
 *
 * <P>Based on {@link SimpleExpressionCompiler}, the prime-preserving
 * condition compiler simplifies Boolean expressions as much as possible
 * while ensuring that any primed variable that appears in the input is
 * still present in the output. This is achieved by adding equations of
 * the form <CODE>x'&nbsp;==&nbsp;x'</CODE> to the result if necessary.
 * Effort is made to re-use such equations if they are already present in
 * the input and only create them if necessary.</P>
 *
 * @author Robi Malik
 */

public class PrimePreservingConditionCompiler
{
  //#########################################################################
  //# Constructor
  public PrimePreservingConditionCompiler(final ModuleProxyFactory factory,
                                          final CompilerOperatorTable optable,
                                          final CompilationInfo compilationInfo,
                                          final boolean cloning)
  {
    mInternalFactory = ModuleElementFactory.getInstance();
    if (cloning || factory != mInternalFactory) {
      mCloner = new SourceInfoCloner(factory, compilationInfo);
    } else {
      mCloner = null;
    }
    mSimplifier = new SimpleExpressionCompiler
      (mInternalFactory, compilationInfo, optable, false);
  }


  //#########################################################################
  //# Invocation
  public SimpleExpressionProxy simplify
    (final SimpleExpressionProxy expr,
     final BindingContext context)
    throws EvalException
  {
    mContext = context;
    final ModuleEqualityVisitor equality = getEquality();
    final ProxyAccessorSet<IdentifierProxy> before =
      new ProxyAccessorHashSet<>(equality);
    final Map<SimpleExpressionProxy,IdentifierProxy> map = new HashMap<>();
    SimpleExpressionProxy simplified =
      mSmartPrimeFinder.simplifyIndexesAndCollectPrimes(expr, before, map);
    simplified = mSimplifier.simplify(simplified, context);
    if (!before.isEmpty() && !mSimplifier.isBooleanFalse(simplified)) {
      final ProxyAccessorSet<IdentifierProxy> after =
        new ProxyAccessorHashSet<>(equality, before.size());
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
            !mSimplifier.isBooleanFalse(simplified)) {
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
            if (mSimplifier.isBooleanTrue(simplified)) {
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
    if (mCloner != null) {
      simplified = (SimpleExpressionProxy) mCloner.getClone(simplified);
    }
    return simplified;
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
          mContext.getBoundExpression(ident) == null) {
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
      return visitIdentifierProxy(ident);
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
          if (mSimplifier.isBooleanFalse(left1)) {
            return left1;
          }
          final SimpleExpressionProxy right0 = expr.getRight();
          final SimpleExpressionProxy right1 =
            (SimpleExpressionProxy) right0.acceptVisitor(this);
          if (left1 == left0 && right1 == right0) {
            return expr;
          } else if (mSimplifier.isBooleanTrue(left1) ||
                     mSimplifier.isBooleanFalse(right1)) {
            return right1;
          } else if (mSimplifier.isBooleanTrue(right1)) {
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
  private final ModuleProxyCloner mCloner;
  private final SimpleExpressionCompiler mSimplifier;
  private final SmartPrimeFinder mSmartPrimeFinder = new SmartPrimeFinder();
  private final SmartSimplifier mSmartSimplifier = new SmartSimplifier();

  private BindingContext mContext;

}
