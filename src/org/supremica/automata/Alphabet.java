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
 * Similar to Events, but with allows for the fast lookup of an event based on the id.
 *
 * Important note:
 * If an event label is changed after it is inserted in
 * an Events object, then rehash must be called otherwise
 * the strange errors will arise.
 *
 * @see org.supremica.automata.Events
 * @see org.supremica.automata.AlphabetHelpers
 */
public class Alphabet
	extends Events
{
	private HashMap idMap;
	private int idIndex = 0;
	private Listeners listeners = null;

	public Alphabet()
	{
		idMap = new HashMap();
	}

	public Alphabet(Alphabet orgAlphabet)
	{
		super(orgAlphabet);
		
		idMap = new HashMap();
		idIndex = orgAlphabet.idIndex;
		// theEvents = new TreeMap(orgAlphabet.theEvents);
		// setEvents(orgAlphabet.getEvents());
		
		rehash();
	}

	/**
	 * Returns a new unique (for this object) id.
	 */
	public String getUniqueId(String prefix)
	{
		return prefix + new Integer(idIndex++);
	}

	/**
	 * True, if an event with this id, is already in the alphabet, false otherwise.
	 */
	public boolean containsEventWithId(String id)
	{
		return idMap.containsKey(id);
	}

	/**
	 * Returns an event with a given id. An exception is thrown if the event
	 * does not exists.
	 */
	public Event getEventWithId(String id)
		throws Exception
	{
		if (containsEventWithId(id))
			return (Event)idMap.get(id);
		else
			throw new Exception("Alphabet.getEventWithId: No event with id \"" + id + "\" exists");
	}

	/**
	 * Add an event to the alphabet. Check with containsEventWithId to make
	 * sure that an event with the id already exists.
	 * If trying to add an event with an id that already exists, an exception is thrown.
	 */
	public void addEvent(Event event)
		throws Exception
	{
		if (!containsEventWithId(event.getId()))
		{
			idMap.put(event.getId(), event);
			super.addEvent(event);
		}
		else
			throw new Exception("Alphabet.addEvent: An event with id \"" + event.getId() + "\" already exists");
	}

	/**
	 * Remove event from alphabet.
	 */
	public void removeEvent(Event event)
	{
		idMap.remove(event.getId());
		super.removeEvent(event);
	}

	/**
	 * Do not use this, use iterator instead.
	 */
	public Iterator eventIterator()
	{
		return idMap.values().iterator();
	}

	/**
	 * Produce a string suitable for debugging
	 */
	public String toString()
	{
		StringBuffer tmpBuf = new StringBuffer("Alphabet:\n   idMap: " + idMap + '\n');
		tmpBuf.append(super.toString());
		return tmpBuf.toString();
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

	/**
	 * Must be called after an event label or id is modified.
	 */
	public void rehash()
	{
		super.rehash();
		Event event;
		Iterator eventIt = iterator();
		idMap.clear();
		while (eventIt.hasNext())
		{
			event = (Event) eventIt.next();
			idMap.put(event.getId(), event);
		}			
	}
}
