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
	private EventToAutomataMap eventToAutomataMap;
	private Collection eventCollection;

	public AlphabetAnalyzer(Automata theAutomata)
	{
		this.theAutomata = theAutomata;
	}

	/**
	 * Builds map and logs info about the different alphabets relationships.
	 */
	public void execute()
	{
		buildEventToAutomataMap();
		checkAllPairs();

		//printUnsynchronizedEvents();
	}

	private void buildEventToAutomataMap()
	{
		eventToAutomataMap = AlphabetHelpers.buildEventToAutomataMap(theAutomata);
	}

	public void printUnsynchronizedEvents()
	{
		//Set eventSet = eventToAutomataMap.keySet();
		//Iterator eventIt = eventSet.iterator();

		Iterator<LabeledEvent> eventIt = eventToAutomataMap.eventIterator();
		while (eventIt.hasNext())
		{
			//LabeledEvent currEvent = (LabeledEvent) eventIt.next();
			LabeledEvent currEvent = eventIt.next();

			if (isUnsynchronizedEvent(currEvent))
			{
				logger.info("UnsynchronizedEvent: " + currEvent.getLabel());
			}
		}
	}

	/**
	 * Determines if an event is not synchronized, that is, present in less than two automata.
	 *
	 * @param  ev the event that should be examined.
	 * @return  true if the given event is present in zero or one automata, and false if it 
	 * is present on more than one automata.
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
			Automata currSet = (Automata) eventMapIt.next();

			inLeft = currSet.containsAutomaton(leftAut);
			inRight = currSet.containsAutomaton(rightAut);

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
			logger.info("Alphabet: " + leftAut.getName() + " == " + rightAut.getName() + 
						" new unsych: " + newUnique);
		}
		else
		{
			if (nbrOnlyLeft == 0)
			{
				logger.info("Alphabet: " + leftAut.getName() + " <= " + rightAut.getName() + 
							" new unsych: " + newUnique);
			}

			if (nbrOnlyRight == 0)
			{
				logger.info("Alphabet: " + rightAut.getName() + " <= " + leftAut.getName() + 
							" new unsych: " + newUnique);
			}
		}

		/*
		 *  if (nbrOnlyLeft == 0)
		 *  System.out.println("*** left <= right");
		 *  if (nbrOnlyRight == 0)
		 *  logger.info("*** right <= left");
		 *  logger.info("left: " + leftAut.getName() + " right: " + rightAut.getName());
		 *  logger.info("#left: " + nbrOnlyLeft +
		 *  " #right: " + nbrOnlyRight +
		 *  " #common: " + nbrCommon);
		 *  logger.info("#uleft: " + nbrUniqueLeft +
		 *  " #uright: " + nbrUniqueRight +
		 *  " #newUnique: " + newUnique);
		 */
	}

	// -----------------------------------------------------------------

	/**
	 * returns the events that are always blocked in an automaton.
	 * assumes that the automaton is trim, or it will give an underapproximated answer.
	 *
	 * FIXME: this algorithm is currently not working :(
	 */
	public static HashSet getBlockedEvents(Automaton a)
	{
		HashSet ret = new HashSet();
		Alphabet alfa = a.getAlphabet();

		for (Iterator<LabeledEvent> evIt = alfa.iterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = evIt.next();

			ret.add(currEvent.getLabel());
		}

		for (ArcIterator ai = a.arcIterator(); ai.hasNext(); )
		{
			Arc currArc = ai.nextArc();

//                      currArc.getEvent().extra1 = 1;
			ret.remove(currArc.getEvent().getLabel());
		}

		return ret;
	}
}
