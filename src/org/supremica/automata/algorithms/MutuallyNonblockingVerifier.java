
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
import org.supremica.automata.*;
import org.supremica.properties.SupremicaProperties;

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

	// Array with results
	private static final int MUTUALLY_NONBLOCKING_YES = 0;
	private static final int MUTUALLY_NONBLOCKING_MAYBE = 1;
	private static final int MUTUALLY_NONBLOCKING_NO = 2;
	private int[] automatonIsMutuallyNonblocking;

	// Largest amount of synchronized automata during execution
	private int maxSynchronized = 0;

	// General count variables
	private int count = 0;
	private int tjong = 0;

	public MutuallyNonblockingVerifier(Automata theAutomata)
	{
		this.theAutomata = theAutomata;
		safeEventsMap = new AutomataToEventMap();

		// Initialize result array
		automatonIsMutuallyNonblocking = new int[theAutomata.size()];

		for (int i = 0; i < automatonIsMutuallyNonblocking.length; i++)
		{
			automatonIsMutuallyNonblocking[i] = MUTUALLY_NONBLOCKING_MAYBE;
		}
	}

	public MutuallyNonblockingVerifier(Automata theAutomata, AutomataSynchronizerHelper synchHelper)
	{
		this(theAutomata);

		this.synchHelper = synchHelper;
	}

	public boolean isMutuallyNonblocking()
	{

		/*
		// Pointless crap
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
		*/

		// synchTest();
		// safeEventsTest();
		// Do the work!
		try
		{
			work();
		}
		catch (Exception ex)
		{
			logger.error("Error in MutuallyNonblockingVerifier: " + ex);
			requestStop();
		}

		// Analyze and present result
		boolean allMutuallyNonblocking = true;
		int nbrMutuallyNonblocking = 0;
		int nbrMaybeMutuallyNonblocking = 0;
		int nbrBlocking = 0;

		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			int index = theAutomata.getAutomatonIndex(currAutomaton);

			if (automatonIsMutuallyNonblocking[index] == MUTUALLY_NONBLOCKING_YES)
			{
				logger.info("Automaton " + currAutomaton + " is mutually nonblocking!");

				nbrMutuallyNonblocking++;
			}
			else if (automatonIsMutuallyNonblocking[index] == MUTUALLY_NONBLOCKING_NO)
			{
				logger.error("Automaton " + currAutomaton + " is blocking!");

				nbrBlocking++;
			}
			else if (automatonIsMutuallyNonblocking[index] == MUTUALLY_NONBLOCKING_MAYBE)
			{
				logger.warn("Automaton " + currAutomaton + " might cause blocks.");

				nbrMaybeMutuallyNonblocking++;
			}
			else
			{
				logger.fatal("This can't happen. Error in MutuallyNonblockingVerifier.");
			}

			// logger.info("Safe events: " + safeEventsMap.getEvents(currAutomaton));
		}

		if (nbrBlocking > 0)
		{
			allMutuallyNonblocking = false;

			logger.info("The system is blocking. There are " + nbrBlocking + " blocking automata and " + nbrMutuallyNonblocking + " mutually nonblocking automata.");
		}
		else if (nbrMutuallyNonblocking == theAutomata.size())
		{
			allMutuallyNonblocking = true;

			logger.info("The system is mutually nonblocking!");
		}
		else
		{
			allMutuallyNonblocking = false;

			logger.info("At least " + nbrMutuallyNonblocking + " out of the total " + theAutomata.size() + " automata are mutually nonblocking.");
		}

		logger.info("During verification, at most " + maxSynchronized + " of " + theAutomata.size() + " automata were synchronized.");
		logger.info("Count: " + count + ".");
		logger.info("Tjong: " + tjong + ".");

		return allMutuallyNonblocking;
	}

	/**
	 * Tests the automata individually, trying to find safe events. Makes no further attempts if
	 * this does not work.
	 */
	private void safeEventsTest()
	{

		// Find safe events in the respective automata
		buildSafeEvents();
		logger.debug(safeEventsMap.toString());

		if (stopRequested)
		{
			return;
		}

		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			Alphabet currEvents = safeEventsMap.getEvents(currAutomaton);

			// System.out.println("Automaton: " + currAutomaton.getName() + "\nEvents: " + currEvents + "\n");
			currAutomaton.extendMutuallyAccepting(currEvents);

			if (currAutomaton.nbrOfMutuallyAcceptingStates() == currAutomaton.nbrOfStates())
			{
				int index = theAutomata.getAutomatonIndex(currAutomaton);

				automatonIsMutuallyNonblocking[index] = MUTUALLY_NONBLOCKING_YES;
			}
		}
	}

	/**
	 * Does work. Not very well, though...
	 */
	private void work()
		throws Exception
	{

		// A copy that we can do what we like with.
		Automata theAutomataCopy = new Automata(theAutomata);

		// Initialize ExecutionDialog
		java.awt.EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				executionDialog = synchHelper.getExecutionDialog();

				executionDialog.setMode(ExecutionDialogMode.verifyingMutualNonblockingFirstRun);
				executionDialog.initProgressBar(0, theAutomata.size());
			}
		});

		// First run, a simple try for each automaton
		first:
		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			logger.info("First run: Building safe events for automaton " + currAutomaton + "...");
			buildSafeEvents(currAutomaton, theAutomataCopy);
			currAutomaton.extendMutuallyAccepting(safeEventsMap.getEvents(currAutomaton));
			logger.info("Safe events: " + safeEventsMap.getEvents(currAutomaton));

			// Successful?
			if (currAutomaton.nbrOfMutuallyAcceptingStates() == currAutomaton.nbrOfStates())
			{
				int index = theAutomata.getAutomatonIndex(currAutomaton);

				automatonIsMutuallyNonblocking[index] = MUTUALLY_NONBLOCKING_YES;
			}

			// Show progress
			if (executionDialog != null)
			{
				executionDialog.setProgress(currAutomaton.getIndex());
			}

			if (stopRequested)
			{
				return;
			}
		}

		// Initialize ExecutionDialog
		java.awt.EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				executionDialog.setMode(ExecutionDialogMode.verifyingMutualNonblockingSecondRun);
				executionDialog.initProgressBar(0, theAutomata.size());
			}
		});

		// Second run, for each automaton, try and prove mutual nonblocking by adding automata if necessary
		int progress = 0;

		second:
		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			// Adjust the state names so that we can keep track of them easier
			adjustStateNames(currAutomaton);

			Automaton currSynchAutomaton = new Automaton(currAutomaton);
			Automata currSynchAutomata = new Automata(currSynchAutomaton);
			Alphabet currSafeEvents = new Alphabet(safeEventsMap.getEvents(currAutomaton));

			// What's the current mutually nonblocking status?
			currSynchAutomaton.extendMutuallyAccepting(currSafeEvents);

			boolean ok = currSynchAutomaton.nbrOfStates() == currSynchAutomaton.nbrOfMutuallyAcceptingStates();

			if (ok)
			{
				logger.info("Second run: Automaton " + currAutomaton + " is mutually accepting from the first run.");
			}
			else
			{
				logger.info("Second run: Examining automaton " + currAutomaton + " closer.");
			}

			// Use BDD algorithm for coaccessibility check?
			// boolean useBDDAlgorithm = false;
			// As long as we haven't established mutual nonblocking, we loop...
			while (!ok)
			{
				if (stopRequested)
				{
					return;
				}

				// Find the automaton predicted to give the best improvement!
				Automaton interestingAutomaton = getInterestingAutomaton(currSynchAutomata, currSynchAutomaton);

				if (interestingAutomaton == null)
				{

					// This hints that the system might be blocking?
					if (verifyBlockingBySafeEvents(currSynchAutomaton, currSafeEvents))
					{
						logger.error("The automaton " + currAutomaton + " definitely has blocking states!");

						int index = theAutomata.getAutomatonIndex(currAutomaton);

						automatonIsMutuallyNonblocking[index] = MUTUALLY_NONBLOCKING_NO;
						ok = false;
					}
					else
					{

						// We should try to find another automaton and try to prove that the blocking
						// states are unreachable... or give up.
						logger.error("I give up! This is a good time for synthesis, though! " + currSynchAutomata.size());
					}

					break;
				}

				// The automaton should be marked in all states
				if (interestingAutomaton.nbrOfAcceptingStates() != interestingAutomaton.nbrOfStates())
				{
					logger.error("Hold it! Something's wrong!");
				}

				// Add the interesting automaton
				currSynchAutomata.addAutomaton(interestingAutomaton);

				// How many automata are we looking at? Are we breaking the record?
				if (currSynchAutomata.size() > maxSynchronized)
				{
					maxSynchronized = currSynchAutomata.size();
				}

				// If there were safe events in the added automaton, they are safe here too!
				Alphabet alreadySafe = safeEventsMap.getEvents(interestingAutomaton);

				logger.debug("The events " + alreadySafe + " were already established to be safe in " + interestingAutomaton + ".");
				currSafeEvents.addEvents(alreadySafe);

				// The events that are unique to this subsystem are safe!
				Alphabet uniqueEvents = findUniqueEvents(currSynchAutomata, theAutomata);

				logger.debug("Events unique to this subsystem are: " + uniqueEvents + ".");
				currSafeEvents.addEvents(uniqueEvents);

				// Did there appear new safe events?
				addSafeEvents(currSafeEvents, currSynchAutomata, theAutomataCopy);
				logger.info("Checking " + currSynchAutomata + ". \nSafe events: " + currSafeEvents + ".");

				// BELOW COMES THE MONOLITHIC-APPROACH SPECIFIC CODE!
				// Synchronize!
				// Make sure the accepting status propagates
				currSynchAutomaton.setAllMutuallyAcceptingStatesAsAccepting();

				currSynchAutomaton = AutomataSynchronizer.synchronizeAutomata(currSynchAutomaton, interestingAutomaton);

				currSynchAutomaton.setName(currSynchAutomaton.getComment());

				// Extend the safe event coaccessibility
				currSynchAutomaton.extendMutuallyAccepting(currSafeEvents);

				// Are there apparent blocks in currSynchAutomaton? Maybe we can prove they're unreachable?
				//languageInclusionTest(currSynchAutomaton);

				/*
				if (!AutomataVerifier.verifyNonblocking(currSynchAutomaton))
				{
						logger.fatal("There are apparent blocks!! This is a good time for language inclusion tests!");
						count++;
				}
				*/

				// Remove as many transitions of uniqueEvents as you dare...
				if ((uniqueEvents != null) && (uniqueEvents.size() > 0))
				{
					count = count + removeUniqueEventTransitions(currSynchAutomaton, uniqueEvents);
				}

				/*
				// Show automaton with only unique events! Oh if we only could remove
				// those unnecessary states!
				if (uniqueEvents.size() > 0)
				{
						Automaton copy = new Automaton(currSynchAutomaton);
						// Remove all arcs wich are not safe events
						for (ArcIterator arcIt = copy.safeArcIterator(); arcIt.hasNext();)
						{
										Arc currArc = arcIt.nextArc();
										if (!uniqueEvents.contains(currArc.getEvent()))
										{
												copy.removeArc(currArc);
										}
						}

						// Add automaton to gui
						// Ignore enormous automata...
						if (currSynchAutomaton.nbrOfStates() < 400)
						{
								Gui gui = ActionMan.getGui();
								gui.getVisualProjectContainer().getActiveProject().addAutomaton(copy);
						}
				}
				*/

				/*
				// Add automaton to gui
				Gui gui = ActionMan.getGui();
				gui.getVisualProjectContainer().getActiveProject().addAutomaton(currSynchAutomaton);
				*/

				// Examinine original automaton compared to the synchronized automaton
				int nbrAccepting = currAutomaton.nbrOfMutuallyAcceptingStates();

				projectAcceptingStatus(currAutomaton, currSynchAutomaton);

				// If the amount of accepting states has increased, maybe we're finished!
				if (currAutomaton.nbrOfMutuallyAcceptingStates() > nbrAccepting)
				{
					logger.info("Progress was made, new mutually accepting states found... (" + currAutomaton.nbrOfMutuallyAcceptingStates() + " > " + nbrAccepting + ")");

					// Mutually nonblocking? We need to examine currSynchAutomaton... one could think that it's
					// enough examining the original automaton, now that we have projected the accepting
					// status, but there might be states there that are unreachable!!
					ok = (currSynchAutomaton.nbrOfStates() == currSynchAutomaton.nbrOfMutuallyAcceptingStates());

					// Assuming the system is controllable, we can ignore all uncontrollable (forbidden) states!!
					//ok = (currSynchAutomaton.nbrOfStates()-currSynchAutomaton.nbrOfForbiddenStates() ==
					//          currSynchAutomaton.nbrOfMutuallyAcceptingNotForbiddenStates());
					// Restart loop with (refined) original automaton
					currSynchAutomaton = new Automaton(currAutomaton);
					currSynchAutomata = new Automata(currSynchAutomaton);
					currSafeEvents = new Alphabet(safeEventsMap.getEvents(currAutomaton));
				}

				// Some statistics
				logger.fatal("states: " + currSynchAutomaton.nbrOfStates() + " forb: " + currSynchAutomaton.nbrOfForbiddenStates() + " mutnotforb: " + currSynchAutomaton.nbrOfMutuallyAcceptingNotForbiddenStates() + " mut: " + currSynchAutomaton.nbrOfMutuallyAcceptingStates());

				// Have we had enough of this mma..mmammm..mmmammmm...mmmadness?
				if (currSynchAutomaton.nbrOfStates() > 2000)
				{

					// Switch to BDD verification?
					// useBDDAlgorithm = true;
					logger.error("Gave up because of state explosion!");

					break;
				}
			}

			// It went ok?
			if (ok)
			{

				// This is already done above...
				// It worked! Set all states as mutually accepting in currAutomaton
				//AutomatonAllMutuallyAccepting allAccepting = new AutomatonAllMutuallyAccepting(currAutomaton);
				//allAccepting.execute();
				// Remember that it went well for this automaton
				int index = theAutomata.getAutomatonIndex(currAutomaton);

				automatonIsMutuallyNonblocking[index] = MUTUALLY_NONBLOCKING_YES;
			}

			// Show the progress!
			if (executionDialog != null)
			{

				//executionDialog.setProgress(currAutomaton.getIndex());
				executionDialog.setProgress(++progress);
			}
		}

		// Add to project the result

		/*
		Gui gui = ActionMan.getGui();
		gui.getVisualProjectContainer().getActiveProject().addAutomata(newSynchAutomata);
		*/
	}

	/**
	 * Finds the most interesting automaton in theAutomata relative (in some sense) to synchAutomaton and
	 * returns it.
	 */
	private Automaton getInterestingAutomaton(Automata synchAutomata, Automaton synchAutomaton)
	{

		// Get the most interesting events
		Vector interestingEvents = getInterestingEvents(synchAutomaton);

		for (int i = 0; i < interestingEvents.size(); i++)
		{
			LabeledEvent currEvent = (LabeledEvent) interestingEvents.elementAt(i);

			// logger.info("Interesting events: (" + i + ") " + currEvent);
		}

		Alphabet synchAlphabet = synchAutomaton.getAlphabet();
		Automaton bestAutomaton = null;
		LabeledEvent bestEvent = null;
		boolean safeInBest = false;
		int bestValue = Integer.MAX_VALUE;

		for (int i = 0; i < interestingEvents.size(); i++)
		{
			int index = i;

			// int index = (int) Math.floor(Math.random() * interestingEvents.size());
			LabeledEvent currEvent = (LabeledEvent) interestingEvents.elementAt(index);

			for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
			{
				Automaton currAutomaton = (Automaton) autIt.next();
				Alphabet currAlphabet = currAutomaton.getAlphabet();

				// Already in the synch?
				if (synchAutomata.containsAutomaton(currAutomaton.getName()))
				{
					continue;
				}

				// Does not contain the most interesting event?
				if (!currAlphabet.contains(currEvent))
				{
					continue;
				}

				// The most interesting event is safe in the automaton?
				if (safeEventsMap.getEvents(currAutomaton).contains(currEvent))
				{

					// Is this the first automaton where the event is safe?
					// Then we override the earlier results.
					if (safeInBest == false)
					{
						bestAutomaton = currAutomaton;
						bestEvent = currEvent;
						bestValue = currAutomaton.nbrOfEvents() - synchAlphabet.nbrOfCommonEvents(currAlphabet);

						//bestValue = currAutomaton.nbrOfStates();
						safeInBest = true;

						continue;
					}
				}
				else
				{

					// Perhaps the best is much more interesting?
					if (safeInBest == true)
					{
						continue;
					}
				}

				// We now know this automaton is quite interesting, but we want as few new events as possible!
				// This value is the number of new events in the automaton
				int value = currAutomaton.nbrOfEvents() - synchAlphabet.nbrOfCommonEvents(currAlphabet);

				// This value is the number of states in the automaton
				//int value = currAutomaton.nbrOfStates();
				if (value < bestValue)
				{
					bestAutomaton = currAutomaton;
					bestEvent = currEvent;
					bestValue = value;
				}
			}
		}

		// Have we a winner?
		if (bestAutomaton != null)
		{
			logger.info("Adding " + bestAutomaton + ", most important event: " + bestEvent + ".");

			// The automaton we add should be accepting in all states!
			Automaton allAcceptingAutomaton = new Automaton(bestAutomaton);
			AutomatonAllAccepting allAccepting = new AutomatonAllAccepting(allAcceptingAutomaton);

			allAccepting.execute();

			//return bestAutomaton;
			return allAcceptingAutomaton;
		}

		// If we haven't found any automaton... then there can be no interesting events, right?
		// Yes there can! But then the subsystem is blocking and we must prove that the blocking
		// states are unreachable!
		return null;
	}

	private void synchTest()
	{
		Automata newAutomata = new Automata();

		for (Iterator autItOne = theAutomata.iterator(); autItOne.hasNext(); )
		{
			Automata synchAutomata = new Automata();
			Automaton currAutomaton = (Automaton) autItOne.next();
			Alphabet currAlphabet = currAutomaton.getAlphabet();

			for (Iterator autItTwo = theAutomata.iterator();
					autItTwo.hasNext(); )
			{
				Automaton otherAutomaton = (Automaton) autItTwo.next();
				Alphabet otherAlphabet = otherAutomaton.getAlphabet();

				Alphabet.intersect(currAlphabet, otherAlphabet);

				if (Alphabet.intersect(currAlphabet, otherAlphabet).size() != 0)
				{
					synchAutomata.addAutomaton(otherAutomaton);
				}
			}

			logger.debug("Synchronizing " + synchAutomata);

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
	}

	/**
	 * Tries to prove that the blocking states in the automaton are reachable by safeEvents.
	 *
	 * It's important that automaton is marked in all states that are not blocking!!
	 */
	private boolean verifyBlockingBySafeEvents(Automaton automaton, Alphabet safeEvents)
	{
		Automaton automatonCopy = new Automaton(automaton);

		// Remove all arcs wich are not safe events
		LinkedList toBeRemoved = new LinkedList();
		for (ArcIterator arcIt = automatonCopy.arcIterator(); arcIt.hasNext(); )
		{
			Arc currArc = arcIt.nextArc();

			if (!safeEvents.contains(currArc.getEvent()))
			{
				toBeRemoved.add(currArc);
			}
		}
		while (toBeRemoved.size() != 0)
		{
			automatonCopy.removeArc((Arc) toBeRemoved.remove(0));
		}

		// logger.info("Nbroftransitions then: " + automaton.nbrOfTransitions() + ", now: " + automatonCopy.nbrOfTransitions());
		// The unmarked states should be blocking in the input of this method!
		automatonCopy.invertMarking();
		automatonCopy.extendMutuallyAccepting(safeEvents);

		// If the initial state is mutually accepting, then the blocking states are
		// guaranteed to be reachable!
		boolean result = automatonCopy.getInitialState().isMutuallyAccepting();

		// If we have blocks, display a (maybe local...) trace that leads to a block!
		if (result == true)
		{
			for (StateIterator stateIt = automatonCopy.stateIterator();
					stateIt.hasNext(); )
			{
				State currState = stateIt.nextState();

				if (currState.isAccepting())
				{
					try
					{
						LabelTrace trace = automatonCopy.getTrace(currState);

						if (trace != null)
						{
							if (trace.size() == 0)
							{
								logger.info("The initial state is blocking.");

								break;
							}

							logger.info("Trace to blocking state: " + trace);

							break;
						}
					}
					catch (Exception ex)
					{
						logger.error(ex);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Removes as many as possible of the transitions with uniqueEvents, but avoids nondeterminism and
	 * destroying the local stateset of the "first automaton"...
	 *
	 * @returns Amount of removed transitions
	 */
	private int removeUniqueEventTransitions(Automaton automaton, Alphabet alpha)
	{
		int nbrOfRemoved = 0;
		int amount = 0;

		// Find percentage unique/total transitions
		int nbrOfUnique = 0;

		for (ArcIterator arcIt = automaton.arcIterator(); arcIt.hasNext(); )
		{
			if (alpha.contains(arcIt.nextArc().getEvent()))
			{
				nbrOfUnique++;
			}
		}

		java.text.DecimalFormat df = new java.text.DecimalFormat();

		logger.warn("Percentage unique: " + df.format(((double) 100 * nbrOfUnique) / automaton.nbrOfTransitions()) + " u: " + nbrOfUnique + " t: " + automaton.nbrOfTransitions());

		//logger.warn("Percentage unique: " + ((double) 100*nbrOfUnique)/automaton.nbrOfTransitions());
		// Loop over states
		for (StateIterator stateIt = automaton.safeStateIterator();
				stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			// Loop over outgoing arcs
			StateSet states = new StateSet();

			for (ArcIterator arcIt = currState.safeOutgoingArcsIterator();
					arcIt.hasNext(); )
			{
				Arc currArc = arcIt.nextArc();

				// Is this a transition with a unique event?
				if (alpha.contains(currArc.getEvent()))
				{

					// Unique event selfloops can always be removed!
					if (currArc.isSelfLoop())
					{

						// removeArcs.addArc(currArc);
						automaton.removeArc(currArc);

						nbrOfRemoved++;

						continue;
					}

					// Only one unique transition to each state is necessary
					if (states.contains(currArc.getToState()))
					{

						// removeArcs.addArc(currArc);
						automaton.removeArc(currArc);

						nbrOfRemoved++;

						continue;
					}
					else
					{
						states.add(currArc.getToState());
					}
				}
			}

			// Is there only one outgoing arc left and is it with an unique event?
			if (currState.nbrOfOutgoingArcs() == 1)
			{
				Arc currArc = currState.outgoingArcsIterator().nextArc();

				// Is that transition a unique event?
				if (alpha.contains(currArc.getEvent()))
				{
					amount++;

					// Merge the states!
					// All incoming should go to currArc.getToState() instead
					// logger.info("Merging states " + currArc.getFromState() + " and " + currArc.getToState());
					State toState = currArc.getToState();

					for (ArcIterator arcIt = currState.incomingArcsIterator();
							arcIt.hasNext(); )
					{
						Arc arc = arcIt.nextArc();

						automaton.addArc(new Arc(arc.getFromState(), toState, arc.getEvent()));

						// arcIt.nextArc().setToState(toState);
					}

					// Don't leave the automaton without initial state!
					if (currState.isInitial())
					{
						automaton.setInitialState(toState);
					}

					// Safely remove the old state
					automaton.removeState(currState);

					nbrOfRemoved++;
				}
			}
		}

		logger.warn("Removed: " + nbrOfRemoved + ", amount of sole and unique transitions " + amount);

		return nbrOfRemoved;
	}

	/**
	 * Examines how many unique events there are in automataA compared to automataB. If automataB contains
	 * automata from automataA, these are ignored so the method is really "\Sigma_A - \Sigma_(B-A)".
	 */
	private Alphabet findUniqueEvents(Automata automataA, Automata automataB)
	{
		Alphabet result = null;

		// Make sure automataA is not included in automataB (which would make the test really stupid)
		Automata automataBminusA = new Automata(automataB);

		for (AutomatonIterator autIt = automataA.iterator(); autIt.hasNext(); )
		{
			automataBminusA.removeAutomaton(autIt.nextAutomaton().getName());
		}

		// Nothing left?
		if (automataBminusA.size() == 0)
		{

			// Everything is unique!!
			return automataA.getUnionAlphabet();
		}

		Alphabet alphabetA = automataA.getUnionAlphabet();
		Alphabet alphabetB = automataBminusA.getUnionAlphabet();

		alphabetA.minus(alphabetB);

		result = alphabetA;

		return result;
	}

	/**
	 * Examines if the blocking states (or rather - the not mutually accepting states) in
	 * theAutomaton are reachable by making a language inclusion test.
	 */
	private void languageInclusionTest(Automaton theAutomaton)
	{
		Automaton automatonCopy = new Automaton(theAutomaton);

		automatonCopy.extendMutuallyAccepting(theAutomaton.getAlphabet());

		// Remove blocking states from automatonCopy...
		boolean statesRemoved = false;

		for (StateIterator stateIt = theAutomaton.stateIterator();
				stateIt.hasNext(); )
		{
			State currState = automatonCopy.getState(stateIt.nextState());

			if (!currState.isMutuallyAccepting())
			{
				statesRemoved = true;

				automatonCopy.removeState(currState);
			}
		}

		if (!statesRemoved)
		{
			return;
		}

		try
		{
			if (AutomataVerifier.verifyModularInclusion(theAutomata, new Automata(automatonCopy)))
			{
				logger.warn("WOHOHOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO!");
			}
		}
		catch (Exception oj)
		{
			logger.error(oj);
		}

		return;
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
		Automata theAutomataCopy = new Automata(theAutomata);

		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			logger.info("Building safe events for automaton " + currAutomaton + "...");
			buildSafeEvents(currAutomaton, theAutomataCopy);

			if (executionDialog != null)
			{
				executionDialog.setProgress(currAutomaton.getIndex());
			}

			if (stopRequested)
			{
				return;
			}
		}
	}

	/**
	 * Finds the safe events for currAutomaton. theAutomataCopy is a (deep) copy of
	 * theAutomata that this method will fiddle with a lot, you should NOT pass
	 * theAutomata as an argument instead of theAutomataCopy (a deep copy of theAutomata).
	 *
	 * @see buildSafeEvents()
	 */
	private void buildSafeEvents(Automaton currAutomaton, Automata theAutomataCopy)
	{
		Alphabet theSafeEvents = safeEventsMap.getEvents(currAutomaton);

		addSafeEvents(theSafeEvents, currAutomaton, theAutomataCopy);
	}

	/**
	 * Add safe events to theSafeEvents according to the safeness of the events in theAutomaton.
	 *
	 * @param theAutomataCopy Will be fiddled with, don't bring sensitive information here.
	 */
	private void addSafeEvents(Alphabet theSafeEvents, Automaton currAutomaton, Automata theAutomataCopy)
	{
		addSafeEvents(theSafeEvents, new Automata(currAutomaton), theAutomataCopy);
	}

	/**
	 * Add safe events to theSafeEvents according to the safeness of the events in selectedAutomata.
	 *
	 * @param theAutomataCopy Will be fiddled with, don't bring sensitive information here.
	 */
	private void addSafeEvents(Alphabet theSafeEvents, Automata selectedAutomata, Automata theAutomataCopy)
	{

		// Get the alphabets of the selected automata
		Alphabet theAlphabet = selectedAutomata.getUnionAlphabet();

		// Iterate over the alphabet
		for (Iterator evIt = theAlphabet.iterator(); evIt.hasNext(); )
		{
			if (stopRequested)
			{
				return;
			}

			LabeledEvent currEvent = (LabeledEvent) evIt.next();

			// Perhaps we have already added this event?
			if (theSafeEvents.contains(currEvent))
			{

				//logger.debug("Already established to be safe!");
				continue;
			}

			// We assume that the system is controllable (which we can NOT do in general).
			// If the event is uncontrollable and we're examining a plant, we know that the
			// event is safe (if the plant alphabets are disjoint!)... I assume this for now.
			// This should be commented out... really...

			/*
			if (!currEvent.isControllable() && theAutomaton.isPlant())
			{
					logger.info("Uncontrollable event in controllable plant => safe!");
					safeEventsMap.addEvent(theAutomaton, currEvent);
					continue;
			}
			*/

			// Otherwise make a safeness check
			boolean isSafe = checkSafeness(theAutomataCopy, selectedAutomata, currEvent);

			if (isSafe)
			{
				if (SupremicaProperties.verboseMode())
				{
					logger.debug("The event " + currEvent + " is safe.");
				}

				try
				{
					theSafeEvents.addEvent(currEvent);
				}
				catch (Exception ex)
				{
					logger.error(ex);
					requestStop();
				}
			}
			else
			{
				if (SupremicaProperties.verboseMode())
				{
					logger.debug("The event " + currEvent + " is unsafe.");
				}
			}

			if (stopRequested)
			{
				return;
			}
		}
	}

	/**
	 * Sets the AutomatonType of the automata and the controllability of the events to
	 * make way for the controllability check in checkSafeness().
	 */
	private void setAttributes(Automata totalSystem, Automaton thePlantAutomaton, LabeledEvent unconEvent)
	{
		setAttributes(totalSystem, new Automata(thePlantAutomaton), unconEvent);
	}

	/**
	 * Sets the AutomatonType of the automata and the controllability of the events to
	 * make way for the controllability check in checkSafeness().
	 */
	private void setAttributes(Automata totalSystem, Automata thePlantAutomata, LabeledEvent unconEvent)
	{
		for (Iterator autIt = totalSystem.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			if (thePlantAutomata.containsAutomaton(currAutomaton.getName()))    // Equal name? Is this enough?
			{
				currAutomaton.setType(AutomatonType.Plant);
			}
			else
			{
				currAutomaton.setType(AutomatonType.Specification);
			}

			Alphabet currAlphabet = currAutomaton.getAlphabet();

			for (Iterator eventIt = currAlphabet.iterator();
					eventIt.hasNext(); )
			{

				// Make all events controllable except for the chosen one (unconEvent)
				LabeledEvent currEvent = (LabeledEvent) eventIt.next();

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
		return checkSafeness(totalSystem, new Automata(thePlantAutomaton), currEvent);
	}

	/**
	 * Examines if the event currEvent is always enabled in totalSystem if
	 * it is enabled in thePlantAutomata (it is never disabled by totalSystem).
	 */
	private boolean checkSafeness(Automata totalSystem, Automata thePlantAutomata, LabeledEvent currEvent)
	{
		setAttributes(totalSystem, thePlantAutomata, currEvent);

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

			// Disable warning messages during verification (we know there will be a lot of those)
			LoggerFilter filter = LoggerFactory.getLoggerFilter();
			boolean allowWarn = filter.allowWarn();

			filter.setAllowWarn(false);

			theVerifier = new AutomataVerifier(totalSystem, synchronizationOptions, verificationOptions);
			isSafe = theVerifier.verify();

			filter.setAllowWarn(allowWarn);
		}
		catch (Exception ex)
		{
			logger.error(ex);

			// logger.debug(ex);
		}

		return isSafe;
	}

	/**
	 * Finds the most relevant events and returns them ordered by level of interest.
	 * This is of course just a heuristic.
	 *   It measures the frequence of appearance of transitions starting in a non-mutually
	 * accepting state and ending in a mutually accepting state.
	 */
	private Vector getInterestingEvents(Automaton theAutomaton)
	{
		int alphabetSize = theAutomaton.nbrOfEvents();
		int[] value = new int[alphabetSize];
		int[] blockValue = new int[alphabetSize];
		Alphabet theAlphabet = new Alphabet(theAutomaton.getAlphabet());

		theAlphabet.setIndicies();

		boolean noValue = true;

		// Give score to interesting events
		for (ArcIterator arcIt = theAutomaton.arcIterator(); arcIt.hasNext(); )
		{
			Arc currArc = arcIt.nextArc();

			// Self-loops are not interesting
			if (currArc.isSelfLoop())
			{
				continue;
			}

			// Is this an arc that could help us with the mutual nonblocking?
			int index = theAlphabet.getEvent(currArc.getEvent().getLabel()).getSynchIndex();

			//if (currArc.getToState().isMutuallyAccepting() && !currArc.getToState().isForbidden() && !currArc.getFromState().isMutuallyAccepting())
			if (currArc.getToState().isMutuallyAccepting() &&!currArc.getFromState().isMutuallyAccepting())
			{
				noValue = false;

				value[index]++;
			}

			// If there is no way to prove nonblocking, maybe we can prove it's not blocking!
			if (noValue &&!currArc.getToState().isMutuallyAccepting() && currArc.getFromState().isMutuallyAccepting())
			{
				blockValue[index]++;
			}

			/*
			// The from-state should be unacceptable.  :o)  Not funny.
			int index = theAlphabet.getEventWithLabel(currArc.getEvent().getLabel()).getSynchIndex();
			if (!currArc.getFromState().isMutuallyAccepting())
			{
					value[index] = value[index] + 1;
			}
			else
			{
					continue;
			}

			// If the to-state is mutually accepting - add a bonus!
			if (currArc.getToState().isMutuallyAccepting())
			{
					value[index] = value[index] + alphabetSize*alphabetSize;
			}
			*/
		}

		// Are there no results in the value array?
		if (noValue)
		{

			// It seems blocking! Use blockValue instead!
			logger.error("This subsystem has blocking states, but maybe they're unreachable...");

			value = blockValue;

			//We can merge all states that are not accepting! But the gain is limited?
		}

		// Sort the result
		Vector result = new Vector(alphabetSize, 0);

		for (int i = 0; i < alphabetSize; i++)
		{
			int high = 0;

			for (int j = 1; j < alphabetSize; j++)
			{
				if (value[high] < value[j])
				{
					high = j;
				}
			}

			// If the best event had value 0, there's no point in continuing.
			if (value[high] == 0)
			{
				break;
			}

			// Add the best event to the result
			result.add(i, theAlphabet.getEventWithIndex(high));

			//logger.debug("Event: " + result.elementAt(i) + ", " + value[high] + ".");
			value[high] = -1;
		}

		return result;
	}

	/**
	 * Adjusts the state names to be easier to identify when synchronized with other automata.
	 */
	private void adjustStateNames(Automaton theAutomaton)
	{
		int count = 0;

		for (StateIterator stateIt = theAutomaton.stateIterator();
				stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			currState.setName(count++ + "_");
		}
	}

	/**
	 * Projects the mutually accepting status of currSynchAutomaton onto currAutomaton.
	 */
	private void projectAcceptingStatus(Automaton currAutomaton, Automaton currSynchautomaton)
	{

		// Set all states in currAutomaton as accepting
		AutomatonAllMutuallyAccepting allAccepting = new AutomatonAllMutuallyAccepting(currAutomaton);

		allAccepting.execute();

		// Check to see if the corresponding states in currSynchautomaton are accepting
		for (StateIterator stateIt = currSynchautomaton.stateIterator();
				stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			if (!currState.isMutuallyAccepting())
			{
				String stateName = currState.getName();
				String projectedStateName = stateName.substring(0, stateName.indexOf('_') + 1);
				State realState = currAutomaton.getStateWithName(projectedStateName);

				realState.setMutuallyAccepting(false);
			}
		}
	}

	/**
	 * Compute NB?, ignores all events [and their arcs!] not in the given alphabet.
	 *
	 * TODO: we should switch to BDDs only if the system is becoming too large
	 *       (BDD algos have some startup latency...)
	 *
	 * I think the exception is thrown only if there is something wrong with th input automata
	 * (non existing events in transitions or controllability inconsistency)
	 *
	 *    /Arash
	 */
	public boolean isMutuallyCoaccessibleBDD(Automata automata, Alphabet alphabet)
		throws Exception
	{
		AutomataBDDVerifier abf = new AutomataBDDVerifier(automata, alphabet);
		boolean ret = abf.isNonblocking();

		abf.cleanup();

		return ret;
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

	protected Alphabet getEvents(Automaton theAutomaton)
	{
		Alphabet theEvents = (Alphabet) theMap.get(theAutomaton.getName());

		if (theEvents == null)
		{
			theEvents = new Alphabet();

			theMap.put(theAutomaton.getName(), theEvents);
		}

		return theEvents;
	}

	public int numberOfEvents(Automaton theAutomaton)
	{
		Alphabet theEvents = (Alphabet) theMap.get(theAutomaton.getName());

		if (theEvents == null)
		{
			return 0;
		}

		return theEvents.size();
	}

	public EventIterator eventIterator(Automaton theAutomaton)
	{
		return getEvents(theAutomaton).iterator();
	}

	public void addEvent(Automaton theAutomaton, LabeledEvent theEvent)
	{
		Alphabet theEvents = getEvents(theAutomaton);

		try
		{
			if (!theEvents.contains(theEvent.getLabel()))
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

	public boolean containsEvent(Automaton theAutomaton, LabeledEvent theEvent)
	{
		Alphabet theEvents = getEvents(theAutomaton);

		return theEvents.contains(theEvent.getLabel());
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		Set keys = theMap.keySet();

		for (Iterator keyIt = keys.iterator(); keyIt.hasNext(); )
		{
			String automatonName = (String) keyIt.next();

			sb.append(automatonName + ": ");

			Alphabet theEvents = (Alphabet) theMap.get(automatonName);

			for (Iterator evIt = theEvents.iterator(); evIt.hasNext(); )
			{
				LabeledEvent currEvent = (LabeledEvent) evIt.next();

				sb.append(currEvent.getLabel() + " ");
			}

			sb.append("\n");
		}

		return sb.toString();
	}
}
