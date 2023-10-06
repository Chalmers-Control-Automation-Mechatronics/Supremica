//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.dialog;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * <P>A text field to enter simple expressions.</P>
 *
 * <P>A SimpleExpressionInputCell allows the user to input text representing
 * a Waters simple expression ({@link SimpleExpressionProxy}) of a specific
 * type.</P>
 *
 * <P>This class provides support for use inside a table or list. An
 * {@link ExpressionParser} is used to validate the input, and error messages
 * from the parser can be sent to a configurable destination. Attempts are
 * made to prevent the entry of characters that are not allowed in an
 * expression of the expected type.</P>
 *
 * @author Robi Malik
 */

public class SimpleExpressionInputCell
  extends ValidatingTextCell<SimpleExpressionProxy>
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a cell to enter expressions of an arbitrary type.
   * @param  parser       The expression parser to be used for input validation.
   *                      It can be obtained from the
   *                      {@link ModuleWindowInterface}.
   * @param  nullAllowed  Whether the text field accepts an empty input.
   *                      If <CODE>true</CODE>, an empty input results in a
   *                      <CODE>null</CODE> expression as the value;
   *                      otherwise committing an empty input produces an
   *                      error.
   */
  public SimpleExpressionInputCell(final ExpressionParser parser,
                                   final boolean nullAllowed)
  {
    this(Operator.TYPE_ANY, parser, nullAllowed);
  }

  /**
   * Creates a cell to enter expressions of a specific type.
   * @param  mask         Type mask of supported types.
   *                      It can be defined using the constants in class
   *                      {@link Operator}.
   * @param  parser       The expression parser to be used for input validation.
   *                      It can be obtained from the
   *                      {@link ModuleWindowInterface}.
   * @param  nullAllowed  Whether the text field accepts an empty input.
   *                      If <CODE>true</CODE>, an empty input results in a
   *                      <CODE>null</CODE> expression as the value;
   *                      otherwise committing an empty input produces an
   *                      error.
   */
  public SimpleExpressionInputCell(final int mask,
                                   final ExpressionParser parser,
                                   final boolean nullAllowed)
  {
    this(new SimpleExpressionInputHandler(mask, parser, nullAllowed));
  }

  /**
   * Creates a cell to enter expressions of an arbitrary type.
   * @param  expr         The initial value for the text field.
   * @param  parser       The expression parser to be used for input validation.
   *                      It can be obtained from the
   *                      {@link ModuleWindowInterface}.
   * @param  nullAllowed  Whether the text field accepts an empty input.
   *                      If <CODE>true</CODE>, an empty input results in a
   *                      <CODE>null</CODE> expression as the value;
   *                      otherwise committing an empty input produces an
   *                      error.
   */
  public SimpleExpressionInputCell(final SimpleExpressionProxy expr,
                                   final ExpressionParser parser,
                                   final boolean nullAllowed)
  {
    this(expr, Operator.TYPE_ANY, parser, nullAllowed);
  }

  /**
   * Creates a cell to enter expressions of a specific type.
   * @param  expr         The initial value for the text field.
   * @param  mask         Type mask of supported types.
   *                      It can be defined using the constants in
   *                      {@link Operator}.
   * @param  parser       The expression parser to be used for input validation.
   *                      It can be obtained from the
   *                      {@link ModuleWindowInterface}.
   * @param  nullAllowed  Whether the text field accepts an empty input.
   *                      If <CODE>true</CODE>, an empty input results in a
   *                      <CODE>null</CODE> expression as the value;
   *                      otherwise committing an empty input produces an
   *                      error.
   */
  public SimpleExpressionInputCell(final SimpleExpressionProxy expr,
                                   final int mask,
                                   final ExpressionParser parser,
                                   final boolean nullAllowed)
  {
    this(expr, new SimpleExpressionInputHandler(mask, parser, nullAllowed));
  }

  /**
   * Creates a customised simple expression cell.
   * @param  handler   An input handler to validate and format the text input.
   *                   By specifying a customised input handler, the user
   *                   can implement type checking beyond the type masks.
   */
  public SimpleExpressionInputCell
    (final FormattedInputHandler<? extends SimpleExpressionProxy> handler)
  {
    super(handler);
  }

  /**
   * Creates a customised simple expression cell.
   * @param  expr      The initial value for the text field.
   * @param  handler   An input handler to validate and format the text input.
   *                   By specifying a customised input handler, the user
   *                   can implement type checking beyond the type masks.
   */
  public SimpleExpressionInputCell
    (final SimpleExpressionProxy expr,
     final FormattedInputHandler<? extends SimpleExpressionProxy> handler)
  {
    super(expr, handler);
  }


  //#########################################################################
  //# Overrides for javax.swing.JFormattedTextField
  @Override
  public SimpleExpressionProxy getValue()
  {
    return (SimpleExpressionProxy) super.getValue();
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -7086968760128989837L;

}
