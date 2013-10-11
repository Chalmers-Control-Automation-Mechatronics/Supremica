//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   BinaryExpressionProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;


/**
 * An expression representing a function call.
 * Function calls are written with the function name, which is recognised by
 * the parser by an initial backslash character, followed by the argument
 * list in brackets, separated by commas. Typical examples are:
 * <UL>
 * <LI><CODE>\min(12, 5, 7)</CODE></LI>
 * <LI><CODE>\ite(x==y, x+1, x)</CODE></LI>
 * </UL>
 *
 * @author Robi Malik
 */

public interface FunctionCallExpressionProxy
  extends SimpleExpressionProxy
{

  //#########################################################################
  //# Getters
  /**
   * Gets the name of the function.
   */
  public String getFunctionName();

  /**
   * Gets the list of arguments passed to the function.
   */
  // @default none
  public List<SimpleExpressionProxy> getArguments();

}
