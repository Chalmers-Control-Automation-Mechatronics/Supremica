//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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
import net.sourceforge.waters.model.compiler.context.OccursChecker;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * <P>A simplification rule to replace formulas that occur literally in
 * a constraint by <I>true</I> if they occur as subterms elsewhere, and to
 * replace formulas formulas whose negation occurs literally by
 * <I>false</I>.</P>
 *
 * <PRE>      EXPR, SUBTERM                 EXPR, !SUBTERM
 * ------------------------      ------------------------
 * EXPR[SUBTERM/1], SUBTERM      EXPR[SUBTERM/0], SUBTERM</PRE>
 *
 * @author Robi Malik
 */

class KnownLiteralReplacementRule extends SimplificationRule
{

  //#########################################################################
  //# Construction
  static KnownLiteralReplacementRule createRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final PlaceHolder EXPR = new PlaceHolder(factory, "EXPR");
    final SimpleIdentifierProxy template = EXPR.getIdentifier();
    return new KnownLiteralReplacementRule(template, EXPR);
  }


  //#########################################################################
  //# Constructors
  private KnownLiteralReplacementRule(final SimpleExpressionProxy template,
                                      final PlaceHolder EXPR)
  {
    super(template, new PlaceHolder[] {EXPR});
    mEXPR = EXPR;
  }


  //#########################################################################
  //# Invocation Interface
  @Override
  boolean match(final SimpleExpressionProxy constraint,
                final ConstraintPropagator propagator)
    throws EvalException
  {
    super.match(constraint, propagator);
    final OccursChecker checker = OccursChecker.getInstance();
    final SimpleExpressionProxy expr = mEXPR.getBoundExpression();
    for (final SimpleExpressionProxy subTerm :
         propagator.getCurrentNormalisedConstraints()) {
      if (subTerm != expr) {
        if (checker.occurs(subTerm, expr)) {
          mFoundSubTerm = subTerm;
          mReplacementValue = 1;
          return true;
        }
        final SimpleExpressionProxy negative =
          propagator.getNegativeSubTerm(subTerm);
        if (negative != null && checker.occurs(negative, expr)) {
          mFoundSubTerm = negative;
          mReplacementValue = 0;
          return true;
        }
      }
    }
    return false;
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
    final GeneralReplacementVisitor visitor =
      GeneralReplacementVisitor.getInstance();
    final SimpleExpressionProxy expr = mEXPR.getBoundExpression();
    final ModuleProxyFactory factory = propagator.getFactory();
    final IntConstantProxy replacement =
      factory.createIntConstantProxy(mReplacementValue);
    final SimpleExpressionProxy newExpr =
      visitor.replaceAll(expr, mFoundSubTerm, replacement, factory);
    propagator.addConstraint(newExpr);
  }


  //#########################################################################
  //# Data Members
  private final PlaceHolder mEXPR;
  private SimpleExpressionProxy mFoundSubTerm;
  private int mReplacementValue;

}
