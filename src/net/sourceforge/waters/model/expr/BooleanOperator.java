//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   BinaryOperator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.expr;


/**
 * <P>A boolean operator.</P>
 *
 * @see Operator
 *
 * @author Martin Byr&ouml;d
 */

public interface BooleanOperator extends Operator {
	//Returns the negated version of this operator
	//i.e. "==" becomes "!=", "<" becomes ">=", etc.
	public BooleanOperator getNegative();
}
