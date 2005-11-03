//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompilerOperatorTable
//###########################################################################
//# $Id: CompilerOperatorTable.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.expr.AbstractOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.IntValue;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.expr.Value;


/**
 * <P>The standard operator table.</P>
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
    super(16, OPCHAR_MIN, OPCHAR_MAX);
    store(new BinaryPlusOperator());
    store(new BinaryMinusOperator());
    store(new BinaryEqualsOperator());
    store(new BinaryRangeOperator());
    store(new UnaryMinusOperator());
  }


  //#########################################################################
  //# Inner Class BinaryIntOperator
  private static abstract class BinaryIntOperator implements BinaryOperator
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
    //# Provided by Subclasses
    abstract int eval(int lhs, int rhs)
      throws EvalException;

  }


  //#########################################################################
  //# Inner Class UnaryIntOperator
  private static abstract class UnaryIntOperator implements UnaryOperator
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


  private static class BinaryPlusOperator extends BinaryIntOperator
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
    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_LEFT;
    }


    //#######################################################################
    //# Overrides for Abstract Base Class BinaryIntOperator
    int eval(final int lhs, final int rhs)
    {
      return lhs + rhs;
    }

  }


  //#########################################################################
  //# Inner Class BinaryMinusOperator
  private static class BinaryMinusOperator extends BinaryIntOperator
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
    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_LEFT;
    }


    //#######################################################################
    //# Overrides for Abstract Base Class BinaryIntOperator
    int eval(final int lhs, final int rhs)
    {
      return lhs - rhs;
    }

  }


  //#########################################################################
  //# Inner Class BinaryEqualsOperator
  private static class BinaryEqualsOperator implements BinaryOperator
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
    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_RIGHT;
    }

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
        return Operator.TYPE_INT;
      } else {
        return 0;
      }
    }

    public CompiledIntValue eval(final Value lhsValue, final Value rhsValue)
    {
      final boolean result = lhsValue.equals(rhsValue);
      return new CompiledIntValue(result);
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
  //# Inner Class UnaryMinusOperator
  private static class UnaryMinusOperator extends UnaryIntOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    public String getName()
    {
      return OPNAME_MINUS;
    }


    //#######################################################################
    //# Overrides for Abstract Base Class UnaryIntOperator
    int eval(final int arg)
    {
      return -arg;
    }

  }


  //#########################################################################
  //# Class Constants
  private static final CompilerOperatorTable INSTANCE =
    new CompilerOperatorTable();

  private static final String OPNAME_EQUALS = "==";
  private static final String OPNAME_MINUS = "-";
  private static final String OPNAME_PLUS = "+";
  private static final String OPNAME_RANGE = "..";

  private static final int PRIORITY_UNARY = 90;
  private static final int PRIORITY_MULT = 80;
  private static final int PRIORITY_PLUS = 70;
  private static final int PRIORITY_RANGE = 60;
  private static final int PRIORITY_EQUALS = 50;
  private static final int PRIORITY_AND = 40;
  private static final int PRIORITY_OR = 30;
  private static final int PRIORITY_OUTER = OperatorTable.PRIORITY_OUTER;

  private static final int OPCHAR_MIN = 32;
  private static final int OPCHAR_MAX = 128;

}
