
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
import java.util.*;

/**
 * A monolithic synthesizer that can handle non-blocking and controllability problems.
 */
public class AutomatonSynthesizer
{
	private Automaton theAutomaton;
	private LinkedList acceptingStates = new LinkedList();
	private Gui workbench;
	private SynthesizerOptions synthesizerOptions;
	private static final boolean debugMode = false;

	public AutomatonSynthesizer(Gui workbench, Automaton theAutomaton, SynthesizerOptions synthesizerOptions)
		throws Exception
	{
		if (synthesizerOptions.getSynthesisType() == SynthesisType.Unknown)
		{
			throw new Exception("Invalid synthesis type: " + SynthesisType.Unknown.toString());
		}

		this.workbench = workbench;
		this.theAutomaton = theAutomaton;
		this.synthesizerOptions = synthesizerOptions;
	}

	// Synthesize a monolithic supervisor
	public void synthesize()
		throws Exception
	{
		theAutomaton.beginTransaction();

		SynthesisType synthesisType = synthesizerOptions.getSynthesisType();

		if (synthesisType == SynthesisType.Controllable)
		{
			synthesizeControllable();
		}
		else if (synthesisType == SynthesisType.Nonblocking)
		{
			synthesizeNonblocking();
		}
		else if (synthesisType == SynthesisType.Both)
		{
			synthesizeControllableNonblocking();
		}

		if (synthesizerOptions.doPurge())
		{
			purge();
		}

		theAutomaton.invalidate();
		theAutomaton.endTransaction();
	}

	// Synthesize a controllable and nonblocking supervisor
	private void synthesizeControllableNonblocking()
		throws Exception
	{
		boolean newUnsafeStates;
		LinkedList stateList = new LinkedList();
		Iterator stateIt = theAutomaton.stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.isAccepting() &&!currState.isForbidden())
			{
				acceptingStates.addLast(currState);
				currState.setCost(State.MIN_COST);
			}

			if (currState.isForbidden())
			{
				stateList.addLast(currState);
				currState.setCost(State.MAX_COST);
			}
		}

		// Do fixed point iteration
		doControllable(stateList);

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

		// Set MIN_COST to all safe states
		// Forbid the rest
		stateIt = theAutomaton.stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.getCost() != State.MAX_COST)
			{
				currState.setCost(State.MIN_COST);
			}
			else
			{
				currState.setForbidden(true);
			}
		}

		theAutomaton.setType(AutomatonType.Supervisor);
	}

	// Synthesize a controllable supervisor
	private void synthesizeControllable()
		throws Exception
	{
		boolean newUnsafeStates;
		LinkedList stateList = new LinkedList();
		Iterator stateIt = theAutomaton.stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.isAccepting() &&!currState.isForbidden())
			{
				acceptingStates.addLast(currState);
				currState.setCost(State.MIN_COST);
			}

			if (currState.isForbidden())
			{
				stateList.addLast(currState);
				currState.setCost(State.MAX_COST);
			}
		}

		// Do fixed point iteration
		doControllable(stateList);

		/*
		 * do
		 * {
		 *       stateList = doCoreachable();
		 *       newUnsafeStates = stateList.size() > 0;
		 *       if (newUnsafeStates)
		 *       {
		 *               newUnsafeStates = doControllable(stateList);
		 *       }
		 * } while (newUnsafeStates);
		 *
		 */
		doReachable();

		// Set MIN_COST to all safe states
		// Forbid the rest
		stateIt = theAutomaton.stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.getCost() != State.MAX_COST)
			{
				currState.setCost(State.MIN_COST);
			}
			else
			{
				currState.setForbidden(true);
			}
		}

		theAutomaton.setType(AutomatonType.Supervisor);
	}

	private void synthesizeNonblocking()
		throws Exception
	{
		boolean newUnsafeStates;
		LinkedList stateList = new LinkedList();

		do
		{
			stateList = doCoreachable();
			newUnsafeStates = stateList.size() > 0;
		}
		while (newUnsafeStates);

		doReachable();

		// Set MIN_COST to all safe states
		// Forbid the rest
		Iterator stateIt = theAutomaton.stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.getCost() != State.MAX_COST)
			{
				currState.setCost(State.MIN_COST);
			}
			else
			{
				currState.setForbidden(true);
			}
		}

		theAutomaton.setType(AutomatonType.Supervisor);
	}

	private LinkedList doCoreachable()
		throws Exception
	{
		if (debugMode)
		{
			System.err.println("doCoreachable");
		}

		theAutomaton.clearVisitedStates();

		// Push all marked states on the stack
		// Mark the state as visited
		LinkedList stateStack = new LinkedList(acceptingStates);

		if (debugMode)
		{
			System.err.println(stateStack.size() + " nbr of accepting states");
		}

		// Do propagate coreachability
		while (stateStack.size() > 0)
		{
			State currState = (State) stateStack.removeLast();

			currState.setVisited(true);

			Iterator arcIt = currState.incomingArcsIterator();

			while (arcIt.hasNext())
			{
				Arc currArc = (Arc) arcIt.next();
				Event currEvent = theAutomaton.getEvent(currArc.getEventId());
				State fromState = currArc.getFromState();

				if ((fromState.getCost() != State.MAX_COST) &&!fromState.isVisited())
				{
					fromState.setVisited(true);
					stateStack.addLast(fromState);
				}
			}
		}

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

		if (debugMode)
		{
			System.err.println("found " + nbrOfNewUnsafeStates + " new blocking states");
		}

		return stateStack;
	}

	private boolean doControllable(LinkedList stateStack)
		throws Exception
	{
		if (debugMode)
		{
			System.err.println("doControllable");
		}

		boolean newUnsafeStates = false;
		int nbrOfNewUnsafeStates = 0;

		// Do propagate uncontrollability
		while (stateStack.size() > 0)
		{
			State currState = (State) stateStack.removeLast();
			Iterator arcIt = currState.incomingArcsIterator();

			while (arcIt.hasNext())
			{
				Arc currArc = (Arc) arcIt.next();
				Event currEvent = theAutomaton.getEvent(currArc.getEventId());

				if (!currEvent.isControllable())
				{
					State fromState = currArc.getFromState();

					if (fromState.getCost() != State.MAX_COST)
					{
						nbrOfNewUnsafeStates++;

						newUnsafeStates = true;

						fromState.setCost(State.MAX_COST);
						stateStack.addLast(fromState);

						if (fromState.isAccepting())
						{
							acceptingStates.remove(fromState);
						}
					}
				}
			}
		}

		if (debugMode)
		{
			System.err.println("found " + nbrOfNewUnsafeStates + " new uncontrollable states");
		}

		return newUnsafeStates;
	}

	private void doReachable()
	{
		if (debugMode)
		{
			System.err.println("doReachable");
		}

		theAutomaton.clearVisitedStates();

		// Push all marked states on the stack
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

	private void purge()
	{
		LinkedList stateList = new LinkedList();
		Iterator stateIt = theAutomaton.stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.getCost() == State.MAX_COST)
			{
				stateList.addLast(currState);
			}
		}

		stateIt = stateList.iterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			theAutomaton.removeState(currState);
		}

		stateList.clear();
	}
}
