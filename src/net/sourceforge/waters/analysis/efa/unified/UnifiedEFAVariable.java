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
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.VariableComponentProxy;


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
                     final ModuleProxyFactory factory,
                     final CompilerOperatorTable op)
  {
    super(var, range, factory, op);
  }


  //#########################################################################
  //# Simple Access


  //#########################################################################
  //# Data Members

}
