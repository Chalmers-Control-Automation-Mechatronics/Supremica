//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariableCollector
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import net.sourceforge.waters.analysis.efa.AbstractEFAVariableFinder;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;


/**
 * A utility class to determine whether a given variable occurs
 * in an expression in primed or unprimed form.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

class EFSMVariableFinder
  extends AbstractEFAVariableFinder<EFSMVariable>
{

  //#########################################################################
  //# Constructor
  EFSMVariableFinder(final CompilerOperatorTable optable)
  {
    super(optable);
  }

}
