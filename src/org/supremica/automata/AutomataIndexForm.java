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
package org.supremica.automata;

import java.util.*;
import org.supremica.gui.*;
import org.supremica.log.*;

public final class AutomataIndexForm
{
	// <automaton,event> -> <true|false>
	private boolean[][] alphabetEventsTable;

	// <automaton,event> -> <true|false>
	private boolean[][] prioritizedEventsTable;

	// <automaton,state> -> <event[]>
	private int[][][] outgoingEventsTable;

	// <automaton,state> -> <event[]>
	private int[][][] incomingEventsTable;

	// <automaton,state,event> -> <state>
	private int[][][] nextStateTable;

	// <automaton, state, event> -> <state[]>
	private int[][][][] prevStatesTable;

	// <automaton,state> -> <State>
	private State[][] stateTable;

	// <automaton,state> -> <status>
	private int[][] stateStatusTable;

	// <event> -> <true|false>
	private boolean[] controllableEventsTable;

	// <event> -> <true|false>
	private boolean[] immediateEventsTable;

	// <automaton> -> <isPlant>
	private boolean[] typeIsPlantTable;

	// <event> -> <priority>
	private int[] eventPriority;

	// <automaton> -> <nbr_of_states>
	private int[] automataSize;

	// <automaton, event> -> <state[]>
	private int[][][] enableEventsTable;

	private Automata theAutomata = null;


	private static Logger logger = LoggerFactory.createLogger(AutomataIndexForm.class);

	/**
	 *@param  theAutomata The automata to be synchronized.
	 *@param  theAutomaton The synchronized automaton.
	 *@exception  Exception Description of the Exception
	 */
	public AutomataIndexForm(Automata theAutomata, Automaton theAutomaton)
		throws Exception
	{
		//Automata newAutomata = new Automata(theAutomata);
		//newAutomata.addAutomaton(theAutomaton);
		Alphabet unionAlphabet = theAutomata.setIndicies();
		theAutomaton.getAlphabet().union(unionAlphabet);
		//theAutomaton.setIndicies();

		this.theAutomata = theAutomata;

		generateAutomataIndices(theAutomata);

		try
		{
			generateEventIndices(theAutomata, theAutomaton);
		}
		catch (Exception ex)
		{
			logger.error("Error while generating AutomataIndexForm", ex);
			logger.debug(ex.getStackTrace());
			throw ex;
		}

		generateStateIndices(theAutomata);
		generateNextStateTransitionIndices(theAutomata, theAutomaton);
		generatePrevStatesTransitionIndices(theAutomata, theAutomaton);
		generateControllableEventsTable(theAutomaton);
	}

	public AutomataIndexForm(AutomataIndexForm indexForm)
	{
		this(indexForm, false);
	}

	public AutomataIndexForm(AutomataIndexForm indexForm, boolean deepCopy)
	{
		if (deepCopy)
		{
			alphabetEventsTable = generateCopy2DBooleanArray(indexForm.alphabetEventsTable);
			prioritizedEventsTable = generateCopy2DBooleanArray(indexForm.prioritizedEventsTable);
			nextStateTable = generateCopy3DIntArray(indexForm.nextStateTable);
			outgoingEventsTable = generateCopy3DIntArray(indexForm.outgoingEventsTable);
			incomingEventsTable = generateCopy3DIntArray(indexForm.incomingEventsTable);
			stateTable = generateCopy2DStateArray(indexForm.stateTable);
			stateStatusTable = generateCopy2DIntArray(indexForm.stateStatusTable);
			typeIsPlantTable = generateCopy1DBooleanArray(indexForm.typeIsPlantTable);
			controllableEventsTable = generateCopy1DBooleanArray(indexForm.controllableEventsTable);
			immediateEventsTable = generateCopy1DBooleanArray(indexForm.immediateEventsTable);
			automataSize = generateCopy1DIntArray(indexForm.automataSize);
			enableEventsTable = generateCopy3DIntArray(indexForm.enableEventsTable);
		}
		else
		{
			alphabetEventsTable = indexForm.alphabetEventsTable;
			prioritizedEventsTable = indexForm.prioritizedEventsTable;
			nextStateTable = indexForm.nextStateTable;
			outgoingEventsTable = indexForm.outgoingEventsTable;
			incomingEventsTable = indexForm.incomingEventsTable;
			stateTable = indexForm.stateTable;
			stateStatusTable = indexForm.stateStatusTable;
			typeIsPlantTable = indexForm.typeIsPlantTable;
			controllableEventsTable = indexForm.controllableEventsTable;
			immediateEventsTable = indexForm.immediateEventsTable;
			automataSize = indexForm.automataSize;
			enableEventsTable = indexForm.enableEventsTable;
		}
	}

	public void generateAutomataIndices(Automata theAutomata)
	{
		// Give each automaton a unique index
		// Remember that this index must be consistent with
		// getAutomatonAt(int) in Automata
		typeIsPlantTable = new boolean[theAutomata.size()];
		automataSize = new int[theAutomata.size()];

		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			int i = currAutomaton.getIndex();

			typeIsPlantTable[i] = currAutomaton.getType() == AutomatonType.Plant;
			automataSize[i] = currAutomaton.nbrOfStates();
		}
	}

	void generateEventIndices(Automata theAutomata, Automaton theAutomaton)
		throws Exception
	{
		Alphabet theAlphabet = theAutomaton.getAlphabet();

		// Generate a synchIndex for each event
		//Collection eventCollection = theAlphabet.values();

		eventPriority = new int[theAlphabet.size()];
		for (int i = 0; i < eventPriority.length; i++)
		{
			eventPriority[i] = 1;
		}

		// Build tables from where it fast can be concluded
		// if a certain event is included or prioritized in a given automaton
		int nbrOfAutomata = theAutomata.size();

		alphabetEventsTable = new boolean[nbrOfAutomata][theAlphabet.size()];
		prioritizedEventsTable = new boolean[nbrOfAutomata][theAlphabet.size()];

		for (Iterator theAlphabetIt = theAlphabet.iterator(); theAlphabetIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) theAlphabetIt.next();
			String currLabel = currEvent.getLabel();
			int currEventSynchIndex = currEvent.getSynchIndex();

			for (int i = 0; i < nbrOfAutomata; i++)
			{
				Alphabet currAutAlphabet = theAutomata.getAutomatonAt(i).getAlphabet();

				if (currAutAlphabet.containsEventWithLabel(currLabel))
				{
					alphabetEventsTable[i][currEventSynchIndex] = true;

					LabeledEvent currAutEvent = currAutAlphabet.getEventWithLabel(currLabel);

					prioritizedEventsTable[i][currEventSynchIndex] = currAutEvent.isPrioritized();
				}
				else
				{
					//System.err.println("i: " + i + " currEventSynchIndex: " + currEventSynchIndex);
					alphabetEventsTable[i][currEventSynchIndex] = false;
					prioritizedEventsTable[i][currEventSynchIndex] = false;
				}
			}
		}
	}

	/**
	 * Creates an index in each state in all automata. The index is
	 * unique for the automaton to which the state belongs.
	 * Also builds a state table that connects the state index to
	 * the physical state.
	 *
	 *@param  theAutomata Description of the Parameter
	 */
	void generateStateIndices(Automata theAutomata)
	{
		stateTable = new State[theAutomata.size()][];
		stateStatusTable = new int[theAutomata.size()][];

		for (Iterator autIt = theAutomata.iterator(); autIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			int currAutomatonIndex = currAutomaton.getIndex();
			int currNbrOfStates = currAutomaton.nbrOfStates();

			stateTable[currAutomatonIndex] = new State[currNbrOfStates];
			stateStatusTable[currAutomatonIndex] = new int[currNbrOfStates];

			for (StateIterator stateIt = currAutomaton.stateIterator(); stateIt.hasNext(); )
			{
				State currState = stateIt.nextState();

				int currIndex = currState.getIndex();

				stateTable[currAutomatonIndex][currIndex] = currState;
				stateStatusTable[currAutomatonIndex][currIndex] = AutomataIndexFormHelper.createStatus(currState);

				//currIndex++;
			}
		}
	}

	/**
	 * For each state in the automaton precompute an array
	 * that contains the index of all events that leave the current
	 * state. This array must be sorted, and the last element must be
	 * Integer.MAX_VALUE. Note that this computation can not be
	 * done in the states, since they do not know about the alphabet.
	 *
	 * Insert into enableEventsTable all states that enables a specific event.
	 *
	 * @param  theAutomata Description of the Parameter
	 * @param  theAutomaton Description of the Parameter
	 * @exception  Exception Description of the Exception
	 */
	void generateNextStateTransitionIndices(Automata theAutomata, Automaton theAutomaton)
		throws Exception
	{
		// Compute the nextStateTable and outgoingEventsTable
		Alphabet theAlphabet = theAutomaton.getAlphabet();
		int nbrOfAutomata = theAutomata.size();
		int nbrOfEvents = theAlphabet.size();

		nextStateTable = new int[nbrOfAutomata][][];
		outgoingEventsTable = new int[nbrOfAutomata][][];
		enableEventsTable = new int[nbrOfAutomata][][];

		TreeSet sortedArcs = new TreeSet();
		int alphabetSize = theAlphabet.size();
		Iterator autIt = theAutomata.iterator();

		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			int currAutomatonIndex = currAutomaton.getIndex();
			int currAutomatonNbrOfStates = currAutomaton.nbrOfStates();

			nextStateTable[currAutomatonIndex] = new int[currAutomatonNbrOfStates][];
			outgoingEventsTable[currAutomatonIndex] = new int[currAutomatonNbrOfStates][];

			// The "worst" case is that all states enables each event
			enableEventsTable[currAutomatonIndex] = new int[alphabetSize][];

			for (int i = 0; i < alphabetSize; i++)
			{
				enableEventsTable[currAutomatonIndex][i] = new int[currAutomatonNbrOfStates + 1];
				enableEventsTable[currAutomatonIndex][i][0] = Integer.MAX_VALUE;
			}

			Alphabet currAlphabet = currAutomaton.getAlphabet();
			Iterator stateIt = currAutomaton.stateIterator();

			while (stateIt.hasNext())
			{
				State currState = (State) stateIt.next();
				int currStateIndex = currState.getIndex();

				nextStateTable[currAutomatonIndex][currStateIndex] = new int[nbrOfEvents];

				// Set a default value of each nextState
				for (int i = 0; i < nbrOfEvents; i++)
				{
					nextStateTable[currAutomatonIndex][currStateIndex][i] = Integer.MAX_VALUE;
				}

				sortedArcs.clear();

				// Insert all indices in a tree (sorted)
				Iterator outgoingArcsIt = currState.outgoingArcsIterator();

				while (outgoingArcsIt.hasNext())
				{
					Arc currArc = (Arc) outgoingArcsIt.next();

					// Get the event from the automaton
					// String eventId = currArc.getEventId();
					LabeledEvent currEvent = currArc.getEvent(); // currAlphabet.getEventWithId(eventId);
					LabeledEvent theEvent = theAlphabet.getEventWithLabel(currEvent.getLabel());
					int currEventIndex = theEvent.getSynchIndex();

					sortedArcs.add(new Integer(currEventIndex));

					// Now insert the nextState index into the table
					State currNextState = currArc.getToState();
					int currNextStateIndex = currNextState.getIndex();

					nextStateTable[currAutomatonIndex][currStateIndex][currEventIndex] = currNextStateIndex;

					// Insert all state that enables the current event into
					// enableEventsTable. This could easily be optimized to avoid the search.
					int i = 0;

					while (enableEventsTable[currAutomatonIndex][currEventIndex][i] != Integer.MAX_VALUE)
					{
						i++;
					}

					enableEventsTable[currAutomatonIndex][currEventIndex][i] = currStateIndex;
					enableEventsTable[currAutomatonIndex][currEventIndex][i + 1] = Integer.MAX_VALUE;
				}

				outgoingEventsTable[currAutomatonIndex][currStateIndex] = new int[sortedArcs.size() + 1];

				// Now copy all indices to an int array
				Iterator sortedArcsIt = sortedArcs.iterator();
				int i = 0;

				while (sortedArcsIt.hasNext())
				{
					int thisIndex = ((Integer) sortedArcsIt.next()).intValue();

					outgoingEventsTable[currAutomatonIndex][currStateIndex][i++] = thisIndex;
				}

				outgoingEventsTable[currAutomatonIndex][currStateIndex][i] = Integer.MAX_VALUE;
			}
		}
	}

	/**
	 * Update these comments.
	 * For each state in the automaton precompute an array
	 * that contains the index of all events that leave the current
	 * state. This array must be sorted, and the last element must be
	 * Integer.MAX_VALUE. Note that this computation can not be
	 * done in the states, since they do not know about the alphabet.
	 *
	 *@param  theAutomata Description of the Parameter
	 *@param  theAutomaton Description of the Parameter
	 *@exception  Exception Description of the Exception
	 */
	void generatePrevStatesTransitionIndices(Automata theAutomata, Automaton theAutomaton)
		throws Exception
	{

		// Compute the prevStateTable and outgoingEventsTable
		Alphabet theAlphabet = theAutomaton.getAlphabet();
		int nbrOfAutomata = theAutomata.size();
		int nbrOfEvents = theAlphabet.size();

		prevStatesTable = new int[nbrOfAutomata][][][];
		incomingEventsTable = new int[nbrOfAutomata][][];

		TreeSet sortedArcs = new TreeSet();
		Iterator autIt = theAutomata.iterator();

		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			int currAutomatonIndex = currAutomaton.getIndex();
			int currAutomatonNbrOfStates = currAutomaton.nbrOfStates();

			prevStatesTable[currAutomatonIndex] = new int[currAutomatonNbrOfStates][][];
			incomingEventsTable[currAutomatonIndex] = new int[currAutomatonNbrOfStates][];

			Alphabet currAlphabet = currAutomaton.getAlphabet();
			Iterator stateIt = currAutomaton.stateIterator();

			while (stateIt.hasNext())
			{
				State currState = (State) stateIt.next();
				int currStateIndex = currState.getIndex();

				prevStatesTable[currAutomatonIndex][currStateIndex] = new int[nbrOfEvents][];

				// Set a default value of each nextState
				for (int i = 0; i < nbrOfEvents; i++)
				{
					prevStatesTable[currAutomatonIndex][currStateIndex][i] = null;
				}

				sortedArcs.clear();

				// Insert all indices in a tree (sorted)
				Iterator incomingArcsIt = currState.incomingArcsIterator();

				while (incomingArcsIt.hasNext())
				{
					Arc currArc = (Arc) incomingArcsIt.next();

					// Get the event from the automaton
					// String eventId = currArc.getEventId();
					LabeledEvent currEvent = currArc.getEvent(); // currAlphabet.getEventWithId(eventId);
					LabeledEvent theEvent = theAlphabet.getEventWithLabel(currEvent.getLabel());
					int currEventIndex = theEvent.getSynchIndex();

					sortedArcs.add(new Integer(currEventIndex));

					// Now insert the prevState index into the table
					State currPrevState = currArc.getFromState();
					int currPrevStateIndex = currPrevState.getIndex();
					int[] currPreviousStates = prevStatesTable[currAutomatonIndex][currStateIndex][currEventIndex];
					int nbrOfIncomingArcs = currState.nbrOfIncomingArcs();

					if (currPreviousStates == null)
					{

						// Allocate memory and initialize, last element contains the number of valid elements
						currPreviousStates = new int[nbrOfIncomingArcs + 1];
						currPreviousStates[nbrOfIncomingArcs] = 0;
					}

					currPreviousStates[currPreviousStates[nbrOfIncomingArcs]++] = currPrevStateIndex;
				}

				incomingEventsTable[currAutomatonIndex][currStateIndex] = new int[sortedArcs.size() + 1];

				// Now copy all indices to an int array
				Iterator sortedArcsIt = sortedArcs.iterator();
				int i = 0;

				while (sortedArcsIt.hasNext())
				{
					int thisIndex = ((Integer) sortedArcsIt.next()).intValue();

					incomingEventsTable[currAutomatonIndex][currStateIndex][i++] = thisIndex;
				}

				incomingEventsTable[currAutomatonIndex][currStateIndex][i] = Integer.MAX_VALUE;
			}
		}
	}

	void generateControllableEventsTable(Automaton theAutomaton)
		throws Exception
	{
		Alphabet theAlphabet = theAutomaton.getAlphabet();

		controllableEventsTable = new boolean[theAlphabet.size()];
		immediateEventsTable = new boolean[theAlphabet.size()];

		for (int i = 0; i < theAlphabet.size(); i++)
		{
			LabeledEvent currEvent = theAlphabet.getEventWithIndex(i);

			controllableEventsTable[i] = currEvent.isControllable();
			immediateEventsTable[i] = currEvent.isImmediate();
		}
	}

	public boolean[][] getAlphabetEventsTable()
	{
		return alphabetEventsTable;
	}

	public boolean[][] getPrioritizedEventsTable()
	{
		return prioritizedEventsTable;
	}

	public int[][][] getOutgoingEventsTable()
	{
		return outgoingEventsTable;
	}

	public int[][][] getIncomingEventsTable()
	{
		return incomingEventsTable;
	}

	public int[][][] getNextStateTable()
	{
		return nextStateTable;
	}

	public int[][][][] getPrevStatesTable()
	{
		return prevStatesTable;
	}

	public State[][] getStateTable()
	{
		return stateTable;
	}

	public int[][] getStateStatusTable()
	{
		return stateStatusTable;
	}

	public int[] getEventPriority()
	{
		return eventPriority;
	}

	public boolean[] getTypeIsPlantTable()
	{
		return typeIsPlantTable;
	}

	public boolean[] getControllableEventsTable()
	{
		return controllableEventsTable;
	}

	public boolean[] getImmediateEventsTable()
	{
		return immediateEventsTable;
	}

	public int[] getAutomataSize()
	{
		return automataSize;
	}

	public int[][][] getEnableEventsTable()
	{
		return enableEventsTable;
	}

	public Automaton getAutomaton(int index)
	{
		return theAutomata.getAutomatonAt(index);
	}

	public State getState(int automatonIndex, int stateIndex)
	{
		return stateTable[automatonIndex][stateIndex];
	}

	private int[] generateCopy1DIntArray(int[] oldArray)
	{
		int[] newArray = new int[oldArray.length];

		System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);

		return newArray;
	}

	private boolean[] generateCopy1DBooleanArray(boolean[] oldArray)
	{
		boolean[] newArray = new boolean[oldArray.length];

		System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);

		return newArray;
	}

	private boolean[][] generateCopy2DBooleanArray(boolean[][] oldArray)
	{
		boolean[][] newArray = new boolean[oldArray.length][];

		for (int i = 0; i < oldArray.length; i++)
		{
			newArray[i] = new boolean[oldArray[i].length];

			System.arraycopy(oldArray[i], 0, newArray[i], 0, oldArray[i].length);
		}

		return newArray;
	}

	private int[][][] generateCopy3DIntArray(int[][][] oldArray)
	{
		int[][][] newArray = new int[oldArray.length][][];

		for (int i = 0; i < oldArray.length; i++)
		{
			newArray[i] = new int[oldArray[i].length][];

			for (int j = 0; j < oldArray[i].length; j++)
			{
				newArray[i][j] = new int[oldArray[i][j].length];

				System.arraycopy(oldArray[i][j], 0, newArray[i][j], 0, oldArray[i][j].length);
			}
		}

		return newArray;
	}

	private int[][][][] generateCopy4DIntArray(int[][][][] oldArray)
	{
		int[][][][] newArray = new int[oldArray.length][][][];

		for (int i = 0; i < oldArray.length; i++)
		{
			newArray[i] = new int[oldArray[i].length][][];

			for (int j = 0; j < oldArray[i].length; j++)
			{
				newArray[i][j] = new int[oldArray[i][j].length][];

				for (int k = 0; k < oldArray[i][j].length; k++)
				{
					if (oldArray[i][j][k] != null)
					{

						// This can be null, see prevStateTable
						newArray[i][j][k] = new int[oldArray[i][j][k].length];

						System.arraycopy(oldArray[i][j][k], 0, newArray[i][j][k], 0, oldArray[i][j][k].length);
					}
					else
					{
						newArray[i][j][k] = null;
					}
				}
			}
		}

		return newArray;
	}

	private int[][] generateCopy2DIntArray(int[][] oldArray)
	{
		int[][] newArray = new int[oldArray.length][];

		for (int i = 0; i < oldArray.length; i++)
		{
			newArray[i] = new int[oldArray[i].length];

			System.arraycopy(oldArray[i], 0, newArray[i], 0, oldArray[i].length);
		}

		return newArray;
	}

	private State[][] generateCopy2DStateArray(State[][] oldArray)
	{
		State[][] newArray = new State[oldArray.length][];

		for (int i = 0; i < oldArray.length; i++)
		{
			newArray[i] = new State[oldArray[i].length];

			System.arraycopy(oldArray[i], 0, newArray[i], 0, oldArray[i].length);
		}

		return newArray;
	}
}
