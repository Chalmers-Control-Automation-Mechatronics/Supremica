
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
package org.supremica.automata.IO;

import java.io.*;
import java.util.*;
import org.supremica.automata.*;

public class AutomatonToFSM
	implements AutomataSerializer
{
	protected Automaton aut;
	protected Alphabet eventsNotOnTransitions;

	public AutomatonToFSM(Automaton aut)
	{
		this.aut = aut;
		eventsNotOnTransitions = new Alphabet(aut.getAlphabet());
	}

	public void serialize(PrintWriter pw)
		throws IOException
	{

		//System.err.println("Org Alph size: " + aut.getAlphabet().size());
		//System.err.println("New Alph size: " + eventsNotOnTransitions.size());
		if (!aut.hasInitialState())
		{
			throw new IllegalStateException("The automaton does not have an initial state");
		}

		pw.println(aut.nbrOfStates());
		pw.println();

		State initialState = aut.getInitialState();

		// Print the initial state first
		serializeState(pw, initialState);

		for (Iterator states = aut.stateIterator(); states.hasNext(); )
		{
			State state = (State) states.next();

			if (state.isInitial())
			{
				if (state != initialState)
				{    // Check that we do not have multiple initial states
					throw new IllegalStateException("Multiple initial states are not allowed");
				}
			}
			else
			{
				serializeState(pw, state);
			}
		}

		serializeEventsNotOnTransitions(pw);
		pw.flush();
		pw.close();
	}

	protected void serializeState(PrintWriter pw, State state)
	{

		// Print the state information
		pw.println(state.getName() + "\t" + (state.isAccepting()
											 ? "1"
											 : "0") + "\t" + state.nbrOfOutgoingArcs());

		// Print all outgoing arcs
		for (Iterator<Arc> arcIt = state.outgoingArcsIterator();
				arcIt.hasNext(); )
		{
			Arc currArc = arcIt.next();
			State targetState = currArc.getToState();
			LabeledEvent currEvent = currArc.getEvent();
			String label = currEvent.getLabel();

			pw.println(label + "\t" + targetState.getName() + "\t" + (currEvent.isControllable()
																	  ? "c"
																	  : "uc") + "\t" + (currEvent.isObservable()
																						? "o"
																						: "uo"));

			if (eventsNotOnTransitions.contains(label))
			{
				eventsNotOnTransitions.removeEvent(label);
			}
		}

		// Print empty line
		pw.println();
	}

	protected void serializeEventsNotOnTransitions(PrintWriter pw)
	{
		if (eventsNotOnTransitions.size() > 0)
		{
			pw.println("EVENTS");

			for (Iterator<LabeledEvent> evIt = eventsNotOnTransitions.iterator();
					evIt.hasNext(); )
			{
				LabeledEvent currEvent = evIt.next();

				pw.println(currEvent.getLabel() + "\t" + (currEvent.isControllable()
														  ? "c"
														  : "uc") + "\t" + (currEvent.isObservable()
																			? "o"
																			: "uo"));
			}

			// Print empty line
			pw.println();
		}
	}

	public void serialize(String fileName)
		throws IOException
	{
		serialize(new PrintWriter(new FileWriter(fileName)));
	}

	public void serialize(File theFile)
		throws IOException
	{
		serialize(theFile.getAbsolutePath());
	}
}
