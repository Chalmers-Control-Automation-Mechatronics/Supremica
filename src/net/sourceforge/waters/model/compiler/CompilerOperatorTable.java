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

package net.sourceforge.waters.model.compiler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.expr.AbstractOperatorTable;
import net.sourceforge.waters.model.expr.AbstractSimpleExpressionSimplifier;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.BuiltInFunction;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * The operator table used by the EFA compilers.
 * In addition to providing access to operators by names as defined by
 * the {@link OperatorTable}
 * interface, this class facilitates semantic access to logical and
 * assignment operators.
 *
 * @author Robi Malik
 */

public class CompilerOperatorTable extends AbstractOperatorTable {

  //#########################################################################
  //# Static Class Methods
  public static CompilerOperatorTable getInstance()
  {
    return INSTANCE;
  }


  //#########################################################################
  //# Constructors
  private CompilerOperatorTable()
  {
    super(32, OPCHAR_MIN, OPCHAR_MAX);
    mComplementMap = new HashMap<BinaryOperator,BinaryOperator>(16);
    mSwapMap = new HashMap<BinaryOperator,BinaryOperator>(16);
    mAssigningMap = new HashMap<BinaryOperator,BinaryOperator>(16);

    mUnaryNextOperator = new UnaryNextOperator();
    mEqualsOperator = new BinaryEqualsOperator();
    mNotEqualsOperator = new BinaryNotEqualsOperator();
    mGreaterThanOperator = new BinaryGreaterThanOperator();
    mGreaterEqualsOperator = new BinaryGreaterEqualsOperator();
    mLessThanOperator = new BinaryLessThanOperator();
    mLessEqualsOperator = new BinaryLessEqualsOperator();
    mNotOperator = new UnaryNotOperator();
    mAndOperator = new BinaryAndOperator();
    mOrOperator= new BinaryOrOperator();
    mAssignmentOperator = new BinaryAssignmentOperator();
    mIncrementOperator = new BinaryIncrementOperator();
    mDecrementOperator = new BinaryDecrementOperator();
    mAndWithOperator = new BinaryAndWithOperator();
    mOrWithOperator= new BinaryOrWithOperator();
    mUnaryMinusOperator = new UnaryMinusOperator();
    mPlusOperator = new BinaryPlusOperator();
    mMinusOperator = new BinaryMinusOperator();
    mTimesOperator = new BinaryTimesOperator();
    mDivideOperator = new BinaryDivideOperator();
    mModuloOperator = new BinaryModuloOperator();
    mRangeOperator = new BinaryRangeOperator();

    store(mUnaryNextOperator, 0);
    store(new BinaryQualificationOperator(), 1);
    store(mEqualsOperator, 2);
    store(mNotEqualsOperator, 3);
    store(mLessThanOperator, 4);
    store(mLessEqualsOperator, 5);
    store(mGreaterThanOperator, 6);
    store(mGreaterEqualsOperator, 7);
    store(mNotOperator, 1);
    store(mAndOperator, 8);
    store(mOrOperator, 9);
    store(mAssignmentOperator, 10);
    store(mIncrementOperator, 11);
    store(mDecrementOperator, 12);
    store(mAndWithOperator, 13);
    store(mOrWithOperator, 14);
    store(mUnaryMinusOperator, 2);
    store(mPlusOperator, 15);
    store(mMinusOperator, 16);
    store(mTimesOperator, 17);
    store(mDivideOperator, 18);
    store(mModuloOperator, 19);
    store(mRangeOperator, 20);

    storeComplements(mEqualsOperator, mNotEqualsOperator);
    storeComplements(mLessThanOperator, mGreaterEqualsOperator);
    storeComplements(mGreaterThanOperator, mLessEqualsOperator);
    storeSwap(mGreaterThanOperator, mLessThanOperator);
    storeSwap(mGreaterEqualsOperator, mLessEqualsOperator);
    storeAssignment(mIncrementOperator, mPlusOperator);
    storeAssignment(mDecrementOperator, mMinusOperator);
    storeAssignment(mAndWithOperator, mAndOperator);
    storeAssignment(mOrWithOperator, mOrOperator);

    mMaxFunction = new MaxFunction();
    mMinFunction = new MinFunction();
    mIteFunction = new IteFunction();
    store(mMaxFunction);
    store(mMinFunction);
    store(mIteFunction);
  }

  private void storeComplements(final BinaryOperator op1,
                                final BinaryOperator op2)
  {
    mComplementMap.put(op1, op2);
    mComplementMap.put(op2, op1);
  }

  private void storeSwap(final BinaryOperator op,
                         final BinaryOperator normal)
  {
    mSwapMap.put(op, normal);
  }

  private void storeAssignment(final BinaryOperator op1,
                               final BinaryOperator op2)
  {
    mAssigningMap.put(op1, op2);
  }


  //#########################################################################
  //# Access by Logic Semantics
  public BinaryOperator getAndOperator()
  {
    return mAndOperator;
  }

  public BinaryOperator getAndWithOperator()
  {
    return mAndWithOperator;
  }

  public BinaryOperator getAssignmentOperator()
  {
    return mAssignmentOperator;
  }

  public BinaryOperator getDecrementOperator()
  {
    return mDecrementOperator;
  }

  public BinaryOperator getEqualsOperator()
  {
    return mEqualsOperator;
  }

  public BinaryOperator getGreaterEqualsOperator()
  {
    return mGreaterEqualsOperator;
  }

  public BinaryOperator getGreaterThanOperator()
  {
    return mGreaterThanOperator;
  }

  public BinaryOperator getLessEqualsOperator()
  {
    return mLessEqualsOperator;
  }

  public BinaryOperator getIncrementOperator()
  {
    return mIncrementOperator;
  }

  public BinaryOperator getLessThanOperator()
  {
    return mLessThanOperator;
  }

  public BinaryOperator getMinusOperator()
  {
    return mMinusOperator;
  }

  public UnaryOperator getNextOperator()
  {
    return mUnaryNextOperator;
  }

  public UnaryOperator getNotOperator()
  {
    return mNotOperator;
  }

  public BinaryOperator getNotEqualsOperator()
  {
    return mNotEqualsOperator;
  }

  public BinaryOperator getOrOperator()
  {
    return mOrOperator;
  }

  public BinaryOperator getOrWithOperator()
  {
    return mOrWithOperator;
  }

  public BinaryOperator getPlusOperator()
  {
    return mPlusOperator;
  }

  public BinaryOperator getTimesOperator()
  {
    return mTimesOperator;
  }

  public BinaryOperator getDivideOperator()
  {
    return mDivideOperator;
  }

  public BinaryOperator getModuloOperator()
  {
    return mModuloOperator;
  }

  public BinaryOperator getRangeOperator()
  {
    return mRangeOperator;
  }

  public UnaryOperator getUnaryMinusOperator()
  {
    return mUnaryMinusOperator;
  }


  public BinaryOperator getComplementaryOperator(final BinaryOperator op)
  {
    return mComplementMap.get(op);
  }

  public BinaryOperator getSwappedNormalOperator(final BinaryOperator op)
  {
    return mSwapMap.get(op);
  }

  /**
   * Retrieves the arithmetic operator for modifying assignment operators
   * such as&nbsp;+=. For example, the assigning operator of&nbsp;+=
   * is&nbsp;+. If the given operator is not an assignment, or the normal
   * assignment operator&nbsp;=, then <CODE>null</CODE> is returned.
   */
  public BinaryOperator getAssigningOperator(final BinaryOperator op)
  {
    return mAssigningMap.get(op);
  }


  public BuiltInFunction getIteFunction()
  {
    return mIteFunction;
  }

  public BuiltInFunction getMaxFunction()
  {
    return mMaxFunction;
  }

  public BuiltInFunction getMinFunction()
  {
    return mMinFunction;
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Checks whether the given expression is a not-expression.
   * @param   expr    The expression to be checked.
   * @return  If the given expression is a not-expression, its subterm is
   *          returned, otherwise <CODE>null</CODE> is returned.
   */
  public SimpleExpressionProxy getNegatedSubterm(final SimpleExpressionProxy expr)
  {
    if (expr instanceof UnaryExpressionProxy) {
      final UnaryExpressionProxy unary = (UnaryExpressionProxy) expr;
      if (unary.getOperator() == mNotOperator) {
        return unary.getSubTerm();
      }
    }
    return null;
  }


  //#########################################################################
  //# Inner Class AbstractBinaryOperator
  /**
   * The abstract type of all binary operators whose parse result is
   * a binary expression ({@link BinaryExpressionProxy}).
   */
  private static abstract class AbstractBinaryOperator
    implements BinaryOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public BinaryExpressionProxy createExpression
      (final ModuleProxyFactory factory,
       final SimpleExpressionProxy lhs,
       final SimpleExpressionProxy rhs,
       final String text)
    {
      return factory.createBinaryExpressionProxy(text, this, lhs, rhs);
    }

    @Override
    public SimpleExpressionProxy simplify
      (final BinaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy simpLHS = simplifier.simplify(origLHS);
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpRHS = simplifier.simplify(origRHS);
      return createExpression(simplifier, simpLHS, simpRHS);
    }

    //#######################################################################
    //# Auxiliary Methods
    BinaryExpressionProxy createExpression
      (final AbstractSimpleExpressionSimplifier simplifier,
       final SimpleExpressionProxy lhs,
       final SimpleExpressionProxy rhs)
    {
      final ModuleProxyFactory factory = simplifier.getFactory();
      final Comparator<SimpleExpressionProxy> comparator =
        simplifier.getExpressionComparator();
      if (isSymmetric() && comparator.compare(lhs, rhs) > 0) {
        return createExpression(factory, rhs, lhs, null);
      } else {
        return createExpression(factory, lhs, rhs, null);
      }
    }

  }


  //#########################################################################
  //# Inner Class AbstractUnaryOperator
  /**
   * The abstract type of all binary operators whose parse result is
   * a unary expression ({@link UnaryExpressionProxy}).
   */
  private static abstract class AbstractUnaryOperator
    implements UnaryOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public int getPriority()
    {
      return PRIORITY_UNARY;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.UnaryOperator
    @Override
    public boolean isPrefix()
    {
      return true;
    }

    @Override
    public SimpleExpressionProxy simplify
      (final UnaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final SimpleExpressionProxy subexpr = expr.getSubTerm();
      final SimpleExpressionProxy subresult = simplifier.simplify(subexpr);
      return createExpression(simplifier, subresult);
    }

    //#######################################################################
    //# Auxiliary Methods
    UnaryExpressionProxy createExpression
      (final AbstractSimpleExpressionSimplifier simplifier,
       final SimpleExpressionProxy subterm)
    {
      final ModuleProxyFactory factory = simplifier.getFactory();
      return factory.createUnaryExpressionProxy(this, subterm);
    }

  }


  //#########################################################################
  //# Inner Class AbstractBinaryIntOperator
  /**
   * The abstract type of all binary operators that combine two integer
   * values and return another integer.
   */
  private static abstract class AbstractBinaryIntOperator
    extends AbstractBinaryOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public int getLHSTypes()
    {
      return Operator.TYPE_INT;
    }

    @Override
    public int getRHSTypes()
    {
      return Operator.TYPE_INT;
    }

    @Override
    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      return lhsType & rhsType & Operator.TYPE_INT;
    }

    @Override
    public SimpleExpressionProxy simplify
      (final BinaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy simpLHS = simplifier.simplify(origLHS);
      final boolean atomLHS = simplifier.isAtomicValue(simpLHS);
      final int intLHS = atomLHS ? simplifier.getIntValue(simpLHS) : -1;
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpRHS = simplifier.simplify(origRHS);
      final boolean atomRHS = simplifier.isAtomicValue(simpRHS);
      final int intRHS = atomRHS ? simplifier.getIntValue(simpRHS) : -1;
      if (atomLHS && atomRHS) {
        final int result = eval(intLHS, intRHS);
        return simplifier.createIntConstantProxy(result);
      } else {
        return createExpression(simplifier, simpLHS, simpRHS);
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_LEFT;
    }

    //#######################################################################
    //# Provided by Subclasses
    abstract int eval(int lhs, int rhs)
      throws EvalException;

  }


  //#########################################################################
  //# Inner Class BinaryPlusOperator
  private static class BinaryPlusOperator extends AbstractBinaryIntOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_PLUS;
    }

    @Override
    public int getPriority()
    {
      return PRIORITY_PLUS;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public boolean isSymmetric()
    {
      return true;
    }

    @Override
    public SimpleExpressionProxy simplify
      (final BinaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy simpLHS = simplifier.simplify(origLHS);
      final boolean atomLHS = simplifier.isAtomicValue(simpLHS);
      final int intLHS = atomLHS ? simplifier.getIntValue(simpLHS) : -1;
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpRHS = simplifier.simplify(origRHS);
      final boolean atomRHS = simplifier.isAtomicValue(simpRHS);
      final int intRHS = atomRHS ? simplifier.getIntValue(simpRHS) : -1;
      if (atomLHS && atomRHS) {
        return simplifier.createIntConstantProxy(intLHS + intRHS);
      } else if (intLHS == 0) {
        return simpRHS;
      } else if (intRHS == 0) {
        return simpLHS;
      } else {
        return createExpression(simplifier, simpLHS, simpRHS);
      }
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryIntOperator
    @Override
    int eval(final int lhs, final int rhs)
    {
      return lhs + rhs;
    }

  }


  //#########################################################################
  //# Inner Class BinaryMinusOperator
  private static class BinaryMinusOperator extends AbstractBinaryIntOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_MINUS;
    }

    @Override
    public int getPriority()
    {
      return PRIORITY_PLUS;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public boolean isSymmetric()
    {
      return false;
    }

    @Override
    public SimpleExpressionProxy simplify
      (final BinaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy simpLHS = simplifier.simplify(origLHS);
      final boolean atomLHS = simplifier.isAtomicValue(simpLHS);
      final int intLHS = atomLHS ? simplifier.getIntValue(simpLHS) : -1;
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpRHS = simplifier.simplify(origRHS);
      final boolean atomRHS = simplifier.isAtomicValue(simpRHS);
      final int intRHS = atomRHS ? simplifier.getIntValue(simpRHS) : -1;
      if (atomLHS && atomRHS) {
        return simplifier.createIntConstantProxy(intLHS - intRHS);
      } else if (intLHS == 0) {
        final ModuleProxyFactory factory = simplifier.getFactory();
        return factory.createUnaryExpressionProxy
          (INSTANCE.mUnaryMinusOperator, simpRHS);
      } else if (intRHS == 0) {
        return simpLHS;
      }
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      if (eq.equals(simpLHS, simpRHS)) {
        return simplifier.createIntConstantProxy(0);
      } else {
        return createExpression(simplifier, simpLHS, simpRHS);
      }
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryIntOperator
    @Override
    int eval(final int lhs, final int rhs)
    {
      return lhs - rhs;
    }

  }


  //#########################################################################
  //# Inner Class BinaryTimesOperator
  private static class BinaryTimesOperator extends AbstractBinaryIntOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_TIMES;
    }

    @Override
    public int getPriority()
    {
      return PRIORITY_TIMES;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public boolean isSymmetric()
    {
      return true;
    }

    @Override
    public SimpleExpressionProxy simplify
      (final BinaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpLHS = simplifier.simplify(origLHS);
      final boolean atomLHS = simplifier.isAtomicValue(simpLHS);
      final int intLHS = atomLHS ? simplifier.getIntValue(simpLHS) : -1;
      if (atomLHS) {
        switch (intLHS) {
        case 0:
          return simplifier.createIntConstantProxy(0);
        case 1:
          return simplifier.simplify(origRHS);
        case -1:
          final ModuleProxyFactory factory = simplifier.getFactory();
          final UnaryExpressionProxy uminus =
            factory.createUnaryExpressionProxy(INSTANCE.mUnaryMinusOperator,
                                               origRHS);
          return simplifier.simplify(uminus);
        default:
          break;
        }
      }
      final SimpleExpressionProxy simpRHS = simplifier.simplify(origRHS);
      if (simplifier.isAtomicValue(simpRHS)) {
        final int intRHS = simplifier.getIntValue(simpRHS);
        switch (intRHS) {
        case 0:
          return simplifier.createIntConstantProxy(0);
        case 1:
          return simpLHS;
        case -1:
          if (atomLHS) {
            return simplifier.createIntConstantProxy(-intLHS);
          } else {
            final ModuleProxyFactory factory = simplifier.getFactory();
            return factory.createUnaryExpressionProxy
              (INSTANCE.mUnaryMinusOperator, simpLHS);
          }
        default:
          if (atomLHS) {
            return simplifier.createIntConstantProxy(intLHS * intRHS);
          }
          break;
        }
      }
      return createExpression(simplifier, simpLHS, simpRHS);
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryIntOperator
    @Override
    int eval(final int lhs, final int rhs)
    {
      return lhs * rhs;
    }

  }


  //#########################################################################
  //# Inner Class BinaryDivideOperator
  private static class BinaryDivideOperator extends AbstractBinaryIntOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_DIVIDE;
    }

    @Override
    public int getPriority()
    {
      return PRIORITY_TIMES;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public boolean isSymmetric()
    {
      return false;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryIntOperator
    @Override
    int eval(final int lhs, final int rhs)
      throws DivisionByZeroException
    {
      if (rhs != 0) {
        return lhs / rhs;
      } else {
        throw new DivisionByZeroException();
      }
    }

  }


  //#########################################################################
  //# Inner Class BinaryModuloOperator
  private static class BinaryModuloOperator extends AbstractBinaryIntOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_MODULO;
    }

    @Override
    public int getPriority()
    {
      return PRIORITY_TIMES;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public boolean isSymmetric()
    {
      return false;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryIntOperator
    @Override
    int eval(final int lhs, final int rhs)
      throws DivisionByZeroException
    {
      if (rhs != 0) {
        return lhs % rhs;
      } else {
        throw new DivisionByZeroException();
      }
    }

  }


  //#########################################################################
  //# Inner Class AbstractUnaryIntOperator
  /**
   * The abstract type of all unary operators that take an integer value
   * and return another integer.
   */
  private static abstract class AbstractUnaryIntOperator
    extends AbstractUnaryOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.UnaryOperator
    @Override
    public int getArgTypes()
    {
      return Operator.TYPE_INT;
    }

    @Override
    public int getReturnTypes(final int argType)
    {
      return argType & Operator.TYPE_INT;
    }

    @Override
    public SimpleExpressionProxy simplify
      (final UnaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final SimpleExpressionProxy subexpr = expr.getSubTerm();
      final SimpleExpressionProxy subresult = simplifier.simplify(subexpr);
      if (simplifier.isAtomicValue(subresult)) {
        final int subvalue = simplifier.getIntValue(subresult);
        final int value = eval(subvalue);
        return simplifier.createIntConstantProxy(value);
      } else {
        return createExpression(simplifier, subresult);
      }
    }

    //#######################################################################
    //# Provided by Subclasses
    abstract int eval(int arg)
      throws EvalException;

  }


  //#########################################################################
  //# Inner Class UnaryMinusOperator
  private static class UnaryMinusOperator extends AbstractUnaryIntOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_MINUS;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractUnaryIntOperator
    @Override
    int eval(final int arg)
    {
      return -arg;
    }

  }


  //#########################################################################
  //# Inner Class AbstractBinaryEqualsOperator
  /**
   * The abstract type of all binary operators that take combine values of
   * arbitrary, but matching types and return a boolean value as result.
   */
  private abstract static class AbstractBinaryEqualsOperator
    extends AbstractBinaryOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public int getLHSTypes()
    {
      return Operator.TYPE_ANY;
    }

    @Override
    public int getRHSTypes()
    {
      return Operator.TYPE_ANY;
    }

    @Override
    public boolean isSymmetric()
    {
      return true;
    }

    @Override
    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      if ((lhsType & rhsType) != 0) {
        return Operator.TYPE_BOOLEAN;
      } else {
        return 0;
      }
    }

    @Override
    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_RIGHT;
    }

    @Override
    public SimpleExpressionProxy simplify
      (final BinaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final boolean eresult = getEqualsResult();
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy origRHS = expr.getRight();
      if (eq.equals(origLHS, origRHS)) {
        return simplifier.createBooleanConstantProxy(eresult);
      }
      final SimpleExpressionProxy simpLHS = simplifier.simplify(origLHS);
      final SimpleExpressionProxy simpRHS = simplifier.simplify(origRHS);
      if (eq.equals(simpLHS, simpRHS)) {
        return simplifier.createBooleanConstantProxy(eresult);
      } else if (simplifier.isAtomicValue(simpLHS) &&
                 simplifier.isAtomicValue(simpRHS)) {
        return simplifier.createBooleanConstantProxy(!eresult);
      } else {
        return createExpression(simplifier, simpLHS, simpRHS);
      }
    }

    //#######################################################################
    //# Provided by Subclasses
    abstract boolean getEqualsResult();

  }


  //#########################################################################
  //# Inner Class BinaryEqualsOperator
  private static class BinaryEqualsOperator
    extends AbstractBinaryEqualsOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_EQUALS;
    }

    @Override
    public int getPriority()
    {
      return PRIORITY_EQUALS;
    }

    //#######################################################################
    //# Overrides for Abstract Base Class AbstractBinaryEqualsOperator
    @Override
    boolean getEqualsResult()
    {
      return true;
    }

  }


  //#########################################################################
  //# Inner Class BinaryNotEqualsOperator
  private static class BinaryNotEqualsOperator
    extends AbstractBinaryEqualsOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_NOTEQUALS;
    }

    @Override
    public int getPriority()
    {
      return PRIORITY_EQUALS;
    }

    //#######################################################################
    //# Overrides for Abstract Base Class AbstractBinaryEqualsOperator
    @Override
    boolean getEqualsResult()
    {
      return false;
    }

  }


  //#########################################################################
  //# Inner Class AbstractBinaryComparisonOperator
  /**
   * The abstract type of all binary operators that combine two integer
   * values and return a boolean result.
   */
  private static abstract class AbstractBinaryComparisonOperator
    extends AbstractBinaryOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public int getLHSTypes()
    {
      return Operator.TYPE_INT;
    }

    @Override
    public int getRHSTypes()
    {
      return Operator.TYPE_INT;
    }

    @Override
    public boolean isSymmetric()
    {
      return false;
    }

    @Override
    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      if ((lhsType & rhsType & Operator.TYPE_INT) != 0) {
        return Operator.TYPE_BOOLEAN;
      } else {
        return 0;
      }
    }

    @Override
    public SimpleExpressionProxy simplify
      (final BinaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy simpLHS = simplifier.simplify(origLHS);
      final boolean atomLHS = simplifier.isAtomicValue(simpLHS);
      final int intLHS = atomLHS ? simplifier.getIntValue(simpLHS) : -1;
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpRHS = simplifier.simplify(origRHS);
      final boolean atomRHS = simplifier.isAtomicValue(simpRHS);
      final int intRHS = atomRHS ? simplifier.getIntValue(simpRHS) : -1;
      if (atomLHS && atomRHS) {
        final boolean result = eval(intLHS, intRHS);
        return simplifier.createBooleanConstantProxy(result);
      }
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      if (eq.equals(simpLHS, simpRHS)) {
        final boolean result = includesEquality();
        return simplifier.createBooleanConstantProxy(result);
      } else {
        return createExpression(simplifier, simpLHS, simpRHS);
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public int getPriority()
    {
      return PRIORITY_EQUALS;
    }

    @Override
    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_RIGHT;
    }

    //#######################################################################
    //# Provided by Subclasses
    abstract boolean includesEquality();

    abstract boolean eval(int lhs, int rhs)
      throws EvalException;

  }


  //#########################################################################
  //# Inner Class BinaryGreaterThanOperator
  private static class BinaryGreaterThanOperator
    extends AbstractBinaryComparisonOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_GREATER_THAN;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryComparisonOperator
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

  }


  //#########################################################################
  //# Inner Class BinaryGreaterEqualsOperator
  private static class BinaryGreaterEqualsOperator
    extends AbstractBinaryComparisonOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_GREATER_EQUALS;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryComparisonOperator
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

  }

  //#########################################################################
  //# Inner Class BinaryLessThanOperator
  private static class BinaryLessThanOperator
    extends AbstractBinaryComparisonOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_LESS_THAN;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryComparisonOperator
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

  }


  //#########################################################################
  //# Inner Class BinaryLessEqualsOperator
  private static class BinaryLessEqualsOperator
    extends AbstractBinaryComparisonOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_LESS_EQUALS;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryComparisonOperator
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

  }


  //#########################################################################
  //# Inner Class AbstractBinaryBooleanOperator
  /**
   * The abstract type of all binary operators that combine two Boolean
   * values and return another Boolean.
   */
  private static abstract class AbstractBinaryBooleanOperator
    extends AbstractBinaryOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public int getLHSTypes()
    {
      return Operator.TYPE_BOOLEAN | Operator.TYPE_INT;
    }

    @Override
    public int getRHSTypes()
    {
      return Operator.TYPE_BOOLEAN | Operator.TYPE_INT;
    }

    @Override
    public boolean isSymmetric()
    {
      return true;
    }

    @Override
    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      if ((lhsType & (Operator.TYPE_BOOLEAN | Operator.TYPE_INT)) != 0 &&
          (rhsType & (Operator.TYPE_BOOLEAN | Operator.TYPE_INT)) != 0) {
        return Operator.TYPE_BOOLEAN;
      } else {
        return 0;
        }
    }

    @Override
    public SimpleExpressionProxy simplify
      (final BinaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy simpLHS = simplifier.simplify(origLHS);
      final boolean atomLHS = simplifier.isAtomicValue(simpLHS);
      final boolean boolLHS = atomLHS && simplifier.getBooleanValue(simpLHS);
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpRHS = simplifier.simplify(origRHS);
      final boolean atomRHS = simplifier.isAtomicValue(simpRHS);
      final boolean boolRHS = atomRHS && simplifier.getBooleanValue(simpRHS);
      if (atomLHS && atomRHS) {
        final boolean result = eval(boolLHS, boolRHS);
        return simplifier.createBooleanConstantProxy(result);
      } else {
        return createExpression(simplifier, simpLHS, simpRHS);
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_LEFT;
    }

    //#######################################################################
    //# Provided by Subclasses
    abstract boolean eval(boolean lhs, boolean rhs)
      throws EvalException;

  }


  //#########################################################################
  //# Inner Class BinaryAndOperator
  private static class BinaryAndOperator extends AbstractBinaryBooleanOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_AND;
    }

    @Override
    public int getPriority()
    {
      return PRIORITY_AND;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public SimpleExpressionProxy simplify
      (final BinaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpLHS = simplifier.simplify(origLHS);
      if (simplifier.isAtomicValue(simpLHS)) {
        final boolean boolLHS = simplifier.getBooleanValue(simpLHS);
        if (boolLHS) {
          return simplifier.simplify(origRHS);
        } else {
          return simplifier.createBooleanConstantProxy(false);
        }
      }
      final SimpleExpressionProxy simpRHS = simplifier.simplify(origRHS);
      if (simplifier.isAtomicValue(simpRHS)) {
        final boolean boolRHS = simplifier.getBooleanValue(simpRHS);
        if (boolRHS) {
          return simpLHS;
        } else {
          return simplifier.createBooleanConstantProxy(false);
        }
      }
      return createExpression(simplifier, simpLHS, simpRHS);
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryBooleanOperator
    @Override
    boolean eval(final boolean lhs, final boolean rhs)
    {
      return lhs && rhs;
    }

  }


  //#########################################################################
  //# Inner Class BinaryOrOperator
  private static class BinaryOrOperator extends AbstractBinaryBooleanOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_OR;
    }

    @Override
    public int getPriority()
    {
      return PRIORITY_OR;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public SimpleExpressionProxy simplify
      (final BinaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpLHS = simplifier.simplify(origLHS);
      if (simplifier.isAtomicValue(simpLHS)) {
        final boolean boolLHS = simplifier.getBooleanValue(simpLHS);
        if (boolLHS) {
          return simplifier.createBooleanConstantProxy(true);
        } else {
          return simplifier.simplify(origRHS);
        }
      }
      final SimpleExpressionProxy simpRHS = simplifier.simplify(origRHS);
      if (simplifier.isAtomicValue(simpRHS)) {
        final boolean boolRHS = simplifier.getBooleanValue(simpRHS);
        if (boolRHS) {
          return simplifier.createBooleanConstantProxy(true);
        } else {
          return simpLHS;
        }
      }
      return createExpression(simplifier, simpLHS, simpRHS);
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryBooleanOperator
    @Override
    boolean eval(final boolean lhs, final boolean rhs)
    {
      return lhs || rhs;
    }

  }


  //#########################################################################
  //# Inner Class AbstractUnaryBooleanOperator
  /**
   * The abstract type of all unary operators that take a Boolean value
   * and return another Boolean.
   */
  private static abstract class AbstractUnaryBooleanOperator
    extends AbstractUnaryOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.UnaryOperator
    @Override
    public int getArgTypes()
    {
      return Operator.TYPE_BOOLEAN | Operator.TYPE_INT;
    }

    @Override
    public int getReturnTypes(final int argType)
    {
      if ((argType & (Operator.TYPE_BOOLEAN | Operator.TYPE_INT)) != 0) {
        return Operator.TYPE_BOOLEAN;
      } else {
        return 0;
      }
    }

    @Override
    public SimpleExpressionProxy simplify
      (final UnaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final SimpleExpressionProxy subexpr = expr.getSubTerm();
      final SimpleExpressionProxy subresult = simplifier.simplify(subexpr);
      if (simplifier.isAtomicValue(subresult)) {
        final boolean subboolean = simplifier.getBooleanValue(subresult);
        final boolean resboolean = eval(subboolean);
        return simplifier.createBooleanConstantProxy(resboolean);
      } else {
        return createExpression(simplifier, subresult);
      }
    }

    //#######################################################################
    //# Provided by Subclasses
    abstract boolean eval(boolean arg)
      throws EvalException;

  }


  //#########################################################################
  //# Inner Class UnaryNotOperator
  private static class UnaryNotOperator extends AbstractUnaryBooleanOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_NOT;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractUnaryBooleanOperator
    @Override
    boolean eval(final boolean arg)
    {
      return !arg;
    }

  }


  //#########################################################################
  //# Inner Class BinaryRangeOperator
  private static class BinaryRangeOperator
    extends AbstractBinaryOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_RANGE;
    }

    @Override
    public int getPriority()
    {
      return PRIORITY_RANGE;
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_NONE;
    }

    @Override
    public int getLHSTypes()
    {
      return Operator.TYPE_INT;
    }

    @Override
    public int getRHSTypes()
    {
      return Operator.TYPE_INT;
    }

    @Override
    public boolean isSymmetric()
    {
      return false;
    }

    @Override
    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      if ((lhsType & rhsType & Operator.TYPE_INT) != 0) {
        return Operator.TYPE_RANGE;
      } else {
        return 0;
      }
    }

    @Override
    public SimpleExpressionProxy simplify
      (final BinaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy simpLHS = simplifier.simplify(origLHS);
      if (simplifier.isAtomicValue(simpLHS)) {
        simplifier.getIntValue(simpLHS);
      }
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpRHS = simplifier.simplify(origRHS);
      if (simplifier.isAtomicValue(simpRHS)) {
        simplifier.getIntValue(simpRHS);
      }
      return createExpression(simplifier, simpLHS, simpRHS);
    }

  }


  //#########################################################################
  //# Inner Class AbstractBinaryAssignmentOperator
  /**
   * The abstract type of all binary assignment operators.
   * These operators take a symbol as their first argument and
   * an arbitrary expression as their second argument.
   * When evaluated, they produce the result of the assignment.
   */
  private abstract static class AbstractBinaryAssignmentOperator
    extends AbstractBinaryOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public int getPriority()
    {
      return PRIORITY_ASSIGNMENT;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public int getLHSTypes()
    {
      return Operator.TYPE_NAME;
    }

    @Override
    public int getRHSTypes()
    {
      return Operator.TYPE_ANY;
    }

    @Override
    public boolean isSymmetric()
    {
      return false;
    }

    @Override
    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      if ((lhsType & Operator.TYPE_NAME) != 0) {
        return rhsType;
      } else {
        return 0;
      }
    }

    @Override
    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_RIGHT;
    }

  }


  //#########################################################################
  //# Inner Class BinaryAssignmentOperator
  private static class BinaryAssignmentOperator
    extends AbstractBinaryAssignmentOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_ASSIGNMENT;
    }

  }


  //#########################################################################
  //# Inner Class BinaryIncrementOperator
  private static class BinaryIncrementOperator
    extends AbstractBinaryAssignmentOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public int getRHSTypes()
    {
      return Operator.TYPE_INT;
    }

    @Override
    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      if ((rhsType & Operator.TYPE_INT) != 0) {
        return super.getReturnTypes(lhsType, rhsType);
      } else {
        return 0;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_INCREMENT;
    }

  }


  //#########################################################################
  //# Inner Class BinaryDecrementOperator
  private static class BinaryDecrementOperator
    extends AbstractBinaryAssignmentOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public int getRHSTypes()
    {
      return Operator.TYPE_INT;
    }

    @Override
    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      if ((rhsType & Operator.TYPE_INT) != 0) {
        return super.getReturnTypes(lhsType, rhsType);
      } else {
        return 0;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_DECREMENT;
    }

  }


  //#########################################################################
  //# Inner Class BinaryAndWithOperator
  private static class BinaryAndWithOperator
    extends AbstractBinaryAssignmentOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public int getRHSTypes()
    {
      return Operator.TYPE_BOOLEAN;
    }

    @Override
    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      if ((rhsType & Operator.TYPE_BOOLEAN) != 0) {
        return super.getReturnTypes(lhsType, rhsType);
      } else {
        return 0;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_ANDWITH;
    }

  }


  //#########################################################################
  //# Inner Class BinaryAndWithOperator
  private static class BinaryOrWithOperator
    extends AbstractBinaryAssignmentOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public int getRHSTypes()
    {
      return Operator.TYPE_BOOLEAN;
    }

    @Override
    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      if ((rhsType & Operator.TYPE_BOOLEAN) != 0) {
        return super.getReturnTypes(lhsType, rhsType);
      } else {
        return 0;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_ORWITH;
    }

  }


  //#########################################################################
  //# Inner Class BinaryQualificationOperator
  /**
   * The abstract type of all binary operators that combine two integer
   * values and return another integer.
   */
  private static class BinaryQualificationOperator
    implements BinaryOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_QUAL;
    }

    @Override
    public int getPriority()
    {
      return PRIORITY_QUAL;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    @Override
    public int getLHSTypes()
    {
      return Operator.TYPE_NAME;
    }

    @Override
    public int getRHSTypes()
    {
      return Operator.TYPE_NAME;
    }

    @Override
    public boolean isSymmetric()
    {
      return false;
    }

    @Override
    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      return lhsType & rhsType & Operator.TYPE_NAME;
    }

    @Override
    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_RIGHT;
    }

    @Override
    public QualifiedIdentifierProxy createExpression
      (final ModuleProxyFactory factory,
       final SimpleExpressionProxy lhs,
       final SimpleExpressionProxy rhs,
       final String text)
    {
      return factory.createQualifiedIdentifierProxy
        (text, (IdentifierProxy) lhs, (IdentifierProxy) rhs);
    }

    @Override
    public SimpleExpressionProxy simplify
      (final BinaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy simpLHS = simplifier.simplify(origLHS);
      final IdentifierProxy identLHS = simplifier.getIdentifierValue(simpLHS);
      final SimpleExpressionProxy origRHS = expr.getRight();
      final SimpleExpressionProxy simpRHS = simplifier.simplify(origRHS);
      final IdentifierProxy identRHS = simplifier.getIdentifierValue(simpRHS);
      final ModuleProxyFactory factory = simplifier.getFactory();
      final IdentifierProxy qual =
        factory.createQualifiedIdentifierProxy(identLHS, identRHS);
      return simplifier.simplify(qual);
    }

  }


  //#########################################################################
  //# Inner Class UnaryNextOperator
  /**
   * The next-state (prime) operator.
   * Takes as argument a name and returns a name.
   */
  private static class UnaryNextOperator
    extends AbstractUnaryOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return OPNAME_NEXT;
    }

    @Override
    public int getPriority()
    {
      return PRIORITY_NEXT;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.UnaryOperator
    @Override
    public boolean isPrefix()
    {
      return false;
    }

    @Override
    public int getArgTypes()
    {
      return Operator.TYPE_NAME;
    }

    @Override
    public int getReturnTypes(final int argType)
    {
      return argType & Operator.TYPE_INDEX;
    }

  }


  //#########################################################################
  //# Inner Class AbstractBuiltInFunction
  private abstract static class AbstractBuiltInFunction
    implements BuiltInFunction
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public int getPriority()
    {
      return PRIORITY_FUNCALL;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BuiltInFunction
    @Override
    public SimpleExpressionProxy createExpression
      (final ModuleProxyFactory factory,
       final List<SimpleExpressionProxy> args,
       final String text)
    {
      final String name = getName();
      return factory.createFunctionCallExpressionProxy(text, name, args);
    }

    @Override
    public SimpleExpressionProxy simplify(final FunctionCallExpressionProxy expr,
                                          final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      // TODO Auto-generated method stub
      return null;
    }
  }


  //#########################################################################
  //# Inner Class IteFunction
  /**
   * The if-then-else function.
   * Function <CODE>\ite(c,x,y)</CODE> a Boolean argument&nbsp;<CODE>c</CODE>
   * and two arguments <CODE>x</CODE> and&nbsp;<CODE>y</CODE> of arbitrary
   * type. If <CODE>c</CODE> is true, the function returns&nbsp;<CODE>x</CODE>,
   * otherwise&nbsp;<CODE>y</CODE>.
   */
  private static class IteFunction
    extends AbstractBuiltInFunction
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return FUNNAME_ITE;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BuiltInFunction
    @Override
    public int getMinimumNumberOfArguments()
    {
      return 3;
    }

    @Override
    public int getMaximumNumberOfArguments()
    {
      return 3;
    }

    @Override
    public int getArgumentTypes(final int argno)
    {
      if (argno == 0) {
        return Operator.TYPE_BOOLEAN;
      } else {
        return Operator.TYPE_ANY;
      }
    }

    @Override
    public int getReturnTypes(final int[] argTypes)
    {
      if ((argTypes[0] & Operator.TYPE_BOOLEAN) != 0) {
        return argTypes[1] & argTypes[2];
      } else {
        return 0;
      }
    }
  }


  //#########################################################################
  //# Inner Class MinFunction
  /**
   * Common superclass to implement minimum and maximum function.
   * Both functions a variable number of integer arguments and returns
   * an integer.
   */
  private static abstract class MinMaxFunction
    extends AbstractBuiltInFunction
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BuiltInFunction
    @Override
    public int getMinimumNumberOfArguments()
    {
      return 2;
    }

    @Override
    public int getMaximumNumberOfArguments()
    {
      return Integer.MAX_VALUE;
    }

    @Override
    public int getArgumentTypes(final int argno)
    {
      return Operator.TYPE_INT | Operator.TYPE_BOOLEAN;
    }

    @Override
    public int getReturnTypes(final int[] argTypes)
    {
      int type = Operator.TYPE_INT | Operator.TYPE_BOOLEAN;
      for (final int mask : argTypes) {
        type &= mask;
      }
      return type;
    }
  }


  //#########################################################################
  //# Inner Class MaxFunction
  /**
   * The maximum function.
   * Take a variable number of integer arguments and returns the largest
   * argument.
   */
  private static class MaxFunction
    extends MinMaxFunction
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return FUNNAME_MAX;
    }
  }


  //#########################################################################
  //# Inner Class MinFunction
  /**
   * The minimum function.
   * Take a variable number of integer arguments and returns the smallest
   * argument.
   */
  private static class MinFunction
    extends MinMaxFunction
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    @Override
    public String getName()
    {
      return FUNNAME_MIN;
    }
  }


  //#########################################################################
  //# Data Members
  private final BinaryOperator mAndOperator;
  private final BinaryOperator mOrOperator;
  private final UnaryOperator mNotOperator;
  private final BinaryOperator mEqualsOperator;
  private final BinaryOperator mNotEqualsOperator;
  private final BinaryOperator mGreaterThanOperator;
  private final BinaryOperator mGreaterEqualsOperator;
  private final BinaryOperator mLessThanOperator;
  private final BinaryOperator mLessEqualsOperator;
  private final BinaryOperator mAssignmentOperator;
  private final BinaryOperator mIncrementOperator;
  private final BinaryOperator mDecrementOperator;
  private final BinaryOperator mAndWithOperator;
  private final BinaryOperator mOrWithOperator;
  private final BinaryOperator mPlusOperator;
  private final BinaryOperator mMinusOperator;
  private final UnaryOperator mUnaryMinusOperator;
  private final BinaryOperator mTimesOperator;
  private final BinaryOperator mDivideOperator;
  private final BinaryOperator mModuloOperator;
  private final BinaryOperator mRangeOperator;
  private final UnaryOperator mUnaryNextOperator;
  private final Map<BinaryOperator,BinaryOperator> mComplementMap;
  private final Map<BinaryOperator,BinaryOperator> mSwapMap;
  private final Map<BinaryOperator,BinaryOperator> mAssigningMap;

  private final BuiltInFunction mIteFunction;
  private final BuiltInFunction mMaxFunction;
  private final BuiltInFunction mMinFunction;


  //#########################################################################
  //# Class Constants
  private static final CompilerOperatorTable INSTANCE =
    new CompilerOperatorTable();

  private static final String OPNAME_QUAL = ".";
  private static final String OPNAME_EQUALS = "==";
  private static final String OPNAME_NOTEQUALS = "!=";
  private static final String OPNAME_GREATER_THAN = ">";
  private static final String OPNAME_GREATER_EQUALS = ">=";
  private static final String OPNAME_LESS_THAN = "<";
  private static final String OPNAME_LESS_EQUALS = "<=";
  private static final String OPNAME_MINUS = "-";
  private static final String OPNAME_PLUS = "+";
  private static final String OPNAME_TIMES = "*";
  private static final String OPNAME_DIVIDE = "/";
  private static final String OPNAME_MODULO = "%";
  private static final String OPNAME_RANGE = "..";
  private static final String OPNAME_AND = "&";
  private static final String OPNAME_OR = "|";
  private static final String OPNAME_NOT = "!";
  private static final String OPNAME_NEXT = "'";
  private static final String OPNAME_INCREMENT = "+=";
  private static final String OPNAME_DECREMENT = "-=";
  private static final String OPNAME_ANDWITH = "&=";
  private static final String OPNAME_ORWITH = "|=";
  private static final String OPNAME_ASSIGNMENT = "=";

  private static final String FUNNAME_ITE = "\\ite";
  private static final String FUNNAME_MAX = "\\max";
  private static final String FUNNAME_MIN = "\\min";

  private static final int PRIORITY_FUNCALL = 200;
  private static final int PRIORITY_QUAL = 100;
  private static final int PRIORITY_NEXT = 95;
  private static final int PRIORITY_UNARY = 90;
  private static final int PRIORITY_TIMES = 80;
  private static final int PRIORITY_PLUS = 70;
  private static final int PRIORITY_RANGE = 60;
  private static final int PRIORITY_EQUALS = 50;
  private static final int PRIORITY_AND = 40;
  private static final int PRIORITY_OR = 30;
  private static final int PRIORITY_ASSIGNMENT = 20;

  private static final int OPCHAR_MIN = 32;
  private static final int OPCHAR_MAX = 128;

}
