
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

import org.supremica.gui.*;
import org.supremica.log.*;
import java.util.Arrays;

// For the automata selection methods
import java.util.ArrayList;
import org.supremica.automata.AutomataIndexForm;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.Automaton;

/**
 * Used in the "Automata Explorer" (an early version that is on ice since way back and therefore not completed).
 * Also used in AutomataVerifier.findUncontrollableStates.
 *
 *@author  ka
 *@created  November 28, 2001
 */
public final class AutomataOnlineSynchronizer
//	extends Thread
{
	private final AutomataSynchronizerHelper helper;
	private final AutomataIndexForm indexForm;
	private static Logger logger = LoggerFactory.createLogger(AutomataOnlineSynchronizer.class);
	private final int nbrOfAutomata;
	private int nbrOfSelectedAutomata;
	private final int nbrOfEvents;
	private final int[][][] nextStateTable;
	private final int[][][] outgoingEventsTable;
	private final int[][][] incomingEventsTable;

	// New!
	private final boolean[][] prioritizedEventsTable;
	private final boolean[] typeIsPlantTable;
	private final boolean[] controllableEventsTable;
	private int[][] currOutgoingEvents;
	private int[] currOutgoingEventsIndex;
	private int[] automataIndices;
	private int[] currPlantAutomata;
	private int[] nextState = null;
	private int[] currState;
	private int[] currEnabledEvents;
	private boolean controllableState;
	private final SynchronizationOptions syncOptions;
	private final SynchronizationType syncType;
	private boolean forbidUncontrollableStates;
	private boolean expandForbiddenStates = false;

	// For AutomataFastControllabilityCheck...
	private int problemPlant = Integer.MAX_VALUE;
	private int problemEvent = Integer.MAX_VALUE;
	private StateMemorizer potentiallyUncontrollableStates;
	private int[] eventPriority;
	private boolean expandEventsUsingPriority = false;

	// For AutomataFastControllabilityCheck...
	private boolean rememberUncontrollable = false;

	// For AutomataControllabilityCheck...
	private boolean exhaustiveSearch = false;

	public AutomataOnlineSynchronizer(AutomataSynchronizerHelper synchronizerHelper)
	{
		//setPriority(Thread.MIN_PRIORITY);

		helper = synchronizerHelper;
		indexForm = helper.getAutomataIndexForm();
		nbrOfAutomata = helper.getAutomata().size();
		nbrOfEvents = helper.getAutomaton().getAlphabet().size();
		nextStateTable = indexForm.getNextStateTable();
		outgoingEventsTable = indexForm.getOutgoingEventsTable();
		incomingEventsTable = indexForm.getIncomingEventsTable();
		prioritizedEventsTable = indexForm.getPrioritizedEventsTable();
		typeIsPlantTable = indexForm.getTypeIsPlantTable();
		controllableEventsTable = indexForm.getControllableEventsTable();
		potentiallyUncontrollableStates = helper.getStateMemorizer();
		exhaustiveSearch = helper.getExhaustiveSearch();
		rememberUncontrollable = helper.getRememberUncontrollable();
		expandEventsUsingPriority = helper.getExpandEventsUsingPriority();

		if (expandEventsUsingPriority)
		{
			eventPriority = helper.getEventPriority();
			exhaustiveSearch = true;
			rememberUncontrollable = true;
		}

		syncOptions = synchronizerHelper.getSynchronizationOptions();
		syncType = syncOptions.getSynchronizationType();
		forbidUncontrollableStates = syncOptions.forbidUncontrollableStates();
		expandForbiddenStates = syncOptions.expandForbiddenStates();

		if (rememberUncontrollable)
		{
			expandForbiddenStates = false;
		}
	}

	// ÄNdrad till public istället för private... för att kunna köra findTransition
	public final void initialize()
	{
		currOutgoingEvents = new int[nbrOfAutomata][];
		currOutgoingEventsIndex = new int[nbrOfAutomata];
		currState = AutomataIndexFormHelper.createState(nbrOfAutomata);

		// +1 status field
		currEnabledEvents = new int[nbrOfEvents + 1];

		// Always end with Integer.MAX_VALUE
		if (automataIndices == null)
		{
			selectAllAutomata();
		}

		// System.arraycopy(initialState, 0, currState, 0, currState.length);
		// enabledEvents(currState);
	}

	// Only used by PairWiseControllabilityCheck (removable)
	// Select two automata
	public void selectTwoAutomata(int plantIndex, int supervisorIndex)
		throws Exception
	{
		automataIndices = new int[2];
		automataIndices[0] = plantIndex;
		automataIndices[1] = supervisorIndex;

		if (exhaustiveSearch)
		{
			throw new Exception("Exhaustive search used wrong way!");
		}
	}

	// Select some automata
	public void selectAutomata(ArrayList selectedAutomata)
		throws Exception
	{
		automataIndices = new int[selectedAutomata.size()];

		for (int i = 0; i < selectedAutomata.size(); i++)
		{
			automataIndices[i] = ((Automaton) selectedAutomata.get(i)).getIndex();
		}

		if (exhaustiveSearch)
		{
			throw new Exception("Exhaustive search used wrong way!");
		}
	}

	// Select some automata
	public void selectAutomata(int[] automataIndices)
		throws Exception
	{
		this.automataIndices = automataIndices;

		if (exhaustiveSearch)
		{
			throw new Exception("Exhaustive search used wrong way!");
		}
	}

	// Selects all automata
	public void selectAllAutomata()
	{
		automataIndices = new int[nbrOfAutomata];

		for (int i = 0; i < nbrOfAutomata; i++)
		{
			automataIndices[i] = i;
		}
	}

	private final void enabledEvents(int[] currState)
	{
		int currMinEventIndex = Integer.MAX_VALUE;

		nbrOfSelectedAutomata = automataIndices.length;

		// Insert all events that leaves the current state
		// into currOutgoingEvents, and intialize
		// currOutgoingEventsIndex.
		// Also find the smallest event index possible from
		// the current state.
		for (int i = 0; i < nbrOfSelectedAutomata; i++)
		{

			// Initialization part
			int currAutIndex = automataIndices[i];
			int currSingleStateIndex = currState[currAutIndex];

			currOutgoingEvents[currAutIndex] = outgoingEventsTable[currAutIndex][currSingleStateIndex];
			currOutgoingEventsIndex[currAutIndex] = 0;

			// Find the event with the smallest index.
			// The last element currOutgoingEvents[currAutIndex]
			// is always Integer.MAX_VALUE
			int currEventIndex = currOutgoingEvents[currAutIndex][0];

			if (currEventIndex < currMinEventIndex)
			{
				currMinEventIndex = currEventIndex;
			}
		}

		// Compute all events that are enabled in the current state
		int nbrOfEnabledEvents = 0;
		boolean thisEventOk;
		boolean thisPlantEventOk;
		boolean canExecuteInPlant;

		controllableState = true;

		while (currMinEventIndex < Integer.MAX_VALUE)
		{
			int currEventIndex = currMinEventIndex;

			currMinEventIndex = Integer.MAX_VALUE;
			thisEventOk = true;
			thisPlantEventOk = true;
			canExecuteInPlant = false;

			int currAutIndex = 0;
			int currAutEventIndex = 0;

			// Check that this event is possible in all automata
			// that must be ready to execute this event
			for (int i = 0; i < nbrOfSelectedAutomata; i++)
			{
				currAutIndex = automataIndices[i];

				// This is the index of the "next" event in the current automaton
				currAutEventIndex = currOutgoingEvents[currAutIndex][currOutgoingEventsIndex[currAutIndex]];

				if (syncType == SynchronizationType.Prioritized)
				{
					if (prioritizedEventsTable[currAutIndex][currEventIndex])
					{

						// The event is prioritized in currAutomaton
						if (!(currEventIndex == currAutEventIndex))
						{

							// Then currIndex (the event) must also be the
							// current event in this automaton
							thisEventOk = false;

							if (typeIsPlantTable[currAutIndex])
							{

								// Then currIndex (the event) must also be the
								// current event in this automaton
								thisPlantEventOk = false;
							}
						}
					}
				}
				else if (syncType == SynchronizationType.Full)
				{
					if (!(currEventIndex == currAutEventIndex))
					{

						// Then currIndex (the event) must also be the
						// current event in this automaton
						thisEventOk = false;

						if (typeIsPlantTable[currAutIndex])
						{

							// Then currIndex (the event) must also be the
							// current event in this automaton
							thisPlantEventOk = false;
						}
					}
				}
				else if (syncType == SynchronizationType.Broadcast)
				{
					if (typeIsPlantTable[currAutIndex])
					{

						// Then currIndex (the event) must also be the
						// current event in this automaton
						thisPlantEventOk = false;
					}
				}
				else
				{
					logger.error("Unknown SynchronizationType");

					// throw new Exception("Unknown SynchronizationType");
				}

				// Check if this can be executed in a plant
				if (!canExecuteInPlant && typeIsPlantTable[currAutIndex])
				{
					if (currEventIndex == currAutEventIndex)
					{

						// Then currIndex (the event) must also be the
						// current event in this automaton
						canExecuteInPlant = true;

						if (rememberUncontrollable)
						{

							// Remember uncontrollable states
							problemEvent = currEventIndex;
							problemPlant = currAutIndex;
						}
					}
				}

				// If the automata can execute the current event then
				// find the next event for this automaton and state
				// Independently of the alphabets!
				if (currEventIndex == currAutEventIndex)
				{

					// Point to the next index;
					int tmpIndex = currOutgoingEventsIndex[currAutIndex];

					currOutgoingEventsIndex[currAutIndex] = ++tmpIndex;
					currAutEventIndex = currOutgoingEvents[currAutIndex][tmpIndex];
				}

				// Find the new minimum index
				if (currAutEventIndex < currMinEventIndex)
				{
					currMinEventIndex = currAutEventIndex;
				}
			}

			if (thisEventOk)
			{
				currEnabledEvents[nbrOfEnabledEvents++] = currEventIndex;
			}

			if (!thisEventOk && canExecuteInPlant && thisPlantEventOk &&!controllableEventsTable[currEventIndex])
			{
				controllableState = false;

				helper.setAutomataIsControllable(false);

				if (exhaustiveSearch)
				{
					logger.info("The automata is uncontrollable.");
					System.err.println("Uncontrollable state found!!!!!");

					return;
				}
			}
		}

		// Always add Integer.MAX_VALUE as the last element
		currEnabledEvents[nbrOfEnabledEvents++] = Integer.MAX_VALUE;

		if (expandEventsUsingPriority)
		{

			// Choose outgoing events among the possibilities, choose after priority...
			int insertionIndex = 0;
			int i = 0;
			int minPrio = 2;
			int currPrio = 0;
			int currEvent;

			while ((currPrio < minPrio) && (insertionIndex == 0))
			{
				i = 0;
				currEvent = currEnabledEvents[i++];

				currPrio++;

				while (currEvent != Integer.MAX_VALUE)
				{
					if (eventPriority[currEvent] <= currPrio)
					{
						currEnabledEvents[insertionIndex++] = currEvent;
					}

					currEvent = currEnabledEvents[i++];
				}
			}

			// logger.debug("Current state has " + i + " enabled events...");
			// ... or if no events are prioritized, take the first two...
			if (insertionIndex == 0)
			{

				// logger.debug("Wandering aimlessly among the events...");
				insertionIndex = 2;
			}
			else
			{

				// logger.debug("Executing prioritized events (there are " + insertionIndex + " with priority " + currPrio + " to choose from)...");
			}

			currEnabledEvents[insertionIndex] = Integer.MAX_VALUE;
		}
	}

	/**
	 * Searches for transtition in automata.
	 *
	 *@param  fromState the state from which we want to know if there is a transition
	 *@param  toState the state t0 which we want to know if there is a transition
	 *@return  index of one (there may be more) transition between fromState and toState or -1 if none exists.
	 */
	public int findTransition(int[] fromState, int[] toState)
	{
		if (nextState == null)
		{
			nextState = AutomataIndexFormHelper.createState(nbrOfAutomata);
		}

		setCurrState(fromState);

		int i = 0;
		int currEventIndex = currEnabledEvents[i];

		// Handle all events
		while (currEventIndex != Integer.MAX_VALUE)
		{

			// Generate an array that contains the indicies of each state
			System.arraycopy(fromState, 0, nextState, 0, fromState.length);

			// Iterate over all automata to construct the new state
			for (int j = 0; j < nbrOfAutomata; j++)
			{
				int currAutomatonIndex = j;
				int currSingleNextState = nextStateTable[currAutomatonIndex][fromState[currAutomatonIndex]][currEventIndex];

				// Jump in all automata that have this event active.
				if (currSingleNextState != Integer.MAX_VALUE)
				{
					nextState[currAutomatonIndex] = currSingleNextState;
				}
			}

			if (equalsIntArray(nextState, toState))
			{
				return currEventIndex;
			}

			currEventIndex = currEnabledEvents[++i];
		}

		return -1;
	}

	// Compares int arrays, except for the last element (the status field)
	private static boolean equalsIntArray(int[] firstArray, int[] secondArray)
	{

		// Assume that the last element is a status field
		for (int i = 0; i < firstArray.length - AutomataIndexFormHelper.STATE_EXTRA_DATA; i++)
		{
			if (firstArray[i] != secondArray[i])
			{
				return false;
			}
		}

		return true;
	}

	public boolean isEnabled(int eventIndex)
	{
		int i = 0;
		int currEventIndex = currEnabledEvents[i];

		while (currEventIndex != Integer.MAX_VALUE)
		{
			if (currEventIndex == eventIndex)
			{
				return true;
			}

			currEventIndex = currEnabledEvents[++i];
		}

		return false;
	}

	// To synchronize the two executers...
	public void setCurrState(int[] state)
	{

		// for (int i=0; i<nbrOfSelectedAutomata; i++)
		// currState[automataIndices[i]] = state[automataIndices[i]];
		currState = state;

		enabledEvents(currState);
	}

	public boolean isControllable()
	{
		return controllableState;
	}

	/*
	 *  public boolean isAccepting()
	 *  {
	 *  return isControllable;
	 *  }
	 */
	public int[] doTransition(int eventIndex)
	{

		//System.err.println("doTransition: eventIndex " + eventIndex);
		// Counting on correct input here... only enabled events, please...
		// Construct new state
		int[] nextState = AutomataIndexFormHelper.createCopyOfState(currState);
		//int[] nextState = new int[currState.length];

		//System.arraycopy(currState, 0, nextState, 0, currState.length);

		//System.err.println("doTransition: nbrOfSelectedAutomata " + nbrOfSelectedAutomata);

		// Iterate over all automata to construct the new state
		for (int j = 0; j < nbrOfSelectedAutomata; j++)
		{
			int currAutomatonIndex = automataIndices[j];
			int currSingleNextState = nextStateTable[currAutomatonIndex][currState[currAutomatonIndex]][eventIndex];

			// Jump in all automata that have this event active.
			if (currSingleNextState != Integer.MAX_VALUE)
			{

				// currState[currAutomatonIndex] = currSingleNextState;
				nextState[currAutomatonIndex] = currSingleNextState;
			}
		}

		// enabledEvents(nextState);
		// System.arraycopy(nextState, 0, currState, 0, currState.length);
		currState = nextState;

		// return currState;
		return nextState;
	}

	public int[] getOutgoingEvents(int[] state)
	{
		enabledEvents(state);

		return currEnabledEvents;
	}

	public int[] getIncomingEvents(int[] state)
	{

		// Not finished... FIXA!
		return (new int[]{ 0, 1, Integer.MAX_VALUE });
	}
}
