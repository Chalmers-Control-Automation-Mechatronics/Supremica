//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis.efa
//# CLASS:   UnifiedEFAVariable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;


import net.sourceforge.waters.analysis.efa.base.AbstractEFAVariable;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;


/**
 * A variable in a unified EFA system.
 * Extends general EFA variables by including a marking predicate.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFAVariable
  extends AbstractEFAVariable<AbstractEFAEvent>
{

  //#########################################################################
  //# Constructors
  UnifiedEFAVariable(final VariableComponentProxy var,
                     final CompiledRange range,
                     final IdentifierProxy defaultMarking,
                     final ModuleProxyFactory factory,
                     final CompilerOperatorTable op)
  {
    super(var, range, factory, op);
    mMarkedStatePredicate = null;
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    for (final VariableMarkingProxy marking : var.getVariableMarkings()) {
      if (eq.equals(marking.getProposition(), defaultMarking)) {
        mMarkedStatePredicate = marking.getPredicate();
        break;
      }
    }
  }


  //#########################################################################
  //# Simple Access
  public SimpleExpressionProxy getMarkedStatePredicate()
  {
    return mMarkedStatePredicate;
  }


  //#########################################################################
  //# Data Members
  private SimpleExpressionProxy mMarkedStatePredicate;

}
