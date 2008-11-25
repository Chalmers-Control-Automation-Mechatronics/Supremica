//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   AtomPlaceHolder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


class AtomPlaceHolder extends PlaceHolder
{
  
  //#########################################################################
  //# Constructors
  AtomPlaceHolder(final ModuleProxyFactory factory, final String name)
  {
    super(factory, name);
  }

  AtomPlaceHolder(final SimpleIdentifierProxy ident)
  {
    super(ident);
  }


  //#########################################################################
  //# Matching
  boolean accepts(final SimpleExpressionProxy expr,
                  final ConstraintPropagator propagator)
  {
    return propagator.isAtomicValue(expr);
  }

}