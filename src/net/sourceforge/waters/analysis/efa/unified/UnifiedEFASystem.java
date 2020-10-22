//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFASystem;


/**
 * <P>A collection of transition relations and variables representing a
 * normalised EFSM system.</P>
 *
 * <P><I>Reference:</I><BR>
 * Sahar Mohajerani, Robi Malik, Martin Fabian. An Algorithm for Compositional
 * Nonblocking Verification of Extended Finite-state Machines. Proc. 12th
 * International Workshop on Discrete Event Systems (WODES&nbsp;'14),
 * 376&ndash;382, Paris, France, 2014.</P>
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
