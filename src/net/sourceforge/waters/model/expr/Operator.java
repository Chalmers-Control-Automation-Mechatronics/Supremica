//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.waters.model.expr
//# CLASS:   Operator
//###########################################################################
//# $Id: Operator.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;


/**
 * <P>The common interface for unary and binary operators.</P>
 *
 * <P>Operators are used by parsers and scanners in situations where
 * expressions need to be created from operator names, before the class
 * of the expression is known. The can be looked up in the operator table
 * ({@link OperatorTable}) by their name and contain information such
 * as the binding priority of operators and instructions how to construct
 * the actual expression.</P>
 *
 * @author Robi Malik
 */

interface Operator {

  public String getName();

  public int getPriority();

}
