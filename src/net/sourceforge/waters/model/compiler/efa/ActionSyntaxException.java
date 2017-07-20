//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.model.compiler.efa;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


public class ActionSyntaxException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception indicating that the given expression does
   * not form a valid action.
   */
  public ActionSyntaxException(final SimpleExpressionProxy expr)
  {
    super("Expression '" + expr + "' does not form a valid action!", expr);
  }

  /**
   * Constructs a new exception indicating that an assignment operator
   * was encountered inside a guard or an expression where it should not
   * occur.
   * @param  assignment  The offending assignment expression.
   * @param  where       A string indicating where the assignment was
   *                     found, either &quot;guard&quot; or
   *                     &quot;expression&quot;.
   */
  public ActionSyntaxException(final BinaryExpressionProxy assignment,
                               final String where)
  {
    super("Assignment operator " + assignment.getOperator().getName() +
          " encountered in " + where + "! Please use == instead.",
          assignment);
  }

  /**
   * Constructs a new exception indicating that the given expression does
   * not form a valid action, because it is attempting to assign to a
   * non-identifier.
   */
  public ActionSyntaxException(final SimpleExpressionProxy expr,
                               final SimpleExpressionProxy nonident)
  {
    super("Attempting to assign to non-identifier " +
          nonident + " in action!", nonident);
  }

  /**
   * Constructs a new exception with the given message and location.
   */
  public ActionSyntaxException(final String msg, final Proxy location)
  {
    super(msg, location);
  }


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
