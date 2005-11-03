//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   BinaryExpressionProxy
//###########################################################################
//# $Id: BinaryExpressionProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.model.expr.BinaryOperator;


/**
 * The base class for all binary expressions.
 * A binary expression has two arguments, <I>left</I> and <I>right</I>,
 * and an <I>operator</I> applied to them. Typical examples are:
 * <UL>
 * <LI><CODE>1 + 1</CODE></LI>
 * <LI><CODE>17 == 0</CODE></LI>
 * <LI><CODE>1 .. 100</CODE></LI>
 * </UL>
 * This base class provides access to the three members.
 * It is extended in several ways to cater for different XML
 * representations and to facilitate different ways of evaluation.
 *
 * @author Robi Malik
 */

public interface BinaryExpressionProxy
  extends SimpleExpressionProxy
{

  //#########################################################################
  //# Getters
  /**
   * Gets the operator of this expression.
   */
  public BinaryOperator getOperator();

  /**
   * Gets the left subterm of this expression.
   */
  public SimpleExpressionProxy getLeft();

  /**
   * Gets the right subterm of this expression.
   */
  public SimpleExpressionProxy getRight();

}
