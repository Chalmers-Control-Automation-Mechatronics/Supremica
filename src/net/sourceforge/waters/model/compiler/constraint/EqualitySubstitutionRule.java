//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * <P>A simplification to perform equality substitution for an
 * equation.</P>
 *
 * <PRE>VARNAME == EXPR</PRE>
 *
 * <P><CODE>VARNAME</CODE> must be a variable, and <CODE>EXPR</CODE> must
 * be an expression that does not contain <CODE>VARNAME</CODE>. Substitutes
 * <CODE>VARNAME</CODE> by&nbsp;<CODE>EXPR</CODE>.</P>
 *
 * @author Robi Malik
 */

class EqualitySubstitutionRule extends SimplificationRule
{

  //#########################################################################
  //# Construction
  static EqualitySubstitutionRule createRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final BinaryOperator op = optable.getEqualsOperator();
    final PlaceHolder VARNAME =
      new VariablePlaceHolder(factory, "VARNAME");
    final PlaceHolder EXPR = new PlaceHolder(factory, "EXPR");
    final SimpleIdentifierProxy varname = VARNAME.getIdentifier();
    final SimpleIdentifierProxy expr = EXPR.getIdentifier();
    final SimpleExpressionProxy template =
      factory.createBinaryExpressionProxy(op, varname, expr);
    return new EqualitySubstitutionRule(template, VARNAME, EXPR);
  }


  //#########################################################################
  //# Constructors
  private EqualitySubstitutionRule(final SimpleExpressionProxy template,
                                   final PlaceHolder VARNAME,
                                   final PlaceHolder EXPR)
  {
    super(template, new PlaceHolder[] {VARNAME, EXPR});
    mVARNAME = VARNAME;
    mEXPR = EXPR;
  }


  //#########################################################################
  //# Invocation Interface
  boolean match(final SimpleExpressionProxy constraint,
                final ConstraintPropagator propagator)
    throws EvalException
  {
    if (super.match(constraint, propagator)) {
      final OccursChecker checker = OccursChecker.getInstance();
      final SimpleExpressionProxy varname = mVARNAME.getBoundExpression();
      final SimpleExpressionProxy expr = mEXPR.getBoundExpression();
      return !checker.occurs(varname, expr);
    } else {
      return false;
    }
  }

  boolean isMakingReplacement()
  {
    return true;
  }

  void execute(final ConstraintPropagator propagator)
    throws EvalException
  {
    final SimpleExpressionProxy varname = mVARNAME.getBoundExpression();
    final SimpleExpressionProxy expr = mEXPR.getBoundExpression();
    propagator.processEquation(varname, expr);
  }


  //#########################################################################
  //# Data Members
  private final PlaceHolder mVARNAME;
  private final PlaceHolder mEXPR;

}








