
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
 * Knut Akesson (KA), knut@supremica.orgex
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.UnaryExpressionSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataIndexForm;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.AutomataIndexMap;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.CompositeState;
import org.supremica.automata.LabeledEvent;
import org.supremica.gui.ExecutionDialog;
import org.supremica.gui.ExecutionDialogMode;
import org.supremica.util.SupremicaException;

/**
 * Performs all kinds of synchronisation tasks, for synchronisation,
 * verification and synthesis.
 *
 *@author  Knut &Aring;kesson
 *@since  November 28, 2001
 *@see  AutomataSynchronizer
 *@see  AutomataSynthesizer
 */
public final class AutomataSynchronizerExecuter
    extends Thread
{
    private final static Logger logger = LogManager.getLogger(AutomataSynchronizerExecuter.class);
    private final Thread threadToBeInterruptedUponException;
    private Throwable causeOfInterrupt;

    private final AutomataSynchronizerHelper helper;
    private final AutomataIndexForm indexForm;
    private final int nbrOfAutomata;
    private final int nbrOfEvents;
    private final int[][][] nextStateTable;
    private final int[][][][] nextStatesTable;
    private final int[][][] outgoingEventsTable;
    private final boolean[][] prioritizedEventsTable;
    private final boolean[][] alphabetEventsTable;
    private final boolean[] typeIsPlantTable;
    private final boolean[] controllableEventsTable;
    private final boolean[] immediateEventsTable;
    private final boolean[] epsilonEventsTable; // New! For the nondeterministic case...
    private int[][] currOutgoingEvents;
    private final int[][] eventToAutomatonTable;
    private int[] currOutgoingEventsIndex;
    private int[] automataIndices;
    private int[] currEnabledEvents;
    private int[] disabledEvents;
    private boolean controllableState;
    private final static int IMMEDIATE_NOT_AVAILABLE = -1;
    private int immediateEvent = IMMEDIATE_NOT_AVAILABLE;

    /** Options determining how the synchronization should be performed. */
    private final SynchronizationOptions options;

    /**
     * Determines if uncontrollable states should be marked as forbidden.
     *
     *@see  SynchronizationOptions
     */
    private final boolean forbidUncontrollableStates;

    /**
     * Determines if uncontrollable states should be expanded in the synchronization.
     *
     *@see  SynchronizationOptions
     */
    private boolean expandForbiddenStates;

    /**
     * If true then an arc for all disabled events to a forbidden state is added.
     * This is used when synthesizing supervisors with partial observability.
     *
     *@see  SynchronizationOptions
     */
    private final boolean rememberDisabledEvents;

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
    private final StateMemorizer potentiallyUncontrollableStates;
    private int[] eventPriority;
    private final boolean expandEventsUsingPriority;
    private boolean exhaustiveSearch = false;
    private boolean coExecute = false;

    //private AutomataOnlineSynchronizer coExecuter = null;
    private AutomataSynchronizerExecuter coExecuter = null;

    /** For stopping of thread. */
    private boolean stopRequested = false;

    // Synchonization of all executors
//	private Rendezvous executerRendezvous = null;

    /**
     *@param  synchronizerHelper helper for multithread execution.
     */
    public AutomataSynchronizerExecuter(final AutomataSynchronizerHelper synchronizerHelper)
    {
        setUncaughtExceptionHandler(new UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(final Thread thread, final Throwable cause)
			{
				cause.printStackTrace();
				causeOfInterrupt = cause;
				getThreadToBeInterruptedUponException().interrupt();
			}
        });
    	setPriority(Thread.MIN_PRIORITY);
        this.threadToBeInterruptedUponException = Thread.currentThread();
        // Helper parameters
        helper = synchronizerHelper;
        nbrOfAutomata = helper.getAutomata().size();
        nbrOfEvents = helper.getUnionAlphabet().size();
        potentiallyUncontrollableStates = helper.getStateMemorizer();
        exhaustiveSearch = helper.getExhaustiveSearch();
        rememberUncontrollable = helper.getRememberUncontrollable();
        expandEventsUsingPriority = helper.getExpandEventsUsingPriority();
        coExecute = helper.getCoExecute();
        coExecuter = helper.getCoExecuter();
//		executerRendezvous = helper.getExecuterRendezvous();

        // Indexform parameters
        indexForm = helper.getAutomataIndexForm();
        nextStateTable = indexForm.getNextStateTable();
        nextStatesTable = indexForm.getNextStatesTable(); // New! For the nondeterministic case...
        outgoingEventsTable = indexForm.getOutgoingEventsTable();
        prioritizedEventsTable = indexForm.getPrioritizedEventsTable();
        alphabetEventsTable = indexForm.getAlphabetEventsTable();
        typeIsPlantTable = indexForm.getTypeIsPlantTable();
        controllableEventsTable = indexForm.getControllableEventsTable();
        immediateEventsTable = indexForm.getImmediateEventsTable();
        epsilonEventsTable = indexForm.getEpsilonEventsTable();
        eventToAutomatonTable = indexForm.getEventToAutomatonTable();

        // Syncoptions parameters
        options = synchronizerHelper.getSynchronizationOptions();
        syncType = options.getSynchronizationType();
        forbidUncontrollableStates = options.forbidUncontrollableStates();
        expandForbiddenStates = options.expandForbiddenStates();
        rememberDisabledEvents = options.rememberDisabledEvents();

        // Overrides
        if (expandEventsUsingPriority)
        {
            eventPriority = helper.getEventPriority();
            exhaustiveSearch = true;    // Why force this?
            rememberUncontrollable = true;    // Why force this?
        }
        if (rememberUncontrollable)
        {
            expandForbiddenStates = false;    // Why force this?
        }
    }

    /**
     * Selects the automata in the ArrayList for synchronization.
     *
     *@param  automataToBeSelected Automata representing the automata to be selected
     *@exception  Exception Throws exception if exhaustive search is used.
     */
    public void selectAutomata(final Automata automataToBeSelected)
    throws Exception
    {
        automataIndices = new int[automataToBeSelected.size()];

        int i = 0;

        final Iterator<Automaton> autIt = automataToBeSelected.iterator();
        while(autIt.hasNext())
        {
			final Automaton automaton = autIt.next();
            automataIndices[i++] = indexForm.getAutomataIndexMap().getAutomatonIndex(automaton);
        }

        helper.selectAutomata(automataIndices);

        // FIXA!
        if (exhaustiveSearch)
        {
            throw new SupremicaException("Exhaustive search used in the wrong way!");
        }
    }

    /**
     * Selects the automata in the ArrayList for synchronization.
     *
     *@param  automataToBeSelected ArrayList of the automata to be selected
     *@exception  Exception Throws exception if exhaustive search is used.
     */
    public void selectAutomata(final ArrayList<Automaton> automataToBeSelected)
    throws Exception
    {
        automataIndices = new int[automataToBeSelected.size()];

        for (int i = 0; i < automataToBeSelected.size(); i++)
        {
            automataIndices[i] =  indexForm.getAutomataIndexMap().getAutomatonIndex((automataToBeSelected.get(i)));
        }

        helper.selectAutomata(automataIndices);

        // FIXA!
        if (exhaustiveSearch)
        {
            throw new SupremicaException("Exhaustive search used in the wrong way!");
        }
    }

    /**
     * Selects the automata with the indices in automataIndices for synchronization
     *
     *@param automataIndices Array of int with the indices of the automata to be selected.
     *@exception  Exception Throws exception if exhaustive search is used.
     */
    public void selectAutomata(final int[] automataIndices)
    throws Exception
    {
        this.automataIndices = automataIndices;

        if (exhaustiveSearch)
        {
            throw new SupremicaException("Exhaustive search used in the wrong way!");
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

        //nextState = AutomataIndexFormHelper.createState(nbrOfAutomata);

        // +1 status field (always end with Integer.MAX_VALUE)
        currEnabledEvents = new int[nbrOfEvents + 1];

        // +1 status field (always end with Integer.MAX_VALUE)
        disabledEvents = new int[nbrOfEvents + 1];
    }

    /**
     * Calculates which events are enabled from the state <tt>currState</tt>,
     * if the state turns out uncontrollable, the boolean controllableState
     * is set false.
     *
     * The enabled events can then be found in currEnabledEvents[].
     *
     *@param currState the (full) state to be examined.
     */
    private final void enabledEvents(final int[] currState)
    {
        int currMinEventIndex = Integer.MAX_VALUE;
        final int nbrOfSelectedAutomata = automataIndices.length;

        // Insert all events that leave the current state
        // into currOutgoingEvents, and intialize
        // currOutgoingEventsIndex.
        // Also find the smallest event index possible from
        // the current state.
        for (int i = 0; i < nbrOfSelectedAutomata; i++)
        {
            // Initialization part
            final int currAutIndex = automataIndices[i];
            final int currSingleStateIndex = currState[currAutIndex];

            currOutgoingEvents[currAutIndex] = outgoingEventsTable[currAutIndex][currSingleStateIndex];
            currOutgoingEventsIndex[currAutIndex] = 0;

            // logger.debug("oe: aut: " + currAutIndex + " e: " + AutomataIndexFormHelper.dumpState(outgoingEventsTable[currAutIndex][currSingleStateIndex]));

            // Find the event with the smallest index.
            // The last element currOutgoingEvents[currAutIndex]
            // is always Integer.MAX_VALUE
            final int currEventIndex = currOutgoingEvents[currAutIndex][0];
            if (currEventIndex < currMinEventIndex)
            {
                currMinEventIndex = currEventIndex;
            }
        }

        // Compute all events that are enabled in the current state
        int nbrOfEnabledEvents = 0;
        int nbrOfDisabledEvents = 0;
        boolean thisEventOk;
        boolean thisPlantEventOk;
        boolean canExecuteInPlant;

        controllableState = true;
        immediateEvent = IMMEDIATE_NOT_AVAILABLE;

        while (currMinEventIndex < Integer.MAX_VALUE)
        {
            final int currEventIndex = currMinEventIndex;

            currMinEventIndex = Integer.MAX_VALUE;
            thisEventOk = true;
            thisPlantEventOk = true;
            canExecuteInPlant = false;

            int currAutIndex = 0;
            int currAutEventIndex = 0;

            // Check that this event is possible in all automata
            // that must be ready to execute this event
//			for (int i = 0; i < nbrOfSelectedAutomata; i++)
//			{
//				currAutIndex = automataIndices[i];

            // Above code was replaced to only check those automata that has
            // the specified event in its alphabet
            final int[] automatonTable = eventToAutomatonTable[currEventIndex];

            if (automatonTable == null)
            {
                logger.error("AutomatonTable is null");
                return;
            }

            for (int i = 0; i < nbrOfSelectedAutomata; i++)
            {
                currAutIndex = automataIndices[i];

                // This is the index of the "next" event in the current automaton
                currAutEventIndex = currOutgoingEvents[currAutIndex][currOutgoingEventsIndex[currAutIndex]];

                // logger.debug("checking event " + currAutEventIndex + " in aut " + currAutIndex);
                if (syncType == SynchronizationType.PRIORITIZED)
                {
                    // If the event is prioritized in this automaton (and hence is in the alphabet)
                    if (prioritizedEventsTable[currAutIndex][currEventIndex])
                    {
                        // but it is not the event we're looking for, then...
                        if (currEventIndex != currAutEventIndex)
                        {
                            //... this event should not be executed
                            thisEventOk = false;

                            // For controllability we need to know whether the event was
                            // disabled by a plant or a spec
                            if (typeIsPlantTable[currAutIndex])
                            {
                                thisPlantEventOk = false;
                            }
                        }
                    }
                }
                else if (syncType == SynchronizationType.FULL)
                {
                    // If the event is in the alphabet (and hence is considered prioritized)
                    if (alphabetEventsTable[currAutIndex][currEventIndex])
                    {
                        // but it is not the event we're looking for, then...
                        if (currEventIndex != currAutEventIndex)
                        {
                            //... this event should not be executed
                            thisEventOk = false;

                            // For controllability we need to know whether the
                            // event was disabled by a plant or a spec
                            if (typeIsPlantTable[currAutIndex])
                            {
                                thisPlantEventOk = false;
                            }
                        }
                    }
                }
                else if (syncType == SynchronizationType.BROADCAST)
                {
                    // Why is this?
                    if (typeIsPlantTable[currAutIndex])
                    {
                        thisPlantEventOk = false;
                    }
                }
                else
                {
                    logger.error("Unknown SynchronizationType");
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
                // logger.debug("thisEventOk " + thisEventOk);
            }

            // If everything is ok, or the event is epsilon, it is enabled!
            // (If the event is epsilon, a lot of the above could have been ignored...)
            if (thisEventOk || epsilonEventsTable[currEventIndex])
            {
                if (immediateEventsTable[currEventIndex])
                {
                    // logger.debug("isImmediate");
                    // Clear out everything else and abort the search for enabled events
                    // If several events that are immediate are found
                    // Then the one with smallest index is chosen.
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
            else
            {
                // Keep track of the disabled events
                // This is used when synthesizing supervisors with partial observability
                if (canExecuteInPlant)
                {
                    disabledEvents[nbrOfDisabledEvents++] = currEventIndex;
                    if(helper.getSynchronizationOptions().getEFAMode())
                        helper.mCurrentBlockedEvents.add(indexForm.getIndexMap().getEventAt(currEventIndex));
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
                    logger.info("Uncontrollable state found.");
                    return;
                }
            }
        }

        // Always add Integer.MAX_VALUE as the last element
        currEnabledEvents[nbrOfEnabledEvents++] = Integer.MAX_VALUE;
        disabledEvents[nbrOfDisabledEvents++] = Integer.MAX_VALUE;

        if (expandEventsUsingPriority)
        {
            // Choose outgoing events among the possibilities, choose after priority...
            int insertionIndex = 0;
            int i = 0;
            final int minPrio = 2;
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

            // If an event is enabled in the coexecuter, put it first in the
            // currEnabledEvents array!
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
                logger.debug("The suspect automaton has no corresponding transitions, wandering aimlessly...");
                // Here, the insertionIndex sets the maximium amount of states
                // that are examined...
                // insertionIndex = 2;
                insertionIndex = currEnabledEvents.length - 1;
            }
            else
            {
                logger.debug("Following transitions in the suspect automaton. There are " + insertionIndex + " such transitions...");
            }

            currEnabledEvents[insertionIndex] = Integer.MAX_VALUE;
        }

        //logger.debug("ee: " + AutomataIndexFormHelper.dumpState(currEnabledEvents));
    }

    /**
     * Performs the synchronization.
     */
    @Override
    public void run()
    {
        initialize();
        // Get the first state to process from the helper (the helper is common to all executer threads)
        int[] currState = helper.getStateToProcess();

        // main loop
        while ((currState != null) &&!stopRequested)
        {
            if (coExecute)
            {
                // Set current state in coExecuter and update enabledEvents there
                coExecuter.setCurrState(currState);
            }

            // Update currEnabledEvents
            enabledEvents(currState);

            // Is this state deadlocked?
            if (options.buildAutomaton() && (currEnabledEvents[0] == Integer.MAX_VALUE))
            {
                helper.setDeadlocked(currState, true);
            }

            // Was the state uncontrollable?
            if (!controllableState)
            {
                // Maybe we'd like to remember this state and later on try to show that
                // it will be excluded in the total synchronization...  or not.
                if (rememberUncontrollable)
                {
                  potentiallyUncontrollableStates.add(automataIndices, currState, problemPlant, problemEvent);
                }

                // Forbid uncontrollable state?
                if (forbidUncontrollableStates)
                {
                    helper.setForbidden(currState, true);
                }

                // We now know that there is an uncontrollable state, if this was all we wanted to know,
                // we can return now. (If there are multiple executer threads, we have to tell them too...)
                if (exhaustiveSearch)
                {
                    return;
                }
            }

            // Should we expand this state?
            if (controllableState || expandForbiddenStates)
            {
                // Expand state
                int i = 0;
                int currEventIndex = currEnabledEvents[i];

                // Handle all events
                while (currEventIndex != Integer.MAX_VALUE)
                {
                                        /* **CHANGE FOR NONDETERMINISM**
                                        // Generate an array that contains the indicies of each state.
                                        // Copy the old state, some parts won't change!
                                        //System.arraycopy(currState, 0, nextState, 0, currState.length);
                                        int[] nextState = AutomataIndexFormHelper.createCopyOfState(currState);

                                        // This is where we should add some stuff to take care of
                                        // any nondeterminism.
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
                                                helper.addState(currState, nextState);
                                        }
                                        catch (Exception e)
                                        {
                                                logger.error("Error in AutomataSynchronizerExecuter");
                                                logger.debug(e.getStackTrace());

                                                return;
                                        }
                                         */

                    addNondeterministicStatePermutations(currState, currEventIndex);

                    // Get next enabled event
                    currEventIndex = currEnabledEvents[++i];
                }
            }

            // Get a new state to process from the helper!
            currState = helper.getStateToProcess();

            if (currState == null)
            {

                //finished = false;
/*
                                // This thread tells the other threads that it failed to get
                                // a new state, thus when all threads has entered this state
                                // all threads can stop executing. There is one exception
                                // and this is the possibility for this thread to experience
                                // a temporary shortage of states and at this position
                                // other threads might inform this thread that new
                                // states to process now exist.
                                boolean finished = false;

                                while (!finished && (currState == null) && !stopRequested)
                                {
                                        try
                                        {
                                                if ((Boolean) executerRendezvous.rendezvous(Boolean.TRUE) == Boolean.TRUE)
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
 */
            }
        }
    }

    /**
     * Starts a recursion adding all nondeterministic alternatives
     */
    private void addNondeterministicStatePermutations(final int[] currState, final int currEventIndex)
    {
        // Let nextIndex be the index of the next automaton that has this event enabled at least once
        int nextIndex = -1;
        while (nextStatesTable[automataIndices[++nextIndex]][currState[automataIndices[nextIndex]]][currEventIndex][0] == Integer.MAX_VALUE);

        // Initialize nextState array
        final int[] nextState = AutomataIndexFormHelper.createCopyOfState(currState);

        // Start recursion
        addNextStatePermutation(currState, nextState, currEventIndex, nextIndex);
    }

    /**
     * For finding the nondeterministic permutations of possible next states
     */
    private void addNextStatePermutation(final int[] currState, final int[] nextState, final int currEventIndex, final int index)
    {
        // Is this index an "end index"
        if (index == Integer.MAX_VALUE)
        {
            // Add the current nextState as new state
            try
            {
                //logger.debug("ansp ev: " + currEventIndex + " state: " +AutomataIndexFormHelper.dumpState(currState));

                helper.addState(currState, nextState);

                //logger.info("Add state: " + nextState[0] + "." + nextState[1]);
            }
            catch (final Exception e)
            {
                logger.error("Error in AutomataSynchronizerExecuter");
                logger.debug(e.getStackTrace());
            }

            return;
        }

        // Find the local constants
        final int currAutomatonIndex = automataIndices[index];
        int i = 0;
        int currSingleNextState = nextStatesTable[currAutomatonIndex][currState[currAutomatonIndex]][currEventIndex][i];
        // Let nextIndex be the index (for automataIndices) of the next automaton that has this
        // event enabled at least once
        int nextIndex;
        for (nextIndex = index+1; nextIndex <= automataIndices.length; nextIndex++)
        {
            if (nextIndex == automataIndices.length)
            {
                nextIndex = Integer.MAX_VALUE;
                break;
            }
            if (nextStatesTable[automataIndices[nextIndex]][currState[automataIndices[nextIndex]]][currEventIndex][0] != Integer.MAX_VALUE)
            {
                // This nextIndex is fine!
                break;
            }
        }

        // It is assumed that this method is only called for viable
        // indices (where there really are enabled events)!
        while (currSingleNextState != Integer.MAX_VALUE)
        {
            // Update the nextState array
            nextState[currAutomatonIndex] = currSingleNextState;

            // If the event is an epsilon event, there is no synchronization!
            // A jump in one automaton is enough!
            if (epsilonEventsTable[currEventIndex])
            {
                // Add the current nextState
                addNextStatePermutation(currState, nextState, currEventIndex, Integer.MAX_VALUE);
                // Reset the nextState array
                nextState[currAutomatonIndex] = currState[currAutomatonIndex];
            }
            else
            {
                // Next depth of the recursion
                addNextStatePermutation(currState, nextState, currEventIndex, nextIndex);
            }

            // Update the current substate for this automaton
            currSingleNextState = nextStatesTable[currAutomatonIndex][currState[currAutomatonIndex]][currEventIndex][++i];
        }

        if ((nextIndex != Integer.MAX_VALUE) && epsilonEventsTable[currEventIndex])
        {
            // Reset the nextState array and start a new "recursion" (with epsilon events, the "recursion"
            // is actually a straight sequence
            addNextStatePermutation(currState, nextState, currEventIndex, nextIndex);
        }
    }

    /**
     * A call to this method stops the execution of the run-method or
     * the buildAutomaton-method as soon as possible.
     *
     *@see  AutomataSynchronizer
     *@see  AutomataVerifier
     */
    public void requestStop()
    {		// logger.debug("Executer requested to stop.");
        stopRequested = true;
    }

    /**
     * Builds automaton using either concatenated state names or new,
     * short, unique names as new state names.
     *
     * @return true if build successful, false if build is stopped with requestStop().
     */
    public boolean buildAutomaton()
    {
        Automaton theAutomaton = helper.getAutomaton();

        // Should we add disabled events to a dump state? If so, create that state!
        org.supremica.automata.DumpState dumpState = null; // This one is used below!
        if (rememberDisabledEvents)
        {
            // dumpState = theAutomaton.createUniqueState("qf");
            // theAutomaton.addState(dumpState);
            dumpState = theAutomaton.getDumpState(true);
            dumpState.setForbidden(true);
        }

        // Initialize execution dialog
        final ExecutionDialog executionDialog = helper.getExecutionDialog();
        if (executionDialog != null)
        {
            executionDialog.initProgressBar(0, helper.getStateTableSize());
            executionDialog.setMode(ExecutionDialogMode.SYNCHRONIZINGBUILDINGSTATES);
        }

        // Create all states
        final int[][] currStateTable = helper.getStateTable();
        int stateNumber = 1; // 0 is reserved for the inital state
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
                final int[] currState = currStateTable[i];
                CompositeState newState = null;

                // Should the state name be based on the names of the states that
                // it is constructed from or not?
                if (options.useShortStateNames())
                {
                    // Make sure the initial state gets number 0
                    if (AutomataIndexFormHelper.isInitial(currState))
                    {
                        newState = new CompositeState("q0", currState, helper);
                    }
                    else
                    {
                        newState = new CompositeState("q" + stateNumber++, currState, helper);
                    }
                }
                else
                {
                    final org.supremica.automata.State[][] stateTable = indexForm.getStateTable();
                    final StringBuilder sb = new StringBuilder();

                    for (int j = 0; j < currState.length - AutomataIndexFormHelper.STATE_EXTRA_DATA; j++)
                    {
                        // It should be name here, right? That's what the method description says...
                        //sb.append(stateTable[j][currState[j]].getId());
                        sb.append(stateTable[j][currState[j]].getName());
                        sb.append(options.getStateNameSeparator());
                    }
                    if (sb.length() == 0) {
                      sb.append("q0");
                    } else {
                      // Remove last separator string element
                      sb.setLength(sb.length() - options.getStateNameSeparator().length());
                    }

                    // Create state
                    newState = new CompositeState(sb.toString(), currState, helper);
                }

                // Set some attributes of the state
                //newState.setAutomataSynchronizerExecutorIndex(i);
                newState.setIndex(i);
                //newState.setName(newState.getName());
                newState.setInitial(AutomataIndexFormHelper.isInitial(currState));
                newState.setAccepting(AutomataIndexFormHelper.isAccepting(currState));
                newState.setForbidden(AutomataIndexFormHelper.isForbidden(currState));
                newState.setFirst(AutomataIndexFormHelper.isFirst(currState));
                newState.setLast(AutomataIndexFormHelper.isLast(currState));
                newState.initCosts();

                if(helper.getSynchronizationOptions().getEFAMode())
                    helper.mCurrentNodeMap.put(newState, helper.importNode(newState));

                theAutomaton.addState(newState);
            }
        }

        if (executionDialog != null)
        {
            executionDialog.initProgressBar(0, currStateTable.length);
            executionDialog.setMode(ExecutionDialogMode.SYNCHRONIZINGBUILDINGTRANSITIONS);
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
                theAutomaton = null;

                return false;
            }

            if (currStateTable[k] != null)
            {
                final int[] currState = currStateTable[k];
                final org.supremica.automata.State thisState = theAutomaton.getStateWithIndex(k);

                // Expand state? Otherwise the transitions will not be shown.
                if (thisState.isForbidden() && !expandForbiddenStates)
                {
                    continue;
                }

                // Adjust the array currEnabledEvents to fit currState
                enabledEvents(currState);

                // Handle all events in a while-loop
                int i = 0;
                int currEventIndex = currEnabledEvents[i];
                while (currEventIndex != Integer.MAX_VALUE)
                {
                                            /* **CHANGE FOR NONDETERMINISM**
                                            // Generate an array that contains the indices of each state
                                            // Copy the old state, some parts won't change!
                                            int[] nextState = AutomataIndexFormHelper.createCopyOfState(currState);

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

                                            // Add arc
                                            try
                                            {
                                                    // Check if nextState exists
                                                    int nextIndex = helper.getStateIndex(nextState);

                                                    if (nextIndex >= 0)
                                                    {
                                                            // Create new arc
                                                            State toState = theAutomaton.getStateWithIndex(nextIndex);
                                                            LabeledEvent theEvent = theAlphabet.getEventWithIndex(currEventIndex);
                                                            Arc newArc = new Arc(thisState, toState, theEvent);

                                                            theAutomaton.addArc(newArc);
                                                    }
                                            }
                                            catch (Exception e)
                                            {
                                                    logger.error("Exception when checking next state. " + e);
                                                    logger.debug(e.getStackTrace());
                                            }
                                             */

                    // Recursion for adding arcs, also nondeterministic
                    addNondeterministicArcPermutations(currState, currEventIndex);

                    // Next event
                    currEventIndex = currEnabledEvents[++i];
                }

                if (rememberDisabledEvents)
                {
                    i = 0;

                    int disabledEventIndex = disabledEvents[i];

                    // Handle all events
                    while (disabledEventIndex != Integer.MAX_VALUE)
                    {
                        //LabeledEvent theEvent = theAlphabet.getEventWithIndex(currDisabledEventIndex);
                        final LabeledEvent event = helper.getIndexMap().getEventAt(disabledEventIndex);
                        final Arc newArc = new Arc(thisState, dumpState, event);

                        theAutomaton.addArc(newArc);

                        disabledEventIndex = disabledEvents[++i];
                    }
                }
            }
        }

        if (helper.isAllAutomataPlants())
        {
            theAutomaton.setType(AutomatonType.PLANT);
        }
        else if (helper.isAllAutomataSupervisors())
        {
            theAutomaton.setType(AutomatonType.SUPERVISOR);
        }
        else if (helper.isAllAutomataSpecifications())
        {
            theAutomaton.setType(AutomatonType.SPECIFICATION);
        }
        else
        {
            // theAutomaton.setType(AutomatonType.Undefined);
            theAutomaton.setType(AutomatonType.PLANT);
        }

        return true;
    }


    /**
     * Starts a recursion adding all nondeterministic alternatives
     */
    private void addNondeterministicArcPermutations(final int[] currState, final int currEventIndex)
    {
        // Let nextIndex be the index of the next automaton that has this event enabled at least once
        int nextIndex = -1;
        while (nextStatesTable[automataIndices[++nextIndex]][currState[automataIndices[nextIndex]]][currEventIndex][0] == Integer.MAX_VALUE);

        // Initialize nextState array
        final int[] nextState = AutomataIndexFormHelper.createCopyOfState(currState);

        // Start recursion
        addNextArcPermutation(currState, nextState, currEventIndex, nextIndex);
    }

    /**
     * For finding the nondeterministic permutations of possible next states
     */
    private void addNextArcPermutation(final int[] currState, final int[] nextState, final int currEventIndex, final int index)
    {
        final AutomataIndexMap indexMap = indexForm.getIndexMap();

        // Is this index an "end index"
        if (index == Integer.MAX_VALUE)
        {
            // Add arc
            try
            {
                // Check if nextState exists
                final int nextIndex = helper.getStateIndex(nextState);

                if (nextIndex >= 0)
                {
                    // Get fromState (from currState) and toState (from nextState) and the event

                    final Automaton theAutomaton = helper.getAutomaton();

                    final org.supremica.automata.State fromState = theAutomaton.getStateWithIndex(helper.getStateIndex(currState));
                    final org.supremica.automata.State toState = theAutomaton.getStateWithIndex(nextIndex);
                    final LabeledEvent theEvent = indexMap.getEventAt(currEventIndex);
                    //theAutomaton.getAlphabet().getEventWithIndex(currEventIndex);

                    ///////////////////for EFA synchronization
                    if(helper.getSynchronizationOptions().getEFAMode())
                    {
                        final org.supremica.automata.State[][] stateTable = indexForm.getStateTable();

                        final List<SimpleExpressionProxy> allGuards = new ArrayList<SimpleExpressionProxy>();
                        final List<BinaryExpressionProxy> allActions = new ArrayList<BinaryExpressionProxy>();
                        for (int j = 0; j < currState.length - AutomataIndexFormHelper.STATE_EXTRA_DATA; j++)
                        {
                            if(indexForm.getAutomaton(j).getAlphabet().contains(theEvent))
                            {
                                final String automatonName = indexForm.getAutomaton(j).getName();
                                final String fStateName = stateTable[j][currState[j]].getName();
                                final String tStateName = stateTable[j][nextState[j]].getName();
                                final String eventName = theEvent.getName();
//                                System.out.println(automatonName+"   "+fStateName+"   "+tStateName+"   "+eventName);

                                final EdgeSubject edge = helper.getEdge(automatonName, fStateName, tStateName, eventName);
                                final List<SimpleExpressionProxy> guards = edge.getGuardActionBlock().getGuards();
                                final List<BinaryExpressionProxy> actions = edge.getGuardActionBlock().getActions();
                                allGuards.addAll(guards);
                                allActions.addAll(actions);
//                                System.out.println(edge.getGuardActionBlock().getGuardsModifiable().get(0).toString());
                            }
                        }

                        final Set<EventProxy> newlabel = new TreeSet<EventProxy>();
                        newlabel.add(theEvent);
    //                    }

                        final ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
                        final ExpressionParser parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());

                        final GuardActionBlockSubject gab = new GuardActionBlockSubject();
                        if(allGuards.size() > 0)
                        {
                            SimpleExpressionSubject synchedGuard = null;

                            if(allGuards.size() > 1)
                            {
                                final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
                                final Iterator<SimpleExpressionProxy> guardIt = allGuards.iterator();
                                final SimpleExpressionProxy leftGuard = parser.parse(guardIt.next().toString(),Operator.TYPE_BOOLEAN);
                                SimpleExpressionProxy rightGuard = parser.parse(guardIt.next().toString(),Operator.TYPE_BOOLEAN);
                                synchedGuard = factory.createBinaryExpressionProxy(optable.getAndOperator(), leftGuard , rightGuard);
                                while(guardIt.hasNext())
                                {
                                    rightGuard = parser.parse(guardIt.next().toString(),Operator.TYPE_BOOLEAN);
                                    synchedGuard = factory.createBinaryExpressionProxy(optable.getAndOperator(),synchedGuard , rightGuard);
                                }
                            }
                            else
                            {
                                if(allGuards.get(0) instanceof BinaryExpressionProxy)
                                    synchedGuard = (BinaryExpressionSubject)allGuards.get(0);
                                else
                                    synchedGuard = (UnaryExpressionSubject)allGuards.get(0);
                            }
                            gab.getGuardsModifiable().add((SimpleExpressionSubject)parser.parse(synchedGuard.toString(),Operator.TYPE_BOOLEAN));
                        }
                        if(allActions.size() > 0)
                        {
                            final Iterator<BinaryExpressionProxy> actionIt = allActions.iterator();
                            while(actionIt.hasNext())
                            {
                                final BinaryExpressionSubject bes = (BinaryExpressionSubject)(parser.parse(actionIt.next().toString(),Operator.TYPE_ARITHMETIC));
                                gab.getActionsModifiable().add(bes);
                            }
                        }
                        final EdgeSubject edge = helper.importEdge(fromState, toState, newlabel,gab);

                        helper.mEdges.add(edge);
                    }

                    //////////////////////////////

                    // Create new arc
                    final Arc newArc = new Arc(fromState, toState, theEvent);
                    //logger.info("Add arc: " + newArc);
                    theAutomaton.addArc(newArc);
                }
            }
            catch (final Exception e)
            {
                logger.error("Exception when checking next state. " + e);
                logger.debug(e.getStackTrace());
            }

            return;
        }

        // Find the local constants
        final int currAutomatonIndex = automataIndices[index];
        int i = 0;
        int currSingleNextState = nextStatesTable[currAutomatonIndex][currState[currAutomatonIndex]][currEventIndex][i];
        // Let nextIndex be the index (for automataIndices) of the next automaton that has this
        // event enabled at least once
        int nextIndex;
        for (nextIndex = index+1; nextIndex <= automataIndices.length; nextIndex++)
        {
            if (nextIndex == automataIndices.length)
            {
                nextIndex = Integer.MAX_VALUE;
                break;
            }
            if (nextStatesTable[automataIndices[nextIndex]][currState[automataIndices[nextIndex]]][currEventIndex][0] != Integer.MAX_VALUE)
            {
                // This nextIndex is fine!
                break;
            }
        }

        // It is assumed that this method is only called for viable
        // indices (where there really are enabled events)!
        while (currSingleNextState != Integer.MAX_VALUE)
        {
            // Update the nextState array
            nextState[currAutomatonIndex] = currSingleNextState;

            // If the event is an epsilon event, there is no synchronization!
            // A jump in one automaton is enough!
            if (epsilonEventsTable[currEventIndex])
            {
                // Add the current nextState
                addNextArcPermutation(currState, nextState, currEventIndex, Integer.MAX_VALUE);
                // Reset the nextState array
                nextState[currAutomatonIndex] = currState[currAutomatonIndex];
            }
            else
            {
                // Next depth of the recursion
                addNextArcPermutation(currState, nextState, currEventIndex, nextIndex);
            }

            // Update the current substate for this automaton
            currSingleNextState = nextStatesTable[currAutomatonIndex][currState[currAutomatonIndex]][currEventIndex][++i];
        }

        if ((nextIndex != Integer.MAX_VALUE) && epsilonEventsTable[currEventIndex])
        {
            // Reset the nextState array and start a new "recursion" (with epsilon events, the "recursion"
            // is actually a straight sequence
            addNextArcPermutation(currState, nextState, currEventIndex, nextIndex);
        }
    }


    @SuppressWarnings("unused")
	private String printTypeIsPlantTable()
    {
        final StringBuilder sb = new StringBuilder();

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
         * public int[][] previousStates(int[] state, int currEventIndex)
         * {
         * int[][][][] prevStatesTable = theAutomataIndexForm.getPrevStatesTable();
         * int[] nbrOfPrevStates = int[nbrOfAutomata];
         * int[] currIndexOfPrevStates = int[nbrOfAutomata];
         * int[] currPrevState = int[nbrOfAutomata + 1];
         *
         * // First compute the maximum nbr of previous states
         * int maxNbrOfPreviousStates = 1;
         * for (int i = 1; i < state.length - 1; i++)
         * {
         * // ToDo Check if this automaton is among the selected
         *
         * int currAutomatonIndex = i;
         * int currStateIndex = state[currAutomatonIndex];
         * int[] prevStates = prevStatesTable[currAutomatonIndex][currStateIndex][currEventIndex];
         * if (prevStates != null)
         * {
         * int currNbrOfPreviousStates = prevStates[prevStates.length - 1];
         * nbrOfPrevStates[i] = currNbrOfPreviousStates;
         * if (currNbrOfPreviousStates > 0)
         * {
         * currIndexOfPrevStates
         * }
         * else
         * {
         * currIndexOfPreviousState[i] = 0;
         * }
         * maxNbrOfPreviousStates = maxNbrOfPreviousStates * currNbrOfPreviousStates;
         * }
         * }
         *
         * int[][] previousStates = new int[maxNbrOfPreviousState][];
         * for (int i = 1; i < state.length - 2; i++)
         * {
         * for(int j = i + 1; j < )
         *
         * }
         *
         * // Check if this event is included
         * int[] existingPrevState = theStates.get(currPrevState);
         * if (existingPrevState != null)
         * {
         * // Check if the event is really possible
         * if (isValidTransition(existingPrevState, state, currEventIndex))
         * {
         * previousStates[xx] = existingPrevState;
         * }
         * }
         *
         * }
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
         * public boolean isValidTransition(int[] fromState, int[] toState, int event)
         * {
         * if (prioritizedEventInResultAutomaton[ewent])
        `* { // Check that event is possible from all automata that have
         * // this event as prioritized.
         *
         * // To do
         * }
         * else
         * { // We assume that the event is possible in at least one of the
         * // original automata.
         * return true;
         * }
         * }
         */

    /**
     * Method for finding transitions between states. Used by displayTrace() in AutomataSynchronizerHelper.
     *
     *@param  fromState state to find transition from.
     *@param  toState state to find transition to.
     *@return  index of one (of perhaps many) transitions between fromState and toState or -1 if none exists.
     *@see  AutomataSynchronizerHelper#displayTrace()
     */
    public int findTransition(final int[] fromState, final int[] toState)
    {
        // Find the currently enabled events (and put these in currEnabledEvents).
        enabledEvents(fromState);

        int i = 0;
        int currEventIndex = currEnabledEvents[i];

        // Handle all events
        while (currEventIndex != Integer.MAX_VALUE)
        {
            // Generate an array that contains the indicies of each state, nextstate, initialize
            // it with the values of toState, to get the correct status values in the end!
            // System.arraycopy(toState, 0, nextState, 0, fromState.length);
            final int[] nextState = AutomataIndexFormHelper.createCopyOfState(toState);

            // System.arraycopy(fromState, 0, nextState, 0, fromState.length);
            // Iterate over all automata to construct the new state
            for (int j = 0; j < nbrOfAutomata; j++)
            {
                final int currAutomatonIndex = j;
                final int currSingleNextState = nextStateTable[currAutomatonIndex][fromState[currAutomatonIndex]][currEventIndex];

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
    private static boolean equalsIntArray(final int[] firstArray, final int[] secondArray)
    {

        // Assume that the last element is a status field
        for (int i = 0;
        i < firstArray.length - AutomataIndexFormHelper.STATE_EXTRA_DATA;
        i++)
        {
            if (firstArray[i] != secondArray[i])
            {
                return false;
            }
        }

        return true;
    }

    // ****************************************************** //
    //  Methods for "online" running of the synchroniser      //
    // ****************************************************** //

    public boolean isEnabled(final LabeledEvent theEvent)
    {
        //return isEnabled(theEvent.getIndex());
        return isEnabled(indexForm.getAutomataIndexMap().getEventIndex(theEvent));
    }

    public boolean isEnabled(final int eventIndex)
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
    public void setCurrState(final int[] state)
    {
        enabledEvents(state);
    }

    /**
     * Changes state along transition of theEvent.
     * IT IS ASSUMED THAT theEvent IS ENABLED!!
     */
    public int[] doTransition(final int[] currState, final LabeledEvent theEvent)
    {
        //return doTransition(currState, theEvent.getIndex());
        return doTransition(currState, indexForm.getAutomataIndexMap().getEventIndex(theEvent));
    }

    /**
     * Changes state along transition of the event with index eventIndex.
     * IT IS ASSUMED THAT THE EVENT WITH INDEX eventIndex IS ENABLED!!
     */
    public int[] doTransition(int[] currState, final int eventIndex)
    {
        //System.err.println("doTransition: eventIndex " + eventIndex);
        // Counting on correct input here... only enabled events, please...
        // Construct new state
        //System.arraycopy(currState, 0, nextState, 0, currState.length);
        final int[] nextState = AutomataIndexFormHelper.createCopyOfState(currState);

        // Iterate over all automata to construct the new state
        //for (int j = 0; j < nbrOfSelectedAutomata; j++)
        for (int j = 0; j < automataIndices.length; j++)
        {
            final int currAutomatonIndex = automataIndices[j];
            final int currSingleNextState = nextStateTable[currAutomatonIndex][currState[currAutomatonIndex]][eventIndex];

            // Jump in all automata that have this event active.
            if (currSingleNextState != Integer.MAX_VALUE)
            {
                nextState[currAutomatonIndex] = currSingleNextState;
            }
        }

        // System.arraycopy(nextState, 0, currState, 0, currState.length);
        currState = nextState;

        // return currState;
        return nextState;
    }

    public int[] getOutgoingEvents(final int[] state)
    {
        enabledEvents(state);

        return currEnabledEvents;
    }

    public int[] getIncomingEvents(final int[] state)
    {
        // Not finished... FIXA!
        return (new int[]{ 0, 1, Integer.MAX_VALUE });
    }

    public boolean isControllable()
    {
        return controllableState;
    }

	public Thread getThreadToBeInterruptedUponException() {
		return threadToBeInterruptedUponException;
	}

	public Throwable getCauseOfInterrupt() {
		return causeOfInterrupt;
	}
}
