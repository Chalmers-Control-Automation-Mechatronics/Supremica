
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

public class AutomatonComplement
{
	private Automaton theAutomaton;

	public AutomatonComplement(Automaton theAutomaton)
	{
		this.theAutomaton = new Automaton(theAutomaton);
	}

	public Automaton execute()
	{
		Alphabet alphabet = theAutomaton.getAlphabet();
		State dumpState = null;
		Iterator stateIterator = theAutomaton.safeStateIterator();

		while (stateIterator.hasNext())
		{
			State currState = (State) stateIterator.next();

			// Invert marking
			if (currState.isAccepting())
			{
				currState.setAccepting(false);
			}
			else
			{
				currState.setAccepting(true);
			}

			// Add arcs for all events that are not currently outgoing from the current state.
			// Those arcs reach the dump state
			Iterator eventIterator = alphabet.iterator();

			while (eventIterator.hasNext())
			{
				boolean found = false;
				LabeledEvent currEvent = (LabeledEvent) eventIterator.next();
				Iterator outgoingArcsIterator = currState.outgoingArcsIterator();

				while (outgoingArcsIterator.hasNext())
				{
					Arc currArc = (Arc) outgoingArcsIterator.next();
					if (currEvent.equals(currArc.getEvent())) // equalId(((Arc) outgoingArcsIterator.next()).getEventId()))
					{
						found = true;

						break;
					}
				}

				if (!found)
				{
					if (dumpState == null)
					{
						dumpState = theAutomaton.createAndAddUniqueState("dump");
					}
					// theAutomaton.addArc(new Arc(currState, dumpState, currEvent.getId()));
					theAutomaton.addArc(new Arc(currState, dumpState, currEvent));
				}
			}
		}

		// If complementation has been done, add the state, mark it as accepting and add self loops to the dump state...
		if(dumpState != null)
		{
			dumpState.setAccepting(true);

			Iterator eventIterator = alphabet.iterator();

			while (eventIterator.hasNext())
			{
				// theAutomaton.addArc(new Arc(dumpState, dumpState, ((LabeledEvent) eventIterator.next()).getId()));
				theAutomaton.addArc(new Arc(dumpState, dumpState, ((LabeledEvent) eventIterator.next())));
			}
		}

		return theAutomaton;
	}
}
