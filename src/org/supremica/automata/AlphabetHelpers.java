
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

public class AlphabetHelpers
{
	public static Alphabet getUnionAlphabet(Automata theAutomata)
		throws Exception
	{
		EventsSet eventsSet = new EventsSet();

		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			Alphabet currAlphabet = currAutomaton.getAlphabet();

			eventsSet.add(currAlphabet);
		}

		return getUnionAlphabet(eventsSet, "a");
	}

	/**
	 * Compute a new alphabet as the union of a set of alphabets.
	 * Adjust the Event attributes properly.
	 *
	 *@param  alphabets Description of the Parameter
	 *@param  idPrefix Description of the Parameter
	 *@return  The unionAlphabet value
	 *@exception  IllegalArgumentException Description of the Exception
	 *@exception  Exception Description of the Exception
	 */
	public static Alphabet getUnionAlphabet(EventsSet alphabets, String idPrefix)
		throws IllegalArgumentException, Exception
	{
		if (alphabets.size() < 1)
		{
			throw new IllegalArgumentException("At least one alphabet is necessary");
		}

		EventsSet eventSet = new EventsSet();
		Events unionEvents = EventsHelpers.union(alphabets);
		Alphabet newAlphabet = new Alphabet();

		// Iterate over all events
		Iterator eventsIt = unionEvents.iterator();

		while (eventsIt.hasNext())
		{
			EventLabel currEvent = (EventLabel) eventsIt.next();

			eventSet.clear();

			// Iterate over all alphabets, and find those alphabets that
			// contain an event with currEvent.getLabel
			Iterator alphabetIt = alphabets.iterator();

			while (alphabetIt.hasNext())
			{
				Alphabet currAlphabet = (Alphabet) alphabetIt.next();

				if (currAlphabet.containsEventWithLabel(currEvent.getLabel()))
				{
					eventSet.add(currAlphabet.getEventWithLabel(currEvent.getLabel()));
				}
			}

			EventLabel newEvent = EventHelpers.createEvent(eventSet, idPrefix);

			newEvent.setId(newAlphabet.getUniqueId(idPrefix));
			newAlphabet.addEvent(newEvent);
		}

		return newAlphabet;
	}
}
