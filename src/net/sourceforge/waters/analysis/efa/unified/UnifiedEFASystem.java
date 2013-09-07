//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   UnifiedEFASystem
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
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
    mUnifiedEvents = new THashSet<UnifiedEFAEvent>();
  }

  UnifiedEFASystem(final String name,
                   final UnifiedEFAVariableContext context,
                   final int size)
  {
    super(name, context, size);
    mUnifiedEvents = new THashSet<UnifiedEFAEvent>();
  }

  UnifiedEFASystem(final String name,
                   final List<UnifiedEFAVariable> variables,
                   final List<UnifiedEFATransitionRelation> transitionRelations,
                   final Collection<UnifiedEFAEvent> unifiedEvents,
                   final UnifiedEFAVariableContext context)
  {
    super(name, variables, transitionRelations, context);
    mUnifiedEvents = unifiedEvents;
  }


  //#########################################################################
  //# Simple Access
  public Collection<UnifiedEFAEvent> getEvents()
  {
    return mUnifiedEvents;
  }

  public void addEvent(final UnifiedEFAEvent event)
  {
    mUnifiedEvents.add(event);
  }

  public void removeEvent(final UnifiedEFAEvent event)
  {
    mUnifiedEvents.remove(event);
  }


  //#########################################################################
  //# Data Members
  private final Collection<UnifiedEFAEvent> mUnifiedEvents;

}
