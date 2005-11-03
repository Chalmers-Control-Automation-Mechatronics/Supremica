//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   UnaryOperator
//###########################################################################
//# $Id: UnaryOperator.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;


/**
 * <P>A unary operator.</P>
 *
 * @see Operator
 *
 * @author Robi Malik
 */

public interface UnaryOperator extends Operator {

  //#########################################################################
  //# Simple Access Methods
  public int getArgTypes();


  //#########################################################################
  //# Evaluation
  public int getReturnTypes(int argType);

  public Value eval(Value arg) throws EvalException;

}
