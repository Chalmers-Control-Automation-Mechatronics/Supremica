//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.DivisionByZeroException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.BuiltInFunction;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * @author Robi Malik
 */

public class SimpleExpressionCompiler
{

  //#########################################################################
  //# Constructors
  public SimpleExpressionCompiler(final ModuleProxyFactory factory,
                                  final CompilerOperatorTable optable)
  {
    this(factory, new CompilationInfo(false, false), optable, true);
  }

  public SimpleExpressionCompiler(final ModuleProxyFactory factory,
                                  final CompilationInfo compilationInfo,
                                  final CompilerOperatorTable optable)
  {
    this(factory, compilationInfo, optable, true);
  }

  public SimpleExpressionCompiler(final ModuleProxyFactory factory,
                                  final CompilationInfo compilationInfo,
                                  final CompilerOperatorTable optable,
                                  final boolean cloning)
  {
    mFactory = factory;
    mCompilationInfo = compilationInfo;
    mOperatorTable = optable;
    mIsCloning = cloning;

    mCloner = new SourceInfoCloner(factory, compilationInfo);

    final BinaryEvaluator assigner = new BinaryAssignmentEvaluator();
    final BinaryEvaluator sumEvaluator = new BinarySumEvaluator();
    mBinaryEvaluatorMap = new HashMap<BinaryOperator,BinaryEvaluator>(32);
    mBinaryEvaluatorMap.put(optable.getAndOperator(),
                            new BinaryAndEvaluator());
    mBinaryEvaluatorMap.put(optable.getAndWithOperator(), assigner);
    mBinaryEvaluatorMap.put(optable.getAssignmentOperator(), assigner);
    mBinaryEvaluatorMap.put(optable.getDecrementOperator(), assigner);
    mBinaryEvaluatorMap.put(optable.getDivideOperator(),
                            new BinaryDivideEvaluator());
    mBinaryEvaluatorMap.put(optable.getEqualsOperator(),
                            new BinaryEqualsEvaluator());
    mBinaryEvaluatorMap.put(optable.getGreaterEqualsOperator(),
                            new BinaryGreaterEqualsEvaluator());
    mBinaryEvaluatorMap.put(optable.getGreaterThanOperator(),
                            new BinaryGreaterThanEvaluator());
    mBinaryEvaluatorMap.put(optable.getIncrementOperator(), assigner);
    mBinaryEvaluatorMap.put(optable.getLessEqualsOperator(),
                            new BinaryLessEqualsEvaluator());
    mBinaryEvaluatorMap.put(optable.getLessThanOperator(),
                            new BinaryLessThanEvaluator());
    mBinaryEvaluatorMap.put(optable.getMinusOperator(), sumEvaluator);
    mBinaryEvaluatorMap.put(optable.getModuloOperator(),
                            new BinaryModuloEvaluator());
    mBinaryEvaluatorMap.put(optable.getNotEqualsOperator(),
                            new BinaryNotEqualsEvaluator());
    mBinaryEvaluatorMap.put(optable.getOrOperator(),
                            new BinaryOrEvaluator());
    mBinaryEvaluatorMap.put(optable.getOrWithOperator(), assigner);
    mBinaryEvaluatorMap.put(optable.getPlusOperator(), sumEvaluator);
    mBinaryEvaluatorMap.put(optable.getRangeOperator(),
                            new BinaryRangeEvaluator());
    mBinaryEvaluatorMap.put(optable.getTimesOperator(),
                            new BinaryTimesEvaluator());

    mUnaryEvaluatorMap = new HashMap<UnaryOperator,UnaryEvaluator>(8);
    mUnaryEvaluatorMap.put(optable.getNotOperator(),
                           new UnaryNotEvaluator());
    mUnaryEvaluatorMap.put(optable.getNextOperator(),
                           new UnaryNextEvaluator());
    mUnaryEvaluatorMap.put(optable.getUnaryMinusOperator(),
                           new UnaryMinusEvaluator());

    mFunctionEvaluatorMap = new HashMap<BuiltInFunction,FunctionEvaluator>(8);
    mFunctionEvaluatorMap.put(optable.getIteFunction(),
                              new FunctionIteEvaluator());
    mFunctionEvaluatorMap.put(optable.getMaxFunction(),
                              new FunctionMaxEvaluator());
    mFunctionEvaluatorMap.put(optable.getMinFunction(),
                              new FunctionMinEvaluator());

    mRangeEstimator = new RangeEstimator(optable);
    mSimplificationVisitor = new SimplificationVisitor();
    mAtomicVisitor = new AtomicVisitor();
    mRangeVisitor = new RangeVisitor();
    mEquality = new ModuleEqualityVisitor(false);
  }


  //#########################################################################
  //# Public Invocation
  public SimpleExpressionProxy simplify(final SimpleExpressionProxy expr,
                                        final BindingContext context)
    throws EvalException
  {
    try {
      mIsEvaluating = false;
      mNumLocks = 0;
      mContext = context;
      if (context instanceof VariableContext) {
        mVariableContext = (VariableContext) context;
      }
      return mSimplificationVisitor.simplify(expr);
    } finally {
      mContext = null;
      mVariableContext = null;
    }
  }

  public SimpleExpressionProxy eval(final SimpleExpressionProxy expr,
                                    final BindingContext context)
    throws EvalException
  {
    try {
      mIsEvaluating = true;
      mNumLocks = 0;
      mContext = context;
      if (context instanceof VariableContext) {
        mVariableContext = (VariableContext) context;
      }
      return mSimplificationVisitor.simplify(expr);
    } finally {
      mIsEvaluating = false;
      mContext = null;
      mVariableContext = null;
    }
  }

  public boolean isAtomicValue(final SimpleExpressionProxy expr,
                               final BindingContext context)
  {
    try {
      mContext = context;
      return isAtomicValue(expr);
    } finally {
      mContext = null;
    }
  }

  public CompiledRange getRangeValue(final SimpleExpressionProxy expr)
    throws EvalException
  {
    return mRangeVisitor.getRangeValue(expr);
  }


  public CompiledRange estimateRange(final SimpleExpressionProxy expr,
                                     final VariableContext context)
    throws EvalException
  {
    return mRangeEstimator.estimateRange(expr, context);
  }

  public boolean isBooleanExpression(final SimpleExpressionProxy expr,
                                     final VariableContext context)
    throws EvalException
  {
    final CompiledRange range = estimateRange(expr, context);
    if (range == null) {
      return false;
    } else if (range instanceof CompiledIntRange) {
      final CompiledIntRange intRange = (CompiledIntRange) range;
      return intRange.isBooleanRange();
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Type Checking
  public boolean isBooleanValue(final SimpleExpressionProxy expr)
  {
    if (expr instanceof IntConstantProxy) {
      final IntConstantProxy intconst = (IntConstantProxy) expr;
      final int value = intconst.getValue();
      return value == 0 || value == 1;
    } else {
      return false;
    }
  }

  public int getIntValue(final SimpleExpressionProxy expr)
    throws TypeMismatchException
  {
    if (expr instanceof IntConstantProxy) {
      final IntConstantProxy intconst = (IntConstantProxy) expr;
      return intconst.getValue();
    } else {
      throw new TypeMismatchException(expr, "INTEGER");
    }
  }

  public boolean getBooleanValue(final SimpleExpressionProxy expr)
    throws TypeMismatchException
  {
    if (expr instanceof IntConstantProxy) {
      final IntConstantProxy intconst = (IntConstantProxy) expr;
      switch (intconst.getValue()) {
      case 0:
        return false;
      case 1:
        return true;
      default:
        break;
      }
    }
    throw new TypeMismatchException(expr, "BOOLEAN");
  }

  public IdentifierProxy getIdentifierValue(final SimpleExpressionProxy expr)
    throws TypeMismatchException
  {
    if (expr instanceof IdentifierProxy) {
      return (IdentifierProxy) expr;
    } else {
      throw new TypeMismatchException(expr, "IDENTIFIER");
    }
  }

  public IntConstantProxy createIntConstantProxy(final int value)
  {
    return mFactory.createIntConstantProxy(value);
  }

  public IntConstantProxy createBooleanConstantProxy(final boolean value)
  {
    return createIntConstantProxy(value ? 1 : 0);
  }


  //#########################################################################
  //# Package Local Invocation
  SimpleExpressionProxy invokeSimplificationVisitor(final SimpleExpressionProxy expr)
    throws VisitorException
  {
    return mSimplificationVisitor.process(expr);
  }

  boolean isAtomicValue(final SimpleExpressionProxy expr)
  {
    try {
      return (Boolean) expr.acceptVisitor(mAtomicVisitor);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private SimpleExpressionProxy simplify(final SimpleExpressionProxy expr)
    throws EvalException
  {
    return mSimplificationVisitor.simplify(expr);
  }

  private SimpleExpressionProxy processIdentifier
    (final SimpleExpressionProxy expr, final boolean alreadyCloned)
    throws EvalException
  {
    if (mNumLocks > 0) {
      // Do not lookup names prefixed by qualifier or in primed subexpression!
      return getClone(expr, alreadyCloned);
    }
    final SimpleExpressionProxy bound = getBoundExpression(expr);
    if (mEquality.equals(expr, bound)) {
      return getClone(expr, alreadyCloned);
    } else if (bound != null) {
      final SimpleExpressionProxy simp = simplify(bound);
      mCompilationInfo.add(simp, expr);
      return simp;
    } else if (mIsEvaluating) {
      throw new UndefinedIdentifierException(expr);
    } else {
      return getClone(expr, alreadyCloned);
    }
  }

  private SimpleExpressionProxy getClone(final SimpleExpressionProxy expr,
                                         final boolean alreadyCloned)
  {
    if (mIsCloning && !alreadyCloned) {
      final SimpleExpressionProxy clone =
        (SimpleExpressionProxy) mCloner.getClone(expr);
      mCompilationInfo.add(clone, expr);
      return clone;
    } else {
      return expr;
    }
  }

  private BinaryEvaluator getEvaluator(final BinaryOperator op)
    throws UnsupportedOperatorException
  {
    final BinaryEvaluator evaluator = mBinaryEvaluatorMap.get(op);
    if (evaluator != null) {
      return evaluator;
    } else {
      throw new UnsupportedOperatorException(op);
    }
  }

  private UnaryEvaluator getEvaluator(final UnaryOperator op)
    throws UnsupportedOperatorException
  {
    final UnaryEvaluator evaluator = mUnaryEvaluatorMap.get(op);
    if (evaluator != null) {
      return evaluator;
    } else {
      throw new UnsupportedOperatorException(op);
    }
  }

  private FunctionEvaluator getEvaluator(final BuiltInFunction function)
    throws UnsupportedOperatorException
  {
    final FunctionEvaluator evaluator = mFunctionEvaluatorMap.get(function);
    if (evaluator != null) {
      return evaluator;
    } else {
      throw new UnsupportedOperatorException(function);
    }
  }

  private SimpleExpressionProxy getBoundExpression
    (final SimpleExpressionProxy ident)
  {
    if (mContext == null) {
      return null;
    } else {
      return mContext.getBoundExpression(ident);
    }
  }

  private boolean isEnumAtom(final IdentifierProxy ident)
  {
    if (mContext == null) {
      return false;
    } else {
      return mContext.isEnumAtom(ident);
    }
  }


  //#########################################################################
  //# Inner Class SimplificationVisitor
  private class SimplificationVisitor
    extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private SimpleExpressionProxy simplify(final SimpleExpressionProxy expr)
      throws EvalException
    {
      try {
        return process(expr);
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof EvalException) {
          throw (EvalException) cause;
        } else {
          throw exception.getRuntimeException();
        }
      }
    }

    private SimpleExpressionProxy process(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      return (SimpleExpressionProxy) expr.acceptVisitor(this);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public SimpleExpressionProxy visitBinaryExpressionProxy
      (final BinaryExpressionProxy expr)
      throws VisitorException
    {
      try {
        final BinaryOperator op = expr.getOperator();
        final BinaryEvaluator evaluator = getEvaluator(op);
        return evaluator.eval(expr);
      } catch (final EvalException exception) {
        exception.provideLocation(expr);
        throw wrap(exception);
      }
    }

    @Override
    public SimpleExpressionProxy visitEnumSetExpressionProxy
      (final EnumSetExpressionProxy expr)
      throws VisitorException
    {
      final List<SimpleIdentifierProxy> items = expr.getItems();
      for (final SimpleIdentifierProxy item : items) {
        final SimpleExpressionProxy found = getBoundExpression(item);
        if (found == null) {
          if (mContext != null) {
            try {
              final ModuleBindingContext modulecontext =
                mContext.getModuleBindingContext();
              modulecontext.insertEnumAtom(item);
            } catch (final EvalException exception) {
              throw wrap(exception);
            }
          }
        } else if (mEquality.equals(found, item)) {
          // nothing ...
        } else {
          final String name = item.getName();
          final DuplicateIdentifierException exception =
            new DuplicateIdentifierException(name, item);
          throw wrap(exception);
        }
      }
      return getClone(expr, false);
    }

    @Override
    public SimpleExpressionProxy visitFunctionCallExpressionProxy
      (final FunctionCallExpressionProxy expr)
      throws VisitorException
    {
      try {
        final String functionName = expr.getFunctionName();
        final BuiltInFunction function = mOperatorTable.getBuiltInFunction(functionName);
        final FunctionEvaluator evaluator = getEvaluator(function);
        return evaluator.eval(expr);
      } catch (final EvalException exception) {
        exception.provideLocation(expr);
        throw wrap(exception);
      }
    }

    @Override
    public SimpleExpressionProxy visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      try {
        final List<SimpleExpressionProxy> indexes =
          new ArrayList<SimpleExpressionProxy>(ident.getIndexes());
        final int size = indexes.size();
        final int locks = mNumLocks;
        boolean change = false;
        try {
          // Evaluate indexes even if primed!
          mNumLocks = 0;
          for (int i = 0; i < size; i++) {
            final SimpleExpressionProxy index = indexes.get(i);
            final SimpleExpressionProxy simplified = process(index);
            if (simplified != index) {
              change = true;
              indexes.set(i, simplified);
            }
          }
        } finally {
          mNumLocks = locks;
        }
        if (change) {
          final String name = ident.getName();
          final IndexedIdentifierProxy copy =
            mFactory.createIndexedIdentifierProxy(name, indexes);
          mCompilationInfo.add(copy, ident);
          return processIdentifier(copy, true);
        } else {
          return processIdentifier(ident, false);
        }
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public SimpleExpressionProxy visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final int locks = mNumLocks;
      try {
        mNumLocks++;
        final IdentifierProxy base0 = ident.getBaseIdentifier();
        final SimpleExpressionProxy basev = process(base0);
        final IdentifierProxy base1 = getIdentifierValue(basev);
        final IdentifierProxy comp0 = ident.getComponentIdentifier();
        final SimpleExpressionProxy compv = process(comp0);
        final IdentifierProxy comp1 = getIdentifierValue(compv);
        mNumLocks--;
        if (base0 == base1 && comp0 == comp1) {
          return processIdentifier(ident, false);
        } else {
          final QualifiedIdentifierProxy copy =
            mFactory.createQualifiedIdentifierProxy(base1, comp1);
          mCompilationInfo.add(copy, ident);
          return processIdentifier(copy, true);
        }
      } catch (final EvalException exception) {
        throw wrap(exception);
      } finally {
        mNumLocks = locks;
      }
    }

    @Override
    public SimpleExpressionProxy visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
    {
      return getClone(expr, false);
    }

    @Override
    public Proxy visitSimpleIdentifierProxy(final SimpleIdentifierProxy ident)
      throws VisitorException
    {
      try {
        return processIdentifier(ident, false);
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public SimpleExpressionProxy visitUnaryExpressionProxy
      (final UnaryExpressionProxy expr)
      throws VisitorException
    {
      try {
        final UnaryOperator op = expr.getOperator();
        final UnaryEvaluator evaluator = getEvaluator(op);
        return evaluator.eval(expr);
      } catch (final EvalException exception) {
        exception.provideLocation(expr);
        throw wrap(exception);
      }
    }

  }


  //#########################################################################
  //# Inner Class AtomicVisitor
  private class AtomicVisitor
    extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Boolean visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
    {
      final BinaryOperator operator = expr.getOperator();
      if (operator == mOperatorTable.getRangeOperator()) {
        final SimpleExpressionProxy lhs = expr.getLeft();
        final SimpleExpressionProxy rhs = expr.getRight();
        return (lhs instanceof IntConstantProxy &&
                rhs instanceof IntConstantProxy);
      } else {
        return false;
      }
    }

    @Override
    public Boolean visitEnumSetExpressionProxy
      (final EnumSetExpressionProxy expr)
    {
      final List<SimpleIdentifierProxy> items = expr.getItems();
      for (final SimpleIdentifierProxy item : items) {
        if (!isEnumAtom(item)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public Boolean visitIntConstantProxy(final IntConstantProxy expr)
    {
      return true;
    }

    @Override
    public Boolean visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    {
      return false;
    }

    @Override
    public Boolean visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
    {
      return isEnumAtom(ident);
    }

  }


  //#########################################################################
  //# Inner Class RangeVisitor
  private class RangeVisitor
    extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private CompiledRange getRangeValue(final SimpleExpressionProxy expr)
      throws EvalException
    {
      try {
        return (CompiledRange) expr.acceptVisitor(this);
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof EvalException) {
          throw (EvalException) cause;
        } else {
          throw exception.getRuntimeException();
        }
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public CompiledRange visitBinaryExpressionProxy
      (final BinaryExpressionProxy expr)
      throws VisitorException
    {
      try {
        final BinaryOperator operator = expr.getOperator();
        if (operator == mOperatorTable.getRangeOperator()) {
          final SimpleExpressionProxy lhsExpr = expr.getLeft();
          final int lhsInt = getIntValue(lhsExpr);
          final SimpleExpressionProxy rhsExpr = expr.getRight();
          final int rhsInt = getIntValue(rhsExpr);
          return new CompiledIntRange(lhsInt, rhsInt);
        } else {
          return visitSimpleExpressionProxy(expr);
        }
      } catch (final TypeMismatchException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public CompiledRange visitEnumSetExpressionProxy
      (final EnumSetExpressionProxy expr)
    {
      final List<SimpleIdentifierProxy> items = expr.getItems();
      return new CompiledEnumRange(items);
    }

    @Override
    public CompiledRange visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
      throws VisitorException
    {
      final TypeMismatchException exception =
        new TypeMismatchException(expr, "RANGE");
      throw wrap(exception);
    }

  }


  //#########################################################################
  //# Inner Class BinaryEvaluator
  private abstract class BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    abstract SimpleExpressionProxy eval(BinaryExpressionProxy expr)
      throws EvalException;

    //#######################################################################
    //# Auxiliary Methods
    BinaryExpressionProxy createExpression(final BinaryExpressionProxy expr,
                                           final SimpleExpressionProxy simpLHS,
                                           final SimpleExpressionProxy simpRHS)
    {
      if (simpLHS == expr.getLeft() && simpRHS == expr.getRight()) {
        return expr;
      } else {
        final BinaryOperator op = expr.getOperator();
        final BinaryExpressionProxy copy =
          mFactory.createBinaryExpressionProxy(op, simpLHS, simpRHS);
        mCompilationInfo.add(copy, expr);
        return copy;
      }
    }

  }


  //#########################################################################
  //# Inner Class UnaryEvaluator
  private abstract class UnaryEvaluator {

    //#######################################################################
    //# Evaluation
    abstract SimpleExpressionProxy eval(UnaryExpressionProxy expr)
      throws EvalException;

    //#######################################################################
    //# Auxiliary Methods
    UnaryExpressionProxy createExpression(final UnaryExpressionProxy expr,
                                          final SimpleExpressionProxy simpsub)
    {
      if (simpsub == expr.getSubTerm()) {
        return expr;
      } else {
        final UnaryOperator op = expr.getOperator();
        final UnaryExpressionProxy copy =
          mFactory.createUnaryExpressionProxy(op, simpsub);
        mCompilationInfo.add(copy, expr);
        return copy;
      }
    }

  }


  //#########################################################################
  //# Inner Class AbstractBinaryEqualsEvaluator
  private abstract class AbstractBinaryComparisonEvaluator
    extends BinaryEvaluator
  {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final BinaryExpressionProxy expr)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy origRHS = expr.getRight();
      if (mEquality.equals(origLHS, origRHS)) {
        return createBooleanConstantProxy(includesEquality());
      }
      final SimpleExpressionProxy simpLHS = simplify(origLHS);
      final boolean atomLHS = isAtomicValue(simpLHS);
      final int intLHS = atomLHS ? getIntValue(simpLHS) : 0;
      final SimpleExpressionProxy simpRHS = simplify(origRHS);
      final boolean atomRHS = isAtomicValue(simpRHS);
      final int intRHS = atomRHS ? getIntValue(simpRHS) : 0;
      if (atomLHS && atomRHS) {
        final boolean result = eval(intLHS, intRHS);
        return createBooleanConstantProxy(result);
      } else if (mEquality.equals(simpLHS, simpRHS)) {
        return createBooleanConstantProxy(includesEquality());
      } else if (mVariableContext != null) {
        final CompiledIntRange rangeLHS =
          mRangeEstimator.estimateIntRange(simpLHS, mVariableContext);
        if (rangeLHS != null) {
          final CompiledIntRange rangeRHS =
            mRangeEstimator.estimateIntRange(simpRHS, mVariableContext);
          if (rangeRHS != null) {
            final Boolean result = eval(rangeLHS, rangeRHS);
            if (result != null) {
              return createBooleanConstantProxy(result);
            }
          }
        }
      }
      return createExpression(expr, simpLHS, simpRHS);
    }

    //#######################################################################
    //# Provided by Subclasses
    abstract boolean includesEquality();

    abstract boolean eval(int lhs, int rhs);

    abstract Boolean eval(CompiledIntRange lhs, CompiledIntRange rhs);

  }


  //#########################################################################
  //# Inner Class AbstractBinaryEqualsEvaluator
  private abstract class AbstractBinaryEqualsEvaluator
    extends BinaryEvaluator
  {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final BinaryExpressionProxy expr)
      throws EvalException
    {
      final boolean eresult = getEqualsResult();
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy origRHS = expr.getRight();
      if (mEquality.equals(origLHS, origRHS)) {
        return createBooleanConstantProxy(eresult);
      }
      final SimpleExpressionProxy simpLHS = simplify(origLHS);
      final SimpleExpressionProxy simpRHS = simplify(origRHS);
      if (mEquality.equals(simpLHS, simpRHS)) {
        return createBooleanConstantProxy(eresult);
      } else if (isAtomicValue(simpLHS) && isAtomicValue(simpRHS)) {
        return createBooleanConstantProxy(!eresult);
      } else if (mVariableContext != null) {
        final CompiledRange rangeLHS =
          estimateRange(simpLHS, mVariableContext);
        if (rangeLHS != null) {
          final CompiledRange rangeRHS =
            estimateRange(simpRHS, mVariableContext);
          if (rangeRHS != null && !rangeLHS.intersects(rangeRHS)) {
            return createBooleanConstantProxy(!eresult);
          }
        }
      }
      return createExpression(expr, simpLHS, simpRHS);
    }

    //#######################################################################
    //# Provided by Subclasses
    abstract boolean getEqualsResult();

  }


  //#########################################################################
  //# Inner Class BinaryAndEvaluator
  private class BinaryAndEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final BinaryExpressionProxy expr)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpLHS = simplify(origLHS);
      if (isAtomicValue(simpLHS)) {
        final boolean boolLHS = getBooleanValue(simpLHS);
        if (boolLHS) {
          return simplify(origRHS);
        } else {
          return createBooleanConstantProxy(false);
        }
      }
      final SimpleExpressionProxy simpRHS = simplify(origRHS);
      if (isAtomicValue(simpRHS)) {
        final boolean boolRHS = getBooleanValue(simpRHS);
        if (boolRHS) {
          return simpLHS;
        } else {
          return createBooleanConstantProxy(false);
        }
      }
      if (mEquality.equals(simpLHS, simpRHS)) {
        return simpLHS;
      } else {
        return createExpression(expr, simpLHS, simpRHS);
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryAssignmentEvaluator
  private class BinaryAssignmentEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final BinaryExpressionProxy expr)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy simpLHS = simplify(origLHS);
      getIdentifierValue(simpLHS);
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpRHS = simplify(origRHS);
      return createExpression(expr, simpLHS, simpRHS);
    }

  }


  //#########################################################################
  //# Inner Class BinaryDivideEvaluator
  private class BinaryDivideEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final BinaryExpressionProxy expr)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy simpLHS = simplify(origLHS);
      final boolean atomLHS = isAtomicValue(simpLHS);
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpRHS = simplify(origRHS);
      final boolean atomRHS = isAtomicValue(simpRHS);
      if (atomRHS) {
        final int intRHS = getIntValue(simpRHS);
        switch (intRHS) {
        case 0:
          throw new DivisionByZeroException(expr);
        case 1:
          return simpLHS;
        default:
          if (atomLHS) {
            final int intLHS = getIntValue(simpLHS);
            return createIntConstantProxy(intLHS / intRHS);
          }
          break;
        }
      }
      return createExpression(expr, simpLHS, simpRHS);
    }

  }


  //#########################################################################
  //# Inner Class BinaryEqualsEvaluator
  private class BinaryEqualsEvaluator extends AbstractBinaryEqualsEvaluator {

    //#######################################################################
    //# Overrides for Abstract Base Class AbstractBinaryEqualsEvaluator
    @Override
    boolean getEqualsResult()
    {
      return true;
    }

  }


  //#########################################################################
  //# Inner Class BinaryGreaterEqualsEvaluator
  private class BinaryGreaterEqualsEvaluator
    extends AbstractBinaryComparisonEvaluator
  {

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryComparisonEvaluator
    @Override
    boolean includesEquality()
    {
      return true;
    }

    @Override
    boolean eval(final int lhs, final int rhs)
    {
      return lhs >= rhs;
    }

    @Override
    Boolean eval(final CompiledIntRange lhs, final CompiledIntRange rhs)
    {
      if (lhs.getLower() >= rhs.getUpper()) {
        return true;
      } else if (lhs.getUpper() < rhs.getLower()) {
        return false;
      } else {
        return null;
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryGreaterThanEvaluator
  private class BinaryGreaterThanEvaluator
    extends AbstractBinaryComparisonEvaluator
  {

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryComparisonEvaluator
    @Override
    boolean includesEquality()
    {
      return false;
    }

    @Override
    boolean eval(final int lhs, final int rhs)
    {
      return lhs > rhs;
    }

    @Override
    Boolean eval(final CompiledIntRange lhs, final CompiledIntRange rhs)
    {
      if (lhs.getLower() > rhs.getUpper()) {
        return true;
      } else if (lhs.getUpper() <= rhs.getLower()) {
        return false;
      } else {
        return null;
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryLessEqualsEvaluator
  private class BinaryLessEqualsEvaluator
    extends AbstractBinaryComparisonEvaluator
  {

    //#######################################################################
    //# Overrides for Abstract Base Class AbstractBinaryComparisonEvaluator
    @Override
    boolean includesEquality()
    {
      return true;
    }

    @Override
    boolean eval(final int lhs, final int rhs)
    {
      return lhs <= rhs;
    }

    @Override
    Boolean eval(final CompiledIntRange lhs, final CompiledIntRange rhs)
    {
      if (lhs.getUpper() <= rhs.getLower()) {
        return true;
      } else if (lhs.getLower() > rhs.getUpper()) {
        return false;
      } else {
        return null;
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryLessThanEvaluator
  private class BinaryLessThanEvaluator
    extends AbstractBinaryComparisonEvaluator
  {

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryComparisonEvaluator
    @Override
    boolean includesEquality()
    {
      return false;
    }

    @Override
    boolean eval(final int lhs, final int rhs)
    {
      return lhs < rhs;
    }

    @Override
    Boolean eval(final CompiledIntRange lhs, final CompiledIntRange rhs)
    {
      if (lhs.getUpper() < rhs.getLower()) {
        return true;
      } else if (lhs.getLower() >= rhs.getUpper()) {
        return false;
      } else {
        return null;
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryModuloEvaluator
  private class BinaryModuloEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final BinaryExpressionProxy expr)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy simpLHS = simplify(origLHS);
      final boolean atomLHS = isAtomicValue(simpLHS);
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpRHS = simplify(origRHS);
      final boolean atomRHS = isAtomicValue(simpRHS);
      if (atomRHS) {
        final int intRHS = getIntValue(simpRHS);
        switch (intRHS) {
        case 0:
          throw new DivisionByZeroException(expr);
        case 1:
          return createIntConstantProxy(0);
        default:
          if (atomLHS) {
            final int intLHS = getIntValue(simpLHS);
            return createIntConstantProxy(intLHS % intRHS);
          }
          break;
        }
      }
      return createExpression(expr, simpLHS, simpRHS);
    }

  }


  //#########################################################################
  //# Inner Class BinaryNotEqualsEvaluator
  private class BinaryNotEqualsEvaluator
    extends AbstractBinaryEqualsEvaluator
  {

    //#######################################################################
    //# Overrides for Abstract Base Class AbstractBinaryEqualsEvaluator
    @Override
    boolean getEqualsResult()
    {
      return false;
    }

  }


  //#########################################################################
  //# Inner Class BinaryOrEvaluator
  private class BinaryOrEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final BinaryExpressionProxy expr)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpLHS = simplify(origLHS);
      if (isAtomicValue(simpLHS)) {
        final boolean boolLHS = getBooleanValue(simpLHS);
        if (!boolLHS) {
          return simplify(origRHS);
        } else {
          return createBooleanConstantProxy(true);
        }
      }
      final SimpleExpressionProxy simpRHS = simplify(origRHS);
      if (isAtomicValue(simpRHS)) {
        final boolean boolRHS = getBooleanValue(simpRHS);
        if (!boolRHS) {
          return simpLHS;
        } else {
          return createBooleanConstantProxy(true);
        }
      }
      if (mEquality.equals(simpLHS, simpRHS)) {
        return simpLHS;
      } else {
        return createExpression(expr, simpLHS, simpRHS);
      }
    }

  }


  //#########################################################################
  //# Inner Class BinarySumEvaluator
  private class BinarySumEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final BinaryExpressionProxy expr)
      throws EvalException
    {
      final SumSimplifier simplifier =
        new SumSimplifier(mFactory, SimpleExpressionCompiler.this);
      return simplifier.normaliseSum(expr);
    }

  }


  //#########################################################################
  //# Inner Class BinaryRangeEvaluator
  private class BinaryRangeEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final BinaryExpressionProxy expr)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy simpLHS = simplify(origLHS);
      if (isAtomicValue(simpLHS)) {
        getIntValue(simpLHS);
      }
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpRHS = simplify(origRHS);
      if (isAtomicValue(simpRHS)) {
        getIntValue(simpRHS);
      }
      return createExpression(expr, simpLHS, simpRHS);
    }

  }


  //#########################################################################
  //# Inner Class BinaryTimesEvaluator
  private class BinaryTimesEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final BinaryExpressionProxy expr)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpLHS = simplify(origLHS);
      final boolean atomLHS = isAtomicValue(simpLHS);
      final int intLHS = atomLHS ? getIntValue(simpLHS) : Integer.MAX_VALUE;
      if (atomLHS) {
        switch (intLHS) {
        case 0:
          return createIntConstantProxy(0);
        case 1:
          return simplify(origRHS);
        case -1:
          final UnaryOperator op = mOperatorTable.getUnaryMinusOperator();
          final UnaryExpressionProxy uminus =
            mFactory.createUnaryExpressionProxy(op, origRHS);
          mCompilationInfo.add(uminus, expr);
          return simplify(uminus);
        default:
          break;
        }
      }
      final SimpleExpressionProxy simpRHS = simplify(origRHS);
      if (isAtomicValue(simpRHS)) {
        final int intRHS = getIntValue(simpRHS);
        switch (intRHS) {
        case 0:
          return createIntConstantProxy(0);
        case 1:
          return simpLHS;
        case -1:
          if (atomLHS) {
            return createIntConstantProxy(-intLHS);
          } else {
            final UnaryOperator op = mOperatorTable.getUnaryMinusOperator();
            final UnaryExpressionProxy uminus =
              mFactory.createUnaryExpressionProxy(op, simpLHS);
            mCompilationInfo.add(uminus, expr);
            return uminus;
          }
        default:
          if (atomLHS) {
            return createIntConstantProxy(intLHS * intRHS);
          }
          break;
        }
      }
      return createExpression(expr, simpLHS, simpRHS);
    }

  }


  //#########################################################################
  //# Inner Class UnaryMinusEvaluator
  private class UnaryMinusEvaluator extends UnaryEvaluator {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final UnaryExpressionProxy expr)
      throws EvalException
    {
      final SumSimplifier simplifier =
        new SumSimplifier(mFactory, SimpleExpressionCompiler.this);
      return simplifier.normaliseSum(expr);
    }

  }


  //#########################################################################
  //# Inner Class UnaryNextEvaluator
  private class UnaryNextEvaluator extends UnaryEvaluator {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final UnaryExpressionProxy expr)
      throws EvalException
    {
      final int locks = mNumLocks;
      try {
        mNumLocks++;
        final SimpleExpressionProxy origsub = expr.getSubTerm();
        final SimpleExpressionProxy simpsub = simplify(origsub);
        final UnaryExpressionProxy newexpr = createExpression(expr, simpsub);
        mNumLocks--;
        return processIdentifier(newexpr, true);
      } finally {
        mNumLocks = locks;
      }
    }

  }


  //#########################################################################
  //# Inner Class UnaryNotEvaluator
  private class UnaryNotEvaluator extends UnaryEvaluator {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final UnaryExpressionProxy expr)
      throws EvalException
    {
      final SimpleExpressionProxy origsub = expr.getSubTerm();
      final SimpleExpressionProxy simpsub = simplify(origsub);
      if (isAtomicValue(simpsub)) {
        final boolean value = getBooleanValue(simpsub);
        return createBooleanConstantProxy(!value);
      } else {
        return createExpression(expr, simpsub);
      }
    }

  }


  //#########################################################################
  //# Inner Class FunctionEvaluator
  private abstract class FunctionEvaluator {

    //#######################################################################
    //# Evaluation
    abstract SimpleExpressionProxy eval(FunctionCallExpressionProxy expr)
      throws EvalException;

    //#######################################################################
    //# Auxiliary Methods
    FunctionCallExpressionProxy createExpression
      (final FunctionCallExpressionProxy expr,
       final List<SimpleExpressionProxy> simpArgs)
    {
      final List<SimpleExpressionProxy> args = expr.getArguments();
      if (args.equals(simpArgs)) {
        return expr;
      } else {
        final String functionName = expr.getFunctionName();
        final FunctionCallExpressionProxy copy =
          mFactory.createFunctionCallExpressionProxy(functionName, simpArgs);
        mCompilationInfo.add(copy, expr);
        return copy;
      }
    }

  }


  //#########################################################################
  //# Inner Class FunctionIteEvaluator
  private class FunctionIteEvaluator extends FunctionEvaluator {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final FunctionCallExpressionProxy expr)
      throws EvalException
    {
      final List<SimpleExpressionProxy> origArgs = expr.getArguments();
      final Iterator<SimpleExpressionProxy> iter = origArgs.iterator();
      final SimpleExpressionProxy origCond = iter.next();
      final SimpleExpressionProxy origThen = iter.next();
      final SimpleExpressionProxy origElse = iter.next();
      final SimpleExpressionProxy simpCond = simplify(origCond);
      if (isAtomicValue(simpCond)) {
        if (getBooleanValue(simpCond)) {
          return simplify(origThen);
        } else {
          return simplify(origElse);
        }
      } else {
        final SimpleExpressionProxy simpThen = simplify(origThen);
        final SimpleExpressionProxy simpElse = simplify(origElse);
        if (mEquality.equals(simpThen, simpElse)) {
          return simpThen;
        } else {
          final List<SimpleExpressionProxy> simpArgs =
            new ArrayList<SimpleExpressionProxy>(3);
          simpArgs.add(simpCond);
          simpArgs.add(simpThen);
          simpArgs.add(simpElse);
          return createExpression(expr, simpArgs);
        }
      }
    }

  }


  //#########################################################################
  //# Inner Class FunctionMaxEvaluator
  private class FunctionMaxEvaluator extends FunctionEvaluator {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final FunctionCallExpressionProxy expr)
      throws EvalException
    {
      final List<SimpleExpressionProxy> simpArgs =
        new LinkedList<SimpleExpressionProxy>();
      SimpleExpressionProxy maxArg = null;
      int maxValue = Integer.MIN_VALUE;
      int minPos = -1;
      int pos = 0;
      for (final SimpleExpressionProxy origArg : expr.getArguments()) {
        final SimpleExpressionProxy simpArg = simplify(origArg);
        if (isAtomicValue(simpArg)) {
          final int value = getIntValue(simpArg);
          if (value > maxValue) {
            maxValue = value;
            maxArg = simpArg;
          }
          if (minPos < 0) {
            minPos = pos;
          }
        } else if (!mEquality.contains(simpArgs, simpArg)) {
          simpArgs.add(simpArg);
        }
        pos++;
      }
      if (simpArgs.isEmpty()) {
        return maxArg;
      } else {
        if (minPos >= 0) {
          simpArgs.add(minPos, maxArg);
        }
        final int size = simpArgs.size();
        if (size == 1) {
          return simpArgs.get(0);
        }
        if (mVariableContext != null) {
          final CompiledIntRange[] ranges = new CompiledIntRange[size];
          int i = 0;
          int maxLowerLimit = Integer.MIN_VALUE;
          for (final SimpleExpressionProxy simpArg : simpArgs) {
            final CompiledIntRange range =
              mRangeEstimator.estimateIntRange(simpArg, mVariableContext);
            if (maxLowerLimit < range.getLower()) {
              maxLowerLimit = range.getLower();
            }
            ranges[i++] = range;
          }
          final Iterator<SimpleExpressionProxy> iter = simpArgs.iterator();
          i = 0;
          while (iter.hasNext()) {
            iter.next();
            final int upper = ranges[i].getUpper();
            if (upper < maxLowerLimit ||
                upper == maxLowerLimit && upper > ranges[i].getLower()) {
              iter.remove();
            }
            i++;
          }
          if (simpArgs.size() == 1) {
            return simpArgs.get(0);
          }
        }
        return createExpression(expr, simpArgs);
      }
    }

  }


  //#########################################################################
  //# Inner Class FunctionMinEvaluator
  private class FunctionMinEvaluator extends FunctionEvaluator {

    //#######################################################################
    //# Evaluation
    @Override
    SimpleExpressionProxy eval(final FunctionCallExpressionProxy expr)
      throws EvalException
    {
      final List<SimpleExpressionProxy> simpArgs =
        new LinkedList<SimpleExpressionProxy>();
      SimpleExpressionProxy minArg = null;
      int minValue = Integer.MAX_VALUE;
      int minPos = -1;
      int pos = 0;
      for (final SimpleExpressionProxy origArg : expr.getArguments()) {
        final SimpleExpressionProxy simpArg = simplify(origArg);
        if (isAtomicValue(simpArg)) {
          final int value = getIntValue(simpArg);
          if (value < minValue) {
            minValue = value;
            minArg = simpArg;
          }
          if (minPos < 0) {
            minPos = pos;
          }
        } else if (!mEquality.contains(simpArgs, simpArg)) {
          simpArgs.add(simpArg);
        }
        pos++;
      }
      if (simpArgs.isEmpty()) {
        return minArg;
      } else {
        if (minPos >= 0) {
          simpArgs.add(minPos, minArg);
        }
        final int size = simpArgs.size();
        if (size == 1) {
          return simpArgs.get(0);
        }
        if (mVariableContext != null) {
          final CompiledIntRange[] ranges = new CompiledIntRange[size];
          int i = 0;
          int minUpperLimit = Integer.MAX_VALUE;
          for (final SimpleExpressionProxy simpArg : simpArgs) {
            final CompiledIntRange range =
              mRangeEstimator.estimateIntRange(simpArg, mVariableContext);
            if (minUpperLimit > range.getUpper()) {
              minUpperLimit = range.getUpper();
            }
            ranges[i++] = range;
          }
          final Iterator<SimpleExpressionProxy> iter = simpArgs.iterator();
          i = 0;
          while (iter.hasNext()) {
            iter.next();
            final int lower = ranges[i].getLower();
            if (lower > minUpperLimit ||
                lower == minUpperLimit && lower < ranges[i].getUpper()) {
              iter.remove();
            }
            i++;
          }
          if (simpArgs.size() == 1) {
            return simpArgs.get(0);
          }
        }
        return createExpression(expr, simpArgs);
      }
    }

  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilationInfo mCompilationInfo;
  private final ModuleProxyCloner mCloner;
  private final CompilerOperatorTable mOperatorTable;
  private final boolean mIsCloning;
  private final Map<BinaryOperator,BinaryEvaluator> mBinaryEvaluatorMap;
  private final Map<UnaryOperator,UnaryEvaluator> mUnaryEvaluatorMap;
  private final Map<BuiltInFunction,FunctionEvaluator> mFunctionEvaluatorMap;
  private final RangeEstimator mRangeEstimator;
  private final SimplificationVisitor mSimplificationVisitor;
  private final AtomicVisitor mAtomicVisitor;
  private final RangeVisitor mRangeVisitor;
  private final ModuleEqualityVisitor mEquality;

  private boolean mIsEvaluating;
  /**
   * The number of locks that prevent evaluation of subterms.
   * Locks are used to prevent evaluation of primed subexpressions or
   * identifiers prefixed by a qualifier.
   */
  private int mNumLocks;
  private BindingContext mContext;
  private VariableContext mVariableContext;

}
