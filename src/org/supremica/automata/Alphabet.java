
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
 *@see  org.supremica.automata.Events
 *@see  org.supremica.automata.AlphabetHelpers
 */
public class Alphabet
	extends Events
{
	private static Logger logger = LoggerFactory.createLogger(Alphabet.class);
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
	 *
	 *@param  prefix Description of the Parameter
	 *@return  The uniqueId value
	 */
	public String getUniqueId(String prefix)
	{
		String newId = null;

		do
		{
			newId = prefix + new Integer(idIndex++);
		}
		while (containsEventWithId(newId));

		return newId;
	}

	/**
	 * True, if an event with this id, is already in the alphabet, false otherwise.
	 *
	 *@param  id Description of the Parameter
	 *@return  Description of the Return Value
	 */
	public boolean containsEventWithId(String id)
	{
		return idMap.containsKey(id);
	}

	public boolean contains(LabeledEvent event)
	{
		return containsEventWithId(event.getId());
	}

	/**
	 * Returns an event with a given id. An exception is thrown if the event
	 * does not exists.
	 *
	 *@param  id Description of the Parameter
	 *@return  The eventWithId value
	 *@exception  Exception Description of the Exception
	 */
	public LabeledEvent getEventWithId(String id)
		throws Exception
	{
		if (containsEventWithId(id))
		{
			return (LabeledEvent) idMap.get(id);
		}
		else
		{
			throw new Exception("Alphabet.getEventWithId: No event with id \"" + id + "\" exists");
		}
	}

	/**
	 * Add an event to the alphabet. Check with containsEventWithId to make
	 * sure that an event with the id not already exists.
	 * If trying to add an event with an id that already exists, an exception is thrown.
	 *
	 *@param  event The feature to be added to the Event attribute
	 *@exception  Exception Description of the Exception
	 */
	public void addEvent(LabeledEvent event)
		throws Exception
	{
		if (!containsEventWithId(event.getId()))
		{
			idMap.put(event.getId(), event);
			super.addEvent(event);
		}
		else
		{
			throw new Exception("Alphabet.addEvent: An event with id \"" + event.getId() + "\" already exists");
		}
	}

	public void addEvent(LabeledEvent event, boolean b)
	{
		try
		{
			addEvent(event);
		}
		catch(Exception excp)
		{
			if(b == true)
			{
				throw new RuntimeException(excp);
			}
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

			if (containsEventWithLabel(currEvent.getLabel()))
			{
				try
				{
					removeEvent(currEvent.getLabel());
				}
				catch (Exception e)
				{    // This should be impossible
					logger.error("Alphabet.minus. Trying to remove a non-existing event.");
				}
			}
		}
	}

	/**
	 * Computes A + B (union) where A is this alphabet and B is other
	 *
	 *@param  other The other alphabet
	 */
	public void plus(Alphabet other)
	{
		for (Iterator alphIt = other.iterator(); alphIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) alphIt.next();

			if (!containsEventWithLabel(currEvent.getLabel()))
			{
				LabeledEvent newEvent = new LabeledEvent(currEvent);

				newEvent.setId(getUniqueId("e"));

				try
				{
					addEvent(newEvent);
				}
				catch (Exception e)
				{    // This should be impossible
					logger.error("Alphabet.plus. Trying to add an existing event.");
				}
			}
		}
	}

	public boolean isAllEventsPrioritized()
	{
		for (Iterator evIt = eventIterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) evIt.next();

			if (!currEvent.isPrioritized())
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Remove event from alphabet.
	 *
	 *@param  event Description of the Parameter
	 */
	public void removeEvent(LabeledEvent event)
		throws Exception
	{
		idMap.remove(event.getId());
		super.removeEvent(event);
	}

	/**
	 * Remove event from alphabet.
	 *
	 *@param  event Description of the Parameter
	 */
	public void removeEvent(String label)
		throws Exception
	{
		LabeledEvent currEvent = getEventWithLabel(label);

		idMap.remove(currEvent.getId());
		super.removeEvent(label);
	}

	/**
	 * Do not use this, use iterator instead.
	 *
	 *@return  Description of the Return Value
	 */
	public Iterator eventIterator()
	{
		return idMap.values().iterator();
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

	/**
	 * Produce a string suitable for debugging
	 *
	 *@return  Description of the Return Value
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

	/** Must be called after an event label or id is modified. */
	public void rehash()
	{
		super.rehash();

		LabeledEvent event;
		Iterator eventIt = iterator();

		idMap.clear();

		while (eventIt.hasNext())
		{
			event = (LabeledEvent) eventIt.next();

			idMap.put(event.getId(), event);
		}
	}
}
