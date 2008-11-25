//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   BooleanVariablePlaceHolder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.context.CompiledIntRange;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


class BooleanVariablePlaceHolder extends VariablePlaceHolder
{
  
  //#########################################################################
  //# Constructors
  BooleanVariablePlaceHolder(final ModuleProxyFactory factory,
                             final String name)
  {
    super(factory, name);
  }

  BooleanVariablePlaceHolder(final SimpleIdentifierProxy ident)
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
    if (super.accepts(expr, propagator) &&
        getRange() instanceof CompiledIntRange) {
      final CompiledIntRange intrange = getIntRange();
      return intrange.getLower() >= 0 && intrange.getUpper() <= 1;
    } else {
      return false;
    }
  }

}