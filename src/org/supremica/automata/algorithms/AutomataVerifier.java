
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
import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.EventIterator;
import org.supremica.automata.AutomatonIterator;
import org.supremica.automata.State;
import org.supremica.automata.Arc;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.algorithms.standard.Determinizer;
import org.supremica.util.BDD.*;

/**
 * For performing verification. Uses AutomataSynchronizerExecuter for the actual verification work.
 *
 *@author  ka
 *@created  November 28, 2001
 *@see  AutomataSynchronizerExecuter
 */
public class AutomataVerifier
	implements Stoppable
{
	private static Logger logger = LoggerFactory.createLogger(AutomataVerifier.class);
	private Automata theAutomata;
	private int nbrOfExecuters;

	// MF Started puting in all these timer.start/stop but...
	// private ActionTimer timer = new ActionTimer();

	/**
	 * Map from an uc LabeledEvent to the Set of plant Automaton-objects that contain this event
	 *
	 *@see  AlphabetAnalyzer
	 */
	private HashMap uncontrollableEventToPlantMap = null;
	private AutomataSynchronizerHelper synchHelper;
	private ArrayList synchronizationExecuters = new ArrayList();
	private StateMemorizer potentiallyUncontrollableStates;
	private int[] initialState;

	// Used by findUncontrollableStates
	private AutomataSynchronizerHelper uncontrollabilityCheckHelper;
	private ArrayList uncontrollabilityCheckExecuters = new ArrayList();

	// Used in excludeUncontrollableStates
	private int stateAmountLimit;
	private int stateAmount;
	private int attempt;

	/**
	 * Determines if more detailed information on the progress of things should be displayed.
	 *
	 *@see SynchronizationOptions
	 */
	private boolean verboseMode;
	private VerificationOptions verificationOptions;
	private SynchronizationOptions synchronizationOptions;

	/** For stopping execution. */
	private boolean stopRequested = false;
	private Stoppable threadToStop = null;

	/** For verifying supervisors by one uncontrollable event at a time. */
	private boolean oneEventAtATime;

	/** For error message when Supremica can't be certain on the answer. */
	private boolean failure = false;

	public AutomataVerifier(Automata theAutomata, SynchronizationOptions synchronizationOptions, VerificationOptions verificationOptions)
		throws IllegalArgumentException, Exception
	{

		/// logger.debug("DEBUG.......... " + theAutomata.size() ); // ARASH: DEBUG
		Automaton currAutomaton;
		State currInitialState;

		this.theAutomata = theAutomata;
		this.verificationOptions = verificationOptions;
		this.synchronizationOptions = synchronizationOptions;
		nbrOfExecuters = synchronizationOptions.getNbrOfExecuters();
		verboseMode = synchronizationOptions.verboseMode();
		oneEventAtATime = verificationOptions.getOneEventAtATime();

		// The helper must be initialized here (this early) only because of the
		// executionDialog, I think...
		// BUT THE EXECUTIONDIALOG IS NOT USED UNTIL MUCH LATER?!!!
		synchHelper = new AutomataSynchronizerHelper(theAutomata, synchronizationOptions);

		// Build the initial state  (including 2 status fields)
		initialState = AutomataIndexFormHelper.createState(theAutomata.size());

		Iterator autIt = theAutomata.iterator();

		while (autIt.hasNext())
		{
			currAutomaton = (Automaton) autIt.next();
			currInitialState = currAutomaton.getInitialState();
			initialState[currAutomaton.getIndex()] = currInitialState.getIndex();
		}
	}

	public static String validOptions(Automata theAutomata, VerificationOptions verificationOptions)
	{

		// Check IDD
		if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.IDD)
		{
			return "The IDD Algorithm is not fully implemented yet";
		}

		// Check Controllability
		if (verificationOptions.getVerificationType() == VerificationType.Controllability)
		{
			if (theAutomata.size() < 2)
			{
				return "At least two automata must be selected";
			}

			if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.Modular)
			{
				if (!theAutomata.isAllEventsPrioritized())
				{
					return "All events must be prioritized in the modular algorithm";
				}
			}
		}

		// Check Nonblocking
		if (verificationOptions.getVerificationType() == VerificationType.Nonblocking)
		{
			if (theAutomata.size() < 1)
			{
				return "At least one automaton must be selected!";
			}

			if (!theAutomata.hasAcceptingState())
			{
				return "Some automaton has no marked states!";
			}

			if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.Modular)
			{
				return "The modular nonblocking algorithm \n" + "is not fully implemented!";
			}
		}

		// Check MutuallyNonblocking
		if (verificationOptions.getVerificationType() == VerificationType.MutuallyNonblocking)
		{
			if (theAutomata.size() < 1)
			{
				return "At least one automaton must be selected!";
			}

			if (!theAutomata.hasAcceptingState())
			{
				return "Some automaton has no marked states!";
			}

			if (verificationOptions.getAlgorithmType() != VerificationAlgorithm.Modular)
			{
				return "The mutual nonblocking algorithm \n" + "is a modular algorithm!";
			}
		}

		// Check Language Inclusion
		if (verificationOptions.getVerificationType() == VerificationType.LanguageInclusion)
		{
			if (theAutomata.size() < 1)
			{
				return "At least one automaton must be selected.";
			}

			if (ActionMan.getGui().getUnselectedAutomata().size() < 1)
			{
				return "At least one automaton must be unselected.";
			}

			if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.Modular)
			{
				if (!theAutomata.isAllEventsPrioritized())
				{
					return "All event must be prioritized in the modular algorithm.";
				}
			}
		}

		// Everything seems OK!
		return null;
	}

	/**
	 * This is an attempt to clean up this interface.
	 */
	public boolean verify()
		throws UnsupportedOperationException
	{
		try
		{
			if (verificationOptions.getVerificationType() == VerificationType.Controllability)
			{
				if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.Monolithic)
				{
					return monolithicControllabilityVerification();
				}
				else if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.Modular)
				{
					return modularControllabilityVerification();
				}
				else if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.BDD)
				{
					return BDDControllabilityVerification(theAutomata);
				}
				else
				{
					throw new UnsupportedOperationException("The selected algorithm is not implemented");
				}
			}
			else if (verificationOptions.getVerificationType() == VerificationType.Nonblocking)
			{
				if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.Monolithic)
				{
					return monolithicNonblockingVerification();
				}
				else if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.BDD)
				{
					return BDDNonBlockingVerification();
				}
				else if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.Modular)
				{

					// This algorithm is under implementation!!
					return modularNonBlockingVerification();

					// This algorithm only verifies pairwise nonblocking!!!
					// return pairwiseNonblockingVerification();
				}
				else
				{
					throw new UnsupportedOperationException("The selected algorithm is not implemented");
				}
			}
			else if (verificationOptions.getVerificationType() == VerificationType.MutuallyNonblocking)
			{
				if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.Modular)
				{

					// This algorithm is under implementation!!
					return modularMutuallyNonblockingVerification();

					// This algorithm only verifies pairwise nonblocking!!!
					// return pairwiseNonblockingVerification();
				}
				else
				{
					throw new UnsupportedOperationException("The selected algorithm is not implemented");
				}
			}
			else if (verificationOptions.getVerificationType() == VerificationType.LanguageInclusion)
			{
				if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.BDD)
				{
					return BDDLanguageInclusionVerification();
				}
				else if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.Monolithic)
				{

					// Language inclusion is performed as a controllability verification!
					return monolithicControllabilityVerification();
				}
				else if (verificationOptions.getAlgorithmType() == VerificationAlgorithm.Modular)
				{

					// Language inclusion is performed as a controllability verification!
					return modularControllabilityVerification();
				}
				else
				{
					throw new UnsupportedOperationException("The selected algorithm is not implemented");
				}
			}
			else
			{
				throw new UnsupportedOperationException("The selected type of verification is not implemented");
			}
		}
		catch (Exception e)
		{
			logger.error("Exception in AutomataVerifier: " + e);

			throw new RuntimeException(e);    // Try change this later
		}
	}

	/**
	 * Prepares the helper and the automataindexform for language inclusion...
	 *
	 *@param inclusionAutomata The automata that should be verified for inclusion
	 */
	public void prepareForLanguageInclusion(Automata inclusionAutomata)
	{

		/* WHY WON'T THIS WORK!?!? GRRR. /hugo
		// Make sure the alphabets have the right relation
		Automata exclusionAutomata = new Automata();
		for (AutomatonIterator autIt = theAutomata.iterator(); autIt.hasNext();)
		{
				Automaton currAut = autIt.nextAutomaton();
				if (!inclusionAutomata.containsAutomaton(currAut))
				{
						exclusionAutomata.addAutomaton(currAut);
				}

				// The "exclusionAutomata"'s alphabet must be included in the "inclusionAutomata"
				if (Alphabet.minus(exclusionAutomata.getUnionAlphabet(),inclusionAutomata.getUnionAlphabet()).size() > 0)
				{
						logger.warn("Warning, the alphabets are not well related for language inclusion.");
				}
		}
		*/

		// After these preparations, controllability verification verifies language inclusion
		synchHelper.getAutomataIndexForm().defineTypeIsPlantTable(inclusionAutomata);

		AlphabetAnalyzer alphabetAnalyzer = new AlphabetAnalyzer(theAutomata);

		uncontrollableEventToPlantMap = alphabetAnalyzer.getEventToAutomataMap(inclusionAutomata);

		// This last one is not really good... we'd like to do this only once! Perhaps
		// a switch in the synchronizeroptions or verificationoptions instead? FIXA!!
		synchHelper.considerAllEventsUncontrollable();
	}

	/**
	 * Performs modular mutually nonblocking verification on theAutomata.
	 *
	 *@return  true if mutually nonblocking, false if not or false (with error message) if don't know.
	 *@exception  Exception Description of the Exception
	 *@see  AutomataVerificationWorker
	 */
	private boolean modularMutuallyNonblockingVerification()
		throws Exception
	{

		// Ensure individual nonblocking
		if (!isIndividuallyNonBlocking())
		{
			return false;
		}

		//MutuallyNonblockingVerifier theVerifier = new MutuallyNonblockingVerifier(theAutomata);
		MutuallyNonblockingVerifier theVerifier = new MutuallyNonblockingVerifier(theAutomata, synchHelper);

		// Prepare for stopping the verifier
		threadToStop = theVerifier;

		return theVerifier.isMutuallyNonblocking();
	}

	/**
	 * Performs modular controllability verification on theAutomata..
	 *
	 *@return  true if controllable, false if not or false (with error message) if don't know.
	 *@exception  Exception Description of the Exception
	 *@see  AutomataVerificationWorker
	 */
	private boolean modularControllabilityVerification()
		throws Exception
	{
		AlphabetAnalyzer alphabetAnalyzer = new AlphabetAnalyzer(theAutomata);

		if (uncontrollableEventToPlantMap == null)
		{
			uncontrollableEventToPlantMap = alphabetAnalyzer.getUncontrollableEventToPlantMap();
		}

		potentiallyUncontrollableStates = synchHelper.getStateMemorizer();

		Automata selectedAutomata = new Automata();
		boolean allModulesControllable = true;
		boolean[] typeIsSupSpecTable = synchHelper.getAutomataIndexForm().getTypeIsSupSpecTable();
		boolean[] controllableEventsTable = synchHelper.getAutomataIndexForm().getControllableEventsTable();

		// Iterate over supervisors/specifications
		loop:
		for (AutomatonIterator supIt = theAutomata.iterator();
				supIt.hasNext(); )
		{
			Automaton currSupervisorAutomaton = supIt.nextAutomaton();

			// To enable the overriding the AutomatonType of automata we use typeIsSupSpecTable!
			// if ((currSupervisorAutomaton.getType() == AutomatonType.Supervisor) || (currSupervisorAutomaton.getType() == AutomatonType.Specification))
			// if (!typeIsPlantTable[currSupervisorAutomaton.getIndex()])
			if (typeIsSupSpecTable[currSupervisorAutomaton.getIndex()])
			{

				// This is a relevant automaton!
				selectedAutomata.addAutomaton(currSupervisorAutomaton);

				// Examine uncontrollable events in currSupervisorAutomaton
				// and select plants containing these events
				for (EventIterator eventIt = currSupervisorAutomaton.eventIterator();
						eventIt.hasNext(); )
				{
					LabeledEvent currEvent = eventIt.nextEvent();

					// To enable overriding the controllability status of events!
					//if (!currEvent.isControllable())
					if (!controllableEventsTable[currEvent.getSynchIndex()])
					{

						// Note that in the language inclusion case, the
						// uncontrollableEventToPlantMap has been adjusted...
						if (uncontrollableEventToPlantMap.get(currEvent) != null)
						{

							// Iterate over the plants and add them to selectedAutomata
							for (Iterator plantIt = ((Set) uncontrollableEventToPlantMap.get(currEvent)).iterator();
									plantIt.hasNext(); )
							{
								Automaton currPlantAutomaton = (Automaton) plantIt.next();

								if (!selectedAutomata.containsAutomaton(currPlantAutomaton))
								{
									selectedAutomata.addAutomaton(currPlantAutomaton);
								}
							}

							if (oneEventAtATime)
							{
								if (stopRequested)
								{
									return false;
								}

								if (selectedAutomata.size() > 1)
								{

									// Check module
									allModulesControllable = allModulesControllable && moduleIsControllable(selectedAutomata);

									// Stop if uncontrollable
									if (!allModulesControllable)
									{
										if (verboseMode)
										{
											logger.info("Uncontrollable state found.");
										}

										break loop;
									}

									// Clean selectedAutomata before continuing
									selectedAutomata.clear();
									selectedAutomata.addAutomaton(currSupervisorAutomaton);
								}
							}
						}
					}
				}

				if (!oneEventAtATime)
				{
					if (stopRequested)
					{
						return false;
					}

					if (selectedAutomata.size() > 1)
					{

						// Check module
						allModulesControllable = allModulesControllable && moduleIsControllable(selectedAutomata);

						// Stop if uncontrollable
						if (!allModulesControllable)
						{
							if (verboseMode)
							{
								logger.info("Uncontrollable state found.");
							}

							break loop;
						}
					}
				}

				// Clean selectedAutomata before continuing
				selectedAutomata.clear();
			}
		}

		// Did the loop finish without failure?
		if (failure)
		{
			logger.warn("Supremica's modular verification algorithm can't solve this " + "problem. Try the monolithic or BDD algorithm instead. There are " + potentiallyUncontrollableStates.size() + " states that perhaps makes this system uncontrollable.");

			return false;
		}

		return allModulesControllable;
	}

	/**
	 * Performs modular controllablity verification on one module using AutomataSynchronizerExecuter.
	 *
	 *@param  selectedAutomata the automata that should be verified
	 *@return  true if controllable, false if not or false if don't know.
	 *@exception  Exception Description of the Exception
	 *@see  AutomataSynchronizerExecuter
	 */

	//private boolean moduleIsControllable(ArrayList selectedAutomata)
	private boolean moduleIsControllable(Automata selectedAutomata)
		throws Exception
	{

		// Clear the hash-table and set some variables in the synchronization helper
		synchHelper.clear();
		synchHelper.setRememberUncontrollable(true);
		synchHelper.addState(initialState);

		if (stopRequested)
		{
			return false;
		}

		// Initialize the synchronizationExecuters
		synchronizationExecuters.clear();

		for (int i = 0; i < nbrOfExecuters; i++)
		{
			AutomataSynchronizerExecuter currSynchronizationExecuter = new AutomataSynchronizerExecuter(synchHelper);

			synchronizationExecuters.add(currSynchronizationExecuter);
		}

		// Start all the synchronization executers and wait for completion
		// For the moment we assume that we only have one thread
		for (int i = 0; i < synchronizationExecuters.size(); i++)
		{
			AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(i);

			currExec.selectAutomata(selectedAutomata);
			currExec.start();
		}

		((AutomataSynchronizerExecuter) synchronizationExecuters.get(0)).join();

		if (stopRequested)
		{
			return false;
		}

		// The name of the "synchronized" automata
		StringBuffer automataNames = new StringBuffer();

		if (verboseMode)
		{

			// For printing the names of the automata in selectedAutomata
			for (Iterator autIt = selectedAutomata.iterator();
					autIt.hasNext(); )
			{
				automataNames = automataNames.append(((Automaton) autIt.next()).getName());

				if (autIt.hasNext())
				{
					automataNames = automataNames.append("||");
				}
			}
		}

		// Was the result uncontrollable?
		if (!synchHelper.getAutomataIsControllable())
		{

			// Try to add some more automata
			// Make array with indices of selected automata to remember which were originally selected
			int[] automataIndices = new int[selectedAutomata.size()];
			int i = 0;

			for (AutomatonIterator autIt = selectedAutomata.iterator();
					autIt.hasNext(); )
			{
				automataIndices[i++] = autIt.nextAutomaton().getIndex();
			}

			if (verboseMode)
			{
				String states;
				int size = potentiallyUncontrollableStates.size(automataIndices);

				if (size == 1)
				{
					states = "one state";
				}
				else if (size == 2)
				{
					states = "two states";
				}
				else
				{
					states = size + " states";
				}

				logger.info("'" + automataNames + "' has " + states + " that might be uncontrollable...");
			}

			// Get a sorted array of indexes of automata with similar alphabets
			int[] similarAutomata = findSimilarAutomata(theAutomata, selectedAutomata);

			if (similarAutomata == null)
			{

				// There are no similar automata, this module must be uncontrollable
				if (verboseMode)
				{

					// Print the uncontrollable state(s)...
					synchHelper.printUncontrollableStates(automataIndices);
				}

				return false;
			}

			if (verboseMode)
			{
				logger.info("There are " + similarAutomata.length + " automata with similar alphabets...");
			}

			// Make nbrOfAttempts attempts on prooving controllability and
			// uncontrollability alternatingly and then give up
			int nbrOfAttempts = verificationOptions.getNbrOfAttempts();

			stateAmount = 1;

			for (attempt = 1; attempt <= nbrOfAttempts; attempt++)
			{
				if (verboseMode)
				{
					logger.debug("Attempt number " + attempt + ".");
				}

				// Have we already added all similar automata?
				if (similarAutomata.length == selectedAutomata.size() - automataIndices.length)
				{

					// Try to find more similarities
					int[] moreSimilarAutomata = findSimilarAutomata(theAutomata, selectedAutomata);

					if (moreSimilarAutomata == null)
					{

						// There were no more automata to add this is monolithically uncontrollable!
						return false;
					}

					int[] newSimilarAutomata = new int[similarAutomata.length + moreSimilarAutomata.length];

					if (moreSimilarAutomata != null)
					{
						if (verboseMode)
						{
							logger.info("All similar automata are already added, " + "trying to add some more...");
						}

						System.arraycopy(similarAutomata, 0, newSimilarAutomata, 0, similarAutomata.length);
						System.arraycopy(moreSimilarAutomata, 0, newSimilarAutomata, similarAutomata.length, moreSimilarAutomata.length);

						similarAutomata = newSimilarAutomata;
					}
					else
					{
						if (verboseMode)
						{
							logger.info("All similar automata are already added, " + "no chance for controllability.");

							// Print the uncontrollable state(s)...
							synchHelper.printUncontrollableStates();

							// Print event trace reaching uncontrollable state
							// synchHelper.displayTrace();  // CAN'T BE DONE... TRACE NOT REMEMBERED... FIXA!
							// Print info on amount of states examined
							// synchHelper.displayInfo(); // This is done always in AutomataVerificationWorker
						}

						return false;
					}
				}

				// Add the similar automata in hope of removing uncontrollable
				// states from potentiallyUncontrollableStates...
				excludeUncontrollableStates(similarAutomata, selectedAutomata, automataIndices);

				if (stopRequested)
				{
					return false;
				}

				// Are there any potentially uncontrollable states left?
				if (potentiallyUncontrollableStates.size(automataIndices) > 0)
				{
					if (!verificationOptions.getSkipUncontrollabilityCheck())
					{
						if (verboseMode)
						{
							logger.info("Couldn't prove controllability, " + "trying to prove uncontrollability...");
						}

						// Try to prove remaining states in the stateMemorizer as being uncontrollable
						if (findUncontrollableStates(automataIndices))
						{

							// Uncontrollable state found!
							if (verboseMode)
							{    // Print the uncontrollable state(s)...
								uncontrollabilityCheckHelper.printUncontrollableStates();

								// Print event trace reaching uncontrollable state
								uncontrollabilityCheckHelper.displayTrace();
							}

							return false;
						}
					}
					else
					{
						if (verboseMode)
						{
							logger.info("Skipped uncontrollability check!");
						}
					}
				}
				else
				{

					// All uncontrollable states were removed!
					break;
				}
			}

			if (potentiallyUncontrollableStates.size(automataIndices) > 0)
			{

				// There are still some uncontrollable states that we're not sure as of being either
				// controllable or uncontrollable. We now have no idea what so ever on the
				// controllability so... we chicken out and give up.
				// Print remaining suspected uncontrollable state(s)
				if (verboseMode)
				{
					logger.info("Unfortunately the following states might be uncontrollable...");
					synchHelper.printUncontrollableStates(automataIndices);
				}

				failure = true;

				return false;
			}
		}

		// Nothing bad has happened. Very nice!
		if (verboseMode)
		{
			logger.info("'" + automataNames + "' is controllable.");
		}

		return true;
	}

	/**
	 * Finds similar automata and sorts these automata in a smart way...
	 *
	 *@param  selectedAutomata the selected automata in the current "composition".
	 *@param  theAutomata reference to the global variable with the same name... eh...
	 *@return an int array with indexes of interesting automata in order of interesting interest.
	 *@see  #compareAlphabets(org.supremica.automata.Alphabet, org.supremica.automata.Alphabet)
	 *@see  #excludeUncontrollableStates(int[], java.util.ArrayList, int[])
	 */
	private int[] findSimilarAutomata(Automata theAutomata, Automata selectedAutomata)
		throws Exception
	{
		int amountOfSelected = selectedAutomata.size();
		int amountOfAutomata = theAutomata.size();
		int amountOfUnselected = amountOfAutomata - amountOfSelected;

		// Are there any automata to find in the first place?
		if (amountOfUnselected == 0)
		{
			return null;
		}

		// Compute the union alphabet of the automata in selectedAutomata
		Alphabet synchAlphabet = selectedAutomata.getUnionAlphabet();

		// Do the work, compare the new automata with the already selected
		Automaton currAutomaton;
		int[] tempArray = new int[amountOfUnselected];
		double[] arraySortValue = new double[amountOfUnselected];
		int count = 0;

		for (AutomatonIterator autIt = theAutomata.iterator();
				autIt.hasNext(); )
		{
			currAutomaton = autIt.nextAutomaton();

			// Is this automaton interesting?
			if (selectedAutomata.containsAutomaton(currAutomaton))
			{
				continue;
			}

			// This line is the essence of it all...
			arraySortValue[count] = compareAlphabets(currAutomaton.getAlphabet(), synchAlphabet);

			// arraySortValue[count] = compareAlphabets(synchAlphabet, currAutomaton.getAlphabet());
			// Did we get a value?
			if (arraySortValue[count] > 0)
			{
				tempArray[count++] = currAutomaton.getIndex();

				// Have we found everything possible already?
				if (count == amountOfUnselected)
				{
					break;
				}
			}
		}

		// Did we find anything interesting at all?
		if (count == 0)
		{
			return null;
		}

		// Bubblesort the array according to arraySortValue... bubblesort? FIXA!
		double tempDouble = 0;
		int tempInt = 0;
		int changes = 1;

		while (changes > 0)
		{
			changes = 0;

			for (int i = 0; i < count - 1; i++)
			{
				if (arraySortValue[i] < arraySortValue[i + 1])
				{
					tempInt = tempArray[i];
					tempArray[i] = tempArray[i + 1];
					tempArray[i + 1] = tempInt;
					tempDouble = arraySortValue[i];
					arraySortValue[i] = arraySortValue[i + 1];
					arraySortValue[i + 1] = tempDouble;

					changes++;
				}
			}
		}

		// Return an array of apropriate length
		int[] outArray = new int[count];

		System.arraycopy(tempArray, 0, outArray, 0, count);

		return outArray;
	}

	/**
	 * Compares two alphabets for determining how similar they are in some sense.
	 * All events in rightAlphabet are examined if they are unique to rightAlphabet
	 * or appear in leftAlphabet too.
	 *
	 *@param  leftAlphabet the alphabet to compare.
	 *@param  rightAlphabet the alphabet to compare to.
	 *@return  double representing how similar the two alphabets are. Returns quota between common
	 * events in the alphabets and unique events in rightAlphabet.
	 */
	private double compareAlphabets(Alphabet leftAlphabet, Alphabet rightAlphabet)
	{

		//
		// USE Alphabet.nbrOfCommonEvents INSTEAD!!!!
		// Naaaah... not the same thing, but this method should be in Alphabet.java
		//
		int amountOfCommon = 0;
		int amountOfUnique = 0;
		Iterator eventIterator = rightAlphabet.iterator();
		LabeledEvent currEvent;

		while (eventIterator.hasNext())
		{
			currEvent = (LabeledEvent) eventIterator.next();

			if (leftAlphabet.contains(currEvent.getLabel()))
			{
				amountOfCommon++;
			}
			else
			{
				amountOfUnique++;
			}
		}

		if (amountOfCommon < 1)
		{
			return 0;
		}

		if (amountOfUnique > 0)
		{

			// return (double)amountOfCommon; // Another way of doing it...
			return (double) amountOfCommon / (double) amountOfUnique;
		}
		else
		{
			return Double.MAX_VALUE;
		}
	}

	/**
	 * Excludes potentially uncontrollable states from potentiallyUncontrollableStates by synchronizing the
	 * automata in the current composition with automata with similar alphabets.
	 *
	 *@param  similarAutomata integer array with indices of automata with similar alphabets (from similarAutomata()).
	 *@param  selectedAutomata The automata currently selected (the ones in the current "composition" plus perhaps some of the similar automata from earlier runs of this method).
	 *@param  automataIndices integer array with indices of automata in the current "composition".
	 *@see  #findSimilarAutomata(org.supremica.automata.Automata, java.util.ArrayList)
	 */
	private void excludeUncontrollableStates(int[] similarAutomata, Automata selectedAutomata, int[] automataIndices)
		throws Exception
	{
		String addedAutomata = "";
		int start = selectedAutomata.size() - automataIndices.length;

		if (attempt == 1)
		{

			// First attempt
			stateAmountLimit = verificationOptions.getExclusionStateLimit();

			for (int i = 0; i < automataIndices.length; i++)
			{
				stateAmount = stateAmount * theAutomata.getAutomatonAt(automataIndices[i]).nbrOfStates();
			}
		}
		else
		{

			// Been here before, already added some automata
			for (int i = 0; i < start; i++)
			{
				addedAutomata = addedAutomata + " " + theAutomata.getAutomatonAt(similarAutomata[i]);
			}

			// Increase the limit each time
			stateAmountLimit = stateAmountLimit * 5;
		}

		if (verboseMode)
		{
			logger.info("stateAmountLimit: " + stateAmountLimit + ".");
		}

		synchHelper.clear();

		// Add some of the similar automata, but make sure the stateAmount doesn't explode!
		for (int i = start; i < similarAutomata.length; i++)
		{

			// Add automaton
			selectedAutomata.addAutomaton(theAutomata.getAutomatonAt(similarAutomata[i]));

			addedAutomata = addedAutomata + " " + theAutomata.getAutomatonAt(similarAutomata[i]);
			stateAmount = stateAmount * theAutomata.getAutomatonAt(similarAutomata[i]).nbrOfStates();

			if ((stateAmount > stateAmountLimit) || (i == similarAutomata.length - 1))
			{

				// Synchronize...
				// synchHelper.clear(); // This is done while analyzing the result se *** below
				synchHelper.addState(initialState);

				if (stopRequested)
				{
					return;
				}

				// Initialize the synchronizationExecuters
				synchronizationExecuters.clear();

				for (int j = 0; j < nbrOfExecuters; j++)
				{
					AutomataSynchronizerExecuter currSynchronizationExecuter = new AutomataSynchronizerExecuter(synchHelper);

					synchronizationExecuters.add(currSynchronizationExecuter);
				}

				// Start all the synchronization executers and wait for completion
				// For the moment we assume that we only have one thread
				for (int j = 0; j < synchronizationExecuters.size(); j++)
				{
					AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(j);

					currExec.selectAutomata(selectedAutomata);
					currExec.start();
				}

				((AutomataSynchronizerExecuter) synchronizationExecuters.get(0)).join();

				if (stopRequested)
				{
					return;
				}

				// Examine if there are states in potentiallyUncontrollableStates
				// that are not represented in the new synchronization
				int[][] currStateTable = synchHelper.getStateTable();
				int stateCount = 0;

				for (int j = 0; j < currStateTable.length; j++)
				{
					if (currStateTable[j] != null)
					{

						// Look for the state among the potentially uncontrollable states
						potentiallyUncontrollableStates.find(automataIndices, currStateTable[j]);

						// Instead of using clear()... se *** above
						currStateTable[j] = null;

						stateCount++;
					}
				}

				if (verboseMode)
				{
					logger.info("Worst-case state amount: " + stateAmount + ", real state amount: " + stateCount + ".");
				}

				stateAmount = stateCount;

				// Remove states in the stateMemorizer that are not represented in the new
				// automaton and therefore can't be reached in the total synchronization.
				// Reachable states are marked with potentiallyUncontrollableStates.find() above.
				potentiallyUncontrollableStates.clean(automataIndices);

				// Print result
				int statesLeft = potentiallyUncontrollableStates.size(automataIndices);

				if (verboseMode)
				{
					String message = "";

					switch (statesLeft)
					{

					case 0 :
						message = "No uncontrollable states ";
						break;

					case 1 :
						message = "Still one state ";
						break;

					case 2 :
						message = "Still two states ";
						break;

					default :
						message = "Still " + statesLeft + " states ";
					}

					logger.info(message + "left after adding" + addedAutomata + ".");
				}

				// Are we ready?
				if (statesLeft == 0)
				{
					return;
				}

				// Is it time to give up this attempt?
				if (stateAmount > stateAmountLimit)
				{

					// Make sure the limit and the real amount is not too different in magnitude.
					stateAmountLimit = (stateAmount / 1000) * 1000;

					break;
				}
			}
		}
	}

	/**
	 * Makes attempt on finding states in the total synchronization that REALLY are uncontrollable.
	 * This is done by making not a full synchronization but a full synchronization limited by
	 * in the greatest extent possible following the enabled transitions in the current "composition".
	 *
	 *@param  automataIndices integer array with indices of automata in the current "composition".
	 *@return  Description of the Return Value
	 *@exception  Exception Description of the Exception
	 */
	private boolean findUncontrollableStates(int[] automataIndices)
		throws Exception
	{

		// WOHOOPS! Eventuellt är det listigt att göra ny onlinesynchronizer,
		// med den nya automataIndices varje gång... tänk på det. FIXA!
		if (uncontrollabilityCheckHelper == null)
		{

			//AutomataOnlineSynchronizer onlineSynchronizer = new AutomataOnlineSynchronizer(synchHelper);
			AutomataSynchronizerExecuter onlineSynchronizer = new AutomataSynchronizerExecuter(synchHelper);

			onlineSynchronizer.selectAutomata(automataIndices);
			onlineSynchronizer.initialize();

			uncontrollabilityCheckHelper = new AutomataSynchronizerHelper(synchHelper);

			if (verboseMode)
			{    // It's important that setRememberTrace occurs before addState(initialState)!
				uncontrollabilityCheckHelper.setRememberTrace(true);
			}

			uncontrollabilityCheckHelper.addState(initialState);
			uncontrollabilityCheckHelper.setCoExecute(true);
			uncontrollabilityCheckHelper.setCoExecuter(onlineSynchronizer);
			uncontrollabilityCheckHelper.setExhaustiveSearch(true);
			uncontrollabilityCheckHelper.setRememberUncontrollable(true);
		}

		// Stop after having found a suitable amount of new states
		uncontrollabilityCheckHelper.stopExecutionAfter(verificationOptions.getReachabilityStateLimit() * attempt);

		// Initialize the synchronizationExecuters
		synchronizationExecuters.clear();

		for (int i = 0; i < nbrOfExecuters; i++)
		{
			AutomataSynchronizerExecuter currSynchronizationExecuter = new AutomataSynchronizerExecuter(uncontrollabilityCheckHelper);

			synchronizationExecuters.add(currSynchronizationExecuter);
		}

		// Start all the synchronization executers and wait for completion
		for (int i = 0; i < nbrOfExecuters; i++)
		{
			AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(i);

			currExec.selectAllAutomata();
			currExec.start();
		}

		((AutomataSynchronizerExecuter) synchronizationExecuters.get(0)).join();

		return !uncontrollabilityCheckHelper.getAutomataIsControllable();

		/*
		 *  // This is the whole method as it was before...
		 *  synchHelper.clear();
		 *  AutomataOnlineSynchronizer onlineSynchronizer = new AutomataOnlineSynchronizer(synchHelper);
		 *  onlineSynchronizer.selectAutomata(automataIndices);
		 *  onlineSynchronizer.initialize();
		 *
		 *  if (verboseMode)
		 *  {       // It's important that setRememberTrace occurs before addState!
		 *  synchHelper.setRememberTrace(true);
		 *  }
		 *  synchHelper.addState(initialState);
		 *  synchHelper.setCoExecute(true);
		 *  synchHelper.setCoExecuter(onlineSynchronizer);
		 *  synchHelper.setExhaustiveSearch(true);
		 *  synchHelper.setRememberUncontrollable(true);
		 *
		 *  // Initialize the synchronizationExecuters
		 *  synchronizationExecuters.clear();
		 *  for (int i = 0; i < nbrOfExecuters; i++)
		 *  {
		 *  AutomataSynchronizerExecuter currSynchronizationExecuter =
		 *  new AutomataSynchronizerExecuter(synchHelper);
		 *  synchronizationExecuters.add(currSynchronizationExecuter);
		 *  }
		 *
		 *  // Start all the synchronization executers and wait for completion
		 *  for (int i = 0; i < nbrOfExecuters; i++)
		 *  {
		 *  AutomataSynchronizerExecuter currExec =
		 *  (AutomataSynchronizerExecuter)synchronizationExecuters.get(i);
		 *  currExec.selectAllAutomata();
		 *  currExec.start();
		 *  }
		 *  ((AutomataSynchronizerExecuter)synchronizationExecuters.get(0)).join();
		 *
		 *  return !synchHelper.getAutomataIsControllable();
		 */
	}

	/**
	 * Answers YES/NO to the language inclusion problem
	 *
	 *@see  BDDAutomata, AutomataBDDVerifier
	 */
	private boolean BDDLanguageInclusionVerification()
		throws Exception
	{
		Automata unselected = ActionMan.getGui().getUnselectedAutomata();

		// we already know the answer: L(P) = \Sigma^*
		if (unselected.nbrOfAutomata() < 1)
		{
			return true;
		}

		Automata selected = new Automata(theAutomata, true);    /* <-- MUST BE SHALLOW COPY ... */

		selected.removeAutomata(unselected);    /* .. OR THIS REMOVE WONT WORK !!! */

		boolean ret = false;
		org.supremica.util.BDD.Timer timer = new org.supremica.util.BDD.Timer("BDDLanguageInclusionVerification");

		switch (Options.inclsuion_algorithm)
		{

		case Options.INCLUSION_ALGO_MONOLITHIC :
			AutomataBDDVerifier abf = new AutomataBDDVerifier(selected, unselected, synchHelper.getHelperData());

			ret = abf.passLanguageInclusion();

			abf.cleanup();
			break;

		case Options.INCLUSION_ALGO_MODULAR :
			org.supremica.util.BDD.li.ModularLI mli = new org.supremica.util.BDD.li.ModularLI(selected, unselected, synchHelper.getHelperData());

			ret = mli.passLanguageInclusion();

			mli.cleanup();
			break;

		case Options.INCLUSION_ALGO_INCREMENTAL :
			org.supremica.util.BDD.li.IncrementalLI ili = new org.supremica.util.BDD.li.IncrementalLI(selected, unselected, synchHelper.getHelperData());

			ret = ili.passLanguageInclusion();

			ili.cleanup();
			break;

		default :
			throw new Exception("Unknown BDD/language containment algorithm!");
		}

		if (Options.profile_on)
		{
			timer.report("total execution time", true);
		}

		Options.out.flush();

		return ret;
	}

	/**
	 * Answers YES/NO to the controllability problem
	 *
	 *@return  true if the system is controllable
	 *@see  BDDAutomata, AutomataBDDVerifier
	 */
	private boolean BDDControllabilityVerification(Automata theAutomata)
		throws Exception
	{
		boolean ret;

		// why compute when we already know the answer: L(P) = \Sigma^*   ?
		if (theAutomata.isNoAutomataPlants())
		{
			return true;
		}

		org.supremica.util.BDD.Timer timer = new org.supremica.util.BDD.Timer("BDDControllabilityVerification");

		switch (Options.inclsuion_algorithm)
		{

		case Options.INCLUSION_ALGO_MONOLITHIC :
			AutomataBDDVerifier abf = new AutomataBDDVerifier(theAutomata, synchHelper.getHelperData());

			ret = abf.isControllable();

			abf.cleanup();
			break;

		case Options.INCLUSION_ALGO_MODULAR :
			org.supremica.util.BDD.li.ModularLI mli = new org.supremica.util.BDD.li.ModularLI(theAutomata, synchHelper.getHelperData());

			ret = mli.isControllable();

			mli.cleanup();
			break;

		case Options.INCLUSION_ALGO_INCREMENTAL :
			org.supremica.util.BDD.li.IncrementalLI ili = new org.supremica.util.BDD.li.IncrementalLI(theAutomata, synchHelper.getHelperData());

			ret = ili.isControllable();

			ili.cleanup();
			break;

		default :
			throw new Exception("Unknown BDD/language containment algorithm!");
		}

		if (Options.profile_on)
		{
			timer.report("total execution time", true);
		}

		Options.out.flush();

		return ret;
	}

	/**
	 * Answers YES/NO to the NON-BLOCKING problem
	 *
	 *@return  true if the system is non-blocking
	 *@see  BDDAutomata, AutomataBDDVerifier
	 */
	private boolean BDDNonBlockingVerification()
		throws Exception
	{

		// timer.start();
		AutomataBDDVerifier abf = new AutomataBDDVerifier(theAutomata, synchHelper.getHelperData());
		boolean ret = abf.isNonBlocking();

		abf.cleanup();

		// timer.stop();
		Options.out.flush();

		return ret;
	}

	/**
	 * Examines controllability by synchronizing all automata in the system and in each state check if some
	 * uncontrollable event is enabled in a plant and not in a supervisor.
	 *
	 *@return  Description of the Return Value
	 *@exception  Exception Description of the Exception
	 *@see  AutomataSynchronizerExecuter
	 */
	private boolean monolithicControllabilityVerification()
		throws Exception
	{
		synchHelper.addState(initialState);
		synchHelper.setExhaustiveSearch(true);

		// Initialize the synchronizationExecuters
		for (int i = 0; i < nbrOfExecuters; i++)
		{
			AutomataSynchronizerExecuter currSynchronizationExecuter = new AutomataSynchronizerExecuter(synchHelper);

			synchronizationExecuters.add(currSynchronizationExecuter);
		}

		// Start all the synchronization executers and wait for completion
		for (int i = 0; i < nbrOfExecuters; i++)
		{
			AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(i);

			currExec.selectAllAutomata();
			currExec.start();
		}

		((AutomataSynchronizerExecuter) synchronizationExecuters.get(0)).join();

		return synchHelper.getAutomataIsControllable();
	}

	public AutomataSynchronizerHelper getHelper()
	{
		return synchHelper;
	}

	/**
	 * Examines non-blocking monolithically, by examining all reachable states.
	 * Lots and lots of work for big systems.
	 *
	 *@return True if non-blocking, false if blocking
	 *@exception  Exception Description of the Exception
	 *@see  AutomataSynchronizerExecuter
	 */
	private boolean monolithicNonblockingVerification()
		throws Exception
	{

		// Maybe the system is monolithic already?
		if (theAutomata.size() == 1)
		{

			// No need to synchronize!
			return moduleIsNonblocking(theAutomata.getFirstAutomaton());
		}

		// Otherwise we must synchronize...
		synchHelper.addState(initialState);
		synchHelper.setExhaustiveSearch(false);

		// Initialize the synchronizationExecuters
		for (int i = 0; i < nbrOfExecuters; i++)
		{
			AutomataSynchronizerExecuter currSynchronizationExecuter = new AutomataSynchronizerExecuter(synchHelper);

			synchronizationExecuters.add(currSynchronizationExecuter);
		}

		// Start all the synchronization executers and wait for completion
		for (int i = 0; i < nbrOfExecuters; i++)
		{
			AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(i);

			currExec.selectAllAutomata();
			currExec.start();
		}

		((AutomataSynchronizerExecuter) synchronizationExecuters.get(0)).join();

		AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(0);

		// Get the synchronized automaton
		Automaton theAutomaton;

		try
		{
			if (currExec.buildAutomaton())
			{
				theAutomaton = synchHelper.getAutomaton();
			}
			else
			{

				// Execution stopped?
				theAutomaton = null;

				return false;
			}
		}
		catch (Exception ex)
		{
			logger.error("Error when building automaton: " + ex.toString());
			logger.debug(ex.getStackTrace());

			throw ex;
		}

		return moduleIsNonblocking(theAutomaton, true);
	}

	private boolean isIndividuallyNonBlocking()
		throws Exception
	{
		boolean allIndividuallyNonblocking = true;
		Iterator autIt = theAutomata.iterator();
		Automaton currAutomaton;

		while (autIt.hasNext())
		{
			currAutomaton = (Automaton) autIt.next();
			allIndividuallyNonblocking = allIndividuallyNonblocking && moduleIsNonblocking(currAutomaton);

			if (stopRequested)
			{
				return false;
			}

			if (!allIndividuallyNonblocking)
			{
				logger.error("The automaton " + currAutomaton + " is individually blocking!");

				// logger.error("Aborting verification...");
				requestStop();

				return false;
			}
		}

		logger.info("All automata are individually nonblocking.");

		return true;
	}

	/**
	 * Examines non-blocking modularily... not fully implemented yet!
	 */
	private boolean modularNonBlockingVerification()
		throws Exception
	{

		// Is this really a modular system?
		if (theAutomata.size() == 1)
		{
			logger.info("The selected system has only one automaton - using monolithic verification...");

			return monolithicNonblockingVerification();
		}

		// Ensure individual nonblocking
		if (!isIndividuallyNonBlocking())
		{
			return false;
		}

		// Do some tests...
		Iterator targetIt = theAutomata.iterator();
		Automaton newAutomaton;
		Automata newAutomata = new Automata();
		Automata restrictedAutomata = null;

		while (targetIt.hasNext())
		{

			// Iterate over theAutomata
			Automaton targetAutomaton = new Automaton((Automaton) targetIt.next());
			Alphabet targetAlphabet = targetAutomaton.getAlphabet();

			// Restrict the other automata to this automations alphabet and synchronize the result
			restrictedAutomata = new Automata();

			Iterator restrictIt = theAutomata.iterator();

			while (restrictIt.hasNext())
			{
				Automaton restrictAutomaton = new Automaton((Automaton) restrictIt.next());

				if (targetAutomaton.equalAutomaton(restrictAutomaton))
				{
					continue;
				}

				Determinizer determinizer = new Determinizer(restrictAutomaton, targetAlphabet, false);

				determinizer.execute();

				newAutomaton = determinizer.getNewAutomaton();

				newAutomaton.setName(restrictAutomaton.getName() + "_REST");
				restrictedAutomata.addAutomaton(newAutomaton);
			}

			Automaton synchAutomaton = AutomataSynchronizer.synchronizeAutomata(restrictedAutomata);

			synchAutomaton.setName(targetAutomaton.getName() + "_BLOB");
			newAutomata.addAutomaton(synchAutomaton);
		}

		ActionMan.getGui().addAutomata(newAutomata);

		//ActionMan.getGui().addAutomata(restrictedAutomata);
		return false;
	}

	/**
	 * Examines non-blocking modularily... not fully implemented yet!
	 */
	private boolean modularNonBlockingVerification2()
		throws Exception
	{

		// Is this really a modular system?
		if (theAutomata.size() == 1)
		{
			logger.info("The selected system has only one automaton - using monolithic verification...");

			return monolithicNonblockingVerification();
		}

		// Ensure individual nonblocking
		boolean allIndividuallyNonblocking = true;
		Iterator autIt = theAutomata.iterator();
		Automaton currAutomaton;

		while (autIt.hasNext())
		{
			currAutomaton = (Automaton) autIt.next();
			allIndividuallyNonblocking = allIndividuallyNonblocking && moduleIsNonblocking(currAutomaton);

			if (stopRequested)
			{
				return false;
			}

			if (!allIndividuallyNonblocking)
			{
				logger.error("The automaton " + currAutomaton.toString() + " is individually blocking!");
				logger.error("Aborting verification...");
				requestStop();

				return false;
			}
		}

		if (allIndividuallyNonblocking)
		{
			logger.info("This system has no individually blocking automata!");
		}

		/*
		// Preparations for the global nonblocking verification...
	ExecutionDialog executionDialog = synchHelper.getExecutionDialog();
		if (executionDialog != null) // The executionDialog might not have been initialized yet! FIXA!
		{
				executionDialog.initProgressBar(0, theAutomata.size());
				executionDialog.setMode(ExecutionDialogMode.verifyingNonblocking);
		}
		// We use a copy of theAutomata instead from this point on. This is to spare us the
		// effort of changing all events to uncontrollable over and over and lets us use the
		// same helper all the time, EXCEPT for AutomataIndexForm.typeIsPlantTable which we
		// have to reinitialize between the language inclusion checks!!
	theAutomata = new Automata(theAutomata, false);
		synchHelper = new AutomataSynchronizerHelper(theAutomata, synchronizationOptions);
		// Make all events in all automata in theAutomata as uncontrollable! (All
		// events in plants should be uncontrollable and the controllability of
		// the events in the supervisors doesn't matter!)
		Iterator eventIterator;
		Iterator automatonIterator = theAutomata.iterator();
		while (automatonIterator.hasNext())
		{
				currAutomaton = (Automaton) automatonIterator.next();
				// currAutomaton.setType(AutomatonType.Plant);
				eventIterator = currAutomaton.eventIterator();
				while (eventIterator.hasNext())
				{
						((LabeledEvent) eventIterator.next()).setControllable(false);
				}
		}
		*/

		// Preparations for the global nonblocking verification...
		java.awt.EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				ExecutionDialog executionDialog = synchHelper.getExecutionDialog();

				executionDialog.initProgressBar(0, theAutomata.size());
				executionDialog.setMode(ExecutionDialogMode.verifyingNonblocking);
			}
		});

		// Ensure global nonblocking...
		boolean allIncluded = true;
		boolean automatonIsIncluded;
		Automata currAutomata = new Automata();

		autIt = theAutomata.iterator();

		while (autIt.hasNext())
		{
			currAutomaton = (Automaton) autIt.next();

			currAutomata.addAutomaton(currAutomaton);
			logger.info("Examining the automaton " + currAutomaton.toString() + ".");

			automatonIsIncluded = modularLanguageinclusionVerification(currAutomata);
			allIncluded = allIncluded && automatonIsIncluded;

			currAutomata.removeAutomaton(currAutomaton);

			if (stopRequested)
			{

				// timer.stop();
				return false;
			}

			if (synchHelper.getExecutionDialog() != null)
			{
				synchHelper.getExecutionDialog().setProgress(theAutomata.getAutomatonIndex(currAutomaton));
			}

			if (!automatonIsIncluded)
			{
				logger.error("The automaton " + currAutomaton.toString() + " is blocked by some other automaton!");
			}
		}

		// timer.stop();
		return allIncluded && allIndividuallyNonblocking;

		/*
		// Ensure global nonblocking...
		boolean allIncluded = true;
		boolean automatonIsIncluded;
		autIt = theAutomata.iterator();
		Automata currAutomata = new Automata();
		while (autIt.hasNext())
		{
				currAutomaton = (Automaton) autIt.next();
				if (executionDialog != null)
				{
						executionDialog.setProgress(theAutomata.getAutomatonIndex(currAutomaton));
				}
			currAutomata.addAutomaton(currAutomaton);

				// Perform the behavioural inclusion check!
				logger.info("Examining the automaton " + currAutomaton.getName() + ".");
				automatonIsIncluded = behaviouralInclusionVerification(currAutomata, theAutomata);
				allIncluded = allIncluded && automatonIsIncluded;
				currAutomata.removeAutomaton(currAutomaton); // Examine one automaton at a time...

				if (stopRequested)
				{
						return false;
				}

				if (!automatonIsIncluded)
				{
						logger.error("The automaton " + currAutomaton.getName() + " is blocked by some other automaton!");
				}
		}
		return allIncluded && allIndividuallyNonblocking;
		*/
	}

	private boolean modularLanguageinclusionVerification(Automata inclusionAutomata)
		throws Exception
	{
		prepareForLanguageInclusion(inclusionAutomata);

		return modularControllabilityVerification();
	}

	private boolean monolithicLanguageinclusionVerification(Automata inclusionAutomata)
		throws Exception
	{
		prepareForLanguageInclusion(inclusionAutomata);

		return monolithicControllabilityVerification();
	}

	/**
	 * Verifies behavioural inclusion of the language of automataA in automataB.
	 * I.e. "Is the behaviour of automataA included in automataB?"
	 * It IS ok for automataB to contain automata in automataA, but not the other
	 * way around!... that is, automataA has priority over automataB... if you're
	 * in A it doesn't matter if you're in B.
	 *
	 * This method presupposes that all events are uncontrollable!... very not intuitive! //Hguo.
	 */

	/*
	private boolean behaviouralInclusionVerification(Automata automataA, Automata automataB)
			throws Exception
	{
			// Change the automata in automataB to specifications
			Automaton currAutomaton;
			Iterator automatonIterator = automataB.iterator();
			while (automatonIterator.hasNext())
			{
					currAutomaton = (Automaton) automatonIterator.next();
					currAutomaton.setType(AutomatonType.Supervisor);
			}

			// Change the automata in automataA to plants
			automatonIterator = automataA.iterator();
			while (automatonIterator.hasNext())
			{
					currAutomaton = (Automaton) automatonIterator.next();
					currAutomaton.setType(AutomatonType.Plant);
			}

			// Update the typeIsPlantTable in the AutomataIndexForm in the synchHelper!
			synchHelper.getAutomataIndexForm().generateAutomataIndices(theAutomata);

			// After the above preparations, the behavioural inclusion check
			// can be performed as a controllability check...
			return modularControllabilityVerification();
	}
	*/

	/*
	/**
	 * THIS DOES NOT WORK! IT'S JUST A TEST! Examines non-blocking modularily
	 * by examining pairwise non-blocking between all automata.
	 * THIS DOES NOT WORK! IT'S JUST  A  TEST!
	 *
	 *@return True if non-blocking, false if blocking
	 *@exception  Exception Description of the Exception
	 *@see  AutomataSynchronizerExecuter
	 *
	private boolean pairwiseNonblockingVerification()
			throws Exception
	{
			if (theAutomata.size() <= 2)
			{   // This is better verified as a monolithic system!
					return monolithicNonblockingVerification();
			}

			// NOTE!! THIS ALGORITHM DOES NOT WORK! IT IS JUST A PRELIMINARY TEST!!
			logger.warn("NOTE! Modular non-blocking verification is not really implemented! " +
									"This algorithm examines pairwise non-blocking between all automata, " +
									"respectively! THIS DOES NOT PROVE GENERAL NONBLOCKING!!");

			boolean allPairsNonblocking = true;

			Iterator automatonIterator = theAutomata.iterator();
			Automaton currAutomaton;
			Automaton currOtherAutomaton;

			while (automatonIterator.hasNext())
			{
					currAutomaton = (Automaton) automatonIterator.next();
					Iterator subIterator = theAutomata.iterator();
					while (subIterator.next() != currAutomaton);
					while (subIterator.hasNext())
					{
							currOtherAutomaton = (Automaton) subIterator.next();
							boolean pairIsNonblocking = automatonPairIsNonblocking(currAutomaton, currOtherAutomaton);
							allPairsNonblocking = allPairsNonblocking && pairIsNonblocking;
							if (!pairIsNonblocking)
							{
									logger.error("The automata " + currAutomaton.getName() +
															 " and " + currOtherAutomaton.getName() + " are blocking each other.");
							}
					}
			}

			// NOTE!! THIS ALGORITHM DOES NOT WORK! IT IS JUST A PRELIMINARY TEST!!
			logger.warn("NOTE! Modular non-blocking verification is not really implemented! " +
									"This algorithm examines pairwise non-blocking between all automata, " +
									"respectively! THIS DOES NOT PROVE GENERAL NONBLOCKING!!");

			return allPairsNonblocking;
	}

	/**
	 * Examines non-blocking between a pair of automata
	 *
	 * @param AutomatonA The first automata in the pair.
	 * @param AutomatonB The second automata in the pair.
	 *
	private boolean automatonPairIsNonblocking(Automaton AutomatonA, Automaton AutomatonB)
			throws Exception
	{
			logger.info("Synchronizing " + AutomatonA.getName() + " and " + AutomatonB.getName() + "...");

			// Synchronize the two automata and verify nonblocking on the result
			Automaton AutomataSynk;
			ArrayList selectedAutomata = new ArrayList();

			selectedAutomata.add(AutomatonA);
			selectedAutomata.add(AutomatonB);

			try
			{
					AutomataSynk = synchronizeAutomata(selectedAutomata);
			}
			catch (Exception ex)
			{
					logger.error("Error in AutomataVerifier when synchronizing automata: " + ex.toString());
					logger.debug(ex.getStackTrace());
					throw ex;
			}

			return moduleIsNonblocking(AutomataSynk);
	}

	/**
	 * Performs synchronous composition on the automata represented by selectedAutomata
	 *
	 * @param selectedAutomata The Automata to be synchronized, ArrayList of Automaton objects
	 * @return The automaton that constitutes the synchronization of the automata
	 * represented by selectedAutomata.
	 *
	private Automaton synchronizeAutomata(ArrayList selectedAutomata)
			throws Exception
	{
			Automaton AutomataSynk;

			// BUILD A NEW SYNCHHELPER! The status of the states must not be affected by the
			// automata we're not looking at for the moment...
			Automata currAutomata = new Automata();
			while (selectedAutomata.size() != 0)
			{
					currAutomata.addAutomaton((Automaton) selectedAutomata.remove(0));
			}

			// Build the initial state  (including 2 status fields)
			int[] currInitialState = AutomataIndexFormHelper.createState(currAutomata.size());
			Iterator autIt = currAutomata.iterator();
			State localInitialState;
			Automaton currAutomaton;

			// The automata have indexes corresponding to theAutomata, we ignore
			// this by using the variable "index".
			int index = 0;
			while (autIt.hasNext())
			{
					currAutomaton = (Automaton) autIt.next();
					localInitialState = currAutomaton.getInitialState();
					currInitialState[index++] = localInitialState.getIndex();
					//currInitialState[currAutomaton.getIndex()] = localInitialState.getIndex();
			}

			// Initialize new synchHelper and move the executionDialog to the new helper...
			ExecutionDialog excutionDialog = synchHelper.getExecutionDialog();
			synchHelper = new AutomataSynchronizerHelper(currAutomata, synchronizationOptions);
			synchHelper.setExecutionDialog(excutionDialog);

			// Clear the hash-table in the helper and set some variables in the synchronization helper
			// synchHelper.clear();
			// synchHelper.setRememberUncontrollable(true);
			synchHelper.addState(currInitialState);

			if (stopRequested)
			{
					return null;
			}

			// Initialize the synchronizationExecuters
			synchronizationExecuters.clear();

			for (int i = 0; i < nbrOfExecuters; i++)
			{
					AutomataSynchronizerExecuter currSynchronizationExecuter = new AutomataSynchronizerExecuter(synchHelper);
					synchronizationExecuters.add(currSynchronizationExecuter);
			}

			// Start all the synchronization executers and wait for completion
			// For the moment we assume that we only have one thread
			for (int i = 0; i < synchronizationExecuters.size(); i++)
			{
					AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(i);
					currExec.selectAllAutomata();
					currExec.start();
			}
			((AutomataSynchronizerExecuter) synchronizationExecuters.get(0)).join();
			AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(0);

			// Build automaton
			try
			{
					if (currExec.buildAutomaton())
					{
							AutomataSynk = synchHelper.getAutomaton();
					}
					else
					{
							AutomataSynk = null;
					}
			}
			catch (Exception ex)
			{
					logger.error("Error when building automaton: " + ex.toString());
					logger.debug(ex.getStackTrace());
					throw ex;
			}

			return AutomataSynk;
	}
	*/

	/**
	 * Examines non-blocking monolithically, by examining all reachable states.
	 * Lots and lots of work for big systems.
	 *
	 *@return True if non-blocking, false if blocking
	 *@exception  Exception Description of the Exception
	 *@see AutomataSynchronizerExecuter
	 */
	private static boolean moduleIsNonblocking(Automaton theAutomaton)
	{
		return moduleIsNonblocking(theAutomaton, false);
	}

	/**
	 * Examines non-blocking monolithically, by examining all reachable states.
	 * Allows destructive verification (perhaps we don't need to make a copy)
	 *
	 *@return True if non-blocking, false if blocking
	 *@exception  Exception Description of the Exception
	 *@see AutomataSynchronizerExecuter
	 */
	private static boolean moduleIsNonblocking(Automaton theAutomaton, boolean destructive)
	{

		// Should we save the original by creating a copy that we can destroy?
		if (!destructive)
		{
			theAutomaton = new Automaton(theAutomaton);
		}

		// Examine all states, starting from the marked ones and moving backwards...
		LinkedList statesToExamine = new LinkedList();
		Iterator stateIterator = theAutomaton.stateIterator();
		State currState;

		// Add all marked states
		while (stateIterator.hasNext())
		{
			currState = (State) stateIterator.next();

			if (currState.isAccepting())
			{
				statesToExamine.add(currState);
			}
		}

		// Examine all guaranteed nonblocking states for incoming arcs
		State examinedState;
		Iterator incomingArcIterator;

		while (statesToExamine.size() > 0)
		{
			examinedState = (State) statesToExamine.removeFirst();    // OBS. removeFirst!
			incomingArcIterator = examinedState.incomingArcsIterator();

			while (incomingArcIterator.hasNext())
			{
				currState = ((Arc) incomingArcIterator.next()).getFromState();

				if (!currState.equals(examinedState))    // Self-loops...
				{
					statesToExamine.add(currState);
				}
			}

			theAutomaton.removeState(examinedState);
		}

		stateIterator = theAutomaton.stateIterator();

		while (stateIterator.hasNext())
		{
			currState = (State) stateIterator.next();

			logger.info("Blocking state: " + currState.getName());

			// If we did a copy of theAutomata before we destroyed it we could display the trace...
			// logger.info("Trace to blocking state: " + (theAutomaton.getTrace(currState)).toString());
		}

		return theAutomaton.nbrOfStates() == 0;
	}

	/**
	 * Method that stops AutomataVerifier as soon as possible.
	 *
	 * @see  ExecutionDialog
	 */
	public void requestStop()
	{
		stopRequested = true;

		logger.debug("AutomataVerifier requested to stop.");

		for (int i = 0; i < synchronizationExecuters.size(); i++)
		{
			((AutomataSynchronizerExecuter) synchronizationExecuters.get(i)).requestStop();
		}

		if (threadToStop != null)
		{
			threadToStop.requestStop();
		}
	}

	/**
	 * Standard method for monolithic nonblocking verification on theAutomaton.
	 */
	public static boolean verifyNonblocking(Automaton theAutomaton)
	{
		return moduleIsNonblocking(theAutomaton);
	}

	/**
	 * Standard method for performing modular controllability verification on theAutomata.
	 */
	public static boolean verifyControllability(Automata theAutomata)
		throws Exception
	{
		SynchronizationOptions synchronizationOptions;
		VerificationOptions verificationOptions;

		synchronizationOptions = SynchronizationOptions.getDefaultVerificationOptions();
		verificationOptions = VerificationOptions.getDefaultControllabilityOptions();

		AutomataVerifier verifier = new AutomataVerifier(theAutomata, synchronizationOptions, verificationOptions);

		return verifier.verify();
	}

	/**
	 * Standard method for performing modular languageInclusion verification on automataA
	 * and automataB.
	 *
	 * @param automataA the automata that should be included.
	 * @param automataB the automata that should include
	 * @return true if "L(automataA)" is included in "L^-1(automataB)".
	 */
	public static boolean verifyInclusion(Automata automataA, Automata automataB)
		throws Exception
	{
		SynchronizationOptions synchronizationOptions;
		VerificationOptions verificationOptions;

		synchronizationOptions = SynchronizationOptions.getDefaultVerificationOptions();
		verificationOptions = VerificationOptions.getDefaultLanguageInclusionOptions();

		Automata theAutomata = new Automata();

		theAutomata.addAutomata(automataA);
		theAutomata.addAutomata(automataB);
		theAutomata.setIndicies();

		AutomataVerifier verifier = new AutomataVerifier(theAutomata, synchronizationOptions, verificationOptions);

		verifier.prepareForLanguageInclusion(automataA);

		return verifier.verify();
	}
}
