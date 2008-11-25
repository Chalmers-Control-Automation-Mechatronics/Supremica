//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   LeftNotEqualsRestrictionRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * <P>A simplification to restrict the range of a variable in an
 * inequation.</P>
 *
 * <PRE>
 *   VARNAME != EXPR
 * </PRE>
 *
 * <UL>
 * <LI>where <CODE>VARNAME</CODE> is a variable;</LI>
 * <LI>where <CODE>EXPR</CODE> is an atomic expression;</LI>
 * <LI>restricts the range of&nbsp;<CODE>VARNAME</CODE> by removing
 *     the value of&nbsp;<CODE>EXPR</CODE>.</LI>
 * </UL>
 *
 * @author Robi Malik
 */

class LeftNotEqualsRestrictionRule extends RangeRestrictionRule
{

  //#########################################################################
  //# Construction
  static LeftNotEqualsRestrictionRule createRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final BinaryOperator op = optable.getNotEqualsOperator();
    final VariablePlaceHolder VARNAME =
      new VariablePlaceHolder(factory, "VARNAME");
    final PlaceHolder EXPR = new AtomPlaceHolder(factory, "EXPR");
    final SimpleIdentifierProxy varname = VARNAME.getIdentifier();
    final SimpleIdentifierProxy expr = EXPR.getIdentifier();
    final BinaryExpressionProxy template =
      factory.createBinaryExpressionProxy(op, varname, expr);
    return new LeftNotEqualsRestrictionRule(template, VARNAME, EXPR);
  }


  //#########################################################################
  //# Constructor
  private LeftNotEqualsRestrictionRule
    (final SimpleExpressionProxy template,
     final VariablePlaceHolder VARNAME,
     final PlaceHolder EXPR)
  {
    super(template, VARNAME, EXPR);
    mVARNAME = VARNAME;
    mEXPR = EXPR;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class RangeRestrictionRule
  CompiledRange getRestrictedRange()
    throws EvalException
  {
    final CompiledRange range = mVARNAME.getRange();
    final SimpleExpressionProxy expr = mEXPR.getBoundExpression();
    final CompiledRange reduced = range.remove(expr);
    return range == reduced ? null : reduced;
  }


  //#########################################################################
  //# Data Members
  private final VariablePlaceHolder mVARNAME;
  private final PlaceHolder mEXPR;

}