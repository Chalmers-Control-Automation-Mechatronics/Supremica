//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   NegativeLiteralRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>A simplification to perform substitution for a positive literal.</P>
 *
 * <PRE>!VARNAME</PRE>
 *
 * <P><CODE>VARNAME</CODE> must be a Boolean variable.
 * Substitutes <CODE>VARNAME</CODE> with <CODE>0</CODE>.</P>
 *
 * @author Robi Malik
 */

class NegativeLiteralRule extends SimplificationRule
{

  //#########################################################################
  //# Construction
  static NegativeLiteralRule createRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final UnaryOperator op = optable.getNotOperator();
    final BooleanVariablePlaceHolder VARNAME =
      new BooleanVariablePlaceHolder(factory, "VARNAME");
    final SimpleIdentifierProxy ident = VARNAME.getIdentifier();
    final SimpleExpressionProxy template =
      factory.createUnaryExpressionProxy(op, ident);
    final IntConstantProxy value = factory.createIntConstantProxy(1);
    return new NegativeLiteralRule(template, VARNAME, value);
  }


  //#########################################################################
  //# Constructor
  private NegativeLiteralRule(final SimpleExpressionProxy template,
                              final BooleanVariablePlaceHolder placeholder,
                              final IntConstantProxy value)
  {
    super(template, placeholder);
    mPlaceHolder = placeholder;
    mValue = value;
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
    final SimpleExpressionProxy varname = mPlaceHolder.getBoundExpression();
    final SimpleExpressionProxy eqn = getMatchedExpression();
    propagator.processEquation(varname, mValue, eqn);
  }


  //#########################################################################
  //# Data Members
  private final BooleanVariablePlaceHolder mPlaceHolder;
  private final IntConstantProxy mValue;

}