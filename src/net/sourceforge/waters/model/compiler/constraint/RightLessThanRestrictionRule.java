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

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * <P>A simplification to restrict the range of a variable in an
 * inequation.</P>
 *
 * <PRE>
 *    EXPR &lt; VARNAME
 * </PRE>
 *
 * <UL>
 * <LI>where <CODE>EXPR</CODE> is an integer range expression;</LI>
 * <LI>where <CODE>VARNAME</CODE> is an integer variable;</LI>
 * <LI>restricts the lower bound of the range of <CODE>VARNAME</CODE>.</LI>
 * </UL>
 *
 * @author Robi Malik
 */

class RightLessThanRestrictionRule extends RangeRestrictionRule
{

  //#########################################################################
  //# Construction
  static RightLessThanRestrictionRule createRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final BinaryOperator op = optable.getLessThanOperator();
    final IntegerVariablePlaceHolder VARNAME =
      new IntegerVariablePlaceHolder(factory, "VARNAME");
    final IntegerExpressionPlaceHolder EXPR =
      new IntegerExpressionPlaceHolder(factory, "EXPR");
    final SimpleIdentifierProxy varname = VARNAME.getIdentifier();
    final SimpleIdentifierProxy expr = EXPR.getIdentifier();
    final SimpleExpressionProxy template =
      factory.createBinaryExpressionProxy(op, expr, varname);
    return new RightLessThanRestrictionRule(template, VARNAME, EXPR);
  }


  //#########################################################################
  //# Constructor
  private RightLessThanRestrictionRule
    (final SimpleExpressionProxy template,
     final IntegerVariablePlaceHolder VARNAME,
     final IntegerExpressionPlaceHolder EXPR)
  {
    super(template, VARNAME, EXPR);
    mVARNAME = VARNAME;
    mEXPR = EXPR;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class RangeRestrictionRule
  CompiledIntRange getRestrictedRange()
    throws EvalException
  {
    final CompiledIntRange varrange = mVARNAME.getIntRange();
    final CompiledIntRange exprrange = mEXPR.getIntRange();
    final int min = exprrange.getLower() + 1;
    if (varrange.getLower() < min) {
      final int upper = varrange.getUpper();
      return new CompiledIntRange(min, upper);
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Data Members
  private final IntegerVariablePlaceHolder mVARNAME;
  private final IntegerExpressionPlaceHolder mEXPR; 

}
