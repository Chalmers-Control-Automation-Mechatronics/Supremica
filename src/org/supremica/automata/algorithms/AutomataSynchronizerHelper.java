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
import org.supremica.util.*;

import org.supremica.gui.*;
import org.apache.log4j.*;

import java.util.*;

/**
 * Contains information that is common to all synchronization threads.
 */
public final class AutomataSynchronizerHelper
{
 	private AutomataIndexForm theAutomataIndexForm;

  	private IntArrayHashTable theStates;

   	private static Category thisCategory = LogDisplay.createCategory(AutomataSynchronizerHelper.class.getName());

  	private LinkedList statesToProcess;
	private int nbrOfStatesToProcess = 0;

  	// Two locks are used to limit the access the statesToProcess
 	private Object gettingFromStatesToProcessLock = new Object();
  	private Object addingToStatesToProcessLock = new Object();

	private Automata theAutomata;
	private Automaton theAutomaton;

	private boolean automataIsControllable = true;

	private int nbrOfAddedStates = 0;
	private int nbrOfCheckedStates = 0;

	private SynchronizationOptions syncOptions = null;

	// Used by AutomataFastControllabillityCheck
	private StateMemorizer stateMemorizer = new StateMemorizer();
	private boolean rememberUncontrollable = false;
	private boolean expandEventsUsingPriority = false;
	private LinkedList fromStateList = new LinkedList();
	private LinkedList stateTrace = new LinkedList();
	private boolean rememberTrace = false;
	private boolean coExecute = false;
	private AutomataOnlineSynchronizer coExecuter = null;

	// Used by AutomataControllabillityCheck
	private boolean exhaustiveSearch = false;

	// For synchronizing without recalculating the AutomataIndexForm
	private boolean[] activeAutomata;

	// For counting states in cancelDialog
	private CancelDialog cancelDialog = null;

	// Verbose mode
	private boolean verboseMode;

 	public AutomataSynchronizerHelper(Automata theAutomata, SynchronizationOptions syncOptions)
 		throws Exception
    {
		if (theAutomata == null)
		{
			throw new Exception("theAutomata must be non-null");
		}
		if (syncOptions == null)
		{
			throw new Exception("syncOptions must be non-null");
		}

		this.theAutomata = theAutomata;
		this.syncOptions = syncOptions;
		verboseMode = syncOptions.verboseMode();
    	statesToProcess = new LinkedList();
		nbrOfStatesToProcess = 0;

       	theStates = new IntArrayHashTable(syncOptions.getInitialHashtableSize(), syncOptions.expandHashtable());
  		theAutomaton = new Automaton();

		/*
  		// Compute the new alphabet
		EventsSet theAlphabets = new EventsSet();
		Iterator autIt = theAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			Alphabet currAlphabet = currAutomaton.getAlphabet();
			theAlphabets.add(currAlphabet);
		}
		*/

		Alphabet theAlphabet = theAutomata.createUnionAlphabet();
		theAutomaton.setAlphabet(theAlphabet);

		/*
		try
  		{
			Alphabet theAlphabet = AlphabetHelpers.getUnionAlphabet(theAlphabets, "a");
			theAutomaton.setAlphabet(theAlphabet);
		}
  		catch (Exception e)
    	{
			System.err.println("Error while generating union alphabet: " + e);
 			thisCategory.error("Error while generating union alphabet: " + e);
        	throw e;
     	}
		*/

  		try
  		{
    		theAutomataIndexForm = new AutomataIndexForm(theAutomata, theAutomaton);
		}
		catch (Exception e)
		{
			thisCategory.error("Error while computing AutomataIndexForm");
			throw e;
		}
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
		expandEventsUsingPriority = false; // Should be an external option?!? FIXA!
		// Is there anything else that needs to be cleared?...
	}

	public SynchronizationOptions getSynchronizationOptions()
	{
		return syncOptions;
	}

    public Automaton getAutomaton()
    {
        return theAutomaton;
    }

    public Automata getAutomata()
    {
        return theAutomata;
    }

    public AutomataIndexForm getAutomataIndexForm()
    {
		return theAutomataIndexForm;
    }

    /**
     * Add a state to the queue of states waiting for being processed.
     * This is only called by the addInitialState and addState methods.
     */
    public void addStateToProcess(int[] state)
    {
        synchronized (addingToStatesToProcessLock)
        {
			statesToProcess.addLast(state);
			nbrOfStatesToProcess++;
   		}
    }

    /**
     * @return A state if there is more states to process, null otherwise.
     */
    public int[] getStateToProcess()
    {
        synchronized (gettingFromStatesToProcessLock)
        {
	        if (nbrOfStatesToProcess == 0)
			{
	        	return null;
			}
			nbrOfStatesToProcess--;
			if (rememberTrace)
			{
				if (fromStateList.size() > 0)
				{
					while (!(Arrays.equals((int[]) fromStateList.getLast(),(int[]) stateTrace.getLast())))
					{
						stateTrace.removeLast();
					}
					if (stateTrace.size() == 0)
						thisCategory.error("Error, error, error...");
					fromStateList.removeLast();
					stateTrace.addLast(statesToProcess.getLast());
				}
				if (stateTrace.size() == 0)
				{
					stateTrace.addLast(statesToProcess.getLast());
				}
				// Depth first search
				return (int[])statesToProcess.removeLast();
			}
			else
				if (coExecute)
				{
					// Depth first search
					return (int[])statesToProcess.removeLast();
				}
				else
				{
					// Width first search
					return (int[])statesToProcess.removeFirst();
				}
   		}
    }

 	// Add this state to theStates
    public void addState(int[] state)
		throws Exception
    {
        int[] newState = theStates.add(state);
        if (newState != null)
        {
			if (rememberTrace && stateTrace.size() == 0)
			{   // Add initial state
				stateTrace.add(newState);
			}
            addStatus(newState);
        	addStateToProcess(newState);
			/*
			if (verboseMode)
				if (++nbrOfAddedStates % 10000 == 0)
					thisCategory.debug(nbrOfAddedStates + " new states found so far.");
			*/
        }
		else
		{
			if (rememberTrace && (fromStateList.size() != 0))
			{
				fromStateList.removeLast();
			}
		}

  		if (++nbrOfCheckedStates % 2000 == 0)
		{
			if (cancelDialog != null)
				cancelDialog.updateCounter(nbrOfCheckedStates);
			
			/*
			if (verboseMode)
				if (nbrOfCheckedStates % 10000 == 0)
					thisCategory.debug(nbrOfCheckedStates + " states checked so far.");
			*/
		}
    }

   	public void setCancelDialog(CancelDialog cancelDialog)
	{
		this.cancelDialog = cancelDialog;
	}

	public CancelDialog getCancelDialog()
	{
		return cancelDialog;
	}

    /**
     * If the toState does not exist then make a copy of this state
     * and add it to the set of states and to the set of states waiting for processing.
     * If it exists then find it.
     * Insert the arc.
     */
    public void addState(int[] fromState, int[] toState, int eventIndex)
    	throws Exception
    {
		if (rememberTrace)
		{
			fromStateList.addLast(fromState);
		}
		addState(toState);
    }

    public void addStatus(int[] state)
    {
    	int[][] stateStatusTable = theAutomataIndexForm.getStateStatusTable();
		int tmpStatus = stateStatusTable[0][state[0]];

  		boolean forbidden = AutomataIndexFormHelper.isForbidden(tmpStatus);
  		int currStatus;
    	for (int i = 1; i < state.length - 1; i++)
    	{
			if (activeAutomata == null || activeAutomata[i] == true)
			{
				currStatus = stateStatusTable[i][state[i]];
				tmpStatus &= currStatus; // works for everything except forbidden
				forbidden |= AutomataIndexFormHelper.isForbidden(currStatus);
			}
		}
  		if (forbidden)
  		{
    		tmpStatus |= (1 << 2);
		}
  		state[state.length - 1] = tmpStatus;
    }

    public void setForbidden(int[] state, boolean forbidden)
    {
		int currStatus = state[state.length - 1];
		if (forbidden)
		{
			currStatus |= (1 << 2);
		}
		else
		{
			currStatus &= ~(1 << 2);
		}
		state[state.length -1] = currStatus;
	}

    public int[][] getStateTable()
    {
    	return theStates.getTable();
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

	// Used by AutomataControllabilityCheckExecuter... AutomataSynchronizerExecuter
	public void setAutomataIsControllable(boolean isControllable)
	{
		automataIsControllable = isControllable;
	}

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
		Iterator eventIterator = unionAlphabet.iterator();
		Event currEvent;
		int index = 0;
		while (eventIterator.hasNext())
		{
			currEvent = (Event) eventIterator.next();
			if (currEvent.getExpansionPriority() < 0)
			    // The events are already ordered after synchIndex!
			    // eventPriority[currEvent.getSynchIndex()] = 10;
				eventPriority[index++] = 10;
			else
			    // The events are already ordered after synchIndex!
				// eventPriority[currEvent.getSynchIndex()] = currEvent.getExpansionPriority();
				eventPriority[index++] = currEvent.getExpansionPriority();
		}
		return eventPriority;
	}

	public void setRememberTrace(boolean rememberTrace)
	{
		this.rememberTrace = rememberTrace;
	}

	public void displayInfo()
	{
		thisCategory.info("During the execution, " + nbrOfCheckedStates + " states were examined.");
	}

	public void displayTrace()
		throws Exception
	{
		String trace = "";
		int index;
		Alphabet unionAlphabet = theAutomaton.getAlphabet();

		// We have to have an executer for finding the transitions
		clear();
		// AutomataSynchronizerExecuter executer =
		//	new AutomataSynchronizerExecuter(this);
		AutomataOnlineSynchronizer executer = new AutomataOnlineSynchronizer(this);
		executer.initialize();

		int[] fromState;
		for (int i = 0; i < stateTrace.size()-1; i++)
		{
			fromState = (int[]) stateTrace.get(i);
			executer.setCurrState(fromState);
			for (int j=stateTrace.size()-1; j>i; j--)
			{
				index = executer.findTransition(fromState, (int[]) stateTrace.get(j));
				if (index >= 0)
				{
					// thisCategory.debug("Event: " + unionAlphabet.getEventWithIndex(index).getLabel());
					trace = trace + " " + unionAlphabet.getEventWithIndex(index).getLabel();
					if (j != stateTrace.size()-1)
						trace = trace + ",";
					if (j > i+1)
						thisCategory.debug("Shortcut found from state number " + i + " to state number " + j + ".");
					i = j-1;
					break;
				}
				if (i == j-1)
				{
					throw new Exception("Error in AutomataSynchronizerHelper.displayTrace(). Impossible transition found.");
				}
			}
		}

		thisCategory.info("The trace leading to the uncontrollable state is:" + trace + ".");

		/*
		thisCategory.error("And again...");

		while (stateTrace.size() > 1)
		{
			index = executer.findTransition((int[]) stateTrace.removeFirst(), (int[]) stateTrace.getFirst());
			if (index >= 0)
				thisCategory.debug("Event: " + unionAlphabet.getEventWithIndex(index).getLabel());
			else
				thisCategory.error("Possible error?");
		}
		*/
	}

	public void setCoExecute(boolean coExecute)
	{
		this.coExecute = coExecute;
	}

	public boolean getCoExecute()
	{
		return coExecute;
	}

	public void setCoExecuter(AutomataOnlineSynchronizer coExecuter)
	{
		this.coExecuter = coExecuter;
	}

	public AutomataOnlineSynchronizer getCoExecuter()
	{
		return coExecuter;
	}

	public void printUncontrollableStates()
		throws Exception
	{
		int[] automataIndices = new int[theAutomata.size()];
		for(int i=0;i<theAutomata.size();i++)
			automataIndices[i] = i;
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
		StateHolder stateHolder;
		Iterator stateHolderIterator = stateMemorizer.iterator(automataIndices);
		while (stateHolderIterator.hasNext())
		{
			stateHolder = (StateHolder) stateHolderIterator.next();
			currState = stateHolder.getArray();
			problemPlant = stateHolder.getProblemPlant();
			problemEvent = stateHolder.getProblemEvent();
			problemAutomaton = theAutomata.getAutomatonAt(problemPlant);
			String state = "";
			for (int i = 0; i < currState.length; i++)
			{
				// Only print states that are not initial if we are looking at a full state
				if (!stateTable[automataIndices[i]][currState[i]].isInitial() || automataIndices.length < theAutomata.size())
				{
					if (state != "")
						state = state + ",";
					state = state + " " + theAutomata.getAutomatonAt(automataIndices[i]).getName() + ": ";
					state = state + stateTable[automataIndices[i]][currState[i]].getName();
				}
			}
			String reason = "the uncontrollable event " + theAutomaton.getAlphabet().getEventWithIndex(problemEvent).getLabel() + " in the plant " + problemAutomaton.getName() + " is enabled.";
			thisCategory.error("The state" + state + " is uncontrollable since " + reason);
		}
	}

	public boolean isAllAutomataPlants()
	{
		Iterator autIt = theAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			if (currAutomaton.getType() != AutomatonType.Plant)
			{
				return false;
			}
		}

		return true;
	}

	public boolean isAllAutomataSupervisors()
	{
		Iterator autIt = theAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			if (currAutomaton.getType() != AutomatonType.Supervisor)
			{
				return false;
			}
		}

		return true;
	}

	public boolean isAllAutomataSpecifications()
	{
		Iterator autIt = theAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			if (currAutomaton.getType() != AutomatonType.Specification)
			{
				return false;
			}
		}

		return true;
	}

	public void selectAutomata(int[] automataIndices)
	{
		if (activeAutomata == null)
			activeAutomata = new boolean[theAutomata.size()];
		else
			for (int i = 0; i < activeAutomata.length; i++)
				activeAutomata[i] = false;
		for (int i = 0; i < automataIndices.length; i++)
			activeAutomata[automataIndices[i]] = true;
	}

	/*
	public void newAutomaton(ArrayList selectedAutomata)
		throws Exception
	{   // Used by automataaSynthesizer (essential when building more than one automata)
  		theAutomaton = new Automaton();

  		// Compute the new alphabet
		EventsSet theAlphabets = new EventsSet();
		Iterator autIt = selectedAutomata.iterator();
		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			Alphabet currAlphabet = currAutomaton.getAlphabet();
			theAlphabets.add(currAlphabet);
		}

		try
  		{
			Alphabet theAlphabet = AlphabetHelpers.getUnionAlphabet(theAlphabets, "a");
			theAutomaton.setAlphabet(theAlphabet);
		}
  		catch (Exception e)
    	{
			System.err.println("Error while generating union alphabet: " + e);
 			thisCategory.error("Error while generating union alphabet: " + e);
        	throw e;
     	}
	}
	*/
}



