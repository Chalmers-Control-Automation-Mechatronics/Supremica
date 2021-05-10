//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.model.compiler.groupnode;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DescendingModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * A utility class to search expressions for primed subterms or assignments.
 *
 * @author Robi Malik
 */

public class PrimeFinder
  extends DescendingModuleProxyVisitor
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new prime finder.
   * @param  optable     The operator table that defines the prime and
   *                     assignment operators.
   */
  public PrimeFinder(final CompilerOperatorTable optable)
  {
    mOperatorTable = optable;
  }


  //#########################################################################
  //# Invocation
  /**
   * Checks whether the given expression contains a primed variable or
   * assignment.
   * @param  expr        The expression to be searched.
   * @return The first subterm found to be an assignment or primed
   *         expression, or <CODE>null</CODE>.
   */
  public SimpleExpressionProxy containsPrimedVariable
    (final SimpleExpressionProxy expr)
  {
    try {
      expr.acceptVisitor(this);
      return null;
    } catch (final PrimeFoundNotification notification) {
      return notification.getExpression();
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
    throws VisitorException
  {
    if (mOperatorTable.isAssignment(expr)) {
      throw new PrimeFoundNotification(expr);
    } else {
      return super.visitBinaryExpressionProxy(expr);
    }
  }

  @Override
  public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
  {
    return null;
  }

  @Override
  public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
    throws VisitorException
  {
    if (expr.getOperator() == mOperatorTable.getNextOperator()) {
      throw new PrimeFoundNotification(expr);
    } else {
      return super.visitUnaryExpressionProxy(expr);
    }
  }


  //#########################################################################
  //# Inner Class PrimeFoundNotification
  private static class PrimeFoundNotification extends VisitorException
  {
    //#######################################################################
    //# Constructor
    private PrimeFoundNotification(final SimpleExpressionProxy expr)
    {
      mExpression = expr;
    }

    //#######################################################################
    //# Simple Access
    private SimpleExpressionProxy getExpression()
    {
      return mExpression;
    }

    //#######################################################################
    //# Data Members
    private final SimpleExpressionProxy mExpression;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 3511954770213121187L;
  }


  //#########################################################################
  //# Data Members
  private final CompilerOperatorTable mOperatorTable;

}
