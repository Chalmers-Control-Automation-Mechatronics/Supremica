
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.automata;

import java.util.*;

/**
 * Contains a collection of events.
 * Implements functionality for quickly
 * returning a event with a given label.
 *
 * Important note:
 * If an event label is changed after it is inserted in
 * an Events object, then rehash must be called otherwise
 * the strange errors will arise.
 *
 * @see org.supremica.automata.Alphabet
 * @see org.supremica.automata.EventsHelpers
 */
public class Events
{
	private TreeMap theEvents;

	/**
	 * Default constructor.
	 */
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
			Event newEvent = new Event((Event) it.next());

			theEvents.put(newEvent.getLabel(), newEvent);
		}
	}

	/**
	 * Return the number of events.
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
	 */
	public Iterator iterator()
	{
		return theEvents.values().iterator();
	}

	/**
	 * Add an event.
	 */
	public void addEvent(Event ev)
		throws Exception
	{
		theEvents.put(ev.getLabel(), ev);
	}

	public void removeEvent(Event ev)
	{
		theEvents.remove(ev.getLabel());
	}

	/**
	 * True, if it exists an event with the label, false otherwise.
	 */
	public boolean containsEventWithLabel(String label)
	{
		return theEvents.containsKey(label);
	}

	/**
	* Return the event with the given label.
	* Throw an exception if it does not exist.
	*/
	public Event getEventWithLabel(String label)
		throws Exception
	{

		// System.err.println(label);
		if (containsEventWithLabel(label))
		{
			return (Event) theEvents.get(label);
		}
		else
		{
			throw new Exception("The event '" + label + "' does not exist.");
		}
	}

	public Event getEventWithIndex(int index)
		throws Exception
	{
		Iterator eventIt = iterator();

		while (eventIt.hasNext())
		{
			Event currEvent = (Event) eventIt.next();

			if (currEvent.getSynchIndex() == index)
			{
				return currEvent;
			}
		}

		throw new Exception("No event with index '" + index + "' exists");
	}

	/**
	 * True, if the event is in the set already, false otherwise.
	 */
	public boolean includes(Event theEvent)
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
	 */
	public Collection values()
	{
		return theEvents.values();
	}

	/**
	 * Must be called after an event label or id is modified.
	 */
	public void rehash()
	{
		TreeMap newEvents = new TreeMap();

		// theEvents = new TreeMap(orgEvents.theEvents);
		// Deep copy
		for (Iterator it = iterator(); it.hasNext(); )
		{
			Event currEvent = (Event) it.next();

			newEvents.put(currEvent.getLabel(), currEvent);
		}

		theEvents.clear();

		theEvents = newEvents;
	}
}
