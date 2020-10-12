//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.model.compiler.instance;

import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * A compiler exception to indicate invalid use of the next-state
 * (prime) operator.
 *
 * @author Robi Malik
 */

public class NestedNextException extends EvalException
{

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception indicating that a primed subterm was
   * encountered within the expression of a variable assignment.
   * @param  primed      The offending primed expression.
   * @param  assignment  The assignment within which the primed expression
   *                     was found.
   */
  public NestedNextException(final UnaryExpressionProxy primed,
                             final BinaryExpressionProxy assignment)
  {
    super("Next-state (prime) operators cannot be used within assignments.",
          assignment);
  }

  /**
   * Constructs a new exception indicating that a primed subterm was
   * encountered within an index of an indexed identifier.
   * @param  primed      The offending primed expression.
   * @param  ident       The indexed identifier within which the primed
   *                     expression was found.
   */
  public NestedNextException(final UnaryExpressionProxy primed,
                             final IndexedIdentifierProxy ident)
  {
    super("Next-state (prime) operators cannot be used within array indexes.",
          primed);
  }

  /**
   * Constructs a new exception indicating that a further prime operator
   * has been encountered within a primed expression.
   * @param  inner       The primed expression found nested within another.
   * @param  outer       The primed expression within which another was found.
   */
  public NestedNextException(final UnaryExpressionProxy inner,
                             final UnaryExpressionProxy outer)
  {
    super("Next-state (prime) operators cannot be nested.", inner);
  }


  //#########################################################################
  //# Static Class Variables
  private static final long serialVersionUID = 6340374553094387042L;

}
