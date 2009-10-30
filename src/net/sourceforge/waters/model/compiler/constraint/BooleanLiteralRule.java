//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   BooleanLiteralRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * <P>A simplification to perform substitution for a positive or negative
 * literal.</P>
 *
 * <PRE>!VARNAME    VARNAME</PRE>
 *
 * <P>There are two versions of this rule. The <I>positive</I> rule
 * substitutes <CODE>VARNAME</CODE> by&nbsp;<CODE>1</CODE> in the presence
 * of a positive literal&nbsp;<CODE>VARNAME</CODE>. The <I>negative</I>
 * rule substitutes <CODE>VARNAME</CODE> by&nbsp;<CODE>0</CODE> in the
 * presence of a negative literal&nbsp;<CODE>!VARNAME</CODE>.</P>
 *
 * @author Robi Malik
 */

class BooleanLiteralRule extends SimplificationRule
{

  //#########################################################################
  //# Construction
  static BooleanLiteralRule createPositiveLiteralRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final BooleanVariablePlaceHolder VARNAME =
      new BooleanVariablePlaceHolder(factory, "VARNAME");
    final SimpleIdentifierProxy ident = VARNAME.getIdentifier();
    final IntConstantProxy value = factory.createIntConstantProxy(1);
    return new BooleanLiteralRule(ident, VARNAME, value);
  }

  static BooleanLiteralRule createNegativeLiteralRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final UnaryOperator op = optable.getNotOperator();
    final BooleanVariablePlaceHolder VARNAME =
      new BooleanVariablePlaceHolder(factory, "VARNAME");
    final SimpleIdentifierProxy ident = VARNAME.getIdentifier();
    final SimpleExpressionProxy template =
      factory.createUnaryExpressionProxy(op, ident);
    final IntConstantProxy value = factory.createIntConstantProxy(0);
    return new BooleanLiteralRule(template, VARNAME, value);
  }


  //#########################################################################
  //# Constructor
  private BooleanLiteralRule(final SimpleExpressionProxy template,
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
    propagator.processEquation(varname, mValue);
  }


  //#########################################################################
  //# Data Members
  private final BooleanVariablePlaceHolder mPlaceHolder;
  private final IntConstantProxy mValue;

}