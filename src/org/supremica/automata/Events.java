
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
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata;

import java.util.*;

/**
 * Contains a collection of events.
 * Implements functionality for quickly
 * returning an event with a given label.
 *
 * Important note:
 * If an event label is changed after it is inserted in
 * an Events object, then rehash must be called otherwise
 * strange errors will arise as the set may become unordered.
 *
 *@author  ka
 *@created  November 28, 2001
 *@see  org.supremica.automata.Alphabet
 *@see  org.supremica.automata.EventsHelpers
 */
public class Events
{
	private TreeMap theEvents;

	/** Default constructor. */
	public Events()
	{
		theEvents = new TreeMap();
	}

	public Events(Events orgEvents)
	{
		this();

		// theEvents = new TreeMap(orgEvents.theEvents);
		// Deep copy
		for (Iterator it = orgEvents.iterator(); it.hasNext(); )
		{
			LabeledEvent newEvent = new LabeledEvent((LabeledEvent) it.next());

			theEvents.put(newEvent.getLabel(), newEvent);
		}
	}

	/**
	 * Return the number of events.
	 *
	 *@return  Description of the Return Value
	 */
	public int size()
	{
		return theEvents.size();
	}

	public void setEvents(TreeMap oldEvents)
	{
		theEvents = new TreeMap(oldEvents);
	}

	public TreeMap getEvents()
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
	 *
	 *@param  ev The feature to be added to the Event attribute
	 *@exception  Exception Description of the Exception
	 */
	public void addEvent(LabeledEvent ev)
		throws Exception // ClassCastException, NullPointerException // only these are really thrown by TreeMap::put
	{
		theEvents.put(ev.getLabel(), ev);
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
		if (!containsEventWithLabel(label))
		{
			throw new IllegalArgumentException("The event is not included in this alphabet");
		}
		theEvents.remove(label);
	}

	/**
	 * True, if it exists an event with the label, false otherwise.
	 *
	 *@param  label Description of the Parameter
	 *@return  Description of the Return Value
	 */
	public boolean containsEventWithLabel(String label)
		throws IllegalArgumentException
	{
		if (label == null)
		{
			throw new IllegalArgumentException("label must be non-null");
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
	public LabeledEvent getEventWithLabel(String label)
		throws IllegalArgumentException
	{
		if (label == null)
		{
			throw new IllegalArgumentException("EventLabel must be non-null");
		}
		return (LabeledEvent) theEvents.get(label);
	}

	public LabeledEvent getEventWithIndex(int index)
		throws Exception
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

		throw new Exception("No event with index '" + index + "' exists");
	}

	public int nbrOfEvents()
	{
		return size();
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

	public String toString()
	{
		StringBuffer tmpBuf = new StringBuffer("Events:\n   theEvents: " + theEvents);

		return tmpBuf.toString();
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

	public void clear()
	{
		theEvents.clear();
	}

	/** Must be called after an event label or id is modified. */
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
				LabeledEvent currEvent = (LabeledEvent)theIterator.next();
				if (currEvent.isControllable() == controllableEvents)
				{
					nextEvent = currEvent;
					return;
				}
			}
			nextEvent = null;
		}
	}

}
