//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   AutomatonElement
//###########################################################################
//# $Id$
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
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.IndexedHashSet;
import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.Proxy;
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
   * @param  attribs      The attribute map for the new automaton.
   * @throws DuplicateNameException to indicate that some state or event
   *                      name is used more than once.
   * @throws NameNotFoundException to indicate that some transition refers
   *                      to a state or event with an unknown name.
   * @throws ItemNotFoundException to indicate that some transition uses
   *                      a state or event object that does not belong
   *                      to the given set of states or events.
   */
  AutomatonElement(final String name,
                   final ComponentKind kind,
                   final Collection<? extends EventProxy> events,
                   final Collection<? extends StateProxy> states,
                   final Collection<? extends TransitionProxy> transitions,
                   final Map<String,String> attribs)
  {
    super(name);
    final EventSet eventscopy =
      events == null ? new EventSet() : new EventSet(events);
    final StateSet statescopy =
      states == null ? new StateSet() : new StateSet(states);
    mKind = kind;
    mEvents = Collections.unmodifiableSet(eventscopy);
    mStates = Collections.unmodifiableSet(statescopy);
    if (transitions == null || transitions.isEmpty()) {
      mTransitions = Collections.emptyList();
    } else {
      final List<TransitionProxy> transitionscopy =
        new ArrayList<TransitionProxy>(transitions.size());
      for (final TransitionProxy trans : transitions) {
        statescopy.checkUnique(trans.getSource());
        eventscopy.checkUnique(trans.getEvent());
        statescopy.checkUnique(trans.getTarget());
        transitionscopy.add(trans);
      }
      mTransitions = Collections.unmodifiableList(transitionscopy);
    }
    if (attribs == null) {
      mAttributes = null;
    } else {
      final Map<String,String> attribscopy = new TreeMap<String,String>(attribs);
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

  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final AutomatonProxy aut = (AutomatonProxy) partner;
      return
        mKind.equals(aut.getKind()) &&
        ProxyTools.isEqualSetByContents(mEvents, aut.getEvents()) &&
        ProxyTools.isEqualSetByContents(mStates, aut.getStates()) &&
        ProxyTools.isEqualSetByContents(mTransitions, aut.getTransitions());
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mKind.hashCode();
    result *= 5;
    result += ProxyTools.getSetHashCodeByContents(mEvents);
    result *= 5;
    result += ProxyTools.getSetHashCodeByContents(mStates);
    result *= 5;
    result += ProxyTools.getSetHashCodeByContents(mTransitions);
    return result;
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
  private class EventSet extends IndexedHashSet<EventProxy> {

    //#######################################################################
    //# Constructors
    EventSet()
    {
    }

    EventSet(final Collection<? extends EventProxy> events)
      throws DuplicateNameException
    {
      super(events);
    }

    //#######################################################################
    //# Overrides from abstract class HashSetProxy
    protected ItemNotFoundException createItemNotFound(final String name)
    {
      return new ItemNotFoundException
        ("Automaton '" + getName() +
         "' does not contain the event named '" + name + "'!");
    }

    protected NameNotFoundException createNameNotFound(final String name)
    {
      return new NameNotFoundException
        ("Automaton '" + getName() +
         "' does not contain an event named '" + name + "'!");
    }

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
  private class StateSet extends IndexedHashSet<StateProxy> {

    //#######################################################################
    //# Constructors
    StateSet()
    {
    }

    StateSet(final Collection<? extends StateProxy> states)
      throws DuplicateNameException
    {
      super(states);
    }

    //#######################################################################
    //# Overrides from abstract class HashSetProxy
    protected ItemNotFoundException createItemNotFound(final String name)
    {
      return new ItemNotFoundException
        ("Automaton '" + getName() +
         "' does not contain the state named '" + name + "'!");
    }

    protected NameNotFoundException createNameNotFound(final String name)
    {
      return new NameNotFoundException
        ("Automaton '" + getName() +
         "' does not contain a state named '" + name + "'!");
    }

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
