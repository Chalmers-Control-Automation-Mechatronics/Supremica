
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
import java.util.ArrayList;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.AutomataIndexForm;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;

/**
 *@author  ka
 *@created  November 28, 2001
 *@deprecated  DYNG-DEPRECATED, ALLTIHOP, NU ANVÄNDS AutomataSynchronizerExecuter TILL ALLT SÅN'T HÄR TRAMS
 */
public final class AutomataControllabilityCheckExecuter
	extends Thread
{
	private static Logger logger = LoggerFactory.createLogger(AutomataControllabilityCheckExecuter.class);
	private final AutomataSynchronizerHelper helper;
	private final AutomataIndexForm indexForm;
	private final int nbrOfAutomata;
	private final int nbrOfEvents;
	private final int[][][] nextStateTable;
	private final int[][][] outgoingEventsTable;
	private final boolean[][] prioritizedEventsTable;
	private final boolean[] typeIsPlantTable;
	private final boolean[] controllableEventsTable;

	// private final boolean[] prioritizedEventInResultAutomaton;
	private int[][] currOutgoingEvents;
	private int[] currOutgoingEventsIndex;
	private int[] currSelectedAutomata;
	private int[] currPlantAutomata;
	private int[] nextState;
	private int[] currEnabledEvents;

	// private int[] currPlantEnabledEvents;
	private boolean controllableState;

	public AutomataControllabilityCheckExecuter(AutomataSynchronizerHelper synchronizerHelper)
	{
		setPriority(Thread.MIN_PRIORITY);
		helper = synchronizerHelper;
		indexForm = helper.getAutomataIndexForm();
		nbrOfAutomata = helper.getAutomata().size();
		nbrOfEvents = helper.getAutomaton().getAlphabet().size();
		nextStateTable = indexForm.getNextStateTable();
		outgoingEventsTable = indexForm.getOutgoingEventsTable();
		prioritizedEventsTable = indexForm.getPrioritizedEventsTable();
		typeIsPlantTable = indexForm.getTypeIsPlantTable();
		controllableEventsTable = indexForm.getControllableEventsTable();
	}

	// Select two automata
	public void selectTwoAutomata(int plantIndex, int supervisorIndex)
	{
		currSelectedAutomata = new int[2];
		currSelectedAutomata[0] = plantIndex;
		currSelectedAutomata[1] = supervisorIndex;
	}

	// Select some automata
	public void selectAutomata(ArrayList automataToBeSelected)
	{
		currSelectedAutomata = new int[automataToBeSelected.size()];

		for (int i = 0; i < automataToBeSelected.size(); i++)
		{
			currSelectedAutomata[i] = ((Integer) automataToBeSelected.get(i)).intValue();

			// logger.debug("Selected: " + currSelectedAutomata[i]);
		}
	}

	// Compute an array that selects all automata
	// A more narrow selection can be made in many cases
	public void selectAllAutomata()
	{
		currSelectedAutomata = new int[nbrOfAutomata];

		for (int i = 0; i < nbrOfAutomata; i++)
		{
			currSelectedAutomata[i] = i;
		}
	}

	private final void initialize()
	{
		currOutgoingEvents = new int[nbrOfAutomata][];
		currOutgoingEventsIndex = new int[nbrOfAutomata];
		nextState = new int[nbrOfAutomata + 1];

		// +1 status field
		currEnabledEvents = new int[nbrOfEvents + 1];

		// Always end with Integer.MAX_VALUE
		// currPlantEnabledEvents = new int[nbrOfEvents + 1]; // Always end with Integer.MAX_VALUE
	}

	private final void enabledEvents(int[] currState)
	{
		int currMinEventIndex = Integer.MAX_VALUE;
		int nbrOfSelectedAutomata = currSelectedAutomata.length;

		// Insert all events that leaves the current state
		// into currOutgoingEvents, and intialize
		// currOutgoingEventsIndex.
		// Also find the smallest event index possible from
		// the current state.
		for (int i = 0; i < nbrOfSelectedAutomata; i++)
		{

			// Initialization part
			int currAutIndex = currSelectedAutomata[i];
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

		// int nbrOfPlantEnabledEvents = 0;
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

			// Check that this event is possible in all automata
			// must be ready to execute this event
			for (int i = 0; i < nbrOfSelectedAutomata; i++)
			{
				int currAutIndex = currSelectedAutomata[i];

				// This is the index of the "next" event in the current automaton
				int currAutEventIndex = currOutgoingEvents[currAutIndex][currOutgoingEventsIndex[currAutIndex]];

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

				// Check if this can be executed in a plant
				if (!canExecuteInPlant && typeIsPlantTable[currAutIndex])
				{
					if (currEventIndex == currAutEventIndex)
					{

						// Then currIndex (the event) must also be the
						// current event in this automaton
						canExecuteInPlant = true;
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

				// We've found an uncontrollable state so we can return, knowing the automata is uncontrollable
				return;
			}
		}

		// Always add Integer.MAX_VALUE as the last element
		currEnabledEvents[nbrOfEnabledEvents++] = Integer.MAX_VALUE;
	}

	public void run()
	{
		initialize();

		// Get the first state to process
		int[] currState = helper.getStateToProcess();

		// main loop
		while (currState != null)
		{
			enabledEvents(currState);

			if (controllableState)
			{
				int i = 0;
				int currEventIndex = currEnabledEvents[i];

				// Handle all events
				while (currEventIndex != Integer.MAX_VALUE)
				{

					// Generate an array that contains the indicies of each state
					System.arraycopy(currState, 0, nextState, 0, currState.length);

					// Iterate over all automata to construct the new state
					for (int j = 0; j < currSelectedAutomata.length; j++)
					{
						int currAutomatonIndex = currSelectedAutomata[j];
						int currSingleNextState = nextStateTable[currAutomatonIndex][currState[currAutomatonIndex]][currEventIndex];

						// Jump in all automata that have this event defined.
						if (currSingleNextState != Integer.MAX_VALUE)
						{
							nextState[currAutomatonIndex] = currSingleNextState;
						}
					}

					// Add this state to the automaton
					// and include it in the queue of states waiting for
					// processing if it has not been processed before
					try
					{
						helper.addState(currState, nextState, currEventIndex);
					}
					catch (Exception e)
					{
						System.err.println(e);
						System.exit(0);
					}

					currEventIndex = currEnabledEvents[++i];
				}
			}
			else
			{

				// We now know that there is a forbidden state in the synhronized
				// automaton and we can return, killing the thread
				return;
			}

			currState = helper.getStateToProcess();
		}
	}

	public void buildAutomaton()
	{
		Automaton theAutomaton = helper.getAutomaton();

		theAutomaton.setName("regaut");

		Alphabet theAlphabet = theAutomaton.getAlphabet();
		int[][] currStateTable = helper.getStateTable();

		// Create all states
		for (int i = 0; i < currStateTable.length; i++)
		{
			if (currStateTable[i] != null)
			{
				int[] currState = currStateTable[i];
				State newState = new State();

				newState.setIndex(i);

				boolean longformId = true;

				if (longformId)
				{
					State[][] stateTable = indexForm.getStateTable();
					StringBuffer sb = new StringBuffer();

					// for (int j = 0; j < currState.length; j++)
					// {
					// System.out.print(currState[j] + " ");
					// sb.append(stateTable[j][currState[j]].getId());
					// }
					// System.out.println("");
					for (int j = 0; j < currState.length - 1; j++)
					{

						// System.out.println(stateTable[j][currState[j]]);
						sb.append(stateTable[j][currState[j]].getId());
					}

					newState.setId(sb.toString());
				}
				else
				{
					newState.setId("q" + i);
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
					for (int j = 0; j < currSelectedAutomata.length; j++)
					{
						int currAutomatonIndex = currSelectedAutomata[j];
						int currSingleNextState = nextStateTable[currAutomatonIndex][currState[currAutomatonIndex]][currEventIndex];

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
							LabeledEvent theEvent = theAlphabet.getEventWithIndex(currEventIndex);
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
	}

	public String printArray(int[] theArray)
	{
		StringBuffer sb = new StringBuffer();

		sb.append("[");

		for (int i = 0; i < theArray.length; i++)
		{
			sb.append(theArray[i]);

			if (i != theArray.length - 1)
			{
				sb.append(", ");
			}
		}

		sb.append("]");

		return sb.toString();
	}

	/*
	 *  public int[][] previousStates(int[] state, int currEventIndex)
	 *  {
	 *  int[][][][] prevStatesTable = theAutomataIndexForm.getPrevStatesTable();
	 *  int[] nbrOfPrevStates = int[nbrOfAutomata];
	 *  int[] currIndexOfPrevStates = int[nbrOfAutomata];
	 *  int[] currPrevState = int[nbrOfAutomata + 1];
	 *
	 *  // First compute the maximum nbr of previous states
	 *  int maxNbrOfPreviousStates = 1;
	 *  for (int i = 1; i < state.length - 1; i++)
	 *  {
	 *  // ToDo Check if this automaton is among the selected
	 *
	 *  int currAutomatonIndex = i;
	 *  int currStateIndex = state[currAutomatonIndex];
	 *  int[] prevStates = prevStatesTable[currAutomatonIndex][currStateIndex][currEventIndex];
	 *  if (prevStates != null)
	 *  {
	 *  int currNbrOfPreviousStates = prevStates[prevStates.length - 1];
	 *  nbrOfPrevStates[i] = currNbrOfPreviousStates;
	 *  if (currNbrOfPreviousStates > 0)
	 *  {
	 *  currIndexOfPrevStates
	 *  }
	 *  else
	 *  {
	 *  currIndexOfPreviousState[i] = 0;
	 *  }
	 *  maxNbrOfPreviousStates = maxNbrOfPreviousStates * currNbrOfPreviousStates;
	 *  }
	 *  }
	 *
	 *  int[][] previousStates = new int[maxNbrOfPreviousState][];
	 *  for (int i = 1; i < state.length - 2; i++)
	 *  {
	 *  for(int j = i + 1; j < )
	 *
	 *  }
	 *
	 *  // Check if this event is included
	 *  int[] existingPrevState = theStates.get(currPrevState);
	 *  if (existingPrevState != null)
	 *  {
	 *  // Check if the event is really possible
	 *  if (isValidTransition(existingPrevState, state, currEventIndex))
	 *  {
	 *  previousStates[xx] = existingPrevState;
	 *  }
	 *  }
	 *
	 *  }
	 */

	/*
	 *  public boolean isValidTransition(int[] fromState, int[] toState, int event)
	 *  {
	 *  if (prioritizedEventInResultAutomaton[event])
	 *  { // Check that event is possible from all automata that have
	 *  // this event as prioritized.
	 *
	 *  // To do
	 *  }
	 *  else
	 *  { // We assume that the event is possible in at least one of the
	 *  // original automata.
	 *  return true;
	 *  }
	 *  }
	 */
}
