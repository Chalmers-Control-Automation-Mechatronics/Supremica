//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.model.compiler.efsm
//# CLASS:   UnifiedEFAVariableCollector
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAVariableCollector;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;


/**
 * A utility class to collect all the EFSM variables (primed or not) in
 * an update or event encoding.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

class UnifiedEFAVariableCollector
  extends AbstractEFAVariableCollector<AbstractEFAEvent,UnifiedEFAVariable>
{


  //#########################################################################
  //# Constructor
  UnifiedEFAVariableCollector(final CompilerOperatorTable optable,
                        final UnifiedEFAVariableContext context)
  {
    super(optable, context);
  }
}
