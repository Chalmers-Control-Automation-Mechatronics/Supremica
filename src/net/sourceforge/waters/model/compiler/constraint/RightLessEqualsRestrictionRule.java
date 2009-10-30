//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   RightLessEqualsRestrictionRule
//###########################################################################
//# $Id$
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
 * <PRE>EXPR &lt;= VARNAME</PRE>
 *
 * <UL>
 * <LI>where <CODE>EXPR</CODE> is an integer range expression;</LI>
 * <LI>where <CODE>VARNAME</CODE> is an integer variable;</LI>
 * <LI>restricts the lower bound of the range of <CODE>VARNAME</CODE>.</LI>
 * </UL>
 *
 * @author Robi Malik
 */

class RightLessEqualsRestrictionRule extends RangeRestrictionRule
{

  //#########################################################################
  //# Construction
  static RightLessEqualsRestrictionRule createRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final BinaryOperator op = optable.getLessEqualsOperator();
    final IntegerVariablePlaceHolder VARNAME =
      new IntegerVariablePlaceHolder(factory, "VARNAME");
    final IntegerExpressionPlaceHolder EXPR =
      new IntegerExpressionPlaceHolder(factory, "EXPR");
    final SimpleIdentifierProxy varname = VARNAME.getIdentifier();
    final SimpleIdentifierProxy expr = EXPR.getIdentifier();
    final SimpleExpressionProxy template =
      factory.createBinaryExpressionProxy(op, expr, varname);
    return new RightLessEqualsRestrictionRule(template, VARNAME, EXPR);
  }


  //#########################################################################
  //# Constructor
  private RightLessEqualsRestrictionRule
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
    final int min = exprrange.getLower();
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