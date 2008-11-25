//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   VariablePlaceHolder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


class VariablePlaceHolder extends PlaceHolder
{
  
  //#########################################################################
  //# Constructors
  VariablePlaceHolder(final ModuleProxyFactory factory, final String name)
  {
    super(factory, name);
  }

  VariablePlaceHolder(final SimpleIdentifierProxy ident)
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
  {
    final VariableContext context = propagator.getContext();
    mRange = context.getVariableRange(expr);
    return mRange != null;
  }


  //#########################################################################
  //# Data Members
  private CompiledRange mRange;

}