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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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

import java.io.PrintWriter;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.SynchronizationType;
import org.supremica.automata.execution.Action;
import org.supremica.automata.execution.Actions;
import org.supremica.automata.execution.Command;
import org.supremica.automata.execution.Condition;
import org.supremica.automata.execution.Control;
import org.supremica.automata.execution.Controls;
import org.supremica.automata.execution.EventTimer;
import org.supremica.automata.execution.Signal;
import org.supremica.automata.execution.Signals;
import org.supremica.util.SupremicaException;


public class AutomataToIEC1131
{
    private static Logger logger = LogManager.getLogger(AutomataToIEC1131.class);
    private final Project theProject;

    //private SynchronizationOptions syncOptions;
    //private AutomataSynchronizerHelper syncHelper;
    private final SynchronizationType syncType = SynchronizationType.PRIORITIZED;
    private Alphabet allEvents;
    private final IEC61131Helper theHelper;

    public AutomataToIEC1131(final Project theProject)
	throws Exception
    {
	this(theProject, IEC61131Helper.getInstance());
    }

    public AutomataToIEC1131(final Project theProject, final IEC61131Helper theHelper)
	throws Exception
    {
	this.theProject = theProject;
	this.theHelper = theHelper;
	this.initialize();
    }

    private void initialize()
    {
	allEvents = theProject.setIndices();
    }

    void printBeginProgram(final PrintWriter pw)
    {
	theHelper.printBeginProgram(pw, "AutomaticallyGeneratedProgram");
    }

    void printEndProgram(final PrintWriter pw)
    {
	theHelper.printEndProgram(pw);
    }

    void printBeginVariables(final PrintWriter pw)
    {
	theHelper.printBeginVariables(pw);
    }

    void printEndVariables(final PrintWriter pw)
    {
	theHelper.printEndVariables(pw);
    }

    void printSignalVariables(final PrintWriter pw)
    {

	// Input signals
	for (final Iterator<Signal> theIt = theProject.inputSignalsIterator();
	     theIt.hasNext(); )
	{
	    final Signal currSignal = theIt.next();
	    final int currPort = currSignal.getPort();

	    theHelper.printBooleanInputVariableDeclaration(pw, "si_" + currPort, currPort, currSignal.getLabel());
	}

	// Output signals
	for (final Iterator<Signal> theIt = theProject.outputSignalsIterator();
	     theIt.hasNext(); )
	{
	    final Signal currSignal = theIt.next();
	    final int currPort = currSignal.getPort();

	    theHelper.printBooleanOutputVariableDeclaration(pw, "so_" + currPort, currPort, currSignal.getLabel());
	}
    }

    void printEventVariables(final PrintWriter pw)
    {

	// Iterate over all events and compute which events that are enabled
	for (final Iterator<LabeledEvent> alphIt = allEvents.iterator(); alphIt.hasNext(); )
	{
	    final LabeledEvent currEvent = alphIt.next();
	    final int currEventIndex = currEvent.getIndex();

	    theHelper.printBooleanVariableDeclaration(pw, "e_" + currEventIndex, currEvent.getLabel() + (currEvent.isControllable()
													 ? " controllable"
													 : " uncontrollable"),2);
	}

	theHelper.printBooleanVariableDeclaration(pw, "enabledEvent", "True if an event is enabled, false otherwise",2);
    }

    void printStateVariables(final PrintWriter pw,final int tabs)
    {
	for (final Iterator<Automaton> autIt = theProject.iterator(); autIt.hasNext(); )
	{
	    final Automaton currAutomaton = autIt.next();
	    final int currAutomatonIndex = currAutomaton.getSynchIndex();

	    for (final Iterator<State> stateIt = currAutomaton.stateIterator();
		 stateIt.hasNext(); )
	    {
		final State currState = stateIt.next();
		final int currStateIndex = currState.getSynchIndex();

		theHelper.printBooleanVariableDeclaration(pw, "q_" + currAutomatonIndex + "_" + currStateIndex, currState.getName() + " in " + currAutomaton.getName(),tabs);
	    }
	}

	theHelper.printBooleanVariableDeclaration(pw, "initialized", "Set the inital state the first scan cycle",1);
    }

    void printTimerVariables(final PrintWriter pw)
    {
	for (final Iterator<EventTimer> theIt = theProject.timerIterator(); theIt.hasNext(); )
	{
	    final EventTimer currTimer = theIt.next();

	    theHelper.printTimerVariableDeclaration(pw, "timer_" + currTimer.getSynchIndex(), currTimer.getName());
	}
    }

    public void printILBegin(final PrintWriter pw)
    {
	theHelper.printILBegin(pw);
    }

    public void printILEnd(final PrintWriter pw)
    {
	theHelper.printILLabel(pw, "end");
	theHelper.printILEnd(pw);
    }

    public void printSTBegin(final PrintWriter pw)
    {
	theHelper.printSTBegin(pw);
    }

    public void printSTEnd(final PrintWriter pw)
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
     *              q_1_0 := TRUE;
     *              q_2_0 := TRUE;
     *              initialized := TRUE;
     * END_IF;
     *
     * In Instruction List
     *
     *                      LD initialized
     *                      JMPC after_initialization
     *                      LD TRUE
     *                      S q_1_0
     *                      S q_2_0
     *                      S initialized;
     *
     * after_initialization:
     */
    void printInitializationStructureAsST(final PrintWriter pw)
    {
	pw.println("\n\t(* Set the initial state *)");
	pw.println("\tIF (NOT initialized)");
	pw.println("\tTHEN");

	for (final Iterator<Automaton> autIt = theProject.iterator(); autIt.hasNext(); )
	{
	    final Automaton currAutomaton = autIt.next();
	    final int currAutomatonIndex = currAutomaton.getSynchIndex();
	    final State initialState = currAutomaton.getInitialState();

	    if (initialState == null)
	    {
		final String errMessage = "AutomataTOIEC1131.printInitializationStructure: " + "all automata must have an initial state";

		logger.error(errMessage);

		throw new IllegalStateException(errMessage);
	    }

	    final int currStateIndex = initialState.getSynchIndex();

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
     *              q_1_0 := TRUE;
     *              q_2_0 := TRUE;
     *              initialized := TRUE;
     * }
     *
     * In Instruction List
     *
     *                      LD initialized
     *                      JMPC after_initialization
     *                      LD TRUE
     *                      S q_1_0
     *                      S q_2_0
     *                      S initialized;
     *
     * after_initialization:
     */
    void printInitializationStructureAsIL(final PrintWriter pw)
	throws Exception
    {
	theHelper.printILComment(pw, "Set the initial state");
	theHelper.printILCommand(pw, "LD", "initialized");
	theHelper.printILCommand(pw, "JMPC", "after_initialization");

	// Initialize timer delays
	for (final Iterator<EventTimer> theIt = theProject.timerIterator(); theIt.hasNext(); )
	{
	    final EventTimer currTimer = theIt.next();

	    theHelper.printILCommand(pw, "LD", "DINT#" + currTimer.getDelay());
	    theHelper.printILCommand(pw, "ST", "timer_" + currTimer.getSynchIndex() + ".tonPT");
	}

	theHelper.printILCommand(pw, "LD", "TRUE");

	for (final Iterator<Automaton> autIt = theProject.iterator(); autIt.hasNext(); )
	{
	    final Automaton currAutomaton = autIt.next();
	    final int currAutomatonIndex = currAutomaton.getSynchIndex();
	    final State initialState = currAutomaton.getInitialState();

	    if (initialState == null)
	    {
		final String errMessage = "AutomataTOIEC1131.printInitializationStructure: " + "all automata must have an initial state";

		logger.error(errMessage);

		throw new SupremicaException(errMessage);
	    }

	    final int currStateIndex = initialState.getSynchIndex();

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
    void printComputeEnabledEventsAsST(final PrintWriter pw)
    {
	pw.println("\n\tenabledEvent = FALSE;");
	pw.println("\n\t(* Compute the enabled events *)");

	// Iterate over all events and compute which events that are enabled
	for (final Iterator<LabeledEvent> alphIt = allEvents.iterator(); alphIt.hasNext(); )
	{
	    while (alphIt.hasNext())
	    {
		final LabeledEvent currEvent = alphIt.next();
		final int currEventIndex = currEvent.getIndex();

		pw.println("\n\t(* Enable condition for event \"" + currEvent.getLabel() + "\" *)");

		boolean previousCondition = false;

		pw.print("\te_" + currEventIndex + " := ");

		for (final Iterator<Automaton> autIt = theProject.iterator(); autIt.hasNext(); )
		{
		    final Automaton currAutomaton = autIt.next();
		    final Alphabet currAlphabet = currAutomaton.getAlphabet();
		    final int currAutomatonIndex = currAutomaton.getSynchIndex();

		    if (syncType == SynchronizationType.PRIORITIZED)
		    {    // All automata that has this event as prioritized must be able to execute it
			if (currAlphabet.containsEqualEvent(currEvent) && currAlphabet.isPrioritized(currEvent))
			{    // Find all states that enables this event

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

			    for (final Iterator<State> stateIt = currAutomaton.statesThatEnableEventIterator(currEvent.getLabel());
				 stateIt.hasNext(); )
			    {
				final State currState = stateIt.next();
				final int currStateIndex = currState.getSynchIndex();

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
			final String errMessage = "Unsupported SynchronizationType";

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
    void printComputeEnabledEventsAsIL(final PrintWriter pw)
	throws Exception
    {
	theHelper.printILCommand(pw, "LD", "FALSE");
	theHelper.printILCommand(pw, "ST", "enabledEvent");
	theHelper.printILComment(pw, "Compute the enabled events");

	// Iterate over all events and compute which events that are enabled
	for (final Iterator<LabeledEvent> alphIt = allEvents.iterator(); alphIt.hasNext(); )
	{
	    final LabeledEvent currEvent = alphIt.next();
	    final int currEventIndex = currEvent.getIndex();

	    theHelper.printILComment(pw, "Enable condition for event \"" + currEvent.getLabel() + "\"");

	    boolean previousCondition = false;

	    for (final Iterator<Automaton> autIt = theProject.iterator(); autIt.hasNext(); )
	    {
		final Automaton currAutomaton = autIt.next();
		final Alphabet currAlphabet = currAutomaton.getAlphabet();
		final int currAutomatonIndex = currAutomaton.getSynchIndex();

		if (syncType == SynchronizationType.PRIORITIZED)
		{    // All automata that has this event as prioritized must be able to execute it
		    if (currAlphabet.containsEqualEvent(currEvent) && currAlphabet.isPrioritized(currEvent))
		    {    // Find all states that enables this event

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

			for (final Iterator<State> stateIt = currAutomaton.statesThatEnableEventIterator(currEvent.getLabel());
			     stateIt.hasNext(); )
			{
			    final State currState = stateIt.next();
			    final int currStateIndex = currState.getSynchIndex();

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
		    final String errMessage = "Unsupported SynchronizationType";

		    logger.error(errMessage);

		    throw new IllegalStateException(errMessage);
		}
	    }

	    theHelper.printILCommand(pw, "ST", "e_" + currEventIndex);
	}
    }

    void printCheckEnabledEventsAsIL(final PrintWriter pw)
    {
	theHelper.printILComment(pw, "Check if the events are externally enabled");

	// Iterate over all events and compute which events that are enabled
	for (final Iterator<LabeledEvent> alphIt = allEvents.iterator(); alphIt.hasNext(); )
	{
	    final LabeledEvent currEvent = alphIt.next();
	    final int currEventIndex = currEvent.getIndex();

	    theHelper.printILComment(pw, "Enable condition for event \"" + currEvent.getLabel() + "\"");
	    theHelper.printILCommand(pw, "JMP", "check_e_" + currEventIndex);
	    theHelper.printILLabel(pw, "after_check_e_" + currEventIndex);
	}

	theHelper.printILComment(pw, "Check if the events are enabled by timers");

	// Iterate over all events and compute which events that are enabled by the timer
	for (final Iterator<EventTimer> timerIt = theProject.timerIterator(); timerIt.hasNext(); )
	{
	    final EventTimer currTimer = timerIt.next();
	    final int currTimerIndex = currTimer.getSynchIndex();

	    //theHelper.printILLabel(pw, "check_timer_" + currTimerIndex);
	    theHelper.printILComment(pw, "Condition for timer " + currTimer.getName());

	    final String currTimeoutLabel = currTimer.getTimeoutEvent();

	    if (!allEvents.contains(currTimeoutLabel))
	    {
		final String errMessage = "Could not find event: " + currTimeoutLabel;

		logger.error(errMessage);

		throw new IllegalStateException(errMessage);
	    }

	    final LabeledEvent currEvent = allEvents.getEvent(currTimeoutLabel);
	    final int currEventIndex = currEvent.getIndex();

	    theHelper.printILComment(pw, "Timeout event is \"" + currEvent.getLabel() + "\"");
	    theHelper.printILCommand(pw, "LD", "e_" + currEventIndex);
	    theHelper.printILCommand(pw, "JMPCN", "after_check_timer_" + currTimerIndex);

	    // Here we know that the event is enabled in the automaton and by the conditions
	    theHelper.printILCommand(pw, "CAL", "timer_" + currTimerIndex);
	    theHelper.printILCommand(pw, "LD", "timer_" + currTimerIndex + ".tonQ");
	    theHelper.printILCommand(pw, "JMPC", "after_check_timer_" + currTimerIndex);

	    // Here we know that the timeout event is not enabled
	    theHelper.printILCommand(pw, "LD", "FALSE");
	    theHelper.printILCommand(pw, "ST", "e_" + currEventIndex);
	    theHelper.printILLabel(pw, "after_check_timer_" + currTimerIndex);
	}
    }

    void printStartTimersAsIL(final PrintWriter pw)
	throws Exception
    {
	theHelper.printILComment(pw, "Start timers");

	for (final Iterator<EventTimer> timerIt = theProject.timerIterator(); timerIt.hasNext(); )
	{
	    final EventTimer currTimer = timerIt.next();
	    final int currTimerIndex = currTimer.getSynchIndex();

	    //theHelper.printILLabel(pw, "check_timer_" + currTimerIndex);
	    theHelper.printILComment(pw, "Start timer " + currTimer.getName());

	    final String currTimeoutLabel = currTimer.getStartEvent();

	    if (!allEvents.contains(currTimeoutLabel))
	    {
		final String errMessage = "Could not find event: " + currTimeoutLabel;

		logger.error(errMessage);

		throw new IllegalStateException(errMessage);
	    }

	    final LabeledEvent currEvent = allEvents.getEvent(currTimeoutLabel);
	    final int currEventIndex = currEvent.getIndex();

	    theHelper.printILComment(pw, "Start event is \"" + currEvent.getLabel() + "\"");
	    theHelper.printILCommand(pw, "LD", "e_" + currEventIndex);
	    theHelper.printILCommand(pw, "JMPCN", "after_start_timer_" + currTimerIndex);

	    // Here we know that the event is enabled in the automaton and by the conditions
	    // First we reset the timer, in case it already was enabled
	    theHelper.printILCommand(pw, "LD", "FALSE");
	    theHelper.printILCommand(pw, "ST", "timer_" + currTimerIndex + ".tonIN");
	    theHelper.printILCommand(pw, "CAL", "timer_" + currTimerIndex);

	    // Now we start the timer
	    theHelper.printILCommand(pw, "LD", "TRUE");
	    theHelper.printILCommand(pw, "ST", "timer_" + currTimerIndex + ".tonIN");
	    theHelper.printILCommand(pw, "CAL", "timer_" + currTimerIndex);
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
     *              (* Automaton 1 *)
     *              if (q_1_0)
     *              {
     *                      q_1_1 = true;
     *                      q_1_0 = false;
     *              }
     *              else if (q_1_2)
     *              {
     *                      q_1_0 = true;
     *                      q_1_2 = false;
     *              }
     *
     *              (* Automaton 2 *)
     *
     * }
     * if (e_1)
     * {
     *              (* Automaton 1 *)
     *              if (q_1_0)
     *              {
     *                      q_1_2 = true;
     *                      q_1_0 = false;
     *              }
     *
     *              (* Automaton 2 *)
     *              if (q_2_0)
     *              {
     *                      q_2_2 = true;
     *                      q_2_0 = false;
     *              }
     * }
     *
     * In Structured Text this will look like:
     *
     *
     * IF (e_0)
     * THEN
     *              IF (q_1_0)
     *              THEN
     *                      q_1_1 := TRUE;
     *                      q_1_0 := FALSE;
     *              ELSIF (q_1_2)
     *              THEN
     *                      q_1_0 := TRUE;
     *                      q_1_2 := FALSE;
     *              END_IF;
     * END_IF;
     * IF (e_1)
     * THEN
     *              ...
     * END_IF;
     *
     * In Instruction List this will look like:
     *
     * e_0:                 LD e_0
     *                              JMPCN e_1
     * e_0_q_1_0:   LD q_1_0
     *                              JMPCN q_1_2_e_1_0
     *                              S q_1_1 (* Note that the result register is true here *)
     *                              R q_1_2
     *                              JMP e_1
     * e_1_q_1_2:   LD q_1_2
     *                              JMPCN end_of_e_0
     *                              S q_1_0
     *                              R q_1_2
     *                              JMP e_1
     * e_1:                 LD e_1
     * end_of_jumps:
     */
    void printChangeStateTransitionsAsST(final PrintWriter pw)
	throws Exception
    {
	pw.println("\n\t(* Change state in the automata *)");
	pw.println("\t(* It is in general not safe to have more than one event set to true at this point *)");

	// Iterate over all events and compute which events that are enabled
	for (final Iterator<LabeledEvent> alphIt = allEvents.iterator(); alphIt.hasNext(); )
	{
	    while (alphIt.hasNext())
	    {
		final LabeledEvent currEvent = alphIt.next();
		final int currEventIndex = currEvent.getIndex();

		pw.println("\n\t(* Transition for event \"" + currEvent.getLabel() + "\" *)");
		pw.println("\tIF (e_" + currEventIndex + ")");
		pw.println("\tTHEN");

		for (final Iterator<Automaton> autIt = theProject.iterator(); autIt.hasNext(); )
		{
		    final Automaton currAutomaton = autIt.next();
		    final Alphabet theAlphabet = currAutomaton.getAlphabet();
		    final int currAutomatonIndex = currAutomaton.getSynchIndex();

		    if (theAlphabet.contains(currEvent.getLabel()))
		    {
			final LabeledEvent currAutomatonEvent = currAutomaton.getAlphabet().getEvent(currEvent.getLabel());

			if (currAutomatonEvent == null)
			{
			    throw new SupremicaException("AutomataToIEC1131.printChangeTransitionsAsST: " + "Could not find " + currEvent.getLabel() + " in automaton " + currAutomaton.getName());
			}

			pw.println("\n\t\t(* Transitions in " + currAutomaton.getName() + " *)");

			boolean previousState = false;

			for (final Iterator<State> stateIt = currAutomaton.statesThatEnableEventIterator(currEvent.getLabel());
			     stateIt.hasNext(); )
			{
			    final State currState = stateIt.next();
			    final int currStateIndex = currState.getSynchIndex();
			    final State toState = currState.nextState(currAutomatonEvent);

			    if (toState == null)
			    {
				throw new SupremicaException("AutomataToIEC1131.printChangeTransitionsAsST: " + "Could not find the next state from state " + currState.getName() + " with label " + currEvent.getLabel() + " in automaton " + currAutomaton.getName());
			    }

			    final int toStateIndex = toState.getSynchIndex();

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
     *                              LD e_0
     *                              JMPCN trans_after_e_1
     * e_0_q_1_0:   LD q_1_0
     *                              JMPCN trans_after_e_1_q_1_0
     *                              S q_1_1 (* Note that the result register is true here *)
     *                              R q_1_2
     *                              JMP e_1
     * e_1_q_1_2:   LD q_1_2
     *                              JMPCN end_of_e_0
     *                              S q_1_0
     *                              R q_1_2
     *                              JMP e_1
     * trans_after_e_1:
     *                              LD e_1
     * end_of_jumps:
     */
    void printChangeStateTransitionsAsIL(final PrintWriter pw)
    {
	theHelper.printILComment(pw, "Change state in the automata");
	theHelper.printILComment(pw, "It is in general not safe to have more than one event set to true at this point");

	// Iterate over all events and compute which events that are enabled
	for (final Iterator<LabeledEvent> alphIt = allEvents.iterator(); alphIt.hasNext(); )
	{
	    while (alphIt.hasNext())
	    {
		final LabeledEvent currEvent = alphIt.next();
		final int currEventIndex = currEvent.getIndex();

		theHelper.printILComment(pw, "Transition for event \"" + currEvent.getLabel() + "\"");
		theHelper.printILCommand(pw, "LD", "e_" + currEventIndex);
		theHelper.printILCommand(pw, "JMPCN", "trans_after_e_" + currEventIndex);

		// Execute the actions
		theHelper.printILCommand(pw, "JMP", "do_e_" + currEventIndex);
		theHelper.printILLabel(pw, "after_do_e_" + currEventIndex);

		for (final Iterator<Automaton> autIt = theProject.iterator(); autIt.hasNext(); )
		{
		    final Automaton currAutomaton = autIt.next();
		    final Alphabet currAlphabet = currAutomaton.getAlphabet();
		    final int currAutomatonIndex = currAutomaton.getSynchIndex();

		    if (currAlphabet.contains(currEvent.getLabel()))
		    {
			final LabeledEvent currAutomatonEvent = currAutomaton.getAlphabet().getEvent(currEvent.getLabel());

			if (currAutomatonEvent == null)
			{
			    throw new IllegalStateException("AutomataToIEC1131.printChangeTransitionsAsIL: " + "Could not find " + currEvent.getLabel() + " in automaton " + currAutomaton.getName());
			}

			theHelper.printILComment(pw, "Transitions in " + currAutomaton.getName());
			for (final Iterator<State> stateIt = currAutomaton.statesThatEnableEventIterator(currEvent.getLabel());
			     stateIt.hasNext(); )
			{
			    final State currState = stateIt.next();
			    final int currStateIndex = currState.getSynchIndex();
			    final State toState = currState.nextState(currAutomatonEvent);

			    if (toState == null)
			    {
				throw new IllegalStateException("AutomataToIEC1131.printChangeTransitionsAsIL: " + "Could not find the next state from state " + currState.getName() + " with label " + currEvent.getLabel() + " in automaton " + currAutomaton.getName());
			    }

			    final int toStateIndex = toState.getSynchIndex();

			    if (currState != toState)
			    {
				theHelper.printILCommand(pw, "LD", "q_" + currAutomatonIndex + "_" + currStateIndex);
				theHelper.printILCommand(pw, "JMPCN", "trans_after_e_" + currEventIndex + "_q_" + currAutomatonIndex + "_" + currStateIndex);
				theHelper.printILCommand(pw, "S", "q_" + currAutomatonIndex + "_" + toStateIndex);
				theHelper.printILCommand(pw, "R", "q_" + currAutomatonIndex + "_" + currStateIndex);
				theHelper.printILCommand(pw, "JMP", "trans_after_e_" + currEventIndex + "_a_" + currAutomatonIndex);
			    }
			    else
			    {
				theHelper.printILComment(pw, "q_" + currAutomatonIndex + "_" + currStateIndex + "  has e_" + currEventIndex + " as self loop, no transition");
			    }

			    theHelper.printILLabel(pw, "trans_after_e_" + currEventIndex + "_q_" + currAutomatonIndex + "_" + currStateIndex);
			}

			theHelper.printILLabel(pw, "trans_after_e_" + currEventIndex + "_a_" + currAutomatonIndex);
		    }
		}

		theHelper.printILLabel(pw, "trans_after_e_" + currEventIndex);
	    }
	}
    }

    void printComputeSingleEnabledEventAsIL(final PrintWriter pw)
	throws Exception
    {
	theHelper.printILComment(pw, "Make sure only one event is enabled");
	theHelper.printILComment(pw, "Priority is given to uncontrollable events");

	// Iterate over all events and compute which events that are enabled
	for (final Iterator<LabeledEvent> alphIt = allEvents.uncontrollableEventIterator();
	     alphIt.hasNext(); )
	{
	    final LabeledEvent currEvent = alphIt.next();
	    final int currEventIndex = currEvent.getIndex();

	    theHelper.printILComment(pw, "Enable condition for event \"" + currEvent.getLabel() + "\"");
	    theHelper.printILCommand(pw, "LD", "e_" + currEventIndex);
	    theHelper.printILCommand(pw, "ANDN", "enabledEvent");
	    theHelper.printILCommand(pw, "ST", "e_" + currEventIndex);
	    theHelper.printILCommand(pw, "OR", "enabledEvent");
	    theHelper.printILCommand(pw, "ST", "enabledEvent");
	}

	for (final Iterator<LabeledEvent> alphIt = allEvents.controllableEventIterator();
	     alphIt.hasNext(); )
	{
	    final LabeledEvent currEvent = alphIt.next();
	    final int currEventIndex = currEvent.getIndex();

	    theHelper.printILComment(pw, "Enable condition for event \"" + currEvent.getLabel() + "\"");
	    theHelper.printILCommand(pw, "LD", "e_" + currEventIndex);
	    theHelper.printILCommand(pw, "ANDN", "enabledEvent");
	    theHelper.printILCommand(pw, "ST", "e_" + currEventIndex);
	    theHelper.printILCommand(pw, "OR", "enabledEvent");
	    theHelper.printILCommand(pw, "ST", "enabledEvent");
	}
    }

    void printComputeSingleEnabledEventAsST(final PrintWriter pw)
	throws Exception
    {
	pw.println("\n\t(* Make sure only one event is enabled *)");
	pw.println("\t(* Priority is given to uncontrollable events *)");

	// Iterate over all events and compute which events that are enabled
	for (final Iterator<LabeledEvent> alphIt = allEvents.uncontrollableEventIterator();
	     alphIt.hasNext(); )
	{
	    final LabeledEvent currEvent = alphIt.next();
	    final int currEventIndex = currEvent.getIndex();

	    pw.println("\n\t(* Enable condition for event \"" + currEvent.getLabel() + "\" *)");
	    pw.println("\te_" + currEventIndex + " = " + "e_" + currEventIndex + " AND (NOT enabledEvent);");
	    pw.println("\tenabledEvent = " + "enabledEvent OR " + "e_" + currEventIndex + ";");
	}

	for (final Iterator<LabeledEvent> alphIt = allEvents.controllableEventIterator();
	     alphIt.hasNext(); )
	{
	    final LabeledEvent currEvent = alphIt.next();
	    final int currEventIndex = currEvent.getIndex();

	    pw.println("\n\t(* Enable condition for event \"" + currEvent.getLabel() + "\" *)");
	    pw.println("\te_" + currEventIndex + " = " + "e_" + currEventIndex + " AND (NOT enabledEvent);");
	    pw.println("\tenabledEvent = " + "enabledEvent OR " + "e_" + currEventIndex + ";");
	}
    }

    void printDoActionsAsIL(final PrintWriter pw)
	throws Exception
    {
	final Actions theActions = theProject.getActions();
	final Signals outputSignals = theProject.getOutputSignals();

	theHelper.printILComment(pw, "The actions");

	// Iterate over all events and compute which events that are externally enabled
	for (final Iterator<LabeledEvent> alphIt = allEvents.iterator(); alphIt.hasNext(); )
	{
	    final LabeledEvent currEvent = alphIt.next();
	    final int currEventIndex = currEvent.getIndex();

	    theHelper.printILLabel(pw, "do_e_" + currEventIndex);
	    theHelper.printILComment(pw, "Action for event " + currEvent.getLabel());

	    if (theActions.hasAction(currEvent.getLabel()))
	    {
		final Action currAction = theActions.getAction(currEvent.getLabel());

		for (final Iterator<Command> it = currAction.commandIterator(); it.hasNext(); )
		{
		    final Command currCommand = it.next();

		    if (currCommand.getValue())
		    {
			theHelper.printILCommand(pw, "LD", "TRUE");
		    }
		    else
		    {
			theHelper.printILCommand(pw, "LD", "FALSE");
		    }

		    if (!outputSignals.hasSignal(currCommand.getLabel()))
		    {
			throw new SupremicaException("Could not find output signal " + currCommand.getLabel());
		    }

		    final Signal currSignal = outputSignals.getSignal(currCommand.getLabel());

		    theHelper.printILCommand(pw, "ST", "so_" + currSignal.getPort());
		}
	    }

	    theHelper.printILCommand(pw, "JMP", "after_do_e_" + currEventIndex);
	}
    }

    void printCheckConditionsAsIL(final PrintWriter pw)
    {
	final Controls theControls = theProject.getControls();
	final Signals inputSignals = theProject.getInputSignals();

	theHelper.printILComment(pw, "The conditions");

	// Iterate over all events and compute which events that are externally enabled
	for (final Iterator<LabeledEvent> alphIt = allEvents.iterator(); alphIt.hasNext(); )
	{
	    final LabeledEvent currEvent = alphIt.next();
	    final int currEventIndex = currEvent.getIndex();

	    theHelper.printILLabel(pw, "check_e_" + currEventIndex);
	    theHelper.printILComment(pw, "Condition for event " + currEvent.getLabel());
	    theHelper.printILCommand(pw, "LD", "e_" + currEventIndex);
	    theHelper.printILCommand(pw, "JMPCN", "after_check_e_" + currEventIndex);

	    if (theControls.hasControl(currEvent.getLabel()))
	    {
		final Control currControl = theControls.getControl(currEvent.getLabel());
		int i = 0;

		for (final Iterator<Condition> it = currControl.conditionIterator();
		     it.hasNext(); )
		{
		    final Condition currCondition = it.next();

		    if (!inputSignals.hasSignal(currCondition.getLabel()))
		    {
			final String errMessage = "Could not find input signal " + currCondition.getLabel();

			logger.error(errMessage);

			throw new IllegalStateException(errMessage);
		    }

		    final Signal currSignal = inputSignals.getSignal(currCondition.getLabel());

		    theHelper.printILCommand(pw, "LD", "si_" + currSignal.getPort());

		    if (currCondition.doInvert())
		    {
			theHelper.printILCommand(pw, "JMPCN", "check_e_" + currEventIndex + "_" + i);
			theHelper.printILCommand(pw, "LD", "FALSE");
			theHelper.printILCommand(pw, "ST", "e_" + currEventIndex);
			theHelper.printILCommand(pw, "JMP", "after_check_e_" + currEventIndex);
			theHelper.printILLabel(pw, "check_e_" + currEventIndex + "_" + i);
		    }
		    else
		    {
			theHelper.printILCommand(pw, "JMPC", "check_e_" + currEventIndex + "_" + i);
			theHelper.printILCommand(pw, "LD", "FALSE");
			theHelper.printILCommand(pw, "ST", "e_" + currEventIndex);
			theHelper.printILCommand(pw, "JMP", "after_check_e_" + currEventIndex);
			theHelper.printILLabel(pw, "check_e_" + currEventIndex + "_" + i);
		    }

		    i++;
		}
	    }

	    theHelper.printILCommand(pw, "JMP", "after_check_e_" + currEventIndex);
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
    public void serializeStructuredText(final PrintWriter pw)
	throws Exception
    {
	//initialize();
	printBeginProgram(pw);
	printBeginVariables(pw);
	printSignalVariables(pw);
	printEventVariables(pw);
	printStateVariables(pw,2);
	printEndVariables(pw);
	printSTBegin(pw);
	printInitializationStructureAsST(pw);
	printComputeEnabledEventsAsST(pw);
	printComputeSingleEnabledEventAsST(pw);
	printChangeStateTransitionsAsST(pw);
	printSTEnd(pw);
	printEndProgram(pw);
    }

    public void serializeInstructionList(final PrintWriter pw)
	throws Exception
    {
	//initialize();
	theHelper.printILTimerFunctions(pw);
	printBeginProgram(pw);
	printBeginVariables(pw);
	printSignalVariables(pw);
	printEndVariables(pw);
	printBeginVariables(pw);
	printEventVariables(pw);
	printStateVariables(pw,2);
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
