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
	implements Stoppable
{
	private static Logger logger = LoggerFactory.createLogger(MutuallyNonblockingVerifier.class);

	protected Automata theAutomata;
	protected AlphabetAnalyzer theAlphabetAnalyzer;
	private AutomataToEventMap safeEventsMap;

	private ExecutionDialog executionDialog = null;
	private AutomataSynchronizerHelper synchHelper = null;

	// Global so that it can be stopped
	private AutomataVerifier theVerifier = null;	
	private boolean stopRequested = false;

	public MutuallyNonblockingVerifier(Automata theAutomata)
	{
		this.theAutomata = theAutomata;
		safeEventsMap = new AutomataToEventMap();
	}

	public MutuallyNonblockingVerifier(Automata theAutomata, AutomataSynchronizerHelper synchHelper)
	{
		this(theAutomata);
		this.synchHelper = synchHelper;
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

		java.awt.EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					executionDialog = synchHelper.getExecutionDialog();
					executionDialog.setMode(ExecutionDialogMode.verifyingMutualNonblocking);
					executionDialog.initProgressBar(0, theAutomata.size());
				}
			});

		// return synchTest();
		return safeEventsTest();
	}

	private boolean safeEventsTest()
	{
		// Find safe events in the respective automata
		buildSafeEvents();
		logger.debug(safeEventsMap.toString());
					
		if (stopRequested)
			return false;

		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			Events currEvents = safeEventsMap.getEvents(currAutomaton);
			// System.out.println("Automaton: " + currAutomaton.getName() + "\nEvents: " + currEvents + "\n");
			currAutomaton.extendMutuallyAccepting(currEvents);
		}

		boolean allMutuallyNonblocking = true;
		int nbrMutuallyNonblocking = 0;
		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			if (currAutomaton.nbrOfStates() != currAutomaton.nbrOfMutuallyAcceptingStates())
			{
				logger.warn("Automaton " + currAutomaton + " might cause blocks.");
				allMutuallyNonblocking = false;
				// return false;
			}
			else
			{
				logger.info("Automaton " + currAutomaton + " is mutually nonblocking!");
				nbrMutuallyNonblocking++;
			}
			logger.info("Safe events: " + safeEventsMap.getEvents(currAutomaton));
		}
		logger.info("At least " + nbrMutuallyNonblocking + " out of the total " + 
					theAutomata.size() + " automata were mutually nonblocking.");
		return allMutuallyNonblocking;
	}

	private boolean synchTest()
	{
		Automata newAutomata = new Automata();
		for (Iterator autItOne = theAutomata.iterator(); autItOne.hasNext(); )
		{
			Automata synchAutomata = new Automata();
			Automaton currAutomaton = (Automaton)autItOne.next();
			Alphabet currAlphabet = currAutomaton.getAlphabet();

			for (Iterator autItTwo = theAutomata.iterator(); autItTwo.hasNext(); )
			{
				Automaton otherAutomaton = (Automaton)autItTwo.next();
				Alphabet otherAlphabet = otherAutomaton.getAlphabet();
				
				Alphabet.intersect(currAlphabet, otherAlphabet);

				if (Alphabet.intersect(currAlphabet, otherAlphabet).size() != 0)
				{
					synchAutomata.addAutomaton(otherAutomaton);
				}
			}

			logger.info("Synchronizing " + synchAutomata);

			try
			{
				Automaton synchAutomaton = AutomataSynchronizer.synchronizeAutomata(synchAutomata);
				newAutomata.addAutomaton(synchAutomaton);
			}
			catch (Exception ojsan)
			{
				logger.error(ojsan);
			}
		}

		theAutomata.addAutomata(newAutomata);

		return false;
	}

	/**
	 * Find all events in all automata that can be executed as soon as
	 * they are enabled in the current automaton.
	 *   An obvious example of this is all events that are only
	 * present in one automation.
	 *   A less obvious example is events that are never disabled by the
	 * system if it is enabled in the current automaton. See checkSafeness().
	 */
	protected void buildSafeEvents()
	{
		Automata newAutomata = new Automata(theAutomata);

		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			logger.info("Building safe events for automaton " + currAutomaton + "...");
			Alphabet currAlphabet = currAutomaton.getAlphabet();
			for (Iterator evIt = currAlphabet.iterator(); evIt.hasNext(); )
			{
				LabeledEvent currEvent = (LabeledEvent)evIt.next();
				logger.info("Event: " + currEvent + " (" + (currEvent.isControllable() ? "controllable" : "uncontrollable") + ")...");

				if (stopRequested)
					return;

				boolean isSafe = checkSafeness(newAutomata, currAutomaton, currEvent);
				if (isSafe)
				{
					safeEventsMap.addEvent(currAutomaton, currEvent);
				}

				if (stopRequested)
					return;
			}

			if (executionDialog != null)
			{
				executionDialog.setProgress(currAutomaton.getIndex());
			}
		}
	}

	/**
	 * Sets the AutomatonType of the automata and the controllability of the events to 
	 * make way for the controllability check in checkSafeness().
	 */
	private void setAttributes(Automata totalSystem, Automaton thePlantAutomaton, LabeledEvent unconEvent)
	{
		for (Iterator autIt = totalSystem.iterator(); autIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			if (currAutomaton.getName() == thePlantAutomaton.getName()) // Equal name? Is this enough?
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
				// Make all events controllable except for the chosen one (unconEvent)
				LabeledEvent currEvent = (LabeledEvent)eventIt.next();
				currEvent.setControllable(!currEvent.getLabel().equals(unconEvent.getLabel()));
			}
		}
	}

	/**
	 * Examines if the event currEvent is always enabled in totalSystem if
	 * it is enabled in thePlantAutomaton (it is never disabled by totalSystem).
	 */
	private boolean checkSafeness(Automata totalSystem, Automaton thePlantAutomaton, LabeledEvent currEvent)
	{
		setAttributes(totalSystem, thePlantAutomaton, currEvent);
		SynchronizationOptions synchronizationOptions = new SynchronizationOptions();
		VerificationOptions verificationOptions = VerificationOptions.getDefaultControllabilityOptions();
		/*
		VerificationOptions verificationOptions;
		verificationOptions = new VerificationOptions(VerificationType.Controllability, 
													  VerificationAlgorithm.Modular, 
													  SupremicaProperties.verifyExclusionStateLimit(), 
													  SupremicaProperties.verifyReachabilityStateLimit(), 
													  false, // Why false? /hugo
													  SupremicaProperties.verifySkipUncontrollabilityCheck(),
													  SupremicaProperties.verifyNbrOfAttempts());
		*/		

		boolean isSafe = false;
		try
		{
			theVerifier = new AutomataVerifier(totalSystem, synchronizationOptions, verificationOptions);
			isSafe = theVerifier.verify();
			// logger.debug(new Boolean(isSafe));
		}
		catch (Exception ex)
		{
			logger.error(ex);
			// logger.debug(ex);
		}
		return isSafe;
	}

	/**
	 * Method that stops MutuallyNonblockingVerifier as soon as possible.
	 *
	 * @see  ExecutionDialog
	 */
	public void requestStop()
	{
		stopRequested = true;
		logger.debug("MutuallyNonblockingVerifier requested to stop.");

		if (theVerifier != null)
		{
			theVerifier.requestStop();
		}
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
