package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.BooleanConstantProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.VariableProxy;

public class VariableHelper {
	// Methods for convenient handling of integer variables
	public static VariableSubject createIntegerVariable(final String name, final int lowerBound,
			final int upperBound, final int initialValue,
			final Integer markedValue) {
		VariableSubject variable = ModuleSubjectFactory.getInstance().createVariableProxy(name, null, null);
		setAsInteger(variable, lowerBound, upperBound, initialValue, markedValue);
		return variable;
	}

	public static boolean isInteger(VariableProxy variable) {
		return variable.getType() instanceof BinaryExpressionProxy;
	}

	public static void setAsInteger(VariableSubject variable, final int lowerBound, final int upperBound,
			final int initialValue, final Integer markedValue) {
		if (initialValue > upperBound || initialValue < lowerBound) {
			throw new IllegalArgumentException(
					"Initial value is not within the specified range");
		}
		if (markedValue != null
				&& (markedValue > getUpperBound(variable) || markedValue < getLowerBound(variable))) {
			throw new IllegalArgumentException(
					"Marked value is not within the specified range");
		}

		ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
		variable.setType(factory.createBinaryExpressionProxy(CompilerOperatorTable
				.getInstance().getBinaryOperator(".."), factory
				.createIntConstantProxy(lowerBound), factory
				.createIntConstantProxy(upperBound)));

		variable.setInitialValue(factory.createIntConstantProxy(initialValue));

		variable.setMarkedValue(markedValue == null ? null : factory
				.createIntConstantProxy(markedValue));
	}

	public static Integer getUpperBound(VariableProxy variable) {
		if (!isInteger(variable)) {
			return null;
		}
		return ((IntConstantProxy) ((BinaryExpressionProxy) variable.getType())
				.getRight()).getValue();
	}

	public static Integer getLowerBound(VariableProxy variable) {
		if (!isInteger(variable)) {
			return null;
		}
		return ((IntConstantProxy) ((BinaryExpressionProxy) variable.getType())
				.getLeft()).getValue();
	}

	public static Integer getInitialIntegerValue(VariableSubject variable) {
		if (!isInteger(variable)) {
			return null;
		}
		return ((IntConstantProxy) variable.getInitialValue()).getValue();
	}

	public static Integer getMarkedIntegerValue(VariableSubject variable) {
		if (!isInteger(variable)) {
			return null;
		}
		if (variable.getMarkedValue() == null) {
			return null;
		}
		return ((IntConstantProxy) variable.getMarkedValue()).getValue();
	}

	// Methods for convenient handling of boolean variables
	public static VariableSubject createBooleanVariable(final String name, final boolean initialValue,
			final Boolean markedValue) {
		VariableSubject variable = ModuleSubjectFactory.getInstance().createVariableProxy(name, null, null);
		setAsBoolean(variable, initialValue, markedValue);
		return variable;
	}

	public static void setAsBoolean(VariableSubject variable, final boolean initialValue,
			final Boolean markedValue) {

		ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
		variable.setType(factory.createSimpleIdentifierProxy(NAME_OF_BOOLEAN_TYPE));

		variable.setInitialValue(factory.createBooleanConstantProxy(initialValue));

		variable.setMarkedValue(markedValue == null ? null : factory
				.createBooleanConstantProxy(markedValue));
	}

	public static boolean isBoolean(VariableProxy variable) {
		return variable.getType() instanceof SimpleIdentifierProxy;
	}

	public static Boolean getInitialBooleanValue(VariableProxy variable) {
		if (!isBoolean(variable)) {
			return null;
		}
		return ((BooleanConstantProxy) variable.getInitialValue()).isValue();
	}

	public static Boolean getMarkedBooleanValue(VariableProxy variable) {
		if (!isBoolean(variable)) {
			return null;
		}
		if (variable.getMarkedValue() == null) {
			return null;
		}
		return ((BooleanConstantProxy) variable.getMarkedValue()).isValue();
	}
	
	public final static String NAME_OF_BOOLEAN_TYPE = "boolean";
}
