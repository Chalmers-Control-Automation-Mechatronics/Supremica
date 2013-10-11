//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   BuiltInFunction
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.List;

import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * <P>A built-in function.</P>
 *
 * <P>Built-in functions have a name and a possibly variable number of
 * arguments. This class supports basic parsing and type checking.</P>
 *
 * @see FunctionCallExpressionProxy
 *
 * @author Robi Malik
 */

public interface BuiltInFunction extends Operator {

  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the minimum number of arguments that must be passed to this
   * function.
   */
  public int getMinimumNumberOfArguments();

  /**
   * Gets the maximum number of arguments that can be passed to this
   * function.
   */
  public int getMaximumNumberOfArguments();

  /**
   * Gets the mask of possible types for the given argument.
   * @param  argno       Argument position. Must at least zero and
   *                     less than the maximum number of arguments
   *                     returned by {@link #getMaximumNumberOfArguments()}.
   */
  public int getArgumentTypes(int argno);


  //#########################################################################
  //# Parsing Support
  public SimpleExpressionProxy createExpression
    (final ModuleProxyFactory factory,
     final List<SimpleExpressionProxy> args,
     final String text);


  //#########################################################################
  //# Evaluation
  /**
   * Returns the type mask of possible return types for given argument
   * types.
   * @param  argTypes    List of types of argument types, indicating the
   *                     number of arguments and their types.
   * @return Type mask of possible return types. A value of&nbsp;0 indicates
   *         that the arguments are not well-typed.
   */
  public int getReturnTypes(int[] argTypes);

  /**
   * Simplifies the given expression.
   * @param  expr        Expression to be simplified.
   * @param  simplifier  Simplifier to be used.
   */
  public SimpleExpressionProxy simplify
    (FunctionCallExpressionProxy expr,
     AbstractSimpleExpressionSimplifier simplifier)
    throws EvalException;

}
