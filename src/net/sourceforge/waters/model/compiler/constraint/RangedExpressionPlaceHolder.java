//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   RangedPlaceHolder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


class RangedExpressionPlaceHolder extends PlaceHolder
{
  
  //#########################################################################
  //# Constructors
  RangedExpressionPlaceHolder(final ModuleProxyFactory factory,
                              final String name)
  {
    super(factory, name);
  }

  RangedExpressionPlaceHolder(final SimpleIdentifierProxy ident)
  {
    super(ident);
  }


  //#########################################################################
  //# Simple Access
  CompiledRange getRange()
  {
    return mRange;
  }


  //#########################################################################
  //# Matching
  void reset()
  {
    super.reset();
    mRange = null;
  }

  boolean accepts(final SimpleExpressionProxy expr,
                  final ConstraintPropagator propagator)
    throws EvalException
  {
    mRange = propagator.estimateRange(expr);
    return mRange != null;
  }


  //#########################################################################
  //# Data Members
  private CompiledRange mRange;

}