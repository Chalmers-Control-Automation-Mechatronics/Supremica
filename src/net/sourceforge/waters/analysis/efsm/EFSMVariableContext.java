//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   EFSMVariableContext
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import net.sourceforge.waters.analysis.efa.AbstractEFAVariableContext;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.ModuleProxy;


/**
 * A variable context for EFSM compilation. Contains ranges of all
 * variables, and identifies enumeration atoms.
 */
class EFSMVariableContext extends AbstractEFAVariableContext<EFSMVariable>
{
  //#######################################################################
  //# Constructor
  public EFSMVariableContext(final ModuleProxy module,
                             final CompilerOperatorTable op)
  {
    super(module, op);
  }

}