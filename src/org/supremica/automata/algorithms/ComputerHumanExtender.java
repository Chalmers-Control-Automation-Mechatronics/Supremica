
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
import java.util.Iterator;

public class ComputerHumanExtender
{
	protected static Logger logger = LoggerFactory.createLogger(ComputerHumanExtender.class);
	protected Alphabet theAlphabet;
	protected Alphabet operatorResetEvents;
	protected Alphabet operatorIncreaseEvents;
	protected Automaton newAutomaton;
	protected int k = 1;
	protected int m = 1;

	public static final LabeledEvent passEvent = new LabeledEvent("pass");

	public ComputerHumanExtender(Automata theAutomata, int k, int m)
	{
		constructor(theAutomata, k, m);
	}

	public ComputerHumanExtender(Alphabet theAlphabet, int k, int m)
	{
		constructor(theAlphabet, k, m);
	}

	private void constructor(Automata theAutomata, int k, int m)
	{
		if (k < 1)
		{
			throw new IllegalArgumentException("k must be >= 1");
		}
		if (m < 1)
		{
			throw new IllegalArgumentException("m must be >= 1");
		}
		try
		{
			Alphabet inputAlphabet = AlphabetHelpers.getUnionAlphabet(theAutomata, true, true);

			constructor(inputAlphabet, k, m);
		}
		catch (Exception e)
		{
			logger.debug(e);

			throw new IllegalStateException("Alphabets not consistent");
		}
	}

	private void constructor(Alphabet theAlphabet, int k, int m)
	{
		this.theAlphabet = theAlphabet;
		this.k = k;
		this.m = m;

		buildAlphabets(theAlphabet);
	}

	protected void buildAlphabets(Alphabet theAlphabet)
	{

		// The best way to deal with the inherently uncontrollable events
		// is to not include them in the alphabet of this automaton at all
		operatorResetEvents = new Alphabet();
		operatorIncreaseEvents = new Alphabet();

		for (Iterator<LabeledEvent> evIt = theAlphabet.iterator(); evIt.hasNext(); )
		{
			LabeledEvent currEvent = evIt.next();

			if (currEvent.isImmediate())
			{
				throw new IllegalStateException("All events must be observable and non immediate");
			}

			LabeledEvent newEvent = new LabeledEvent(currEvent);

			if (currEvent.isOperatorReset())
			{
				operatorResetEvents.addEvent(newEvent);
			}
			else
			{    // The event is uncontrollable
				if (currEvent.isOperatorIncrease())
				{

					//System.err.println("found operator event");
					operatorIncreaseEvents.addEvent(newEvent);
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

	public void setM(int m)
	{
		this.m = m;
	}

	public int getM()
	{
		return m;
	}

	public void execute()
		throws Exception
	{
		if (k < 1)
		{
			throw new IllegalStateException("k must be >= 1");
		}
		if (m < 1)
		{
			throw new IllegalStateException("m must be >= 1");
		}

		newAutomaton = new Automaton();

		newAutomaton.setName("E^{k=" + k + ", m=" + m +"}");
		newAutomaton.setType(AutomatonType.Plant);

		Alphabet newAutAlphabet = newAutomaton.getAlphabet();

		newAutAlphabet.addEvents(operatorResetEvents);
		newAutAlphabet.addEvents(operatorIncreaseEvents);

//		LabeledEvent passEvent = new LabeledEvent("pass");

		newAutAlphabet.addEvent(ComputerHumanExtender.passEvent);

		State initialState = newAutomaton.createUniqueState("qe_0_1");

		initialState.setInitial(true);
		initialState.setAccepting(true);
		newAutomaton.addState(initialState);

/*
		// Add all reset events (controllable events) as self loops to the initial state
		for (EventIterator evIt = operatorResetEvents.iterator();
				evIt.hasNext(); )
		{
			LabeledEvent currEvent = evIt.nextEvent();
			Arc newArc = new Arc(initialState, initialState, currEvent);

			newAutomaton.addArc(newArc);
		}
*/
		State prevUpperState = null;

		State[] higherMStates = new State[k+1];

		// Create E^{k,m} backwards
		for (int im = m; im >= 1; im--)
		{
			// logger.info("im: " + im);

			for (int ik = k; ik >= 0; ik--)
			{
				//logger.info("ik: " + ik);

				State newState = null;

				if (!(ik == 0 && im == 1))
				{
					// logger.info("creating state ik: " + ik + " im: " + im);
					newState = newAutomaton.createAndAddUniqueState("qe_" + ik + "_" + im);

					newState.setAccepting(true);
				}
				else
				{
					newState = initialState;
				}

/*
				// Add all controllable events to the initial state
				for (EventIterator evIt = operatorResetEvents.iterator();
						evIt.hasNext(); )
				{
					LabeledEvent currEvent = evIt.nextEvent();
					Arc newArc = new Arc(newState, initialState, currEvent);

					newAutomaton.addArc(newArc);
				}
*/

				//
				// First add the operator increase events
				//

				if (ik < k)
				{
					// Add all operator to the next upper level (in this case the current level)
					for (Iterator<LabeledEvent> evIt = operatorIncreaseEvents.iterator();
							evIt.hasNext(); )
					{

						// System.err.println("added operator arc");
						LabeledEvent currEvent = evIt.next();
						Arc newArc = new Arc(newState, prevUpperState, currEvent);

						newAutomaton.addArc(newArc);
					}

					if (ik == k-1)
					{
						// Add the pass event between level k and k-1
						Arc newArc = new Arc(prevUpperState, newState, passEvent);

						newAutomaton.addArc(newArc);

					}
				}
				if (ik == k)
				{    // The top level state

					newState.setAccepting(true);
					//newState.setAccepting(false);
				}

				//
				// Then add the operator reset events
				//

				if (im < m)
				{
					State higherMState = higherMStates[ik];

					// Add all operator reset events to the corresponding level in m+1
					for (Iterator<LabeledEvent> evIt = operatorResetEvents.iterator();
							evIt.hasNext(); )
					{
						// System.err.println("added operator arc");
						LabeledEvent currEvent = evIt.next();
						Arc newArc = new Arc(newState, higherMState, currEvent);

						newAutomaton.addArc(newArc);
					}
				}
				else if (im == m)
				{
					// Add all operator reset events to the initial state
					for (Iterator<LabeledEvent> evIt = operatorResetEvents.iterator();
							evIt.hasNext(); )
					{
						LabeledEvent currEvent = evIt.next();
						Arc newArc = new Arc(newState, initialState, currEvent);

						newAutomaton.addArc(newArc);
					}
				}

				higherMStates[ik] = newState;
				prevUpperState = newState;

			}
		}

	}

	public Automaton getNewAutomaton()
	{
		return newAutomaton;
	}
}
