//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>A simplification rule to simplify a negated disjunction.</P>
 *
 * <PRE>
 *   !(LHS | RHS)
 *   ------------
 *    !LHS, !RHS
 * </PRE>
 *
 * @author Robi Malik
 */

class DeMorgansOrRule extends SimplificationRule
{

  //#########################################################################
  //# Construction
  static DeMorgansOrRule createRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final BinaryOperator orop = optable.getOrOperator();
    final UnaryOperator notop = optable.getNotOperator();
    final PlaceHolder LHS = new PlaceHolder(factory, "LHS");
    final PlaceHolder RHS = new PlaceHolder(factory, "RHS");
    final SimpleIdentifierProxy lhs = LHS.getIdentifier();
    final SimpleIdentifierProxy rhs = RHS.getIdentifier();
    final BinaryExpressionProxy disjunction =
      factory.createBinaryExpressionProxy(orop, lhs, rhs);
    final UnaryExpressionProxy template =
      factory.createUnaryExpressionProxy(notop, disjunction);
    return new DeMorgansOrRule(template, LHS, RHS);
  }

  //#########################################################################
  //# Constructors
  private DeMorgansOrRule(final SimpleExpressionProxy template,
                          final PlaceHolder LHS,
                          final PlaceHolder RHS)
  {
    super(template, new PlaceHolder[] {LHS, RHS});
    mLHS = LHS;
    mRHS = RHS;
  }


  //#########################################################################
  //# Invocation Interface
  @Override
  boolean isMakingReplacement()
  {
    return true;
  }

  @Override
  void execute(final ConstraintPropagator propagator)
    throws EvalException
  {
    final SimpleExpressionProxy lhs = mLHS.getBoundExpression();
    final SimpleExpressionProxy neglhs = propagator.getNegatedLiteral(lhs);
    propagator.addConstraint(neglhs);
    final SimpleExpressionProxy rhs = mRHS.getBoundExpression();
    final SimpleExpressionProxy negrhs = propagator.getNegatedLiteral(rhs);
    propagator.addConstraint(negrhs);
  }


  //#########################################################################
  //# Data Members
  private final PlaceHolder mLHS;
  private final PlaceHolder mRHS;

}
