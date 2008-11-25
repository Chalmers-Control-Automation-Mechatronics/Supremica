//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   IntegerVariablePlaceHolder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


class IntegerVariablePlaceHolder extends VariablePlaceHolder
{
  
  //#########################################################################
  //# Constructors
  IntegerVariablePlaceHolder(final ModuleProxyFactory factory,
                             final String name)
  {
    super(factory, name);
  }

  IntegerVariablePlaceHolder(final SimpleIdentifierProxy ident)
  {
    super(ident);
  }


  //#########################################################################
  //# Simple Access
  CompiledIntRange getIntRange()
  {
    return (CompiledIntRange) getRange();
  }


  //#########################################################################
  //# Matching
  boolean accepts(final SimpleExpressionProxy expr,
                  final ConstraintPropagator propagator)
  {
    return
      super.accepts(expr, propagator) &&
      getRange() instanceof CompiledIntRange;
  }

}