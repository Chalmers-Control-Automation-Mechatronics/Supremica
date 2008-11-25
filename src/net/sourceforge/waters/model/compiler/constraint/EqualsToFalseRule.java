//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   EqualsToFalseRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * <P>A simplification rule simplify Boolean equations involving FALSE</P>
 *
 * <PRE>
 *   0 == EXPR
 *   ---------
 *     !EXPR
 * </PRE>
 *
 * <P><CODE>EXPR</CODE> must be an expression of Boolean range.</P>
 *
 * @author Robi Malik
 */

class EqualsToFalseRule extends SimplificationRule
{

  //#########################################################################
  //# Construction
  static EqualsToFalseRule createRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final BinaryOperator op = optable.getEqualsOperator();
    final IntConstantProxy falseconst = factory.createIntConstantProxy(0);
    final PlaceHolder EXPR = new BooleanExpressionPlaceHolder(factory, "EXPR");
    final SimpleIdentifierProxy expr = EXPR.getIdentifier();
    final BinaryExpressionProxy template =
      factory.createBinaryExpressionProxy(op, falseconst, expr);
    return new EqualsToFalseRule(template, EXPR);
  }


  //#########################################################################
  //# Constructor
  private EqualsToFalseRule(final SimpleExpressionProxy template,
                            final PlaceHolder placeholder)
  {
    super(template, placeholder);
    mPlaceHolder = placeholder;
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
    final SimpleExpressionProxy expr = mPlaceHolder.getBoundExpression();
    final SimpleExpressionProxy negexpr = propagator.getNegatedLiteral(expr);
    propagator.addConstraint(negexpr);
  }


  //#########################################################################
  //# Data Members
  private final PlaceHolder mPlaceHolder;

}