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
import org.supremica.gui.*;
import org.supremica.log.*;
import org.supremica.properties.*;
import org.supremica.automata.*;

public class MutuallyNonblockingVerifier
{
	private static Logger logger = LoggerFactory.createLogger(MutuallyNonblockingVerifier.class);

	protected Automata theAutomata;
	protected AlphabetAnalyzer theAlphabetAnalyzer;
	private AutomataToEventMap safeEventsMap;

	public MutuallyNonblockingVerifier(Automata theAutomata)
	{
		this.theAutomata = theAutomata;
		safeEventsMap = new AutomataToEventMap();
	}

	public boolean isMutuallyNonblocking()
	{
		theAlphabetAnalyzer = new AlphabetAnalyzer(theAutomata);
		theAlphabetAnalyzer.execute();

		int synchronizedEvents = 0;
		int unsynchronizedEvents = 0;
		for (Iterator theIt = theAlphabetAnalyzer.eventIterator(); theIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent)theIt.next();
			if (theAlphabetAnalyzer.isUnsynchronizedEvent(currEvent))
			{
				unsynchronizedEvents++;
			}
			else
			{
				synchronizedEvents++;
			}

		}

		logger.info(unsynchronizedEvents + " unsynchronized events.");
		logger.info(synchronizedEvents + " synchronized events.");

		buildSafeEvents();
		logger.debug(safeEventsMap.toString());

		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			Events currEvents = safeEventsMap.getEvents(currAutomaton);
			currAutomaton.extendMutuallyAccepting(currEvents);
		}

		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			if (currAutomaton.nbrOfStates() != currAutomaton.nbrOfMutuallyAcceptingStates())
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Find all events in all automata that can be executed as soon as
	 * they are enabled in the current automaton.
	 * An obvious example of this is all events that are only
	 * present in one automation.
	 */
	protected void buildSafeEvents()
	{
		Automata newAutomata = new Automata(theAutomata);

		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			Alphabet currAlphabet = currAutomaton.getAlphabet();
			for (Iterator evIt = currAlphabet.iterator(); evIt.hasNext(); )
			{
				LabeledEvent currEvent = (LabeledEvent)evIt.next();

				boolean isSafe = checkSafeness(newAutomata, currAutomaton, currEvent);
				if (isSafe)
				{
					safeEventsMap.addEvent(currAutomaton, currEvent);
				}
			}

		}
	}

	private void setAttributes(Automata totalSystem, Automaton thePlantAutomaton, LabeledEvent unconEvent)
	{
		for (Iterator autIt = totalSystem.iterator(); autIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			if (currAutomaton == thePlantAutomaton)
			{
				currAutomaton.setType(AutomatonType.Plant);
			}
			else
			{
				currAutomaton.setType(AutomatonType.Specification);
			}
			Alphabet currAlphabet = currAutomaton.getAlphabet();
			for (Iterator eventIt = currAlphabet.iterator(); eventIt.hasNext();)
			{
				LabeledEvent currEvent = (LabeledEvent)eventIt.next();
				currEvent.setControllable(!currEvent.getLabel().equals(unconEvent.getLabel()));
			}
		}
	}

	private boolean checkSafeness(Automata totalSystem, Automaton thePlantAutomaton, LabeledEvent currEvent)
	{
		setAttributes(totalSystem, thePlantAutomaton, currEvent);
		SynchronizationOptions synchronizationOptions = new SynchronizationOptions();
		VerificationOptions verificationOptions = new VerificationOptions(VerificationType.Controllability, VerificationAlgorithm.Modular, SupremicaProperties.verifyExclusionStateLimit(), SupremicaProperties.verifyReachabilityStateLimit(), false, SupremicaProperties.verifySkipUncontrollabilityCheck());

		AutomataVerifier theVerifier = null;
		boolean isSafe = false;

		try
		{
			theVerifier = new AutomataVerifier(totalSystem, synchronizationOptions, verificationOptions);
			isSafe = theVerifier.verify();
			logger.debug(new Boolean(isSafe));

		}
		catch (Exception ex)
		{
			logger.error(ex);
			logger.debug(ex);
		}
		return isSafe;

	}

}

class AutomataToEventMap
{
	private static Logger logger = LoggerFactory.createLogger(AutomataToEventMap.class);

	protected Map theMap;

	public AutomataToEventMap()
	{
		theMap = new HashMap();
	}

	protected Events getEvents(Automaton theAutomaton)
	{
		Events theEvents = (Events)theMap.get(theAutomaton.getName());
		if (theEvents == null)
		{
			theEvents = new Events();
			theMap.put(theAutomaton.getName(), theEvents);
		}
		return theEvents;
	}

	public int numberOfEvents(Automaton theAutomaton)
	{
		Events theEvents = (Events)theMap.get(theAutomaton.getName());
		if (theEvents == null)
		{
			return 0;
		}
		return theEvents.size();
	}

	public Iterator eventIterator(Automaton theAutomaton)
	{
		return getEvents(theAutomaton).iterator();
	}

	public void addEvent(Automaton theAutomaton, LabeledEvent theEvent)
	{
		Events theEvents = getEvents(theAutomaton);
		try
		{
			if (!theEvents.containsEventWithLabel(theEvent.getLabel()))
			{
				theEvents.addEvent(theEvent);
			}
		}
		catch (Exception ex)
		{
			logger.error("addEvent: " + ex.getMessage());
			logger.debug(ex);
		}
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		Set keys = theMap.keySet();
		for (Iterator keyIt = keys.iterator(); keyIt.hasNext(); )
		{
			String automatonName = (String)keyIt.next();
			sb.append(automatonName + ": ");
			Events theEvents = (Events)theMap.get(automatonName);
			for (Iterator evIt = theEvents.iterator(); evIt.hasNext(); )
			{
				LabeledEvent currEvent = (LabeledEvent)evIt.next();
				sb.append(currEvent.getLabel() + " ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}