
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


public class AutomatonToDsx
	implements AutomataSerializer
{

	private Automaton aut;

	public AutomatonToDsx(Automaton aut)
	{
		this.aut = aut;
	}

	public void serialize(PrintWriter pw)
		throws Exception
	{

		pw.println("STATESPACE;");
		pw.println("number of states: " + aut.nbrOfStates());
		pw.println("number of events: " + aut.nbrOfEvents());

		// Print all states
		Iterator states = aut.stateIterator();

		while (states.hasNext())
		{
			State state = (State) states.next();

			pw.print(state.getName());

			if (states.hasNext())
			{
				pw.print(", ");
			}
			else
			{
				pw.println(":");
			}
		}

		// Print all events
		Iterator events = aut.eventIterator();

		while (events.hasNext())
		{
			Event event = (Event) events.next();

			if (!event.isControllable())
			{
				pw.print("!");
			}

			if (!event.isPrioritized())
			{
				pw.print("?");
			}

			pw.print(event.getLabel());

			if (events.hasNext())
			{
				pw.print(", ");
			}
			else
			{
				pw.println(":");
			}
		}

		// Print all transitions
		states = aut.stateIterator();

		while (states.hasNext())
		{
			State sourceState = (State) states.next();

			pw.print(sourceState.getName());

			if (sourceState.isInitial())
			{
				pw.print(",i");
			}

			if (sourceState.isAccepting())
			{
				pw.print(",d");
			}

			if (sourceState.isForbidden())
			{
				pw.print(",x");
			}

			pw.print(":");

			Iterator outgoingArcs = sourceState.outgoingArcsIterator();

			while (outgoingArcs.hasNext())
			{
				Arc arc = (Arc) outgoingArcs.next();
				State destState = arc.getToState();

				pw.print(" " + destState.getName() + ":" + aut.getAlphabet().getEventWithId(arc.getEventId()).getLabel());
			}

			pw.println("");
		}

		pw.flush();
		pw.close();
	}

	public void serialize(String fileName)
		throws Exception
	{
		serialize(new PrintWriter(new FileWriter(fileName)));
	}
}
