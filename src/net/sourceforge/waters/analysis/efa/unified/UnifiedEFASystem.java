//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFASystem
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFASystem;


/**
 * A collection of transition relations and variables representing a
 * normalised EFSM system.
 *
 * <I>Reference:</I><BR>
 * Sahar Mohajerani, Robi Malik, Martin Fabian. An Algorithm for Compositional
 * Nonblocking Verification of Extended Finite-state Machines. Proc. 12th
 * International Workshop on Discrete Event Systems (WODES&nbsp;'14), 376-382,
 * Paris, France, 2014.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

class UnifiedEFASystem
  extends AbstractEFASystem<AbstractEFAEvent,
                            UnifiedEFAVariable,
                            UnifiedEFATransitionRelation,
                            UnifiedEFAVariableContext>
{

  //#########################################################################
  //# Constructors
  UnifiedEFASystem(final String name, final UnifiedEFAVariableContext context)
  {
    super(name, context);
    mUnifiedEvents = new ArrayList<>();
  }

  UnifiedEFASystem(final String name,
                   final UnifiedEFAVariableContext context,
                   final int size)
  {
    super(name, context, size);
    mUnifiedEvents = new ArrayList<>();
  }

  UnifiedEFASystem(final String name,
                   final List<UnifiedEFAVariable> variables,
                   final List<UnifiedEFATransitionRelation> transitionRelations,
                   final List<AbstractEFAEvent> unifiedEvents,
                   final UnifiedEFAVariableContext context)
  {
    super(name, variables, transitionRelations, context);
    mUnifiedEvents = unifiedEvents;
  }


  //#########################################################################
  //# Simple Access
  public List<AbstractEFAEvent> getEvents()
  {
    return mUnifiedEvents;
  }

  /**
   * Adds the given event to the EFA system. The new event is added
   * to the end of the event list without checking for duplicates.
   */
  public void addEvent(final UnifiedEFAEvent event)
  {
    mUnifiedEvents.add(event);
  }

  @Override
  public boolean addTransitionRelation(
   final UnifiedEFATransitionRelation transitionRelation)
  {
    return super.addTransitionRelation(transitionRelation);
  }

  @Override
  public void removeTransitionRelation(
   final UnifiedEFATransitionRelation transitionRelation)
  {
    super.removeTransitionRelation(transitionRelation);
  }

  @Override
  public List<UnifiedEFATransitionRelation> getTransitionRelations()
  {
    return super.getTransitionRelations();
  }

  @Override
  public void addVariable(final UnifiedEFAVariable variable)
  {
    super.addVariable(variable);
  }

  @Override
  public void removeVariable(final UnifiedEFAVariable var)
  {
    super.removeVariable(var);
  }

  //#########################################################################
  //# Data Members
  private final List<AbstractEFAEvent> mUnifiedEvents;

}
