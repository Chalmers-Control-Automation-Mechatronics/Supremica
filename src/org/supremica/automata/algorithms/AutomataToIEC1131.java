
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

import org.supremica.gui.*;
import org.supremica.log.*;
import org.supremica.automata.*;
import java.io.*;
import java.util.*;

public class AutomataToIEC1131
	//implements AutomataSerializer
{
	private static Logger logger = LoggerFactory.createLogger(AutomataToIEC1131.class);
	private Automata theAutomata;
	private SynchronizationOptions syncOptions;
	private AutomataSynchronizerHelper syncHelper;
	private SynchronizationType syncType;
	private Alphabet allEvents;

	public AutomataToIEC1131(Automata theAutomata, SynchronizationOptions syncOptions)
		throws Exception
	{
		this.theAutomata = theAutomata;
		this.syncOptions = syncOptions;
	}

	private void initialize()
		throws Exception
	{
		syncHelper = new AutomataSynchronizerHelper(theAutomata, syncOptions);
		allEvents = syncHelper.getUnionAlphabet();
	}

	/**
	 * Compute which events that are enabled
	 *
	 * The logic will be something like the following
	 *
	 * e_0 = (q_1_0 || q_1_1) && (q_2_3)
	 * e_1 = (q_1_2) && (q_2_1 || q_2_3)
	 *
	 * In Structured Text this will look like:
	 *
	 * e_0 := (q_1_0 OR q_1_1) AND (q_2_3);
	 * e_1 := (q_1_2) AND (q_2_1 OR q_2_3);
	 *
	 * In Instruction List this will look like:
	 *
	 * LD q_1_0
	 * OR q_1_1
	 * AND q_2_3
	 * ST e_0
	 * LD q_1_2
	 * AND( q_2_1
	 * OR q_2_3
	 * )
	 * ST e_1
	 */

	void printComputeEnabledEventsStructuredText(PrintWriter pw)
		throws Exception
	{
		// Iterate over all events and compute which events that are enabled
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			while (alphIt.hasNext())
			{
				LabeledEvent currEvent = (LabeledEvent)alphIt.next();
				int currEventIndex = currEvent.getSynchIndex();
				pw.println("(* Enable condition for event \"" + currEvent.getLabel() + "\"*)");
				boolean previousCondition = false;
				for (Iterator autIt = theAutomata.iterator(); autIt.hasNext();)
				{
					Automaton currAutomaton = (Automaton)autIt.next();

					if (syncType == SynchronizationType.Prioritized)
					{ // All automata that has this event as prioritized must be able to execute it
						if (currAutomaton.isEventPrioritized(currEvent.getLabel()))
						{ // Find all states that enables this event
						  // Use OR between states in the same automaton.
						  // Use AND between states in different automata.
							for (Iterator stateIt = currAutomaton.statesThatEnableEventIterator(currEvent.getLabel()); stateIt.hasNext();)
							{
								State currState = (State)stateIt.next();

							}
						}
					}
					else if (syncType == SynchronizationType.Full)
					{ // All automata that has this event in it's alphabet must be able to execute it
						if (currAutomaton.hasEventInAlphabet(currEvent.getLabel()))
						{ // Find all states that enables this event
						  // Use OR between states in the same automaton.
						  // Use AND between states in different automata.
						}
					}
					else if (syncType == SynchronizationType.Broadcast)
					{ // The event must be enabled in at least one automaton
						if (currAutomaton.isEventPrioritized(currEvent.getLabel()))
						{ // Find all states that enables this event
						  // Use OR between states in the same automaton.
						  // Use OR between states in different automata.
						  // FALSE OR ... for the case when no automaton enables this event
						}
					}
					else
					{
						throw new Exception("Unknown SynchronizationType");
					}
				}
			}
		}
	}

	/**
	 * If self loop then do not do anything, else update state
	 *
	 * The logic will be something like the following
	 * if (e_0)
	 * {
	 * 		(* Automaton 1 *)
	 * 		if (q_1_0)
	 * 		{
	 *			q_1_1 = true;
	 *			q_1_0 = false;
	 * 		}
	 * 		else if (q_1_2)
	 * 		{
	 *			q_1_0 = true;
	 *			q_1_2 = false;
	 * 		}
	 *
	 *		(* Automaton 2 *)
	 *
	 * }
	 * if (e_1)
	 * {
	 * 		(* Automaton 1 *)
	 * 		if (q_1_0)
	 * 		{
	 *			q_1_2 = true;
	 *			q_1_0 = false;
	 * 		}
	 *
	 * 		(* Automaton 2 *)
	 * 		if (q_2_0)
	 * 		{
	 *			q_2_2 = true;
	 *			q_2_0 = false;
	 * 		}
	 * }
	 *
	 * In Structured Text this will look like:
	 *
	 *
	 * IF (e_0)
	 * THEN
	 * 		IF (q_1_0)
	 * 		THEN
	 *			q_1_1 := TRUE;
	 *			q_1_0 := FALSE;
	 * 		ELSIF (q_1_2)
	 *		THEN
	 *			q_1_0 := TRUE;
	 *			q_1_2 := FALSE;
	 * 		END_IF;
	 * END_IF;
	 * IF (e_1)
	 * THEN
	 * 		...
	 * END_IF;
	 *
	 * In Instruction List this will look like:
	 *
	 * e_0:			LD e_0
	 *				JMPCN e_1
	 * e_0_q_1_0:	LD q_1_0
	 *				JMPCN q_1_2_e_1_0
	 *				S q_1_1 (* Note that the result register is true here *)
	 *				R q_1_2
	 *				JMP e_1
	 * e_1_q_1_2:	LD q_1_2
	 *				JMPCN end_of_e_0
	 *				S q_1_0
	 *				R q_1_2
	 *				JMP e_1
	 * e_1:			LD e_1
	 * end_of_jumps:
	 */

/*
	void printChangeStateTransitionsAsST(PrintWriter pw)
	{

	}

	void printChangeStatesTransitionsAsIL(PrintWriter pw)
	{

	}

	public void serialize_StructuredText(PrintWriter pw)
	{
		this.pw = pw;
		initialize();
		printComputeEnabledEventsStructuredText(pw);
	}

	public void serialize_InstructionList(PrintWriter pw)
	{
		this.pw = pw;
		initialize();
		printComputeEnabledEventsInstructionList(pw);
	}
*/
}
