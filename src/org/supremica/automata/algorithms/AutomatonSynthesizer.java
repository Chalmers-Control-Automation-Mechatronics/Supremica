
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
import org.supremica.log.*;
import java.util.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.standard.ObserverBuilder;

/**
 * A monolithic synthesizer that can handle non-blocking and controllability problems.
 *
 *@author  ka
 *@created  November 28, 2000
 */
public class AutomatonSynthesizer
	implements Stoppable
{
	protected static Logger logger = LoggerFactory.createLogger(AutomatonSynthesizer.class);
	protected Automaton theAutomaton;
	protected LinkedList acceptingStates = new LinkedList();
	protected SynthesizerOptions synthesizerOptions;
	protected boolean rememberDisabledEvents = false;
	protected Alphabet disabledEvents;
	protected final static boolean debugMode = false;
	protected boolean forcedPurge = false;

	private boolean stopRequested = false;

	/**
	 * theAutomaton will be changed!
	 */
	public AutomatonSynthesizer(Automaton theAutomaton, SynthesizerOptions synthesizerOptions)
		throws Exception
	{
		if (synthesizerOptions.getSynthesisType() == SynthesisType.Unknown)
		{
			throw new SupremicaException("Invalid synthesis type: " + SynthesisType.Unknown.toString());
		}

		this.theAutomaton = theAutomaton;
		this.synthesizerOptions = synthesizerOptions;
	}

	// Synthesize a monolithic supervisor
	public boolean synthesize()
		throws Exception
	{
		logger.debug("AutomatonSynthesizer::synthesize()");
		theAutomaton.beginTransaction();

		SynthesisType synthesisType = synthesizerOptions.getSynthesisType();
		boolean didSomething = false;    // records whether we actually did anything

		if (synthesisType == SynthesisType.Controllable)
		{
			didSomething = synthesizeControllable();
		}
		else if (synthesisType == SynthesisType.Nonblocking)
		{
			didSomething = synthesizeNonblocking();
		}
		else if (synthesisType == SynthesisType.Both)
		{
			didSomething = synthesizeControllableNonblocking();
		}
		else if (synthesisType == SynthesisType.Observable)
		{
			didSomething = synthesizeControllableNonblockingObservable();
		}

		if (synthesizerOptions.doRememberDisabledEvents())
		{
			computeDisabledEvents();
		}

		if (synthesizerOptions.doPurge() || forcedPurge)
		{
			purge();
		}

		theAutomaton.invalidate();
		theAutomaton.endTransaction();

		theAutomaton.setComment("sup(" + theAutomaton.getName() + ")");
		theAutomaton.setName(null);

		return didSomething;
	}

	public void initializeAcceptingStates()
	{
		Iterator stateIt = theAutomaton.stateIterator();

		logger.debug("AutomatonSynthesizer::initializeAcceptingStates");

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.isAccepting() &&!currState.isForbidden())
			{
				acceptingStates.addLast(currState);
			}
		}
	}

	// Synthesize a controllable, nonblocking and observable supervisor
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

			currObserver.setAllStatesAsAccepting(true);
			currObserver.setName("Observer");
			logger.info("Number of states in observer: " + currObserver.nbrOfStates() + " nbr forb states: " + currObserver.nbrOfForbiddenStates());

			Automata observerAndSupervisor = new Automata();

			observerAndSupervisor.addAutomaton(currObserver);
			observerAndSupervisor.addAutomaton(theAutomaton);

			// observerAndSupervisor.setIndicies();
			SynchronizationOptions observerSynchOptions = new SynchronizationOptions();

			observerSynchOptions.setSynchronizationType(SynchronizationType.Full);

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

	// Synthesize a controllable and nonblocking supervisor
	protected boolean synthesizeControllableNonblocking()
		throws Exception
	{
		LinkedList stateList = new LinkedList();

		logger.debug("AutomatonSynthesizer::synthesizeControllableNonblocking");

		for (Iterator stateIt = theAutomaton.stateIterator();
				stateIt.hasNext(); )
		{
			State currState = (State) stateIt.next();

			if (currState.isAccepting() &&!currState.isForbidden())
			{
				acceptingStates.addLast(currState);
			}

			if (currState.isForbidden())
			{
				stateList.addLast(currState);
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

		doReachable();

		// Forbid the states with MAX_COST set
		boolean didSomething = false;
		for (StateIterator stateIt = theAutomaton.stateIterator(); stateIt.hasNext(); )
		{
			State currState = (State) stateIt.next();

			if (currState.getCost() == State.MAX_COST)
			{
				currState.setForbidden(true);

				didSomething = true;
			}
		}

		theAutomaton.setType(AutomatonType.Supervisor);

		return didSomething;
	}

	// Synthesize a controllable supervisor
	protected boolean synthesizeControllable()
		throws Exception
	{
		logger.debug("AutomatonSynthesizer::synthesizeControllable");

		// boolean newUnsafeStates;
		LinkedList stateList = new LinkedList();
		Iterator stateIt = theAutomaton.stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.isAccepting() &&!currState.isForbidden())
			{
				acceptingStates.addLast(currState);
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

		theAutomaton.setType(AutomatonType.Supervisor);

		return didSomething;
	}

	protected boolean synthesizeNonblocking()
		throws Exception
	{
		logger.debug("AutomatonSynthesizer::synthesizeNonblocking");

		boolean newUnsafeStates;
		LinkedList stateList = new LinkedList();

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

		theAutomaton.setType(AutomatonType.Supervisor);

		return didSomething;
	}

	public LinkedList doCoreachable()
		throws Exception
	{
		logger.debug("AutomatonSynthesizer::doCoreachable");
		theAutomaton.clearVisitedStates();

		// Push all marked states on the stack
		// Mark the state as visited
		if (acceptingStates.size() == 0)
		{
			// Not initialized? Do it.
			initializeAcceptingStates();
		}
		LinkedList stateStack = new LinkedList(acceptingStates);

		logger.debug(stateStack.size() + " nbr of accepting states");

		// Do propagate coreachability
		while (stateStack.size() > 0)
		{
			if (stopRequested)
			{
				return new LinkedList();
			}

			State currState = (State) stateStack.removeLast();
			currState.setVisited(true);

			Iterator arcIt = currState.incomingArcsIterator();
			while (arcIt.hasNext())
			{
				Arc currArc = (Arc) arcIt.next();
				LabeledEvent currEvent = currArc.getEvent();    // theAutomaton.getEvent(currArc.getEventId());
				State fromState = currArc.getFromState();

				if ((fromState.getCost() != State.MAX_COST) && !fromState.isVisited())
				{
					fromState.setVisited(true);
					stateStack.addLast(fromState);
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
				stateStack.addLast(currState);
			}
		}

		logger.debug("found " + nbrOfNewUnsafeStates + " new blocking states");

		return stateStack;    // return the set of non-coreachable states
	}

	// returns true if uncontrollable states found
	protected boolean doControllable(LinkedList stateStack)
		throws Exception
	{
		logger.debug("AutomatonSynthesizer::doControllable");

		boolean newUnsafeStates = false;
		int nbrOfNewUnsafeStates = 0;

		// Do propagate uncontrollability
		while (stateStack.size() > 0)
		{
			if (stopRequested)
			{
				return false;
			}

			State currState = (State) stateStack.removeLast();

/*
						Iterator arcIt = currState.incomingArcsIterator();

						while (arcIt.hasNext())
						{
								Arc currArc = (Arc) arcIt.next();
								LabeledEvent currEvent = currArc.getEvent(); // theAutomaton.getEvent(currArc.getEventId());

								if (!currEvent.isControllable())
								{
										State fromState = currArc.getFromState();       // backwards over this uc-event

										if (fromState.getCost() != State.MAX_COST)      // if not already forbidden, forbid it
										{
												nbrOfNewUnsafeStates++;

												newUnsafeStates = true;

												fromState.setCost(State.MAX_COST);
												stateStack.addLast(fromState);  // this makes it a fix-point calculation

												if (fromState.isAccepting())
												{
														acceptingStates.remove(fromState);
												}
										}
								}
						}
*/
			LinkedList newXstates = doControllable(currState);

			if (newXstates.size() != 0)
			{
				newUnsafeStates = true;
				nbrOfNewUnsafeStates += newXstates.size();

				stateStack.addAll(newXstates);    // could be optimized here - adding one linked list to another
			}
		}

		logger.debug("found " + nbrOfNewUnsafeStates + " new uncontrollable states");

		return newUnsafeStates;
	}

	// This one does for one state
	public LinkedList doControllable(State currState)
	{
		LinkedList stateStack = new LinkedList();
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
					stateStack.addLast(fromState);

					if (fromState.isAccepting())
					{
						acceptingStates.remove(fromState);
					}
				}
			}
		}

		return stateStack;
	}

	public void doReachable()
	{
		logger.debug("AutomatonSynthesizer::doReachable");
		theAutomaton.clearVisitedStates();

		// Push the initial state on the stack
		// Mark the state as visited
		State initialState = theAutomaton.getInitialState();
		LinkedList stateStack = new LinkedList();

		if (initialState.getCost() != State.MAX_COST)
		{
			stateStack.addLast(initialState);
		}

		if (debugMode)
		{
			System.err.println(stateStack.size() + " nbr of initial states");
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

			Iterator arcIt = currState.outgoingArcsIterator();

			while (arcIt.hasNext())
			{
				Arc currArc = (Arc) arcIt.next();
				State toState = currArc.getToState();

				if ((toState.getCost() != State.MAX_COST) &&!toState.isVisited())
				{
					toState.setVisited(true);
					stateStack.addLast(toState);
				}
			}
		}

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
	 * Returns the set of UNCONTROLLABLE events that needed to be disabled in the synthesis.
	 * If not rememberDisabledEvents are set to true then null is returned.
	 * This method might only be called after synthesize has returned.
	 */
	public Alphabet getDisabledEvents()
	{
		return disabledEvents;
	}

	protected void computeDisabledEvents()
	{
		disabledEvents = new Alphabet();

		for (Iterator stateIt = theAutomaton.stateIterator();
				stateIt.hasNext(); )
		{
			State currState = (State) stateIt.next();

			if (currState.getCost() == State.MAX_COST)
			{
				for (Iterator evIt = theAutomaton.incomingEventsIterator(currState);
						evIt.hasNext(); )
				{
					LabeledEvent currEvent = (LabeledEvent) evIt.next();

					if (!currEvent.isControllable())
					{
						try
						{
							if (!disabledEvents.contains(currEvent.getLabel()))
							{
								disabledEvents.addEvent(currEvent);
							}
						}
						catch (Exception ex)
						{
							logger.error("AutomatonSynthesizer::computeDisabledEvents: " + ex.getMessage());
							logger.debug(ex.getStackTrace());
						}
					}
				}
			}
		}
	}

	public void purge()
	{
		List stateList = new LinkedList();

		for (StateIterator stateIt = theAutomaton.stateIterator();
				stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			if (currState.getCost() == State.MAX_COST)
			{
				stateList.add(currState);
			}
		}

		for (Iterator stateIt = stateList.iterator(); stateIt.hasNext(); )
		{
			State currState = (State) stateIt.next();

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
	 * @see  ExecutionDialog
	 */
	public void requestStop()
	{
		stopRequested = true;

		logger.debug("AutomatonSynthesizer requested to stop.");
	}
}
