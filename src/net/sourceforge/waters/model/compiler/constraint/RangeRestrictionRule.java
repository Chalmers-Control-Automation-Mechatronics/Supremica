//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   RangeRestrictionRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


abstract class RangeRestrictionRule extends SimplificationRule
{

  //#########################################################################
  //# Constructors
  RangeRestrictionRule(final SimpleExpressionProxy template,
                       final VariablePlaceHolder var,
                       final PlaceHolder expr)
  {
    super(template, new PlaceHolder[] {var, expr});
    mVariablePlaceHolder = var;
  }


  //#########################################################################
  //# Invocation Interface
  boolean match(final SimpleExpressionProxy constraint,
                final ConstraintPropagator propagator)
    throws EvalException
  {
    if (super.match(constraint, propagator)) {
      mRestrictedRange = getRestrictedRange();
      return mRestrictedRange != null;
    } else {
      mRestrictedRange = null;
      return false;
    }
  }

  boolean isMakingReplacement()
  {
    return false;
  }

  void execute(final ConstraintPropagator propagator)
    throws EvalException
  {
    final SimpleExpressionProxy varname =
      mVariablePlaceHolder.getBoundExpression();
    propagator.restrictRange(varname, mRestrictedRange);
  }


  //#########################################################################
  //# Provided by Subclasses
  abstract CompiledRange getRestrictedRange()
    throws EvalException;


  //#########################################################################
  //# Data Members
  private final VariablePlaceHolder mVariablePlaceHolder;

  private CompiledRange mRestrictedRange;

}