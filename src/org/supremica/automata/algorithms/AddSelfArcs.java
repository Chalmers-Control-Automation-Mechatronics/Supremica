
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



import java.util.*;

import java.io.*;

import org.supremica.automata.*;


public class AddSelfArcs
{

	private AddSelfArcs() {}

	public static void execute(Automaton theAutomaton)
		throws Exception
	{
		execute(theAutomaton, false);
	}

	/**
	 *
	 *
	 *
	 */
	public static void execute(Automaton theAutomaton, boolean expandedSystem)
		throws Exception
	{

		Alphabet theAlphabet = theAutomaton.getAlphabet();
		Event passEvent = null;
		String passEventId = null;

		if (expandedSystem)
		{
			try
			{
				passEvent = theAlphabet.getEventWithLabel("pass");
			}
			catch (Exception ex) {}

			if (passEvent != null)
			{
				theAlphabet.removeEvent(passEvent);

				passEventId = passEvent.getId();
			}
		}

		// Handle all states with an pass event first.
		// This to make sure that the state after the pass event is not modified.
		if (expandedSystem && (passEvent != null))
		{
			Iterator stateIt = theAutomaton.stateIterator();

			while (stateIt.hasNext())
			{
				State currState = (State) stateIt.next();

				if (containsPassEvent(currState, theAlphabet, passEventId))
				{
					doPassState(currState, theAutomaton, passEventId);
				}
			}

			stateIt = theAutomaton.stateIterator();

			while (stateIt.hasNext())
			{
				State currState = (State) stateIt.next();

				if (!containsPassEvent(currState, theAlphabet, passEventId))
				{
					doState(currState, theAutomaton, passEventId);
				}
			}
		}
		else
		{		// The standard case
			Iterator stateIt = theAutomaton.stateIterator();

			while (stateIt.hasNext())
			{
				State currState = (State) stateIt.next();

				doState(currState, theAutomaton, passEventId);
			}
		}

		if (passEvent != null)
		{
			theAlphabet.addEvent(passEvent);
		}
	}

	private static void doState(State currState, Automaton theAutomaton, String passEventId)
		throws Exception
	{

		Alphabet currAlphabet = new Alphabet(theAutomaton.getAlphabet());

		// Remove all events that is possible from the current state
		Iterator arcIt = currState.outgoingArcsIterator();

		while (arcIt.hasNext())
		{
			Arc currArc = (Arc) arcIt.next();
			String currEventId = currArc.getEventId();

			if ((passEventId == null) ||!passEventId.equals(currEventId))
			{
				Event currEvent = currAlphabet.getEventWithId(currEventId);

				currAlphabet.removeEvent(currEvent);
			}
		}

		// Add all remaining events as self loop to the current state
		Iterator eventIt = currAlphabet.iterator();

		while (eventIt.hasNext())
		{
			Event currEvent = (Event) eventIt.next();
			Arc currArc = new Arc(currState, currState, currEvent.getId());

			theAutomaton.addArc(currArc);
		}
	}

	private static void doPassState(State currState, Automaton theAutomaton, String passEventId)
		throws Exception
	{

		Alphabet currAlphabet = new Alphabet(theAutomaton.getAlphabet());

		// Remove all events that is possible from the current state
		Iterator arcIt = currState.outgoingArcsIterator();

		while (arcIt.hasNext())
		{
			Arc currArc = (Arc) arcIt.next();
			String currEventId = currArc.getEventId();

			if ((passEventId == null) ||!passEventId.equals(currEventId))
			{
				Event currEvent = currAlphabet.getEventWithId(currEventId);

				if (currEvent.getLabel().equals("pass"))
				{
					State toState = currArc.getToState();
					Iterator passArcIt = toState.outgoingArcsIterator();

					while (arcIt.hasNext())
					{
						Arc currPassArc = (Arc) passArcIt.next();
						String currPassEventId = currPassArc.getEventId();
						Event currPassEvent = currAlphabet.getEventWithId(currEventId);

						currAlphabet.removeEvent(currPassEvent);
					}
				}
				else
				{
					currAlphabet.removeEvent(currEvent);
				}
			}
		}

		// Add all remaining events as self loop to the current state
		Iterator eventIt = currAlphabet.iterator();

		while (eventIt.hasNext())
		{
			Event currEvent = (Event) eventIt.next();
			Arc currArc = new Arc(currState, currState, currEvent.getId());

			theAutomaton.addArc(currArc);
		}
	}

	private static boolean containsPassEvent(State theState, Alphabet currAlphabet, String passEventId)
		throws Exception
	{

		if (passEventId == null)
		{
			return false;
		}

		Iterator arcIt = theState.outgoingArcsIterator();

		while (arcIt.hasNext())
		{
			Arc currArc = (Arc) arcIt.next();
			String currEventId = currArc.getEventId();

			if ((passEventId == null) ||!passEventId.equals(currEventId))
			{
				return true;
			}
		}

		return false;
	}
}
