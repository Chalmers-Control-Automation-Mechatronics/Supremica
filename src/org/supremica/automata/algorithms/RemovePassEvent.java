
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

import java.util.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;

public class RemovePassEvent
{
	private RemovePassEvent() {}

	/**
	 * If the pass event is active from a state q leading to q', then
	 * all uncontrollable arcs from from q' are copied.
	 * The arc with the pass event is removed.
	 *
	 *@param  theAutomaton Description of the Parameter
	 *@exception  Exception Description of the Exception
	 */
	public static void execute(Automaton theAutomaton)
		throws Exception
	{
		Alphabet theAlphabet = theAutomaton.getAlphabet();
		Iterator stateIt = theAutomaton.stateIterator();

		while (stateIt.hasNext())
		{

			// Find a state with an outgoing pass event
			State currState = (State) stateIt.next();
			Iterator arcIt = currState.safeOutgoingArcsIterator();

			while (arcIt.hasNext())
			{
				Arc currArc = (Arc) arcIt.next();

				// String currEventId = currArc.getEventId();
				LabeledEvent currEvent = currArc.getEvent();    // theAlphabet.getEventWithId(currEventId);

				if (currEvent.equals(ComputerHumanExtender.passEvent))
				{

					// A state with outgoing pass event is found
					// Copy all uncontrollable arcs
					State currToState = currArc.getToState();
					Iterator nextArcIt = currToState.outgoingArcsIterator();

					while (nextArcIt.hasNext())
					{
						Arc nextArc = (Arc) nextArcIt.next();

						// String nextEventId = nextArc.getEventId();
						LabeledEvent nextEvent = nextArc.getEvent();    // theAlphabet.getEventWithId(nextEventId);

						if (!nextEvent.isControllable())
						{
							State nextToState = nextArc.getToState();

							// Arc newArc = new Arc(currState, nextToState, nextEventId);
							// WARNING, Red Flag, may be broken...
							Arc newArc = new Arc(currState, nextToState, nextEvent);
						}
					}

					currArc.clear();

					// Remove the pass event
					// We can do a break here?
				}
			}
		}

		if (theAlphabet.contains(ComputerHumanExtender.passEvent))
		{
			LabeledEvent passEvent = theAlphabet.getEvent(ComputerHumanExtender.passEvent);

			theAlphabet.removeEvent(passEvent);
		}
	}
}
