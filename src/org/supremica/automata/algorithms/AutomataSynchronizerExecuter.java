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
import org.supremica.gui.*;
import org.apache.log4j.*;

import java.util.Arrays;

// For the automata selection methods
import java.util.ArrayList;

public final class AutomataSynchronizerExecuter
	extends Thread
{
	private final AutomataSynchronizerHelper helper;
	private final AutomataIndexForm indexForm;

 	private static Category thisCategory = LogDisplay.createCategory(AutomataSynchronizerExecuter.class.getName());

	private final int nbrOfAutomata;
	private final int nbrOfEvents;

	private final int[][][] nextStateTable;
	private final int[][][] outgoingEventsTable;
	private final boolean[][] prioritizedEventsTable;
	private final boolean[] typeIsPlantTable;
	private final boolean[] controllableEventsTable;

	private int[][] currOutgoingEvents;
	private int[] currOutgoingEventsIndex;
	private int[] automataIndices;
	private int[] currPlantAutomata;
	private int[] nextState;
	private int[] currEnabledEvents;
	private boolean controllableState;

	private final SynchronizationOptions syncOptions;
	private final SynchronizationType syncType;
	private boolean forbidUncontrollableStates;
	private boolean expandForbiddenStates;

	// For AutomataFastControllabilityCheck...
	private int problemPlant = Integer.MAX_VALUE;
	private int problemEvent = Integer.MAX_VALUE;
	private StateMemorizer potentiallyUncontrollableStates;
	private int[] eventPriority;
	private boolean expandEventsUsingPriority;
	private boolean rememberUncontrollable = false;
	private boolean exhaustiveSearch = false;

	private boolean coExecute = false;
	private AutomataOnlineSynchronizer coExecuter = null;

	// For "one event at a time"-execution
	private int currUncontrollableEvent = -1;

    public AutomataSynchronizerExecuter(AutomataSynchronizerHelper synchronizerHelper)
    {
        helper = synchronizerHelper;
        indexForm = helper.getAutomataIndexForm();
 		nbrOfAutomata = helper.getAutomata().size();
		nbrOfEvents = helper.getAutomaton().getAlphabet().size();
  		nextStateTable = indexForm.getNextStateTable();
		outgoingEventsTable = indexForm.getOutgoingEventsTable();
		prioritizedEventsTable = indexForm.getPrioritizedEventsTable();
		typeIsPlantTable = indexForm.getTypeIsPlantTable();
		controllableEventsTable = indexForm.getControllableEventsTable();
		potentiallyUncontrollableStates = helper.getStateMemorizer();
		exhaustiveSearch = helper.getExhaustiveSearch();
		rememberUncontrollable = helper.getRememberUncontrollable();
		expandEventsUsingPriority = helper.getExpandEventsUsingPriority();
		coExecute = helper.getCoExecute();
		coExecuter = helper.getCoExecuter();
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

	// Only used by AutomataPairWiseCheck (removable)
	// Select two automata
	public void selectTwoAutomata(int plantIndex, int supervisorIndex)
		throws Exception
	{
		automataIndices = new int[2];
		automataIndices[0] = plantIndex;
		automataIndices[1] = supervisorIndex;
		if (exhaustiveSearch)
			throw new Exception("Exhaustive search used wrong way!");
	}

	// Select some automata
	public void selectAutomata(ArrayList automataToBeSelected)
		throws Exception
	{
		automataIndices = new int[automataToBeSelected.size()];
		for (int i = 0; i < automataToBeSelected.size(); i++)
		{
			automataIndices[i] = ((Automaton) automataToBeSelected.get(i)).getIndex();
		}
		helper.selectAutomata(automataIndices); // FIXA!
		if (exhaustiveSearch)
			throw new Exception("Exhaustive search used wrong way!");
	}

	// Select some automata
	public void selectAutomata(int[] automataIndices)
		throws Exception
	{
		this.automataIndices = automataIndices;
		if (exhaustiveSearch)
			throw new Exception("Exhaustive search used wrong way!");
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

	// ÄNdrad till public istället för private... för att kunna köra findTransistion
	public final void initialize()
	{
		currOutgoingEvents = new int[nbrOfAutomata][];
		currOutgoingEventsIndex = new int[nbrOfAutomata];

		if (automataIndices == null)
			selectAllAutomata();

		nextState = new int[nbrOfAutomata + 1]; // +1 status field
		currEnabledEvents = new int[nbrOfEvents + 1]; // Always end with Integer.MAX_VALUE
	}

 	private final void enabledEvents(int[] currState)
  	{
		int currMinEventIndex = Integer.MAX_VALUE;
		int nbrOfSelectedAutomata = automataIndices.length;

		// Insert all events that leaves the current state
		// into currOutgoingEvents, and intialize
		// currOutgoingEventsIndex.
		// Also find the smallest event index possible from
		// the current state.
		for(int i = 0; i < nbrOfSelectedAutomata; i++)
		{
			// Initialization part
			int currAutIndex = automataIndices[i];
			int currSingleStateIndex = currState[currAutIndex];

			currOutgoingEvents[currAutIndex] =
				outgoingEventsTable[currAutIndex][currSingleStateIndex];

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
				currAutEventIndex =
					currOutgoingEvents[currAutIndex][currOutgoingEventsIndex[currAutIndex]];

				if (syncType == SynchronizationType.Prioritized)
				{
					if (prioritizedEventsTable[currAutIndex][currEventIndex])
					{   // The event is prioritized in currAutomaton
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
					thisCategory.error("Unknown SynchronizationType");
					// throw new Exception("Unknown SynchronizationType");
				}

				// Check if this can be executed in a plant
				if (!canExecuteInPlant && typeIsPlantTable[currAutIndex])
				{
					if (currEventIndex == currAutEventIndex)
					{	// Then currIndex (the event) must also be the
						// current event in this automaton
						canExecuteInPlant = true;

						if (rememberUncontrollable)
						{   // Remember uncontrollable states
							problemEvent = currEventIndex;
							problemPlant = currAutIndex;
						}
					}
				}

				// If the automata can execute the current event then
				// find the next event for this automaton and state
				// Independently of the alphabets!
				if (currEventIndex == currAutEventIndex)
				{ // Point to the next index;
					int tmpIndex = currOutgoingEventsIndex[currAutIndex];
					currOutgoingEventsIndex[currAutIndex] = ++tmpIndex;
					currAutEventIndex =
						currOutgoingEvents[currAutIndex][tmpIndex];
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
			if (!thisEventOk && canExecuteInPlant && thisPlantEventOk && !controllableEventsTable[currEventIndex])
			{
				controllableState = false;
				helper.setAutomataIsControllable(false);
				//
				if (exhaustiveSearch)
				{   // Stop when uncontrollable state found
					// thisCategory.info("The automata is uncontrollable.");
					// System.err.println("Uncontrollable state found!!!!!");
					return;
				}
			}
		}

		// Always add Integer.MAX_VALUE as the last element
		currEnabledEvents[nbrOfEnabledEvents++] = Integer.MAX_VALUE;

		if (expandEventsUsingPriority)
		{   // Choose outgoing events among the possibilities, choose after priority...
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
			// ... or if no events are prioritized, take the first two...
			if (insertionIndex == 0)
			{
				insertionIndex = 2;
			}
			currEnabledEvents[insertionIndex] = Integer.MAX_VALUE;
		}

		if (coExecute)
		{
			int insertionIndex = 0;
			int i = 0;
			int currEvent = currEnabledEvents[i++];
			while (currEvent != Integer.MAX_VALUE)
			{
				if (coExecuter.isEnabled(currEvent))
				{
					currEnabledEvents[insertionIndex++] = currEvent;
				}
				currEvent = currEnabledEvents[i++];
			}
			if (insertionIndex == 0)
			{
				// thisCategory.debug("Wandering aimlessly...");
				// insertionIndex = 2;
				insertionIndex = 20;
			}
			else
			{
				// thisCategory.debug("Following the problematic automata, there are " + insertionIndex + " transitions...");
			}
			currEnabledEvents[insertionIndex] = Integer.MAX_VALUE;
		}
	}

    public void run()
    {
    	initialize();

        // Get the first state to process
       	int[] currState = helper.getStateToProcess();

 		// main loop
     	while (currState != null)
     	{
			if (coExecute)
				coExecuter.setCurrState(currState);

			enabledEvents(currState);

			if (!controllableState)
			{	// We'd like to remember this state and later on try to show that
				// it will be excluded in the total synchronization...  or not.
				if (rememberUncontrollable)
					if (currUncontrollableEvent == -1 || currUncontrollableEvent == problemEvent)
						potentiallyUncontrollableStates.add(automataIndices, currState, problemPlant, problemEvent);
				// We now know that there is an uncontrollable state
				if (exhaustiveSearch)
					return;
				// Forbid uncontrollable state?
				if (forbidUncontrollableStates)
					helper.setForbidden(currState, true);
			}

			if (controllableState || expandForbiddenStates)
			{   // Expand state
				int i = 0;
				int currEventIndex = currEnabledEvents[i];
				// Handle all events
				while (currEventIndex != Integer.MAX_VALUE)
				{
					// Generate an array that contains the indicies of each state
					System.arraycopy(currState, 0, nextState, 0, currState.length);

					// Iterate over all automata to construct the new state
					for (int j = 0; j < automataIndices.length; j++)
					{
						int currAutomatonIndex = automataIndices[j];
						int currSingleNextState =
							nextStateTable[currAutomatonIndex][currState[currAutomatonIndex]][currEventIndex];
						// Jump in all automata that have this event defined.
						if (currSingleNextState != Integer.MAX_VALUE)
						{
							nextState[currAutomatonIndex] = currSingleNextState;
						}
					}
					// Add this state to the automaton
					// and include it in the queue of states waiting for
					// processing if it has not been processed before

					// Update this to check if the state is not forbidden instead of
					// only controllable.
					try
					{
						helper.addState(currState, nextState, currEventIndex);
					}
					catch (Exception e)
					{
						System.err.println(e);
						thisCategory.error("Error in SynchronizerExecuter");
						return;
					}
					currEventIndex = currEnabledEvents[++i];
				}
			}
      		currState = helper.getStateToProcess();
        }
    }

	public void buildAutomaton()
    {
		buildAutomaton(true);
	}
	
	public void buildAutomaton(boolean longformId)
    {
        Automaton theAutomaton = helper.getAutomaton();
        theAutomaton.setName("regaut");
		Alphabet theAlphabet = theAutomaton.getAlphabet();

        int[][] currStateTable = helper.getStateTable();
		int stateNumber = 0;

        // Create all states
		for (int i = 0; i < currStateTable.length; i++)
  		{
			if (currStateTable[i] != null)
   			{
				int[] currState = currStateTable[i];
    			State newState = new State();

    			newState.setIndex(i);

       			if (longformId)
          		{
                	State[][] stateTable = indexForm.getStateTable();
                	StringBuffer sb = new StringBuffer();
					for (int j = 0; j < currState.length - 1; j++)
     				{
             			sb.append(stateTable[j][currState[j]].getId());
					}
     				newState.setId(sb.toString());
                }
                else
                {
       				newState.setId("q" + stateNumber++);
				}
				newState.setName(newState.getId());
          		newState.setInitial(AutomataIndexFormHelper.isInitial(currState));
          		newState.setAccepting(AutomataIndexFormHelper.isAccepting(currState));
           		newState.setForbidden(AutomataIndexFormHelper.isForbidden(currState));
          		newState.setFirst(AutomataIndexFormHelper.isFirst(currState));
          		newState.setLast(AutomataIndexFormHelper.isLast(currState));

          		theAutomaton.addState(newState);
          	}
        }

        // Create all transitions
 		for (int k = 0; k < currStateTable.length; k++)
  		{
			if (currStateTable[k] != null)
   			{
				int[] currState = currStateTable[k];
    			State thisState = theAutomaton.getStateWithIndex(k);

				enabledEvents(currState);

		      	int i = 0;
		      	int currEventIndex = currEnabledEvents[i];

         		// Handle all events
		        while (currEventIndex != Integer.MAX_VALUE)
		        {
		  			// Generate an array that contains the indicies of each state
		         	System.arraycopy(currState, 0, nextState, 0, currState.length);

		    		// Iterate over all automata to construct the new state
					for (int j = 0; j < automataIndices.length; j++)
		    		{
		           		int currAutomatonIndex = automataIndices[j];
						int currSingleNextState =
		     				nextStateTable[currAutomatonIndex][currState[currAutomatonIndex]][currEventIndex];
		           		// Jump in all automata that have this event active.
		            	if (currSingleNextState != Integer.MAX_VALUE)
		            	{
		            		nextState[currAutomatonIndex] = currSingleNextState;
		               	}
		            }

		            try
		            {
                  		// Check if nextState exists
                  		int nextIndex = helper.getStateIndex(nextState);
                    	if (nextIndex >= 0)
                     	{
                      		State nextState = theAutomaton.getStateWithIndex(nextIndex);
							Event theEvent = theAlphabet.getEventWithIndex(currEventIndex);
       						Arc newArc = new Arc(thisState, nextState, theEvent.getId());
                    		theAutomaton.addArc(newArc);
                     	}
		       		}
		            catch (Exception e)
		            {
		        		System.err.println(e);
		           		System.exit(0);
		       		}

		         	currEventIndex = currEnabledEvents[++i];
		         }
          	}
        }

        if (helper.isAllAutomataPlants())
        {
			theAutomaton.setType(AutomatonType.Plant);
		}
		else if (helper.isAllAutomataSupervisors())
        {
			theAutomaton.setType(AutomatonType.Supervisor);
		}
		else if (helper.isAllAutomataSpecifications())
        {
			theAutomaton.setType(AutomatonType.Specification);
		}
		else
		{
			theAutomaton.setType(AutomatonType.Undefined);
		}
    }

    public String printTypeIsPlantTable()
    {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < typeIsPlantTable.length; i++)
		{
			sb.append(typeIsPlantTable[i]);
			if (i != typeIsPlantTable.length - 1)
			{
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

/*
    public int[][] previousStates(int[] state, int currEventIndex)
    {
   		int[][][][] prevStatesTable = theAutomataIndexForm.getPrevStatesTable();
   		int[] nbrOfPrevStates = int[nbrOfAutomata];
   		int[] currIndexOfPrevStates = int[nbrOfAutomata];
   		int[] currPrevState = int[nbrOfAutomata + 1];

   		// First compute the maximum nbr of previous states
    	int maxNbrOfPreviousStates = 1;
    	for (int i = 1; i < state.length - 1; i++)
    	{
    		// ToDo Check if this automaton is among the selected

    		int currAutomatonIndex = i;
    		int currStateIndex = state[currAutomatonIndex];
    		int[] prevStates = prevStatesTable[currAutomatonIndex][currStateIndex][currEventIndex];
    		if (prevStates != null)
    		{
    			int currNbrOfPreviousStates = prevStates[prevStates.length - 1];
    			nbrOfPrevStates[i] = currNbrOfPreviousStates;
    			if (currNbrOfPreviousStates > 0)
    			{
    				currIndexOfPrevStates
    			}
    			else
    			{
    				currIndexOfPreviousState[i] = 0;
    			}
    			maxNbrOfPreviousStates = maxNbrOfPreviousStates * currNbrOfPreviousStates;
    		}
    	}

    	int[][] previousStates = new int[maxNbrOfPreviousState][];
		for (int i = 1; i < state.length - 2; i++)
		{
			for(int j = i + 1; j < )

		}

		// Check if this event is included
		int[] existingPrevState = theStates.get(currPrevState);
		if (existingPrevState != null)
		{
			// Check if the event is really possible
			if (isValidTransition(existingPrevState, state, currEventIndex))
			{
				previousStates[xx] = existingPrevState;
			}
		}

    }
*/

    /**
     * Check if the event is possible between fromState and toState.
     * For perfomance reasons we assume that event is possible in
     * at least one of the original automata.
     */
/*    public boolean isValidTransition(int[] fromState, int[] toState, int event)
    {
    	if (prioritizedEventInResultAutomaton[event])
    	{ // Check that event is possible from all automata that have
    	  // this event as prioritized.

    		// To do
    	}
    	else
    	{ // We assume that the event is possible in at least one of the
    	  // original automata.
    		return true;
    	}
    }
*/

	// Returns index of one (of perhaps many) transitions between fromState and toState or -1 if none exists.
	public int findTransition(int[] fromState, int[] toState)
	{
		enabledEvents(fromState);

		int i = 0;
		int currEventIndex = currEnabledEvents[i];
		// Handle all events
		while (currEventIndex != Integer.MAX_VALUE)
		{	// Generate an array that contains the indicies of each state
			System.arraycopy(fromState, 0, nextState, 0, fromState.length);

			// Iterate over all automata to construct the new state
			for (int j = 0; j < nbrOfAutomata; j++)
			{
				int currAutomatonIndex = j;
				int currSingleNextState =
					nextStateTable[currAutomatonIndex][fromState[currAutomatonIndex]][currEventIndex];
				// Jump in all automata that have this event active.
				if (currSingleNextState != Integer.MAX_VALUE)
				{
					nextState[currAutomatonIndex] = currSingleNextState;
				}
			}
			if (equalsIntArray(nextState, toState))
				return currEventIndex;
			currEventIndex = currEnabledEvents[++i];
		}
		return -1;
	}

	// Compares int arrays, except for the last element (the status field)
	private static boolean equalsIntArray(int[] firstArray, int[] secondArray)
 	{	// Assume that the last element is a status field
		for (int i = 0; i < firstArray.length - 1; i++)
  		{
			if (firstArray[i] != secondArray[i])
				return false;
        }
        return true;
	}

	public void setCurrUncontrollableEvent(Event event)
	{
		currUncontrollableEvent = event.getSynchIndex();
	}

// 	public boolean isEnabled(int eventIndex)
// 	{
// 		int i = 0;
// 		currEventIndex = currEnabledEvent[i];
// 		while (currEventIndex != Integer.MAX_VALUE)
// 		{
// 			if (currEventIndex == eventIndex)
// 				return true;
// 			currEventIndex = currEnabledEvents[++i];
// 		}
// 		return false;
// 	}

// 	public void doTransition(int eventIndex)
// 	{
// 		// Construct new state
// 		System.arraycopy(currState, 0, nextState, 0, fromState.length);
// 		// Iterate over all automata to construct the new state
// 		for (int j = 0; j < nbrOfAutomata; j++)
// 		{
// 			int currAutomatonIndex = j;
// 			int currSingleNextState =
// 				nextStateTable[currAutomatonIndex][fromState[currAutomatonIndex]][eventIndex];
// 			// Jump in all automata that have this event active.
// 			if (currSingleNextState != Integer.MAX_VALUE)
// 			{
// 				nextState[currAutomatonIndex] = currSingleNextState;
// 			}
// 		}
// 		enabledEvents(nextState);
// 	}
}
