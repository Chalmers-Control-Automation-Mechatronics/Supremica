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

package org.supremica.automata.algorithms;

import org.supremica.automata.*;

import java.util.*;
import org.supremica.util.IntArrayHashTable;
import java.io.PrintWriter;

import org.supremica.gui.*;
import org.apache.log4j.*;

/**
 * For performing verification. Uses AutomataSynchronizerExecuter for the actual verification work.
 * @see AutomataSynchronizerExecuter
 */
public class AutomataVerifier
	implements Stoppable
{
	private static Category thisCategory = LogDisplay.createCategory(AutomataVerifier.class.getName());

    private Automata theAutomata;
	private int nbrOfExecuters;

	/** Map from an uncontrollable Event-object to the Set of plant-type Automaton-objects that contains this event.
	 * @see AlphabetAnalyzer */
	private HashMap uncontrollableEventToPlantMap = new HashMap();

    private AutomataSynchronizerHelper synchHelper;
    private ArrayList synchronizationExecuters = new ArrayList();
	private StateMemorizer potentiallyUncontrollableStates;
	private int[] initialState;

	private int nbrOfUncontrollableStates = 0;
	private int nbrOfUncontrollableSpecifications = 0;

	// Used in excludeUncontrollableStates
	private int stateAmount = 1;
	private int stateAmountLimit = 1000;

	/** Determines if more detailed information on the progress of things should be displayed.
	 * @see SynchronizationOptions */
	private boolean verboseMode;

	private VerificationOptions verificationOptions;
	private SynchronizationOptions synchronizationOptions;

	/** For stopping execution. */
	private boolean stopRequested = false;

    public AutomataVerifier(Automata theAutomata, SynchronizationOptions synchronizationOptions, VerificationOptions verificationOptions)
		throws IllegalArgumentException, Exception
    {
		Automaton currAutomaton;
		State currInitialState;

		this.theAutomata = theAutomata;
		this.verificationOptions = verificationOptions;
		this.synchronizationOptions = synchronizationOptions;
		nbrOfExecuters = synchronizationOptions.getNbrOfExecuters();
		verboseMode = synchronizationOptions.verboseMode();

		synchHelper = new AutomataSynchronizerHelper(theAutomata, synchronizationOptions);

		// Allocate the synchronizationExecuters
		// synchronizationExecuters = new ArrayList(nbrOfExecuters);

		// Build the initial state
	    initialState = new int[theAutomata.size() + 1]; // + 1 status field
		Iterator autIt = theAutomata.iterator();
		while (autIt.hasNext())
		{
			currAutomaton = (Automaton) autIt.next();
			currInitialState = currAutomaton.getInitialState();
			initialState[currAutomaton.getIndex()] = currInitialState.getIndex();
		}
	}

	/**
	 * Performs verification using the AutomataSynchronizerExecuter.
	 * @deprecated this is done in AutomataVerificationWorker.
	 * @return true if controllable, false if not or false if don't know.
	 * @see AutomataSynchronizerExecuter
	 */
    public boolean execute()
		throws Exception
	{
		if (verificationOptions.getAlgorithmType() == 0)
		{   // Modular...
			return modularControllabilityVerification();
		}
		else if (verificationOptions.getAlgorithmType() == 1)
		{	// Monolithic...
			return monolithicControllabilityVerification();
		}
		else if (verificationOptions.getAlgorithmType() == 2)
		{   // IDD...
			thisCategory.error("Option not implemented...");
			return false;
		}
		else
		{   // Error...
			thisCategory.error("Unavailable option chosen.");
			return false;
		}
	}

	/** 
	 * Method called from external class stopping AutomataVerifier as soon as possible. 
	 * @see CancelDialog
	 */
 	public void requestStop()
	{
		stopRequested = true;
		for (int i = 0; i < synchronizationExecuters.size(); i++)
			((AutomataSynchronizerExecuter) synchronizationExecuters.get(i)).requestStop();
	}

	/** 
	 * Performs modular controllablity verification using the AutomataSynchronizerExecuter.
	 * @return true if controllable, false if not or false if don't know.
	 * @see AutomataSynchronizerExecuter
	 */
	public boolean modularControllabilityVerification()
		throws Exception
	{
		potentiallyUncontrollableStates = synchHelper.getStateMemorizer();

		AlphabetAnalyzer alphabetAnalyzer = new AlphabetAnalyzer(theAutomata);
		uncontrollableEventToPlantMap = alphabetAnalyzer.getUncontrollableEventToPlantMap();

		Event currEvent;
		Automaton currPlantAutomaton;
		Automaton currSupervisorAutomaton;
		ArrayList selectedAutomata = new ArrayList();
		Iterator eventIterator;
		Iterator plantIterator;
		boolean allSupervisorsControllable = true;
		Iterator supervisorIterator = theAutomata.iterator();
		while (supervisorIterator.hasNext())
		{   // Iterate over supervisors/specifications
			currSupervisorAutomaton = (Automaton) supervisorIterator.next();
			if ((currSupervisorAutomaton.getType() == AutomatonType.Supervisor) ||
				(currSupervisorAutomaton.getType() == AutomatonType.Specification))
			{	// Examine uncontrollable events in currSupervisorAutomaton and select plants containing these events
				selectedAutomata.add(currSupervisorAutomaton);
				eventIterator = currSupervisorAutomaton.eventIterator();
				while (eventIterator.hasNext())
				{
					currEvent = (Event) eventIterator.next();
					if (!currEvent.isControllable())
					{
						if (uncontrollableEventToPlantMap.get(currEvent) != null)
						{
							plantIterator = ((Set) uncontrollableEventToPlantMap.get(currEvent)).iterator();
							while (plantIterator.hasNext())
							{
								currPlantAutomaton = (Automaton) plantIterator.next();
								if (!selectedAutomata.contains(currPlantAutomaton))
									selectedAutomata.add(currPlantAutomaton);
							}
						}
					}
				}

				if (selectedAutomata.size() > 1)
				{	// Clear the hash-table and set some variables in the synchronization helper
					synchHelper.clear();
					synchHelper.setRememberUncontrollable(true);
					synchHelper.addState(initialState);

					// Initialize the synchronizationExecuters
					synchronizationExecuters.clear();
					for (int i = 0; i < nbrOfExecuters; i++)
					{
						AutomataSynchronizerExecuter currSynchronizationExecuter =
							new AutomataSynchronizerExecuter(synchHelper);
						synchronizationExecuters.add(currSynchronizationExecuter);
					}

					if (stopRequested)
						return false;

					// Start all the synchronization executers and wait for completion
					// For the moment we assume that we only have one thread
					for (int i = 0; i < synchronizationExecuters.size(); i++)
					{
						AutomataSynchronizerExecuter currExec =
							(AutomataSynchronizerExecuter) synchronizationExecuters.get(i);
						currExec.selectAutomata(selectedAutomata);
						currExec.start();
					}
					((AutomataSynchronizerExecuter)synchronizationExecuters.get(0)).join();

					if (stopRequested)
						return false;
					
					String automataNames = "";
					if (verboseMode)
					{	// For printing the names of the automata in selectedAutomata
						// Object[] automatonArray = selectedAutomata.toArray();
						Iterator autIt = selectedAutomata.iterator();
						automataNames = "";
						// for (int i = 0; i < automatonArray.length; i++)
						//automataNames = automataNames + ((Automaton) automatonArray[i]).getName() + " ";
						while (autIt.hasNext())
							automataNames = automataNames + ((Automaton) autIt.next()).getName() + " ";
					}

					if (synchHelper.getAutomataIsControllable())
					{	// Very nice
						if (verboseMode)
							thisCategory.info(automataNames + "is controllable.");
					}
					else
					{   // Try to add some more automata

						// Make array with indices of selected automata to remember which were originally selected
						int[] automataIndices = new int[selectedAutomata.size()];
						for (int i = 0; i < selectedAutomata.size(); i++)
						{
							automataIndices[i] = ((Automaton) selectedAutomata.get(i)).getIndex();
						}
						if (verboseMode)
							thisCategory.error(automataNames + "has " + potentiallyUncontrollableStates.size(automataIndices) + " states that might be uncontrollable...");

						// Sort automata in order of similar alphabets
						int[] similarAutomata = findSimilarAutomata(theAutomata, selectedAutomata);
						if (similarAutomata != null)
							if (verboseMode)
								thisCategory.info("There are " + similarAutomata.length + " automata with similar alphabets...");

						for (int attempt = 1; attempt <= 5; attempt++)
						{
							if (verboseMode)
								thisCategory.info("Attempt number " + attempt + ", stateAmountLimit: " + stateAmountLimit + ".");

							if (similarAutomata != null)
							{	// Add the similar automata  in hope of removing uncontrollable
								// states from potentiallyUncontrollableStates...
								excludeUncontrollableStates(similarAutomata, selectedAutomata, automataIndices);
								if (stopRequested)
									return false;
							}

							if (potentiallyUncontrollableStates.size(automataIndices) > 0)
							{
								if (verboseMode)
									thisCategory.info("Couldn't proove controllability, trying to proove uncontrollability...");

								// Try to prove remaining states in the stateMemorizer as beeing uncontrollable
								if (findUncontrollableStates(automataIndices))
								{
									if (verboseMode)
									{	// Print the uncontrollable state(s)...
										synchHelper.printUncontrollableStates();
										// Print event trace reaching uncontrollable state
										synchHelper.displayTrace();
										// Print info on amount of states examined
										// synchHelper.displayInfo(); // This is done always in AutomataVerificationWorker
									}
									return false;
								}
							}
							else
							{   // All uncontrollable states were removed!
								// thisCategory.info("The supervisor " + ((Automaton) selectedAutomata.get(0)).getName() + " was found to be controllable afterall!");
								break;
							}
						}

						if (potentiallyUncontrollableStates.size(automataIndices) > 0)
						{	// There are still some uncontrollable states that we're not sure as of being either
							// controllable or uncontrollable. We now have no idea what so ever on the
							// controllability... we chicken out and give up.
							// Print remaining suspected uncontrollable state(s)
							if (verboseMode)
							{
								thisCategory.info("Unfortunately the following states might be uncontrollable...");
								synchHelper.printUncontrollableStates(automataIndices);
							}
							allSupervisorsControllable = false;
						}
					}
				}
				selectedAutomata.clear();
			}
		}
		if (nbrOfUncontrollableStates > 1)
		{
			if (verboseMode)
				thisCategory.info("Can't proove either controllability or uncontrollability. There are " + potentiallyUncontrollableStates.size() + " states that perhaps makes theese automata uncontrollable.");
		}
		if (verboseMode)
			synchHelper.displayInfo();
		return allSupervisorsControllable;
	}

	/**
	 * Finds similar automata and sorts the automata in a smart way...
	 * @param selectedAutomata the collection automata in the current "composition".
	 * @param theAutomata reference to the global variable with the same name... eh...
	 * @see #compareAlphabets(org.supremica.automata.Alphabet, org.supremica.automata.Alphabet)
	 * @see #excludeUncontrollableStates(int[], java.util.ArrayList, int[])
	 */
	private int[] findSimilarAutomata(Automata theAutomata, ArrayList selectedAutomata)
		throws Exception
	{
		int amountOfSelected = selectedAutomata.size();
		int amountOfAutomata = theAutomata.size();
		int amountOfUnselected = amountOfAutomata - amountOfSelected;
		// Are there any automata to find in the first place?
		if (amountOfAutomata == 0)
			return null;

  		// Compute the union alphabet of the automata in selectedAutomata
		Alphabet unionAlphabet;
	    Automaton currAutomaton;
		EventsSet theAlphabets = new EventsSet();
		for (int i = 0; i < selectedAutomata.size(); i++)
		{
			currAutomaton = (Automaton) selectedAutomata.get(i);
			Alphabet currAlphabet = currAutomaton.getAlphabet();
			theAlphabets.add(currAlphabet);
		}
		unionAlphabet = AlphabetHelpers.getUnionAlphabet(theAlphabets, "");

		int[] tempArray = new int[amountOfUnselected];
	    double[] arraySortValue = new double[amountOfUnselected];
		int count = 0;
		boolean found = false;
		Iterator automataIterator = theAutomata.iterator();
		while (automataIterator.hasNext())
		{
			currAutomaton = (Automaton) automataIterator.next();
			arraySortValue[count] = compareAlphabets(currAutomaton.getAlphabet(), unionAlphabet);
			if (arraySortValue[count] > 0)
			{
				for (int i = 0; i < amountOfSelected; i++)
				{
					if (currAutomaton == (Automaton) selectedAutomata.get(i))
						found = true;
				}
				if (!found)
					tempArray[count++] = currAutomaton.getIndex();
				if (count == amountOfUnselected)
					break;
				found = false;
			}
		}

		if (count == 0)
			return null;
		int[] outArray = new int[count];

		// Bubblesort the array according to arraySortValue... bubblesort? FIXA!
		double tempDouble = 0;
		int tempInt = 0;
		int changes = 1;
		while (changes > 0)
		{
			changes = 0;
			for (int i = 0; i < count-1; i++)
				if (arraySortValue[i] < arraySortValue[i+1])
				{
					tempInt = tempArray[i];
					tempArray[i] = tempArray[i+1];
					tempArray[i+1] = tempInt;
					tempDouble = arraySortValue[i];
					arraySortValue[i] = arraySortValue[i+1];
					arraySortValue[i+1] = tempDouble;
					changes++;
				}
		}
		System.arraycopy(tempArray, 0, outArray, 0, count);
		return outArray;
	}

	/**
	 * Compares two alphabets for determining how similar they are, in some sense. All events in rightAlphabet are
	 * examined if they are unique to rightAlphabet or appear in leftAlphabet too.
	 * @return double representing how similar the two alphabets are. Returns quota between common events in the alphabets and unique events in rightAlphabet.
	 * @param leftAlphabet the alphabet to compare.
	 * @param rightAlphabet the alphabet to compare to.
	 */
	private double compareAlphabets(Alphabet leftAlphabet, Alphabet rightAlphabet)
	{
		int amountOfCommon = 0;
		int amountOfUnique = 0;

		Iterator eventIterator = rightAlphabet.iterator();
		Event currEvent;
		while (eventIterator.hasNext())
		{
			currEvent = (Event) eventIterator.next();
			if (leftAlphabet.containsEventWithLabel(currEvent.getLabel()))
				amountOfCommon++;
			else
				amountOfUnique++;
		}
		if (amountOfCommon < 1) // Perhaps <= 1? Only one event won't do much good?
			return 0;
		if (amountOfUnique > 0)
			return (double)amountOfCommon/(double)amountOfUnique;
		else
			return Double.MAX_VALUE;
	}

	/**
	 * Excudes potentially uncontrollable states from potentiallyUncontrollableStates by synchronizing the
	 * automata in the current composition with automata with similar alphabets.
	 * @param similarAutomata integer array with indices of automata with similar alphabets (from similarAutomata()).
	 * @param selectedAutomata ArrayList of the Automaton-objects currently selected (the ones in the current "composition" plus perhaps some of the similar automata from earlier rins of this method).
	 * @param automataIndices integer array with indices of automata in the current "composition".
	 * @see #findSimilarAutomata(org.supremica.automata.Automata, java.util.ArrayList)
	 */
	private void excludeUncontrollableStates(int[] similarAutomata, ArrayList selectedAutomata, int[] automataIndices)
		throws Exception
	{
		String addedAutomata = "";
		int start = selectedAutomata.size() - automataIndices.length;

		if (start == similarAutomata.length)
		{   // Already added all similar automata
			if (verboseMode)
				thisCategory.info("All similar automata are already added, there is no hope for prooving controllability...");
			return;
		}
		else if (start > 0)
		{   // Been here before, already added some automata
			for (int i = 0; i < start; i++)
				addedAutomata = addedAutomata + " " + theAutomata.getAutomatonAt(similarAutomata[i]).getName();
			// Increase the limit each time
			stateAmountLimit = stateAmountLimit * 5;
		}
		else
		{   // First attempt
			for (int i = 0; i < automataIndices.length; i++)
				stateAmount = stateAmount * theAutomata.getAutomatonAt(automataIndices[i]).nbrOfStates();
		}

		synchHelper.clear();

		for (int i = start; i < similarAutomata.length; i++)
		{
			// Add automaton
			selectedAutomata.add(theAutomata.getAutomatonAt(similarAutomata[i]));
			addedAutomata = addedAutomata + " " + theAutomata.getAutomatonAt(similarAutomata[i]).getName();
			// stateAmount = (int) (stateAmount * theAutomata.getAutomatonAt(similarAutomata[i]).nbrOfStates() / (1+arraySortValue[i])/(arraySortValue[0]));  // INGE BRA... SVÅRT ATT MOTIVERA LOGISKT... FIXA!
			stateAmount = stateAmount * theAutomata.getAutomatonAt(similarAutomata[i]).nbrOfStates();
			if ((stateAmount > stateAmountLimit) || (i == similarAutomata.length-1))
			{   // Synchronize...
				// synchHelper.clear(); // This is done while analyzing the result se *** below
				synchHelper.addState(initialState);
				
				/*
				AutomataSynchronizerExecuter currExecuter =
					new AutomataSynchronizerExecuter(synchHelper);
				currExecuter.selectAutomata(selectedAutomata);
				currExecuter.start();
				currExecuter.join();
				*/

				// Initialize the synchronizationExecuters
				synchronizationExecuters.clear();
				for (int j = 0; j < nbrOfExecuters; j++)
				{
					AutomataSynchronizerExecuter currSynchronizationExecuter =
						new AutomataSynchronizerExecuter(synchHelper);
					synchronizationExecuters.add(currSynchronizationExecuter);
				}
				
				// Start all the synchronization executers and wait for completion
				// For the moment we assume that we only have one thread
				for (int j = 0; j < synchronizationExecuters.size(); j++)
				{
					AutomataSynchronizerExecuter currExec =
						(AutomataSynchronizerExecuter) synchronizationExecuters.get(j);
					currExec.selectAutomata(selectedAutomata);
					currExec.start();
				}
				((AutomataSynchronizerExecuter)synchronizationExecuters.get(0)).join();

				if (stopRequested)
					return;

				// Examine if there are states in potentiallyUncontrollableStates
				// that are not represented in the new synchronization
				int[][] currStateTable = synchHelper.getStateTable();
				int stateCount = 0;
 				for (int j = 0; j < currStateTable.length; j++)
				{
 					if (currStateTable[j] != null)
 					{
						potentiallyUncontrollableStates.find(automataIndices, currStateTable[j]);
						currStateTable[j] = null; // Instead of using clear()... se *** above
 						stateCount++;
					}
				}
				if (verboseMode)
					thisCategory.info("Worst-case state amount: " + stateAmount + ", real state amount: " + stateCount + ".");
				stateAmount = stateCount;
				// Remove states in the stateMemorizer that are not represented in the new
				// automaton and therefore can't be reached in the total synchronization.
				// Reachable states are marked with potentiallyUncontrollableStates.find() above.
				potentiallyUncontrollableStates.clean(automataIndices);

				// Print result
				int statesLeft = potentiallyUncontrollableStates.size(automataIndices);
				if  (statesLeft == 0)
				{
					if (verboseMode)
						thisCategory.info("No uncontrollable states left after adding" + addedAutomata + ", the automata is controllable.");
					return;
				}
				else
				{
					if  (statesLeft == 1)
						if (verboseMode)
							thisCategory.info("Still one state left after adding" + addedAutomata + ".");
					else
						if (verboseMode)
							thisCategory.info("Still " + statesLeft + " states left after adding" + addedAutomata + ".");
				}

				if (stateAmount > stateAmountLimit)
				{   // Limit reached!!
					break;
				}
			}
		}
	}

	/*
	private boolean oldFindUncontrollableStates(int[] automataIndices, int[] similarAutomata)
		throws Exception
	{
		synchHelper.clear();
		// synchHelper.setRememberUncontrollable(false);
		synchHelper.setExpandEventsUsingPriority(true);
		synchHelper.setRememberTrace(true);
		synchHelper.addState(initialState);

		thisCategory.debug("Searching for uncontrollable states...");

		// Set expansion priority
		Iterator eventIterator;
		Automaton currAutomaton;
		Alphabet currAlphabet;
		Alphabet unionAlphabet = synchHelper.getAutomaton().getAlphabet();
		Event currEvent;
		for (int i = 0; i < similarAutomata.length; i++)
		{
			currAlphabet = theAutomata.getAutomatonAt(similarAutomata[i]).getAlphabet();
			eventIterator = currAlphabet.iterator();
			while (eventIterator.hasNext())
			{
				currEvent = unionAlphabet.getEventWithLabel(((Event) eventIterator.next()).getLabel());
				currEvent.setExpansionPriority(2);
				// thisCategory.debug("Titta: " + currEvent.getLabel() + " " + currEvent.getExpansionPriority());
			}
		}
		for (int i = 0; i < automataIndices.length; i++)
		{
			currAlphabet = theAutomata.getAutomatonAt(automataIndices[i]).getAlphabet();
			eventIterator = currAlphabet.iterator();
			while (eventIterator.hasNext())
			{
				currEvent = unionAlphabet.getEventWithLabel(((Event) eventIterator.next()).getLabel());
				currEvent.setExpansionPriority(1);
				// thisCategory.debug("Titta: " + currEvent.getLabel() + " " + currEvent.getExpansionPriority());
			}
		}

		// Allocate and initialize the synchronizationExecuters
		ArrayList synchronizationExecuters = new ArrayList(nbrOfExecuters);
		for (int i = 0; i < nbrOfExecuters; i++)
		{
			AutomataSynchronizerExecuter currSynchronizationExecuter =
				new AutomataSynchronizerExecuter(synchHelper);
			synchronizationExecuters.add(currSynchronizationExecuter);
		}

		// Start all the synchronization executers and wait for completion
		// For the moment we assume that we only have one thread
		for (int i = 0; i < synchronizationExecuters.size(); i++)
		{
			AutomataSynchronizerExecuter currExec =
				(AutomataSynchronizerExecuter) synchronizationExecuters.get(i);
			currExec.selectAllAutomata();
			currExec.start();
		}
		((AutomataSynchronizerExecuter)synchronizationExecuters.get(0)).join();
		// synchHelper.setExpandEventsUsingPriority(false);
		// synchHelper.setRememberTrace(false);
		return !synchHelper.getAutomataIsControllable();
	}
	*/

	/**
	 * Makes attempt on finding states in the total synchronization that REALLY are uncontrollable.
	 * This is done without doing a full synchronization but a full synchronization limited by
	 * in the gratest extent possible following the enabled transitions in the current "composition".
	 *
	 * @param automataIndices integer array with indices of automata in the current "composition".
	 */
	private boolean findUncontrollableStates(int[] automataIndices)
		throws Exception
	{
		synchHelper.clear();
		AutomataOnlineSynchronizer onlineSynchronizer = new AutomataOnlineSynchronizer(synchHelper);
		onlineSynchronizer.selectAutomata(automataIndices);
		onlineSynchronizer.initialize();

		// The order of the two first lines here is important...
		synchHelper.setRememberTrace(true);
		synchHelper.addState(initialState);
		synchHelper.setCoExecute(true);
		synchHelper.setCoExecuter(onlineSynchronizer);
		synchHelper.setExhaustiveSearch(true);
		// synchHelper.setRememberUncontrollable(true);
		AutomataSynchronizerExecuter executer = new AutomataSynchronizerExecuter(synchHelper);
		executer.selectAllAutomata();
		executer.start();
		executer.join();

		return !synchHelper.getAutomataIsControllable();
	}

	public boolean monolithicControllabilityVerification()
		throws Exception
	{
		synchHelper.addState(initialState);
		synchHelper.setExhaustiveSearch(true);
		
		// Initialize the synchronizationExecuters
		for (int i = 0; i < nbrOfExecuters; i++)
		{
			AutomataSynchronizerExecuter currSynchronizationExecuter =
				new AutomataSynchronizerExecuter(synchHelper);
			synchronizationExecuters.add(currSynchronizationExecuter);
		}

		// Start all the synchronization executers and wait for completion
		for (int i = 0; i < nbrOfExecuters; i++)
		{
			AutomataSynchronizerExecuter currExec =
				(AutomataSynchronizerExecuter)synchronizationExecuters.get(i);
			currExec.selectAllAutomata();
			currExec.start();
		}
		((AutomataSynchronizerExecuter)synchronizationExecuters.get(0)).join();
		return synchHelper.getAutomataIsControllable();
	}

	public AutomataSynchronizerHelper getHelper()
	{
		return synchHelper;
	}
}
