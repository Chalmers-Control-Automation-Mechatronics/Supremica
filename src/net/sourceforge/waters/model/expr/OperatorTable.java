//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   OperatorTable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.Comparator;

import net.sourceforge.waters.model.module.SimpleExpressionProxy;


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

  public Comparator<SimpleExpressionProxy> getExpressionComparator();

  //#########################################################################
  //# Class Constants
  public static final int PRIORITY_OUTER = 0;

}
