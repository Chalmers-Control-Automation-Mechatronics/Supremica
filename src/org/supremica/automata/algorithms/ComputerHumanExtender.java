
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

import org.supremica.automata.*;
import org.supremica.log.*;

public class ComputerHumanExtender
{
	protected static Logger logger = LoggerFactory.createLogger(ComputerHumanExtender.class);
	protected Alphabet theAlphabet;

	protected Alphabet controllableEvents;
	protected Alphabet operatorEvents;

	protected Automaton newAutomaton;
	protected int k = 1;

	public ComputerHumanExtender(Automata theAutomata, int k)
	{
		constructor(theAutomata, k);
	}

	public ComputerHumanExtender(Alphabet theAlphabet, int k)
	{
		constructor(theAlphabet, k);
	}

	private void constructor(Automata theAutomata, int k)
	{
		if (k < 0)
		{
			throw new IllegalArgumentException("k must be >= 0");
		}
		try
		{
			Alphabet inputAlphabet = AlphabetHelpers.getUnionAlphabet(theAutomata, true, true);
			constructor(inputAlphabet, k);
		}
		catch (Exception e)
		{
			logger.debug(e);
			throw new IllegalStateException("Alphabets not consistent");
		}
	}

	private void constructor(Alphabet theAlphabet, int k)
	{
		this.theAlphabet = theAlphabet;
		this.k = k;
		buildAlphabets(theAlphabet);
	}

	protected void buildAlphabets(Alphabet theAlphabet)
	{
		// The best way to deal with the inherently uncontrollable events
		// is to not include them in the alphabet of this automaton at all

		controllableEvents = new Alphabet();
		operatorEvents = new Alphabet();

		for (EventIterator evIt = theAlphabet.iterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = evIt.nextEvent();

			if (currEvent.isImmediate())
			{
				throw new IllegalStateException("All events must be observable and non immediate");
			}

			LabeledEvent newEvent = new LabeledEvent(currEvent);

			if (currEvent.isControllable())
			{
				controllableEvents.addEvent(newEvent);
			}
			else
			{ // The event is uncontrollable
				if (currEvent.isOperator())
				{
					//System.err.println("found operator event");
					operatorEvents.addEvent(newEvent);
				}
				// do nothing for the inherently uncontrollable events
			}
		}


	}

	public void setK(int k)
	{
		this.k = k;
	}

	public int getK()
	{
		return k;
	}

	public void execute()
		throws Exception
	{
		newAutomaton = new Automaton();
		newAutomaton.setName("Lifting, k = " + k);
		newAutomaton.setType(AutomatonType.Plant);

		Alphabet newAutAlphabet = newAutomaton.getAlphabet();
		newAutAlphabet.addEvents(controllableEvents);
		newAutAlphabet.addEvents(operatorEvents);
		LabeledEvent passEvent = new LabeledEvent("pass", "3.1415926");
		newAutAlphabet.addEvent(passEvent);

		State initialState = newAutomaton.createUniqueState("qi");
		initialState.setInitial(true);
		initialState.setAccepting(true);
		newAutomaton.addState(initialState);

		// Add all controllable events as self loops to the initial state
		for (EventIterator evIt = controllableEvents.iterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = evIt.nextEvent();
			Arc newArc = new Arc(initialState, initialState, currEvent);
			newAutomaton.addArc(newArc);
		}

		State prevState = initialState;
		for (int i = 1; i <= k; i++)
		{
			State newState = newAutomaton.createAndAddUniqueState("q_" + i);
			newState.setAccepting(true);

			// Add all controllable events to the initial state
			for (EventIterator evIt = controllableEvents.iterator(); evIt.hasNext(); )
			{
				LabeledEvent currEvent = evIt.nextEvent();
				Arc newArc = new Arc(newState, initialState, currEvent);
				newAutomaton.addArc(newArc);
			}

			// Add all operator to the next upper level (in this case the current level)
			for (EventIterator evIt = operatorEvents.iterator(); evIt.hasNext(); )
			{
				// System.err.println("added operator arc");
				LabeledEvent currEvent = evIt.nextEvent();
				Arc newArc = new Arc(prevState, newState, currEvent);
				newAutomaton.addArc(newArc);
			}

			if (i == k)
			{ // The top level state
			  // Add the pass event to the lower level
				Arc newArc = new Arc(newState, prevState, passEvent);
				newAutomaton.addArc(newArc);
				newState.setAccepting(false);
			}

			prevState = newState;
		}

	}

	public Automaton getNewAutomaton()
	{
		return newAutomaton;
	}
}
