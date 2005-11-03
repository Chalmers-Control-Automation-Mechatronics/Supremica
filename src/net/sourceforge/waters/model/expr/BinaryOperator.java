//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   BinaryOperator
//###########################################################################
//# $Id: BinaryOperator.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;


/**
 * <P>A binary operator.</P>
 *
 * @see Operator
 *
 * @author Robi Malik
 */

public interface BinaryOperator extends Operator {
  
  //#########################################################################
  //# Simple Access Methods
  public int getAssociativity();

  public int getLHSTypes();

  public int getRHSTypes();


  //#########################################################################
  //# Evaluation
  public int getReturnTypes(int lhsType, int rhsType);

  public Value eval(Value lhs, Value rhs) throws EvalException;


  //#########################################################################
  //# Class Constants
  public static final int ASSOC_NONE = 0;
  public static final int ASSOC_LEFT = 1;
  public static final int ASSOC_RIGHT = 2;

}
