//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   UnifiedEFAVariableContext
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAVariableContext;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.ModuleProxy;


/**
 * A variable context for unified EFA compilation.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

class UnifiedEFAVariableContext
  extends AbstractEFAVariableContext<AbstractEFAEvent,UnifiedEFAVariable>
{

  //#######################################################################
  //# Constructor
  public UnifiedEFAVariableContext(final ModuleProxy module,
                                   final CompilerOperatorTable op)
  {
    super(module, op);
  }

}