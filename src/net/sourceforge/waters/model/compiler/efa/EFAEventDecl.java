//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAEventDecl
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
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
