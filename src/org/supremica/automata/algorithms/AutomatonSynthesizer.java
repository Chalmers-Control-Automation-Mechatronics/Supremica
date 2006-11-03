
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
import java.util.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.standard.ObserverBuilder;
/**
 * A monolithic synthesizer that can handle non-blocking and controllability problems.
 *
 *@author  ka
 *@since  November 28, 2000
 */
public class AutomatonSynthesizer
    implements Stoppable
{
    protected static Logger logger = LoggerFactory.createLogger(AutomatonSynthesizer.class);
    protected Automaton theAutomaton;
    protected StateSet acceptingStates = new StateSet();
    protected SynthesizerOptions synthesizerOptions;
    protected Alphabet disabledUncontrollableEvents;
    protected boolean forcedPurge = false;
    private boolean stopRequested = false;
    
    /**
     * theAutomaton will be changed!
     */
    public AutomatonSynthesizer(Automaton theAutomaton, SynthesizerOptions synthesizerOptions)
    {
        this.theAutomaton = theAutomaton;
        this.synthesizerOptions = synthesizerOptions;
    }
    
    /**
     * Synthesize a monolithic supervisor.
     */
    public boolean synthesize()
    throws Exception
    {
        logger.debug("AutomatonSynthesizer.synthesize...");
        theAutomaton.beginTransaction();

        // Find out which type of synthesis and do it!
        boolean didSomething = false;    // records whether we actually did anything
        SynthesisType synthesisType = synthesizerOptions.getSynthesisType();
        if (synthesisType == SynthesisType.CONTROLLABLE)
        {
            didSomething = synthesizeControllable();
        }
        else if (synthesisType == SynthesisType.NONBLOCKING)
        {
            didSomething = synthesizeNonblocking();
        }
        else if (synthesisType == SynthesisType.NONBLOCKINGCONTROLLABLE)
        {
            didSomething = synthesizeControllableNonblocking();
        }
        else if (synthesisType == SynthesisType.NONBLOCKINGCONTROLLABLEOBSERVABLE)
        {
            didSomething = synthesizeControllableNonblockingObservable();
        }
        else
        {
            throw new IllegalArgumentException("Unknown synthesis type " + synthesisType);
        }
        
        // Compute which uncontrollable were disabled?
        if (synthesizerOptions.doRememberDisabledUncontrollableEvents())
        {
            computeDisabledUncontrollableEvents();
        }
        
        // Purge?
        if (synthesizerOptions.doPurge() || forcedPurge)
        {
            purge();
        }
        
        // Finish
        theAutomaton.invalidate();
        theAutomaton.endTransaction();
        theAutomaton.setComment("sup(" + theAutomaton.getName() + ")");
        theAutomaton.setName(null);
        return didSomething;
    }
    
    public void initializeAcceptingStates()
    {
        Iterator stateIt = theAutomaton.stateIterator();
        logger.debug("AutomatonSynthesizer.initializeAcceptingStates...");
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            if (currState.isAccepting() && !currState.isForbidden())
            {
                acceptingStates.add(currState);
            }
        }
    }
    
    /**
     * Synthesize a controllable, nonblocking and observable supervisor.
     */
    protected boolean synthesizeControllableNonblockingObservable()
    throws Exception
    {
        forcedPurge = true;
        boolean didSomething = false;
        boolean observable = false;
        int observerIteration = 1;
        while (!observable)
        {
            boolean changed = synthesizeControllableNonblocking();
            if (stopRequested)
            {
                return false;
            }
            didSomething = didSomething || changed;
            ObserverBuilder observerBuilder = new ObserverBuilder(theAutomaton, true);
            observerBuilder.execute();
            observable = observerBuilder.isObservable();
            Automaton currObserver = observerBuilder.getNewAutomaton();
            currObserver.setAllStatesAccepting(true);
            currObserver.setName("Observer");
            logger.info("Number of states in observer: " + currObserver.nbrOfStates() + " nbr forb states: " + currObserver.nbrOfForbiddenStates());
            Automata observerAndSupervisor = new Automata();
            observerAndSupervisor.addAutomaton(currObserver);
            observerAndSupervisor.addAutomaton(theAutomaton);
            // observerAndSupervisor.setIndicies();
            SynchronizationOptions observerSynchOptions = new SynchronizationOptions();
            observerSynchOptions.setSynchronizationType(SynchronizationType.FULL);
            AutomataSynchronizer observerSynchronizer = new AutomataSynchronizer(observerAndSupervisor, observerSynchOptions);
            observerSynchronizer.execute();
            Automaton newSystem = observerSynchronizer.getAutomaton();
            logger.info("Number of states in observer||sup: " + newSystem.nbrOfStates() + " nbr forb states: " + newSystem.nbrOfForbiddenStates());
            theAutomaton = newSystem;
            logger.debug("Observer in iteration " + observerIteration + " is " + (observable
                ? "observable"
                : "unobservable"));
            observerIteration++;
        }
        return true;
    }
    
    /**
     * Synthesize a controllable and nonblocking supervisor.
     */
    protected boolean synthesizeControllableNonblocking()
    throws Exception
    {
        StateSet stateList = new StateSet();
        logger.debug("AutomatonSynthesizer.synthesizeControllableNonblocking...");
        for (Iterator stateIt = theAutomaton.stateIterator();
        stateIt.hasNext(); )
        {
            State currState = (State) stateIt.next();
            if (currState.isAccepting() &&!currState.isForbidden())
            {
                acceptingStates.add(currState);
            }
            if (currState.isForbidden())
            {
                stateList.add(currState);
                currState.setCost(State.MAX_COST);
            }
        }
        // Do fixed point iteration
        doControllable(stateList);
        boolean newUnsafeStates = false;
        do
        {
            stateList = doCoreachable();
            newUnsafeStates = stateList.size() > 0;
            if (newUnsafeStates)
            {
                newUnsafeStates = doControllable(stateList);
            }
        }
        while (newUnsafeStates);
        // Reachability
        doReachable();
        // Forbid the states with MAX_COST set
        boolean didSomething = false;
        for (Iterator<State> stateIt = theAutomaton.stateIterator(); stateIt.hasNext(); )
        {
            State currState = (State) stateIt.next();
            if (currState.getCost() == State.MAX_COST)
            {
                currState.setForbidden(true);
                didSomething = true;
            }
        }
        theAutomaton.setType(AutomatonType.SUPERVISOR);
        return didSomething;
    }
    
    /**
     * Synthesize a controllable supervisor.
     */
    protected boolean synthesizeControllable()
    throws Exception
    {
        logger.debug("AutomatonSynthesizer.synthesizeControllable...");
        // boolean newUnsafeStates;
        LinkedList stateList = new LinkedList();
        Iterator stateIt = theAutomaton.stateIterator();
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            if (currState.isAccepting() &&!currState.isForbidden())
            {
                acceptingStates.add(currState);
            }
            if (currState.isForbidden())
            {
                stateList.addLast(currState);
                currState.setCost(State.MAX_COST);
            }
        }
        // Do fixed point iteration
        doControllable(stateList);
        doReachable();
        // Forbid the states with MAX_COST
        boolean didSomething = false;
        stateIt = theAutomaton.stateIterator();
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            if (currState.getCost() == State.MAX_COST)
            {
                currState.setForbidden(true);
                didSomething = true;
            }
        }
        theAutomaton.setType(AutomatonType.SUPERVISOR);
        return didSomething;
    }
    
    protected boolean synthesizeNonblocking()
    throws Exception
    {
        logger.debug("AutomatonSynthesizer.synthesizeNonblocking...");
        boolean newUnsafeStates;
        StateSet stateList = new StateSet();
        initializeAcceptingStates();
        do
        {
            stateList = doCoreachable();
            newUnsafeStates = stateList.size() > 0;
        }
        while (newUnsafeStates);
        doReachable();
        // Forbid the states with MAX_COST set
        boolean didSomething = false;
        Iterator stateIt = theAutomaton.stateIterator();
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            if (currState.getCost() == State.MAX_COST)
            {
                currState.setForbidden(true);
                didSomething = true;
            }
        }
        theAutomaton.setType(AutomatonType.SUPERVISOR);
        return didSomething;
    }
    
    /**
     * Sets the cost of all non-coreachable states to MAX_COST.
     *
     * @return the set of NON-coreachable states.
     */
    public StateSet doCoreachable()
    throws Exception
    {
        logger.debug("AutomatonSynthesizer.doCoreachable...");
        theAutomaton.clearVisitedStates();
        // Push all marked states on the stack
        // Mark the state as visited
        if (acceptingStates.size() == 0)
        {
            // Not initialized? Do it.
            initializeAcceptingStates();
        }
        StateSet stateStack = new StateSet(acceptingStates);
        logger.debug(stateStack.size() + " nbr of accepting states");
        // Do propagate coreachability
        while (stateStack.size() > 0)
        {
            if (stopRequested)
            {
                return new StateSet();
            }
            // Get and remove a state
            State currState = stateStack.remove();
            currState.setVisited(true);
            Iterator arcIt = currState.incomingArcsIterator();
            while (arcIt.hasNext())
            {
                Arc currArc = (Arc) arcIt.next();
                LabeledEvent currEvent = currArc.getEvent();
                State fromState = currArc.getFromState();
                if ((fromState.getCost() != State.MAX_COST) && !fromState.isVisited())
                {
                    fromState.setVisited(true);
                    stateStack.add(fromState);
                }
            }
        }
        // The stateStack is now empty!!
        // Find all states that are not coreachable and
        // mark them as unsafe.
        int nbrOfNewUnsafeStates = 0;
        Iterator stateIt = theAutomaton.stateIterator();
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            if ((!currState.isVisited()) && (currState.getCost() != State.MAX_COST))
            {
                nbrOfNewUnsafeStates++;
                currState.setCost(State.MAX_COST);
                stateStack.add(currState);
            }
        }
        logger.debug("found " + nbrOfNewUnsafeStates + " new blocking states");
        return stateStack;    // return the set of non-coreachable states
    }
    
    /**
     * returns true if uncontrollable states found.
     */
    protected boolean doControllable(Collection<State> stateStack)
    throws Exception
    {
        logger.debug("AutomatonSynthesizer.doControllable...");
        boolean newUnsafeStates = false;
        int nbrOfNewUnsafeStates = 0;
        // Do propagate uncontrollability
        while (stateStack.size() > 0)
        {
            if (stopRequested)
            {
                return false;
            }
            // Get and remove a state from the stack
            State currState = stateStack.iterator().next();
            stateStack.remove(currState);
            Collection<State> newXstates = doControllable(currState);
            if (newXstates.size() != 0)
            {
                newUnsafeStates = true;
                nbrOfNewUnsafeStates += newXstates.size();
                stateStack.addAll(newXstates);
            }
        }
        logger.debug("found " + nbrOfNewUnsafeStates + " new uncontrollable states");
        return newUnsafeStates;
    }
    
    /**
     * This one does for one state
     */
    public StateSet doControllable(State currState)
    {
        StateSet stateStack = new StateSet();
        Iterator arcIt = currState.incomingArcsIterator();
        while (arcIt.hasNext())
        {
            Arc currArc = (Arc) arcIt.next();
            LabeledEvent currEvent = currArc.getEvent();    // theAutomaton.getEvent(currArc.getEventId());
            if (!currEvent.isControllable())
            {
                State fromState = currArc.getFromState();    // backwards over this uc-event
                if (fromState.getCost() != State.MAX_COST)    // if not already forbidden, forbid it
                {
                    fromState.setCost(State.MAX_COST);
                    stateStack.add(fromState);
                    if (fromState.isAccepting())
                    {
                        acceptingStates.remove(fromState);
                    }
                }
            }
        }
        return stateStack;
    }
    
    /**
     * Sets the cost of all non reachable states to State.MAX_COST. Forbidden states
     * stop reachability "synthesis style".
     */
    public void doReachable()
    {
        doReachable(false);
    }
    
    /**
     * Sets the cost of all non reachable states to State.MAX_COST. The treatment of
     * forbidden states as stops for reachability is selectable through the argument.
     *
     * @param expandForbidden If false, states that are reachable only
     * by paths that pass through forbidden states are considered non-reachable.
     */
    public void doReachable(boolean expandForbidden)
    {
        logger.debug("AutomatonSynthesizer.doReachable...");
        theAutomaton.clearVisitedStates();
        // Push the initial state on the stack
        // Mark the state as visited
        State initialState = theAutomaton.getInitialState();
        LinkedList stateStack = new LinkedList();
        //if ((initialState.getCost() != State.MAX_COST)  || expandForbidden)
        if (expandForbidden || !initialState.isForbidden())
        {
            stateStack.addLast(initialState);
        }
        // Do propagate reachability
        while (stateStack.size() > 0)
        {
            if (stopRequested)
            {
                return;
            }
            State currState = (State) stateStack.removeLast();
            currState.setVisited(true);
            // Look at states reachable from here
            Iterator arcIt = currState.outgoingArcsIterator();
            while (arcIt.hasNext())
            {
                Arc currArc = (Arc) arcIt.next();
                State toState = currArc.getToState();
                // Expand unvisited states
                //if (!toState.isVisited() && ((toState.getCost() != State.MAX_COST) || expandForbidden))
                if (!toState.isVisited() && (expandForbidden || !toState.isForbidden()))
                {
                    toState.setVisited(true);
                    stateStack.addLast(toState);
                }
            }
        }
        
        // Set max cost on the states that can't be reached
        Iterator stateIt = theAutomaton.stateIterator();
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            if (!currState.isVisited())
            {
                currState.setCost(State.MAX_COST);
            }
        }
    }
    
    /**
     * Returns the set of UNCONTROLLABLE events that needed to be
     * disabled in the synthesis. If not
     * rememberDisabledUncontrollableEvents is set to true then null
     * is returned.  This method must only be called after completed 
     * synthesis.
     */
    public Alphabet getDisabledUncontrollableEvents()
    {
        return disabledUncontrollableEvents;
    }
    
    protected void computeDisabledUncontrollableEvents()
    {
        disabledUncontrollableEvents = new Alphabet();
        for (State state : theAutomaton)
        {
            // Is this a forbidden state (then all incoming from
            // previous states must be disabled (unless the previous state were
            // forbidden for other reasons?))
            if (state.getCost() == State.MAX_COST)
            {
                // Look through the incoming arcs
                for (Iterator<Arc> evIt = state.incomingArcsIterator(); evIt.hasNext(); )
                {
                    Arc arc = evIt.next();
                    // Don't count selfloops!
                    if (!arc.isSelfLoop())
                    {
                        LabeledEvent event = arc.getEvent();
                        if (!event.isControllable())
                        {
                            try
                            {
                                if (!disabledUncontrollableEvents.contains(event.getLabel()))
                                {
                                    disabledUncontrollableEvents.addEvent(event);
                                }
                            }
                            catch (Exception ex)
                            {
                                logger.error("Error in AutomatonSynthesizer: " + ex.getMessage());
                                logger.debug(ex.getStackTrace());
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void purge()
    {
        List<State> stateList = new LinkedList<State>();
        
        // Find all max cost states
        for (Iterator<State> stateIt = theAutomaton.stateIterator();
        stateIt.hasNext(); )
        {
            State currState = stateIt.next();
            if (currState.getCost() == State.MAX_COST)
            {
                stateList.add(currState);
            }
        }
        // Remove max cost states
        for (Iterator<State> stateIt = stateList.iterator(); stateIt.hasNext(); )
        {
            State currState = stateIt.next();
            theAutomaton.removeState(currState);
        }
        stateList.clear();
    }
    
    /**
     * Return the previously computed result.
     */
    public Automaton getAutomaton()
    {
        return theAutomaton;
    }
    
    /**
     * Method that stops the synthesizer as soon as possible.
     *
     * @see org.supremica.gui.ExecutionDialog
     */
    public void requestStop()
    {
        stopRequested = true;
        logger.debug("AutomatonSynthesizer requested to stop.");
    }
            
    public boolean isStopped()
    {
        return stopRequested;
    }
}
