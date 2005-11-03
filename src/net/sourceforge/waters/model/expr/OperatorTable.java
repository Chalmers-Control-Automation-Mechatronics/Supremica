//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   OperatorTable
//###########################################################################
//# $Id: OperatorTable.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;


/**
 * <P>An operator table.</P>
 *
 * <P>This is the interface needed for the identification of operators
 * given their name.</P>
 *
 * @author Robi Malik
 */

public interface OperatorTable {

  //#########################################################################
  //# Access Methods
  public UnaryOperator getUnaryOperator(final String name);

  public BinaryOperator getBinaryOperator(final String name);

  public boolean contains(final String name);

  public boolean isOperatorCharacter(final char ch);

  //#########################################################################
  //# Class Constants
  public static final int PRIORITY_OUTER = 0;

}
