
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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.analysis.Abortable;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.automata.StateSet;
import org.supremica.automata.algorithms.standard.ObserverBuilder;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.properties.Config;

/**
 * A monolithic synthesizer that can handle non-blocking and controllability problems.
 *
 *@author ka
 *@since November 28, 2000
 */
public class AutomatonSynthesizer
    implements Abortable
{
    protected static Logger logger = LoggerFactory.createLogger(AutomatonSynthesizer.class);
    protected Automaton theAutomaton;
    protected StateSet acceptingStates = new StateSet();
    protected SynthesizerOptions synthesizerOptions;
    protected Alphabet disabledUncontrollableEvents;
    protected boolean forcedPurge = false;
    private boolean abortRequested = false;

    /**
     * theAutomaton will be changed!
     */
    public AutomatonSynthesizer(final Automaton theAutomaton, final SynthesizerOptions synthesizerOptions)
    {
        this.theAutomaton = theAutomaton;
        this.synthesizerOptions = synthesizerOptions;
    }

    /**
     * Synthesize a monolithic supervisor.
     */
    public boolean synthesize()
    {
        logger.debug("AutomatonSynthesizer.synthesize...");
        theAutomaton.beginTransaction();

        // Find out which type of synthesis and do it!
        boolean didSomething = false;    // records whether we actually did anything
        final SynthesisType synthesisType = synthesizerOptions.getSynthesisType();
        if (synthesisType == SynthesisType.CONTROLLABLE)
        {
            didSomething = synthesizeControllable();
        }
        else if (synthesisType == SynthesisType.NONBLOCKING)
        {
            didSomething = synthesizeNonblocking();
        }
        else if (synthesisType == SynthesisType.NONBLOCKING_CONTROLLABLE)
        {
            didSomething = synthesizeControllableNonblocking();
        }
        else if (synthesisType == SynthesisType.NONBLOCKING_CONTROLLABLE_NORMAL)
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
        final Iterator<?> stateIt = theAutomaton.stateIterator();
        logger.debug("AutomatonSynthesizer.initializeAcceptingStates...");
        while (stateIt.hasNext())
        {
            final State currState = (State) stateIt.next();
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
    {
        forcedPurge = true;
        boolean didSomething = false;
        boolean observable = false;
        int observerIteration = 1;
        while (!observable)
        {
            final boolean changed = synthesizeControllableNonblocking();
            if (abortRequested)
            {
                return false;
            }
            didSomething = didSomething || changed;
            final ObserverBuilder observerBuilder = new ObserverBuilder(theAutomaton, true);
            observerBuilder.execute();
            observable = observerBuilder.isObservable();
            final Automaton currObserver = observerBuilder.getNewAutomaton();
            currObserver.setAllStatesAccepting(true);
            currObserver.setName("Observer");
            logger.info("Number of states in observer: " + currObserver.nbrOfStates() + " nbr forb states: " + currObserver.nbrOfForbiddenStates());
            final Automata observerAndSupervisor = new Automata();
            observerAndSupervisor.addAutomaton(currObserver);
            observerAndSupervisor.addAutomaton(theAutomaton);
            // observerAndSupervisor.setIndicies();
            final SynchronizationOptions observerSynchOptions = new SynchronizationOptions();
            observerSynchOptions.setSynchronizationType(SynchronizationType.FULL);
            final AutomataSynchronizer observerSynchronizer = new AutomataSynchronizer(observerAndSupervisor, observerSynchOptions, Config.SYNTHESIS_SUP_AS_PLANT.get());
            observerSynchronizer.execute();
            final Automaton newSystem = observerSynchronizer.getAutomaton();
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
    {
        StateSet stateList = new StateSet();
        logger.debug("AutomatonSynthesizer.synthesizeControllableNonblocking...");
        for (final State state: theAutomaton)
        {
            if (state.isAccepting() &&!state.isForbidden())
            {
                acceptingStates.add(state);
            }
            if (state.isForbidden())
            {
                stateList.add(state);
                state.setCost(State.MAX_COST);
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
        for (final State state: theAutomaton)
        {
            if (state.getCost() == State.MAX_COST)
            {
                state.setForbidden(true);
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
    {
        logger.debug("AutomatonSynthesizer.synthesizeControllable...");
        // boolean newUnsafeStates;
        final LinkedList<State> stateList = new LinkedList<State>();
        Iterator<?> stateIt = theAutomaton.stateIterator();
        while (stateIt.hasNext())
        {
            final State currState = (State) stateIt.next();
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
            final State currState = (State) stateIt.next();
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
        final Iterator<?> stateIt = theAutomaton.stateIterator();
        while (stateIt.hasNext())
        {
            final State currState = (State) stateIt.next();
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
        final StateSet stateStack = new StateSet(acceptingStates);
        logger.debug(stateStack.size() + " nbr of accepting states");
        // Do propagate coreachability
        while (stateStack.size() > 0)
        {
            if (abortRequested)
            {
                return new StateSet();
            }
            // Get and remove a state
            final State currState = stateStack.remove();
            currState.setVisited(true);
            final Iterator<?> arcIt = currState.incomingArcsIterator();
            while (arcIt.hasNext())
            {
                final Arc currArc = (Arc) arcIt.next();
                final State fromState = currArc.getFromState();
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
        final Iterator<?> stateIt = theAutomaton.stateIterator();
        while (stateIt.hasNext())
        {
            final State currState = (State) stateIt.next();
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
    protected boolean doControllable(final Collection<State> stateStack)
    {
        logger.debug("AutomatonSynthesizer.doControllable...");
        boolean newUnsafeStates = false;
        int nbrOfNewUnsafeStates = 0;
        // Do propagate uncontrollability
        while (stateStack.size() > 0)
        {
            if (abortRequested)
            {
                return false;
            }
            // Get and remove a state from the stack
            final State currState = stateStack.iterator().next();
            stateStack.remove(currState);
            final Collection<State> newXstates = doControllable(currState);
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
    public StateSet doControllable(final State currState)
    {
        final StateSet stateStack = new StateSet();
        final Iterator<?> arcIt = currState.incomingArcsIterator();
        while (arcIt.hasNext())
        {
            final Arc currArc = (Arc) arcIt.next();
            final LabeledEvent currEvent = currArc.getEvent();    // theAutomaton.getEvent(currArc.getEventId());
            if (!currEvent.isControllable())
            {
                final State fromState = currArc.getFromState();    // backwards over this uc-event
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
    public void doReachable(final boolean expandForbidden)
    {
        logger.debug("AutomatonSynthesizer.doReachable...");
        theAutomaton.clearVisitedStates();
        // Push the initial state on the stack
        // Mark the state as visited
        final State initialState = theAutomaton.getInitialState();
        final LinkedList<State> stateStack = new LinkedList<State>();
        //if ((initialState.getCost() != State.MAX_COST)  || expandForbidden)
        if (expandForbidden || !initialState.isForbidden())
        {
            stateStack.addLast(initialState);
        }
        // Do propagate reachability
        while (stateStack.size() > 0)
        {
            if (abortRequested)
            {
                return;
            }
            final State currState = stateStack.removeLast();
            currState.setVisited(true);
            // Look at states reachable from here
            final Iterator<?> arcIt = currState.outgoingArcsIterator();
            while (arcIt.hasNext())
            {
                final Arc currArc = (Arc) arcIt.next();
                final State toState = currArc.getToState();
                // Expand unvisited states
                //if (!toState.isVisited() && ((toState.getCost() != State.MAX_COST) || expandForbidden))
                if (!toState.isVisited() && (expandForbidden || !(toState.isForbidden() || (toState.getCost() == State.MAX_COST))))
                {
                    toState.setVisited(true);
                    stateStack.addLast(toState);
                }
            }
        }

        // Set max cost on the states that can't be reached
        final Iterator<?> stateIt = theAutomaton.stateIterator();
        while (stateIt.hasNext())
        {
            final State currState = (State) stateIt.next();

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
        for (final State state : theAutomaton)
        {
            // Is this a forbidden state (then all incoming from
            // previous states must be disabled (unless the previous state were
            // forbidden for other reasons?))
            if (state.getCost() == State.MAX_COST)
            {
                // Look through the incoming arcs
                for (final Iterator<Arc> evIt = state.incomingArcsIterator(); evIt.hasNext(); )
                {
                    final Arc arc = evIt.next();
                    // Don't count selfloops!
                    if (!arc.isSelfLoop())
                    {
                        final LabeledEvent event = arc.getEvent();
                        if (!event.isControllable())
                        {
                            try
                            {
                                if (!disabledUncontrollableEvents.contains(event.getLabel()))
                                {
                                    disabledUncontrollableEvents.addEvent(event);
                                }
                            }
                            catch (final Exception ex)
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
        final List<State> stateList = new LinkedList<State>();

        // Find all max cost states
        for (final Iterator<State> stateIt = theAutomaton.stateIterator();
        stateIt.hasNext(); )
        {
            final State currState = stateIt.next();
            if (currState.getCost() == State.MAX_COST)
            {
                stateList.add(currState);
            }
        }
        // Remove max cost states
        for (final Iterator<State> stateIt = stateList.iterator(); stateIt.hasNext(); )
        {
            final State currState = stateIt.next();
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
    @Override
    public void requestAbort()
    {
        abortRequested = true;
        logger.debug("AutomatonSynthesizer requested to stop.");
    }

    @Override
    public boolean isAborting()
    {
        return abortRequested;
    }

    @Override
    public void resetAbort(){
      abortRequested = false;
    }
}
