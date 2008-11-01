//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   SimpleExpressionProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * The base class of all expressions.
 *
 * This is the base class of all expressions that have a simple textual
 * representation, i.e., one that easily fits into a single line. This
 * includes all expressions except event lists ({@link
 * EventListExpressionProxy}).
 *
 * @author Robi Malik
 */

public interface SimpleExpressionProxy
  extends ExpressionProxy
{

  public SimpleExpressionProxy clone();

  /**
   * Gets the original text of this expression.
   * If present, this string contains the original text entered by
   * the user, including all redundant parentheses and whitespace exactly
   * as it was typed.
   * @return The original text, or <CODE>null</CODE> to indicate that no
   *         original text is available.
   */
  // @geometry
  public String getPlainText();

}
