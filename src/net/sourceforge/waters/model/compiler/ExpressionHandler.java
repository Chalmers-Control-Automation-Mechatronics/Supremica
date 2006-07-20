package net.sourceforge.waters.model.compiler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.BooleanValue;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.IntValue;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.BooleanConstantProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;

public class ExpressionHandler {
	public static enum Type {BOOLEAN, INTEGER}

	protected final Set<String> constants = new HashSet<String>(
				Arrays.asList(new String[] {"true", "false"}));
	protected final Set<String> mReservedWords;
	protected ModuleSubjectFactory mFactory;
	protected SimpleExpressionSubject mExpression;
	protected boolean mExpressionSet;
	protected Map<String, AbstractVariable> mVariables;
	
	public ExpressionHandler() {
		//we separate reserved words from constants in
	    //case we want to add more reserved words later.
		mReservedWords = new HashSet<String>();
		mReservedWords.addAll(constants);
		
		mVariables = new HashMap<String, AbstractVariable>();
		declareConstants();
		
		mFactory = ModuleSubjectFactory.getInstance();
	}

	protected void declareConstants() {
		//true
		final AbstractVariable TRUE = 
			createVariable("true", Type.BOOLEAN);
		mVariables.put("true", TRUE);
		assignValueToVariable("true", true);
		
		//false
		final AbstractVariable FALSE = 
			createVariable("false", Type.BOOLEAN);
		mVariables.put("false", FALSE);
		assignValueToVariable("false", false);
	}

	public boolean evaluateBoolean() throws EvalException {
		if(mExpressionSet) {	
		final Value value = eval(mExpression);
			if(value instanceof BooleanValue) {
				return ((BooleanValue) value).getValue();
			}
			else {
				System.err.println("expression not boolean");
				return false;
			}
		} else {
			System.err.println("expression not initialized");
			return false;
		}
	}
	
	public Value evaluate() throws EvalException {
		if(mExpressionSet) {	
			return eval(mExpression);
		} else {
			System.err.println("expression not initialized");
			return null;
		}
	}

	private Value eval(SimpleExpressionProxy expression) throws EvalException {
		//recursive descent of the syntax tree
		if(expression instanceof UnaryExpressionProxy) {
			return evalUnaryExpression((UnaryExpressionProxy) expression);
		}
		else if(expression instanceof BinaryExpressionProxy) {
			return evalBinaryExpression((BinaryExpressionProxy) expression);
		}
		else if(expression instanceof IntConstantProxy) {
			final IntValue value = new CompiledIntValue((
					(IntConstantProxy) expression).getValue());
			return value;
		}
		else if(expression instanceof BooleanConstantProxy) {
			final BooleanValue value = new CompiledBooleanValue((
					(BooleanConstantProxy) expression).isValue());
			return value;
		}
		else if(expression instanceof SimpleIdentifierProxy) {
			final String variableName = 
				((SimpleIdentifierProxy) expression).getName();
			if(mVariables.containsKey(variableName)) {
				final AbstractVariable variable = 
					mVariables.get(variableName);
				return variable.getValue();
			}
			else {
				System.err.println("GuardExpressionEvaluater: variable "
						+ variableName + " not declared");
				return null;
			}
		}
		else {
			System.err.println("GuardExpressionEvaluater: " +
					"illegal expression type");
			return null;
		}
	}

	private Value evalUnaryExpression(UnaryExpressionProxy expression) throws EvalException {
		SimpleExpressionProxy subExpression = expression.getSubTerm();
		Value value = eval(subExpression);
		UnaryOperator operator = expression.getOperator();
		return operator.eval(value);
	}

	private Value evalBinaryExpression(BinaryExpressionProxy expression) throws EvalException {
		final SimpleExpressionProxy leftHandSide = expression.getLeft();
		final SimpleExpressionProxy rightHandSide = expression.getRight();
		final Value rhsValue = eval(rightHandSide);
		final Value lhsValue = eval(leftHandSide);
		final BinaryOperator operator = expression.getOperator();
		return operator.eval(lhsValue, rhsValue);
	}

	public void setExpression(SimpleExpressionSubject expression) {
		mExpressionSet = true;
		mExpression = expression;
	}

	public void declareVariable(String variableName, Type type) {
		if(!mVariables.containsKey(variableName)) {
			
		mVariables.put(variableName, createVariable(variableName, type));
		}
		else if(mReservedWords.contains(variableName)) {
			System.err.println("GuardExpressionEvaluater: " +
					variableName + " is a reserved word and " +
							"cannot be used as a variable");
					
		}
		else {
			System.err.println("GuardExpressionEvaluater: Variable "
					+ variableName + " already declared");
		}
	}

	AbstractVariable createVariable(String name, Type type) {
		switch(type) {
		case INTEGER:
			return new IntVariable(name, 0);
		case BOOLEAN:
			return new BooleanVariable(name, false);
		default:
			System.err.println("GuardExpressionHandler: unhandled " +
					"variable type");
			return null;
		}
	}

	public void assignValueToVariable(String variableName, Integer value) {
		if(mVariables.containsKey(variableName)) {
			final AbstractVariable variable =
				mVariables.get(variableName);
			final Value setValue = new CompiledIntValue(value);
			variable.setValue(setValue);
		}
		else {
			System.err.println("GuardExpressionEvaluater: Variable "
					+ variableName + " not declared");
		}
		
	}

	public void assignValueToVariable(String variableName, boolean value) {
		if(mVariables.containsKey(variableName)) {
			final AbstractVariable variable =
				mVariables.get(variableName);
			final Value setValue = new CompiledBooleanValue(value);
			variable.setValue(setValue);
		}
		else {
			System.err.println("GuardExpressionHandler: Variable "
					+ variableName + " not declared");
		}
		
	}

	public SimpleExpressionProxy getExpression() {
		return mExpression;
	}
	abstract class AbstractVariable {
		protected String mName;
		
		public String getName() {
			return mName;
		}
		
		public void setName(String name) {
			mName = name;
		}
		public abstract Type getType();
		public abstract Value getValue();
		public abstract void setValue(Value value);
	}

	class BooleanVariable extends AbstractVariable {
		
		private BooleanValue mValue;

		public BooleanVariable(String name, boolean value) {
			mValue = new CompiledBooleanValue(value);
			mName = name;
		}

		public Type getType() {
			return Type.BOOLEAN;
		}

		public Value getValue() {
			return mValue;
		}

		public void setValue(Value value) {
			if(value instanceof BooleanValue) {
				mValue = (BooleanValue) value;
			}
			else {
				System.err.println("illegal assignment to " +
						"boolean variable");
			}
		}
	}

	class IntVariable extends AbstractVariable {

		private IntValue mValue;

		public IntVariable(String name, int value) {
			mValue = new CompiledIntValue(value);
			mName = name;
		}

		public Type getType() {
			return Type.INTEGER;
		}

		public Value getValue() {
			return mValue;
		}

		public void setValue(Value value) {
			if(value instanceof IntValue) {
				mValue = (IntValue) value;
			}
			else {
				System.err.println("illegal assignment to " +
						"integer variable");
			}
		}
	}
}


