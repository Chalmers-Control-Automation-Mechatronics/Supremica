//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context;
//# CLASS:   SimpleExpressionCompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.DivisionByZeroException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
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
    this(factory, optable, true);
  }

  public SimpleExpressionCompiler(final ModuleProxyFactory factory,
                                  final CompilerOperatorTable optable,
                                  final boolean cloning)
  {
    mFactory = factory;
    mOperatorTable = optable;
    mIsCloning = cloning;

    final BinaryEvaluator assigner = new BinaryAssignmentEvaluator();
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
    mBinaryEvaluatorMap.put(optable.getMinusOperator(),
                            new BinaryMinusEvaluator());
    mBinaryEvaluatorMap.put(optable.getModuloOperator(),
                            new BinaryModuloEvaluator());
    mBinaryEvaluatorMap.put(optable.getNotEqualsOperator(),
                            new BinaryNotEqualsEvaluator());
    mBinaryEvaluatorMap.put(optable.getOrOperator(),
                            new BinaryOrEvaluator());
    mBinaryEvaluatorMap.put(optable.getOrWithOperator(), assigner);
    mBinaryEvaluatorMap.put(optable.getPlusOperator(),
                            new BinaryPlusEvaluator());
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

    mRangeEstimator = new RangeEstimator(optable);
    mSimplificationVisitor = new SimplificationVisitor();
    mAtomicVisitor = new AtomicVisitor();
    mRangeVisitor = new RangeVisitor();
  }


  //#########################################################################
  //# Invocation
  public SimpleExpressionProxy simplify(final SimpleExpressionProxy expr,
                                        final VariableContext context)
    throws EvalException
  {
    try {
      mVariableContext = context;
      return simplify(expr, context);
    } finally {
      mVariableContext = null;
    }
  }

  public SimpleExpressionProxy simplify(final SimpleExpressionProxy expr,
                                        final BindingContext context)
    throws EvalException
  {
    try {
      mIsEvaluating = false;
      mNumPrimes = 0;
      mContext = context;
      return mSimplificationVisitor.simplify(expr);
    } finally {
      mContext = null;
    }
  }

  public SimpleExpressionProxy eval(final SimpleExpressionProxy expr,
                                    final VariableContext context)
    throws EvalException
  {
    try {
      mVariableContext = context;
      return eval(expr, context);
    } finally {
      mVariableContext = null;
    }
  }

  public SimpleExpressionProxy eval(final SimpleExpressionProxy expr,
                                    final BindingContext context)
    throws EvalException
  {
    try {
      mIsEvaluating = true;
      mNumPrimes = 0;
      mContext = context;
      return mSimplificationVisitor.simplify(expr);
    } finally {
      mIsEvaluating = false;
      mContext = null;
    }
  }

  public boolean isAtomicValue(final SimpleExpressionProxy expr)
  {
    try {
      return (Boolean) expr.acceptVisitor(mAtomicVisitor);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }

  public CompiledRange getRangeValue(final SimpleExpressionProxy expr)
    throws EvalException
  {
    return mRangeVisitor.getRangeValue(expr);
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
    if (mNumPrimes > 0) {
      // Do not lookup names in primed subexpression!
      return getClone(expr, alreadyCloned);
    }
    final SimpleExpressionProxy bound = getBoundExpression(expr);
    if (expr.equalsByContents(bound)) {
      return getClone(expr, alreadyCloned);
    } else if (bound != null) {
      return simplify(bound);
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
      final ModuleProxyCloner cloner = mFactory.getCloner();
      return (SimpleExpressionProxy) cloner.getClone(expr);
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
    extends AbstractModuleProxyVisitor
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

    public SimpleExpressionProxy visitEnumSetExpressionProxy
      (final EnumSetExpressionProxy expr)
      throws VisitorException
    {
      final List<SimpleIdentifierProxy> items = expr.getItems();
      final int numitems = items.size();
      for (final SimpleIdentifierProxy item : items) {
        final SimpleExpressionProxy found = getBoundExpression(item);
        if (found == null) {
          if (mContext != null) {
            final ModuleBindingContext modulecontext =
              mContext.getModuleBindingContext();
            modulecontext.addBinding(item, item);
          }
        } else if (found.equalsByContents(item)) {
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

    public SimpleExpressionProxy visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      try {
        final List<SimpleExpressionProxy> indexes =
          new ArrayList<SimpleExpressionProxy>(ident.getIndexes());
        final int size = indexes.size();
        final int numprimes = mNumPrimes;
        boolean change = false;
        try {
          // Evaluate indexes even if primed!
          mNumPrimes = 0;
          for (int i = 0; i < size; i++) {
            final SimpleExpressionProxy index = indexes.get(i);
            final SimpleExpressionProxy simplified = process(index);
            if (simplified != index) {
              change = true;
              indexes.set(i, simplified);
            }
          }
        } finally {
          mNumPrimes = numprimes;
        }
        if (change) {
          final String name = ident.getName();
          final IndexedIdentifierProxy copy =
            mFactory.createIndexedIdentifierProxy(name, indexes);
          return processIdentifier(copy, true);
        } else {
          return processIdentifier(ident, false);
        }
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    public SimpleExpressionProxy visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      try {
        final IdentifierProxy base0 = ident.getBaseIdentifier();
        final SimpleExpressionProxy basev = process(base0);
        final IdentifierProxy base1 = getIdentifierValue(basev);
        final IdentifierProxy comp0 = ident.getComponentIdentifier();
        final SimpleExpressionProxy compv = process(comp0);
        final IdentifierProxy comp1 = getIdentifierValue(compv);
        if (base0 == base1 && comp0 == comp1) {
          return processIdentifier(ident, false);
        } else {
          final QualifiedIdentifierProxy copy =
            mFactory.createQualifiedIdentifierProxy(base1, comp1);
          return processIdentifier(copy, true);
        }
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    public SimpleExpressionProxy visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
    {
      return getClone(expr, false);
    }

    public Proxy visitSimpleIdentifierProxy(final SimpleIdentifierProxy ident)
      throws VisitorException
    {
      try {
        return processIdentifier(ident, false);
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

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
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
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

    public Boolean visitIntConstantProxy(final IntConstantProxy expr)
    {
      return true;
    }

    public Boolean visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    {
      return false;
    }

    public Boolean visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
    {
      return isEnumAtom(ident);
    }

  }


  //#########################################################################
  //# Inner Class RangeVisitor
  private class RangeVisitor
    extends AbstractModuleProxyVisitor
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

    public CompiledRange visitEnumSetExpressionProxy
      (final EnumSetExpressionProxy expr)
    {
      final List<SimpleIdentifierProxy> items = expr.getItems();
      return new CompiledEnumRange(items);
    }

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
        return mFactory.createBinaryExpressionProxy(op, simpLHS, simpRHS);
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
        return mFactory.createUnaryExpressionProxy(op, simpsub);
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
    SimpleExpressionProxy eval(final BinaryExpressionProxy expr)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy origRHS = expr.getRight();
      if (includesEquality() && origLHS.equalsByContents(origRHS)) {
        return createBooleanConstantProxy(true);
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
      } else if (includesEquality() && simpLHS.equalsByContents(simpRHS)) {
        return createBooleanConstantProxy(true);
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
    SimpleExpressionProxy eval(final BinaryExpressionProxy expr)
      throws EvalException
    {
      final boolean eresult = getEqualsResult();
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy origRHS = expr.getRight();
      if (origLHS.equalsByContents(origRHS)) {
        return createBooleanConstantProxy(eresult);
      }
      final SimpleExpressionProxy simpLHS = simplify(origLHS);
      final SimpleExpressionProxy simpRHS = simplify(origRHS);
      if (simpLHS.equalsByContents(simpRHS)) {
        return createBooleanConstantProxy(eresult);
      } else if (isAtomicValue(simpLHS) && isAtomicValue(simpRHS)) {
        return createBooleanConstantProxy(!eresult);
      } else if (mVariableContext != null) {
        final CompiledRange rangeLHS =
          mRangeEstimator.estimateRange(simpLHS, mVariableContext);
        if (rangeLHS != null) {
          final CompiledRange rangeRHS =
            mRangeEstimator.estimateRange(simpRHS, mVariableContext);
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
      return createExpression(expr, simpLHS, simpRHS);
    }

  }


  //#########################################################################
  //# Inner Class BinaryAssignmentEvaluator
  private class BinaryAssignmentEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
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
    boolean includesEquality()
    {
      return true;
    }

    boolean eval(final int lhs, final int rhs)
    {
      return lhs >= rhs;
    }

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
    boolean includesEquality()
    {
      return false;
    }

    boolean eval(final int lhs, final int rhs)
    {
      return lhs > rhs;
    }

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
    //# Overrides for Abstract Baseclass AbstractBinaryComparisonEvaluator
    boolean includesEquality()
    {
      return true;
    }

    boolean eval(final int lhs, final int rhs)
    {
      return lhs <= rhs;
    }

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
    boolean includesEquality()
    {
      return false;
    }

    boolean eval(final int lhs, final int rhs)
    {
      return lhs < rhs;
    }

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
  //# Inner Class BinaryMinusEvaluator
  private class BinaryMinusEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    SimpleExpressionProxy eval(final BinaryExpressionProxy expr)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy simpLHS = simplify(origLHS);
      final boolean atomLHS = isAtomicValue(simpLHS);
      final int intLHS = atomLHS ? getIntValue(simpLHS) : Integer.MAX_VALUE;
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpRHS = simplify(origRHS);
      final boolean atomRHS = isAtomicValue(simpRHS);
      final int intRHS = atomRHS ? getIntValue(simpRHS) : Integer.MAX_VALUE;
      if (atomLHS && atomRHS) {
        return createIntConstantProxy(intLHS - intRHS);
      } else if (intLHS == 0) {
        final UnaryOperator op = mOperatorTable.getUnaryMinusOperator();
        return mFactory.createUnaryExpressionProxy(op, simpRHS);
      } else if (intRHS == 0) {
        return simpLHS;
      } else if (simpLHS.equalsByContents(simpRHS)) {
        return createIntConstantProxy(0);
      } else {
        return createExpression(expr, simpLHS, simpRHS);
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryModuloEvaluator
  private class BinaryModuloEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
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
      return createExpression(expr, simpLHS, simpRHS);
    }

  }


  //#########################################################################
  //# Inner Class BinaryPlusEvaluator
  private class BinaryPlusEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
    SimpleExpressionProxy eval(final BinaryExpressionProxy expr)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy simpLHS = simplify(origLHS);
      final boolean atomLHS = isAtomicValue(simpLHS);
      final int intLHS = atomLHS ? getIntValue(simpLHS) : Integer.MAX_VALUE;
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpRHS = simplify(origRHS);
      final boolean atomRHS = isAtomicValue(simpRHS);
      final int intRHS = atomRHS ? getIntValue(simpRHS) : Integer.MAX_VALUE;
      if (atomLHS && atomRHS) {
        return createIntConstantProxy(intLHS + intRHS);
      } else if (intLHS == 0) {
        return simpRHS;
      } else if (intRHS == 0) {
        return simpLHS;
      } else {
        return createExpression(expr, simpLHS, simpRHS);
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryRangeEvaluator
  private class BinaryRangeEvaluator extends BinaryEvaluator {

    //#######################################################################
    //# Evaluation
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
            return mFactory.createUnaryExpressionProxy(op, simpLHS);
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
    SimpleExpressionProxy eval(final UnaryExpressionProxy expr)
      throws EvalException
    {
      final SimpleExpressionProxy origsub = expr.getSubTerm();
      final SimpleExpressionProxy simpsub = simplify(origsub);
      if (isAtomicValue(simpsub)) {
        final int value = getIntValue(simpsub);
        return createIntConstantProxy(-value);
      } else {
        return createExpression(expr, simpsub);
      }
    }

  }


  //#########################################################################
  //# Inner Class UnaryNextEvaluator
  private class UnaryNextEvaluator extends UnaryEvaluator {

    //#######################################################################
    //# Evaluation
    SimpleExpressionProxy eval(final UnaryExpressionProxy expr)
      throws EvalException
    {
      final int numprimes = mNumPrimes;
      try {
        mNumPrimes++;
        final SimpleExpressionProxy origsub = expr.getSubTerm();
        final SimpleExpressionProxy simpsub = simplify(origsub);
        final IdentifierProxy ident = getIdentifierValue(simpsub);
        final UnaryExpressionProxy newexpr = createExpression(expr, simpsub);
        mNumPrimes--;
        return processIdentifier(newexpr, true);
      } finally {
        mNumPrimes = numprimes;
      }
    }

  }


  //#########################################################################
  //# Inner Class UnaryNotEvaluator
  private class UnaryNotEvaluator extends UnaryEvaluator {

    //#######################################################################
    //# Evaluation
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
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final boolean mIsCloning;
  private final Map<BinaryOperator,BinaryEvaluator> mBinaryEvaluatorMap;
  private final Map<UnaryOperator,UnaryEvaluator> mUnaryEvaluatorMap;
  private final RangeEstimator mRangeEstimator;
  private final SimplificationVisitor mSimplificationVisitor;
  private final AtomicVisitor mAtomicVisitor;
  private final RangeVisitor mRangeVisitor;

  private boolean mIsEvaluating;
  private int mNumPrimes;
  private BindingContext mContext;
  private VariableContext mVariableContext;

}
