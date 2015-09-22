//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.model.compiler.efa;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A compiler-internal representation of an event group in a system
 * of synchronised EFA.</P>
 *
 * <P>Each compiled event represents a single event declaration ({@link
 * EventDeclProxy}) of the input module, which has been flattened so as not
 * to allow any further array indexes. EFA compilation may split this
 * event, creating individual events of type {@link EFAEvent} for different
 * value combinations of EFA variable components.</P>
 *
 * @see {@link EFACompiler}.
 * @author Robi Malik
 */

class EFAEventDecl {

  //#########################################################################
  //# Constructors
  EFAEventDecl(final EventDeclProxy decl)
  {
    mEventDecl = decl;
    mVariables = new HashSet<EFAVariable>();
    mTransitionMap =
      new HashMap<SimpleComponentProxy,EFAAutomatonTransitionGroup>();
    mEvents = new LinkedList<EFAEvent>();
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    return mEventDecl.getName();
  }


  //#########################################################################
  //# Simple Access
  EventDeclProxy getEventDecl()
  {
    return mEventDecl;
  }

  EventKind getKind()
  {
    return mEventDecl.getKind();
  }

  boolean isObservable()
  {
    return mEventDecl.isObservable();
  }

  Map<String,String> getAttributes()
  {
    return mEventDecl.getAttributes();
  }

  boolean isBlocked()
  {
    return mIsBlocked;
  }

  void setBlocked()
  {
    mVariables.clear();
    mTransitionMap.clear();
    mEvents.clear();
    mIsBlocked = true;
  }

  EFAAutomatonTransitionGroup getTransitionGroup
    (final SimpleComponentProxy comp)
  {
    return mTransitionMap.get(comp);
  }

  EFAAutomatonTransitionGroup createTransitionGroup
    (final SimpleComponentProxy comp)
  {
    EFAAutomatonTransitionGroup trans = mTransitionMap.get(comp);
    if (trans == null) {
      trans = new EFAAutomatonTransitionGroup(comp);
      mTransitionMap.put(comp, trans);
    }
    return trans;
  }

  Collection<EFAAutomatonTransitionGroup> getTransitionGroups()
  {
    return mTransitionMap.values();
  }

  Collection<EFAVariable> getVariables()
  {
    return mVariables;
  }

  boolean isEventVariable(final EFAVariable var)
  {
    return mVariables.contains(var);
  }

  void addVariable(final EFAVariable var)
  {
    mVariables.add(var);
  }

  void addVariables(final Collection<EFAVariable> vars)
  {
    mVariables.addAll(vars);
  }

  void addEvent(final EFAEvent event)
  {
    mEvents.add(event);
  }

  Collection<EFAEvent> getEvents()
  {
    return mEvents;
  }


  //#########################################################################
  //# Data Members
  /**
   * The event declaration in the input module,
   * from which this event is being compiled.
   */
  private final EventDeclProxy mEventDecl;
  /**
   * The <I>event variable set</I>, consisting of all the variables
   * whose value may change when this event occurs. This set contains
   * only the EFA variable objects for the current state of the
   * concerned variables.
   * @see {@link EFACompiler}.
   */
  private final Set<EFAVariable> mVariables;
  /**
   * The map that assigns to each automaton ({@link SimpleComponentProxy})
   * the collection of transitions of this event that are to be associated
   * with it. Automata that do not have the event in their alphabet are
   * not listed. Automata that block the event are listed with an empty
   * transition group.
   */
  private final Map<SimpleComponentProxy,EFAAutomatonTransitionGroup>
    mTransitionMap;
  /**
   * The set of individual events to be generated from this event group. For
   * each guard condition, representing a set of possible combination of
   * variable values, an event may be generated.
   */
  private final Collection<EFAEvent> mEvents;
  /**
   * A flag indicating that the event has been recognised as globally blocked.
   * In some cases, the EFA compiler can identify that an event is globally
   * disabled and can never cause a violation of a safety property. Such
   * an event is marked as blocked, and no transitions are generated for it.
   */
  private boolean mIsBlocked;
}








