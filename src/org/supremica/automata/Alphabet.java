
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this oftware.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata;

import java.util.*;
import org.supremica.log.*;

/**
 * Similar to Events, but allows for the fast lookup of an event based on the id.
 *
 * Important note:
 * If an event label is changed after it is inserted in
 * an Events object, then rehash must be called otherwise
 * strange errors will arise.
 *
 *@author  ka
 *@created  November 28, 2001
 *@see  org.supremica.automata.AlphabetHelpers
 */
public class Alphabet
{
	private static Logger logger = LoggerFactory.createLogger(Alphabet.class);
	private int idIndex = 0;
	private Listeners listeners = null;
	private TreeMap theEvents = null;

	public Alphabet()
	{
		theEvents = new TreeMap();
	}

	public Alphabet(Alphabet orgAlphabet)
	{
		this();

		for (Iterator it = orgAlphabet.iterator(); it.hasNext(); )
		{
			LabeledEvent newEvent = new LabeledEvent((LabeledEvent) it.next());

			theEvents.put(newEvent.getLabel(), newEvent);
		}

		idIndex = orgAlphabet.idIndex;

		rehash();
	}

	/**
	 * Return the number of events.
	 *
	 *@return number of events
	 */
	public int size()
	{
		return theEvents.size();
	}

	/**
	 * Return the number of events.
	 *
	 *@return number of events
	 */
	public int nbrOfEvents()
	{
		return size();
	}

	public void setEvents(TreeMap oldEvents)
	{
		theEvents = new TreeMap(oldEvents);
	}

	public Map getEvents()
	{
		return theEvents;
	}

	/**
	 * Return an iterator to the events.
	 *
	 *@return  An iterator
	 */
	public EventIterator iterator()
	{
		return new EventIterator(theEvents.values().iterator());
	}

	/**
	 * Return an iterator to the controllable events.
	 *
	 *@return  An iterator
	 */
	public EventIterator controllableEventIterator()
	{
		return new EventIterator(new ControllableEventIterator(theEvents.values().iterator(), true));
	}

	/**
	 * Return an iterator to the uncontrollable events.
	 *
	 *@return  An iterator
	 */
	public EventIterator uncontrollableEventIterator()
	{
		return new EventIterator(new ControllableEventIterator(theEvents.values().iterator(), false));
	}

	/**
	 * Add an event.
	 */
	public void addEvent(LabeledEvent ev)
		throws IllegalArgumentException
	{
		if (ev == null)
		{
			throw new IllegalArgumentException("addEvent: event mist be non-null");
		}

		if (ev.getLabel() == null)
		{
			throw new IllegalArgumentException("addEvent: event label mist be non-null");
		}

		if (theEvents.containsKey(ev.getLabel()))
		{
			throw new IllegalArgumentException("getEvent: event is already in the alphabet ");
		}

		theEvents.put(ev.getLabel(), ev);
	}

	/**
	 * Given an event, returns an "equal" event from this alphabet
	 * The def of "equal" is an internal matter.
	 * Use this method instead of fiddling with event ids in user code
	 * Return null if the event does not exist
	 */
	public LabeledEvent getEvent(LabeledEvent ev)
		throws IllegalArgumentException
	{
		if (ev == null)
		{
			throw new IllegalArgumentException("getEvent: event mist be non-null");
		}

		if (ev.getLabel() == null)
		{
			throw new IllegalArgumentException("getEvent: event label mist be non-null");
		}

		return (LabeledEvent) theEvents.get(ev.getLabel());
	}

	/**
	 * Adds all events in another Events to this Events.
	 * Makes sure they are not already included!
	 */
	public void addEvents(Alphabet otherEvents)
	{
		for (EventIterator eventIt = otherEvents.iterator();
				eventIt.hasNext(); )
		{
			LabeledEvent currEvent = eventIt.nextEvent();

			if (!contains(currEvent))
			{
				addEvent(currEvent);
			}
		}
	}

	public void removeEvent(LabeledEvent ev)
		throws IllegalArgumentException
	{
		if (!includes(ev))
		{
			throw new IllegalArgumentException("The event is not included in this alphabet");
		}

		removeEvent(ev.getLabel());
	}

	public void removeEvent(String label)
		throws IllegalArgumentException
	{
		if (!contains(label))
		{
			throw new IllegalArgumentException("The event is not included in this alphabet");
		}

		theEvents.remove(label);
	}

	/**
	 * True, if there exists an event with the same label, false otherwise.
	 *
	 *@param  event The event with an interesting label
	 */
	public boolean contains(LabeledEvent event)
		throws IllegalArgumentException
	{
		return contains(event.getLabel());
	}

	/**
	 * True, if it exists an event with the label, false otherwise.
	 *
	 *@param  label The label of interest
	 */
	public boolean contains(String label)
		throws IllegalArgumentException
	{
		if (label == null)
		{
			throw new IllegalArgumentException("Event label must be non-null");
		}

		return theEvents.containsKey(label);
	}

	/**
	 * Return the event with the given label.
	 * Throw an exception if it does not exist.
	 *
	 *@param  label Description of the Parameter
	 *@return  The eventWithLabel value
	 *@exception  Exception Description of the Exception
	 */
	public LabeledEvent getEvent(String label)
		throws IllegalArgumentException
	{
		if (label == null)
		{
			throw new IllegalArgumentException("Event label must be non-null");
		}

		return (LabeledEvent) theEvents.get(label);
	}

	public LabeledEvent getEventWithIndex(int index)
		throws IllegalArgumentException
	{
		Iterator eventIt = iterator();

		while (eventIt.hasNext())
		{
			LabeledEvent currEvent = (LabeledEvent) eventIt.next();

			if (currEvent.getSynchIndex() == index)
			{
				return currEvent;
			}
		}

		throw new IllegalArgumentException("No event with index '" + index + "' exists");
	}

	public int nbrOfControllableEvents()
	{
		int nbrOfFoundEvents = 0;

		for (EventIterator evIt = iterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = evIt.nextEvent();

			if (currEvent.isControllable())
			{
				nbrOfFoundEvents++;
			}
		}

		return nbrOfFoundEvents;
	}

	public int nbrOfUncontrollableEvents()
	{
		int nbrOfFoundEvents = 0;

		for (EventIterator evIt = iterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = evIt.nextEvent();

			if (!currEvent.isControllable())
			{
				nbrOfFoundEvents++;
			}
		}

		return nbrOfFoundEvents;
	}

	/**
	 * Returns the number of unobservable events, epsilon events
	 * are assumed to be unobservable
	 */
	public int nbrOfUnobservableEvents()
	{
		int nbrOfFoundEvents = 0;

		for (EventIterator evIt = iterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = evIt.nextEvent();

			if (!currEvent.isObservable() || currEvent.isEpsilon())
			{
				nbrOfFoundEvents++;
			}
		}

		return nbrOfFoundEvents;
	}

	public int nbrOfPrioritizedEvents()
	{
		int nbrOfFoundEvents = 0;

		for (EventIterator evIt = iterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = evIt.nextEvent();

			if (currEvent.isPrioritized())
			{
				nbrOfFoundEvents++;
			}
		}

		return nbrOfFoundEvents;
	}

	public int nbrOfImmediateEvents()
	{
		int nbrOfFoundEvents = 0;

		for (EventIterator evIt = iterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = evIt.nextEvent();

			if (currEvent.isImmediate())
			{
				nbrOfFoundEvents++;
			}
		}

		return nbrOfFoundEvents;
	}

	public int nbrOfEpsilonEvents()
	{
		int nbrOfFoundEvents = 0;

		for (EventIterator evIt = iterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = evIt.nextEvent();

			if (currEvent.isEpsilon())
			{
				nbrOfFoundEvents++;
			}
		}

		return nbrOfFoundEvents;
	}

	/**
	 * True, if the event is in the set already, false otherwise.
	 *
	 *@param  theEvent Description of the Parameter
	 *@return  Description of the Return Value
	 */
	public boolean includes(LabeledEvent theEvent)
	{
		return theEvents.containsValue(theEvent);
	}

	public String toDebugString()
	{
		StringBuffer tmpBuf = new StringBuffer(theEvents.toString());

		return tmpBuf.toString();
	}

	public String toString()
	{
		StringBuffer sbuf = new StringBuffer("{");

		if (size() > 0)
		{
			for (EventIterator it = iterator(); it.hasNext(); )
			{
				LabeledEvent event = it.nextEvent();

				sbuf.append(event);

				if (it.hasNext())
				{
					sbuf.append(", ");
				}
			}

			sbuf.append("}");
		}

		return sbuf.toString();
	}

	/**
	 * Returns collection of the events..
	 *
	 *@return  Description of the Return Value
	 */
	public Collection values()
	{
		return theEvents.values();
	}

	/** Must be called after an event label is modified. */
	public void rehash()
	{
		TreeMap newEvents = new TreeMap();

		// theEvents = new TreeMap(orgEvents.theEvents);
		// Deep copy
		for (Iterator it = iterator(); it.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) it.next();

			newEvents.put(currEvent.getLabel(), currEvent);
		}

		theEvents.clear();

		theEvents = newEvents;
	}

	class ControllableEventIterator
		implements Iterator
	{
		private final Iterator theIterator;
		private final boolean controllableEvents;
		private Object nextEvent = null;

		public ControllableEventIterator(Iterator theIterator, boolean controllableEvents)
		{
			this.theIterator = theIterator;
			this.controllableEvents = controllableEvents;

			findNextEvent();
		}

		public boolean hasNext()
		{
			return nextEvent != null;
		}

		public Object next()
			throws NoSuchElementException
		{
			if (nextEvent != null)
			{
				Object oldEvent = nextEvent;

				findNextEvent();

				return oldEvent;
			}
			else
			{
				throw new NoSuchElementException();
			}
		}

		public void remove()
			throws UnsupportedOperationException, IllegalStateException
		{
			throw new UnsupportedOperationException();
		}

		private void findNextEvent()
		{
			while (theIterator.hasNext())
			{
				LabeledEvent currEvent = (LabeledEvent) theIterator.next();

				if (currEvent.isControllable() == controllableEvents)
				{
					nextEvent = currEvent;

					return;
				}
			}

			nextEvent = null;
		}
	}

	/**
	 * Computes A \ B (difference) where A is this alphabet and B is other
	 *
	 *@param  other The other alphabet
	 */
	public void minus(Alphabet other)
	{
		for (Iterator alphIt = other.iterator(); alphIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();

			if (contains(currEvent.getLabel()))
			{
				try
				{
					removeEvent(currEvent.getLabel());
				}
				catch (Exception ex)
				{    // This should be impossible
					logger.error("Alphabet.minus. Trying to remove a non-existing event. " + ex);
					logger.debug(ex.getStackTrace());
				}
			}
		}
	}

	static public Alphabet minus(Alphabet op1, Alphabet op2)
	{
		Alphabet result = new Alphabet(op1);

		result.minus(op2);

		return result;
	}

	/**
	 * Computes A intersection B, where A is this alphabet and B the other
	 *
	 *@param  other The other alphabet
	 */
	public void intersect(Alphabet other)
	{
		ArrayList removeList = new ArrayList();

		for (Iterator alphIt = this.iterator(); alphIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();

			if (!other.contains(currEvent.getLabel()))
			{
				removeList.add(currEvent);

				//removeList.add(currEvent.getLabel());

				/*
				try
				{
					removeEvent(currEvent.getLabel());
				}
				catch (Exception ex)
				{   // This should be impossible
						logger.error("Alphabet.intersect. Trying to remove a non-existing event. " + ex);
						logger.debug(ex.getStackTrace());
				}
				*/
			}
		}

		for (Iterator removeIt = removeList.iterator(); removeIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) removeIt.next();

			//String currEvent = (String) removeIt.next();
			removeEvent(currEvent);
		}
	}

	/**
	 * returns true if the two alphabets overlap, that is, they have at least one common event
	 *
	 *@param  other The other alphabet
	 */
	public boolean overlap(Alphabet other)
	{
		ArrayList removeList = new ArrayList();

		for (Iterator alphIt = this.iterator(); alphIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();

			if (other.contains(currEvent.getLabel()))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Computes and returns "A intersection B"
	 */
	public static Alphabet intersect(Alphabet a1, Alphabet a2)
	{
		Alphabet result = new Alphabet(a1);

		result.intersect(a2);

		return result;
	}

	/**
	 * Computes A union B, where A is this alphabet and B is other
	 *
	 *@param  other The other alphabet
	 */
	public void union(Alphabet other)
	{
		for (Iterator alphIt = other.iterator(); alphIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();

			if (!contains(currEvent.getLabel()))
			{
				LabeledEvent newEvent = new LabeledEvent(currEvent);

				// newEvent.setId(getUniqueId("e"));
				try
				{
					addEvent(newEvent);
				}
				catch (Exception ex)
				{    // This should be impossible
					logger.error("Alphabet.union. Trying to add an existing event. " + ex);
					logger.debug(ex.getStackTrace());
				}
			}
		}
	}

	static public Alphabet union(Alphabet op1, Alphabet op2)
	{
		Alphabet result = new Alphabet(op1);

		result.union(op2);

		return result;
	}

	/**
	 * Returns the controllable part of the alphabet.
	 */
	public Alphabet getControllableAlphabet()
	{
		Alphabet subAlphabet = new Alphabet();

		for (EventIterator evIt = controllableEventIterator(); evIt.hasNext(); )
		{
			subAlphabet.addEvent(evIt.nextEvent());
		}

		return subAlphabet;
	}

	/**
	 * Returns the uncontrollable part of the alphabet.
	 */
	public Alphabet getUncontrollableAlphabet()
	{
		Alphabet subAlphabet = new Alphabet();

		for (EventIterator evIt = uncontrollableEventIterator();
				evIt.hasNext(); )
		{
			subAlphabet.addEvent(evIt.nextEvent());
		}

		return subAlphabet;
	}

	public void setIndicies()
	{
		int i = 0;

		for (EventIterator evIt = iterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = evIt.nextEvent();

			currEvent.setSynchIndex(i++);
		}
	}

	public void setIndicies(Alphabet otherAlphabet)
		throws IllegalArgumentException
	{
		for (EventIterator evIt = iterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = evIt.nextEvent();
			LabeledEvent otherEvent = otherAlphabet.getEvent(currEvent.getLabel());

			if (otherEvent == null)
			{
				throw new IllegalArgumentException("otherAlphabet must contains all events in this alphabet");
			}

			currEvent.setSynchIndex(otherEvent.getSynchIndex());
		}
	}

	public boolean isAllEventsPrioritized()
	{
		for (Iterator evIt = iterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) evIt.next();

			if (!currEvent.isPrioritized())
			{
				return false;
			}
		}

		return true;
	}

	public boolean isAllEventsObservable()
	{
		for (Iterator evIt = iterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) evIt.next();

			if (!currEvent.isObservable())
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns true if this alphabet contains an event with the same label as other.
	 */
	public boolean containsEqualEvent(LabeledEvent otherEvent)
	{
		return contains(otherEvent.getLabel());
	}

	/**
	 * Returns true if the event with same label as other event is prioritized in this alphabet.
	 */
	public boolean isPrioritized(LabeledEvent otherEvent)
		throws IllegalArgumentException
	{
		if (!containsEqualEvent(otherEvent))
		{
			throw new IllegalArgumentException();
		}

		LabeledEvent thisEvent = getEvent(otherEvent.getLabel());

		return thisEvent.isPrioritized();
	}

	/**
	 * Returns true if the event with same label as other event is controllable in this alphabet.
	 */
	public boolean isControllable(LabeledEvent otherEvent)
		throws IllegalArgumentException
	{
		if (!containsEqualEvent(otherEvent))
		{
			throw new IllegalArgumentException();
		}

		LabeledEvent thisEvent = getEvent(otherEvent.getLabel());

		return thisEvent.isControllable();
	}

	public boolean equalAlphabet(Alphabet other)
	{
		if (nbrOfEvents() != other.nbrOfEvents())
		{

			//System.err.println("equalAlphabet::non equal nbr of events");
			return false;
		}

		if (nbrOfControllableEvents() != other.nbrOfControllableEvents())
		{

			//System.err.println("equalAlphabet::non equal nbr of controllable events");
			return false;
		}

		if (nbrOfPrioritizedEvents() != other.nbrOfPrioritizedEvents())
		{

			//System.err.println("equalAlphabet::non equal nbr of prioritized events");
			return false;
		}

		if (nbrOfUnobservableEvents() != other.nbrOfUnobservableEvents())
		{
			return false;
		}

		if (nbrOfImmediateEvents() != other.nbrOfImmediateEvents())
		{

			//System.err.println("equalAlphabet::non equal nbr of immediate events");
			return false;
		}

		if (nbrOfEpsilonEvents() != other.nbrOfEpsilonEvents())
		{

			//System.err.println("equalAlphabet::non equal nbr of epsilon events");
			return false;
		}

		return true;
	}

	public void clear()
	{
		theEvents.clear();
		rehash();
	}

	public Listeners getListeners()
	{
		if (listeners == null)
		{
			listeners = new Listeners(this);
		}

		return listeners;
	}

	private void notifyListeners()
	{
		if (listeners != null)
		{
			listeners.notifyListeners();
		}
	}

	// Why was this method out-commented before? Something wrong? Or... did I do that?  /hguo
	public int nbrOfCommonEvents(Alphabet otherAlphabet)
	{
		int nbrOfCommon = 0;
		EventIterator eventIterator = iterator();
		LabeledEvent currEvent;

		while (eventIterator.hasNext())
		{
			currEvent = eventIterator.nextEvent();

			if (otherAlphabet.contains(currEvent.getLabel()))
			{
				nbrOfCommon++;
			}
		}

		return nbrOfCommon;
	}

	/**
	 * Returns true if the alphabet has alphabetically (ignoring case) equal event names.
	 */
	public boolean hasEqualEventNamesIgnoringCase()
	{

		// Make copy to make iterating easier...
		Alphabet copy = new Alphabet(this);

		// Did we find any alphabetically equal names?
		boolean found = false;

		// Iterate
		for (EventIterator copyIt = copy.iterator(); copyIt.hasNext(); )
		{
			LabeledEvent eventA = copyIt.nextEvent();

			// Another iterator
			EventIterator eventIt = iterator();

			// Make sure each pair is only examined once
			while (!eventA.equals(eventIt.nextEvent()));

			while (eventIt.hasNext())
			{
				LabeledEvent eventB = eventIt.nextEvent();

				// Compare names ignoring case
				if (eventA.getLabel().equalsIgnoreCase(eventB.getLabel()))
				{
					logger.warn("The events " + eventA + " and " + eventB + " have dangerously " + "similar names.");

					found = true;
				}
			}
		}

		return found;
	}

	public static void main(String[] args)
	{
		Alphabet sigma1 = new Alphabet();

		sigma1.addEvent(new LabeledEvent("e1"));
		sigma1.addEvent(new LabeledEvent("e2"));

		Alphabet sigma2 = new Alphabet();

		sigma2.addEvent(new LabeledEvent("e2"));
		sigma2.addEvent(new LabeledEvent("e3"));
		sigma1.union(sigma2);
		System.out.println("sigma1 + sigma2 = " + sigma1.toString());
	}
}
