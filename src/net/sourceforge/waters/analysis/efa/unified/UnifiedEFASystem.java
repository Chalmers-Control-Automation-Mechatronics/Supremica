//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   UnifiedEFASystem
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFASystem;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

class UnifiedEFASystem
  extends AbstractEFASystem<UnifiedEFAEvent,
                            UnifiedEFAVariable,
                            UnifiedEFATransitionRelation,
                            UnifiedEFAVariableContext>
{

  //#########################################################################
  //# Constructors
  UnifiedEFASystem(final String name, final UnifiedEFAVariableContext context)
  {
    super(name, context);
  }

  UnifiedEFASystem(final String name,
                   final UnifiedEFAVariableContext context,
                   final int size)
  {
    super(name, context, size);
  }

  UnifiedEFASystem(final String name,
                   final List<UnifiedEFAVariable> variables,
                   final List<UnifiedEFATransitionRelation> transitionRelations,
                   final UnifiedEFAVariableContext context)
  {
    super(name, variables, transitionRelations, context);
  }

}
