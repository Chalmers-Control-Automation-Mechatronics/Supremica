//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   OperatorTable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.expr;


/**
 * <P>An operator table.</P>
 *
 * <P>This is the interface needed for the identification of operators
 * and built-in functions given their name.</P>
 *
 * @author Robi Malik
 */

public interface OperatorTable {

  //#########################################################################
  //# Access Methods
  public UnaryOperator getUnaryOperator(String name);

  public BinaryOperator getBinaryOperator(String name);

  public BuiltInFunction getBuiltInFunction(String name);

  public boolean containsOperator(String name);

  public boolean isOperatorCharacter(char ch);

  public int getOperatorValue(Operator op);

  public char getFunctionKeyCharacter();


  //#########################################################################
  //# Class Constants
  public static final int PRIORITY_OUTER = 0;

}
