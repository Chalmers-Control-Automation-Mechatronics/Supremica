//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   TrueEliminationRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.IntConstantProxy;


/**
 * <P>A simplification rule to remove the constant TRUE.</P>
 *
 * <PRE>
 *    1
 *   ---
 *    &nbsp;
 * </PRE>
 *
 * @author Robi Malik
 */

class TrueEliminationRule extends SimplificationRule
{

  //#########################################################################
  //# Construction
  static TrueEliminationRule createRule
    (final ModuleProxyFactory factory,
     final CompilerOperatorTable optable)
  {
    final IntConstantProxy template = factory.createIntConstantProxy(1);
    return new TrueEliminationRule(template);
  }


  //#########################################################################
  //# Constructor
  private TrueEliminationRule(final IntConstantProxy template)
  {
    super(template, PLACEHOLDERS);
  }


  //#########################################################################
  //# Invocation Interface
  boolean isMakingReplacement()
  {
    return true;
  }

  void execute(final ConstraintPropagator propagator)
  {
  }


  //#########################################################################
  //# Static Class Constants
  private static final PlaceHolder[] PLACEHOLDERS = {};

}