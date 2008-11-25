//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   LeftLessThanRestrictionRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
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
 *   VARNAME &lt; EXPR
 * </PRE>
 *
 * <UL>
 * <LI>where <CODE>VARNAME</CODE> is an integer variable;</LI>
 * <LI>where <CODE>EXPR</CODE> is an integer range expression;</LI>
 * <LI>restricts the upper bound of the range of <CODE>VARNAME</CODE>.</LI>
 * </UL>
 *
 * @author Robi Malik
 */

class LeftLessThanRestrictionRule extends RangeRestrictionRule
{

  //#########################################################################
  //# Construction
  static LeftLessThanRestrictionRule createRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final BinaryOperator op = optable.getLessThanOperator();
    final IntegerVariablePlaceHolder VARNAME =
      new IntegerVariablePlaceHolder(factory, "VARNAME");
    final IntegerExpressionPlaceHolder EXPR =
      new IntegerExpressionPlaceHolder(factory, "EXPR");
    final SimpleIdentifierProxy varname = VARNAME.getIdentifier();
    final SimpleIdentifierProxy expr = EXPR.getIdentifier();
    final SimpleExpressionProxy template =
      factory.createBinaryExpressionProxy(op, varname, expr);
    return new LeftLessThanRestrictionRule(template, VARNAME, EXPR);
  }


  //#########################################################################
  //# Constructor
  private LeftLessThanRestrictionRule
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
    final int max = exprrange.getUpper() - 1;
    if (varrange.getUpper() > max) {
      final int lower = varrange.getLower();
      return new CompiledIntRange(lower, max);
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Data Members
  private final IntegerVariablePlaceHolder mVARNAME;
  private final IntegerExpressionPlaceHolder mEXPR; 

}