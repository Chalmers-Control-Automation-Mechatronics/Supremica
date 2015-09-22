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

package net.sourceforge.waters.plain.des;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.ImmutableOrderedSet;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.plain.base.NamedElement;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * A finite-state machine.
 * This is a simple immutable implementation of the {@link AutomatonProxy}
 * interface.
 *
 * @author Robi Malik
 */

public final class AutomatonElement
  extends NamedElement
  implements AutomatonProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new automaton.
   * @param  name         The name to be given to the new automaton.
   * @param  kind         The kind (<I>plant</I>, <I>specification</I>, etc.)
   *                      of the new automaton.
   * @param  events       The event alphabet for the new automaton,
   *                      or <CODE>null</CODE> if empty.
   * @param  states       The state set for the new automaton,
   *                      or <CODE>null</CODE> if empty.
   * @param  transitions  The list of transitions for the new automaton,
   *                      or <CODE>null</CODE> if empty.
   * @param  attribs      The attribute map for the new automaton,
   *                      or <CODE>null</CODE> if empty.
   * @throws DuplicateNameException to indicate that some state or event
   *                      name is used more than once.
   */
  AutomatonElement(final String name,
                   final ComponentKind kind,
                   final Collection<? extends EventProxy> events,
                   final Collection<? extends StateProxy> states,
                   final Collection<? extends TransitionProxy> transitions,
                   final Map<String,String> attribs)
  {
    super(name);
    mKind = kind;
    mEvents = new EventSet(events);
    mStates = new StateSet(states);
    if (transitions == null || transitions.isEmpty()) {
      mTransitions = Collections.emptyList();
    } else {
      final List<TransitionProxy> transitionscopy =
        new ArrayList<TransitionProxy>(transitions);
      mTransitions = Collections.unmodifiableList(transitionscopy);
    }
    if (attribs == null) {
      mAttributes = Collections.emptyMap();
    } else {
      final Map<String,String> attribscopy =
        new TreeMap<String,String>(attribs);
      mAttributes = Collections.unmodifiableMap(attribscopy);
    }
  }

  /**
   * Creates a new automaton without attributes.
   * @param  name         The name to be given to the new automaton.
   * @param  kind         The kind (<I>plant</I>, <I>specification</I>, etc.)
   *                      of the new automaton.
   * @param  events       The event alphabet for the new automaton,
   *                      or <CODE>null</CODE> if empty.
   * @param  states       The state set for the new automaton,
   *                      or <CODE>null</CODE> if empty.
   * @param  transitions  The list of transitions for the new automaton,
   *                      or <CODE>null</CODE> if empty.
   */
  AutomatonElement(final String name,
                   final ComponentKind kind,
                   final Collection<? extends EventProxy> events,
                   final Collection<? extends StateProxy> states,
                   final Collection<? extends TransitionProxy> transitions)
  {
    this(name, kind, events, states, transitions, null);
  }

  /**
   * Creates a new automaton using default values.
   * This constructor creates an automaton with empty lists of events,
   * states, and transitions.
   * @param  name         The name to be given to the new automaton.
   * @param  kind         The kind (<I>plant</I>, <I>specification</I>, etc.)
   *                      of the new automaton.
   */
  AutomatonElement(final String name,
                   final ComponentKind kind)
  {
    this(name,
         kind,
         emptyEventProxyList(),
         emptyStateProxyList(),
         emptyTransitionProxyList());
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  public AutomatonElement clone()
  {
    return (AutomatonElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitAutomatonProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.AutomatonProxy
  /**
   * Gets the kind (<I>plant</I>, <I>specification</I>, etc.) of this
   * automaton.
   */
  public ComponentKind getKind()
  {
    return mKind;
  }

  /**
   * Gets the event alphabet for this automaton.
   * This method returns the set of events on which this automaton
   * synchronises, or the set of all events that can occur on its
   * transitions.
   * @return  An unmodifiable set of objects of type {@link EventProxy}.
   */
  public Set<EventProxy> getEvents()
  {
    return mEvents;
  }

  /**
   * Gets the set of states for this automaton.
   * @return  An unmodifiable set of objects of type {@link StateProxy}.
   */
  public Set<StateProxy> getStates()
  {
    return mStates;
  }

  /**
   * Gets the list of transitions for this automaton.
   * @return  An unmodifiable list of objects of type {@link TransitionProxy}.
   */
  public Collection<TransitionProxy> getTransitions()
  {
    return mTransitions;
  }

  public Map<String,String> getAttributes()
  {
    return mAttributes;
  }


  //#########################################################################
  //# Equals and Hashcode
  public Class<AutomatonProxy> getProxyInterface()
  {
    return AutomatonProxy.class;
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<EventProxy> emptyEventProxyList()
  {
    return Collections.emptyList();
  }

  private static List<StateProxy> emptyStateProxyList()
  {
    return Collections.emptyList();
  }

  private static List<TransitionProxy> emptyTransitionProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Local Class EventSet
  private class EventSet extends ImmutableOrderedSet<EventProxy>
  {

    //#######################################################################
    //# Constructor
    EventSet(final Collection<? extends EventProxy> events)
    {
      super(events);
    }

    //#######################################################################
    //# Overrides from base class ImmutableOrderedSet
    protected DuplicateNameException createDuplicateName(final String name)
    {
      return new DuplicateNameException
        ("Automaton '" + getName() +
         "' already contains an event named '" + name + "'!");
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Local Class StateSet
  private class StateSet extends ImmutableOrderedSet<StateProxy>
  {

    //#######################################################################
    //# Constructor
    StateSet(final Collection<? extends StateProxy> states)
      throws DuplicateNameException
    {
      super(states);
    }

    //#######################################################################
    //# Overrides from base class ImmutableOrderedSet
    protected DuplicateNameException createDuplicateName(final String name)
    {
      return new DuplicateNameException
        ("Automaton '" + getName() +
         "' already contains a state named '" + name + "'!");
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Data Members
  private final ComponentKind mKind;
  private final Set<EventProxy> mEvents;
  private final Set<StateProxy> mStates;
  private final Collection<TransitionProxy> mTransitions;
  private final Map<String,String> mAttributes;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}








