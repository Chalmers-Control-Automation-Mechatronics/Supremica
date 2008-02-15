//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.external.iec61499fb2efa
//# CLASS:   VariableHelper
//###########################################################################
//# $Id: VariableHelper.java,v 1.5 2008-02-15 07:31:49 robi Exp $
//###########################################################################


package org.supremica.automata;

import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.IntConstantSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.VariableComponentSubject;
import net.sourceforge.waters.subject.module.VariableMarkingSubject;

/**
 * Methods for convenient handling of integer and boolean variables.
 */

public class VariableHelper {

    //#######################################################################
    //# Integer
    public static VariableComponentSubject createIntegerVariable
        (final String name,
         final int lowerBound,
         final int upperBound,
         final int initialValue,
         final Integer markedValue)
    {
        if (initialValue > upperBound || initialValue < lowerBound) {
            throw new IllegalArgumentException
                ("Initial value is not within the specified range");
        }
        if (markedValue != null &&
            (markedValue > upperBound || markedValue < lowerBound)) {
            throw new IllegalArgumentException
                ("Marked value is not within the specified range");
        }
        final ModuleSubjectFactory factory =
            ModuleSubjectFactory.getInstance();
        final CompilerOperatorTable optable =
            CompilerOperatorTable.getInstance();
        final IdentifierSubject ident =
            factory.createSimpleIdentifierProxy(name);
        final IntConstantSubject lower =
            factory.createIntConstantProxy(lowerBound);
        final IntConstantSubject upper =
            factory.createIntConstantProxy(upperBound);
        final BinaryOperator oprange =  optable.getRangeOperator();
        final BinaryExpressionSubject range =
            factory.createBinaryExpressionProxy(oprange, lower, upper);
        final IdentifierSubject iclone1 = ident.clone();
        final IntConstantSubject initval =
            factory.createIntConstantProxy(initialValue);
        final BinaryOperator opeq =  optable.getEqualsOperator();
        final BinaryExpressionSubject init =
            factory.createBinaryExpressionProxy(opeq, iclone1, initval);
        final List<VariableMarkingSubject> markings;
        if (markedValue == null) {
            markings = null;
        } else {
            final IdentifierSubject accepting =
                factory.createSimpleIdentifierProxy
                (EventDeclProxy.DEFAULT_MARKING_NAME);
            final IdentifierSubject iclone2 = ident.clone();
            final IntConstantSubject markedval =
                factory.createIntConstantProxy(markedValue);
            final BinaryExpressionSubject pred =
                factory.createBinaryExpressionProxy(opeq, iclone2, markedval);
            final VariableMarkingSubject marking =
                factory.createVariableMarkingProxy(accepting, pred);
            markings = Collections.singletonList(marking);
        }
        return factory.createVariableComponentProxy
            (ident, range, true, init, markings);
	}

	public static boolean isInteger(final VariableComponentProxy variable)
    {
		return variable.getType() instanceof BinaryExpressionProxy;
	}

	public static void setAsInteger(final VariableComponentSubject variable,
                                    final int lowerBound,
                                    final int upperBound,
                                    final int initialValue,
                                    final Integer markedValue)
    {
		if (initialValue > upperBound || initialValue < lowerBound) {
			throw new IllegalArgumentException
                ("Initial value is not within the specified range");
		}
		if (markedValue != null &&
            (markedValue > upperBound || markedValue < lowerBound)) {
			throw new IllegalArgumentException
                ("Marked value is not within the specified range");
		}
        final ModuleSubjectFactory factory =
            ModuleSubjectFactory.getInstance();
        final CompilerOperatorTable optable =
            CompilerOperatorTable.getInstance();
        final IntConstantSubject lower =
            factory.createIntConstantProxy(lowerBound);
        final IntConstantSubject upper =
            factory.createIntConstantProxy(upperBound);
        final BinaryOperator oprange =  optable.getRangeOperator();
        final BinaryExpressionSubject range =
            factory.createBinaryExpressionProxy(oprange, lower, upper);
        variable.setType(range);
        final IdentifierSubject ident = variable.getIdentifier();
        final IdentifierSubject iclone1 = ident.clone();
        final IntConstantSubject initval =
            factory.createIntConstantProxy(initialValue);
        final BinaryOperator opeq =  optable.getEqualsOperator();
        final BinaryExpressionSubject init =
            factory.createBinaryExpressionProxy(opeq, iclone1, initval);
        variable.setInitialStatePredicate(init);
        final List<VariableMarkingSubject> markings =
            variable.getVariableMarkingsModifiable();
        markings.clear();
        if (markedValue != null) {
            final IdentifierSubject accepting =
                factory.createSimpleIdentifierProxy
                (EventDeclProxy.DEFAULT_MARKING_NAME);
            final IdentifierSubject iclone2 = ident.clone();
            final IntConstantSubject markedval =
                factory.createIntConstantProxy(markedValue);
            final BinaryExpressionSubject pred =
                factory.createBinaryExpressionProxy(opeq, iclone2, markedval);
            final VariableMarkingSubject marking =
                factory.createVariableMarkingProxy(accepting, pred);
            markings.add(marking);
        }
	}

	public static Integer getUpperBound(final VariableComponentProxy variable)
    {
		if (!isInteger(variable)) {
			return null;
		} else {
            return ((IntConstantProxy) ((BinaryExpressionProxy) variable.getType()).getRight()).getValue();
        }
	}

	public static Integer getLowerBound(final VariableComponentProxy variable)
    {
		if (!isInteger(variable)) {
			return null;
		} else {
            return ((IntConstantProxy) ((BinaryExpressionProxy) variable.getType()).getLeft()).getValue();
        }
	}

	public static Integer getInitialIntegerValue
        (final VariableComponentProxy variable)
    {
		if (!isInteger(variable)) {
			return null;
		}
        final SimpleExpressionProxy pred = variable.getInitialStatePredicate();
        if (!(pred instanceof BinaryExpressionProxy)) {
            return null;
        }
        final BinaryExpressionProxy binpred = (BinaryExpressionProxy) pred;
        final SimpleExpressionProxy rhs = binpred.getRight();
        if (!(rhs instanceof IntConstantProxy)) {
            return null;
        }
        final IntConstantProxy intconst = (IntConstantProxy) rhs;
        return intconst.getValue();
	}

	public static Integer getMarkedIntegerValue
        (final VariableComponentProxy variable)
    {
		if (!isInteger(variable)) {
			return null;
		}
        final List<VariableMarkingProxy> markings =
            variable.getVariableMarkings();
        VariableMarkingProxy found = null;
        for (final VariableMarkingProxy marking : markings) {
            final IdentifierProxy ident = marking.getProposition();
            if (ident instanceof SimpleIdentifierProxy) {
                final SimpleIdentifierProxy simple =
                    (SimpleIdentifierProxy) ident;
                if (simple.getName().equals
                      (EventDeclProxy.DEFAULT_MARKING_NAME)) {
                    found = marking;
                }
            }
        }
        if (found == null) {
            return null;
        }
        final SimpleExpressionProxy pred = found.getPredicate();
        if (!(pred instanceof BinaryExpressionProxy)) {
            return null;
        }
        final BinaryExpressionProxy binpred = (BinaryExpressionProxy) pred;
        final SimpleExpressionProxy rhs = binpred.getRight();
        if (!(rhs instanceof IntConstantProxy)) {
            return null;
        }
        final IntConstantProxy intconst = (IntConstantProxy) rhs;
        return intconst.getValue();
	}


    //#######################################################################
    //# Boolean
	public static VariableComponentSubject createBooleanVariable
        (final String name,
         final boolean initialValue,
         final Boolean markedValue)
    {
		final ModuleSubjectFactory factory =
            ModuleSubjectFactory.getInstance();
        final CompilerOperatorTable optable =
            CompilerOperatorTable.getInstance();
        final IdentifierSubject ident =
            factory.createSimpleIdentifierProxy(name);
        final SimpleExpressionSubject range =
            factory.createSimpleIdentifierProxy(NAME_OF_BOOLEAN_TYPE);
        final IdentifierSubject iclone1 = ident.clone();
        final IntConstantSubject initval =
            factory.createIntConstantProxy(initialValue ? 1 : 0);
        final BinaryOperator opeq =  optable.getEqualsOperator();
        final BinaryExpressionSubject init =
            factory.createBinaryExpressionProxy(opeq, iclone1, initval);
        final List<VariableMarkingSubject> markings;
        if (markedValue == null) {
            markings = null;
        } else {
            final IdentifierSubject accepting =
                factory.createSimpleIdentifierProxy
                (EventDeclProxy.DEFAULT_MARKING_NAME);
            final IdentifierSubject iclone2 = ident.clone();
            final IntConstantSubject markedval =
                factory.createIntConstantProxy(markedValue ? 1 : 0);
            final BinaryExpressionSubject pred =
                factory.createBinaryExpressionProxy(opeq, iclone2, markedval);
            final VariableMarkingSubject marking =
                factory.createVariableMarkingProxy(accepting, pred);
            markings = Collections.singletonList(marking);
        }
        return factory.createVariableComponentProxy
            (ident, range, true, init, markings);
	}

    public static void setAsBoolean(final VariableComponentSubject variable,
                                    final boolean initialValue,
                                    final Boolean markedValue)
    {
		final ModuleSubjectFactory factory =
            ModuleSubjectFactory.getInstance();
        final CompilerOperatorTable optable =
            CompilerOperatorTable.getInstance();
        final SimpleExpressionSubject range =
            factory.createSimpleIdentifierProxy(NAME_OF_BOOLEAN_TYPE);
        variable.setType(range);
        final IdentifierSubject ident = variable.getIdentifier();
        final IdentifierSubject iclone1 = ident.clone();
        final IntConstantSubject initval =
            factory.createIntConstantProxy(initialValue ? 1 : 0);
        final BinaryOperator opeq =  optable.getEqualsOperator();
        final BinaryExpressionSubject init =
            factory.createBinaryExpressionProxy(opeq, iclone1, initval);
        variable.setInitialStatePredicate(init);
        final List<VariableMarkingSubject> markings =
            variable.getVariableMarkingsModifiable();
        markings.clear();
        if (markedValue != null) {
            final IdentifierSubject accepting =
                factory.createSimpleIdentifierProxy
                (EventDeclProxy.DEFAULT_MARKING_NAME);
            final IdentifierSubject iclone2 = ident.clone();
            final IntConstantSubject markedval =
                factory.createIntConstantProxy(markedValue ? 1 : 0);
            final BinaryExpressionSubject pred =
                factory.createBinaryExpressionProxy(opeq, iclone2, markedval);
            final VariableMarkingSubject marking =
                factory.createVariableMarkingProxy(accepting, pred);
            markings.add(marking);
        }
    }

	public static boolean isBoolean(final VariableComponentProxy variable)
    {
        final SimpleExpressionProxy type = variable.getType();
        if (type instanceof SimpleIdentifierProxy) {
            final SimpleIdentifierProxy ident = (SimpleIdentifierProxy) type;
            return ident.getName().equals(NAME_OF_BOOLEAN_TYPE);
        } else {
            return false;
        }
	}

    public static Boolean getInitialBooleanValue
        (final VariableComponentProxy variable)
    {
        if (!isBoolean(variable)) {
            return null;
        }
        final SimpleExpressionProxy pred = variable.getInitialStatePredicate();
        if (!(pred instanceof BinaryExpressionProxy)) {
            return null;
        }
        final BinaryExpressionProxy binpred = (BinaryExpressionProxy) pred;
        final SimpleExpressionProxy rhs = binpred.getRight();
        if (!(rhs instanceof IntConstantProxy)) {
            return null;
        }
        final IntConstantProxy intconst = (IntConstantProxy) rhs;
        return intconst.getValue() != 0;
    }

    public static Boolean getMarkedBooleanValue
        (final VariableComponentProxy variable)
    {
        if (!isBoolean(variable)) {
            return null;
        }
        final List<VariableMarkingProxy> markings =
            variable.getVariableMarkings();
        VariableMarkingProxy found = null;
        for (final VariableMarkingProxy marking : markings) {
            final IdentifierProxy ident = marking.getProposition();
            if (ident instanceof SimpleIdentifierProxy) {
                final SimpleIdentifierProxy simple =
                    (SimpleIdentifierProxy) ident;
                if (simple.getName().equals
                      (EventDeclProxy.DEFAULT_MARKING_NAME)) {
                    found = marking;
                }
            }
        }
        if (found == null) {
            return null;
        }
        final SimpleExpressionProxy pred = found.getPredicate();
        if (!(pred instanceof BinaryExpressionProxy)) {
            return null;
        }
        final BinaryExpressionProxy binpred = (BinaryExpressionProxy) pred;
        final SimpleExpressionProxy rhs = binpred.getRight();
        if (!(rhs instanceof IntConstantProxy)) {
            return null;
        }
        final IntConstantProxy intconst = (IntConstantProxy) rhs;
        return intconst.getValue() != 0;
    }
   

    //#######################################################################
    //# Class Constants
    public static final String NAME_OF_BOOLEAN_TYPE = "boolean";

}
