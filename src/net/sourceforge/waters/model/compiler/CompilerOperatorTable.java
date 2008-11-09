//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompilerOperatorTable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.expr.AbstractOperatorTable;
import net.sourceforge.waters.model.expr.AbstractSimpleExpressionSimplifier;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.IntValue;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
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
    mUnaryMinusOperator = new UnaryMinusOperator();
    mPlusOperator = new BinaryPlusOperator();
    mMinusOperator = new BinaryMinusOperator();
    mRangeOperator = new BinaryRangeOperator();

    // store(mUnaryNextOperator, 0);
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
    store(mUnaryMinusOperator, 2);
    store(mPlusOperator, 13);
    store(mMinusOperator, 14);
    store(new BinaryTimesOperator(), 15);
    store(new BinaryDivideOperator(), 16);
    store(new BinaryModuloOperator(), 17);
    store(mRangeOperator, 18);

    storeComplements(mEqualsOperator, mNotEqualsOperator);
    storeComplements(mLessThanOperator, mGreaterEqualsOperator);
    storeComplements(mGreaterThanOperator, mLessEqualsOperator);
    storeSwap(mGreaterThanOperator, mLessThanOperator);
    storeSwap(mGreaterEqualsOperator, mLessEqualsOperator);
    storeAssignment(mIncrementOperator, mPlusOperator);
    storeAssignment(mDecrementOperator, mMinusOperator);
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
  //# Acess by Logic Semantics
  public BinaryOperator getAndOperator()
  {
    return mAndOperator;
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

  public BinaryOperator getPlusOperator()
  {
    return mPlusOperator;
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

  public BinaryOperator getAssigningOperator(final BinaryOperator op)
  {
    return mAssigningMap.get(op);
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
    public BinaryExpressionProxy createExpression
      (final ModuleProxyFactory factory,
       final SimpleExpressionProxy lhs,
       final SimpleExpressionProxy rhs,
       final String text)
    {
      return factory.createBinaryExpressionProxy(text, this, lhs, rhs);
    }

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
    public int getPriority()
    {
      return PRIORITY_UNARY;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.UnaryOperator
    public boolean isPrefix()
    {
      return true;
    }

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
    public int getLHSTypes()
    {
      return Operator.TYPE_INT;
    }

    public int getRHSTypes()
    {
      return Operator.TYPE_INT;
    }

    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      return lhsType & rhsType & Operator.TYPE_INT;
    }

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

    public Value eval(final Value lhsValue, final Value rhsValue)
      throws EvalException
    {
      if (!(lhsValue instanceof IntValue)) {
        throw new TypeMismatchException(lhsValue, "INTEGER");
      }
      if (!(rhsValue instanceof IntValue)) {
        throw new TypeMismatchException(rhsValue, "INTEGER");
      }
      final IntValue lhsIntValue = (IntValue) lhsValue;
      final IntValue rhsIntValue = (IntValue) rhsValue;
      final int lhs = lhsIntValue.getValue();
      final int rhs = rhsIntValue.getValue();
      final int result = eval(lhs, rhs);
      return new CompiledIntValue(result);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
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
    public String getName()
    {
      return OPNAME_PLUS;
    }

    public int getPriority()
    {
      return PRIORITY_PLUS;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    public boolean isSymmetric()
    {
      return true;
    }

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
    public String getName()
    {
      return OPNAME_MINUS;
    }

    public int getPriority()
    {
      return PRIORITY_PLUS;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    public boolean isSymmetric()
    {
      return false;
    }

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
      } else if (simpLHS.equalsByContents(simpRHS)) {
        return simplifier.createIntConstantProxy(0);
      } else {
        return createExpression(simplifier, simpLHS, simpRHS);
      }
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryIntOperator
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
    public String getName()
    {
      return OPNAME_TIMES;
    }

    public int getPriority()
    {
      return PRIORITY_TIMES;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    public boolean isSymmetric()
    {
      return true;
    }

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
    public String getName()
    {
      return OPNAME_DIVIDE;
    }

    public int getPriority()
    {
      return PRIORITY_TIMES;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    public boolean isSymmetric()
    {
      return false;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryIntOperator
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
    public String getName()
    {
      return OPNAME_MODULO;
    }

    public int getPriority()
    {
      return PRIORITY_TIMES;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    public boolean isSymmetric()
    {
      return false;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryIntOperator
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
    public int getArgTypes()
    {
      return Operator.TYPE_INT;
    }

    public int getReturnTypes(final int argType)
    {
      return argType & Operator.TYPE_INT;
    }

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

    public CompiledIntValue eval(final Value argValue)
      throws EvalException
    {
      if (!(argValue instanceof IntValue)) {
        throw new TypeMismatchException(argValue, "INTEGER");
      }
      final IntValue argIntValue = (IntValue) argValue;
      final int arg = argIntValue.getValue();
      final int result = eval(arg);
      return new CompiledIntValue(result);
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
    public String getName()
    {
      return OPNAME_MINUS;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractUnaryIntOperator
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
    public int getLHSTypes()
    {
      return Operator.TYPE_ANY;
    }

    public int getRHSTypes()
    {
      return Operator.TYPE_ANY;
    }

    public boolean isSymmetric()
    {
      return true;
    }

    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      if ((lhsType & rhsType) != 0) {
        return Operator.TYPE_BOOLEAN;
      } else {
        return 0;
      }
    }

    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_RIGHT;
    }

    public SimpleExpressionProxy simplify
      (final BinaryExpressionProxy expr,
       final AbstractSimpleExpressionSimplifier simplifier)
      throws EvalException
    {
      final boolean eresult = getEqualsResult();
      final SimpleExpressionProxy origLHS = expr.getLeft();
      final SimpleExpressionProxy origRHS = expr.getRight();
      if (origLHS.equalsByContents(origRHS)) {
        return simplifier.createBooleanConstantProxy(eresult);
      }
      final SimpleExpressionProxy simpLHS = simplifier.simplify(origLHS);
      final SimpleExpressionProxy simpRHS = simplifier.simplify(origRHS);
      if (simpLHS.equalsByContents(simpRHS)) {
        return simplifier.createBooleanConstantProxy(eresult);
      } else if (simplifier.isAtomicValue(simpLHS) &&
                 simplifier.isAtomicValue(simpRHS)) {
        return simplifier.createBooleanConstantProxy(!eresult);
      } else {
        return createExpression(simplifier, simpLHS, simpRHS);
      }
    }

    public CompiledIntValue eval(final Value lhsValue, final Value rhsValue)
    {
      final boolean result = lhsValue.equals(rhsValue);
      final boolean eresult = getEqualsResult();
      return new CompiledIntValue(result ? eresult : !eresult);
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
    public String getName()
    {
      return OPNAME_EQUALS;
    }

    public int getPriority()
    {
      return PRIORITY_EQUALS;
    }

    //#######################################################################
    //# Overrides for Abstract Base Class AbstractBinaryEqualsOperator
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
    public String getName()
    {
      return OPNAME_NOTEQUALS;
    }

    public int getPriority()
    {
      return PRIORITY_EQUALS;
    }

    //#######################################################################
    //# Overrides for Abstract Base Class AbstractBinaryEqualsOperator
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
    public int getLHSTypes()
    {
      return Operator.TYPE_INT;
    }

    public int getRHSTypes()
    {
      return Operator.TYPE_INT;
    }

    public boolean isSymmetric()
    {
      return false;
    }

    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      if ((lhsType & rhsType & Operator.TYPE_INT) != 0) {
        return Operator.TYPE_BOOLEAN;
      } else {
        return 0;
      }
    }

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
      } else if (simpLHS.equalsByContents(simpRHS)) {
        final boolean result = includesEquality();
        return simplifier.createBooleanConstantProxy(result);
      } else {
        return createExpression(simplifier, simpLHS, simpRHS);
      }
    }

    public Value eval(final Value lhsValue, final Value rhsValue)
      throws EvalException
    {
      if (!(lhsValue instanceof IntValue)) {
        throw new TypeMismatchException(lhsValue, "INTEGER");
      }
      if (!(rhsValue instanceof IntValue)) {
        throw new TypeMismatchException(rhsValue, "INTEGER");
      }
      final IntValue lhsIntValue = (IntValue) lhsValue;
      final IntValue rhsIntValue = (IntValue) rhsValue;
      final int lhs = lhsIntValue.getValue();
      final int rhs = rhsIntValue.getValue();
      final boolean result = eval(lhs, rhs);
      return new CompiledIntValue(result);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    public int getPriority()
    {
      return PRIORITY_EQUALS;
    }

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
    public String getName()
    {
      return OPNAME_GREATER_THAN;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryComparisonOperator
    boolean includesEquality()
    {
      return false;
    }

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
    public String getName()
    {
      return OPNAME_GREATER_EQUALS;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryComparisonOperator
    boolean includesEquality()
    {
      return true;
    }

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
    public String getName()
    {
      return OPNAME_LESS_THAN;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryComparisonOperator
    boolean includesEquality()
    {
      return false;
    }

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
    public String getName()
    {
      return OPNAME_LESS_EQUALS;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractBinaryComparisonOperator
    boolean includesEquality()
    {
      return true;
    }

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
    public int getLHSTypes()
    {
      return Operator.TYPE_BOOLEAN;
    }

    public int getRHSTypes()
    {
      return Operator.TYPE_BOOLEAN;
    }

    public boolean isSymmetric()
    {
      return true;
    }

    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      return lhsType & rhsType & Operator.TYPE_BOOLEAN;
    }

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

    public Value eval(final Value lhsValue, final Value rhsValue)
      throws EvalException
    {
      if (!(lhsValue instanceof IntValue)) {
        throw new TypeMismatchException(lhsValue, "BOOLEAN");
      }
      final IntValue lhsIntValue = (IntValue) lhsValue;
      final int lhs = lhsIntValue.getValue();
      if (lhs < 0 || lhs > 1) {
        throw new TypeMismatchException(lhsValue, "BOOLEAN");
      }
      if (!(rhsValue instanceof IntValue)) {
        throw new TypeMismatchException(rhsValue, "BOOLEAN");
      }
      final IntValue rhsIntValue = (IntValue) rhsValue;
      final int rhs = rhsIntValue.getValue();
      if (rhs < 0 || rhs > 1) {
        throw new TypeMismatchException(rhsValue, "BOOLEAN");
      }
      final boolean result = eval(lhs != 0, rhs != 0);
      return new CompiledIntValue(result);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
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
    public String getName()
    {
      return OPNAME_AND;
    }

    public int getPriority()
    {
      return PRIORITY_AND;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
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
    public String getName()
    {
      return OPNAME_OR;
    }

    public int getPriority()
    {
      return PRIORITY_OR;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
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
    public int getArgTypes()
    {
      return Operator.TYPE_BOOLEAN;
    }

    public int getReturnTypes(final int argType)
    {
      return argType & Operator.TYPE_BOOLEAN;
    }

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

    public CompiledIntValue eval(final Value argValue)
      throws EvalException
    {
      if (!(argValue instanceof IntValue)) {
        throw new TypeMismatchException(argValue, "BOOLEAN");
      }
      final IntValue argIntValue = (IntValue) argValue;
      final int arg = argIntValue.getValue();
      if (arg < 0 || arg > 1) {
        throw new TypeMismatchException(argValue, "BOOLEAN");
      }
      final boolean result = eval(arg != 0);
      return new CompiledIntValue(result);
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
    public String getName()
    {
      return OPNAME_NOT;
    }

    //#######################################################################
    //# Overrides for Abstract Baseclass AbstractUnaryBooleanOperator
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
    public String getName()
    {
      return OPNAME_RANGE;
    }

    public int getPriority()
    {
      return PRIORITY_RANGE;
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_NONE;
    }

    public int getLHSTypes()
    {
      return Operator.TYPE_INT;
    }

    public int getRHSTypes()
    {
      return Operator.TYPE_INT;
    }

    public boolean isSymmetric()
    {
      return false;
    }

    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      if ((lhsType & rhsType & Operator.TYPE_INT) != 0) {
        return Operator.TYPE_RANGE;
      } else {
        return 0;
      }
    }

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

    public CompiledIntRangeValue eval(final Value lhsValue,
                                      final Value rhsValue)
      throws TypeMismatchException
    {
      if (!(lhsValue instanceof IntValue)) {
        throw new TypeMismatchException(lhsValue, "INTEGER");
      }
      if (!(rhsValue instanceof IntValue)) {
        throw new TypeMismatchException(rhsValue, "INTEGER");
      }
      final IntValue lhsIntValue = (IntValue) lhsValue;
      final IntValue rhsIntValue = (IntValue) rhsValue;
      final int lhs = lhsIntValue.getValue();
      final int rhs = rhsIntValue.getValue();
      return new CompiledIntRangeValue(lhs, rhs);
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
    public int getPriority()
    {
      return PRIORITY_ASSIGNMENT;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    public int getLHSTypes()
    {
      return Operator.TYPE_NAME;
    }

    public int getRHSTypes()
    {
      return Operator.TYPE_ANY;
    }

    public boolean isSymmetric()
    {
      return false;
    }

    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      if ((lhsType & Operator.TYPE_NAME) != 0) {
        return rhsType;
      } else {
        return 0;
      }
    }

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
    public String getName()
    {
      return OPNAME_ASSIGNMENT;
    }

    public Value eval(final Value lhsValue, final Value rhsValue)
    {
      return rhsValue;
    }

  }


  //#########################################################################
  //# Inner Class BinaryIncrementOperator
  private static class BinaryIncrementOperator
    extends AbstractBinaryAssignmentOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    public int getRHSTypes()
    {
      return Operator.TYPE_INT;
    }

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
    public String getName()
    {
      return OPNAME_INCREMENT;
    }

    public Value eval(final Value lhsValue, final Value rhsValue)
      throws EvalException
    {
      if (!(lhsValue instanceof IntValue)) {
        throw new TypeMismatchException(lhsValue, "INTEGER");
      }
      if (!(rhsValue instanceof IntValue)) {
        throw new TypeMismatchException(rhsValue, "INTEGER");
      }
      final IntValue lhsIntValue = (IntValue) lhsValue;
      final IntValue rhsIntValue = (IntValue) rhsValue;
      final int lhs = lhsIntValue.getValue();
      final int rhs = rhsIntValue.getValue();
      final int result = lhs + rhs;
      return new CompiledIntValue(result);
    }

  }


  //#########################################################################
  //# Inner Class BinaryDecrementOperator
  private static class BinaryDecrementOperator
    extends AbstractBinaryAssignmentOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    public int getRHSTypes()
    {
      return Operator.TYPE_INT;
    }

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
    public String getName()
    {
      return OPNAME_DECREMENT;
    }

    public Value eval(final Value lhsValue, final Value rhsValue)
      throws EvalException
    {
      if (!(lhsValue instanceof IntValue)) {
        throw new TypeMismatchException(lhsValue, "INTEGER");
      }
      if (!(rhsValue instanceof IntValue)) {
        throw new TypeMismatchException(rhsValue, "INTEGER");
      }
      final IntValue lhsIntValue = (IntValue) lhsValue;
      final IntValue rhsIntValue = (IntValue) rhsValue;
      final int lhs = lhsIntValue.getValue();
      final int rhs = rhsIntValue.getValue();
      final int result = lhs - rhs;
      return new CompiledIntValue(result);
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
    public String getName()
    {
      return OPNAME_QUAL;
    }

    public int getPriority()
    {
      return PRIORITY_QUAL;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    public int getLHSTypes()
    {
      return Operator.TYPE_NAME;
    }

    public int getRHSTypes()
    {
      return Operator.TYPE_NAME;
    }

    public boolean isSymmetric()
    {
      return false;
    }

    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      return lhsType & rhsType & Operator.TYPE_NAME;
    }

    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_RIGHT;
    }

    public QualifiedIdentifierProxy createExpression
      (final ModuleProxyFactory factory,
       final SimpleExpressionProxy lhs,
       final SimpleExpressionProxy rhs,
       final String text)
    {
      return factory.createQualifiedIdentifierProxy
        (text, (IdentifierProxy) lhs, (IdentifierProxy) rhs);
    }

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

    public Value eval(final Value lhsValue, final Value rhsValue)
      throws EvalException
    {
      throw new IllegalStateException
        ("BinaryQualificationOperator cannot be evaluated!");
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
    public String getName()
    {
      return OPNAME_NEXT;
    }

    public int getPriority()
    {
      return PRIORITY_NEXT;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.UnaryOperator
    public boolean isPrefix()
    {
      return false;
    }

    public int getArgTypes()
    {
      return Operator.TYPE_NAME;
    }

    public int getReturnTypes(final int argType)
    {
      return argType & Operator.TYPE_NAME;
    }

    public Value eval(final Value argValue)
      throws EvalException
    {
      throw new EvalException("Next cannot be avaluated!");
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
  private final BinaryOperator mPlusOperator;
  private final BinaryOperator mMinusOperator;
  private final UnaryOperator mUnaryMinusOperator;
  private final BinaryOperator mRangeOperator;
  private final UnaryOperator mUnaryNextOperator;
  private final Map<BinaryOperator,BinaryOperator> mComplementMap;
  private final Map<BinaryOperator,BinaryOperator> mSwapMap;
  private final Map<BinaryOperator,BinaryOperator> mAssigningMap;


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
  private static final String OPNAME_ASSIGNMENT = "=";

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
