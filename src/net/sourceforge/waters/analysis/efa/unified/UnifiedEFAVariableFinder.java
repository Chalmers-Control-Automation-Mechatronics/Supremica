//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFAVariableFinder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAVariableFinder;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;


/**
 * A utility class to determine whether a given variable occurs
 * in an expression in primed or unprimed form.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

class UnifiedEFAVariableFinder
  extends AbstractEFAVariableFinder<UnifiedEFAEvent,UnifiedEFAVariable>
{

  //#########################################################################
  //# Constructor
  UnifiedEFAVariableFinder(final CompilerOperatorTable optable)
  {
    super(optable);
  }

}
