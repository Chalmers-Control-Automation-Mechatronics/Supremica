//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   EFSMSystem
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.util.List;

import net.sourceforge.waters.analysis.efa.AbstractEFASystem;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

class EFSMSystem
  extends AbstractEFASystem<ConstraintList,
                            EFSMVariable,
                            EFSMTransitionRelation,
                            EFSMVariableContext>
{

  //#########################################################################
  //# Constructors
  EFSMSystem(final String name, final EFSMVariableContext context)
  {
    super(name, context);
  }

  EFSMSystem(final String name,
             final EFSMVariableContext context,
             final int size)
  {
    super(name, context, size);
  }

  EFSMSystem(final String name,
             final List<EFSMVariable> variables,
             final List<EFSMTransitionRelation> transitionRelations,
             final EFSMVariableContext context)
  {
    super(name, variables, transitionRelations, context);
  }

}
