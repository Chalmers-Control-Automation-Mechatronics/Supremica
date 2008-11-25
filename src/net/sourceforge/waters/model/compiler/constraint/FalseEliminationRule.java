//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   FalseEliminationRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.IntConstantProxy;


/**
 * A simplification rule to process the constant FALSE.
 * When the constant&nbsp;<CODE>0</CODE> is encountered as a literal,
 * the constraint propagator stops and returns FALSE as a result of
 * constraint propagation.
 *
 * @author Robi Malik
 */

class FalseEliminationRule extends SimplificationRule
{

  //#########################################################################
  //# Construction
  static FalseEliminationRule createRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final IntConstantProxy template = factory.createIntConstantProxy(0);
    return new FalseEliminationRule(template);
  }


  //#########################################################################
  //# Constructors
  private FalseEliminationRule(final IntConstantProxy template)
  {
    super(template, PLACEHOLDERS);
  }


  //#########################################################################
  //# Invocation Interface
  boolean isMakingReplacement()
  {
    return false;
  }

  void execute(final ConstraintPropagator propagator)
  {
    propagator.setFalse();
  }


  //#########################################################################
  //# Static Class Constants
  private static final PlaceHolder[] PLACEHOLDERS = {};

}