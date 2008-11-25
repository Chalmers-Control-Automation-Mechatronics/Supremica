//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   EqualitySubstitutionRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.OccursChecker;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
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
 * <CODE>VARNAME</CODE> with <CODE>EXPR</CODE>.</P>
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
    final SimpleExpressionProxy eqn = getMatchedExpression();
    propagator.processEquation(varname, expr, eqn);
  }


  //#########################################################################
  //# Data Members
  private final PlaceHolder mVARNAME;
  private final PlaceHolder mEXPR;

}
