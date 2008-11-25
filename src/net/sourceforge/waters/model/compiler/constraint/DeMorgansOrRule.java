//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   DeMorgansOrRule
//###########################################################################
//# $Id$
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
 * <P>A simplification simplify a negated disjunction.</P>
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
  boolean isMakingReplacement()
  {
    return true;
  }

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