//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.des
//# CLASS:   AutomatonProxy
//###########################################################################
//# $Id: AutomatonProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.HashSetProxy;
import net.sourceforge.waters.model.base.ImmutableNamedProxy;
import net.sourceforge.waters.model.base.ListProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.model.base.TopLevelListProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.des.AutomataListType;
import net.sourceforge.waters.xsd.des.AutomatonType;
import net.sourceforge.waters.xsd.des.EventRefListType;
import net.sourceforge.waters.xsd.des.StateListType;
import net.sourceforge.waters.xsd.des.TransitionListType;


/**
 * <P>A finite-state machine.</P>
 *
 * <P>This class is a straightforward representation of a finite-state
 * machine as part of a discrete-event system, consisting of an event
 * alphabet, a list of states, and a list transitions.</P>
 *
 * <P>The event alphabet contains all the events that are used for
 * synchronisation between this and other automata. Events not contained in
 * the event alphabet are assumed to be selflooped in all states of this
 * automaton.</P>
 *
 * <P>In addition, finite-state machines in a discrete-event systems
 * context can be classified as <I>plant</I> or <I>specification</I>.
 * This is information is also associated with each automaton.</P>
 *
 * @author Robi Malik
 */

public class AutomatonProxy extends ImmutableNamedProxy {

  //#########################################################################
  //# Constructors
  public AutomatonProxy(final String name,
			final ComponentKind kind)
  {
    this(name, kind, null);
  }

  public AutomatonProxy(final String name,
			final ComponentKind kind,
			final EventLookupFactory eventfactory)
  {
    super(name);
    mKind = kind;
    mEventFactory = eventfactory;
    mEventRefSetProxy = new EventRefSetProxy();
    mStateSetProxy = new StateSetProxy();
    mTransitionListProxy = new TransitionListProxy();
  }

  AutomatonProxy(final AutomatonType aut,
		 final EventLookupFactory eventfactory)
    throws ModelException
  {
    super(aut);
    mKind = aut.getKind();
    mEventFactory = eventfactory;
    mEventRefSetProxy = new EventRefSetProxy(aut, eventfactory);
    final EventLookupFactory localeventfactory =
      new EventLookupFactory(mEventRefSetProxy);
    mStateSetProxy = new StateSetProxy(aut, localeventfactory);
    final StateLookupFactory localstatefactory =
      new StateLookupFactory(mStateSetProxy);
    mTransitionListProxy =
      new TransitionListProxy(aut, localeventfactory, localstatefactory);
  }


  //#########################################################################
  //# Getters and Setters
  public ComponentKind getKind()
  {
    return mKind;
  }

  public void setKind(final ComponentKind kind)
  {
    mKind = kind;
  }

  /**
   * Get the set of events for this automaton.
   * @return  An unmodifiable set of objects of type {@link EventProxy}.
   */
  public Set getEvents()
  {
    return Collections.unmodifiableSet(mEventRefSetProxy);
  }

  /**
   * Get the set of states for this automaton.
   * @return  An unmodifiable set of objects of type {@link StateProxy}.
   */
  public Set getStates()
  {
    return Collections.unmodifiableSet(mStateSetProxy);
  }

  /**
   * Get the list of transitions for this automaton.
   * @return  An unmodifiable list of objects of type {@link TransitionProxy}.
   */
  public List getTransitions()
  {
    return Collections.unmodifiableList(mTransitionListProxy);
  }

  /**
   * Add an event to this automaton.
   * This method makes sure that the automaton's event set includes
   * the given event, by adding it if not already present.
   * @param  event  The event to be added.
   * @throws DuplicateNameException to indicate that the automaton already
   *                has an event with the same name.
   */
  public void addEvent(final EventProxy event)
    throws DuplicateNameException
  {
    verifyGlobalEvent(event);
    mEventRefSetProxy.insert(event);
  }

  /**
   * Add a state to this automaton.
   * Before adding, it makes sure that the all the state's propositions
   * are included in the event set, by adding them if necessary.
   * @param  state  The state to be added.
   * @return The item that is now contained in the collection. This may be
   *         the given item or another one that is equal and was there
   *         before.
   * @throws DuplicateNameException to indicate that the automaton already
   *                has a state or an event with the same name.
   */
  public StateProxy addState(final StateProxy state)
    throws DuplicateNameException
  {
    final Iterator iter = state.getPropositions().iterator();
    while (iter.hasNext()) {
      final EventProxy event = (EventProxy) iter.next();
      addEvent(event);
    }
    return (StateProxy) mStateSetProxy.insert(state);
  }

  /**
   * Add a transition to this automaton.
   * This method appends the given transition to the automaton's transition
   * list. Before adding, it makes sure that the transition's event is
   * included in the event set, by adding it if necessary.
   * @param  trans  The transition to be added.
   * @throws DuplicateNameException to indicate that the automaton already
   *                has an event with the same name.
   * @throws IllegalArgumentException to indicate that the transition's
   *                source or target state has not been added to the
   *                automaton's state list.
   */
  public void addTransition(final TransitionProxy trans)
    throws DuplicateNameException
  {
    if (!mStateSetProxy.contains(trans.getSource())) {
      throw new IllegalArgumentException
	("Source state '" + trans.getSource().getName() +
	 "' must be added to automaton '" + getName() +
	 "' before inserting transition!");
    }
    if (!mStateSetProxy.contains(trans.getTarget())) {
      throw new IllegalArgumentException
	("Target state '" + trans.getTarget().getName() +
	 "' must be added to automaton '" + getName() +
	 "' before inserting transition!");
    }
    addEvent(trans.getEvent());
    mTransitionListProxy.add(trans);
  }

  /**
   * Find a state with given name.
   * @param  name   The name of the state to be looked for.
   * @return The corresponding state of the automaton.
   * @throws NameNotFoundException to indicate that the automaton
   *                does not have any state with the given name.
   */
  public StateProxy findState(final String name)
    throws NameNotFoundException
  {
    return (StateProxy) mStateSetProxy.find(name);
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final AutomatonProxy aut = (AutomatonProxy) partner;
      return
	getKind().equals(aut.getKind()) &&
	mEventRefSetProxy.equals(aut.mEventRefSetProxy) &&
	mStateSetProxy.equals(aut.mStateSetProxy) &&
	equalTransitionSets(mTransitionListProxy, aut.mTransitionListProxy);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    final ComponentKind kind = getKind();
    final String kindname = kind.toString();
    final String lowername = kindname.toLowerCase();
    printer.print(lowername);
    printer.print(' ');
    printer.print(getName());
    printer.println(" {");
    printer.indentIn();
    mEventRefSetProxy.pprint(printer);
    mStateSetProxy.pprint(printer);
    mTransitionListProxy.pprint(printer);
    printer.indentOut();
    printer.print('}');
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final AutomatonType aut = (AutomatonType) element;
    aut.setKind(mKind);
    final ElementFactory eventfactory =
      new EventProxy.EventRefElementFactory();
    final EventRefListType eventset =
      (EventRefListType) mEventRefSetProxy.toJAXB(eventfactory);
    aut.setEventRefList(eventset);
    final ElementFactory statefactory =
      new StateProxy.StateElementFactory();
    final StateListType stateset =
      (StateListType) mStateSetProxy.toJAXB(statefactory);
    aut.setStateList(stateset);
    final ElementFactory transfactory =
      new TransitionProxy.TransitionElementFactory();
    final TransitionListType translist =
      (TransitionListType) mTransitionListProxy.toJAXB(transfactory);
    aut.setTransitionList(translist);
  }


  //#########################################################################
  //# Checking Events Globally
  void setEventFactory(final EventLookupFactory factory)
  {
    final EventLookupFactory backup = mEventFactory;
    mEventFactory = factory;
    try {
      verifyGlobalEvents(mEventRefSetProxy);
    } catch (final RuntimeException exception) {
      mEventFactory = backup;
      throw exception;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void verifyGlobalEvents(final Collection collection)
  {
    if (mEventFactory != null) {
      final Iterator iter = collection.iterator();
      while (iter.hasNext()) {
	final EventProxy event = (EventProxy) iter.next();
	verifyGlobalEvent(event);
      }
    }
  }

  private void verifyGlobalEvent(final EventProxy event)
  {
    if (mEventFactory != null) {
      if (!mEventFactory.contains(event)) {
	throw new IllegalArgumentException
	  ("Event '" + event.getName() +
	   "' must be added to global list before usage in automaton '"
	   + getName() + "'!");
      }
    }
  }

  private static boolean equalTransitionSets(final List list1,
					     final List list2)
  {
    if (list1.size() == list2.size()) {
      final Set set1 = new HashSet(list1);
      return set1.containsAll(list2);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Local Class EventRefSetProxy
  private class EventRefSetProxy extends HashSetProxy {

    //#######################################################################
    //# Constructor
    EventRefSetProxy()
    {
      super(false);
    }

    EventRefSetProxy(final AutomatonType aut, final ProxyFactory eventfactory)
      throws ModelException
    {
      super(aut.getEventRefList(), eventfactory);
    }

    //#######################################################################
    //# Overrides from abstract class HashSetProxy
    protected String getPPrintName()
    {
      return "EVENTS";
    }

    protected NameNotFoundException createNameNotFound(final String name)
    {
      return new NameNotFoundException
	("Automaton '" + getName() +
	 "' does not have an event named '" + name + "'!");
    }
    
    protected DuplicateNameException createDuplicateName(final String name)
    {
      return new DuplicateNameException
	("Automaton '" + getName() +
	 "' already contains an event named '" + name + "'!");
    }
  
  }


  //#########################################################################
  //# Local Class StateSetProxy
  private class StateSetProxy extends HashSetProxy {

    //#######################################################################
    //# Constructor
    StateSetProxy()
    {
    }

    StateSetProxy(final AutomatonType aut, final ProxyFactory eventfactory)
      throws ModelException
    {
      super(aut.getStateList(),
	    new StateProxy.StateProxyFactory(eventfactory));
    }

    //#######################################################################
    //# Overrides from abstract class HashSetProxy
    protected String getPPrintName()
    {
      return "STATES";
    }
  
    protected NameNotFoundException createNameNotFound(final String name)
    {
      return new NameNotFoundException
	("Automaton '" + getName() +
	 "' does not have a state named '" + name + "'!");
    }
    
    protected DuplicateNameException createDuplicateName(final String name)
    {
      return new DuplicateNameException
	("Automaton '" + getName() +
	 "' already contains a state named '" + name + "'!");
    }

  }


  //#########################################################################
  //# Local Class TransitionListProxy
  private static class TransitionListProxy extends TopLevelListProxy {

    //#######################################################################
    //# Constructor
    TransitionListProxy()
    {
    }

    TransitionListProxy(final AutomatonType aut,
			final EventLookupFactory eventfactory,
			final StateLookupFactory statefactory)
      throws ModelException
    {
      super(aut.getTransitionList(),
	    new TransitionProxy.TransitionProxyFactory
	          (eventfactory, statefactory));
    }

    //#######################################################################
    //# Overrides from abstract class TopLevelListProxy
    protected String getPPrintName()
    {
      return "TRANSITIONS";
    }

  }


  //#########################################################################
  //# Local Class AutomatonProxyFactory
  static class AutomatonProxyFactory implements ProxyFactory
  {

    //#######################################################################
    //# Constructor
    AutomatonProxyFactory(final EventLookupFactory eventfactory)
    {
      mEventFactory = eventfactory;
    }

    //#######################################################################
    //# Interface waters.model.base.ProxyFactory
    public Proxy createProxy(final ElementType element)
      throws ModelException
    {
      final AutomatonType aut = (AutomatonType) element;
      return new AutomatonProxy(aut, mEventFactory);
    }
    
    public List getList(final ElementType parent)
    {
      final AutomataListType list = (AutomataListType) parent;
      return list.getList();
    }

    //#######################################################################
    //# Data Members
    private final EventLookupFactory mEventFactory;

  }


  //#########################################################################
  //# Local Class AutomatonElementFactory
  static class AutomatonElementFactory extends DESElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createAutomaton();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      return getFactory().createAutomataList();
    }

    public List getElementList(final ElementType container)
    {
      final AutomataListType list = (AutomataListType) container;
      return list.getList();
    }

  }


  //#########################################################################
  //# Data Members
  private ComponentKind mKind;
  private EventLookupFactory mEventFactory;
  private final HashSetProxy mEventRefSetProxy;
  private final HashSetProxy mStateSetProxy;
  private final ListProxy mTransitionListProxy;

}
