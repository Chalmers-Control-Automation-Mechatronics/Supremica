//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.model.compiler.efsm
//# CLASS:   EFSMVariableCollector
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.util.Collection;

import net.sourceforge.waters.analysis.efa.AbstractEFAVariableCollector;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;


/**
 * A utility class to collect all the EFSM variables (primed or not) in
 * an update or event encoding.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

class EFSMVariableCollector
  extends AbstractEFAVariableCollector<EFSMVariable>
{


  //#########################################################################
  //# Constructor
  EFSMVariableCollector(final CompilerOperatorTable optable,
                        final EFSMVariableContext context)
  {
    super(optable, context);
  }


  //#########################################################################
  //# Invocation
  /**
   * Collects all variables in the given event encoding.
   * @param  update   The event encoding to be searched.
   * @param  vars     All variables will be added to this collection.
   */
  void collectAllVariables(final EFSMEventEncoding encoding,
                           final Collection<EFSMVariable> vars)
  {
    collectAllVariables(encoding, vars, vars);
  }

  /**
   * Collects all variables in the given event encoding.
   * @param  update   The event encoding to be searched.
   * @param  unprimed Unprimed variables will be added to this collection.
   *                  This may be <CODE>null</CODE> to suppress collecting
   *                  unprimed variables.
   * @param  primed   Primed variables will be added to this collection.
   *                  This may be <CODE>null</CODE> to suppress collecting
   *                  primed variables.
   */
  void collectAllVariables(final EFSMEventEncoding encoding,
                           final Collection<EFSMVariable> unprimed,
                           final Collection<EFSMVariable> primed)
  {
    for (int i = 0; i < encoding.size(); i++) {
      final ConstraintList update = encoding.getUpdate(i);
      collectAllVariables(update, unprimed, primed);
    }
  }
  
}
