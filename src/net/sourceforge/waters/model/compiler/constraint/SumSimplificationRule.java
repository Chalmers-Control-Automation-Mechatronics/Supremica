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

package net.sourceforge.waters.model.compiler.constraint;

import java.util.Comparator;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SumSimplifier;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>A simplification to rewrite sums.</P>
 *
 * <PRE>LHS == RHS</PRE>
 *
 * <P>This rewrite rules partially solves linear equations to give a value
 * for the lowest-ordered variable, if possible. It handles associativity
 * and commutativity of addition, and cancellation by subtraction including
 * nested use of the unary minus operator. Aggregation of equal terms and
 * distributivity are not implemented.</P>
 *
 * <P>Following are examples of supported simplifications:</P>
 *
 * <PRE>
 *   1 == 1 + x      1 - y == x + y
 *   ----------      --------------
 *     x == 0        x == 1 - y - y
 * </PRE>
 *
 * <P>The rule can be parametrised with an operator, which can be
 * <CODE>==</CODE> or <CODE>!=</CODE>. Other inequalities are not yet
 * supported.</P>
 *
 * <P>The implementation of the rule is based on {@link SumSimplifier},
 * which performs the normalisation of additive terms in the {@link
 * SimpleExpressionCompiler} and provides most of the simplification logic.</P>
 *
 * @author Robi Malik
 */

class SumSimplificationRule extends SimplificationRule
{

  //#########################################################################
  //# Construction
  static SumSimplificationRule createRule
    (final ModuleProxyFactory factory,
     final BinaryOperator op)
  {
    final SumPlaceHolder LHS = new SumPlaceHolder(factory, "LHS");
    final SumPlaceHolder RHS = new SumPlaceHolder(factory, "RHS");
    final SimpleIdentifierProxy lhs = LHS.getIdentifier();
    final SimpleIdentifierProxy rhs = RHS.getIdentifier();
    final BinaryExpressionProxy template =
      factory.createBinaryExpressionProxy(op, lhs, rhs);
    return new SumSimplificationRule(template, LHS, RHS);
  }


  //#########################################################################
  //# Constructors
  private SumSimplificationRule(final BinaryExpressionProxy template,
                                final SumPlaceHolder LHS,
                                final SumPlaceHolder RHS)
  {
    super(template, new PlaceHolder[] {LHS, RHS});
    mOperator = template.getOperator();
    mLHS = LHS;
    mRHS = RHS;
  }


  //#########################################################################
  //# Invocation Interface
  @Override
  boolean match(final SimpleExpressionProxy constraint,
                final ConstraintPropagator propagator)
    throws EvalException
  {
    if (!super.match(constraint, propagator)) {
      return false;
    } else if (mLHS.isPlusOrMinus()) {
      return true;
    } else if (mRHS.isPlusOrMinus()) {
      final SumSimplifier simplifier = propagator.getSumSimplifier();
      final SimpleExpressionProxy lhs = mLHS.getBoundExpression();
      final SimpleExpressionProxy rhs = mRHS.getBoundExpression();
      final Comparator<SimpleExpressionProxy> comparator =
        propagator.getEquationComparator();
      return !simplifier.isNormalisedEquation(lhs, rhs, mOperator, comparator);
    } else {
      return false;
    }
  }

  @Override
  boolean isMakingReplacement()
  {
    return true;
  }

  @Override
  void execute(final ConstraintPropagator propagator)
    throws EvalException
  {
    final SumSimplifier simplifier = propagator.getSumSimplifier();
    final SimpleExpressionProxy lhs = mLHS.getBoundExpression();
    final SimpleExpressionProxy rhs = mRHS.getBoundExpression();
    final Comparator<SimpleExpressionProxy> comparator =
      propagator.getEquationComparator();
    final SimpleExpressionProxy constraint =
      simplifier.normaliseEquation(lhs, rhs, mOperator, comparator);
    propagator.addConstraint(constraint);
  }


  //#########################################################################
  //# Inner Class SumPlaceHolder
  private static class SumPlaceHolder extends IntegerExpressionPlaceHolder
  {
    //#######################################################################
    //# Constructor
    SumPlaceHolder(final ModuleProxyFactory factory, final String name)
    {
      super(factory, name);
    }

    //#######################################################################
    //# Sum Matching and Normalisation
    private boolean isPlusOrMinus()
    {
      final SimpleExpressionProxy expr = getBoundExpression();
      if (expr instanceof BinaryExpressionProxy) {
        final BinaryExpressionProxy binary = (BinaryExpressionProxy) expr;
        final BinaryOperator op = binary.getOperator();
        final CompilerOperatorTable optable =
          CompilerOperatorTable.getInstance();
        return
          op == optable.getPlusOperator() || op == optable.getMinusOperator();
      } else if (expr instanceof UnaryExpressionProxy) {
        final UnaryExpressionProxy unary = (UnaryExpressionProxy) expr;
        final UnaryOperator op = unary.getOperator();
        final CompilerOperatorTable optable =
          CompilerOperatorTable.getInstance();
        return op == optable.getUnaryMinusOperator();
      } else {
        return false;
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final BinaryOperator mOperator;
  private final SumPlaceHolder mLHS;
  private final SumPlaceHolder mRHS;

}
