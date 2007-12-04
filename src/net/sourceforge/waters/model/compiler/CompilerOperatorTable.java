//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompilerOperatorTable
//###########################################################################
//# $Id: CompilerOperatorTable.java,v 1.9 2007-12-04 03:22:55 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.expr.AbstractOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.IntValue;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.expr.Value;


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

    mAndOperator = new BinaryAndOperator();
    mOrOperator= new BinaryOrOperator();
    mEqualsOperator = new BinaryEqualsOperator();
    mNotEqualsOperator = new BinaryNotEqualsOperator();
    mGreaterThanOperator = new BinaryGreaterThanOperator();
    mGreaterEqualsOperator = new BinaryGreaterEqualsOperator();
    mLessThanOperator = new BinaryLessThanOperator();
    mLessEqualsOperator = new BinaryLessEqualsOperator();
    mNotOperator = new UnaryNotOperator();
    mAssignmentOperator = new BinaryAssignmentOperator();
    mIncrementOperator = new BinaryIncrementOperator();
    mDecrementOperator = new BinaryDecrementOperator();
    mRangeOperator = new BinaryRangeOperator();

    store(mAndOperator);
    store(mOrOperator);
    store(mNotOperator);
    store(mEqualsOperator);
    store(mNotEqualsOperator);
    store(mGreaterThanOperator);
    store(mGreaterEqualsOperator);
    store(mLessThanOperator);
    store(mLessEqualsOperator);
    store(mAssignmentOperator);
    store(mIncrementOperator);
    store(mDecrementOperator);
    store(new BinaryPlusOperator());
    store(new BinaryMinusOperator());
    store(new BinaryTimesOperator());
    store(new BinaryDivideOperator());
    store(new BinaryModuloOperator());
    store(new UnaryMinusOperator());
    store(mRangeOperator);
    storeComplements(mEqualsOperator, mNotEqualsOperator);
    storeComplements(mLessThanOperator, mGreaterEqualsOperator);
    storeComplements(mGreaterThanOperator, mLessEqualsOperator);
  }

  private void storeComplements(final BinaryOperator op1,
                                final BinaryOperator op2)
  {
    store(op1);
    store(op2);
    mComplementMap.put(op1, op2);
    mComplementMap.put(op2, op1);
  }


  //#########################################################################
  //# Acess by Logic Semantics
  public BinaryOperator getAndOperator()
  {
    return mAndOperator;
  }

  public BinaryOperator getOrOperator()
  {
    return mOrOperator;
  }

  public UnaryOperator getNotOperator()
  {
    return mNotOperator;
  }
  
  public BinaryOperator getEqualsOperator() {
    return mEqualsOperator;
  }
  
  public BinaryOperator getNotEqualsOperator() {
    return mNotEqualsOperator;
  }
  
  public BinaryOperator getGreaterThanOperator() {
    return mGreaterThanOperator;
  }
  
  public BinaryOperator getGreaterEqualsOperator() {
    return mGreaterEqualsOperator;
  }
  
  public BinaryOperator getLessThanOperator() {
    return mLessThanOperator;
  }
  
  public BinaryOperator getLessEqualsOperator() {
    return mLessEqualsOperator;
  }

  public BinaryOperator getAssignmentOperator()
  {
    return mAssignmentOperator;
  }

  public BinaryOperator getIncrementOperator()
  {
    return mIncrementOperator;
  }

  public BinaryOperator getDecrementOperator()
  {
    return mDecrementOperator;
  }

  public BinaryOperator getRangeOperator() {
    return mRangeOperator;
  }
  
  public BinaryOperator getComplementaryOperator(final BinaryOperator op)
  {
    return mComplementMap.get(op);
  }
  
  public boolean isNotEqualsOperator(BinaryOperator op) {
		return (op instanceof BinaryNotEqualsOperator);
  }
  
  public boolean isLessThanOperator(BinaryOperator op) {
		return (op instanceof BinaryLessThanOperator);
  }
  
  public boolean isLessEqualsOperator(BinaryOperator op) {
		return (op instanceof BinaryLessEqualsOperator);
  }


  //#########################################################################
  //# Inner Class AbstractBinaryIntOperator
  /**
   * The abstract type of all binary operators that combine two integer
   * values and return another integer.
   */
  private static abstract class AbstractBinaryIntOperator
    implements BinaryOperator
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
    public int getArgTypes()
    {
      return Operator.TYPE_INT;
    }

    public int getReturnTypes(final int argType)
    {
      return argType & Operator.TYPE_INT;
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
    implements BinaryOperator
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

    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      if ((lhsType & rhsType) != 0) {
        return Operator.TYPE_BOOLEAN;
      } else {
        return 0;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_RIGHT;
    }

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
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    public CompiledIntValue eval(final Value lhsValue, final Value rhsValue)
    {
      final boolean result = lhsValue.equals(rhsValue);
      return new CompiledIntValue(result);
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
    //# Interface net.sourceforge.waters.model.expr.BinaryOperator
    public CompiledIntValue eval(final Value lhsValue, final Value rhsValue)
    {
      final boolean result = !lhsValue.equals(rhsValue);
      return new CompiledIntValue(result);
    }

  }


  //#########################################################################
  //# Inner Class AbstractBinaryComparisonOperator
  /**
   * The abstract type of all binary operators that combine two integer
   * values and return a boolean result.
   */
  private static abstract class AbstractBinaryComparisonOperator
    implements BinaryOperator
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
      if ((lhsType & rhsType & Operator.TYPE_INT) != 0) {
        return Operator.TYPE_BOOLEAN;
      } else {
        return 0;
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
    implements BinaryOperator
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

    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      return lhsType & rhsType & Operator.TYPE_BOOLEAN;
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
    public int getArgTypes()
    {
      return Operator.TYPE_BOOLEAN;
    }

    public int getReturnTypes(final int argType)
    {
      return argType & Operator.TYPE_BOOLEAN;
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
  //# Inner Class UnaryMinusOperator
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
  private static class BinaryRangeOperator implements BinaryOperator
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

    public int getReturnTypes(final int lhsType, final int rhsType)
    {
      if ((lhsType & rhsType & Operator.TYPE_INT) != 0) {
        return Operator.TYPE_RANGE;
      } else {
        return 0;
      }
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
    implements BinaryOperator
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
  //# Data Members
  private final BinaryOperator mAndOperator;
  private final BinaryOperator mOrOperator;
  private final UnaryOperator mNotOperator;
  private final BinaryOperator mRangeOperator;
  private final BinaryOperator mEqualsOperator;
  private final BinaryOperator mNotEqualsOperator;
  private final BinaryOperator mGreaterThanOperator;
  private final BinaryOperator mGreaterEqualsOperator;
  private final BinaryOperator mLessThanOperator;
  private final BinaryOperator mLessEqualsOperator;
  private final BinaryOperator mAssignmentOperator;
  private final BinaryOperator mIncrementOperator;
  private final BinaryOperator mDecrementOperator;
  private final Map<BinaryOperator,BinaryOperator> mComplementMap;


  //#########################################################################
  //# Class Constants
  private static final CompilerOperatorTable INSTANCE =
    new CompilerOperatorTable();

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
  private static final String OPNAME_INCREMENT = "+=";
  private static final String OPNAME_DECREMENT = "-=";
  private static final String OPNAME_ASSIGNMENT = "=";

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
