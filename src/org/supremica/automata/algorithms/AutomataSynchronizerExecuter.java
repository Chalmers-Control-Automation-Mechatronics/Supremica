
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

import org.supremica.log.*;
import org.supremica.gui.*;
import java.util.Arrays;

// For the automata selection methods
import java.util.ArrayList;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.AutomataIndexForm;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;
import EDU.oswego.cs.dl.util.concurrent.Rendezvous;

/**
 * Performs all kinds of synchronization tasks, for synchronization, verification and synthesis.
 *
 *@author  ka
 *@created  November 28, 2001
 *@see  AutomataSynchronizer
 *@see  AutomataFastControllabilityCheck
 *@see  AutomataSynthesizer
 */
public final class AutomataSynchronizerExecuter
	extends Thread
{
	private static Logger logger = LoggerFactory.createLogger(AutomataSynchronizerExecuter.class);

	private final AutomataSynchronizerHelper helper;
	private final AutomataIndexForm indexForm;
	private final int nbrOfAutomata;
	private final int nbrOfEvents;
	private final int[][][] nextStateTable;
	private final int[][][] outgoingEventsTable;
	private final boolean[][] prioritizedEventsTable;
	private final boolean[][] alphabetEventsTable;
	private final boolean[] typeIsPlantTable;
	private final boolean[] controllableEventsTable;
	private final boolean[] immediateEventsTable;
	private int[][] currOutgoingEvents;
	private int[] currOutgoingEventsIndex;
	private int[] automataIndices;
	private int[] currPlantAutomata;
	private int[] nextState;
	private int[] currEnabledEvents;
	private boolean controllableState;
	private final static int IMMEDIATE_NOT_AVAILABLE = -1;
	private int immediateEvent = IMMEDIATE_NOT_AVAILABLE;

	private int numberOfAddedStates = 0;

	/** Options determining how the synchronization should be performed. */
	private final SynchronizationOptions syncOptions;

	/**
	 * Determines if uncontrollable states should be marked as forbidden.
	 *
	 *@see  SynchronizationOptions
	 */
	private boolean forbidUncontrollableStates;

	/**
	 * Determines if uncontrollable states should be expanded in the synchronization.
	 *
	 *@see  SynchronizationOptions
	 */
	private boolean expandForbiddenStates;

	/**
	 * Determines if more detailed information on the progress of things should be displayed.
	 *
	 *@see  SynchronizationOptions
	 */
	private boolean verboseMode;

	/**
	 * Determines synchronization type.
	 *
	 *@see  SynchronizationType
	 *@see  SynchronizationOptions
	 */
	private final SynchronizationType syncType;

	/**
	 * Determines if uncontrollable states should be stored in potentiallyUncontrollableStates
	 * for later analysis.
	 */
	private boolean rememberUncontrollable = false;

	private int problemPlant = Integer.MAX_VALUE;
	private int problemEvent = Integer.MAX_VALUE;
	private StateMemorizer potentiallyUncontrollableStates;
	private int[] eventPriority;
	private boolean expandEventsUsingPriority;
	private boolean exhaustiveSearch = false;
	private boolean coExecute = false;
	private AutomataOnlineSynchronizer coExecuter = null;

	/** For "one event at a time"-execution. */
	private int currUncontrollableEvent = -1;

	/** For stopping of thread. */
	private boolean stopRequested = false;


	// Synchonization of all executors
	private Rendezvous executerRendezvous = null;

	/**
	 *@param  synchronizerHelper helper for multithread execution.
	 */
	public AutomataSynchronizerExecuter(AutomataSynchronizerHelper synchronizerHelper)
	{
		setPriority(Thread.MIN_PRIORITY);

		helper = synchronizerHelper;
		indexForm = helper.getAutomataIndexForm();
		nbrOfAutomata = helper.getAutomata().size();
		nbrOfEvents = helper.getAutomaton().getAlphabet().size();
		nextStateTable = indexForm.getNextStateTable();
		outgoingEventsTable = indexForm.getOutgoingEventsTable();
		prioritizedEventsTable = indexForm.getPrioritizedEventsTable();
		alphabetEventsTable = indexForm.getAlphabetEventsTable();
		typeIsPlantTable = indexForm.getTypeIsPlantTable();
		controllableEventsTable = indexForm.getControllableEventsTable();
		immediateEventsTable = indexForm.getImmediateEventsTable();
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
		verboseMode = syncOptions.verboseMode();

		if (rememberUncontrollable)
		{
			expandForbiddenStates = false;
		}

		executerRendezvous = synchronizerHelper.getExecuterRendezvous();

	}

	/**
	 * Selects the automata in the ArrayList for synchronization.
	 *
	 *@param  automataToBeSelected ArrayList of the automata to be selected
	 *@exception  Exception Throws exception if exhaustiva search is used.
	 */
	public void selectAutomata(ArrayList automataToBeSelected)
		throws Exception
	{
		automataIndices = new int[automataToBeSelected.size()];

		for (int i = 0; i < automataToBeSelected.size(); i++)
		{
			automataIndices[i] = ((Automaton) automataToBeSelected.get(i)).getIndex();
		}

		helper.selectAutomata(automataIndices);

		// FIXA!
		if (exhaustiveSearch)
		{
			throw new Exception("Exhaustive search used in the wrong way!");
		}
	}

	/**
	 * Selects the automata with the indices in automataIndices for synchronization
	 *
	 *@param automataIndices Array of int with the indices of the automata to be selected.
	 *@exception  Exception Throws exception if exhaustiva search is used.
	 */
	public void selectAutomata(int[] automataIndices)
		throws Exception
	{
		this.automataIndices = automataIndices;

		if (exhaustiveSearch)
		{
			throw new Exception("Exhaustive search used in the wrong way!");
		}
	}

	/** Selects all automata for synchronization. */
	public void selectAllAutomata()
	{
		automataIndices = new int[nbrOfAutomata];

		for (int i = 0; i < nbrOfAutomata; i++)
		{
			automataIndices[i] = i;
		}
	}

	/** Initializes variables vital for the synchronization. */
	public final void initialize()
	{
		currOutgoingEvents = new int[nbrOfAutomata][];
		currOutgoingEventsIndex = new int[nbrOfAutomata];

		if (automataIndices == null)
		{
			selectAllAutomata();
		}

		nextState = AutomataIndexFormHelper.createState(nbrOfAutomata);

		// +1 status field
		currEnabledEvents = new int[nbrOfEvents + 1];

		// Always end with Integer.MAX_VALUE
	}

	/**
	 * Calculates what events are enabled from the state <tt>currState</tt>, if the state turns out
	 * uncontrollable the boolean controllableState is set false.
	 *
	 *@param  currState the (full) state to be examined.
	 */
	private final void enabledEvents(int[] currState)
	{
		int currMinEventIndex = Integer.MAX_VALUE;
		int nbrOfSelectedAutomata = automataIndices.length;

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
		immediateEvent = IMMEDIATE_NOT_AVAILABLE;

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
					// If the event is prioritized in this automaton (and hence is in the alphabet)
					if (prioritizedEventsTable[currAutIndex][currEventIndex])
					{
						// but it is not the event we're looking for, then...
						if (currEventIndex != currAutEventIndex)
						{
							//... this event should not be executed
							thisEventOk = false;
							
							// For controllability we need to know whether the event was disabled by a plant or a spec
							if (typeIsPlantTable[currAutIndex])
							{
								thisPlantEventOk = false;
							}
						}
					}
				}
				else if (syncType == SynchronizationType.Full)
				{
					// If the event is in the alphabet (and hence is considered prioritized)
					if (alphabetEventsTable[currAutIndex][currEventIndex])
					{
						// but it is not the event we're looking for, then...
						if (currEventIndex != currAutEventIndex)
						{
							//... this event should not be executed
							thisEventOk = false;

							// For controllability we need to know whether the event was disabled by a plant or a spec
							if (typeIsPlantTable[currAutIndex])
							{
								thisPlantEventOk = false;
							}
						}
					}
				}
				else if (syncType == SynchronizationType.Broadcast)
				{
					if (typeIsPlantTable[currAutIndex])
					{
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
				if (immediateEventsTable[currEventIndex])
				{

					// Clear out everything else and abort the search for enabled events
					// If several events that are immediate are found
					// Then the one with smallest index are chosen.
					immediateEvent = currEventIndex;
					nbrOfEnabledEvents = 0;
					currEnabledEvents[nbrOfEnabledEvents++] = immediateEvent;
					currMinEventIndex = Integer.MAX_VALUE;
				}
				else
				{
					currEnabledEvents[nbrOfEnabledEvents++] = currEventIndex;
				}
			}

			if (!thisEventOk && canExecuteInPlant && thisPlantEventOk && !controllableEventsTable[currEventIndex])
			{

				// Uncontrollable state found
				controllableState = false;

				helper.setAutomataIsControllable(false);

				if (exhaustiveSearch)
				{
					// Stop when uncontrollable state found
					if (verboseMode)
					{
						logger.info("Uncontrollable state found.");
					}

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

			// ... or if no events are prioritized, take the first two...
			if (insertionIndex == 0)
			{
				insertionIndex = 2;
			}

			currEnabledEvents[insertionIndex] = Integer.MAX_VALUE;
		}

		if (coExecute)
		{

			// In co-execution mode, an enabledEvents-method in another executer
			// follows the automaton we're suspecting has uncontrollable states.
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

				// Found no corresponding transitions in the suspect automaton...
				// /*
				if (verboseMode)
				{
					logger.debug("The suspect automaton has no corresponding transitions, wandering aimlessly...");
				}

				// */
				// Here, the insertionIndex sets the maximium amount of states that are examined...
				// insertionIndex = 2;
				insertionIndex = currEnabledEvents.length - 1;
			}
			else
			{

				// There are transitions in the suspect automaton...
				// /*
				if (verboseMode)
				{
					logger.debug("Following transitions in the suspect automaton, there are " + insertionIndex + " such transitions...");
				}

				// */
			}

			currEnabledEvents[insertionIndex] = Integer.MAX_VALUE;
		}
	}

	/** Performs the synchronization. */
	public void run()
	{
		initialize();

		// Get the first state to process
		int[] currState = helper.getStateToProcess();

		// main loop
		while ((currState != null) && !stopRequested)
		{
			if (coExecute)
			{
				coExecuter.setCurrState(currState);
			}

			enabledEvents(currState);

			if (syncOptions.buildAutomaton() && currEnabledEvents[0] == Integer.MAX_VALUE)
			{
				helper.setDeadlocked(currState, true);
			}

			if (!controllableState)
			{
				// We'd like to remember this state and later on try to show that
				// it will be excluded in the total synchronization...  or not.
				if (rememberUncontrollable)
				{
					if ((currUncontrollableEvent == -1) || (currUncontrollableEvent == problemEvent))
					{
						potentiallyUncontrollableStates.add(automataIndices, currState, problemPlant, problemEvent);
					}
				}

				// We now know that there is an uncontrollable state
				if (exhaustiveSearch)
				{
					return;
				}

				// Forbid uncontrollable state?
				if (forbidUncontrollableStates)
				{
					helper.setForbidden(currState, true);
				}
			}

			if (controllableState || expandForbiddenStates)
			{
				// Expand state
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
					// Update this to check if the state is not forbidden instead of
					// only controllable.
					try
					{
						helper.addState(currState, nextState, currEventIndex);
						//if(numberOfAddedStates++ % 10000 == 0)
						//{
							// KA: What the heck is this?? Extremely ugly.
							//for (int j = 0; j < 10; j++)
							//{
							//	yield();
							//}
						//}

					}
					catch (Exception e)
					{
						// System.err.println(e);
						logger.error("Error in SynchronizerExecuter");
						logger.debug(e.getStackTrace());
						return;
					}

					currEventIndex = currEnabledEvents[++i];
				}
			}

			currState = helper.getStateToProcess();

			if (currState == null)
			{
				// This thread tells the other threads that it failed to get
				// a new state, thus when all threads has enter this state
				// all threads can stop executing. There is one exception
				// and this is the possibility for this thread to experience
				// a temporary shortage of states and at this position
				// other threads might notice this thread that new
				// states to process now exist.
				boolean finished = false;
				while (!finished && currState == null && !stopRequested)
				{
					try
					{
						if ((Boolean)executerRendezvous.rendezvous(Boolean.TRUE) == Boolean.TRUE)
						{
							finished = true;
						}
						else
						{
							currState = helper.getStateToProcess();
						}
					}
					catch (InterruptedException ex)
					{
						logger.error("InterruptedException in AutomataSynchronizerExecuter");
						finished = true;
					}
				}
			}
		}
	}

	/**
	 * A call to this method stops the execution of the run-method or the buildAutomaton-method as soon as possible.
	 *
	 *@see  AutomataSynchronizer
	 *@see  AutomataVerifier
	 */
	public void requestStop()
	{

		// System.out.println("Executer requested to stop.");
		stopRequested = true;
	}

	/**
	 * Builds automaton using concatenated state names as new state names.
	 *
	 *@return  true if build successful, false if build is stopped with requestStop().
	 *@exception  Exception Description of the Exception
	 */
	public boolean buildAutomaton()
		throws Exception
	{
		return buildAutomaton(true);
	}

	/**
	 * Builds automaton using either concatenated state names or new, short, unique names as new state names.
	 *
	 *@param  longformId true for concateated state names, false for new names.
	 *@return  true if build successful, false if build is stopped with requestStop().
	 *@exception  Exception Description of the Exception
	 */
	public boolean buildAutomaton(boolean longformId)
		throws Exception
	{
		try
		{
			Automaton theAutomaton = helper.getAutomaton();

			// theAutomaton.setName("regaut");

			Alphabet theAlphabet = theAutomaton.getAlphabet();
			int[][] currStateTable = helper.getStateTable();
			int stateNumber = 0;
			ExecutionDialog executionDialog = helper.getExecutionDialog();

			if (executionDialog != null)
			{
				executionDialog.initProgressBar(0, currStateTable.length);
				executionDialog.setMode(ExecutionDialogMode.buildingStates);
			}

			// Create all states
			for (int i = 0; i < currStateTable.length; i++)
			{
				if (i % 100 == 0)
				{
					if (executionDialog != null)
					{
						executionDialog.setProgress(i);
					}
				}

				if (stopRequested)
				{
					return false;
				}

				if (currStateTable[i] != null)
				{
					int[] currState = currStateTable[i];
					State newState = null;

					//newState.setAutomataSynchronizerExecutorIndex(i);

					if (longformId)
					{
						State[][] stateTable = indexForm.getStateTable();
						StringBuffer sb = new StringBuffer();

						for (int j = 0; j < currState.length - AutomataIndexFormHelper.STATE_EXTRA_DATA; j++)
						{
							sb.append(stateTable[j][currState[j]].getId());
						}

						newState = new State(sb.toString());
					}
					else
					{
						newState = new State("q" + stateNumber++);
					}

					newState.setAutomataSynchronizerExecutorIndex(i);

					newState.setName(newState.getId());
					newState.setInitial(AutomataIndexFormHelper.isInitial(currState));
					newState.setAccepting(AutomataIndexFormHelper.isAccepting(currState));
					newState.setForbidden(AutomataIndexFormHelper.isForbidden(currState));
					newState.setFirst(AutomataIndexFormHelper.isFirst(currState));
					newState.setLast(AutomataIndexFormHelper.isLast(currState));
					theAutomaton.addState(newState);
				}
			}

			if (executionDialog != null)
			{
				executionDialog.initProgressBar(0, currStateTable.length);
				executionDialog.setMode(ExecutionDialogMode.buildingTransitions);
			}

			// Create all transitions
			for (int k = 0; k < currStateTable.length; k++)
			{
				if (k % 100 == 0)
				{
					if (executionDialog != null)
					{
						executionDialog.setProgress(k);
					}
				}

				if (stopRequested)
				{
					theAlphabet = null;
					theAutomaton = null;

					// theAutomaton.setDisabled(true);
					// System.out.println(theAutomaton == null);
					// System.out.println(helper.getAutomaton() == null);
					return false;
				}

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
								// Arc newArc = new Arc(thisState, nextState, theEvent.getId());
								Arc newArc = new Arc(thisState, nextState, theEvent);

								theAutomaton.addArc(newArc);
							}
						}
						catch (Exception e)
						{
							// System.err.println(e);
							// System.exit(0);
							logger.error("Exception when checking next state. " + e);
							logger.debug(e.getStackTrace());
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

			return true;
		}
		catch (OutOfMemoryError ex)
		{
			throw new Exception("Out of memory. Try to increase the JVM heap.");	// why not throw new Exception(ex)?
		}
	}

	private String printTypeIsPlantTable()
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

	/**
	 * Check if the event is possible between fromState and toState.
	 * For perfomance reasons we assume that event is possible in
	 * at least one of the original automata.
	 *
	 *@param  fromState Description of the Parameter
	 *@param  toState Description of the Parameter
	 *@return  Description of the Return Value
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

	/**
	 * Method for finding transitions between states. Used by displayTrace() in AutomataSynchronizerHelper.
	 *
	 *@param  fromState state to find transition from.
	 *@param  toState state to find transition to.
	 *@return  index of one (of perhaps many) transitions between fromState and toState or -1 if none exists.
	 *@see  AutomataSynchronizerHelper#displayTrace()
	 */
	public int findTransition(int[] fromState, int[] toState)
	{
		enabledEvents(fromState);

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

	/**
	 * Compares two arrays, except for the last element (the status field)
	 *
	 *@param  firstArray Description of the Parameter
	 *@param  secondArray Description of the Parameter
	 *@return  true if equal, false otherwise.
	 */
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

	/**
	 * Select uncontrollable event (for "one event at a time"-execution).
	 *
	 *@param  event the current uncontrollable event the synchronization should focus on.
	 */
	public void setCurrUncontrollableEvent(LabeledEvent event)
	{
		currUncontrollableEvent = event.getSynchIndex();
	}
}
