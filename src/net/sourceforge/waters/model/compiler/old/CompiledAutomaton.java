//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   CompiledAutomaton
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.old;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
 * This is a simple MUTABLE implementation of the {@link AutomatonProxy}
 * interface.
 *
 * <STRONG>BUG.</STRONG> Not a proper an implementation of {@link
 * AutomatonProxy}, also the compiler must create everything through its
 * factory. Create your own data structure as needed, but do not pretend to
 * be {@link AutomatonProxy} ...
 *
 * @author Markus Sk&ouml;ldstam
 */

public final class CompiledAutomaton
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
   * @throws DuplicateNameException to indicate that some state or event
   *                      name is used more than once.
   * @throws NameNotFoundException to indicate that some transition refers
   *                      to a state or event with an unknown name.
   * @throws ItemNotFoundException to indicate that some transition uses
   *                      a state or event object that does not belong
   *                      to the given set of states or events.
   */
  CompiledAutomaton(String name,
                   ComponentKind kind,
                   Collection<? extends EventProxy> events,
                   Collection<? extends StateProxy> states,
                   Collection<? extends TransitionProxy> transitions)
  {
    super(name);
    EventSet eventscopy =
      events == null ? new EventSet() : new EventSet(events);
    StateSet statescopy =
      states == null ? new StateSet() : new StateSet(states);
    mKind = kind;
    mEvents = Collections.synchronizedSet(eventscopy);
    mStates = Collections.synchronizedSet(statescopy);
    if (transitions == null || transitions.isEmpty()) {
      // *** BUG ***
      mTransitions = Collections.emptyList();
    } else {
      List<TransitionProxy> transitionscopy =
        new ArrayList<TransitionProxy>(transitions.size());
      for (final TransitionProxy trans : transitions) {
        statescopy.checkUnique(trans.getSource());
        eventscopy.checkUnique(trans.getEvent());
        statescopy.checkUnique(trans.getTarget());
        transitionscopy.add(trans);
      }
      mTransitions = Collections.synchronizedList(transitionscopy);
    }
  }

  /**
   * Creates a new automaton using default values.
   * This constructor creates an automaton with empty lists of events,
   * states, and transitions.
   * @param  name         The name to be given to the new automaton.
   * @param  kind         The kind (<I>plant</I>, <I>specification</I>, etc.)
   *                      of the new automaton.
   */
  CompiledAutomaton(final String name,
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
  public CompiledAutomaton clone()
  {
    // *** BUG ***
    return (CompiledAutomaton) super.clone();
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
    // *** NOT unmodifiable ***
    return mEvents;
  }

  /**
   * Gets the set of states for this automaton.
   * @return  An unmodifiable set of objects of type {@link StateProxy}.
   */
  public Set<StateProxy> getStates()
  {
    // *** NOT unmodifiable ***
    return mStates;
  }

  /**
   * Gets the list of transitions for this automaton.
   * @return  An unmodifiable list of objects of type {@link TransitionProxy}.
   */
  public Collection<TransitionProxy> getTransitions()
  {
    // *** NOT unmodifiable ***
    return mTransitions;
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

  }

  //#########################################################################
  //# Setters 
  public void setKind(ComponentKind kind)
  {
    if (mKind== kind) {
      return;
    }
    mKind = kind;
  }
  
  public void setEvents(Set<EventProxy> events)
  {
    if (mEvents == events) {
      return;
    }
    mEvents = events;
  }
  
  public void addEvent(EventProxy event)
  {
    /*
     * If mEvents already contains an event with the same name an exception shall be generated.
     */
    mEvents.add(event);
  }

  public void removeEvent(String name) {
		for (EventProxy event : mEvents) {
			if (event.getName().equals(name)) {
				mEvents.remove(event);
				return;
			}
		}
	}
  
  
  public void setStates(Set<StateProxy> states)
  {
	  if (mStates == states) {
	      return;
	    }
    mStates=states;
  }
  public void addState(StateProxy state)
  {
	  /*
	   * If mStates already contains a state with the same name an exception shall be generated.
	   */
    mStates.add(state);
  }

  public void removeState(String name) {
		for (StateProxy state : mStates) {
			if (state.getName().equals(name)) {
				mStates.remove(state);
				return;
			}
		}
	}
  
  public void setTransitions(Collection <TransitionProxy> transitions)
  {
    mTransitions = transitions;
  }
  
  public void removeTransition(String source, String target, String event) {
		for (TransitionProxy trans : mTransitions) {
			if (trans.getSource().getName().equals(source)
					&& trans.getTarget().getName().equals(target)
					&& trans.getEvent().getName().equals(event)) {
				mTransitions.remove(trans);
				return;
			}
		}
	}
  
  public void addTransition(TransitionProxy transition)
  {
    mTransitions.add(transition);
  }
  

  //#########################################################################
  //# Data Members
  private ComponentKind mKind;
  private Set<EventProxy> mEvents;
  private Set<StateProxy> mStates;
  private Collection<TransitionProxy> mTransitions;

}
