
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

import org.supremica.util.SupremicaException;
import org.supremica.util.*;
import org.supremica.gui.*;
import org.supremica.log.*;
import java.util.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataIndexForm;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;
import EDU.oswego.cs.dl.util.concurrent.Rendezvous;

/**
 * Contains information that is common to all synchronization threads.
 *
 *@author  ka
 *@created  November 28, 2001
 */
public final class AutomataSynchronizerHelper
{
	private static Logger logger = LoggerFactory.createLogger(AutomataSynchronizerHelper.class);
	private AutomataIndexForm theAutomataIndexForm;
	private IntArrayHashTable theStates;
	private IntArrayList statesToProcess;
	private int nbrOfStatesToProcess = 0;
	
//      private int totalNbrOfStates = 0;
//      private int nbrOfUncontrollableStates = 0;
//      private int nbrOfDeadlockedStates = 0;
	// Two locks are used to limit the access the statesToProcess
	private final Object gettingFromStatesToProcessLock = new Object();
	private final Object addingToStatesToProcessLock = gettingFromStatesToProcessLock;
	private final Object addStateLock = new Object();
	private Automata theAutomata;
	private Automaton theAutomaton;    // the result
	private boolean automataIsControllable = true;

	// Keeps information common to helpers.
	private HelperData helperData;
	private SynchronizationOptions syncOptions = null;

	// Used by AutomataSynchronizerExecuter
	private StateMemorizer stateMemorizer = new StateMemorizer();
	private boolean rememberUncontrollable = false;
	private boolean expandEventsUsingPriority = false;
	private IntArrayList fromStateList = new IntArrayList();
	private IntArrayList stateTrace = new IntArrayList();
	private boolean rememberTrace = false;
	private boolean coExecute = false;

	//private AutomataOnlineSynchronizer coExecuter = null;
	private AutomataSynchronizerExecuter coExecuter = null;
	private Rendezvous executerRendezvous = null;

	/* Used by AutomataControllabillityCheck.
	 * Causes the synchronization to stop as soon as an uncontrollable
	 * state is found.
	 */
	private boolean exhaustiveSearch = false;

	// For synchronizing without recalculating the AutomataIndexForm
	private boolean[] activeAutomata;

	// For counting states in executionDialog
	private ExecutionDialog executionDialog = null;

	// Stop execution after amount of state
	private int stopExecutionLimit = -1;

	public AutomataSynchronizerHelper(Automata theAutomata, SynchronizationOptions syncOptions)
		throws Exception
	{
		if (theAutomata == null)
		{
			throw new SupremicaException("theAutomata must be non-null");
		}

		if (syncOptions == null)
		{
			throw new SupremicaException("syncOptions must be non-null");
		}

		this.theAutomata = theAutomata;
		this.syncOptions = syncOptions;
		helperData = new HelperData();
		statesToProcess = new IntArrayList();
		nbrOfStatesToProcess = 0;
		theStates = new IntArrayHashTable(syncOptions.getInitialHashtableSize(),
										  syncOptions.expandHashtable());
		theAutomaton = new Automaton();
		executerRendezvous = new Rendezvous(syncOptions.getNbrOfExecuters(), new ExecuterRendezvous());

		// Calculate the automataIndexForm (a more efficient representation of an automata)
		try
		{
			theAutomataIndexForm = new AutomataIndexForm(theAutomata, theAutomaton);
		}
		catch (Exception e)
		{
			logger.error("Error while computing AutomataIndexForm");
			logger.debug(e.getStackTrace());

			throw e;
		}
	}

	/**
	 * Constructs new helper but keeps the same AutomataIndexForm-, Automata-, HelperData and
	 * Automaton-Objects.
	 *
	 *@param  orgHelper The old helper to collect information from
	 *@see  AutomataVerificationOptions#findUncontrollableStates(int[])
	 */
	public AutomataSynchronizerHelper(AutomataSynchronizerHelper orgHelper)
		throws Exception
	{
		theAutomata = orgHelper.getAutomata();
		theAutomaton = orgHelper.getAutomaton();
		theAutomataIndexForm = orgHelper.getAutomataIndexForm();
		syncOptions = orgHelper.getSynchronizationOptions();
		helperData = orgHelper.getHelperData();
		executionDialog = orgHelper.getExecutionDialog();
		statesToProcess = new IntArrayList();
		nbrOfStatesToProcess = 0;
		theStates = new IntArrayHashTable(syncOptions.getInitialHashtableSize(), syncOptions.expandHashtable());
		executerRendezvous = orgHelper.getExecuterRendezvous();
	}

	public void clear()
	{
		theStates.clear();

		automataIsControllable = true;
		coExecute = false;
		coExecuter = null;
		rememberTrace = false;
		exhaustiveSearch = false;
		rememberUncontrollable = false;
		expandEventsUsingPriority = false;

		// Should be an external option?!? FIXA!
		// Is there anything else that needs to be cleared?...
	}

	/**
	 * Initializes the helper for a new run. Generates a new initial state and adds it to the queue. 
	 */
	public void initialize()
		throws Exception
	{
		// The helper (or rather theStates) should be clear before executing this method
		if (theStates.size() > 0)
		{
			throw new Exception("AutomataSynchronizerHelper not cleared properly before reinitialization.");
		}

		// Build the initial state  (including 2 status fields)
		int[] initialState = AutomataIndexFormHelper.createState(theAutomata.size());

		Iterator autIt = theAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			State currInitialState = currAutomaton.getInitialState();
			initialState[currAutomaton.getIndex()] = currInitialState.getIndex();
		}

		// Add state to stack
		addState(initialState);
	}

	public SynchronizationOptions getSynchronizationOptions()
	{
		return syncOptions;
	}

	public Automaton getAutomaton()
	{
		return theAutomaton;
	}

	public Rendezvous getExecuterRendezvous()
	{
		return executerRendezvous;
	}

	public Alphabet getUnionAlphabet()
	{
		return theAutomaton.getAlphabet();
	}

	public int getNbrOfEvents()
	{
		return getUnionAlphabet().size();
	}

	public Automata getAutomata()
	{
		return theAutomata;
	}

	public AutomataIndexForm getAutomataIndexForm()
	{
		return theAutomataIndexForm;
	}

	public HelperData getHelperData()
	{
		return helperData;
	}

	public int getNbrOfStatesToProcess()
	{
		return nbrOfStatesToProcess;
	}

	/**
	 *@return  a state if there are more states to process, null otherwise.
	 */
	public int[] getStateToProcess()
	{
		synchronized (gettingFromStatesToProcessLock)
		{
			if ((nbrOfStatesToProcess == 0) || (stopExecutionLimit == 0))
			{
				return null;
			}

			if (stopExecutionLimit > 0)
			{
				stopExecutionLimit--;
			}

			nbrOfStatesToProcess--;

			if (rememberTrace)
			{
				if (fromStateList.size() > 0)
				{
					while (!(Arrays.equals(fromStateList.getLast(), stateTrace.getLast())))
					{
						stateTrace.removeLast();
					}

					if (stateTrace.size() == 0)
					{
						logger.error("Error when recording trace.");
					}

					fromStateList.removeLast();
					stateTrace.addLast(statesToProcess.getLast());
				}

				if (stateTrace.size() == 0)
				{
					stateTrace.addLast(statesToProcess.getLast());
				}

				// Depth first search
				return statesToProcess.removeLast();
			}
			else
			{
				if (coExecute)
				{

					// Depth first search
					return statesToProcess.removeLast();
				}
				else
				{

					// Width first search
					return statesToProcess.removeFirst();
				}
			}
		}
	}

	public void addComment(String comment)
	{
		theAutomaton.setComment(comment);
	}

	public void setExecutionDialog(ExecutionDialog executionDialog)
	{
		this.executionDialog = executionDialog;
	}

	public ExecutionDialog getExecutionDialog()
	{
		return executionDialog;
	}

	/**
	 * If the toState does not exist then make a copy of this state
	 * and add it to the set of states and to the set of states waiting for processing.
	 * If it exists then find it.
	 * Insert the arc.
	 *
	 *@param fromState The feature to be added to the State attribute
	 *@param toState The feature to be added to the State attribute
	 *@param eventIndex The feature to be added to the State attribute
	 *@exception  Exception Description of the Exception
	 */
	public void addState(int[] fromState, int[] toState)
		throws Exception
	{
		if (rememberTrace)
		{
			fromStateList.addLast(fromState);
		}

		if (true)    // What? /Hguo.
		{
			int prevStateIndex = theStates.getIndex(fromState);

			if (prevStateIndex >= 0)
			{
				AutomataIndexFormHelper.setPrevStateIndex(toState, prevStateIndex);
			}
			else
			{
				AutomataIndexFormHelper.setPrevStateIndex(toState, AutomataIndexFormHelper.STATE_NO_PREVSTATE);
			}
		}

		addState(toState);
	}

	// Add this state to theStates
	public void addState(int[] state)
		throws Exception
	{
		int[] newState = null;

		synchronized (addStateLock)
		{
			newState = theStates.add(state);
		}

		if (newState != null)
		{
			if (rememberTrace && (stateTrace.size() == 0))
			{

				// Add initial state
				stateTrace.add(newState);
			}

			addStatus(newState);
			addStateToProcess(newState);

			// helperData.incrNbrOfAddedStates();
			helperData.nbrOfAddedStates++;
		}
		else if (rememberTrace && (fromStateList.size() != 0))
		{
			fromStateList.removeLast();
		}

		helperData.nbrOfCheckedStates++;

		if ((executionDialog != null) && (helperData.nbrOfCheckedStates % 2000 == 0))
		{
			executionDialog.setValue((int) helperData.nbrOfAddedStates);
		}
	}

	/**
	 * Add a state to the queue of states waiting for being processed.
	 * This is only called by the addInitialState and addState methods.
	 *
	 *@param  state The feature to be added to the StateToProcess attribute
	 */
	public void addStateToProcess(int[] state)
	{
		synchronized (addingToStatesToProcessLock)
		{
			statesToProcess.addLast(state);

			nbrOfStatesToProcess++;
		}
	}

	public void addStatus(int[] state)
	{
		int[][] stateStatusTable = theAutomataIndexForm.getStateStatusTable();
		int tmpStatus = stateStatusTable[0][state[0]];
		boolean forbidden = AutomataIndexFormHelper.isForbidden(tmpStatus);
		int currStatus;

		for (int i = 1;
				i < state.length - AutomataIndexFormHelper.STATE_EXTRA_DATA;
				i++)
		{
			if ((activeAutomata == null) || (activeAutomata[i] == true))
			{
				currStatus = stateStatusTable[i][state[i]];
				tmpStatus &= currStatus;

				// works for everything except forbidden
				forbidden |= AutomataIndexFormHelper.isForbidden(currStatus);
			}
		}

		if (forbidden)
		{
			tmpStatus |= (1 << 2);
		}

		state[state.length - AutomataIndexFormHelper.STATE_STATUS_FROM_END] = tmpStatus;
	}

	public void setForbidden(int[] state, boolean forbidden)
	{
		int currStatus = state[state.length - AutomataIndexFormHelper.STATE_STATUS_FROM_END];

		if (forbidden)
		{
			currStatus |= (1 << 2);
		}
		else
		{
			currStatus &= ~(1 << 2);
		}

		state[state.length - AutomataIndexFormHelper.STATE_STATUS_FROM_END] = currStatus;

		helperData.nbrOfForbiddenStates++;
	}

	public void setDeadlocked(int[] state, boolean deadlocked)
	{

		/*
		if (logger.isDebugEnabled())
		{
				logger.debug("Deadlocked state:\n" +
										 AutomataIndexFormHelper.dumpVerboseState(state, theAutomataIndexForm));
				logger.debug(displayTrace(state));
		}
		*/
		int currStatus = state[state.length - AutomataIndexFormHelper.STATE_STATUS_FROM_END];

		if (deadlocked)
		{
			currStatus |= (1 << 6);

			helperData.nbrOfDeadlockedStates++;
		}
		else
		{
			currStatus &= ~(1 << 6);
		}

		state[state.length - AutomataIndexFormHelper.STATE_STATUS_FROM_END] = currStatus;
	}

	public int[][] getStateTable()
	{
		return theStates.getTable();
	}

	public int getStateTableSize()
	{
		return theStates.size();
	}

	public Iterator getStateIterator()
	{
		return theStates.iterator();
	}

	public long getNumberOfAddedStates()
	{
		return helperData.nbrOfAddedStates;
	}

	public State[][] getIndexFormStateTable()
	{
		return theAutomataIndexForm.getStateTable();
	}

	public int getStateIndex(int[] state)
	{
		return theStates.getIndex(state);
	}

	public String toString()
	{
		return theStates.toString();
	}

	/**
	 * Used for getting the synchronization result to the worker-class.
	 *
	 *@param  isControllable The new automataIsControllable value
	 *@see  AutomataSynchronizerExecuter
	 */
	public void setAutomataIsControllable(boolean isControllable)
	{
		automataIsControllable = isControllable;
	}

	// automataIsControllable is set to false by AutomataSynchronizerhelper, AutomataSynchronizerExecuter
	// when an uncontrollable state is found.
	public boolean getAutomataIsControllable()
	{
		return automataIsControllable;
	}

	public StateMemorizer getStateMemorizer()
	{
		return stateMemorizer;
	}

	public boolean isGoalState(int[] state)
	{
		return stateMemorizer.contains(state);
	}

	public void setRememberUncontrollable(boolean remember)
	{
		rememberUncontrollable = remember;
	}

	public boolean getRememberUncontrollable()
	{
		return rememberUncontrollable;
	}

	public void setExhaustiveSearch(boolean exhaustive)
	{
		exhaustiveSearch = exhaustive;
	}

	public boolean getExhaustiveSearch()
	{
		return exhaustiveSearch;
	}

	public void setExpandEventsUsingPriority(boolean use)
	{
		expandEventsUsingPriority = use;
	}

	public boolean getExpandEventsUsingPriority()
	{
		return expandEventsUsingPriority;
	}

	// Returns array with priorities, 0 is the highest priority, larger numbers - lower priority
	public int[] getEventPriority()
	{
		Alphabet unionAlphabet = theAutomaton.getAlphabet();
		int[] eventPriority = new int[unionAlphabet.size()];
		int index = 0;

		for (Iterator eventIterator = unionAlphabet.iterator();
				eventIterator.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) eventIterator.next();

			if (currEvent.getExpansionPriority() < 0)
			{

				// The events are already ordered after synchIndex!
				// eventPriority[currEvent.getSynchIndex()] = 10;
				eventPriority[index++] = 10;
			}
			else
			{

				// The events are already ordered after synchIndex!
				// eventPriority[currEvent.getSynchIndex()] = currEvent.getExpansionPriority();
				eventPriority[index++] = currEvent.getExpansionPriority();
			}
		}

		return eventPriority;
	}

	public void setRememberTrace(boolean rememberTrace)
		throws Exception
	{
		if (theStates.size() > 0)
		{
			throw new Exception("Error in AutomataSynchronizerHelper. Helper must be cleared before calling setRememberTrace().");
		}

		this.rememberTrace = rememberTrace;
	}

	/** 
	 * Logs the amount of states examined during the execution and some other stuff. 
	 */
	public void displayInfo()
	{
		// Did we do anything?
		if (helperData.getNumberOfCheckedStates() != 0)
		{
			logger.info("Operation statistics:\n\t" + (helperData.getNumberOfCheckedStates() - 1) + " transitions were examined.\n\t" + helperData.getNumberOfReachableStates() + " reachable states were found.\n\t" + helperData.getNumberOfForbiddenStates() + " forbidden states were found.\n\t" + helperData.getNumberOfDeadlockedStates() + " deadlocked states were found.");
		}
	}

	/**
	 * Displays the event-trace leading to the uncontrollable state.
	 */
	public void displayTrace()
		throws Exception
	{
		Alphabet unionAlphabet = theAutomaton.getAlphabet();

		// We have to have an executer for finding the transitions
		clear();

		//AutomataOnlineSynchronizer executer = new AutomataOnlineSynchronizer(this);
		AutomataSynchronizerExecuter executer = new AutomataSynchronizerExecuter(this);

		executer.initialize();

		// This version does not remove shortcuts, add this later. FIXA!
		StringBuffer trace = new StringBuffer();
		int[] prevState = null;

		for (Iterator traceIt = stateTrace.iterator(); traceIt.hasNext(); )
		{
			int[] nextState = (int[]) traceIt.next();

			if (prevState != null)
			{
				int currEventIndex = executer.findTransition(prevState, nextState);

				trace.append(" ");
				trace.append(unionAlphabet.getEventWithIndex(currEventIndex).getLabel());
			}

			prevState = nextState;
		}

		logger.info("The trace leading to the uncontrollable state is:" + trace.toString() + ".");
	}

/*
		public void displayTrace(int[] currState)
		{
				Alphabet unionAlphabet = theAutomaton.getAlphabet();

				// AutomataOnlineSynchronizer executer = new AutomataOnlineSynchronizer(this);
				AutomataSynchronizerExecuter executer = new AutomataSynchronizerExecuter(this);

				executer.initialize();

				int prevStateIndex = AutomataIndexFormHelper.getPrevStateIndex(currState);
				if (prevStateIndex != AutomataIndexFormHelper.STATE_NO_PREVSTATE)
				{
						int[] prevState = theStates.get(prevStateIndex);
						if (prevState != null)
						{
								displayTrace(prevState);
								int currEventIndex = executer.findTransition(prevState, currState);
								if (currEventIndex >= 0)
								{
										logger.info(unionAlphabet.getEventWithIndex(currEventIndex).getLabel());
								}
								else
								{
										logger.error("Could not find an event between prevState and currState");
										logger.error("Current state, index: " + theStates.getIndex(currState));
										logger.error(AutomataIndexFormHelper.dumpVerboseState(currState, theAutomataIndexForm));
										logger.error("Previous state, index: " + theStates.getIndex(prevState));
										logger.error(AutomataIndexFormHelper.dumpVerboseState(prevState, theAutomataIndexForm));
								}
						}
				}
		}
*/

	/**
	 * Returns a string with events from the initial state to currState
	 * "a" -> "b" -> "c"
	 */
	public String displayTrace(int[] currState)
	{
		Alphabet unionAlphabet = theAutomaton.getAlphabet();

		// AutomataOnlineSynchronizer executer = new AutomataOnlineSynchronizer(this);
		AutomataSynchronizerExecuter executer = new AutomataSynchronizerExecuter(this);

		executer.initialize();

		int prevStateIndex = AutomataIndexFormHelper.getPrevStateIndex(currState);

		if (prevStateIndex != AutomataIndexFormHelper.STATE_NO_PREVSTATE)
		{
			int[] prevState = theStates.get(prevStateIndex);

			if (prevState != null)
			{
				String prevString = displayTrace(prevState);
				int currEventIndex = executer.findTransition(prevState, currState);

				if (currEventIndex >= 0)
				{
					if (prevString.equals(""))
					{
						return prevString + unionAlphabet.getEventWithIndex(currEventIndex);
					}
					else
					{
						return prevString + " -> " + unionAlphabet.getEventWithIndex(currEventIndex);
					}

					// logger.info(unionAlphabet.getEventWithIndex(currEventIndex).getLabel());
				}
				else
				{
					logger.error("Error in AutomataSynchronizerHelper");
					logger.error("Could not find an event between prevState and currState\n");
					logger.error("Current state, index: " + theStates.getIndex(currState));
					logger.error(AutomataIndexFormHelper.dumpVerboseState(currState, theAutomataIndexForm));
					logger.error("Previous state, index: " + theStates.getIndex(prevState));
					logger.error(AutomataIndexFormHelper.dumpVerboseState(prevState, theAutomataIndexForm));

					return "";
				}
			}
			else
			{
				return "";
			}
		}
		else
		{
			return "";
		}
	}

	public void setCoExecute(boolean coExecute)
	{
		this.coExecute = coExecute;
	}

	public boolean getCoExecute()
	{
		return coExecute;
	}

	//public void setCoExecuter(AutomataOnlineSynchronizer coExecuter)
	public void setCoExecuter(AutomataSynchronizerExecuter coExecuter)
	{
		this.coExecuter = coExecuter;
	}

	//public AutomataOnlineSynchronizer getCoExecuter()
	public AutomataSynchronizerExecuter getCoExecuter()
	{
		return coExecuter;
	}

	public void printUncontrollableStates()
		throws Exception
	{
		int[] automataIndices = new int[theAutomata.size()];

		for (int i = 0; i < theAutomata.size(); i++)
		{
			automataIndices[i] = i;
		}

		printUncontrollableStates(automataIndices);
	}

	public void printUncontrollableStates(int[] automataIndices)
		throws Exception
	{
		int problemPlant;
		int problemEvent;
		Automaton problemAutomaton;
		int[] currState = new int[automataIndices.length];
		State[][] stateTable = getIndexFormStateTable();

		for (Iterator stateHolderIterator = stateMemorizer.iterator(automataIndices);
				stateHolderIterator.hasNext(); )
		{
			StateHolder stateHolder = (StateHolder) stateHolderIterator.next();

			currState = stateHolder.getArray();
			problemPlant = stateHolder.getProblemPlant();
			problemEvent = stateHolder.getProblemEvent();
			problemAutomaton = theAutomata.getAutomatonAt(problemPlant);

			StringBuffer state = new StringBuffer();
			boolean firstEntry = true;

			for (int i = 0; i < currState.length; i++)
			{
				// Only print states that are not initial if we are looking at a full state
				if (!stateTable[automataIndices[i]][currState[i]].isInitial() || 
					(automataIndices.length < theAutomata.size()))
				{
					if (firstEntry)
					{
						firstEntry = false;
					}
					else
					{
						state.append(", ");
					}

					state.append(theAutomata.getAutomatonAt(automataIndices[i]).getName());
					state.append(": ");
					state.append(stateTable[automataIndices[i]][currState[i]].getName());
				}
			}
			
			String reason = "the event " + theAutomaton.getAlphabet().getEventWithIndex(problemEvent) +
				" is enabled in " + problemAutomaton;
			
			// Log the message
			if (!state.toString().equals(""))
			{
				logger.info("The state " + state + " is uncontrollable since " + reason + ".");
			}
			else
			{
				logger.info("The initial state is uncontrollable since " + reason + ".");
			}
		}
	}

	public boolean isAllAutomataPlants()
	{
		return theAutomata.isAllAutomataPlants();
	}

	public boolean isAllAutomataSupervisors()
	{
		return theAutomata.isAllAutomataSupervisors();
	}

	public boolean isAllAutomataSpecifications()
	{
		return theAutomata.isAllAutomataSpecifications();
	}

	public void selectAutomata(int[] automataIndices)
	{
		if (activeAutomata == null)
		{
			activeAutomata = new boolean[theAutomata.size()];
		}
		else
		{
			for (int i = 0; i < activeAutomata.length; i++)
			{
				activeAutomata[i] = false;
			}
		}

		for (int i = 0; i < automataIndices.length; i++)
		{
			activeAutomata[automataIndices[i]] = true;
		}
	}

	/*
	 *  public void newAutomaton(ArrayList selectedAutomata)
	 *  throws Exception
	 *  {   // Used by automataaSynthesizer (essential when building more than one automata)
	 *  theAutomaton = new Automaton();
	 *
	 *  // Compute the new alphabet
	 *  EventsSet theAlphabets = new EventsSet();
	 *  Iterator autIt = selectedAutomata.iterator();
	 *  while (autIt.hasNext())
	 *  {
	 *  Automaton currAutomaton = (Automaton)autIt.next();
	 *  Alphabet currAlphabet = currAutomaton.getAlphabet();
	 *  theAlphabets.add(currAlphabet);
	 *  }
	 *
	 *  try
	 *  {
	 *  Alphabet theAlphabet = AlphabetHelpers.getUnionAlphabet(theAlphabets, "a");
	 *  theAutomaton.setAlphabet(theAlphabet);
	 *  }
	 *  catch (Exception e)
	 *  {
	 *  System.err.println("Error while generating union alphabet: " + e);
	 *  logger.error("Error while generating union alphabet: " + e);
	 *      logger.debug(e.getStackTrace());
	 *  throw e;
	 *  }
	 *  }
	 */
	public void stopExecutionAfter(int stopExecutionLimit)
	{
		this.stopExecutionLimit = stopExecutionLimit;
	}

	/**
	 * Redefines the controllableEventsTable so that all events are considered uncontrollable.
	 * Used in the AutomataVerifier when performing language inclusion verifications.
	 */
	public void considerAllEventsUncontrollable()
	{
		boolean[] controllableEventsTable = theAutomataIndexForm.getControllableEventsTable();

		for (int i = 0; i < controllableEventsTable.length; i++)
		{
			controllableEventsTable[i] = false;
		}
	}

	/**
	 * Inverts the values of the controllableEventsTable.
	 */
	public void invertControllability()
	{
		boolean[] controllableEventsTable = theAutomataIndexForm.getControllableEventsTable();
		for (int i = 0; i < controllableEventsTable.length; i++)
		{
			controllableEventsTable[i] = !controllableEventsTable[i];
		}
	}

	public class HelperData
	{
		public long nbrOfAddedStates = 0;
		public long nbrOfCheckedStates = 0;
		public long nbrOfForbiddenStates = 0;
		public long nbrOfDeadlockedStates = 0;

		public HelperData() {}

		public long getNumberOfReachableStates()
		{
			return nbrOfAddedStates;
		}

		public long getNumberOfCheckedStates()
		{
			return nbrOfCheckedStates;
		}

		public long getNumberOfForbiddenStates()
		{
			return nbrOfForbiddenStates;
		}

		public long getNumberOfDeadlockedStates()
		{
			return nbrOfDeadlockedStates;
		}

		public void setNumberOfReachableStates(long n)
		{
			nbrOfAddedStates = n;
		}

		public void setNumberOfCheckedStates(long n)
		{
			nbrOfCheckedStates = n;
		}

		public void setNumberOfForbiddenStates(long n)
		{
			nbrOfForbiddenStates = n;
		}

		public void setNumberOfDeadlockedStates(long n)
		{
			nbrOfDeadlockedStates = n;
		}
	}
}
