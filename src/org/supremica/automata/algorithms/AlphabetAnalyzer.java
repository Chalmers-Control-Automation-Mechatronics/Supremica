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
package org.supremica.automata.algorithms;

import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.*;

public class AlphabetAnalyzer
{
	private static Logger logger = LoggerFactory.createLogger(AlphabetAnalyzer.class);
	private Automata theAutomata;

	/**
	 * Map from an Event-object to the Set of Automaton-objects that contains this event.
	 */
	private HashMap eventToAutomataMap = new HashMap();
	private Collection eventCollection;

	public AlphabetAnalyzer(Automata theAutomata)
	{
		this.theAutomata = theAutomata;
	}

	public void execute()
	{
		buildEventToAutomataMap();

		checkAllPairs();
		//printUnsynchronizedEvents();
	}

	private void buildEventToAutomataMap()
	{
		buildEventToAutomataMap(theAutomata);
	}

	private void buildEventToAutomataMap(Automata anAutomata)
	{
		// Loop over automata
		for (Iterator automataIt = anAutomata.iterator(); automataIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton) automataIt.next();
			Alphabet currAlphabet = currAutomaton.getAlphabet();

			// Loop over alphabet
			for (EventIterator eventIt = currAlphabet.iterator(); eventIt.hasNext();)
			{	// Insert in map
				insertEvent(eventIt.nextEvent(), currAutomaton);
			}
		}
	}

	/**
	 * Builds the eventToAutomataMap to map the events in anAutomata to the automata in anAutomata
	 *
	 *@return HashMap mapping Event-object to Set of Automaton-objects.
	 */
	public HashMap getEventToAutomataMap(Automata anAutomata)
	{
		buildEventToAutomataMap(anAutomata);
		return eventToAutomataMap;
	}

	/**
	 * Builds the eventToAutomataMap to map uncontrollable events to plants.
	 *
	 *@return HashMap mapping uncontrollable Event-object to Set of plant-type Automaton-objects.
	 */
	public HashMap getUncontrollableEventToPlantMap()
		 throws Exception
	{
		///* There will be no exceptions
  		  try
		  {
		      buildUncontrollableEventToPlantMap();
		  }
		  catch (Exception e)
		  {
			  logger.error("Error in AlphabetAnalyzer. " + e);
			  logger.debug(e.getStackTrace());
			  throw e;
		  }
		  //*/

		  //buildUncontrollableEventToPlantMap();
		return eventToAutomataMap;
	}

	private void buildUncontrollableEventToPlantMap()
	{
		Iterator automataIt = theAutomata.iterator();

		while (automataIt.hasNext())
		{
			Automaton currAutomaton = (Automaton) automataIt.next();

			if (currAutomaton.getType() == AutomatonType.Plant)
			{
				Alphabet currAlphabet = (Alphabet) currAutomaton.getAlphabet();
				Iterator eventIt = currAlphabet.iterator();

				while (eventIt.hasNext())
				{
					LabeledEvent currEvent = (LabeledEvent) eventIt.next();

					if (!currEvent.isControllable())
					{
						insertEvent(currEvent, currAutomaton);
					}
				}
			}
		}
	}

	private void insertEvent(LabeledEvent ev, Automaton aut)
	{
		HashSet automatonSet = (HashSet) eventToAutomataMap.get(ev);

		if (automatonSet == null)
		{   // There were no automata in the map for this event,
			automatonSet = new HashSet();
			eventToAutomataMap.put(ev, automatonSet);
		}

		automatonSet.add(aut);
	}

	/**
	 * Determines if an event is not synchronized, that is, present in less than two automata.
	 *
	 *@param  ev the event that should be examined.
	 *@return  true if the given event is present in zero or one automata, and false if it is present on more than one automata.
	 */
	public boolean isUnsynchronizedEvent(LabeledEvent ev)
	{
		Set automatonSet = (Set) eventToAutomataMap.get(ev);

		if (automatonSet == null)
		{
			return true;
		}

		return automatonSet.size() <= 1;
	}

	public Iterator eventIterator()
	{
		return eventToAutomataMap.keySet().iterator();
	}

	public void printUnsynchronizedEvents()
	{
		Set eventSet = eventToAutomataMap.keySet();
		Iterator eventIt = eventSet.iterator();

		while (eventIt.hasNext())
		{
			LabeledEvent currEvent = (LabeledEvent) eventIt.next();

			if (isUnsynchronizedEvent(currEvent))
			{
				logger.info("UnsynchronizedEvent: " + currEvent.getLabel());
			}
		}
	}

	private void checkAllPairs()
	{
		eventCollection = eventToAutomataMap.values();

		int nbrOfAutomata = theAutomata.size();

		for (int i = 0; i < nbrOfAutomata - 1; i++)
		{
			for (int j = i + 1; j < nbrOfAutomata; j++)
			{
				Automaton leftAut = theAutomata.getAutomatonAt(i);
				Automaton rightAut = theAutomata.getAutomatonAt(j);

				pairComparison(leftAut, rightAut);
			}
		}
	}

	private void pairComparison(Automaton leftAut, Automaton rightAut)
	{
		int nbrOnlyLeft = 0;
		int nbrOnlyRight = 0;
		int nbrCommon = 0;
		int nbrUniqueLeft = 0;
		int nbrUniqueRight = 0;
		int newUnique = 0;
		boolean inLeft;
		boolean inRight;
		Iterator eventMapIt = eventCollection.iterator();

		while (eventMapIt.hasNext())
		{
			Set currSet = (Set) eventMapIt.next();

			inLeft = currSet.contains(leftAut);
			inRight = currSet.contains(rightAut);

			if (inLeft && inRight)
			{
				nbrCommon++;

				if (currSet.size() == 2)
				{
					newUnique++;
				}
			}
			else if (inLeft)
			{
				nbrOnlyLeft++;

				if (currSet.size() == 1)
				{
					nbrUniqueLeft++;
				}
			}
			else if (inRight)
			{
				nbrOnlyRight++;

				if (currSet.size() == 1)
				{
					nbrUniqueRight++;
				}
			}
		}

		if ((nbrOnlyLeft == 0) && (nbrOnlyRight == 0))
		{
			logger.info("Alphabet: " + leftAut.getName() + " == " +
						rightAut.getName() + " new unsych: " + newUnique);
		}
		else
		{
			if (nbrOnlyLeft == 0)
			{
				logger.info("Alphabet: " + leftAut.getName() + " <= " +
							rightAut.getName() + " new unsych: " + newUnique);
			}

			if (nbrOnlyRight == 0)
			{
				logger.info("Alphabet: " + rightAut.getName() + " <= " +
							leftAut.getName() + " new unsych: " + newUnique);
			}
		}

		/*
		 *  if (nbrOnlyLeft == 0)
		 *  System.out.println("*** left <= right");
		 *  if (nbrOnlyRight == 0)
		 *  System.out.println("*** right <= left");
		 *  System.out.println("left: " + leftAut.getName() + " right: " + rightAut.getName());
		 *  System.out.println("#left: " + nbrOnlyLeft +
		 *  " #right: " + nbrOnlyRight +
		 *  " #common: " + nbrCommon);
		 *  System.out.println("#uleft: " + nbrUniqueLeft +
		 *  " #uright: " + nbrUniqueRight +
		 *  " #newUnique: " + newUnique);
		 */
	}
}
