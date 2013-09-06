//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
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
 * A representation of an EFSM variable for use in compositional
 * analysis.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFAVariable
  extends AbstractEFAVariable<UnifiedEFAEvent>
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
    final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
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
