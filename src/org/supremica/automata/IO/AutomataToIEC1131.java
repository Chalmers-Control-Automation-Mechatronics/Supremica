
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
package org.supremica.automata.IO;

import java.io.*;
import java.util.*;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.SynchronizationType;
import org.supremica.automata.execution.*;

public class AutomataToIEC1131
	//implements AutomataSerializer
{
	private static Logger logger = LoggerFactory.createLogger(AutomataToIEC1131.class);
	private Project theProject;
	//private SynchronizationOptions syncOptions;
	//private AutomataSynchronizerHelper syncHelper;
	private SynchronizationType syncType = SynchronizationType.Prioritized;
	private Alphabet allEvents;
	private IEC61131Helper theHelper;

	public AutomataToIEC1131(Project theProject)
		throws Exception
	{
		this(theProject, IEC61131Helper.getInstance());
	}

	public AutomataToIEC1131(Project theProject, IEC61131Helper theHelper)
		throws Exception
	{
		this.theProject = theProject;
		this.theHelper = theHelper;
	}

	private void initialize()
	{
		allEvents = theProject.setIndicies();
	}


	void printBeginProgram(PrintWriter pw)
	{
		theHelper.printBeginProgram(pw, "AutomaticallyGeneratedProgram");
	}

	void printEndProgram(PrintWriter pw)
	{
		theHelper.printEndProgram(pw);
	}

	void printBeginVariables(PrintWriter pw)
	{
		theHelper.printBeginVariables(pw);
	}

	void printEndVariables(PrintWriter pw)
	{
		theHelper.printEndVariables(pw);
	}

	void printSignalVariables(PrintWriter pw)
	{
		// Input signals
		for (Iterator theIt = theProject.inputSignalsIterator(); theIt.hasNext();)
		{
			Signal currSignal = (Signal)theIt.next();
			int currPort = currSignal.getPort();
			theHelper.printBooleanInputVariableDeclaration(pw, "si_" + currPort, currPort, currSignal.getLabel());
		}
		// Output signals
		for (Iterator theIt = theProject.outputSignalsIterator(); theIt.hasNext();)
		{
			Signal currSignal = (Signal)theIt.next();
			int currPort = currSignal.getPort();
			theHelper.printBooleanOutputVariableDeclaration(pw, "so_" + currPort, currPort, currSignal.getLabel());
		}
	}

	void printEventVariables(PrintWriter pw)
	{
		// Iterate over all events and compute which events that are enabled
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent)alphIt.next();
			int currEventIndex = currEvent.getSynchIndex();
			theHelper.printBooleanVariableDeclaration(pw, "e_" + currEventIndex, currEvent.getLabel() + (currEvent.isControllable() ? " controllable" : " uncontrollable"));
		}
		theHelper.printBooleanVariableDeclaration(pw, "enabledEvent", "True if a event is enabled, false otherwise");

	}

	void printStateVariables(PrintWriter pw)
	{
		for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton)autIt.next();

			int currAutomatonIndex = currAutomaton.getSynchIndex();
			for (Iterator stateIt = currAutomaton.stateIterator(); stateIt.hasNext(); )
			{
				State currState = (State)stateIt.next();
				int currStateIndex = currState.getSynchIndex();
				theHelper.printBooleanVariableDeclaration(pw, "q_" + currAutomatonIndex + "_" + currStateIndex, currState.getName() + " in " + currAutomaton.getName());

			}
		}
		theHelper.printBooleanVariableDeclaration(pw, "initialized", "Set the inital state the first scan cycle");
	}

	void printTimerVariables(PrintWriter pw)
	{
		for (Iterator theIt = theProject.timerIterator(); theIt.hasNext();)
		{
			EventTimer currTimer = (EventTimer)theIt.next();
			theHelper.printTimerVariableDeclaration(pw, "timer_" + currTimer.getSynchIndex(), currTimer.getName());
		}
	}

	public void printILBegin(PrintWriter pw)
	{
		theHelper.printILBegin(pw);
	}

	public void printILEnd(PrintWriter pw)
	{
		theHelper.printILLabel(pw, "end");
		theHelper.printILEnd(pw);
	}

	public void printSTBegin(PrintWriter pw)
	{
		theHelper.printSTBegin(pw);
	}

	public void printSTEnd(PrintWriter pw)
	{
		theHelper.printSTEnd(pw);
	}

	/**
	 * Set the initial state the first time the code is executed.
	 *
	 * In Structured Text:
	 *
	 * IF (NOT initialized)
	 * THEN
	 *		q_1_0 := TRUE;
	 *		q_2_0 := TRUE;
	 *		initialized := TRUE;
	 * END_IF;
	 *
	 * In Instruction List
	 *
	 * 			LD initialized
	 *			JMPC after_initialization
	 *			LD TRUE
	 *			S q_1_0
	 *			S q_2_0
	 *			S initialized;
	 *
	 * after_initialization:
	 */
	void printInitializationStructureAsST(PrintWriter pw)
	{
		pw.println("\n\t(* Set the initial state *)");
		pw.println("\tIF (NOT initialized)");
		pw.println("\tTHEN");
		for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			int currAutomatonIndex = currAutomaton.getSynchIndex();

			State initialState = currAutomaton.getInitialState();
			if (initialState == null)
			{
				String errMessage = "AutomataTOIEC1131.printInitializationStructure: " + "all automata must have an initial state";
				logger.error(errMessage);
				throw new IllegalStateException(errMessage);
			}
			int currStateIndex = initialState.getSynchIndex();
			pw.println("\t\tq_" + currAutomatonIndex + "_" + currStateIndex + " := TRUE;");
		}
		pw.println("\t\tinitialized := TRUE;");
		pw.println("\tEND_IF;");
	}

	/**
	 * Set the initial state the first time the code is executed.
	 *
	 * In Structured Text:
	 *
	 * if (NOT initialized)
	 * {
	 *		q_1_0 := TRUE;
	 *		q_2_0 := TRUE;
	 *		initialized := TRUE;
	 * }
	 *
	 * In Instruction List
	 *
	 * 			LD initialized
	 *			JMPC after_initialization
	 *			LD TRUE
	 *			S q_1_0
	 *			S q_2_0
	 *			S initialized;
	 *
	 * after_initialization:
	 */
	void printInitializationStructureAsIL(PrintWriter pw)
		throws Exception
	{
		theHelper.printILComment(pw, "Set the initial state");
		theHelper.printILCommand(pw, "LD", "initialized");
		theHelper.printILCommand(pw, "JMPC", "after_initialization");
		// Initialize timer delays
		for (Iterator theIt = theProject.timerIterator(); theIt.hasNext();)
		{
			EventTimer currTimer = (EventTimer)theIt.next();
			theHelper.printILCommand(pw, "LD", "DINT#" + currTimer.getDelay());
			theHelper.printILCommand(pw, "ST", "timer_" + currTimer.getSynchIndex() + ".tonPT");
		}
		theHelper.printILCommand(pw, "LD", "TRUE");
		for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			int currAutomatonIndex = currAutomaton.getSynchIndex();

			State initialState = currAutomaton.getInitialState();
			if (initialState == null)
			{
				String errMessage = "AutomataTOIEC1131.printInitializationStructure: " + "all automata must have an initial state";
				logger.error(errMessage);
				throw new Exception(errMessage);
			}
			int currStateIndex = initialState.getSynchIndex();
			theHelper.printILCommand(pw, "S", "q_" + currAutomatonIndex + "_" + currStateIndex);
		}
		theHelper.printILCommand(pw, "S", "initialized");
		theHelper.printILLabel(pw, "after_initialization");
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
	void printComputeEnabledEventsAsST(PrintWriter pw)
	{
		pw.println("\n\tenabledEvent = FALSE;");

		pw.println("\n\t(* Compute the enabled events *)");
		// Iterate over all events and compute which events that are enabled
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			while (alphIt.hasNext())
			{
				LabeledEvent currEvent = (LabeledEvent)alphIt.next();
				int currEventIndex = currEvent.getSynchIndex();
				pw.println("\n\t(* Enable condition for event \"" + currEvent.getLabel() + "\" *)");
				boolean previousCondition = false;
				pw.print("\te_" + currEventIndex + " := ");
				for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
				{
					Automaton currAutomaton = (Automaton)autIt.next();
					Alphabet currAlphabet = currAutomaton.getAlphabet();

					int currAutomatonIndex = currAutomaton.getSynchIndex();

					if (syncType == SynchronizationType.Prioritized)
					{ // All automata that has this event as prioritized must be able to execute it
						if (currAlphabet.containsEqualEvent(currEvent) && currAlphabet.isPrioritized(currEvent))
						{ // Find all states that enables this event
						  // Use OR between states in the same automaton.
						  // Use AND between states in different automata.
							if (previousCondition)
							{
								pw.print(" AND ");
							}
							else
							{
								previousCondition = true;
							}

							boolean previousState = false;
							pw.print("(");
							for (Iterator stateIt = currAutomaton.statesThatEnableEventIterator(currEvent.getLabel()); stateIt.hasNext();)
							{
								State currState = (State)stateIt.next();
								int currStateIndex = currState.getSynchIndex();
								if (previousState)
								{
									pw.print(" OR ");
								}
								else
								{
									previousState = true;
								}
								pw.print("q_" + currAutomatonIndex + "_" + currStateIndex);

							}
							if (!previousState)
							{
								pw.print(" FALSE ");
							}
							pw.print(")");
						}
					}
					else
					{
						String errMessage = "Unsupported SynchronizationType";
						logger.error(errMessage);
						throw new IllegalStateException(errMessage);
					}
				}
				pw.println(";");
			}
		}
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
	void printComputeEnabledEventsAsIL(PrintWriter pw)
		throws Exception
	{

		theHelper.printILCommand(pw, "LD", "FALSE");
		theHelper.printILCommand(pw, "ST", "enabledEvent");

		theHelper.printILComment(pw, "Compute the enabled events");
		// Iterate over all events and compute which events that are enabled
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent)alphIt.next();
			int currEventIndex = currEvent.getSynchIndex();
			theHelper.printILComment(pw, "Enable condition for event \"" + currEvent.getLabel() + "\"");
			boolean previousCondition = false;
			for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
			{
				Automaton currAutomaton = (Automaton)autIt.next();
				Alphabet currAlphabet = currAutomaton.getAlphabet();

				int currAutomatonIndex = currAutomaton.getSynchIndex();

				if (syncType == SynchronizationType.Prioritized)
				{ // All automata that has this event as prioritized must be able to execute it
					if (currAlphabet.containsEqualEvent(currEvent) && currAlphabet.isPrioritized(currEvent))
					{ // Find all states that enables this event
					  // Use OR between states in the same automaton.
					  // Use AND between states in different automata.
						if (previousCondition)
						{
							pw.print("\tAND(\t");
						}
						else
						{
							pw.print("\tLD\t");
						}

						boolean previousState = false;
						for (StateIterator stateIt = currAutomaton.statesThatEnableEventIterator(currEvent.getLabel()); stateIt.hasNext();)
						{
							State currState = stateIt.nextState();
							int currStateIndex = currState.getSynchIndex();
							if (previousState)
							{
								pw.print("\tOR\t ");
							}
							else
							{
								previousState = true;
							}
							pw.println("q_" + currAutomatonIndex + "_" + currStateIndex + "\t");

						}
						if (!previousState)
						{
							pw.println("FALSE\t");
						}
						if (previousCondition)
						{
							pw.println("\t)\t\t");
						}
						else
						{
							previousCondition = true;
						}
					}
				}
				else
				{
						String errMessage = "Unsupported SynchronizationType";
						logger.error(errMessage);
						throw new IllegalStateException(errMessage);
				}
			}
			theHelper.printILCommand(pw, "ST", "e_" + currEventIndex);
		}
	}


	void printCheckEnabledEventsAsIL(PrintWriter pw)
	{

		theHelper.printILComment(pw, "Check if the events are externally enabled");
		// Iterate over all events and compute which events that are enabled
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent)alphIt.next();
			int currEventIndex = currEvent.getSynchIndex();
			theHelper.printILComment(pw, "Enable condition for event \"" + currEvent.getLabel() + "\"");
			theHelper.printILCommand(pw, "JMP", "check_e_" + currEventIndex);
			theHelper.printILLabel(pw, "after_check_e_" + currEventIndex);
		}

		theHelper.printILComment(pw, "Check if the events are enabled by timers");
		// Iterate over all events and compute which events that are enabled by the timer
		for (Iterator timerIt = theProject.timerIterator(); timerIt.hasNext();)
		{
			EventTimer currTimer = (EventTimer)timerIt.next();

			int currTimerIndex = currTimer.getSynchIndex();
			//theHelper.printILLabel(pw, "check_timer_" + currTimerIndex);
			theHelper.printILComment(pw, "Condition for timer " + currTimer.getName());

			String currTimeoutLabel = currTimer.getTimeoutEvent();
			if (!allEvents.contains(currTimeoutLabel))
			{
				String errMessage = "Could not find event: " + currTimeoutLabel;
				logger.error(errMessage);
				throw new IllegalStateException(errMessage);
			}
			LabeledEvent currEvent = allEvents.getEvent(currTimeoutLabel);
			int currEventIndex = currEvent.getSynchIndex();

			theHelper.printILComment(pw, "Timeout event is \"" + currEvent.getLabel() + "\"");
			theHelper.printILCommand(pw, "LD",  "e_" + currEventIndex);
			theHelper.printILCommand(pw, "JMPCN",  "after_check_timer_" + currTimerIndex);
			// Here we know that the event is enabled in the automaton and by the conditions
			theHelper.printILCommand(pw, "CAL",  "timer_" + currTimerIndex);
			theHelper.printILCommand(pw, "LD",  "timer_" + currTimerIndex + ".tonQ");
			theHelper.printILCommand(pw, "JMPC",  "after_check_timer_" + currTimerIndex);
			// Here we know that the timeout event is not enabled
			theHelper.printILCommand(pw, "LD",  "FALSE");
			theHelper.printILCommand(pw, "ST",  "e_" + currEventIndex);
			theHelper.printILLabel(pw, "after_check_timer_" + currTimerIndex);
		}
	}


	void printStartTimersAsIL(PrintWriter pw)
		throws Exception
	{

		theHelper.printILComment(pw, "Start timers");
		for (Iterator timerIt = theProject.timerIterator(); timerIt.hasNext();)
		{
			EventTimer currTimer = (EventTimer)timerIt.next();

			int currTimerIndex = currTimer.getSynchIndex();
			//theHelper.printILLabel(pw, "check_timer_" + currTimerIndex);
			theHelper.printILComment(pw, "Start timer " + currTimer.getName());

			String currTimeoutLabel = currTimer.getStartEvent();
			if (!allEvents.contains(currTimeoutLabel))
			{
				String errMessage = "Could not find event: " + currTimeoutLabel;
				logger.error(errMessage);
				throw new IllegalStateException(errMessage);
			}
			LabeledEvent currEvent = allEvents.getEvent(currTimeoutLabel);
			int currEventIndex = currEvent.getSynchIndex();

			theHelper.printILComment(pw, "Start event is \"" + currEvent.getLabel() + "\"");
			theHelper.printILCommand(pw, "LD",  "e_" + currEventIndex);
			theHelper.printILCommand(pw, "JMPCN",  "after_start_timer_" + currTimerIndex);
			// Here we know that the event is enabled in the automaton and by the conditions
			// First we reset the timer, in case it already was enabled
			theHelper.printILCommand(pw, "LD",  "FALSE");
			theHelper.printILCommand(pw, "ST",  "timer_" + currTimerIndex + ".tonIN");
			theHelper.printILCommand(pw, "CAL",  "timer_" + currTimerIndex);
			// Now we start the timer
			theHelper.printILCommand(pw, "LD",  "TRUE");
			theHelper.printILCommand(pw, "ST",  "timer_" + currTimerIndex + ".tonIN");
			theHelper.printILCommand(pw, "CAL",  "timer_" + currTimerIndex);
			theHelper.printILLabel(pw, "after_start_timer_" + currTimerIndex);
		}
		theHelper.printILCommand(pw, "JMP", "end");
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


	void printChangeStateTransitionsAsST(PrintWriter pw)
		throws Exception
	{
		pw.println("\n\t(* Change state in the automata *)");
		pw.println("\t(* It is in general not safe to have more than one event set to true at this point *)");
		// Iterate over all events and compute which events that are enabled
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			while (alphIt.hasNext())
			{
				LabeledEvent currEvent = (LabeledEvent)alphIt.next();
				int currEventIndex = currEvent.getSynchIndex();
				pw.println("\n\t(* Transition for event \"" + currEvent.getLabel() + "\" *)");
				boolean previousCondition = false;
				pw.println("\tIF (e_" + currEventIndex + ")");
				pw.println("\tTHEN");
				for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
				{
					Automaton currAutomaton = (Automaton)autIt.next();
					Alphabet theAlphabet = currAutomaton.getAlphabet();
					int currAutomatonIndex = currAutomaton.getSynchIndex();

					if (theAlphabet.contains(currEvent.getLabel()))
					{
						LabeledEvent currAutomatonEvent = currAutomaton.getEvent(currEvent.getLabel());
						if (currAutomatonEvent == null)
						{
							throw new Exception("AutomataToIEC1131.printChangeTransitionsAsST: " + "Could not find " + currEvent.getLabel() + " in automaton " + currAutomaton.getName());
						}

						pw.println("\n\t\t(* Transitions in " + currAutomaton.getName() + " *)");
						boolean previousState = false;
						for (Iterator stateIt = currAutomaton.statesThatEnableEventIterator(currEvent.getLabel()); stateIt.hasNext();)
						{
							State currState = (State)stateIt.next();
							int currStateIndex = currState.getSynchIndex();

							State toState = currState.nextState(currAutomatonEvent);
							if (toState == null)
							{
								throw new Exception("AutomataToIEC1131.printChangeTransitionsAsST: " + "Could not find the next state from state " + currState.getName() + " with label " + currEvent.getLabel() + " in automaton " + currAutomaton.getName());
							}
							int toStateIndex = toState.getSynchIndex();
							if (currState != toState)
							{
								if (!previousState)
								{
									pw.print("\t\tIF");
									previousState = true;
								}
								else
								{
									pw.print("\t\tELSIF");
								}
								pw.println(" (q_" + currAutomatonIndex + "_" + currStateIndex + ")");
								pw.println("\t\tTHEN");
								pw.println("\t\t\tq_" + currAutomatonIndex + "_" + toStateIndex + " := TRUE;");
								pw.println("\t\t\tq_" + currAutomatonIndex + "_" + currStateIndex + " := FALSE;");
							}
							else
							{
								pw.println("\t\t(* q_" + currAutomatonIndex + "_" + currStateIndex + "  has e_" + currEventIndex + " as self loop, no transition *)");
							}
						}
						if (previousState)
						{
							pw.println("\t\tEND_IF;");
						}
					}

				}
				pw.println("\tEND_IF;");
			}
		}
	}

	/**
	 * In Instruction List this will look like:
	 *
	 * 				LD e_0
	 *				JMPCN trans_after_e_1
	 * e_0_q_1_0:	LD q_1_0
	 *				JMPCN trans_after_e_1_q_1_0
	 *				S q_1_1 (* Note that the result register is true here *)
	 *				R q_1_2
	 *				JMP e_1
	 * e_1_q_1_2:	LD q_1_2
	 *				JMPCN end_of_e_0
	 *				S q_1_0
	 *				R q_1_2
	 *				JMP e_1
	 * trans_after_e_1:
	 * 				LD e_1
	 * end_of_jumps:
	 */
	void printChangeStateTransitionsAsIL(PrintWriter pw)
	{
		theHelper.printILComment(pw, "Change state in the automata");
		theHelper.printILComment(pw, "It is in general not safe to have more than one event set to true at this point");
		// Iterate over all events and compute which events that are enabled
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			while (alphIt.hasNext())
			{
				LabeledEvent currEvent = (LabeledEvent)alphIt.next();
				int currEventIndex = currEvent.getSynchIndex();
				theHelper.printILComment(pw, "Transition for event \"" + currEvent.getLabel() + "\"");
				boolean previousCondition = false;
				theHelper.printILCommand(pw, "LD", "e_" + currEventIndex);
				theHelper.printILCommand(pw, "JMPCN", "trans_after_e_" + currEventIndex);
				// Execute the actions
				theHelper.printILCommand(pw, "JMP", "do_e_" + currEventIndex);
				theHelper.printILLabel(pw, "after_do_e_" + currEventIndex);

				for (Iterator autIt = theProject.iterator(); autIt.hasNext();)
				{
					Automaton currAutomaton = (Automaton)autIt.next();
					Alphabet currAlphabet = currAutomaton.getAlphabet();

					int currAutomatonIndex = currAutomaton.getSynchIndex();

					if (currAlphabet.contains(currEvent.getLabel()))
					{
						LabeledEvent currAutomatonEvent = currAutomaton.getEvent(currEvent.getLabel());
						if (currAutomatonEvent == null)
						{
							throw new IllegalStateException("AutomataToIEC1131.printChangeTransitionsAsIL: " + "Could not find " + currEvent.getLabel() + " in automaton " + currAutomaton.getName());
						}

						theHelper.printILComment(pw, "Transitions in " + currAutomaton.getName());
						boolean previousState = false;
						for (Iterator stateIt = currAutomaton.statesThatEnableEventIterator(currEvent.getLabel()); stateIt.hasNext();)
						{
							State currState = (State)stateIt.next();
							int currStateIndex = currState.getSynchIndex();

							State toState = currState.nextState(currAutomatonEvent);
							if (toState == null)
							{
								throw new IllegalStateException("AutomataToIEC1131.printChangeTransitionsAsIL: " + "Could not find the next state from state " + currState.getName() + " with label " + currEvent.getLabel() + " in automaton " + currAutomaton.getName());
							}
							int toStateIndex = toState.getSynchIndex();
							if (currState != toState)
							{
								theHelper.printILCommand(pw, "LD",  "q_" + currAutomatonIndex + "_" + currStateIndex);
								theHelper.printILCommand(pw, "JMPCN", "trans_after_e_" + currEventIndex + "_q_"+ currAutomatonIndex + "_" + currStateIndex);
								theHelper.printILCommand(pw, "S", "q_" + currAutomatonIndex + "_" + toStateIndex);
								theHelper.printILCommand(pw, "R", "q_" + currAutomatonIndex + "_" + currStateIndex);
								theHelper.printILCommand(pw, "JMP", "trans_after_e_" + currEventIndex + "_a_" + currAutomatonIndex);
							}
							else
							{
								theHelper.printILComment(pw, "q_" + currAutomatonIndex + "_" + currStateIndex + "  has e_" + currEventIndex + " as self loop, no transition");
							}
							theHelper.printILLabel(pw, "trans_after_e_" + currEventIndex + "_q_"+ currAutomatonIndex + "_" + currStateIndex);
						}
						theHelper.printILLabel(pw, "trans_after_e_" + currEventIndex + "_a_" + currAutomatonIndex);
					}

				}
				theHelper.printILLabel(pw, "trans_after_e_" + currEventIndex);
			}
		}
	}

	void printComputeSingleEnabledEventAsIL(PrintWriter pw)
		throws Exception
	{
		theHelper.printILComment(pw, "Make sure only one event is enabled");
		theHelper.printILComment(pw, "Priority is given to uncontrollable events");

		// Iterate over all events and compute which events that are enabled
		for (Iterator alphIt = allEvents.uncontrollableEventIterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent)alphIt.next();
			int currEventIndex = currEvent.getSynchIndex();
			theHelper.printILComment(pw, "Enable condition for event \"" + currEvent.getLabel() + "\"");
			theHelper.printILCommand(pw, "LD",  "e_" + currEventIndex);
			theHelper.printILCommand(pw, "ANDN",  "enabledEvent");
			theHelper.printILCommand(pw, "ST",  "e_" + currEventIndex);
			theHelper.printILCommand(pw, "OR",  "enabledEvent");
			theHelper.printILCommand(pw, "ST",  "enabledEvent");
		}
		for (Iterator alphIt = allEvents.controllableEventIterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent)alphIt.next();
			int currEventIndex = currEvent.getSynchIndex();
			theHelper.printILComment(pw, "Enable condition for event \"" + currEvent.getLabel() + "\"");
			theHelper.printILCommand(pw, "LD",  "e_" + currEventIndex);
			theHelper.printILCommand(pw, "ANDN",  "enabledEvent");
			theHelper.printILCommand(pw, "ST",  "e_" + currEventIndex);
			theHelper.printILCommand(pw, "OR",  "enabledEvent");
			theHelper.printILCommand(pw, "ST",  "enabledEvent");
		}
	}


	void printComputeSingleEnabledEventAsST(PrintWriter pw)
		throws Exception
	{
		pw.println("\n\t\t(* Make sure only one event is enabled *)");
		pw.println("\t\t(* Priority is given to uncontrollable events *)");
		// Iterate over all events and compute which events that are enabled
		for (Iterator alphIt = allEvents.uncontrollableEventIterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent)alphIt.next();
			int currEventIndex = currEvent.getSynchIndex();
			pw.println("\n\t(* Enable condition for event \"" + currEvent.getLabel() + "\" *)");
			pw.println("\te_" + currEventIndex + " = " + "e_" + currEventIndex + " AND (NOT enabledEvent);");
			pw.println("\tenabledEvent = " + "enabledEvent OR " + "e_" + currEventIndex + ";");
		}
		for (Iterator alphIt = allEvents.controllableEventIterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent)alphIt.next();
			int currEventIndex = currEvent.getSynchIndex();
			pw.println("\n\t(* Enable condition for event \"" + currEvent.getLabel() + "\" *)");
			pw.println("\te_" + currEventIndex + " = " + "e_" + currEventIndex + " AND (NOT enabledEvent);");
			pw.println("\tenabledEvent = " + "enabledEvent OR " + "e_" + currEventIndex + ";");
		}
	}


	void printDoActionsAsIL(PrintWriter pw)
		throws Exception
	{
		Actions theActions = theProject.getActions();
		Signals outputSignals = theProject.getOutputSignals();

		theHelper.printILComment(pw, "The actions");
		// Iterate over all events and compute which events that are externally enabled
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent)alphIt.next();
			int currEventIndex = currEvent.getSynchIndex();
			theHelper.printILLabel(pw, "do_e_" + currEventIndex);
			theHelper.printILComment(pw, "Action for event " + currEvent.getLabel());
			if (theActions.hasAction(currEvent.getLabel()))
			{
				Action currAction = theActions.getAction(currEvent.getLabel());
				for (Iterator it = currAction.commandIterator(); it.hasNext(); )
				{
					Command currCommand = (Command)it.next();
					if (currCommand.getValue())
					{
						theHelper.printILCommand(pw, "LD",  "TRUE");
					}
					else
					{
						theHelper.printILCommand(pw, "LD",  "FALSE");
					}
					if (!outputSignals.hasSignal(currCommand.getLabel()))
					{
						throw new Exception("Could not find output signal " + currCommand.getLabel());
					}
					Signal currSignal = outputSignals.getSignal(currCommand.getLabel());
					theHelper.printILCommand(pw, "ST",  "so_" + currSignal.getPort());
				}
			}

			theHelper.printILCommand(pw, "JMP",  "after_do_e_" + currEventIndex);
		}
	}

	void printCheckConditionsAsIL(PrintWriter pw)
	{
		Controls theControls = theProject.getControls();
		Signals inputSignals = theProject.getInputSignals();

		theHelper.printILComment(pw, "The conditions");
		// Iterate over all events and compute which events that are externally enabled
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent)alphIt.next();
			int currEventIndex = currEvent.getSynchIndex();
			theHelper.printILLabel(pw, "check_e_" + currEventIndex);
			theHelper.printILComment(pw, "Condition for event " + currEvent.getLabel());

			theHelper.printILCommand(pw, "LD",  "e_" + currEventIndex);
			theHelper.printILCommand(pw, "JMPCN",  "after_check_e_" + currEventIndex);
			if (theControls.hasControl(currEvent.getLabel()))
			{
				Control currControl = theControls.getControl(currEvent.getLabel());
				int i = 0;
				for (Iterator it = currControl.conditionIterator(); it.hasNext(); )
				{
					Condition currCondition = (Condition)it.next();
					if (!inputSignals.hasSignal(currCondition.getLabel()))
					{
						String errMessage = "Could not find input signal " + currCondition.getLabel();
						logger.error(errMessage);
						throw new IllegalStateException(errMessage);
					}
					Signal currSignal = inputSignals.getSignal(currCondition.getLabel());

					theHelper.printILCommand(pw, "LD",  "si_" + currSignal.getPort());
					if (currCondition.doInvert())
					{
						theHelper.printILCommand(pw, "JMPCN",  "check_e_" + currEventIndex + "_" + i);
						theHelper.printILCommand(pw, "LD",  "FALSE");
						theHelper.printILCommand(pw, "ST",  "e_" + currEventIndex);
						theHelper.printILCommand(pw, "JMP",  "after_check_e_" + currEventIndex);
						theHelper.printILLabel(pw, "check_e_" + currEventIndex + "_" + i);
					}
					else
					{
						theHelper.printILCommand(pw, "JMPC",  "check_e_" + currEventIndex + "_" + i);
						theHelper.printILCommand(pw, "LD",  "FALSE");
						theHelper.printILCommand(pw, "ST",  "e_" + currEventIndex);
						theHelper.printILCommand(pw, "JMP",  "after_check_e_" + currEventIndex);
						theHelper.printILLabel(pw, "check_e_" + currEventIndex + "_" + i);
					}
					i++;
				}
			}
			theHelper.printILCommand(pw, "JMP",  "after_check_e_" + currEventIndex);
		}
	}

/*
	void printCheckTimersAsIL(PrintWriter pw)
	{
		theHelper.printILComment(pw, "Check the timers");
		// Iterate over all events and compute which events that are enabled by the timer
		for (Iterator timerIt = theProject.timerIterator(); timerIt.hasNext();)
		{
			EventTimer currTimer = (EventTimer)timerIt.next();

			int currTimerIndex = currTimer.getSynchIndex();
			theHelper.printILLabel(pw, "check_timer_" + currTimerIndex);
			theHelper.printILComment(pw, "Condition for timer " + currTimer.getName());

			String currTimeoutLabel = currTimer.getTimeoutEvent();
			if (!theEvents.containsEventWithLabel(currTimeoutLabel))
			{
				throw new IllegalStateException("Could not find event: " + currTimeoutLabel);
			}
			LabeledEvent currEvent = theEvents.getEventWithLabel(currTimeoutLabel);

			theHelper.printILComment(pw, "Timeout event is " + currEvent.getLabel());
			int currEventIndex = currEvent.getSynchIndex();

			theHelper.printILCommand(pw, "LD",  "e_" + currEventIndex);
			theHelper.printILCommand(pw, "JMPCN",  "after_check_timer_" + currTimerIndex);
			// Here we know that the event is enabled in the automaton and by the conditions
			theHelper.printILCommand(pw, "LD",  "timer_" + currTimerIndex + ".tonQ");
			theHelper.printILCommand(pw, "JMPC",  "after_check_timer_" + currTimerIndex);
			// Here we know that the timeout event is not enabled
			theHelper.printILCommand(pw, "LD",  "FALSE");
			theHelper.printILCommand(pw, "ST",  "e_" + currEventIndex);
			theHelper.printILCommand(pw, "JMP",  "after_check_timer_" + currTimerIndex);
		}
	}
*/
/*

	void printComputeExternalEnabledEventsAsIL(PrintWriter pw)
		throws Exception
	{
		pw.println("\n\t\t// Compute the external enabled events");
		// Iterate over all events and compute which events that are externally enabled
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent)alphIt.next();
			int currEventIndex = currEvent.getSynchIndex();
			pw.println("\n\t\t// Enable condition for event \"" + currEvent.getLabel() + "\"");
			pw.println("\t\tenabled_" + currEvent.getLabel() + "(e_" + currEventIndex + ");");
		}
	}

	void printInitializationFunctionAsIL(PrintWriter pw)
		throws Exception
	{
		pw.println("\n// Intitialization function");
		pw.println("void initialize()");
		pw.println("{");
		pw.println("\t// Add initialization code here");
		pw.println("}");
	}

	void printComputeExternalEnabledEventsFunctionsAsIL(PrintWriter pw)
		throws Exception
	{
		pw.println("\n// Functions for the external enabled events");
		pw.println("// Note that all labels must valid identifiers in NQC");
		// Iterate over all events and compute which events that are externally enabled
		for (Iterator alphIt = allEvents.iterator(); alphIt.hasNext();)
		{
			LabeledEvent currEvent = (LabeledEvent)alphIt.next();
			int currEventIndex = currEvent.getSynchIndex();
			pw.println("\n// External enable condition for event \"" + currEvent.getLabel() + "\"");
			pw.println("void enabled_" + currEvent.getLabel() + "(int& e)");
			pw.println("{");
			pw.println("\t// Add a condition here, the default does not change anything. Example:");
			pw.println("\t// e = e && (SensorValue(0) == 2);");
			pw.println("}");
		}
	}
*/
	public void serializeStructuredText(PrintWriter pw)
		throws Exception
	{
		initialize();
		printBeginProgram(pw);
		printBeginVariables(pw);
		printSignalVariables(pw);
		printEventVariables(pw);
		printStateVariables(pw);
		printEndVariables(pw);
		printSTBegin(pw);
		printInitializationStructureAsST(pw);
		printComputeEnabledEventsAsST(pw);
		printComputeSingleEnabledEventAsST(pw);
		printChangeStateTransitionsAsST(pw);
		printSTEnd(pw);
		printEndProgram(pw);
	}

	public void serializeInstructionList(PrintWriter pw)
		throws Exception
	{
		initialize();
		theHelper.printILTimerFunctions(pw);
		printBeginProgram(pw);
		printBeginVariables(pw);
		printSignalVariables(pw);
		printEndVariables(pw);
		printBeginVariables(pw);
		printEventVariables(pw);
		printStateVariables(pw);
		printTimerVariables(pw);
		printEndVariables(pw);
		printILBegin(pw);
		printInitializationStructureAsIL(pw);
		printComputeEnabledEventsAsIL(pw);
		printCheckEnabledEventsAsIL(pw);
		printComputeSingleEnabledEventAsIL(pw);
		printChangeStateTransitionsAsIL(pw);
		printStartTimersAsIL(pw);
		printCheckConditionsAsIL(pw);
		printDoActionsAsIL(pw);
		printILEnd(pw);
		printEndProgram(pw);
		theHelper.printILPrintFunctions(pw);
	}
}
