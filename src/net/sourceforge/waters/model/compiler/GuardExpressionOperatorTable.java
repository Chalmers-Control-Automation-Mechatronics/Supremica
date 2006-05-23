//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompilerOperatorTable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.expr.AbstractOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.BooleanOperator;
import net.sourceforge.waters.model.expr.BooleanValue;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.IntValue;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.expr.Value;


/**
 * <P>The guard expression operator table.</P>
 *
 * @author Martin Byröd
 */

public class GuardExpressionOperatorTable extends AbstractOperatorTable {

  //#########################################################################
  //# Static Class Methods
  public static GuardExpressionOperatorTable getInstance()
  {
    return INSTANCE;
  }


  //#########################################################################
  //# Constructors
  private GuardExpressionOperatorTable()
  {
    super(16, OPCHAR_MIN, OPCHAR_MAX);
    store(new BinaryNotEqualOperator());
    store(new UnaryNotOperator());
    store(new BinaryEqualsOperator());
    store(new BinaryLessThanOperator());
    store(new BinaryGreaterThanOperator());
    store(new BinaryLessEqualOperator());
    store(new BinaryGreaterEqualOperator());
    store(new BinaryOrOperator());
    store(new BinaryAndOperator());
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
  //# Inner Class BinaryBooleanOperator
  public static abstract class BinaryBooleanOperator 
  		implements BinaryOperator, BooleanOperator
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
      if (!(lhsValue instanceof BooleanValue)) {
        throw new TypeMismatchException(lhsValue, "BOOLEAN");
      }
      if (!(rhsValue instanceof BooleanValue)) {
        throw new TypeMismatchException(rhsValue, "BOOLEAN");
      }
      final BooleanValue lhsBooleanValue = (BooleanValue) lhsValue;
      final BooleanValue rhsBooleanValue = (BooleanValue) rhsValue;
      final boolean lhs = lhsBooleanValue.getValue();
      final boolean rhs = rhsBooleanValue.getValue();
      final boolean result = eval(lhs, rhs);
      return new CompiledBooleanValue(result);
    }


    //#######################################################################
    //# Provided by Subclasses
    abstract boolean eval(boolean lhs, boolean rhs)
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
    
//  #######################################################################
    //# Provided by Subclasses
    abstract int eval(int arg)
      throws EvalException;
  }

//  #########################################################################
    //# Inner Class UnaryIntOperator
    public static abstract class UnaryBooleanOperator
    		implements UnaryOperator, BooleanOperator
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

      public CompiledBooleanValue eval(final Value argValue)
        throws EvalException
      {
        if (!(argValue instanceof BooleanValue)) {
          throw new TypeMismatchException(argValue, "BOOLEAN");
        }
        final BooleanValue argBooleanValue = (BooleanValue) argValue;
        final boolean arg = argBooleanValue.getValue();
        final boolean result = eval(arg);
        return new CompiledBooleanValue(result);
      }


      
    //#######################################################################
    //# Provided by Subclasses
    abstract boolean eval(boolean arg)
      throws EvalException;

  }


  


//  #########################################################################
    //# Inner Class BinaryEqualsOperator
    public static class BinaryEqualsOperator 
    	extends BinaryBooleanOperator
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
      public int getLHSTypes()
      {
        return Operator.TYPE_ANY;
      }

      public int getRHSTypes()
      {
        return Operator.TYPE_ANY;
      }

  	public int getReturnTypes(int lhsType, int rhsType) {
  		if ((lhsType & rhsType) != 0) {
  			return Operator.TYPE_BOOLEAN;
  		}
  		else {
  			return 0;
  		}
  	}
      
      public int getAssociativity()
      {
        return BinaryOperator.ASSOC_RIGHT;
      }

      public CompiledBooleanValue eval(Value lhs, Value rhs) throws EvalException {
      	CompiledBooleanValue result;
      	result = new CompiledBooleanValue(lhs.equals(rhs));
      	return result;
      }

	public BooleanOperator getNegative() {
		return new BinaryNotEqualOperator();
	}

	@Override
	boolean eval(boolean lhs, boolean rhs) throws EvalException {
		return lhs == rhs;
	}
    }
    
//  #########################################################################
    //# Inner Class BinaryNotEqualOperator
    public static class BinaryNotEqualOperator extends BinaryBooleanOperator
    {

      //#######################################################################
      //# Interface net.sourceforge.waters.model.expr.Operator
      public String getName()
      {
        return OPNAME_NOTEQUAL;
      }

      public int getPriority()
      {
        return PRIORITY_EQUALS;
      }


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

  	public int getReturnTypes(int lhsType, int rhsType) {
  		if ((lhsType & rhsType) != 0) {
  			return Operator.TYPE_BOOLEAN;
  		}
  		else {
  			return 0;
  		}
  	}
      
      public int getAssociativity()
      {
        return BinaryOperator.ASSOC_RIGHT;
      }

      public CompiledBooleanValue eval(Value lhs, Value rhs) throws EvalException {
      	CompiledBooleanValue result;
      	result = new CompiledBooleanValue(! lhs.equals(rhs));
      	return result;
      }

	public BooleanOperator getNegative() {
		return new BinaryEqualsOperator();
	}

	@Override
	boolean eval(boolean lhs, boolean rhs) throws EvalException {
		return lhs != rhs;
	}
    }
    
//  #########################################################################
    //# Inner Class BinaryLessThanOperator
    public static class BinaryLessThanOperator 
    	implements BinaryOperator, BooleanOperator
    {

      //#######################################################################
      //# Interface net.sourceforge.waters.model.expr.Operator
      public String getName()
      {
        return OPNAME_LESSTHAN;
      }

      public int getPriority()
      {
        return PRIORITY_EQUALS;
      }


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

  	public int getReturnTypes(int lhsType, int rhsType) {
  		if ((lhsType & rhsType) != 0) {
  			return Operator.TYPE_BOOLEAN;
  		}
  		else {
  			return 0;
  		}
  	}
      
      public int getAssociativity()
      {
        return BinaryOperator.ASSOC_RIGHT;
      }

      public CompiledBooleanValue eval(Value lhs, Value rhs) throws EvalException {
      	CompiledBooleanValue result;
      	if(lhs instanceof IntValue &
      			rhs instanceof IntValue) {
      		result = new CompiledBooleanValue(((IntValue) lhs).getValue() <
      				((IntValue) rhs).getValue());
      	} else {
      		result = null;
      		System.err.println("GuardExpressionHandler: inequalities" +
      				" only applicable for integer types");
      	}
      	return result;
      }

	public BooleanOperator getNegative() {
		return new BinaryGreaterEqualOperator();
	}
    }
    
//  #########################################################################
    //# Inner Class BinaryGreaterThanOperator
    public static class BinaryGreaterThanOperator
    	implements BinaryOperator, BooleanOperator
    {

      //#######################################################################
      //# Interface net.sourceforge.waters.model.expr.Operator
      public String getName()
      {
        return OPNAME_GREATERTHAN;
      }

      public int getPriority()
      {
        return PRIORITY_EQUALS;
      }


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

  	public int getReturnTypes(int lhsType, int rhsType) {
  		if ((lhsType & rhsType) != 0) {
  			return Operator.TYPE_BOOLEAN;
  		}
  		else {
  			return 0;
  		}
  	}
      
      public int getAssociativity()
      {
        return BinaryOperator.ASSOC_RIGHT;
      }

      public CompiledBooleanValue eval(Value lhs, Value rhs) throws EvalException {
      	CompiledBooleanValue result;
      	if(lhs instanceof IntValue &
      			rhs instanceof IntValue) {
      		result = new CompiledBooleanValue(((IntValue) lhs).getValue() >
      				((IntValue) rhs).getValue());
      	} else {
      		result = null;
      		System.err.println("GuardExpressionHandler: inequalities" +
      				" only applicable for integer types");
      	}
      	return result;
      }

	public BooleanOperator getNegative() {
		return new BinaryLessEqualOperator();
	}
    }
    
//  #########################################################################
    //# Inner Class BinaryLessEqualOperator
    public static class BinaryLessEqualOperator implements BinaryOperator, BooleanOperator
    {

      //#######################################################################
      //# Interface net.sourceforge.waters.model.expr.Operator
      public String getName()
      {
        return OPNAME_LESSEQUAL;
      }

      public int getPriority()
      {
        return PRIORITY_EQUALS;
      }


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

  	public int getReturnTypes(int lhsType, int rhsType) {
  		if ((lhsType & rhsType) != 0) {
  			return Operator.TYPE_BOOLEAN;
  		}
  		else {
  			return 0;
  		}
  	}
      
      public int getAssociativity()
      {
        return BinaryOperator.ASSOC_RIGHT;
      }

      public CompiledBooleanValue eval(Value lhs, Value rhs) throws EvalException {
      	CompiledBooleanValue result;
      	if(lhs instanceof IntValue &
      			rhs instanceof IntValue) {
      		result = new CompiledBooleanValue(((IntValue) lhs).getValue() <=
      				((IntValue) rhs).getValue());
      	} else {
      		result = null;
      		System.err.println("GuardExpressionHandler: inequalities" +
      				" only applicable for integer types");
      	}
      	return result;
      }

	public BooleanOperator getNegative() {
		return new BinaryGreaterThanOperator();
	}
    }
    
//  #########################################################################
    //# Inner Class BinaryGreaterEqualOperator
    public static class BinaryGreaterEqualOperator 
    	implements BinaryOperator, BooleanOperator
    {

      //#######################################################################
      //# Interface net.sourceforge.waters.model.expr.Operator
      public String getName()
      {
        return OPNAME_GREATEREQUAL;
      }

      public int getPriority()
      {
        return PRIORITY_EQUALS;
      }


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

  	public int getReturnTypes(int lhsType, int rhsType) {
  		if ((lhsType & rhsType) != 0) {
  			return Operator.TYPE_BOOLEAN;
  		}
  		else {
  			return 0;
  		}
  	}
      
      public int getAssociativity()
      {
        return BinaryOperator.ASSOC_RIGHT;
      }

      public CompiledBooleanValue eval(Value lhs, Value rhs) throws EvalException {
      	CompiledBooleanValue result;
      	if(lhs instanceof IntValue &
      			rhs instanceof IntValue) {
      		result = new CompiledBooleanValue(((IntValue) lhs).getValue() >=
      				((IntValue) rhs).getValue());
      	} else {
      		result = null;
      		System.err.println("GuardExpressionHandler: inequalities" +
      				" only applicable for integer types");
      	}
      	return result;
      }

	public BooleanOperator getNegative() {
		return new BinaryLessThanOperator();
	}
    }
    
  //#########################################################################
  //# Inner Class BinaryOrOperator
  public static class BinaryOrOperator extends BinaryBooleanOperator
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
    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_RIGHT;
    }


	boolean eval(boolean lhs, boolean rhs) throws EvalException {
		return lhs | rhs;
	}

	public BooleanOperator getNegative() {
		return new BinaryAndOperator();
	}
  }
  
  //#########################################################################
  //# Inner Class BinaryEqualsOperator
  public static class BinaryAndOperator extends BinaryBooleanOperator
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
    public int getAssociativity()
    {
      return BinaryOperator.ASSOC_RIGHT;
    }
    
	boolean eval(boolean lhs, boolean rhs) throws EvalException {
		return lhs & rhs;
	}

	public BooleanOperator getNegative() {
		return new BinaryOrOperator();
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
  //# Inner Class UnaryMinusOperator
  public static class UnaryNotOperator extends UnaryBooleanOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    public String getName()
    {
      return OPNAME_NOT;
    }


    //#######################################################################
    //# Overrides for Abstract Base Class UnaryBooleanOperator
    boolean eval(final boolean arg)
    {
    	return !arg;
    }


	public int getReturnTypes(int argType) {
		return argType & Operator.TYPE_BOOLEAN;
	}


	public BooleanOperator getNegative() {
		return new IdentityOperator();
	}
  }
  
//#########################################################################
  //# Inner Class IdentityOperator
  public static class IdentityOperator implements UnaryOperator, BooleanOperator
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.expr.Operator
    public String getName()
    {
      return OPNAME_IDENTITY;
    }


    boolean eval(final boolean arg)
    {
    	return arg;
    }
    
    int eval(final int arg) {
    	return arg;
    }


	public int getReturnTypes(int argType) {
		return argType & (Operator.TYPE_ANY);
	}


	public BooleanOperator getNegative() {
		return new UnaryNotOperator();
	}


	public int getArgTypes() {
		return Operator.TYPE_ANY;
	}


	public Value eval(Value arg) throws EvalException {
		return arg;
	}


	public int getPriority() {
		return PRIORITY_UNARY;
	}
  }


  //#########################################################################
  //# Class Constants
  private static final GuardExpressionOperatorTable INSTANCE =
    new GuardExpressionOperatorTable();

  private static final String OPNAME_EQUALS = "==";
  private static final String OPNAME_NOTEQUAL = "!=";
  private static final String OPNAME_MINUS = "-";
  private static final String OPNAME_PLUS = "+";
  private static final String OPNAME_OR = "|";
  private static final String OPNAME_AND = "&";
  private static final String OPNAME_NOT = "!";
  private static final String OPNAME_IDENTITY = "";
  private static final String OPNAME_LESSTHAN = "<";
  private static final String OPNAME_GREATERTHAN = ">";
  private static final String OPNAME_LESSEQUAL = "<=";
  private static final String OPNAME_GREATEREQUAL = ">=";
  private static final String OPNAME_ASSIGN = "=";
  private static final String OPNAME_INCREASE = "+=";
  private static final String OPNAME_DECREASE = "-=";

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
