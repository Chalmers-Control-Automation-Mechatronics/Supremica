//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   BinaryOperator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


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

  public boolean isSymmetric();


  //#########################################################################
  //# Parsing Support
  public SimpleExpressionProxy createExpression
    (final ModuleProxyFactory factory,
     final SimpleExpressionProxy lhs,
     final SimpleExpressionProxy rhs,
     final String text);


  //#########################################################################
  //# Evaluation
  public int getReturnTypes(int lhsType, int rhsType);

  public SimpleExpressionProxy simplify
    (BinaryExpressionProxy expr,
     AbstractSimpleExpressionSimplifier simplifier)
    throws EvalException;


  //#########################################################################
  //# Class Constants
  public static final int ASSOC_NONE = 0;
  public static final int ASSOC_LEFT = 1;
  public static final int ASSOC_RIGHT = 2;

}
